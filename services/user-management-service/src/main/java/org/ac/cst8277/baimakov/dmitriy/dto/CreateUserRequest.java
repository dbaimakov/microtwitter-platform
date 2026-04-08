package org.ac.cst8277.baimakov.dmitriy.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String displayName,
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotEmpty Set<String> roles
) {
}
