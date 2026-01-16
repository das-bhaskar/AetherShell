//
//  ContentView.swift
//  iAetherShell
//
//  Created by Bhaskar Das on 2026-01-15.
//
import SwiftUI

struct ConnectView: View {
    @State private var baseURL: String = ""
    @State private var sessionID: String = ""
    @State private var isConnected: Bool = false

    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                Text("AetherShell Viewer")
                    .font(.largeTitle)
                    .bold()

                TextField("Cloudflare Base URL", text: $baseURL)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .autocapitalization(.none)
                    .disableAutocorrection(true)
                    .padding(.horizontal)

                TextField("Session ID", text: $sessionID)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .autocapitalization(.none)
                    .disableAutocorrection(true)
                    .padding(.horizontal)

                Button(action: {
                    guard !baseURL.isEmpty, !sessionID.isEmpty else { return }
                    isConnected = true
                }) {
                    Text("Load Frames")
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                        .padding(.horizontal)
                }

                Spacer()
            }
            .navigationTitle("Connect")
            .background(
                NavigationLink(
                    destination: FrameViewerView(baseURL: baseURL, sessionID: sessionID),
                    isActive: $isConnected,
                    label: { EmptyView() }
                )
            )
        }
    }
}
