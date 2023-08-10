FROM nginx:1.18
RUN apt-get update
RUN apt-get install -y certbot python-certbot-nginx bash cron
RUN rm /etc/nginx/conf.d/default.conf
COPY custom_50x.html /usr/share/nginx/html/
COPY custom_404.html /usr/share/nginx/html/
COPY custom_403.html /usr/share/nginx/html/
COPY . /opt/rnatango/nginx
RUN mkdir -p /logs/
