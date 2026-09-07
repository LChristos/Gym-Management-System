package com.example.API.Repositories;

import com.example.API.models.UserReservations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationsRepository extends JpaRepository<UserReservations,Long>{
    List<UserReservations> findByUser_Id(Long userid);

    UserReservations findByUser_IdAndProgram_IdOrderByCreatedAtDesc(Long userid, Long programid);

    //Delete all reservations that have this user id
    void deleteByUser_Id(Long userid);

    //Delete all reservations that have this program
    void deleteByProgram_Id(Long programid);

    //Delete all reservations from all the programs
    @Modifying
    @Query("DELETE FROM UserReservations u WHERE u.program.id IN :programid")
    void deleteAllReservationsFromPrograms(@Param("programid") List<Long> programid);

    //Return only the program id of the user reservations
    @Query("SELECT u.program.id FROM UserReservations u WHERE u.user.id = :userid")
    List<Long> findAllProgramidFromUser(@Param("userid") Long userid);
}
