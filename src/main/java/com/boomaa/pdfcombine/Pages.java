package com.boomaa.pdfcombine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pages extends ArrayList<Pages.Range> {
    private final int endPage;

    public Pages(int endPage) {
        this.endPage = endPage;
        super.add(new Range(1, endPage));
    }

    public void resetPages() {
        super.clear();
    }

    public void addRange(Range range) {
        super.add(range);
    }

    public boolean[] remPages() {
        boolean[] remPages = new boolean[endPage + 1];
        Arrays.fill(remPages, true);
        for (int i = 0; i < super.size(); i++) {
            Range range = super.get(i);
            for (int j = range.min; j <= range.max; j++) {
                remPages[j] = false;
            }
        }
        return remPages;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < super.size(); i++) {
            out.append(super.get(i).toString());
            if (i != super.size() - 1) {
                out.append(", ");
            }
        }
        return out.toString();
    }

    public static class Range {
        protected final int min;
        protected final int max;
        protected final boolean singleValue;

        public Range(int value) {
            this.min = value;
            this.max = value;
            this.singleValue = true;
        }

        public Range(int min, int max) {
            this.min = min;
            this.max = max;
            this.singleValue = false;
        }

        @Override
        public String toString() {
            if (singleValue) {
                return String.valueOf(min);
            } else {
                return min + "-" + max;
            }
        }
    }
}
