<div align="center"><img  src="./logornatango.svg" alt="logo"></div>
<br/>

[RNAtango](https://rnatango.cs.put.poznan.pl) is a web server to study 3D RNA structures through torsion angles. Depending on the selected scenario, users can explore the distribution of torsion angles in a single RNA structure or its fragment, compare the RNA model(s) with the native structure, or perform a comparative analysis in a set of models. The comparison procedure applies MCQ and LCS-TA metrics to assess RNA angular similarity.

## Installation
It is needed to declare *.env* file in parent directory to determine security settings. Create *.env* file from following template:
```
POSTGRES_PASSWORD=""
POSTGRES_USER=""
POSTGRES_DB=""
RABBITMQ_DEFAULT_USER=""
RABBITMQ_DEFAULT_PASS=""
VAPID_PUBLIC_KEY=""
VAPID_PRIVATE_KEY=""
```

To deploy server instance it is necessary to install [docker](https://www.docker.com/) with compose plugin. Then in parent directory run:
```bash
docker-compose up --build
```
## Swagger API documentation
RNAtango serves access to [API declaration](https://rnatango.cs.put.poznan.pl/api/swagger-ui/index.html) for personal use.
## Websocket
There are three websocket endpoints that enable user to pool task status.
* wss://rnatango.cs.put.poznan.pl/api/ws/single
* wss://rnatango.cs.put.poznan.pl/api/ws/manymany
* wss://rnatango.cs.put.poznan.pl/api/ws/onemany

They require request in format json:
```json
{
    "hashId":"<taskHashId>"
}
```

If task exists, they return object with status and result url:
```json
{
    "status":"<statusEnum>",
    "progress":<0.0-1.0>,
    "resultUrl":"/<scenario>/<taskHashId>/result"
}
```

If task does not exist or there was an error during processing, they return object with error.
```json
{
    "reason":"<error>"
}
```
