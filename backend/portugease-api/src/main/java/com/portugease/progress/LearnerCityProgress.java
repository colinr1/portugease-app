package com.portugease.progress;

import com.portugease.city.City;
import com.portugease.common.enums.CityStatus;
import com.portugease.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "learner_city_progress")
public class LearnerCityProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CityStatus status = CityStatus.LOCKED;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(name = "completed_locations_count", nullable = false)
    private Integer completedLocationsCount = 0;

    @Column(name = "total_locations_count", nullable = false)
    private Integer totalLocationsCount = 0;

    @Column(name = "unlocked_at")
    private OffsetDateTime unlockedAt;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public LearnerCityProgress() {
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

    public CityStatus getStatus() {
        return status;
    }

    public Integer getScore() {
        return score;
    }

    public Integer getCompletedLocationsCount() {
        return completedLocationsCount;
    }

    public Integer getTotalLocationsCount() {
        return totalLocationsCount;
    }

    public OffsetDateTime getUnlockedAt() {
        return unlockedAt;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
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

    public void setStatus(CityStatus status) {
        this.status = status;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setCompletedLocationsCount(Integer completedLocationsCount) {
        this.completedLocationsCount = completedLocationsCount;
    }

    public void setTotalLocationsCount(Integer totalLocationsCount) {
        this.totalLocationsCount = totalLocationsCount;
    }

    public void setUnlockedAt(OffsetDateTime unlockedAt) {
        this.unlockedAt = unlockedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}