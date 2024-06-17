# TLSSample
An application for testing a secure connection.

---

- [Transport Layer Security](https://en.wikipedia.org/wiki/Transport_Layer_Security)
- [Diffie–Hellman key exchange](https://en.wikipedia.org/wiki/Diffie%E2%80%93Hellman_key_exchange)

---

### backlog

- [x] post double
- [x] RSA
- [x] RSA + AES
- [ ] RSA + AES + MAC

---

PKT - Public Key Transmitter
PKR - Public Key Receiver
SK - Secret Key
SID - Session ID
P - Payload
M - Message

PKT -->
<-- PKR/PKT(SK)/SK(SID)/SIG(P)

SIG(SID+P)/SK(M) -->
<-- SIG(SID+P)/SK(f(M))

---
