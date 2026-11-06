package com.example.API.service;

import com.example.API.Repositories.*;
import com.example.API.config.SecurityConfig;
import com.example.API.controllers.UsersController;
import com.example.API.filters.AuthenticationFilter;
import com.example.API.models.Articles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(UsersController.class)//Starts a Mock test with the controller
@Import({
        SecurityConfig.class,
        MyUserDetailsService.class,
        AuthenticationFilter.class
})
public class SecurityAccessTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    UsersRepository usersRepository;

    @MockitoBean
    TrainersRepository trainersRepository;

    @MockitoBean
    ServicesRepository servicesRepository;

    @MockitoBean
    ProgramsRepository programsRepository;

    @MockitoBean
    ArticlesRepository articlesRepository;

    @MockitoBean
    ReservationsRepository reservationsRepository;

    @Test
    void anonymousCannotReadAnnouncements() throws Exception {
        mvc.perform(get("/managearticles"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost/Login_Register/newuser.html"))
                .andExpect(unauthenticated());
    }

    @Test
    @WithMockUser(username = "member", authorities = "USER")
    void userCanReadAnnouncements() throws Exception{
        when(articlesRepository.findAll()).thenReturn(List.of());
        mvc.perform(get("/managearticles"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

    }

    @Test
    @WithMockUser(username = "member", authorities = "USER")
    void userCannotCreateAnnouncement() throws  Exception{
        mvc.perform(post("/managearticles/create").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Test announcement", "body":"Test body"}
                        """))
            .andExpect(status().isForbidden());
        verifyNoInteractions(articlesRepository);
    }

    @Test
    @WithMockUser(username = "member", authorities = "ADMIN")
    void adminCanCreateAnnouncement() throws Exception{
        mvc.perform(post("/managearticles/create").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Test announcement", "body":"Test body"}
                        """))
            .andExpect(status().isOk());
        verify(articlesRepository).save(any(Articles.class));
    }

    @Test
    @WithMockUser(username = "member", authorities = "ADMIN")
    void adminCannotCreateAnnouncementWithoutCsrf() throws Exception{
        mvc.perform(post("/managearticles/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Test announcement", "body":"Test body"}
                        """))
            .andExpect(status().isForbidden());
        verifyNoInteractions(articlesRepository);
    }

    @Test
    @WithMockUser(username = "member", authorities = "ADMIN")
    void adminCannotCreateAnnouncementWithInvalidCsrf() throws Exception{
        mvc.perform(post("/managearticles/create").with(csrf().useInvalidToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"Test announcement", "body":"Test body"}
                    """))
            .andExpect(status().isForbidden());
        verifyNoInteractions(articlesRepository);
    }
}
