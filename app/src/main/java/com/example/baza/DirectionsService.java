package com.example.baza;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DirectionsService {
    @GET("maps/api/directions/json")
    Call<DirectionsResponse> getDirections(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("key") String apiKey,
            @Query("mode") String mode // e.g., "walking"
    );
}
