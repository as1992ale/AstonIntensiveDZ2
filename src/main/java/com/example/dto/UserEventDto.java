package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEventDto {
    private String eventType; // "CREATED" или "DELETED"
    private String email;
    private String name;
    private Long userId;
}