package com.portugease.user;

import com.portugease.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoUserService {

    private static final String DEMO_USERNAME = "demo";

    private final UserRepository userRepository;

    public DemoUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User getDemoUser() {
        return userRepository.findByUsername(DEMO_USERNAME)
                .orElseThrow(() -> new ResourceNotFoundException("Demo user not found"));
    }
}