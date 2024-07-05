# .env
```
POSTGRES_PASSWORD=""
POSTGRES_USER=""
POSTGRES_DB=""
RABBITMQ_DEFAULT_USER=""
RABBITMQ_DEFAULT_PASS=""
```
# Websocket

There are three websocket endpoints that enable user to pool task status.
* wss://rnatango.cs.put.poznan.pl/ws/single
* wss://rnatango.cs.put.poznan.pl/ws/manymany [Not available yet]
* wss://rnatango.cs.put.poznan.pl/ws/onemany [Not available yet]

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
    "resultUrl":"/single/<taskHashId>/result"
}
```

If task does not exist or there was an error during processing, they return object with error.
```json
{
    "reason":"<error>"
}
```
