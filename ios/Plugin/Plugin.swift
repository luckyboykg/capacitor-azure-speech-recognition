import Capacitor
import Speech

@objc(SpeechRecognition)
public class SpeechRecognition: CAPPlugin {
    
    let DEFAULT_LANGUAGE = "en-US"
    let MESSAGE_ACCESS_DENIED_MICROPHONE = "User denied access to microphone"
    
    @objc func start(_ call: CAPPluginCall) {
        AVAudioSession.sharedInstance().requestRecordPermission { (granted) in
            if !granted {
                call.reject(self.MESSAGE_ACCESS_DENIED_MICROPHONE)
                return
            }
            
            DispatchQueue.global(qos: DispatchQoS.QoSClass.default).async {
                self.recognizeFromMic(call)
            }
        }
    }
    
    @objc func hasPermission(_ call: CAPPluginCall) {
        AVAudioSession.sharedInstance().requestRecordPermission { (granted: Bool) in
            call.resolve([
                "permission": granted
            ])
        }
    }
    
    @objc func requestPermission(_ call: CAPPluginCall) {
        AVAudioSession.sharedInstance().requestRecordPermission { (granted: Bool) in
            if (granted) {
                call.resolve()
            } else {
                call.reject(self.MESSAGE_ACCESS_DENIED_MICROPHONE)
            }
        }
    }
    
    func recognizeFromMic(_ call: CAPPluginCall) {
        call.keepAlive = true
        
        var speechConfig: SPXSpeechConfiguration?
        do {
            try speechConfig = SPXSpeechConfiguration(subscription: call.getString("subscription") ?? "", region: call.getString("region") ?? "")
        } catch {
            speechConfig = nil
        }
        
        let language: String = call.getString("language") ?? self.DEFAULT_LANGUAGE
        speechConfig?.speechRecognitionLanguage = language
        speechConfig?.setPropertyTo("3000", byName: "3201")
        
        let referenceText = call.getString("referenceText") ?? ""
        
        let audioConfig = SPXAudioConfiguration()
        let pronunciationAssessmentConfig = try! SPXPronunciationAssessmentConfiguration(referenceText,gradingSystem: SPXPronunciationAssessmentGradingSystem.hundredMark,granularity: SPXPronunciationAssessmentGranularity.word)
        
        let recognizer = try! SPXSpeechRecognizer(speechConfiguration: speechConfig!, audioConfiguration: audioConfig)
        
        // apply the pronunciation assessment configuration to the speech recognizer
        try! pronunciationAssessmentConfig.apply(to: recognizer)
        
        recognizer.addRecognizingEventHandler() {_recognizer, event in
            if(event.result.text?.lowercased() == referenceText.lowercased()){
                call.resolve(["isStarting": true, "pronunciationScore":0])
            }
        }
        
        recognizer.addRecognizedEventHandler { _recognizer, event in
            let pronunciationAssessmentResult = SPXPronunciationAssessmentResult(event.result)
            call.resolve(["isStarting": false, "pronunciationScore": pronunciationAssessmentResult?.pronunciationScore ?? 0])
            call.keepAlive = false;
        }
        
        try! recognizer.recognizeOnce()
    }
}
