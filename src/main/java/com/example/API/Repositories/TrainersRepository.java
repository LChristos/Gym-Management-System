package com.example.API.Repositories;

import com.example.API.models.Trainers;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainersRepository extends MongoRepository<Trainers, String> { // Use String for the ID type

}
