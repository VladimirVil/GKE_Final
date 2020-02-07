package nodesV2;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SecurityUtil {

	//creates the messages for the upflow
	//as described in the algorithm, it will be every element from the previous multiplied by the 
	//current secret + at the end the super element is added as it is. Super element is the first arrived elemnt (order
	//is important and kept along the whole algorithm)
	public static List<String> getTokenList(List<String> fromPrevious, BigInteger randomNumber) {
		System.out.println("using prime :"+GKENode.prime);
		BigInteger nodeSecret = GKENode.generator.pow(randomNumber.intValueExact()).mod(GKENode.prime);
		//first participant (Alice) case
		if(fromPrevious.isEmpty()) {
			return Arrays.asList(nodeSecret.toString(),"1");
		//general case
		}else {
			List<String> nextSequence = fromPrevious.stream()
					.map(e -> nodeSecret.multiply(new BigInteger(e)))
					.map(BigInteger::toString)
					.collect(Collectors.toList());
			nextSequence.add(fromPrevious.get(0));
			return nextSequence;
		}
		
	}
	
	public static String getSharedSecret(BigInteger fromLastNode, BigInteger randomNumber) {
		BigInteger sharedSecret = GKENode.generator.pow(randomNumber.intValueExact()).multiply(fromLastNode).mod(GKENode.prime);
		return sharedSecret.toString();
	}

	//getting the shared secret for the last element (multiplication already done)
	public static String getSharedSecret(BigInteger selfSuperElement) {
		BigInteger sharedSecret = selfSuperElement.mod(GKENode.prime);
		return sharedSecret.toString();
	}
	
	//calculation of diffie hellman for 2 participants. Not a part of algorithm, part of testing negative tests 
    public static String deffieHellmanForTwo (BigInteger aliceSec, BigInteger bobSec) {
    	
    	BigInteger aTag, bTag, middleResultA, middleResultB;
    	aTag = GKENode.generator.modPow(aliceSec, GKENode.prime);
    	bTag = GKENode.generator.modPow(bobSec, GKENode.prime);
    	
    	middleResultB = aTag.modPow(bobSec, GKENode.prime);
    	middleResultA = bTag.modPow(aliceSec, GKENode.prime);
    	System.out.println("MiddleResultA is :" + middleResultA + "middleResultB is : " + middleResultB);
    	if (middleResultA.compareTo(middleResultB) == 0) {
    		return middleResultA.toString();
    	}
    	else {
    		System.out.print("Diffie hellman provided unacceptable result. Returning -1");
    		return BigInteger.valueOf(Integer.parseInt("-1")).toString();
    	}
    	
    }

}
