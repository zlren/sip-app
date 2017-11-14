/**
 * DIALOGIC CONFIDENTIAL      
 * Copyright (C) 2005-2013 Dialogic Corporation. All Rights Reserved.
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

package testing.reference_conf_demo_with_outb_call_layout;

import java.io.Serializable;

import javax.media.mscontrol.MediaEvent;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.JoinableContainer;
import javax.servlet.sip.SipApplicationSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.reference_conf_demo_with_outb_call_layout.DlgcOutbCallConferenceStorage.DlgcConferenceStorageMgrException;


/**
 * Event listener for conference Player, Signal and Network events
 */
public class DlgcReferenceMixerMediaListenerWOBC<T extends MediaEvent<?>> implements MediaEventListener<T>, Serializable {

	private static final long serialVersionUID = 1;
	transient protected DlgcReferenceConferenceParticipantWOBC 		participant;
	transient String participantId = null;
	private static Logger log = LoggerFactory.getLogger(DlgcReferenceMixerMediaListenerWOBC.class);
	
	public DlgcReferenceMixerMediaListenerWOBC()
	{
		
	}
	
	@Override
	public void onEvent(T theEvent) {
		log.debug("Entering DlgcReferenceMixerMediaListener::onEvent");
		
		//EventType joinEvType = theEvent.getEventType();
		log.debug("DlgcReferenceMixerMediaListener::Source" + theEvent.getEventType() );
		log.debug("DlgcReferenceMixerMediaListener::Source" + theEvent.getSource().toString());
		log.debug("DlgcReferenceMixerMediaListener::ErrorText" + theEvent.getErrorText());
		JoinableContainer container = (JoinableContainer)theEvent.getSource();
		MediaSession ms = container.getMediaSession();
		DlgcOutbCallConferenceStorage conferenceStorage = (DlgcOutbCallConferenceStorage) ms.getAttribute("DlgcOutbCallConferenceStorage");
		DlgcReferenceConferenceWOBC conf;
		try {
			conf = conferenceStorage.getConferenceInfo();
			participantId = (String) ms.getAttribute("ParticipantId");
			participant = conf.findParticipant(participantId);
		} catch (DlgcConferenceStorageMgrException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		try {
			SipApplicationSession sas = DlgcOutbCallConferenceStorage.loadSas();
			//this.executeAsync(sas.getId(), participant, theEvent );
			this.execute(sas.getId(), participant, theEvent );

		} catch (DlgcConferenceStorageMgrException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
			
		
	}
	
	/**********
	protected void executeAsync(final String sasId, final DlgcReferenceConferenceParticipantWOBC participant, final MediaEvent<?>   theEvent ) throws MsControlException 
	{
		log.debug("DlgcReferenceNcJoinMixerListenerWOBC::Entering executeAsync"); 

		if ( sasId == null ) {
			log.error("DlgcReferenceNcJoinMixerListenerWOBC::executeAsync cant execute sasId is NULL..returning immediately.");
		}
		try {
			final SipSessionsUtilAdapter occas5SessionUtil = (SipSessionsUtilAdapter) DlgcReferenceConferenceWithOutBCallServlet.getSSU();
			//final String sasId = this.getProxyId();

			((WlssSipSessionsUtil)occas5SessionUtil).doAsynchronousAction(sasId, new WlssAsynchronousAction()
			{
				private static final long serialVersionUID = 1L;

				public void run(SipApplicationSession appSession) {
					try {
						participant.myConference.myControlServlet.conferenceStorage.saveConference(participant.myConference);
					} catch (DlgcConferenceStorageMgrException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}); 
		} catch (Exception e) {

			//throw new MsControlException(appLogId+"Failed to get player. play failed", e);
		}
		log.debug("DlgcReferenceNcJoinMixerListenerWOBC::Leaving executeAsync"); 


	}
	******/
	
	protected void execute(final String sasId, final DlgcReferenceConferenceParticipantWOBC participant, final MediaEvent<?>   theEvent ) throws MsControlException 
	{
		log.debug("DlgcReferenceNcJoinMixerListenerWOBC::Entering execute non async"); 

		if ( sasId == null ) {
			log.error("DlgcReferenceNcJoinMixerListenerWOBC::executeAsync cant execute sasId is NULL..returning immediately.");
		}
		try {
			try {
				participant.myConference.myControlServlet.conferenceStorage.saveConference(participant.myConference);
			} catch (DlgcConferenceStorageMgrException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (Exception e) {

			//throw new MsControlException(appLogId+"Failed to get player. play failed", e);
		}
		log.debug("DlgcReferenceNcJoinMixerListenerWOBC::Leaving execute non async"); 


	}
	
}
