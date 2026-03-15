import SwiftUI
import PhotosUI
import AVFoundation
import UniformTypeIdentifiers

struct ChatInputBar: View {
    @Binding var text: String
    @Binding var selectedMode: ThinkingMode
    var onSend: ((String, [MessageAttachment]?) -> Void)?
    var onInputFocus: (() -> Void)?
    var onVoiceInput: ((String) -> Void)?
    var onAudioRecorded: ((Data) -> Void)?
    @State private var attachmentActive = false
    @State private var attachmentType: AttachmentType = .image
    @State private var selectedItem: PhotosPickerItem?
    @State private var loadedImageData: Data?
    @State private var loadedFileName: String?
    @State private var showDocumentPicker = false
    @State private var showCamera = false
    @State private var isRecording = false
    @State private var audioRecorder: AVAudioRecorder?
    @State private var recordingURL: URL?
    @FocusState private var inputFocused: Bool

    var body: some View {
        VStack(spacing: 0) {
            Color.tradeBorder.frame(height: 1)

            VStack(spacing: 10) {
                ModeSelector(selectedMode: $selectedMode)
                    .padding(.horizontal, 12)
                    .padding(.top, 10)

                HStack(spacing: 0) {
                    if attachmentActive {
                        Button {
                            withAnimation(.spring(duration: 0.3)) {
                                attachmentActive = false
                                attachmentType = .image
                                selectedItem = nil
                                loadedImageData = nil
                                loadedFileName = nil
                            }
                        } label: {
                            Image(systemName: "plus")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundStyle(Color.tradeText)
                                .rotationEffect(.degrees(45))
                                .frame(width: 30, height: 30)
                                .background(Color.tradeInput)
                                .clipShape(Circle())
                        }
                        .padding(.leading, 12)

                        if attachmentType == .image, let data = loadedImageData, let uiImage = UIImage(data: data) {
                            Image(uiImage: uiImage)
                                .resizable()
                                .scaledToFill()
                                .frame(width: 40, height: 40)
                                .clipShape(.rect(cornerRadius: 8))
                        } else if attachmentType == .document {
                            VStack(spacing: 2) {
                                Image(systemName: "doc.fill")
                                    .font(.system(size: 16))
                                    .foregroundStyle(Color.tradeTextSecondary)
                                Text(loadedFileName ?? "File")
                                    .font(.system(size: 8))
                                    .foregroundStyle(Color.tradeTextSecondary)
                                    .lineLimit(1)
                            }
                            .frame(width: 40, height: 40)
                            .background(Color.tradeInput)
                            .clipShape(.rect(cornerRadius: 8))
                        }
                    } else {
                        Menu {
                            Button {
                                showCamera = true
                            } label: {
                                Label("Take Photo", systemImage: "camera")
                            }
                            PhotosPicker(selection: $selectedItem, matching: .any(of: [.images, .videos])) {
                                Label("Photo Library", systemImage: "photo")
                            }
                            Button {
                                showDocumentPicker = true
                            } label: {
                                Label("Browse Files", systemImage: "doc")
                            }
                        } label: {
                            Image(systemName: "plus")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundStyle(Color.tradeText)
                                .frame(width: 30, height: 30)
                                .background(Color.tradeInput)
                                .clipShape(Circle())
                        }
                        .accessibilityLabel("Attach file")
                        .padding(.leading, 12)
                        .onChange(of: selectedItem) { _, newItem in
                            if newItem != nil {
                                withAnimation(.spring(duration: 0.3)) {
                                    attachmentActive = true
                                    attachmentType = .image
                                }
                                Task {
                                    if let data = try? await newItem?.loadTransferable(type: Data.self) {
                                        loadedImageData = data
                                        loadedFileName = "photo.jpg"
                                    }
                                }
                            }
                        }
                        .fileImporter(
                            isPresented: $showDocumentPicker,
                            allowedContentTypes: [.pdf, .plainText, .data],
                            allowsMultipleSelection: false
                        ) { result in
                            if case .success(let urls) = result, let url = urls.first {
                                guard url.startAccessingSecurityScopedResource() else { return }
                                defer { url.stopAccessingSecurityScopedResource() }
                                if let data = try? Data(contentsOf: url) {
                                    loadedImageData = data
                                    loadedFileName = url.lastPathComponent
                                    attachmentType = .document
                                    withAnimation(.spring(duration: 0.3)) {
                                        attachmentActive = true
                                    }
                                }
                            }
                        }
                        .fullScreenCover(isPresented: $showCamera) {
                            CameraView(
                                onCapture: { data in
                                    loadedImageData = data
                                    loadedFileName = "photo.jpg"
                                    attachmentType = .image
                                    withAnimation(.spring(duration: 0.3)) {
                                        attachmentActive = true
                                    }
                                    showCamera = false
                                },
                                onClose: {
                                    showCamera = false
                                }
                            )
                        }
                    }

                    TextField("Ask TradeGuru", text: $text, axis: .vertical)
                        .focused($inputFocused)
                        .font(.system(size: 16))
                        .lineLimit(1...5)
                        .padding(10)
                        .background(Color.tradeLight)
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(Color.tradeBorder, lineWidth: 0.5)
                        )
                        .clipShape(.rect(cornerRadius: 20))
                        .padding(.horizontal, 10)
                        .onChange(of: inputFocused) { _, focused in
                            if focused { onInputFocus?() }
                        }

                    if !text.isEmpty || attachmentActive {
                        Button {
                            if let fileData = loadedImageData {
                                let attachment = MessageAttachment(
                                    type: attachmentType,
                                    fileName: loadedFileName ?? (attachmentType == .image ? "photo.jpg" : "file"),
                                    fileSize: fileData.count,
                                    thumbnailData: fileData
                                )
                                onSend?(text, [attachment])
                            } else {
                                onSend?(text, nil)
                            }
                            text = ""
                            attachmentActive = false
                            attachmentType = .image
                            selectedItem = nil
                            loadedImageData = nil
                            loadedFileName = nil
                        } label: {
                            Image(systemName: "arrow.up.circle.fill")
                                .font(.system(size: 28))
                                .foregroundStyle(Color.tradeGreen)
                        }
                        .accessibilityLabel("Send message")
                        .padding(.trailing, 12)
                        .transition(.scale.combined(with: .opacity))
                    } else if !isRecording {
                        Button {
                            startRecording()
                        } label: {
                            Image(systemName: "mic.fill")
                                .font(.system(size: 28))
                                .foregroundStyle(Color.tradeGreen)
                        }
                        .frame(minWidth: 44, minHeight: 44)
                        .accessibilityLabel("Record voice message")
                        .padding(.trailing, 12)
                        .transition(.scale.combined(with: .opacity))
                    } else {
                        Button {
                            stopRecording()
                        } label: {
                            ZStack {
                                Circle()
                                    .fill(Color.red.opacity(0.2))
                                    .frame(width: 36, height: 36)
                                Image(systemName: "stop.fill")
                                    .font(.system(size: 14))
                                    .foregroundStyle(.red)
                            }
                        }
                        .frame(minWidth: 44, minHeight: 44)
                        .accessibilityLabel("Stop recording")
                        .padding(.trailing, 12)
                        .transition(.scale.combined(with: .opacity))
                    }
                }
                .padding(.bottom, 10)
            }
            .background(.ultraThinMaterial)
        }
    }
    private func startRecording() {
        let url = FileManager.default.temporaryDirectory.appendingPathComponent("voice_\(UUID().uuidString).m4a")
        let settings: [String: Any] = [
            AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
            AVSampleRateKey: 44100,
            AVNumberOfChannelsKey: 1,
            AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue
        ]
        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(.record, mode: .default)
            try session.setActive(true)
            let recorder = try AVAudioRecorder(url: url, settings: settings)
            recorder.record()
            audioRecorder = recorder
            recordingURL = url
            isRecording = true
        } catch {
            isRecording = false
        }
    }

    private func stopRecording() {
        audioRecorder?.stop()
        isRecording = false
        guard let url = recordingURL, let data = try? Data(contentsOf: url) else { return }
        onAudioRecorded?(data)
        try? FileManager.default.removeItem(at: url)
        audioRecorder = nil
        recordingURL = nil
    }
}

#Preview {
    ChatInputBar(text: .constant(""), selectedMode: .constant(.faultFinder))
}
