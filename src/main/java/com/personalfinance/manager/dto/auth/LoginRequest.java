package com.personalfinance.manager.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Username must not be blank")
    @Email(message = "Username must be a valid email address")
    private String username;

    @NotBlank(message = "Password must not be blank")
    private String password;
}
