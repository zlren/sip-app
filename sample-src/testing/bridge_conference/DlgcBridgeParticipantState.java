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
import java.net.URI;

import javax.media.mscontrol.MediaEvent;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.servlet.sip.SipServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.bridge_conference.DlgcBridgeConference.ConferenceState;
import testing.bridge_conference.DlgcBridgeParticipant.MenuOperation;


public class DlgcBridgeParticipantState implements Serializable {

		private static final long serialVersionUID = 33801076251800071L;
		protected String stateName = null;

		
		
		//static supported states as defined by State Machine Design Pattern
		protected static DlgcParticipantInitialState 				 dlgcParticipantInitialState = new DlgcParticipantInitialState();
		protected static DlgcParticipantConnectLegState 			 dlgcParticipantConnectLegState = new DlgcParticipantConnectLegState();
		protected static DlgcParticipantConnectedLegState 			 dlgcParticipantConnectedLegState = new DlgcParticipantConnectedLegState();
		protected static DlgcParticipantJoiningConfState 			 dlgcParticipantJoiningConfState = new DlgcParticipantJoiningConfState();
		protected static DlgcParticipantJoinedConfState 			 dlgcParticipantJoinedConfState = new DlgcParticipantJoinedConfState();
		protected static DlgcParticipantDtmfEnablingState  			 dlgcParticipantDtmfEnablingState = new DlgcParticipantDtmfEnablingState();
		protected static DlgcParticipantDtmfEnabledState 	 	     dlgcParticipantDtmfEnabledState = new DlgcParticipantDtmfEnabledState();
		protected static DlgcParticipantDtmfDisablingState 			 dlgcParticipantDtmfDisablingState = new DlgcParticipantDtmfDisablingState();
		protected static DlgcParticipantPlayingPromptState 			 dlgcParticipantPlayingPromptState = new DlgcParticipantPlayingPromptState();
		protected static DlgcParticipantPlayingStoppingPromptState	 dlgcParticipantPlayingStoppingPromptState = new DlgcParticipantPlayingStoppingPromptState();
		
		public static Logger log = LoggerFactory.getLogger(DlgcBridgeParticipantState.class);
		
		//events should be depricted by joinLegs
		public void enterConference(DlgcBridgeParticipant participant , byte[] remoteSD) throws MsControlException
		{
			log.info("Invalid State: enterConference (With SDP) event request not supported in the present state");
		}
		
		public void joinLegs(DlgcBridgeParticipant participant1 , DlgcBridgeParticipant participant2) throws MsControlException
		{
			log.info("Invalid State: joinLegs event request not supported in the present state");
		}
		
		public void connectedLegResponse(DlgcBridgeParticipant participant,SipServletResponse response, SdpPortManager sdp) throws  MsControlException
		{
			log.info("Invalid State: connectedLeg event request not supported in the present state");
		}
		
		
		
		//events
		public void joinConferenceResponse(DlgcBridgeParticipant participant ) throws MsControlException
		{
			log.info("Invalid State: joinConferenceResponse event request not supported in the present state");
		}
		
		//events
		public void unjoinConferenceResponse(DlgcBridgeParticipant participant ) throws MsControlException
		{
			log.info("Invalid State: unjoinConferenceResponse event request not supported in the present state");
		}
		
		//events
		public void playComplete(DlgcBridgeParticipant participant ) throws MsControlException
		{
			log.info("Invalid State: playComplete event request not supported in the present state");
		}
		
		//processAnswer
		public void processSipPhoneAck(DlgcBridgeParticipant participant, byte[] remoteSdp) throws MsControlException
		{
			log.info("Invalid State: processSipPhoneAck event request not supported in the present state");
		}
		
				
		public void enableAsyncDtmf(DlgcBridgeParticipant participant) throws MsControlException
		{
			log.info("Invalid State: enableAsyncDtmf event request not supported in the present state");
		}
		
		public void enablingAsyncDtmfResponse(DlgcBridgeParticipant participant ) throws MsControlException
		{
			log.info("Invalid State: enablingAsyncDtmfResponse event request not supported in the present state");
		}
		
		public void processDtmfDigitsRequest(DlgcBridgeParticipant participant, String dtmfString, SignalDetectorEvent sigEvent ) throws MsControlException
		{
			log.info("Invalid State: processDtmfDigitsRequest event request not supported in the present state");
			
		}
		
