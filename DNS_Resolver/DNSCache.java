import java.util.HashMap;

public class DNSCache {

HashMap<DNSQuestion,DNSRecord> questionToRecord;
    DNSCache(){
        questionToRecord = new HashMap<>();
    }

    public void addToCache(DNSQuestion dnsQuestion, DNSRecord dnsRecord){
        questionToRecord.put(dnsQuestion,dnsRecord);
    }

    public boolean questionInCache(DNSQuestion dnsQuestion){
        if(!questionToRecord.containsKey(dnsQuestion)){ // If the question isn't in the cache
            return false;
        }else if (questionToRecord.containsKey(dnsQuestion) && !questionToRecord.get(dnsQuestion).timestampValid()){
            // If it's in the cache but the timestamp isn't valid
            questionToRecord.remove(dnsQuestion); // Remove the old key
            return false;
        }
        return true;
    }

    public DNSRecord fetchFromCache(DNSQuestion dnsQuestion){
        return questionToRecord.get(dnsQuestion);
    }

}
