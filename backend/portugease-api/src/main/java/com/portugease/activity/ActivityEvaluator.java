package com.portugease.activity;

import com.portugease.activity.dto.ActivityEvaluationResult;
import com.portugease.common.enums.DifficultyLevel;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.*;

@Component
public class ActivityEvaluator {

    public ActivityEvaluationResult evaluate(
            Activity activity,
            Map<String, Object> submittedAnswer,
            DifficultyLevel selectedDifficulty
    ) {
        Map<String, Object> safeAnswer = submittedAnswer == null
                ? Map.of()
                : submittedAnswer;

        DifficultyLevel difficulty = selectedDifficulty == null
                ? DifficultyLevel.NORMAL
                : selectedDifficulty;

        Map<String, Object> definition = adaptiveDifficultyService.selectDefinition(
                activity,
                difficulty
        );

        return switch (activity.getActivityType()) {
            case MULTIPLE_CHOICE -> evaluateMultipleChoice(activity, definition, safeAnswer);
            case SENTENCE_BUILDING -> evaluateSentenceBuilding(activity, definition, safeAnswer);
            case WORD_MATCHING -> evaluateWordMatching(activity, definition, safeAnswer);
            case LISTENING -> evaluateListening(activity, definition, safeAnswer);
            case SENTENCE_TRANSFORMATION -> evaluateTransformation(activity, definition, safeAnswer);
            case SCENARIO_CHALLENGE -> evaluateScenarioChallenge(activity, definition, safeAnswer);
        };
    }

    private final AdaptiveDifficultyService adaptiveDifficultyService;

    public ActivityEvaluator(AdaptiveDifficultyService adaptiveDifficultyService) {
        this.adaptiveDifficultyService = adaptiveDifficultyService;
    }

    private ActivityEvaluationResult evaluateMultipleChoice(
            Activity activity,
            Map<String, Object> definition,
            Map<String, Object> submittedAnswer
    ) {

        String selectedOptionId = getString(submittedAnswer, "selectedOptionId");
        String selectedAnswer = getString(submittedAnswer, "selectedAnswer");

        List<Map<String, Object>> options = getListOfMaps(definition.get("options"));

        boolean correct = options.stream().anyMatch(option -> {
            boolean isCorrect = Boolean.TRUE.equals(option.get("isCorrect"));
            String optionId = getString(option, "id");
            String optionText = getString(option, "text");

            return isCorrect &&
                    (
                            Objects.equals(optionId, selectedOptionId)
                                    || normalise(optionText).equals(normalise(selectedAnswer))
                    );
        });

        return result(
                activity,
                correct,
                correct ? "Correct answer." : "That answer is not correct yet.",
                correct
                        ? getStringOrDefault(definition, "feedbackCorrect", "Correct.")
                        : getStringOrDefault(definition, "feedbackIncorrect", "Review the prompt and try again.")
        );
    }

    private ActivityEvaluationResult evaluateSentenceBuilding(
            Activity activity,
            Map<String, Object> definition,
            Map<String, Object> submittedAnswer
    ) {

        String correctSentence = getString(definition, "correctSentence");
        String submittedSentence = getString(submittedAnswer, "sentence");

        if (submittedSentence == null) {
            List<String> tokens = getListOfStrings(submittedAnswer.get("tokens"));
            submittedSentence = String.join(" ", tokens);
        }

        boolean correct = normaliseSentence(submittedSentence).equals(normaliseSentence(correctSentence));

        return result(
                activity,
                correct,
                correct ? "The sentence is correct." : "The sentence order is not correct yet.",
                correct
                        ? "You built the sentence correctly."
                        : "Check the word order and compare it with the target sentence pattern."
        );
    }

    private ActivityEvaluationResult evaluateWordMatching(
            Activity activity,
            Map<String, Object> definition,
            Map<String, Object> submittedAnswer
    ) {
        List<Map<String, Object>> correctPairs = getListOfMaps(definition.get("pairs"));
        List<Map<String, Object>> submittedPairs = getListOfMaps(submittedAnswer.get("matches"));

        Set<String> correctSet = new HashSet<>();
        for (Map<String, Object> pair : correctPairs) {
            correctSet.add(pairKey(getString(pair, "left"), getString(pair, "right")));
        }

        int correctCount = 0;
        for (Map<String, Object> pair : submittedPairs) {
            if (correctSet.contains(pairKey(getString(pair, "left"), getString(pair, "right")))) {
                correctCount++;
            }
        }

        int maxScore = Math.max(correctPairs.size(), 1);
        boolean correct = correctCount == maxScore;

        Map<String, Object> evaluationJson = new LinkedHashMap<>();
        evaluationJson.put("isCorrect", correct);
        evaluationJson.put("correctPairs", correctCount);
        evaluationJson.put("totalPairs", maxScore);

        return new ActivityEvaluationResult(
                correct,
                correctCount,
                maxScore,
                correct ? "All matches are correct." : "Some matches need review.",
                correct
                        ? "You matched all vocabulary items correctly."
                        : "Review the pairs and focus on the unmatched words.",
                evaluationJson
        );
    }

