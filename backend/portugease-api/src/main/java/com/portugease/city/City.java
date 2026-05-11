package com.portugease.city;

import com.portugease.asset.StaticAsset;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "cities")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "display_order", nullable = false, unique = true)
    private Integer displayOrder;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "marker_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> markerJson = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_image_asset_id")
    private StaticAsset cityImageAsset;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "unlock_rule_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> unlockRuleJson = new HashMap<>();

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public City() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Map<String, Object> getMarkerJson() {
        return markerJson;
    }

    public StaticAsset getCityImageAsset() {
        return cityImageAsset;
    }

    public Map<String, Object> getUnlockRuleJson() {
        return unlockRuleJson;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setMarkerJson(Map<String, Object> markerJson) {
        this.markerJson = markerJson;
    }

    public void setCityImageAsset(StaticAsset cityImageAsset) {
        this.cityImageAsset = cityImageAsset;
    }

    public void setUnlockRuleJson(Map<String, Object> unlockRuleJson) {
        this.unlockRuleJson = unlockRuleJson;
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