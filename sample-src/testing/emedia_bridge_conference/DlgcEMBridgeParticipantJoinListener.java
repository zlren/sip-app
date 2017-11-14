/**
 * DIALOGIC CONFIDENTIAL
 * Copyright (C) 2005-2012 Dialogic Corporation. All Rights Reserved.
 *
 * The source code contained or described herein and all documents related to
 * the source code ("Material") are owned by Dialogic Corporation or its
 * suppliers or licensors.  Title to the Material remains with Dialogic
 * Corporation or its suppliers and licensors.  The Material contains trade
 * secrets and proprietary and confidential information of Dialogic or its
 * suppliers and licensors.  The Material is protected by worldwide copyright
 * and trade secret laws and treaty provisions.  No part of the Material may be
 * used, copied, reproduced, modified, published, uploaded, posted, transmitted,
 * distributed, or disclosed in any way without Dialogic's prior express written
 * permission.
 *
 * No license under any patent, copyright, trade secret or other intellectual
 * property right is granted to or conferred upon you by disclosure or delivery
 * of the Materials, either expressly, by implication, inducement, estoppel or
 * otherwise.  Any license under such intellectual property rights must be
 * express and approved by Dialogic in writing.
 */

package testing.emedia_bridge_conference;

import java.io.Serializable;

import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.JoinEvent;
import javax.media.mscontrol.join.JoinEventListener;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class DlgcEMBridgeParticipantJoinListener implements JoinEventListener , Serializable{

	private static final long serialVersionUID = 33489699088881L;
	protected DlgcEMBridgeParticipant 		participant;
	private static Logger log = LoggerFactory.getLogger(DlgcEMBridgeParticipantJoinListener.class);
	
	
	public DlgcEMBridgeParticipantJoinListener(DlgcEMBridgeParticipant assocParticipant)
	{
		participant = assocParticipant;
	}
	
	//note that JoinEvent in synchronous mode will not be call anymore for join() and unjoin() methods 
	//John June 3 2014
	
	@Override
	public void onEvent(JoinEvent theEvent) {
		log.debug("Entering DlgcEMBridgeParticipantJoinListener::onEvent");
		log.debug("DlgcEMBridgeParticipantJoinListener::Source" + theEvent.getEventType() );
		log.debug("DlgcEMBridgeParticipantJoinListener::Source" + theEvent.getSource().toString());
		log.debug("DlgcEMBridgeParticipantJoinListener::ErrorText" + theEvent.getErrorText());
		//MediaSession myMediaSession = ((NetworkConnection)(theEvent.getSource())).getMediaSession();
		//SipSession mySipSession = (SipSession) myMediaSession.getAttribute("SIP_SESSION");
		
		if ( theEvent.isSuccessful() ) {
			log.debug("DlgcEMBridgeParticipant::DlgcEMBridgeParticipantJoinListener");
	
			try {
				if ( theEvent.getEventType() == JoinEvent.UNJOINED) {
					participant.presentState.unjoinConferenceResponse(participant);
				} else if (theEvent.getEventType() == JoinEvent.JOINED  ) {
					participant.presentState.joinConferenceResponse(participant);
				} else {
					log.debug("Unknown Join Event received from the dialogic connector.");
				}
			} catch (MsControlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			
			try {
				participant.release();
			} catch (MsControlException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
}
