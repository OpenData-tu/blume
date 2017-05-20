# blume-api

A RESTful API to retrieve particulate matter sensor data from the [BLUME sensor network](http://www.stadtentwicklung.berlin.de/umwelt/luftqualitaet/luftdaten/index.shtml) in Berlin, Germany.

## Docker

```
$ docker build -t blume-api .
$ docker run -p 4567:4567 blume
```

## API

* `GET /stations` Retrieves the list of the sensor stations (*Messstationen*).
* `GET /daily/:date` Gets the daily readings for the given `date` (an ISO-8601 compliant date)
