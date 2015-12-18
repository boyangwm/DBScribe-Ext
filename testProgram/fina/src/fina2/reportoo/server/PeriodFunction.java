package fina2.reportoo.server;

public class PeriodFunction extends AgregateFunction {

    public final static String F_LAST = "last";
    public final static String F_YAVERAGE = "yaverage";
    public final static String F_YDTAVERAGE = "ytdaverage";

    public final static int LAST = 6;
    public final static int YAVERAGE = 7;
    public final static int YDTAVERAGE = 8;

    public static String getFinaFunction(int funcCode) {
        switch (funcCode) {
        case LAST:
            return F_LAST;
        case YAVERAGE:
            return F_YAVERAGE;
        case YDTAVERAGE:
            return F_YDTAVERAGE;
        default:
            return AgregateFunction.getSqlFunction(funcCode);
        }
    }

    public static int getCode(String function) {
        if (function.equalsIgnoreCase(F_LAST)) {
            return LAST;
        }
        if (function.equalsIgnoreCase(F_YAVERAGE)) {
            return YAVERAGE;
        }
        if (function.startsWith(F_YDTAVERAGE)) {
            return YDTAVERAGE;
        }
        return AgregateFunction.getCode(function);
    }
}
