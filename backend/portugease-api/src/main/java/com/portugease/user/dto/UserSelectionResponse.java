package com.portugease.user.dto;

import java.util.UUID;

public record UserSelectionResponse(
        UUID id,
        String username,
        String displayName,
        String status
) {
}