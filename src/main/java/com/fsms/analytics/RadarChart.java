package com.fsms.analytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// ============================================================
// RadarChart turns 0..100 scores into SVG coordinates so the
// template can draw a radar (spider) chart with no JavaScript
// library. One axis per statistic; up to two series (A and B).
// ============================================================
public final class RadarChart {

    public static final int SIZE = 320;
    private static final double CENTER = SIZE / 2.0;
    private static final double RADIUS = 110.0;

    private final List<String> labels;
    private final List<Integer> scoresA;
    private final List<Integer> scoresB;

    public RadarChart(List<String> labels, List<Integer> scoresA, List<Integer> scoresB) {
        this.labels = labels;
        this.scoresA = scoresA;
        this.scoresB = scoresB;
    }

    private double angle(int i) {
        return -Math.PI / 2 + (2 * Math.PI * i / labels.size());
    }

    private String point(int i, double fraction) {
        double x = CENTER + Math.cos(angle(i)) * RADIUS * fraction;
        double y = CENTER + Math.sin(angle(i)) * RADIUS * fraction;
        return String.format(Locale.UK, "%.1f,%.1f", x, y);
    }

    private String polygon(List<Integer> scores) {
        if (scores == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < labels.size(); i++) {
            // Small floor so a zero score is still visible as a point near the centre
            double f = Math.max(0.04, scores.get(i) / 100.0);
            if (sb.length() > 0) sb.append(' ');
            sb.append(point(i, f));
        }
        return sb.toString();
    }

    public String getPolygonA() { return polygon(scoresA); }
    public String getPolygonB() { return polygon(scoresB); }
    public boolean isHasB() { return scoresB != null; }
    public String getViewBox() { return "0 0 " + SIZE + " " + SIZE; }

    // Concentric grid rings at 25 / 50 / 75 / 100 %
    public List<String> getGrid() {
        List<String> rings = new ArrayList<>();
        for (int ring = 1; ring <= 4; ring++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < labels.size(); i++) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(point(i, ring / 4.0));
            }
            rings.add(sb.toString());
        }
        return rings;
    }

    public List<Axis> getAxes() {
        List<Axis> axes = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            double cos = Math.cos(angle(i));
            double sin = Math.sin(angle(i));
            String anchor = Math.abs(cos) < 0.2 ? "middle" : (cos > 0 ? "start" : "end");
            axes.add(new Axis(
                    fmt(CENTER + cos * RADIUS), fmt(CENTER + sin * RADIUS),
                    fmt(CENTER + cos * (RADIUS + 14)), fmt(CENTER + sin * (RADIUS + 14) + (sin > 0.2 ? 10 : (sin < -0.2 ? -4 : 4))),
                    anchor, labels.get(i)));
        }
        return axes;
    }

    private static String fmt(double v) {
        return String.format(Locale.UK, "%.1f", v);
    }

    // One spoke of the chart with its label position
    public static final class Axis {
        private final String x, y, labelX, labelY, anchor, label;

        Axis(String x, String y, String labelX, String labelY, String anchor, String label) {
            this.x = x; this.y = y; this.labelX = labelX; this.labelY = labelY;
            this.anchor = anchor; this.label = label;
        }

        public String getX() { return x; }
        public String getY() { return y; }
        public String getLabelX() { return labelX; }
        public String getLabelY() { return labelY; }
        public String getAnchor() { return anchor; }
        public String getLabel() { return label; }
    }
}
