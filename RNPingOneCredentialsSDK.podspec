require 'json'
package = JSON.parse(File.read('./package.json'))

Pod::Spec.new do |s|
  s.name         = 'RNPingOneCredentialsSDK'
  s.dependency 'React-Core'
  
  s.version         = package["version"]
  s.license         = package["license"]
  s.summary         = package["description"]
  s.authors         = package["author"]
  s.homepage        = package["homepage"]
  
  s.platform     = :ios, '13.0'
  
  s.source          = { :git => package["repository"]["url"], :tag => s.version }
  s.source_files  = 'ios/*.{h,m,swift}'

  s.vendored_frameworks = "ios/SDK/AnyCoder.xcframework", "ios/SDK/CommunicationManager.xcframework", "ios/SDK/CryptoTools.xcframework", "ios/SDK/DIDSDK.xcframework", "ios/SDK/JOSESwift.xcframework", "ios/SDK/JoseTools.xcframework", "ios/SDK/libsecp256k1.xcframework", "ios/SDK/PingOneWallet.xcframework", "ios/SDK/secp256k1Wrapper.xcframework"
end
