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
import java.net.URI;

import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MediaEvent;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.Joinable.Direction;
import javax.media.mscontrol.mediagroup.signals.SignalDetector;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.reference_conf_demo_with_outb_call_layout.DlgcOutbCallConferenceStorage.DlgcConferenceStorageMgrException;
import testing.reference_conf_demo_with_outb_call_layout.DlgcReferenceConferenceParticipantWOBC.LegMenuOperation;

public class DlgcReferenceConferenceParticipantStateWOBC implements Serializable {

		private static final long serialVersionUID = 1;
		protected String stateName = null;

		
		
		//static supported states as defined by State Machine Design Pattern
		protected static DlgcParticipantInitialStateWOBC 				 dlgcParticipantInitialStateWOBC = new DlgcParticipantInitialStateWOBC();
		protected static DlgcParticipantJoiningConfStateWOBC 			 dlgcParticipantJoiningConfStateWOBC = new DlgcParticipantJoiningConfStateWOBC();
		protected static DlgcParticipantJoinedConfStateWOBC 			 dlgcParticipantJoinedConfStateWOBC = new DlgcParticipantJoinedConfStateWOBC();
		protected static DlgcParticipantJoinedConfDtmfEnablingStateWOBC  dlgcParticipantJoinedConfDtmfEnablingStateWOBC = new DlgcParticipantJoinedConfDtmfEnablingStateWOBC();
		protected static DlgcParticipantJoinedConfDtmfEnabledStateWOBC 	 dlgcParticipantJoinedConfDtmfEnabledStateWOBC = new DlgcParticipantJoinedConfDtmfEnabledStateWOBC();
		protected static DlgcParticipantJoinedConfDtmfDisablingStateWOBC dlgcParticipantJoinedConfDtmfDisablingStateWOBC = new DlgcParticipantJoinedConfDtmfDisablingStateWOBC();
		protected static DlgcParticipantPlayingPromptStateWOBC 			 dlgcParticipantPlayingPromptStateWOBC = new DlgcParticipantPlayingPromptStateWOBC();
		protected static DlgcParticipantPlayingStoppingPromptStateWOBC	 dlgcParticipantPlayingStoppingPromptStateWOBC = new DlgcParticipantPlayingStoppingPromptStateWOBC();
		
		
		//Outbound caller extra states
		protected static DlgcParticipantGeneratingOfferState 			 dlgcParticipantGeneratingOfferState = new DlgcParticipantGeneratingOfferState();
		protected static DlgcParticipantOutboundJoinConfPendingState 	 dlgcParticipantOutboundJoinConfPendingState = new DlgcParticipantOutboundJoinConfPendingState();
		protected static DlgcParticipantOutboundCallPendingState 		 dlgcParticipantOutboundCallPendingState = new DlgcParticipantOutboundCallPendingState();
		
		
		public static Logger log = LoggerFactory.getLogger(DlgcReferenceConferenceParticipantStateWOBC.class);
		
		
		
		//events
		public void enterConference(DlgcReferenceConferenceParticipantWOBC participant , byte[] remoteSD) throws MsControlException
		{
			log.info("Invalid State: enterConference (With SDP) event request not supported in the present state");
		}
		


		public void askMediaServerToGenerateSDP(DlgcReferenceConferenceParticipantWOBC participant) throws MsControlException
		{
			log.info("Invalid State: askMediaServerToGenerateSDP event request not supported in the present state");
		}
		
		public void msAnswerGeneratedResponse(DlgcReferenceConferenceParticipantWOBC participant,   SdpPortManagerEvent event) throws MsControlException
		{
			log.info("Invalid State: msAnswerGeneratedResponse event request not supported in the present state");

		}
		
		
		public void outboundCallResponse(DlgcReferenceConferenceOutboundParticipantWOBC participant,  SipServletResponse response ) throws MsControlException
		{
			log.info("Invalid State: outboundCallResponse event request not supported in the present state");

		}
		
