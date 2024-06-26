FROM nginx:1.18
RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y certbot python-certbot-nginx bash cron 

RUN rm /etc/nginx/conf.d/default.conf
COPY custom_50x.html /usr/share/nginx/html/
COPY custom_404.html /usr/share/nginx/html/
RUN mkdir -p /var/letsencrypt
COPY nginx.conf /etc/nginx/conf.d/default.conf
# RUN certbot certonly --webroot --webroot-path=/var/letsencrypt --email bartosz.adamczyk@student.put.poznan.pl --agree-tos --no-eff-email -d rnasolo.cs.put.poznan.pl
#RUN certbot --nginx --email bartosz.adamczyk@student.put.poznan.pl -d rnasolo.cs.put.poznan.pl --agree-tos --redirect
USER root
#EXPOSE 8080
