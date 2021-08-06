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
    
    @objc func stop(_ call: CAPPluginCall) {
        DispatchQueue.global(qos: DispatchQoS.QoSClass.default).async {
            call.resolve()
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
        var speechConfig: SPXSpeechConfiguration?
        do {
            try speechConfig = SPXSpeechConfiguration(subscription: call.getString("subscription") ?? "", region: call.getString("region") ?? "")
        } catch {
            speechConfig = nil
        }
        
        let language: String = call.getString("language") ?? self.DEFAULT_LANGUAGE
        speechConfig?.speechRecognitionLanguage = language
        
        let referenceText = call.getString("referenceText") ?? ""
        
        let audioConfig = SPXAudioConfiguration()
        let pronunciationAssessmentConfig = try! SPXPronunciationAssessmentConfiguration(referenceText,gradingSystem: SPXPronunciationAssessmentGradingSystem.hundredMark,granularity: SPXPronunciationAssessmentGranularity.word)
        
        let reco = try! SPXSpeechRecognizer(speechConfiguration: speechConfig!, audioConfiguration: audioConfig)
        
        // apply the pronunciation assessment configuration to the speech recognizer
        try! pronunciationAssessmentConfig.apply(to: reco)
        
        let result = try! reco.recognizeOnce()
        let pronunciationAssessmentResult = SPXPronunciationAssessmentResult(result)
        call.resolve(["result": pronunciationAssessmentResult?.pronunciationScore ?? 0])
    }
}
