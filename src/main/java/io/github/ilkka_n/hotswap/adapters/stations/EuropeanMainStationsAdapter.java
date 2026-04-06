package io.github.ilkka_n.hotswap.adapters.stations;

import io.github.ilkka_n.hotswap.core.domain.Station;
import io.github.ilkka_n.hotswap.core.ports.RailwayDataPort;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Euroopan suurimmat rautatieterminaalit — staattinen data.
 */
@Component("European")
public class EuropeanMainStationsAdapter implements RailwayDataPort {

    @Override
    public String name() {
        return "European";
    }

    @Override
    public List<Station> fetchStations() {
        return List.of(
                new Station("STCKH", "Stockholm Centralstation",   59.3305,  18.0585, "European"),
                new Station("OSLO",  "Oslo Sentralstasjon",        59.9119,  10.7502, "European"),
                new Station("CPH",   "København H",                55.6729,  12.5648, "European"),
                new Station("BERLN", "Berlin Hauptbahnhof",        52.5251,  13.3694, "European"),
                new Station("HBURG", "Hamburg Hauptbahnhof",       53.5535,  10.0064, "European"),
                new Station("FRNKF", "Frankfurt Hauptbahnhof",     50.1069,   8.6625, "European"),
                new Station("MNCH",  "München Hauptbahnhof",       48.1402,  11.5600, "European"),
                new Station("ZRCH",  "Zürich HB",                  47.3779,   8.5402, "European"),
                new Station("WIEN",  "Wien Hauptbahnhof",          48.1851,  16.3796, "European"),
                new Station("PRAG",  "Praha hlavní nádraží",       50.0828,  14.4356, "European"),
                new Station("WSAW",  "Warszawa Centralna",         52.2288,  21.0032, "European"),
                new Station("BRSLS", "Bruxelles-Midi",             50.8356,   4.3357, "European"),
                new Station("AMSTR", "Amsterdam Centraal",         52.3791,   4.9003, "European"),
                new Station("PGDN",  "Paris Gare du Nord",         48.8809,   2.3553, "European"),
                new Station("PGLY",  "Paris Gare de Lyon",         48.8448,   2.3736, "European"),
                new Station("LONSP", "London St Pancras",          51.5320,  -0.1236, "European"),
                new Station("LONWL", "London Waterloo",            51.5036,  -0.1132, "European"),
                new Station("ROME",  "Roma Termini",               41.9011,  12.5004, "European"),
                new Station("BRCLN", "Barcelona Sants",            41.3793,   2.1409, "European"),
                new Station("MDRID", "Madrid Atocha",              40.4079,  -3.6896, "European"),
                new Station("LISBN", "Lisboa Santa Apolónia",      38.7132,  -9.1218, "European"),
                new Station("BPST",  "Budapest Keleti",            47.5001,  19.0839, "European"),
                new Station("TALLN", "Tallinn Balti jaam",         59.4373,  24.7370, "European"),
                new Station("RIGA",  "Riga Pasažieru stacija",     56.9455,  24.1138, "European"),
                new Station("VILNS", "Vilnius geležinkelio stotis",54.6713,  25.2838, "European"),
                new Station("KYIV",  "Kyiv Pasazhyrskyi",          50.4407,  30.4868, "European"),
                new Station("ATHEN", "Athína Larissa",             37.9901,  23.7176, "European"),
                new Station("BCHST", "Bucharest Nord",             44.4447,  26.0970, "European"),
                new Station("SOFIA", "Sofia Centralna Gara",       42.7122,  23.3225, "European"),
                new Station("BGRAD", "Beograd Centar",             44.8018,  20.4595, "European")
        );
    }
}
