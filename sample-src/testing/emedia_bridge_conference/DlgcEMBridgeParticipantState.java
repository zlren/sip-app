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

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MediaEvent;

import javax.media.mscontrol.MsControlException;

import javax.media.mscontrol.join.Joinable;


import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.sdp.SdpException;
import javax.sdp.SessionDescription;

import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;

import javax.sdp.SdpFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import testing.bridge_conference.DlgcBridgeConstants;
import testing.emedia_bridge_conference.DlgcEMBridgeConference.ConferenceState;



public class DlgcEMBridgeParticipantState implements Serializable {

	private static final long serialVersionUID = 1076251800071L;
	protected String stateName = null;



	//static supported states as defined by State Machine Design Pattern
	protected static DlgcEMParticipantInitialState 				 	dlgcParticipantInitialState = new DlgcEMParticipantInitialState();
	protected static DlgcEMParticipantConnectLegState 			 	dlgcParticipantConnectLegState = new DlgcEMParticipantConnectLegState();
	protected static DlgcEMParticipantConnectedLegState 			dlgcParticipantConnectedLegState = new DlgcEMParticipantConnectedLegState();
	protected static DlgcEMParticipantJoiningConfState 			 	dlgcParticipantJoiningConfState = new DlgcEMParticipantJoiningConfState();
	protected static DlgcEMParticipantJoinedConfState 			 	dlgcParticipantJoinedConfState = new DlgcEMParticipantJoinedConfState();
	protected static DlgcEMParticipantDtmfEnablingState  			dlgcParticipantDtmfEnablingState = new DlgcEMParticipantDtmfEnablingState();
	protected static DlgcEMParticipantDtmfEnabledState 	 	     	dlgcParticipantDtmfEnabledState = new DlgcEMParticipantDtmfEnabledState();
	protected static DlgcEMParticipantDtmfDisablingState 			dlgcParticipantDtmfDisablingState = new DlgcEMParticipantDtmfDisablingState();
	protected static DlgcEMParticipantPlayingPromptState 			dlgcParticipantPlayingPromptState = new DlgcEMParticipantPlayingPromptState();
	
	protected static DlgcEMParticipantJoinedMixerConfState 			dlgcParticipantJoinedMixerConfState = new DlgcEMParticipantJoinedMixerConfState();
	protected static DlgcEMParticipantJoiningMixerConfState			dlgcParticipantJoiningMixerConfState = new DlgcEMParticipantJoiningMixerConfState();


	//generate Offer States
	protected static DlgcEMParticipantGeneratingOfferState 			 dlgcParticipantGeneratingOfferState = new DlgcEMParticipantGeneratingOfferState();

	protected static DlgcParticipantEMJoiningConfState			 	 dlgcParticipantEMJoiningConfState = new DlgcParticipantEMJoiningConfState();

	protected static DlgcParticipantEMOutboundCallPendingState		 dlgcParticipantEMOutboundCallPendingState = new DlgcParticipantEMOutboundCallPendingState();

	protected static DlgcParticipantEMOutboundCallWaitingFor_ANSWER_PROCESSED_State dlgcParticipantEMOutboundCallWaitingFor_ANSWER_PROCESSED_State = new DlgcParticipantEMOutboundCallWaitingFor_ANSWER_PROCESSED_State();


	public static Logger log = LoggerFactory.getLogger(DlgcEMBridgeParticipantState.class);

	//events should be deprecated by joinLegs
	public void enterConference(DlgcEMBridgeParticipant participant , byte[] remoteSD) throws MsControlException
	{
		log.info("Invalid State: enterConference (With SDP) event request not supported in the present state");
	}

	public void joinLegs(DlgcEMBridgeParticipant participant1 , DlgcEMBridgeParticipant participant2) throws MsControlException
	{
		log.info("Invalid State: joinLegs event request not supported in the present state");
	}


	public void connectedLegResponse(DlgcEMBridgeParticipant participant, SdpPortManager sdp, SdpPortManagerEvent sdpPortManagerEvent) throws  MsControlException
	{
		log.info("Invalid State: connectedLegResponse event request not supported in the present state");
	}

