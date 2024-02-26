package com.openclassrooms.tourguide.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttractionsResponse {
    public List<AttractionDto> attractions;
}
