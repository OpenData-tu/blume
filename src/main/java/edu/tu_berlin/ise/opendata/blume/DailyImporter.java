package edu.tu_berlin.ise.opendata.blume;

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

        String jsonString = BlumeWrapper.getDailyMeasurements(dateToImport);
        System.out.println(jsonString);
    }
}
