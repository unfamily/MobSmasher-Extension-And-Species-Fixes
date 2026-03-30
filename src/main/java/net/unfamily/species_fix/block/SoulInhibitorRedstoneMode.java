package net.unfamily.species_fix.block;

/**
 * Redstone behavior (vague analogue to Iskandert utilities: NONE/LOW/HIGH/PULSE style).
 */
public enum SoulInhibitorRedstoneMode {
    /** Rising edge on powered toggles ON; falling edge clears powered tracking only. */
    PULSE,
    /** Redstone does not drive ON; only player click toggles output (powered property still follows wire for edge tracking). */
    MANUAL,
    /** ON while block receives redstone power; OFF when unpowered. */
    HIGH,
    /** ON while unpowered; OFF when powered (inverted). */
    LOW;

    private static final SoulInhibitorRedstoneMode[] VALUES = values();

    public SoulInhibitorRedstoneMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public static SoulInhibitorRedstoneMode fromOrdinal(int o) {
        if (o < 0 || o >= VALUES.length) return PULSE;
        return VALUES[o];
    }

    public String translationKey() {
        return "message.mobsmash_ext_species_fix.soul_inhibitor.mode." + name().toLowerCase();
    }
}
