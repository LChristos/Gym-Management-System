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
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
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

    @MockitoSpyBean
    ProgramsRepository programsRepository;

    @Autowired
    ReservationService reservationService;

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

    @Test
    void reservationCreationRollsBackWhenProgramSaveFails() {
        doThrow(new IllegalStateException("Counter save failed")).when(programsRepository).save(any(Programs.class));

        InvalidDataAccessApiUsageException failure = assertThrows(InvalidDataAccessApiUsageException.class,
                () -> reservationService.createReservation(
                        memberA.getId(),
                        program.getId()));

        assertInstanceOf(IllegalStateException.class , failure.getCause());
        assertEquals("Counter save failed", failure.getCause().getMessage());

        assertEquals(List.of(), reservationsRepository.findAllProgramidFromUser(memberA.getId()));
        Optional<Programs> serverProgram = programsRepository.findById(program.getId());
        assertTrue(serverProgram.isPresent());
        assertEquals(0, serverProgram.get().getPeoplereserve());

    }

    @Test
    void secondMemberCannotBookAFullProgram() throws Exception{
        program.setMax_people(1);
        program = programsRepository.save(program);


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

        List<Long> programsIdList = reservationsRepository.findAllProgramidFromUser(memberA.getId());
        assertEquals(List.of(program.getId()),programsIdList);
        Optional<Programs> serverProgram = programsRepository.findById(programsIdList.getFirst());
        assertTrue(serverProgram.isPresent());
        assertEquals(1, serverProgram.get().getPeoplereserve());


        ReservationDTO requestB = new ReservationDTO();
        requestB.setProgramid(program.getId());
        String jsonBodyB = objectMapper.writeValueAsString(requestB);

        MvcResult result = mvc.perform(post("/reservations/create")
                        .with(user(new UserPrincipal(memberB)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBodyB))
                .andExpect(status().isConflict())
                .andExpect(authenticated().withUsername("other-member"))
                .andReturn();

        String message = result.getResponse().getContentAsString();
        assertEquals("The program is full!", message);

        List<Long> programsIdListAfter = reservationsRepository.findAllProgramidFromUser(memberA.getId());
        assertEquals(List.of(program.getId()), programsIdListAfter);
        assertEquals(List.of(), reservationsRepository.findAllProgramidFromUser(memberB.getId()));
        Optional<Programs> persistedProgram= programsRepository.findById(programsIdListAfter.getFirst());
        assertTrue(persistedProgram.isPresent());
        assertEquals(1, persistedProgram.get().getPeoplereserve());
    }

    @Test
    void userCannotBookAProgramThatHasHappened() throws Exception{
        program.setProgramdate(program.getProgramdate().minusDays(5));
        program = programsRepository.save(program);

        ReservationDTO request = new ReservationDTO();
        request.setProgramid(program.getId());
        String jsonBody = objectMapper.writeValueAsString(request);

        mvc.perform(post("/reservations/create")
                        .with(user(new UserPrincipal(memberA)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(authenticated().withUsername("owner-member"));

        assertEquals(List.of(),reservationsRepository.findAllProgramidFromUser(memberA.getId()));
    }

    @Test
    void userCannotBookAProgramThatHasHappenedEarlierToday() throws Exception{
        program.setHour(LocalTime.now().minusMinutes(10));
        program.setProgramdate(LocalDate.now());
        program = programsRepository.save(program);

        ReservationDTO request = new ReservationDTO();
        request.setProgramid(program.getId());
        String jsonBody = objectMapper.writeValueAsString(request);

        mvc.perform(post("/reservations/create")
                        .with(user(new UserPrincipal(memberA)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(authenticated().withUsername("owner-member"));

        assertEquals(List.of(),reservationsRepository.findAllProgramidFromUser(memberA.getId()));
    }
}
