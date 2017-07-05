package edu.tu_berlin.ise.opendata.blume;

import com.google.gson.*;
import edu.tu_berlin.ise.opendata.blume.model.Coordinates;

import java.lang.reflect.Type;

/**
 * Created by aardila on 7/5/17.
 */
class CoordinatesDeserializer implements JsonDeserializer<Coordinates> {

    @Override
    public Coordinates deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonArray array = json.getAsJsonArray();
        Coordinates coordinates = new Coordinates();
        coordinates.latitude = array.get(0).getAsDouble();
        coordinates.longitude = array.get(1).getAsDouble();
        return coordinates;
    }
}
