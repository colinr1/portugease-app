package com.portugease.adaptive;

import com.portugease.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdaptiveEventRepository extends JpaRepository<AdaptiveEvent, UUID> {
    List<AdaptiveEvent> findAllByUserOrderByCreatedAtDesc(User user);
}