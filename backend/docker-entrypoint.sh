#!/bin/bash

python3 manage.py makemigrations;
python3 manage.py migrate;

if [[ ! -d /opt/rnatango/supervisor ]];
then
    mkdir -p /opt/rnatango/supervisor
fi

supervisord;


rm /opt/rnatango/static/* -R
cd /opt/rnatango/frontend;
npm install;

cd /opt/rnatango/build/;
bash ./build_front.sh;

cd /opt/rnatango/;
python3 manage.py collectstatic --no-input;
cp /opt/rnatango/public/* /opt/rnatango/static/
if [[ ! -d /opt/rnatango/supervisor ]];
then
    mkdir -p /opt/rnatango/supervisor
fi

if [[ ! -d /opt/rnatango/logs/celery ]];
then
    mkdir -p /opt/rnatango/logs/celery
fi

if [[ ! -d /opt/rnatango/ssl ]];
then
    mkdir -p /opt/rnatango/ssl
fi
echo "from django.contrib.auth import get_user_model; User = get_user_model(); User.objects.create_superuser('admin', 'admin@admin.com', 'admin') if User.objects.all().count()==0 else None" | python3 manage.py shell;


while :
do
  sleep 10
done
