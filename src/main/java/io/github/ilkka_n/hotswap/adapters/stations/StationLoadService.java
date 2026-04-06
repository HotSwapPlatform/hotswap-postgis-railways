package io.github.ilkka_n.hotswap.adapters.stations;

import io.github.ilkka_n.hotswap.core.domain.Station;
import io.github.ilkka_n.hotswap.core.ports.RailwayDataPort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class StationLoadService {

    private final PostgreSQLStationRepository stationRepository;

    private final AtomicReference<StationLoadStatus> status =
            new AtomicReference<>(StationLoadStatus.NOT_LOADED);
    private final AtomicLong count = new AtomicLong(0);

    @PostConstruct
    void initStatusFromDatabase() {
        try {
            long existingCount = stationRepository.count();
            if (existingCount > 0) {
                count.set(existingCount);
                status.set(StationLoadStatus.LOADED);
                log.info("Liikennepaikkatiedot löydetty PostgreSQL:sta: {} kpl", existingCount);
            }
        } catch (DataAccessException e) {
            log.debug("PostgreSQL ei saatavilla käynnistyksessä");
        }
    }

    /**
     * Lataa yhden adapterin asemat asynkronisesti.
     * Poistaa ensin kyseisen adapterin vanhat tiedot kannasta.
     */
    public void loadAdapterAsync(String adapterName, RailwayDataPort adapter) {
        status.set(StationLoadStatus.LOADING);
        CompletableFuture.runAsync(() -> {
            try {
                List<Station> stations = adapter.fetchStations().stream()
                        .map(s -> new Station(s.shortCode(), s.name(), s.latitude(), s.longitude(), adapterName))
                        .toList();
                stationRepository.deleteBySource(adapterName);
                stationRepository.saveAll(stations);
                long total = stationRepository.count();
                count.set(total);
                status.set(StationLoadStatus.LOADED);
                log.info("Adapteri '{}': ladattu {} liikennepaikkkaa (yhteensä {})", adapterName, stations.size(), total);
            } catch (Exception e) {
                log.error("Adapterin '{}' lataus epäonnistui: {}", adapterName, e.getMessage());
                status.set(StationLoadStatus.ERROR);
            }
        });
    }

    /**
     * Poistaa yhden adapterin asemat kannasta.
     */
    public void unloadAdapter(String adapterName) {
        try {
            stationRepository.deleteBySource(adapterName);
            long total = stationRepository.count();
            count.set(total);
            if (total == 0) status.set(StationLoadStatus.NOT_LOADED);
            else status.set(StationLoadStatus.LOADED);
            log.info("Adapteri '{}' poistettu kannasta", adapterName);
        } catch (DataAccessException e) {
            log.error("Adapterin '{}' poisto epäonnistui: {}", adapterName, e.getMessage());
        }
    }

    public StationStatusDTO getStatusDTO() {
        return new StationStatusDTO(status.get().name(), count.get());
    }

    public void clear() {
        stationRepository.deleteAll();
        count.set(0);
        status.set(StationLoadStatus.NOT_LOADED);
        log.info("Kaikki liikennepaikkatiedot tyhjennetty");
    }

    public List<StationDTO> getAllStations() {
        try {
            return stationRepository.findAll().stream()
                    .map(s -> new StationDTO(s.shortCode(), s.name(), s.latitude(), s.longitude(), s.source()))
                    .toList();
        } catch (DataAccessException e) {
            log.warn("Asemien haku epäonnistui: {}", e.getMessage());
            return List.of();
        }
    }
}
