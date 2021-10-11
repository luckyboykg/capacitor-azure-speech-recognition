package com.getcapacitor.community.speechrecognition;

import android.Manifest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.BuildConfig;
import com.orhanobut.logger.Logger;
import java.util.concurrent.Future;

@CapacitorPlugin(
  permissions = {
          @Permission(alias = "RECORD_AUDIO",
                  strings = {Manifest.permission.RECORD_AUDIO})
  }
)
public class SpeechRecognition extends Plugin implements Constants {
  public static final String DEFAULT_LANGUAGE = "en-US";

  @Override
  public void load() {
    super.load();

    Logger.addLogAdapter(
      new AndroidLogAdapter() {

        @Override
        public boolean isLoggable(int priority, @Nullable String tag) {
          return BuildConfig.DEBUG;
        }
      }
    );

//    bridge
//      .getWebView()
//      .post(
//        new Runnable() {
//
//          @Override
//          public void run() {
//
//          }
//        }
//      );
  }

  @PluginMethod
  public void start(PluginCall call) {
    if (!hasAudioPermissions(RECORD_AUDIO_PERMISSION)) {
      call.reject(MISSING_PERMISSION);
      return;
    }

    String language = call.getString("language",DEFAULT_LANGUAGE);
    String subscription = call.getString("subscription");
    String region = call.getString("region");

    bridge
            .getWebView()
            .post(
                    () -> {

                      fromMic(call,language,subscription, region);
                    }
            );
  }

  @PluginMethod
  public void hasPermission(PluginCall call) {
    call.resolve(
      new JSObject()
      .put("permission", hasAudioPermissions(RECORD_AUDIO_PERMISSION))
    );
  }

  @PluginMethod
  public void requestPermission(PluginCall call) {
    if (!hasAudioPermissions(RECORD_AUDIO_PERMISSION)) {
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
        bridge
          .getActivity()
          .requestPermissions(
            new String[] { RECORD_AUDIO_PERMISSION },
            REQUEST_CODE_PERMISSION
          );
      }
      call.resolve();
    }
  }

  private boolean hasAudioPermissions(String type) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return true;
    }

    return getPermissionState(type) == PermissionState.GRANTED;
  }

  private void fromMic(PluginCall call, String language, String subscription,String region) {
    try {
      SpeechConfig speechConfig = SpeechConfig.fromSubscription(subscription, region);
      speechConfig.setSpeechRecognitionLanguage(language);

      com.microsoft.cognitiveservices.speech.SpeechRecognizer recognizer = new com.microsoft.cognitiveservices.speech.SpeechRecognizer(speechConfig);
      Future<SpeechRecognitionResult> task = recognizer.recognizeOnceAsync();
      SpeechRecognitionResult result = task.get();

      call.resolve(
              new JSObject().put("status", "success").put("result", result.getText())
      );
    } catch (Exception ex) {
      Log.e("fromMic", "unexpected " + ex.getMessage());
    }
  }
}
