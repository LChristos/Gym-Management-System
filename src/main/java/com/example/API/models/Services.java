package com.example.API.models;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Document(collection = "services")
public class Services {
    @Id
    private String id;

    private String name;
    private String description;
    private int price;
    private int max_people;
    private String url_image;

    public Services(String name , String description , int price , int max_people , String url_image) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.max_people = max_people;
        this.url_image = url_image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getMax_people() {
        return max_people;
    }

    public void setMax_people(int max_people) {
        this.max_people = max_people;
    }

    public String getUrl_image() {
        return url_image;
    }
}
