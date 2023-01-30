package com.dynatrace.orders.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="configs")
public class Config {
    @Id
    private String id;

    @Column(name="property_long", nullable = true)
    private long propertyLong;

    @Column(name="property_double", nullable = true)
    private double propertyDouble;

    @Column(name="property_str", nullable = true)
    private String propertyStr;

    @Column(name="property_bool", nullable = true)
    private boolean propertyBool;

    public Config() {
    }

    public Config(String id, long propertyLong, double propertyDouble, String propertyStr, boolean propertyBool) {
        this.id = id;
        this.propertyLong = propertyLong;
        this.propertyDouble = propertyDouble;
        this.propertyStr = propertyStr;
        this.propertyBool = propertyBool;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getPropertyLong() {
        return propertyLong;
    }

    public void setPropertyLong(long propertyLong) {
        this.propertyLong = propertyLong;
    }

    public double getPropertyDouble() {
        return propertyDouble;
    }

    public void setPropertyDouble(double propertyDouble) {
        this.propertyDouble = propertyDouble;
    }

    public String getPropertyStr() {
        return propertyStr;
    }

    public void setPropertyStr(String propertyStr) {
        this.propertyStr = propertyStr;
    }

    public boolean isPropertyBool() {
        return propertyBool;
    }

    public void setPropertyBool(boolean propertyBool) {
        this.propertyBool = propertyBool;
    }
}
