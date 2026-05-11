package com.portugease.statistics;

import com.portugease.common.enums.LearningItemType;
import com.portugease.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "learner_item_statistics")
public class LearnerItemStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "item_key", nullable = false, length = 180)
    private String itemKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private LearningItemType itemType;

    @Column(name = "item_text", nullable = false, length = 250)
    private String itemText;

    @Column(name = "times_seen", nullable = false)
    private Integer timesSeen = 0;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount = 0;

    @Column(name = "incorrect_count", nullable = false)
    private Integer incorrectCount = 0;

    @Column(name = "difficulty_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal difficultyScore = BigDecimal.ZERO;

    @Column(name = "last_seen_at")
    private OffsetDateTime lastSeenAt;

    @Column(name = "last_correct_at")
    private OffsetDateTime lastCorrectAt;

    @Column(name = "last_incorrect_at")
    private OffsetDateTime lastIncorrectAt;

    @Column(name = "needs_review", nullable = false)
    private Boolean needsReview = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> metadataJson = new HashMap<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public LearnerItemStatistics() {
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getItemKey() {
        return itemKey;
    }

    public LearningItemType getItemType() {
        return itemType;
    }

    public String getItemText() {
        return itemText;
    }

    public Integer getTimesSeen() {
        return timesSeen;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public Integer getIncorrectCount() {
        return incorrectCount;
    }

    public BigDecimal getDifficultyScore() {
        return difficultyScore;
    }

    public OffsetDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public OffsetDateTime getLastCorrectAt() {
        return lastCorrectAt;
    }

    public OffsetDateTime getLastIncorrectAt() {
        return lastIncorrectAt;
    }

    public Boolean getNeedsReview() {
        return needsReview;
    }

    public Map<String, Object> getMetadataJson() {
        return metadataJson;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
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

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public void setItemType(LearningItemType itemType) {
        this.itemType = itemType;
    }

    public void setItemText(String itemText) {
        this.itemText = itemText;
    }

    public void setTimesSeen(Integer timesSeen) {
        this.timesSeen = timesSeen;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public void setIncorrectCount(Integer incorrectCount) {
        this.incorrectCount = incorrectCount;
    }

    public void setDifficultyScore(BigDecimal difficultyScore) {
        this.difficultyScore = difficultyScore;
    }

    public void setLastSeenAt(OffsetDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public void setLastCorrectAt(OffsetDateTime lastCorrectAt) {
        this.lastCorrectAt = lastCorrectAt;
    }

    public void setLastIncorrectAt(OffsetDateTime lastIncorrectAt) {
        this.lastIncorrectAt = lastIncorrectAt;
    }

    public void setNeedsReview(Boolean needsReview) {
        this.needsReview = needsReview;
    }

    public void setMetadataJson(Map<String, Object> metadataJson) {
        this.metadataJson = metadataJson;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}