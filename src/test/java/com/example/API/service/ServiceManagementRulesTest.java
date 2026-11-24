package com.example.API.service;

import com.example.API.Repositories.*;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ServiceManagementRulesTest {

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
    ServiceManagementService serviceManagementService;

    private Users memberA;
    private Users memberB;
    private Services serviceA;
    private Services serviceB;
    private Trainers trainer;
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
        if (serviceA != null){
            servicesRepository.delete(serviceA);
        }
        if (serviceB != null){
            servicesRepository.delete(serviceB);
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

        serviceA = new Services();
        serviceA.setName("service");
        serviceA.setMax_people(10);
        serviceA = servicesRepository.save(serviceA);

        serviceB = new Services();
        serviceB.setName("other-service");
        serviceB.setMax_people(10);
        serviceB = servicesRepository.save(serviceB);

        trainer = new Trainers("trainerA", "tr");
        trainer = trainersRepository.save(trainer);

        program = new Programs(serviceA, LocalDate.now().plusDays(3), LocalTime.of(12, 0), trainer, 10);
        program = programsRepository.save(program);

        programB = new Programs(serviceB, LocalDate.now().minusDays(5), LocalTime.of(12, 0), trainer, 10);
        programB = programsRepository.save(programB);

        programC = new Programs(serviceB, LocalDate.now().plusDays(6), LocalTime.of(12, 0), trainer, 10);
        programC = programsRepository.save(programC);
    }

    @Test
    void deleteService() {
        programB.addpeople();
        programB = programsRepository.save(programB);

        UserReservations oldReservation = new UserReservations(programB,memberA);
        oldReservation = reservationsRepository.save(oldReservation);
        reservationService.createReservation(memberA.getId(), program.getId());
        reservationService.createReservation(memberA.getId(), programC.getId());

        serviceManagementService.deleteServiceData(serviceB.getId());

        Optional<Programs> oldProgram =programsRepository.findById(programB.getId());
        assertTrue(oldProgram.isPresent());
        assertNull(oldProgram.get().getService());
        assertTrue(reservationsRepository.existsById(oldReservation.getId()));
        assertEquals(1, oldProgram.get().getPeoplereserve());
        assertFalse(servicesRepository.existsById(serviceB.getId()));

        assertFalse(programsRepository.existsById(programC.getId()));
        assertNull(reservationsRepository.findByUser_IdAndProgram_IdOrderByCreatedAtDesc(memberA.getId(), programC.getId()));

        assertNotNull(reservationsRepository.findByUser_IdAndProgram_IdOrderByCreatedAtDesc(memberA.getId(), program.getId()));
        assertTrue(servicesRepository.existsById(serviceA.getId()));
        assertTrue(programsRepository.existsById(program.getId()));


    }
}
