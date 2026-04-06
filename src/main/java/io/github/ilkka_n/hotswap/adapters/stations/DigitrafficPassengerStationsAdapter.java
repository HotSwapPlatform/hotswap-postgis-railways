package io.github.ilkka_n.hotswap.adapters.stations;

import io.github.ilkka_n.hotswap.core.domain.Station;
import io.github.ilkka_n.hotswap.core.ports.RailwayDataPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

/**
 * Hakee Suomen matkustajaliikenneasemien tiedot Digitraffic REST API:sta.
 * Aineisto: Digitraffic / Fintraffic (CC 4.0 BY)
 * API-dokumentaatio: <a href="https://www.digitraffic.fi/rautatieliikenne/">https://www.digitraffic.fi/rautatieliikenne/</a>
 */
@Slf4j
@Component("Passenger")
public class DigitrafficPassengerStationsAdapter implements RailwayDataPort {

    private static final String STATIONS_URL = "https://rata.digitraffic.fi/api/v1/metadata/stations";

    private final RestClient restClient;

    DigitrafficPassengerStationsAdapter() {
        this.restClient = RestClient.builder()
                .defaultHeader("Digitraffic-User", "hotswap-platform/1.0 github.com/ilkka-n")
                .build();
    }

    @Override
    public String name() {
        return "Passenger";
    }

    @Override
    public List<Station> fetchStations() {
        log.info("Haetaan matkustajaliikenneasemien tiedot Digitrafficista");
        DigitrafficStationResponse[] response = restClient.get()
                .uri(STATIONS_URL)
                .retrieve()
                .body(DigitrafficStationResponse[].class);

        if (response == null) {
            log.warn("Digitraffic palautti tyhjän vastauksen");
            return List.of();
        }

        List<Station> stations = Arrays.stream(response)
                .filter(s -> "FI".equals(s.countryCode()) && Boolean.TRUE.equals(s.passengerTraffic()))
                .map(s -> new Station(s.stationShortCode(), s.stationName(),
                        s.latitude() != null ? s.latitude() : 0.0,
                        s.longitude() != null ? s.longitude() : 0.0,
                        "Passenger"))
                .toList();

        log.info("Haettu {} matkustajaliikenneasemaa", stations.size());
        return stations;
    }

    record DigitrafficStationResponse(
            String stationShortCode,
            String stationName,
            Double latitude,
            Double longitude,
            String countryCode,
            Boolean passengerTraffic,
            String type
    ) {}
}
