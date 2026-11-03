package com.example.API.service;

import com.example.API.Repositories.UsersRepository;
import com.example.API.models.UserPrincipal;
import com.example.API.models.Users;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    public MyUserDetailsService(UsersRepository usersRepository){
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Users user = usersRepository.findByUsername(username);
        if ( user == null){
            throw new UsernameNotFoundException("user not found");
        }

        return new UserPrincipal(user);
    }
}