	public void connectedLegResponse(DlgcEMBridgeParticipant participant, SipServletResponse response ) throws  MsControlException
	{
		log.info("Invalid State: connectedLegResponse event for Outbound Call request not supported in the present state");
	}

	//events
	public void joinConferenceResponse(DlgcEMBridgeParticipant participant ) throws MsControlException
	{
		log.info("Invalid State: joinConferenceResponse event request not supported in the present state");
	}

	//events
	public void unjoinConferenceResponse(DlgcEMBridgeParticipant participant ) throws MsControlException
	{
		log.info("Invalid State: unjoinConferenceResponse event request not supported in the present state");
	}

	//events
	public void playComplete(DlgcEMBridgeParticipant participant ) throws MsControlException
	{
		log.info("Invalid State: playComplete event request not supported in the present state");
	}

	//processAnswer
	public void processSipPhoneAck(DlgcEMBridgeParticipant participant, byte[] remoteSdp) throws MsControlException
	{
		log.info("Invalid State: processSipPhoneAck event request not supported in the present state");
	}


	public void enableAsyncDtmf(DlgcEMBridgeParticipant participant) throws MsControlException
	{
		log.info("Invalid State: enableAsyncDtmf event request not supported in the present state");
	}

	public void enablingAsyncDtmfResponse(DlgcEMBridgeParticipant participant ) throws MsControlException
	{
		log.info("Invalid State: enablingAsyncDtmfResponse event request not supported in the present state");
	}

	public void processDtmfDigitsRequest(DlgcEMBridgeParticipant participant, String dtmfString, SignalDetectorEvent sigEvent ) throws MsControlException
	{
		log.info("Invalid State: processDtmfDigitsRequest event request not supported in the present state");

	}

	public void error(DlgcEMBridgeParticipant participant, MediaEvent<?> playerEvent) throws MsControlException
	{
		log.info("Invalid State: error method event request not supported in the present state");
	}

	public void answerProcessedResponse( DlgcEMBridgeParticipant participant, SdpPortManagerEvent event) throws MsControlException
	{
		log.info("Invalid State: error method answerProcessedResponse event not supported in the present state");
	}


	protected void playMenu(DlgcEMBridgeParticipant participant)
	{
		log.debug("DlgcEMBridgeParticipant::playMenu");
		participant.setState(participant.presentState, DlgcEMBridgeParticipantState.dlgcParticipantPlayingPromptState);
		//participant.bridgePartner.setState(participant.presentState, DlgcEMBridgeParticipantState.dlgcParticipantPlayingPromptState);
		participant.playPromptParticipant (participant.mySipSession, new URI[] {URI.create(DlgcBridgeConstants.XMS_PROMPT_JMC_MENU)});
	}

	public void connectLeg(DlgcEMBridgeParticipant participant , byte[] remoteSdp) throws MsControlException
	{
		log.info("Error In State: " + this.stateName + "connectLeg (With SDP) event not supported in this state");

	}

	public void connectLeg(DlgcEMBridgeParticipant participant ) throws MsControlException
	{
		log.info("Error In State: " + this.stateName + "generateOffer (NO SDP) event not supported in this state");
	}


	// menu 
	//	*00:  Play Menu Test Options
	//  *77:  Hear information on how this demo works.
	//	*88   Play Conference Mode Info
	//	*99:  Transfer from Bridge to a full conference 
	
	
	
	protected void processDtmfSelectedOption(DlgcEMBridgeParticipant participant, String buffereddtmf, SignalDetectorEvent anEvent) throws MsControlException 
	{
		log.debug("Entering  processDtmfSelectedOption ");				
		log.info("["+participant.mySipSession.getCallId()+"] SIGNALDETECTOR EVENT -> "+anEvent.getEventType());
		//URI prompts[] = null;
		// Empty the digit buffer
		participant.mySipSession.setAttribute("BUFFERED_SIGNALS","");
		// Check if buffered digits match MUTE option
		if (buffereddtmf.equals(DlgcEMBridgeParticipant.MENU_99)) {
			participant.myConference.transferBridgeToFullConference();
		} else if (buffereddtmf.equals(DlgcEMBridgeParticipant.MENU_88)) {
			participant.playConferenceStateInfo();
		} else if (buffereddtmf.equals(DlgcEMBridgeParticipant.MENU_00)) {
			participant.playMenu00();
		} else  if (buffereddtmf.equals(DlgcEMBridgeParticipant.MENU_77)) {
			participant.playHowDoesItWork77();
		}else {
			log.debug("DlgcEMBridgeParticipant::processDtmfSelectedOption:: TODO handle DEFAULT STATE Option");
		}
	} 
}


