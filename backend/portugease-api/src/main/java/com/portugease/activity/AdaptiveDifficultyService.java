package com.portugease.activity;

import com.portugease.common.enums.ActivityType;
import com.portugease.common.enums.AttemptResult;
import com.portugease.common.enums.DifficultyLevel;
import com.portugease.user.User;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdaptiveDifficultyService {

    private final LearnerActivityTypeDifficultyRepository difficultyRepository;
    private final ActivityAttemptRepository activityAttemptRepository;

    public AdaptiveDifficultyService(
            LearnerActivityTypeDifficultyRepository difficultyRepository,
            ActivityAttemptRepository activityAttemptRepository
    ) {
        this.difficultyRepository = difficultyRepository;
        this.activityAttemptRepository = activityAttemptRepository;
    }

    public DifficultyLevel getCurrentDifficulty(User user, ActivityType activityType) {
        return findOrCreateState(user, activityType).getCurrentDifficulty();
    }

    public Map<String, Object> selectDefinition(Activity activity, DifficultyLevel difficulty) {
        Map<String, Object> selected = switch (difficulty) {
            case EASY -> activity.getEasyDefinitionJson();
            case NORMAL -> activity.getNormalDefinitionJson();
            case HARD -> activity.getHardDefinitionJson();
        };

        if (selected != null && !selected.isEmpty()) {
            return selected;
        }

        Map<String, Object> normal = activity.getNormalDefinitionJson();
        if (normal != null && !normal.isEmpty()) {
            return normal;
        }

        return activity.getDefinitionJson();
    }

    public DifficultyUpdateResult updateDifficultyAfterCompletedAttempt(
            User user,
            Activity activity,
            ActivityAttempt completedAttempt
    ) {
        if (completedAttempt.getResult() != AttemptResult.CORRECT) {
            return DifficultyUpdateResult.unchanged(
                    getCurrentDifficulty(user, activity.getActivityType())
            );
        }

        LearnerActivityTypeDifficulty state = findOrCreateState(user, activity.getActivityType());
        DifficultyLevel currentDifficulty = state.getCurrentDifficulty();

        List<ActivityAttempt> recentCompletions =
                activityAttemptRepository.findTop5ByUserAndActivityTypeAndResultAndSelectedDifficultyAndCreatedAtAfterOrderByCreatedAtDesc(
                        user,
                        activity.getActivityType(),
                        AttemptResult.CORRECT,
                        currentDifficulty,
                        state.getUpdatedAt()
                );

        if (recentCompletions.size() < 5) {
            return DifficultyUpdateResult.unchanged(currentDifficulty);
        }

        long perfectCount = recentCompletions.stream()
                .filter(attempt -> Boolean.TRUE.equals(attempt.getCompletedPerfectly()))
                .count();

        long mistakeCount = recentCompletions.size() - perfectCount;

        DifficultyLevel before = currentDifficulty;
        DifficultyLevel after = before;

        if (perfectCount == 5) {
            after = promote(before);
        } else if (mistakeCount >= 3) {
            after = demote(before);
        }

        if (after == before) {
            return DifficultyUpdateResult.unchanged(before);
        }

        state.setCurrentDifficulty(after);
        state.setUpdatedAt(OffsetDateTime.now());
        difficultyRepository.save(state);

        String message = after.ordinal() > before.ordinal()
                ? "Great progress. Future " + activity.getActivityType().name().replace('_', ' ').toLowerCase() + " activities will be harder."
                : "Future " + activity.getActivityType().name().replace('_', ' ').toLowerCase() + " activities will be a little easier.";

        return new DifficultyUpdateResult(before, after, true, message);
    }

    private LearnerActivityTypeDifficulty findOrCreateState(User user, ActivityType activityType) {
        return difficultyRepository.findByUserAndActivityType(user, activityType)
                .orElseGet(() -> {
                    LearnerActivityTypeDifficulty created = new LearnerActivityTypeDifficulty();
                    created.setUser(user);
                    created.setActivityType(activityType);
                    created.setCurrentDifficulty(DifficultyLevel.NORMAL);
                    return difficultyRepository.save(created);
                });
    }

    private DifficultyLevel promote(DifficultyLevel current) {
        return switch (current) {
            case EASY -> DifficultyLevel.NORMAL;
            case NORMAL -> DifficultyLevel.HARD;
            case HARD -> DifficultyLevel.HARD;
        };
    }

    private DifficultyLevel demote(DifficultyLevel current) {
        return switch (current) {
            case HARD -> DifficultyLevel.NORMAL;
            case NORMAL -> DifficultyLevel.EASY;
            case EASY -> DifficultyLevel.EASY;
        };
    }

    public record DifficultyUpdateResult(
            DifficultyLevel previousDifficulty,
            DifficultyLevel newDifficulty,
            boolean changed,
            String message
    ) {
        static DifficultyUpdateResult unchanged(DifficultyLevel difficulty) {
            return new DifficultyUpdateResult(difficulty, difficulty, false, null);
        }
    }
}
