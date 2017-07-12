package edu.tu_berlin.ise.opendata.blume;

import edu.tu_berlin.ise.opendata.blume.model.DailyMeasurements;
import edu.tu_berlin.ise.opendata.blume.model.Measurement;
import edu.tu_berlin.ise.opendata.blume.model.Station;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * @author Andres Ardila
 */
public class BlumeWrapper {

    private static final Logger logger = LoggerFactory.getLogger(BlumeWrapper.class);

    private final static String[] MEASURANDS =
            {
                    "Partikel-PM10", "Ruß", "Stickstoffdioxid", "Benzol",
                    "Kohlenmonoxid", "Ozon", "Schwefeldioxid"
            };
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
            logger.error("Got HTTP 404 when when getting {}", url);
            throw new Exception(
                    String.format("No data is available for %s (HTTP 404)", date.toString()));
        }

        Document doc = response.parse();

        Element table = doc.select("table.datenhellgrauklein").first();
        Elements rows = table.getElementsByTag("tr");
        String[] measurands = parseHeaderRow(rows.get(0));

        //Check if the measurands are still the same as the static list
        //NOTE: they have additional unwanted characters (like asterisks) following the measurand name, so we can't deepEquals
        //still, we could check that the text beginsWith the corresponding element in MEASURANDS just to be sure
        if (measurands.length == MEASURANDS.length) {
            //take our static list
            measurands = MEASURANDS;
        }
        else {
            //in this case, we use the list of measurands from the HTML page
            //also, the new measurands should be added to the static list so as to avoid these chars & have a clean string
            logger.warn("Unexpected number of measurands in HTML document. Expected: %d, found: %d");
        }

        String[] measurementTypes = parseHeaderRow(rows.get(1));

        //check the page still has the same number of measurement types we had originally
        if (measurementTypes.length != EXPECTED_MEASUREMENT_TYPE_HEADER_CELLS) {
            logger.error("Unexpected number of measurement type header cells. Expected: %s, found: %s",
                    EXPECTED_MEASUREMENT_TYPE_HEADER_CELLS, measurementTypes.length);
        }

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
            Station station = parseStation(stationCell); //should be null if parsing failed

            //Skip rows which are not data rows
            //there should be 2 of these: the separator row and the last row
            if (station == null) { continue; }

            Measurement[] measurements =
                    parseMeasurements(date, measurands, measurementTypes, cells, station);

            dailyMeasurements.stationMeasurements.put(station, measurements);
        }

        return dailyMeasurements;
    }

    private static String[] parseHeaderRow(Element headerRow) {
        //The second header row has our measurement types, get it and populate a list with the values
        Elements measurementTypeCells = headerRow.children();
        measurementTypeCells.remove(0); //remove first cell (header column)

        //Filter out empty cells
        return measurementTypeCells.stream()
                            .map(cell -> cell.text().trim())
                            .filter(text -> !text.isEmpty())
                            .toArray(String[]::new);
    }

    private static Measurement[] parseMeasurements(
            LocalDate date, String[] measurands, String[] measurementTypes, Elements cells, Station station)
            throws Exception {

        //check that the array dimensions correspond to our expectations & implementation below
        //we require that there are two measurementTypes per measurand
        if (measurementTypes.length / measurands.length != 2) {
            throw new Exception("Unexpected array measurements: Expected two measurementTypes per measurand");
        }

        Measurement[] measurements = new Measurement[measurands.length];

        for (int i = 0; i < measurands.length; i++) {
            String measurand = measurands[i];

            Measurement measurement = new Measurement();
            measurement.date = date;
            measurement.station = station;

            for (int offset = 0; offset < 2; offset++){
                int index = (i*2) + offset;
                String frequency = measurementTypes[index];
                Double value = parseMeasurementValue(cells.get(index));

                measurement.measurand = measurand;
                measurement.measurementType = frequency;
                measurement.value = value;
                measurement.unit = MEASUREMENT_UNIT;
            }

            measurements[i] = measurement;
        }

        return measurements;
    }

    private static Double parseMeasurementValue(Element cell) {
        String text = cell.text();

        //Replace "blank" values with nulls
        return  text.equals("---") ? null : Double.parseDouble(text);
    }

    /**
     * Parses a Station from the HTML element
     * @param stationCell the HTML DOM element with the station header
     * @return the parsed Station object, or null if parsing failed
     */
    private static Station parseStation(Element stationCell) {
        String stationIdentifier = stationCell.text();
        String[] stationTokens = stationIdentifier.split("\u00a0"); //split by &nbsp;

        if (stationTokens.length != 2) {
            //System.out.println("Station tokens != 2. Input string was '"+ stationIdentifier + "'");
            return null;
        }

        String parsedStationId = stationTokens[0];
        String parsedStationName = stationTokens[1];

        //Get the station from our static resource by its id
        //we do this since the static resource has information we need (like the station coordinates)
        Station station = StationsStaticDataProvider.get(parsedStationId);
        return station;
    }
}
