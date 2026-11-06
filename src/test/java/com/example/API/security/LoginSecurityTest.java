package com.example.API.security;


import com.example.API.Repositories.UsersRepository;
import com.example.API.models.Users;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginSecurityTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private Users savedUser;

    @AfterEach
    void removeFixture(){
        if (savedUser != null){
            usersRepository.delete(savedUser);
        }
    }

    @BeforeEach
    void createApprovedAccount(){
        savedUser = new Users();
        savedUser.setUsername("approved-member");
        savedUser.setPassword(passwordEncoder.encode("password"));
        savedUser.setRole("USER");
        savedUser.setAccepted(true);
        usersRepository.save(savedUser);
    }

    @Test
    void approvedUserCanLoginWithCorrectPassword() throws Exception {
        mvc.perform(formLogin("/Login_Register/process-login")
                .user("approved-member")
                .password("password"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/User/Show_Services/show_services.html"))
                .andExpect(authenticated().withUsername("approved-member"));
    }

    @Test
    void approvedUserCannotLoginWithIncorrectPassword() throws Exception{
        mvc.perform(formLogin("/Login_Register/process-login")
                        .user("approved-member")
                        .password("wrong"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/Login_Register/newuser.html?error=true"))
                .andExpect(unauthenticated());
    }

    @Test
    void unapprovedUserCannotLoginEvenWithCorrectPassword() throws Exception {
        savedUser.setAccepted(false);
        usersRepository.save(savedUser);
        mvc.perform(formLogin("/Login_Register/process-login")
                        .user("approved-member")
                        .password("password"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/Login_Register/newuser.html?pending"))
                .andExpect(unauthenticated());
    }

    @Test
    void approvedAdminCanLoginWithCorrectPassword() throws Exception{
        savedUser.setRole("Admin");
        usersRepository.save(savedUser);
        mvc.perform(formLogin("/Login_Register/process-login")
                        .user("approved-member")
                        .password("password"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/User/Show_Services/show_services.html"))
                .andExpect(authenticated().withUsername("approved-member"))
                .andExpect(authenticated().withAuthorities(Collections.singleton(new SimpleGrantedAuthority("ADMIN"))));
    }

    @Test
    void approvedAdminCannotLoginWithIncorrectPassword() throws Exception{
        savedUser.setRole("Admin");
        usersRepository.save(savedUser);
        mvc.perform(formLogin("/Login_Register/process-login")
                        .user("approved-member")
                        .password("wrong"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/Login_Register/newuser.html?error=true"))
                .andExpect(unauthenticated());
    }


    @Test
    void loginSessionRemainsAuthenticatedOnNextRequest() throws Exception{
        MvcResult loginResult = mvc.perform(formLogin("/Login_Register/process-login")
                        .user("approved-member")
                        .password("password"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/User/Show_Services/show_services.html"))
                .andExpect(authenticated().withUsername("approved-member"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);

        mvc.perform(get("/user").session(session))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername("approved-member"))
                .andExpect(jsonPath("$.username").value("approved-member"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void loginSessionDoNotRemainAuthenticatedOnNextRequest() throws Exception{
        MvcResult loginResult = mvc.perform(formLogin("/Login_Register/process-login")
                        .user("approved-member")
                        .password("password"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/User/Show_Services/show_services.html"))
                .andExpect(authenticated().withUsername("approved-member"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);

        savedUser.setRole("Admin");
        usersRepository.save(savedUser);
        mvc.perform(get("/user").session(session))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/Login_Register/newuser.html"))
                .andExpect(unauthenticated());
        assertTrue(session.isInvalid());
    }

    @Test
    void approvalRevocationInvalidatesExistingSession() throws Exception{
        MvcResult loginResult = mvc.perform(formLogin("/Login_Register/process-login")
                        .user("approved-member")
                        .password("password"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/User/Show_Services/show_services.html"))
                .andExpect(authenticated().withUsername("approved-member"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);

        savedUser.setAccepted(false);
        usersRepository.save(savedUser);
        mvc.perform(get("/user").session(session))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/Login_Register/newuser.html"))
                .andExpect(unauthenticated());
        assertTrue(session.isInvalid());
    }

    @Test
    void deletionOfUserDuringSession() throws Exception {
        MvcResult loginResult = mvc.perform(formLogin("/Login_Register/process-login")
                        .user("approved-member")
                        .password("password"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/User/Show_Services/show_services.html"))
                .andExpect(authenticated().withUsername("approved-member"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);

        usersRepository.delete(savedUser);
        mvc.perform(get("/user").session(session))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/Login_Register/newuser.html"))
                .andExpect(unauthenticated());
        assertTrue(session.isInvalid());
    }

    @Test
    void changeUsernameInvalidatesExistingSession() throws Exception {
        MvcResult loginResult = mvc.perform(formLogin("/Login_Register/process-login")
                        .user("approved-member")
                        .password("password"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/User/Show_Services/show_services.html"))
                .andExpect(authenticated().withUsername("approved-member"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);

        savedUser.setUsername("member");
        usersRepository.save(savedUser);
        mvc.perform(get("/user").session(session))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/Login_Register/newuser.html"))
                .andExpect(unauthenticated());
        assertTrue(session.isInvalid());
    }

    @Test
    void logoutInvalidatesSessionAndRemovesAuthentication() throws Exception{
        MvcResult loginResult = mvc.perform(formLogin("/Login_Register/process-login")
                        .user("approved-member")
                        .password("password"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/User/Show_Services/show_services.html"))
                .andExpect(authenticated().withUsername("approved-member"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);

        mvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/User/Show_Services/show_services.html"))
                .andExpect(unauthenticated());
        assertTrue(session.isInvalid());
    }

    @Test
    void logoutWithoutCsrfDoesNotEndAuthenticatedSession() throws Exception{
        MvcResult loginResult = mvc.perform(formLogin("/Login_Register/process-login")
                        .user("approved-member")
                        .password("password"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/User/Show_Services/show_services.html"))
                .andExpect(authenticated().withUsername("approved-member"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);

        mvc.perform(post("/logout").session(session))
                .andExpect(status().isForbidden())
                .andExpect(authenticated().withUsername("approved-member"));
        assertFalse(session.isInvalid());

        mvc.perform(get("/managearticles").session(session))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername("approved-member"));
    }
}
