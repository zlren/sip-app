/*DIALOGIC CONFIDENTIAL
 

 Copyright 2010 Dialogic Corporation. All Rights Reserved.
 The source code contained or described herein and all documents related to
 the source code (collectively "Material") are owned by Dialogic Corporation 
 or its suppliers or licensors ("Dialogic"). 

 BY DOWNLOADING, ACCESSING, INSTALLING, OR USING THE MATERIAL YOU AGREE TO BE
 BOUND BY THE TERMS AND CONDITIONS DENOTED HERE AND ANY ADDITIONAL TERMS AND
 CONDITIONS SET FORTH IN THE MATERIAL. Title to the Material remains with 
 Dialogic. The Material contains trade secrets and proprietary and 
 confidential information of Dialogic. The Material is protected by worldwide
 Dialogic copyright(s) and applicable trade secret laws and treaty provisions.
 No part of the Material may be used, copied, reproduced, modified, published, 
 uploaded, posted, transmitted, distributed, or disclosed in any way without
 prior express written permission from Dialogic Corporation.
 
 No license under any applicable patent, copyright, trade secret or other 
 intellectual property right is granted to or conferred upon you by disclosure
 or delivery of the Material, either expressly, by implication, inducement, 
 estoppel or otherwise. Any license under any such applicable patent, 
 copyright, trade secret or other intellectual property rights must be express
 and approved by Dialogic Corporation in writing.

 You understand and acknowledge that the Material is provided on an 
 AS-IS basis, without warranty of any kind.  DIALOGIC DOES NOT WARRANT THAT 
 THE MATERIAL WILL MEET YOUR REQUIREMENTS OR THAT THE SOURCE CODE WILL RUN 
 ERROR-FREE OR UNINTERRUPTED.  DIALOGIC MAKES NO WARRANTIES, EXPRESS OR 
 IMPLIED, INCLUDING, WITHOUT LIMITATION, ANY WARRANTY OF NON-INFRINGEMENT, 
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  DIALOGIC ASSUMES NO 
 RISK OF ANY AND ALL DAMAGE OR LOSS FROM USE OR INABILITY TO USE THE MATERIAL. 
 THE ENTIRE RISK OF THE QUALITY AND PERFORMANCE OF THE MATERIAL IS WITH YOU.  
 IF YOU RECEIVE ANY WARRANTIES REGARDING THE MATERIAL, THOSE WARRANTIES DO NOT 
 ORIGINATE FROM, AND ARE NOT BINDING ON DIALOGIC.

 IN NO EVENT SHALL DIALOGIC OR ITS OFFICERS, EMPLOYEES, DIRECTORS, 
 SUBSIDIARIES, REPRESENTATIVES, AFFILIATES AND AGENTS HAVE ANY LIABILITY TO YOU 
 OR ANY OTHER THIRD PARTY, FOR ANY LOST PROFITS, LOST DATA, LOSS OF USE OR 
 COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES, OR FOR ANY INDIRECT, 
 SPECIAL OR CONSEQUENTIAL DAMAGES RELATING TO THE MATERIAL, UNDER ANY CAUSE OF
 ACTION OR THEORY OF LIABILITY, AND IRRESPECTIVE OF WHETHER DIALOGIC OR ITS 
 OFFICERS, EMPLOYEES, DIRECTORS, SUBSIDIARIES, REPRESENTATIVES, AFFILIATES AND 
 AGENTS HAVE ADVANCE NOTICE OF THE POSSIBILITY OF SUCH DAMAGES.  THESE 
 LIMITATIONS SHALL APPLY NOTWITHSTANDING THE FAILURE OF THE ESSENTIAL PURPOSE 
 OF ANY LIMITED REMEDY.  IN ANY CASE, DIALOGIC'S AND ITS OFFICERS', 
 EMPLOYEES', DIRECTORS', SUBSIDIARIES', REPRESENTATIVES', AFFILIATES' AND 
 AGENTS' ENTIRE LIABILITY RELATING TO THE MATERIAL SHALL NOT EXCEED THE 
 AMOUNTS OF THE FEES THAT YOU PAID FOR THE MATERIAL (IF ANY). THE MATERIALE 
 IS NOT FAULT-TOLERANT AND IS NOT DESIGNED, INTENDED, OR AUTHORIZED FOR USE IN 
 ANY MEDICAL, LIFE SAVING OR LIFE SUSTAINING SYSTEMS, OR FOR ANY OTHER 
 APPLICATION IN WHICH THE FAILURE OF THE MATERIAL COULD CREATE A SITUATION 
 WHERE PERSONAL INJURY OR DEATH MAY OCCUR. Should You or Your direct or 
 indirect customers use the MATERIAL for any such unintended or unauthorized 
 use, You shall indemnify and hold Dialogic and its officers, employees, 
 directors, subsidiaries, representatives, affiliates and agents harmless 
 against all claims, costs, damages, and expenses, and attorney fees and 
 expenses arising out of, directly or indirectly, any claim of product 
 liability, personal injury or death associated with such unintended or 
 unauthorized use, even if such claim alleges that Dialogic was negligent 
 regarding the design or manufacture of the part.

**********/


