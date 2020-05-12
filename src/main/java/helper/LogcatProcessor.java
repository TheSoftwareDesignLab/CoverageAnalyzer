package helper;

import model.MethodObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;

public class LogcatProcessor {
    public static final String INSTRUAPK = "InstruAPK";
    public static final String ANDROID_RUNTIME = "E AndroidRuntime:";
    public static final String SYSTEM_ERR = "System.err:";
    public static final String CAUSED_BY = "Caused by:";
    public static final void processLogcat(String packageName, String logcatPath, HashMap<Integer, MethodObject> instrumentedMethods, List<String> stackTraceError, List<String> stackRuntimeError){
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(logcatPath));
            String line = bufferedReader.readLine();
            String error = "";
            boolean causedByFound = false;
            while(line != null){
                System.out.println("Logcat line: " +line + " line contains sys: "+ line.contains(SYSTEM_ERR) + " line contains runtime: " + line.contains(ANDROID_RUNTIME));
                if(line.contains(SYSTEM_ERR)){
                    System.out.println("contains " + SYSTEM_ERR);
                    line = line.split(SYSTEM_ERR)[1];
                    if(error == "" || line.contains("at")){
                        error += line;
                    }else {
                        stackTraceError.add(error);
                        error = new String("");
                    }
                }else if(line.contains(ANDROID_RUNTIME)){
                    System.out.println("contains " + ANDROID_RUNTIME);
                    line = line.split(ANDROID_RUNTIME)[1];
                    if(!causedByFound){
                        if(line.contains(CAUSED_BY)){
                            causedByFound = true;
                        }
                        error += line;
                    }else{
                        if(line.contains("at")){
                            error += line;
                        }else{
                            error += line;
                            stackRuntimeError.add(error);
                            error = new String("");
                            causedByFound=false;
                        }
                    }
                }else if(line.contains(INSTRUAPK)){
                    System.out.println("contains " + INSTRUAPK);
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
                }
                line = bufferedReader.readLine();
            }
        }catch (Exception e){
            System.out.println("There was a problem reading the exploration report (logcat)");
            e.printStackTrace();
        }

    }

}
