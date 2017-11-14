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

/*This test servlet is similar to the DialogicBridgeConference 
 * except that it simulates an early media scenario.
 * Set up your SIP phone to point to the Web Application Server. 
 * Configure the SIP phone address (i.e. user name) to DlgcEarlyMediaBridgeDemo.
 * Make sure that the Web Application Server is running the dlgmsc_tests.war 
 * application. See Quick Start document
 * 
 * 
 */


package testing.emedia_bridge_conference;

import java.io.IOException;
import java.io.Serializable;

import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.MsControlFactory;
import javax.media.mscontrol.networkconnection.SdpPortManagerException;
import javax.media.mscontrol.spi.Driver;
import javax.media.mscontrol.spi.DriverManager;
import javax.servlet.ServletException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSessionsUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.DlgcDemoProperty;
import testing.bridge_conference.DlgcBridgeConference;




/**
 * Servlet answering SIP conference service incoming calls
 */

public class DlgcEMBridgeServlet extends SipServlet implements Serializable, SipApplicationSessionListener, SipServletListener {

	private static final long serialVersionUID = 1;
	static  DlgcDemoProperty			demoPropertyObj;


	public final static boolean DELAY_JOIN_TEST = false;   //set to false if you want to do cut-thru early media else set it to true

	/****
	 * public final static String MENU_EMBRIDGE_00 = "file:////var/lib/xms/media/en_US/verification/snow/prompts/menuEmBridge00.wav";

	public final static String MENU_EMBRIDGE_77 = "file:////var/lib/xms/media/en_US/verification/snow/prompts/menuEmBridge77.wav";
	public final static String MENU_EMBRIDGE_88_BRIDGE_MODE = "file:////var/lib/xms/media/en_US/verification/snow/prompts/menuEmBridge88_bridgeMode.wav";
	public final static String MENU_EMBRIDGE_88_FULL_MIXER_MODE = "file:////var/lib/xms/media/en_US/verification/snow/prompts/menuEmBridge88_FullMixerMode.wav";
	 *****/

	public final static String MENU_EMBRIDGE_00 = "file:////var/lib/xms/media/en_US/verification/demoJSR309/EarlyMediaBridge/intro.wav"; 
	public final static String MENU_EMBRIDGE_77 = "file:////var/lib/xms/media/en_US/verification/demoJSR309/EarlyMediaBridge/how_it_works.wav";
	public final static String MENU_EMBRIDGE_88_BRIDGE_MODE = "file:////var/lib/xms/media/en_US/verification/demoJSR309/EarlyMediaBridge/bridge_conf.wav";
	public final static String MENU_EMBRIDGE_88_FULL_MIXER_MODE = "file:////var/lib/xms/media/en_US/verification/demoJSR309/EarlyMediaBridge/mix_conf.wav";





	// Flag used to verify the servlet is initialized and ready to take calls
	protected Boolean isReady = false;
	// Common factory MSC objects used by all service classes
	public transient static MsControlFactory theMediaSessionFactory;

	//public static TimerService timerService;
	private static Logger log = LoggerFactory.getLogger(DlgcEMBridgeServlet.class);

	private transient static String dlgcDriverName = "com.dialogic.dlg309";

	static SipServlet instance=null;

	public transient SipSessionsUtil sipSessionsUtil;
	public DlgcEMBridgeConference bridgeConference = null;
	public static SipFactory sip289factory = null;
	public static SipApplicationSession appSipSession= null;
	public static String sasSipId=null;
	public static String sipServletHandler=null;

	static String	applicationUserNamePropertyId = "dlgEmBridge";
	static String	applicationDisplayNamePropertyId = "dlgEmBridge";
	static String   applicationUserEarlyUriPropertyId ="user.uri.early";
	static String   applicationUserWebRtcUriPropertyId ="user.uri.webrtc";
	private final String	applicationSipAddressPropertyId = "DlgcEarlyMediaBridgeDemo.sip.address";
	private final String	applicationSipPortPropertyId = "DlgcEarlyMediaBridgeDemo.sip.port";
	private final String	applicationSipTOAAddressPropertyId = "DlgcEarlyMediaBridgeDemo.toa.sip.address";
	private final String	applicationSipTOAPortPropertyId = "DlgcEarlyMediaBridgeDemo.toa.sip.port";
	private final String	applicationSipTOAUsername = "DlgcEarlyMediaBridgeDemo.toa.sip.username";

