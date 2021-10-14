package com.getcapacitor.community.speechrecognition;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.microsoft.cognitiveservices.speech.CancellationDetails;
import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentConfig;
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentGradingSystem;
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentGranularity;
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentResult;
import com.microsoft.cognitiveservices.speech.PropertyId;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;

@CapacitorPlugin(
  permissions = {
    @Permission(
      strings = { Manifest.permission.RECORD_AUDIO },
      alias = "record_audio"
    ),
  }
)
public class SpeechRecognition extends Plugin {

  private static final int REQUEST_CODE_PERMISSION = 2001;
  private static final String MISSING_PERMISSION = "Missing permission";
  private static final String DEFAULT_LANGUAGE = "en-US";

  @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
  public void start(PluginCall call) {
    if (!hasAudioPermissions()) {
      call.reject(MISSING_PERMISSION);
      return;
    }
    call.setKeepAlive(true);

    String language = call.getString("language", DEFAULT_LANGUAGE);
    String subscription = call.getString("subscription");
    String region = call.getString("region");
    String referenceText = call.getString("referenceText");

    fromMic(call, language, subscription, region, referenceText);
  }

  @PluginMethod
  public void hasPermission(PluginCall call) {
    call.resolve(new JSObject().put("permission", hasAudioPermissions()));
  }

  @PluginMethod
  public void requestPermission(PluginCall call) {
    if (hasAudioPermissions()) {
      call.resolve();
      return;
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      ActivityCompat.requestPermissions(
        bridge.getActivity(),
        new String[] { Manifest.permission.RECORD_AUDIO },
        REQUEST_CODE_PERMISSION
      );
    }
    call.resolve();
  }

  private boolean hasAudioPermissions() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return true;
    }

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
      speechConfig.setProperty(
        PropertyId.SpeechServiceConnection_EndSilenceTimeoutMs,
        "3000"
      );

      SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig);
      {
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

        SpeechRecognitionResult result = recognizer.recognizeOnceAsync().get();

        if (result.getReason() == ResultReason.RecognizedSpeech) {
          PronunciationAssessmentResult pronunciationAssessmentResult = PronunciationAssessmentResult.fromResult(
            result
          );
          Double pronunciationScore = pronunciationAssessmentResult.getPronunciationScore();

          call.resolve(
            new JSObject()
              .put("isStarting", false)
              .put("pronunciationScore", pronunciationScore)
          );
        } else if (result.getReason() == ResultReason.NoMatch) {
          call.resolve(
            new JSObject().put("isStarting", false).put("pronunciationScore", 0)
          );
        } else if (result.getReason() == ResultReason.Canceled) {
          CancellationDetails cancellation = CancellationDetails.fromResult(
            result
          );
          System.out.println("CANCELED: Reason=" + cancellation.getReason());

          if (cancellation.getReason() == CancellationReason.Error) {
            System.out.println(
              "CANCELED: ErrorCode=" + cancellation.getErrorCode()
            );
            System.out.println(
              "CANCELED: ErrorDetails=" + cancellation.getErrorDetails()
            );
            System.out.println(
              "CANCELED: Did you update the subscription info?"
            );
          }
          call.resolve(
            new JSObject().put("isStarting", false).put("pronunciationScore", 0)
          );
        }

        result.close();
      }

      pronunciationAssessmentConfig.close();
      speechConfig.close();
      recognizer.close();

      call.release(bridge);
    } catch (Exception ex) {
      Log.e("fromMic", "unexpected " + ex.getMessage());
    }
  }
}
