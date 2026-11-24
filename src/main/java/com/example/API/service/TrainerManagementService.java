package com.example.API.service;

import com.example.API.Repositories.ProgramsRepository;
import com.example.API.Repositories.ReservationsRepository;
import com.example.API.Repositories.TrainersRepository;
import com.example.API.models.Trainers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TrainerManagementService {
    private final TrainersRepository trainersRepository;
    private final ReservationsRepository reservationsRepository;
    private final ProgramsRepository programsRepository;

    public TrainerManagementService(TrainersRepository trainersRepository, ReservationsRepository reservationsRepository, ProgramsRepository programsRepository){
        this.programsRepository = programsRepository;
        this.reservationsRepository = reservationsRepository;
        this.trainersRepository = trainersRepository;
    }

    @Transactional
    public void deleteTrainerData(Long id){
        Trainers trainer = trainersRepository.findByIdForUpdate(id).orElseThrow(
                () -> new NoSuchElementException("Trainer not found!"));
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        List<Long> program_ids = programsRepository.findByTrainerIdAndProgramdateGreaterThanEqual(date, id , time);
        if (!program_ids.isEmpty()){
            reservationsRepository.deleteAllReservationsFromPrograms(program_ids);
        }
        programsRepository.deleteByTrainer_IdFuturePrograms(date ,id, time);
        programsRepository.detachTrainerFromProgram(id);
        trainersRepository.deleteById(id);

    }
}
