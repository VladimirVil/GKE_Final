package nodesV2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import net.sharksystem.asap.ASAPChunkReceivedListener;
import net.sharksystem.asap.ASAPEngine;
import net.sharksystem.asap.ASAPEngineFS;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPStorage;
import net.sharksystem.asap.MultiASAPEngineFS;
import net.sharksystem.asap.MultiASAPEngineFS_Impl;
import net.sharksystem.asap.apps.ASAPJavaApplication;
import net.sharksystem.asap.apps.ASAPJavaApplicationFS;
import net.sharksystem.asap.apps.ASAPMessageReceivedListener;
import net.sharksystem.asap.apps.ASAPMessages;

public class GKENode implements ASAPJavaApplication{
	// the final values re from RFC3526, id 16. Modular Exponential DH Group 4096
	// bit
	final static BigInteger generator = new BigInteger("2");
//	final static String hex = "FFFFFFFF FFFFFFFF C90FDAA2 2168C234 C4C6628B 80DC1CD1\n" + 
//			"      29024E08 8A67CC74 020BBEA6 3B139B22 514A0879 8E3404DD\n" + 
//			"      EF9519B3 CD3A431B 302B0A6D F25F1437 4FE1356D 6D51C245\n" + 
//			"      E485B576 625E7EC6 F44C42E9 A637ED6B 0BFF5CB6 F406B7ED\n" + 
//			"      EE386BFB 5A899FA5 AE9F2411 7C4B1FE6 49286651 ECE45B3D\n" + 
//			"      C2007CB8 A163BF05 98DA4836 1C55D39A 69163FA8 FD24CF5F\n" + 
//			"      83655D23 DCA3AD96 1C62F356 208552BB 9ED52907 7096966D\n" + 
//			"      670C354E 4ABC9804 F1746C08 CA18217C 32905E46 2E36CE3B\n" + 
//			"      E39E772C 180E8603 9B2783A2 EC07A28F B5C55DF0 6F4C52C9\n" + 
//			"      DE2BCBF6 95581718 3995497C EA956AE5 15D22618 98FA0510\n" + 
//			"      15728E5A 8AAAC42D AD33170D 04507A33 A85521AB DF1CBA64\n" + 
//			"      ECFB8504 58DBEF0A 8AEA7157 5D060C7D B3970F85 A6E1E4C7\n" + 
//			"      ABF5AE8C DB0933D7 1E8C94E0 4A25619D CEE3D226 1AD2EE6B\n" + 
//			"      F12FFA06 D98A0864 D8760273 3EC86A64 521F2B18 177B200C\n" + 
//			"      BBE11757 7A615D6C 770988C0 BAD946E2 08E24FA0 74E5AB31\n" + 
//			"      43DB5BFC E0FD108E 4B82D120 A9210801 1A723C12 A787E6D7\n" + 
//			"      88719A10 BDBA5B26 99C32718 6AF4E23C 1A946834 B6150BDA\n" + 
//			"      2583E9CA 2AD44CE8 DBBBC2DB 04DE8EF9 2E8EFC14 1FBECAA6\n" + 
//			"      287C5947 4E6BC05D 99B2964F A090C3A2 233BA186 515BE7ED\n" + 
//			"      1F612970 CEE2D7AF B81BDD76 2170481C D0069127 D5B05AA9\n" + 
//			"      93B4EA98 8D8FDDC1 86FFB7DC 90A6C08F 4DF435C9 34063199\n" + 
//			"      FFFFFFFF FFFFFFFF";
//	
	//Same number just without spaces 
	//final static String hex = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6BF12FFA06D98A0864D87602733EC86A64521F2B18177B200CBBE117577A615D6C770988C0BAD946E208E24FA074E5AB3143DB5BFCE0FD108E4B82D120A92108011A723C12A787E6D788719A10BDBA5B2699C327186AF4E23C1A946834B6150BDA2583E9CA2AD44CE8DBBBC2DB04DE8EF92E8EFC141FBECAA6287C59474E6BC05D99B2964FA090C3A2233BA186515BE7ED1F612970CEE2D7AFB81BDD762170481CD0069127D5B05AA993B4EA988D8FDDC186FFB7DC90A6C08F4DF435C934063199FFFFFFFFFFFFFFFF";
	final static BigInteger prime = new BigInteger("1044388881413152506679602719846529545831269060992135009022588756444338172022322690710444046669809783930111585737890362691860127079270495454517218673016928427459146001866885779762982229321192368303346235204368051010309155674155697460347176946394076535157284994895284821633700921811716738972451834979455897010306333468590751358365138782250372269117968985194322444535687415522007151638638141456178420621277822674995027990278673458629544391736919766299005511505446177668154446234882665961680796576903199116089347634947187778906528008004756692571666922964122566174582776707332452371001272163776841229318324903125740713574141005124561965913888899753461735347970011693256316751660678950830027510255804846105583465055446615090444309583050775808509297040039680057435342253926566240898195863631588888936364129920059308455669454034010391478238784189888594672336242763795138176353222845524644040094258962433613354036104643881925238489224010194193088911666165584229424668165441688927790460608264864204237717002054744337988941974661214699689706521543006262604535890998125752275942608772174376107314217749233048217904944409836238235772306749874396760463376480215133461333478395682746608242585133953883882226786118030184028136755970045385534758453247");
	//final static BigInteger prime = new BigInteger("99");
//	final static BigInteger prime = BigInteger.probablePrime(128, new Random());

