package com.portugease.progress.dto;

import java.util.List;

public record ProgressResponse(
        List<CityProgressResponse> cities,
        List<LocationProgressResponse> locations
) {
}