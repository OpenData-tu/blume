package edu.tu_berlin.ise.opendata.blume.model;

import java.time.LocalDate;
import java.util.Map;

/**
 * Created by aardila on 7/5/17.
 */
public class DailyMeasurements {
    public LocalDate date;
    public String url;
    public Map<Station, Measurement[]> stationMeasurements;
}
