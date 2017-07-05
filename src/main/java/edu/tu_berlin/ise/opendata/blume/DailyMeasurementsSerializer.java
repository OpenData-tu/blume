package edu.tu_berlin.ise.opendata.blume;

import com.google.gson.*;
import edu.tu_berlin.ise.opendata.blume.model.Coordinates;
import edu.tu_berlin.ise.opendata.blume.model.DailyMeasurements;
import edu.tu_berlin.ise.opendata.blume.model.Measurement;
import edu.tu_berlin.ise.opendata.blume.model.Station;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by aardila on 7/5/17.
 */
class DailyMeasurementsSerializer implements JsonSerializer<DailyMeasurements> {

    @Override
    public JsonElement serialize(DailyMeasurements dailyMeasurements, Type typeOfSrc, JsonSerializationContext context) {

        JsonArray array = new JsonArray();

        for (Map.Entry<Station, Measurement[]> mapEntry: dailyMeasurements.stationMeasurements.entrySet()) {
            Station station = mapEntry.getKey();

            JsonObject rootObject = new JsonObject();
            rootObject.addProperty("source_id", "BLUME"); //TODO get from global config somehow (ctor?)
            rootObject.addProperty("device", station.id);
            rootObject.addProperty("timestamp", dailyMeasurements.date.toString());
            rootObject.add("location", context.serialize(station.coord, Coordinates.class));
            rootObject.addProperty("license", "TODO"); //TODO check license terms for BLUME

            JsonObject sensorsObject = new JsonObject();

            for (Measurement measurement : mapEntry.getValue()) {
                JsonObject sensorObject = new JsonObject();
                sensorObject.addProperty("observation_type", measurement.measurementType);
                sensorObject.addProperty("observation_value", measurement.value);
                sensorObject.addProperty("unit", measurement.unit);
                sensorsObject.add(measurement.measurand, sensorObject);
            }

            rootObject.add("sensors", sensorsObject);
            array.add(rootObject);
        }
        return array;
    }
}
