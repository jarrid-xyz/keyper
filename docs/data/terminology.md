---
title: Terminology
---

## Cryptography Algorithms

### AES (Advanced Encryption Standard)
[AES](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard){:target="_blank"} is a [symmetric encryption](#symmetric-encryption) algorithm widely used for securing data. It encrypts data using a single key for both encryption and decryption. While AES itself is deterministic, it is often used in modes of operation that introduce randomness to make the encryption non-deterministic. AES is known for its efficiency and security.

### RSA (Rivest-Shamir-Adleman)
[RSA](https://en.wikipedia.org/wiki/RSA_(cryptosystem)){:target="_blank"} is an [asymmetric encryption](#asymmetric-encryption) algorithm used for secure data transmission. It uses a pair of keys: a [public key](#public-private-key-pairs) for encryption and a [private key](#public-private-key-pairs) for decryption. RSA is widely used for securing data transmission and digital signatures.

### Symmetric Encryption
[Symmetric encryption](https://en.wikipedia.org/wiki/Symmetric-key_algorithm){:target="_blank"} uses the same key for both encryption and decryption. It is efficient and suitable for encrypting large amounts of data. Examples include [AES](#aes-advanced-encryption-standard) and [DES](https://en.wikipedia.org/wiki/Data_Encryption_Standard){:target="_blank"}.

### Asymmetric Encryption
[Asymmetric encryption](https://en.wikipedia.org/wiki/Public-key_cryptography){:target="_blank"} uses a pair of keys: a [public key](#public-private-key-pairs) for encryption and a [private key](#public-private-key-pairs) for decryption. It provides enhanced security features like data confidentiality, integrity, authenticity, and non-repudiation. Examples include [RSA](#rsa-rivest-shamir-adleman) and [ECC](https://en.wikipedia.org/wiki/Elliptic-curve_cryptography){:target="_blank"}.

#### Public-Private Key Pairs
In [asymmetric encryption](#asymmetric-encryption), a public key is used to encrypt data, while a private key is used to decrypt it. The public key is shared openly, while the private key is kept secret. This key pair mechanism ensures secure data transmission and authentication.

### Symmetric and Asymmetric Encryption Comparison

| Feature                      | Symmetric Encryption                                                                                                      | Asymmetric Encryption                                                                                                 |
| ---------------------------- | ------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------- |
| **Key Usage**                | Same key for both encryption and decryption                                                                               | [Public key](#public-private-key-pairs) for encryption, [private key](#public-private-key-pairs) for decryption       |
| **Performance**              | Faster and more efficient                                                                                                 | Slower and computationally intensive                                                                                  |
| **Key Management**           | Requires secure sharing and management of the secret key                                                                  | Easier to manage as the public key can be shared openly                                                               |
| **Use Cases**                | Encrypting large amounts of data, secure storage                                                                          | Secure data transmission, digital signatures, key exchange                                                            |
| **Confidentiality**          | Ensures data confidentiality                                                                                              | Ensures data confidentiality                                                                                          |
| **Integrity & Authenticity** | Does not inherently provide integrity and authenticity                                                                    | Provides integrity and authenticity when used for digital signatures                                                  |
| **Algorithm Complexity**     | Relatively simple algorithm                                                                                               | Complex algorithm based on mathematical problems (e.g., factoring large numbers)                                      |
| **Examples**                 | [AES](#aes-advanced-encryption-standard), [DES](https://en.wikipedia.org/wiki/Data_Encryption_Standard){:target="_blank"} | [RSA](#rsa-rivest-shamir-adleman), [ECC](https://en.wikipedia.org/wiki/Elliptic-curve_cryptography){:target="_blank"} |

### AES and RSA Encryption Comparison

| Feature                      | [AES (Advanced Encryption Standard)](#aes-advanced-encryption-standard)  | [RSA (Rivest-Shamir-Adleman)](#rsa-rivest-shamir-adleman)                        |
| ---------------------------- | ------------------------------------------------------------------------ | -------------------------------------------------------------------------------- |
| **Type**                     | [Symmetric Encryption](#symmetric-encryption)                            | [Asymmetric Encryption](#asymmetric-encryption)                                  |
| **Key Usage**                | Same key for both encryption and decryption                              | Public key for encryption, private key for decryption                            |
| **Performance**              | Faster and more efficient, suitable for encrypting large amounts of data | Slower and computationally intensive                                             |
| **Key Length**               | Commonly 128, 192, or 256 bits                                           | Commonly 2048 or 4096 bits                                                       |
| **Security Level**           | High security with shorter key lengths                                   | High security but requires longer key lengths                                    |
| **Encryption Speed**         | Very fast and efficient                                                  | Slower due to complex mathematical operations                                    |
| **Decryption Speed**         | Very fast and efficient                                                  | Slower due to complex mathematical operations                                    |
| **Key Management**           | Requires secure sharing and management of the secret key                 | Easier to manage as the public key can be shared openly                          |
| **Use Cases**                | Encrypting large amounts of data, secure storage                         | Secure data transmission, digital signatures, key exchange                       |
| **Algorithm Complexity**     | Relatively simple algorithm                                              | Complex algorithm based on mathematical problems (e.g., factoring large numbers) |
| **Confidentiality**          | Ensures data confidentiality                                             | Ensures data confidentiality                                                     |
| **Integrity & Authenticity** | Does not inherently provide integrity and authenticity                   | Provides integrity and authenticity when used for digital signatures             |
| **Common Applications**      | Disk encryption, file encryption, VPNs                                   | Secure email, SSL/TLS, digital signatures                                        |

### Deterministic Encryption
[Deterministic encryption](https://en.wikipedia.org/wiki/Deterministic_encryption){:target="_blank"} produces the same ciphertext for any given plaintext and key. This predictability can be useful in certain applications, such as database indexing and searching, but it also makes it vulnerable to certain types of attacks compared to non-deterministic encryption methods. AES itself is deterministic, but when used in modes like CBC, CTR, or GCM, it can achieve non-deterministic encryption.

#### Deterministic AES Encryption for Analytics

For analytics use cases, you can use AES in a mode that does not introduce randomness or use a consistent initialization vector (IV). Common methods include using ECB mode or a fixed IV with CBC mode. For more details, refer to the external [Deterministic encryption](https://en.wikipedia.org/wiki/Deterministic_encryption){:target="_blank"} page.

## Access Control

### Identity and Access Management (IAM)
[Identity and Access Management (IAM)](https://en.wikipedia.org/wiki/Identity_management){:target="_blank"} is a framework of policies and technologies for ensuring that the right individuals have access to the right resources at the right times for the right reasons. It is a crucial part of modern security infrastructure.

## Confidential Computing

### TEE (Trusted Execution Environment)
A [Trusted Execution Environment (TEE)](https://en.wikipedia.org/wiki/Trusted_execution_environment){:target="_blank"} is a secure area of a main processor. It ensures that sensitive data is stored, processed, and protected in an isolated and trusted environment. The TEE protects data from unauthorized access and tampering, providing a higher level of security for sensitive operations.

## Data Integrity Checks

### MAC (Message Authentication Code)
[MAC](https://en.wikipedia.org/wiki/Message_authentication_code){:target="_blank"} is a cryptographic code that provides data integrity and authenticity. It is generated using a secret key and the data to be authenticated. The recipient can verify the MAC to ensure that the data has not been tampered with and is from an authenticated source.

### HMAC (Hash-based Message Authentication Code)
[HMAC](https://en.wikipedia.org/wiki/HMAC){:target="_blank"} is a type of [MAC](#mac-message-authentication-code) that uses a cryptographic hash function along with a secret key to provide data integrity and authenticity. It is commonly used in various secure communication protocols.

### Digital Signatures
[Digital signatures](https://en.wikipedia.org/wiki/Digital_signature){:target="_blank"} use [asymmetric encryption](#asymmetric-encryption) to verify the authenticity and integrity of a message or document. The sender signs the data with their [private key](#public-private-key-pairs), and the recipient verifies the signature using the sender's [public key](#public-private-key-pairs).

## Key Exchange

### Diffie-Hellman (DH)
[Diffie-Hellman](https://en.wikipedia.org/wiki/Diffie%E2%80%93Hellman_key_exchange){:target="_blank"} is a method of securely exchanging cryptographic keys over a public channel. It allows two parties to establish a shared secret key, which can then be used for symmetric encryption.

### ECDSA (Elliptic Curve Digital Signature Algorithm) and ECDH (Elliptic Curve Diffie-Hellman)
[ECDSA](https://en.wikipedia.org/wiki/Elliptic_Curve_Digital_Signature_Algorithm){:target="_blank"} is an elliptic curve implementation of the Digital Signature Algorithm (DSA) that uses the mathematics of elliptic curves to provide a high level of security with smaller key sizes. [ECDH](https://en.wikipedia.org/wiki/Elliptic-curve_Diffie%E2%80%93Hellman){:target="_blank"} is a variant of the Diffie-Hellman key exchange protocol that uses elliptic curve cryptography to establish a shared secret over an insecure channel.

## MAC, Digital Signature, Key Exchange Comparison

| Feature             | MAC                                                                                                      | Digital Signature                                                                                                                                                     | Key Exchange                                                                                                                                                            |
| ------------------- | -------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Purpose**         | Ensures data integrity and authenticity                                                                  | Ensures data integrity, authenticity, and non-repudiation                                                                                                             | Securely exchanges cryptographic keys over a public channel                                                                                                             |
| **Key Type**        | [Symmetric](#symmetric-encryption)                                                                       | [Asymmetric](#asymmetric-encryption)                                                                                                                                  | [Asymmetric](#asymmetric-encryption)                                                                                                                                    |
| **Key Usage**       | Uses a single shared secret key for both generating and verifying the MAC                                | Uses a pair of keys: a private key for signing and a public key for verifying the signature                                                                           | Establishes a shared secret key between two parties                                                                                                                     |
| **Integrity**       | Provides integrity by verifying that the data has not been altered                                       | Provides integrity by verifying that the data has not been altered                                                                                                    | Provides a shared key which can be used with HMAC or digital signatures for integrity                                                                                   |
| **Authenticity**    | Confirms the authenticity of the message if the secret key is known only to the communicating parties    | Confirms the authenticity of the message and the identity of the sender                                                                                               | Does not inherently provide authenticity; additional mechanisms like digital signatures are needed                                                                      |
| **Non-Repudiation** | Does not provide non-repudiation, as both parties share the same secret key                              | Provides non-repudiation, as only the sender has the private key used to generate the signature                                                                       | Does not provide non-repudiation; it is primarily used for key exchange                                                                                                 |
| **Performance**     | Faster and more efficient, suitable for high-performance requirements                                    | Slower due to the computational overhead of asymmetric cryptography                                                                                                   | Efficient for key exchange but involves computational overhead during key establishment                                                                                 |
| **Key Management**  | Requires secure sharing and management of the secret key                                                 | Public key can be shared openly, while the private key must be kept secure                                                                                            | Each party generates their own public/private key pair and shares the public key                                                                                        |
| **Use Cases**       | Used in secure communication protocols (e.g., TLS, IPsec), data integrity checks, and API authentication | Used in digital certificates, secure email (e.g., S/MIME), software distribution, and blockchain transactions                                                         | Used in secure key exchange for protocols like TLS, SSH, and other cryptographic systems                                                                                |
| **Examples**        | HMAC-SHA256, HMAC-SHA1                                                                                   | RSA Digital Signature, [ECDSA (Elliptic Curve Digital Signature Algorithm)](#ecdsa-elliptic-curve-digital-signature-algorithm-and-ecdh-elliptic-curve-diffie-hellman) | [DH key exchange](#diffie-hellman-dh), [ECDH (Elliptic Curve Diffie-Hellman)](#ecdsa-elliptic-curve-digital-signature-algorithm-and-ecdh-elliptic-curve-diffie-hellman) |

## Example: TLS (Transport Layer Security)

The [TLS (Transport Layer Security)](https://en.wikipedia.org/wiki/Transport_Layer_Security){:target="_blank"} protocol is an example of how these cryptographic mechanisms are combined to provide secure communication over a network.

1. **Key Exchange:**

    Asymmetric encryption (e.g., RSA or Diffie-Hellman) is used to securely exchange a symmetric session key between the client and server.

2. **Data Encryption:**

    Once the session key is established, symmetric encryption (e.g., AES) is used for encrypting the data during transmission. This ensures data confidentiality and efficiency.

3. **Data Integrity:**

    HMAC is used to ensure the integrity and authenticity of the transmitted data. Each message includes an HMAC, which the recipient can verify to ensure the data has not been tampered with.

By combining these techniques, TLS ensures that data is encrypted and transmitted securely while also being protected against tampering and ensuring the authenticity of the communication parties. For a detailed explanation of the TLS Handshake, refer to [this link](https://dev.to/techschoolguru/a-complete-overview-of-ssl-tls-and-its-cryptographic-system-36pd#10-tls-13-handshake-protocol){:target="_blank"}.
