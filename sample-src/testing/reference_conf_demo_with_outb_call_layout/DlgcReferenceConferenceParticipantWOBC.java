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

package testing.reference_conf_demo_with_outb_call_layout;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.join.JoinableStream.StreamType;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.mixer.MediaMixer;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.servlet.ServletException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionsUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Remember the Conference room The parent for DlgcReferenceConferenceParticipant
 *  - Has its own MediaSession, and a MediaMixer
 */
public class DlgcReferenceConferenceParticipantWOBC implements Serializable {


	private static final long serialVersionUID = 1L;
	
	public static boolean confJoinUseMixerAdapter=true;    //default should be true	
	
	protected DlgcReferenceConferenceParticipantStateWOBC	presentState;
	protected DlgcReferenceConferenceParticipantStateWOBC	previousState;
	public static Logger log = LoggerFactory.getLogger(DlgcReferenceConferenceParticipantWOBC.class);
	transient SipSession clientEndPointSipSession = null;
	NetworkConnection nc = null;
	MediaGroup		  mg = null;
	MediaSession	  ms = null;
	byte[] remoteSdp = null;		//Media Server SDP
	byte[] localSdp = null;			//Sip Phone SDP
	String 	dirLeg=null;
	
	//DlgcOutBound  outBoundCaller;
	
	String clientSASID;
	String clientSSID;
	
	DlgcReferenceConferenceWOBC 	myConference;
	
	public static final String LEG_MENU_OPERATION = "LEG_MENU_OPERATION";
	
