import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import javax.json.stream.JsonGenerator;
import java.io.IOException;
import java.util.Scanner;

/**
 * @author Andres Ardila
 */
public class Program {

    final static String[] measurands =
            { "PM10", "Ruß", "Stickstoffdioxid", "Benzol", "Kohlenmonoxid", "Ozon", "Schwefeldioxid"};

    public static void main(String[] args) throws IOException {
        final String urlFormat =
                "http://www.stadtentwicklung.berlin.de/umwelt/luftqualitaet/de/messnetz/tageswerte/download/%s.html";

        Scanner reader = new Scanner(System.in);
        System.out.print("> ");
        String date = reader.nextLine();

        final String url = String.format(urlFormat, date);

        Document doc = Jsoup.connect(url).get();
        //TODO get/check output is as expected

        Element table = doc.select("table.datenhellgrauklein").first();

        //TODO check table dimensions are as expected (num columns & rows)

        //TODO get & check header

        JsonGenerator jsonGenerator;//TODO = Json.createGenerator

        for (Element row : table.getElementsByTag("tr")) {
            Elements cells = row.getElementsByTag("td");

            if (!cells.first().text().matches("^\\d{3}&nbsp;\\w*$"))
                continue;

            String[] sensorTokens = cells.first().text().split("&nbsp;");

            jsonGenerator.writeStartObject()
                    .write("date", date)
                    .writeStartObject("sensor")
                    .write("id", sensorTokens[0].substring(0,3))
                    .write("name", sensorTokens[1])
                    .writeEnd()
                    .writeStartArray("measuremnts");

            for (int i = 0; i < row.children().size(); i++) {
                jsonGenerator.writeStartObject()
                        .write(measurands[i], row.children().indexOf(i+1))
                        .writeEnd();
            }

            jsonGenerator
                    .writeEnd() //end array
                    .writeEnd(); //end root object
        }
    }
}
