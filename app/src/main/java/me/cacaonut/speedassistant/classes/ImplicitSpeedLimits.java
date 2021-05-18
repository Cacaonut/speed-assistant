package me.cacaonut.speedassistant.classes;

import java.util.HashMap;
import java.util.Map;

public class ImplicitSpeedLimits {
    public static Map<String, String> values = new HashMap<String, String>() {{
        // Argentina
        put("AR:urban", "40");
        put("AR:urban:primary", "60");
        put("AR:urban:secondary", "60");
        put("AR:rural", "110");

        // Austria
        put("AT:urban", "50");
        put("AT:rural", "100");
        put("AT:bicycle_road", "30");
        put("AT:trunk", "100");
        put("AT:motorway", "130");

        // Australia
        put("AU:urban", "50");
        put("AU:rural", "100");

        // Belgium
        put("BE-VLG:urban", "50");
        put("BE-WAL:urban", "50");
        put("BE-BRU:urban", "30");
        put("BE-VLG:rural", "70");
        put("BE-WAL:rural", "90");
        put("BE-BRU:rural", "70");
        put("BE:living_street", "20");
        put("BE:bicycle_road", "30");
        put("BE:school", "30");
        put("BE:zone30", "30");
        put("BE:trunk", "120");
        put("BE:motorway", "120");

        // Bulgaria
        put("BG:urban", "50");
        put("BG:rural", "90");
        put("BG:living_street", "20");
        put("BG:trunk", "120");
        put("BG:motorway", "140");

        // Belarus
        put("BY:urban", "60");
        put("BY:rural", "90");
        put("BY:living_street", "20");
        put("BY:motorway", "110");

        // Canada
        put("CA-AB:urban", "50");
        put("CA-BC:urban", "50");
        put("CA-MB:urban", "50");
        put("CA-ON:urban", "50");
        put("CA-QC:urban", "50");
        put("CA-AB:rural", "80");
        put("CA-BC:rural", "80");
        put("CA-MB:rural", "90");
        put("CA-ON:rural", "80");
        put("CA-QC:rural", "80");
        put("CA-QC:motorway", "100");
        put("CA-SK:nsl", "80");

        // Switzerland
        put("CH:urban", "50");
        put("CH:rural", "80");
        put("CH:trunk", "100");
        put("CH:motorway", "120");

        // Czechia
        put("CZ:urban", "50");
        put("CZ:rural", "90");
        put("CZ:pedestrian_zone", "20");
        put("CZ:living_street", "20");
        put("CZ:urban_motorway", "80");
        put("CZ:urban_trunk", "80");
        put("CZ:trunk", "110");
        put("CZ:motorway", "130");

        // Germany
        put("DE:urban", "50");
        put("DE:rural", "100");
        put("DE:living_street", "7");
        put("DE:bicycle_road", "30");
        put("DE:motorway", "none");

        // Denmark
        put("DK:urban", "50");
        put("DK:rural", "80");
        put("DK:motorway", "130");

        // Estonia
        put("EE:urban", "50");
        put("EE:rural", "90");

        // Spain
        put("ES:urban", "50");
        put("ES:rural", "90");
        put("ES:living_street", "20");
        put("ES:zone30", "30");
        put("ES:trunk", "90");
        put("ES:motorway", "120");

        // Finland
        put("FI:urban", "50");
        put("FI:rural", "80");
        put("FI:trunk", "100");
        put("FI:motorway", "120");

        // France
        put("FR:urban", "50");
        put("FR:rural", "80");
        put("FR:zone30", "30");
        put("FR:motorway", "130");

        // Great Britain
        put("GB:motorway", "70 mph");
        put("GB:nsl_dual", "70 mph");
        put("GB:nsl_single", "60 mph");

        // Greece
        put("GR:urban", "50");
        put("GR:rural", "90");
        put("GR:trunk", "110");
        put("GR:motorway", "130");

        // Hungary
        put("HU:urban", "50");
        put("HU:rural", "90");
        put("HU:living_street", "20");
        put("HU:trunk", "110");
        put("HU:motorway", "130");

        // Italy
        put("IT:urban", "50");
        put("IT:rural", "90");
        put("IT:trunk", "110");
        put("IT:motorway", "130");

        // Japan
        put("JP:nsl", "60");
        put("JP:express", "100");

        // Lithuania
        put("LT:urban", "50");
        put("LT:rural", "90");

        // Norway
        put("NO:urban", "50");
        put("NO:rural", "80");

        // Philippines
        put("PH:urban", "30");
        put("PH:rural", "80");

        // Portugal
        put("PT:urban", "50");
        put("PT:rural", "90");
        put("PT:trunk", "100");
        put("PT:motorway", "120");

        // Romania
        put("RO:urban", "50");
        put("RO:rural", "90");
        put("RO:trunk", "100");
        put("RO:motorway", "130");

        // Serbia
        put("RS:urban", "50");
        put("RS:rural", "80");
        put("RS:living_street", "10");
        put("RS:trunk", "100");
        put("RS:motorway", "130");

        // Russia
        put("RU:urban", "60");
        put("RU:rural", "90");
        put("RU:living_street", "20");
        put("RU:motorway", "110");

        // Sweden
        put("SE:urban", "50");
        put("SE:rural", "70");
        put("SE:trunk", "90");
        put("SE:motorway", "110");

        // Slovenia
        put("SI:urban", "50");
        put("SI:rural", "90");
        put("SI:trunk", "110");
        put("SI:motorway", "130");

        // Slovakia
        put("SK:urban", "50");

        // Ukraine
        put("UA:urban", "50");
        put("UA:rural", "90");
        put("UA:living_street", "20");
        put("UA:trunk", "110");
        put("UA:motorway", "130");

        // United Kingdom
        put("UK:nsl_restricted", "30 mph");
        put("UK:motorway", "70 mph");
        put("UK:nsl_dual", "70 mph");
        put("UK:nsl_single", "60 mph");

        // Uzbekistan
        put("UZ:urban", "70");
        put("UZ:rural", "100");
        put("UZ:living_street", "30");
        put("UZ:motorway", "110");
    }};
}
