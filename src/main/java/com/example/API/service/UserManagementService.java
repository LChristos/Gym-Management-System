package com.example.API.service;

import com.example.API.Repositories.ProgramsRepository;
import com.example.API.Repositories.ReservationsRepository;
import com.example.API.Repositories.UsersRepository;
import com.example.API.models.Users;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserManagementService {
    private final UsersRepository usersRepository;
    private final ReservationsRepository reservationsRepository;
    private final ProgramsRepository programsRepository;

    public UserManagementService(UsersRepository usersRepository , ReservationsRepository reservationsRepository, ProgramsRepository programsRepository){
        this.usersRepository = usersRepository;
        this.reservationsRepository = reservationsRepository;
        this.programsRepository = programsRepository;
    }

    @Transactional
    public void deleteUserData(Long userId){
        Users user = usersRepository.findByIdForUpdate(userId).orElseThrow(() ->
                new NoSuchElementException("User not found with id: " + userId));
        List<Long> programIds = reservationsRepository.findAllProgramidFromUser(userId);
        reservationsRepository.deleteByUser_Id(userId);
        programsRepository.decreasePeoplereserveFromPrograms(programIds);
        usersRepository.deleteById(userId);
    }
}