/****************************** STATES START SESSION *****************************************/
//DlgcEMParticipantInitialState
class DlgcEMParticipantInitialState extends DlgcEMBridgeParticipantState
{

	private static final long serialVersionUID = 1L;

	public DlgcEMParticipantInitialState()
	{
		this.stateName = new String("DlgcEMParticipantInitialState");
	}

	@Override
	public void connectLeg(DlgcEMBridgeParticipant participant , byte[] remoteSdp) throws MsControlException
	{
		log.info("In State: " + this.stateName + "connecting leg (invite with SDP) event - Privious State the same");
		participant.setState(dlgcParticipantInitialState, DlgcEMBridgeParticipantState.dlgcParticipantConnectLegState);
		//participant.bridgePartner.setState(dlgcParticipantInitialState, DlgcEMBridgeParticipantState.dlgcParticipantConnectLegState);

		participant.nc.getSdpPortManager().processSdpOffer(remoteSdp);
	}

	@Override
	public void connectLeg(DlgcEMBridgeParticipant participant ) throws MsControlException
	{
		log.info("In State: " + this.stateName + "generating offer for leg (invite with NO SDP) event - Privious State the same");
		//log.info("John jump to scenario 4 and come back to this later on...");
		participant.setState(dlgcParticipantInitialState, DlgcEMBridgeParticipantState.dlgcParticipantGeneratingOfferState );
		participant.nc.getSdpPortManager().generateSdpOffer();  
	}

}

//DlgcEMParticipantJoiningConfState
class DlgcEMParticipantJoiningConfState extends DlgcEMBridgeParticipantState
{

	private static final long serialVersionUID = 1L;

	public DlgcEMParticipantJoiningConfState()
	{
		this.stateName = new String("DlgcEMParticipantJoiningConfState");
	}

	
	@Override
	public void joinConferenceResponse(DlgcEMBridgeParticipant participant ) throws MsControlException
	{
		log.debug("In State: " + this.stateName + "enterConference (With SDP) event - Privious State: " + participant.getPreviousState() );
		participant.myConference.setConfState(ConferenceState.IN_CONF);
		participant.setState(dlgcParticipantJoiningConfState, dlgcParticipantEMOutboundCallPendingState);
		participant.bridgePartner.setState(dlgcParticipantJoiningConfState, dlgcParticipantEMOutboundCallPendingState);
		//invite the TUA here
		DlgcEMBridgeParticipant tua = participant.bridgePartner;
		byte[] msOfferedSdp = tua.nc.getSdpPortManager().getMediaServerSessionDescription();
		tua.bridgePartner.externalTua.sendOutboutCall( new String(msOfferedSdp) );
		
	}

}

class DlgcEMParticipantJoiningMixerConfState extends DlgcEMBridgeParticipantState
{
	private static final long serialVersionUID = 1L;

	public DlgcEMParticipantJoiningMixerConfState()
	{
		this.stateName = new String("DlgcEMParticipantJoiningMixerConfState");
	}
	
	@Override
	public void processDtmfDigitsRequest(DlgcEMBridgeParticipant participant, String dtmfString, SignalDetectorEvent sigEvent ) throws MsControlException
	{
		log.info("IN State: DlgcEMParticipantJoiningMixerConfState::processDtmfRequest");
		processDtmfSelectedOption(participant, dtmfString, sigEvent);
	}
	