	public static  enum LegMenuOperation {
		NOTHING, UNMUTE, MUTE, PARK, UNPARK;
	};
	
	
	public DlgcReferenceConferenceParticipantWOBC(DlgcReferenceConferenceWOBC conference, SipSession sipSession, DlgcOutbCallConferenceStorage cfStorage) 
	{
		presentState  = DlgcReferenceConferenceParticipantStateWOBC.dlgcParticipantInitialStateWOBC;
		previousState = DlgcReferenceConferenceParticipantStateWOBC.dlgcParticipantInitialStateWOBC;
		myConference  = conference;
		clientEndPointSipSession  = sipSession;
		clientSASID = sipSession.getApplicationSession().getId();
		clientSSID = sipSession.getId();

		try {
			//Note as per Rule one Media Session per invite (call)
			ms = DlgcReferenceConferenceWithOutBCallServlet.theMediaSessionFactory.createMediaSession();
			nc = ms.createNetworkConnection(NetworkConnection.BASIC);
			Parameters sdpConfiguration = ms.createParameters();
			Map<String,String>  configurationData = new HashMap<String,String>();
			configurationData.put("SIP_REQ_URI_USERNAME", "msml=3232777");
			sdpConfiguration.put(SdpPortManager.SIP_HEADERS, configurationData);
			nc.setParameters(sdpConfiguration);
			
			mg = ms.createMediaGroup(MediaGroup.PLAYER_SIGNALDETECTOR);
			ms.setAttribute("NETWORK_CONNECTION", nc);
			ms.setAttribute("MEDIA_GROUP", mg);
			ms.setAttribute("SIP_SESSION", clientEndPointSipSession);
			ms.setAttribute("ParticipantId", clientSSID);
			ms.setAttribute("DlgcOutbCallConferenceStorage", cfStorage);
			clientEndPointSipSession.setAttribute("MEDIA_SESSION", ms);  

			setMenuOperation(clientEndPointSipSession, LegMenuOperation.UNMUTE);


			//Add Event Listeners for each required JSR309 Component
			DlgcReferenceParticipantMediaListenerWOBC<PlayerEvent> l = new DlgcReferenceParticipantMediaListenerWOBC<PlayerEvent>();
			mg.getPlayer().addListener(l);

			DlgcReferenceParticipantMediaListenerWOBC<SignalDetectorEvent> lsd = new DlgcReferenceParticipantMediaListenerWOBC<SignalDetectorEvent>();
			mg.getSignalDetector().addListener(lsd);


			mg.addListener(new DlgcReferenceSignalDetectorAllocationListenerWOBC(this));

			DlgcReferenceParticipantMediaListenerWOBC<SdpPortManagerEvent> la = new DlgcReferenceParticipantMediaListenerWOBC<SdpPortManagerEvent>();
			nc.getSdpPortManager().addListener(la);


			nc.addListener(new DlgcReferenceNcJoinMixerListenerWOBC(this));

			//Join Network Connection to Media Group
			//Allows us to play to the leg or collect dtmfs
			//Properties props = DlgcReferenceConferenceWithOutBCallServlet.theMediaSessionFactory.getProperties();

			Joinable.Direction jsr309Dir = Joinable.Direction.DUPLEX; 
			if ( myConference.getNumberOfParticipants() == 0 )
				dirLeg = DlgcReferenceConferenceWithOutBCallServlet.demoPropertyObj.getProperty("demos.join.direction.leg1");
			else if ( myConference.getNumberOfParticipants() == 1 )
				dirLeg = DlgcReferenceConferenceWithOutBCallServlet.demoPropertyObj.getProperty("demos.join.direction.leg2");
			else if ( myConference.getNumberOfParticipants() == 2 )
				dirLeg = DlgcReferenceConferenceWithOutBCallServlet.demoPropertyObj.getProperty("demos.join.direction.leg3");

			if ( dirLeg == null)
				dirLeg = "duplex";
			else if ( dirLeg.compareToIgnoreCase("recv") == 0 ) 
				jsr309Dir = Joinable.Direction.RECV;
			else if ( dirLeg.compareToIgnoreCase("send") == 0 ) 
				jsr309Dir = Joinable.Direction.SEND;

			nc.join(Joinable.Direction.DUPLEX, mg);

			Boolean bDemoJoinStreamEnableFlag = false;
			String sProp = DlgcReferenceConferenceWithOutBCallServlet.demoPropertyObj.getProperty("demos.join.stream.enable");
			if ( sProp != null ) {
				bDemoJoinStreamEnableFlag = new Boolean(sProp);
			}

			Integer iDemoJoinStreamLegNumber = 0;
			sProp = DlgcReferenceConferenceWithOutBCallServlet.demoPropertyObj.getProperty("demos.join.stream.legNumber");
			if ( sProp != null ) {
				iDemoJoinStreamLegNumber = new Integer(sProp);
			}

			StreamType demoStringType = StreamType.audio;
			String sDemoJoinStreamType = new String("audio");
			sProp = DlgcReferenceConferenceWithOutBCallServlet.demoPropertyObj.getProperty("demos.join.stream.type");
			if ( sProp != null ) {
				sDemoJoinStreamType = sProp;
				if ( sDemoJoinStreamType.compareToIgnoreCase("audio") == 0 ) {
					demoStringType = StreamType.audio;
				} else if ( sDemoJoinStreamType.compareToIgnoreCase("video") == 0 ) {
					demoStringType = StreamType.video;
				}
			}


			if ( myConference.confMediaMixerAdapter != null ) {
				log.debug("...joining participant to conference mixer adapter");

				if ( bDemoJoinStreamEnableFlag == false ) {
					log.debug("Joining using full nc join");
					try {
						nc.join(jsr309Dir, myConference.confMediaMixerAdapter);
						if ( DlgcReferenceConferenceWithOutBCallServlet.apiSyncModeProperty ) {
							log.debug("DlgcReferenceConferenceWithOutBCallServlet returned from synchronous nc join to Mixer Adapter executing next step here no Join Event");
							this.presentState.joinConferenceResponse(this);
						}else {
							log.debug("DlgcReferenceConferenceWithOutBCallServlet returned from asynchronous nc join to Mixer Adapter... waiting for Join Event");
						}	
					} catch (MsControlException e1) {
						log.error(e1.toString());
						//log.error("Calling release for this leg...join failed");
						//this.release();
					}
					
				}else {
					if ( myConference.getNumberOfParticipants() == (iDemoJoinStreamLegNumber - 1) ) {
						//this is the se
						log.debug("Joining using nc.getJoinableStream(StreamType.audio) of type: " + sDemoJoinStreamType);
						try {
							nc.getJoinableStream(demoStringType).join(jsr309Dir, myConference.confMediaMixerAdapter);
							if ( DlgcReferenceConferenceWithOutBCallServlet.apiSyncModeProperty ) {
								log.debug("DlgcReferenceConferenceWithOutBCallServlet returned from synchronous nc join stream to Mixer Adapter executing next step here no Join Event");
								this.presentState.joinConferenceResponse(this);
							}else {
								log.debug("DlgcReferenceConferenceWithOutBCallServlet returned from asynchronous nc join stream to Mixer Adapter... waiting for Join Event");
							}	
						} catch (MsControlException e1) {
							log.error(e1.toString());
							log.error("Calling release for this leg...join stream failed");
							this.release();
						}
					}else {
						log.debug("Joining using full nc join");
						try {
							nc.join(jsr309Dir, myConference.confMediaMixerAdapter);
							if ( DlgcReferenceConferenceWithOutBCallServlet.apiSyncModeProperty ) {
								log.debug("DlgcReferenceConferenceWithOutBCallServlet returned (else part) from synchronous nc join to Mixer Adapter executing next step here no Join Event");
								this.presentState.joinConferenceResponse(this);
							}else {
								log.debug("DlgcReferenceConferenceWithOutBCallServlet returned (else part)  from asynchronous nc join to Mixer Adapter... waiting for Join Event");
							}	
						} catch (MsControlException e1) {
							log.error(e1.toString());
							log.error("Calling release for this leg...join failed");
							this.release();
						}
					}
				}
				
			} else if ( myConference.confMediaMixer != null ) {
				log.debug("...joining participant to conference mixer");

				if ( bDemoJoinStreamEnableFlag == false ) {
					log.debug("Joining using full nc join");
					try {
						nc.join(jsr309Dir, myConference.confMediaMixer);
						if ( DlgcReferenceConferenceWithOutBCallServlet.apiSyncModeProperty ) {
							log.debug("DlgcReferenceConferenceWithOutBCallServlet returned from synchronous nc join to Mixer executing next step here no Join Event");
							this.presentState.joinConferenceResponse(this);
						}else {
							log.debug("DlgcReferenceConferenceWithOutBCallServlet returned from asynchronous nc join to Mixer... waiting for Join Event");
						}	
					} catch (MsControlException e1) {
						log.error(e1.toString());
						log.error("Calling release for this leg...join failed");
						this.release();
					}
				}else {
					if ( myConference.getNumberOfParticipants() == (iDemoJoinStreamLegNumber - 1) ) {
						log.debug("Joining using nc.getJoinableStream(StreamType.audio) of type: " + sDemoJoinStreamType);
						try {
							nc.getJoinableStream(demoStringType).join(jsr309Dir, myConference.confMediaMixer);
							if ( DlgcReferenceConferenceWithOutBCallServlet.apiSyncModeProperty ) {
								log.debug("DlgcReferenceConferenceWithOutBCallServlet returned from synchronous nc join stream to Mixer executing next step here no Join Event");
								this.presentState.joinConferenceResponse(this);
							}else {
								log.debug("DlgcReferenceConferenceWithOutBCallServlet returned from asynchronous nc join stream to Mixer... waiting for Join Event");
							}	
						} catch (MsControlException e1) {
							log.error(e1.toString());
							log.error("Calling release for this leg...join failed");
							this.release();
						}
					}else {
						log.debug("Joining using full nc join");
						try {
							nc.join(jsr309Dir, myConference.confMediaMixer);
							if ( DlgcReferenceConferenceWithOutBCallServlet.apiSyncModeProperty ) {
								log.debug("DlgcReferenceConferenceWithOutBCallServlet returned (else part) from synchronous nc join to Mixer executing next step here no Join Event");
								this.presentState.joinConferenceResponse(this);
							}else {
								log.debug("DlgcReferenceConferenceWithOutBCallServlet returned (else part) from asynchronous nc join to Mixer... waiting for Join Event");
							}	
						} catch (MsControlException e1) {
							log.error(e1.toString());
							log.error("Calling release for this leg...join failed");
							this.release();
						}						
					}
				}
				//myConference.confMediaMixer.join(jsr309Dir, nc);
				//Note no mixer confirm thus we are using non control conference leg. 
			} else {
				log.debug("....not joining participant to conference mixer... going into IVR mode...Warning::Demo not intented to do this...");
			}

		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clientEndPointSipSession.setAttribute("MEDIA_SESSION", ms);
	}
	
	//create an Out Bound Call Leg
	public DlgcReferenceConferenceParticipantWOBC(DlgcReferenceConferenceWOBC conference, DlgcOutbCallConferenceStorage cfStorage) 
	{
			presentState  = DlgcReferenceConferenceParticipantStateWOBC.dlgcParticipantInitialStateWOBC;
			previousState = DlgcReferenceConferenceParticipantStateWOBC.dlgcParticipantInitialStateWOBC;
			myConference  = conference;
			clientEndPointSipSession  = null;
		    clientSASID = null;
		    clientSSID = null;
			try {
				//Note as per Rule one Media Session per invite (call)
				ms = DlgcReferenceConferenceWithOutBCallServlet.theMediaSessionFactory.createMediaSession();
				nc = ms.createNetworkConnection(NetworkConnection.BASIC);
				mg = ms.createMediaGroup(MediaGroup.PLAYER_SIGNALDETECTOR);
				ms.setAttribute("NETWORK_CONNECTION", nc);
				ms.setAttribute("MEDIA_GROUP", mg);
				
				ms.setAttribute("DlgcOutbCallConferenceStorage", cfStorage);
				
				
				//Add Event Listeners for each required JSR309 Component
				//mg.getPlayer().addListener(new DlgcReferenceParticipantMediaListenerWOBC<PlayerEvent>(this));
				DlgcReferenceParticipantMediaListenerWOBC<PlayerEvent> l = new DlgcReferenceParticipantMediaListenerWOBC<PlayerEvent>();
				/* at this point set a bogus participant id which it should be the sipSession to the outbound call but we dont have this at 
				 * this point... it is needed in the 200 OK callback generated by the Media Session
				 */
				Random r = new Random(10000);
				Long lRan = r.nextLong();
				clientSSID = new String("TempId:" + lRan.toString());
				mg.getPlayer().addListener(l);
				
				ms.setAttribute("ParticipantId",clientSSID);

				DlgcReferenceParticipantMediaListenerWOBC<SignalDetectorEvent> lsd = new DlgcReferenceParticipantMediaListenerWOBC<SignalDetectorEvent>();
				mg.getSignalDetector().addListener(lsd);
				
				mg.addListener(new DlgcReferenceSignalDetectorAllocationListenerWOBC(this));
				
				
				DlgcReferenceParticipantMediaListenerWOBC<SdpPortManagerEvent> la = new DlgcReferenceParticipantMediaListenerWOBC<SdpPortManagerEvent>();
				nc.getSdpPortManager().addListener(la);
				
				nc.addListener(new DlgcReferenceNcJoinMixerListenerWOBC(this));
				nc.join(Joinable.Direction.DUPLEX, mg);
				
				
			} catch (MsControlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}
	
	public void generateSdpOfferToMS() throws ServletException, IOException
	{
		log.debug("DlgcReferenceConferenceParticipant:: generateSdpOfferToMS: Do nothing override by subclass");
	}
	
		
	public void makeOutboundCall( String msAnsweredSDPString)
	{
		log.debug("DlgcReferenceConferenceParticipant:: makeOutboundCall: Do nothing override by subclass");

	}
	
	public SipSession loadSipSession()
	{
		SipSessionsUtil ssu = DlgcReferenceConferenceWithOutBCallServlet.getSSU();
		SipApplicationSession sas = ssu.getApplicationSessionById(clientSASID);
		clientEndPointSipSession = sas.getSipSession(clientSSID);
		log.debug("DlgcReferenceConferenceParticipant:: loadSipSession: Application Sip Session: " + clientEndPointSipSession.toString());
		return clientEndPointSipSession;
	}

	public String getLegSipSessionId()
	{
		return this.clientSSID;
	}
	
	public void joinConference(SipServletRequest req) throws ServletException, IOException
	{
		
		req.getSession().setAttribute("UNANSWERED_INVITE", req);  
		req.getSession().setAttribute("PARTICIPANT", this);
		
		byte[] remoteSdp = req.getRawContent();
		try {
			presentState.enterConference(this, remoteSdp);		//call the participant state machine to enter conference
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void sendOkToOUA()
	{    
		SipServletRequest  request = (SipServletRequest)this.clientEndPointSipSession.getAttribute("INVITE_REQUEST");
		SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
		try {
			response.send();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void release() throws MsControlException
	{
		if (clientEndPointSipSession != null && clientEndPointSipSession.isValid()) 
		{
        	
			log.info("["+clientEndPointSipSession.getCallId()+"] calling mediaSession.release() for NC");
			ms.release();
			log.info("["+clientEndPointSipSession.getCallId()+"] calling mediaSession.release() for NC done");		
			if (clientEndPointSipSession.isValid()) {
				clientEndPointSipSession.invalidate();
			}
			//if (clientEndPointSipSession.getApplicationSession().isValid()) {
			//	clientEndPointSipSession.getApplicationSession().invalidate();
			//}
		}
	}
	
	protected void sendBye() 
	{
		// Need to check the state of the SipSession in order to make sure user
		// did not already terminate session from their end.
		Boolean releasing = (Boolean)clientEndPointSipSession.getAttribute("RELEASING");
		if (clientEndPointSipSession != null && clientEndPointSipSession.isValid() && clientEndPointSipSession.getState() != SipSession.State.TERMINATED && !releasing)
		{
			clientEndPointSipSession.setAttribute("RELEASING", true);
			log.info("["+clientEndPointSipSession.getCallId()+"] SENDING BYE");
			try {
				SipServletRequest req = clientEndPointSipSession.createRequest("BYE");
				req.send();
			}
			catch (IOException e) {
				log.error("Unable to send BYE to user agent: "+e);
			}
		}
	}
	
	public void terminate(Exception e) {
		
		log.info("["+clientEndPointSipSession.getCallId()+"] *TERMINATE EXCEPTION* -> "+e);
		e.printStackTrace();
		sendBye();
		try {
			release();
		} catch (MsControlException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void processSipPhoneAck(SipServletRequest request) throws MsControlException 
	{
		log.debug("Entering processSipPhoneAck() " );

		log.debug("Leaving processSipPhoneAck() " );

	}
	
	public void processSipPhone200Invite(SipServletResponse response) throws MsControlException 
	{
		log.debug("Entering processSipPhone200Invite()  should never get here only true for OutboundParticipant" );

	}
	
	
	protected void playPromptParticipant(SipSession clientEndPointSipSession, URI[] prompts) 
	{
		try {
			this.mg.getPlayer().play(prompts,null,	Parameters.NO_PARAMETER);
			if ( DlgcReferenceConferenceWithOutBCallServlet.apiSyncModeProperty ) {
				log.debug("DlgcReferenceConferenceParticipant returned from synchronous play executing not waiting for Event play resume");
			}else {
				log.debug("DlgcReferenceConferenceParticipant returned from asynchronous play executing waiting for Event play resume");
			}	
		} catch (MsControlException e) {
			log.error(e.toString());
			log.error("Fail to start plaing.");		
		}
	}
	
	
	protected void playNumberOfConferenceParticipants() 
	{	
		playPromptParticipant (clientEndPointSipSession, new URI[] {URI.create(DlgcReferenceConferencePromptsWOBC.XMS_PROMPT_CONFERENCE_SIZE)});
	}

	protected void confRecord()
	{
		log.debug("DlgcReferenceConferenceParticipant::confReocrd");
		if ( myConference != null )
			myConference.record();
	}
	
	protected void setVideoLayout(String videolayout)
	{
		log.debug("DlgcReferenceConferenceParticipant::setVideoLayout");
		if ( myConference != null )
			myConference.setVideoLayout(videolayout);
	}
	
	protected void confStopRecord()
	{
		log.debug("DlgcReferenceConferenceParticipant::confStopReocrd");
		if ( myConference != null )
			myConference.stopRecord();
	}

	
	//check for dtmf options selections
	public void processDetectorEvent(SignalDetectorEvent anEvent) throws MsControlException 
	{
		if ( (anEvent.getEventType() == SignalDetectorEvent.SIGNAL_DETECTED) && (anEvent.getQualifier() == SignalDetectorEvent.RTC_TRIGGERED) ) 
		{
			
			log.info("DTMF A DTMF SIGNAL HAS BEEN DETECTED FOR THIS PARTICIPANT");
			return;
		}
		log.info("["+clientEndPointSipSession.getCallId()+"] SIGNALDETECTOR EVENT -> "+anEvent.getEventType());;
		String dtmf = anEvent.getSignalString();
		String buffereddtmf = (String)clientEndPointSipSession.getAttribute("BUFFERED_SIGNALS");
			
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
			for (int i = 0; i < DlgcReferenceConferenceConstantsWOBC.participantOptions.length; i++) {
				if (buffereddtmf.equals(DlgcReferenceConferenceConstantsWOBC.participantOptions[i])) 
				{
					this.presentState.processDtmfDigitsRequest(this, buffereddtmf, anEvent);
					break;
				}
			}

			// Make sure the buffer doesn't go past 3 digits 
			if (buffereddtmf.length() > 3) {buffereddtmf = "";}
		}
		clientEndPointSipSession.setAttribute("BUFFERED_SIGNALS",buffereddtmf);
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
	
	public void executeMute() {
		try {
			NetworkConnection myNetworkConnection = this.nc;
			MediaMixer mx =myConference.confMediaMixer;
			MediaSession myMediaSession = (MediaSession) clientEndPointSipSession.getAttribute("MEDIA_SESSION");
			log.debug("Muting line");
			log.info("JOIN NC -> MIXER RECV ONLY: MUTING");
			myNetworkConnection.join(Joinable.Direction.RECV, mx);
			if ( DlgcReferenceConferenceWithOutBCallServlet.apiSyncModeProperty ) {
				log.debug("DlgcReferenceConferenceWithOutBCallServlet returned from synchronous nc join to Mixer (MUTING - NC receive only) executing not waiting for Join Event");
			}else {
				log.debug("DlgcReferenceConferenceWithOutBCallServlet returned from asynchronous nc join to Mixer (MUTING - NC receive only)... waiting for Join Event");
			}	
			DlgcReferenceConferenceParticipantWOBC.setMenuOperation(clientEndPointSipSession, LegMenuOperation.MUTE);
			clientEndPointSipSession.setAttribute("MEDIA_SESSION", myMediaSession);
		} catch (MsControlException e) {
			log.error("Unable to mute the participant");
		}

	}
	
	public void executeUnmute() {
		try {
			NetworkConnection myNetworkConnection = this.nc;
			MediaMixer mx =myConference.confMediaMixer;
			MediaSession myMediaSession = (MediaSession) clientEndPointSipSession.getAttribute("MEDIA_SESSION");
			log.debug("Unmuting line");
			log.info("JOIN NC -> FULL DUPLEX Remove Leg From being Mute");
			myNetworkConnection.join(Joinable.Direction.DUPLEX, mx);
			if ( DlgcReferenceConferenceWithOutBCallServlet.apiSyncModeProperty ) {
				log.debug("DlgcReferenceConferenceWithOutBCallServlet returned from synchronous nc join to Mixer (UNMUTING - NC FULL JOIN) executing not waiting for Join Event");
			}else {
				log.debug("DlgcReferenceConferenceWithOutBCallServlet returned from asynchronous nc join to Mixer (UNMUTING - NC FULL JOIN)... waiting for Join Event");
			}	
			DlgcReferenceConferenceParticipantWOBC.setMenuOperation(clientEndPointSipSession, LegMenuOperation.UNMUTE);
			clientEndPointSipSession.setAttribute("MEDIA_SESSION", myMediaSession);
		} catch (MsControlException e) {
			log.error("Unable to unmute the participant");
		}

	}
	
	//JOHN WORK PENDING
	public void unjoinConference() {
		if (clientEndPointSipSession != null && clientEndPointSipSession.isValid()) {
			log.info(" Unjoining leg from conference.");
			NetworkConnection myNetworkConnection = this.nc;
			log.info("Unjoining Session from Conference using Mixer");
			MediaMixer mx = myConference.confMediaMixer;
			if ( mx != null ) {
				try {
					myNetworkConnection.unjoin(mx);
				}catch (MsControlException ex)
				{
					log.error("Error unjoing from conference ", ex);
					ex.printStackTrace();
				}catch (Exception ex) {
					log.error("Error unjoing from conference ", ex);
					ex.printStackTrace();
				}
			}else {
				log.info("["+clientEndPointSipSession.getCallId()+"] could not Unjoining Session from Conference using Mixer since MediaMixer was found to be null");
			}
		}

	}
	
	public void rejoinConference() {
		try {
			NetworkConnection myNetworkConnection = this.nc;
			MediaMixer mx = myConference.confMediaMixer;
			log.info("Conference leg directly joining Mixer");
			log.info("RE-JOINING Conference");
			
			Joinable.Direction jsr309Dir = Joinable.Direction.DUPLEX;
			if ( dirLeg.compareToIgnoreCase("recv") == 0 ) 
				jsr309Dir = Joinable.Direction.RECV;
			else if ( dirLeg.compareToIgnoreCase("send") == 0 ) 
					jsr309Dir = Joinable.Direction.SEND;
			
			myNetworkConnection.join(jsr309Dir, mx);
			if ( DlgcReferenceConferenceWithOutBCallServlet.apiSyncModeProperty ) {
				log.debug("DlgcReferenceConferenceWithOutBCallServlet returned from synchronous nc join to Mixer (rejoinConference - NC FULL JOIN) executing not waiting for Join Event");
			}else {
				log.debug("DlgcReferenceConferenceWithOutBCallServlet returned from asynchronous nc join to Mixer (rejoinConference - NC FULL JOIN)... waiting for Join Event");
			}	
		}
		catch (MsControlException e)
		{
			log.error("Exception found: " +e);
		}
		catch (Exception e) {
			log.error("Exception found: " +e);
		}
	}
	public static void setMenuOperation( SipSession clientEndPointSipSession, LegMenuOperation mo) {
		clientEndPointSipSession.setAttribute(LEG_MENU_OPERATION, mo);
	}
	
	public static LegMenuOperation getMenuOperation( SipSession clientEndPointSipSession) {
		return ( (LegMenuOperation) clientEndPointSipSession.getAttribute(LEG_MENU_OPERATION) ); 
	}
	
	public static boolean getMuttingProcess( SipSession clientEndPointSipSession) {
		Boolean b = (Boolean)(clientEndPointSipSession.getAttribute("MUTING_PROCESS") );
		if ( b == null )
			return false;
		else
			return b.booleanValue();
	}
	
	public static void setMutingProcess( SipSession clientEndPointSipSession, boolean val) {
		clientEndPointSipSession.setAttribute("MUTING_PROCESS", new Boolean(val)) ;
	}
	
	public void setState( DlgcReferenceConferenceParticipantStateWOBC previous, DlgcReferenceConferenceParticipantStateWOBC present) {
		presentState = present;
		previousState = previous;
	}
	
	public DlgcReferenceConferenceParticipantStateWOBC getPreviousState()
	{
		return previousState;
	}
	
	public DlgcReferenceConferenceParticipantStateWOBC getPresentState()
	{
		return presentState;
	}
	
	public void printState()
	{
		log.debug("DlgcReferenceConferenceParticipantWOBC::Previous State is " + presentState.stateName);
		log.debug("DlgcReferenceConferenceParticipantWOBC::Present  State is " + previousState.stateName);
	}

	
	

	
}
