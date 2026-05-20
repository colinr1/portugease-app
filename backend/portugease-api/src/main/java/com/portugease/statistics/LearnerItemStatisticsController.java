package com.portugease.statistics;

import com.portugease.statistics.dto.LearnerItemStatisticsResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class LearnerItemStatisticsController {

    private final LearnerItemStatisticsService service;

    public LearnerItemStatisticsController(LearnerItemStatisticsService service) {
        this.service = service;
    }

    @GetMapping("/review-items")
    public List<LearnerItemStatisticsResponse> getReviewItems() {
        return service.getItemsNeedingReview();
    }
}