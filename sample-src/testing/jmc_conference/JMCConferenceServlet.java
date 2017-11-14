/**
 * DIALOGIC CONFIDENTIAL      
 * Copyright (C) 2005-2014 Dialogic Corporation. All Rights Reserved.
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

package testing.jmc_conference;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Date;

import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaEvent;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.MsControlFactory;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.JoinEvent;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.Player;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.mediagroup.RecorderEvent;
import javax.media.mscontrol.mediagroup.signals.SignalDetector;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.mixer.MediaMixer;
import javax.media.mscontrol.mixer.MixerAdapter;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.networkconnection.SdpPortManagerException;
import javax.media.mscontrol.resource.AllocationEvent;
import javax.media.mscontrol.resource.AllocationEventListener;
import javax.media.mscontrol.resource.RTC;
import javax.media.mscontrol.spi.Driver;
import javax.media.mscontrol.spi.DriverManager;
import javax.servlet.ServletException;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionsUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.DlgcDemoProperty;
import testing.jmc_conference.DlgcConferenceStorageMgr.DlgcConferenceInfo;
import testing.jmc_conference.DlgcConferenceStorageMgr.DlgcConferenceStorageMgrException;
import testing.jmc_conference.DlgcConferenceStorageMgr.DlgcConferenceStorageMgrExceptionTypes;



/**
 * Servlet answering SIP conference service incoming calls
 */

public class JMCConferenceServlet extends SipServlet implements Serializable, SipApplicationSessionListener, SipServletListener {

	private static final long serialVersionUID = 1;
	static  DlgcDemoProperty			demoPropertyObj;


	// Store play prompts
	private final static String XMS_PROMPT_ENTER_CONFERENCE_ID = "file:////var/lib/xms/media/en_US/verification/snow/prompts/enter_conf_id.wav";
	private final static String XMS_PROMPT_WELCOME_TO_CONFERENCE = "file:////var/lib/xms/media/en_US/verification/snow/prompts/welcome_to_conf.wav";
	
	private final static String XMS_PROMPT_CONFERENCE_MUTE = "file:////var/lib/xms/media/en_US/verification/snow/prompts/muted.wav";
	private final static String XMS_PROMPT_CONFERENCE_UNMUTE = "file:////var/lib/xms/media/en_US/verification/snow/prompts/unmuted.wav";
	
	
	private final static String XMS_MUSIC_NBLUES = "file:////var/lib/xms/media/en_US/verification/demoJSR309/player/intro.wav";
	

	private final static String XMS_PROMPT_PARKED_FROM_CONFERENCE =  "file:////var/lib/xms/media/en_US/verification/demoJSR309/conferencedemo/jmclegunjoin.wav";
	private final static String XMS_PROMPT_UNPARKED_FROM_CONFERENCE= "file:////var/lib/xms/media/en_US/verification/demoJSR309/conferencedemo/jmclegrejoin.wav";
	

	// Store valid caller options while in a conference
	
	private final static String PARTICIPANT_OPTION_MUTE_TOGGLE = "*31";
	private final static String PARTICIPANT_OPTION_UNMUTE_TOGGLE = "*32";

	private final static String PARTICIPANT_OPTION_UNJOIN_CONFERENCE = "*02";
	private final static String PARTICIPANT_OPTION_REJOIN_CONFERENCE_USING_MIXER  = "*03";
	
	private final static String PARTICIPANT_OPTION_PLAY_NBLUES = "*05";
	
	private final static String PARTICIPANT_OPTION_STOP_PLAY = "*99";
	
	
	//Dec 13 2015
	private final static String CONFERENCE_START_RECORDING = "*21";
	private final static String CONFERENCE_STOP_RECORDING = "*22";



	private final static String[] participantOptions = { 
		PARTICIPANT_OPTION_MUTE_TOGGLE, 
		PARTICIPANT_OPTION_UNMUTE_TOGGLE,
		PARTICIPANT_OPTION_UNJOIN_CONFERENCE,
		PARTICIPANT_OPTION_REJOIN_CONFERENCE_USING_MIXER,
		PARTICIPANT_OPTION_PLAY_NBLUES,
		PARTICIPANT_OPTION_STOP_PLAY,
		CONFERENCE_START_RECORDING,
		CONFERENCE_STOP_RECORDING
	};   

	
	
	public static boolean confJoinUseMixerAdapter=true;    //default should be true	
	protected Boolean isReady = false;
	public transient static MsControlFactory theMediaSessionFactory;
	private static Logger log = LoggerFactory.getLogger(JMCConferenceServlet.class);
	private transient static String dlgcDriverName = "com.dialogic.dlg309";


	public JMCConferenceServlet jcs = this;
	public transient SipSessionsUtil sipSessionsUtil;

	public static DlgcConferenceStorageMgr confMgr = null;

	public static final String MENUOPERATION = "MENU_OPERATION";

	protected boolean dlgcSipServletLoaded=false;
	protected String platform = null; 
	protected Boolean jmcConferenceServletInitCalled = false;
	protected static final String ORACLE_PLATFORM = "ORACLE";
	protected static final String TELESTAX_PLATFORM    = "TELESTAX";
	protected static final String TROPO_PLATFORM    = "TROPO";
	private boolean servletInitializedFlag = false;
	boolean useConfirm =false;

	public void servletInitialized(SipServletContextEvent evt){

		String sName = evt.getSipServlet().getServletName();


		if ( ( platform != null ) && (platform.compareToIgnoreCase(TELESTAX_PLATFORM) == 0 ) || ( platform != null ) && (platform.compareToIgnoreCase(ORACLE_PLATFORM) == 0 )) {
			if( sName.equalsIgnoreCase("DlgcSipServlet") )
			{
				dlgcSipServletLoaded = true;
				log.debug(" JMCConferenceServlet::servletInitialized DlgcSipServlet loaded");			
			}

			else if( sName.equalsIgnoreCase("JMCConferenceServlet") ) 
				jmcConferenceServletInitCalled =true;

			if( jmcConferenceServletInitCalled && dlgcSipServletLoaded)
			{
				if ( servletInitializedFlag == false ) {
					log.debug("Entering JMCConferenceServlet::servletInitialized servletName: " + sName);			
					servletInitializedFlag = true;
					synchronized (isReady)
					{
						try {
							try
							{
								Driver dlgcDriver = (Driver) DriverManager.getDriver(dlgcDriverName);
								theMediaSessionFactory = dlgcDriver.getFactory(null);
								demoPropertyObj = new DlgcDemoProperty(this.getClass());
								String useConfirmString = demoPropertyObj.getProperty("DlgcMultiConferenceDemo.media.mixer.useConfirm");
								if ( useConfirmString != null ) {
									if (useConfirmString.equalsIgnoreCase("true") ) {
										this.useConfirm=true;
									}
								}else {
									this.useConfirm =false;
								}

							}
							catch (Exception e)
							{
								log.debug("JMCConferenceServlet::Exception: " + e);
							}

							sipSessionsUtil = (SipSessionsUtil) getServletContext().getAttribute(SIP_SESSIONS_UTIL);

						
							if ( confMgr == null )
								confMgr = new DlgcConferenceStorageMgr(this);

						

						} catch (Exception e) {
							log.error("Error in servletInitialized",e.toString());
							e.printStackTrace();
						}
						isReady = Boolean.TRUE;
					}

				} else {
					log.debug("JMCConferenceServlet::servletInitialized(): already servletInitialized was called...debouncing " + sName);
				}
			}

		} else { //TROPO Framework

			if( sName.equalsIgnoreCase("DlgcSipServlet") )
			{
				dlgcSipServletLoaded = true;
				log.debug(" JMCConferenceServlet::servletInitialized DlgcSipServlet loaded");			
				return;
			}

			if( (sName.equalsIgnoreCase("JMCConferenceServlet") ) && dlgcSipServletLoaded)
			{
				if ( servletInitializedFlag == false ) {
					log.debug("Entering JMCConferenceServlet::servletInitialized servletName: " + sName);			
					servletInitializedFlag = true;
					synchronized (isReady)
					{
						try {
							try
							{
								Driver dlgcDriver = (Driver) DriverManager.getDriver(dlgcDriverName);
								theMediaSessionFactory = dlgcDriver.getFactory(null);
								demoPropertyObj = new DlgcDemoProperty(this.getClass());

							}
							catch (Exception e)
							{
								log.debug("JMCConferenceServlet::Exception: " + e);
							}

							sipSessionsUtil = (SipSessionsUtil) getServletContext().getAttribute(SIP_SESSIONS_UTIL);

							
							if ( confMgr == null )
								confMgr = new DlgcConferenceStorageMgr(this);

							

						} catch (Exception e) {
							log.error("Error in servletInitialized",e.toString());
							e.printStackTrace();
						}
						isReady = Boolean.TRUE;
					}

				} else {
					log.debug("JMCConferenceServlet::servletInitialized(): already servletInitialized was called...debouncing " + sName);
				}
			}
		}
	}

	public static  enum MenuOperation {
		NOTHING, UNMUTE, MUTE, PARK, UNPARK;
	};

	public static void setMenuOperation( SipSession mySipSession, MenuOperation mo) {
		mySipSession.setAttribute(JMCConferenceServlet.MENUOPERATION, mo);
	}

