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
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MediaEvent;
import javax.media.mscontrol.MediaEventListener;

import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.JoinEvent;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Event listener for conference Player, Signal and Network events
 */
public class DlgcEMBridgeParticipantMediaListener<T extends MediaEvent<?>> implements MediaEventListener<T>, Serializable {

	private static final long serialVersionUID = 33777788881L;
	protected DlgcEMBridgeParticipant 		participant;
	private static Logger log = LoggerFactory.getLogger(DlgcEMBridgeParticipantMediaListener.class);
	
	public DlgcEMBridgeParticipantMediaListener(DlgcEMBridgeParticipant assocParticipant)
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
				log.info("["+participant.mySipSession.getCallId()+"] RECEIVED PLAYER EVENT: "+anEvent.getEventType());
				participant.processPlayerEvent((PlayerEvent)anEvent);
			} else if (anEvent instanceof SignalDetectorEvent) {
				handleSignalDetectorEvent( participant, (SignalDetectorEvent) anEvent );
			} else if (anEvent instanceof SdpPortManagerEvent) {
				handleMediaEvent( participant,  (SdpPortManagerEvent)anEvent);
			}
		} catch (Exception e) {
			try {
				participant.release();
			} catch (MsControlException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	void handleSignalDetectorEvent( DlgcEMBridgeParticipant participant, SignalDetectorEvent signalDetectorEvent )
	{
		log.debug("DlgcBridgeConferenceParticipant handling signal detector event participant ObjID: " + participant.toString() );
		log.info("["+participant.mySipSession.getCallId()+"] RECEIVED SIGNAL DETECTOR EVENT: "+signalDetectorEvent.getEventType());
		if ( signalDetectorEvent.getError() == MediaErr.NO_ERROR ) {
			log.debug("DlgcEMBridgeParticipantMediaListener::onEvent::SignalDetector:: event no error received");
			//String dtmfString = signalDetectorEvent.getSignalString();
			try {
				participant.processDetectorEvent(signalDetectorEvent);
			} catch (MsControlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			log.debug("DlgcEMBridgeParticipantMediaListener::onEvent::SignalDetector:: event error " + signalDetectorEvent.getErrorText());
		}
	}
	
	private void handleMediaEvent( DlgcEMBridgeParticipant participant, SdpPortManagerEvent sdpPortManagerEvent)
	{
		log.debug("DlgcBridgeConferenceParticipant::handleMediaEvent received event vo type SdpPortManagerEvent participant ObjID: " + participant.toString() );
		//SdpPortManagerEvent sdpPortManagerEvent = (SdpPortManagerEvent)anEvent;

		SdpPortManager sdp = sdpPortManagerEvent.getSource();

		//MediaSession myMediaSession = sdpPortManagerEvent.getSource().getMediaSession();
		//Note in the case of the TUA offer generated the mySipSsession is null - needs to be set
		String sipSessionCallId = "Undefined";
		if ( participant.mySipSession != null )
			sipSessionCallId = participant.mySipSession.getCallId();
		else {

		}
		log.info("["+sipSessionCallId+"] RECEIVED SDP PORT MANAGER EVENT: "+sdpPortManagerEvent.getEventType());
		log.info("["+sipSessionCallId+"] RECEIVED SDP PORT MANAGER EVENTB: "+sdpPortManagerEvent.getErrorText());
		log.info("["+sipSessionCallId+"] RECEIVED SDP PORT MANAGER EVENTC: "+sdpPortManagerEvent.getError());

		if ((sdpPortManagerEvent.getEventType().equals(JoinEvent.JOINED)) || (sdpPortManagerEvent.getEventType().equals(JoinEvent.UNJOINED)) )		//mute unmute operation
		{
			
			if (sdpPortManagerEvent.getEventType().equals(JoinEvent.JOINED) ){
				log.debug("LEG WAS JOINED");
				try {
					participant.presentState.joinConferenceResponse(participant);
				} catch (MsControlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if (sdpPortManagerEvent.getEventType().equals(JoinEvent.UNJOINED) ){
				log.debug("LEG WAS UNJOINED");
			}
		} else if (sdpPortManagerEvent.getEventType().equals(SdpPortManagerEvent.ANSWER_GENERATED)) //due to ANSWER_GENERATED EVENT from processSdpOffer(remoteSdp);
		{
			try {
					participant.presentState.connectedLegResponse(participant,sdp,sdpPortManagerEvent);
			} catch (MsControlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		else if (sdpPortManagerEvent.getEventType().equals(SdpPortManagerEvent.ANSWER_PROCESSED)) 
		{
		
			//do nothing...handle already in the DlgcEMBridgeServlet 
			/****	try {
					participant.presentState.answerProcessedResponse(participant, sdpPortManagerEvent);
				} catch (MsControlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				***/
		
		}else if (sdpPortManagerEvent.getEventType().equals(SdpPortManagerEvent.OFFER_GENERATED)) 
		{
			try {
				boolean  rejectionTest = false;  //set it to true to thest rejectSdpOffer() API
				if ( rejectionTest ) {
					log.warn("TESTING rejecctSdpOffer API....");
					participant.nc.getSdpPortManager().rejectSdpOffer();
					//participant.nc.release();
				} else 
					participant.presentState.connectedLegResponse(participant,  sdp, sdpPortManagerEvent);
			} catch (MsControlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (sdpPortManagerEvent.getEventType().equals(SdpPortManagerEvent.UNSOLICITED_OFFER_GENERATED))	{
			log.warn("["+participant.mySipSession.getCallId()+"] SDPEVENT: UNSOLICITIED OFFER GENERATED NOT NEEDED FOR MSML");
		}
		else if (sdpPortManagerEvent.getEventType().equals(SdpPortManagerEvent.NETWORK_STREAM_FAILURE)) {
			try {
				participant.release();
			} catch (MsControlException e) {
				e.printStackTrace();
			}
			return;
		}
	}
}