		//events
		public void joinConferenceResponse(DlgcReferenceConferenceParticipantWOBC participant ) throws MsControlException
		{
			log.info("Invalid State: joinConferenceResponse event request not supported in the present state");
		}
		
		//events
		public void playComplete(DlgcReferenceConferenceParticipantWOBC participant ) throws MsControlException
		{
			log.info("Invalid State: playComplete event request not supported in the present state");
		}
		
		//200 ok answer to generateSdpOffer()
		public void processSipPhone200Invite(DlgcReferenceConferenceParticipantWOBC participant, byte[] localSdp) throws MsControlException
		{
			log.info("Invalid State: DlgcReferenceConferenceParticipantWOBC:: processSipPhone200Invite event request not supported in the present state");
		}
		
		//Ack
		public void processSipPhoneAckAnswer(DlgcReferenceConferenceParticipantWOBC participant, byte[] localSdp) throws MsControlException
		{
			log.info("Invalid State: processSipPhoneAckAnswer event request not supported in the present state");
		}
		
		public void processSipPhoneAckAnswer(DlgcReferenceConferenceOutboundParticipantWOBC participant, byte[] localSdp) throws MsControlException
		{
			log.info("Invalid State: DlgcReferenceConferenceOutboundParticipantWOBC:: processSipPhoneAckAnswer event request not supported in the present state");
		}
		
		public void enableAsyncDtmf(DlgcReferenceConferenceParticipantWOBC participant) throws MsControlException
		{
			log.info("Invalid State: enableAsyncDtmf event request not supported in the present state");
		}
		
		public void enablingAsyncDtmfResponse(DlgcReferenceConferenceParticipantWOBC participant ) throws MsControlException
		{
			log.info("Invalid State: enablingAsyncDtmfResponse event request not supported in the present state");
		}
		
		public void processDtmfDigitsRequest(DlgcReferenceConferenceParticipantWOBC participant, String dtmfString, SignalDetectorEvent sigEvent ) throws MsControlException
		{
			log.info("Invalid State: processDtmfDigitsRequest event request not supported in the present state");
			
		}
		
		public void error(DlgcReferenceConferenceParticipantWOBC participant, MediaEvent<?> playerEvent) throws MsControlException
		{
			log.info("Invalid State: error method event request not supported in the present state");
		}
		
		
		protected void muteControl(DlgcReferenceConferenceParticipantWOBC participant) {
			log.debug("DlgcQuickConferenceParticipant::mute");
			// Check if caller is already muted or un-muted and toggle appropriately
			LegMenuOperation mo = DlgcReferenceConferenceParticipantWOBC.getMenuOperation(participant.loadSipSession());
			if ( ( mo.compareTo(LegMenuOperation.PARK) == 0 ) || ( mo.compareTo(LegMenuOperation.UNPARK) == 0 ) )
				DlgcReferenceConferenceParticipantWOBC.setMenuOperation(participant.loadSipSession(), LegMenuOperation.UNMUTE);
			if ( mo.compareTo(LegMenuOperation.UNMUTE) == 0) {
				DlgcReferenceConferenceParticipantWOBC.setMutingProcess(participant.loadSipSession(), true);
				    participant.executeMute();
			}
			else if ( mo.compareTo(LegMenuOperation.MUTE) == 0) {
				DlgcReferenceConferenceParticipantWOBC.setMutingProcess(participant.loadSipSession(), true);
				 participant.executeUnmute();
			}
		}
		
		
		protected void stopPlaying(DlgcReferenceConferenceParticipantWOBC participant)
		{
			log.debug("DlgcQuickConferenceParticipant::stopPlaying");
			participant.setState(participant.presentState, DlgcReferenceConferenceParticipantStateWOBC.dlgcParticipantPlayingStoppingPromptStateWOBC);
			
			try {
				participant.mg.getPlayer().stop(true);
				if ( DlgcReferenceConferenceWithOutBCallServlet.apiSyncModeProperty ) {
					log.debug("DlgcReferenceConferenceParticipant returned from synchronous stop play not waiting for Event play stop");
					participant.setState(participant.presentState, DlgcReferenceConferenceParticipantStateWOBC.dlgcParticipantJoinedConfDtmfEnabledStateWOBC);
				}else {
					log.debug("DlgcReferenceConferenceParticipant returned from asynchronous stop play not wiating for Event play stop");
				}
			} catch (MsControlException e) {
				// TODO Auto-generated catch block
				log.error(e.toString());
				log.error("Fail to stop playing.");					}
		}
		
	
		protected void playNBlue(DlgcReferenceConferenceParticipantWOBC participant)
		{
			log.debug("DlgcQuickConferenceParticipant::playNBlue");
			participant.setState(participant.presentState, DlgcReferenceConferenceParticipantStateWOBC.dlgcParticipantPlayingPromptStateWOBC);
			participant.playPromptParticipant (participant.loadSipSession(), new URI[] {URI.create(DlgcReferenceConferencePromptsWOBC.XMS_MUSIC_NBLUE)});
		}
		
