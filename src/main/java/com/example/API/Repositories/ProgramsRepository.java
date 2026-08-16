package com.example.API.Repositories;

import com.example.API.customrepositories.ProgramsRepositoryCustom;
import com.example.API.models.Programs;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramsRepository extends MongoRepository<Programs, String> , ProgramsRepositoryCustom { // Use String for the ID type

}
