package com.example.API.service;

import com.example.API.Repositories.*;
import com.example.API.dto.ProgramDTO;
import com.example.API.models.Programs;
import com.example.API.models.Services;
import com.example.API.models.Trainers;
import com.example.API.models.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProgramRulesTest {

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
    void userChangeProgramData() throws Exception{
        reservationService.createReservation(memberA.getId(), program.getId());
        reservationService.createReservation(memberB.getId(), program.getId());
        ProgramDTO programDTO = new ProgramDTO();
        programDTO.setId(program.getId());
        programDTO.setServices_name(program.getService().getId());
        programDTO.setProgramdate(program.getProgramdate());
        programDTO.setHour(program.getHour());
        programDTO.setTrainer_id(program.getTrainer().getId());
        programDTO.setMax_people(1);
        String jsonBody = objectMapper.writeValueAsString(programDTO);

        mvc.perform(post("/manageservices/program/change")
                        .with(user("admin")
                        .authorities(new SimpleGrantedAuthority("ADMIN")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(authenticated().withUsername("admin"));

        Optional<Programs> savedProgram = programsRepository.findById(program.getId());
        assertTrue(savedProgram.isPresent());
        assertEquals(10, savedProgram.get().getMax_people());
        assertEquals(2 , savedProgram.get().getPeoplereserve());
    }
}
