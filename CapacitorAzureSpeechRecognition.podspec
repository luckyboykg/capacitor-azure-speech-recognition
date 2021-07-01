
  Pod::Spec.new do |s|
    s.name = 'CapacitorAzureSpeechRecognition'
    s.version = '0.0.2'
    s.summary = 'Capacitor community plugin for speech recognition combined with Azure SDK'
    s.license = 'MIT'
    s.homepage = 'https://github.com/luckyboykg/capacitor-azure-speech-recognition'
    s.author = 'https://github.com/luckyboykg'
    s.source = { :git => 'https://github.com/luckyboykg/capacitor-azure-speech-recognition', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '12.0'
    s.dependency 'Capacitor'
  end