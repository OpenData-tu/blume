package edu.tu_berlin.ise.opendata.blume;

import edu.tu_berlin.ise.opendata.blume.model.DailyMeasurements;
import edu.tu_berlin.ise.opendata.blume.model.Measurement;
import edu.tu_berlin.ise.opendata.blume.model.Station;
import org.jsoup.Connection;
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
import java.util.HashMap;

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
    private static final String MEASUREMENT_UNIT = "µg/m³";

    private static String getDailyUrl(LocalDate date) {
        String formattedDate = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        return String.format(URL_FORMAT, formattedDate);
    }

    public static DailyMeasurements getDailyMeasurements(LocalDate date) throws Exception {

        final String url = getDailyUrl(date);
        Connection.Response response = Jsoup.connect(url).execute();

        if (response.statusCode() == 404) {
            throw new Exception("No data is available for " + date.toString());
        }

        Document doc = response.parse();

        //TODO get/check output is as expected

        Element table = doc.select("table.datenhellgrauklein").first();

        //TODO check table dimensions are as expected (num columns & rows)

        //TODO get & check header

        StringWriter stringWriter = new StringWriter();
        JsonGenerator jsonGenerator = Json.createGenerator(stringWriter);

        Elements rows = table.getElementsByTag("tr");

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

        DailyMeasurements dailyMeasurements = new DailyMeasurements();
        dailyMeasurements.date = date;
        dailyMeasurements.url = url;

        HashMap<Station, Measurement[]> stationMeasurements = new HashMap<>();

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

            String stationId = stationTokens[0];
            String stationName = stationTokens[1];

            Station station = new Station();
            station.id = stationId;
            station.name = stationName;

            ArrayList<Measurement> measurements = new ArrayList<>();

            for (int j = 0; j < MEASURANDS.length; j++) {
                String measurand = MEASURANDS[j];
                Measurement measurement = new Measurement();
                measurement.date = date;
                measurement.station = station;

                for (int offset = 0; offset < 2; offset++){
                    int index = (j*2) + offset;
                    String frequency = frequencies.get(index);
                    String value = cells.eq(index).text();

                    if (value.equals("---"))
                        value = null;

                    measurement.measurand = measurand;
                    measurement.measurementType = frequency;
                    measurement.value = value == null ? null : Double.parseDouble(value);
                    measurement.unit = MEASUREMENT_UNIT;
                }

                measurements.add(measurement);
            }

            stationMeasurements.put(station, measurements.toArray(new Measurement[0]));
        }

        dailyMeasurements.stationMeasurements = stationMeasurements;
        return dailyMeasurements;
    }
}
