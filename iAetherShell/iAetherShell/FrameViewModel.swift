//
//  FrameViewModel.swift
//  iAetherShell
//
//  Created by Bhaskar Das on 2026-01-15.
//

import SwiftUI
import Combine

class FrameViewModel: ObservableObject {
    @Published var frames: [UIImage] = []
    @Published var isLoading = true
    @Published var aiDescription = "Waiting for AI..."
    
    private var timer: Timer?

    func startPolling(baseURL: String, sessionID: String) {
        isLoading = true
        // Check every 2 seconds
        timer = Timer.scheduledTimer(withTimeInterval: 2.0, repeats: true) { _ in
            self.checkStatus(baseURL: baseURL, sessionID: sessionID)
        }
    }

    private func checkStatus(baseURL: String, sessionID: String) {
        let urlString = "\(baseURL)/session/results/\(sessionID)"
        guard let url = URL(string: urlString) else { return }

        URLSession.shared.dataTask(with: url) { data, _, _ in
            guard let data = data else { return }
            
            if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
                // Check if Java has finished processing
                if let status = json["status"] as? String, status == "success" {
                    self.timer?.invalidate() // Stop polling
                    
                    let imageNames = json["images"] as? [String] ?? []
                    let description = json["ai_description"] as? String ?? "Unknown"
                    
                    DispatchQueue.main.async {
                        self.aiDescription = description
                        self.downloadAllImages(baseURL: baseURL, names: imageNames)
                    }
                }
            }
        }.resume()
    }

    private func downloadAllImages(baseURL: String, names: [String]) {
        let group = DispatchGroup()
        var tempImages = [UIImage]()

        for name in names {
            // Path: BaseURL + /output/ + filename
            let imageURLString = "\(baseURL)/output/\(name)"
            guard let url = URL(string: imageURLString) else { continue }
            
            group.enter()
            URLSession.shared.dataTask(with: url) { data, _, _ in
                if let data = data, let image = UIImage(data: data) {
                    tempImages.append(image)
                }
                group.leave()
            }.resume()
        }

        group.notify(queue: .main) {
            self.frames = tempImages
            self.isLoading = false // THIS hides the "Loading" screen
        }
    }
}