	String 	emApplicationIpAddrStr = null;
	int 	emApplicationPortInt = 0	;
	String 	externalSipTOAIpAddrStr = null;
	static public String  propertyEarlyUserURI = null;
	static public String propertyWebRtcUserURI = null;
	int 	externalSipTOAPortInt = 0;
	String  externalSipTOAUsername = null;
	protected boolean dlgcSipServletLoaded =false;

	protected String platform = null; 
	protected Boolean emBridgeServletInitCalled = false;
	protected static final String ORACLE_PLATFORM = "ORACLE";
	protected static final String TELESTAX_PLATFORM    = "TELESTAX";
	protected static final String TROPO_PLATFORM    = "TROPO";
	private boolean servletInitializedFlag = false;
	static public boolean apiSyncModeProperty =false;

	

	public void servletInitialized(SipServletContextEvent evt){

		String sName = evt.getSipServlet().getServletName();


		if ( ( platform != null ) && (platform.compareToIgnoreCase(TELESTAX_PLATFORM) == 0 ) || ( platform != null ) && (platform.compareToIgnoreCase(ORACLE_PLATFORM) == 0 )) {
			if( sName.equalsIgnoreCase("DlgcSipServlet") )
			{
				dlgcSipServletLoaded = true;
				log.debug(" DlgcEMBridgeServlet::servletInitialized DlgcSipServlet loaded");			
			}

			else if( sName.equalsIgnoreCase("DlgcEMBridgeServlet") ) 
				emBridgeServletInitCalled =true;

			if( emBridgeServletInitCalled && dlgcSipServletLoaded)
			{
				if ( servletInitializedFlag == false ) {
					log.debug("Entering DlgcEMBridgeServlet::servletInitialized servletName: " + sName);			
					servletInitializedFlag = true;
					synchronized (isReady)
					{
						try {
							try
							{
								Driver dlgcDriver = (Driver) DriverManager.getDriver(dlgcDriverName);
								theMediaSessionFactory = dlgcDriver.getFactory(null);
								demoPropertyObj = new DlgcDemoProperty(this.getClass());
								sip289factory = (SipFactory)getServletContext().getAttribute(SipServlet.SIP_FACTORY);
								appSipSession = DlgcEMBridgeServlet.sip289factory.createApplicationSession();
								sasSipId = appSipSession.getId();
								sipServletHandler = this.getServletName();
								initApplicationSipProperties();
								instance = this;
							}
							catch (Exception e)
							{
								throw new ServletException(e);
							}

							sipSessionsUtil = (SipSessionsUtil) getServletContext().getAttribute(SIP_SESSIONS_UTIL);



						} catch (Exception e) {
							log.error("Error in servletInitialized",e.toString());
							e.printStackTrace();
						}
						isReady = Boolean.TRUE;
					}

				} else {
					log.debug("DlgcEMBridgeServlet::servletInitialized(): already servletInitialized was called...debouncing " + sName);
				}
			}

		} else { //TROPO Framework

			if( sName.equalsIgnoreCase("DlgcSipServlet") )
			{
				dlgcSipServletLoaded = true;
				log.debug(" DlgcEMBridgeServlet::servletInitialized DlgcSipServlet loaded");			
				return;
			}

			if( (sName.equalsIgnoreCase("DlgcEMBridgeServlet") ) && dlgcSipServletLoaded)
			{
				if ( servletInitializedFlag == false ) {
					log.debug("Entering DlgcEMBridgeServlet::servletInitialized servletName: " + sName);			
					servletInitializedFlag = true;
					synchronized (isReady)
					{
						try {
							try
							{
								Driver dlgcDriver = (Driver) DriverManager.getDriver(dlgcDriverName);
								theMediaSessionFactory = dlgcDriver.getFactory(null);
								demoPropertyObj = new DlgcDemoProperty(this.getClass());
								sip289factory = (SipFactory)getServletContext().getAttribute(SipServlet.SIP_FACTORY);
								appSipSession = DlgcEMBridgeServlet.sip289factory.createApplicationSession();
								sasSipId = appSipSession.getId();
								sipServletHandler = this.getServletName();
								initApplicationSipProperties();
								instance = this;
							}
							catch (Exception e)
							{
								throw new ServletException(e);
							}

							sipSessionsUtil = (SipSessionsUtil) getServletContext().getAttribute(SIP_SESSIONS_UTIL);

						} catch (Exception e) {
							log.error("Error in servletInitialized",e.toString());
							e.printStackTrace();
						}
						isReady = Boolean.TRUE;
					}

				} else {
					log.debug("DlgcEMBridgeServlet::servletInitialized(): already servletInitialized was called...debouncing " + sName);
				}
			}
		}
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
		log.info("APPSERVER_PLATFORM set to: " + platform);
		return platform;
	}

