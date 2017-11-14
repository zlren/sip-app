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
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.join.Joinable.Direction;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.mediagroup.signals.SignalDetector;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.bridge_conference.DlgcBridgeConference.ConferenceState;


/**
 * Conference room - Has its own MediaSession, and a MediaMixer
 */
public class DlgcBridgeParticipant implements Serializable {


	private static final long serialVersionUID = 15674333657L;
	
	public static boolean confJoinUseMixerAdapter=true;    //default should be true	
	
	protected DlgcBridgeParticipantState	presentState;
	protected DlgcBridgeParticipantState	previousState;
	public static Logger log = LoggerFactory.getLogger(DlgcBridgeParticipant.class);
	SipSession mySipSession = null;
	NetworkConnection nc = null;
	MediaGroup		  mg = null;
	MediaSession	  ms = null;
	DlgcBridgeParticipant bridgePartner=null;
	
	protected DlgcBridgeConference 	myConference;
	public String myName;
	
	public static final String MENUOPERATION = "MENU_OPERATION";
	
	public static  enum MenuOperation {
		NOTHING, UNMUTE, MUTE, PARK, UNPARK;
	};
	
	public static void setMenuOperation( SipSession mySipSession, MenuOperation mo) {
		mySipSession.setAttribute(MENUOPERATION, mo);
	}
	
	public static MenuOperation getMenuOperation( SipSession mySipSession) {
		return ( (MenuOperation) mySipSession.getAttribute(MENUOPERATION) );
	}
	
	public static boolean getMuttingProcess( SipSession mySipSession) {
		Boolean b = (Boolean)(mySipSession.getAttribute("MUTING_PROCESS") );
		if ( b == null )
			return false;
		else
			return b.booleanValue();
	}
	
	public static void setMutingProcess( SipSession mySipSession, boolean val) {
		mySipSession.setAttribute("MUTING_PROCESS", new Boolean(val)) ;
	}
	
	public void setState( DlgcBridgeParticipantState previous, DlgcBridgeParticipantState present) {
		presentState = present;
		previousState = previous;
	}
	
	public DlgcBridgeParticipantState getPreviousState()
	{
		return previousState;
	}
	
	public DlgcBridgeParticipantState getPresentState()
	{
		return presentState;
	}
	
	public void printState()
	{
		log.debug("DlgcBridgeParticipant::Previous State is " + presentState.stateName);
		log.debug("DlgcBridgeParticipant::Present  State is " + previousState.stateName);
	}
	
