//
//  FrameViewerView.swift
//  iAetherShell
//
//  Created by Bhaskar Das on 2026-01-15.
//


import SwiftUI

struct FrameViewerView: View {
    let baseURL: String
    let sessionID: String
    @State private var frames: [UIImage] = []
    @State private var currentIndex = 0
    @State private var timer: Timer?

    var body: some View {
        VStack {
            if frames.isEmpty {
                Text("Loading frames...")
                    .font(.headline)
                    .padding()
            } else {
                GeometryReader { geo in
                    Image(uiImage: frames[currentIndex])
                        .resizable()
                        .scaledToFit()
                        .frame(width: geo.size.width, height: geo.size.height)
                        .background(Color.clear)
                }
            }
        }
        .onAppear {
            loadFrames()
        }
        .onDisappear {
            timer?.invalidate()
        }
        .navigationTitle("Session \(sessionID)")
        .navigationBarTitleDisplayMode(.inline)
    }

    func loadFrames() {
        let maxFrames = 50 // adjust depending on session
        var loadedFrames = 0

        for i in 0..<maxFrames {
            let urlStr = "\(baseURL)/output/\(sessionID)_frame_\(i).png"
            guard let url = URL(string: urlStr) else { continue }

            URLSession.shared.dataTask(with: url) { data, response, error in
                if let data = data, let img = UIImage(data: data) {
                    DispatchQueue.main.async {
                        frames.append(img)
                        loadedFrames += 1

                        // Start looping timer once first frame loads
                        if loadedFrames == 1 {
                            startLooping()
                        }
                    }
                }
            }.resume()
        }
    }

    func startLooping() {
        timer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { _ in
            withAnimation(.linear(duration: 0.05)) {
                currentIndex = (currentIndex + 1) % frames.count
            }
        }
    }
}