	@Override
	public void joinConferenceResponse(DlgcEMBridgeParticipant participant ) throws MsControlException
	{
		log.info("In State: " + this.stateName + "joinConferenceResponse event - Privious State: " + participant.getPreviousState() );
		participant.myConference.setConfState(ConferenceState.IN_CONF);
		participant.setState(dlgcParticipantJoiningConfState, dlgcParticipantJoinedMixerConfState);
		
		if ( participant.bridgePartner.getPresentState() != dlgcParticipantJoinedMixerConfState) {
		 participant.bridgePartner.setState(dlgcParticipantJoiningConfState, dlgcParticipantJoiningMixerConfState);
		 participant.bridgePartner.nc.join(Joinable.Direction.DUPLEX, participant.myConference.confMediaMixer);
		}
	}
	
}


class DlgcEMParticipantJoinedMixerConfState extends DlgcEMBridgeParticipantState
{
	private static final long serialVersionUID = 1L;

	public DlgcEMParticipantJoinedMixerConfState()
	{
		this.stateName = new String("DlgcEMParticipantJoinedMixerConfState");
	}
	
	@Override
	public void processDtmfDigitsRequest(DlgcEMBridgeParticipant participant, String dtmfString, SignalDetectorEvent sigEvent ) throws MsControlException
	{
		log.info("IN State: DlgcEMParticipantJoinedMixerConfState::processDtmfRequest");
		processDtmfSelectedOption(participant, dtmfString, sigEvent);
	}
	
}

//DlgcEMParticipantJoinedConfState
//The application can trigger the dtmf collection after it knows that the leg has joined the conference
//Not done here, but this state should check if the DTMF Signal Detector was allocated by the Media Server
//This test application has a Media Group Allocation Listener that can be used to see if the detector was indeed
//created in the Media Server.
class DlgcEMParticipantJoinedConfState extends DlgcEMBridgeParticipantState
{

	private static final long serialVersionUID = 1L;

	public DlgcEMParticipantJoinedConfState()
	{
		this.stateName = new String("DlgcEMParticipantJoinedConfState");
	}

	@Override
	public void processDtmfDigitsRequest(DlgcEMBridgeParticipant participant, String dtmfString, SignalDetectorEvent sigEvent ) throws MsControlException
	{
		log.info("IN State: DlgcEMParticipantJoinedConfDtmfEnabledState::processDtmfRequest");
		processDtmfSelectedOption(participant, dtmfString, sigEvent);
	}

	//events
	@Override
	public void unjoinConferenceResponse(DlgcEMBridgeParticipant participant ) throws MsControlException
	{
		log.info("DlgcEMParticipantJoinedConfState State: unjoinConferenceResponse event request received");
		DlgcEMBridgeConference bridgeConf = participant.myConference;
		bridgeConf.setConfState(ConferenceState.NOT_IN_CONF);
		participant.setState(dlgcParticipantJoinedConfState, dlgcParticipantDtmfEnabledState);
		participant.bridgePartner.setState(dlgcParticipantJoinedConfState, dlgcParticipantDtmfEnabledState);
		
		participant.myConference.joinFullMixerconference();
	}


}

class DlgcParticipantEMJoiningConfState extends DlgcEMBridgeParticipantState
{
	private static final long serialVersionUID = 1567310L;

	public DlgcParticipantEMJoiningConfState()
	{
		this.stateName = new String("DlgcParticipantEMJoiningConfState");
	}


	@Override
	public void joinConferenceResponse(DlgcEMBridgeParticipant participant ) throws MsControlException
	{
		log.info("State: DlgcParticipantEMJoiningConfState:: joinConferenceResponse");
		participant.myConference.setConfState(ConferenceState.IN_CONF);
		
		DlgcEMBridgeParticipant tua = participant;
		if ( DlgcEMBridgeServlet.DELAY_JOIN_TEST == false ) 
		{
			log.debug("State: DlgcParticipantEMJoiningConfState::joinConferenceResponse DELAY JOIN TEST is false...calling send outbound call as usual..");
			
			if ( participant.myName.compareToIgnoreCase("OUA-PARTICIPANT") == 0) {
				tua = participant.getBridgePartner();
			}
			
			SdpPortManager sdpMgr = tua.nc.getSdpPortManager();
			byte[] msAnsweredSDP = sdpMgr.getMediaServerSessionDescription();
			String msAnsweredSDPString = new String(msAnsweredSDP);
			participant.setState(dlgcParticipantJoinedConfState, dlgcParticipantEMOutboundCallPendingState);
			participant.bridgePartner.setState(dlgcParticipantJoinedConfState, dlgcParticipantEMOutboundCallPendingState);
			log.debug("State: DlgcParticipantEMJoiningConfState::joinConferenceResponse Participant type " + tua.myName + " calling sendOutboundCall with sdp " + msAnsweredSDPString);
			participant.bridgePartner.externalTua.sendOutboutCall(msAnsweredSDPString);
		} else {
			log.info("State: DlgcParticipantEMJoiningConfState::joinConferenceResponse DELAY JOIN TEST is true...NOT calling send outbound call as usual..");
			participant.setState(dlgcParticipantJoinedConfState, dlgcParticipantJoinedConfState);
			participant.bridgePartner.setState(dlgcParticipantJoinedConfState, dlgcParticipantJoinedConfState);
		}

	}

}


