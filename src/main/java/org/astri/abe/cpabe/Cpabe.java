package org.astri.abe.cpabe;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.astri.abe.bswabe.Bswabe;
import org.astri.abe.bswabe.BswabeCph;
import org.astri.abe.bswabe.BswabeCphKey;
import org.astri.abe.bswabe.BswabeElementBoolean;
import org.astri.abe.bswabe.BswabeMsk;
import org.astri.abe.bswabe.BswabePrv;
import org.astri.abe.bswabe.BswabePub;
import org.astri.abe.bswabe.SerializeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unisa.dia.gas.jpbc.Element;

public class Cpabe {

	private static final Logger LOG = LoggerFactory.getLogger(Cpabe.class);
	
	/**
	 * @param args
	 * @author Junwei Wang(wakemecn@gmail.com)
	 */

	public void setup(String pubfile, String mskfile) throws IOException,
			ClassNotFoundException {
		byte[] pub_byte, msk_byte;
		BswabePub pub = new BswabePub();
		BswabeMsk msk = new BswabeMsk();
		Bswabe.setup(pub, msk);

		/* store BswabePub into mskfile */
		pub_byte = SerializeUtils.serializeBswabePub(pub);
		Common.spitFile(pubfile, pub_byte);

		/* store BswabeMsk into mskfile */
		msk_byte = SerializeUtils.serializeBswabeMsk(msk);
		Common.spitFile(mskfile, msk_byte);
	}

	public void keygen(String pubfile, String prvfile, String mskfile,
			String[] attributeArray) throws NoSuchAlgorithmException, IOException {
		BswabePub pub;
		BswabeMsk msk;
		byte[] pub_byte, msk_byte, prv_byte;

		/* get BswabePub from pubfile */
		pub_byte = Common.suckFile(pubfile);
		pub = SerializeUtils.unserializeBswabePub(pub_byte);

		/* get BswabeMsk from mskfile */
		msk_byte = Common.suckFile(mskfile);
		msk = SerializeUtils.unserializeBswabeMsk(pub, msk_byte);

		BswabePrv prv = Bswabe.keygen(pub, msk, attributeArray);

		/* store BswabePrv into prvfile */
		prv_byte = SerializeUtils.serializeBswabePrv(prv);
		Common.spitFile(prvfile, prv_byte);
	}
	
	public byte[] keygen(byte[] publicKey, byte[] masterKey,
			String[] attributeArray) throws NoSuchAlgorithmException, IOException {

		BswabePub pub = SerializeUtils.unserializeBswabePub(publicKey);
		BswabeMsk msk = SerializeUtils.unserializeBswabeMsk(pub, masterKey);
		BswabePrv prv = Bswabe.keygen(pub, msk, attributeArray);

		return SerializeUtils.serializeBswabePrv(prv);
	}

	public void enc(String pubfile, String policy, String inputfile,
			String encfile) throws Exception {
		BswabePub pub;
		BswabeCph cph;
		BswabeCphKey keyCph;
		byte[] plt;
		byte[] cphBuf;
		byte[] aesBuf;
		byte[] pub_byte;
		Element m;

		/* get BswabePub from pubfile */
		pub_byte = Common.suckFile(pubfile);
		pub = SerializeUtils.unserializeBswabePub(pub_byte);

		keyCph = Bswabe.enc(pub, policy);
		cph = keyCph.cph;
		m = keyCph.key;
		// System.err.println("m = " + m.toString());

		if (cph == null) {
			System.out.println("Error happed in enc");
			throw new Exception("Error in encryption");
		}

		cphBuf = SerializeUtils.bswabeCphSerialize(cph);

		/* read file to encrypted */
		plt = Common.suckFile(inputfile);
		aesBuf = AESCoder.encrypt(m.toBytes(), plt);
		// PrintArr("element: ", m.toBytes());
		Common.writeCpabeFile(encfile, cphBuf, aesBuf);
	}

	public boolean dec(String pubfile, String prvfile, String encfile,
			String decfile) throws Exception {

		/* get BswabePub from pubfile */
		byte[] pub_byte = Common.suckFile(pubfile);
		BswabePub pub = SerializeUtils.unserializeBswabePub(pub_byte);

		/* read ciphertext */
		byte[][] tmp = Common.readCpabeFile(encfile);
		byte[] aesBuf = tmp[0];
		byte[] cphBuf = tmp[1];
		BswabeCph cph = SerializeUtils.bswabeCphUnserialize(pub, cphBuf);

		/* get BswabePrv form prvfile */
		byte[] prv_byte = Common.suckFile(prvfile);
		BswabePrv prv = SerializeUtils.unserializeBswabePrv(pub, prv_byte);

		BswabeElementBoolean beb = Bswabe.dec(pub, prv, cph);

		if (beb.b) {
			byte[] plt = AESCoder.decrypt(beb.e.toBytes(), aesBuf);
			Common.spitFile(decfile, plt);
			return true;
		} else {
			return false;
		}
	}
	
	public void encStream(byte[] pubKey, String policy, String inputfile,
			String encfile) throws Exception {

		BswabePub pub = SerializeUtils.unserializeBswabePub(pubKey);

		BswabeCphKey keyCph = Bswabe.enc(pub, policy);
		BswabeCph cph = keyCph.cph;
		Element seed = keyCph.key;

		if (cph == null) {
			LOG.error("Error happenedd in enc");
			throw new Exception("Error in encryption");
		}

		byte[] cphBuf = SerializeUtils.bswabeCphSerialize(cph);
		
		// or use RandomAccessFile ?
		FileInputStream fis = new FileInputStream(inputfile);
		FileOutputStream fos = new FileOutputStream(encfile);
		
		Common.writeCpabeHeader(fos, cphBuf);
		AESCoder.encryptStream(seed.toBytes(), fis, fos);
		fos.close();
	}
	
	public boolean decStream(byte[] pubKey, byte[] privateKey, String encfile,
			String decfile) throws Exception {

		BswabePub pub = SerializeUtils.unserializeBswabePub(pubKey);

		FileInputStream fis = new FileInputStream(encfile);
		
		// read ciphertext
		byte[] cphBuf = Common.readCpabeHeader(fis);

		BswabeCph cph = SerializeUtils.bswabeCphUnserialize(pub, cphBuf);

		BswabePrv prv = SerializeUtils.unserializeBswabePrv(pub, privateKey);

		BswabeElementBoolean beb = Bswabe.dec(pub, prv, cph);

		if (beb.b) {
			FileOutputStream outStream = new FileOutputStream(decfile);
			AESCoder.decryptStream(beb.e.toBytes(), fis, outStream);
			fis.close();
			return true;
		} else {
			fis.close();
			return false;
		}
	}
	
}
