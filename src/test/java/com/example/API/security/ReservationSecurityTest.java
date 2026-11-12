package com.example.API.security;

import com.example.API.Repositories.*;
import com.example.API.dto.ReservationDTO;
import com.example.API.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReservationSecurityTest {
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

    private Users memberB;

    private Services service;
    private Trainers trainer;
    private Programs program;

    @AfterEach
    void removeEverything(){
        String[] usernames = {"owner-member" , "other-member"};
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
    void reservationCreationUsesAuthenticatedMemberInsteadOfSubmittedUserId() throws Exception{
        ReservationDTO request = new ReservationDTO();
        request.setProgramid(program.getId());
        request.setUserid(memberB.getId());
        String jsonBody = objectMapper.writeValueAsString(request);

        mvc.perform(post("/reservations/create")
                .with(user(new UserPrincipal(memberA)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername("owner-member"));

        assertEquals(List.of(program.getId()),reservationsRepository.findAllProgramidFromUser(memberA.getId()));
        assertTrue(reservationsRepository.findAllProgramidFromUser(memberB.getId()).isEmpty());
    }

    @Test
    void reservationCancelationOfAnotherUser() throws Exception{
        UserReservations res = new UserReservations(program,memberB);
        reservationsRepository.save(res);
        program.addpeople();
        programsRepository.save(program);

        mvc.perform(delete("/reservations/delete/{programid}", program.getId())
                .with(user(new UserPrincipal(memberA)))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(authenticated().withUsername("owner-member"));

        assertEquals(List.of(program.getId()), reservationsRepository.findAllProgramidFromUser(memberB.getId()));
    }

    @Test
    void memberCanCancelOwnReservation() throws Exception{
        UserReservations res = new UserReservations(program,memberB);
        res = reservationsRepository.save(res);
        program.addpeople();
        programsRepository.save(program);

        mvc.perform(delete("/reservations/delete/{programid}", program.getId())
                        .with(user(new UserPrincipal(memberB)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(memberB.getUsername()));

        assertTrue(reservationsRepository.findAllProgramidFromUser(memberB.getId()).isEmpty());
        assertFalse(reservationsRepository.existsById(res.getId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/reservations?userid={userid}", "/reservationuser?userid={userid}"})
    void reservationListsAreScopedToAuthenticatedMember(String endpoint) throws Exception{
        UserReservations res = new UserReservations(program,memberB);
        reservationsRepository.save(res);
        program.addpeople();
        programsRepository.save(program);

        mvc.perform(get(endpoint, memberB.getId())
                        .with(user(new UserPrincipal(memberA))))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(memberA.getUsername()))
                .andExpect(content().json("[]"));


        mvc.perform(get(endpoint, memberB.getId())
                        .with(user(new UserPrincipal(memberB))))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(memberB.getUsername()))
                .andExpect(jsonPath("$[0].id").value(program.getId()));
    }

    @Test
    void availableProgramsArePersonalizedForAuthenticatedMember() throws Exception{
        UserReservations res = new UserReservations(program,memberB);
        reservationsRepository.save(res);
        program.addpeople();
        programsRepository.save(program);

        mvc.perform(get("/reservationsavailable")
                        .with(user(new UserPrincipal(memberA))))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(memberA.getUsername()))
                .andExpect(jsonPath("$[0].id").value(program.getId()));

        mvc.perform(get("/reservationsavailable")
                        .with(user(new UserPrincipal(memberB))))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(memberB.getUsername()))
                .andExpect(content().json("[]"));

        assertEquals(List.of(program.getId()), reservationsRepository.findAllProgramidFromUser(memberB.getId()));
        assertEquals(List.of(), reservationsRepository.findByUser_Id(memberA.getId()));

    }
}