class DlgcParticipantEMOutboundCallPendingState extends DlgcEMBridgeParticipantState
{
	private static final long serialVersionUID = 1891230L;
	public DlgcParticipantEMOutboundCallPendingState()
	{
		this.stateName = new String("DlgcParticipantEMOutboundCallPendingState");
	}

	@Override
	//not called by the connector. This is an response to an outbound call by the Application
	//This is called from the demo Application Servlet doResponse method
	public void connectedLegResponse(DlgcEMBridgeParticipant participant, SipServletResponse response ) throws  MsControlException
	{
		log.info("DlgcParticipantEMOutboundCallPendingState: connectedLegResponse");
		int status = response.getStatus();
		if ( status == 183 )
		{
			
			log.debug("DlgcParticipantEMOutboundCallPendingState: connectedLegResponse - got 183 outbound call response");
			try {
				String contentType = response.getContentType();
				if ( contentType.compareToIgnoreCase("sdp") == 0 ) {
					participant.externalTua.remoteSdp = response.getRawContent();
				} else {
					log.error("DlgcParticipantEMOutboundCallPendingState: invalid content type in 183 response... expecting SDP type");
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		} else if ( status == 200)
		{ 
			participant.setSipSession(response.getSession());
			response.getSession().setAttribute("PARTICIPANT", participant);
			
			String contentType = response.getContentType();
			if ( contentType.compareToIgnoreCase("application/sdp") == 0 ) {
				try {
					
						//SessionDescription sd = SdpFactory.getInstance().createSessionDescription(new String(response.getRawContent()));
						//sd.setAttribute("a", "sendrecv");
						byte[] testSd =response.getRawContent();
						String s = new String(testSd);
						s = s+"a=sendrecv\r";
						//log.debug("TEST JOHN OF SDP=" + s);
					
					
					//participant.externalTua.remoteSdp = response.getRawContent();
						participant.externalTua.remoteSdp = s.getBytes();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				log.error("DlgcParticipantEMOutboundCallPendingState: invalid content type in 200 response... expecting SDP type");
			}
			
			participant.externalTua.tuaSipSession = response.getSession();
			SdpPortManager sdp = participant.nc.getSdpPortManager();
			participant.externalTua.tuaSipSession.setAttribute("TUA_INITIAL_200_OK_RESPONSE",response);  //read later to send 200 OK to OUA

			log.debug("KAPANGA [5] TUA => IN 200 OK- TUA replay with SDP = " + new String(participant.externalTua.remoteSdp) );
			log.debug("KAPANGA [6]  => Sending TUA SDP To Media Server via ACK with SDP = " + new String(participant.externalTua.remoteSdp) );
			sdp.processSdpAnswer(participant.externalTua.remoteSdp);		//send ack with sdp back to the Media Server via the connector
			//participant.setState(dlgcParticipantEMOutboundCallPendingState, dlgcParticipantEMOutboundCallWaitingFor_ANSWER_PROCESSED_State)
			participant.setState(dlgcParticipantEMOutboundCallPendingState, dlgcParticipantJoinedConfState);
			DlgcEMBridgeParticipant myPartner = participant.bridgePartner;
			//myPartner.setState(dlgcParticipantEMOutboundCallPendingState, dlgcParticipantEMOutboundCallWaitingFor_ANSWER_PROCESSED_State);
			myPartner.setState(dlgcParticipantEMOutboundCallPendingState, dlgcParticipantJoinedConfState);
		} else {
			log.error("MAJOR Error in DlgcParticipantEMOutboundCallPendingState::connectedLegResponse invalid response status = " + new Integer(status).toString());
		}
	}
	
	//this is not used... not using asnwerPRocessedResponse on all states remove later on
	@Override 
	public void answerProcessedResponse( DlgcEMBridgeParticipant participant, SdpPortManagerEvent event) throws MsControlException
	{
		if ( event.getError() == MediaEvent.NO_ERROR ) { 
			//send 200 OK to OUA end point
			participant.setState(dlgcParticipantEMOutboundCallWaitingFor_ANSWER_PROCESSED_State, dlgcParticipantJoinedConfState);
			participant.bridgePartner.setState(dlgcParticipantEMOutboundCallWaitingFor_ANSWER_PROCESSED_State,dlgcParticipantJoinedConfState );
			
			SipServletResponse response = (SipServletResponse)participant.externalTua.tuaSipSession.getAttribute("TUA_INITIAL_200_OK_RESPONSE");
			participant.externalTua.sendAck(response);
			participant.sendOkToOUA();
			//want to call conference join here
		} else {
			log.error("DlgcParticipantEMOutboundCallWaitingFor_ANSWER_PROCESSED_State::answerProcessedResponse error = " + event.getErrorText());
		}
	}


}

//TBD
class DlgcParticipantEMOutboundCallWaitingFor_ANSWER_PROCESSED_State extends DlgcEMBridgeParticipantState
{
	private static final long serialVersionUID = 9082L;
	public DlgcParticipantEMOutboundCallWaitingFor_ANSWER_PROCESSED_State()
	{
		this.stateName = new String("DlgcParticipantEMOutboundCallWaitingFor_ANSWER_PROCESSED_State");
	}

	@Override 
	public void answerProcessedResponse( DlgcEMBridgeParticipant participant, SdpPortManagerEvent event) throws MsControlException
	{
		if ( event.getError() != MediaEvent.NO_ERROR ) { 
			//send 200 OK to OUA end point
			participant.setState(dlgcParticipantEMOutboundCallWaitingFor_ANSWER_PROCESSED_State, dlgcParticipantJoinedConfState);
			participant.bridgePartner.setState(dlgcParticipantEMOutboundCallWaitingFor_ANSWER_PROCESSED_State,dlgcParticipantJoinedConfState );
			
			SipServletResponse response = (SipServletResponse)participant.externalTua.tuaSipSession.getAttribute("TUA_INITIAL_200_OK_RESPONSE");
			participant.externalTua.sendAck(response);
			participant.sendOkToOUA();
			//want to call conference join here
		} else {
			log.error("DlgcParticipantEMOutboundCallWaitingFor_ANSWER_PROCESSED_State::answerProcessedResponse error = " + event.getErrorText());
		}
	}
}


class DlgcEMParticipantDtmfEnablingState extends DlgcEMBridgeParticipantState
{
	private static final long serialVersionUID = 1L;
	public DlgcEMParticipantDtmfEnablingState()
	{
		this.stateName = new String("DlgcEMParticipantDtmfEnablingState");
	}

	@Override
	public void enablingAsyncDtmfResponse(DlgcEMBridgeParticipant participant ) throws MsControlException
	{
		log.info("IN State: DlgcEMParticipantJoinedConfDtmfEnablingState::enablingAsyncDtmfResponse... will never get here");
		participant.setState(dlgcParticipantJoinedConfState, dlgcParticipantDtmfEnabledState);
		//participant.bridgePartner.setState(dlgcParticipantJoinedConfState, dlgcParticipantDtmfEnabledState);
	}

}



//DlgcEMParticipantDtmfEnabledState
//At this point we are listening for menu selection
//At this time we are not in a bridge conference until user enters *03 to join bridge
class DlgcEMParticipantDtmfEnabledState extends DlgcEMBridgeParticipantState
{

	private static final long serialVersionUID = 1L;

	public DlgcEMParticipantDtmfEnabledState()
	{
		this.stateName = new String("DlgcEMParticipantDtmfEnabledState");
	}

	@Override
	public void enablingAsyncDtmfResponse(DlgcEMBridgeParticipant participant ) throws MsControlException
	{
		log.info("IN State: DlgcEMParticipantJoinedConfDtmfEnabledState::enablingAsyncDtmfResponse");
		participant.setState(participant.previousState, dlgcParticipantDtmfEnabledState);
		//participant.bridgePartner.setState(participant.previousState, dlgcParticipantDtmfEnabledState);
	}

	@Override
	public void processDtmfDigitsRequest(DlgcEMBridgeParticipant participant, String dtmfString, SignalDetectorEvent sigEvent ) throws MsControlException
	{
		log.info("IN State: DlgcEMParticipantJoinedConfDtmfEnabledState::processDtmfRequest");
		processDtmfSelectedOption(participant, dtmfString, sigEvent);
	}


}

//DlgcEMParticipantJoinedConfDtmfDisablingState
class DlgcEMParticipantDtmfDisablingState extends DlgcEMBridgeParticipantState
{
	private static final long serialVersionUID = 1L;
	public DlgcEMParticipantDtmfDisablingState()
	{
		this.stateName = new String("DlgcEMParticipantDtmfDisablingState");
	}
}

//DlgcEMParticipantPlayingPromptState
class DlgcEMParticipantPlayingPromptState extends DlgcEMBridgeParticipantState
{
	private static final long serialVersionUID = 1L;
	public DlgcEMParticipantPlayingPromptState()
	{
		this.stateName = new String("DlgcEMParticipantPlayingPromptState");
	}

	@Override
	public void playComplete(DlgcEMBridgeParticipant participant ) throws MsControlException
	{
		log.info("IN State: DlgcEMParticipantPlayingPromptState::playComplete event");
		participant.setState(participant.presentState, participant.previousState);
		//participant.bridgePartner.setState(participant.presentState, participant.previousState);

	}

	@Override
	public void error(DlgcEMBridgeParticipant participant, MediaEvent<?> playerEvent) throws MsControlException
	{
		log.info("IN State: error method event request");
		participant.setState(participant.presentState, participant.previousState);
		participant.bridgePartner.setState(participant.presentState, participant.previousState);
		log.error("Playing Prompt Error during DlgcEMParticipantPlayingPromptState - error: " + playerEvent.getErrorText());
	}

	
}



/****************************************/

class DlgcEMParticipantConnectLegState extends DlgcEMBridgeParticipantState
{
	private static final long serialVersionUID = 1L;
	public DlgcEMParticipantConnectLegState()
	{
		this.stateName = new String("DlgcEMParticipantConnectLegState");
	}

	@Override
	public void connectedLegResponse(DlgcEMBridgeParticipant participant, SdpPortManager sdp, SdpPortManagerEvent sdpPortManagerEvent) throws MsControlException
	{
		log.info("DlgcEMParticipantConnectLegState: Entering");

		log.info("DlgcEMParticipantConnectLegState: connectedLeg event received");
		//200 OK response to the phone has been sent by the DlgcEMBridgeParticipantMediaListener
		//send 183 with  OK to sip phone uac
		SipServletRequest request = (SipServletRequest) participant.mySipSession.getAttribute("UNANSWERED_INVITE");
		log.info("DlgcEMParticipantConnectLegState: got SipServletRequest for the unanswered invite");
		SipServletResponse response183 = request.createResponse(SipServletResponse.SC_SESSION_PROGRESS);  //183
		log.info("DlgcEMParticipantConnectLegState: response183 Created");
		
		//test stuff john
		participant.msSdpToSave = sdp.getMediaServerSessionDescription();

		try {
			
			response183.setContent(sdp.getMediaServerSessionDescription(), "application/sdp");  
			log.info("DlgcEMParticipantConnectLegState: response183.setContent completed");

			try {
				
				if ( sdp == null )
					log.debug(" DlgcEMParticipantConnectLegState::connectedLegResponse()  sdp null" );

				
				String sdpString = new String(sdp.getMediaServerSessionDescription());
				log.debug("KAPANGA [2] OUT => Send 183 to  Participant Name =" + participant.myName + "183 WITH SDP " + sdpString);
				response183.send(); 
				participant.setState(participant.presentState, DlgcEMBridgeParticipantState.dlgcParticipantConnectedLegState);
				log.info("DlgcEMParticipantConnectLegState: calling myconference.setupTUALeg()");

				participant.myConference.setupTUALeg();
				log.info("DlgcEMParticipantConnectLegState: completed myconference.setupTUALeg()");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.info("DlgcEMParticipantConnectLegState: exception: " + e.toString());
				participant.setState(participant.presentState, DlgcEMBridgeParticipantState.dlgcParticipantInitialState);
				e.printStackTrace();
			}

		} catch (Exception e) {
			log.info("DlgcEMParticipantConnectLegState: exception: " + e.toString());
			participant.setState(participant.presentState, DlgcEMBridgeParticipantState.dlgcParticipantInitialState);
			e.printStackTrace();
		}


	}

}

class DlgcEMParticipantConnectedLegState extends DlgcEMBridgeParticipantState
{
	private static final long serialVersionUID = 1L;
	public DlgcEMParticipantConnectedLegState()
	{
		this.stateName = new String("DlgcEMParticipantConnectedLegState");
	}

	public void joinLegs(DlgcEMBridgeParticipant participant1 , DlgcEMBridgeParticipant participant2) throws MsControlException
	{
		log.info("In State: DlgcEMParticipantConnectLegState::joinLegs event request");
		participant1.setState(participant1.presentState, dlgcParticipantJoiningConfState);
		participant2.setState(participant1.presentState, dlgcParticipantJoiningConfState);
		//TBD
	}

}


class DlgcEMParticipantGeneratingOfferState extends DlgcEMBridgeParticipantState
{
	private static final long serialVersionUID = 1L;
	public DlgcEMParticipantGeneratingOfferState()
	{
		this.stateName = new String("DlgcEMParticipantGeneratingOfferState");
	}

	@Override
	public void connectedLegResponse(DlgcEMBridgeParticipant participant,  SdpPortManager sdp, SdpPortManagerEvent sdpPortManagerEvent) throws MsControlException
	{
		log.info("DlgcEMParticipantGeneratingOfferState: connectedLeg OFFER GENERATED EVENT RECEIVED");
		SdpPortManager sdpMgr = participant.nc.getSdpPortManager();
		byte[] msAnsweredSDP = sdpMgr.getMediaServerSessionDescription();
		String msAnsweredSDPString = new String(msAnsweredSDP);
		participant.msSdpToSave = msAnsweredSDP;
		log.debug("KAPANGA [3] IN => FROM MS  Participant Name =" + participant.myName + " Media Server Generated Answer OFFER WITH SDP " + msAnsweredSDPString);
		//note the ack is sent later with the TUASDP  once we get the TUA SDP



		if ( sdpPortManagerEvent.getError() == MediaErr.NO_ERROR ) {
			log.debug("Got OFFER_GENERATED ANSWER NO ERROR -> calling joinToBridge");
			if ( DlgcEMBridgeServlet.DELAY_JOIN_TEST == false) {
				log.debug("DlgcEMParticipantGeneratingOfferState::connectedLegResponse - DELAY JOIN TEST is false... thus initiating early join");
				participant.myConference.joinToBridge();
			} else {
				//setup proper state
				participant.myConference.setBridgePartners();
				log.debug("DlgcEMParticipantGeneratingOfferState::connectedLegResponse - DELAY JOIN TEST is true... thus is not initiating early join");

				participant.setState(dlgcParticipantJoinedConfState, dlgcParticipantEMOutboundCallPendingState);
				participant.bridgePartner.setState(dlgcParticipantJoinedConfState, dlgcParticipantEMOutboundCallPendingState);
				participant.externalTua.sendOutboutCall(msAnsweredSDPString);
			}

		} else {
			log.error(sdpPortManagerEvent.getErrorText() );
			throw ( new MsControlException(sdpPortManagerEvent.getErrorText()));
		}
	}

}