	public static final CharSequence APP = "GKE";

	
	
	CharSequence owner;
	//CharSequence folder;
	Collection<CharSequence> formats;
    private GKEMessage_Impl[] messages = null;

	private Map<CharSequence, ASAPMessageReceivedListener> messageReceivedListener = new HashMap<>();
	private ASAPJavaApplication asap;

	GKENode() throws IOException, ASAPException {
	}
	
	public GKENode(String owner, String folder, Collection<CharSequence> formats)
			throws IOException, ASAPException {
		this.asap = ASAPJavaApplicationFS.createASAPJavaApplication(owner, folder, formats);
		this.owner = owner;
		//this.folder = folder;
		this.formats = formats;
	}
	
	@Override
	public void handleConnection(InputStream inputStream, OutputStream outputStream) throws IOException, ASAPException {
		this.asap.handleConnection(inputStream, outputStream);
	}

	@Override
	public void sendASAPMessage(CharSequence format, CharSequence uri, Collection<CharSequence> recipients, byte[] message)
			throws ASAPException, IOException {
		this.asap.sendASAPMessage(format, uri, recipients, message);  
	}

	@Override
	public void setASAPMessageReceivedListener(CharSequence format, ASAPMessageReceivedListener listener)
			throws ASAPException, IOException {
		// TODO: possibly add additional code here later
		this.asap.setASAPMessageReceivedListener(format, listener);
	}
		
	private String getLogStart() {
		return this.getClass().getSimpleName() + ": ";
	}	

	public CharSequence getName() {
		return owner;
	}

	public void setName(CharSequence name) {
		this.owner = name;
	}

	//public CharSequence getFolder() {
	//	return folder;
	//}

	//public void setFolder(CharSequence folder) {
	//	this.folder = folder;
	//}

	public Collection<CharSequence> getFormats() {
		return formats;
	}

	public void setFormats(Collection<CharSequence> formats) {
		this.formats = formats;
	}

	public static BigInteger getGenerator() {
		return generator;
	}


	public static CharSequence getApp() {
		return APP;
	}

	public Map<CharSequence, ASAPMessageReceivedListener> getMessageReceivedListener() {
		return messageReceivedListener;
	}

	public void setMessageReceivedListener(Map<CharSequence, ASAPMessageReceivedListener> messageReceivedListener) {
		this.messageReceivedListener = messageReceivedListener;
	}

	//public GKEMessage_Impl[] getMessages() {
	//	return messages;
	//}

	public void setMessages(GKEMessage_Impl[] messages) {
		this.messages = messages;
	}
	
	private class MessageListenerWrapper implements ASAPChunkReceivedListener {
	    private final ASAPMessageReceivedListener listener;

	    public MessageListenerWrapper(ASAPMessageReceivedListener listener) throws ASAPException {
		    System.out.println("*Inside MesageListenerWrapper");

	        if(listener == null) throw new ASAPException("listener must not be null");
	        this.listener = listener;
	    }
		@Override
		public void chunkReceived(String format, String sender, String uri, int era) {
			// TODO: possible update later
			 /*System.out.println(getLogStart() + "Chunk received - convert to gke message listener");
	         try {
	             ASAPEngine engine = multiEngine.getEngineByFormat(APP);
	             //will probably need to adjust to GKEMessage
	             ASAPMessages messages = engine.getIncomingChunkStorage(sender).getASAPChunkCache(uri, era);
	             this.listener.asapMessagesReceived(messages);
	         } catch (ASAPException | IOException e) {
	             System.err.println(getLogStart() + e.getLocalizedMessage());
	         }*/
	     }
	}
}


