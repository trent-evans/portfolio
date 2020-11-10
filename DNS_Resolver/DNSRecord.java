import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class DNSRecord {

    String[] domainName;
    short type;
    short recordClass;
    int ttl; // Stored in seconds
    short rdLength;
    byte[] rdData;
    Date whenCreated;

    static DNSRecord decodeRecord(InputStream inStream, DNSMessage dnsMessage) throws IOException {
        DNSRecord newRecord = new DNSRecord();
        newRecord.domainName = dnsMessage.readDomainName(inStream);
        newRecord.type = new BigInteger(inStream.readNBytes(2)).shortValue();
        newRecord.recordClass = new BigInteger(inStream.readNBytes(2)).shortValue();
        newRecord.ttl = new BigInteger(inStream.readNBytes(4)).intValue();
        newRecord.rdLength = new BigInteger(inStream.readNBytes(2)).shortValue();
        newRecord.rdData = inStream.readNBytes(newRecord.rdLength);
        newRecord.whenCreated = new Date();
        return newRecord;
    }

    void writeBytes(ByteArrayOutputStream byteOutStream, HashMap<String, Short> map) throws IOException {
        byteOutStream.write(ByteBuffer.allocate(2).putShort(type).array());
        byteOutStream.write(ByteBuffer.allocate(2).putShort(recordClass).array());
        byteOutStream.write(ByteBuffer.allocate(4).putInt(ttl).array());
        byteOutStream.write(ByteBuffer.allocate(2).putShort(rdLength).array());
        byteOutStream.write(rdData);
    }

    boolean timestampValid(){
        Date validCheck = new Date(); // Create a new date as a validity check
        long ttlMilliseconds = ttl*1000; // Convert ttl from s to ms
        return((validCheck.getTime()-ttlMilliseconds) < whenCreated.getTime());
    }

    @Override
    public String toString() {
        return "DNSRecord{" +
                "domainName=" + Arrays.toString(domainName) +
                ", type=" + type +
                ", recordClass=" + recordClass +
                ", ttl=" + ttl +
                ", rdLength=" + rdLength +
                ", rdData=" + Arrays.toString(rdData) +
                '}';
    }


}
