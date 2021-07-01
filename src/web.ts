import { WebPlugin } from "@capacitor/core";
export class SpeechRecognitionWeb extends WebPlugin {
  constructor() {
    super({
      name: "SpeechRecognition",
      platforms: ["web"],
    });
  }
  available() {
    throw new Error("Method not implemented.");
  }
  start(_options: any) {
    throw new Error("Method not implemented.");
  }
  stop() {
    throw new Error("Method not implemented.");
  }
  getSupportedLanguages() {
    throw new Error("Method not implemented.");
  }
  hasPermission() {
    throw new Error("Method not implemented.");
  }
  requestPermission() {
    throw new Error("Method not implemented.");
  }
}
