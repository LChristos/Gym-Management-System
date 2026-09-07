package com.example.API.Repositories;

import com.example.API.models.Programs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProgramsRepository extends JpaRepository<Programs, Long>{ // Use String for the ID type
    //Find all the available reservations from today and after.
    @Query("Select u FROM Programs u WHERE u.programdate >= :date AND u.max_people > u.peoplereserve ORDER BY u.programdate ASC")
    List<Programs> findReservationsAvailable(@Param("date") LocalDate today);

    //Find all the program of this week.
    @Query("SELECT u FROM Programs u WHERE u.service.id = :serviceId AND u.programdate BETWEEN :startDate AND :endDate ORDER BY u.programdate ASC")
    List<Programs> findWeekProgram(@Param("serviceId") Long service_id, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    //Find all the program from today and after.
    List<Programs> findByProgramdateGreaterThanEqualOrderByProgramdateAsc(LocalDate date);

    //Find the program of a specific trainer on specific date
    List<Programs> findByTrainer_IdAndProgramdate(Long trainer_id, LocalDate date);

    //Find all the available programs that the user hasn't made a reservation yet
    @Query("Select u FROM Programs u WHERE u.programdate >= :date AND u.max_people > u.peoplereserve AND u.id NOT IN :id ORDER BY u.programdate ASC")
    List<Programs> findReservationsAvailableWithoutUserid(@Param("date") LocalDate date, @Param("id") List<Long> programid);

    //Find all the programs that the user has made a reservation and are today and after
    @Query("Select u FROM Programs u WHERE u.programdate > :date AND u.id IN :id ORDER BY u.programdate ASC")
    List<Programs> findFutureReservationsForUserid(@Param("date") LocalDate date, @Param("id") List<Long> programid);

    //Find all the programs that the user has made a reservation
    List<Programs> findAllByIdIn(List<Long> programid);

    //Find all the future programs for a specific trainer
    @Query("SELECT u.id FROM Programs u WHERE u.programdate >= :date AND u.trainer.id = :trainerid")
    List<Long> findByTrainerIdAndProgramdateGreaterThanEqual(@Param("date") LocalDate date, @Param("trainerid") Long id);

    //Delete the future programs that have this specific trainer
    @Modifying
    @Query("DELETE FROM Programs u WHERE u.programdate >= :date AND u.trainer.id = :trainerid")
    void deleteByTrainer_IdFuturePrograms(@Param("date") LocalDate date, @Param("trainerid") Long trainerid);

    //Find all the future programs for a specific service
    @Query("SELECT u.id FROM Programs u WHERE u.programdate >= :date AND u.service.id = :serviceid")
    List<Long> findByServiceIdAndProgramdateGreaterThanEqual(@Param("date") LocalDate date, @Param("serviceid") Long id);

    //Delete the future programs that have this specific trainer
    @Modifying
    @Query("DELETE FROM Programs u WHERE u.programdate >= :date AND u.service.id = :serviceid")
    void deleteByService_IdFuturePrograms(@Param("date") LocalDate date, @Param("serviceid") Long trainerid);

    //Update the programs with this sevice id to null
    @Modifying
    @Query("UPDATE Programs u SET u.service = NULL WHERE u.service.id = :serviceid")
    void detachServiceFromProgram(@Param("serviceid") Long serviceid);

    //Update the programs with this trainer id to null
    @Modifying
    @Query("UPDATE Programs u SET u.trainer = NULL WHERE u.trainer.id = :trainerid")
    void detachTrainerFromProgram(@Param("trainerid") Long trainerid);
}
