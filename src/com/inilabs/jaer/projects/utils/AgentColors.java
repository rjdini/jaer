package com.inilabs.jaer.projects.util;

import java.awt.Color;

public final class AgentColors {
    private static final Color[] PALETTE = new Color[]{
        new Color(0x1f77b4), new Color(0xff7f0e), new Color(0x2ca02c),
        new Color(0xd62728), new Color(0x9467bd), new Color(0x8c564b),
        new Color(0xe377c2), new Color(0x7f7f7f), new Color(0xbcbd22),
        new Color(0x17becf)
    };
    private AgentColors() {}
    public static Color colorForKey(String key){
        if (key == null) return PALETTE[0];
        int idx = Math.floorMod(key.hashCode(), PALETTE.length);
        return PALETTE[idx];
    }
    public static Color[] palette(){ return PALETTE.clone(); }
}
