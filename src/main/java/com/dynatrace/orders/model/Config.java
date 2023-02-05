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

    @Column(name="load_cpu", nullable = true)
    private long loadCPU;

    @Column(name="load_ram", nullable = true)
    private long loadRAM;

    @Column(name="probab_fail", nullable = true)
    private double probabilityFailure;

    @Column(name="property_str", nullable = true)
    private String propertyStr;

    @Column(name="turn_on", nullable = true)
    private boolean turnedOn;

    public Config() {
    }

    public Config(String id, long loadCPU, long loadRAM, double probabilityFailure, String propertyStr, boolean turnedOn) {
        this.id = id;
        this.loadCPU = loadCPU;
        this.loadRAM = loadRAM;
        this.probabilityFailure = probabilityFailure;
        this.propertyStr = propertyStr;
        this.turnedOn = turnedOn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getLoadCPU() {
        return loadCPU;
    }

    public void setLoadCPU(long loadCPU) {
        this.loadCPU = loadCPU;
    }

    public long getLoadRAM() {
        return loadRAM;
    }

    public void setLoadRAM(long loadRAM) {
        this.loadRAM = loadRAM;
    }
    public double getProbabilityFailure() {
        return probabilityFailure;
    }

    public void setProbabilityFailure(double propertyDouble) {
        this.probabilityFailure = propertyDouble;
    }

    public String getPropertyStr() {
        return propertyStr;
    }

    public void setPropertyStr(String propertyStr) {
        this.propertyStr = propertyStr;
    }

    public boolean isTurnedOn() {
        return turnedOn;
    }

    public void setTurnedOn(boolean propertyBool) {
        this.turnedOn = propertyBool;
    }
}