	public void executeMute() 
	{
			NetworkConnection myNetworkConnection = this.nc;
			NetworkConnection bridgePartner = this.bridgePartner.nc;
			MediaSession myMediaSession = (MediaSession) mySipSession.getAttribute("MEDIA_SESSION");
			log.debug("Bridge Muting a line");
			log.info("JOIN NC -> NC 2 as RECV ONLY: ");
			try {
				if ( bridgePartner != null ) {
					myNetworkConnection.join(Joinable.Direction.RECV, bridgePartner);
					this.presentState.joinConferenceResponse(this);	//new John
					DlgcBridgeParticipant.setMenuOperation(mySipSession, MenuOperation.MUTE);
					mySipSession.setAttribute("MEDIA_SESSION", myMediaSession);
				} else {
					log.debug("execute mute of leg in bridge - cant execute operation not in a bridge conference - ignoring request");
				}
			} catch (MsControlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void executeUnmute() {
		//try {
			NetworkConnection myNetworkConnection = this.nc;
			NetworkConnection bridgePartner = this.bridgePartner.nc;
			
			MediaSession myMediaSession = (MediaSession) mySipSession.getAttribute("MEDIA_SESSION");
			log.debug("Bridge Unmuting line");
			log.info("JOIN NC -> FULL DUPLEX ");
			try {
				if ( bridgePartner != null ) {
					myNetworkConnection.join(Joinable.Direction.DUPLEX, bridgePartner); 
					this.presentState.unjoinConferenceResponse(this);	//new John
					DlgcBridgeParticipant.setMenuOperation(mySipSession, MenuOperation.UNMUTE);
					mySipSession.setAttribute("MEDIA_SESSION", myMediaSession);
				} else {
					log.debug("execute unmute of leg in bridge - cant execute operation not in a bridge conference - ignoring request");
				}
			} catch (MsControlException e) {
				log.error("Unable to unmute the participant");
			}
	}
	
	public void unjoinConference() {
		if (mySipSession != null && mySipSession.isValid()) {
			log.info(" Unjoining leg from conference.");
			NetworkConnection nc = this.nc;
			DlgcBridgeParticipant peerParticipant = this.myConference.findADestinationParticipant( this );
			NetworkConnection peerNC = peerParticipant.nc;
						
			if ( peerNC != null ) {
				try {
					nc.unjoin(peerNC);
					this.presentState.unjoinConferenceResponse(this);	//new John
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
	

		
	public DlgcBridgeParticipant(DlgcBridgeConference conference, SipSession sipSession, String name) 
	{
			presentState  = DlgcBridgeParticipantState.dlgcParticipantInitialState;
			previousState = DlgcBridgeParticipantState.dlgcParticipantInitialState;
			myConference  = conference;
			mySipSession  = sipSession;
			try {
				myName = name;
				//Properties props = DlgcBridgeServlet.theMediaSessionFactory.getProperties();
				String mxMode = DlgcBridgeServlet.demoPropertyObj.getProperty("DlgcBridgeDemo.media.mixer.mode");
				ms = DlgcBridgeServlet.theMediaSessionFactory.createMediaSession();
				if (mxMode!=null)
					ms.setAttribute("BRIDGE_MODE",mxMode);
				nc = ms.createNetworkConnection(NetworkConnection.BASIC);
				
				Parameters sdpConfiguration = ms.createParameters();
				Map<String,String>  configurationData = new HashMap<String,String>();
				configurationData.put("SIP_REQ_URI_USERNAME", "msml=2323777");
				sdpConfiguration.put(SdpPortManager.SIP_HEADERS, configurationData);
				nc.setParameters(sdpConfiguration);
				
				
				mg = ms.createMediaGroup(MediaGroup.PLAYER_SIGNALDETECTOR);
				ms.setAttribute("NETWORK_CONNECTION", nc);
				ms.setAttribute("MEDIA_GROUP", mg);
				ms.setAttribute("SIP_SESSION", mySipSession);
				mySipSession.setAttribute("MEDIA_SESSION", ms);  
				
				setMenuOperation(mySipSession, MenuOperation.UNMUTE);
				
				mg.getPlayer().addListener(new DlgcBridgeParticipantMediaListener<PlayerEvent>(this));
				
				mg.addListener(new DlgcBridgeSignalDetectorAllocationListener(this));
				nc.getSdpPortManager().addListener(new DlgcBridgeParticipantMediaListener<SdpPortManagerEvent>(this));
				mg.getSignalDetector().addListener(new DlgcBridgeParticipantMediaListener<SignalDetectorEvent>(this));
				
				nc.addListener(new DlgcBridgeParticipantJoinListener(this));
				
				nc.join(Joinable.Direction.DUPLEX, mg);
				
				
			} catch (MsControlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mySipSession.setAttribute("MEDIA_SESSION", ms);
	}

	
	public void bridgeLegs(DlgcBridgeParticipant destParticipant,Direction dir) throws MsControlException
	{
		DlgcBridgeConference bridgeConf = this.myConference;
		ConferenceState confState = bridgeConf.getConfState();
		log.info("joinLegs::joinLegs event request");
		if ( confState == ConferenceState.NOT_IN_CONF ) 
		{
			this.setState(this.presentState, DlgcBridgeParticipantState.dlgcParticipantJoiningConfState);
			destParticipant.setState(this.presentState, DlgcBridgeParticipantState.dlgcParticipantJoiningConfState);
			setBridgePartner( destParticipant );
			destParticipant.setBridgePartner( this );
			try {
				NetworkConnection nc1 = this.nc;
				NetworkConnection nc2 = destParticipant.nc;
				log.info("Conference Bridge joining two NETWORK CONNECTIONS");
				bridgeConf.setConfState(ConferenceState.CONF_PEND);
				//NOTE joinEventListener is called with the join result
				//this listener should set the proper conference state
				nc1.join(dir, nc2);	
				this.presentState.joinConferenceResponse(this);	//new John
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
		
		byte[] remoteSdp = req.getRawContent();
		try {
			presentState.connectLeg(this, remoteSdp);
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setBridgePartner( DlgcBridgeParticipant p)
	{
		bridgePartner = p;
	}
	
	public DlgcBridgeParticipant getBridgePartner()
	{
		return bridgePartner;
	}
	
	
	
	
	public void release() throws MsControlException
	{
		if (mySipSession != null && mySipSession.isValid()) 
		{
        	
        	//ConferenceSession myConferenceSession = getConferenceSession(mySipSession);
			log.info("["+mySipSession.getCallId()+"] calling mediaSession.release() for NC");
			ms.release();
			this.myConference.removeParticipantFromConferenceList(this);
			log.info("["+mySipSession.getCallId()+"] calling mediaSession.release() for NC done");		
			if (mySipSession.isValid()) {
				mySipSession.invalidate();
			}
			if (mySipSession.getApplicationSession().isValid()) {
				mySipSession.getApplicationSession().invalidate();
			}
		}
	}
	
	protected void sendBye() 
	{
		// Need to check the state of the SipSession in order to make sure user
		// did not already terminate session from their end.
		Boolean releasing = (Boolean)mySipSession.getAttribute("RELEASING");
		if (mySipSession != null && mySipSession.isValid() && mySipSession.getState() != SipSession.State.TERMINATED && !releasing)
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
	
	protected void playNumberOfConferenceParticipants() 
	{

		//ConferenceSession myConferenceSession = getConferenceSession(mySipSession);
		
		// Play the "The size of the conference is " prompt and then the speech synthesized number
		//mySipSession.setAttribute("STATE",State.PlayingPrompt);
		
			playPromptParticipant (mySipSession, new URI[] {URI.create(DlgcBridgeConstants.XMS_PROMPT_CONFERENCE_SIZE)});
	
	}

	
	//check for dtmf options selections
	public void processDetectorEvent(SignalDetectorEvent anEvent) throws MsControlException 
	{
		if ( (anEvent.getEventType() == SignalDetectorEvent.SIGNAL_DETECTED) && (anEvent.getQualifier() == SignalDetectorEvent.RTC_TRIGGERED) ) 
		{
			
			log.info("DTMF A DTMF SIGNAL HAS BEEN DETECTED FOR THIS PARTICIPANT");
			return;
		}
		log.info("["+mySipSession.getCallId()+"] SIGNALDETECTOR EVENT -> "+anEvent.getEventType());;
		String dtmf = anEvent.getSignalString();
		String buffereddtmf = (String)mySipSession.getAttribute("BUFFERED_SIGNALS");
			
		// If caller enters in *, reset the digit buffer and buffer the *
		if (dtmf.equals("*")) {
			buffereddtmf = "*";
		} else if ( buffereddtmf == null) {
			log.debug("Leaving State.CollectingParticipantOptions - FOUND buffereddtmf null.. jumping out baby...");
			return;
		} else if (!buffereddtmf.isEmpty()) {
				
			// Store the collected digit into the buffer if the buffer isn't empty
			buffereddtmf=buffereddtmf+dtmf;
				
			/*
			 * Check if the caller entered in any valid options and stop digit detection
			 * if its valid
			 */
			for (int i = 0; i < DlgcBridgeConstants.participantOptions.length; i++) {
				if (buffereddtmf.equals(DlgcBridgeConstants.participantOptions[i])) 
				{
					//processDtmfSelectedOption(buffereddtmf, anEvent); 
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
		log.info("In State: enableAsyncDtmf::processSipPhoneAck (same as processAnswer) event");
		 
		Parameters detectorOptions = DlgcBridgeServlet.theMediaSessionFactory.createParameters();
		detectorOptions.put(SignalDetector.BUFFERING, Boolean.FALSE );
		EventType[] arrayEnabledEvents = {SignalDetectorEvent.SIGNAL_DETECTED} ;
		detectorOptions.put(SignalDetector.ENABLED_EVENTS, arrayEnabledEvents);	
		log.info("["+mySipSession.getCallId()+"] CALLING RECEIVESIGNALS (-1)");
		setState(presentState, DlgcBridgeParticipantState.dlgcParticipantDtmfEnabledState);
		mg.getSignalDetector().receiveSignals(-1, null,	null, detectorOptions);
	}
	
}
