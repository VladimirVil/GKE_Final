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
	// the final values are from RFC3526, id 16. Modular Exponential DH Group 4096
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
	//same number converted into int using https://www.rapidtables.com/convert/number/hex-to-decimal.html converter. 
	final static BigInteger prime = new BigInteger("10443888814131525066796027198465295458312690609921350090"
			+"2258875644433817202232269071044404666980978393011158573789036269186012707927049545451721867301"
			+"6928427459146001866885779762982229321192368303346235204368051010309155674155697460347176946394"
			+"0765351572849948952848216337009218117167389724518349794558970103063334685907513583651387822503"
			+"7226911796898519432244453568741552200715163863814145617842062127782267499502799027867345862954"
			+"4391736919766299005511505446177668154446234882665961680796576903199116089347634947187778906528"
			+"0080047566925716669229641225661745827767073324523710012721637768412293183249031257407135741410"
			+"0512456196591388889975346173534797001169325631675166067895083002751025580484610558346505544661"
			+"5090444309583050775808509297040039680057435342253926566240898195863631588888936364129920059308"
			+"4556694540340103914782387841898885946723362427637951381763532228455246440400942589624336133540"
			+"3610464388192523848922401019419308891166616558422942466816544168892779046060826486420423771700"
			+"2054744337988941974661214699689706521543006262604535890998125752275942608772174376107314217749"
			+"2330482179049444098362382357723067498743967604633764802151334613334783956827466082425851339538"
			+"83882226786118030184028136755970045385534758453247");
	public static final CharSequence APP = "GKE";
	CharSequence owner;
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