	private void initApplicationSipProperties() throws MsControlException 
	{
		emApplicationIpAddrStr = demoPropertyObj.getProperty(applicationSipAddressPropertyId);
		if ( emApplicationIpAddrStr == null ) {
			String msg = "Dialogic Early Media Bridge Conference DEMO invalid Application IP Address - please enter this property in the property file";
			log.error(msg);
			throw ( new MsControlException(msg) );
		}

		String appPort = demoPropertyObj.getProperty(applicationSipPortPropertyId);
		if ( appPort == null) {
			String msg = "Dialogic Early Media Bridge Conference DEMO invalid Port value - please enter this property in the property file";
			log.error(msg);
			throw ( new MsControlException(msg) );
		} else 
			emApplicationPortInt = Integer.valueOf(appPort);	

		externalSipTOAIpAddrStr = demoPropertyObj.getProperty(applicationSipTOAAddressPropertyId);
		if ( externalSipTOAIpAddrStr == null ) {
			String msg ="Dialogic Early Media Bridge Conference DEMO invalid Out Bound Call End Point IP Address - please enter this property in the property file";
			throw ( new MsControlException(msg) );
		}

		String externalSipTOAPort = demoPropertyObj.getProperty(applicationSipTOAPortPropertyId);
		if ( externalSipTOAPort == null) {
			String msg ="Dialogic Early Media Bridge Conference DEMO Out Bound Call End Point invalid Port value - please enter this property in the property file";
			log.error(msg);
			throw ( new MsControlException(msg) );
		} else 
			externalSipTOAPortInt = Integer.valueOf(externalSipTOAPort);	

		externalSipTOAUsername = demoPropertyObj.getProperty(applicationSipTOAUsername);
		if ( externalSipTOAUsername == null ) {
			String msg = "Dialogic Early Media Bridge Conference DEMO invalid Out Bound Call End Point TUA Username - please enter this property in the property file";
			log.error(msg);
			throw ( new MsControlException(msg) );
		}


		propertyEarlyUserURI = demoPropertyObj.getProperty(applicationUserEarlyUriPropertyId);
		if ( propertyEarlyUserURI != null ) {
			log.debug("Dialogic Early Media Bridge Conference DEMO using user URI early= " + propertyEarlyUserURI );
		}


		propertyWebRtcUserURI = demoPropertyObj.getProperty(applicationUserWebRtcUriPropertyId);
		if ( propertyWebRtcUserURI != null ) {
			log.debug("Dialogic Early Media Bridge Conference DEMO using user  URI webrtc= " + propertyWebRtcUserURI );
		}
		
		String apiSyncMode = demoPropertyObj.getProperty("demos.api.sync.mode");
		if ( apiSyncMode != null ) {
			if ( apiSyncMode.compareToIgnoreCase("true") ==0) {
				apiSyncModeProperty = true;
				log.debug("Dialogic Early Media Bridge Conference DEMO using user  API synchronous mode true" );
			}else {
				apiSyncModeProperty = false;
				log.debug("Dialogic Early Media Bridge Conference DEMO using user  API synchronous mode false" );
			}
		}
	}

	/**
	 * Process incoming INVITE request
	 */
	@Override
	protected void doInvite(SipServletRequest req) throws ServletException,	IOException {
		synchronized (isReady)
		{
			bridgeConference = new DlgcEMBridgeConference(this);
			// Verify the servlet has finished initialization
			if (isReady == false)
			{

				req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
				return;
			}
		}	

		bridgeConference.addOUAParticipant(req);			//create OUA  Leg


	}

