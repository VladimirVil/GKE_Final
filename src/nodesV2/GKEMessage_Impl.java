package nodesV2;

import net.sharksystem.asap.ASAPException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.StringTokenizer;


public class GKEMessage_Impl implements GKEMessage{
    private static final String DELIMITER = "^^^^";
    private CharSequence senderID;
    private CharSequence messageBody;
    private Date sentDate;
    private CharSequence serializedMessage;

    public GKEMessage_Impl(CharSequence senderID, CharSequence messageBody, Date sentDate) {
        this.senderID = senderID;
        this.messageBody = messageBody;
        this.sentDate = sentDate;
    }

    private GKEMessage_Impl(CharSequence asapMessage) throws ASAPException {
        this.deserializeMessage(asapMessage); // throws exception if malformed

        // not malformed - set rest of it
        this.serializedMessage = asapMessage;
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
                + df.format(this.sentDate) + DELIMITER
                + this.messageBody;
    }

    private void deserializeMessage(CharSequence message) throws ASAPException {
        // parse of the incoming message
        StringTokenizer st = new StringTokenizer(message.toString(), DELIMITER);
        if(!st.hasMoreTokens()) {
            throw new ASAPException("message with no token!");
        }

        this.senderID = st.nextToken();

        if(!st.hasMoreTokens()) {
            throw new ASAPException("message with no tokes");
        }

        String dateString = st.nextToken();

        if(!st.hasMoreTokens()) {
            throw new ASAPException("message with no tokes");
        }

        this.messageBody = st.nextToken();

        // no set sent date
        DateFormat df = DateFormat.getInstance();

        try {
            this.sentDate = df.parse(dateString);
        } catch (ParseException e) {
            throw new ASAPException(" date string in the message is : " +
                    e.getLocalizedMessage());
        }
    }

//    @Override
//    public CharSequence getSenderID() {
//        return this.senderID;
//    }

    @Override
    public byte[] getContent() throws ASAPException {
        return this.messageBody.toString().getBytes();
    }

    @Override
    public CharSequence getContentAsString() throws ASAPException, IOException {
        return this.messageBody;
    }

    @Override
    public Date getSentDate() {
        return this.sentDate;
    }

    @Override
    public boolean isLaterThan(GKEMessage message) throws ASAPException, IOException {
        Date sentDateMessage = message.getSentDate();
        Date sentDateMe = this.getSentDate();

        return sentDateMe.after(sentDateMessage);
    }

	@Override
	public CharSequence getSenderName() throws ASAPException {
		// TODO Auto-generated method stub
		return null;
	}

	public CharSequence getSenderID() {
		return senderID;
	}

	public void setSenderID(CharSequence senderID) {
		this.senderID = senderID;
	}

	public CharSequence getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(CharSequence messageBody) {
		this.messageBody = messageBody;
	}

	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	public void setSerializedMessage(CharSequence serializedMessage) {
		this.serializedMessage = serializedMessage;
	}
}

