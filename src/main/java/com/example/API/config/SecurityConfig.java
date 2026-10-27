package com.example.API.config;

import com.example.API.filters.AuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.util.Map;

import static jakarta.servlet.DispatcherType.ERROR;
import static jakarta.servlet.DispatcherType.FORWARD;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    private final AuthenticationFilter authenticationFilter;

    public SecurityConfig(UserDetailsService userDetailsService, AuthenticationFilter authenticationFilter){
        this.userDetailsService= userDetailsService;
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationFailureHandler loginFailureHandler) throws Exception {
        http.csrf(Customizer.withDefaults());
        http.addFilterAfter( authenticationFilter, BasicAuthenticationFilter.class);
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
                        .failureHandler(loginFailureHandler)
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutSuccessUrl("/User/Show_Services/show_services.html")
                        .deleteCookies("JSESSIONID")
                );
        return http.build();
    }

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> activeAuthenticationFilterRegistration(AuthenticationFilter filter) {
        FilterRegistrationBean<AuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public AuthenticationFailureHandler loginFailureHandler() {
        ExceptionMappingAuthenticationFailureHandler handler = new ExceptionMappingAuthenticationFailureHandler();
        handler.setExceptionMappings(Map.of(
                DisabledException.class.getName(),
                "/Login_Register/newuser.html?pending"
        ));

        handler.setDefaultFailureUrl(
                "/Login_Register/newuser.html?error=true"
        );
        return handler;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public PasswordEncoder getPasswordEncoder(){
        return new BCryptPasswordEncoder(12);
    }
}
