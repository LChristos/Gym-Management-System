package com.example.API.service;

import com.example.API.Repositories.UsersRepository;
import com.example.API.models.Users;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MyUserDetailsServiceTest {

    @Test
    void WrongUsernameinLogin(){
        UsersRepository usersRepository = mock(UsersRepository.class);
        MyUserDetailsService userDetailsService = new MyUserDetailsService(usersRepository);
        assertThrows(UsernameNotFoundException.class,
                () -> {
                    userDetailsService.loadUserByUsername("missing-member");
                });
    }

    @Test
    void unapprovedUserIsDisabled(){
        UsersRepository usersRepository = mock(UsersRepository.class);
        Users user = new Users();
        user.setUsername("pending-member");
        user.setRole("USER");
        user.setAccepted(false);
        when(usersRepository.findByUsername("pending-member")).thenReturn(user);
        MyUserDetailsService userDetailsService = new MyUserDetailsService(usersRepository);
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        assertFalse(userDetails.isEnabled());
    }

}