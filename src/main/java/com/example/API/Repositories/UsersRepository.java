package com.example.API.Repositories;

import com.example.API.models.Users;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Users u WHERE u.id= :id")
    Optional<Users> findByIdForUpdate(@Param("id") Long id);

    Users findByUsername(String username);

    List<Users> findByAcceptedFalse();

    List<Users> findByAcceptedTrue();
}

