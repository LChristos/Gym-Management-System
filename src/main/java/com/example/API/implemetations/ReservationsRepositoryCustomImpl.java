package com.example.API.implemetations;


import com.example.API.customrepositories.ReservationsRepositoryCustom;
import com.example.API.models.UserReservations;
import com.example.API.models.Programs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class ReservationsRepositoryCustomImpl implements ReservationsRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<UserReservations> findReservationsByUserid(String userid){
        Query query = new Query();
        query.addCriteria(Criteria.where("userid").is(userid));
        return mongoTemplate.find(query , UserReservations.class);
    }

    public UserReservations findByUseridAndProgramid(String userid , String programid){
        Query query = new Query();
        query.addCriteria(Criteria.where("userid").is(userid));
        query.addCriteria(Criteria.where("programid").is(programid));
        return mongoTemplate.findOne(query, UserReservations.class);
    }


}
