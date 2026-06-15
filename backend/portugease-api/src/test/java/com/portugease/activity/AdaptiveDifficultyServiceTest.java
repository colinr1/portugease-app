package com.portugease.activity;

import com.portugease.common.enums.ActivityType;
import com.portugease.common.enums.AttemptResult;
import com.portugease.common.enums.DifficultyLevel;
import com.portugease.user.User;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdaptiveDifficultyServiceTest {

    private final LearnerActivityTypeDifficultyRepository difficultyRepository =
            mock(LearnerActivityTypeDifficultyRepository.class);
    private final ActivityAttemptRepository activityAttemptRepository =
            mock(ActivityAttemptRepository.class);
    private final AdaptiveDifficultyService service =
            new AdaptiveDifficultyService(difficultyRepository, activityAttemptRepository);

    @Test
    void promotesOnlyAfterFivePerfectAttemptsAtCurrentDifficulty() {
        User user = new User();
        Activity activity = activity(ActivityType.MULTIPLE_CHOICE);
        LearnerActivityTypeDifficulty state = state(user, ActivityType.MULTIPLE_CHOICE, DifficultyLevel.EASY);

        when(difficultyRepository.findByUserAndActivityType(user, ActivityType.MULTIPLE_CHOICE))
                .thenReturn(Optional.of(state));
        when(activityAttemptRepository.findTop5ByUserAndActivityTypeAndResultAndSelectedDifficultyAndCreatedAtAfterOrderByCreatedAtDesc(
                user,
                ActivityType.MULTIPLE_CHOICE,
                AttemptResult.CORRECT,
                DifficultyLevel.EASY,
                state.getUpdatedAt()
        )).thenReturn(correctAttempts(DifficultyLevel.EASY, true, true, true, true, true));

        AdaptiveDifficultyService.DifficultyUpdateResult result =
                service.updateDifficultyAfterCompletedAttempt(user, activity, correctAttempt(DifficultyLevel.EASY, true));

        assertThat(result.changed()).isTrue();
        assertThat(result.previousDifficulty()).isEqualTo(DifficultyLevel.EASY);
        assertThat(result.newDifficulty()).isEqualTo(DifficultyLevel.NORMAL);
        assertThat(state.getCurrentDifficulty()).isEqualTo(DifficultyLevel.NORMAL);
    }

    @Test
    void previousDifficultyAttemptsDoNotImmediatelyPromoteAfterPromotion() {
        User user = new User();
        Activity activity = activity(ActivityType.MULTIPLE_CHOICE);
        LearnerActivityTypeDifficulty state = state(user, ActivityType.MULTIPLE_CHOICE, DifficultyLevel.NORMAL);

        when(difficultyRepository.findByUserAndActivityType(user, ActivityType.MULTIPLE_CHOICE))
                .thenReturn(Optional.of(state));
        when(activityAttemptRepository.findTop5ByUserAndActivityTypeAndResultAndSelectedDifficultyAndCreatedAtAfterOrderByCreatedAtDesc(
                user,
                ActivityType.MULTIPLE_CHOICE,
                AttemptResult.CORRECT,
                DifficultyLevel.NORMAL,
                state.getUpdatedAt()
        )).thenReturn(correctAttempts(DifficultyLevel.NORMAL, true));

        AdaptiveDifficultyService.DifficultyUpdateResult result =
                service.updateDifficultyAfterCompletedAttempt(user, activity, correctAttempt(DifficultyLevel.NORMAL, true));

        assertThat(result.changed()).isFalse();
        assertThat(result.newDifficulty()).isEqualTo(DifficultyLevel.NORMAL);
        assertThat(state.getCurrentDifficulty()).isEqualTo(DifficultyLevel.NORMAL);
        verify(difficultyRepository, never()).save(state);
    }

    @Test
    void rollingAttemptCountStartsAfterLatestDifficultyChange() {
        User user = new User();
        Activity activity = activity(ActivityType.MULTIPLE_CHOICE);
        LearnerActivityTypeDifficulty state = state(user, ActivityType.MULTIPLE_CHOICE, DifficultyLevel.NORMAL);
        OffsetDateTime latestDifficultyChange = OffsetDateTime.parse("2026-06-01T12:00:00Z");
        state.setUpdatedAt(latestDifficultyChange);

        when(difficultyRepository.findByUserAndActivityType(user, ActivityType.MULTIPLE_CHOICE))
                .thenReturn(Optional.of(state));
        when(activityAttemptRepository.findTop5ByUserAndActivityTypeAndResultAndSelectedDifficultyAndCreatedAtAfterOrderByCreatedAtDesc(
                user,
                ActivityType.MULTIPLE_CHOICE,
                AttemptResult.CORRECT,
                DifficultyLevel.NORMAL,
                latestDifficultyChange
        )).thenReturn(correctAttempts(DifficultyLevel.NORMAL, true, true, true, true));

        AdaptiveDifficultyService.DifficultyUpdateResult result =
                service.updateDifficultyAfterCompletedAttempt(user, activity, correctAttempt(DifficultyLevel.NORMAL, true));

        assertThat(result.changed()).isFalse();
        assertThat(result.newDifficulty()).isEqualTo(DifficultyLevel.NORMAL);
        verify(activityAttemptRepository)
                .findTop5ByUserAndActivityTypeAndResultAndSelectedDifficultyAndCreatedAtAfterOrderByCreatedAtDesc(
                        user,
                        ActivityType.MULTIPLE_CHOICE,
                        AttemptResult.CORRECT,
                        DifficultyLevel.NORMAL,
                        latestDifficultyChange
                );
        verify(difficultyRepository, never()).save(state);
    }

    @Test
    void promotesFromNormalToHardAfterFivePerfectNormalAttempts() {
        User user = new User();
        Activity activity = activity(ActivityType.WORD_MATCHING);
        LearnerActivityTypeDifficulty state = state(user, ActivityType.WORD_MATCHING, DifficultyLevel.NORMAL);

        when(difficultyRepository.findByUserAndActivityType(user, ActivityType.WORD_MATCHING))
                .thenReturn(Optional.of(state));
        when(activityAttemptRepository.findTop5ByUserAndActivityTypeAndResultAndSelectedDifficultyAndCreatedAtAfterOrderByCreatedAtDesc(
                user,
                ActivityType.WORD_MATCHING,
                AttemptResult.CORRECT,
                DifficultyLevel.NORMAL,
                state.getUpdatedAt()
        )).thenReturn(correctAttempts(DifficultyLevel.NORMAL, true, true, true, true, true));

        AdaptiveDifficultyService.DifficultyUpdateResult result =
                service.updateDifficultyAfterCompletedAttempt(user, activity, correctAttempt(DifficultyLevel.NORMAL, true));

        assertThat(result.changed()).isTrue();
        assertThat(result.newDifficulty()).isEqualTo(DifficultyLevel.HARD);
        assertThat(state.getCurrentDifficulty()).isEqualTo(DifficultyLevel.HARD);
    }

    @Test
    void demotesFromHardAfterThreeImperfectHardAttemptsWithinLatestFiveHardAttempts() {
        User user = new User();
        Activity activity = activity(ActivityType.SENTENCE_BUILDING);
        LearnerActivityTypeDifficulty state = state(user, ActivityType.SENTENCE_BUILDING, DifficultyLevel.HARD);

        when(difficultyRepository.findByUserAndActivityType(user, ActivityType.SENTENCE_BUILDING))
                .thenReturn(Optional.of(state));
        when(activityAttemptRepository.findTop5ByUserAndActivityTypeAndResultAndSelectedDifficultyAndCreatedAtAfterOrderByCreatedAtDesc(
                user,
                ActivityType.SENTENCE_BUILDING,
                AttemptResult.CORRECT,
                DifficultyLevel.HARD,
                state.getUpdatedAt()
        )).thenReturn(correctAttempts(DifficultyLevel.HARD, false, false, false, true, true));

        AdaptiveDifficultyService.DifficultyUpdateResult result =
                service.updateDifficultyAfterCompletedAttempt(user, activity, correctAttempt(DifficultyLevel.HARD, false));

        assertThat(result.changed()).isTrue();
        assertThat(result.previousDifficulty()).isEqualTo(DifficultyLevel.HARD);
        assertThat(result.newDifficulty()).isEqualTo(DifficultyLevel.NORMAL);
        assertThat(state.getCurrentDifficulty()).isEqualTo(DifficultyLevel.NORMAL);
    }

    @Test
    void ignoresIncorrectCompletedAttemptForDifficultyChanges() {
        User user = new User();
        Activity activity = activity(ActivityType.LISTENING);
        LearnerActivityTypeDifficulty state = state(user, ActivityType.LISTENING, DifficultyLevel.HARD);
        ActivityAttempt attempt = correctAttempt(DifficultyLevel.HARD, false);
        attempt.setResult(AttemptResult.INCORRECT);

        when(difficultyRepository.findByUserAndActivityType(user, ActivityType.LISTENING))
                .thenReturn(Optional.of(state));

        AdaptiveDifficultyService.DifficultyUpdateResult result =
                service.updateDifficultyAfterCompletedAttempt(user, activity, attempt);

        assertThat(result.changed()).isFalse();
        assertThat(result.newDifficulty()).isEqualTo(DifficultyLevel.HARD);
        verify(activityAttemptRepository, never())
                .findTop5ByUserAndActivityTypeAndResultAndSelectedDifficultyAndCreatedAtAfterOrderByCreatedAtDesc(
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    private static Activity activity(ActivityType activityType) {
        Activity activity = new Activity();
        activity.setActivityType(activityType);
        return activity;
    }

    private static LearnerActivityTypeDifficulty state(
            User user,
            ActivityType activityType,
            DifficultyLevel currentDifficulty
    ) {
        LearnerActivityTypeDifficulty state = new LearnerActivityTypeDifficulty();
        state.setUser(user);
        state.setActivityType(activityType);
        state.setCurrentDifficulty(currentDifficulty);
        return state;
    }

    private static List<ActivityAttempt> correctAttempts(
            DifficultyLevel selectedDifficulty,
            boolean... completedPerfectly
    ) {
        List<ActivityAttempt> attempts = new ArrayList<>();
        for (boolean perfect : completedPerfectly) {
            attempts.add(correctAttempt(selectedDifficulty, perfect));
        }
        return attempts;
    }

    private static ActivityAttempt correctAttempt(
            DifficultyLevel selectedDifficulty,
            boolean completedPerfectly
    ) {
        ActivityAttempt attempt = new ActivityAttempt();
        attempt.setResult(AttemptResult.CORRECT);
        attempt.setSelectedDifficulty(selectedDifficulty);
        attempt.setCompletedPerfectly(completedPerfectly);
        return attempt;
    }
}
