package io.github.ilkka_n.hotswap.adapters.stations;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestStationController.class)
class RestStationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StationLoadService stationLoadService;

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
    void shouldTriggerLoadAndReturn202() throws Exception {
        mockMvc.perform(post("/api/stations/load"))
                .andExpect(status().isAccepted());

        verify(stationLoadService).loadAsync();
    }

    @Test
    void shouldReturnStationList() throws Exception {
        when(stationLoadService.getAllStations()).thenReturn(List.of(
                new StationDTO("HKI", "Helsinki", 60.1719, 24.9414),
                new StationDTO("TPE", "Tampere", 61.4978, 23.7610)));

        mockMvc.perform(get("/api/stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shortCode").value("HKI"))
                .andExpect(jsonPath("$[0].name").value("Helsinki"))
                .andExpect(jsonPath("$[1].shortCode").value("TPE"));
    }

    @Test
    void shouldReturnEmptyListWhenNoStations() throws Exception {
        when(stationLoadService.getAllStations()).thenReturn(List.of());

        mockMvc.perform(get("/api/stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
