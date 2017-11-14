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

import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.JoinEvent;
import javax.media.mscontrol.join.JoinEventListener;
import javax.media.mscontrol.join.JoinableContainer;
import javax.servlet.sip.SipApplicationSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.reference_conf_demo_with_outb_call_layout.DlgcOutbCallConferenceStorage.DlgcConferenceStorageMgrException;



public class DlgcReferenceNcJoinMixerListenerWOBC implements JoinEventListener, Serializable {

	private static final long serialVersionUID = 1;
	transient protected DlgcReferenceConferenceParticipantWOBC 		participant;
	private static Logger log = LoggerFactory.getLogger(DlgcReferenceNcJoinMixerListenerWOBC.class);
	transient String participantId = null;
	
	
	public DlgcReferenceNcJoinMixerListenerWOBC(DlgcReferenceConferenceParticipantWOBC p)
	{
		participant = p;
		if ( p != null )
			participantId = participant.clientSSID;
	}
	
	@Override
	public void onEvent(JoinEvent theEvent) {
		log.debug("Entering DlgcNcJoinMixerListener::onEvent");
		log.debug("DlgcNcJoinMixerListener::EventType" + theEvent.getEventType() );
		log.debug("DlgcNcJoinMixerListener::Event Source" + theEvent.getSource().toString());
		log.debug("DlgcNcJoinMixerListener::Event ErrorText" + theEvent.getErrorText());
		
		try {
			JoinableContainer container = (JoinableContainer)theEvent.getSource();
			MediaSession ms = container.getMediaSession();
			DlgcOutbCallConferenceStorage conferenceStorage = (DlgcOutbCallConferenceStorage) ms.getAttribute("DlgcOutbCallConferenceStorage");
			DlgcReferenceConferenceWOBC conf = conferenceStorage.getConferenceInfo();
			participantId = (String) ms.getAttribute("ParticipantId");
			participant = conf.findParticipant(participantId);

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
	/***********
	protected void executeAsync(final String sasId, final DlgcReferenceConferenceParticipantWOBC participant, final JoinEvent theEvent ) throws MsControlException 
	{
		log.debug("DlgcReferenceNcJoinMixerListenerWOBC::Entering executeAsync"); 
		
		if ( sasId == null ) {
			log.error("DlgcReferenceNcJoinMixerListenerWOBC::executeAsync cant execute sasId is NULL..returning immediately.");
		}
	    try {
	    	final SipSessionsUtilAdapter occas5SessionUtil = (SipSessionsUtilAdapter) DlgcReferenceConferenceWithOutBCallServlet.getSSU();
	    	
			((WlssSipSessionsUtil)occas5SessionUtil).doAsynchronousAction(sasId, new WlssAsynchronousAction()
			{
				private static final long serialVersionUID = 1L;

				public void run(SipApplicationSession appSession) {
						log.debug("Entering PlayerProxy::playAsync Task run() => Media Server");
						try {
							if ( theEvent.isSuccessful() ) {
								log.debug("DlgcReferenceNcJoinMixerListener::event:: NC was joined to the Mixer");
								try {
									log.debug("DlgcReferenceNcJoinMixerListener::calling participant presentState with joinConferenceResponse event.");
									participant.getPresentState().joinConferenceResponse(participant);
									
									try {
										participant.myConference.myControlServlet.conferenceStorage.saveConference(participant.myConference);
									} catch (DlgcConferenceStorageMgrException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
								} catch (MsControlException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}else {
								
								try {
									participant.release();
								} catch (MsControlException e1) {
									e1.printStackTrace();
								}
							}
						} catch (Exception e) {
							log.debug(e.toString());
						}
						log.debug("DlgcReferenceNcJoinMixerListenerWOBC::executeAsync run() => Media Server");
					}
				}); 
	    } catch (Exception e) {
		  
	      //throw new MsControlException(appLogId+"Failed to get player. play failed", e);
	    }
		log.debug("DlgcReferenceNcJoinMixerListenerWOBC::Leaving executeAsync"); 

		
	}
	*******/
	
	protected void execute(final String sasId, final DlgcReferenceConferenceParticipantWOBC participant, final JoinEvent theEvent ) throws MsControlException 
	{
		log.debug("DlgcReferenceNcJoinMixerListenerWOBC::Entering execute non async way");

		if ( sasId == null ) {
			log.error("DlgcReferenceNcJoinMixerListenerWOBC::executeAsync cant execute sasId is NULL..returning immediately.");
		}
		try {
			log.debug("Entering PlayerProxy::playAsync Task run() => Media Server");
			try {
				if ( theEvent.isSuccessful() ) {
					log.debug("DlgcReferenceNcJoinMixerListener::event:: NC was joined to the Mixer");
					try {
						log.debug("DlgcReferenceNcJoinMixerListener::calling participant presentState with joinConferenceResponse event.");
						participant.getPresentState().joinConferenceResponse(participant);

						try {
							participant.myConference.myControlServlet.conferenceStorage.saveConference(participant.myConference);
						} catch (DlgcConferenceStorageMgrException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					} catch (MsControlException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else {

					try {
						participant.release();
					} catch (MsControlException e1) {
						e1.printStackTrace();
					}
				}
			} catch (Exception e) {
				log.debug(e.toString());
			}
			log.debug("DlgcReferenceNcJoinMixerListenerWOBC::executeAsync run() => Media Server");

		} catch (Exception e) {

			//throw new MsControlException(appLogId+"Failed to get player. play failed", e);
		}
		log.debug("DlgcReferenceNcJoinMixerListenerWOBC::Leaving execute non async way"); 


	}
	
}
