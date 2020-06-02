package helper;

import com.sun.jmx.snmp.SnmpStatusException;
import model.MethodObject;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LogcatProcessor {
    public static final String INSTRUAPK = "InstruAPK";
    public static final String INSTRUAPK_SPLIT = "InstruAPK(\\(\\d*\\))*:";
    public static final String ANDROID_RUNTIME = "AndroidRuntime";
    public static final String ANDROID_RUNTIME_SPLIT = "AndroidRuntime(\\(\\d*\\))*:";
    public static final String SYSTEM_ERR = "System.err";
    public static final String SYSTEM_ERR_SPLIT = "System\\.err(\\(\\d*\\))*:";
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
//                System.out.println("LINE: " + line);
//                System.out.println("ANDROID: " + line.contains(ANDROID_RUNTIME) + " : " + ANDROID_RUNTIME);
//                System.out.println("ERROR: " + line.contains(SYSTEM_ERR) + " : " + SYSTEM_ERR);
                if(line.contains(SYSTEM_ERR)){
                    if(error == "" || line.contains("at")){
                        lastLine = line;
                        line = line.split(SYSTEM_ERR_SPLIT)[1];
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
                    //System.out.println("android runtime line: " + line);
                    line = line.split(ANDROID_RUNTIME_SPLIT)[1];
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
                    //System.out.println("android runtime full error: " + error);
                    error = "";
                    causedByFound = false;
                } else if(line.contains(INSTRUAPK)){
                    lastLine = line;
                    String[] instruSplit =line.split(INSTRUAPK_SPLIT);
                   // System.out.println("instru split: " + Arrays.toString(instruSplit));
                    line = line.split(INSTRUAPK_SPLIT)[1];
                    //System.out.println("line: " +line);
                    String[] values = line.split(";;");
                    //System.out.println("values: " + Arrays.toString(values));
                    int methodId = Integer.parseInt(values[1]);
                    MethodObject currentMethod = instrumentedMethods.get(methodId);
                    if(currentMethod.getCalledTimes() == 0){
                        String callTime = values[values.length-1];
                        currentMethod.addCall(callTime);
                    }else{
                        currentMethod.addCall(values[values.length-1]);
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
               // System.out.println("Error in for: " + error);
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
            System.out.println("Message Error: " + e.getMessage() + "\nStack Trace: " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }

    }
}
