package com.portugease.user;

import com.portugease.common.enums.LearnerStatus;
import com.portugease.common.exception.ResourceNotFoundException;
import com.portugease.user.dto.UserSelectionResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/lookup")
    public UserSelectionResponse lookupUser(@RequestParam String username) {
        String normalisedUsername = username == null ? "" : username.trim();

        if (normalisedUsername.isBlank()) {
            throw new ResourceNotFoundException("Username is required");
        }

        User user = userRepository.findByUsername(normalisedUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + normalisedUsername));

        if (user.getStatus() != LearnerStatus.ACTIVE) {
            throw new ResourceNotFoundException("User is not active: " + normalisedUsername);
        }

        return new UserSelectionResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getStatus().name()
        );
    }
}