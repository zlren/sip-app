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

package testing.bridge_conference;

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

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testing.bridge_conference.DlgcBridgeParticipant.MenuOperation;

/**
 * Event listener for conference Player, Signal and Network events
 */
public class DlgcBridgeParticipantMediaListener<T extends MediaEvent<?>> implements MediaEventListener<T>, Serializable {

	private static final long serialVersionUID = 33777788881L;
	protected DlgcBridgeParticipant 		participant;
	private static Logger log = LoggerFactory.getLogger(DlgcBridgeParticipantMediaListener.class);
	
	public DlgcBridgeParticipantMediaListener(DlgcBridgeParticipant assocParticipant)
	{
		participant = assocParticipant;
	}
	
	public void onEvent(T anEvent) {

		if ( !anEvent.isSuccessful() ) {
			log.debug("DlgcBridgeConferenceParticipant Error onEvent received problem is: " + anEvent.getErrorText());
			return;
		}
		log.debug("DlgcBridgeConferenceParticipant  received event =" + anEvent.getEventType().toString() );
		
		try {
			if (anEvent instanceof PlayerEvent) {
				if ( anEvent.getEventType() == PlayerEvent.RESUMED ) {
					log.info("Received Player Event: Play Resumed which means Play Started...");
					return;
				} 
				log.debug("DlgcBridgeConferenceParticipant handling player event participant ObjID: " + participant.toString()  );
				PlayerEvent playerEvent = (PlayerEvent)anEvent;
				log.info("["+participant.mySipSession.getCallId()+"] RECEIVED PLAYER EVENT: "+anEvent.getEventType());
				//TODO: call the correct state here from the state machine object
				//myState.onPlayerEvent(jcs, mySipSession, playerEvent);
				participant.processPlayerEvent((PlayerEvent)anEvent);
			} else if (anEvent instanceof SignalDetectorEvent) {
				log.debug("DlgcBridgeConferenceParticipant handling signal detector event participant ObjID: " + participant.toString() );
				log.info("["+participant.mySipSession.getCallId()+"] RECEIVED SIGNAL DETECTOR EVENT: "+anEvent.getEventType());
				SignalDetectorEvent signalDetectorEvent = (SignalDetectorEvent)anEvent;
				if ( signalDetectorEvent.getError() == MediaErr.NO_ERROR ) {
					log.debug("DlgcBridgeParticipantMediaListener::onEvent::SignalDetector:: event no error received");
					String dtmfString = signalDetectorEvent.getSignalString();
					//participant.presentState.processDtmfDigitsRequest(participant,  dtmfString, signalDetectorEvent  );
					participant.processDetectorEvent(signalDetectorEvent);
				}else {
					log.debug("DlgcBridgeParticipantMediaListener::onEvent::SignalDetector:: event error " + signalDetectorEvent.getErrorText());
				}
			} else if (anEvent instanceof SdpPortManagerEvent) {
				log.debug("DlgcBridgeConferenceParticipant received event vo type SdpPortManagerEvent participant ObjID: " + participant.toString() );
				SdpPortManagerEvent sdpPortManagerEvent = (SdpPortManagerEvent)anEvent;
				
				SdpPortManager sdp = sdpPortManagerEvent.getSource();
				
				//MediaSession myMediaSession = sdpPortManagerEvent.getSource().getMediaSession();
				String sipSessionCallId = participant.mySipSession.getCallId();
				log.info("["+sipSessionCallId+"] RECEIVED SDP PORT MANAGER EVENT: "+anEvent.getEventType());
				log.info("["+sipSessionCallId+"] RECEIVED SDP PORT MANAGER EVENTB: "+anEvent.getErrorText());
				log.info("["+sipSessionCallId+"] RECEIVED SDP PORT MANAGER EVENTC: "+anEvent.getError());
				log.info("["+sipSessionCallId+"] CHECK: "+ participant.mySipSession+" / "+participant.mySipSession.isValid());
				
				if (participant.mySipSession.isValid())
				{
					//myState = (State)mySipSession.getAttribute("STATE");
					SipServletRequest request = (SipServletRequest) participant.mySipSession.getAttribute("UNANSWERED_INVITE");
					if ((anEvent.getEventType().equals(JoinEvent.JOINED)) || (anEvent.getEventType().equals(JoinEvent.UNJOINED)) )		//mute unmute operation
					{
						MenuOperation  mo  = DlgcBridgeParticipant.getMenuOperation(participant.mySipSession);
						if ( mo.compareTo(MenuOperation.MUTE) == 0) {
							//participant.mySipSession.setAttribute("STATE", State.CollectingParticipantOptions);
							log.debug("DlgcBridgeConferenceParticipant TODO");
						} else if ( mo.compareTo(MenuOperation.UNMUTE) == 0) {
							//participant.mySipSession.setAttribute("STATE", State.CollectingParticipantOptions);
							log.debug("DlgcBridgeConferenceParticipant TODO");
						} else if ( mo.compareTo(MenuOperation.PARK) == 0 ) {
							//TODO myState.playYouAreParked(jcs,mySipSession);
							log.debug("DlgcBridgeConferenceParticipant TODO");
						} else if ( mo.compareTo(MenuOperation.UNPARK) == 0 ) {
							//TODO myState.playYouAreUnParked(jcs,mySipSession);
							log.debug("DlgcBridgeConferenceParticipant TODO");
						}
						if (anEvent.getEventType().equals(JoinEvent.JOINED) ){
							log.debug("LEG WAS JOINED");
							participant.presentState.joinConferenceResponse(participant);
						}else if (anEvent.getEventType().equals(JoinEvent.UNJOINED) ){
							log.debug("LEG WAS UNJOINED");
						}
					} else if (anEvent.getEventType().equals(SdpPortManagerEvent.ANSWER_GENERATED)) //due to ANSWER_GENERATED EVENT from processSdpOffer(remoteSdp);
					{
						SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
						try
						{
							//send 200 OK to sip phone uac
							//response.setContent(sdp.getMediaServerSessionDescription(), "application/sdp");
							//response.send();
							participant.presentState.connectedLegResponse(participant, response, sdp);
						} catch (SdpPortManagerException e)
						{
							e.printStackTrace();
						}
						
					}
					//only true for IPMS ..this event not sent in MSML...need to handle join confirm
					else if (anEvent.getEventType().equals(SdpPortManagerEvent.ANSWER_PROCESSED)) 
					{
						String callId = participant.mySipSession.getCallId();
						log.info("["+callId+"] SDPEVENT: ANSWER PROCESSED, RESPONSE: "+ participant.mySipSession.getAttribute("RESPONSE").toString());
						log.info("["+callId+"] SESSION IS "+participant.mySipSession.toString());
						SipServletResponse response = (SipServletResponse) participant.mySipSession.getAttribute("RESPONSE");
						if (response != null)
						{
							try
							{
								response.createAck().send();
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
						//myState.onReInviteOK(jcs, mySipSession);
					}
					// Send REINVITE to caller once they JOIN the conference
					else if (anEvent.getEventType().equals(SdpPortManagerEvent.UNSOLICITED_OFFER_GENERATED))
					{
						log.warn("["+participant.mySipSession.getCallId()+"] SDPEVENT: UNSOLICITIED OFFER GENERATED NOT NEEDED FOR MSML");
					}
					else if (anEvent.getEventType().equals(SdpPortManagerEvent.NETWORK_STREAM_FAILURE)) {
						participant.release();
						//sendBye(mySipSession);
						//jcs.releaseSession(mySipSession);
						return;
					}
				}
			}
		} catch (Exception e) {
			try {
				participant.release();
			} catch (MsControlException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//terminate(mySipSession, e);
		}
	}
}
