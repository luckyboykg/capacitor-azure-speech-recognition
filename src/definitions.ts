export type CallbackID = string;

export type SpeechRecognitionCallback = (
  data: SpeechRecognitionResult | null,
  err?: any
) => void;
export interface SpeechRecognitionResult {
  pronunciationScore: number;
  isStarting: boolean;
}
export interface SpeechRecognitionPlugin {
  start(
    options: SpeechRecognitionOptions,
    callback: SpeechRecognitionCallback
  ): Promise<CallbackID>;
  hasPermission(): Promise<{ permission: boolean }>;
  requestPermission(): Promise<void>;
}

export interface SpeechRecognitionOptions {
  language: string;
  referenceText: string;
  subscription: string;
  region: string;
}
