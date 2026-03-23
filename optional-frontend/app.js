const state = {
    token: null,
    user: null
};

const elements = {
    role: document.getElementById("current-role"),
    user: document.getElementById("current-user"),
    consultant: document.getElementById("current-consultant"),
    directoryOutput: document.getElementById("directory-output"),
    timeOutput: document.getElementById("time-output"),
    billingOutput: document.getElementById("billing-output"),
    activityLog: document.getElementById("activity-log"),
    timeConsultantId: document.getElementById("time-consultant-id"),
    timeProjectId: document.getElementById("time-project-id"),
    billingClientId: document.getElementById("billing-client-id"),
    billingProjectId: document.getElementById("billing-project-id"),
    selectedEntryId: document.getElementById("selected-entry-id"),
    entryNote: document.getElementById("entry-note")
};

const today = new Date().toISOString().slice(0, 10);
document.getElementById("time-work-date").value = today;
document.getElementById("billing-start-date").value = today.slice(0, 8) + "01";
document.getElementById("billing-end-date").value = today;

document.getElementById("login-form").addEventListener("submit", login);
document.getElementById("time-entry-form").addEventListener("submit", createTimeEntry);
document.getElementById("billing-form").addEventListener("submit", generateBilling);
document.getElementById("load-time-entries").addEventListener("click", loadTimeEntries);
document.getElementById("submit-entry").addEventListener("click", () => entryAction("submit"));
document.getElementById("approve-entry").addEventListener("click", () => entryAction("approve"));
document.getElementById("reject-entry").addEventListener("click", () => entryAction("reject"));
document.getElementById("load-billing-periods").addEventListener("click", loadBillingPeriods);
document.getElementById("load-billing-summaries").addEventListener("click", loadBillingSummaries);

document.querySelectorAll("[data-load]").forEach((button) => {
    button.addEventListener("click", () => loadDirectory(button.dataset.load));
});

async function login(event) {
    event.preventDefault();
    const email = document.getElementById("login-email").value;
    const password = document.getElementById("login-password").value;

    const response = await request("/user-api/api/v1/auth/login", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({email, password})
    }, false);

    state.token = response.accessToken;
    state.user = response;
    elements.role.textContent = response.role;
    elements.user.textContent = `${response.fullName} (${response.email})`;
    elements.consultant.textContent = response.consultantId || "N/A";

    if (response.consultantId) {
        elements.timeConsultantId.value = response.consultantId;
    }

    log("Signed in", response);
}

async function loadDirectory(type) {
    const pathMap = {
        consultants: "/user-api/api/v1/consultants",
        clients: "/user-api/api/v1/clients",
        projects: "/user-api/api/v1/projects",
        assignments: buildAssignmentPath()
    };

    const data = await request(pathMap[type]);
    const items = data.content || [];

    elements.directoryOutput.innerHTML = "";
    if (!items.length) {
        elements.directoryOutput.innerHTML = `<div class="list-item"><strong>No ${type} found.</strong></div>`;
        return;
    }

    items.forEach((item) => {
        const div = document.createElement("div");
        div.className = "list-item";
        div.innerHTML = renderDirectoryItem(type, item);
        div.addEventListener("click", () => fillFromDirectory(type, item));
        elements.directoryOutput.appendChild(div);
    });

    log(`Loaded ${type}`, items);
}

function buildAssignmentPath() {
    if (state.user?.consultantId) {
        return `/user-api/api/v1/assignments?consultantId=${encodeURIComponent(state.user.consultantId)}`;
    }
    return "/user-api/api/v1/assignments";
}

function renderDirectoryItem(type, item) {
    if (type === "consultants") {
        return `<strong>${item.fullName}</strong><small>${item.id}<br>${item.jobTitle} · ${item.status}</small>`;
    }
    if (type === "clients") {
        return `<strong>${item.name}</strong><small>${item.id}<br>${item.contactEmail}</small>`;
    }
    if (type === "projects") {
        return `<strong>${item.name}</strong><small>${item.id}<br>${item.code} · ${item.status}</small>`;
    }
    return `<strong>${item.consultantName} → ${item.projectName}</strong><small>${item.id}<br>${item.assignedRole} · ${item.allocationPercentage}%</small>`;
}

function fillFromDirectory(type, item) {
    if (type === "consultants") {
        elements.timeConsultantId.value = item.id;
    }
    if (type === "clients") {
        elements.billingClientId.value = item.id;
    }
    if (type === "projects") {
        elements.timeProjectId.value = item.id;
        elements.billingProjectId.value = item.id;
    }
    if (type === "assignments") {
        elements.timeConsultantId.value = item.consultantId;
        elements.timeProjectId.value = item.projectId;
    }
}

