package com.portugease.progress;

import com.portugease.progress.dto.ProgressResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping
    public ProgressResponse getProgress() {
        return progressService.getDemoProgress();
    }
}