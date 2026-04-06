package io.github.ilkka_n.hotswap.adapters.stations;

import io.github.ilkka_n.hotswap.core.ports.MultiAdapterSelectorPort;
import io.github.ilkka_n.hotswap.core.ports.RailwayDataPort;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hallitsee useita samanaikaisesti aktiivisia rautatiesdatalähteitä.
 * Kartalla voidaan näyttää yhtä aikaa dataa useammasta lähteestä.
 */
public class MultiRailwayDataSelectorAdapter implements MultiAdapterSelectorPort<RailwayDataPort> {

    private final Map<String, RailwayDataPort> implementations;
    private final Map<String, RailwayDataPort> activeAdapters = new LinkedHashMap<>();

    public MultiRailwayDataSelectorAdapter(Map<String, RailwayDataPort> implementations) {
        this.implementations = implementations;
    }

    @Override
    public void activate(String name) {
        if (name != null && implementations.containsKey(name) && !activeAdapters.containsKey(name)) {
            activeAdapters.put(name, implementations.get(name));
        }
    }

    @Override
    public void close(String name) {
        activeAdapters.remove(name);
    }

    @Override
    public List<RailwayDataPort> getActiveAdapters() {
        return new ArrayList<>(activeAdapters.values());
    }

    @Override
    public List<String> getActiveAdapterNames() {
        return new ArrayList<>(activeAdapters.keySet());
    }

    @Override
    public List<String> getAdapterNames() {
        return new ArrayList<>(implementations.keySet());
    }
}
