package com.portugease.city;

import com.portugease.city.dto.CityDetailResponse;
import com.portugease.city.dto.CityListItemResponse;
import com.portugease.location.dto.LocationMenuItemResponse;
import org.springframework.web.bind.annotation.*;

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
    public List<CityListItemResponse> getCities() {
        return cityContentService.getCities();
    }

    @GetMapping("/{cityId}")
    public CityDetailResponse getCity(@PathVariable UUID cityId) {
        return cityContentService.getCity(cityId);
    }

    @GetMapping("/{cityId}/locations")
    public List<LocationMenuItemResponse> getCityLocations(@PathVariable UUID cityId) {
        return cityContentService.getCityLocations(cityId);
    }
}