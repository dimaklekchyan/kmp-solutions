import Foundation
import UIKit
import PhotosUI

@objc public class NativeImagePreviewProvider: NSObject {

    @objc public func provide(
        itemProvider: NSItemProvider,
        onImage: @escaping (UIImage?) -> Void
    ){
        if itemProvider.canLoadObject(ofClass: UIImage.self) {
            itemProvider.loadObject(ofClass: UIImage.self) { (object, error) in
                DispatchQueue.main.async {
                    if let image = object as? UIImage {
                        if let resizedImage = self.resize(image: image) {
                            onImage(resizedImage)
                        } else {
                            onImage(nil)
                        }

                    } else {
                        onImage(nil)
                    }
                }
            }
        } else {
            onImage(nil)
        }
    }

    private func resize(
        image: UIImage,
        maxSize: CGFloat = 1500
    ) -> UIImage? {
        let originalSize = image.size
        if originalSize.width <= maxSize && originalSize.height <= maxSize {
            return image
        }

        let aspectRatio = originalSize.width / originalSize.height
        var newSize: CGSize
        if originalSize.width > originalSize.height {
            newSize = CGSize(width: maxSize, height: maxSize / aspectRatio)
        } else {
            newSize = CGSize(width: maxSize * aspectRatio, height: maxSize)
        }

        let renderer = UIGraphicsImageRenderer(size: newSize)
        let resizedImage = renderer.image { _ in
            image.draw(in: CGRect(origin: .zero, size: newSize))
        }

        return resizedImage
    }
}

