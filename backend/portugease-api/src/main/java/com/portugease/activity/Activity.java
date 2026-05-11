package com.portugease.activity;

import com.portugease.common.enums.ActivityType;
import com.portugease.location.Location;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "hotspot_id", nullable = false, length = 150)
    private String hotspotId;

    @Column(name = "activity_key", nullable = false, unique = true, length = 180)
    private String activityKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(columnDefinition = "text")
    private String instructions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "definition_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> definitionJson = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "learning_items_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> learningItemsJson = new HashMap<>();

    @Column(name = "max_score", nullable = false)
    private Integer maxScore = 1;

    @Column(name = "required_for_completion", nullable = false)
    private Boolean requiredForCompletion = true;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 1;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public Activity() {
    }

    public UUID getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public String getHotspotId() {
        return hotspotId;
    }

    public String getActivityKey() {
        return activityKey;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public String getTitle() {
        return title;
    }

    public String getInstructions() {
        return instructions;
    }

    public Map<String, Object> getDefinitionJson() {
        return definitionJson;
    }

    public Map<String, Object> getLearningItemsJson() {
        return learningItemsJson;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public Boolean getRequiredForCompletion() {
        return requiredForCompletion;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Boolean getActive() {
        return active;
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

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setHotspotId(String hotspotId) {
        this.hotspotId = hotspotId;
    }

    public void setActivityKey(String activityKey) {
        this.activityKey = activityKey;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public void setDefinitionJson(Map<String, Object> definitionJson) {
        this.definitionJson = definitionJson;
    }

    public void setLearningItemsJson(Map<String, Object> learningItemsJson) {
        this.learningItemsJson = learningItemsJson;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public void setRequiredForCompletion(Boolean requiredForCompletion) {
        this.requiredForCompletion = requiredForCompletion;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}