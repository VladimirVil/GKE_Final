package nodesV2;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SecurityUtil {

	public static List<String> getTokenList(List<String> fromPrevious, int randomNumber) {
		System.out.println("using prime :"+GKENode.prime);
		BigInteger nodeSecret = GKENode.generator.pow(randomNumber).mod(GKENode.prime);
		if(fromPrevious.isEmpty()) {
			return Arrays.asList(nodeSecret.toString(),"1");
		}else {
			List<String> nextSequence = fromPrevious.stream()
					.map(e -> nodeSecret.multiply(new BigInteger(e)))
					.map(BigInteger::toString)
					.collect(Collectors.toList());
			nextSequence.add(fromPrevious.get(0));
			return nextSequence;
		}
		
	}
	
	public static String getSharedSecret(BigInteger fromLastNode, int randomNumber) {
		BigInteger sharedSecret = GKENode.generator.pow(randomNumber).multiply(fromLastNode).mod(GKENode.prime);
		return sharedSecret.toString();
	}

	public static String getSharedSecret(BigInteger selfSuperElement) {
		BigInteger sharedSecret = selfSuperElement.mod(GKENode.prime);
		return sharedSecret.toString();
	}

}
