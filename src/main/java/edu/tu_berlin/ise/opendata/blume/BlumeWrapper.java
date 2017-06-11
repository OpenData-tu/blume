package edu.tu_berlin.ise.opendata.blume;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * @author Andres Ardila
 */
public class BlumeWrapper {

    private final static String[] MEASURANDS =
            {
                    "Partikel-PM10", "Ruß", "Stickstoffdioxid", "Benzol",
                    "Kohlenmonoxid", "Ozon", "Schwefeldioxid"
            };

    private static final int EXPECTED_FREQUENCY_CELLS = 14;
    private static final String URL_FORMAT =
            "http://www.stadtentwicklung.berlin.de/umwelt/luftqualitaet/de/messnetz/tageswerte/download/%s.html";

    public static String getDailyMeasurements(LocalDate date) throws Exception {

        String formattedDate = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        final String url = String.format(URL_FORMAT, formattedDate);

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
                .write("date", date.toString())
                .write("url", url)
                .writeStartArray("stations");

        //skip first two header rows
        for (int i = 2; i < rows.size(); i++) {
            Element row = rows.get(i);
            Elements cells = row.getElementsByTag("td");

//            if (!cells.first().text().matches("^\\d{3}[ ][a-zA-ZäöüÄÖÜß]+"))
//                continue; TODO regex not working :\

            Element stationCell = cells.remove(0);
            String stationText = stationCell.text();
            String[] stationTokens = stationText.split("\u00a0");

            if (stationTokens.length != 2) {
                System.out.println("Station tokens != 2. Input string was '"+ stationText + "'");
                continue;
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

        return stringWriter.toString();
    }
}
