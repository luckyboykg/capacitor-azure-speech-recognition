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
        
        let audioConfig = SPXAudioConfiguration()
        
        let reco = try! SPXSpeechRecognizer(speechConfiguration: speechConfig!, audioConfiguration: audioConfig)
        
        reco.addRecognizingEventHandler() {reco, evt in
            print("intermediate recognition result: \(evt.result.text ?? "(no result)")")
        }
        
        let result = try! reco.recognizeOnce()
        call.resolve(["result": result.text ?? ""])
    }
}
