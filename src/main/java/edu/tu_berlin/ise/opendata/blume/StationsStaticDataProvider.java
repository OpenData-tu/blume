package edu.tu_berlin.ise.opendata.blume;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.tu_berlin.ise.opendata.blume.model.Coordinates;
import edu.tu_berlin.ise.opendata.blume.model.Station;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by artichoke on 7/5/17.
 */
public class StationsStaticDataProvider {

    private static final String RESOURCE_FILENAME = "stations.json";

    private static HashMap<String, Station> StationsHashMap;

    static {
        try {
            String stationsResourceFileContent = getResourceFileAsString("stations.json", "UTF-8");
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Coordinates.class, new CoordinatesDeserializer())
                    .create();
            Station[] stations = gson.fromJson(stationsResourceFileContent, Station[].class);
            StationsHashMap = new HashMap<>(stations.length);
            for (int i = 0; i < stations.length; i++) {
                StationsHashMap.put(stations[i].id, stations[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getResourceFileAsString(String path, String charset) throws IOException {
        // couldn't get simpler one-liners with ClassLoader.getResource() to work in a static method from a fat JAR
        // https://stackoverflow.com/a/35446009/5846378
        // seems to indicate that this is the fastest way to read a file (and works)
        // could be improved in future if needed
        InputStream inputStream = BlumeWrapper.class.getClassLoader().getResourceAsStream(path);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        inputStream.close();
        // StandardCharsets.UTF_8.name() > JDK 7
        return result.toString(charset);
    }

    public static Station get(String stationId) {
        return StationsHashMap.get(stationId);
    }
}