async function createTimeEntry(event) {
    event.preventDefault();
    const payload = {
        consultantId: elements.timeConsultantId.value.trim(),
        projectId: elements.timeProjectId.value.trim(),
        workDate: document.getElementById("time-work-date").value,
        hours: Number(document.getElementById("time-hours").value),
        description: document.getElementById("time-description").value,
        billable: document.getElementById("time-billable").checked
    };

    const response = await request("/timesheet-api/api/v1/time-entries", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(payload)
    });

    elements.selectedEntryId.value = response.id;
    log("Created time entry", response);
    await loadTimeEntries();
}

async function loadTimeEntries() {
    const query = state.user?.consultantId
        ? `?consultantId=${encodeURIComponent(state.user.consultantId)}`
        : "";
    const data = await request(`/timesheet-api/api/v1/time-entries${query}`);
    const items = data.content || [];
    elements.timeOutput.innerHTML = "";

    items.forEach((item) => {
        const div = document.createElement("div");
        div.className = "list-item";
        div.innerHTML = `<strong>${item.projectName} · ${item.hours}h</strong><small>${item.id}<br>${item.workDate} · ${item.status}</small>`;
        div.addEventListener("click", () => {
            elements.selectedEntryId.value = item.id;
        });
        elements.timeOutput.appendChild(div);
    });

    if (!items.length) {
        elements.timeOutput.innerHTML = `<div class="list-item"><strong>No time entries found.</strong></div>`;
    }

    log("Loaded time entries", items);
}

async function entryAction(action) {
    const entryId = elements.selectedEntryId.value.trim();
    if (!entryId) {
        log("Action blocked", {message: "Select a time entry first."});
        return;
    }

    let options = {method: "POST"};
    if (action === "approve") {
        options.headers = {"Content-Type": "application/json"};
        options.body = JSON.stringify({note: elements.entryNote.value.trim() || null});
    }
    if (action === "reject") {
        options.headers = {"Content-Type": "application/json"};
        options.body = JSON.stringify({reason: elements.entryNote.value.trim() || "Needs clarification"});
    }

    const response = await request(`/timesheet-api/api/v1/time-entries/${entryId}/${action}`, options);
    log(`Entry ${action}d`, response);
    await loadTimeEntries();
}

async function generateBilling(event) {
    event.preventDefault();
    const payload = {
        clientId: elements.billingClientId.value.trim(),
        projectId: elements.billingProjectId.value.trim() || null,
        startDate: document.getElementById("billing-start-date").value,
        endDate: document.getElementById("billing-end-date").value,
        currency: document.getElementById("billing-currency").value.trim()
    };

    const response = await request("/billing-api/api/v1/billing/generate", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(payload)
    });

    log("Generated billing", response);
    await loadBillingSummaries();
}

async function loadBillingPeriods() {
    const data = await request("/billing-api/api/v1/billing-periods");
    const items = data.content || [];
    renderList(elements.billingOutput, items, (item) =>
        `<strong>${item.clientName}</strong><small>${item.id}<br>${item.startDate} to ${item.endDate} · ${item.status}</small>`
    );
    log("Loaded billing periods", items);
}

async function loadBillingSummaries() {
    const clientId = elements.billingClientId.value.trim();
    const query = clientId ? `?clientId=${encodeURIComponent(clientId)}` : "";
    const data = await request(`/billing-api/api/v1/billing/summaries${query}`);
    const items = data.content || [];
    renderList(elements.billingOutput, items, (item) =>
        `<strong>${item.projectName} · ${item.consultantName}</strong><small>${item.id}<br>${item.approvedHours}h · ${item.totalAmount} ${item.currency}</small>`
    );
    log("Loaded billing summaries", items);
}

function renderList(target, items, template) {
    target.innerHTML = "";
    if (!items.length) {
        target.innerHTML = `<div class="list-item"><strong>No records found.</strong></div>`;
        return;
    }
    items.forEach((item) => {
        const div = document.createElement("div");
        div.className = "list-item";
        div.innerHTML = template(item);
        target.appendChild(div);
    });
}

async function request(url, options = {}, authRequired = true) {
    if (authRequired && !state.token) {
        throw new Error("Sign in first.");
    }

    const headers = {...(options.headers || {})};
    if (authRequired && state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    const response = await fetch(url, {...options, headers});
    const text = await response.text();
    const payload = text ? JSON.parse(text) : null;

    if (!response.ok) {
        log("Request failed", payload || {status: response.status});
        throw new Error(payload?.message || `Request failed with status ${response.status}`);
    }

    return payload;
}

function log(title, payload) {
    elements.activityLog.textContent = `${title}\n\n${JSON.stringify(payload, null, 2)}`;
}
