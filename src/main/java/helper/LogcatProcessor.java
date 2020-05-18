package helper;

import com.sun.jmx.snmp.SnmpStatusException;
import model.MethodObject;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogcatProcessor {
    public static final String INSTRUAPK = "InstruAPK";
    public static final String ANDROID_RUNTIME = "E AndroidRuntime:";
    public static final String SYSTEM_ERR = "System.err:";
    public static final String CAUSED_BY = "Caused by:";
    public static final void processLogcat(String packageName, String logcatPath, HashMap<Integer, MethodObject> instrumentedMethods, List<String> allCrashes, Map<String,Integer> stackTraceError, Map<String,Integer> stackRuntimeError){
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(logcatPath));
            String line = bufferedReader.readLine();
            String error = "";
            ArrayList<String> auxError = new ArrayList<>();
            ArrayList<String> auxRuntime = new ArrayList<>();
            boolean causedByFound = false;
            String lastLine = "";
            while(line != null){
                if(line.contains(SYSTEM_ERR)){
                    if(error == "" || line.contains("at")){
                        lastLine = line;
                        line = line.split(SYSTEM_ERR)[1];
                        error = error + " " + line.trim();
                        line = bufferedReader.readLine();
                    }else {
                        auxError.add(error);
                        error = "";
                    }
                }else if(error != "" && lastLine.contains(SYSTEM_ERR)){
                        auxError.add(error);
                        error = "";
                }else if(line.contains(ANDROID_RUNTIME)){
                    lastLine = line;
                    line = line.split(ANDROID_RUNTIME)[1];
                    if(!causedByFound){
                        if(line.contains(CAUSED_BY)){
                            causedByFound = true;
                        }
                        error = error + " " + line.trim();
                    }else{
                        if(line.contains("at")){
                            error = error + " " + line.trim();
                        }else{
                            error = error + " " + line.trim();
                            auxRuntime.add(error);
                            error = "";
                            causedByFound=false;
                        }
                    }
                    line = bufferedReader.readLine();
                }else if(error != "" && lastLine.contains(ANDROID_RUNTIME)){
                    auxRuntime.add(error);
                    error = "";
                    causedByFound = false;
                } else if(line.contains(INSTRUAPK)){
                    lastLine = line;
                    line = line.split(INSTRUAPK +": ")[1];
                    String[] values = line.split(";;");
                    int methodId = Integer.parseInt(values[1]);
                    MethodObject currentMethod = instrumentedMethods.get(methodId);
                    if(currentMethod.getCalledTimes() == 0){
                        String callTime = values[5];
                        currentMethod.addCall(callTime);
                    }else{
                        currentMethod.addCall(values[5]);
                    }
                    line = bufferedReader.readLine();
                }else{
                    line = bufferedReader.readLine();
                }
            }
            for(int i = 0; i < auxError.size(); i++){
                error = auxError.get(i);
                if(error.contains(packageName) && stackTraceError.get(error) == null){
                    stackTraceError.put(error,1);
                }else if(stackTraceError.get(error) != null){
                    stackTraceError.replace(error,stackTraceError.get(error) +1);
                }
            }
            for(int i =0; i < auxRuntime.size(); i++){
                error = auxRuntime.get(i);
                if(error.contains(packageName) && stackRuntimeError.get(error) == null){
                    stackRuntimeError.put(error,1);
                }else if(stackRuntimeError.get(error) != null){
                    stackRuntimeError.replace(error,stackRuntimeError.get(error)+1);
                }
            }
            allCrashes.addAll(stackTraceError.keySet());
            allCrashes.addAll(stackRuntimeError.keySet());
        }catch (Exception e){
            System.out.println("There was a problem reading the exploration report (logcat)");
            e.printStackTrace();
        }

    }
}
