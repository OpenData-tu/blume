# blume

An importer to retrieve particulate matter sensor data from the [BLUME sensor network](http://www.stadtentwicklung.berlin.de/umwelt/luftqualitaet/luftdaten/index.shtml) in Berlin, Germany.

## Disclaimer

Please note that this application is only intended as a proof of concept. It accesses data from the berlin.de website. You should check that you are allowed to access and use this data. Also, I am not a lawyer and this does not constitute legal advice :)

## Docker

```
$ docker run \
--env KAFKA_HOST=[hostname|ip]:9092 \
--env KAFKA_TOPIC=topic_override
 aardila/blume:[version] "2017-05-22"
```

### Environment Variables
- `KAFKA_HOST` (mandatory)
- `KAFKA_TOPIC` (optional --defaults to "BLUME" if not set)

### Runtime Arguments
1. An ISO-8601 date representing the date for which data is to be imported (mandatory)

### Version

See https://hub.docker.com/r/aardila/blume/tags/
