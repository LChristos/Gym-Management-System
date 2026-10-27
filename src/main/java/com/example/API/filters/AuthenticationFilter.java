package com.example.API.filters;

import com.example.API.Repositories.UsersRepository;
import com.example.API.models.UserPrincipal;
import com.example.API.models.Users;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final UsersRepository usersRepository;

    public AuthenticationFilter(UsersRepository usersRepository){
        this.usersRepository = usersRepository;
    }
    //TODO Add also to display a message to the user before ask them to log in again

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication == null){
            filterChain.doFilter(request, response);
            return;
        }
        if(!(authentication.getPrincipal() instanceof UserPrincipal)){//When is Visitor and does not have user
            filterChain.doFilter(request, response);
            return;
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Users user = usersRepository.findById(principal.getId()).orElse(null);
        if (user != null ) {
            if (user.getUsername().equals(principal.getUsername()) && user.getRole().equalsIgnoreCase(principal.getRole()) && user.getAccepted() == principal.isEnabled()) {
                filterChain.doFilter(request, response);
                return;//successfully authenticated
            }
        }
        SecurityContextLogoutHandler logoutHandler= new SecurityContextLogoutHandler();
        logoutHandler.logout(request,response,null);
        response.sendRedirect("/Login_Register/newuser.html");
    }

}
