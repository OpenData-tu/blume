package edu.tu_berlin.ise.opendata.blume;

import spark.Spark;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        get("/", (request, response) -> "hello!");

        get("/stations", (request, response) -> {
            response.type("application/json");
            URL resource = API.class.getClassLoader().getResource("messstationen.json");
            Path path = Paths.get(resource.toURI());
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes);
        });

        get("daily/:date", ((request, response) -> {

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

            response.type("application/json");
            return BlumeWrapper.getDailyMeasurements(date);
        }));
    }
}
