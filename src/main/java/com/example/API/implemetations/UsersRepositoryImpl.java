package com.example.API.implemetations;

import com.example.API.customrepositories.UsersRepositoryCustom;
import com.example.API.models.Programs;
import com.example.API.models.UserReservations;
import com.example.API.models.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class UsersRepositoryImpl implements UsersRepositoryCustom {

    @Autowired
    private MongoTemplate MongoTemplate;

    @Override
    public List<Users> GetNotAccepted(){//all users where accepted==false
        return MongoTemplate.query(Users.class)
                .matching(Query.query(Criteria.where("accepted").is(false)))
                .all();
    }

    public List<Users> GetAllAccepted(){//all users where accepted==true
        return MongoTemplate.query(Users.class)
                            .matching(Query.query(Criteria.where("accepted").is(true)))
                            .all();
    }

    public List<UserReservations> findByUserid(String id){//Show all the user's reservations
        //Find all the programs with userid
        Query query = new Query();
        query.addCriteria(Criteria.where("userid").is(id));

        return MongoTemplate.find(query , UserReservations.class);
    }
}
