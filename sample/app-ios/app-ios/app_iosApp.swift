
import SwiftUI
import Shared

 @main
 struct app_iosApp: App {
     var body: some Scene {
         WindowGroup {
             ComposeViewControllerWrapper()
         }
     }
 }

struct ComposeViewControllerWrapper: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainKt.SampleViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}