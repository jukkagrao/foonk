## Foonk

A streaming media server based on akka-streams.

### Configuration

All settings currently here:
`src/main/resources/aplication.conf`

For connecting a source client (based on libshout) it uses port + 1.
(On this port running a proxy for ICE protocol handling) 

### Building

Just run `sbt assembly` command to create executable jar file.
