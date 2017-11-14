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

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MediaEvent;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.JoinEvent;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.networkconnection.SdpPortManagerException;
import javax.media.mscontrol.resource.Resource;
import javax.media.mscontrol.resource.ResourceContainer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.reference_conf_demo_with_outb_call_layout.DlgcOutbCallConferenceStorage.DlgcConferenceStorageMgrException;
import testing.reference_conf_demo_with_outb_call_layout.DlgcReferenceConferenceParticipantWOBC.LegMenuOperation;

/**
 * Event listener for conference Player, Signal and Network events
 */
public class DlgcReferenceParticipantMediaListenerWOBC<T extends MediaEvent<?>> implements MediaEventListener<T>, Serializable {

	private static final long serialVersionUID = 1;
	transient protected DlgcReferenceConferenceParticipantWOBC 		participant;
	transient String participantId = null;
	private static Logger log = LoggerFactory.getLogger(DlgcReferenceParticipantMediaListenerWOBC.class);
	
	
	
	@SuppressWarnings("unchecked")
	public void onEvent(T anEvent) {


		if ( !anEvent.isSuccessful() ) {
			log.debug("DlgcReferenceParticipantMediaListener Error onEvent received problem is: " + anEvent.getErrorText());
			return;
		}
		log.debug("DlgcReferenceParticipantMediaListener  received event =" + anEvent.getEventType().toString() );

		
		Resource r = (Resource)anEvent.getSource();
		ResourceContainer container = r.getContainer();
		MediaSession ms = container.getMediaSession();
		DlgcOutbCallConferenceStorage conferenceStorage = (DlgcOutbCallConferenceStorage) ms.getAttribute("DlgcOutbCallConferenceStorage");
		DlgcReferenceConferenceWOBC conf;
		try {
			conf = conferenceStorage.getConferenceInfo();
			participantId = (String) ms.getAttribute("ParticipantId");
			participant = conf.findParticipant(participantId);
			
		} catch (DlgcConferenceStorageMgrException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		SipApplicationSession sas;
		try {
			sas = DlgcOutbCallConferenceStorage.loadSas();
			//executeAsync(sas.getId(),  participant, anEvent );
			execute(sas.getId(),  participant, anEvent );

		} catch (DlgcConferenceStorageMgrException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/*********
	protected void executeAsync(final String sasId, final DlgcReferenceConferenceParticipantWOBC participant, final MediaEvent<?> anEvent ) throws MsControlException 
	{
		log.debug("DlgcReferenceParticipantMediaListener::Entering executeAsync"); 

		if ( sasId == null ) {
			log.error("DlgcReferenceParticipantMediaListener::executeAsync cant execute sasId is NULL..returning immediately.");
		}

		try {
			final SipSessionsUtilAdapter occas5SessionUtil = (SipSessionsUtilAdapter) DlgcReferenceConferenceWithOutBCallServlet.getSSU();
			((WlssSipSessionsUtil)occas5SessionUtil).doAsynchronousAction(sasId, new WlssAsynchronousAction() 
			{
				private static final long serialVersionUID = 1L;

				public void run(SipApplicationSession appSession)
				{
					log.debug("Entering executeAsync::executeAsync Task run() => Media Server");
					try {
						if (anEvent instanceof PlayerEvent) {
							participant.loadSipSession();
							if ( anEvent.getEventType() == PlayerEvent.RESUMED ) {
								log.info("Received Player Event: Play Resumed which means Play Started...");
								return;
							} 
							log.debug("DlgcReferenceParticipantMediaListener handling player event participant ObjID: " + participant.toString()  );
							//PlayerEvent playerEvent = (PlayerEvent)anEvent;
							log.info("["+participant.loadSipSession().getCallId()+"] RECEIVED PLAYER EVENT: "+anEvent.getEventType());
							//TODO: call the correct state here from the state machine object
							//myState.onPlayerEvent(jcs, mySipSession, playerEvent);
							participant.processPlayerEvent((PlayerEvent)anEvent);
						} else if (anEvent instanceof SignalDetectorEvent) {
							participant.loadSipSession();
							log.debug("DlgcReferenceParticipantMediaListener handling signal detector event participant ObjID: " + participant.toString() );
							log.info("["+participant.loadSipSession().getCallId()+"] RECEIVED SIGNAL DETECTOR EVENT: "+anEvent.getEventType());
							SignalDetectorEvent signalDetectorEvent = (SignalDetectorEvent)anEvent;
							if ( signalDetectorEvent.getError() == MediaErr.NO_ERROR ) {
								log.debug("DlgcReferenceParticipantMediaListener::onEvent::SignalDetector:: event no error received");
								participant.processDetectorEvent(signalDetectorEvent);
							}else {
								log.debug("DlgcReferenceParticipantMediaListener::onEvent::SignalDetector:: event error " + signalDetectorEvent.getErrorText());
							}
						} else if (anEvent instanceof SdpPortManagerEvent) {
							log.debug("DlgcReferenceParticipantMediaListener received event of type SdpPortManagerEvent participant ObjID: " + participant.toString() );
							SdpPortManagerEvent sdpPortManagerEvent = (SdpPortManagerEvent)anEvent;

							SdpPortManager sdp = sdpPortManagerEvent.getSource();

							if (anEvent.getEventType().equals(SdpPortManagerEvent.OFFER_GENERATED) ) {
								log.debug("DlgcReferenceParticipantMediaListener:: OFFER_GENERATED... calling state machine msAnswerGeneratedResponse()");
								participant.presentState.msAnswerGeneratedResponse(participant, (SdpPortManagerEvent) anEvent);	
							} else {

								//MediaSession myMediaSession = sdpPortManagerEvent.getSource().getMediaSession();
								String sipSessionCallId = participant.loadSipSession().getCallId();
								log.info("["+sipSessionCallId+"] RECEIVED SDP PORT MANAGER EVENT: "+anEvent.getEventType());
								log.info("["+sipSessionCallId+"] RECEIVED SDP PORT MANAGER EVENTB: "+anEvent.getErrorText());
								log.info("["+sipSessionCallId+"] RECEIVED SDP PORT MANAGER EVENTC: "+anEvent.getError());
								log.info("["+sipSessionCallId+"] CHECK: "+ participant.loadSipSession()+" / "+participant.loadSipSession().isValid());

								if (participant.loadSipSession().isValid())
								{
									SipServletRequest request = (SipServletRequest) participant.loadSipSession().getAttribute("UNANSWERED_INVITE");
									if ((anEvent.getEventType().equals(JoinEvent.JOINED)) || (anEvent.getEventType().equals(JoinEvent.UNJOINED)) )		//mute unmute operation
									{
										participant.loadSipSession();
										DlgcReferenceConferenceParticipantWOBC.LegMenuOperation  mo  = DlgcReferenceConferenceParticipantWOBC.getMenuOperation(participant.loadSipSession());
										if ( mo.compareTo(LegMenuOperation.MUTE) == 0) {
											log.debug("DlgcReferenceParticipantMediaListener MUTE TODO");
										} else if ( mo.compareTo(LegMenuOperation.UNMUTE) == 0) {  //This is your Initial State
											log.debug("DlgcReferenceParticipantMediaListener TODO");
										} else if ( mo.compareTo(LegMenuOperation.PARK) == 0 ) {
											//TODO myState.playYouAreParked(jcs,mySipSession);
											log.debug("DlgcReferenceParticipantMediaListener PARK TODO");
										} else if ( mo.compareTo(LegMenuOperation.UNPARK) == 0 ) {
											//TODO myState.playYouAreUnParked(jcs,mySipSession);
											log.debug("DlgcReferenceParticipantMediaListener UNPARK TODO");
										}
										if (anEvent.getEventType().equals(JoinEvent.JOINED) ){
											log.debug("DlgcReferenceParticipantMediaListener:: LEG WAS JOINED");
											participant.presentState.joinConferenceResponse(participant);
										}else if (anEvent.getEventType().equals(JoinEvent.UNJOINED) ){
											log.debug("DlgcReferenceParticipantMediaListener:: LEG WAS UNJOINED");
										}
									} else if (anEvent.getEventType().equals(SdpPortManagerEvent.ANSWER_GENERATED)) //due to ANSWER_GENERATED EVENT from processSdpOffer(remoteSdp);
									{
										SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
										try
										{
											//send 200 OK to sip phone uac
											log.debug("DlgcReferenceParticipantMediaListener:: ANSWER GENERATED");
											response.setContent(sdp.getMediaServerSessionDescription(), "application/sdp");
											response.send();
											participant.presentState.joinConferenceResponse(participant);
										}
										catch (UnsupportedEncodingException e)
										{
											e.printStackTrace();
										}
										catch (SdpPortManagerException e)
										{
											e.printStackTrace();
										}
										catch (IOException e)
										{
											e.printStackTrace();
										}
									} else if (anEvent.getEventType().equals(SdpPortManagerEvent.NETWORK_STREAM_FAILURE)) {
										participant.loadSipSession();
										participant.release();
										log.debug("DlgcReferenceParticipantMediaListener:: This is not good we got an Network Conenction failure");
										return;
									}
								}
							}

							try {
								if (participant != null ) {
									participant.myConference.myControlServlet.conferenceStorage.saveConference(participant.myConference);
								}
							} catch (DlgcConferenceStorageMgrException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} catch (Exception e) {
					
							log.debug(e.toString());
					} 


				} //run
			}); //doAsync
		}catch (Exception e) {

			//throw new MsControlException(appLogId+"Failed to get player. play failed", e);
		}
		log.debug("DlgcReferenceParticipantMediaListener::Leaving executeAsync"); 
	}
	*******/
	
	
	
	protected void execute(final String sasId, final DlgcReferenceConferenceParticipantWOBC participant, final MediaEvent<?> anEvent ) throws MsControlException 
	{
		log.debug("DlgcReferenceParticipantMediaListener::Entering execute non Async way"); 

		if ( sasId == null ) {
			log.error("DlgcReferenceParticipantMediaListener::executeAsync cant execute sasId is NULL..returning immediately.");
		}


		log.debug("Entering executeAsync::executeAsync Task run() => Media Server");
		try {
			if (anEvent instanceof PlayerEvent) {
				participant.loadSipSession();
				if ( anEvent.getEventType() == PlayerEvent.RESUMED ) {
					log.info("Received Player Event: Play Resumed which means Play Started...");
					return;
				} 
				log.debug("DlgcReferenceParticipantMediaListener handling player event participant ObjID: " + participant.toString()  );
				log.info("["+participant.loadSipSession().getCallId()+"] RECEIVED PLAYER EVENT: "+anEvent.getEventType());
				participant.processPlayerEvent((PlayerEvent)anEvent);
			} else if (anEvent instanceof SignalDetectorEvent) {
				participant.loadSipSession();
				log.debug("DlgcReferenceParticipantMediaListener handling signal detector event participant ObjID: " + participant.toString() );
				log.info("["+participant.loadSipSession().getCallId()+"] RECEIVED SIGNAL DETECTOR EVENT: "+anEvent.getEventType());
				SignalDetectorEvent signalDetectorEvent = (SignalDetectorEvent)anEvent;
				if ( signalDetectorEvent.getError() == MediaErr.NO_ERROR ) {
					log.debug("DlgcReferenceParticipantMediaListener::onEvent::SignalDetector:: event no error received");
					participant.processDetectorEvent(signalDetectorEvent);
				}else {
					log.debug("DlgcReferenceParticipantMediaListener::onEvent::SignalDetector:: event error " + signalDetectorEvent.getErrorText());
				}
			} else if (anEvent instanceof SdpPortManagerEvent) {
				log.debug("DlgcReferenceParticipantMediaListener received event of type SdpPortManagerEvent participant ObjID: " + participant.toString() );
				SdpPortManagerEvent sdpPortManagerEvent = (SdpPortManagerEvent)anEvent;

				SdpPortManager sdp = sdpPortManagerEvent.getSource();

				if (anEvent.getEventType().equals(SdpPortManagerEvent.OFFER_GENERATED) ) {
					//participant.loadSipSession();
					log.debug("DlgcReferenceParticipantMediaListener:: OFFER_GENERATED... calling state machine msAnswerGeneratedResponse()");
					participant.presentState.msAnswerGeneratedResponse(participant, (SdpPortManagerEvent) anEvent);	
				} else {

					//MediaSession myMediaSession = sdpPortManagerEvent.getSource().getMediaSession();
					String sipSessionCallId = participant.loadSipSession().getCallId();
					log.info("["+sipSessionCallId+"] RECEIVED SDP PORT MANAGER EVENT: "+anEvent.getEventType());
					log.info("["+sipSessionCallId+"] RECEIVED SDP PORT MANAGER EVENTB: "+anEvent.getErrorText());
					log.info("["+sipSessionCallId+"] RECEIVED SDP PORT MANAGER EVENTC: "+anEvent.getError());
					log.info("["+sipSessionCallId+"] CHECK: "+ participant.loadSipSession()+" / "+participant.loadSipSession().isValid());

					if (participant.loadSipSession().isValid())
					{
						SipServletRequest request = (SipServletRequest) participant.loadSipSession().getAttribute("UNANSWERED_INVITE");
						if ((anEvent.getEventType().equals(JoinEvent.JOINED)) || (anEvent.getEventType().equals(JoinEvent.UNJOINED)) )		//mute unmute operation
						{
							participant.loadSipSession();
							DlgcReferenceConferenceParticipantWOBC.LegMenuOperation  mo  = DlgcReferenceConferenceParticipantWOBC.getMenuOperation(participant.loadSipSession());
							if ( mo.compareTo(LegMenuOperation.MUTE) == 0) {
								log.debug("DlgcReferenceParticipantMediaListener MUTE TODO");
							} else if ( mo.compareTo(LegMenuOperation.UNMUTE) == 0) {  //This is your Initial State
								log.debug("DlgcReferenceParticipantMediaListener TODO");
							} else if ( mo.compareTo(LegMenuOperation.PARK) == 0 ) {
								//TODO myState.playYouAreParked(jcs,mySipSession);
								log.debug("DlgcReferenceParticipantMediaListener PARK TODO");
							} else if ( mo.compareTo(LegMenuOperation.UNPARK) == 0 ) {
								//TODO myState.playYouAreUnParked(jcs,mySipSession);
								log.debug("DlgcReferenceParticipantMediaListener UNPARK TODO");
							}
							if (anEvent.getEventType().equals(JoinEvent.JOINED) ){
								log.debug("DlgcReferenceParticipantMediaListener:: LEG WAS JOINED");
								participant.presentState.joinConferenceResponse(participant);
							}else if (anEvent.getEventType().equals(JoinEvent.UNJOINED) ){
								log.debug("DlgcReferenceParticipantMediaListener:: LEG WAS UNJOINED");
							}
						} else if (anEvent.getEventType().equals(SdpPortManagerEvent.ANSWER_GENERATED)) //due to ANSWER_GENERATED EVENT from processSdpOffer(remoteSdp);
						{
							SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
							try
							{
								//send 200 OK to sip phone uac
								log.debug("DlgcReferenceParticipantMediaListener:: ANSWER GENERATED");
								response.setContent(sdp.getMediaServerSessionDescription(), "application/sdp");
								response.send();
								participant.presentState.joinConferenceResponse(participant);
							}
							catch (UnsupportedEncodingException e)
							{
								e.printStackTrace();
							}
							catch (SdpPortManagerException e)
							{
								e.printStackTrace();
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						} else if (anEvent.getEventType().equals(SdpPortManagerEvent.NETWORK_STREAM_FAILURE)) {
							participant.loadSipSession();
							participant.release();
							log.debug("DlgcReferenceParticipantMediaListener:: This is not good we got an Network Conenction failure");
							return;
						}
					}
				}

				try {
					if (participant != null ) {
						participant.myConference.myControlServlet.conferenceStorage.saveConference(participant.myConference);
					}
				} catch (DlgcConferenceStorageMgrException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			
			log.debug(e.toString());
			
		} 
	}

} //end of class
