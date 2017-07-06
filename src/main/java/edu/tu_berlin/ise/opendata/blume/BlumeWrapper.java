package edu.tu_berlin.ise.opendata.blume;

import edu.tu_berlin.ise.opendata.blume.model.DailyMeasurements;
import edu.tu_berlin.ise.opendata.blume.model.Measurement;
import edu.tu_berlin.ise.opendata.blume.model.Station;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * @author Andres Ardila
 */
public class BlumeWrapper {


    private final static String[] MEASURANDS =
            {
                    "Partikel-PM10", "Ruß", "Stickstoffdioxid", "Benzol",
                    "Kohlenmonoxid", "Ozon", "Schwefeldioxid"
            }; //TODO this list should be created dynamically based on the header row of the response html

    private static final int EXPECTED_MEASUREMENT_TYPE_HEADER_CELLS = 14;
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

        Elements rows = table.getElementsByTag("tr");

        //The second header row has our measurement types, get it and populate a list with the values
        Element measurementTypesHeaderRow = rows.get(1);
        Elements measurementTypeCells = measurementTypesHeaderRow.children();
        measurementTypeCells.remove(0); //remove first cell (header column)

        //check the page still has the same number of measurement types we had originally
        if (measurementTypeCells.size() != EXPECTED_MEASUREMENT_TYPE_HEADER_CELLS) {
            System.out.println("Unexpected number of measurement type header cells.");
            System.out.println("Expected: " + EXPECTED_MEASUREMENT_TYPE_HEADER_CELLS + ", found: " + measurementTypeCells.size());
            System.out.println("Continuing...");
        }

        //Filter out empty cells
        String[] measurementTypes =
                measurementTypeCells.stream()
                                    .map(cell -> cell.text().trim())
                                    .filter(text -> !text.isEmpty())
                                    .toArray(String[]::new);

        DailyMeasurements dailyMeasurements = new DailyMeasurements();
        dailyMeasurements.date = date;
        dailyMeasurements.url = url;
        dailyMeasurements.stationMeasurements = new HashMap<>();

        //Process the table one row at a time
        //skip first two header rows
        for (int i = 2; i < rows.size(); i++) {
            Element row = rows.get(i);
            Elements cells = row.getElementsByTag("td");

            //Remove the station identifier header cell and parse the station ID & name
            Element stationCell = cells.remove(0);
            Station station = parseStation(stationCell);

            //Skip rows which are not data rows
            //there should be 2 of these; the separator row and the last row
            if (station == null) { continue; }

            Measurement[] measurements = parseMeasurements(date, measurementTypes, cells, station);

            dailyMeasurements.stationMeasurements.put(station, measurements);
        }

        return dailyMeasurements;
    }

    private static Measurement[] parseMeasurements(
            LocalDate date, String[] measurementTypes, Elements cells, Station station) {

        Measurement[] measurements = new Measurement[MEASURANDS.length];

        for (int j = 0; j < MEASURANDS.length; j++) {
            String measurand = MEASURANDS[j];

            Measurement measurement = new Measurement();
            measurement.date = date;
            measurement.station = station;

            for (int offset = 0; offset < 2; offset++){
                int index = (j*2) + offset;
                String frequency = measurementTypes[index];
                String value = cells.eq(index).text();

                //Replace "blank" values with nulls
                if (value.equals("---"))
                    value = null;

                measurement.measurand = measurand;
                measurement.measurementType = frequency;
                measurement.value = value == null ? null : Double.parseDouble(value);
                measurement.unit = MEASUREMENT_UNIT;
            }

            measurements[j] = measurement;
        }

        return measurements;
    }

    private static Station parseStation(Element stationCell) {
        String stationIdentifier = stationCell.text();
        String[] stationTokens = stationIdentifier.split("\u00a0"); //split by &nbsp;

        if (stationTokens.length != 2) {
            //System.out.println("Station tokens != 2. Input string was '"+ stationIdentifier + "'");
            return null;
        }

        String stationId = stationTokens[0];
        String stationName = stationTokens[1];

        //Get the station from our static resource by its id
        //we do this since the static resource has information we need (like the coordinates)
        Station station = StationsStaticDataProvider.get(stationId);
        return station;
    }
}
