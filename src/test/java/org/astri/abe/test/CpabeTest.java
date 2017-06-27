package org.astri.abe.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

import org.astri.abe.cpabe.Cpabe;
import org.junit.Before;
import org.junit.Test;

public class CpabeTest {
	
	private Cpabe cpabe = null;
	private String publicKeyFile = null;
	private String masterKeyFile = null;
	private String testFile = null;
	
	@Before
	public void setup() throws ClassNotFoundException, IOException, NoSuchAlgorithmException {
		
		ClassLoader classLoader = getClass().getClassLoader();
		publicKeyFile = classLoader.getResource("publickey").getPath();
		masterKeyFile = classLoader.getResource("masterkey").getPath();
		testFile = classLoader.getResource("test.txt").getPath();

		//System.out.println("pub: " + publicKeyFile + ", master: " + masterKeyFile);
		
		cpabe = new Cpabe();
		cpabe.setup(publicKeyFile, masterKeyFile);
		
	}

	@Test
	public void fileShouldBeEncryptedAndDecoded() throws Exception {
		String[] attributes = {"dept:CTO", "dept:CFO", "group:SNDS", "team:DATA"};
		String policy = "dept:CTO group:SNDS team:DATA 3of3 dept:CFO 1of2";
		
		File encrypted = encryptFile(policy);
		assertTrue(decodeFile(attributes, encrypted));
	}
	
	
	@Test
	public void fileStreamShouldBeEncryptedAndDecoded() throws Exception {
		String[] attributes = {"dept:CTO", "dept:CFO", "group:SNDS", "team:DATA"};
		String policy = "dept:CTO group:SNDS team:DATA 3of3 dept:CFO 1of2";
		
		File encrypted = encryptStream(policy);
		assertTrue(decodeStream(attributes, encrypted));
	}
	

	@Test
	public void fileShouldNotBeDecoded() throws Exception {
		String[] attributes = {"group:SNDS", "team:DATA"};
		String policy = "dept:CTO group:SNDS team:DATA 3of3 dept:CFO 1of2";
		
		File encrypted = encryptFile(policy);
		assertFalse(decodeFile(attributes, encrypted));
	}
	
	@Test
	public void fileShouldBeDecoded() throws Exception {
		String[] attributes = {"dept:CFO"};
		String policy = "dept:CTO group:SNDS team:DATA 3of3 dept:CFO 1of2";
		
		File encrypted = encryptFile(policy);
		assertTrue(decodeFile(attributes, encrypted));
	}
	
	@Test
	public void testGroupWithIndividualUsersShare() throws Exception {
		// share to intersection of G1 and G2, and also to users A and B
		String[] attributes = {"user:B"};
		String policy = "group:G1 group:G2 2of2 user:A user:B 1of2 1of2";
		
		File encrypted = encryptFile(policy);
		assertTrue(decodeFile(attributes, encrypted));
	}
	
	@Test
	public void testGroupWithIndividualUsersShareWithSingleGroup() throws Exception {
		// share to intersection of G1 and G2, and also to users A and B
		String[] attributes = {"group:G2", "user:C"};
		String policy = "group:G1 group:G2 2of2 user:A user:B 1of2 1of2";
		
		File encrypted = encryptFile(policy);
		assertFalse(decodeFile(attributes, encrypted));
	}
	
	@Test
	public void testShareWithPartialGroupMatchAndUser() throws Exception {
		// share to intersection of G1 and G2, and also to users A and B
		String[] attributes = {"group:G2", "user:A"};
		String policy = "group:G1 group:G2 2of2 user:A user:B 1of2 2of2";
		
		File encrypted = encryptFile(policy);
		assertFalse(decodeFile(attributes, encrypted));
	}

	private File encryptFile(String policy) throws Exception {
		File encryptedFile = File.createTempFile("encrypted", "");
		cpabe.enc(publicKeyFile, policy, testFile, encryptedFile.getAbsolutePath());
		return encryptedFile;
	}
	
	private boolean decodeFile(String[] attributes, File encryptedFile) throws Exception {
		
		File decodedFile = File.createTempFile("decoded", "");
		System.out.println("decoded file: " + decodedFile);
		
		File privateKeyfile = File.createTempFile("privatekey", "");
		cpabe.keygen(publicKeyFile, privateKeyfile.getAbsolutePath(), masterKeyFile, attributes);
		
		boolean success = cpabe.dec(publicKeyFile, privateKeyfile.getAbsolutePath(),
				encryptedFile.getAbsolutePath(), decodedFile.getAbsolutePath());
		
		Files.delete(privateKeyfile.toPath());
		Files.delete(encryptedFile.toPath());
		Files.delete(decodedFile.toPath());
		
		return success;
	}
	
	private File encryptStream(String policy) throws Exception {
		File encryptedFile = File.createTempFile("encrypted", "");
		cpabe.encStream(publicKeyFile, policy, testFile, encryptedFile.getAbsolutePath());
		return encryptedFile;
	}
	
	private boolean decodeStream(String[] attributes, File encryptedFile) throws Exception {
		File decodedFile = File.createTempFile("decoded", "");
		System.out.println("decoded stream: " + decodedFile);
		
		File privateKeyfile = File.createTempFile("privatekey", "");
		cpabe.keygen(publicKeyFile, privateKeyfile.getAbsolutePath(), masterKeyFile, attributes);
		
		boolean success = cpabe.decStream(publicKeyFile, privateKeyfile.getAbsolutePath(),
				encryptedFile.getAbsolutePath(), decodedFile.getAbsolutePath());
		
		Files.delete(privateKeyfile.toPath());
		Files.delete(encryptedFile.toPath());
		Files.delete(decodedFile.toPath());
		
		return success;
	}
	
}
