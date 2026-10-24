package com.frozendevs.periodictable.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A navigable decay branch: the daughter nuclide a decay mode leads to.
 *
 * Parsed from the per-isotope {@code decayModes} display string, whose lines
 * look like {@code "α → ²¹¹Th"} or {@code "β⁺ (60%) → ⁹B"} (one branch per
 * line, superscript mass number + element symbol after the arrow). Cluster
 * decays list two products ({@code "CD → ²⁰⁷Tl, ³⁴Si"}); the first (heavy)
 * product is taken as the path to follow.
 */
public class DecayTarget {

    public final String label;         // full branch text, e.g. "α → ²¹¹Th"
    public final String isotopeSymbol; // daughter nuclide, e.g. "²¹¹Th"
    public final int atomicNumber;     // daughter element's atomic number

    // Superscript digits used for mass numbers, plus the superscript-m isomer marker.
    private static final String SUP = "⁰¹²³⁴⁵⁶⁷⁸⁹";
    private static final char ISOMER = 'ᵐ';
    private static final char ARROW = '→'; // →

    public DecayTarget(String label, String isotopeSymbol, int atomicNumber) {
        this.label = label;
        this.isotopeSymbol = isotopeSymbol;
        this.atomicNumber = atomicNumber;
    }

    /** Navigable daughter nuclides found in a decayModes string (one per branch). */
    public static List<DecayTarget> parse(String decayModes) {
        List<DecayTarget> targets = new ArrayList<>();

        if (decayModes == null || decayModes.isEmpty()) {
            return targets;
        }

        for (String line : decayModes.split("\n")) {
            String branch = line.trim();

            int arrow = branch.indexOf(ARROW);
            if (arrow < 0) {
                continue;
            }

            // First product after the arrow (cluster decay lists several).
            String after = branch.substring(arrow + 1).trim();
            String token = after.split(",")[0].trim();

            String symbol = elementSymbol(token);
            if (symbol == null) {
                continue; // e.g. "Various" for spontaneous fission
            }

            int number = Elements.numberOf(symbol);
            if (number < 0) {
                continue;
            }

            targets.add(new DecayTarget(branch, token, number));
        }

        return targets;
    }

    /**
     * Element symbol from a nuclide token like "²¹¹Th" (-> "Th"), or null if the
     * token isn't a nuclide (no leading superscript mass, or non-letter tail).
     */
    private static String elementSymbol(String token) {
        int i = 0;
        while (i < token.length()) {
            char c = token.charAt(i);
            if (SUP.indexOf(c) >= 0 || c == ISOMER) {
                i++;
            } else {
                break;
            }
        }

        if (i == 0 || i >= token.length()) {
            return null;
        }

        String rest = token.substring(i);
        for (int j = 0; j < rest.length(); j++) {
            if (!Character.isLetter(rest.charAt(j))) {
                return null;
            }
        }

        return rest;
    }
}
