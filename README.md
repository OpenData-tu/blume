# blume-api

An importer to retrieve particulate matter sensor data from the [BLUME sensor network](http://www.stadtentwicklung.berlin.de/umwelt/luftqualitaet/luftdaten/index.shtml) in Berlin, Germany.

## Docker

```
$ docker run --env KAFKA_HOST=[hostname|ip]:9092 aardila/blume "2017-05-22"
```

### Environment Variables
- `KAFKA_HOST` (mandatory)

### Runtime Arguments
1. An ISO-8601 date representing the date for which data is to be imported (mandatory)
