import { WebPlugin } from "@capacitor/core";
import {
  CallbackID,
  SpeechRecognitionCallback,
  SpeechRecognitionOptions,
  SpeechRecognitionPlugin,
} from "./definitions";
export class SpeechRecognitionWeb
  extends WebPlugin
  implements SpeechRecognitionPlugin {
  constructor() {
    super({
      name: "SpeechRecognition",
      platforms: ["web"],
    });
  }
  start(
    _options: SpeechRecognitionOptions,
    _callback: SpeechRecognitionCallback
  ): Promise<CallbackID> {
    throw new Error("Method not implemented.");
  }
  hasPermission(): Promise<{ permission: boolean }> {
    throw new Error("Method not implemented.");
  }
  requestPermission(): Promise<void> {
    throw new Error("Method not implemented.");
  }
}
