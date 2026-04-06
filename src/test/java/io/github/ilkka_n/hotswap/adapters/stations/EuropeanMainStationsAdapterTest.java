package io.github.ilkka_n.hotswap.adapters.stations;

import io.github.ilkka_n.hotswap.core.domain.Station;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EuropeanMainStationsAdapterTest {

    private final EuropeanMainStationsAdapter adapter = new EuropeanMainStationsAdapter();

    @Test
    void nameShouldBeEuropean() {
        assertEquals("European", adapter.name());
    }

    @Test
    void fetchStationsShouldReturnNonEmptyList() {
        List<Station> stations = adapter.fetchStations();
        assertFalse(stations.isEmpty());
    }

    @Test
    void fetchStationsShouldReturnValidCoordinates() {
        adapter.fetchStations().forEach(s -> {
            assertTrue(s.latitude() >= -90 && s.latitude() <= 90, "Virheellinen leveysaste: " + s.latitude());
            assertTrue(s.longitude() >= -180 && s.longitude() <= 180, "Virheellinen pituusaste: " + s.longitude());
        });
    }

    @Test
    void shortCodesShouldBeUnique() {
        List<Station> stations = adapter.fetchStations();
        long uniqueCount = stations.stream().map(Station::shortCode).distinct().count();
        assertEquals(stations.size(), uniqueCount, "Duplikaatti short code löytyi");
    }
}
