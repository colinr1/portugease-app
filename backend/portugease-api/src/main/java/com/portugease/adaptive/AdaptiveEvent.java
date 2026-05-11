package com.portugease.adaptive;

import com.portugease.activity.Activity;
import com.portugease.activity.ActivityAttempt;
import com.portugease.city.City;
import com.portugease.common.enums.AdaptiveEventType;
import com.portugease.location.Location;
import com.portugease.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "adaptive_events")
public class AdaptiveEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id")
    private ActivityAttempt attempt;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 80)
    private AdaptiveEventType eventType;

    @Column(columnDefinition = "text")
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> contextJson = new HashMap<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public AdaptiveEvent() {
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public City getCity() {
        return city;
    }

    public Location getLocation() {
        return location;
    }

    public Activity getActivity() {
        return activity;
    }

    public ActivityAttempt getAttempt() {
        return attempt;
    }

    public AdaptiveEventType getEventType() {
        return eventType;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getContextJson() {
        return contextJson;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setAttempt(ActivityAttempt attempt) {
        this.attempt = attempt;
    }

    public void setEventType(AdaptiveEventType eventType) {
        this.eventType = eventType;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setContextJson(Map<String, Object> contextJson) {
        this.contextJson = contextJson;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}