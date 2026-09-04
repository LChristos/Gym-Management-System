package com.example.API.Repositories;

import com.example.API.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long>{ // Use String for the ID type
    Users findByUsername(String username);

    List<Users> findByAcceptedFalse();

    List<Users> findByAcceptedTrue();
}

