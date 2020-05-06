import helper.APKAnalyzerHelper;
import helper.LogcatProcessor;
import model.ApkInfoAnalyzer;
import model.MethodObject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class CoverageAnalyzer {
    public static final String ORIGINAL_INFORMATION = "ORIGINAL_INFORMATION";
    public static final String MUTATED_INFORMATION = "MUTATED_INFORMATION";
    public static void main(String[] args) {
        Long start = System.currentTimeMillis();
            runCoverageAnalyzer(args);
        Long end = System.currentTimeMillis();
        Long total = end - start;
        System.out.println("Total time: " + total);
    }

    public static void runCoverageAnalyzer(String[] args){
        //TODO leer archivos tanto los de la mutación como los del resultado de la exploración que se supone debe ser un logcat
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
        //Getting arguments
            //Reports
        String instrumentationReport = args[0];
        String logcat = args[1];
            //APKs
        String apkPathNoInstru = args[2];
        String apkPathOriginal = args[3];

        ApkInfoAnalyzer apkInfoOriginal = APKAnalyzerHelper.runApkAnalyzer(apkPathNoInstru);
        ApkInfoAnalyzer apkInfoInstrumented = APKAnalyzerHelper.runApkAnalyzer(apkPathOriginal);

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
                    instrumentedMethods.put(i,new MethodObject(i));
                }
            }
        }catch (Exception e){
            System.out.println("There was an error reading the instrumentation report");
            e.printStackTrace();
        }
        apkInfoInstrumented.setNumberOfMethodsInstrumented(instrumentedMethods.size());
        System.out.println("Original APK Info: " + apkInfoOriginal);
        System.out.println("Instrumented APK Info: " + apkInfoInstrumented);

        //Read logcat to measure coverage; All data will be in the hashmap
        LogcatProcessor.processLogcat(apkInfoInstrumented.getPackageName(),logcat,instrumentedMethods);

        //Calculate all metrics

        //List to know what methods were never explored (Cold methods)
        ArrayList<MethodObject> neverCalled = new ArrayList<>();

        //List to know what methods were called at least ones but not as many times as the most called ones.
        //(Warm Methods)
        ArrayList<MethodObject> lessCalledList = new ArrayList<>();
        MethodObject lessCalled = new MethodObject(999999999);

        //List to know what methods were the most called ones (Hot methods)
        ArrayList<MethodObject> mostCalledList = new ArrayList<>();
        MethodObject mostCalled = new MethodObject(-999999999);

        for(int i =0; i < instrumentedMethods.size(); i++){
            MethodObject currentMethod =instrumentedMethods.get(i);
            if(currentMethod.getCalledTimes() ==0){
                neverCalled.add(currentMethod);
            }else if(currentMethod.getCalledTimes() < lessCalled.getCalledTimes()){
                lessCalled = currentMethod;
            }

            if(currentMethod.getCalledTimes() > mostCalled.getCalledTimes()){
                mostCalled = currentMethod;
            }
        }

        //Find the list of hot, warm and cold methods
        for (int i =0; i < instrumentedMethods.size(); i++){
            MethodObject currentMethod =instrumentedMethods.get(i);
            if(currentMethod.getCalledTimes() == lessCalled.getCalledTimes()){
                lessCalledList.add(currentMethod);
            }else if(currentMethod.getCalledTimes() == mostCalled.getCalledTimes()){
                mostCalledList.add(currentMethod);
            }
        }

       // buildReport(apkInfoOriginal,apkInfoInstrumented);
    }

    private static void buildReport(ApkInfoAnalyzer apkInfoOriginal, ApkInfoAnalyzer apkInfoNoInstrumented) {
        try{
            //TODO maybe a json file is better
            FileWriter fileWriter = new FileWriter(new File("coverageReport.txt"));
            fileWriter.write(ORIGINAL_INFORMATION + ":"+ apkInfoOriginal);
            fileWriter.write(MUTATED_INFORMATION + ":"+ apkInfoNoInstrumented);
            fileWriter.flush();
        }catch (Exception e){
            System.out.println("Error building report");
            e.printStackTrace();
        }

    }
}
