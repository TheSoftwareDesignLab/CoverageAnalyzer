package model;

import java.util.ArrayList;
import java.util.List;

public class MethodObject {
    private int methodIndex = -1;
    private String fileName = "";
    private String methodName = "";
    private String methodParameters = "";
    private Long firstCall;
    private Long lastCall;
    private List<Long> callsHistory;
    private int calledTimes = 0;

    public MethodObject (String line){
        callsHistory = new ArrayList<>();
        String mutantIndex = line.split("\\s+")[1];
        methodIndex = Integer.parseInt(mutantIndex);
    }

    public void setMethodIndex(int methodIndex) {
        this.methodIndex = methodIndex;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setMethodParameters(String methodParameters) {
        this.methodParameters = methodParameters;
    }

    public void setFirstCall(Long firstCall) {
        this.firstCall = firstCall;
    }

    public void setLastCall(Long lastCall) {
        this.lastCall = lastCall;
    }

    public void setCallsHistory(List<Long> callsHistory) {
        this.callsHistory = callsHistory;
    }

    public void setCalledTimes(int calledTimes) {
        this.calledTimes = calledTimes;
    }

    public int getMethodIndex() {
        return methodIndex;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodParameters() {
        return methodParameters;
    }

    public Long getFirstCall() {
        return firstCall;
    }

    public Long getLastCall() {
        return lastCall;
    }

    public List<Long> getCallsHistory() {
        return callsHistory;
    }

    public int getCalledTimes() {
        return calledTimes;
    }

    public void addCall(String stringMillis){
        Long millis = Long.parseLong(stringMillis);
        callsHistory.add(millis);
        if(firstCall == null){
            firstCall = millis;
        }
        lastCall = millis;
        calledTimes = callsHistory.size();
    }

}
