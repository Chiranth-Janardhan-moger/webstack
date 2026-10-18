import type { EncryptedPayload } from './types';

export async function deriveKey(pin: string, saltBytes: Uint8Array): Promise<CryptoKey> {
  const enc = new TextEncoder();
  const baseKey = await window.crypto.subtle.importKey(
    'raw',
    enc.encode(pin),
    { name: 'PBKDF2' },
    false,
    ['deriveKey']
  );
  return window.crypto.subtle.deriveKey(
    {
      name: 'PBKDF2',
      salt: saltBytes as BufferSource,
      iterations: 100000,
      hash: 'SHA-256'
    },
    baseKey,
    { name: 'AES-GCM', length: 256 },
    false,
    ['encrypt', 'decrypt']
  );
}

export function getOrCreateSalt(): Uint8Array {
  let saltStr = localStorage.getItem('webstack_vault_salt');
  if (!saltStr) {
    const arr = new Uint8Array(16);
    if (window.crypto && window.crypto.getRandomValues) {
      window.crypto.getRandomValues(arr);
    } else {
      for (let i = 0; i < 16; i++) arr[i] = Math.floor(Math.random() * 256);
    }
    saltStr = btoa(String.fromCharCode(...arr));
    localStorage.setItem('webstack_vault_salt', saltStr);
  }
  const binary = atob(saltStr);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
  return bytes;
}

export async function encryptPayload<T>(dataObj: T, pin: string): Promise<string> {
  if (!pin || !window.crypto || !window.crypto.subtle) {
    return JSON.stringify(dataObj);
  }
  try {
    const salt = getOrCreateSalt();
    const key = await deriveKey(pin, salt);
    const iv = new Uint8Array(12);
    window.crypto.getRandomValues(iv);
    const enc = new TextEncoder();
    const plaintext = enc.encode(JSON.stringify(dataObj));
    const ciphertext = await window.crypto.subtle.encrypt(
      { name: 'AES-GCM', iv: iv },
      key,
      plaintext
    );
    const payload: EncryptedPayload = {
      __encrypted: true,
      iv: Array.from(iv),
      data: Array.from(new Uint8Array(ciphertext))
    };
    return JSON.stringify(payload);
  } catch (e) {
    return JSON.stringify(dataObj);
  }
}

export async function decryptPayload<T>(rawString: string | null, pin: string): Promise<T | null> {
  if (!rawString) return null;
  let parsed: any;
  try {
    parsed = JSON.parse(rawString);
  } catch (e) {
    return null;
  }
  if (!parsed || !parsed.__encrypted) {
    return parsed as T;
  }
  if (!pin || !window.crypto || !window.crypto.subtle) {
    return null;
  }
  try {
    const salt = getOrCreateSalt();
    const key = await deriveKey(pin, salt);
    const iv = new Uint8Array(parsed.iv);
    const ciphertext = new Uint8Array(parsed.data);
    const decrypted = await window.crypto.subtle.decrypt(
      { name: 'AES-GCM', iv: iv },
      key,
      ciphertext
    );
    const dec = new TextDecoder();
    return JSON.parse(dec.decode(decrypted)) as T;
  } catch (e) {
    throw new Error('DECRYPT_FAILED');
  }
}
