package io.github.ilkka_n.hotswap.adapters.stations;

import io.github.ilkka_n.hotswap.core.ports.MultiAdapterSelectorPort;
import io.github.ilkka_n.hotswap.core.ports.RailwayDataPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestStationController.class)
class RestStationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StationLoadService stationLoadService;

    @MockitoBean
    private MultiAdapterSelectorPort<RailwayDataPort> railwayDataSelector;

    @MockitoBean
    private Map<String, RailwayDataPort> railwayDataAdapters;

    @Test
    void shouldReturnNotLoadedStatus() throws Exception {
        when(stationLoadService.getStatusDTO())
                .thenReturn(new StationStatusDTO("NOT_LOADED", 0));

        mockMvc.perform(get("/api/stations/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOT_LOADED"))
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void shouldReturnLoadedStatusWithCount() throws Exception {
        when(stationLoadService.getStatusDTO())
                .thenReturn(new StationStatusDTO("LOADED", 57));

        mockMvc.perform(get("/api/stations/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LOADED"))
                .andExpect(jsonPath("$.count").value(57));
    }

    @Test
    void shouldReturnStationList() throws Exception {
        when(stationLoadService.getAllStations()).thenReturn(List.of(
                new StationDTO("HKI", "Helsinki", 60.1719, 24.9414, "Passenger"),
                new StationDTO("BERLN", "Berlin Hbf", 52.5251, 13.3694, "European")));

        mockMvc.perform(get("/api/stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shortCode").value("HKI"))
                .andExpect(jsonPath("$[0].source").value("Passenger"))
                .andExpect(jsonPath("$[1].shortCode").value("BERLN"))
                .andExpect(jsonPath("$[1].source").value("European"));
    }

    @Test
    void shouldReturnEmptyListWhenNoStations() throws Exception {
        when(stationLoadService.getAllStations()).thenReturn(List.of());

        mockMvc.perform(get("/api/stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldReturnAdapterStatus() throws Exception {
        when(railwayDataSelector.getAdapterNames()).thenReturn(List.of("Passenger", "European"));
        when(railwayDataSelector.getActiveAdapterNames()).thenReturn(List.of("Passenger"));

        mockMvc.perform(get("/api/stations/adapters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available[0]").value("Passenger"))
                .andExpect(jsonPath("$.active[0]").value("Passenger"));
    }

    @Test
    void shouldLoadAdapterAndReturn202() throws Exception {
        RailwayDataPort mockAdapter = mock(RailwayDataPort.class);
        when(railwayDataAdapters.get("Passenger")).thenReturn(mockAdapter);

        mockMvc.perform(post("/api/stations/adapters/Passenger/load"))
                .andExpect(status().isAccepted());

        verify(railwayDataSelector).activate("Passenger");
        verify(stationLoadService).loadAdapterAsync("Passenger", mockAdapter);
    }

    @Test
    void shouldReturn404WhenLoadingUnknownAdapter() throws Exception {
        when(railwayDataAdapters.get("Unknown")).thenReturn(null);

        mockMvc.perform(post("/api/stations/adapters/Unknown/load"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUnloadAdapter() throws Exception {
        mockMvc.perform(delete("/api/stations/adapters/Passenger/unload"))
                .andExpect(status().isNoContent());

        verify(railwayDataSelector).close("Passenger");
        verify(stationLoadService).unloadAdapter("Passenger");
    }
}
