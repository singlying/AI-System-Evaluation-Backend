package com.ai.backend.entity;

public class Dataset {
    private int id;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private int type;
    private String name;
    private String description;
    private double mean1;
    private double mean2;
    private double mean3;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    private int userId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getMean1() {
        return mean1;
    }

    public void setMean1(Double mean1) {
        this.mean1 = mean1;
    }

    public double getMean2() {
        return mean2;
    }

    public void setMean2(Double mean2) {
        this.mean2 = mean2;
    }

    public double getMean3() {
        return mean3;
    }

    public void setMean3(Double mean3) {
        this.mean3 = mean3;
    }

    public double getStd1() {
        return std1;
    }

    public void setStd1(Double std1) {
        this.std1 = std1;
    }

    public double getStd2() {
        return std2;
    }

    public void setStd2(Double std2) {
        this.std2 = std2;
    }

    public double getStd3() {
        return std3;
    }

    public void setStd3(Double std3) {
        this.std3 = std3;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private double std1;
    private double std2;
    private double std3;
}
