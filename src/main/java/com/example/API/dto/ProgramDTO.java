package com.example.API.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

// Wire-format shape kept identical to the old (pre-relations) Programs entity,
// so the frontend doesn't need to change: services_name + trainer_id stay flat.
public class ProgramDTO {
    private Long id;
    // Field name kept as "services_name" to match the existing frontend JSON key,
    // but the value has always actually been the service's id (see new_program.js / manage_system.js).
    private Long services_name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate programdate;

    private LocalTime hour;
    private Long trainer_id;
    private int max_people;
    private int peoplereserve;

    public ProgramDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServices_name() {
        return services_name;
    }

    public void setServices_name(Long services_name) {
        this.services_name = services_name;
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

    public Long getTrainer_id() {
        return trainer_id;
    }

    public void setTrainer_id(Long trainer_id) {
        this.trainer_id = trainer_id;
    }

    public int getMax_people() {
        return max_people;
    }

    public void setMax_people(int max_people) {
        this.max_people = max_people;
    }

    public int getPeoplereserve() {
        return peoplereserve;
    }

    public void setPeoplereserve(int peoplereserve) {
        this.peoplereserve = peoplereserve;
    }
}