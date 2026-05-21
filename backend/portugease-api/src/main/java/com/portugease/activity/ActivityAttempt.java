package com.portugease.activity;

import com.portugease.common.enums.ActivityType;
import com.portugease.common.enums.AttemptResult;
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
@Table(name = "activity_attempts")
public class ActivityAttempt {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answer_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> answerJson = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AttemptResult result;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(name = "max_score", nullable = false)
    private Integer maxScore = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evaluation_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> evaluationJson = new HashMap<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public ActivityAttempt() {
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

    public ActivityType getActivityType() {
        return activityType;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public Map<String, Object> getAnswerJson() {
        return answerJson;
    }

    public AttemptResult getResult() {
        return result;
    }

    public Integer getScore() {
        return score;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public Map<String, Object> getEvaluationJson() {
        return evaluationJson;
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

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public void setAnswerJson(Map<String, Object> answerJson) {
        this.answerJson = answerJson;
    }

    public void setResult(AttemptResult result) {
        this.result = result;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public void setEvaluationJson(Map<String, Object> evaluationJson) {
        this.evaluationJson = evaluationJson;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}