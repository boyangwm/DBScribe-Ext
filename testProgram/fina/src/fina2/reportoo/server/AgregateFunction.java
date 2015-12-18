package fina2.reportoo.server;

public class AgregateFunction {
    public final static String F_AVG = "average";
    public final static String F_SUM = "sum";
    public final static String F_MAX = "max";
    public final static String F_MIN = "min";
    public final static String F_COUNT = "count";

    public final static String SQL_AVG = "avg";

    public final static int UNK = 0;
    public final static int AVG = 1;
    public final static int SUM = 2;
    public final static int MAX = 3;
    public final static int MIN = 4;
    public final static int COUNT = 5;

    private final static String[] SQL_FUNC = { "unknown", SQL_AVG, F_SUM,
            F_MAX, F_MIN, F_COUNT };

    private final static String[] FINA_FUNC = { "unknown", F_AVG, F_SUM, F_MAX,
            F_MIN, F_COUNT };

    public static String getSqlFunction(int funcCode) {
        String retVal = "";
        try {
            retVal = SQL_FUNC[funcCode];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalFinaFunctionArgumentException(e);
        }
        return retVal;
    }

    public static String getFinaFunction(int funcCode) {
        String retVal = "";
        try {
            retVal = FINA_FUNC[funcCode];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalFinaFunctionArgumentException(e);
        }
        return retVal;
    }

    public static String getSqlFunction(String finaFunc) {
        return SQL_FUNC[getCode(finaFunc)];
    }

    public static int getCode(String function) {
        if (function.equalsIgnoreCase(F_AVG)) {
            return AVG;
        } else if (function.equalsIgnoreCase(F_SUM)) {
            return SUM;
        } else if (function.equalsIgnoreCase(F_MIN)) {
            return MIN;
        } else if (function.equalsIgnoreCase(F_MAX)) {
            return MAX;
        } else if (function.equalsIgnoreCase(F_COUNT)) {
            return COUNT;
        } else {
            return UNK;
        }
    }

}

class IllegalFinaFunctionArgumentException extends RuntimeException {

    IllegalFinaFunctionArgumentException(Throwable cause) {
        super(cause);
    }

    IllegalFinaFunctionArgumentException() {
        super();
    }
}
