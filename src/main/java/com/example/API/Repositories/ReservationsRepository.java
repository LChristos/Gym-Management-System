package com.example.API.Repositories;

import com.example.API.models.UserReservations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationsRepository extends JpaRepository<UserReservations,Long>{
    List<UserReservations> findByUser_Id(Long userid);

    UserReservations findByUser_IdAndProgram_IdOrderByCreatedAtDesc(Long userid, Long programid);

    //Return only the program id of the user reservations
    @Query("SELECT u.program.id FROM UserReservations u WHERE u.user.id = :userid")
    List<Long> findAllProgramidFromUser(@Param("userid") Long userid);
}
