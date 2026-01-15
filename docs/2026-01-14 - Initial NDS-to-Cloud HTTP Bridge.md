# AetherShell Developer Log 01

## Project Phase: The Initial Bridge (NDS -> Ngrok -> Flask)
**Status:** Success (Bi-directional communication established)

### Networking Context & Constraints
- **Hardware:** Nintendo DS (ARM9)
- **Infrastructure:** ngrok Free Tier (Tunneling) + Local Flask Server (Python 3.10)
- **Network Protocol:** HTTP/1.1 over Plaintext TCP Sockets.
- **Physical Layer:** Open WiFi Network. (DS hardware lacks WPA2/3 support; WEP was unavailable, so a non-encrypted Access Point was used for initial testing).

---

### Technical Implementation Details

#### 1. The "Ngrok Interstitial" Bypass
**Problem:** Modern tunneling services like ngrok serve a "Browser Warning" page to prevent phishing. Because the DS uses raw sockets, it cannot execute JavaScript or click "Accept," causing the connection to hang on the HTML response.
**Solution:** Injected a custom HTTP header `ngrok-skip-browser-warning: 1` into the raw request buffer in `main.cpp`.
**Result:** Direct access to the API endpoint without the interstitial interference.

#### 2. The Port Mapping Strategy
- **Local Machine:** The Flask API was configured to listen on `port 8080`.
- **Tunneling:** Ngrok was instructed to tunnel traffic from its public edge to `localhost:8080`.
- **DS Client:** The `SERVER_PORT` macro was set to `80`.
**Logic:** Even though Flask is at 8080, the DS communicates with ngrok's public URL via the standard HTTP Port 80. Ngrok then routes that traffic down to our local 8080.

#### 3. Low-Level Hardware Integration (devkitPro/libnds)
To achieve this bridge, we utilized the following stacks:
- **`libdswifi9.a`:** The core NDS WiFi library. We used `Wifi_InitDefault(true)` to pull connection settings directly from the DS firmware's WFC (WiFi Connection) memory.
- **`dswifi9.h`:** Provided the state machine for `Wifi_AssocStatus()`, allowing the program to wait for a successful handshake with the Access Point.
- **BSD Sockets (`sys/socket.h` & `netdb.h`):** Used standard POSIX-style networking. `gethostbyname()` was critical for resolving the dynamic ngrok hostname to a temporary IP address that the DS could understand.

---

### Trial & Error Notes
- **DNS Resolution:** Initially, DNS resolution was a failure point. Ensuring `Wifi_InitDefault` was fully associated before calling `gethostbyname` was the fix.
- **Payload Limits:** Discovered that large HTTP responses can overflow the small 1024-byte buffer. Responses must stay lightweight (plaintext is best).
- **CRLF Necessity:** The HTTP protocol requires strict `\r\n` (Carriage Return + Line Feed) endings for headers. Standard `\n` caused 400 Bad Request errors on the Flask side.

---

### Milestones Achieved
- [x] Initialized NDS WiFi Stack.
- [x] Resolved public DNS via NDS.
- [x] Successfully bypassed ngrok security walls via custom headers.
- [x] Received plaintext response from Python backend on the NDS console.