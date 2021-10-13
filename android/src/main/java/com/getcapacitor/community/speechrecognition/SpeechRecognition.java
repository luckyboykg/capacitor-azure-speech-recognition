package com.getcapacitor.community.speechrecognition;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentConfig;
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentGradingSystem;
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentGranularity;
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentResult;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@CapacitorPlugin(
  permissions = {
    @Permission(
      strings = { Manifest.permission.RECORD_AUDIO },
      alias = "record_audio"
    ),
  }
)
public class SpeechRecognition extends Plugin implements Constants {

  @Override
  public void load() {
    super.load();
  }

  @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
  public void start(PluginCall call) {
    if (!hasAudioPermissions(Manifest.permission.RECORD_AUDIO)) {
      call.reject(MISSING_PERMISSION);
      return;
    }

    String language = call.getString("language", DEFAULT_LANGUAGE);
    String subscription = call.getString("subscription");
    String region = call.getString("region");
    String referenceText = call.getString("referenceText");

    bridge
      .getWebView()
      .post(
        () -> {
          fromMic(call, language, subscription, region, referenceText);
        }
      );
  }

  @PluginMethod
  public void hasPermission(PluginCall call) {
    call.resolve(
      new JSObject()
      .put("permission", hasAudioPermissions(Manifest.permission.RECORD_AUDIO))
    );
  }

  @PluginMethod
  public void requestPermission(PluginCall call) {
    if (hasAudioPermissions(Manifest.permission.RECORD_AUDIO)) {
      call.resolve();
      return;
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      //      bridge
      //        .getActivity()
      //        .requestPermissions(
      //          new String[]{Manifest.permission.RECORD_AUDIO},
      //          REQUEST_CODE_PERMISSION
      //        );

      ActivityCompat.requestPermissions(
        bridge.getActivity(),
        new String[] { Manifest.permission.RECORD_AUDIO },
        REQUEST_CODE_PERMISSION
      );
    }
    call.resolve();
  }

  private boolean hasAudioPermissions(String type) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return true;
    }
    //    PermissionState permissionState = getPermissionState(type);
    //    return permissionState == PermissionState.GRANTED;

    int test = ContextCompat.checkSelfPermission(
      bridge.getActivity(),
      Manifest.permission.RECORD_AUDIO
    );

    return (
      ContextCompat.checkSelfPermission(
        bridge.getActivity(),
        Manifest.permission.RECORD_AUDIO
      ) ==
      PackageManager.PERMISSION_GRANTED
    );
  }

  private void fromMic(
    PluginCall call,
    String language,
    String subscription,
    String region,
    String referenceText
  ) {
    try {
      call.setKeepAlive(true);

      PronunciationAssessmentConfig pronunciationAssessmentConfig = new PronunciationAssessmentConfig(
        referenceText,
        PronunciationAssessmentGradingSystem.HundredMark,
        PronunciationAssessmentGranularity.Word
      );

      SpeechConfig speechConfig = SpeechConfig.fromSubscription(
        subscription,
        region
      );
      speechConfig.setSpeechRecognitionLanguage(language);

      com.microsoft.cognitiveservices.speech.SpeechRecognizer recognizer = new com.microsoft.cognitiveservices.speech.SpeechRecognizer(
        speechConfig
      );
      pronunciationAssessmentConfig.applyTo(recognizer);

      recognizer.recognizing.addEventListener(
        (s, e) -> {
          if (e.getResult().getText().equalsIgnoreCase(referenceText)) {
            call.resolve(
              new JSObject()
                .put("isStarting", true)
                .put("pronunciationScore", 0)
            );
          }
        }
      );

      Future<SpeechRecognitionResult> future = recognizer.recognizeOnceAsync();
      SpeechRecognitionResult result = future.get(30, TimeUnit.SECONDS);
      PronunciationAssessmentResult pronunciationAssessmentResult = PronunciationAssessmentResult.fromResult(
        result
      );
      Double pronunciationScore = pronunciationAssessmentResult.getPronunciationScore();

      call.resolve(
        new JSObject()
          .put("isStarting", false)
          .put("pronunciationScore", pronunciationScore)
      );

      recognizer.close();
      speechConfig.close();
      pronunciationAssessmentConfig.close();
      result.close();
    } catch (Exception ex) {
      Log.e("fromMic", "unexpected " + ex.getMessage());
    }
  }
}
