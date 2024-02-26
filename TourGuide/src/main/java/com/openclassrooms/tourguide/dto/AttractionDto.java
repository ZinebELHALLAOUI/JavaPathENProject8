package com.openclassrooms.tourguide.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttractionDto {
    public String attractionName;
    public LocationDto attractionLocation;
    public LocationDto userLocation;
    public double attractionDistance;
    public int rewardPoints;
}
