# Disclaimers

1. To test and use all aspects of this application, including the core SLM and RAG pipeline functionalities, you will need access to a higher-end Android device. This is due to the emulator within Android Studio not being able to reliably run the SLM inference.
2. As noted within the evaluation chapter of the report, the SDK used for the RAG pipeline is now deprecated. As such, the RAG pipeline may not function, depending on when the app is tested. To check the status of the SDK, you can visit [this link](https://ai.google.dev/edge/mediapipe/solutions/genai/rag/android).

---

# Instructions

## Setting up the environment 
1. To run the application, first [download Android Studio](https://developer.android.com/studio). Once complete, open the source code within Android Studio.
2. Next, the SLM and embedding model both need to be downloaded and pushed to the correct location on the connected Android device.
3. Download Gemma 3n E2B from [this link](https://huggingface.co/google/gemma-3n-E2B-it-litert-lm/tree/main), ensuring to download the file named `gemma-3n-E2B-it-int4.litertlm`.
4. Download EmbeddingGemma from [this link](https://huggingface.co/litert-community/embeddinggemma-300m/tree/main), ensuring to download the file named `embeddinggemma-300M_seq1024_mixed-precision.tflite`. Additionally, also download the `sentencepiece.model` file from this same link.
5. Now that all three files are downloaded, perform the following three commands within the project directory while the Android device is connected:

`adb push [gemma-3n-E2B file path] /data/local/tmp/gemma-3n-E2B-it-int4.litertlm`

`adb push [EmbeddingGemma download path] /data/local/tmp/slm/embeddinggemma-300M_seq1024_mixed-precision.tflite`

`adb push [sertencepiece download path] /data/local/tmp/slm/sentencepiece.model`

6. As a result, the following command:

`adb shell ls /data/local/tmp/slm`

Should give the corresponding output:

```                                                                                     
embeddinggemma-300M_seq1024_mixed-precision.tflite
gemma-3n-E2B-it-int4.litertlm
sentencepiece.model
```

## Running the app & the tests

### Running the app
1. To run the app, press the run button within Android Studio - ensure that the Android device is connected and the app configuration is selected.
2. To achieve better performance, the app can be run in release mode rather than the default debug mode. This can be changed within the build variants section of Android Studio
3. Please note that when first booting the app, the initialisation of the SLM can take upwards of one minute. To ensure that it is properly initialised, keep the app in the foreground during this time and do not let the device be put to sleep. If any issues are experienced, the app can be uninstalled from the device and re-installed and run via Android Studio. 

### Running the tests
1. To run the local tests, right click on the `com.example.docbot (test)` package within the Android file explorer section in Android Studio and click the run tests option.
2. To run the instrumented tests, right click on the `com.example.docbot (androidTest)` package within the Android file explorer section in Android Studio and click the run tests option. When doing this, ensure that the device is connected and is not put to sleep while the tests are running, otherwise some could fail.

