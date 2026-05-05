<div align="center"><img  src="./logornatango.svg" alt="logo"></div>
<br/>

[RNAtango](https://rnatango.cs.put.poznan.pl) is a web server to study 3D RNA structures through torsion angles. Depending on the selected scenario, users can explore the distribution of torsion angles in a single RNA structure or its fragment, compare the RNA model(s) with the native structure, or perform a comparative analysis in a set of models. The comparison procedure applies MCQ and LCS-TA metrics to assess RNA angular similarity.

## Installation

It is needed to declare _.env_ file in parent directory to determine security settings. Create _.env_ file from following template:

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
git submodule update --init
docker compose up --build
```

> **Note:** If a `docker-compose.override.yml` is present, Docker Compose merges it automatically. The development override file is named `docker-compose.dev.yml` to avoid accidental activation in production.

## Development

### Run a development instance

The production `docker-compose.yml` serves an optimised static export of the frontend behind nginx, which makes debugging difficult (React errors are minified).

To run with live-reload and full error messages, start the **dev profile**:

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml --profile dev up --build db rabbit maxit web frontend-dev
```

This does the following:

- Runs the backend (`web`) with its port `8080` exposed to the host.
- Runs a `frontend-dev` container that binds `./rnatango-frontend` and launches `next dev` on port `3000`.
- Sets `NEXT_PUBLIC_SERVER_URL=http://localhost:8080/api` and `NEXT_PUBLIC_SERVER_WEB_SOCKET_URL=ws://localhost:8080/api/ws` so the dev frontend talks to the local backend directly (bypassing nginx).

Then open [http://localhost:3000](http://localhost:3000). Changes to frontend source are reflected instantly.

### Debugging common issues

#### Minified React error

If you see `Error #418` (or other minified error numbers) in the browser console, it means a hydration mismatch was caught in production. A hydration mismatch happens when the server-rendered HTML does not match the first client render.

**Fix:** Use the dev profile above — `next dev` shows the full plain-text error and a stack trace. The most common cause in this project is render-time branching on `useMediaQuery()` (responsive design hooks), which produces different DOM trees on server vs client. The fix is to use CSS media queries instead (see `Header.tsx` for the pattern).

#### CORS errors

When running the frontend outside Docker (plain `npm run dev` on the host), the backend must be reachable. Either:

- Add `ports: ["8080:8080"]` to the `web` service (already present in `docker-compose.dev.yml`), or
- Use the full stack behind nginx at `http://localhost/api`.

#### RabbitMQ noise in tests

Backend tests log non-fatal `Failed to check/redeclare auto-delete queue(s)` errors because RabbitMQ is not running during unit tests. These can be ignored.

## Swagger API documentation

RNAtango serves access to [API declaration](https://rnatango.cs.put.poznan.pl/api/swagger-ui/index.html) for personal use.

## Websocket

There are three websocket endpoints that enable user to pool task status.

- wss://rnatango.cs.put.poznan.pl/api/ws/single
- wss://rnatango.cs.put.poznan.pl/api/ws/manymany
- wss://rnatango.cs.put.poznan.pl/api/ws/onemany

They require request in format json:

```json
{
  "hashId": "<taskHashId>"
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
  "reason": "<error>"
}
```
