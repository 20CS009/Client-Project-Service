package com.example.client.project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteConfirmationDto {
    private String message;

    public DeleteConfirmationDto(String message) {
        this.message = message;
    }
}
