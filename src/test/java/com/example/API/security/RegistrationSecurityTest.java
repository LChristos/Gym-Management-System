package com.example.API.security;

import com.example.API.Repositories.UsersRepository;
import com.example.API.models.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RegistrationSecurityTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @AfterEach
    void removeSignupAccount() {
        String[] usernames = {"signup-member", "existing-member"};
        for (String username : usernames){
            Users user = usersRepository.findByUsername(username);
            if (user != null){
                usersRepository.delete(user);
            }
        }
    }

    @Test
    void signupForcesPendingUserAndHashesPassword() throws Exception {
        Users user = new Users("John" , "Doe", "Greece", "Athens" , "Athens 21", "john@doe.com", "signup-member", "pass123");
        user.setRole("Admin");
        user.setAccepted(true);
        String jsonBody = objectMapper.writeValueAsString(user);
        mvc.perform(post("/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(unauthenticated());

        Users registered = usersRepository.findByUsername("signup-member");
        assertNotNull(registered);
        assertEquals("user", registered.getRole());
        assertFalse(registered.getAccepted());
        assertNotEquals(user.getPassword(), registered.getPassword());
        assertTrue(passwordEncoder.matches(user.getPassword() , registered.getPassword()));
    }

    @Test
    void signupIgnoresClientIdAndPreservesExistingAccount() throws Exception{
        Users savedUser = new Users();
        savedUser.setUsername("existing-member");
        savedUser.setPassword(passwordEncoder.encode("password"));
        savedUser.setRole("user");
        savedUser.setAccepted(true);
        usersRepository.save(savedUser);

        Users existingUser = usersRepository.findByUsername("existing-member");
        Users signUser = new Users("John" , "Doe", "Greece", "Athens" , "Athens 21", "john@doe.com", "signup-member", "pass123");
        signUser.setId(existingUser.getId());
        String jsonBody = objectMapper.writeValueAsString(signUser);

        mvc.perform(post("/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(unauthenticated());

        Users savedExistingUser = usersRepository.findByUsername("existing-member");
        Users savedSignUser = usersRepository.findByUsername("signup-member");
        assertNotNull(savedSignUser);
        assertNotNull(savedExistingUser);
        assertNotNull(savedSignUser.getId());
        //Checks for existing user
        assertEquals(existingUser.getId(), savedExistingUser.getId());
        assertEquals(existingUser.getUsername(), savedExistingUser.getUsername());
        assertEquals(existingUser.getPassword(), savedExistingUser.getPassword());
        assertEquals(existingUser.getRole(), savedExistingUser.getRole());
        assertEquals(existingUser.getAccepted(), savedExistingUser.getAccepted());


        assertNotEquals(savedExistingUser.getId(), savedSignUser.getId());

    }

    @Test
    void signupWithoutCsrfDoesNotCreateAccount() throws Exception{

        Users savedUser = new Users("John" , "Doe", "Greece", "Athens" , "Athens 21", "john@doe.com", "signup-member", "pass123");
        String jsonBody = objectMapper.writeValueAsString(savedUser);

        mvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isForbidden())
                .andExpect(unauthenticated());

        assertNull(usersRepository.findByUsername("signup-member"));
    }
}