package testing.unit;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Properties;

import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.MediaConfigException;
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.MsControlFactory;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.JoinEvent;
import javax.media.mscontrol.join.JoinEventListener;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.Player;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.networkconnection.SdpPortManagerException;
import javax.media.mscontrol.spi.Driver;
import javax.media.mscontrol.spi.DriverManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSession.State;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.DlgcDemoProperty;
import testing.DlgcSdpPortEventListener;


/*
 * This Servlet should only be use to test the performance of the connector.
 * It should be use in conjunction with sipp for example to test loading, and performance.
 */


public  class DlgcPlayerPerformanceTest extends SipServlet implements Serializable, SipServletListener
{

	private static final long serialVersionUID = 1;
	// Each incoming call goes through the following states:
	public final static String WAITING_FOR_MEDIA_SERVER = "WAITING_FOR_MEDIA_SERVER";
	public final static String WAITING_FOR_ACK = "WAITING_FOR_ACK";
	public final static String WAITING_FOR_MEDIA_SERVER_2 = "WAITING_FOR_MEDIA_SERVER_2"; // (only if the initial INVITE had no SDP offer)
	public final static String JOINING = "JOINING";
	public final static String DIALOG = "DIALOG";
	public final static String BYE_SENT = "BYE_SENT";
	public final static String STOP_COLLECTING = "STOP_COLLECTING";
	

	    
	//protected ConfigSymbol<MediaGroupConfig> mediaGroupConfig;
	protected Configuration<MediaGroup> mediaGroupConfig;
    protected boolean asyncJoin = false;
    protected boolean bTestStopReq =false;
    protected boolean terminateAfterDialog = true;
    transient protected MyJoinEventListener statusListener;
    
    protected static Boolean isReady = false;
    
    protected static String DialogicDriverName = "com.dialogic.dlg309";

	// Listener for NetworkConnection events
	private MyNetworkSdpPortConnectionListener networkConnectionListener;
	
	// Listener for MediaGroup events
	protected MyPlayerListener playerListener;
	
	// The prompt to play
	protected URI prompt;
	private boolean servletInitializedFlag =false;
	
	transient protected MsControlFactory mscFactory;
	static protected DlgcDemoProperty			demoPropertyObj;
	protected boolean dlgcSipServletLoaded =false;
	
