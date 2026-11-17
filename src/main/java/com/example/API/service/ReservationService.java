package com.example.API.service;

import com.example.API.Repositories.ProgramsRepository;
import com.example.API.Repositories.ReservationsRepository;
import com.example.API.Repositories.UsersRepository;
import com.example.API.models.Programs;
import com.example.API.models.UserReservations;
import com.example.API.models.Users;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class ReservationService {

    private final UsersRepository usersRepository;
    private final ProgramsRepository programsRepository;
    private final ReservationsRepository reservationsRepository;

    public ReservationService(UsersRepository usersRepository, ProgramsRepository programsRepository, ReservationsRepository reservationsRepository){
        this.usersRepository = usersRepository;
        this.programsRepository= programsRepository;
        this.reservationsRepository= reservationsRepository;
    }

    @Transactional
    public void createReservation(Long userId , Long programId) throws  DuplicateReservationException{
        Programs program = programsRepository.findById(programId).orElseThrow(() ->
                new NoSuchElementException("Program not found with id: " + programId));
        Users user = usersRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("User not found with id: " + userId));
        UserReservations res = new UserReservations(program, user);

        //If there is already a reservation for this user in the specific program
        UserReservations savedRes = reservationsRepository.findByUser_IdAndProgram_IdOrderByCreatedAtDesc(res.getUser().getId(), res.getProgram().getId());
        if (savedRes != null){
            throw new DuplicateReservationException("You have already done reservation for this specific program for this user");
        }

        reservationsRepository.save(res);
        Programs theProgram = res.getProgram();
        theProgram.addpeople();
        programsRepository.save(theProgram);
    }
}
