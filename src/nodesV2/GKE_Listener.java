package nodesV2;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import net.sharksystem.asap.apps.ASAPMessageReceivedListener;
import net.sharksystem.asap.apps.ASAPMessages;

public class GKE_Listener implements ASAPMessageReceivedListener {

    private boolean hasReceivedMessage = false;
    private Queue<ASAPMessages> messagesList;
    
    public GKE_Listener() {
    	this.messagesList = new LinkedList<ASAPMessages>();
    }

    @Override
    public void asapMessagesReceived(ASAPMessages messages) {
        try {
            System.out.println("#message == " + messages.size());
            this.hasReceivedMessage = true;
            this.messagesList.add(messages);
        } catch (IOException e) {
            // do something with it.
        }
    }

    public boolean hasReceivedMessage() {
        return this.hasReceivedMessage;
    }
    
    public ASAPMessages popASAPMessages() {
    	return messagesList.remove();
    }
}