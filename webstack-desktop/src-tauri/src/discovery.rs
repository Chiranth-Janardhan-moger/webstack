use serde::{Deserialize, Serialize};
use std::net::{SocketAddr, UdpSocket};
use std::time::Duration;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DiscoveredServer {
    pub ip: String,
    pub port: u16,
    pub url: String,
    pub name: Option<String>,
}

pub async fn probe_network() -> Option<DiscoveredServer> {
    // Attempt UDP broadcast discovery probe on port 8080
    let broadcast_addr: SocketAddr = match "255.255.255.255:8080".parse() {
        Ok(addr) => addr,
        Err(_) => return None,
    };
    let bind_addr: SocketAddr = match "0.0.0.0:0".parse() {
        Ok(addr) => addr,
        Err(_) => return None,
    };

    if let Ok(socket) = UdpSocket::bind(bind_addr) {
        let _ = socket.set_broadcast(true);
        let _ = socket.set_read_timeout(Some(Duration::from_millis(800)));
        let ping = b"WEBSTACK_DISCOVER_PROBE";
        let _ = socket.send_to(ping, broadcast_addr);

        let mut buf = [0u8; 512];
        if let Ok((len, src)) = socket.recv_from(&mut buf) {
            let msg = String::from_utf8_lossy(&buf[..len]);
            if msg.contains("WEBSTACK") {
                let ip = src.ip().to_string();
                return Some(DiscoveredServer {
                    ip: ip.clone(),
                    port: src.port(),
                    url: format!("http://{}:{}", ip, src.port()),
                    name: Some("WebStack Phone".to_string()),
                });
            }
        }
    }

    None
}
