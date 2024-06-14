# TLSSample
An application for testing a secure connection.

---

- [Transport Layer Security](https://en.wikipedia.org/wiki/Transport_Layer_Security)
- [Diffie–Hellman key exchange](https://en.wikipedia.org/wiki/Diffie%E2%80%93Hellman_key_exchange)

---

### backlog

- [x] post double
- [x] RSA
- [ ] RSA + AES

---

PKT - Public Key Transmitter
SK - Secret Key
SID - Session ID
M - Message

PKT --> PKT
SK, SID <-- PKT(SK) + SK(SID)

SK(SID, M) --> M
f(M) <-- SK(f(M))

---
