package name.abuchen.portfolio.util;

import java.util.Calendar;
import java.util.Date;

public class Dates
{

    public static Date today()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    public static Calendar cal(int year, int month, int day)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }

    public static Date date(int year, int month, int day)
    {
        return cal(year, month, day).getTime();
    }

    public static int daysBetween(Date start, Date end)
    {
        int answer = 0;

        if (start.after(end))
        {
            Date temp = start;
            start = end;
            end = temp;
        }

        Calendar c1 = Calendar.getInstance();
        c1.setTime(start);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(end);

        while (c1.get(Calendar.YEAR) < c2.get(Calendar.YEAR))
        {
            int dayOfYear = c1.get(Calendar.DAY_OF_YEAR);
            int daysInYear = c1.getActualMaximum(Calendar.DAY_OF_YEAR);

            answer += daysInYear - dayOfYear + 1;
            c1.set(Calendar.DATE, 1);
            c1.set(Calendar.MONDAY, Calendar.JANUARY);
            c1.add(Calendar.YEAR, 1);
        }

        answer += c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR);

        return answer;
    }

}