	public static MenuOperation getMenuOperation( SipSession mySipSession) {
		return ( (MenuOperation) mySipSession.getAttribute(JMCConferenceServlet.MENUOPERATION) );
	}

	
	public void processDetectorEvent(JMCConferenceServlet jcs, SipSession mySipSession, SignalDetectorEvent anEvent) throws MsControlException 
	{
		if ( (anEvent.getEventType() == SignalDetectorEvent.SIGNAL_DETECTED) && (anEvent.getQualifier() == SignalDetectorEvent.RTC_TRIGGERED) ) 
		{

			log.debug("DTMF WAS ENABLED IN THE MEDIA SERVER");
			return;
		}

		log.debug("Entering State.CollectingParticipantOptions");
		log.debug("["+mySipSession.getCallId()+"] SIGNALDETECTOR EVENT -> "+anEvent.getEventType());

		String dtmf = anEvent.getSignalString();
		log.debug("["+mySipSession.getCallId()+"] SIGNALDETECTOR Digit Received -> "+dtmf);

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
			log.debug("["+mySipSession.getCallId()+"] SIGNALDETECTOR Buffered Digit Received -> "+buffereddtmf);

			/*
			 * Check if the caller entered in any valid options and stop digit detection
			 * if its valid
			 */
			for (int i = 0; i < participantOptions.length; i++) {
				if (buffereddtmf.equals(participantOptions[i])) 
				{
					log.debug("["+mySipSession.getCallId()+"]SIGNALDETECTOR We got a match :  Buffered Digit Received -> "+buffereddtmf);
					log.debug("["+mySipSession.getCallId()+"]SIGNALDETECTOR calling dispatchSignalDetectorEvent");

					jcs.dispatchSignalDetectorEvent(jcs, mySipSession, buffereddtmf, anEvent); 
					break;
				}
			}

			// Make sure the buffer doesn't go past 3 digits 
			if (buffereddtmf.length() > 3) {buffereddtmf = "";}
		}
		mySipSession.setAttribute("BUFFERED_SIGNALS",buffereddtmf);
		log.debug("START PARSING COLLECTED DIGITS DONE");
		log.debug("Leaving State.CollectingParticipantOptions");
	}


	public void dispatchSignalDetectorEvent(JMCConferenceServlet jcs, SipSession mySipSession, String buffereddtmf, SignalDetectorEvent anEvent) 
	throws MsControlException 
	{
		log.debug("Entering State.CollectingParticipantOptions dispatchSignalDetectorEvent ");
		log.debug("["+mySipSession.getCallId()+"] SIGNALDETECTOR EVENT -> "+anEvent.getEventType());;

		// Empty the digit buffer
		mySipSession.setAttribute("BUFFERED_SIGNALS","");

		// Check if buffered digits match MUTE option
		if (buffereddtmf.equals(PARTICIPANT_OPTION_STOP_PLAY)) {
			State myState = (State)mySipSession.getAttribute("STATE");
			myState.stopPlaying(this, mySipSession);
		}else if (buffereddtmf.equals(PARTICIPANT_OPTION_MUTE_TOGGLE)) {
			log.debug("Receive PARTICIPANT_OPTION_MUTE_TOGGLE: " + PARTICIPANT_OPTION_MUTE_TOGGLE );
			State myState = (State)mySipSession.getAttribute("STATE");
			myState.executeMute(jcs,  mySipSession ); 
		} else if (buffereddtmf.equals(this.PARTICIPANT_OPTION_UNMUTE_TOGGLE)) {
			log.debug("Receive PARTICIPANT_OPTION_MUTE_TOGGLE: " + PARTICIPANT_OPTION_UNMUTE_TOGGLE );
			State myState = (State)mySipSession.getAttribute("STATE");
			myState.executeUnmute(jcs, mySipSession); 
		} else if (buffereddtmf.equals(PARTICIPANT_OPTION_PLAY_NBLUES)) {
			// Check if caller is already muted or un-muted and toggle appropriately
			mySipSession.setAttribute("STATE",State.PlayingPrompt);
			playPromptParticipant (mySipSession, new URI[] {URI.create(XMS_MUSIC_NBLUES)});
		} else if (buffereddtmf.equals(PARTICIPANT_OPTION_UNJOIN_CONFERENCE)) {
			log.debug("Receive PARTICIPANT_OPTION_UNJOIN_CONFERENCE (*02)... calling exitConference");
			JMCConferenceServlet.setMenuOperation(mySipSession, MenuOperation.PARK);
			jcs.exitConference( mySipSession);
		}else if (buffereddtmf.equals(PARTICIPANT_OPTION_REJOIN_CONFERENCE_USING_MIXER )) {
			log.debug("Receive PARTICIPANT_OPTION_REJOIN_CONFERENCE_USING_MIXER (*03)... resetting confJoinUseMixerAdapter flag and calling enterConference");
			jcs.confJoinUseMixerAdapter = false;
			JMCConferenceServlet.setMenuOperation(mySipSession, MenuOperation.UNPARK);
			jcs.enterConference( mySipSession, true);
		}else if (buffereddtmf.equals(CONFERENCE_START_RECORDING )) {
			log.debug("Receive CONFERENCE_START_RECORDING (*1234)... Start Conference Recording");
			Date date = new Date();
			Long t = date.getTime();
			String recordingFileName = "confRecDemo-" + t.toString();
			jcs.startRecording( mySipSession, recordingFileName);
		}else if (buffereddtmf.equals(CONFERENCE_STOP_RECORDING)) {

			log.debug("Receive CONFERENCE_STOP_RECORDING (*4321)... Stop Conference Recording");
			jcs.stopRecording( mySipSession);
		}
		
		log.debug("Leaving State.CollectingParticipantOptions DispatchSignalDetectorEvent ");
	} //end of dispatchSignalDetectorEvent

	private void startRecording(SipSession mySipSession,String recordingFileName) {
		log.debug("Entering startRecording - Recording filename: " + recordingFileName);
		ConferenceSession conf = this.getConferenceSession(mySipSession);
		try {
			conf.record(mySipSession, recordingFileName);
		} catch (MsControlException e) {
			log.error(e.toString());
		}
		log.debug("Exiting startRecording - Recording filename: " + recordingFileName);

	}
	
	private void stopRecording(SipSession mySipSession) {
		log.debug("Entering stopRecording");
		ConferenceSession conf = this.getConferenceSession(mySipSession);
		try {
			conf.stopRecord(mySipSession);
		} catch (MsControlException e) {
			log.error(e.toString());
		}
		log.debug("Leaving stopRecording");

	}

	public void startCollecting(SipSession mySipSession)
	{
		log.debug("Entering State.startCollecting");
		MediaGroup myMediaGroup = (MediaGroup) mySipSession.getAttribute("MEDIA_GROUP");
		mySipSession.setAttribute("STATE", State.CollectingParticipantOptions);

		Parameters detectorOptions = JMCConferenceServlet.theMediaSessionFactory.createParameters();

		detectorOptions.put(SignalDetector.BUFFERING, Boolean.FALSE );
		EventType[] arrayEnabledEvents = {SignalDetectorEvent.SIGNAL_DETECTED} ;
		detectorOptions.put(SignalDetector.ENABLED_EVENTS, arrayEnabledEvents);	

		log.debug("["+mySipSession.getCallId()+"] CALLING RECEIVESIGNALS (-1)");
		try {
			myMediaGroup.getSignalDetector().receiveSignals(-1, null,
					null, detectorOptions);
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug("["+mySipSession.getCallId()+"] CALLING RECEIVESIGNALS (-1) [DONE]"); 
		log.debug("Leaving State.startCollecting");
	}


	@Override
	public void init() throws ServletException {

		synchronized (isReady)
		{
			try {
				super.init();
				platform = this.getWebServerPlatform();

			} catch (Exception e) {
				throw new ServletException(
						"Cannot initialize ConferenceServlet due to internale service error",
						e);
			}
			isReady = Boolean.TRUE;
		}
	}

	protected String getWebServerPlatform()
	{

		String platform = System.getenv("APPSERVER_PLATFORM");
		if (platform == null)
		{
			log.warn((new StringBuilder()).append("Environment Variable: ").append("APPSERVER_PLATFORM").append(" not provided").toString());
			log.warn("Assuming OCCAS WEB Application Server Platform");
			platform = ORACLE_PLATFORM;
		} 
		log.debug("APPSERVER_PLATFORM set to: " + platform);
		return platform;
	}


	/**
	 * Process incoming INVITE request
	 */
	@Override
	protected void doInvite(SipServletRequest req) throws ServletException,
	IOException {
		synchronized (isReady)
		{
			// Verify the servlet has finished initialization
			if (isReady == false)
			{
				req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
				return;
			}
		}			
		processNewConnection(req);
	}

	/**
	 * Process response 
	 */
	@Override
	protected void doResponse(SipServletResponse response)
	throws ServletException, IOException
	{
		log.warn("["+response.getSession().getCallId()+"] RECEIVED RESPONSE: "+response.getStatus()+" responding to "+response.getMethod());
		if (response.getMethod().equals("INVITE"))
		{
			if (response.getStatus() == SipServletResponse.SC_OK)
			{
				try
				{
					NetworkConnection conn = (NetworkConnection) response.getRequest().getSession().getAttribute("NETWORK_CONNECTION");
					byte[] remoteSdp = response.getRawContent();

					if (remoteSdp != null)
					{
						response.getSession().setAttribute("RESPONSE", response);
						log.warn("remoteSdp = " + remoteSdp.toString() + " calling SdpPortManager processSdpAnswer..." );
						conn.getSdpPortManager().processSdpAnswer(remoteSdp);
					}
				}
				catch (MsControlException e)
				{
					e.printStackTrace();
				}
			}
		}
		else if (response.getMethod().equals("BYE"))
		{
			if (response.getStatus() == SipServletResponse.SC_OK)
			{
				releaseSession(response.getRequest().getSession());
			}
		}		
	}

	/**
	 * Process incoming CANCEL request
	 */
	@Override
	public void doCancel(final SipServletRequest arg0)
	throws ServletException,IOException
	{
		log.debug("["+arg0.getCallId()+"] RECEIVED CANCEL REQUEST");


		// Send 200 OK in response to CANCEL request
		arg0.createResponse(SipServletResponse.SC_OK).send();
		if (arg0.getSession().isValid()) {
			arg0.getSession().invalidate();
		}
		if (arg0.getApplicationSession().isValid()) {
			arg0.getApplicationSession().invalidate();
		}
		log.debug("["+arg0.getCallId()+"] RECEIVED CANCEL REQUEST [DONE]");
	}

	/**
	 * Process incoming BYE request
	 */
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
	IOException {
		request.createResponse(SipServletResponse.SC_OK).send();
		releaseSession(request.getSession());
	}

	/**
	 * Process incoming 200 OK response [
	 */
	@Override
	protected void doSuccessResponse(SipServletResponse resp)
	throws ServletException, IOException {
	}

	/**
	 * Add a participant to the give conference room, create the
	 * ConferenceSession if not existing
	 * 
	 * @param confId
	 * @param aParticipant
	 * @return ConferenceSession
	 * @throws MscontrolException
	 */

	synchronized public ConferenceSession  addParticipant(String confId,	SipSession sipSession) throws MsControlException {
		DlgcConferenceInfo confInfo = null;
		ConferenceSession theConference=null;
		try {
			confInfo = confMgr.getConferenceInfo(confId);
		} catch( DlgcConferenceStorageMgrException e1 ) {

			if ( e1.getType().compareTo(DlgcConferenceStorageMgrExceptionTypes.NULL_APP_SESSION_FOUND ) == 0 ) {
				log.error("Asserting Major Error Found in addParticipant(): " + e1.getMessage() );
				e1.printStackTrace();
				assert(true);
			}
		}

		if (confInfo == null) {
			theConference = this.createNewConference(confId, sipSession);	
		} else {
			theConference = this.addParticipantToExistingConference(confId, sipSession, confInfo );
		}
		sipSession.setAttribute("CONFERENCE_ID", confId);
		jcs.setMenuOperation(sipSession, MenuOperation.UNMUTE);

		log.debug("["+sipSession.getCallId()+"] CREATING/JOINING DONE");
		
				
		return theConference;
	}

	private ConferenceSession createNewConference(String confId, SipSession sipSession ) throws MsControlException
	{
		ConferenceSession myConferenceSession = null;
		MediaSession myConferenceMediaSession = null;

		log.debug("["+sipSession.getCallId()+"] CREATING NEW CONFERENCE: "+confId);
		sipSession.setAttribute("OWNER", true);
		myConferenceSession = new ConferenceSession(this, confId, sipSession);
		try {
			confMgr.saveConference(  myConferenceSession.getMediaMixer().getMediaSession().getURI(), confId, myConferenceSession.getMediaMixer());
			sipSession.setAttribute("CONFERENCE_MEDIA_SESSION", myConferenceSession.getMediaSession());
			log.debug("---------------STORING NEW CONFERENCE; "+myConferenceSession.getMediaMixer().getMediaSession().toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			myConferenceMediaSession = (MediaSession)theMediaSessionFactory.getMediaObject(myConferenceSession.getMediaMixer().getMediaSession().getURI());
		} catch (Exception e1) {
			log.debug("---------------STORING NEW CONFERENCE: ITS ALREADY GONE!!!!!!!!");
		}
		log.debug("---------------New Conference Created - "+ "pin = " + confId + " " + myConferenceMediaSession);
		return myConferenceSession;
	}

	synchronized private ConferenceSession addParticipantToExistingConference(String confId, SipSession sipSession, DlgcConferenceInfo confInfo  )
	{
		log.debug("Adding new participant to conference with ID = " + confId );
		MediaSession myConferenceMediaSession = getMediaSessionFromURI(confInfo.getURI());
		ConferenceSession myConferenceSession = (ConferenceSession)myConferenceMediaSession.getAttribute("CONFERENCE_SESSION");
		// Add the participant to existing conference
		//myConferenceSession = (ConferenceSession)myConferenceMediaSession.getAttribute("CONFERENCE_SESSION");
		sipSession.setAttribute("OWNER", false);
		sipSession.setAttribute("CONFERENCE_MEDIA_SESSION", myConferenceSession.getMediaSession());
		myConferenceSession.addParticipant(sipSession);
		log.debug("---------------New Participant was added to conference with - "+ "pin = " + confId + " " + myConferenceMediaSession);
		
		return myConferenceSession;
	}

	/**
	 * Send a SIP BYE to the User Agent, remove Participant on Servlet side
	 * 
	 * @param theSipSession
	 *            participant SipSession
	 */
	public void sendBye(SipSession mySipSession) 
	{
		// Need to check the state of the SipSession in order to make sure user
		// did not already terminate session from their end.
		if ( mySipSession == null){
			log.debug("sendBye: SipSession Timeout encountered ignoring it since SipSession is Null");
			return;
		}
		
		Boolean releasing = (Boolean)mySipSession.getAttribute("RELEASING");
		if (mySipSession != null && mySipSession.isValid() && mySipSession.getState() != SipSession.State.TERMINATED && !releasing)
		{
			mySipSession.setAttribute("RELEASING", true);
			log.debug("["+mySipSession.getCallId()+"] SENDING BYE");
			try {
				SipServletRequest req = mySipSession.createRequest("BYE");
				req.send();
			}
			catch (IOException e) {
				log.error("Unable to send BYE to user agent: "+e);
			}
		}
	}

	public void removeConference(String confId, URI uri) {
		try {
			//database.removeConferenceSession(confId, uri);
			this.confMgr.removeConference(confId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processNewConnection(SipServletRequest req) throws ServletException, IOException {

		SipSession mySipSession = req.getSession();

		if (req.isInitial()) {
			try {
				MediaSession myMediaSession = theMediaSessionFactory.createMediaSession();

				// Forced to store SIP session ID into application session because it doesn't carry over into
				// SipApplicationListener when retrieving SIP session by ID
				req.getApplicationSession().setAttribute("SIP_SESSION", req.getSession());

				// Store details regarding caller's session
				myMediaSession.setAttribute("SIP_SESSION", mySipSession);
				mySipSession.setAttribute("MEDIA_SESSION", myMediaSession);  
				//mySipSession.setAttribute("MUTE", false);
				JMCConferenceServlet.setMenuOperation(mySipSession, MenuOperation.UNMUTE);

				mySipSession.setAttribute("STATE", State.Initial);
				mySipSession.setAttribute("RELEASING",false);
				mySipSession.setAttribute("ON_HOLD",false);

				// Create network connection for caller
				NetworkConnection myNetworkConnection = myMediaSession.createNetworkConnection(NetworkConnection.BASIC);
				mySipSession.setAttribute("NETWORK_CONNECTION", myNetworkConnection);

				// Create IVR media group and event listeners
				MediaGroup myMediaGroup = myMediaSession.createMediaGroup(MediaGroup.PLAYER_RECORDER_SIGNALDETECTOR);
				myMediaGroup.getPlayer().addListener(new MediaListener<PlayerEvent>());
				myMediaGroup.getRecorder().addListener(new MediaListener<RecorderEvent>());
				myMediaGroup.getSignalDetector().addListener(new MediaListener<SignalDetectorEvent>());
				myNetworkConnection.getSdpPortManager().addListener(new MediaListener<SdpPortManagerEvent>());
				myNetworkConnection.addListener(new AllocationListener());

				myMediaGroup.addListener(new AllocationListener());
				mySipSession.setAttribute("MEDIA_GROUP", myMediaGroup);


				// Join the network connection to the mediagroup and create the event listener
				myNetworkConnection.join(Joinable.Direction.DUPLEX, myMediaGroup);
				doModify(req,myNetworkConnection);

			} catch (Exception e) {
				log.error("Cannot create MediaSession or MediaSessionFactory :", e);
				throw new ServletException(e);
			}
		}
		else {
			NetworkConnection myNetworkConnection = (NetworkConnection) mySipSession.getAttribute("NETWORK_CONNECTION");
			doModify(req,myNetworkConnection);
		}


	}

	/**
	 * Modify SIP connection
	 */
	private void doModify(SipServletRequest req, NetworkConnection conn)
	throws ServletException, IOException
	{
		// Request the media server to start SDP negotiation
		try
		{
			req.getSession().setAttribute("UNANSWERED_INVITE", req); 

			byte[] remoteSdp = req.getRawContent();

			if (remoteSdp == null)
			{
				conn.getSdpPortManager().generateSdpOffer();
			}
			else
			{
				conn.getSdpPortManager().processSdpOffer(remoteSdp);
			}
		} 
		catch (MsControlException e)
		{
			req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
		}

	}

	/* 
	 * --------------------------------- STATE TABLE -------------------------------------
	 */
	/**
	 * Initiate the caller's state table
	 * call after the invite is sent to the media server
	 */
	public void start(SipSession mySipSession) {
		mySipSession.setAttribute("STATE", State.Initial);
		try {
			State state = (State)mySipSession.getAttribute("STATE");
			state.onInitialization(this, mySipSession);
		} catch (MsControlException e) {
			e.printStackTrace();
			terminate(mySipSession, e);
		}
	}	

	/**
	 * States table for caller after INVITE is received
	 */
	public enum State {
		// Initial state, start listening for digits for conference ID

		Initial {
			//request the conference ID
			public void onInitialization(JMCConferenceServlet jcs, SipSession mySipSession)
			throws MsControlException {

				MediaGroup myMediaGroup = (MediaGroup) mySipSession.getAttribute("MEDIA_GROUP");
				log.debug("Entering State.Initial");

				JMCConferenceServlet.setMenuOperation(mySipSession, MenuOperation.NOTHING);

				Parameters detectorOptions;
				detectorOptions = theMediaSessionFactory.createParameters();
				detectorOptions.put(SignalDetector.PROMPT, URI.create(XMS_PROMPT_ENTER_CONFERENCE_ID));

				detectorOptions.put(SignalDetector.INITIAL_TIMEOUT, 30000);
				detectorOptions.put(SignalDetector.INTER_SIG_TIMEOUT, 30000);
				detectorOptions.put(SignalDetector.MAX_DURATION, -1);
				RTC rtcStop = new RTC(SignalDetector.DETECTION_OF_ONE_SIGNAL, Player.STOP);
				
				//Request conference ping number using Prompt and Collect
				log.debug("["+mySipSession.getCallId()+"] CALLING RECEIVESIGNALS (4)");
				myMediaGroup.getSignalDetector().receiveSignals(4, null,
						new RTC[]{rtcStop}, detectorOptions);
				log.debug("["+mySipSession.getCallId()+"] CALLING RECEIVESIGNALS (4) [DONE]");

				//set state to collecting conference ID and wait for user to enter the conference
				//number via prompt and collect Media Request
				mySipSession.setAttribute("STATE", State.CollectingConferenceID);
				log.debug("Leaving State.Initial");

			}
		},
		CollectingConferenceID {
			public void onSignalDetectorEvent(JMCConferenceServlet jcs, SipSession mySipSession, 
					SignalDetectorEvent anEvent) throws MsControlException {

				log.debug("Entering State.CollectingConferenceID");
				log.debug("["+mySipSession.getCallId()+"] COLLECTED CONFERENCE ID: "+anEvent.getSignalString());
				//Note calling addParticipant() in addition to handle the new call leg, it checks 
				//if the DTMF entered matches an existing conference, if not, it creates a new conference
				//an associates this call leg to this new conference; otherwise, it associate this leg
				//to an existing conference.
				ConferenceSession conference = jcs.addParticipant(anEvent.getSignalString(), mySipSession);
				if (conference != null ) {
					//FYI mixer confirm() API is not supported today by the Connector
					//so basically we just going to jump to the next state ... Please read
					if ( jcs.useConfirm == false ) {
						WaitingConferenceConfirm.onConferenceConfirm(jcs, mySipSession);
					}else if (conference.getConfirmation() == false) {
							DlgcConferenceMonitor monitor = new DlgcConferenceMonitor("conference.confirm()");
							if ( conference.confirm(monitor) == true ) {
								log.debug("Conference Confirm request sent to Media Server via Connector...must wait for Allocation Event before continuing");
								if ( monitor.waitForRequestCompletion() ) {
									mySipSession.setAttribute("STATE", State.WaitingConferenceConfirm);
									log.debug("Conference Confirm received ");
									WaitingConferenceConfirm.onConferenceConfirm(jcs, mySipSession);
								}else {
									log.error("Conference Confirm request failed");
									jcs.sendBye(mySipSession);
									jcs.releaseSession(mySipSession);
								}
							}
					} else {
						mySipSession.setAttribute("STATE", State.WaitingConferenceConfirm);
						WaitingConferenceConfirm.onConferenceConfirm(jcs, mySipSession);
					}
				}else {
					log.error("Error Creating Conference");
					jcs.sendBye(mySipSession);
					jcs.releaseSession(mySipSession);
				}
				log.debug("["+mySipSession.getCallId()+"] COLLECTED CONFERENCE ID: "+anEvent.getSignalString()+" [DONE]");
				log.debug("Leaving State.CollectingConferenceID");
			}
			// Listen for initial play to finish
			public void onPlayerEvent(JMCConferenceServlet jcs, SipSession mySipSession, 
					PlayerEvent anEvent) throws MsControlException {
			}
			public void onConferenceConfirm(JMCConferenceServlet jcs, SipSession mySipSession) {
				log.debug("-------------------------> I GOT A CONFIRM!!!!!!");
			}
		},
		// Wait for AllocationConfirm event in ConferenceSession's Mixer listener for conference creation
		WaitingConferenceConfirm {
			public void onConferenceConfirm(JMCConferenceServlet jcs, SipSession mySipSession) 
			throws MsControlException{
				log.debug("Entering State.WaitingConferenceConfirm");
				log.debug("["+mySipSession.getCallId()+"] GOT CONFERENCE CONFIRMATION");

				mySipSession.setAttribute("STATE", Greeting);
				jcs.playWelcomeConferenceGreeting(mySipSession);
				
				log.debug("["+mySipSession.getCallId()+"] GOT CONFERENCE CONFIRMATION [DONE]");
				log.debug("Leaving State.WaitingConferenceConfirm");
			}
		},
		
		// Play "Welcome to the conference" prompt to the caller before they join the conference
		Greeting {
			public void onPlayWelcomeConferenceGreeting (JMCConferenceServlet jcs, SipSession mySipSession) throws MsControlException {

				log.debug("Entering State.onPlayWelcomeConferenceGreeting");
				log.debug("["+mySipSession.getCallId()+"] PLAYING: WELCOME TO CONFERENCE GREETING");
				MediaGroup myMediaGroup = (MediaGroup) mySipSession.getAttribute("MEDIA_GROUP");
				//ConferenceSession myConferenceSession = jcs.getConferenceSession(mySipSession);
				//int participantId = Integer.parseInt(mySipSession.getAttribute("PARTICIPANT_ID").toString());
				URI prompts[] = null;
				
				prompts = new URI[1];
				prompts[0] = URI.create(XMS_PROMPT_WELCOME_TO_CONFERENCE);

				myMediaGroup.getPlayer().play(
						prompts,
						null, Parameters.NO_PARAMETER);

				mySipSession.setAttribute("STATE", JoinConference); 

				log.debug("["+mySipSession.getCallId()+"] PLAYING: WELCOME TO CONFERENCE GREETING [DONE]");
				log.debug("Leaving State.onPlayWelcomeConferenceGreeting");
			}
		},



		// Join the conference after welcome prompt has been played
		JoinConference {
			public void onPlayerEvent(JMCConferenceServlet jcs, SipSession mySipSession,PlayerEvent anEvent) throws MsControlException {
				log.debug("Entering State.JoinConference");
				log.debug("["+mySipSession.getCallId()+"] CALLING NC -> MIXER JOIN");
				Boolean conferenceOwner = (Boolean)mySipSession.getAttribute("OWNER");
				ConferenceSession myConferenceSession = jcs.getConferenceSession(mySipSession);

				jcs.enterConference(mySipSession,false);
				MediaGroup myMediaGroup = (MediaGroup) mySipSession.getAttribute("MEDIA_GROUP");
				mySipSession.setAttribute("STATE", CollectingParticipantOptions);

				Parameters detectorOptions = JMCConferenceServlet.theMediaSessionFactory.createParameters();

				detectorOptions.put(SignalDetector.BUFFERING, Boolean.FALSE );
				EventType[] arrayEnabledEvents = {SignalDetectorEvent.SIGNAL_DETECTED} ;
				detectorOptions.put(SignalDetector.ENABLED_EVENTS, arrayEnabledEvents);	

				log.debug("["+mySipSession.getCallId()+"] CALLING RECEIVESIGNALS (-1)");
				myMediaGroup.getSignalDetector().receiveSignals(-1, null,
						null, detectorOptions);
				
				mySipSession.setAttribute("STATE", CollectingParticipantOptions);
				
				log.debug("["+mySipSession.getCallId()+"] CALLING RECEIVESIGNALS (-1) [DONE]"); 
				
				log.debug("["+mySipSession.getCallId()+"] CALLING NC-> MIXER JOIN [DONE]");
				log.debug("Leaving State.JoinConference");
			}
		},




		// Collect individual digits or indication to stop listening for digits
		//At this point we are in conference
		CollectingParticipantOptions {


			public void playYouAreMuted(JMCConferenceServlet jcs, SipSession mySipSession){
				log.debug("["+mySipSession.getCallId()+"] PLAYING: MUTE PROMPT");
				mySipSession.setAttribute("STATE", State.PlayingPrompt);
				jcs.playPromptParticipant(mySipSession, new URI[] {URI.create(XMS_PROMPT_CONFERENCE_MUTE)} );
			}

			public void playYouAreUnmuted(JMCConferenceServlet jcs, SipSession mySipSession){
				log.debug("["+mySipSession.getCallId()+"] PLAYING: UNMUTE PROMPT");
				mySipSession.setAttribute("STATE",State.PlayingPrompt);
				jcs.playPromptParticipant (mySipSession, new URI[] { URI.create(XMS_PROMPT_CONFERENCE_UNMUTE) });
				log.debug("["+mySipSession.getCallId()+"] PLAYING: UNMUTE PROMPT [DONE]");
			}

			public void playYouAreParked(JMCConferenceServlet jcs, SipSession mySipSession){
				log.debug("["+mySipSession.getCallId()+"] PLAYING: PARK PROMPT");
				mySipSession.setAttribute("STATE", State.PlayingPrompt);
				jcs.playPromptParticipant(mySipSession, new URI[] {URI.create(XMS_PROMPT_PARKED_FROM_CONFERENCE)} );
			}

			public void playYouAreUnParked(JMCConferenceServlet jcs, SipSession mySipSession){
				log.debug("["+mySipSession.getCallId()+"] PLAYING: UNPARKED PROMPT");
				mySipSession.setAttribute("STATE",State.PlayingPrompt);
				jcs.playPromptParticipant (mySipSession, new URI[] { URI.create(XMS_PROMPT_UNPARKED_FROM_CONFERENCE) });
				log.debug("["+mySipSession.getCallId()+"] PLAYING: UNPARKED PROMPT [DONE]");
			}

			//may be called do to a rejoin 
			public void onLegJoinToConference( JMCConferenceServlet jcs, SipSession mySipSession,	JoinEvent anEvent) {
				log.debug("ConferenceApplication JMCConferenceServlet:: CollectingParticipantOptions::onLegJoinToConference leg rejoined to conference");
				//playYouAreUnParked(jcs, mySipSession);
			}

			public void onSignalDetectorEvent(JMCConferenceServlet jcs, SipSession mySipSession, SignalDetectorEvent anEvent) throws MsControlException 
			{
				jcs.processDetectorEvent(jcs,  mySipSession, anEvent);

			}

			public void executeUnmute( JMCConferenceServlet jcs, SipSession mySipSession ){
				jcs.unmute(mySipSession);
			}

			public void executeMute( JMCConferenceServlet jcs, SipSession mySipSession ){
				jcs.mute(mySipSession);
			}
		},
		// Wait for play to participant to finish and then go back to collecting digits JOHN CRUZ
		PlayingPrompt {
			public void onPlayerEvent(JMCConferenceServlet jcs, SipSession mySipSession, 
					PlayerEvent anEvent) throws MsControlException {
				log.debug("Entering State.PlayingPrompt");
				log.debug("PLAY HAS FINISHED");
				MenuOperation  mo  = JMCConferenceServlet.getMenuOperation(mySipSession);
				if ( mo.compareTo(MenuOperation.UNMUTE) == 0) {
					jcs.setMenuOperation(mySipSession, MenuOperation.MUTE);
					jcs.mute(mySipSession);
				} else if ( mo.compareTo(MenuOperation.MUTE) == 0 ) {
					jcs.setMenuOperation(mySipSession, MenuOperation.UNMUTE);
					jcs.unmute(mySipSession);
				}
			}

			public void stopPlaying( JMCConferenceServlet jcs, SipSession mySipSession ) {
				log.debug("StopPlaying");		
				MediaGroup customerMediaGroup = (MediaGroup) mySipSession.getAttribute("MEDIA_GROUP");
				try {
					customerMediaGroup.getPlayer().stop(true);
				} catch (MsControlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			public void onSignalDetectorEvent(JMCConferenceServlet jcs, SipSession mySipSession, SignalDetectorEvent anEvent) throws MsControlException 
			{
				jcs.processDetectorEvent(jcs,  mySipSession, anEvent);
			}



		};

		/**
		 * Log errors if unexpected event is received
		 * @throws IOException 
		 */
		public void onPlayerEvent(JMCConferenceServlet jcs, SipSession mySipSession, PlayerEvent anEvent)
		throws MsControlException {
			log.debug("Entering State.onPlayerEvent");
			log.error("Unexpected player event: " + anEvent
					+ " in state " + this + " - releasing");
			//anEvent.getSource().getMediaSession().release();
			jcs.sendBye(mySipSession);
			jcs.releaseSession(mySipSession);
			throw new MsControlException(this.toString());
		}
		public void executeMute(JMCConferenceServlet jcs, SipSession mySipSession) {
			// TODO Auto-generated method stub
			log.debug("Unexpected State while calling executeMute");

		}

		public void executeUnmute(JMCConferenceServlet jcs, SipSession mySipSession) {
			// TODO Auto-generated method stub
			log.debug("Unexpected State while calling executeUnmute");

		}

		public void onRecorderEvent(JMCConferenceServlet jcs, SipSession mySipSession, RecorderEvent anEvent)
		throws MsControlException {
			log.error("Unexpected recorder event: " + anEvent
					+ " in state " + this + " - releasing");
			//anEvent.getSource().getMediaSession().release();
			jcs.sendBye(mySipSession);
			jcs.releaseSession(mySipSession);
			throw new MsControlException(this.toString());
		}
		public void onSignalDetectorEvent(JMCConferenceServlet jcs, SipSession mySipSession, SignalDetectorEvent anEvent)
		throws MsControlException {
			log.error("Unexpected signal detector event Ignoring request: "
					+ anEvent + " in state " + this + " - releasing");
			//anEvent.getSource().getMediaSession().release();
			//jcs.sendBye(mySipSession);
			//jcs.releaseSession(mySipSession);
			//throw new MsControlException(this.toString());
		}
		public void onReInviteOK (JMCConferenceServlet jcs, SipSession mySipSession)
		throws MsControlException {
			log.error("Unexpected ReINVITE OK " + " in state " + this);
			jcs.sendBye(mySipSession);
			jcs.releaseSession(mySipSession);
			throw new MsControlException(this.toString());
		}
		public void onConferenceConfirm(JMCConferenceServlet jcs, SipSession mySipSession) 
		throws MsControlException {
			log.error("Unexpected Conference Confirm in state " + this);
			jcs.sendBye(mySipSession);
			jcs.releaseSession(mySipSession);
			throw new MsControlException(this.toString());
		}
		public void onInitialization(JMCConferenceServlet jcs, SipSession mySipSession) 
		throws MsControlException {
			log.error("Unexpected Initialization in state " + this);
			jcs.sendBye(mySipSession);
			jcs.releaseSession(mySipSession);
			throw new MsControlException(mySipSession.toString());
		}
		public void onPlayWelcomeConferenceGreeting(JMCConferenceServlet jcs, SipSession mySipSession) 
		throws MsControlException {
			log.error("Unexpected conference monitoring in state " + this);
			jcs.sendBye(mySipSession);
			jcs.releaseSession(mySipSession);
			throw new MsControlException(this.toString());
		}
		public void onStartCollectingParticipantOptions(JMCConferenceServlet jcs, SipSession mySipSession) 
		throws MsControlException {
			log.error("Unexpected conference monitoring in state " + this);
			jcs.sendBye(mySipSession);
			jcs.releaseSession(mySipSession);
			throw new MsControlException(this.toString());
		}

		public void onLegJoinToConference( JMCConferenceServlet jcs, SipSession mySipSession,	JoinEvent anEvent) throws MsControlException {
			log.error("Unexpected onLegJoinToConference monitoring in state " + this);
			jcs.sendBye(mySipSession);
			jcs.releaseSession(mySipSession);
			throw new MsControlException(this.toString());
		}
		public void playYouAreUnmuted(JMCConferenceServlet jcs,
				SipSession mySipSession) {
			log.error("Unexpected playYouAreUnmuted in state " + this);

		}
		public void playYouAreMuted(JMCConferenceServlet jcs,
				SipSession mySipSession) {
			log.error("Unexpected playYouAreMuted in state " + this);
			// TODO Auto-generated method stub

		}

		public void playYouAreParked(JMCConferenceServlet jcs,
				SipSession mySipSession) {
			log.error("Unexpected playYouAreParked in state " + this);
			// TODO Auto-generated method stub

		}

		public void playYouAreUnParked(JMCConferenceServlet jcs,
				SipSession mySipSession) {
			log.error("Unexpected playYouAreParked in state " + this);
			// TODO Auto-generated method stub

		}

		public void stopPlaying( JMCConferenceServlet jcs, SipSession mySipSession ) {
			log.error("Unexpected stopPlaying in state " + this);
			// TODO Auto-generated method stub
		}


	}	
	/**
	 * Set state to start playing the welcome greeting
	 */
	public void playWelcomeConferenceGreeting(SipSession mySipSession) {
		try {
			State myState = (State)mySipSession.getAttribute("STATE");
			myState.onPlayWelcomeConferenceGreeting(this, mySipSession);
		} catch (MsControlException e) {
			e.printStackTrace();
			terminate(mySipSession, e);
		}
	}

	/**
	 * Set state to start signal detection for caller
	 */
	public void startCollectingParticipantOptions(SipSession mySipSession) {
		try {
			State myState = (State)mySipSession.getAttribute("STATE");
			myState.onStartCollectingParticipantOptions(this, mySipSession);
		} catch (MsControlException e) {
			e.printStackTrace();
			terminate(mySipSession, e);
		}
	}

	/**
	 * Set state indicating conference was confirmed
	 */
	public void confirmConference(SipSession mySipSession) {
		try {
			State myState = (State)mySipSession.getAttribute("STATE");
			myState.onConferenceConfirm(jcs, mySipSession);
		} catch (MsControlException e) {
			e.printStackTrace();
			terminate(mySipSession, e);
		}
	}

	/**
	 * Join the conference
	 */

	public void enterConference(SipSession mySipSession, boolean addConfSession) {
		try {
			NetworkConnection myNetworkConnection = (NetworkConnection) mySipSession.getAttribute("NETWORK_CONNECTION");
			ConferenceSession myConferenceSession = getConferenceSession(mySipSession);
			MediaGroup myMediaGroup = (MediaGroup) mySipSession.getAttribute("MEDIA_GROUP");
			//int participantId = Integer.parseInt(mySipSession.getAttribute("PARTICIPANT_ID").toString());

			//FR6008 Start
			confJoinUseMixerAdapter = true; //for testing only

			if ( confJoinUseMixerAdapter ) {
				MixerAdapter mxa = null;
				mxa = (MixerAdapter)mySipSession.getAttribute("MIXER_ADAPTER");
				log.debug("Conference leg Joining to  MixerAdapter");

				MediaMixer mx = myConferenceSession.getMediaMixer();


				Configuration<MixerAdapter> mxaCfg = MixerAdapter.DTMF_CLAMP;
				//Configuration<MixerAdapter> mxaCfg = MixerAdapter.EMPTY;
				//Configuration<MixerAdapter> mxaCfg = MixerAdapter.DTMFCLAMP_VOLUME;
				mxa = mx.createMixerAdapter(mxaCfg);
				log.debug("["+mySipSession.getCallId()+"] JOINING -> "+ mxa);

				Joinable.Direction jsr309Dir = Joinable.Direction.DUPLEX; 
				ConferenceSession cs = this.getConferenceSession(mySipSession);
				//Properties props = theMediaSessionFactory.getProperties();
				String dirLeg = "duplex";				
				if (cs == null)
					dirLeg = demoPropertyObj.getProperty("demos.join.direction.leg1");
				else if ( this.getConferenceSession(mySipSession).getNumParticipant() == 1 )
					dirLeg = demoPropertyObj.getProperty("demos.join.direction.leg2");
				else if ( this.getConferenceSession(mySipSession).getNumParticipant()  == 2 )
					dirLeg = demoPropertyObj.getProperty("demos.join.direction.leg3");

				if ( dirLeg != null ) {

					if ( dirLeg.compareToIgnoreCase("recv") == 0 ) 
						jsr309Dir = Joinable.Direction.RECV;
					else if ( dirLeg.compareToIgnoreCase("send") == 0 ) 
						jsr309Dir = Joinable.Direction.SEND;
				} 

				myNetworkConnection.join(jsr309Dir, mxa);
				mySipSession.setAttribute("MIXER_ADAPTER", mxa);

			} else	{ //FR6008 Ends
				log.debug("Conference leg directly joining Mixer");
				log.debug("["+mySipSession.getCallId()+"] JOINING -> "+myConferenceSession.getMediaMixer());
				myNetworkConnection.join(Joinable.Direction.DUPLEX, myConferenceSession.getMediaMixer());
				mySipSession.removeAttribute("MIXER_ADAPTER");
			}
			if (addConfSession == true && myConferenceSession != null) {
				myConferenceSession.addParticipant(mySipSession);
				// Remove the conference if there are no longer any participants
				log.debug("LEFT IN CONFERENCE: "+myConferenceSession.getNumParticipant());
			}

			//not needed myNetworkConnection.getSdpPortManager().generateSdpOffer();
		}
		catch (MsControlException e)
		{
			terminate(mySipSession,e);
		}
		catch (Exception e) {
			terminate(mySipSession,e);
		}
	}

	/**
	 * Delete Participant because of unexpected error
	 */
	public void terminate(SipSession mySipSession, Exception e) {

		log.debug("["+mySipSession.getCallId()+"] *TERMINATE EXCEPTION* -> "+e);
		e.printStackTrace();
		sendBye(mySipSession);
		releaseSession(mySipSession);
	}


	/**
	 * Mute the caller's outgoing audio to conference when they enter in *01 and they
	 * are currently unmuted
	 */
	public void mute(SipSession mySipSession) {
		MenuOperation mop =null;
	
		try {
			NetworkConnection myNetworkConnection = (NetworkConnection) mySipSession.getAttribute("NETWORK_CONNECTION");
			ConferenceSession myConferenceSession = getConferenceSession(mySipSession);
			MediaSession myMediaSession = (MediaSession) mySipSession.getAttribute("MEDIA_SESSION");
			//FR6008 Start
			if ( confJoinUseMixerAdapter ) {
				log.debug("Mute method: using MixerAdapter");
				MixerAdapter mxa = (MixerAdapter) mySipSession.getAttribute("MIXER_ADAPTER");
				if ( mxa == null ) {
					log.debug("mute method major error MixerAdapter can not be retrived from sip session...");
					return;
				}
				log.debug("["+mySipSession.getCallId()+"] (MUTE METHOD - JOIN NC -> MIXER Adapter RECV ONLY: "+mxa);
				//Feb 5 MSC-18
				//Note Application Developers 
				//in order to mute in the Dialogic JSR 309 implementation of the connector
				//you must first unjoin... Note unjoin is synchronous and then follow by
				//a join(Joinable.Direction.RECV)
				//To put it back in conference simply repeat the same steps 
				//except use the join(FULLDUPLEX);
				mop = JMCConferenceServlet.getMenuOperation(mySipSession);
				if (mop != null) 
					log.debug("MUTE: MenuOperation value = " + mop.name());
				if ( mop != MenuOperation.MUTE ) {
					log.debug("JMCConferenceServlet DEMO Application: mute() - First calling unjoin on this leg");
					myNetworkConnection.unjoin(mxa);
					log.debug("JMCConferenceServlet DEMO Application: mute() - Then call join( Joinable.Direction.RECV ) on the leg");
					myNetworkConnection.join(Joinable.Direction.RECV, mxa);
					JMCConferenceServlet.setMenuOperation(mySipSession, MenuOperation.MUTE); //save the menu operation state in leg session
				}else {
					log.debug("JMCConferenceServlet:: Cant execute MUTE sequence since the the leg still is in MUTED State");
				}
			} else 	//FR6008 End
			{
				log.debug("Mute method: using Mixer directly");
				log.debug("["+mySipSession.getCallId()+"] JOIN NC -> MIXER RECV ONLY: "+myConferenceSession.getMediaMixer());
				//Feb 5 MSC-18
				//Note Application Developers 
				//in order to mute in the Dialogic JSR 309 implementation of the connector
				//you must first unjoin... Note unjoin is synchronous and then follow by
				//a join(Joinable.Direction.RECV)
				//To put it back in conference simply repeat the same steps 
				//except use the join(FULLDUPLEX);
				mop = JMCConferenceServlet.getMenuOperation(mySipSession);
				if (mop != null)
					log.debug("MUTE: MenuOperation value = " + mop.name());

				if ( mop != MenuOperation.MUTE ) {
					log.debug("JMCConferenceServlet DEMO Application: mute() - First calling unjoin on this leg");
					myNetworkConnection.unjoin( myConferenceSession.getMediaMixer());
					log.debug("JMCConferenceServlet DEMO Application: mute() - Then call join( Joinable.Direction.RECV ) on the leg");			
					myNetworkConnection.join(Joinable.Direction.RECV, myConferenceSession.getMediaMixer());
					JMCConferenceServlet.setMenuOperation(mySipSession, MenuOperation.MUTE);
				}else {
					log.debug("JMCConferenceServlet:: Cant execute MUTE sequence since the the leg still is in MUTED State");
				}
			}

			mySipSession.setAttribute("MEDIA_SESSION", myMediaSession);
		} catch (MsControlException e) {
			log.error("Unable to mute the participant");
		}

	}

	/**
	 * Un-mute the caller's outgoing audio to conference when they enter in *01 and they
	 * are currently muted
	 */
	public void unmute(SipSession mySipSession) {
		MenuOperation mop =null;
	
		try {
			NetworkConnection myNetworkConnection = (NetworkConnection) mySipSession.getAttribute("NETWORK_CONNECTION");
			ConferenceSession myConferenceSession = getConferenceSession(mySipSession);
			MediaSession myMediaSession = (MediaSession) mySipSession.getAttribute("MEDIA_SESSION");

			//FR6008 Start
			if ( confJoinUseMixerAdapter ) {
				log.debug("UnMute method: using MixerAdapter");
				MixerAdapter mxa = (MixerAdapter) mySipSession.getAttribute("MIXER_ADAPTER");
				if ( mxa == null ) {
					log.debug("unmute method major error MixerAdapter can not be retrived from sip session...");
					return;
				}
				log.debug("["+mySipSession.getCallId()+"] (MUTE METHOD - JOIN NC -> MIXER Adapter DUPLEX: "+mxa);
				log.debug("["+mySipSession.getCallId()+"] (MUTE METHOD - JOIN NC -> MIXER Adapter RECV ONLY: "+mxa);
				//Feb 5 MSC-18
				//Note Application Developers 
				//In order to unmute in the Dialogic JSR 309 implementation of the connector
				//you must first unjoin... Note unjoin is synchronous and then follow by
				//a join(Joinable.Direction)
				mop = JMCConferenceServlet.getMenuOperation(mySipSession);
				if (mop != null) {
					log.debug("UNMUTE: MenuOperation value = " + mop.name());
				}
				if ( mop == MenuOperation.MUTE ) {
					log.debug("JMCConferenceServlet:: Executing Unmuting sequence");
					log.debug("JMCConferenceServlet:: Executing First unjoin the leg ");
					myNetworkConnection.unjoin(mxa);
					log.debug("JMCConferenceServlet:: Executing Second re-unjoin the leg back in full duplex ");
					myNetworkConnection.join(Joinable.Direction.DUPLEX, mxa);
					JMCConferenceServlet.setMenuOperation(mySipSession, MenuOperation.UNMUTE);
				}else {
					log.debug("JMCConferenceServlet:: Cant execute UNMUTE sequence since the the leg is not in MUTED State");
				}
				
			} else 	//FR6008 End
			{
				log.debug("UnMute method: using Mixer directly");
				log.debug("["+mySipSession.getCallId()+"] JOIN NC -> UNMUTE");
				//Feb 5 MSC-18
				//Note Application Developers 
				//In order to unmute in the Dialogic JSR 309 implementation of the connector
				//you must first unjoin... Note unjoin is synchronous and then follow by
				//a join(Joinable.Direction)
				mop = JMCConferenceServlet.getMenuOperation(mySipSession);
				if (mop != null) {
					log.debug("UNMUTE: MenuOperation value = " + mop.name());
				}
				//if ( mop.compareTo(MenuOperation.MUTE) == 0) {
					log.debug("JMCConferenceServlet:: Executing Unmuting sequence");
					log.debug("JMCConferenceServlet:: Executing First unjoin the leg ");
					myNetworkConnection.unjoin(myConferenceSession.getMediaMixer());
					log.debug("JMCConferenceServlet:: Executing Second re-unjoin the leg back in full duplex ");
					myNetworkConnection.join(Joinable.Direction.DUPLEX, myConferenceSession.getMediaMixer());
					JMCConferenceServlet.setMenuOperation(mySipSession, MenuOperation.UNMUTE);
				//}else {
				//	log.debug("JMCConferenceServlet:: Cant execute UNMUTE sequence since the the leg is not in MUTED State");
				//}
				
			}


			mySipSession.setAttribute("MEDIA_SESSION", myMediaSession);

		} catch (MsControlException e) {
			log.error("Unable to mute the participant");
		}
	}


	/**
	 *  Play prompt(s) to participant mediagroup only
	 */
	public void playPromptParticipant(SipSession mySipSession, URI[] prompts) {

		MediaGroup myMediaGroup = (MediaGroup) mySipSession.getAttribute("MEDIA_GROUP");

		try {
			myMediaGroup.getPlayer().play(prompts,
					null,
					Parameters.NO_PARAMETER);

		} catch (MsControlException e) {
			log.error("Unable to mute the participant");
		}
	}

	// Monitor for when SessionTimer expires for whatever
	private class AllocationListener implements AllocationEventListener, Serializable {

		private static final long serialVersionUID = 1L;

		@Override
		public void onEvent(AllocationEvent anEvent) {
			MediaSession myMediaSession = anEvent.getSource().getMediaSession();
			SipSession mySipSession = (SipSession) myMediaSession.getAttribute("SIP_SESSION");

			log.debug("["+mySipSession.getCallId()+"] RECEIVED ALLOCATION EVENT: "+anEvent.getEventType());
			log.debug("["+mySipSession.getCallId()+"] RECEIVED ALLOCATION EVENTB: "+anEvent.getErrorText());
			log.debug("["+mySipSession.getCallId()+"] RECEIVED SDP PORT MANAGER EVENTC: "+anEvent.getError());
			log.debug("["+mySipSession.getCallId()+"] SESSION: "+mySipSession);
			log.debug("["+mySipSession.getCallId()+"] CHECK: "+mySipSession+" / "+mySipSession.isValid());

			if (anEvent.getEventType().equals(AllocationEvent.IRRECOVERABLE_FAILURE))
			{
				if (mySipSession != null && mySipSession.isValid()) {
					sendBye(mySipSession);
					jcs.releaseSession(mySipSession);
				}
			}
		}
	}

	/**
	 * Event listener for conference Player, Signal and Network events
	 */
	private class MediaListener<T extends MediaEvent<?>> implements MediaEventListener<T>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void onEvent(T anEvent) {

			SipSession mySipSession = null;
			State myState = null;
			if ( !anEvent.isSuccessful() ) {
				log.debug("Error onEvent received problem is: " + anEvent.getErrorText());
				return;
			}
			log.debug("JMConferenceServlet Applicaiton received event =" + anEvent.getEventType().toString() );

			try {
				if (anEvent instanceof PlayerEvent) {
					log.debug("JMConferenceServlet handling player event"  );
					PlayerEvent playerEvent = (PlayerEvent)anEvent;
					MediaSession myMediaSession = playerEvent.getSource().getMediaSession();
					mySipSession = (SipSession) myMediaSession.getAttribute("SIP_SESSION");
					myState = (State)mySipSession.getAttribute("STATE");
					log.debug("["+mySipSession.getCallId()+"] RECEIVED PLAYER EVENT: "+anEvent.getEventType());
					myState.onPlayerEvent(jcs, mySipSession, playerEvent);
				}else 	if (anEvent instanceof SignalDetectorEvent) {
					log.debug("JMConferenceServlet handling signal detector event"  );
					SignalDetectorEvent signalDetectorEvent = (SignalDetectorEvent)anEvent;
					log.debug("JMConferenceServlet handling signal detector event 2"  );
					MediaSession myMediaSession = signalDetectorEvent.getSource().getMediaSession();
					log.debug("JMConferenceServlet handling signal detector event 3"  );
					mySipSession = (SipSession) myMediaSession.getAttribute("SIP_SESSION");
					if ( mySipSession == null )
						log.debug("mySipSession = null");
					else
						log.debug("mySipSession = " + mySipSession.toString());
					log.debug("JMConferenceServlet handling signal detector event 4"  );
					myState = (State)mySipSession.getAttribute("STATE");
					log.debug("JMConferenceServlet handling signal detector event 5"  );
					log.debug("["+mySipSession.getCallId()+"] RECEIVED SIGNAL DETECTOR EVENT: "+anEvent.getEventType());
					myState.onSignalDetectorEvent(jcs, mySipSession, signalDetectorEvent);
					log.debug("JMConferenceServlet handling signal detector event 6 Done"  );
				}
				else if (anEvent instanceof SdpPortManagerEvent) {
					log.debug("JMConferenceServlet Applicaiton received event vo type SdpPortManagerEvent " );
					SdpPortManagerEvent sdpPortManagerEvent = (SdpPortManagerEvent)anEvent;
					log.debug("JMConferenceServlet handling signal SdpPortManagerEvent event 2"  );
					SdpPortManager sdp = sdpPortManagerEvent.getSource();
					log.debug("JMConferenceServlet handling signal SdpPortManagerEvent event 3"  );
					MediaSession myMediaSession = sdpPortManagerEvent.getSource().getMediaSession();
					log.debug("JMConferenceServlet handling signal SdpPortManagerEvent event 4"  );
					mySipSession = (SipSession) myMediaSession.getAttribute("SIP_SESSION");
					log.debug("JMConferenceServlet handling signal SdpPortManagerEvent event 5"  );

					log.debug("["+mySipSession.getCallId()+"] RECEIVED SDP PORT MANAGER EVENT: "+anEvent.getEventType());
					log.debug("["+mySipSession.getCallId()+"] RECEIVED SDP PORT MANAGER EVENTB: "+anEvent.getErrorText());
					log.debug("["+mySipSession.getCallId()+"] RECEIVED SDP PORT MANAGER EVENTC: "+anEvent.getError());
					log.debug("JMConferenceServlet handling signal SdpPortManagerEvent event 6"  );
					log.debug("["+mySipSession.getCallId()+"] CHECK: "+mySipSession+" / "+mySipSession.isValid());

					if (mySipSession.isValid())
					{
						myState = (State)mySipSession.getAttribute("STATE");
						SipServletRequest request = (SipServletRequest) mySipSession.getAttribute("UNANSWERED_INVITE");
						if ((anEvent.getEventType().equals(JoinEvent.JOINED)) || (anEvent.getEventType().equals(JoinEvent.UNJOINED)) )		//mute unmute operation
						{
							MenuOperation  mo  = JMCConferenceServlet.getMenuOperation(mySipSession);
							if ( mo.compareTo(MenuOperation.NOTHING) == 0){
								startCollecting( mySipSession);
								mySipSession.setAttribute("STATE", State.CollectingParticipantOptions);
							}else if ( mo.compareTo(MenuOperation.MUTE) == 0) {
								//if ((Boolean)mySipSession.getAttribute("MUTE") == true) {
								//myState.playYouAreMuted(jcs,mySipSession);
								mySipSession.setAttribute("STATE", State.CollectingParticipantOptions);
							} else if ( mo.compareTo(MenuOperation.UNMUTE) == 0) {
								//if ((Boolean)mySipSession.getAttribute("MUTE") == false) {
								//mySipSession.setAttribute("STATE", State.WaitingForUnMuttingConfirmation);
								//myState.playYouAreUnmuted(jcs,mySipSession);
								mySipSession.setAttribute("STATE", State.CollectingParticipantOptions);
							} else if ( mo.compareTo(MenuOperation.PARK) == 0 ) {
								myState.playYouAreParked(jcs,mySipSession);
							} else if ( mo.compareTo(MenuOperation.UNPARK) == 0 ) {
								myState.playYouAreUnParked(jcs,mySipSession);
							}

						} else if (anEvent.getEventType().equals(SdpPortManagerEvent.ANSWER_GENERATED))
						{

							SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
							try
							{
								response.setContent(sdp.getMediaServerSessionDescription(), "application/sdp");
								response.send();

								// Start the state machine for this participant
								if (mySipSession.getAttribute("STATE").equals(State.Initial))
									start(mySipSession);
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

						}
						else if (anEvent.getEventType().equals(SdpPortManagerEvent.ANSWER_PROCESSED)) //only true for IPMS ..this event not sent in MSML...need to handle join confirm
						{
							log.debug("["+mySipSession.getCallId()+"] SDPEVENT: ANSWER PROCESSED, RESPONSE: "+mySipSession.getAttribute("RESPONSE").toString());
							log.debug("["+mySipSession.getCallId()+"] SESSION IS "+mySipSession.toString());
							SipServletResponse response = (SipServletResponse) mySipSession.getAttribute("RESPONSE");
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
							myState.onReInviteOK(jcs, mySipSession);
						}
						// Send REINVITE to caller once they JOIN the conference
						else if (anEvent.getEventType().equals(SdpPortManagerEvent.UNSOLICITED_OFFER_GENERATED))
						{
							log.warn("["+mySipSession.getCallId()+"] SDPEVENT: UNSOLICITIED OFFER GENERATED");
							SipServletMessage reInviteMessage = mySipSession.createRequest("INVITE");
							try
							{
								byte[] sessionDesc = sdp.getMediaServerSessionDescription();
								reInviteMessage.setContent(sessionDesc, "application/sdp");
								reInviteMessage.send();
							}
							catch (SdpPortManagerException e)
							{
								e.printStackTrace();
							}
							catch (UnsupportedEncodingException e)
							{
								e.printStackTrace();
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
						else if (anEvent.getEventType().equals(SdpPortManagerEvent.NETWORK_STREAM_FAILURE)) {
							sendBye(mySipSession);
							jcs.releaseSession(mySipSession);
							return;
						}
					}
				}
			} catch (Exception e) {
				terminate(mySipSession, e);
			}
		}
	}

	public void releaseSession(SipSession mySipSession) {
		if (mySipSession != null && mySipSession.isValid()) {

			ConferenceSession myConferenceSession = getConferenceSession(mySipSession);

			log.debug("["+mySipSession.getCallId()+"] ACTUALLY RELEASING NC's MEDIAGROUP/SESSION");
			MediaGroup myMediaGroup = (MediaGroup) mySipSession.getAttribute("MEDIA_GROUP");
			NetworkConnection myNetworkConnection = (NetworkConnection) mySipSession.getAttribute("NETWORK_CONNECTION");

			if (myConferenceSession != null) {
				myConferenceSession.removeParticipant(mySipSession);
				// Remove the conference if there are no longer any participants
				log.debug("LEFT IN CONFERENCE: "+myConferenceSession.getNumParticipant());
				if (myConferenceSession.getNumParticipant() == 0) {
					log.debug("["+mySipSession.getCallId()+"] LAST ONE IN CONFERENCE, ATTEMPTING TO RELEASE CONFERENCE: "+myConferenceSession.getConfId());
					myConferenceSession.release();
					log.debug("["+mySipSession.getCallId()+"] Release last leg after release the conference first");
					MediaSession myMediaSession = (MediaSession) mySipSession.getAttribute("MEDIA_SESSION");
					myMediaSession.release();
				}else {
					log.debug("["+mySipSession.getCallId()+"] Conference Still joined to other call legs..not Release the Conference");
					log.debug("["+mySipSession.getCallId()+"] calling mediaSession.release() for NC");
					MediaSession myMediaSession = (MediaSession) mySipSession.getAttribute("MEDIA_SESSION");
					myMediaSession.release();
					log.debug("["+mySipSession.getCallId()+"] calling mediaSession.release() for NC done");
				}
			}

			if (mySipSession.isValid()) {
				mySipSession.invalidate();
			}
			if (mySipSession.getApplicationSession().isValid()) {
				mySipSession.getApplicationSession().invalidate();
			}
		}		
	}

	//FR6008 Start
	public void exitConference(SipSession mySipSession) {
		if (mySipSession != null && mySipSession.isValid()) {

			ConferenceSession myConferenceSession = getConferenceSession(mySipSession);

			log.debug("["+mySipSession.getCallId()+"] This Leg is exiting the conference session");
			NetworkConnection myNetworkConnection = (NetworkConnection) mySipSession.getAttribute("NETWORK_CONNECTION");

			if (myConferenceSession != null) {
				myConferenceSession.removeParticipant(mySipSession);
				// Remove the conference if there are no longer any participants
				log.debug("LEFT IN CONFERENCE: "+myConferenceSession.getNumParticipant());
				log.debug("["+mySipSession.getCallId()+"] Unjoining NC from existing conference");
				MixerAdapter mxa = (MixerAdapter)mySipSession.getAttribute("MIXER_ADAPTER");


				if ( mxa != null ) {
					try {
						myNetworkConnection.unjoin(mxa);
					}catch (MsControlException ex)
					{
						log.error("Error unjoing from conference ", ex);
						ex.printStackTrace();
					}catch (Exception ex) {
						log.error("Error unjoing from conference ", ex);
						ex.printStackTrace();
					}
				} else {
					log.debug("["+mySipSession.getCallId()+"] Unjoining Session from Conference using Mixer");
					MediaMixer mx = myConferenceSession.getMediaMixer();
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
						log.debug("["+mySipSession.getCallId()+"] could not Unjoining Session from Conference using Mixer since MediaMixer was found to be null");
					}
				}

			}

		}		
	}

	public void turnOnDtmfDetector(MediaGroup myMediaGroup, SipSession mySipSession)
	{
		try {
			Parameters detectorOptions = JMCConferenceServlet.theMediaSessionFactory.createParameters();

			detectorOptions.put(SignalDetector.BUFFERING, Boolean.FALSE );
			EventType[] arrayEnabledEvents = {SignalDetectorEvent.SIGNAL_DETECTED} ;
			detectorOptions.put(SignalDetector.ENABLED_EVENTS, arrayEnabledEvents);	

			log.debug("["+mySipSession.getCallId()+"] RESTARTING RECEIVESIGNALS (-1) FOREVER");
			myMediaGroup.getSignalDetector().receiveSignals(-1, null,
					null, detectorOptions);
			log.debug("["+mySipSession.getCallId()+"] RESTARTING RECEIVESIGNALS (-1) FOREVER [DONE]");
		} catch ( MsControlException mex ) {
			mex.printStackTrace();
		}
	}

	//FR6008 End


	public ConferenceSession getConferenceSession(SipSession mySipSession) {
		ConferenceSession myConferenceSession = null;
		MediaSession conferenceMediaSession = (MediaSession)mySipSession.getAttribute("CONFERENCE_MEDIA_SESSION");
		if (conferenceMediaSession != null) {
			myConferenceSession = (ConferenceSession)conferenceMediaSession.getAttribute("CONFERENCE_SESSION");
		}
		return myConferenceSession;
	}

	public MediaSession getMediaSessionFromURI(URI uri) {
		MediaSession mediaSession = null;
		log.debug("[GETMEDIABOJECT]---------------> URI: "+uri);
		try {
			mediaSession = (MediaSession)theMediaSessionFactory.getMediaObject(uri);
		} catch (Exception e1) {
			log.debug("[GETMEDIABOJECT]---------------> ERROR: No media object returned");	
		}
		if (mediaSession != null) {
			log.debug("[GETMEDIABOJECT]---------------> MEDIA OBJECT: "+mediaSession);
		}
		return mediaSession;
	}

	/*
	 * Handle session timeouts
	 */
	@Override
	public void sessionCreated(SipApplicationSessionEvent sasEvent) {
	}
	@Override
	public void sessionDestroyed(SipApplicationSessionEvent sasEvent) {
	}
	// No sessions left over on IPMS after timeout, did everything invalidate and release properly?
	@Override
	public void sessionExpired(SipApplicationSessionEvent sasEvent) {
		SipSession mySipSession = (SipSession)sasEvent.getApplicationSession().getAttribute("SIP_SESSION");
		sendBye(mySipSession);
		releaseSession(mySipSession);
		
	}
	@Override
	public void sessionReadyToInvalidate(SipApplicationSessionEvent arg0) {
	}

	public void executeUnmute( JMCConferenceServlet jcs, SipSession mySipSession ){
		log.error("Unexpected executeUnmute in state " + this);
	}

	public void executeMute( JMCConferenceServlet jcs, SipSession mySipSession ){
		log.error("Unexpected executeMute in state " + this);
	}

}


