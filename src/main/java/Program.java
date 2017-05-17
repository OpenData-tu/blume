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

    private final static String[] MEASURANDS =
            {
                    "Partikel-PM10", "Ruß", "Stickstoffdioxid", "Benzol",
                    "Kohlenmonoxid", "Ozon", "Schwefeldioxid"
            };

    private static final int EXPECTED_FREQUENCY_CELLS = 14;

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
        Element frequenciesRow = rows.eq(1).first();
        Elements frequencyCells = frequenciesRow.children();
        frequencyCells.remove(0); //skip first cell

        if (frequencyCells.size() != EXPECTED_FREQUENCY_CELLS) {
            System.out.println("Unexpected number of frequency cells. Expected: " + EXPECTED_FREQUENCY_CELLS + ", found: " + frequencyCells.size());
        }

        for (Element cell : frequencyCells) {
            String headerText = cell.text().trim();

            if (!headerText.isEmpty())
                frequencies.add(headerText);
        }

        jsonGenerator.writeStartObject() //root object
                .write("date", date)
                .writeStartArray("stations");

        for (int i = 0; i <= rows.size(); i++) {
            Element row = rows.get(2);
            Elements cells = row.getElementsByTag("td");

//            if (!cells.first().text().matches("^\\d{3}[ ][a-zA-ZäöüÄÖÜß]+"))
//                continue; TODO regex not working :\

            Element stationCell = cells.remove(0);
            String[] stationTokens = stationCell.text().split("\u00a0");

            if (stationTokens.length != 2) {
                System.out.println("Station tokens != 2");
                return;
            }


            jsonGenerator.writeStartObject()
                        .write("id", stationTokens[0])
                        .write("name", stationTokens[1])
                        .writeStartArray("measurements");

            for (int j = 0; j < MEASURANDS.length; j++) {
                String measurand = MEASURANDS[j];

                for (int offset = 0; offset < 2; offset++){
                    int index = (j*2) + offset;
                    String frequency = frequencies.get(index);
                    String value = cells.eq(index).text();

                    if (value.equals("---"))
                        value = "";

                    jsonGenerator.writeStartObject()
                            .write("measurand", measurand)
                            .write("valueType", frequency)
                            .write("value", value)
                    .writeEnd();
                }
            }

            jsonGenerator.writeEnd(); //end measurements array
            jsonGenerator.writeEnd(); //end station object
        }

        jsonGenerator.writeEnd(); //end sensor array
        jsonGenerator.writeEnd(); //end root object
        jsonGenerator.close();

        System.out.println(stringWriter.toString());
    }
}
