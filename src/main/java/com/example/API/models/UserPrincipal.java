package com.example.API.models;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final Users user;

    public UserPrincipal(Users user){
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {//What authority this specific User will have, put default as user for now
        String role = user.getRole();
        if (role.equalsIgnoreCase("admin")){
            return Collections.singleton(new SimpleGrantedAuthority("ADMIN"));
        }
        return Collections.singleton(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public Long getId(){
        return user.getId();
    }

    public String getRole(){
        return user.getRole();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getAccepted();
    }
}
