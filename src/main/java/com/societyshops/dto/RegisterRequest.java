package com.societyshops.dto;

import com.societyshops.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String name;

    @Email @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String phone;

    @NotNull
    private Role role;
}
