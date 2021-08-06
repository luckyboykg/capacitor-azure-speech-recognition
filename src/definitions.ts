export interface SpeechRecognitionPlugin {
  start(options: UtteranceOptions): Promise<{ result: number }>;
  stop(): Promise<void>;
  hasPermission(): Promise<{ permission: boolean }>;
  requestPermission(): Promise<void>;
}

export interface UtteranceOptions {
  language: string;
  referenceText: string;
  subscription: string;
  region: string;
}
