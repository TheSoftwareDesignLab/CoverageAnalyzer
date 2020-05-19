import helper.APKAnalyzerHelper;
import helper.LogcatProcessor;
import model.ApkInfoAnalyzer;
import model.MethodObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoverageAnalyzer {
    public static final String ORIGINAL_INFORMATION = "originalInformation";
    public static final String INSTRUMENTED_INFORMATION = "instrumentedInformation";
    public static void main(String[] args) {
        Long start = System.currentTimeMillis();
            runCoverageAnalyzer(args);
        Long end = System.currentTimeMillis();
        Long total = end - start;
        System.out.println("Total time: " + total);
    }

    public static void runCoverageAnalyzer(String[] args){
        //Usage Error
        if (args.length < 2) {
            System.out.println("******* ERROR: INCORRECT USAGE *******");
            System.out.println("Argument List:");
            System.out.println("1. Instrumentation report file (-locations.json)");
            System.out.println("2. Exploration Coverage Report (Logcat)");
            System.out.println("3. APK not instrumented path");
            System.out.println("4. APK instrumented path");
            return;
        }
        //Getting command  arguments
            //Reports
        String instrumentationReport = args[0];
        String logcat = args[1];
            //APKs
        String apkPathOriginal = args[2];
        String apkPathInstru = args[3];

        ApkInfoAnalyzer apkInfoOriginal = APKAnalyzerHelper.runApkAnalyzer(apkPathOriginal);
        ApkInfoAnalyzer apkInfoInstrumented = APKAnalyzerHelper.runApkAnalyzer(apkPathInstru);

        HashMap<Integer,MethodObject> instrumentedMethods = new HashMap<>();
        //Instrumentation report
        try{
            FileReader fileReader = new FileReader(instrumentationReport);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonReport = (JSONObject) jsonParser.parse(fileReader);
            JSONObject mutationJson = new JSONObject();
            for(int i = 1; mutationJson != null; i++ ){
                mutationJson = (JSONObject) jsonReport.get(i +"");
                if(mutationJson != null){
                    MethodObject methodObject = new MethodObject(i);
                    methodObject.setMethodName((String) mutationJson.get("methodName"));
                    methodObject.setFileName((String) mutationJson.get("fileName"));
                    methodObject.setMethodParameters((String) mutationJson.get("methodParameters"));
                    instrumentedMethods.put(i,methodObject);
                }
            }
        }catch (Exception e){
            System.out.println("There was an error reading the instrumentation report");
            e.printStackTrace();
        }
        apkInfoInstrumented.setNumberOfMethodsInstrumented(instrumentedMethods.size());
        System.out.println("Original APK Info: " + apkInfoOriginal);
        System.out.println("Instrumented APK Info: " + apkInfoInstrumented);

        System.out.println("Extracting data from exploration");
        //Read logcat to measure coverage; All data will be in the hashmap

        //List to store errors that aren't tagged with AndroidRuntime
        HashMap<String,Integer> stackTraceError = new HashMap<>();
        //List to store errors that are tagged with AndroidRuntime
        HashMap<String, Integer> stackRuntimeError = new HashMap<>();
        //List to store the unique crashes
        ArrayList<String> allCrashes = new ArrayList<>();
        LogcatProcessor.processLogcat(apkInfoInstrumented.getPackageName(),logcat,instrumentedMethods,allCrashes,stackTraceError,stackRuntimeError);
        //LogcatProcessor.processLogcat("com.example.testappjava",logcat,instrumentedMethods,allCrashes,stackTraceError,stackRuntimeError);
        System.out.println("Total crashes found with package: " +  apkInfoInstrumented.getPackageName() + " : " + allCrashes.size());
        //Calculate all metrics
        //List to know what methods were never explored (Cold methods)
        ArrayList<MethodObject> neverCalled = new ArrayList<>();

        //List to know what methods were called at least ones but not as many times as the most called ones.
        //(Warm Methods)
        //Warm methods are in fact almost all the other methods because they are neither in a extreme either other (Not 0 but either the max number)
        //Could not be relevant or maybe does not add any new information but at least we have the data.
        ArrayList<MethodObject> lessCalledList = new ArrayList<>();

        //All methods that were called at least once
        ArrayList<MethodObject> allCalled = new ArrayList<>();

        //List to know what methods were the most called ones (Hot methods)
        ArrayList<MethodObject> mostCalledList = new ArrayList<>();
        MethodObject mostCalled = new MethodObject(-999999999);

        for(int i =1; i <= instrumentedMethods.size(); i++){
            MethodObject currentMethod =instrumentedMethods.get(i);
            if(currentMethod.getCalledTimes() == 0){
                //Filling the never called list of methods
                neverCalled.add(currentMethod);
            }else if(currentMethod.getCalledTimes() > mostCalled.getCalledTimes()){
                //Search for the most called method
                mostCalled = currentMethod;
            }
            if(currentMethod.getCalledTimes() > 0){
                allCalled.add(currentMethod);
            }
        }

        //Find the list of hot and warm methods
        for (int i =1; i <= instrumentedMethods.size(); i++){
            MethodObject currentMethod =instrumentedMethods.get(i);
            if(currentMethod.getCalledTimes() > 0){
                if(currentMethod.getCalledTimes() == mostCalled.getCalledTimes()){
                    mostCalledList.add(currentMethod);
                }else{
                    lessCalledList.add(currentMethod);
                }
            }
        }

        System.out.println("Data from exploration extracted");

        System.out.println("Measuring coverage");
        //Calculate coverage percentage according the number of methods given by apkanalyzer
        double coveragePercentageAccordingAPKAnalyzer = ((double) allCalled.size() /(double) apkInfoInstrumented.getNumberOfMethodsApkAnalyzer())*100d;
        //Calculate coverage percentage according the number of instrumentedMethods
        double coveragePercentageAccordingInstruAPK = ((double)allCalled.size()/(double)apkInfoInstrumented.getNumberOfMethodsInstrumented())*100d;
        System.out.println("Coverage Measured");
        System.out.println("Building report");
        buildReport(apkInfoOriginal,apkInfoInstrumented,instrumentedMethods,
                neverCalled,lessCalledList,mostCalledList,allCalled,
                coveragePercentageAccordingAPKAnalyzer,coveragePercentageAccordingInstruAPK,allCrashes,stackTraceError,stackRuntimeError);
        System.out.println("Report built");
    }

    private static void buildReport(ApkInfoAnalyzer apkInfoOriginal, ApkInfoAnalyzer apkInfoInstrumented, HashMap<Integer,MethodObject> instrumentedMethods
            , List<MethodObject> coldMethods, List<MethodObject> warmMethods, List<MethodObject> hotMethods, List<MethodObject> allMethodsCalled,
                                    double coverageApkAnalyzer, double coverageCA, List<String> allCrashes, Map<String,Integer> stackErrors, Map<String, Integer> errorsRuntime) {
        try{
            JSONObject finalReport = new JSONObject();
            //APKs information
            finalReport.put(ORIGINAL_INFORMATION,apkInfoOriginal.parseToJSON());
            finalReport.put(INSTRUMENTED_INFORMATION, apkInfoInstrumented.parseToJSON() );
            int differenceBetweenNumberOfMethods = apkInfoInstrumented.getNumberOfMethodsApkAnalyzer() - apkInfoInstrumented.getNumberOfMethodsInstrumented();
            int sizeDifference = apkInfoInstrumented.getSize() - apkInfoOriginal.getSize();
            //store the information of difference between APKs
            finalReport.put("differenceBetweenNumberOfMethods",differenceBetweenNumberOfMethods);
            finalReport.put("sizeDifferenceBytes", sizeDifference);
            //Coverage Information
            System.out.println("Coverage based on APK Analyzer (Android Studio):" + coverageApkAnalyzer);
            System.out.println("Coverage based on Instrumentation:" + coverageCA);
            finalReport.put("coverageApkAnalyzer",coverageApkAnalyzer);
            finalReport.put("coverageInstruAPK",coverageCA);
            //store the lists of methods instrumented methods, all methods called, cold methods, warm methods, hot methods
            finalReport.put("instrumentedMethods",instrumentedMethods.values());
            finalReport.put("allMethodsCalled",allMethodsCalled);
            finalReport.put("totalCalledMethods",allMethodsCalled.size());
            finalReport.put("coldMethods",coldMethods);
            finalReport.put("totalColdMethods",coldMethods.size());
            finalReport.put("warmMethods",warmMethods);
            finalReport.put("totalWarmMethods",warmMethods.size());
            finalReport.put("hotMethods",hotMethods);
            finalReport.put("totalHotMethods",hotMethods.size());

            // Log crashes found in the exploration
            JSONArray arrayErrorTraces = new JSONArray();
            for (String trace:stackErrors.keySet()) {
                JSONObject obj = new JSONObject();
                obj.put("trace",trace);
                obj.put("times",stackErrors.get(trace));
                arrayErrorTraces.add(obj);
            }
            JSONArray arrayRuntimeErrors = new JSONArray();
            for(String trace:errorsRuntime.keySet()){
                JSONObject obj = new JSONObject();
                obj.put("trace",trace);
                obj.put("times",errorsRuntime.get(trace));
                arrayRuntimeErrors.add(obj);
            }
            finalReport.put("errorTraces",arrayErrorTraces);
            int totalErrorTraces = 0;
            for(String s: stackErrors.keySet()){
                totalErrorTraces += stackErrors.get(s);
            }
            finalReport.put("totalErrorTraces",totalErrorTraces);
            finalReport.put("runtimeErrorTraces",arrayErrorTraces);
            for(String s: errorsRuntime.keySet()){
                totalErrorTraces += errorsRuntime.get(s);
            }
            finalReport.put("totalRuntimeErrorTraces",totalErrorTraces);
            finalReport.put("uniqueTraces",allCrashes);
            finalReport.put("totalUniqueTraces",allCrashes.size());
            FileWriter fileWriter = new FileWriter(new File("coverageReport.json"));
            fileWriter.write(finalReport.toJSONString());
            fileWriter.flush();
        }catch (Exception e){
            System.out.println("Error building report");
            e.printStackTrace();
        }
    }
}
