package helper;

import model.MethodObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

public class LogcatProcessor {
    public static final String INSTRUAPK = "InstruAPK";
    public static final void processLogcat(String packageName, String logcatPath, HashMap<Integer, MethodObject> instrumentedMethods){
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(logcatPath));
            String line = bufferedReader.readLine();
            while(line != null){
                if(line.contains(packageName)){
                    //TODO find the errors ???
                    System.out.println(line);
                }else if(line.contains(INSTRUAPK)){
                    line = line.split(INSTRUAPK +": ")[1];
                    String[] values = line.split(";;");
                    int methodId = Integer.parseInt(values[1]);
                    MethodObject currentMethod = instrumentedMethods.get(methodId);
                    if(currentMethod.getCalledTimes() == 0){
                        String className = values[2];
                        String methodName = values[3];
                        String parameters = values[4];
                        String callTime = values[5];
                        currentMethod.setFileName(className);
                        currentMethod.setMethodName(methodName);
                        currentMethod.setMethodParameters(parameters);
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
