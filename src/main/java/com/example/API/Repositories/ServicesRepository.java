package com.example.API.Repositories;

import com.example.API.models.Services;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicesRepository extends MongoRepository<Services, String> { // Use String for the ID type

}
