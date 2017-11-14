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
import java.util.Random;

import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.MsControlFactory;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.spi.Driver;
import javax.media.mscontrol.spi.DriverManager;
import javax.servlet.ServletException;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionsUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.DlgcDemoProperty;
import testing.DlgcTest;



/**
 * 
 * This unit test servlet illustrates how to implement a simple conference 
 * that does not required a mixer. That is two legs are joined 
 * into a bridge conference.
 * Set up your SIP phone to point to the Web Application Server. 
 * Configure the SIP phone address (i.e. user name) to DialogicBridgeDemo. 
 * Make sure that the Web Application Server is running the dlgmsc_tests.war 
 * application.
 * In the SIP phone, select your newly created test conference contact. Notice that you will need at least two SIP phones. The first connection entering the conference will not hear anything until the other legs join in.
 * This simple conference performs the following:
 * 	It basically joins nc.join(dir, nc2);
 * In order for the leg to enter the bridge, each leg must enter *03 
 * after making the call. To test the application, dial the following:
 * DialogicBridgeDemo@<OCCAS5-IP-ADDRESS>
 * 
 *  Note the application can use each Network Connection Media Session setAttribute
 *  ("BRIDGE_DTMF_CLAMP_CONTROL", ["off", "on"]) to control the leg dtmf clamping.
 *  Note the default value of each leg is to have clamp on.
 */

public class DlgcBridgeServlet extends SipServlet implements Serializable, SipApplicationSessionListener, SipServletListener {

	private static final long serialVersionUID = 3310009181818L; 
	private boolean servletInitializedFlag = false;


	// Flag used to verify the servlet is initialized and ready to take calls
	protected Boolean isReady = false;
	// Common factory MSC objects used by all service classes
	public transient static MsControlFactory theMediaSessionFactory;

	//public static TimerService timerService;
	private static Logger log = LoggerFactory.getLogger(DlgcBridgeServlet.class);

	private transient static String dlgcDriverName = "com.dialogic.dlg309";

	public transient SipSessionsUtil sipSessionsUtil;
	public DlgcBridgeConference bridgeConference = null;
	static protected DlgcDemoProperty	demoPropertyObj;
	protected boolean dlgcSipServletLoaded =false;

