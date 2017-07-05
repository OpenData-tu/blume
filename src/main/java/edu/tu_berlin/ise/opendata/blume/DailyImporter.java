package edu.tu_berlin.ise.opendata.blume;

import edu.tu_berlin.ise.opendata.blume.model.DailyMeasurements;
import edu.tu_berlin.ise.opendata.blume.model.Measurement;
import edu.tu_berlin.ise.opendata.blume.model.Station;

import java.time.LocalDate;
import java.util.Map;

/**
 * Created by aardila on 2017-07/03.
 */
public class DailyImporter {

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            throw new Exception("Mandatory import date argument missing!");
        }

        String dateArg = args[0];
        LocalDate dateToImport = LocalDate.parse(dateArg);

        DailyMeasurements dailyMeasurements = BlumeWrapper.getDailyMeasurements(dateToImport);

        //augment station with extra info in the stations.json file
        for (Map.Entry<Station, Measurement[]> mapEntry : dailyMeasurements.stationMeasurements.entrySet()) {
            Station key = mapEntry.getKey();
            Station s = StationsStaticDataProvider.get(key.id);
            key.coord = s.coord;
            for (Measurement measurement : mapEntry.getValue()) {
                measurement.station.coord = s.coord;
            }
        }
        String jsonString = Serializer.serializeDailyMeasurements(dailyMeasurements);
        System.out.println(jsonString);//TODO write to Kafka
    }
}