	/**
	 * Process response 
	 */
	@Override
	protected void doResponse(SipServletResponse response)
	throws ServletException, IOException
	{
		log.debug("["+response.getSession().getCallId()+"] RECEIVED RESPONSE: "+response.getStatus()+" responding to "+response.getMethod());
		log.debug("RECEIVED RESPONSE: "+response.toString());
		String method = response.getMethod();
		if (method.equals("INVITE"))
		{
			if (response.getStatus() == SipServletResponse.SC_OK)
			{
				DlgcEMBridgeParticipant tuaParticipant = (DlgcEMBridgeParticipant)response.getSession().getAttribute("PARTICIPANT");
				log.debug("Participant name = " + tuaParticipant.myName);

				if ( tuaParticipant != null ) {
					DlgcEMBridgeParticipant ouaParticipant =  tuaParticipant.bridgePartner;

					try {
						byte[] sdp = ouaParticipant.nc.getSdpPortManager().getMediaServerSessionDescription();
						String printSdp1 = new String(sdp);
						log.debug("Test Print Debug - oua SDP 1 = " + printSdp1);
						log.debug("TUA Participant name = " + ouaParticipant.myName);
						SipServletRequest invReq = (SipServletRequest)ouaParticipant.mySipSession.getAttribute("INITIAL_INVITE_REQUEST");
						SipServletResponse resp200ok = invReq.createResponse(SipServletResponse.SC_OK);
						resp200ok.setContent (sdp, "application/sdp");
						String printSdp2 = new String(ouaParticipant.msSdpToSave);
						//log.debug("Test Print Debug - oua SDP 2 = " + printSdp2);
						log.debug("KAPANGA [7] OUT => 200 OK to  Participant Name =" + ouaParticipant.myName + "OFFER WITH SDP " + printSdp2);
						//resp200ok.setContent(ouaParticipant.msSdpToSave, "application/sdp");
						resp200ok.send();

						//note this next call triggers processSdpAnswer(tuaSDP) request to the connector thus sending the SDP
						//in the ack to the media server.  Note we don't wait for the SdpPortManagerEvent.ANSWER_PROCESSED event
						//that is generated by the connector due to the processSdpAnswerto send the 200 OK to the oua as done above.
						tuaParticipant.externalTua.handle200Response(response);
						this.bridgeConference.enableDigitDetectionsOnAllLegs();
					} catch (SdpPortManagerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MsControlException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


				} else {
					log.info("doResponse INVITE:: 200 OK:: INFO tuaParticipant is null") ;
				}
			} else if (response.getStatus() == SipServletResponse.SC_SESSION_PROGRESS) { //183
				DlgcEMBridgeParticipant aParticipant = (DlgcEMBridgeParticipant)response.getSession().getAttribute("PARTICIPANT");
				if ( aParticipant != null ) {
					DlgcEMBridgeParticipant tuaParticipant =  aParticipant.bridgePartner;
					log.debug("TUA Participant name = " + tuaParticipant.myName);
					tuaParticipant.externalTua.handle183Response(response);
				} else {
					log.info("doResponse INVITE:: SESSION_PROGRESS:: INFO tuaParticipant is null") ;
				}
			} 
		}  else if (method.equals("BYE"))
		{
			log.debug("Ingore BYE 200 OK");
		}		

	}


	//	/**
	//	 * Process incoming ACK
	//	 */
	@Override
	protected void doAck(SipServletRequest req) throws ServletException,IOException 
	{
		log.debug("Inside DlgcEMBridgeServlet::doAck");
		//if DELAY_JOIN then call join here
		if ( DELAY_JOIN_TEST ) {
			log.debug("Excecuting delay join test");
			DlgcEMBridgeParticipant participant = (DlgcEMBridgeParticipant)req.getSession().getAttribute("PARTICIPANT");
			bridgeConference.bridgeLeg(participant);
		}
	}



	/**
	 * Process incoming CANCEL request
	 */
	@Override
	public void doCancel(final SipServletRequest arg0)
	throws ServletException,IOException
	{
		log.info("["+arg0.getCallId()+"] RECEIVED CANCEL REQUEST");	
		// Send 200 OK in response to CANCEL request
		arg0.createResponse(SipServletResponse.SC_OK).send();
		if (arg0.getSession().isValid()) {
			if ( bridgeConference.getState() == DlgcEMBridgeConference.ConferenceState.IN_CONF )
				bridgeConference.destroyActiveConference( arg0.getSession());
			bridgeConference =null;
		}

		log.info("["+arg0.getCallId()+"] RECEIVED CANCEL REQUEST [DONE]");
	}

	/**
	 * Process incoming BYE request
	 */
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,IOException {
		if ( bridgeConference.getState() == DlgcEMBridgeConference.ConferenceState.IN_CONF ) {
			request.createResponse(SipServletResponse.SC_OK).send();
			bridgeConference.destroyActiveConference(request.getSession());
			bridgeConference =null;
		}
	}




	@Override
	public void sessionCreated(SipApplicationSessionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionDestroyed(SipApplicationSessionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionExpired(SipApplicationSessionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionReadyToInvalidate(SipApplicationSessionEvent arg0) {
		// TODO Auto-generated method stub

	}






}


