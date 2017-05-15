import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Andres Ardila
 */
public class Program {

    final static String[] measurands =
            {
                    "PM10", "Ruß", "Stickstoffdioxid", "Benzol",
                    "Kohlenmonoxid", "Ozon", "Schwefeldioxid", "Benzol",
                    "Kohlenmonoxid", "Ozon", "Schwefeldioxid"
            };

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

        StringWriter stringWriter = new StringWriter();
        JsonGenerator jsonGenerator = Json.createGenerator(stringWriter);

        Elements rows = table.getElementsByTag("tr");

        //ArrayList<String> measurands = new ArrayList<String>();
        ArrayList<String> frequencies = new ArrayList<>();
        Element fRow = rows.eq(1).first();

        for (int i = 1; i <= fRow.children().size(); i++) {
            frequencies.add(fRow.children().eq(i).text());
        }

        for (Element row : rows) {
            Elements cells = row.getElementsByTag("td");

//            if (!cells.first().text().matches("^\\d{3}[ ][a-zA-ZäöüÄÖÜß]+"))
//                continue; TODO regex not working :\

            String[] sensorTokens = cells.first().text().split(" ");

            if (sensorTokens.length != 2)
                continue; //HACK

            jsonGenerator.writeStartObject()
                    .write("date", date)
                    .writeStartObject("sensor")
                    .write("id", sensorTokens[0].substring(0,3))
                    .write("name", sensorTokens[1])
                    .writeEnd()
                    .writeStartArray("measuremnts");

            for (int i = 0; i < cells.size(); i++) {
                //String key = measurands[i-1]; //TODO
                String key = frequencies.get(i);
                String value = cells.eq(i).text();

                if (value == "---") value = "";

                jsonGenerator.writeStartObject()
                        .write(key, value)
                        .writeEnd();
            }

            jsonGenerator
                    .writeEnd() //end array
                    .writeEnd(); //end root object
        }

        jsonGenerator.flush();
        System.out.println(stringWriter.toString());
    }
}
