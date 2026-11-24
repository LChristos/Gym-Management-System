package com.example.API.service;

import com.example.API.Repositories.*;
import com.example.API.exceptions.programs.ProgramExpiredException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TrainerManagementRulesTest {

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
    ProgramManagementService programManagementService;

    @Autowired
    UserManagementService userManagementService;

    @Autowired
    TrainerManagementService trainerManagementService;

    private Users memberA;
    private Users memberB;
    private Services service;
    private Trainers trainer;
    private Trainers trainerB;
    private Programs program;
    private Programs programB;
    private Programs programC;

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
        if(programB != null){
            programsRepository.delete(programB);
        }
        if(programC != null){
            programsRepository.delete(programC);
        }
        if (trainer != null){
            trainersRepository.delete(trainer);
        }
        if (trainerB != null){
            trainersRepository.delete(trainerB);
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

        trainerB = new Trainers("trainerB", "trainer");
        trainerB = trainersRepository.save(trainerB);

        program = new Programs(service, LocalDate.now().plusDays(3), LocalTime.of(12, 0), trainer, 10);
        program = programsRepository.save(program);

        programB = new Programs(service, LocalDate.now().minusDays(5), LocalTime.of(12, 0), trainerB, 10);
        programB = programsRepository.save(programB);

        programC = new Programs(service, LocalDate.now().plusDays(6), LocalTime.of(12, 0), trainerB, 10);
        programC = programsRepository.save(programC);
    }

    @Test
    void deleteTrainer() {

        programB.addpeople();
        programB = programsRepository.save(programB);

        UserReservations oldReservation = new UserReservations(programB,memberB);
        oldReservation = reservationsRepository.save(oldReservation);
        reservationService.createReservation(memberA.getId(), program.getId());
        reservationService.createReservation(memberB.getId(), programC.getId());

        trainerManagementService.deleteTrainerData(trainerB.getId());

        Optional<Programs> oldProgram =programsRepository.findById(programB.getId());
        assertTrue(oldProgram.isPresent());
        assertNull(oldProgram.get().getTrainer());
        assertTrue(reservationsRepository.existsById(oldReservation.getId()));
        assertEquals(1, oldProgram.get().getPeoplereserve());
        assertFalse(trainersRepository.existsById(trainerB.getId()));

        assertFalse(programsRepository.existsById(programC.getId()));
        assertNull(reservationsRepository.findByUser_IdAndProgram_IdOrderByCreatedAtDesc(memberB.getId(), programC.getId()));

        Optional<Programs> savedUpComingProgram = programsRepository.findById(program.getId());
        assertTrue(savedUpComingProgram.isPresent());
        assertEquals(1, savedUpComingProgram.get().getPeoplereserve());
        assertNotNull(reservationsRepository.findByUser_IdAndProgram_IdOrderByCreatedAtDesc(memberA.getId(), program.getId()));

    }

}
