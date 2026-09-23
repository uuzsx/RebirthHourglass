package com.xiaohunao.rebirthhourglass.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExperienceMathTest {
    @Test void vanillaLevelBoundaries() {
        assertEquals(0, ExperienceMath.pointsAtLevel(0));
        assertEquals(352, ExperienceMath.pointsAtLevel(16));
        assertEquals(394, ExperienceMath.pointsAtLevel(17));
        assertEquals(1395, ExperienceMath.pointsAtLevel(30));
        assertEquals(1507, ExperienceMath.pointsAtLevel(31));
        assertEquals(1628, ExperienceMath.pointsAtLevel(32));
    }

    @Test void enchantingDoesNotRefundAlreadySpentLevels() {
        assertEquals(1089, ExperienceMath.currentPoints(27, 0, 97));
        assertEquals(544, ExperienceMath.retained(1089, 0.5));
    }

    @Test void floatProgressRoundTripsIndividualExperiencePoints() {
        for (int level = 0; level < 100; level++) {
            int next = level >= 30 ? 112 + (level - 30) * 9
                    : level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
            for (int points = 0; points < next; points++) {
                assertEquals(ExperienceMath.pointsAtLevel(level) + points,
                        ExperienceMath.currentPoints(level, (float) points / next, next));
            }
        }
    }

    @Test void malformedOrHugeValuesCannotOverflow() {
        assertEquals(Integer.MAX_VALUE, ExperienceMath.currentPoints(Integer.MAX_VALUE, 1, Integer.MAX_VALUE));
        assertEquals(0, ExperienceMath.currentPoints(-1, Float.NaN, 7));
        assertEquals(0, ExperienceMath.retained(100, Double.NaN));
        assertEquals(100, ExperienceMath.retained(100, 5));
    }
}
