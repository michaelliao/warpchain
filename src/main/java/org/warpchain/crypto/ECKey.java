package org.warpchain.crypto;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.util.Arrays;
import org.warpchain.util.ByteUtils;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Compressed ECKey.
 * 
 * @author liaoxuefeng
 */
public final class ECKey {

	private BigInteger privateKey = null;
	private BigInteger publicKey = null;
	private byte[] publicKeyAsBytes = null;

	private static final BigInteger PUBKEY_FIRST_BYTE = new BigInteger("ff" + "00".repeat(32), 16);

	private ECKey(BigInteger privateKey, BigInteger publicKey, byte[] publicKeyAsBytes) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.publicKeyAsBytes = publicKeyAsBytes;
	}

	public static ECKey createECKey() {
		for (;;) {
			byte[] privateKeyAsBytes = new byte[32];
			SECURE_RANDOM.nextBytes(privateKeyAsBytes);
			BigInteger privateKey = new BigInteger(1, privateKeyAsBytes);
			if (privateKey.compareTo(MIN_PRIVATE_KEY) > 0 && privateKey.compareTo(CURVE_N) < 0) {
				return new ECKey(privateKey, null, null);
			}
		}
	}

	public static ECKey fromPrivateKey(BigInteger privateKey) {
		if (privateKey == null || privateKey.signum() <= 0 || privateKey.compareTo(CURVE_N) >= 0) {
			throw new IllegalArgumentException("Invalid private key.");
		}
		return new ECKey(privateKey, null, null);
	}

	public static ECKey fromPublicKey(BigInteger publicKey) {
		if (publicKey == null || publicKey.signum() <= 0) {
			throw new IllegalArgumentException("Invalid public key.");
		}
		int n = publicKey.and(PUBKEY_FIRST_BYTE).shiftRight(256).intValue();
		if (n != 0x02 && n != 0x03) {
			throw new IllegalArgumentException("Invalid public key.");
		}
		return new ECKey(null, publicKey, null);
	}

	public static ECKey fromPublicKey(byte[] publicKey) {
		if (publicKey == null || publicKey.length != 33 || (publicKey[0] != 0x02 && publicKey[0] != 0x03)) {
			throw new IllegalArgumentException("Invalid public key.");
		}
		return new ECKey(null, null, publicKey);
	}

	public BigInteger getPrivateKey() {
		return this.privateKey;
	}

	public byte[] getPublicKeyAsBytes() {
		if (this.publicKeyAsBytes == null) {
			if (this.publicKey != null) {
				this.publicKeyAsBytes = ByteUtils.bigIntegerToBytes(this.publicKey, 33);
			} else {
				this.publicKeyAsBytes = CURVE.getG().multiply(this.privateKey).getEncoded(true);
			}
		}
		return Arrays.copyOf(this.publicKeyAsBytes, 33);
	}

	public BigInteger getPublicKey() {
		if (this.publicKey == null) {
			if (this.publicKeyAsBytes != null) {
				this.publicKey = new BigInteger(1, this.publicKeyAsBytes);
			} else {
				this.publicKeyAsBytes = CURVE.getG().multiply(this.privateKey).getEncoded(true);
				this.publicKey = new BigInteger(1, this.publicKeyAsBytes);
			}
		}
		return this.publicKey;
	}

	public ECSignature sign(byte[] message) {
		ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
		ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKey, CURVE);
		signer.init(true, privKey);
		BigInteger[] sigs = signer.generateSignature(message);
		BigInteger r = sigs[0];
		BigInteger s = sigs[1];
		// canonicalize s:
		if (s.compareTo(CURVE_HALF_N) > 0) {
			s = CURVE_N.subtract(s);
		}
		return new ECSignature(r, s);
	}

	public boolean verify(byte[] message, BigInteger r, BigInteger s) {
		if (s.compareTo(CURVE_HALF_N) > 0) {
			throw new IllegalArgumentException("Signature s is not canonicalized.");
		}
		ECDSASigner signer = new ECDSASigner();
		ECPublicKeyParameters params = new ECPublicKeyParameters(CURVE.getCurve().decodePoint(getPublicKeyAsBytes()),
				CURVE);
		signer.init(false, params);
		return signer.verifySignature(message, r, s);
	}

	public boolean verify(byte[] message, ECSignature signature) {
		return verify(message, signature.r, signature.s);
	}

	// static fields //////////////////////////////////////////////////////////

	private static final ECDomainParameters CURVE;
	private static final BigInteger CURVE_N;
	private static final BigInteger CURVE_HALF_N;
	private static final SecureRandom SECURE_RANDOM;
	private static final BigInteger MIN_PRIVATE_KEY = new BigInteger("ff".repeat(24), 16);

	static {
		X9ECParameters params = SECNamedCurves.getByName("secp256k1");
		CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
		CURVE_N = params.getN();
		CURVE_HALF_N = params.getN().shiftRight(1);
		SecureRandom secureRandom = null;
		try {
			secureRandom = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			secureRandom = new SecureRandom();
		}
		SECURE_RANDOM = secureRandom;
	}

	public static BigInteger getN() {
		return CURVE_N;
	}
}
