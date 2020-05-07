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
java -jar ./CoverageAnalyzer.jar ./mutant/com.evancharlton.mileage-locations.json ./RIPExplorationReport/explorationReport.txt ./apksTest/com.evancharlton.mileage.apk ./mutant/com.evancharlton.mileage-mutant0/com.evancharlton.mileage-aligned-debugSigned.apk

```

### Coverage Report

```Json

{
    "originalInformation":{},
    "instrumentedInformation":{
        //Same content than originalInformation
    },
    "differenceBetweenNumberOfMethods":0,
    "sizeDifferenceBytes":0,
    "coverageApkAnalyzer":0,
    "CoverageInstruAPK":0,
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

1. ``originalInformation``

2. ``instrumentedInformation``

3. ``differenceBetweenNumberOfMethods``

4. d

5. e

6.

7.

8.

9.