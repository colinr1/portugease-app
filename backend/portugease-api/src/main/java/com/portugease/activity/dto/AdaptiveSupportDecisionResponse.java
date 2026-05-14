package com.portugease.activity.dto;

import java.util.List;

public record AdaptiveSupportDecisionResponse(
        String scaffoldingLevel,
        boolean addToReview,
        boolean offerTranscript,
        boolean offerSlowerAudio,
        boolean reduceScaffolding,
        List<String> messages
) {
}