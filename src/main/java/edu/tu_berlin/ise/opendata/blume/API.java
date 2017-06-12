package edu.tu_berlin.ise.opendata.blume;

import spark.Spark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static spark.Spark.get;
import static spark.Spark.halt;

/**
 * @author Andres Ardila
 */
public class API {

    static {
        //register error handler to display uncaught exceptions
        Spark.exception(Exception.class, (exception, request, response) -> {
            exception.printStackTrace();
        });
    }

    public static void main(String[] args) throws IOException {

        get("/", (request, response) ->
                "hello!<br/><br/><pre>GET /stations<br/>GET /daily/:date (e.g. 2017-05-16)</pre>");

        get("/stations", (request, response) -> {

            String json = "";

            try {
                json = getResourceFileAsString("stations.json", "UTF-8");
            }
            catch (IOException ex) {
                halt(500);
            }

            response.type("application/json; charset=utf-8");
            return json;
        });

        get("/daily/:date", (request, response) -> {

            String param = request.params(":date");
            if (param == null || param.length() == 0)
                halt(400);

            LocalDate date = java.time.LocalDate.now();
            try {
                date = LocalDate.parse(param);
            }
            catch (DateTimeParseException ex){
                halt(400);
            }

            response.type("application/json; charset=utf-8");
            return BlumeWrapper.getDailyMeasurements(date);
        });
    }

    private static String getResourceFileAsString(String path, String charset) throws IOException {
        // couldn't get simpler one-liners wtih ClassLoader.getResource() to work in a static method from a fat JAR
        // https://stackoverflow.com/a/35446009/5846378
        // seems to indicate that this is the fastest way to read a file (and works)
        // could be improved in future if needed
        InputStream inputStream = API.class.getClassLoader().getResourceAsStream(path);
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
}
