package com.example.baza;


import static org.junit.Assert.assertEquals;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class MapTest {
    @Test
    @DisplayName("Url")
    public void testGetDirectionsUrl() {
        LatLng origin =  new LatLng(51.250945,22.5747267);
        String expectedUrl = "https://maps.googleapis.com/maps/api/directions/json?origin=51.250945,22.5747267&destination=49.47356220575305,20.178220197558403&mode=walking&key=AIzaSyC4KaLnKSYLuF9xCyzudGh8DMCB-6HefJA";
        LatLng destination= new LatLng(49.47356220575305,20.1782201975584030);
        String actualUrl = MapActivity.getDirectionsUrl(origin, destination);

        assertEquals(expectedUrl, actualUrl);
    }
}
