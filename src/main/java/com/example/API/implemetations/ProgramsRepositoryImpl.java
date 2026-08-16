package com.example.API.implemetations;

import com.example.API.customrepositories.ProgramsRepositoryCustom;
import com.example.API.models.Programs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ProgramsRepositoryImpl implements ProgramsRepositoryCustom {

    @Autowired
    private MongoTemplate MongoTemplate;

    public List<Programs> FindAllPrograms(){
        LocalDate today = LocalDate.now();
        Query query = new Query();
        query.addCriteria(Criteria.where("date").gte(today));
        query.with(Sort.by(Sort.Direction.ASC, "date"));
        return MongoTemplate.find(query , Programs.class);

    }

    public List<Programs> ShowAllAvailable(){//Show all the available reservations
        LocalDate today = LocalDate.now();
        Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        String isoDate = todayDate.toInstant().toString(); // e.g. "2025-02-10T00:00:00Z"

        // Build the JSON query string.
        // This query matches documents where:
        //   1. The expression: { $gt: [ "$max_people", "$peoplereserve" ] } is true,
        //   2. AND programdate is greater than or equal to our start date.
        String queryJson = "{ \"$and\": [ " +
                "  { \"$expr\": { \"$gt\": [ \"$max_people\", \"$peoplereserve\" ] } }, " +
                "  { \"programdate\": { \"$gte\": { \"$date\": \"" + isoDate + "\" } } } " +
                "] }";

        Query query = new BasicQuery(queryJson);
        return MongoTemplate.find(query, Programs.class);

    }

    public List<Programs> showProgram(String service_name){//Show this week's program for the service
        LocalDate today = LocalDate.now();

        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate saturday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        //Transform into days
        Date start_date = Date.from(monday.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end_date = Date.from(saturday.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Query query = new Query();
        query.addCriteria(Criteria.where("services_name").is(service_name));
        query.addCriteria(Criteria.where("programdate").gte(start_date).lte(end_date));
        return MongoTemplate.find(query , Programs.class);
    }
}