    private ActivityEvaluationResult evaluateListening(
            Activity activity,
            Map<String, Object> definition,
            Map<String, Object> submittedAnswer
    ) {
        String correctAnswer = getString(definition, "correctAnswer");
        String submittedText = getString(submittedAnswer, "text");

        boolean correct = normaliseSentence(submittedText).equals(normaliseSentence(correctAnswer));

        return result(
                activity,
                correct,
                correct ? "Correct listening answer." : "Listening answer not quite right.",
                correct
                        ? "You understood the audio correctly."
                        : "Try listening again. A transcript or slower audio may help."
        );
    }

    private ActivityEvaluationResult evaluateTransformation(
            Activity activity,
            Map<String, Object> definition,
            Map<String, Object> submittedAnswer
    ) {
        String submittedText = getString(submittedAnswer, "text");
        String correctAnswer = getString(definition, "correctAnswer");

        List<String> acceptedAnswers = getListOfStrings(definition.get("acceptedAnswers"));

        boolean correct = normaliseSentence(submittedText).equals(normaliseSentence(correctAnswer))
                || acceptedAnswers.stream()
                .anyMatch(answer -> normaliseSentence(answer).equals(normaliseSentence(submittedText)));

        return result(
                activity,
                correct,
                correct ? "Correct transformation." : "The transformation needs review.",
                correct
                        ? "You transformed the sentence correctly."
                        : "Check the grammar change requested by the prompt."
        );
    }

    private ActivityEvaluationResult evaluateScenarioChallenge(
            Activity activity,
            Map<String, Object> definition,
            Map<String, Object> submittedAnswer
    ) {
        String selectedOptionId = getString(submittedAnswer, "selectedOptionId");
        String correctOptionId = getString(definition, "correctOptionId");

        if (correctOptionId == null) {
            List<Map<String, Object>> options = getListOfMaps(definition.get("options"));
            correctOptionId = options.stream()
                    .filter(option -> Boolean.TRUE.equals(option.get("isCorrect")))
                    .map(option -> getString(option, "id"))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        boolean correct = Objects.equals(selectedOptionId, correctOptionId);

        return result(
                activity,
                correct,
                correct ? "Good choice for the situation." : "That is not the best response for this situation.",
                correct
                        ? "Your response fits the scenario."
                        : "Think about the social context and what the speaker is trying to achieve."
        );
    }

    private ActivityEvaluationResult result(
            Activity activity,
            boolean correct,
            String feedback,
            String explanation
    ) {
        int maxScore = activity.getMaxScore() == null ? 1 : activity.getMaxScore();
        int score = correct ? maxScore : 0;

        Map<String, Object> evaluationJson = new LinkedHashMap<>();
        evaluationJson.put("isCorrect", correct);
        evaluationJson.put("feedback", feedback);
        evaluationJson.put("explanation", explanation);

        return new ActivityEvaluationResult(
                correct,
                score,
                maxScore,
                feedback,
                explanation,
                evaluationJson
        );
    }

    private String pairKey(String left, String right) {
        return normalise(left) + "::" + normalise(right);
    }

    private String normaliseSentence(String value) {
        return normalise(value)
                .replaceAll("[?.!,;:]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalise(String value) {
        if (value == null) {
            return "";
        }

        String normalised = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return normalised.trim().toLowerCase(Locale.ROOT);
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }

    private String getStringOrDefault(Map<String, Object> map, String key, String fallback) {
        String value = getString(map, key);
        return value == null || value.isBlank() ? fallback : value;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getListOfMaps(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        return list.stream()
                .filter(Map.class::isInstance)
                .map(item -> (Map<String, Object>) item)
                .toList();
    }

    private List<String> getListOfStrings(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        return list.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .toList();
    }
}