import Helpers.APKAnalyzerHelper;
import Model.ApkInfoAnalyzer;
import Model.MethodObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CoverageAnalyzer {
    public static final String ORIGINAL_INFORMATION = "ORIGINAL_INFORMATION";
    public static final String MUTATED_INFORMATION = "MUTATED_INFORMATION";
    public static final String INSTRUMENTER = "INSTRUMENTER";
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
            System.out.println("1. Instrumentation report file");
            System.out.println("2. Exploration Coverage Report (Logcat)");
            System.out.println("3. APK instrumented path");
            System.out.println("4. APK non instrumented path");
            return;
        }
        //Getting arguments
        String instrumentationReport = args[0];
        String explorationCoverageReport = args[1];

        String apkPath = args[2];
        String apkPathNoInstru = args[3];

//        ApkInfoAnalyzer apkInfoOriginal = APKAnalyzerHelper.runApkAnalyzer(apkPathNoInstru);
//        ApkInfoAnalyzer apkInfoInstrumented = APKAnalyzerHelper.runApkAnalyzer(apkPath);
//
//        System.out.println("Original APK Info: " + apkInfoOriginal);
//        System.out.println("Instrumented APK Info: " + apkInfoInstrumented);
        HashMap<Integer,MethodObject> instrumentedMethods = new HashMap<>();
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(instrumentationReport));
            String line;
            line = bufferedReader.readLine();
            while(line != null){
                line = line + "";
                System.out.println(line + " contains: " + INSTRUMENTER + " " + line.contains(INSTRUMENTER) );
                if(line.contains(INSTRUMENTER)){
                    MethodObject method = new MethodObject(line);
                    instrumentedMethods.put(method.getMethodIndex(),method);
                }
                line = bufferedReader.readLine();
            }
            System.out.println(instrumentedMethods.size());
        }catch (Exception e){
            System.out.println("There was an error reading the instrumentation report");
            e.printStackTrace();
        }

       // buildReport(apkInfoOriginal,apkInfoInstrumented);
    }

    private static void buildReport(ApkInfoAnalyzer apkInfoOriginal, ApkInfoAnalyzer apkInfoNoInstrumented) {
        try{
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
