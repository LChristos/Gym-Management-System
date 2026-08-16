package com.example.API.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;
import java.util.Date;
@Document(collection = "program")
public class Programs {
    @Id
    private String id;

    private String services_name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date programdate;

    private LocalTime hour;
    private String trainer_id;
    private int max_people;
    private int peoplereserve=0;

    public Programs(String services_name, Date programdate, LocalTime hour, String trainer_id, int max_people) {
        this.services_name = services_name;
        this.programdate = programdate;
        this.hour = hour;
        this.trainer_id = trainer_id;
        this.max_people = max_people;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServices_name() {
        return services_name;
    }

    public void setServices_name(String services_name) {
        this.services_name = services_name;
    }

    public Date getProgramdate() {
        return programdate;
    }

    public void setProgramdate(Date programdate) {
        this.programdate = programdate;
    }

    public LocalTime getHour() {
        return hour;
    }

    public void setHour(LocalTime hour) {
        this.hour = hour;
    }

    public String getTrainer_id() {
        return trainer_id;
    }

    public void setTrainer_id(String trainer_id) {
        this.trainer_id = trainer_id;
    }

    public int getMax_people() {
        return max_people;
    }

    public void setMax_people(int maxpeople) {
        this.max_people = maxpeople;
    }

    public int getPeoplereserve() {
        return peoplereserve;
    }

    public void setPeoplereserve(int peoplereserve) {
        this.peoplereserve = peoplereserve;
    }

    public void addpeople(){
        this.peoplereserve +=1;
    }

    public void removepeople(){
        this.peoplereserve -=1;
    }
}
