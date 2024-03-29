package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@Service
public class RewardsService {
    public static final int PARALLELISM = 1000;
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private final int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;
    private final Executor executor = Executors.newFixedThreadPool(1000);

    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    public void calculateRewards(User user) {
        List<VisitedLocation> userLocations = List.copyOf(user.getVisitedLocations());
        List<Attraction> attractions = List.copyOf(gpsUtil.getAttractions());

        List<CompletableFuture<Void>> futures = userLocations.stream()
                .flatMap(visitedLocation ->
                        attractions.stream().filter(attraction -> nearAttraction(visitedLocation, attraction))
                                .map(attraction -> calculateRewardAsync(visitedLocation, attraction, user))
                ).toList();

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join(); //execution de l'ajout de user reward
    }

    public void calculateRewards(List<User> users) {
        ForkJoinPool customThreadPool = new ForkJoinPool(PARALLELISM);// je fixe le size du pool de threads

        customThreadPool.submit(() ->
                users.parallelStream().forEach(this::calculateRewards)
        ).join();

        customThreadPool.shutdown(); // suppression du pool

    }

    public CompletableFuture<Void> calculateRewardAsync(VisitedLocation visitedLocation, Attraction attraction, User user) {
        return CompletableFuture.supplyAsync(() -> getRewardPoints(attraction, user), executor).thenAccept((integer) -> {
            user.addUserReward(new UserReward(visitedLocation, attraction, integer));
        });
    }

    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return !(getDistance(attraction, location) > attractionProximityRange);
    }

    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
    }

    public int getRewardPoints(Attraction attraction, User user) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
    }

}