	protected static final String ORACLE_PLATFORM = "ORACLE";
	protected static final String TELESTAX_PLATFORM    = "TELESTAX";
	protected static final String TROPO_PLATFORM    = "TROPO";
	protected Boolean playerServletInitCalled = false;
	protected String platform = null; 
	String targetMediaServer = null;
	protected static String dlgcDriverName = "com.dialogic.dlg309";
	transient protected DlgcSdpPortEventListener speListener;
	transient protected Driver dlgcDriver =null;

	
	protected void myServletInitialized(SipServletContextEvent x )
	{
					
		    String promptString = null;
			synchronized (isReady) 
			{	
				try 
				{
					servletInitializedFlag = true;
					Driver dialogicDriver = DriverManager.getDriver(DialogicDriverName);
					mscFactory = dialogicDriver.getFactory(null) ;
					demoPropertyObj = new DlgcDemoProperty(this.getClass());
					
					//Properties props = LoadProperties();
					String testPlayerProp = "test_regular";
					
					if ( demoPropertyObj != null ) {
						log.info("DlgcPlayerPerformanceTest Property file found");
						testPlayerProp = demoPropertyObj.getProperty("player.test.configuration");
						if ( testPlayerProp == null )
							bTestStopReq =false;
						else if ( testPlayerProp.compareToIgnoreCase("testStop") == 0 )
							bTestStopReq =true;
						else
							bTestStopReq =false;
					} else {
						log.info("DlgcPlayerPerformanceTest Property file not found");
						bTestStopReq = false;
					}
					
					Boolean ipmsMediaServerType = false;
		
					
					
					if (promptString == null)
					{
						//promptString = "file:////opt/snowshore/prompts/generic/en_US/twenty_sec_test.ulaw";
						
						if ( ipmsMediaServerType == true )
							promptString = "file:////opt/snowshore/prompts/generic/en_US/twenty_sec_test.ulaw";
						else
							//XMS PROMPT
							promptString = "file:////var/lib/xms/media/en_US/verification/snow/prompts/generic/en_US/twenty_sec_test.wav";
						
						if ( testPlayerProp != null )
						{
							if ( testPlayerProp.compareToIgnoreCase("test_wrong_uri_local" ) == 0 )
							{
								//Test Invalid URI
								log.info("DlgcPlayerPerformanceTest Property file set to test_wrong_uri using local FILE");
								promptString = "file:////opt/snowshore/prompts/generic/en_US/does_not_exists.ulaw";
							} else if ( testPlayerProp.compareToIgnoreCase("test_wrong_uri_remote" ) == 0 ) {
								log.info("DlgcPlayerPerformanceTest Property file set to test_wrong_uri using Remote");
								promptString = "http://localhost/snowshore/prompts/generic/en_US/does_not_exists.wav";
							} else {
								//promptString = "file:////opt/snowshore/prompts/generic/en_US/twenty_sec_test.ulaw";
								if ( testPlayerProp.compareToIgnoreCase("test_stop_api") == 0) {
									log.info("DlgcPlayerPerformanceTest Property file set to test_stop_api");
									bTestStopReq = true;
								} else if ( testPlayerProp.compareToIgnoreCase("test_regular" ) == 0 ) {
									log.info("DlgcPlayerPerformanceTest Property file set to test_regular");
									bTestStopReq = false;
								}
							} 
						}
					}
				} 
				catch (Exception e) 
				{
					log.error("Error in servletInitialized",e.toString());
					e.printStackTrace();
				}
			
				mediaGroupConfig = MediaGroup.PLAYER; 
				networkConnectionListener = new MyNetworkSdpPortConnectionListener();
				playerListener = new MyPlayerListener();
				prompt = URI.create(promptString);
			
				isReady = Boolean.TRUE; 
			}
			
			log.info("DlgcPlayerPerformanceTest initialized...");
		}
		
	
	@Override
	public void servletInitialized(SipServletContextEvent arg0) {
	//	String promptString = null;
		String sName = arg0.getSipServlet().getServletName();
		


		if ( ( platform != null ) && (platform.compareToIgnoreCase(TELESTAX_PLATFORM) == 0 ) || ( platform != null ) && (platform.compareToIgnoreCase(ORACLE_PLATFORM) == 0 )) {
			if( sName.equalsIgnoreCase("DlgcSipServlet") )
			{
				dlgcSipServletLoaded = true;
				log.debug(" DlgcPlayerTest::servletInitialized DlgcSipServlet loaded");			
			}
			
			else if( sName.equalsIgnoreCase("DlgcPlayerTest") ) 
					playerServletInitCalled =true;

			if( playerServletInitCalled && dlgcSipServletLoaded)
			{
				if ( servletInitializedFlag == false ) {
					log.debug("Entering DlgcPlayerTest::servletInitialized servletName: " + sName);			
					servletInitializedFlag = true;
					initDriver();
					myServletInitialized(arg0);
				} else {
					log.debug("DlgcPlayerTest::servletInitialized(): already servletInitialized was called...debouncing " + sName);
				}
			}

		} else { //TROPO Framework

			if( sName.equalsIgnoreCase("DlgcSipServlet") )
			{
				dlgcSipServletLoaded = true;
				log.debug(" DlgcPlayerTest::servletInitialized DlgcSipServlet loaded");			
				return;
			}

			if( (sName.equalsIgnoreCase("DlgcPlayerTest") ) && dlgcSipServletLoaded)
			{
				if ( servletInitializedFlag == false ) {
					log.debug("Entering DlgcPlayerTest::servletInitialized servletName: " + sName);			
					servletInitializedFlag = true;
					initDriver();
					myServletInitialized(arg0);
				} else {
					log.debug("DlgcPlayerTest::servletInitialized(): already servletInitialized was called...debouncing " + sName);
				}
			}
		}

	}
	
