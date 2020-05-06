package helper;

import model.ApkInfoAnalyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class APKAnalyzerHelper {
    private final static String FILE_SIZE = "FILE_SIZE";
    private final static String SUMMARY = "SUMMARY";
    private final static String MIN_SDK = "MIN_SDK";
    private final static String TARGET_SDK = "TARGET_SDK";
    private final static String METHOD_COUNT = "METHOD_COUNT";
    public final static ApkInfoAnalyzer runApkAnalyzer(String apkPath){
        System.out.println("apk path: " + apkPath + "\n");
        try{
            ApkInfoAnalyzer apkInfo = new ApkInfoAnalyzer();
            //Get summary and package name
            String summary = getSummary(apkPath);
            String packageName = summary.split("\\s+")[0];
            apkInfo.setSummary(summary);
            apkInfo.setPackageName(packageName);
            //Get apk size
            apkInfo.setSize(getSizeBytes(apkPath));
            //Get min sdk
            apkInfo.setMinSdkVersion(getMinSDK(apkPath));
            //Get target sdk
            apkInfo.setTargetSdk(getTargetSDK(apkPath));
            //Get number of methods
            apkInfo.setNumberOfMethodsApkAnalyzer(getMethodsCount(apkPath));
            return apkInfo;
        }catch (Exception e ){
            System.out.println("error getting apk info ");
            e.printStackTrace();
        }
        return null;
    }

    public final static String getSummary(String apkPath) throws Exception{
        List<String> response;
        if(isWindowsOS()){
            response = ProcessExecutorHelper.executeProcess(Arrays.asList("apkanalyzer.bat","apk","summary",apkPath),SUMMARY);
        }else{
            response = ProcessExecutorHelper.executeProcess(Arrays.asList("apkanalyzer","apk","summary",apkPath),SUMMARY);
        }
        return response.get(0).trim();
    }

    public final static int getSizeBytes(String apkPath) throws Exception{
        List<String> response;
        if(isWindowsOS()){
            response = ProcessExecutorHelper.executeProcess(Arrays.asList("apkanalyzer.bat","apk","file-size",apkPath),FILE_SIZE);
        }else{
            response = ProcessExecutorHelper.executeProcess(Arrays.asList("apkanalyzer","apk","file-size",apkPath),FILE_SIZE);
        }
        return Integer.parseInt(response.get(0).trim());
    }

    public final static int getMinSDK(String apkPath) throws Exception{
        List<String> response;
        if(isWindowsOS()){
            response = ProcessExecutorHelper.executeProcess(Arrays.asList("apkanalyzer.bat","manifest","min-sdk",apkPath),MIN_SDK);
        }else{
            response = ProcessExecutorHelper.executeProcess(Arrays.asList("apkanalyzer","manifest","min-sdk",apkPath),MIN_SDK);
        }

        return Integer.parseInt(response.get(0).trim());
    }

    public final static int getTargetSDK(String apkPath) throws Exception{
        List<String> response;
        if(isWindowsOS()){
            response = ProcessExecutorHelper.executeProcess(Arrays.asList("apkanalyzer.bat","manifest","target-sdk",apkPath),TARGET_SDK);
        }else{
            response = ProcessExecutorHelper.executeProcess(Arrays.asList("apkanalyzer","manifest","target-sdk",apkPath),TARGET_SDK);
        }
        return  Integer.parseInt(response.get(0).trim());
    }

    public final static int getMethodsCount(String apkPath) throws Exception{
        List<String> response;
        if(isWindowsOS()){
            response = ProcessExecutorHelper.executeProcess(Arrays.asList("apkanalyzer.bat","dex","references",apkPath),METHOD_COUNT);
        }else{
            response = ProcessExecutorHelper.executeProcess(Arrays.asList("apkanalyzer","dex","references",apkPath),METHOD_COUNT);
        }
        String[] splited = response.get(0).split("\\s+");
        ArrayList<Integer> references = new ArrayList<>();
        for(int i = 1; i < splited.length; i+=2){
            references.add(new Integer(splited[i].trim()));
        }
        int total = 0;
        for(int i = 0; i < references.size(); i++){
            total += references.get(i);
        }
        return total;
    }

    private static boolean isWindowsOS(){
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    }
}
