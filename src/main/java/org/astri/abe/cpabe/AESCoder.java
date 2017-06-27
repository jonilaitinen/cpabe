package org.astri.abe.cpabe;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AESCoder {

	private static final Logger LOG = LoggerFactory.getLogger(AESCoder.class);
	
	private static final int BLOCK_BYTES = 1024 * 5;
	
	private static byte[] getRawKey(byte[] seed) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(seed);
		kgen.init(128, sr); // 192 and 256 bits may not be available
		SecretKey skey = kgen.generateKey();
		byte[] raw = skey.getEncoded();
		return raw;
	}

	public static byte[] encrypt(byte[] seed, byte[] plaintext)
			throws Exception {
		byte[] raw = getRawKey(seed);
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(plaintext);
		LOG.debug("AES encrypted bytes: " + encrypted.length);
		return encrypted;
	}

	public static byte[] decrypt(byte[] seed, byte[] ciphertext)
			throws Exception {
		byte[] raw = getRawKey(seed);
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(ciphertext);
		
		return decrypted;
	}

	public static int encryptStream(byte[] seed, InputStream plaintext, OutputStream outStream)
			throws Exception {
		byte[] raw = getRawKey(seed);
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		
		byte[] block = new byte[BLOCK_BYTES];
		int writtenBytes = 0;
		while(plaintext.available() > 0) {
			int read = plaintext.read(block);
			byte[] encrypted = cipher.update(block, 0, read);
			writtenBytes += encrypted.length;
			outStream.write(encrypted);
		}
		
		byte[] finalData = cipher.doFinal();
		outStream.write(finalData);
		writtenBytes += finalData.length;
		
		LOG.debug("AES written bytes: " + writtenBytes);
		return writtenBytes;
	}
	
	public static int decryptStream(byte[] seed, InputStream ciphertext, OutputStream outStream)
			throws Exception {
		byte[] raw = getRawKey(seed);
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		
		byte[] block = new byte[BLOCK_BYTES];
		int readBytes = 0;
		while(ciphertext.available() > 0) {
			int read = ciphertext.read(block);
			readBytes += read;
			outStream.write(cipher.update(block, 0, read));
		}
		LOG.debug("AES read bytes: " + readBytes);
		byte[] finalData = cipher.doFinal();
		readBytes += finalData.length;
		outStream.write(finalData);
		
		return readBytes;
	}
	
}