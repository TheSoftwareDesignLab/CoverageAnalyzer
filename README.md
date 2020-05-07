# Coverage Analyzer (CA)

This tool extract statistics of the instumentation made by [InstruAPK](https://github.com/caev03/InstruAPK.git) and measure the coverage reached by some automatic exploration tools such as [RIP](https://github.com/TheSoftwareDesignLab/rip), Monkey and Droidbot. (The apk used in the automatic exploration should be intrumented by InstruAPK in order to be able to use CoverageAnalyzer).

## Compile

Download and compile CoverageAnalyzer tool for InstruAPK instrumentations

```bash
git clone https://github.com/MichaelOsorio2017/CoverageAnalyzer.git
cd CoverageAnalyzer
gradle clean
gradle coverageanalyzer
```

The generated ```.jar``` can be found in ``CoverageAnalyzer/build/libs/CoverageAnalyzer.jar``

## Usage

To run CoverageAnalyzer use the following command

```Bash
java -jar CoverageAnalyzer.jar <InstrumentationReportPath> <LogcatPath> <OriginalAPKPath> <InstrumentedAPKPath>
```

1. ``<InstrumentationReportPath>`` Instrumentation report .json file (mutation report)

2. ``<LogcatPath>`` Logcat

3. ``<OriginalAPKPath>`` Original APK (Before InstruAPK instrumentation)

4. ``<InstrumentedAPKPath>`` Instrumented (mutated) APK

Note: You need to setup the system variable called ``Path`` to contain the path to ``/Android/Sdk/cmdline-tools/latest/bin/`` in case of your SO is Windows

### Example

 ```Bash
cd CoverageAnalyzer
java -jar ./CoverageAnalyzer.jar ./mutant/com.evancharlton.mileage-locations.json ./RIPExplorationReport/logcat.txt ./apksTest/com.evancharlton.mileage.apk ./mutant/com.evancharlton.mileage-mutant0/com.evancharlton.mileage-aligned-debugSigned.apk

```

### Coverage Report

```Javascript

{
    "originalInformation":{
        //ApkInfoAnalyzer
    },
    "instrumentedInformation":{
        //Same content than originalInformation
    },
    "differenceBetweenNumberOfMethods":0,
    "sizeDifferenceBytes":0,
    "coverageApkAnalyzer":0,
    "coverageInstruAPK":0,
    "instrumentedMethods":[
        //MethodObjects
    ],
    "numberInstrumentedMethods":0,
    "allMethodsCalled":[
        //MethodObjects
    ],
    "numberCalledMethods":0,
    "coldMethods":[
        //MethodObjects
    ],
    "numberColdMethods":0,
    "warmMethods":[
        //MethodObjects
    ],
    "numberWarmMethods":0,
    "hotMethods":[
        //MethodObjects
    ],
    "numberHotMethods":0,
}

```

1. ``originalInformation`` APK information before being instrumented by InstruAPK
    1.1. ``ApkInfoAnalyzer`` objects have the following structure

    ```Javascript
    {
        "packageName":"",
        "sizeInBytes":0,
        "minSdkVersion":0,
        "targetSdk":0,
        "numberOfMethodsApkAnalyzer":0, //Number of methods find by apkanalyzer on this apk
        "numberOfMethodsInstrumented":0, //Number of methods on this apk that were instrumented
        "summary":"", //summary got by executing apkanalyzer apk summary
    }
    ```

2. ``instrumentedInformation`` APK information after instrumentation by InstruAPK

3. ``differenceBetweenNumberOfMethods`` Difference between the number of methods acording Apkanalyzer (android studio tool) and the total number of methods instrumented by InstruAPK. apkanalyzerMethods - totalNumberInstrumentedMethods

4. ``sizeDifferenceBytes`` Difference between the size of the apk without being instrumented and the one after instrumentation.

5. ``coverageInstruAPK`` Coverage measurement using the number of methods instrumented. ``coverageInstruAPK = (allMethodsCalled/numberInstrumentedMethods)*100``

6. ``instrumentedMethods`` List of methods instrumented by InstruAPK.

    6.1. ``MethodObjects`` objects with the following structure

    ```Javascript
    {
        "methodIndex":0, //Method unique id
        "fileName":"", //Java file were the method is implemented
        "methodName":"",
        "methodParameters":"", //Smali representation of the method parameters
        "firstCall":0000, //Time in milliseconds when the method was called for the first time
        "lastCall":0000,//Time in milliseconds when the method was called for the last time
        "callHistory":[0000,00000,0000], //List of all the times the method was called
        "calledTimes":0, //Number of times the method was called
    }
    ```

7. ``numberInstrumentedMethods`` Number of instrumented methods

8. ``allMethodsCalled`` List of all methods that were called

9. ``numberCalledMethods`` Number of methods called

10. ``coldMethods``List of methods that were never called

11. ``numberColdMethods`` Number of methods never called (Cold methods)

12. ``warmMethods`` List of methods that were called at least once but not as many times as hot methods

13. ``numberWarmMethods`` Number of warm methods

14. ``hotMethods`` List of methods that were called the most

15. ``numberHotMethods``Number of methods that were called the most (Hot methods)
