package io.github.ilkka_n.hotswap.adapters.stations;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class StationMapViewController {

    @GetMapping("/")
    String mapPage() {
        return "station-map";
    }
}
