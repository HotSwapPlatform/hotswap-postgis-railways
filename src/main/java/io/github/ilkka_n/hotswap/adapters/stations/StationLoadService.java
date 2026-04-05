package io.github.ilkka_n.hotswap.adapters.stations;

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
    private final DigitrafficClient digitrafficClient;

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
                log.info("Asematiedot löydetty PostgreSQL:sta: {} asemaa", existingCount);
            }
        } catch (DataAccessException e) {
            log.debug("Asematietojen tila: PostgreSQL ei saatavilla käynnistyksessä");
        }
    }

    /**
     * Käynnistää asematietojen latauksen asynkronisesti.
     * Ei tee mitään jos lataus on jo käynnissä tai valmis.
     */
    public void loadAsync() {
        boolean started = status.compareAndSet(StationLoadStatus.NOT_LOADED, StationLoadStatus.LOADING)
                || status.compareAndSet(StationLoadStatus.ERROR, StationLoadStatus.LOADING);
        if (!started) {
            log.debug("Asematietojen lataus jo käynnissä tai valmis, ohitetaan");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                List<Station> stations = digitrafficClient.fetchFinnishPassengerStations();
                stationRepository.saveAll(stations);
                count.set(stations.size());
                status.set(StationLoadStatus.LOADED);
                log.info("Asematiedot ladattu: {} asemaa", stations.size());
            } catch (Exception e) {
                log.error("Asematietojen lataus epäonnistui: {}", e.getMessage());
                status.set(StationLoadStatus.ERROR);
            }
        });
    }

    public StationStatusDTO getStatusDTO() {
        return new StationStatusDTO(status.get().name(), count.get());
    }

    public void clear() {
        stationRepository.deleteAll();
        count.set(0);
        status.set(StationLoadStatus.NOT_LOADED);
        log.info("Asematiedot tyhjennetty");
    }

    public List<StationDTO> getAllStations() {
        try {
            return stationRepository.findAll().stream()
                    .map(s -> new StationDTO(s.shortCode(), s.name(), s.latitude(), s.longitude()))
                    .toList();
        } catch (DataAccessException e) {
            log.warn("Asemien haku epäonnistui: {}", e.getMessage());
            return List.of();
        }
    }
}
