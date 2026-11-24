package com.example.API.Repositories;

import com.example.API.models.Trainers;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainersRepository extends JpaRepository<Trainers, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trainers t WHERE t.id = :id")
    Optional<Trainers> findByIdForUpdate(@Param("id") Long id);

}
