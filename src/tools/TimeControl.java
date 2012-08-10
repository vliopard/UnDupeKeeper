package tools;
import java.util.concurrent.TimeUnit;

// TODO: 00 JAVADOC
public class TimeControl
{
    public static long getTime()
    {
        return System.currentTimeMillis();
    }

    public static long getNano()
    {
        return System.nanoTime();
    }

    public static long getElapsedTime(long start)
    {
        return getNano()-
               start;
    }

    public static long getElapsedNano(long start)
    {
        return getNano()-
               start;
    }

    public static long getMic(long time)
    {
        return TimeUnit.NANOSECONDS.toMicros(time);
    }

    public static long getMil(long time)
    {
        return TimeUnit.NANOSECONDS.toMillis(time);
    }

    public static long getSec(long time)
    {
        return TimeUnit.NANOSECONDS.toSeconds(time);
    }

    public static long getMin(long time)
    {
        return TimeUnit.NANOSECONDS.toMinutes(time);
    }

    public static long getHour(long time)
    {
        return TimeUnit.NANOSECONDS.toHours(time);
    }

    public static long getDay(long time)
    {
        return TimeUnit.NANOSECONDS.toDays(time);
    }

    public static String getHMS(long time)
    {
        return getHour(time)+
               ":"+
               getMin(time)+
               ":"+
               getSec(time);
    }

    public static String getTotal(long time)
    {
        return Utils.addCustomLeadingZeros("02",
                                           getHour(time))+
               ":"+
               Utils.addCustomLeadingZeros("02",
                                           getMin(time))+
               ":"+
               Utils.addCustomLeadingZeros("02",
                                           getSec(time))+
               ":"+
               Utils.customFormat("###,###",
                                  getMic(time));
    }
}
