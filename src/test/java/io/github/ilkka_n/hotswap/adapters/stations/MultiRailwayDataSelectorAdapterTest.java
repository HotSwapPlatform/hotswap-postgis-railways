package io.github.ilkka_n.hotswap.adapters.stations;

import io.github.ilkka_n.hotswap.core.domain.Station;
import io.github.ilkka_n.hotswap.core.ports.RailwayDataPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MultiRailwayDataSelectorAdapterTest {

    private RailwayDataPort adapterA;
    private RailwayDataPort adapterB;
    private MultiRailwayDataSelectorAdapter selector;

    @BeforeEach
    void setUp() {
        adapterA = new StubRailwayDataPort("A", List.of(new Station("HKI", "Helsinki", 60.17, 24.94, "A")));
        adapterB = new StubRailwayDataPort("B", List.of(new Station("TPE", "Tampere", 61.49, 23.76, "B")));
        selector = new MultiRailwayDataSelectorAdapter(Map.of("A", adapterA, "B", adapterB));
    }

    @Test
    void getAdapterNamesShouldReturnAllAdapters() {
        List<String> names = selector.getAdapterNames();
        assertTrue(names.contains("A"));
        assertTrue(names.contains("B"));
    }

    @Test
    void initiallyNoAdaptersAreActive() {
        assertTrue(selector.getActiveAdapters().isEmpty());
    }

    @Test
    void activateShouldAddAdapterToActive() {
        selector.activate("A");
        assertTrue(selector.getActiveAdapterNames().contains("A"));
    }

    @Test
    void activateShouldNotDuplicateAdapter() {
        selector.activate("A");
        selector.activate("A");
        assertEquals(1, selector.getActiveAdapters().size());
    }

    @Test
    void activateUnknownNameShouldBeIgnored() {
        selector.activate("tuntematon");
        assertTrue(selector.getActiveAdapters().isEmpty());
    }

    @Test
    void closeShouldRemoveAdapterFromActive() {
        selector.activate("A");
        selector.activate("B");
        selector.close("A");
        assertFalse(selector.getActiveAdapterNames().contains("A"));
        assertTrue(selector.getActiveAdapterNames().contains("B"));
    }

    @Test
    void getActiveAdaptersShouldReturnBothWhenBothActive() {
        selector.activate("A");
        selector.activate("B");
        assertEquals(2, selector.getActiveAdapters().size());
    }

    private record StubRailwayDataPort(String name, List<Station> stations) implements RailwayDataPort {
        @Override
        public List<Station> fetchStations() {
            return stations;
        }
    }
}
