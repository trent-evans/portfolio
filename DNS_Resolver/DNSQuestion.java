import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class DNSQuestion {

    String[] QName;
    short QType;
    short QClass;

    static DNSQuestion decodeQuestion(InputStream inStream, DNSMessage dnsMessage) throws IOException {
        DNSQuestion returnQuestion = new DNSQuestion();
        returnQuestion.QName = dnsMessage.readDomainName(inStream);
        returnQuestion.QType = new BigInteger(inStream.readNBytes(2)).shortValue();
        returnQuestion.QClass = new BigInteger(inStream.readNBytes(2)).shortValue();
        return returnQuestion;
    }

    void writeBytes(ByteArrayOutputStream byteOutStream, HashMap<String, Short> domainNameLocations) throws IOException {
        byteOutStream.write(ByteBuffer.allocate(2).putShort(QType).array());
        byteOutStream.write(ByteBuffer.allocate(2).putShort(QClass).array());
    }

    @Override
    public String toString() {
        return "DNSQuestion{" +
                "QName=" + Arrays.toString(QName) +
                ", QType=" + QType +
                ", QClass=" + QClass +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DNSQuestion that = (DNSQuestion) o;
        return QType == that.QType &&
                QClass == that.QClass &&
                Arrays.equals(QName, that.QName);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(QType, QClass);
        result = 31 * result + Arrays.hashCode(QName);
        return result;
    }
}
