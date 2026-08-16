package com.example.API.Repositories;

import com.example.API.customrepositories.UsersRepositoryCustom;
import com.example.API.models.Users;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends MongoRepository<Users, String> , UsersRepositoryCustom { // Use String for the ID type
    Users findByUsername(String username);
}

