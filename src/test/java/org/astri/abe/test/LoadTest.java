package org.astri.abe.test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.astri.abe.cpabe.Cpabe;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadTest {

	private static final Logger LOG = LoggerFactory.getLogger(LoadTest.class);
	
	private Cpabe cpabe = null;
	private String publicKeyFile = null;
	private String masterKeyFile = null;
	private String testFile = null;
	
	@Before
	public void setup() throws ClassNotFoundException, IOException, NoSuchAlgorithmException {
		
		ClassLoader classLoader = getClass().getClassLoader();
		publicKeyFile = classLoader.getResource("publickey").getPath();
		masterKeyFile = classLoader.getResource("masterkey").getPath();
		testFile = "/Volumes/BICI/test/1gb.bin";

		//System.out.println("pub: " + publicKeyFile + ", master: " + masterKeyFile);
		
		cpabe = new Cpabe();
		cpabe.setup(publicKeyFile, masterKeyFile);
		
	}
	
	@Test
	public void fileStreamShouldBeEncrypted() throws Exception {
		String policy = "user:user";
		
		LOG.debug("Starting to encrypt file: " + testFile);
		File encrypted = encryptStream(policy);
		System.out.println("Encryption finished, out file: " + encrypted.getAbsolutePath());
	}
	
	private File encryptStream(String policy) throws Exception {
		File encryptedFile = new File("/Volumes/BICI/test/1gb_out.bin");
		cpabe.encStream(publicKeyFile, policy, testFile, encryptedFile.getAbsolutePath());
		return encryptedFile;
	}
	
}
