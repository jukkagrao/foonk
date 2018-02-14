## Foonk

A streaming media server based on akka-http. Tested with mp3/aac streams only.


### Building

Just run `sbt assembly` command to create executable jar file or download jar file from releases.

### Configuration

All settings are available here:
`src/main/resources/application.conf`

For connecting a source client (based on libshout) it uses icy-port setting.
(On this port is running a connector for Icecast source protocol handling)

When it's necessary to use external config file you can put your settings to `/conf/foonk.conf` then this file will be added to the container with the application. 

### Running

- With Docker:
  If you have `docker-compose` installed you can run `docker-compose up` to run an application within docker container.
- Without:
  To run the application you must have Java installed and then just type `java -Dconfig.file=path/to/foonk.conf -jar path/to/foonk.jar` in command line. 

### API documentation

This documentation is available in OpenAPI (Swagger 2) format, its possible to download docs from running application by accessing to http://app-host:8000/api-docs/swagger.json or in `docs/api` folder.