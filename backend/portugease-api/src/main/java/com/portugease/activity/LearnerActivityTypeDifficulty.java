package com.portugease.activity;

import com.portugease.common.enums.ActivityType;
import com.portugease.common.enums.DifficultyLevel;
import com.portugease.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "learner_activity_type_difficulty",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_learner_activity_type_difficulty_user_type",
                        columnNames = {"user_id", "activity_type"}
                )
        }
)
public class LearnerActivityTypeDifficulty {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 80)
    private ActivityType activityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_difficulty", nullable = false, length = 20)
    private DifficultyLevel currentDifficulty = DifficultyLevel.NORMAL;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public DifficultyLevel getCurrentDifficulty() {
        return currentDifficulty;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public void setCurrentDifficulty(DifficultyLevel currentDifficulty) {
        this.currentDifficulty = currentDifficulty;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}