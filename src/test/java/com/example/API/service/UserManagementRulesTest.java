package com.example.API.service;

import com.example.API.Repositories.*;
import com.example.API.exceptions.DuplicateReservationException;
import com.example.API.exceptions.programs.ProgramExpiredException;
import com.example.API.exceptions.programs.ProgramFullException;
import com.example.API.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.*;

import static  org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserManagementRulesTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ReservationsRepository reservationsRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    ServicesRepository servicesRepository;

    @Autowired
    TrainersRepository trainersRepository;

    @MockitoSpyBean
    ProgramsRepository programsRepository;

    @Autowired
    ReservationService reservationService;

    @Autowired
    UserManagementService userManagementService;

    private Users memberA;
    private Users memberB;
    private Services service;
    private Trainers trainer;
    private Programs program;

    @AfterEach
    void removeEverything(){
        String[] usernames = {"owner-member", "other-member"};
        for (String username : usernames){
            Users user = usersRepository.findByUsername(username);
            if(user != null){
                reservationsRepository.deleteAll(reservationsRepository.findByUser_Id(user.getId()));
                usersRepository.delete(user);
            }
        }
        if(program != null){
            programsRepository.delete(program);
        }
        if (trainer != null){
            trainersRepository.delete(trainer);
        }
        if (service != null){
            servicesRepository.delete(service);
        }
    }

    @BeforeEach
    void createAccounts(){
        memberA = new Users();
        memberA.setRole("user");
        memberA.setAccepted(true);
        memberA.setUsername("owner-member");
        memberA.setPassword(passwordEncoder.encode("password1"));
        usersRepository.save(memberA);

        memberB = new Users();
        memberB.setRole("user");
        memberB.setAccepted(true);
        memberB.setUsername("other-member");
        memberB.setPassword(passwordEncoder.encode("pass1"));
        usersRepository.save(memberB);

        service = new Services();
        service.setName("service");
        service.setMax_people(10);
        service = servicesRepository.save(service);

        trainer = new Trainers("trainerA", "tr");
        trainer = trainersRepository.save(trainer);

        program = new Programs(service, LocalDate.now().plusDays(3), LocalTime.of(12, 0), trainer, 10);
        program = programsRepository.save(program);

    }

    @Test
    void concurrentBookingAttemptsAndUserDeletion() throws InterruptedException, ExecutionException {
        reservationService.createReservation(memberB.getId(), program.getId());
        CountDownLatch ready;
        CountDownLatch start;
        Future<Boolean> creation;
        Future<Boolean> deletion;
        try (ExecutorService executor = Executors.newFixedThreadPool(2)){
            ready = new CountDownLatch(2);
            start = new CountDownLatch(1);
            creation = executor.submit(() -> {
                ready.countDown();
                if (!start.await(5, TimeUnit.SECONDS)){
                    throw new IllegalStateException("Start signal was not received");
                }
                try {
                    reservationService.createReservation(memberA.getId(), program.getId());
                    return true;
                }catch (NoSuchElementException exception){
                    return false;
                }
            });
            deletion = executor.submit(() -> {
                ready.countDown();
                if (!start.await(5, TimeUnit.SECONDS)){
                    throw new IllegalStateException("Start signal was not received");
                }
                try {
                    userManagementService.deleteUserData(memberA.getId());
                    return true;
                }catch (NoSuchElementException exception){
                    return false;
                }
            });
            assertTrue(ready.await(5, TimeUnit.SECONDS), "Both workers should become ready");
            start.countDown();
        }
        boolean creationValue = creation.get();
        boolean deletionValue = deletion.get();
        assertTrue(deletionValue);

        assertFalse(usersRepository.existsById(memberA.getId()));
        assertEquals(List.of(), reservationsRepository.findAllProgramidFromUser(memberA.getId()));
        assertTrue(usersRepository.existsById(memberB.getId()));
        assertEquals(List.of(program.getId()), reservationsRepository.findAllProgramidFromUser(memberB.getId()));
        Optional<Programs> savedProgram = programsRepository.findById(program.getId());
        assertTrue(savedProgram.isPresent());
        assertEquals(1, savedProgram.get().getPeoplereserve());

    }
}
