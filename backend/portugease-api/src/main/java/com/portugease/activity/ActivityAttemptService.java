package com.portugease.activity;

import com.portugease.activity.dto.ActivityAttemptSubmissionRequest;
import com.portugease.activity.dto.ActivityAttemptSubmissionResponse;
import com.portugease.activity.dto.ActivityEvaluationResult;
import com.portugease.activity.dto.AdaptiveSupportDecisionResponse;
import com.portugease.activity.dto.ProgressUpdateSummaryResponse;
import com.portugease.adaptive.AdaptiveEvent;
import com.portugease.adaptive.AdaptiveEventRepository;
import com.portugease.common.enums.AdaptiveEventType;
import com.portugease.common.enums.AttemptResult;
import com.portugease.common.exception.ResourceNotFoundException;
import com.portugease.user.DemoUserService;
import com.portugease.user.User;
import com.portugease.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class ActivityAttemptService {

    private final ActivityRepository activityRepository;
    private final ActivityAttemptRepository activityAttemptRepository;
    private final LearnerActivityProgressRepository learnerActivityProgressRepository;
    private final AdaptiveEventRepository adaptiveEventRepository;
    private final UserRepository userRepository;
    private final DemoUserService demoUserService;
    private final ActivityEvaluator activityEvaluator;
    private final ActivityAdaptiveSupportService adaptiveSupportService;

    public ActivityAttemptService(
            ActivityRepository activityRepository,
            ActivityAttemptRepository activityAttemptRepository,
            LearnerActivityProgressRepository learnerActivityProgressRepository,
            AdaptiveEventRepository adaptiveEventRepository,
            UserRepository userRepository,
            DemoUserService demoUserService,
            ActivityEvaluator activityEvaluator,
            ActivityAdaptiveSupportService adaptiveSupportService
    ) {
        this.activityRepository = activityRepository;
        this.activityAttemptRepository = activityAttemptRepository;
        this.learnerActivityProgressRepository = learnerActivityProgressRepository;
        this.adaptiveEventRepository = adaptiveEventRepository;
        this.userRepository = userRepository;
        this.demoUserService = demoUserService;
        this.activityEvaluator = activityEvaluator;
        this.adaptiveSupportService = adaptiveSupportService;
    }

    @Transactional
    public ActivityAttemptSubmissionResponse submitAttempt(
            UUID activityId,
            ActivityAttemptSubmissionRequest request
    ) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + activityId));

        User user = resolveUser(request.userId());

        int hintsUsed = request.hintsUsed() == null ? 0 : Math.max(request.hintsUsed(), 0);

        ActivityEvaluationResult evaluation = activityEvaluator.evaluate(
                activity,
                request.submittedAnswer(),
                hintsUsed
        );

        ActivityAttempt attempt = saveAttempt(user, activity, request, evaluation, hintsUsed);

        List<ActivityAttempt> recentSimilarAttempts =
                activityAttemptRepository.findTop10ByUserAndActivityTypeOrderByCreatedAtDesc(
                        user,
                        activity.getActivityType()
                );

        AdaptiveSupportDecisionResponse adaptiveDecision = adaptiveSupportService.decide(
                activity,
                evaluation.correct(),
                hintsUsed,
                false,
                recentSimilarAttempts
        );

        saveAdaptiveEvent(
                user,
                activity,
                attempt,
                adaptiveDecision,
                hintsUsed
        );

        ProgressUpdateSummaryResponse progressSummary = updateActivityProgress(
                user,
                activity,
                evaluation
        );

        ProgressUpdateSummaryResponse finalProgressSummary = new ProgressUpdateSummaryResponse(
                progressSummary.activityCompleted(),
                progressSummary.activityMastered(),
                progressSummary.attemptsCount(),
                progressSummary.incorrectAttemptsCount(),
                progressSummary.bestScore(),
                progressSummary.maxScore(),
                false
        );

        return new ActivityAttemptSubmissionResponse(
                attempt.getId(),
                activity.getId(),
                evaluation.correct(),
                evaluation.score(),
                evaluation.maxScore(),
                evaluation.feedbackMessage(),
                evaluation.explanation(),
                adaptiveDecision,
                finalProgressSummary
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
            int hintsUsed
    ) {
        long previousAttempts = activityAttemptRepository.countByUserAndActivity(user, activity);

        Map<String, Object> evaluationJson = new LinkedHashMap<>(evaluation.evaluationJson());
        evaluationJson.put("hintsUsed", hintsUsed);

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

        return activityAttemptRepository.save(attempt);
    }

    private ProgressUpdateSummaryResponse updateActivityProgress(
            User user,
            Activity activity,
            ActivityEvaluationResult evaluation
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
                false
        );
    }

    private void saveAdaptiveEvent(
            User user,
            Activity activity,
            ActivityAttempt attempt,
            AdaptiveSupportDecisionResponse decision,
            int hintsUsed
    ) {
        AdaptiveEvent event = new AdaptiveEvent();
        event.setUser(user);
        event.setCity(activity.getLocation().getCity());
        event.setLocation(activity.getLocation());
        event.setActivity(activity);
        event.setAttempt(attempt);
        event.setEventType(resolveAdaptiveEventType(decision));
        event.setMessage(String.join(" ", decision.messages()));

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("scaffoldingLevel", decision.scaffoldingLevel());
        context.put("addToReview", decision.addToReview());
        context.put("offerTranscript", decision.offerTranscript());
        context.put("offerSlowerAudio", decision.offerSlowerAudio());
        context.put("reduceScaffolding", decision.reduceScaffolding());
        context.put("hintsUsed", hintsUsed);
        context.put("reviewItemKeys", List.of());

        event.setContextJson(context);

        adaptiveEventRepository.save(event);
    }

    private AdaptiveEventType resolveAdaptiveEventType(AdaptiveSupportDecisionResponse decision) {
        if (decision.addToReview()) {
            return AdaptiveEventType.REVIEW_RECOMMENDED;
        }

        if (decision.offerTranscript() || decision.offerSlowerAudio()) {
            return AdaptiveEventType.EXAMPLE_SHOWN;
        }

        if (decision.reduceScaffolding()) {
            return AdaptiveEventType.ACTIVITY_DIFFICULTY_ADJUSTED;
        }

        if ("HIGH".equalsIgnoreCase(decision.scaffoldingLevel())) {
            return AdaptiveEventType.HINT_SHOWN;
        }

        return AdaptiveEventType.EXTRA_FEEDBACK_SHOWN;
    }
}