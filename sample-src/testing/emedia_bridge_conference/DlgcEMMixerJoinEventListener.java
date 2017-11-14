
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

import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.mixer.MixerEvent;


import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.JoinEvent;
import javax.media.mscontrol.join.JoinEventListener;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class DlgcEMMixerJoinEventListener implements JoinEventListener, Serializable
{
	private static final long serialVersionUID = 7789699088881L;
	private static Logger log = LoggerFactory.getLogger(DlgcEMMixerJoinEventListener.class);
	

	
	
	@Override
	public void onEvent(JoinEvent theEvent) {
		log.debug("Entering DlgcEMMixerJoinEventListener::onEvent");
		log.debug("DlgcEMMixerJoinEventListener::Source" + theEvent.getEventType() );
		log.debug("DlgcEMMixerJoinEventListener::Source" + theEvent.getSource().toString());
		log.debug("DlgcEMMixerJoinEventListener::ErrorText" + theEvent.getErrorText());
		//MediaSession myMediaSession = ((NetworkConnection)(theEvent.getSource())).getMediaSession();
		//SipSession mySipSession = (SipSession) myMediaSession.getAttribute("SIP_SESSION");
		
		if ( theEvent.isSuccessful() ) {
			log.debug("DlgcEMMixerJoinEventListener:: TODO On Mixer Join");
	
		//	try {
			//	log.debug("DlgcQuickConferenceParticipant::DlgcNcJoinMixerListener:: TODO onLegJoinToConference");
				//myState.onLegJoinToConference(jcs, mySipSession, theEvent);
			//} catch (MsControlException e) {
				// TODO Auto-generated catch block
			//	log.debug("DlgcQuickConferenceParticipant::DlgcNcJoinMixerListener::onEvent Exception error joining leg to conference...terminating");
			//	participant.release();
				//terminate(mySipSession,  e);
			//}
		}else {
			
			//try {
			//	participant.release();
			//} catch (MsControlException e1) {
				// TODO Auto-generated catch block
			//	e1.printStackTrace();
			//}
			//terminate(mySipSession,  e);
		}
	}
	
}
