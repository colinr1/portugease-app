package com.portugease.activity;

import com.portugease.activity.dto.AdaptiveSupportDecisionResponse;
import com.portugease.common.enums.ActivityType;
import com.portugease.common.enums.AttemptResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ActivityAdaptiveSupportService {

    public AdaptiveSupportDecisionResponse decide(
            Activity activity,
            boolean isCorrect,
            int hintsUsed,
            boolean anyItemNeedsReview,
            List<ActivityAttempt> recentSimilarAttempts
    ) {
        List<String> messages = new ArrayList<>();

        boolean addToReview = false;
        boolean offerTranscript = false;
        boolean offerSlowerAudio = false;
        boolean reduceScaffolding = false;

        String scaffoldingLevel = "NORMAL";

        if (!isCorrect && anyItemNeedsReview) {
            addToReview = true;
            scaffoldingLevel = "HIGH";
            messages.add("This item has been missed several times, so it has been added to review.");
        }

        if (hintsUsed >= 2) {
            scaffoldingLevel = "HIGH";
            messages.add("Multiple hints were used, so extra scaffolding will remain available.");
        }

        if (isCorrect && hintsUsed == 0 && hasThreeRecentCorrectWithoutHints(recentSimilarAttempts)) {
            reduceScaffolding = true;
            scaffoldingLevel = "LOW";
            messages.add("You answered several similar activities correctly without hints, so scaffolding can be reduced.");
        }

        if (!isCorrect && activity.getActivityType() == ActivityType.LISTENING) {
            offerTranscript = true;
            offerSlowerAudio = true;
            scaffoldingLevel = "HIGH";
            messages.add("Listening seems difficult here. A transcript or slower audio should be offered.");
        }

        if (messages.isEmpty()) {
            messages.add(isCorrect
                    ? "Good work. Continue to the next activity."
                    : "Review the feedback and try again.");
        }

        return new AdaptiveSupportDecisionResponse(
                scaffoldingLevel,
                addToReview,
                offerTranscript,
                offerSlowerAudio,
                reduceScaffolding,
                messages
        );
    }

    private boolean hasThreeRecentCorrectWithoutHints(List<ActivityAttempt> attempts) {
        if (attempts == null || attempts.size() < 3) {
            return false;
        }

        int correctWithoutHints = 0;

        for (ActivityAttempt attempt : attempts) {
            boolean correct = attempt.getResult() == AttemptResult.CORRECT;
            boolean noHints = getHintsUsed(attempt) == 0;

            if (correct && noHints) {
                correctWithoutHints++;
            }

            if (correctWithoutHints >= 3) {
                return true;
            }
        }

        return false;
    }

    private int getHintsUsed(ActivityAttempt attempt) {
        if (attempt.getEvaluationJson() == null) {
            return 0;
        }

        Object value = attempt.getEvaluationJson().get("hintsUsed");

        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value == null) {
            return 0;
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}