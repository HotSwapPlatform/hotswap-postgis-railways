package io.github.ilkka_n.hotswap.adapters.stations;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StationLoadServiceTest {

    @Mock
    private PostgreSQLStationRepository stationRepository;

    @Mock
    private DigitrafficClient digitrafficClient;

    private StationLoadService service;

    @BeforeEach
    void setUp() {
        service = new StationLoadService(stationRepository, digitrafficClient);
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
    void loadAsyncSetsLoadedStatusAfterSuccess() {
        List<Station> stations = List.of(
                new Station("HKI", "Helsinki", 60.1719, 24.9414),
                new Station("TPE", "Tampere", 61.4978, 23.7610));
        when(digitrafficClient.fetchFinnishPassengerStations()).thenReturn(stations);

        service.loadAsync();

        await().atMost(3, TimeUnit.SECONDS).until(
                () -> "LOADED".equals(service.getStatusDTO().status()));

        assertEquals(2, service.getStatusDTO().count());
        verify(stationRepository).saveAll(stations);
    }

    @Test
    void loadAsyncSetsErrorStatusOnFailure() {
        when(digitrafficClient.fetchFinnishPassengerStations())
                .thenThrow(new RuntimeException("Verkkovirhe"));

        service.loadAsync();

        await().atMost(3, TimeUnit.SECONDS).until(
                () -> "ERROR".equals(service.getStatusDTO().status()));
    }

    @Test
    void loadAsyncIsIdempotentWhenAlreadyLoaded() {
        when(stationRepository.count()).thenReturn(10L);
        service.initStatusFromDatabase();

        service.loadAsync();

        verify(digitrafficClient, never()).fetchFinnishPassengerStations();
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
