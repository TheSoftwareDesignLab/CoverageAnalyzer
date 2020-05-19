# Coverage Analyzer (CA)

This tool computes statistics of the instrumentation made by [InstruAPK](https://github.com/TheSoftwareDesignLab/InstruAPK), measures the coverage reached by some automatic exploration tools such as [RIP](https://github.com/TheSoftwareDesignLab/rip), Monkey and Droidbot. (The apk used in the automatic exploration should be intrumented by InstruAPK in order to be able to use CoverageAnalyzer and the logcat while the application was explored needs to be extracted).

Note: Some of the tools already extract the logcat. In that case, that logcat can be used.
Note 2: The logcat file should have as encoding type UTF-8

## Compile

Download and compile CoverageAnalyzer tool for InstruAPK instrumentations

```bash
git clone https://github.com/TheSoftwareDesignLab/CoverageAnalyzer.git
cd CoverageAnalyzer
gradle clean
gradle coverageanalyzer
```

The generated ```.jar``` can be found in ``CoverageAnalyzer/build/libs/CoverageAnalyzer.jar``

## Usage

To run CoverageAnalyzer use the following command

Note: You need to setup the system variable called ``Path`` to contain the path to ``/Android/Sdk/cmdline-tools/latest/bin/`` which is the location of apkanalyzer.bat, in case of your operating system is Windows.

```Bash
java -jar CoverageAnalyzer.jar <InstrumentationReportPath> <LogcatPath> <OriginalAPKPath> <InstrumentedAPKPath>
```

1. ``<InstrumentationReportPath>`` Instrumentation report .json file (mutation report)

2. ``<LogcatPath>`` Logcat .txt file

3. ``<OriginalAPKPath>`` Original APK (Before InstruAPK instrumentation)

4. ``<InstrumentedAPKPath>`` Instrumented APK

### Example

 ```Bash
cd CoverageAnalyzer
java -jar ./CoverageAnalyzer.jar ./mutant/com.evancharlton.mileage-locations.json ./RIPExplorationReport/logcat.txt ./apksTest/com.evancharlton.mileage.apk ./mutant/com.evancharlton.mileage-mutant0/com.evancharlton.mileage-aligned-debugSigned.apk

```

### Coverage Report

```Javascript

{
    "originalInformation":{
        //ApkInfoAnalyzer (1.1)
    },
    "instrumentedInformation":{
        //ApkInfoAnalyzer (1.1)
    },
    "differenceBetweenNumberOfMethods":0,
    "sizeDifferenceBytes":0,
    "coverageApkAnalyzer":0,
    "coverageInstruAPK":0,
    "instrumentedMethods":[
        //MethodObjects (6.1)
    ],
    "allMethodsCalled":[
        //MethodObjects (6.1)
    ],
    "totalCalledMethods":0,
    "coldMethods":[
        //MethodObjects (6.1)
    ],
    "totalColdMethods":0,
    "warmMethods":[
        //MethodObjects (6.1)
    ],
    "totalWarmMethods":0,
    "hotMethods":[
        //MethodObjects (6.1)
    ],
    "totalHotMethods":0,
    "errorTraces":[{
        "trace":"", //Complete trace
        "times":0, //Times the exact same trace was found
    }]
    "totalErrorTraces":0,
    "runtimeErrorTraces":[{
        "trace":"", //Complete trace
        "times":0 //Times the exact same trace was found
    }],
    "totalRuntimeErrorTraces":0,
    "uniqueTraces":[""],
    "totalUniqueTraces":0,
}

```

1. ``originalInformation`` APK information before being instrumented by InstruAPK

    1.1. ``ApkInfoAnalyzer`` objects represent an APK. Their structure is as follows

    ```Javascript
    {
        "packageName":"",
        "sizeInBytes":0,
        "minSdkVersion":0,
        "targetSdk":0,
        "numberOfMethodsApkAnalyzer":0, //Number of methods find by apkanalyzer in this apk
        "numberOfMethodsInstrumented":0, //Number of methods instrumented in this APK
        "summary":"", //summary got by executing apkanalyzer's command 'apk summary'
    }
    ```

2. ``instrumentedInformation`` APK information after instrumentation by InstruAPK. Same structure of 1.1

3. ``differenceBetweenNumberOfMethods`` Difference between the number of methods acording Apkanalyzer (android studio tool) and the total number of methods instrumented by InstruAPK. apkanalyzerMethods - totalNumberInstrumentedMethods

4. ``sizeDifferenceBytes`` Difference between the size of the apk without being instrumented and the one after instrumentation.

5. ``coverageInstruAPK`` Coverage measurement using the number of methods instrumented. ``coverageInstruAPK = (allMethodsCalled/numberInstrumentedMethods)*100``

6. ``instrumentedMethods`` List of methods instrumented by InstruAPK. Collection of MethodObject.

    6.1. ``MethodObject`` objects represent an instrumented method. Their structure is as follows

    ```Javascript
    {
        "methodIndex":0, //Method unique id
        "fileName":"", //Smali file name - Class name.
        "methodName":"", //Method name
        "methodParameters":"", //Smali representation of the method parameters
        "firstCall":0000, //Time in milliseconds when the method was called for the first time
        "lastCall":0000,//Time in milliseconds when the method was called for the last time
        "callHistory":[0000,00000,0000], //List of all the times the method was called
        "calledTimes":0, //Number of times the method was called
    }
    ```

7. ``allMethodsCalled`` List of all methods that were called. Collection of MethodObject

8. ``totalCalledMethods`` Total number of methods called

9. ``coldMethods``List of methods that were never called. Collection of MethodObject

10. ``totalColdMethods`` Total number of methods never called (i.e., cold methods)

11. ``warmMethods`` List of methods that were called at least once but not as many times as hot methods. Collection of MethodObject

12. ``totalWarmMethods`` Total number of warm methods

13. ``hotMethods`` List of methods that were called the most (i.e., top-1 methods). Collection of MethodObject

14. ``totalHotMethods`` Total number of methods that were called the most (Hot methods)

15. ``errorTraces`` List of traces of the application under analysis that are tagged with 'System.err' found in the logcat file

16. ``totalErrorTraces`` Total number of traces tagged with 'System.err' found

17. ``runtimeErrorTraces`` List of traces of the application under analysis that are tagged with 'AndroidRuntime' found in the logcat file

18. ``totalRuntimeErrorTraces`` Total Number of traces tagged with 'AndroidRuntime' found

19. ``uniqueTraces`` List of unique traces of the application under analysis that were tagged with 'System.err' or 'AndroidRuntime' found in the logcat file

20. ``totalUniqueTraces`` Total number of unique traces found in the logcat file
