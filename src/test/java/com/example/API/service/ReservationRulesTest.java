package com.example.API.service;

import com.example.API.Repositories.*;
import com.example.API.dto.ReservationDTO;
import com.example.API.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReservationRulesTest {

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

    @Autowired
    ProgramsRepository programsRepository;

    private Users memberA;
    private Services service;
    private Trainers trainer;
    private Programs program;

    @AfterEach
    void removeEverything(){
        String username = "owner-member";
        Users user = usersRepository.findByUsername(username);
        if(user != null){
            reservationsRepository.deleteAll(reservationsRepository.findByUser_Id(user.getId()));
            usersRepository.delete(user);
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
    void userTriesJoiningAProgramMultipleTimes() throws Exception{
        ReservationDTO request = new ReservationDTO();
        request.setProgramid(program.getId());
        String jsonBody = objectMapper.writeValueAsString(request);

        mvc.perform(post("/reservations/create")
                .with(user(new UserPrincipal(memberA)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername("owner-member"));

        assertEquals(List.of(program.getId()),reservationsRepository.findAllProgramidFromUser(memberA.getId()));

        mvc.perform(post("/reservations/create")
                        .with(user(new UserPrincipal(memberA)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(authenticated().withUsername("owner-member"));

        assertEquals(List.of(program.getId()),reservationsRepository.findAllProgramidFromUser(memberA.getId()));
        Optional<Programs> serverProgram = programsRepository.findById(program.getId());
        assertTrue(serverProgram.isPresent());
        assertEquals(1, serverProgram.get().getPeoplereserve());
    }
}
