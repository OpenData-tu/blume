package edu.tu_berlin.ise.opendata.blume;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import edu.tu_berlin.ise.opendata.blume.model.DailyMeasurements;

import java.time.LocalDate;

/**
 * Created by aardila on 7/5/17.
 */
public class Serializer {

    public static JsonElement serializeDailyMeasurements(DailyMeasurements dailyMeasurements) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                .registerTypeAdapter(DailyMeasurements.class, new DailyMeasurementsSerializer())
                .serializeNulls()
                .create();
        JsonElement jsonElement = gson.toJsonTree(dailyMeasurements);
        return jsonElement;
    }
}
