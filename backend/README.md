# RNAtango

## Essentials
To build server with docker you need to install 'docker' and 'docker-compose'

Then to run build docker just run command:

```
./docker-run.sh
```

To run django dev server run:

```
python3 manage.py runserver 0.0.0.0
```

## .env for django server
```.env
POSTGRES_USER=
REDIS_PASSWORD=
POSTGRES_PASSWORD=
POSTGRES_DB=
DEBUG=
PLAUSIBLE_TOKEN=
SECRET_KEY=
```