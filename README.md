## Foonk

A streaming media server based on akka-http.

### Configuration

All settings here:
`src/main/resources/application.conf`

For connecting a source client (based on libshout) it uses icy-port setting.
(On this port running a proxy for Icecast source protocol handling)

### Building

Just run `sbt assembly` command to create executable jar file.
If you have `docker-compose` installed you can run `docker-compose up` to run it in docker container.
