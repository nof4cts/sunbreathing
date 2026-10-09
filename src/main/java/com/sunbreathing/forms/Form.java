package com.sunbreathing.forms;

/**
 * The twelve forms of Sun Breathing.
 *
 * Each constant carries its DEFAULT balance numbers; the live values come from the
 * server config ({@link com.sunbreathing.config.SunConfig}) so they can be tuned
 * without recompiling. {@code implemented} gates forms that have not been built yet
 * (Stage 1 ships only the First Form) so the wheel can show them as locked.
 */
public enum Form {
    //            num  key                              impl   dmg   cd(t) cost  lock(t)
    DANCE(            1, "dance",                        true,  9.0,  30,  18,   10),
    CLEAR_BLUE_SKY(   2, "clear_blue_sky",               false, 8.0,  50,  22,   14),
    RAGING_SUN(       3, "raging_sun",                   false, 7.0,  80,  30,   14),
    BURNING_BONES(    4, "burning_bones_summer_sun",     false, 4.0,  100, 35,   24),
    SETTING_SUN(      5, "setting_sun_transformation",   false, 11.0, 90,  30,   12),
    SOLAR_HEAT_HAZE(  6, "solar_heat_haze",              false, 0.0,  120, 25,   10),
    BENEFICENT_RADIANCE(7, "beneficent_radiance",        false, 12.0, 110, 35,   16),
    SUNFLOWER_THRUST( 8, "sunflower_thrust",             false, 14.0, 70,  25,   10),
    DRAGON_SUN_HALO(  9, "dragon_sun_halo_head_dance",   false, 6.0,  140, 45,   26),
    FIRE_WHEEL(      10, "fire_wheel",                   false, 10.0, 80,  30,   16),
    FAKE_RAINBOW(    11, "fake_rainbow",                 false, 3.5,  120, 40,   22),
    FLAME_DANCE(     12, "flame_dance",                  false, 5.0,  400, 80,   60);

    public static final int COUNT = values().length;
    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII"};

    public final int number;
    public final String key;
    public final boolean implemented;
    public final double defaultDamage;
    public final int defaultCooldownTicks;
    public final double defaultBreathCost;
    /** How long (ticks) the player is "busy" performing the form; blocks other casts. */
    public final int castLockTicks;

    Form(int number, String key, boolean implemented, double damage, int cooldown, double cost, int lock) {
        this.number = number;
        this.key = key;
        this.implemented = implemented;
        this.defaultDamage = damage;
        this.defaultCooldownTicks = cooldown;
        this.defaultBreathCost = cost;
        this.castLockTicks = lock;
    }

    public static Form byIndex(int index) {
        Form[] v = values();
        return v[Math.floorMod(index, v.length)];
    }

    public String langKey() {
        return "form.sunbreathing." + key;
    }

    public String roman() {
        return ROMAN[ordinal()];
    }
}
