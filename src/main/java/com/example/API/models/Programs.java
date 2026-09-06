package com.example.API.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Entity
@Table(name = "program")
public class Programs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK -> service.id  (was: private String services_name;)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Services service;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "programdate")
    private LocalDate programdate;

    private LocalTime hour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id" , nullable = false)
    private Trainers trainer;

    private int max_people;
    private int peoplereserve=0;

    public Programs() {}

    public Programs(Services service, LocalDate programdate, LocalTime hour, Trainers trainer, int max_people) {
        this.service = service;
        this.programdate = programdate;
        this.hour = hour;
        this.trainer = trainer;
        this.max_people = max_people;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Services getService() {
        return service;
    }

    public void setService(Services service) {
        this.service = service;
    }

    public LocalDate getProgramdate() {
        return programdate;
    }

    public void setProgramdate(LocalDate programdate) {
        this.programdate = programdate;
    }

    public LocalTime getHour() {
        return hour;
    }

    public void setHour(LocalTime hour) {
        this.hour = hour;
    }

    public Trainers getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainers trainer) {
        this.trainer = trainer;
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