	protected String platform = null; 
	protected Boolean bridgeServletInitCalled = false;
	protected static final String ORACLE_PLATFORM = "ORACLE";
	protected static final String TELESTAX_PLATFORM    = "TELESTAX";
	protected static final String TROPO_PLATFORM    = "TROPO";


	
	public void servletInitialized(SipServletContextEvent evt){

		String sName = evt.getSipServlet().getServletName();


		if ( ( platform != null ) && (platform.compareToIgnoreCase(TELESTAX_PLATFORM) == 0 ) || ( platform != null ) && (platform.compareToIgnoreCase(ORACLE_PLATFORM) == 0 )) {
			if( sName.equalsIgnoreCase("DlgcSipServlet") )
			{
				dlgcSipServletLoaded = true;
				log.debug(" DlgcBridgeServlet::servletInitialized DlgcSipServlet loaded");			
			}

			else if( sName.equalsIgnoreCase("DlgcBridgeServlet") ) 
				bridgeServletInitCalled =true;

			if( bridgeServletInitCalled && dlgcSipServletLoaded)
			{
				if ( servletInitializedFlag == false ) {
					log.debug("Entering DlgcBridgeServlet::servletInitialized servletName: " + sName);			
					servletInitializedFlag = true;
					synchronized (isReady)
					{
						try {
							try
							{
								Driver dlgcDriver = (Driver) DriverManager.getDriver(dlgcDriverName);
								theMediaSessionFactory = dlgcDriver.getFactory(null);
								bridgeConference = new DlgcBridgeConference(this);
								demoPropertyObj = new DlgcDemoProperty(this.getClass());

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
					log.debug("DlgcBridgeServlet::servletInitialized(): already servletInitialized was called...debouncing " + sName);
				}
			}

		} else { //TROPO Framework

			if( sName.equalsIgnoreCase("DlgcSipServlet") )
			{
				dlgcSipServletLoaded = true;
				log.debug(" DlgcBridgeServlet::servletInitialized DlgcSipServlet loaded");			
				return;
			}

			if( (sName.equalsIgnoreCase("DlgcBridgeServlet") ) && dlgcSipServletLoaded)
			{
				if ( servletInitializedFlag == false ) {
					log.debug("Entering DlgcBridgeServlet::servletInitialized servletName: " + sName);			
					servletInitializedFlag = true;
					synchronized (isReady)
					{
						try {
							try
							{
								Driver dlgcDriver = (Driver) DriverManager.getDriver(dlgcDriverName);
								theMediaSessionFactory = dlgcDriver.getFactory(null);
								bridgeConference = new DlgcBridgeConference(this);
								demoPropertyObj = new DlgcDemoProperty(this.getClass());

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
					log.debug("DlgcBridgeServlet::servletInitialized(): already servletInitialized was called...debouncing " + sName);
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


			// Servlet is ready to take calls
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

	/**
	 * Process incoming INVITE request
	 */
	@Override
	protected void doInvite(SipServletRequest req) throws ServletException,	IOException {
		synchronized (isReady)
		{
			// Verify the servlet has finished initialization
			if (isReady == false)
			{
				req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
				return;
			}
		}	

		Random ran = new Random(10);
		Integer ri = ran.nextInt();
		bridgeConference.addNewParticipant(req, ri.toString());
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
				//bridgeConference.doResponse(response);
			}
		}
		else if (response.getMethod().equals("BYE"))
		{
			if (response.getStatus() == SipServletResponse.SC_OK)
			{
				bridgeConference.removeParticipant(response.getRequest().getSession());
			}
		}		
		//log.info("["+response.getSession().getCallId()+"] RECEIVED RESPONSE: "+response.getMethod()+" [DONE]");
	}

	//	/**
	//	 * Process incoming ACK
	//	 */
	@Override
	protected void doAck(SipServletRequest req) throws ServletException,IOException 
	{
		log.debug("Inside DlgcBridgeServlet::doAck");
		bridgeConference.processSipPhoneAck(req);		
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
			bridgeConference.removeParticipant(arg0.getSession());
			arg0.getSession().invalidate();
		}
		if (arg0.getApplicationSession().isValid()) {
			arg0.getApplicationSession().invalidate();
		}
		log.info("["+arg0.getCallId()+"] RECEIVED CANCEL REQUEST [DONE]");
	}

	/**
	 * Process incoming BYE request
	 */
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,IOException {
		boolean executeUnjoinTest = false;
		
		//CR47969 phytech unjoin bridge issue jan 2014  executeUnjoinTest=true
		if ( executeUnjoinTest ) {
			log.debug("DlgcBridgeServlet::doBye - applying unjoin test on both bridge leg...not typical... only for testing the unjoin operation");
			SipSession mySipSession = request.getSession();
			DlgcBridgeParticipant participant = (DlgcBridgeParticipant)mySipSession.getAttribute("PARTICIPANT");
			NetworkConnection thisLeg = participant.nc;
			
			log.debug("DlgcBridgeServlet::doBye - thisLeg = " + thisLeg.toString());
			
			Joinable[] arrJoinable;
			try {
				arrJoinable = thisLeg.getJoinees();
				int numSize = arrJoinable.length;

				for (int i = 0; i < numSize; i++) {
					Joinable j = arrJoinable[i];
					log.debug("DlgcBridgeServlet::doBye - Joinable = " + j.toString());
					thisLeg.unjoin(j);
					thisLeg.release();
				}
			} catch (MsControlException e) {
				log.debug("DlgcBridgeServlet::doBye - exception = " + e.toString());
				e.printStackTrace();
			}          

			
		} else {
			log.debug("DlgcBridgeServlet::doBye - applying normal release of leg");
			request.createResponse(SipServletResponse.SC_OK).send();
			bridgeConference.removeParticipant(request.getSession());
		}
	}

	public void sendBye(SipSession mySipSession) 
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


