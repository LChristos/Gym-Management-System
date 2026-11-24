package com.example.API.Repositories;

import com.example.API.models.Programs;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramsRepository extends JpaRepository<Programs, Long>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Programs p WHERE p.id= :id")
    Optional<Programs> findByIdForUpdate(@Param("id") Long id);

    //Find all the available reservations from today and after.
    @Query("SELECT u FROM Programs u WHERE (u.programdate > :date OR (u.programdate = :date AND u.hour >= :hour)) AND u.max_people > u.peoplereserve ORDER BY u.programdate, u.hour, u.id ASC")
    List<Programs> findReservationsAvailable(@Param("date") LocalDate today , @Param("hour") LocalTime hour);

    //Find all the program of this week.
    @Query("SELECT u FROM Programs u WHERE u.service.id = :serviceId AND u.programdate BETWEEN :startDate AND :endDate ORDER BY u.programdate ASC")
    List<Programs> findWeekProgram(@Param("serviceId") Long service_id, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    //Find all the program from today and after.
    List<Programs> findByProgramdateGreaterThanEqualOrderByProgramdateAsc(LocalDate date);

    //Find the program of a specific trainer on specific date
    List<Programs> findByTrainer_IdAndProgramdate(Long trainer_id, LocalDate date);

    //Find all the available programs that the user hasn't made a reservation yet
    @Query("Select u FROM Programs u WHERE (u.programdate > :date OR (u.programdate = :date AND u.hour >= :hour)) AND u.max_people > u.peoplereserve AND u.id NOT IN :id ORDER BY u.programdate, u.hour, u.id ASC")
    List<Programs> findReservationsAvailableWithoutUserid(@Param("date") LocalDate date, @Param("id") List<Long> programid , @Param("hour") LocalTime hour);

    //Find all the programs that the user has made a reservation and are today and after
    @Query("Select u FROM Programs u WHERE u.programdate >= :date AND u.id IN :id ORDER BY u.programdate, u.hour, u.id ASC")
    List<Programs> findFutureReservationsForUserid(@Param("date") LocalDate date, @Param("id") List<Long> programid);

    //Find all the programs that the user has made a reservation
    List<Programs> findAllByIdIn(List<Long> programid);

    //Find all the future programs for a specific trainer
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u.id FROM Programs u WHERE (u.programdate > :date OR (u.programdate = :date AND u.hour >= :hour)) AND u.trainer.id = :trainerid")
    List<Long> findByTrainerIdAndProgramdateGreaterThanEqual(@Param("date") LocalDate date, @Param("trainerid") Long id, @Param("hour") LocalTime hour);

    //Delete the future programs that have this specific trainer
    @Modifying
    @Query("DELETE FROM Programs u WHERE (u.programdate > :date OR (u.programdate = :date AND u.hour >= :hour)) AND u.trainer.id = :trainerid")
    void deleteByTrainer_IdFuturePrograms(@Param("date") LocalDate date, @Param("trainerid") Long trainerid, @Param("hour")LocalTime hour);

    //Find all the future programs for a specific service
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u.id FROM Programs u WHERE (u.programdate > :date OR (u.programdate = :date AND u.hour >= :hour)) AND u.service.id = :serviceid")
    List<Long> findByServiceIdAndProgramdateGreaterThanEqual(@Param("date") LocalDate date, @Param("serviceid") Long id, @Param("hour") LocalTime hour);

    //Delete the future programs that have this specific trainer
    @Modifying
    @Query("DELETE FROM Programs u WHERE (u.programdate > :date OR (u.programdate = :date AND u.hour >= :hour)) AND u.service.id = :serviceid")
    void deleteByService_IdFuturePrograms(@Param("date") LocalDate date, @Param("serviceid") Long trainerid, @Param("hour") LocalTime hour);

    //Update the programs with this sevice id to null
    @Modifying
    @Query("UPDATE Programs u SET u.service = NULL WHERE u.service.id = :serviceid")
    void detachServiceFromProgram(@Param("serviceid") Long serviceid);

    //Update the programs with this trainer id to null
    @Modifying
    @Query("UPDATE Programs u SET u.trainer = NULL WHERE u.trainer.id = :trainerid")
    void detachTrainerFromProgram(@Param("trainerid") Long trainerid);

    //Remove from the Programs one from peoplereserve
    @Modifying
    @Query("UPDATE Programs p SET p.peoplereserve = p.peoplereserve -1 WHERE p.id IN :id")
    void decreasePeoplereserveFromPrograms(@Param("id") List<Long> id);
}
