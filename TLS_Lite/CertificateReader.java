import java.io.FileInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

public class CertificateReader {
    public static Certificate readInCertificate(String filePath) throws Exception{
        CertificateFactory serverCertificateFactory = CertificateFactory.getInstance("X.509");
        return serverCertificateFactory.generateCertificate(new FileInputStream(filePath));
    }
}
