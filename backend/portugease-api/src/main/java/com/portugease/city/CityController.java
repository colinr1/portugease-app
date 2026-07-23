package com.portugease.city;

import com.portugease.city.dto.CityDetailResponse;
import com.portugease.city.dto.CityListItemResponse;
import com.portugease.location.dto.LocationMenuItemResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final CityContentService cityContentService;

    public CityController(CityContentService cityContentService) {
        this.cityContentService = cityContentService;
    }

    @GetMapping
    public List<CityListItemResponse> getCities(
            @RequestParam(required = false) UUID userId
    ) {
        return cityContentService.getCities(userId);
    }

    @GetMapping("/{cityId}")
    public CityDetailResponse getCity(
            @PathVariable UUID cityId,
            @RequestParam(required = false) UUID userId
    ) {
        return cityContentService.getCity(cityId, userId);
    }

    @GetMapping("/by-slug/{citySlug}")
    public CityDetailResponse getCityBySlug(
            @PathVariable String citySlug,
            @RequestParam(required = false) UUID userId
    ) {
        return cityContentService.getCityBySlug(citySlug, userId);
    }

    @GetMapping("/{cityId}/locations")
    public List<LocationMenuItemResponse> getCityLocations(
            @PathVariable UUID cityId,
            @RequestParam(required = false) UUID userId
    ) {
        return cityContentService.getCityLocations(cityId, userId);
    }
}
