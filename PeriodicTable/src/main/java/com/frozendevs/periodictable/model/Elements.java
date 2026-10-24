package com.frozendevs.periodictable.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Static element symbol &rarr; atomic number lookup. Used to resolve a decay
 * daughter (e.g. "Th") to the atomic number needed to open its element screen,
 * without touching the database on the UI thread.
 */
public final class Elements {

    private static final String[] SYMBOLS = {
            "H", "He", "Li", "Be", "B", "C", "N", "O", "F", "Ne",
            "Na", "Mg", "Al", "Si", "P", "S", "Cl", "Ar", "K", "Ca",
            "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn",
            "Ga", "Ge", "As", "Se", "Br", "Kr", "Rb", "Sr", "Y", "Zr",
            "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In", "Sn",
            "Sb", "Te", "I", "Xe", "Cs", "Ba", "La", "Ce", "Pr", "Nd",
            "Pm", "Sm", "Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb",
            "Lu", "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "Hg",
            "Tl", "Pb", "Bi", "Po", "At", "Rn", "Fr", "Ra", "Ac", "Th",
            "Pa", "U", "Np", "Pu", "Am", "Cm", "Bk", "Cf", "Es", "Fm",
            "Md", "No", "Lr", "Rf", "Db", "Sg", "Bh", "Hs", "Mt", "Ds",
            "Rg", "Cn", "Nh", "Fl", "Mc", "Lv", "Ts", "Og"
    };

    private static final Map<String, Integer> NUMBER = new HashMap<>();

    static {
        for (int i = 0; i < SYMBOLS.length; i++) {
            NUMBER.put(SYMBOLS[i], i + 1);
        }
    }

    private Elements() {
    }

    /** Atomic number for an element symbol, or -1 if unknown. */
    public static int numberOf(String symbol) {
        Integer n = NUMBER.get(symbol);
        return n == null ? -1 : n;
    }
}
