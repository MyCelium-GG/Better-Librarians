package my.celium.org.betterlibrarians;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import my.celium.org.betterlibrarians.restock.RestockRules;
import org.junit.jupiter.api.Test;

/** The restock rules against vanilla's numbers (work 2000 to 9000, twice a day, 2400 ticks apart). */
class RestockRulesTest {
    @Test
    void workHours() {
        assertFalse(RestockRules.isWorkTime(1999));
        assertTrue(RestockRules.isWorkTime(2000));
        assertTrue(RestockRules.isWorkTime(8999));
        assertFalse(RestockRules.isWorkTime(9000));
        assertTrue(RestockRules.isWorkTime(24000 * 5 + 3000));
        assertEquals(0, RestockRules.untilWork(5000));
        assertEquals(1500, RestockRules.untilWork(500));
        assertEquals(24000 - 13000 + 2000, RestockRules.untilWork(13000));
    }

    @Test
    void firstRestockOfTheDayWaitsOnlyForWork() {
        assertEquals(0, RestockRules.untilAllowed(5000, 0, 100, false));
        assertEquals(1000, RestockRules.untilAllowed(1000, 0, 100, false));
    }

    @Test
    void secondRestockNeedsTwoMinutes() {
        // Restocked 400 ticks ago: 2001 more ticks until "more than 2400 ticks ago".
        assertEquals(2001, RestockRules.untilAllowed(5000, 1, 400, false));
        // Cooldown over at 8500, inside work hours.
        assertEquals(0, RestockRules.untilAllowed(8000, 1, 2500, false));
        // Cooldown ends after work: wait for tomorrow's shift.
        long wait = RestockRules.untilAllowed(8000, 1, 1000, false);
        assertEquals(24000 - 8000 + 2000, wait);
    }

    @Test
    void afterTwoRestocksWaitForTheNextDay() {
        // Twice today, last one 1000 ticks ago at 7000: the day count goes up at clock 24000, then work at 26000.
        long wait = RestockRules.untilAllowed(8000, 2, 1000, false);
        assertEquals(24000 - 8000 + 2000, wait);
        // A pending new day resets the count: only work hours matter.
        assertEquals(0, RestockRules.untilAllowed(3000, 2, 13000, true));
    }
}