		public void error(DlgcBridgeParticipant participant, MediaEvent<?> playerEvent) throws MsControlException
		{
			log.info("Invalid State: error method event request not supported in the present state");
		}
		
		
		protected void muteControl(DlgcBridgeParticipant participant) {
			log.debug("DlgcBridgeParticipant::mute");
			// Check if caller is already muted or un-muted and toggle appropriately
			MenuOperation mo = DlgcBridgeParticipant.getMenuOperation(participant.mySipSession);
			if ( ( mo.compareTo(MenuOperation.PARK) == 0 ) || ( mo.compareTo(MenuOperation.UNPARK) == 0 ) )
				DlgcBridgeParticipant.setMenuOperation(participant.mySipSession, MenuOperation.UNMUTE);
			if ( mo.compareTo(MenuOperation.UNMUTE) == 0) {
				DlgcBridgeParticipant.setMutingProcess(participant.mySipSession, true);
				    participant.executeMute();
			}
			else if ( mo.compareTo(MenuOperation.MUTE) == 0) {
				DlgcBridgeParticipant.setMutingProcess(participant.mySipSession, true);
				 participant.executeUnmute();
			}
		}
		
		
		protected void stopPlaying(DlgcBridgeParticipant participant)
		{
			log.debug("DlgcBridgeParticipant::stopPlaying");
			participant.setState(participant.presentState, DlgcBridgeParticipantState.dlgcParticipantPlayingStoppingPromptState);
			//participant.bridgePartner.setState(participant.presentState, DlgcBridgeParticipantState.dlgcParticipantPlayingStoppingPromptState);
					
			//MediaGroup customerMediaGroup = (MediaGroup) participant.mySipSession.getAttribute("MEDIA_GROUP");
			try {
				participant.mg.getPlayer().stop(true);
			} catch (MsControlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	
		protected void playNBlues(DlgcBridgeParticipant participant)
		{
			log.debug("DlgcBridgeParticipant::playNBlues");
			//participant.presentState.
			participant.setState(participant.presentState, DlgcBridgeParticipantState.dlgcParticipantPlayingPromptState);
			//participant.bridgePartner.setState(participant.presentState, DlgcBridgeParticipantState.dlgcParticipantPlayingPromptState);
			participant.playPromptParticipant (participant.mySipSession, new URI[] {URI.create(DlgcBridgeConstants.XMS_MUSIC_NBLUES)});
		}
		
	
		protected void playMenu(DlgcBridgeParticipant participant)
		{
			log.debug("DlgcBridgeParticipant::playMenu");
			participant.setState(participant.presentState, DlgcBridgeParticipantState.dlgcParticipantPlayingPromptState);
			//participant.bridgePartner.setState(participant.presentState, DlgcBridgeParticipantState.dlgcParticipantPlayingPromptState);
			participant.playPromptParticipant (participant.mySipSession, new URI[] {URI.create(DlgcBridgeConstants.XMS_PROMPT_JMC_MENU)});
		}
		
		public void connectLeg(DlgcBridgeParticipant participant , byte[] remoteSdp) throws MsControlException
		{
			log.info("Error In State: " + this.stateName + "connectLeg (With SDP) event not supported in this state");
			
		}
		
		//setup AsynDTMF on this call leg
		//wait for 200 ok
		//after this you can start receiving digits and
		//establish a menu
		// menu 
		//		*00:	 Play Menu Test Options
		//		*01:  Toggle between MUTE/UNMUTE 
		//		*02:  UNJOIN Conference (rejoin NO TONE CLAMPING)
		//		*03:  JOIN Conference using "Only Mixer" (NO TONE CLAMPING)
		//      *04:  JOIN Conference using "MixerAdapter" (rejoin  using TONE CLAMPING)
		//		*05:  Play Niles Blues
		//      *06:  Play Conference Size is
		//		*07:  Join Conference in half duplex (Receive Only)  (NEW to be added)
		//      *08:  TBD
		//		*99:  Stop any announcements and prompts and return back to conference
		//Note when all annc/play are completed the connector automatically puts back the leg into conference (ie unparks)
		protected void processDtmfSelectedOption(DlgcBridgeParticipant participant, String buffereddtmf, SignalDetectorEvent anEvent) throws MsControlException 
		{
				log.debug("Entering  processDtmfSelectedOption ");				
				log.info("["+participant.mySipSession.getCallId()+"] SIGNALDETECTOR EVENT -> "+anEvent.getEventType());
				//URI prompts[] = null;
				// Empty the digit buffer
				participant.mySipSession.setAttribute("BUFFERED_SIGNALS","");
				// Check if buffered digits match MUTE option
				if (buffereddtmf.equals(DlgcBridgeConstants.PARTICIPANT_OPTION_STOP_PLAY)) {
					stopPlaying(participant);
				}else if (buffereddtmf.equals(DlgcBridgeConstants.PARTICIPANT_OPTION_MUTE_TOGGLE)) {
					muteControl(participant);
				} else if (buffereddtmf.equals(DlgcBridgeConstants.PARTICIPANT_OPTION_PLAY_NBLUES)) {
					playNBlues(participant);
				} else if (buffereddtmf.equals(DlgcBridgeConstants.PARTICIPANT_OPTION_PLAY_MENU)) {
					playMenu(participant);
				}else if (buffereddtmf.equals(DlgcBridgeConstants.PARTICIPANT_OPTION_GET_CONFERENCE_SIZE)) {
					log.debug("DlgcBridgeParticipant::processDtmfSelectedOption:: handle playNumberOfConferenceParticipants Option");
					participant.playNumberOfConferenceParticipants();
				}else if (buffereddtmf.equals(DlgcBridgeConstants.PARTICIPANT_OPTION_UNJOIN_CONFERENCE)) {
					participant.unjoinConference();
				}else if (buffereddtmf.equals(DlgcBridgeConstants.PARTICIPANT_OPTION_REJOIN_CONFERENCE_USING_MIXER )) {
					participant.myConference.bridgeLeg(participant);
				}else if (buffereddtmf.equals( DlgcBridgeConstants.PARTICIPANT_OPTION_REJOIN_CONFERENCE_USING_MIXER_APDATER ) ) {
					//rejoinConferenceUsingMixerAdapter(participant);	
					log.debug("NOT SUPPORTED YET....");
					//participant.unjoinConference();
				}else {
					log.debug("DlgcBridgeParticipant::processDtmfSelectedOption:: TODO handle DEFAULT STATE Option");
				}
		} 
		
	} //end of base state

	//DlgcParticipantInitialState
	class DlgcParticipantInitialState extends DlgcBridgeParticipantState
	{

		private static final long serialVersionUID = 1L;

		public DlgcParticipantInitialState()
		{
			this.stateName = new String("DlgcParticipantInitialState");
		}
			
		@Override
		public void connectLeg(DlgcBridgeParticipant participant , byte[] remoteSdp) throws MsControlException
		{
			log.info("In State: " + this.stateName + "enterConference (With SDP) event - Privious State the same");
			participant.setState(dlgcParticipantInitialState, DlgcBridgeParticipantState.dlgcParticipantConnectLegState);
			//participant.bridgePartner.setState(dlgcParticipantInitialState, DlgcBridgeParticipantState.dlgcParticipantConnectLegState);
			
			participant.nc.getSdpPortManager().processSdpOffer(remoteSdp);
		}
		
	}
	
	//DlgcParticipantJoiningConfState
	class DlgcParticipantJoiningConfState extends DlgcBridgeParticipantState
	{

		private static final long serialVersionUID = 1L;

		public DlgcParticipantJoiningConfState()
		{
			this.stateName = new String("DlgcParticipantJoiningConfState");
		}
		
		//due to ANSWER_GENERATED EVENT from processSdpOffer(remoteSdp);
		@Override
		public void joinConferenceResponse(DlgcBridgeParticipant participant ) throws MsControlException
		{
			log.info("In State: " + this.stateName + "enterConference (With SDP) event - Privious State: " + participant.getPreviousState() );
			participant.setState(dlgcParticipantJoiningConfState, dlgcParticipantJoinedConfState);
			participant.bridgePartner.setState(dlgcParticipantJoiningConfState, dlgcParticipantJoinedConfState);
		}
		
	}
	
	//DlgcParticipantJoinedConfState
	//The application can trigger the dtmf collection after it knows that the leg has joined the conference
	//Not done here, but this state should check if the DTMF Signal Detector was allocated by the Media Server
	//This test application has a Media Group Allocation Listener that can be used to see if the detector was indeed
	//created in the Media Server.
	class DlgcParticipantJoinedConfState extends DlgcBridgeParticipantState
	{

		private static final long serialVersionUID = 1L;

		public DlgcParticipantJoinedConfState()
		{
			this.stateName = new String("DlgcParticipantJoinedConfState");
		}
		
		@Override
		public void processDtmfDigitsRequest(DlgcBridgeParticipant participant, String dtmfString, SignalDetectorEvent sigEvent ) throws MsControlException
		{
			log.info("IN State: DlgcParticipantJoinedConfDtmfEnabledState::processDtmfRequest");
			processDtmfSelectedOption(participant, dtmfString, sigEvent);
		}
		
		//events
		public void unjoinConferenceResponse(DlgcBridgeParticipant participant ) throws MsControlException
		{
			log.info("DlgcParticipantJoinedConfState State: unjoinConferenceResponse event request received");
			DlgcBridgeConference bridgeConf = participant.myConference;
			bridgeConf.setConfState(ConferenceState.NOT_IN_CONF);
			participant.setState(dlgcParticipantJoinedConfState, dlgcParticipantDtmfEnabledState);
			participant.bridgePartner.setState(dlgcParticipantJoinedConfState, dlgcParticipantDtmfEnabledState);
		}
		
					
	}
	
	
	class DlgcParticipantDtmfEnablingState extends DlgcBridgeParticipantState
	{
		private static final long serialVersionUID = 1L;
		public DlgcParticipantDtmfEnablingState()
		{
			this.stateName = new String("DlgcParticipantDtmfEnablingState");
		}
		
		@Override
		public void enablingAsyncDtmfResponse(DlgcBridgeParticipant participant ) throws MsControlException
		{
			log.info("IN State: DlgcParticipantJoinedConfDtmfEnablingState::enablingAsyncDtmfResponse... will never get here");
			participant.setState(dlgcParticipantJoinedConfState, dlgcParticipantDtmfEnabledState);
			//participant.bridgePartner.setState(dlgcParticipantJoinedConfState, dlgcParticipantDtmfEnabledState);
		}
		
	}
	
	
	
	//DlgcParticipantDtmfEnabledState
	//At this point we are listening for menu selection
	//At this time we are not in a bridge conference until user enters *03 to join bridge
	class DlgcParticipantDtmfEnabledState extends DlgcBridgeParticipantState
	{

		private static final long serialVersionUID = 1L;

		public DlgcParticipantDtmfEnabledState()
		{
			this.stateName = new String("DlgcParticipantDtmfEnabledState");
		}
		
		@Override
		public void enablingAsyncDtmfResponse(DlgcBridgeParticipant participant ) throws MsControlException
		{
			log.info("IN State: DlgcParticipantJoinedConfDtmfEnabledState::enablingAsyncDtmfResponse");
			participant.setState(participant.previousState, dlgcParticipantDtmfEnabledState);
			//participant.bridgePartner.setState(participant.previousState, dlgcParticipantDtmfEnabledState);
		}
		
		@Override
		public void processDtmfDigitsRequest(DlgcBridgeParticipant participant, String dtmfString, SignalDetectorEvent sigEvent ) throws MsControlException
		{
			log.info("IN State: DlgcParticipantJoinedConfDtmfEnabledState::processDtmfRequest");
			processDtmfSelectedOption(participant, dtmfString, sigEvent);
		}
		
		
	}
	
	//DlgcParticipantJoinedConfDtmfDisablingState
	class DlgcParticipantDtmfDisablingState extends DlgcBridgeParticipantState
	{
		private static final long serialVersionUID = 1L;
		public DlgcParticipantDtmfDisablingState()
		{
			this.stateName = new String("DlgcParticipantDtmfDisablingState");
		}
	}

	//DlgcParticipantPlayingPromptState
	class DlgcParticipantPlayingPromptState extends DlgcBridgeParticipantState
	{
		private static final long serialVersionUID = 1L;
		public DlgcParticipantPlayingPromptState()
		{
			this.stateName = new String("DlgcParticipantPlayingPromptState");
		}
		
		@Override
		public void playComplete(DlgcBridgeParticipant participant ) throws MsControlException
		{
			log.info("IN State: DlgcParticipantPlayingPromptState::playComplete event");
			participant.setState(participant.presentState, participant.previousState);
			//participant.bridgePartner.setState(participant.presentState, participant.previousState);
			
		}
		
		@Override
		public void error(DlgcBridgeParticipant participant, MediaEvent<?> playerEvent) throws MsControlException
		{
			log.info("IN State: error method event request");
			participant.setState(participant.presentState, participant.previousState);
			participant.bridgePartner.setState(participant.presentState, participant.previousState);
			log.error("Playing Prompt Error during DlgcParticipantPlayingPromptState - error: " + playerEvent.getErrorText());
		}
		
		@Override
		public void processDtmfDigitsRequest(DlgcBridgeParticipant participant, String dtmfString, SignalDetectorEvent sigEvent ) throws MsControlException
		{
			log.info("IN State: DlgcParticipantPlayingPromptState::processDtmfRequest");
			processDtmfSelectedOption(participant, dtmfString, sigEvent);
		}
	}
	
	//dlgcParticipantPlayingStoppingPromptState
	class DlgcParticipantPlayingStoppingPromptState extends DlgcBridgeParticipantState
	{
		private static final long serialVersionUID = 1L;
		public DlgcParticipantPlayingStoppingPromptState()
		{
			this.stateName = new String("DlgcParticipantPlayingStoppingPromptState");
		}
		
		
		@Override
		public void playComplete(DlgcBridgeParticipant participant ) throws MsControlException
		{
			log.info("IN State: DlgcParticipantPlayingPromptState::playComplete event");
			participant.setState(participant.presentState, dlgcParticipantDtmfEnabledState);
			//participant.bridgePartner.setState(participant.presentState, dlgcParticipantDtmfEnabledState);
			
		}
		
		
		@Override
		public void error(DlgcBridgeParticipant participant, MediaEvent<?> playerEvent) throws MsControlException
		{
			log.info("IN State: error method event request");
			participant.setState(participant.presentState, dlgcParticipantDtmfEnabledState);
			participant.bridgePartner.setState(participant.presentState, dlgcParticipantDtmfEnabledState);
			log.error("Playing Prompt Error during DlgcParticipantPlayingPromptState - error: " + playerEvent.getErrorText());
		}
	}
	
	class DlgcParticipantConnectLegState extends DlgcBridgeParticipantState
	{
		private static final long serialVersionUID = 1L;
		public DlgcParticipantConnectLegState()
		{
			this.stateName = new String("DlgcParticipantConnectLegState");
		}
		
		@Override
		public void connectedLegResponse(DlgcBridgeParticipant participant,SipServletResponse response, SdpPortManager sdp) throws MsControlException
		{
			log.info("DlgcParticipantConnectLegState: connectedLeg event received");
			//200 OK response to the phone has been sent by the DlgcBridgeParticipantMediaListener
			//send 200 OK to sip phone uac
			try {
				response.setContent(sdp.getMediaServerSessionDescription(), "application/sdp");
				try {
					response.send();
					participant.setState(participant.presentState, DlgcBridgeParticipantState.dlgcParticipantConnectedLegState);
					//participant.bridgePartner.setState(participant.presentState, DlgcBridgeParticipantState.dlgcParticipantConnectedLegState);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					participant.setState(participant.presentState, DlgcBridgeParticipantState.dlgcParticipantInitialState);
					e.printStackTrace();
				}
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				participant.setState(participant.presentState, DlgcBridgeParticipantState.dlgcParticipantInitialState);
				//participant.bridgePartner.setState(participant.presentState, DlgcBridgeParticipantState.dlgcParticipantInitialState);
				e.printStackTrace();
			}
			
			
		}
		
	}
	
	class DlgcParticipantConnectedLegState extends DlgcBridgeParticipantState
	{
		private static final long serialVersionUID = 1L;
		public DlgcParticipantConnectedLegState()
		{
			this.stateName = new String("DlgcParticipantConnectedLegState");
		}
		
		public void joinLegs(DlgcBridgeParticipant participant1 , DlgcBridgeParticipant participant2) throws MsControlException
		{
			log.info("In State: DlgcParticipantConnectLegState::joinLegs event request");
			participant1.setState(participant1.presentState, dlgcParticipantJoiningConfState);
			participant2.setState(participant1.presentState, dlgcParticipantJoiningConfState);
			//TBD
		}
		
		@Override
		public void processSipPhoneAck(DlgcBridgeParticipant participant, byte[] remoteSdp) throws MsControlException
		{
			log.info("In State: DlgcParticipantJoinedConfState::processSipPhoneAck (same as processAnswer) event");
			if (remoteSdp != null)
			{
				participant.nc.getSdpPortManager().processSdpAnswer(remoteSdp);
			}
			
			//Enable AsyncDtmf
			participant.enableAsyncDtmf();
			DlgcBridgeParticipant partner = participant.bridgePartner;
			
			if ( partner != null ) {
				partner.setState(partner.presentState, dlgcParticipantDtmfEnablingState);
				partner.enableAsyncDtmf();
			}
			
			
		}
	}
	
	
	


