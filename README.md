# .env
```
POSTGRES_PASSWORD="zaq1@WSXcde3"
POSTGRES_USER="rnatango"
POSTGRES_DB="rnatango"

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