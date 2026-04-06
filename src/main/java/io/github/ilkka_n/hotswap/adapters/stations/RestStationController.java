package io.github.ilkka_n.hotswap.adapters.stations;

import io.github.ilkka_n.hotswap.core.ports.MultiAdapterSelectorPort;
import io.github.ilkka_n.hotswap.core.ports.RailwayDataPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class RestStationController {

    private final StationLoadService stationLoadService;
    private final MultiAdapterSelectorPort<RailwayDataPort> railwayDataSelector;
    private final Map<String, RailwayDataPort> railwayDataAdapters;

    @GetMapping("/status")
    public StationStatusDTO getStatus() {
        return stationLoadService.getStatusDTO();
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

    @GetMapping("/adapters")
    public AdapterStatusDTO getAdapters() {
        return new AdapterStatusDTO(
                railwayDataSelector.getAdapterNames(),
                railwayDataSelector.getActiveAdapterNames());
    }

    @PostMapping("/adapters/{name}/load")
    public ResponseEntity<Void> loadAdapter(@PathVariable String name) {
        RailwayDataPort adapter = railwayDataAdapters.get(name);
        if (adapter == null) return ResponseEntity.notFound().build();
        railwayDataSelector.activate(name);
        stationLoadService.loadAdapterAsync(name, adapter);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/adapters/{name}/unload")
    public ResponseEntity<Void> unloadAdapter(@PathVariable String name) {
        railwayDataSelector.close(name);
        stationLoadService.unloadAdapter(name);
        return ResponseEntity.noContent().build();
    }

    record AdapterStatusDTO(List<String> available, List<String> active) {}
}
