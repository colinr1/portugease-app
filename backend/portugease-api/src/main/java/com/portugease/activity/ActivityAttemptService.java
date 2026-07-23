package com.portugease.activity;

import com.portugease.activity.dto.ActivityAttemptSubmissionRequest;
import com.portugease.activity.dto.ActivityAttemptSubmissionResponse;
import com.portugease.activity.dto.ActivityEvaluationResult;
import com.portugease.activity.dto.AdaptiveDifficultyResponse;
import com.portugease.activity.dto.ProgressUpdateSummaryResponse;
import com.portugease.common.enums.AttemptResult;
import com.portugease.common.enums.DifficultyLevel;
import com.portugease.common.exception.ResourceNotFoundException;
import com.portugease.progress.ProgressionService;
import com.portugease.progress.dto.ProgressionUpdateResponse;
import com.portugease.user.DemoUserService;
import com.portugease.user.User;
import com.portugease.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ActivityAttemptService {

    private final ActivityRepository activityRepository;
    private final ActivityAttemptRepository activityAttemptRepository;
    private final LearnerActivityProgressRepository learnerActivityProgressRepository;
    private final UserRepository userRepository;
    private final DemoUserService demoUserService;
    private final ActivityEvaluator activityEvaluator;
    private final AdaptiveDifficultyService adaptiveDifficultyService;
    private final ProgressionService progressionService;

    public ActivityAttemptService(
            ActivityRepository activityRepository,
            ActivityAttemptRepository activityAttemptRepository,
            LearnerActivityProgressRepository learnerActivityProgressRepository,
            UserRepository userRepository,
            DemoUserService demoUserService,
            ActivityEvaluator activityEvaluator,
            AdaptiveDifficultyService adaptiveDifficultyService,
            ProgressionService progressionService
    ) {
        this.activityRepository = activityRepository;
        this.activityAttemptRepository = activityAttemptRepository;
        this.learnerActivityProgressRepository = learnerActivityProgressRepository;
        this.userRepository = userRepository;
        this.demoUserService = demoUserService;
        this.activityEvaluator = activityEvaluator;
        this.adaptiveDifficultyService = adaptiveDifficultyService;
        this.progressionService = progressionService;
    }

    @Transactional
    public ActivityAttemptSubmissionResponse submitAttempt(
            UUID activityId,
            ActivityAttemptSubmissionRequest request
    ) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + activityId));

        User user = resolveUser(request.userId());
        progressionService.assertLocationUnlocked(user, activity.getLocation());

        DifficultyLevel selectedDifficulty = request.selectedDifficulty() == null
                ? adaptiveDifficultyService.getCurrentDifficulty(user, activity.getActivityType())
                : request.selectedDifficulty();

        ActivityEvaluationResult evaluation = activityEvaluator.evaluate(
                activity,
                request.submittedAnswer(),
                selectedDifficulty
        );

        ActivityAttempt attempt = saveAttempt(
                user,
                activity,
                request,
                evaluation,
                selectedDifficulty
        );

        int incorrectBeforeSuccess = request.incorrectSubmissionCount() == null
                ? 0
                : Math.max(request.incorrectSubmissionCount(), 0);

        ProgressUpdateSummaryResponse progressUpdate = updateActivityProgress(
                user,
                activity,
                evaluation,
                incorrectBeforeSuccess
        );

        ProgressionUpdateResponse progressionUpdate =
                progressionService.applyProgressionAfterActivityAttempt(user, activity);

        AdaptiveDifficultyService.DifficultyUpdateResult difficultyUpdate =
                evaluation.correct()
                        ? adaptiveDifficultyService.updateDifficultyAfterCompletedAttempt(user, activity, attempt)
                        : AdaptiveDifficultyService.DifficultyUpdateResult.unchanged(selectedDifficulty);

        return new ActivityAttemptSubmissionResponse(
                attempt.getId(),
                activity.getId(),
                evaluation.correct(),
                evaluation.score(),
                evaluation.maxScore(),
                evaluation.feedbackMessage(),
                evaluation.explanation(),
                new AdaptiveDifficultyResponse(
                        selectedDifficulty.name(),
                        difficultyUpdate.changed(),
                        difficultyUpdate.newDifficulty().name(),
                        difficultyUpdate.message()
                ),
                progressUpdate,
                progressionUpdate
        );
    }

    private User resolveUser(UUID userId) {
        if (userId == null) {
            return demoUserService.getDemoUser();
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private ActivityAttempt saveAttempt(
            User user,
            Activity activity,
            ActivityAttemptSubmissionRequest request,
            ActivityEvaluationResult evaluation,
            DifficultyLevel selectedDifficulty
    ) {
        long previousAttempts = activityAttemptRepository.countByUserAndActivity(user, activity);

        int incorrectBeforeSuccess = request.incorrectSubmissionCount() == null
                ? 0
                : Math.max(request.incorrectSubmissionCount(), 0);

        boolean completedPerfectly = evaluation.correct() && incorrectBeforeSuccess == 0;

        Map<String, Object> evaluationJson = new LinkedHashMap<>(evaluation.evaluationJson());
        evaluationJson.put("selectedDifficulty", selectedDifficulty.name());
        evaluationJson.put("incorrectBeforeSuccess", incorrectBeforeSuccess);
        evaluationJson.put("completedPerfectly", completedPerfectly);

        if (request.learnerSessionId() != null) {
            evaluationJson.put("learnerSessionId", request.learnerSessionId().toString());
        }

        ActivityAttempt attempt = new ActivityAttempt();
        attempt.setUser(user);
        attempt.setActivity(activity);
        attempt.setLocation(activity.getLocation());
        attempt.setActivityType(activity.getActivityType());
        attempt.setAttemptNumber((int) previousAttempts + 1);
        attempt.setAnswerJson(request.submittedAnswer());
        attempt.setResult(evaluation.correct() ? AttemptResult.CORRECT : AttemptResult.INCORRECT);
        attempt.setScore(evaluation.score());
        attempt.setMaxScore(evaluation.maxScore());
        attempt.setEvaluationJson(evaluationJson);
        attempt.setSelectedDifficulty(selectedDifficulty);
        attempt.setIncorrectBeforeSuccess(evaluation.correct() ? incorrectBeforeSuccess : 0);
        attempt.setCompletedPerfectly(completedPerfectly);

        return activityAttemptRepository.save(attempt);
    }

    private ProgressUpdateSummaryResponse updateActivityProgress(
            User user,
            Activity activity,
            ActivityEvaluationResult evaluation,
            int incorrectBeforeSuccess
    ) {
        LearnerActivityProgress progress = learnerActivityProgressRepository
                .findByUserAndActivity(user, activity)
                .orElseGet(() -> {
                    LearnerActivityProgress created = new LearnerActivityProgress();
                    created.setUser(user);
                    created.setActivity(activity);
                    created.setLocation(activity.getLocation());
                    created.setMaxScore(activity.getMaxScore());
                    return created;
                });

        OffsetDateTime now = OffsetDateTime.now();

        if (progress.getFirstAttemptAt() == null) {
            progress.setFirstAttemptAt(now);
        }

        progress.setLastAttemptAt(now);
        progress.setAttemptsCount(progress.getAttemptsCount() + 1);

        if (!evaluation.correct()) {
            progress.setIncorrectAttemptsCount(progress.getIncorrectAttemptsCount() + 1);
        }

        if (evaluation.score() > progress.getBestScore()) {
            progress.setBestScore(evaluation.score());
        }

        boolean completed = evaluation.correct();
        boolean mastered = evaluation.correct() && evaluation.score() >= evaluation.maxScore();

        if (completed && !Boolean.TRUE.equals(progress.getCompleted())) {
            progress.setCompleted(true);
            progress.setCompletedAt(now);
        }

        if (mastered) {
            progress.setMastered(true);
        }

        progress.setUpdatedAt(now);

        LearnerActivityProgress saved = learnerActivityProgressRepository.save(progress);

        return new ProgressUpdateSummaryResponse(
                Boolean.TRUE.equals(saved.getCompleted()),
                Boolean.TRUE.equals(saved.getMastered()),
                saved.getAttemptsCount(),
                saved.getIncorrectAttemptsCount(),
                saved.getBestScore(),
                saved.getMaxScore(),
                evaluation.correct() && incorrectBeforeSuccess == 0,
                incorrectBeforeSuccess
        );
    }
}
