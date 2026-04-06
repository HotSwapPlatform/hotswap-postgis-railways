package io.github.ilkka_n.hotswap.adapters.stations;

import io.github.ilkka_n.hotswap.core.domain.Station;
import io.github.ilkka_n.hotswap.core.ports.RailwayDataPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StationLoadServiceTest {

    @Mock
    private PostgreSQLStationRepository stationRepository;

    @Mock
    private RailwayDataPort railwayDataPort;

    private StationLoadService service;

    @BeforeEach
    void setUp() {
        service = new StationLoadService(stationRepository);
    }

    @Test
    void initialStatusIsNotLoaded() {
        assertEquals("NOT_LOADED", service.getStatusDTO().status());
        assertEquals(0, service.getStatusDTO().count());
    }

    @Test
    void initStatusFromDatabaseSetsLoadedWhenDataExists() {
        when(stationRepository.count()).thenReturn(42L);

        service.initStatusFromDatabase();

        assertEquals("LOADED", service.getStatusDTO().status());
        assertEquals(42, service.getStatusDTO().count());
    }

    @Test
    void initStatusFromDatabaseStaysNotLoadedWhenEmpty() {
        when(stationRepository.count()).thenReturn(0L);

        service.initStatusFromDatabase();

        assertEquals("NOT_LOADED", service.getStatusDTO().status());
    }

    @Test
    void initStatusFromDatabaseStaysNotLoadedWhenPgDown() {
        when(stationRepository.count())
                .thenThrow(new DataAccessResourceFailureException("Yhteys katkaistu"));

        service.initStatusFromDatabase();

        assertEquals("NOT_LOADED", service.getStatusDTO().status());
    }

    @Test
    void loadAdapterAsyncSetsLoadedStatusAfterSuccess() {
        List<Station> stations = List.of(
                new Station("HKI", "Helsinki", 60.1719, 24.9414, "Passenger"),
                new Station("TPE", "Tampere", 61.4978, 23.7610, "Passenger"));
        when(railwayDataPort.fetchStations()).thenReturn(stations);
        when(stationRepository.count()).thenReturn(2L);

        service.loadAdapterAsync("Passenger", railwayDataPort);

        await().atMost(3, TimeUnit.SECONDS).until(
                () -> "LOADED".equals(service.getStatusDTO().status()));

        verify(stationRepository).deleteBySource("Passenger");
        verify(stationRepository).saveAll(anyList());
    }

    @Test
    void loadAdapterAsyncSetsErrorStatusOnFailure() {
        when(railwayDataPort.fetchStations()).thenThrow(new RuntimeException("Verkkovirhe"));

        service.loadAdapterAsync("Passenger", railwayDataPort);

        await().atMost(3, TimeUnit.SECONDS).until(
                () -> "ERROR".equals(service.getStatusDTO().status()));
    }

    @Test
    void unloadAdapterRemovesStationsFromSource() {
        when(stationRepository.count()).thenReturn(0L);

        service.unloadAdapter("Passenger");

        verify(stationRepository).deleteBySource("Passenger");
        assertEquals("NOT_LOADED", service.getStatusDTO().status());
    }

    @Test
    void unloadAdapterKeepsLoadedStatusWhenOtherStationsRemain() {
        when(stationRepository.count()).thenReturn(5L);

        service.unloadAdapter("European");

        verify(stationRepository).deleteBySource("European");
        assertEquals("LOADED", service.getStatusDTO().status());
    }

    @Test
    void getAllStationsReturnsEmptyListWhenPgDown() {
        when(stationRepository.findAll())
                .thenThrow(new DataAccessResourceFailureException("Ei yhteyttä"));

        List<StationDTO> result = service.getAllStations();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
