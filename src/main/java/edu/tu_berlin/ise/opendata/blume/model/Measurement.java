package edu.tu_berlin.ise.opendata.blume.model;

import java.time.LocalDate;

/**
 * Created by artichoke on 7/5/17.
 */
public class Measurement {
    public LocalDate date;
    public Station station;
    public String measurand;
    public String measurementType;
    public Double value;

    public String unit;
}
