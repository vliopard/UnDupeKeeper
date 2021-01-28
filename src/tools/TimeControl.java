package tools;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

/**
 * TimeControl class is responsible to manage time calculations for
 * benchmarking.
 * 
 * @author vliopard
 */
public class TimeControl
{
    /**
     * This method returns the current time in milliseconds.
     * 
     * @return Returns a <code>long</code> value that represents the time in
     *         milliseconds.
     */
    public static long getTime()
    {
        return System.currentTimeMillis();
    }

    /**
     * This method returns the current time in nanoseconds.
     * 
     * @return Returns a <code>long</code> value that represents the time in
     *         nanoseconds.
     */
    public static long getNano()
    {
        return System.nanoTime();
    }

    /**
     * This method returns the elapsed time between a start and end point.
     * 
     * @param start
     *            A <code>long</code> value containing the start time to
     *            calculate delta from end.
     * @return Returns a <code>long</code> value that represents the interval of
     *         time in milliseconds.
     */
    public static long getElapsedTime(long start)
    {
        return getTime() - start;
    }

    /**
     * This method returns the elapsed time between a start and end point.
     * 
     * @param start
     *            A <code>long</code> value containing the start time to
     *            calculate delta from end.
     * @return Returns a <code>long</code> value that represents the interval of
     *         time in nanoseconds.
     */
    public static long getElapsedNano(long start)
    {
        return getNano() - start;
    }

    /**
     * This method returns the time represented in microseconds.
     * 
     * @param time
     *            A <code>long</code> value containing the nanoseconds to be
     *            converted.
     * @return Returns a <code>long</code> value that represents the time in to
     *         microseconds.
     */
    public static long getMic(long time)
    {
        return TimeUnit.NANOSECONDS.toMicros(time);
    }

    /**
     * This method returns the time represented in milliseconds.
     * 
     * @param time
     *            A <code>long</code> value containing the nanoseconds to be
     *            converted.
     * @return Returns a <code>long</code> value that represents the time in to
     *         milliseconds.
     */
    public static long getMil(long time)
    {
        return TimeUnit.NANOSECONDS.toMillis(time);
    }

    /**
     * This method returns the time represented in seconds.
     * 
     * @param time
     *            A <code>long</code> value containing the nanoseconds to be
     *            converted.
     * @return Returns a <code>long</code> value that represents the time in to
     *         seconds.
     */
    public static long getSec(long time)
    {
        return TimeUnit.NANOSECONDS.toSeconds(time);
    }

    /**
     * This method returns the time represented in minutes.
     * 
     * @param time
     *            A <code>long</code> value containing the nanoseconds to be
     *            converted.
     * @return Returns a <code>long</code> value that represents the time in to
     *         minutes.
     */
    public static long getMin(long time)
    {
        return TimeUnit.NANOSECONDS.toMinutes(time);
    }

    /**
     * This method returns the time represented in hours.
     * 
     * @param time
     *            A <code>long</code> value containing the nanoseconds to be
     *            converted.
     * @return Returns a <code>long</code> value that represents the time in to
     *         hours.
     */
    public static long getHour(long time)
    {
        return TimeUnit.NANOSECONDS.toHours(time);
    }

    /**
     * This method returns the time represented in days.
     * 
     * @param time
     *            A <code>long</code> value containing the nanoseconds to be
     *            converted.
     * @return Returns a <code>long</code> value that represents the time in to
     *         days.
     */
    public static long getDay(long time)
    {
        return TimeUnit.NANOSECONDS.toDays(time);
    }

    /**
     * This method returns the time formated in Hour:Minutes:Seconds.
     * 
     * @param time
     *            A <code>long</code> value containing the nanoseconds to be
     *            converted.
     * @return Returns an <code>String</code> value that represents the
     *         formatted time in Hour:Minutes:Seconds.
     */
    public static String getHMS(long time)
    {
        return getHour(time) + ":" + getMin(time) + ":" + getSec(time);
    }

    /**
     * This method returns the time formated in
     * Hour:Minutes:Seconds:Milliseconds:Microseconds.
     * 
     * @param time
     *            A <code>long</code> value containing the nanoseconds to be
     *            converted.
     * @return Returns an <code>String</code> value that represents the
     *         formatted time in Hour:Minutes:Seconds:Milliseconds:Microseconds.
     */
    public static String getTotal(long time)
    {
        Timestamp ts=new Timestamp(TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS));
        SimpleDateFormat format=new SimpleDateFormat("°:mm'\"':ss'':SSS:");
        long hr = getHour(time);
        long mic = getMic(time);
        long roundTime = mic / 1000;
        return Utils.addCustomLeadingZeros("02", hr) + format.format(ts) +
               Utils.addCustomLeadingZeros("03", (mic - (roundTime * 1000)));
    }
}
