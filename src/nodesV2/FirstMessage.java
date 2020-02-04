package nodesV2;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class FirstMessage {
    private static final String DELIMITER = "^^^^";
	final static String hex = "AF33";
    public static final String ALICE = "Alice";
	BigInteger base = BigInteger.valueOf(Integer.parseInt(hex, 16));
	BigInteger generator = BigInteger.valueOf(2);
	Random random = new Random();
	int secret = random.nextInt(999999);
	DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
	Date dateobj = new Date();
    private CharSequence serializedMessage;
    public static CharSequence senderID = "ALICE";
    private CharSequence messageBody;



	GKEMessage_Impl message = new GKEMessage_Impl(ALICE, "", dateobj);
 
	
	private BigInteger createFirstElement() {
		return generator.modPow(BigInteger.valueOf(secret), base);
	}
	
	   public CharSequence getSerializedMessage() {
	        if(this.serializedMessage == null) {
	            this.serializedMessage = this.serializeMessage();
	        }

	        return this.serializedMessage;
	    }

	    private CharSequence serializeMessage() {
	        DateFormat df = DateFormat.getInstance();

	        return this.senderID + DELIMITER
	                + df.format(this.dateobj) + DELIMITER
	                + this.createFirstElement().byteValueExact();
	    }
	    
}
