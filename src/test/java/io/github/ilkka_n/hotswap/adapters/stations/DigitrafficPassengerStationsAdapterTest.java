package io.github.ilkka_n.hotswap.adapters.stations;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DigitrafficPassengerStationsAdapterTest {

    private final DigitrafficPassengerStationsAdapter adapter = new DigitrafficPassengerStationsAdapter();

    @Test
    void nameShouldBePassenger() {
        assertEquals("Passenger", adapter.name());
    }
}
