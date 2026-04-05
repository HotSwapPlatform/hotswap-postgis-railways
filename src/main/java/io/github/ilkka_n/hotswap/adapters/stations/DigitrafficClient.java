package io.github.ilkka_n.hotswap.adapters.stations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

/**
 * Hakee Suomen rautatieasemien tiedot Digitraffic REST API:sta.
 * API-dokumentaatio: <a href="https://www.digitraffic.fi/rautatieliikenne/">https://www.digitraffic.fi/rautatieliikenne/</a>
 */
@Slf4j
@Component
class DigitrafficClient {

    private static final String STATIONS_URL = "https://rata.digitraffic.fi/api/v1/metadata/stations";

    private final RestClient restClient;

    DigitrafficClient() {
        this.restClient = RestClient.builder()
                .defaultHeader("Digitraffic-User", "hotswap-platform/1.0 github.com/ilkka-n")
                .build();
    }

    List<Station> fetchFinnishPassengerStations() {
        log.info("Haetaan asematiedot Digitrafficista: {}", STATIONS_URL);
        DigitraficStationResponse[] response = restClient.get()
                .uri(STATIONS_URL)
                .retrieve()
                .body(DigitraficStationResponse[].class);

        if (response == null) {
            log.warn("Digitraffic palautti tyhjän vastauksen");
            return List.of();
        }

        List<Station> stations = Arrays.stream(response)
                .filter(s -> "FI".equals(s.countryCode()) && Boolean.TRUE.equals(s.passengerTraffic()))
                .map(s -> new Station(s.stationShortCode(), s.stationName(), s.latitude(), s.longitude()))
                .toList();

        log.info("Haettu {} matkustajaliikenneasemaa", stations.size());
        return stations;
    }

    record DigitraficStationResponse(
            String stationShortCode,
            String stationName,
            Double latitude,
            Double longitude,
            String countryCode,
            Boolean passengerTraffic,
            String type
    ) {}
}
