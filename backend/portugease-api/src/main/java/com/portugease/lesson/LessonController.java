package com.portugease.lesson;

import com.portugease.lesson.dto.LessonDetailResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonContentService lessonContentService;

    public LessonController(LessonContentService lessonContentService) {
        this.lessonContentService = lessonContentService;
    }

    @GetMapping("/{lessonId}")
    public LessonDetailResponse getLesson(
            @PathVariable UUID lessonId,
            @RequestParam(required = false) UUID userId
    ) {
        return lessonContentService.getLesson(lessonId, userId);
    }
}
