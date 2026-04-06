package io.github.ilkka_n.hotswap.adapters.config;

import io.github.ilkka_n.hotswap.adapters.stations.MultiRailwayDataSelectorAdapter;
import io.github.ilkka_n.hotswap.core.ports.MultiAdapterSelectorPort;
import io.github.ilkka_n.hotswap.core.ports.RailwayDataPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RailwayAdapterConfig {

    @Bean
    public MultiAdapterSelectorPort<RailwayDataPort> railwayDataSelector(
            Map<String, RailwayDataPort> railwayDataAdapters) {
        return new MultiRailwayDataSelectorAdapter(railwayDataAdapters);
    }
}
