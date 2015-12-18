package fina2.reportoo;

import java.io.Serializable;

public class FinaFunction implements Serializable {

    private String functionName;
    private Object[] arguments;
    private Object result;

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String toString() {

        StringBuffer buff = new StringBuffer();

        buff.append("Function name: ");
        buff.append(this.functionName);
        buff.append(", arguments: ");

        buff.append("[");
        for (Object arg : this.arguments) {
            if (arg != null && arg.getClass().isArray()) {
                buff.append("[");
                for (Object innArg : (Object[]) arg) {
                    buff.append(innArg).append(", ");
                }
                if (((Object[]) arg).length > 0) {
                    buff.setLength(buff.length() - 2);
                }
                buff.append("]");
            } else {
                buff.append(arg);
            }
            buff.append(", ");
        }
        if (this.arguments.length > 0) {
            buff.setLength(buff.length() - 2);
        }
        buff.append("]");

        return buff.toString();
    }
}
