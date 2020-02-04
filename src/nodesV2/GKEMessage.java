package nodesV2;

import java.io.IOException;
import java.util.Date;

import net.sharksystem.asap.ASAPException;

public interface GKEMessage {
    /** return sender name  */
    CharSequence getSenderName() throws ASAPException;

    /** return message content as byte */
    byte[] getContent() throws ASAPException, IOException;

    /** return message content as string */
    CharSequence getContentAsString() throws ASAPException, IOException;
    
    /** return whole message including additional fields (keys for instance) */
    CharSequence getSerializedMessage();

    /** get sent date */
    Date getSentDate() throws ASAPException, IOException;

    boolean isLaterThan(GKEMessage message) throws ASAPException, IOException;
}
