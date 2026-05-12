package com.portugease.lesson;

import com.portugease.lesson.dto.LessonDetailResponse;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonContentService lessonContentService;

    public LessonController(LessonContentService lessonContentService) {
        this.lessonContentService = lessonContentService;
    }

    @GetMapping("/{lessonId}")
    public LessonDetailResponse getLesson(@PathVariable UUID lessonId) {
        return lessonContentService.getLesson(lessonId);
    }
}