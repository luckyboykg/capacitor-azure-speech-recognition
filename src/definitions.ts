export interface SpeechRecognitionPlugin {
  start(options: UtteranceOptions): Promise<{ result: string }>;
  stop(): Promise<void>;
  hasPermission(): Promise<{ permission: boolean }>;
  requestPermission(): Promise<void>;
}

export interface UtteranceOptions {
  language: string;
  subscription: string;
  region: string;
}
