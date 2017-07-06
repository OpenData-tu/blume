package edu.tu_berlin.ise.opendata.blume.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by aardila on 7/5/17.
 */
public class Coordinates {
    @SerializedName("lat") public double latitude;
    @SerializedName("lon") public double longitude;
}
