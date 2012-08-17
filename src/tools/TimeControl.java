package tools;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

// TODO: JAVADOC
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
        Timestamp ts=new Timestamp(TimeUnit.MILLISECONDS.convert(time,
                                                                 TimeUnit.NANOSECONDS));
        SimpleDateFormat format=new SimpleDateFormat("°:mm'\"':ss'':SSS:");
        long hr=getHour(time);
        long mic=getMic(time);
        long roundTime=mic/1000;
        return Utils.addCustomLeadingZeros("02",
                                           hr)+
               format.format(ts)+
               (mic-(roundTime*1000));
        /*
         * return Utils.addCustomLeadingZeros("02",
         * getHour(time))+
         * "°:"+
         * Utils.addCustomLeadingZeros("02",
         * getMin(time))+
         * "\":"+
         * Utils.addCustomLeadingZeros("02",
         * getMin(time)-getSec(time))+
         * "':"+
         * Utils.addCustomLeadingZeros("03",
         * getMil(time)-
         * (getSec(time)*1000))+
         * ":"+
         * Utils.addCustomLeadingZeros("03",
         * getMic(time)-
         * (getMil(time)*1000));
         */
    }
}
