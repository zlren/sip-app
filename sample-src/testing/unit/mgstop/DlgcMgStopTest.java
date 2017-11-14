/**
 * DIALOGIC CONFIDENTIAL
 * Copyright (C) 2005-2015 Dialogic Corporation. All Rights Reserved.
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

package testing.unit.mgstop;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.MsControlFactory;
import javax.media.mscontrol.networkconnection.NetworkConnection;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DlgcMgStopTest extends SipServlet implements Serializable, SipServletListener 
{
	private static final long serialVersionUID = 1L;
	boolean dlgcSipServletLoaded = false;
	String  platform = null;
	boolean dlgcMgStopTestInitCalled = false;
	boolean servletInitializedFlag =false;
	protected Boolean myServletLoaded = false;
	protected ServletConfig myCfg;


	static protected DlgcMgStopTestProperties	mgStopTestProperties;
	String targetMediaServer = null;
	
	protected static final String ORACLE_PLATFORM = "ORACLE";
	protected static final String TELESTAX_PLATFORM    = "TELESTAX";
	protected static final String TROPO_PLATFORM    = "TROPO";
	protected static String dlgcDriverName = "com.dialogic.dlg309";
	
	transient MsControlFactory mscFactory;
	transient protected Driver dlgcDriver =null;
	
	private static Logger log = LoggerFactory.getLogger(DlgcMgStopTest.class);

	
	private static final String testTypeID = 								"mg.stop.test.type";
	private static final String testTypeID_NO_RESOURCES_RUNNING = 			"NO_RESOURCES_RUNNING";
	private static final String testTypeID_PLAYER_ONLY_RUNNING =  			"PLAYER_ONLY_RUNNING";
	
	//MSC-129 Restcomm request to add mg.stop() Play + async DTMF Scenario
	private static final String testTypeID_PLAYER_DETECTOR_RUNNING =  		"PLAYER_DETECTOR_RUNNING";
	
	//MSC-129 Restcomm request to add mg.stop() PlayCollect
	private static final String testTypeID_PLAYER_COLLECT_RUNNING =  		"PLAYER_COLLECT_RUNNING";
	
	
	
	private static final String testTypeID_ONLY_DETECTOR_RUNNING =  		"ONLY_DETECTOR_RUNNING";
	private static final String testTypeID_RECORDER_ONLY_RUNNING =  		"RECORDER_ONLY_RUNNING";
	private static final String testTypeID_RECORDER_DECTECTOR_RUNNING =  	"RECORDER_DECTECTOR_RUNNING";
	
	private static  Boolean ENABLED_NO_RESOURCES_RUNNING = 			false;
	private static  Boolean ENABLED_PLAYER_ONLY_RUNNING =  			false;
	private static  Boolean ENABLED_PLAYER_DETECTOR_RUNNING =  		false;
	private static  Boolean ENABLED_ONLY_DETECTOR_RUNNING =  		false;
	private static  Boolean ENABLED_RECORDER_ONLY_RUNNING =  		false;
	private static  Boolean ENABLED_RECORDER_DECTECTOR_RUNNING =  	false;
	
	//MSC-129 Restcomm request to add mg.stop() PlayCollect
	private static  Boolean ENABLED_PLAYER_COLLECT_RUNNING =  		false;

	
	
	@Override
	public void init(ServletConfig cfg)
			throws ServletException
	{
		super.init(cfg);
		myServletLoaded = true;
		myCfg = cfg;
		myServletLoaded = false;
		mgStopTestProperties = new DlgcMgStopTestProperties(this.getClass());
		platform = this.getApplicationServerPlatform();
		loadTestCaseProperites(); 

		targetMediaServer = mgStopTestProperties.getProperty("mgstop.test.regex.ms");	
		if ( targetMediaServer != null)
			log.debug("targetMediaServer: " + targetMediaServer);
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
		}
		catch (Exception e)
		{
			log.error("Error in servletInitialized",e.toString());
			e.printStackTrace();
		}
	}
	
	protected void loadTestCaseProperites()
	{
		
		log.debug("Entering DlgMgStopTest::loadTestCaseProperites()");
		ENABLED_NO_RESOURCES_RUNNING = 				false;
		ENABLED_PLAYER_ONLY_RUNNING =  				false;
		ENABLED_PLAYER_DETECTOR_RUNNING =  			false;
		ENABLED_ONLY_DETECTOR_RUNNING =  			false;
		ENABLED_RECORDER_ONLY_RUNNING =  			false;
		ENABLED_RECORDER_DECTECTOR_RUNNING =  		false;
		
		//MSC-129 Restcomm request to add mg.stop() PlayCollect
		ENABLED_PLAYER_COLLECT_RUNNING		=		false;
		
		mgStopTestProperties = new DlgcMgStopTestProperties(this.getClass());
		String testType = mgStopTestProperties.getProperty(testTypeID);
		if ( testType != null ) {
			log.debug("DlgcMgStopTest::test type configure in demo property file set to:  " + testType);
			if ( testType.equalsIgnoreCase(testTypeID_NO_RESOURCES_RUNNING) ) {
				ENABLED_NO_RESOURCES_RUNNING = true;
			}else if ( testType.equalsIgnoreCase(testTypeID_PLAYER_ONLY_RUNNING) ) {
				ENABLED_PLAYER_ONLY_RUNNING = true;
			}else if ( testType.equalsIgnoreCase(testTypeID_PLAYER_DETECTOR_RUNNING) ) {
				ENABLED_PLAYER_DETECTOR_RUNNING = true;
			}else if ( testType.equalsIgnoreCase(testTypeID_ONLY_DETECTOR_RUNNING) ) {
				ENABLED_ONLY_DETECTOR_RUNNING = true;
			}else if ( testType.equalsIgnoreCase(testTypeID_RECORDER_ONLY_RUNNING) ) {
				ENABLED_RECORDER_ONLY_RUNNING = true;
			}else if ( testType.equalsIgnoreCase(testTypeID_RECORDER_DECTECTOR_RUNNING) ) {
				ENABLED_RECORDER_DECTECTOR_RUNNING = true;
			}else if ( testType.equalsIgnoreCase(testTypeID_PLAYER_COLLECT_RUNNING) ) {
				ENABLED_PLAYER_COLLECT_RUNNING = true;
			}else{
				log.debug("DlgcMgStopTest::test type configure in demo property file set to:  " + testType + " not supported defaulting to " + testTypeID_NO_RESOURCES_RUNNING);
				ENABLED_NO_RESOURCES_RUNNING = true;
			}
		}else {
			log.debug("DlgcMgStopTest::no test type configure in demo property file default to:  " + testTypeID_NO_RESOURCES_RUNNING);
			ENABLED_NO_RESOURCES_RUNNING = true;
		}
		log.debug("Leaving DlgMgStopTest::loadTestCaseProperites()");

	}

	@Override
	public void servletInitialized(SipServletContextEvent evt){

		String sName = evt.getSipServlet().getServletName();


		if ( ( platform != null ) && (platform.compareToIgnoreCase(TELESTAX_PLATFORM) == 0 ) || ( platform != null ) && (platform.compareToIgnoreCase(ORACLE_PLATFORM) == 0 )) 
		{
			if( sName.equalsIgnoreCase("DlgcSipServlet") )
			{
				dlgcSipServletLoaded = true;
				log.debug(" DlgcMgStopTest::servletInitialized DlgcSipServlet loaded");			
			} else if( sName.equalsIgnoreCase("DlgcMgStopTest") ) {
				dlgcMgStopTestInitCalled =true;
			}

			if( dlgcMgStopTestInitCalled && dlgcSipServletLoaded)
			{
				if ( servletInitializedFlag == false ) {
					log.debug("Entering DlgcPlayerTest::servletInitialized servletName: " + sName);			
					servletInitializedFlag = true;
					initDriver();
					myServletInitialized(evt);
				} else {
					log.debug("DlgcMgStopTest::servletInitialized(): already servletInitialized was called...debouncing " + sName);
				}
			}

		} else { //TROPO Framework

			if( sName.equalsIgnoreCase("DlgcSipServlet") )
			{
				dlgcSipServletLoaded = true;
				log.debug(" DlgcMgStopTest::servletInitialized DlgcSipServlet loaded");			
				return;
			}

			if( (sName.equalsIgnoreCase("DlgcMgStopTest") ) && dlgcSipServletLoaded)
			{
				if ( servletInitializedFlag == false ) {
					log.debug("Entering DlgcMgStopTest::servletInitialized servletName: " + sName);			
					servletInitializedFlag = true;
					initDriver();
					myServletInitialized(evt);
				} else {
					log.debug("DlgcPlayerTest::DlgcMgStopTest(): already servletInitialized was called...debouncing " + sName);
				}
			}
		}

	}
	
	protected void myServletInitialized(SipServletContextEvent x )
	{
		//tbd
	}

	//MSC-129 Restcomm request to add mg.stop() PlayCollect
	@Override
	public void doInvite(final SipServletRequest req) throws ServletException, IOException
	{
		log.debug("Entering DlgcMgStopTest doInvite()" ) ;
		DlgcMgStopBaseTestCase testCase = null;
		if (ENABLED_NO_RESOURCES_RUNNING) {
			log.debug("DlgcMgStopTest::doInvite request for NO RESOURCE RUNNING TEST SCENARIO");
			testCase = new DlgcMgStopNoResourceEnabledTest(this);
			//SipSession ss = req.getSession();
			//ss.setAttribute("TEST_CASE", testCase);
			testCase.invite(req);
		}else if ( ENABLED_PLAYER_ONLY_RUNNING ) {
			log.debug("DlgcMgStopTest::doInvite request for PLAYER ONLY RUNNING TEST SCENARIO");
			testCase = new DlgcMgStopPlayerOnlyEnabledTest(this);
			testCase.invite(req);
		}else if ( ENABLED_PLAYER_DETECTOR_RUNNING ) {
			log.debug("DlgcMgStopTest::doInvite request for PLAYER And Async Detector  RUNNING TEST SCENARIO");
			testCase = new DlgcMgStopAsyncDetectorEnabledTest(this);
			testCase.invite(req);
		}else if ( ENABLED_RECORDER_ONLY_RUNNING ) {
			log.debug("DlgcMgStopTest::doInvite request for Recorder RUNNING TEST SCENARIO");
			testCase = new DlgcMgStopRecorderEnabledTest(this);
			testCase.invite(req);
		}else if ( ENABLED_RECORDER_DECTECTOR_RUNNING ) {
			log.debug("DlgcMgStopTest::doInvite request for Detector  RUNNING TEST SCENARIO");
			testCase = new DlgcMgStopRecorderDetectorEnabledTest(this);
			testCase.invite(req);
		}else if ( ENABLED_ONLY_DETECTOR_RUNNING ) {
			log.debug("DlgcMgStopTest::doInvite request for Detector  RUNNING TEST SCENARIO");
			testCase = new DlgcMgStopDetectorEnabledTest(this);
			testCase.invite(req);
		}else if (ENABLED_PLAYER_COLLECT_RUNNING ) {
			log.debug("DlgcMgStopTest::doInvite request for Prompt and Collect (Play and Collect) RUNNING TEST SCENARIO");
			testCase = new DlgcMgStopPlayCollectEnabledTest(this);
			testCase.invite(req);
		} else {
			log.debug("DlgcMgStopTest::doInvite request for USING DEFAULT NO RESOURCE RUNNING TEST SCENARIO");
			testCase = new DlgcMgStopNoResourceEnabledTest(this);
			testCase.invite(req);
		}
		log.debug("Leaving DlgcMgStopTest doInvite()" ) ;

	}
	
	
	@Override
	public void doAck(SipServletRequest req)
	{
		log.debug("Entering  DlgcMgStopTest::doAck");
		SipSession ss = req.getSession();
		DlgcMgStopBaseTestCase testCase = (DlgcMgStopBaseTestCase) ss.getAttribute("TEST_CASE");
		if ( testCase != null) {
			testCase.ack(req);
		}else {
			log.error("DlgcMgStopTest.doResponse: testCase is NULL cant execute doAck");
		}
		log.debug("Leaving  DlgcMgStopTest::doAck");
	}
	
	@Override
	protected void doResponse(SipServletResponse response)
		throws IOException, ServletException
	{
		log.debug("Entering DlgcMgStopTest::doResponse()");
		SipSession ss = response.getSession();
		DlgcMgStopBaseTestCase testCase = (DlgcMgStopBaseTestCase) ss.getAttribute("TEST_CASE");
		if ( testCase != null) {
			testCase.response(response);
		}else {
			log.error("DlgcMgStopTest.doResponse: testCase is NULL cant execute doResponse");
		}
		log.debug("Leaving DlgcMgStopTest::doResponse()");
	}
	
	@Override
	public void doBye(final SipServletRequest req)
		throws ServletException, IOException
	{
		log.debug("Entering DlgcMgStopTest::doBye()");
		SipSession ss = req.getSession();
		DlgcMgStopBaseTestCase testCase = (DlgcMgStopBaseTestCase) ss.getAttribute("TEST_CASE");
		if ( testCase != null) {
			testCase.terminateSession(ss);
		}else {
			log.error("DlgcMgStopTest.doBye: testCase is NULL cant execute doBye");
		}
		log.debug("Leaving DlgcMgStopTest::doBye()");
			
	}

	protected String getApplicationServerPlatform()
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
	
}
