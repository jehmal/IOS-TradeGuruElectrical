import SwiftUI
@preconcurrency import AVFoundation

struct CameraView: View {
    let onCapture: (Data) -> Void
    let onClose: () -> Void

    @State private var flashOn = false
    @State private var cameraAvailable = false
    @State private var controller: CameraController?

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            if cameraAvailable, let controller {
                CameraPreviewView(session: controller.session)
                    .ignoresSafeArea()
            } else {
                VStack(spacing: 16) {
                    Image(systemName: "camera.fill")
                        .font(.system(size: 48))
                        .foregroundStyle(.white.opacity(0.3))
                    Text("Camera not available")
                        .font(.system(size: 17, weight: .medium))
                        .foregroundStyle(.white.opacity(0.5))
                }
            }

            VStack {
                HStack {
                    Spacer()

                    Button {
                        onClose()
                    } label: {
                        Image(systemName: "xmark")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundStyle(.white)
                            .frame(width: 36, height: 36)
                            .background(.ultraThinMaterial)
                            .clipShape(Circle())
                    }
                    .accessibilityLabel("Close camera")
                    .padding(.trailing, 16)
                    .padding(.top, 16)
                }

                Spacer()

                HStack {
                    Button {
                        flashOn.toggle()
                        controller?.toggleFlash(flashOn)
                    } label: {
                        Image(systemName: flashOn ? "bolt.fill" : "bolt.slash.fill")
                            .font(.system(size: 22))
                            .foregroundStyle(flashOn ? .yellow : .white)
                            .frame(width: 44, height: 44)
                    }
                    .accessibilityLabel(flashOn ? "Flash on" : "Flash off")

                    Spacer()

                    Button {
                        controller?.capturePhoto { data in
                            onCapture(data)
                        }
                    } label: {
                        ZStack {
                            Circle()
                                .fill(.white)
                                .frame(width: 70, height: 70)
                            Circle()
                                .fill(.white)
                                .frame(width: 64, height: 64)
                                .overlay(
                                    Circle()
                                        .stroke(Color.black.opacity(0.2), lineWidth: 2)
                                )
                        }
                    }
                    .accessibilityLabel("Take photo")

                    Spacer()

                    Color.clear.frame(width: 44, height: 44)
                }
                .padding(.horizontal, 32)
                .padding(.bottom, 40)
            }
        }
        .onAppear {
            checkCameraAndStart()
        }
        .onDisappear {
            controller?.stop()
        }
    }

    private func checkCameraAndStart() {
        guard AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back) != nil else {
            cameraAvailable = false
            return
        }
        AVCaptureDevice.requestAccess(for: .video) { granted in
            Task { @MainActor in
                if granted {
                    let ctrl = CameraController()
                    ctrl.start()
                    controller = ctrl
                    cameraAvailable = true
                }
            }
        }
    }
}

@MainActor
class CameraController: NSObject, AVCapturePhotoCaptureDelegate {
    let session = AVCaptureSession()
    private let photoOutput = AVCapturePhotoOutput()
    private var captureCompletion: ((Data) -> Void)?

    func start() {
        session.beginConfiguration()
        session.sessionPreset = .photo

        guard let device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back),
              let input = try? AVCaptureDeviceInput(device: device) else {
            session.commitConfiguration()
            return
        }

        if session.canAddInput(input) {
            session.addInput(input)
        }
        if session.canAddOutput(photoOutput) {
            session.addOutput(photoOutput)
        }

        session.commitConfiguration()

        let captureSession = session
        Task.detached {
            captureSession.startRunning()
        }
    }

    func stop() {
        let captureSession = session
        Task.detached {
            captureSession.stopRunning()
        }
    }

    func toggleFlash(_ on: Bool) {
        guard let device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back),
              device.hasTorch else { return }
        try? device.lockForConfiguration()
        device.torchMode = on ? .on : .off
        device.unlockForConfiguration()
    }

    func capturePhoto(completion: @escaping (Data) -> Void) {
        captureCompletion = completion
        let settings = AVCapturePhotoSettings()
        photoOutput.capturePhoto(with: settings, delegate: self)
    }

    nonisolated func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
        guard let data = photo.fileDataRepresentation() else { return }
        Task { @MainActor [weak self] in
            self?.captureCompletion?(data)
        }
    }
}

struct CameraPreviewView: UIViewRepresentable {
    let session: AVCaptureSession

    func makeUIView(context: Context) -> UIView {
        let view = UIView()
        let previewLayer = AVCaptureVideoPreviewLayer(session: session)
        previewLayer.videoGravity = .resizeAspectFill
        view.layer.addSublayer(previewLayer)
        context.coordinator.previewLayer = previewLayer
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        context.coordinator.previewLayer?.frame = uiView.bounds
    }

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    class Coordinator {
        var previewLayer: AVCaptureVideoPreviewLayer?
    }
}

#Preview {
    CameraView(onCapture: { _ in }, onClose: {})
}
