package com.portugease.adaptive;

import com.portugease.adaptive.dto.AdaptiveEventResponse;
import com.portugease.user.DemoUserService;
import com.portugease.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdaptiveService {

    private final AdaptiveEventRepository adaptiveEventRepository;
    private final DemoUserService demoUserService;

    public AdaptiveService(
            AdaptiveEventRepository adaptiveEventRepository,
            DemoUserService demoUserService
    ) {
        this.adaptiveEventRepository = adaptiveEventRepository;
        this.demoUserService = demoUserService;
    }

    @Transactional(readOnly = true)
    public List<AdaptiveEventResponse> getRecentEvents() {
        User user = demoUserService.getDemoUser();

        return adaptiveEventRepository.findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(event -> new AdaptiveEventResponse(
                        event.getId(),
                        event.getEventType(),
                        event.getMessage(),
                        event.getContextJson(),
                        event.getCreatedAt()
                ))
                .toList();
    }
}