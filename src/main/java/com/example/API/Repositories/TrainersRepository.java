package com.example.API.Repositories;

import com.example.API.models.Trainers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainersRepository extends JpaRepository<Trainers, Long> { // Use String for the ID type

}
