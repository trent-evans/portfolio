import java.math.BigInteger;
import java.security.Signature;
import java.security.cert.Certificate;

public class SignatureVerify {
    public static boolean verifySignature(Certificate certificate, BigInteger publicKey, byte[] signedPublicKey) throws Exception{
        Signature verifyServerSignature = Signature.getInstance("SHA256WithRSA");
        verifyServerSignature.initVerify(certificate);
        verifyServerSignature.update(publicKey.toByteArray());
        return verifyServerSignature.verify(signedPublicKey);
    }
}
