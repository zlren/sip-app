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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;



import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.join.Joinable.Direction;


import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;

import javax.media.mscontrol.mediagroup.MediaGroup;


import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.mediagroup.signals.SignalDetector;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;


import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;

import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;




import javax.media.mscontrol.Parameters;


import javax.servlet.ServletException;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import javax.servlet.sip.SipSession;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import testing.emedia_bridge_conference.DlgcEMBridgeConference.ConferenceState;

public class DlgcEMBridgeParticipant implements Serializable {


	public String myName;
	private boolean destroyRequest;
	private static final long serialVersionUID = 15674333657L;

	public static boolean confJoinUseMixerAdapter=true;    //default should be true	

	protected DlgcEMBridgeParticipantState	presentState;
	protected DlgcEMBridgeParticipantState	previousState;
	public static Logger log = LoggerFactory.getLogger(DlgcEMBridgeParticipant.class);
	SipSession mySipSession = null;
	NetworkConnection nc = null;
	MediaGroup		  mg = null;
	MediaSession	  ms = null;
	DlgcEMBridgeParticipant bridgePartner=null;
	DlgcOutbound_TUA externalTua =null;
	
	public final static String MENU_00 = "*00";
	public final static String MENU_77 = "*77";
	public final static String MENU_88 = "*88";
	public final static String MENU_99 ="*99";
	
	 public final static String[] participantOptions = { 
		 MENU_00,
		 MENU_77,
		 MENU_88,
		 MENU_99
		};  


	protected DlgcEMBridgeConference 	myConference;

	
	public byte[] msSdpToSave;


	

	

	public void setState( DlgcEMBridgeParticipantState previous, DlgcEMBridgeParticipantState present) {
		presentState = present;
		previousState = previous;
	}

	public DlgcEMBridgeParticipantState getPreviousState()
	{
		return previousState;
	}

	public DlgcEMBridgeParticipantState getPresentState()
	{
		return presentState;
	}

	public void printState()
	{
		log.debug("DlgcEMBridgeParticipant::Previous State is " + presentState.stateName);
		log.debug("DlgcEMBridgeParticipant::Present  State is " + previousState.stateName);
	}

	public void unjoinConference() {
		if (mySipSession != null && mySipSession.isValid()) {
			log.info(" Unjoining leg from conference.");
			NetworkConnection nc = this.nc;
			DlgcEMBridgeParticipant peerParticipant = this.myConference.findADestinationParticipant( this );
			NetworkConnection peerNC = peerParticipant.nc;

			if ( peerNC != null ) {
				try {
					nc.unjoin(peerNC);
				}catch (MsControlException ex)
				{
					log.error("Error unjoing from bridge ", ex);
					ex.printStackTrace();
				}catch (Exception ex) {
					log.error("Error unjoing from bridge ", ex);
					ex.printStackTrace();
				}
			}else {
				log.info("["+mySipSession.getCallId()+"] could not Unjoining Session from Bridge conference because cant find peer participant");
			}

		}

	}

	void setSipSession( SipSession ss)
	{
		mySipSession = ss;
		mySipSession.setAttribute("MEDIA_SESSION", ms);
	}


