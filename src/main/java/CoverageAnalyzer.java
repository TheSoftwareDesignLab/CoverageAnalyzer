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
        //Warm methods are in fact almost all the other methods because they are neither in a extreme either other (Not 0 but either the max number)
        //Could not be relevant or maybe does not add any new information but at least we have the data.
        ArrayList<MethodObject> lessCalledList = new ArrayList<>();

        //All methods that were called at least once
        ArrayList<MethodObject> allCalled = new ArrayList<>();

        //List to know what methods were the most called ones (Hot methods)
        ArrayList<MethodObject> mostCalledList = new ArrayList<>();
        MethodObject mostCalled = new MethodObject(-999999999);

        for(int i =0; i < instrumentedMethods.size(); i++){
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
        for (int i =0; i < instrumentedMethods.size(); i++){
            MethodObject currentMethod =instrumentedMethods.get(i);
            if(currentMethod.getCalledTimes() == mostCalled.getCalledTimes()){
                mostCalledList.add(currentMethod);
            } else if(currentMethod.getCalledTimes() > 0){
                lessCalledList.add(currentMethod);
            }
        }
        //Calculate coverage percentage according the number of methods given by apkanalyzer
        double coveragePercentageAccordingAPKAnalyzer = (allCalled.size()/apkInfoInstrumented.getNumberOfMethodsApkAnalyzer())*100;
        //Calculate coverage percentage according the number of instrumentedMethods
        double coveragePercentageAccordingThis = (allCalled.size()/apkInfoInstrumented.getNumberOfMethodsInstrumented())*100;

       // buildReport(apkInfoOriginal,apkInfoInstrumented);
    }

    private static void buildReport(ApkInfoAnalyzer apkInfoOriginal, ApkInfoAnalyzer apkInfoInstrumented) {
        try{
            //TODO maybe a json file is better
            //TODO write all these comments somewhere i can remember why i made this decisions and also documentation for the program.
            
            FileWriter fileWriter = new FileWriter(new File("coverageReport.txt"));
            fileWriter.write(ORIGINAL_INFORMATION + ":"+ apkInfoOriginal);
            fileWriter.write(MUTATED_INFORMATION + ":"+ apkInfoInstrumented);
            //TODO store the information of diference

            int differenceBetweenNumberOfMethods = apkInfoInstrumented.getNumberOfMethodsApkAnalyzer() - apkInfoInstrumented.getNumberOfMethodsInstrumented();
            int sizeDifference = apkInfoInstrumented.getSize() - apkInfoOriginal.getSize();

            //TODO store the relative coverage

            //TODO store the list of methods never called, hot methods, all called and warm methods

            // I think that's all the information we can get from the instrumentation. I wanted to separate calls by class but
            // it is possible to have too files with the same name when they are in different package.
            // Maybe that can be solve using the location stored for every mutation in the mutation process.

            fileWriter.flush();
        }catch (Exception e){
            System.out.println("Error building report");
            e.printStackTrace();
        }

    }
}
