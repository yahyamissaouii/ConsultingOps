FROM nginx:1.27-alpine
COPY optional-frontend/index.html /usr/share/nginx/html/index.html
COPY optional-frontend/styles.css /usr/share/nginx/html/styles.css
COPY optional-frontend/app.js /usr/share/nginx/html/app.js
COPY optional-frontend/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
