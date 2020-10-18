package org.warpchain.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;

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
		try (var output = new ByteArrayOutputStream()) {
			DERSequenceGenerator seq = new DERSequenceGenerator(output);
			seq.addObject(new ASN1Integer(r));
			seq.addObject(new ASN1Integer(s));
			seq.close();
			return output.toByteArray();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