	public DlgcEMBridgeParticipant(DlgcEMBridgeConference conference, SipSession sipSession, String name) 
	{
		presentState  = DlgcEMBridgeParticipantState.dlgcParticipantInitialState;
		previousState = DlgcEMBridgeParticipantState.dlgcParticipantInitialState;
		myConference  = conference;
		mySipSession  = sipSession;
		myName		  = name;

		try {
			ms = DlgcEMBridgeServlet.theMediaSessionFactory.createMediaSession();
			nc = ms.createNetworkConnection(NetworkConnection.BASIC);
			
			Parameters sdpConfiguration = ms.createParameters();
			
			Map<String,String>  configurationData = new HashMap<String,String>();
			if ( conference.participantList.size() == 1) {
				if ( DlgcEMBridgeServlet.propertyEarlyUserURI != null ) {
					log.debug("using URI user name: " + DlgcEMBridgeServlet.propertyEarlyUserURI);
					configurationData.put("early", DlgcEMBridgeServlet.propertyEarlyUserURI);				
					//configurationData.put("TONE_CLAMPING", "true");
				}
				if ( DlgcEMBridgeServlet.propertyWebRtcUserURI != null ) {
					log.debug("using URI user name: " + DlgcEMBridgeServlet.propertyWebRtcUserURI);
					//configurationData.put("SIP_REQ_URI", webrtc );
					configurationData.put("webrtc", DlgcEMBridgeServlet.propertyWebRtcUserURI );
					nc.setParameters(sdpConfiguration);
				}
				if ( configurationData.isEmpty() == false ) {
					sdpConfiguration.put(SdpPortManager.SIP_HEADERS, configurationData);
					nc.setParameters(sdpConfiguration);
				}
			}
			
			mg = ms.createMediaGroup(MediaGroup.PLAYER_SIGNALDETECTOR);
			ms.setAttribute("NETWORK_CONNECTION", nc);
			ms.setAttribute("MEDIA_GROUP", mg);
			ms.setAttribute("SIP_SESSION", mySipSession);

			if ( mySipSession != null ) {
				mySipSession.setAttribute("MEDIA_SESSION", ms);  
			}

			mg.getPlayer().addListener(new DlgcEMBridgeParticipantMediaListener<PlayerEvent>(this));
			mg.addListener(new DlgcEMBridgeSDAllocationListener(this));
			nc.getSdpPortManager().addListener(new DlgcEMBridgeParticipantMediaListener<SdpPortManagerEvent>(this));
			mg.getSignalDetector().addListener(new DlgcEMBridgeParticipantMediaListener<SignalDetectorEvent>(this));

			nc.addListener(new DlgcEMBridgeParticipantJoinListener(this));
			//new
			nc.addListener(new DlgcEMBridgeAllocListener());
			
			nc.join(Joinable.Direction.DUPLEX, mg);

		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if ( mySipSession != null )
			mySipSession.setAttribute("MEDIA_SESSION", ms);
		externalTua = new DlgcOutbound_TUA(this, ms, (DlgcEMBridgeServlet)DlgcEMBridgeServlet.instance);
	}


	public void bridgeLegs(DlgcEMBridgeParticipant destParticipant,Direction dir) throws MsControlException
	{
		DlgcEMBridgeConference bridgeConf = this.myConference;
		ConferenceState confState = bridgeConf.getConfState();
		log.info("DlgcEmBridgeParticipant::bridgeLegs method");
		if ( confState == ConferenceState.NOT_IN_CONF ) 
		{
			this.setState(this.presentState, DlgcEMBridgeParticipantState.dlgcParticipantEMJoiningConfState);
			destParticipant.setState(this.presentState, DlgcEMBridgeParticipantState.dlgcParticipantEMJoiningConfState);
			setBridgePartner( destParticipant );
			destParticipant.setBridgePartner( this );
			try {
				NetworkConnection nc1 = this.nc;
				NetworkConnection nc2 = destParticipant.nc;
				log.info("Conference Bridge joining two NETWORK CONNECTIONS");
				bridgeConf.setConfState(ConferenceState.CONF_PEND);
				//NOTE joinEventListener is called with the join result
				//this listener should set the proper conference state if in asynchronous mode
				//otherwise the join request is synchronous which is the default
				nc1.join(dir, nc2);	
				if ( DlgcEMBridgeServlet.apiSyncModeProperty )
					this.presentState.joinConferenceResponse(this);
			} catch (MsControlException e)
			{
				log.error("Exception found: " +e);
			}
			catch (Exception e) {
				log.error("Exception found: " +e);
			} 
		} else {
			log.error("Cant join legs into a bridge conference - bridge already established or is pending...");
		}
	}

	public void connectLeg(SipServletRequest req) throws ServletException, IOException
	{

		req.getSession().setAttribute("UNANSWERED_INVITE", req); 
		req.getSession().setAttribute("PARTICIPANT", this);

		if ( mySipSession != null )
			mySipSession.setAttribute("INVITE_REQUEST",req);
		else {
			log.error("DlgcEMBridgeParticipant::connectLeg - Error sipSesion is NULL");
		}

		byte[] remoteSdp = req.getRawContent();
		log.debug("Participant Connecting Leg of Participant Type " + this.myName );
		log.debug("KAPANGA [1] IN => Invite from OUA  Participant Name =" + this.myName + "OFFER WITH SDP " + new String(remoteSdp) );
		try {
			presentState.connectLeg(this, remoteSdp);
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//Ask Media Server to generate an SDP offer to be used for early media
	//public void connectLegNoSDP() throws ServletException, IOException
	public void generateSdpOfferToMS() throws ServletException, IOException
	{
		try {
			presentState.connectLeg(this);
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setBridgePartner( DlgcEMBridgeParticipant p)
	{
		bridgePartner = p;
	}

	public DlgcEMBridgeParticipant getBridgePartner()
	{
		return bridgePartner;
	}


	public void sendOkToOUA()
	{
		SipServletRequest  request = (SipServletRequest)this.mySipSession.getAttribute("INVITE_REQUEST");
		SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
		try {
			response.send();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void setDestroyRequest( boolean destroy)
	{
		destroyRequest = destroy;
	}

	public void release() throws MsControlException
	{
		if (mySipSession != null && mySipSession.isValid()) 
		{
			log.info("["+mySipSession.getCallId()+"] calling mediaSession.release() for NC");

			if ( destroyRequest ) 
				log.info("This participant sent the cancel or bye request no need to send the bye request");
			else
				sendBye();
			ms.release();	// release the media session controlling the XMS side
			
			//if ( this.myName.equalsIgnoreCase("TUA-PARTICIPANT"))
			//	this.externalTua.sendBye();
			this.myConference.removeParticipantFromConferenceList(this);
			log.info("["+mySipSession.getCallId()+"] calling mediaSession.release() for NC done");		

		}
	}

	protected void sendBye() 
	{
		// Need to check the state of the SipSession in order to make sure user
		// did not already terminate session from their end.
		if (mySipSession != null && mySipSession.isValid() )
		{
			mySipSession.setAttribute("RELEASING", true);
			log.info("["+mySipSession.getCallId()+"] SENDING BYE");
			try {
				SipServletRequest req = mySipSession.createRequest("BYE");
				req.send();
			}
			catch (IOException e) {
				log.error("Unable to send BYE to user agent: "+e);
			}
		}
	}

	public void terminate(Exception e) {

		log.info("["+mySipSession.getCallId()+"] *TERMINATE EXCEPTION* -> "+e);
		e.printStackTrace();
		sendBye();
		try {
			release();
		} catch (MsControlException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void processSipPhoneAck(SipServletRequest req) throws MsControlException 
	{


		try {
			byte[] remoteSdp = req.getRawContent();
			presentState.processSipPhoneAck(this, remoteSdp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected void playPromptParticipant(SipSession mySipSession, URI[] prompts) {

		//MediaGroup myMediaGroup = (MediaGroup) mySipSession.getAttribute("MEDIA_GROUP");
		//this.mg

		try {
			this.mg.getPlayer().play(prompts,null,	Parameters.NO_PARAMETER);
		} catch (MsControlException e) {
			log.error("Unable to mute the participant");
		}
	}

	
	public DlgcEMBridgeConference getConference()
	{
		return myConference;
	}


	public void processDetectorEvent(SignalDetectorEvent anEvent) throws MsControlException 
	{
		if ( (anEvent.getEventType() == SignalDetectorEvent.SIGNAL_DETECTED) && (anEvent.getQualifier() == SignalDetectorEvent.RTC_TRIGGERED) ) 
		{

			log.info("DTMF A DTMF SIGNAL HAS BEEN DETECTED FOR THIS PARTICIPANT");
			return;
		}
		String dtmf = anEvent.getSignalString();
		String buffereddtmf = (String)mySipSession.getAttribute("BUFFERED_SIGNALS");
		
		log.debug("*********>DlgcEMBridgeParticipant::processDetectorEvent dtmf justed pressed="+dtmf);

		if (dtmf.equals("*")) {
			buffereddtmf = "*";
		} else if ( buffereddtmf == null) {
			log.debug("Leaving State.CollectingParticipantOptions - FOUND buffereddtmf null..means no initial match for * thus jumping out baby...");
			return;
		} else if (!buffereddtmf.isEmpty()) {

			// Store the collected digit into the buffer if the buffer isn't empty
			buffereddtmf=buffereddtmf+dtmf;
			

			/*
			 * Check if the caller entered in any valid options and stop digit detection
			 * if its valid
			 */
			for (int i = 0; i < participantOptions.length; i++) {
				if (buffereddtmf.equals(participantOptions[i])) 
				{
					this.presentState.processDtmfDigitsRequest(this, buffereddtmf, anEvent);
					break;
				}
			}

			// Make sure the buffer doesn't go past 3 digits 
			if (buffereddtmf.length() > 3) {buffereddtmf = "";}
		}
		mySipSession.setAttribute("BUFFERED_SIGNALS",buffereddtmf);
		log.info("START PARSING COLLECTED DIGITS DONE");
	}

	public void processPlayerEvent(PlayerEvent playEvent) { 

		if ( playEvent.getError() == MediaErr.NO_ERROR ) {
			try {
				this.presentState.playComplete(this);
			} catch (MsControlException e) {
				e.printStackTrace();
			}
		} else {
			try {
				this.presentState.error(this, playEvent);
			} catch (MsControlException e) {
				e.printStackTrace();
			}
		}
	}

	public void enableAsyncDtmf() throws MsControlException 
	{
		log.info("DlgcEMBridgeParticipant::enableAsyncDtmf");
		//setState(presentState, DlgcEMBridgeParticipantState.dlgcParticipantDtmfEnablingState); 
		Parameters detectorOptions = DlgcEMBridgeServlet.theMediaSessionFactory.createParameters();
		detectorOptions.put(SignalDetector.BUFFERING, Boolean.FALSE );
		EventType[] arrayEnabledEvents = {SignalDetectorEvent.SIGNAL_DETECTED} ;
		detectorOptions.put(SignalDetector.ENABLED_EVENTS, arrayEnabledEvents);	
		log.info("["+mySipSession.getCallId()+"] CALLING RECEIVESIGNALS (-1)");
		mg.getSignalDetector().receiveSignals(-1, null,	null, detectorOptions);

	}

	public void playConferenceStateInfo()
	{
		log.debug("DlgcEMBridgeParticipant::playConferenceStateInfo");
		URI[] prompts = null;
		if ( this.myConference.myConfMode == DlgcEMBridgeConference.ConferenceMode.BRIDGE_MODE ) {
			prompts = new URI[] {URI.create(DlgcEMBridgeServlet.MENU_EMBRIDGE_88_BRIDGE_MODE)};
			log.debug("DlgcEMBridgeParticipant::playConferenceStateInfo => Bridge Mode");
		} else {
			prompts = new URI[] {URI.create(DlgcEMBridgeServlet.MENU_EMBRIDGE_88_FULL_MIXER_MODE) };
			log.debug("DlgcEMBridgeParticipant::playConferenceStateInfo => Mixer Mode");
		}
		try {
			this.mg.getPlayer().play(prompts,null,	Parameters.NO_PARAMETER);
		} catch (MsControlException e) {
			log.error("Unable to play menu to the participant");
		}
	}
	
	public void playMenu00()
	{
		log.debug("DlgcEMBridgeParticipant::playMenu00...");
		URI[] prompts = new URI[] {URI.create(DlgcEMBridgeServlet.MENU_EMBRIDGE_00)};
		log.debug("DlgcEMBridgeParticipant::playHowDoesItWork77");
		try {
			this.mg.getPlayer().play(prompts,null,	Parameters.NO_PARAMETER);
		} catch (MsControlException e) {
			log.error("Unable to play menu to the participant");
		}
	}
	

	public void playHowDoesItWork77()
	{
		URI[] prompts = new URI[] {URI.create(DlgcEMBridgeServlet.MENU_EMBRIDGE_77)};
		log.debug("DlgcEMBridgeParticipant::playHowDoesItWork77");
		try {
			this.mg.getPlayer().play(prompts,null,	Parameters.NO_PARAMETER);
		} catch (MsControlException e) {
			log.error("Unable to play how does it work the participant");
		}
	}



}
