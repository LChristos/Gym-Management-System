package com.example.API.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static jakarta.servlet.DispatcherType.ERROR;
import static jakarta.servlet.DispatcherType.FORWARD;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(Customizer.withDefaults());
        http.authorizeHttpRequests((requests) -> requests
                        .dispatcherTypeMatchers(FORWARD,ERROR).permitAll()
                        .requestMatchers("/Login_Register/**", "/User/Show_Services/**", "/Images/*" , "/csrf", "/js/csrf.js", "/signup", "/manageservices/services", "/user" , "/navigation_bar.js").permitAll()
                        .requestMatchers("/manageservices/trainers" , "/manageservices/services").authenticated()
                        .requestMatchers("/Admin/**" , "/request/**" , "/manageusers/**", "/manageservices/**" , "/uploadimage").hasAuthority("ADMIN")
                        .requestMatchers("/managearticles/create", "/managearticles/change" , "/managearticles/delete/**").hasAuthority("ADMIN")
                        .requestMatchers("/User/**").authenticated() //it doesn't matter if ADMIN has access to this
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/Login_Register/newuser.html")
                        .loginProcessingUrl("/Login_Register/process-login")
                        .defaultSuccessUrl("/User/Show_Services/show_services.html", true)
                        .failureUrl("/Login_Register/newuser.html?error=true")
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutSuccessUrl("/User/Show_Services/show_services.html")
                        .deleteCookies("JSESSIONID")
                );
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }
}
