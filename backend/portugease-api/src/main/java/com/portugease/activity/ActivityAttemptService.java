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
import com.portugease.common.enums.LearningItemType;
import com.portugease.common.exception.ResourceNotFoundException;
import com.portugease.statistics.LearnerItemStatistics;
import com.portugease.statistics.LearnerItemStatisticsRepository;
import com.portugease.user.DemoUserService;
import com.portugease.user.User;
import com.portugease.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class ActivityAttemptService {

    private final ActivityRepository activityRepository;
    private final ActivityAttemptRepository activityAttemptRepository;
    private final LearnerActivityProgressRepository learnerActivityProgressRepository;
    private final LearnerItemStatisticsRepository learnerItemStatisticsRepository;
    private final AdaptiveEventRepository adaptiveEventRepository;
    private final UserRepository userRepository;
    private final DemoUserService demoUserService;
    private final ActivityEvaluator activityEvaluator;
    private final ActivityAdaptiveSupportService adaptiveSupportService;

    public ActivityAttemptService(
            ActivityRepository activityRepository,
            ActivityAttemptRepository activityAttemptRepository,
            LearnerActivityProgressRepository learnerActivityProgressRepository,
            LearnerItemStatisticsRepository learnerItemStatisticsRepository,
            AdaptiveEventRepository adaptiveEventRepository,
            UserRepository userRepository,
            DemoUserService demoUserService,
            ActivityEvaluator activityEvaluator,
            ActivityAdaptiveSupportService adaptiveSupportService
    ) {
        this.activityRepository = activityRepository;
        this.activityAttemptRepository = activityAttemptRepository;
        this.learnerActivityProgressRepository = learnerActivityProgressRepository;
        this.learnerItemStatisticsRepository = learnerItemStatisticsRepository;
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

        List<LearnerItemStatistics> updatedItemStatistics = updateLearningItemStatistics(
                user,
                activity,
                evaluation.correct()
        );

        boolean anyItemNeedsReview = updatedItemStatistics.stream()
                .anyMatch(item -> item.getIncorrectCount() >= 3 || Boolean.TRUE.equals(item.getNeedsReview()));

        List<ActivityAttempt> recentSimilarAttempts =
                activityAttemptRepository.findTop10ByUserAndActivityTypeOrderByCreatedAtDesc(
                        user,
                        activity.getActivityType()
                );

        AdaptiveSupportDecisionResponse adaptiveDecision = adaptiveSupportService.decide(
                activity,
                evaluation.correct(),
                hintsUsed,
                anyItemNeedsReview,
                recentSimilarAttempts
        );

        boolean itemMarkedForReview = applyAdaptiveReviewDecision(
                updatedItemStatistics,
                adaptiveDecision.addToReview()
        );

        saveAdaptiveEvent(
                user,
                activity,
                attempt,
                adaptiveDecision,
                hintsUsed,
                updatedItemStatistics
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
                itemMarkedForReview
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
        attempt.setHotspotId(activity.getHotspotId());
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

    private List<LearnerItemStatistics> updateLearningItemStatistics(
            User user,
            Activity activity,
            boolean correct
    ) {
        List<LearningItemData> learningItems = extractLearningItems(activity);

        if (learningItems.isEmpty()) {
            return List.of();
        }

        List<LearnerItemStatistics> updatedStatistics = new ArrayList<>();

        for (LearningItemData item : learningItems) {
            LearnerItemStatistics statistics = learnerItemStatisticsRepository
                    .findByUserAndItemKey(user, item.itemKey())
                    .orElseGet(() -> {
                        LearnerItemStatistics created = new LearnerItemStatistics();
                        created.setUser(user);
                        created.setItemKey(item.itemKey());
                        created.setItemText(item.text());
                        created.setItemType(item.type());
                        return created;
                    });

            OffsetDateTime now = OffsetDateTime.now();

            statistics.setTimesSeen(statistics.getTimesSeen() + 1);
            statistics.setLastSeenAt(now);

            if (correct) {
                statistics.setCorrectCount(statistics.getCorrectCount() + 1);
                statistics.setLastCorrectAt(now);
            } else {
                statistics.setIncorrectCount(statistics.getIncorrectCount() + 1);
                statistics.setLastIncorrectAt(now);
            }

            statistics.setDifficultyScore(calculateDifficultyScore(statistics));
            statistics.setUpdatedAt(now);

            if (statistics.getIncorrectCount() >= 3) {
                statistics.setNeedsReview(true);
            }

            updatedStatistics.add(learnerItemStatisticsRepository.save(statistics));
        }

        return updatedStatistics;
    }

    private boolean applyAdaptiveReviewDecision(
            List<LearnerItemStatistics> statistics,
            boolean addToReview
    ) {
        if (!addToReview || statistics.isEmpty()) {
            return false;
        }

        OffsetDateTime now = OffsetDateTime.now();

        for (LearnerItemStatistics item : statistics) {
            item.setNeedsReview(true);
            item.setUpdatedAt(now);
        }

        learnerItemStatisticsRepository.saveAll(statistics);
        return true;
    }

    private void saveAdaptiveEvent(
            User user,
            Activity activity,
            ActivityAttempt attempt,
            AdaptiveSupportDecisionResponse decision,
            int hintsUsed,
            List<LearnerItemStatistics> itemStatistics
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
        context.put("reviewItemKeys", itemStatistics.stream()
                .filter(item -> Boolean.TRUE.equals(item.getNeedsReview()))
                .map(LearnerItemStatistics::getItemKey)
                .toList()
        );

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

    private List<LearningItemData> extractLearningItems(Activity activity) {
        Object itemsObject = activity.getLearningItemsJson().get("items");

        if (!(itemsObject instanceof List<?> items) || items.isEmpty()) {
            return List.of();
        }

        List<LearningItemData> result = new ArrayList<>();

        for (Object itemObject : items) {
            if (!(itemObject instanceof Map<?, ?> rawMap)) {
                continue;
            }

            String itemKey = valueAsString(rawMap.get("itemKey"));
            String typeValue = valueAsString(rawMap.get("type"));
            String text = valueAsString(rawMap.get("text"));

            if (itemKey == null || typeValue == null || text == null) {
                continue;
            }

            Optional<LearningItemType> itemType = parseLearningItemType(typeValue);

            itemType.ifPresent(type -> result.add(
                    new LearningItemData(itemKey, type, text)
            ));
        }

        return result;
    }

    private Optional<LearningItemType> parseLearningItemType(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String normalised = value.trim().toUpperCase(Locale.ROOT);

        try {
            return Optional.of(LearningItemType.valueOf(normalised));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private BigDecimal calculateDifficultyScore(LearnerItemStatistics statistics) {
        int total = statistics.getCorrectCount() + statistics.getIncorrectCount();

        if (total == 0) {
            return BigDecimal.ZERO;
        }

        double incorrectRatio = (double) statistics.getIncorrectCount() / total;
        double score = incorrectRatio * 100.0;

        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    private String valueAsString(Object value) {
        return value == null ? null : value.toString();
    }

    private record LearningItemData(
            String itemKey,
            LearningItemType type,
            String text
    ) {
    }
}