package com.alikhwa.thikrni;

import android.content.Context;

/** ألوان المظهر (ثيمات). */
public final class Theme {
    public static final String[] NAMES = {"زمردي", "أزرق ليلي", "ذهبي"};

    public final int id, bgTop, bgMid, bgBottom, glowA, glowB;
    public final int accent, accentDark, accentLight, sub, onAccent, navBg;

    private Theme(int id, int bgTop, int bgMid, int bgBottom, int glowA, int glowB,
                  int accent, int accentDark, int accentLight, int sub, int onAccent, int navBg) {
        this.id = id;
        this.bgTop = bgTop;
        this.bgMid = bgMid;
        this.bgBottom = bgBottom;
        this.glowA = glowA;
        this.glowB = glowB;
        this.accent = accent;
        this.accentDark = accentDark;
        this.accentLight = accentLight;
        this.sub = sub;
        this.onAccent = onAccent;
        this.navBg = navBg;
    }

    private static final Theme[] ALL = {
            new Theme(0, 0xFF0B4A36, 0xFF072A24, 0xFF050E1F, 0x20C997, 0xE6C15A,
                    0xFFE6C15A, 0xFFB8912B, 0xFFFFE9A0, 0xFFB7D3C8, 0xFF06281D, 0xF0072A24),
            new Theme(1, 0xFF123C7A, 0xFF0A2250, 0xFF040B1A, 0x4C8DFF, 0x9CC9FF,
                    0xFF9CC9FF, 0xFF5B8FD1, 0xFFE3F0FF, 0xFFB5C7E3, 0xFF07142E, 0xF00A1A3F),
            new Theme(2, 0xFF4A3510, 0xFF2A1D08, 0xFF120C03, 0xE0A030, 0xFFD27A,
                    0xFFF2C94C, 0xFFB8861B, 0xFFFFF0B3, 0xFFE0CFA6, 0xFF2A1A00, 0xF02A1D08)
    };

    public static Theme of(int id) {
        return ALL[Math.max(0, Math.min(ALL.length - 1, id))];
    }

    public static Theme current(Context c) {
        return of(new Prefs(c).theme());
    }

    /** يضع قيمة شفافية على لون. */
    public static int a(int color, int alpha) {
        return (alpha << 24) | (color & 0xFFFFFF);
    }
}