		protected void confRecord(DlgcReferenceConferenceParticipantWOBC participant)
		{
			log.debug("DlgcQuickConferenceParticipant::confReocrd");
			participant.confRecord();
		}
		
		protected void confStopRecord(DlgcReferenceConferenceParticipantWOBC participant)
		{
			log.debug("DlgcQuickConferenceParticipant::confRecord");
			participant.confStopRecord();
		}
		
		//This method is call when user enters DTMF *44
		protected void createOutBoundCallFlow(DlgcReferenceConferenceParticipantWOBC participant) 
		{
			log.debug("DlgcQuickConferenceParticipant::createOutBoundCallFlow");
			SipApplicationSession sas;
			try {
				sas = DlgcOutbCallConferenceStorage.loadSas();
				DlgcOutbCallConferenceStorage conferenceStorage = (DlgcOutbCallConferenceStorage) sas.getAttribute("DlgcOutbCallConferenceStorage");
				participant.myConference.createOutBoundCallParticipant(participant,conferenceStorage);
			} catch (DlgcConferenceStorageMgrException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	
		protected void playMenu(DlgcReferenceConferenceParticipantWOBC participant)
		{
			log.debug("DlgcQuickConferenceParticipant::playMenu");
			participant.setState(participant.presentState, DlgcReferenceConferenceParticipantStateWOBC.dlgcParticipantPlayingPromptStateWOBC);
			participant.playPromptParticipant (participant.loadSipSession(), new URI[] {URI.create(DlgcReferenceConferencePromptsWOBC.XMS_PROMPT_JMC_MENU)});
		}
				
		
		protected void processDtmfSelectedOption(DlgcReferenceConferenceParticipantWOBC participant, String buffereddtmf, SignalDetectorEvent anEvent) throws MsControlException 
		{
				log.debug("Entering  processDtmfSelectedOption ");
				log.info("["+participant.loadSipSession().getCallId()+"] SIGNALDETECTOR EVENT -> "+anEvent.getEventType());
				//URI prompts[] = null;
				// Empty the digit buffer
				participant.loadSipSession().setAttribute("BUFFERED_SIGNALS","");
				// Check if buffered digits match MUTE option
				if (buffereddtmf.equals(DlgcReferenceConferenceConstantsWOBC.PARTICIPANT_OPTION_STOP_PLAY)) {
					stopPlaying(participant);
				}else if (buffereddtmf.equals(DlgcReferenceConferenceConstantsWOBC.PARTICIPANT_OPTION_MUTE_TOGGLE)) {
					muteControl(participant);
				} else if (buffereddtmf.equals(DlgcReferenceConferenceConstantsWOBC.PARTICIPANT_OPTION_PLAY_NBLUE)) {
					playNBlue(participant);
				} else if (buffereddtmf.equals(DlgcReferenceConferenceConstantsWOBC.PARTICIPANT_OPTION_PLAY_MENU)) {
					playMenu(participant);
				}else if (buffereddtmf.equals(DlgcReferenceConferenceConstantsWOBC.PARTICIPANT_OPTION_GET_CONFERENCE_SIZE)) {
					log.debug("DlgcQuickConferenceParticipant::processDtmfSelectedOption:: handle playNumberOfConferenceParticipants Option");
					participant.playNumberOfConferenceParticipants();
				}else if (buffereddtmf.equals(DlgcReferenceConferenceConstantsWOBC.PARTICIPANT_OPTION_UNJOIN_CONFERENCE)) {
					participant.unjoinConference();
				}else if (buffereddtmf.equals(DlgcReferenceConferenceConstantsWOBC.PARTICIPANT_OPTION_REJOIN_CONFERENCE_USING_MIXER )) {
					//rejoinConference(participant);
					participant.rejoinConference();
				}else if (buffereddtmf.equals(DlgcReferenceConferenceConstantsWOBC.CONFERENCE_OPTION_RECORD)) {
					confRecord(participant);
				}else if (buffereddtmf.equals(DlgcReferenceConferenceConstantsWOBC.CONFERENCE_OPTION_STOP_RECORD)) {
					confStopRecord(participant);
				}else if (buffereddtmf.equals(DlgcReferenceConferenceConstantsWOBC.CREATE_PARTICIPANT_OUTBOUND_CALL)) {
					createOutBoundCallFlow(participant);
				}else if (buffereddtmf.contains("*7")) {
					participant.setVideoLayout(buffereddtmf);
					//createOutBoundCallFlow(participant);
				}else {
					log.debug("DlgcQuickConferenceParticipant::processDtmfSelectedOption:: This Option is not implemented");
				}
		} 
		
	} //end of base state

	//DlgcParticipantInitialState
	class DlgcParticipantInitialStateWOBC extends DlgcReferenceConferenceParticipantStateWOBC
	{

		private static final long serialVersionUID = 1L;

		public DlgcParticipantInitialStateWOBC()
		{
			this.stateName = new String("DlgcParticipantInitialStateWOBC");
		}
		
		@Override
		public void enterConference(DlgcReferenceConferenceParticipantWOBC participant , byte[] remoteSdp) throws MsControlException
		{
			log.info("In State: " + this.stateName + "enterConference (With SDP) event - Privious State the same");
			participant.setState(dlgcParticipantInitialStateWOBC, dlgcParticipantJoiningConfStateWOBC);
			participant.nc.getSdpPortManager().processSdpOffer(remoteSdp);		//ask the connector to send offer to the media server
																				//the connector response with an async event.
																				//Note that the connector is going to do lots of work for given this simple request
																				//since you previously joined this network connection to a mixer (no confirm) the connector
																				//will first going to establish the sip session connection (RTP in the Media Server), then it will
																				//use the first connected leg to create the conference if conference was not created.
																				//Once conference is created, and leg is joined to it, connector will send you the processSdpOffer
																				//Answer.
		}
		
		public void askMediaServerToGenerateSDP(DlgcReferenceConferenceParticipantWOBC participant) throws MsControlException
		{
			log.info("In State: " + this.stateName + "askMediaServerToGenerateSDP event - Privious State the same");
			participant.setState(dlgcParticipantInitialStateWOBC, dlgcParticipantGeneratingOfferState );
			participant.nc.getSdpPortManager().generateSdpOffer();  
		}
		
	}
	
	//DlgcParticipantJoiningConfState
	class DlgcParticipantJoiningConfStateWOBC extends DlgcReferenceConferenceParticipantStateWOBC
	{

		private static final long serialVersionUID = 1L;

		public DlgcParticipantJoiningConfStateWOBC()
		{
			this.stateName = new String("DlgcParticipantJoiningConfStateWOBC");
		}
		
		//due to ANSWER_GENERATED EVENT from processSdpOffer(remoteSdp);
		@Override
		public void joinConferenceResponse(DlgcReferenceConferenceParticipantWOBC participant ) throws MsControlException
		{
			log.info("In State: " + this.stateName + "enterConference (With SDP) event - Privious State: " + participant.getPreviousState() );
			participant.setState(dlgcParticipantJoiningConfStateWOBC, dlgcParticipantJoinedConfStateWOBC);
		}
		
	}
	
	//DlgcParticipantJoinedConfState
	//The application can trigger the dtmf collection after it knows that the leg has joined the conference
	//Not done here, but this state should check if the DTMF Signal Detector was allocated by the Media Server
	//This test application has a Media Group Allocation Listener that can be used to see if the detector was indeed
	//created in the Media Server.
	class DlgcParticipantJoinedConfStateWOBC extends DlgcReferenceConferenceParticipantStateWOBC
	{

		private static final long serialVersionUID = 1L;

		public DlgcParticipantJoinedConfStateWOBC()
		{
			this.stateName = new String("DlgcParticipantJoinedConfStateWOBC");
		}
		
		//processAnswer the UA send an ACK 
		@Override
		//public void processSipPhoneAck(DlgcReferenceConferenceParticipantWOBC participant, byte[] localSdp) throws MsControlException
		public void processSipPhoneAckAnswer(DlgcReferenceConferenceParticipantWOBC participant, byte[] localSdp) throws MsControlException

		{
			log.info("In State: DlgcParticipantJoinedConfState::processSipPhoneAckAnswer (same as processAnswer) event");
			participant.nc.getSdpPortManager().processSdpAnswer(localSdp);
		}
		
		@Override
		//as mention above we should test to see if the Detector was allocated.. this test assumes that it is.
		public void enableAsyncDtmf(DlgcReferenceConferenceParticipantWOBC participant) throws MsControlException
		{
			log.info("In State: enableAsyncDtmf::processSipPhoneAck (same as processAnswer) event");
			participant.setState(dlgcParticipantJoinedConfStateWOBC, dlgcParticipantJoinedConfDtmfEnablingStateWOBC);
			Parameters detectorOptions = DlgcReferenceConferenceWithOutBCallServlet.theMediaSessionFactory.createParameters();
			detectorOptions.put(SignalDetector.BUFFERING, Boolean.FALSE );
			EventType[] arrayEnabledEvents = {SignalDetectorEvent.SIGNAL_DETECTED} ;
			detectorOptions.put(SignalDetector.ENABLED_EVENTS, arrayEnabledEvents);	
			log.info("["+participant.loadSipSession().getCallId()+"] CALLING RECEIVESIGNALS (-1)");
			participant.mg.getSignalDetector().receiveSignals(-1, null,	null, detectorOptions);
		}
		
		@Override
		//as mention above we should test to see if the Detector was allocated.. this test assumes that it is.
		public void joinConferenceResponse(DlgcReferenceConferenceParticipantWOBC participant ) throws MsControlException
		{
			log.info("In State: " + this.stateName + "joinConferenceResponse  - Privious State: " + participant.getPreviousState() );
			
			//participant.setState(dlgcParticipantJoiningConfState, dlgcParticipantJoinedConfState);
			this.enableAsyncDtmf(participant);
		}
	}
	
	
	class DlgcParticipantJoinedConfDtmfEnablingStateWOBC extends DlgcReferenceConferenceParticipantStateWOBC
	{
		private static final long serialVersionUID = 1L;
		public DlgcParticipantJoinedConfDtmfEnablingStateWOBC()
		{
			this.stateName = new String("DlgcParticipantJoinedConfDtmfEnablingStateWOBC");
		}
		
		@Override
		public void enablingAsyncDtmfResponse(DlgcReferenceConferenceParticipantWOBC participant ) throws MsControlException
		{
			log.info("IN State: DlgcParticipantJoinedConfDtmfEnablingState::enablingAsyncDtmfResponse... will never get here");
			participant.setState(dlgcParticipantJoinedConfStateWOBC, dlgcParticipantJoinedConfDtmfEnabledStateWOBC);
		}
		
		@Override
		public void processDtmfDigitsRequest(DlgcReferenceConferenceParticipantWOBC participant, String dtmfString, SignalDetectorEvent sigEvent ) throws MsControlException
		{
			log.info("IN State: DlgcParticipantJoinedConfDtmfEnabledState::processDtmfRequest");
			processDtmfSelectedOption(participant, dtmfString, sigEvent);
		}
		
	}
	
	
	
	//DlgcParticipantJoinedConfDtmfEnabledState
	//At this point we are listening for menu selection
	class DlgcParticipantJoinedConfDtmfEnabledStateWOBC extends DlgcReferenceConferenceParticipantStateWOBC
	{

		private static final long serialVersionUID = 1L;

		public DlgcParticipantJoinedConfDtmfEnabledStateWOBC()
		{
			this.stateName = new String("DlgcParticipantJoinedConfDtmfEnabledStateWOBC");
		}
		
		@Override
		public void enablingAsyncDtmfResponse(DlgcReferenceConferenceParticipantWOBC participant ) throws MsControlException
		{
			log.info("IN State: DlgcParticipantJoinedConfDtmfEnabledState::enablingAsyncDtmfResponse");
			participant.setState(participant.previousState, dlgcParticipantJoinedConfDtmfEnabledStateWOBC);
		}
		
		@Override
		public void processDtmfDigitsRequest(DlgcReferenceConferenceParticipantWOBC participant, String dtmfString, SignalDetectorEvent sigEvent ) throws MsControlException
		{
			log.info("IN State: DlgcParticipantJoinedConfDtmfEnabledState::processDtmfRequest");
			processDtmfSelectedOption(participant, dtmfString, sigEvent);
		}
		
		
	}
	
	//DlgcParticipantJoinedConfDtmfDisablingState
	class DlgcParticipantJoinedConfDtmfDisablingStateWOBC extends DlgcReferenceConferenceParticipantStateWOBC
	{
		private static final long serialVersionUID = 1L;
		public DlgcParticipantJoinedConfDtmfDisablingStateWOBC()
		{
			this.stateName = new String("DlgcParticipantJoinedConfDtmfDisablingStateWOBC");
		}
	}

	//DlgcParticipantPlayingPromptState
	class DlgcParticipantPlayingPromptStateWOBC extends DlgcReferenceConferenceParticipantStateWOBC
	{
		private static final long serialVersionUID = 1L;
		public DlgcParticipantPlayingPromptStateWOBC()
		{
			this.stateName = new String("DlgcParticipantPlayingPromptStateWOBC");
		}
		
		@Override
		public void playComplete(DlgcReferenceConferenceParticipantWOBC participant ) throws MsControlException
		{
			log.info("IN State: DlgcParticipantPlayingPromptState::playComplete event");
			participant.setState(participant.presentState, participant.previousState);
			
		}
		
		@Override
		public void error(DlgcReferenceConferenceParticipantWOBC participant, MediaEvent<?> playerEvent) throws MsControlException
		{
			log.info("IN State: error method event request");
			participant.setState(participant.presentState, participant.previousState);
			log.error("Playing Prompt Error during DlgcParticipantPlayingPromptState - error: " + playerEvent.getErrorText());
		}
		
		@Override
		public void processDtmfDigitsRequest(DlgcReferenceConferenceParticipantWOBC participant, String dtmfString, SignalDetectorEvent sigEvent ) throws MsControlException
		{
			log.info("IN State: DlgcParticipantPlayingPromptState::processDtmfRequest");
			processDtmfSelectedOption(participant, dtmfString, sigEvent);
		}
	}
	
	//dlgcParticipantPlayingStoppingPromptState
	class DlgcParticipantPlayingStoppingPromptStateWOBC extends DlgcReferenceConferenceParticipantStateWOBC
	{
		private static final long serialVersionUID = 1L;
		public DlgcParticipantPlayingStoppingPromptStateWOBC()
		{
			this.stateName = new String("DlgcParticipantPlayingStoppingPromptStateWOBC");
		}
		
		
		@Override
		public void playComplete(DlgcReferenceConferenceParticipantWOBC participant ) throws MsControlException
		{
			log.info("IN State: DlgcParticipantPlayingPromptState::playComplete event");
			participant.setState(participant.presentState, DlgcParticipantJoinedConfDtmfEnabledStateWOBC.dlgcParticipantJoinedConfDtmfEnabledStateWOBC);
			
		}
		
		
		@Override
		public void error(DlgcReferenceConferenceParticipantWOBC participant, MediaEvent<?> playerEvent) throws MsControlException
		{
			log.info("IN State: error method event request");
			participant.setState(participant.presentState, DlgcParticipantJoinedConfDtmfEnabledStateWOBC.dlgcParticipantJoinedConfDtmfEnabledStateWOBC);
			log.error("Playing Prompt Error during DlgcParticipantPlayingPromptState - error: " + playerEvent.getErrorText());
		}
	}

	class  DlgcParticipantGeneratingOfferState extends DlgcReferenceConferenceParticipantStateWOBC
	{
		private static final long serialVersionUID = 1L;
		public DlgcParticipantGeneratingOfferState()
		{
			this.stateName = new String("DlgcParticipantGeneratingOfferState");
		}
		
		@Override
		public void msAnswerGeneratedResponse(DlgcReferenceConferenceParticipantWOBC participant,   SdpPortManagerEvent sdpPortManagerEvent) throws MsControlException
		{
			log.info("DlgcParticipantGeneratingOfferState: connectedLeg OFFER GENERATED EVENT RECEIVED from Media Server");
			SdpPortManager sdpMgr = participant.nc.getSdpPortManager();
			byte[] msAnsweredSDP = sdpMgr.getMediaServerSessionDescription();
			String msAnsweredSDPString = new String(msAnsweredSDP);
			participant.remoteSdp = msAnsweredSDP;			//media server sdp
			
			log.debug("KAPANGA [3] IN => FROM MS  Participant ID =" + participant.clientSSID + " Media Server Generated Answer OFFER WITH SDP " + msAnsweredSDPString);

			if ( sdpPortManagerEvent.getError() == MediaErr.NO_ERROR ) {
				log.debug("Got OFFER_GENERATED ANSWER NO ERROR -> calling join to conference");
				participant.setState(dlgcParticipantGeneratingOfferState, dlgcParticipantOutboundJoinConfPendingState);
				participant.nc.join(Direction.DUPLEX, participant.myConference.confMediaMixer);
			} else {
				log.error(sdpPortManagerEvent.getErrorText() );
				throw ( new MsControlException(sdpPortManagerEvent.getErrorText()));
			}
		}
	}
	
	class DlgcParticipantOutboundJoinConfPendingState extends DlgcReferenceConferenceParticipantStateWOBC
	{

		private static final long serialVersionUID = 1L;
		public DlgcParticipantOutboundJoinConfPendingState()
		{
			this.stateName = new String("DlgcParticipantOutboundJoinConfPendingState");
		}
		
		@Override
		public void joinConferenceResponse(DlgcReferenceConferenceParticipantWOBC participant ) throws MsControlException
		{
			log.debug("Entering DlgcParticipantOutboundJoinConfPendingState::joinConferenceResponse" );
			log.info("In State: " + this.stateName + "enterConference event - Privious State: " + participant.getPreviousState() );
			participant.setState(dlgcParticipantOutboundJoinConfPendingState, dlgcParticipantOutboundCallPendingState);
			String msAnsweredSDPString = new String(participant.remoteSdp);			
			log.debug("KAPANGA [3] IN => FROM MS  Participant ID =" + participant.clientSSID + " Media Server Generated Answer OFFER WITH SDP " + msAnsweredSDPString);
			log.debug("DlgcParticipantOutboundJoinConfPendingState::calling makeOutboundCall()" );
			
			//add outbound participant now to the conference
			participant.myConference.addParticipantToConferenceList(participant);
			participant.makeOutboundCall(msAnsweredSDPString); 
			

			try {
				participant.myConference.myControlServlet.conferenceStorage.saveConference(participant.myConference);
			} catch (DlgcConferenceStorageMgrException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			log.debug("returning DlgcParticipantOutboundJoinConfPendingState::joinConferenceResponse" );
		}
		
	}
	
	
	
	class DlgcParticipantOutboundCallPendingState extends DlgcReferenceConferenceParticipantStateWOBC
	{
		private static final long serialVersionUID = 1L;
		public DlgcParticipantOutboundCallPendingState()
		{
			this.stateName = new String("DlgcParticipantOutboundCallPendingState");
		}

		@Override
		//not called by the connector. This is an response to an outbound call by the Application
		//This is called from the demo Application Servlet doResponse method
		public void outboundCallResponse(DlgcReferenceConferenceOutboundParticipantWOBC participant,  SipServletResponse response ) throws MsControlException
		{
		
			log.info("Entering DlgcParticipantOutboundCallPendingState::outboundCallResponse");
			int status = response.getStatus();
			if ( status == 183 )
			{
				
				log.debug("DlgcParticipantOutboundCallPendingState: outboundCallResponse - got 183 outbound call response");
				try {
					String contentType = response.getContentType();
					if ( contentType.compareToIgnoreCase("sdp") == 0 ) {
						participant.localSdp = response.getRawContent();
					} else {
						log.error("DlgcParticipantOutboundCallPendingState:outboundCallResponse:: invalid content type in 183 response... expecting SDP type");
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
				response.getSession().setAttribute("PARTICIPANT", participant);
				
				String contentType = response.getContentType();
				if ( contentType.compareToIgnoreCase("application/sdp") == 0 ) {
					try {
							byte[] testSd =response.getRawContent();
							String s = new String(testSd);
							s = s+"a=sendrecv\r";
						participant.localSdp = s.getBytes();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					log.error("DlgcParticipantOutboundCallPendingState: invalid content type in 200 response... expecting SDP type");
				}
				
				participant.outBoundCaller.tuaSipSession = response.getSession();
				SdpPortManager sdp = participant.nc.getSdpPortManager();
				log.debug("KAPANGA [5] TUA => IN 200 OK- TUA reply with Local SDP = " + new String(participant.localSdp) );
				log.debug("KAPANGA [6]  => Sending TUA SDP To Media Server via ACK with SDP = " + new String(participant.localSdp) );
				
				sdp.processSdpAnswer(participant.localSdp);		//send ack with sdp back to the Media Server via the connector
				participant.outBoundCaller.sendAck(response);
				
			} else {
				log.error("MAJOR Error in DlgcParticipantOutboundCallPendingState::connectedLegResponse invalid response status = " + new Integer(status).toString());
			}
		}
		
		@Override
		//as mention above we should test to see if the Detector was allocated.. this test assumes that it is.
		public void enableAsyncDtmf(DlgcReferenceConferenceParticipantWOBC participant) throws MsControlException
		{
			log.info("In State: enableAsyncDtmf::processSipPhoneAck (same as processAnswer) event");
			participant.setState(dlgcParticipantJoinedConfStateWOBC, dlgcParticipantJoinedConfDtmfEnablingStateWOBC);
			Parameters detectorOptions = DlgcReferenceConferenceWithOutBCallServlet.theMediaSessionFactory.createParameters();
			detectorOptions.put(SignalDetector.BUFFERING, Boolean.FALSE );
			EventType[] arrayEnabledEvents = {SignalDetectorEvent.SIGNAL_DETECTED} ;
			detectorOptions.put(SignalDetector.ENABLED_EVENTS, arrayEnabledEvents);	
			log.info("["+participant.loadSipSession().getCallId()+"] CALLING RECEIVESIGNALS (-1)");
			participant.mg.getSignalDetector().receiveSignals(-1, null,	null, detectorOptions);
		}
		
		
		
	}
	
	

