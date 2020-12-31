package labrador.server.messaging;

import java.io.Serializable;

import labrador.server.LabradorServer;

import pandorasbox.simpleclientserver.messaging.Message;

/**
 * This is a marker interface for {@link Message}s that a {@link LabradorServer}
 * knows how to handle.
 * 
 * @author jtwebb
 * 
 */
public interface LabradorMessage extends Message, Serializable {

}
