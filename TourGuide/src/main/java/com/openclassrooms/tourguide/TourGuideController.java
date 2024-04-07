package com.openclassrooms.tourguide;
// test wkf
import com.openclassrooms.tourguide.dto.AttractionDto;
import com.openclassrooms.tourguide.dto.AttractionsResponse;
import com.openclassrooms.tourguide.dto.LocationDto;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.location.VisitedLocation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tripPricer.Provider;

import java.util.List;

@RestController
@AllArgsConstructor
public class TourGuideController {

    public final TourGuideService tourGuideService;
    public final RewardsService rewardsService;

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @RequestMapping("/getLocation")
    public VisitedLocation getLocation(@RequestParam String userName) {
        return tourGuideService.getUserLocation(getUser(userName));
    }

    @RequestMapping("/getNearbyAttractions")
    public ResponseEntity<AttractionsResponse> getNearbyAttractions(@RequestParam String userName) {
        final User user = getUser(userName);
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);
        List<AttractionDto> attractionDtos = tourGuideService.getNearByAttractions(visitedLocation).stream().map(attraction ->
                new AttractionDto(
                        attraction.attractionName,
                        new LocationDto(attraction.longitude, attraction.latitude),
                        new LocationDto(visitedLocation.location.longitude, visitedLocation.location.latitude),
                        rewardsService.getDistance(attraction, visitedLocation.location),
                        rewardsService.getRewardPoints(attraction,user)
                )
        ).toList();
        return ResponseEntity.ok(new AttractionsResponse(attractionDtos));
    }

    @RequestMapping("/getRewards")
    public List<UserReward> getRewards(@RequestParam String userName) {
        return tourGuideService.getUserRewards(getUser(userName));
    }

    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
        return tourGuideService.getTripDeals(getUser(userName));
    }

    private User getUser(String userName) {
        return tourGuideService.getUser(userName);
    }


}
