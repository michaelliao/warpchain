package org.warpchain.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;

/**
 * EC signature with raw r and s.
 * 
 * @author liaoxuefeng
 */
public final class ECSignature {

	public final BigInteger r;
	public final BigInteger s;

	public ECSignature(BigInteger r, BigInteger s) {
		this.r = r;
		this.s = s;
	}

	/**
	 * Serialize signature to DER format.
	 * 
	 * @return Byte array as DER.
	 */
	public byte[] toDER() {
		try (var output = new ByteArrayOutputStream(80)) {
			DERSequenceGenerator seq = new DERSequenceGenerator(output);
			seq.addObject(new ASN1Integer(r));
			seq.addObject(new ASN1Integer(s));
			seq.close();
			return output.toByteArray();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public String toString() {
		return String.format("{ECSignature: r=%x, s=%x}", r, s);
	}
}
