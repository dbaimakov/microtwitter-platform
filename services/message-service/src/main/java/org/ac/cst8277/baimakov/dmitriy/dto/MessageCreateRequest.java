package org.ac.cst8277.baimakov.dmitriy.dto;

import javax.validation.constraints.NotBlank;

public record MessageCreateRequest(@NotBlank String content) {
}
