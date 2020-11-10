import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class DNSHeader {

    byte[] ID; // Id = 16 bits
    byte QR; // Query or Response code = 1 bit
    byte opcode; // Opcode  = 4 bits
    byte AA; // Authoritative Answer = 1 bit
    byte TC; // Truncation = 1 bit
    byte RD; // Recursive query = 1 bit
    byte RA; // Recursion available = 1 bit
    byte Z; // Zero => Doesn't do anything = 1 bit that should default to 0
    byte AD; // Authentic Data = 1 bit
    byte CD; // Checking Disables = 1 bit
    byte ResponseCode; // 1 - 5 = 4 bits;
    short QDCount; // Question count
    short ANCount; // Answer count
    short NSCount; // Authority count
    short ARCount; // Additional record count

    static DNSHeader decodeHeader(ByteArrayInputStream inStream) throws IOException {
        DNSHeader decodedHeader = new DNSHeader();

        decodedHeader.ID = inStream.readNBytes(2);

        byte[] firstSplit = inStream.readNBytes(2);

        // First byte
        decodedHeader.QR = (byte) ((firstSplit[0] & 0x80) >>> 7);     // 0x80 = 1000 0000
        decodedHeader.opcode = (byte) ((firstSplit[0] & 0x78) >>> 3); // 0x78 = 0111 1000
        decodedHeader.AA = (byte) ((firstSplit[0] & 0x04) >>> 2) ;    // 0x04 = 0000 0100
        decodedHeader.TC = (byte) ((firstSplit[0] & 0x02) >>> 1);     // 0x02 = 0000 0010
        decodedHeader.RD = (byte) (firstSplit[0] & 0x01);             // 0x01 = 0000 0001

        // Second byte
        decodedHeader.RA = (byte) ((firstSplit[1] & 0x80) >>> 7);     // 0x80 = 1000 0000
        decodedHeader.Z  = (byte) ((firstSplit[1] & 0x40) >>> 6);     // 0x40 = 0100 0000
        decodedHeader.AD = (byte) ((firstSplit[1] & 0x20) >>> 5);     // 0x20 = 0010 0000
        decodedHeader.CD = (byte) ((firstSplit[1] & 0x10) >>> 4);     // 0x10 = 0001 0000
        decodedHeader.ResponseCode = (byte) (firstSplit[1] & 0x0F);   // 0x0F = 0000 1111

        // Counts
        decodedHeader.QDCount = new BigInteger(inStream.readNBytes(2)).shortValue();
        decodedHeader.ANCount = new BigInteger(inStream.readNBytes(2)).shortValue();
        decodedHeader.NSCount = new BigInteger(inStream.readNBytes(2)).shortValue();
        decodedHeader.ARCount = new BigInteger(inStream.readNBytes(2)).shortValue();

        return decodedHeader;
    }

    static DNSHeader buildResponseHeader(DNSMessage request, DNSRecord answers){
        DNSHeader responseHeader = new DNSHeader();

        responseHeader.ID           = request.dnsHeader.ID;
        responseHeader.QR           = 1; // Because it's an answer now
        responseHeader.opcode       = request.dnsHeader.opcode;
        responseHeader.AA           = request.dnsHeader.AA;
        responseHeader.TC           = request.dnsHeader.TC;
        responseHeader.RD           = request.dnsHeader.RD;
        responseHeader.RA           = request.dnsHeader.RA;
        responseHeader.Z            = request.dnsHeader.Z;
        responseHeader.AD           = request.dnsHeader.AD;
        responseHeader.CD           = request.dnsHeader.CD;
        responseHeader.ResponseCode = request.dnsHeader.ResponseCode;
        responseHeader.QDCount      = request.dnsHeader.QDCount;
        responseHeader.ANCount      = 1; // Only return one answer / question
        responseHeader.NSCount      = request.dnsHeader.NSCount;
        responseHeader.ARCount      = request.dnsHeader.ARCount;

        return responseHeader;
    }

    void writeBytes(OutputStream outStream) throws IOException {

        // Write out the ID
        outStream.write(ID);

        // QR = 1, opcode = 4, AA = 1, TC = 1, RD = 1
        byte byteQrOpcodeAaTcRd = (byte) (QR << 7);
        byteQrOpcodeAaTcRd = (byte) (byteQrOpcodeAaTcRd | (opcode << 3));
        byteQrOpcodeAaTcRd = (byte) (byteQrOpcodeAaTcRd | (AA << 2));
        byteQrOpcodeAaTcRd = (byte) (byteQrOpcodeAaTcRd | (TC << 1));
        byteQrOpcodeAaTcRd = (byte) (byteQrOpcodeAaTcRd | RD);
        outStream.write(byteQrOpcodeAaTcRd);

        // RA = 1, Z = 1, AD = 1, CD = 1, ResponseCode = 4
        byte byteRaZAdCd = (byte) (RA << 7);
        byteRaZAdCd = (byte) (byteRaZAdCd | (Z << 6));
        byteRaZAdCd = (byte) (byteRaZAdCd | (AD << 5));
        byteRaZAdCd = (byte) (byteRaZAdCd | (CD << 4));
        byteRaZAdCd = (byte) (byteRaZAdCd | ResponseCode);
        outStream.write(byteRaZAdCd);

        // Write out the counts
        outStream.write(ByteBuffer.allocate(2).putShort(QDCount).array());
        outStream.write(ByteBuffer.allocate(2).putShort(ANCount).array());
        outStream.write(ByteBuffer.allocate(2).putShort(NSCount).array());
        outStream.write(ByteBuffer.allocate(2).putShort(ARCount).array());

    }

    @Override
    public String toString() {
        return "DNSHeader{" +
                "ID=" + ByteBuffer.wrap(ID).getShort() +
                ", QR=" + QR +
                ", opcode=" + opcode +
                ", AA=" + AA +
                ", TC=" + TC +
                ", RD=" + RD +
                ", RA=" + RA +
                ", Z=" + Z +
                ", AD=" + AD +
                ", CD=" + CD +
                ", ResponseCode=" + ResponseCode +
                ", QDCount=" + QDCount +
                ", ANCount=" + ANCount +
                ", NSCount=" + NSCount +
                ", ARCount=" + ARCount +
                '}';
    }

    int getQuestionCount(){
        return QDCount;
    }

    int getAnswerCount(){
        return ANCount;
    }

    int getAuthorityCount(){
        return NSCount;
    }

    int getAdditionalCount(){
        return ARCount;
    }
}
