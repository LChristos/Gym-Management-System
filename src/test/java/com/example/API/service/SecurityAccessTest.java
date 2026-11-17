package com.example.API.service;

import com.example.API.Repositories.*;
import com.example.API.config.SecurityConfig;
import com.example.API.controllers.UsersController;
import com.example.API.filters.AuthenticationFilter;
import com.example.API.models.Articles;
import com.example.API.models.Users;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(UsersController.class)//Starts a Mock test with the controller
@Import({
        SecurityConfig.class,
        MyUserDetailsService.class,
        AuthenticationFilter.class,
        ReservationService.class
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

    @Autowired
    ObjectMapper objectMapper;

    @MockitoSpyBean
    UsersController usersController;

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
    @CsvSource({
            "USER, 403, /managearticles/create",
            "ADMIN, 200, /managearticles/create"
    })
    void announcementCreationNeedsAdminRole(String authority, int expectedStatus, String endpoint) throws Exception{
        Articles article = new Articles("title", "this is a body");
        String jsonBody = objectMapper.writeValueAsString(article);
        mvc.perform(post(endpoint)
                        .with(user("member")
                        .authorities(new SimpleGrantedAuthority(authority)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().is(expectedStatus))
                .andExpect(authenticated().withUsername("member"));

        if(authority.equalsIgnoreCase("ADMIN")){
            verify(articlesRepository).save(any(Articles.class));
        }
        else {
            verifyNoInteractions(articlesRepository);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "USER, 403, /managearticles/delete/{id}",
            "ADMIN, 200, /managearticles/delete/{id}"
    })
    void announcementDeletionNeedsAdminRole(String authority, int expectedStatus, String endpoint) throws Exception{
        Articles article = new Articles("title", "this is a body");
        article.setId(32L);
        mvc.perform(delete(endpoint, article.getId())
                        .with(user("member")
                                .authorities(new SimpleGrantedAuthority(authority)))
                        .with(csrf()))
                .andExpect(status().is(expectedStatus))
                .andExpect(authenticated().withUsername("member"));

        if(authority.equalsIgnoreCase("ADMIN")){
            verify(articlesRepository).deleteById(article.getId());
        }
        else {
            verifyNoInteractions(articlesRepository);
        }
    }


    @ParameterizedTest
    @CsvSource({
            "USER, 403, /managearticles/change",
            "ADMIN, 200, /managearticles/change"
    })
    void announcementChangesNeedsAdminRole(String authority, int expectedStatus, String endpoint) throws Exception{
        Articles existArticle = new Articles("title", "this is a body");
        existArticle.setId(12L);
        when(articlesRepository.findById(existArticle.getId())).thenReturn(Optional.of(existArticle));
        Articles changeArticle= new Articles("Test announcement", "Test body");
        changeArticle.setId(12L);
        String jsonBody = objectMapper.writeValueAsString(changeArticle);
        mvc.perform(post(endpoint)
                        .with(user("member")
                                .authorities(new SimpleGrantedAuthority(authority)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().is(expectedStatus))
                .andExpect(authenticated().withUsername("member"));

        if(authority.equalsIgnoreCase("ADMIN")){
            assertEquals(changeArticle.getTitle(), existArticle.getTitle());
            assertEquals(changeArticle.getBody(), existArticle.getBody());
            verify(articlesRepository).save(existArticle);
        }
        else {
            assertNotEquals(changeArticle.getTitle(), existArticle.getTitle());
            assertNotEquals(changeArticle.getBody(), existArticle.getBody());
            verifyNoInteractions(articlesRepository);
        }
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
        mvc.perform(post("/request/accepted/{id}", user.getId())
                        .with(user("approver")
                                .authorities(new SimpleGrantedAuthority(authority)))
                        .with(csrf()))
                .andExpect(status().is(expectedStatus))
                .andExpect(authenticated().withUsername("approver"));

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

    @ParameterizedTest
    @CsvSource({
            "USER, 403",
            "ADMIN, 200"
    })
    void imageUploadRequiresAdmin(String authority, int expectedStatus) throws Exception{
        doReturn(ResponseEntity.ok(Map.of("imageUrl", "/Images/test-upload.jpg"))).when(usersController).upload_image(any(MultipartFile.class));

        MockMultipartFile image;
        try (var input = getClass().getResourceAsStream("/fixtures/crossfit.jpg")){
            assertNotNull(input);

            image = new MockMultipartFile("image", "crossfit.jpg", "image/jpeg", input);
        }

        mvc.perform(multipart("/uploadimage").file(image)
                .with(user("uploader")
                        .authorities(new SimpleGrantedAuthority(authority)))
                .with(csrf()))
                .andExpect(status().is(expectedStatus))
                .andExpect(authenticated().withUsername("uploader"));

        if (authority.equalsIgnoreCase("ADMIN")){
            verify(usersController).upload_image(any(MultipartFile.class));
        }
        else {
            verify(usersController, never()).upload_image(any(MultipartFile.class));
        }

    }
}
