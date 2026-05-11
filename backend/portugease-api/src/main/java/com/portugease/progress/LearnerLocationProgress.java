package com.portugease.progress;

import com.portugease.common.enums.LocationStatus;
import com.portugease.location.Location;
import com.portugease.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "learner_location_progress")
public class LearnerLocationProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private LocationStatus status = LocationStatus.LOCKED;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(name = "completed_activities_count", nullable = false)
    private Integer completedActivitiesCount = 0;

    @Column(name = "total_required_activities_count", nullable = false)
    private Integer totalRequiredActivitiesCount = 0;

    @Column(name = "unlocked_at")
    private OffsetDateTime unlockedAt;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Column(name = "intro_dialogue_seen", nullable = false)
    private Boolean introDialogueSeen = false;

    @Column(name = "intro_dialogue_seen_at")
    private OffsetDateTime introDialogueSeenAt;

    @Column(name = "intro_dialogue_replay_count", nullable = false)
    private Integer introDialogueReplayCount = 0;

    public LearnerLocationProgress() {
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Location getLocation() {
        return location;
    }

    public LocationStatus getStatus() {
        return status;
    }

    public Integer getScore() {
        return score;
    }

    public Integer getCompletedActivitiesCount() {
        return completedActivitiesCount;
    }

    public Integer getTotalRequiredActivitiesCount() {
        return totalRequiredActivitiesCount;
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

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setStatus(LocationStatus status) {
        this.status = status;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setCompletedActivitiesCount(Integer completedActivitiesCount) {
        this.completedActivitiesCount = completedActivitiesCount;
    }

    public void setTotalRequiredActivitiesCount(Integer totalRequiredActivitiesCount) {
        this.totalRequiredActivitiesCount = totalRequiredActivitiesCount;
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

    public Boolean getIntroDialogueSeen() {
        return introDialogueSeen;
    }

    public void setIntroDialogueSeen(Boolean introDialogueSeen) {
        this.introDialogueSeen = introDialogueSeen;
    }

    public OffsetDateTime getIntroDialogueSeenAt() {
        return introDialogueSeenAt;
    }

    public void setIntroDialogueSeenAt(OffsetDateTime introDialogueSeenAt) {
        this.introDialogueSeenAt = introDialogueSeenAt;
    }

    public Integer getIntroDialogueReplayCount() {
        return introDialogueReplayCount;
    }

    public void setIntroDialogueReplayCount(Integer introDialogueReplayCount) {
        this.introDialogueReplayCount = introDialogueReplayCount;
    }
}