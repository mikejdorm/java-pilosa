package com.pilosa.client;

import com.pilosa.client.exceptions.ValidationException;

/**
 * Valid time quantum values for frames having support for that.
 *
 * @see <a href="https://www.pilosa.com/docs/data-model/">Data Model</a>
 */
public enum TimeQuantum {
    NONE(0),
    YEAR(TimeQuantum.Y),
    MONTH(TimeQuantum.M),
    DAY(TimeQuantum.D),
    HOUR(TimeQuantum.H),
    YEAR_MONTH(TimeQuantum.Y | TimeQuantum.M),
    MONTH_DAY(TimeQuantum.M | TimeQuantum.D),
    DAY_HOUR(TimeQuantum.D | TimeQuantum.H),
    YEAR_MONTH_DAY(TimeQuantum.Y | TimeQuantum.M | TimeQuantum.D),
    MONTH_DAY_HOUR(TimeQuantum.M | TimeQuantum.D | TimeQuantum.H),
    YEAR_MONTH_DAY_HOUR(TimeQuantum.Y | TimeQuantum.M | TimeQuantum.D | TimeQuantum.H);

    /**
     * Converts a string to the corresponding TimeQuantum.
     *
     * @param s the string to be converted
     * @return a TimeQuantum object
     */
    public static TimeQuantum fromString(String s) {
        switch (s) {
            case "":
                return TimeQuantum.NONE;
            case "Y":
                return TimeQuantum.YEAR;
            case "M":
                return TimeQuantum.MONTH;
            case "D":
                return TimeQuantum.DAY;
            case "H":
                return TimeQuantum.HOUR;
            case "YM":
                return TimeQuantum.YEAR_MONTH;
            case "MD":
                return TimeQuantum.MONTH_DAY;
            case "DH":
                return TimeQuantum.DAY_HOUR;
            case "YMD":
                return TimeQuantum.YEAR_MONTH_DAY;
            case "MDH":
                return TimeQuantum.MONTH_DAY_HOUR;
            case "YMDH":
                return TimeQuantum.YEAR_MONTH_DAY_HOUR;
        }
        throw new ValidationException(String.format("Invalid time quantum string: %s", s));
    }

    /**
     * Converts a TimeQuantum object to the corresponding string
     *
     * @return string representation of a TimeQuantum object
     */
    public String getStringValue() {
        StringBuilder sb = new StringBuilder(4);
        if ((this.value & Y) == Y) sb.append('Y');
        if ((this.value & M) == M) sb.append('M');
        if ((this.value & D) == D) sb.append('D');
        if ((this.value & H) == H) sb.append('H');
        return sb.toString();
    }

    TimeQuantum(int value) {
        this.value = value;
    }

    private final int value;
    private static final byte Y = 0b00000001;
    private static final byte M = 0b00000010;
    private static final byte D = 0b00000100;
    private static final byte H = 0b00001000;
}
