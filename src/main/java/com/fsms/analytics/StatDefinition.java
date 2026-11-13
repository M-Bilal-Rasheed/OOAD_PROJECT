package com.fsms.analytics;

import com.fsms.model.PlayerStatistics;

import java.util.Locale;

// ============================================================
// StatDefinition describes ONE of the four generic statistics
// (stat1..stat4) for a specific sport.
//
// It knows:
//   - which slot it reads (1..4)
//   - its human label
//   - the storage scale (×10 statistics are stored as integers,
//     e.g. economy 7.4 is stored as 74)
//   - whether a higher or lower value is better
//   - whether a raw 0 means "no data" (e.g. Tennis ranking 0 = unranked,
//     Cricket economy 0 = did not bowl) and what to show instead
//
// Encapsulation: immutable — all fields are final and private.
// ============================================================
public final class StatDefinition {

    private final int slot;
    private final String label;
    private final String shortLabel;
    private final int scale;
    private final boolean higherIsBetter;
    private final boolean zeroMeansMissing;
    private final String unit;
    private final String missingLabel;

    public StatDefinition(int slot, String label, String shortLabel, int scale,
                          boolean higherIsBetter, boolean zeroMeansMissing, String unit) {
        this(slot, label, shortLabel, scale, higherIsBetter, zeroMeansMissing, unit, "No data");
    }

    public StatDefinition(int slot, String label, String shortLabel, int scale,
                          boolean higherIsBetter, boolean zeroMeansMissing, String unit, String missingLabel) {
        if (slot < 1 || slot > 4) {
            throw new IllegalArgumentException("Stat slot must be between 1 and 4");
        }
        if (scale != 1 && scale != 10) {
            throw new IllegalArgumentException("Scale must be 1 or 10");
        }
        this.slot = slot;
        this.label = label;
        this.shortLabel = shortLabel;
        this.scale = scale;
        this.higherIsBetter = higherIsBetter;
        this.zeroMeansMissing = zeroMeansMissing;
        this.unit = unit == null ? "" : unit;
        this.missingLabel = missingLabel == null ? "No data" : missingLabel;
    }

    // Reads the raw integer stored in the database for this slot
    public int rawValue(PlayerStatistics stats) {
        if (stats == null) return 0;
        switch (slot) {
            case 1: return stats.getStat1();
            case 2: return stats.getStat2();
            case 3: return stats.getStat3();
            default: return stats.getStat4();
        }
    }

    // Converts the stored integer into its real value (×10 stats are divided by 10)
    public double displayValue(int raw) {
        return scale == 10 ? raw / 10.0 : raw;
    }

    // True when this raw value carries no information (e.g. ranking 0 = unranked)
    public boolean isMissing(int raw) {
        return zeroMeansMissing && raw == 0;
    }

    // Human-readable value, e.g. "7.4", "42", "62.5%", or the missing label
    public String format(int raw) {
        if (isMissing(raw)) return missingLabel;
        return formatNumber(displayValue(raw)) + unit;
    }

    // Formats an already-decoded number (used for averages)
    public String formatDecoded(double value) {
        return formatNumber(value) + unit;
    }

    private String formatNumber(double value) {
        if (scale == 10) {
            return String.format(Locale.UK, "%.1f", value);
        }
        if (value == Math.rint(value)) {
            return String.format(Locale.UK, "%,d", (long) value);
        }
        return String.format(Locale.UK, "%,.1f", value);
    }

    // Returns > 0 if a is better than b, < 0 if worse, 0 if equal.
    // Missing values always lose against real values.
    public int compareRaw(int a, int b) {
        boolean aMissing = isMissing(a);
        boolean bMissing = isMissing(b);
        if (aMissing && bMissing) return 0;
        if (aMissing) return -1;
        if (bMissing) return 1;
        int cmp = Integer.compare(a, b);
        return higherIsBetter ? cmp : -cmp;
    }

    public int getSlot() { return slot; }
    public String getLabel() { return label; }
    public String getShortLabel() { return shortLabel; }
    public int getScale() { return scale; }
    public boolean isScaled() { return scale == 10; }
    public boolean isHigherIsBetter() { return higherIsBetter; }
    public boolean isZeroMeansMissing() { return zeroMeansMissing; }
    public String getUnit() { return unit; }
    public String getMissingLabel() { return missingLabel; }
    public String getDirectionLabel() { return higherIsBetter ? "Higher is better" : "Lower is better"; }
}
