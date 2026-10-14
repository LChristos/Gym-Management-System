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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(Customizer.withDefaults());
        http.authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/Login_Register/**", "/User/Show_Services/**", "/Images/*" , "/csrf").permitAll()
                        .requestMatchers("/Admin/**").authenticated()
                        .requestMatchers("/User/**").authenticated() //.hasRole("USER")
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
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(logout.getLogoutSuccessHandler())
                );
        return http.build();
    }
//    @Bean Inmemory example
//    public UserDetailsService userDetailsService(){
//
//        UserDetails user1 = User
//                .withDefaultPasswordEncoder()
//                .username("kiram")
//                .password("kiran123")
//                .roles("USER")
//                .build();
//
//        UserDetails user2 = User
//                .withDefaultPasswordEncoder()
//                .username("qaz")
//                .password("qaz123")
//                .roles("ADMIN")
//                .build();
//
//        return new InMemoryUserDetailsManager(user1 , user2);
//    }
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }
}
