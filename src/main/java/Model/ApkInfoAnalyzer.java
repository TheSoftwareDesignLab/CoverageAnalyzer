package Model;

public class ApkInfoAnalyzer {
    private String packageName ="";
    private int size = 0;
    private int minSdkVersion = 0;
    private int targetSdk = 0;
    private int numberOfMethodsApkAnalyzer = 0;
    private int numberOfMethodsInstrumented = 0;
    private String summary = "";

    public void ApkInfoAnalyzer(String packageName, int size, int minSdkVersion, int targetSdk, int numberOfMethodsApkAnalyzer, String summary){
        this.packageName = packageName;
        this.size = size;
        this.minSdkVersion = minSdkVersion;
        this.targetSdk = targetSdk;
        this.numberOfMethodsApkAnalyzer = numberOfMethodsApkAnalyzer;
        this.summary = summary;
    }

    public  void ApkInfoAnalyzer(){

    }

    public void setSummary(String summary) {
        this.summary = summary;
        this.setPackageName(summary.split(" ")[0]);
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setMinSdkVersion(int minSdkVersion) {
        this.minSdkVersion = minSdkVersion;
    }

    public void setTargetSdk(int targetSdk) {
        this.targetSdk = targetSdk;
    }

    public void setNumberOfMethodsApkAnalyzer(int numberOfMethods) {
        this.numberOfMethodsApkAnalyzer = numberOfMethods;
    }

    public void setNumberOfMethodsInstrumented(int numberOfMethodsInstrumented) {
        this.numberOfMethodsInstrumented = numberOfMethodsInstrumented;
    }

    public String getPackageName(){
        return this.packageName;
    }

    public int getSize() {
        return size;
    }

    public int getMinSdkVersion() {
        return minSdkVersion;
    }

    public int getTargetSdk() {
        return targetSdk;
    }

    public int setNumberOfMethodsApkAnalyzer() {
        return numberOfMethodsApkAnalyzer;
    }

    @Override
    public String toString() {
        return "{" +
                "packageName=" + packageName +
                ", size in bytes=" + size +
                ", minSdkVersion=" + minSdkVersion +
                ", targetSdk= " + targetSdk +
                ", numberOfMethodsApkAnalyzer= " + numberOfMethodsApkAnalyzer +
                ", numberOfMethodsApkAnalyzer= " + numberOfMethodsInstrumented +
                ", summary= '" + summary + '\'' +
                "}\n";
    }
}
