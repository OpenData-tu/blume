package edu.tu_berlin.ise.opendata.blume;

import edu.tu_berlin.ise.opendata.blume.model.DailyMeasurements;

import java.time.LocalDate;

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

        String jsonString = Serializer.serializeDailyMeasurements(dailyMeasurements);
        System.out.println(jsonString);//TODO write to Kafka
    }
}
