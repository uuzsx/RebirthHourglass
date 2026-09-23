package com.xiaohunao.rebirthhourglass.logic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class ChargeRulesTest {
    @ParameterizedTest
    @ValueSource(ints = {1, 299, 300, 360, 599, 600, 604800})
    void everyConfiguredCostDecaysToZeroAfterFiveMinutes(int base) {
        assertEquals(base, ChargeRules.teleportCost(base, 0, 300));
        assertEquals((base + 1) / 2, ChargeRules.teleportCost(base, 3000, 300));
        assertEquals(0, ChargeRules.teleportCost(base, 6000, 300));
        assertEquals(0, ChargeRules.teleportCost(base, Long.MAX_VALUE, 300));
    }

    @Test void exactBalanceAndZeroCostAreAllowed() {
        assertEquals(0, ChargeRules.consume(360, 360));
        assertEquals(0, ChargeRules.consume(0, 0));
        assertEquals(123, ChargeRules.consume(123, 0));
    }

    @Test void invalidSpendingCannotCreateCharge() {
        assertThrows(IllegalArgumentException.class, () -> ChargeRules.consume(359, 360));
        assertThrows(IllegalArgumentException.class, () -> ChargeRules.consume(360, -1));
    }

    @Test void fixedCostAndClockRollbackArePredictable() {
        assertEquals(360, ChargeRules.teleportCost(360, Long.MAX_VALUE, 0));
        assertEquals(360, ChargeRules.teleportCost(360, -100, 300));
        assertEquals(0, ChargeRules.teleportCost(0, 0, 300));
    }

    @Test void chargeSaturatesWithoutOverflow() {
        assertEquals(1440, ChargeRules.charge(1440, 1440));
        assertEquals(1440, ChargeRules.charge(Integer.MAX_VALUE, 1440));
        assertEquals(Integer.MAX_VALUE, ChargeRules.charge(Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(1, ChargeRules.charge(-200, 1440));
        assertEquals(0, ChargeRules.charge(1, 0));
    }
}
