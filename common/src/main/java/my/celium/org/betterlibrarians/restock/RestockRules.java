package my.celium.org.betterlibrarians.restock;

/**
 * Vanilla's restock rules (Villager.shouldRestock, allowedToRestock and WorkAtPoi in 26.1 to 26.3), as
 * plain arithmetic so client and server agree and tests can check them:
 *
 * <ul>
 *   <li>a villager restocks only while it works (time of day 2000 to 9000, 8:00 to 15:00) and stands at
 *       its workstation; it checks about every 15 to 30 seconds;</li>
 *   <li>at most twice per day, the second at least 2400 ticks (2 minutes) after the first;</li>
 *   <li>a new day starts 12000 ticks after the last restock, or when the day count goes up;</li>
 *   <li>only when at least one trade has been used.</li>
 * </ul>
 */
public final class RestockRules {
    public static final long DAY = 24000;
    public static final long WORK_START = 2000;
    public static final long WORK_END = 9000;
    public static final long COOLDOWN = 2400;
    public static final long HALF_DAY = 12000;
    public static final int MAX_PER_DAY = 2;

    private RestockRules() {
    }

    /** Whether villagers are at work at this time of day (any clock value, wrapped to one day). */
    public static boolean isWorkTime(long clock) {
        long t = Math.floorMod(clock, DAY);
        return t >= WORK_START && t < WORK_END;
    }

    /** Ticks until work time, 0 while working. */
    public static long untilWork(long clock) {
        long t = Math.floorMod(clock, DAY);
        if (t >= WORK_START && t < WORK_END) {
            return 0;
        }
        return t < WORK_START ? WORK_START - t : DAY - t + WORK_START;
    }

    /** Ticks until the day count goes up (the clock passes a multiple of {@link #DAY}). */
    public static long untilNextDay(long clock) {
        return DAY - Math.floorMod(clock, DAY);
    }

    /**
     * Ticks until the villager may restock next, if it stands at its workstation whenever it works:
     * the later of "the daily limit allows it" and "it is at work". 0 means it can restock now.
     *
     * @param clock          overworld clock time now
     * @param restocksToday  restocks since the day started
     * @param sinceRestock   ticks since the last restock
     * @param newDayPending  whether a new day already started (vanilla resets the count at its next check)
     */
    public static long untilAllowed(long clock, int restocksToday, long sinceRestock, boolean newDayPending) {
        long allowedIn;
        if (newDayPending || restocksToday <= 0) {
            allowedIn = 0;
        } else if (restocksToday < MAX_PER_DAY) {
            allowedIn = Math.max(0, COOLDOWN + 1 - sinceRestock);
        } else {
            allowedIn = Math.max(0, Math.min(HALF_DAY + 1 - sinceRestock, untilNextDay(clock)));
        }
        return allowedIn + untilWork(clock + allowedIn);
    }
}
