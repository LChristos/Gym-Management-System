package com.example.API.service;

import com.example.API.Repositories.*;
import com.example.API.dto.ProgramDTO;
import com.example.API.models.Programs;
import com.example.API.models.Services;
import com.example.API.models.Trainers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class ProgramManagementService {
    private final ProgramsRepository programsRepository;
    private final ReservationsRepository reservationsRepository;
    private final UsersRepository usersRepository;
    private final TrainersRepository trainersRepository;
    private final ServicesRepository servicesRepository;

    public ProgramManagementService(ProgramsRepository programsRepository, ReservationsRepository reservationsRepository ,
                                    UsersRepository usersRepository, TrainersRepository trainersRepository, ServicesRepository servicesRepository){
        this.programsRepository = programsRepository;
        this.reservationsRepository = reservationsRepository;
        this.usersRepository = usersRepository;
        this.servicesRepository = servicesRepository;
        this.trainersRepository = trainersRepository;
    }

    @Transactional
    public void deleteProgramData(Long programId){
        Programs program = programsRepository.findByIdForUpdate(programId).orElseThrow(
                () -> new NoSuchElementException("Could not found this specific program" + programId));
        reservationsRepository.deleteByProgram_Id(programId);
        programsRepository.deleteById(programId);
    }

    @Transactional
    public void changeProgramData(ProgramDTO programDTO){
        Programs program = programsRepository.findByIdForUpdate(programDTO.getId()).orElseThrow(
                () -> new NoSuchElementException("Program not found!"));
        Services service = servicesRepository.findById(programDTO.getServices_name()).orElseThrow(() ->
                new NoSuchElementException("Service not found with id: " + programDTO.getServices_name()));
        Trainers trainer = trainersRepository.findById(programDTO.getTrainer_id()).orElseThrow(() ->
                new NoSuchElementException("Trainer not found with id: " + programDTO.getTrainer_id()));
        program.setService(service);
        program.setHour(programDTO.getHour());
        program.setProgramdate(programDTO.getProgramdate());
        program.setTrainer(trainer);
        if(programDTO.getMax_people() >= program.getPeoplereserve()){
            program.setMax_people(programDTO.getMax_people());
        }
        programsRepository.save(program);

    }
}
