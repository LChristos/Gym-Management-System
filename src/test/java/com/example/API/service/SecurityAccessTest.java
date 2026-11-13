package com.example.API.service;

import com.example.API.Repositories.*;
import com.example.API.config.SecurityConfig;
import com.example.API.controllers.UsersController;
import com.example.API.filters.AuthenticationFilter;
import com.example.API.models.Articles;
import com.example.API.models.Users;
import org.hibernate.annotations.common.reflection.XMember;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
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

    @Autowired
    PasswordEncoder passwordEncoder;

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

    @ParameterizedTest
    @ValueSource(strings = {"/request", "/manageusers", "/manageservices/program"})
    @WithMockUser(username = "member", authorities = "USER")
    void userCannotReadAdminEndpoints(String endpoint) throws Exception{
        mvc.perform(get(endpoint))
                .andExpect(status().isForbidden())
                .andExpect(authenticated().withUsername("member"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/request", "/manageusers", "/manageservices/program"})
    @WithMockUser(username = "member", authorities = "ADMIN")
    void adminCanReadEndpoints(String endpoint) throws Exception{
        mvc.perform(get(endpoint))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"))
                .andExpect(authenticated().withUsername("member"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/request", "/manageusers"})
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void adminUserListsDoNotExposePasswordHashes(String endpoint) throws Exception{
        Users user = new Users();
        user.setId(42L);
        user.setUsername("member");
        user.setPassword(passwordEncoder.encode("pass1"));
        user.setRole("user");
        if (endpoint.equalsIgnoreCase("/manageusers")){
            user.setAccepted(true);
        }
        when(usersRepository.findByAcceptedFalse()).thenReturn(List.of(user));
        when(usersRepository.findByAcceptedTrue()).thenReturn(List.of(user));
        MvcResult result = mvc.perform((get(endpoint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("member"))
                .andExpect(jsonPath("$[0].password").doesNotHaveJsonPath())
                .andExpect(authenticated().withUsername("admin"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertFalse(responseBody.contains(user.getPassword()));
    }

    @Test
    void visitorCanReadServices() throws Exception{
        mvc.perform(get("/manageservices/services"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"))
                .andExpect(unauthenticated());
    }

    @Test
    void visitorIsRedirectedWhenReadingTrainers() throws Exception{
        mvc.perform(get("/manageservices/trainers"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost/Login_Register/newuser.html"))
                .andExpect(unauthenticated());
    }

    @ParameterizedTest
    @CsvSource({
            "USER, /manageservices/services",
            "ADMIN, /manageservices/services",
            "USER, /manageservices/trainers",
            "ADMIN, /manageservices/trainers"
    })
    void authenitcatedReadersCanReadServicesAndTrainers(String authority, String endpoint) throws Exception{
        mvc.perform(get(endpoint)
                .with(user("reader")
                        .authorities(new SimpleGrantedAuthority(authority))))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"))
                .andExpect(authenticated().withUsername("reader"));
    }

    @ParameterizedTest
    @CsvSource({
            "USER, 403, false",
            "ADMIN, 200, true"
    })
    void approvingAccountsRequiresAdmin(String authority , int expectedStatus, boolean expectedApproved) throws Exception{
        Users user = new Users();
        user.setId(42L);
        user.setUsername("member");
        user.setPassword(passwordEncoder.encode("pass1"));
        user.setRole("user");
        user.setAccepted(false);
        when(usersRepository.findById(user.getId())).thenReturn(Optional.of(user));
        MvcResult result = mvc.perform(post("/request/accepted/{id}", user.getId())
                        .with(user("approver")
                                .authorities(new SimpleGrantedAuthority(authority)))
                        .with(csrf()))
                .andExpect(status().is(expectedStatus))
                .andExpect(authenticated().withUsername("approver"))
                .andReturn();

        assertEquals(expectedApproved, user.getAccepted());
        if (expectedApproved){
            verify(usersRepository).save(user);
        }
        else{
            verifyNoInteractions(usersRepository);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "/Admin/Registration_Requests/users_request.html",
            "/User/Show_Announcements/announcement.html",
    })
    void visitorAreRedirectedFromPrivatePages(String page) throws Exception{
        mvc.perform(get(page))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost/Login_Register/newuser.html"))
                .andExpect(unauthenticated());
    }

    @ParameterizedTest
    @CsvSource({
            "USER, 403, /Admin/Registration_Requests/users_request.html",
            "ADMIN, 200, /Admin/Registration_Requests/users_request.html",
            "USER, 200, /User/Show_Announcements/announcement.html",
            "ADMIN, 200, /User/Show_Announcements/announcement.html",
    })
    void privatePageAccessFollowsAuthority(String authority, int expectedStatus, String page) throws Exception{
        mvc.perform(get(page)
                .with(user("viewer")
                        .authorities(new SimpleGrantedAuthority(authority))))
                .andExpect(status().is(expectedStatus))
                .andExpect(authenticated().withUsername("viewer"));
    }

    @Test
    void visitorCanOpenServicesPage() throws Exception{
        mvc.perform(get("/User/Show_Services/show_services.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(unauthenticated());
    }
}
