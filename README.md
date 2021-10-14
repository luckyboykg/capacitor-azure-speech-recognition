# Capacitor Speech Recognition Plugin Combined With Azure SDK

Capacitor community plugin for speech recognition combined with Azure SDK

## Installation

To use npm

```bash
npm install @trieutulong/speech-recognition
```

Sync native files

```bash
npx cap sync
```

iOS Platform: No further action required.

Android Platform: Register the plugin in your main activity:

```java
import com.getcapacitor.community.speechrecognition.SpeechRecognition;

public class MainActivity extends BridgeActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    registerPlugin(SpeechRecognition.class);
  }
}

```

## Configuration

No configuration required for this plugin

## Supported methods

| Name              | Android | iOS | Web |
| :---------------- | :------ | :-- | :-- |
| start             | ✅      | ✅  | ❌  |
| hasPermission     | ✅      | ✅  | ❌  |
| requestPermission | ✅      | ✅  | ❌  |
