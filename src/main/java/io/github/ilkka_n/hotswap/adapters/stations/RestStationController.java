package io.github.ilkka_n.hotswap.adapters.stations;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class RestStationController {

    private final StationLoadService stationLoadService;

    @GetMapping("/status")
    public StationStatusDTO getStatus() {
        return stationLoadService.getStatusDTO();
    }

    @PostMapping("/load")
    public ResponseEntity<Void> load() {
        stationLoadService.loadAsync();
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clear() {
        stationLoadService.clear();
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<StationDTO> getStations() {
        return stationLoadService.getAllStations();
    }
}
