package com.portugease.activity;

import com.portugease.location.Location;
import com.portugease.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "learner_activity_progress")
public class LearnerActivityProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "is_completed", nullable = false)
    private Boolean completed = false;

    @Column(name = "is_mastered", nullable = false)
    private Boolean mastered = false;

    @Column(name = "best_score", nullable = false)
    private Integer bestScore = 0;

    @Column(name = "max_score", nullable = false)
    private Integer maxScore = 1;

    @Column(name = "attempts_count", nullable = false)
    private Integer attemptsCount = 0;

    @Column(name = "incorrect_attempts_count", nullable = false)
    private Integer incorrectAttemptsCount = 0;

    @Column(name = "first_attempt_at")
    private OffsetDateTime firstAttemptAt;

    @Column(name = "last_attempt_at")
    private OffsetDateTime lastAttemptAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public LearnerActivityProgress() {
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Activity getActivity() {
        return activity;
    }

    public Location getLocation() {
        return location;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public Boolean getMastered() {
        return mastered;
    }

    public Integer getBestScore() {
        return bestScore;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public Integer getAttemptsCount() {
        return attemptsCount;
    }

    public Integer getIncorrectAttemptsCount() {
        return incorrectAttemptsCount;
    }

    public OffsetDateTime getFirstAttemptAt() {
        return firstAttemptAt;
    }

    public OffsetDateTime getLastAttemptAt() {
        return lastAttemptAt;
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

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public void setMastered(Boolean mastered) {
        this.mastered = mastered;
    }

    public void setBestScore(Integer bestScore) {
        this.bestScore = bestScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public void setAttemptsCount(Integer attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    public void setIncorrectAttemptsCount(Integer incorrectAttemptsCount) {
        this.incorrectAttemptsCount = incorrectAttemptsCount;
    }

    public void setFirstAttemptAt(OffsetDateTime firstAttemptAt) {
        this.firstAttemptAt = firstAttemptAt;
    }

    public void setLastAttemptAt(OffsetDateTime lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}