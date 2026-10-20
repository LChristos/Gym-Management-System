package com.example.API.controllers;

import com.example.API.dto.UserPrincipalDTO;
import com.example.API.models.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityController {

    private UserPrincipalDTO toUserDetailsDTO(UserPrincipal user) {
        UserPrincipalDTO dto = new UserPrincipalDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getAuthorities().iterator().next().getAuthority());
        return dto;
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken  csrfToken) {
        return csrfToken;
    }

    @GetMapping("/user")
    public UserPrincipalDTO getUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null){
            return new UserPrincipalDTO("VISITOR");
        }
        return toUserDetailsDTO(userPrincipal);
    }
}
