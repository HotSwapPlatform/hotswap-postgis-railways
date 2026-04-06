package io.github.ilkka_n.hotswap.core.ports;

import io.github.ilkka_n.hotswap.core.domain.Station;

import java.util.List;

public interface RailwayDataPort {
    List<Station> fetchStations();
    String name();
}
