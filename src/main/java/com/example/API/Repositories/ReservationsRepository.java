package com.example.API.Repositories;

import com.example.API.customrepositories.ReservationsRepositoryCustom;
import com.example.API.models.UserReservations;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationsRepository extends MongoRepository<UserReservations,String> , ReservationsRepositoryCustom {
}