	protected void initDriver()
	{
		try
		{
			dlgcDriver = DriverManager.getDriver(dlgcDriverName);	

			if ( targetMediaServer != null ) {
				Properties factoryProperties = new Properties();
				factoryProperties.setProperty(MsControlFactory.MEDIA_SERVER_URI, targetMediaServer);
				mscFactory = dlgcDriver.getFactory(factoryProperties);
			}else 
				mscFactory = dlgcDriver.getFactory(null);

			speListener = new DlgcSdpPortEventListener();
		}
		catch (Exception e)
		{
			//			throw new ServletException(e);
			log.error("Error in servletInitialized",e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public void init(ServletConfig cfg) throws ServletException 
	{
		super.init(cfg);
		platform = this.getWebServerPlatform();
		servletInitializedFlag =false;
		log.info("DlgcPlayerPerformanceTest initialized...");
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
		log.info("APPSERVER_PLATFORM set to: " + platform);
		return platform;
	}

	@Override
    public void doInvite(final SipServletRequest req)
		throws ServletException,IOException 
	{
		NetworkConnection conn = null;
		
		synchronized (isReady)
		{
			if (isReady.booleanValue() == false)
			{
				req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
				return;
			}
		}			

		SipSession sipSession = req.getSession();
				
		if (req.isInitial()) {
			// New Call
			try {

				MediaSession mediaSession = mscFactory.createMediaSession();
				sipSession.setAttribute("MEDIA_SESSION", mediaSession);
				mediaSession.setAttribute("SIP_SESSION", sipSession);
				mediaSession.setAttribute("SIP_REQUEST", req);

				// Create a new NetworkConnection and store in SipSession
				//conn = mediaSession.createContainer(NetworkConnectionConfig.c_Basic);
				conn = mediaSession.createNetworkConnection(NetworkConnection.BASIC);
				mediaSession.setAttribute("NETWORK_CONNECTION", conn);
								
				// Set this servlet class as listener
				//conn.addListener(networkConnectionListener);
				conn.getSdpPortManager().addListener(networkConnectionListener); 
				sipSession.setAttribute("NETWORK_CONNECTION", conn);
				
				// Create and join a MediaGroup 
				// TBD: still needs a case here probably because our symbols are not yet genericlized..
				//MediaGroup mg = (MediaGroup) mediaSession.createMediaGroup(mediaGroupConfig).createContainer(mediaGroupConfig);
				MediaGroup mg = (MediaGroup) mediaSession.createMediaGroup(mediaGroupConfig);
				
				// Save reference for future use
				sipSession.setAttribute("MEDIAGROUP", mg);
				// Attach a listener to the Player
				mg.getPlayer().addListener(playerListener);
				// Join it to the NetworkConnection
				doJoin(sipSession, mg, (NetworkConnection)sipSession.getAttribute("NETWORK_CONNECTION"));
				
			} catch (MediaConfigException e) {
				req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR).send();
				return;
			} catch (MsControlException e) {
				// Probably out of resources, or other media server problem.  send 503
				req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
				return;
			} catch (Exception e) {
				req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR).send();
				return;
			}
			
			// Setup the play prompt
			String playPrompt = req.getParameter("play");
			if (playPrompt != null) {
				log.info("Play this file: " + playPrompt);
				prompt = URI.create(playPrompt);
			}
			else
				log.info("Nothing to play");
			
		} else {
			// Existing call.  This is an re-INVITE
			// Get NetworkConnection from SipSession
			conn = (NetworkConnection) sipSession.getAttribute("NETWORK_CONNECTION");
		}
		
		 if (!asyncJoin) { 
			 doModify(req, conn);
		 }
		
	}

	@Override
	protected void doAck(SipServletRequest req)
		throws ServletException, IOException
	{
		SipSession sipSession = req.getSession();
		
		byte[] remoteSdp = req.getRawContent();
		if (remoteSdp != null)
		{
			// Get NetworkConnection from SipSession
			NetworkConnection conn = (NetworkConnection) sipSession.getAttribute("NETWORK_CONNECTION");
			if (conn != null)
			{
				try
				{
					conn.getSdpPortManager().processSdpAnswer(remoteSdp);
					setState(sipSession, WAITING_FOR_MEDIA_SERVER_2);
				}
				catch (MsControlException e)
				{
					e.printStackTrace();
				}
			}
		}


		if (compareState(sipSession, WAITING_FOR_ACK))
		{
			// Play file now
			runDialog(sipSession);
		} 
	}

	public void doCancel(final SipServletRequest req)
	throws ServletException,IOException
	{
		
		MediaSession mediaSession = (MediaSession) req.getSession().getAttribute("MEDIA_SESSION");
		if (mediaSession != null) {
			
			//log.warn("Inside doBye. calling mediaSession release method");
			mediaSession.release();
			
		}
		else
		{
			// Session maybe previously terminated
			log.warn("MEDIA_SESSION attribute does not exist in SIP Session");
		}
		req.createResponse(SipServletResponse.SC_OK).send();
		
		releaseSession(req.getSession() );
	}
	
	@Override
    public void doBye(final SipServletRequest req)
		throws ServletException,IOException
	{
		//log.warn("Inside doBye. ");
		MediaSession mediaSession = (MediaSession) req.getSession().getAttribute("MEDIA_SESSION");
		if (mediaSession != null) {
			
			//log.warn("Inside doBye. calling mediaSession release method");
			mediaSession.release();
			
		}
		else
		{
			// Session maybe previously terminated
			log.warn("MEDIA_SESSION attribute does not exist in SIP Session");
		}
		req.createResponse(SipServletResponse.SC_OK).send();
		
		releaseSession(req.getSession() );
		//req.getSession().invalidate();
		
	}
	
	protected void doJoin(SipSession sipSession, MediaGroup mg, NetworkConnection nc) throws MsControlException {
		if (!asyncJoin) {
			//mg.join(Direction.DUPLEX, nc);
			mg.join(Joinable.Direction.DUPLEX, nc);
		}
		else {
			// Attach a status listener for join events
			statusListener = new MyJoinEventListener();
			mg.addListener(statusListener);
			// Request to join it to the NetworkConnection
			setState(sipSession, JOINING);
			mg.joinInitiate(Joinable.Direction.DUPLEX, nc, null);
		}
	}
	
	protected void doModify(SipServletRequest req, NetworkConnection conn) throws ServletException, IOException 
	{
		// set SDP of peer UA to NetworkConnection (assume here that the only possible body is an SDP)
		//String remoteSdp = new String(req.getRawContent()); // may be null, indicating an INVITE w/o SDP
		
		try {
			req.getSession().setAttribute("UNANSWERED_INVITE", req);
			setState(req.getSession(), WAITING_FOR_MEDIA_SERVER);
			byte[]  remoteSdp = req.getRawContent();
			if ( remoteSdp == null ) 
				conn.getSdpPortManager().generateSdpOffer();
				//conn.modify("$", remoteSdp);
				// Store INVITE so it can be responded to later
			else 
				conn.getSdpPortManager().processSdpOffer(remoteSdp);	
			
		//} catch ( SdpException e) {
		//	req.createResponse(SipServletResponse.SC_NOT_ACCEPTABLE_HERE).send();
		//	return;
		} catch ( SdpPortManagerException e ) {
			//catch (NetworkConnectionException e) {
			// Unknown exception, just send 503
			req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
			return;
		} catch ( MsControlException e ) {
			//catch (NetworkConnectionException e) {
			// Unknown exception, just send 503
			req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
			return;
		}
	}
	
	@Override
	protected void doResponse(SipServletResponse resp) throws IOException, ServletException
	{
		log.warn(">>>>>>>>>>  DO RESPONSE ENTER: " + resp.getRequest().getMethod() + ": " + resp.getApplicationSession().hashCode());
		SipSession sipSession= resp.getSession();
		
		
		if ( compareState( sipSession,  BYE_SENT)   ){
			log.warn(">>>>>>>>>>  calling setInvalidate");
			releaseSession(sipSession);
		}
		
	}

	protected boolean compareState(SipSession sipSession, String state)
	{
		return state.equals((String) sipSession.getAttribute("STATE"));
	}

	protected void setState(SipSession sipSession, String state)
	{
		//log.info(new StringBuilder().append("Setting state to: ").append(state).toString());
		sipSession.setAttribute("STATE", state);
	}

	//private class MyNetworkSdpPortConnectionListener implements MediaEventListener<NetworkConnectionEvent>
	private class MyNetworkSdpPortConnectionListener implements MediaEventListener<SdpPortManagerEvent> , Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 3787570336434311378L; 

		public void onEvent(SdpPortManagerEvent event)
		{
			// Get network connection
			
			//NetworkConnection conn = event.getSource();
			SdpPortManager sdpMgr = event.getSource();
			MediaSession mediaSession = sdpMgr.getMediaSession();
			
			log.info("DlgcPlayerPerformanceTest received event = " + event.getEventType() );


			SipSession sipSession = (SipSession) mediaSession.getAttribute("SIP_SESSION");
			NetworkConnection conn = (NetworkConnection)mediaSession.getAttribute("NETWORK_CONNECTION");
			
			if (sipSession.isValid())
			{
				SipServletRequest inv = (SipServletRequest) sipSession.getAttribute("UNANSWERED_INVITE");
				sipSession.removeAttribute("UNANSWERED_INVITE");

				if (event.getEventType() == SdpPortManagerEvent.NETWORK_STREAM_FAILURE) {
					if ( ( event.getError() == MediaErr.CALL_DROPPED ) && ( event.getErrorText().compareToIgnoreCase("MRB DOWN") == 0 ) ) {
						log.debug("MRB or XMS has gone down... don't release media session just null for the GC to clean up.");
						// Session Disconnected by connector...
						sipSession.removeAttribute("MEDIA_SESSION");
						terminateButDontRelease(sipSession, mediaSession);
						return;
					}
				}
				
				try 
				{
					//if (Error.e_OK.equals(event.getError()) && NetworkConnectionConstants.ev_Modify.equals(event.getEventID()))
					if ( event.isSuccessful() )
					{
						if (compareState(sipSession, WAITING_FOR_MEDIA_SERVER)) 
						{
							// Return an SDP answer attached to a 200 OK message
							SipServletResponse resp = inv.createResponse(SipServletResponse.SC_OK);
							// Get SDP from NetworkConnection
							//String sdp = conn.getRawLocalSessionDescription();
							byte[] sdp = conn.getSdpPortManager().getMediaServerSessionDescription();
							resp.setContent(sdp, "application/sdp");
							// Send 200 OK
							resp.send();
							setState(sipSession, WAITING_FOR_ACK);
						} 
						else if (compareState(sipSession, WAITING_FOR_MEDIA_SERVER_2)) 
						{
							// The media server has updated the remote SDP received with the ACK.
							// The INVITE is complete, we are ready to play.
							runDialog(sipSession);
						}
					} 
					else 
					{
						if (SdpPortManagerEvent.SDP_NOT_ACCEPTABLE.equals(event.getError())) 
						{
							// Send 488 error response to INVITE
							if ( inv != null )
								inv.createResponse(SipServletResponse.SC_NOT_ACCEPTABLE_HERE).send();
						} 
						else if (SdpPortManagerEvent.RESOURCE_UNAVAILABLE.equals(event.getError())) 
						{
							// Send 486 error response to INVITE
							if ( inv != null )
								inv.createResponse(SipServletResponse.SC_BUSY_HERE).send();
						
						//else if (Error.e_Disconnected.equals(event.getError()))
						} else 
						{
							// Some unknown error. Send 500 error response to INVITE
							if ( inv != null )
								inv.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR).send();
						}
					
						// Clean up media session
						// Session Disconnected by connector...
						sipSession.removeAttribute("MEDIA_SESSION");
						terminate(sipSession, mediaSession);
					} 
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
					// Clean up
					sipSession.invalidate();
					mediaSession.release();
				}
			}
		}
	}
	
	
	// Join status listener, used for async join mode only
	//class MyJoinEventListener implements StatusEventListener {
	class MyJoinEventListener implements JoinEventListener , Serializable 
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = -6114679718943335034L;

		@Override
		
		
		public void onEvent( JoinEvent anEvent) {
			//public void onEvent(StatusEvent anEvent) {
			
			//if ( anEvent.getError().equals( MediaErr.NO_ERROR) ) same as isSuccessful()
			if ( anEvent.isSuccessful() )
			{
			//if (anEvent.getError().equals(Error.e_OK)) {
				SipSession sipSession = null;
				MediaSession mediaSession = null;
				try {
					// Successfully joined, proceed with the prompt.
					MediaGroup mg = (MediaGroup)anEvent.getSource();
					mediaSession = (MediaSession)mg.getMediaSession();
					sipSession = (SipSession)mediaSession.getAttribute("SIP_SESSION");
					NetworkConnection conn = (NetworkConnection) sipSession.getAttribute("NETWORK_CONNECTION");
					SipServletRequest req = (SipServletRequest) mediaSession.getAttribute("SIP_REQUEST");
					doModify(req, conn);
				} catch (Exception e) {
					log.info("Inside MyJoinEventListener OnEvent... got exception calling terminate...");
					e.printStackTrace();
					// Clean up
					if (sipSession != null) 
						sipSession.invalidate();
					if (mediaSession != null) 
						mediaSession.release();
				}
			}
		}
	}

	/**
	 * Play the prompt to the remote user agent.
	 * @param sipSession
	 */
	protected void runDialog(SipSession sipSession) {
		
		try {
			log.info("Inside runDialog calling play.");
			MediaGroup mg = (MediaGroup)sipSession.getAttribute("MEDIAGROUP");
			
			// Play prompt
			//Parameters params = mediaSessionFactory.createParameters();
			Parameters params = mscFactory.createParameters();
			
			//params.put(PlayerConstants.p_RepeatCount, 3);
			//params.put(PlayerConstants.p_Interval, 2000);
			
			//params.put(Player.REPEAT_COUNT, 3);
			params.put(Player.REPEAT_COUNT, 1);
			params.put(Player.INTERVAL, 2000);
			//log.error("THIS IS NOT AN ERROR VITO - 30 SEC MAX DURATION");
			params.put(Player.MAX_DURATION, 30000);			//change for daniel colovito testing from 20000 to 30000
			mg.getPlayer().play(prompt, null, params);
			setState(sipSession, DIALOG);
			
			
		} catch (Exception e) {
			// Clean up media session
			MediaSession mediaSession = (MediaSession)sipSession.getAttribute("MEDIA_SESSION");
			log.info("Inside runDialog some exeption in the play calling terminate.");
			terminate(sipSession, mediaSession);
			return;
		}
	}

	protected void runDialogAfterPlay(PlayerEvent anEvent)
	{
		MediaSession mediaSession = ((Player)anEvent.getSource()).getMediaSession();
		SipSession sipSession = (SipSession) mediaSession.getAttribute("SIP_SESSION");
		if (terminateAfterDialog) {
			log.info("Inside myPlayerListenter OnEvent... terminateAfterDialog Ends");
			terminate(sipSession, mediaSession);
		}
		
	}
	
	/**
	 * The <code>onEvent</code> method will be called by the Player to notify us when the play terminates.
	 */
	class MyPlayerListener implements MediaEventListener<PlayerEvent> , Serializable 
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = -3262368533599774691L;

		public void onEvent(PlayerEvent anEvent) {
			log.info("Play terminated with errorId : "+ 	anEvent.getError() );
			log.info("Play terminated with errorString : "+ anEvent.getErrorText() );
			log.info("Play terminated with qualifier : "+ anEvent.getQualifier() );
			// Release the call and terminate
			
			if ( anEvent.getEventType() == PlayerEvent.RESUMED ) {
				log.info("Received Player Event: Play Resumed which means Play Started...");
				if ( bTestStopReq ) {
					log.debug(" waiting 2 seconds before sending stop");
					try {
						log.debug("sending stop play");
						Player player = (Player)anEvent.getSource();
						player.stop(true);
					} catch (Exception ex) {
						log.debug(ex.getMessage());
					}
				}
			} else {
				if ( anEvent.getError() == MediaErr.NO_ERROR)
					runDialogAfterPlay(anEvent);
				else 
					log.debug("Error Received: " + anEvent.getErrorText() ) ;
			}
			
		}
	}
	
	protected void terminate(SipSession sipSession, MediaSession mediaSession) {
		if (sipSession.getState() != State.TERMINATED)
		{
			SipServletRequest bye = sipSession.createRequest("BYE");
			log.info("Inside terminiate method");
			try 
			{
				bye.send();
				// Clean up media session
				mediaSession.release();
				setState(sipSession, BYE_SENT);
			} 
			catch (Exception e1) 
			{
				//log.error("Terminating: Cannot send BYE: "+e1);
			}
		}
	}
	
	protected void terminateButDontRelease(SipSession sipSession, MediaSession mediaSession) {
		if (sipSession.getState() != State.TERMINATED)
		{
			SipServletRequest bye = sipSession.createRequest("BYE");
			log.info("Inside terminiate method");
			try 
			{
				bye.send();
				// Clean up media session
				mediaSession=null;  //for GC collection
				setState(sipSession, BYE_SENT);
			} 
			catch (Exception e1) 
			{
				//log.error("Terminating: Cannot send BYE: "+e1);
			}
		}
	}
	
	
	
	//Added  9/11/2009 see if clears memory leak by releasing the session
	protected void releaseSession( SipSession sipSession )
	{
		log.debug("Inside releaseSession. calling invalidate after bye send");
			
		try {
			MediaSession mediaSession = (MediaSession) sipSession.getAttribute("MEDIA_SESSION");
			sipSession.invalidate(); 
			sipSession.getApplicationSession().invalidate();
			if (mediaSession != null)
			{
			//	mediaSession.release();
			}
			//mediaSession.release();
		} catch (Exception e) {
			log.warn("invalidate exception", e);
		}
	    
	}
	
	private Properties LoadProperties() {
		Properties properties = new Properties();
		String configPath = System.getenv("DLG_PROPERTY_FILE");

		try {
			FileInputStream inputStream = new FileInputStream(configPath);
			properties.load(inputStream);
			inputStream.close();
			log.info("Configuration File: " + configPath
					+ " Successfully Loaded");
		} catch (IOException ioe) {
			log.error(ioe.toString());
			log.error((new StringBuilder()).append("Configuration File: ")
					.append(configPath).append(" load failed").toString());
		}
		return properties;
	}
	
	
	private static Logger log = LoggerFactory.getLogger(DlgcPlayerPerformanceTest.class);
	
	

}




