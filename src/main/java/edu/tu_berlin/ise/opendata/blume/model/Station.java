package edu.tu_berlin.ise.opendata.blume.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by aardila on 7/5/17.
 */
public class Station {
    public String id;
    public String name;
    @SerializedName("alt") public String altName;
    public String ref;
    public Coordinates coord;
    @SerializedName("addr") public Address address;
    public String url;
}
