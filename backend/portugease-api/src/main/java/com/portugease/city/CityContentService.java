package com.portugease.city;

import com.portugease.asset.AssetResponseMapper;
import com.portugease.asset.StaticAsset;
import com.portugease.asset.dto.AssetMetadataResponse;
import com.portugease.city.dto.CityDetailResponse;
import com.portugease.city.dto.CityListItemResponse;
import com.portugease.city.dto.CityMarkerResponse;
import com.portugease.common.enums.CityStatus;
import com.portugease.common.exception.ResourceNotFoundException;
import com.portugease.common.json.JsonValueReader;
import com.portugease.location.LocationContentService;
import com.portugease.location.dto.LocationMenuItemResponse;
import com.portugease.progress.LearnerCityProgressRepository;
import com.portugease.user.DemoUserService;
import com.portugease.user.User;
import com.portugease.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CityContentService {

    private final CityRepository cityRepository;
    private final LearnerCityProgressRepository learnerCityProgressRepository;
    private final DemoUserService demoUserService;
    private final UserRepository userRepository;
    private final LocationContentService locationContentService;

    public CityContentService(
            CityRepository cityRepository,
            LearnerCityProgressRepository learnerCityProgressRepository,
            DemoUserService demoUserService,
            UserRepository userRepository,
            LocationContentService locationContentService
    ) {
        this.cityRepository = cityRepository;
        this.learnerCityProgressRepository = learnerCityProgressRepository;
        this.demoUserService = demoUserService;
        this.userRepository = userRepository;
        this.locationContentService = locationContentService;
    }

    @Transactional(readOnly = true)
    public List<CityListItemResponse> getCities(UUID userId) {
        User user = resolveUser(userId);

        return cityRepository.findAllByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(city -> toListItem(city, getCityStatus(user, city)))
                .toList();
    }

    @Transactional(readOnly = true)
    public CityDetailResponse getCity(UUID cityId, UUID userId) {
        User user = resolveUser(userId);

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found: " + cityId));

        List<LocationMenuItemResponse> locations =
                locationContentService.getLocationMenuForCity(city.getId(), user.getId());

        return new CityDetailResponse(
                city.getId(),
                city.getName(),
                city.getSlug(),
                city.getDescription(),
                city.getDisplayOrder(),
                toMarker(city.getMarkerJson()),
                toAsset(city.getCityImageAsset()),
                getCityStatus(user, city),
                city.getUnlockRuleJson(),
                locations
        );
    }

    @Transactional(readOnly = true)
    public List<LocationMenuItemResponse> getCityLocations(UUID cityId, UUID userId) {
        if (!cityRepository.existsById(cityId)) {
            throw new ResourceNotFoundException("City not found: " + cityId);
        }

        User user = resolveUser(userId);

        return locationContentService.getLocationMenuForCity(cityId, user.getId());
    }

    private CityListItemResponse toListItem(City city, CityStatus status) {
        return new CityListItemResponse(
                city.getId(),
                city.getName(),
                city.getSlug(),
                city.getDescription(),
                city.getDisplayOrder(),
                toMarker(city.getMarkerJson()),
                toAsset(city.getCityImageAsset()),
                status
        );
    }

    private CityStatus getCityStatus(User user, City city) {
        return learnerCityProgressRepository.findByUserAndCity(user, city)
                .map(progress -> progress.getStatus())
                .orElse(CityStatus.LOCKED);
    }

    private CityMarkerResponse toMarker(Map<String, Object> markerJson) {
        if (markerJson == null) {
            return new CityMarkerResponse(null, null, null, Map.of());
        }

        return new CityMarkerResponse(
                getDouble(markerJson, "xPercent"),
                getDouble(markerJson, "yPercent"),
                getString(markerJson, "iconAssetKey"),
                markerJson
        );
    }

    private AssetMetadataResponse toAsset(StaticAsset asset) {
        return AssetResponseMapper.toMetadataResponse(asset);
    }

    private String getString(Map<String, Object> map, String key) {
        return JsonValueReader.getString(map, key);
    }

    private Double getDouble(Map<String, Object> map, String key) {
        return JsonValueReader.getDouble(map, key);
    }

    private User resolveUser(UUID userId) {
        if (userId == null) {
            return demoUserService.getDemoUser();
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}
