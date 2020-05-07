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
import java.util.List;

public class CoverageAnalyzer {
    public static final String ORIGINAL_INFORMATION = "originalInformation";
    public static final String MUTATED_INFORMATION = "mutatedInformation";
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

        System.out.println("Extracting data from exploration");
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

        for(int i =1; i < instrumentedMethods.size(); i++){
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
        for (int i =1; i < instrumentedMethods.size(); i++){
            MethodObject currentMethod =instrumentedMethods.get(i);
            if(currentMethod.getCalledTimes() == mostCalled.getCalledTimes()){
                mostCalledList.add(currentMethod);
            } else if(currentMethod.getCalledTimes() > 0){
                lessCalledList.add(currentMethod);
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
        buildReport(apkInfoOriginal,apkInfoInstrumented,instrumentedMethods, neverCalled,lessCalledList,mostCalledList,allCalled,coveragePercentageAccordingAPKAnalyzer,coveragePercentageAccordingInstruAPK);
        System.out.println("Report built");
    }

    private static void buildReport(ApkInfoAnalyzer apkInfoOriginal, ApkInfoAnalyzer apkInfoInstrumented, HashMap<Integer,MethodObject> instrumentedMethods
            , List<MethodObject> coldMethods, List<MethodObject> warmMethods, List<MethodObject> hotMethods, List<MethodObject> allMethodsCalled,
                                    double coverageApkAnalyzer, double coverageCA) {
        try{
            JSONObject finalReport = new JSONObject();
            //APKs information
            finalReport.put(ORIGINAL_INFORMATION,apkInfoOriginal.parseToJSON());
            finalReport.put(MUTATED_INFORMATION, apkInfoInstrumented.parseToJSON() );
            int differenceBetweenNumberOfMethods = apkInfoInstrumented.getNumberOfMethodsApkAnalyzer() - apkInfoInstrumented.getNumberOfMethodsInstrumented();
            int sizeDifference = apkInfoInstrumented.getSize() - apkInfoOriginal.getSize();
            //store the information of difference between APKs
            finalReport.put("differenceBetweenNumberOfMethods",differenceBetweenNumberOfMethods);
            finalReport.put("sizeDifferenceBytes", sizeDifference);
            finalReport.put("numberMethodsCalled",allMethodsCalled.size());
            //Coverage Information
            System.out.println("Coverage analyzer: " + coverageApkAnalyzer);
            System.out.println("Coverage CA: " + coverageCA);
            finalReport.put("coverageApkAnalyzer",coverageApkAnalyzer);
            finalReport.put("CoverageInstruAPK",coverageCA);
            //store the lists of methods instrumented methods, all methods called, cold methods, warm methods, hot methods
            finalReport.put("instrumentedMethods",instrumentedMethods);
            finalReport.put("allMethodsCalled",allMethodsCalled);
            finalReport.put("coldMethods",coldMethods);
            finalReport.put("warmMethods",warmMethods);
            finalReport.put("hotMethods",hotMethods);

            FileWriter fileWriter = new FileWriter(new File("coverageReport.json"));
            fileWriter.write(finalReport.toJSONString());
            fileWriter.flush();
        }catch (Exception e){
            System.out.println("Error building report");
            e.printStackTrace();
        }

    }
}
