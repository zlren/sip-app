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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.media.mscontrol.MediaConfigException;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.MsControlFactory;
import javax.media.mscontrol.resource.video.VideoLayout;
import javax.media.mscontrol.spi.Driver;
import javax.media.mscontrol.spi.DriverManager;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;

import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.DlgcDemoProperty;
import testing.reference_conf_demo_with_outb_call_layout.DlgcOutbCallConferenceStorage.DlgcConferenceStorageMgrException;
import testing.reference_conf_demo_with_outb_call_layout.DlgcReferenceConferenceConstantsWOBC;



public class DlgcReferenceConferenceWithOutBCallServlet extends SipServlet implements Serializable, SipServletListener {

	private static final long serialVersionUID = 1L;
	static  DlgcDemoProperty			demoPropertyObj;



	// Flag used to verify the Servlet is initialized and ready to take calls
	protected Boolean isReady = false;



	public static DlgcReferenceConferenceWithOutBCallServlet instance;

	// Common factory MSC objects used by all service classes
	public transient static MsControlFactory theMediaSessionFactory;
	public static SipFactory sip289factory = null;

	private static Logger log = LoggerFactory.getLogger(DlgcReferenceConferenceWithOutBCallServlet.class);

	private transient static String dlgcDriverName = "com.dialogic.dlg309";

	static String	applicationUserNamePropertyId = "dlgRefConfDemoWithOutBoundCall";

	static String	applicationDisplayNamePropertyId = "dlgRefConfDemoWithOutBoundCall";
	String 	emApplicationIpAddrStr = null;
	int 	emApplicationPortInt = 0	;
	String 	externalSipTOAIpAddrStr = null;
	static public String  propertyEarlyUserURI = null;
	static public String propertyWebRtcUserURI = null;
	int 	externalSipTOAPortInt = 0;
	String  externalSipTOAUsername = null;

	transient SipSessionsUtil sipSessionsUtil;
	transient DlgcOutbCallConferenceStorage conferenceStorage = null;
	DlgcReferenceConferenceWOBC referenceConferenceWOBC =null;
	private final String	applicationSipAddressPropertyId = "DlgcAvLayoutConferenceDemo.sip.address";
	private final String	applicationSipPortPropertyId = "DlgcAvLayoutConferenceDemo.sip.port";
	private final String	applicationSipTOAAddressPropertyId = "DlgcAvLayoutConferenceDemo.toa.sip.address";
	private final String	applicationSipTOAPortPropertyId = "DlgcAvLayoutConferenceDemo.toa.sip.port";
	private final String	applicationSipTOAUsername = "DlgcAvLayoutConferenceDemo.toa.sip.username";

	static String   applicationUserEarlyUriPropertyId ="user.uri.early";
	static String   applicationUserWebRtcUriPropertyId ="user.uri.webrtc";

	private final String	videoLayoutFileAttributeId = "DlgcAvLayoutConferenceDemo.media.mixer.video.layout";

	private String videoLayoutFileName =null;
	
	private Map<String, String > videoLayoutFileMap;

	public static String sipServletHandler=null;

	static public final String MSML_LAYOUT_MIME = "application/dlgcsmil+xml";


	protected Boolean dlgcReferenceConferenceWithOutBCallServletInitCalled = false;
	private boolean servletInitializedFlag = false;
	protected boolean dlgcSipServletLoaded =false;
	
	static public boolean apiSyncModeProperty =false;

	//Call once upon the container loading this Servlet
	//Initialize JSR309 Factory and other attributes
	@Override
	public void init() throws ServletException {

		synchronized (isReady)
		{
			try {
				super.init();								
			} catch (Exception e) {
				throw new ServletException(
						"Cannot initialize ConferenceServlet due to internale service error",
						e);
			}					
			// Servlet is ready to take calls
			isReady = Boolean.TRUE;
		}
	}

	VideoLayout getVideoLayout(String vStyle)
	{
		VideoLayout  vl = null;
		
		String videoLayoutFileName = videoLayoutFileMap.get(vStyle);
	    
		log.info("Videolay anyActiveStream = "+VideoLayout.anyActiveStream+
				"Videolay anyStream = "+VideoLayout.anyStream+
				"Videolay MostActiveStream = "+VideoLayout.mostActiveStream);

		
		if ( videoLayoutFileName == null ) {
			log.debug("Sorry cant get video layout since end user video layout filename not given in property file");
			return null;
		} else {
			FileInputStream fis;
			try {
				fis = new FileInputStream(videoLayoutFileName);
				InputStreamReader in = new InputStreamReader(fis, "UTF-8");
				try {
					vl = theMediaSessionFactory.createVideoLayout(MSML_LAYOUT_MIME, in);
				} catch (MediaConfigException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				log.debug("Fail to create video layout :"+e.getMessage());
			} catch (IOException ioex) {
				ioex.printStackTrace();
			}

		}
		return vl;

	}

	/**
	 * Process incoming INVITE request
	 */
	@Override
	protected void doInvite(SipServletRequest req) throws ServletException,	IOException {
		synchronized (isReady)
		{


			try {
				SipApplicationSession sas = DlgcOutbCallConferenceStorage.loadSas();
				conferenceStorage = (DlgcOutbCallConferenceStorage) sas.getAttribute("DlgcOutbCallConferenceStorage");
				referenceConferenceWOBC = conferenceStorage.getConferenceInfo();

				if ( referenceConferenceWOBC == null ) {
					referenceConferenceWOBC =  DlgcReferenceConferenceWOBC.createNewConference(this,conferenceStorage);
				}	
				if ( referenceConferenceWOBC != null ) {
					log.debug("referenceConferenceWOBC is valid execute doInvite request.");
					referenceConferenceWOBC.addNewParticipant(req,conferenceStorage);
					try {
						conferenceStorage.saveConference(referenceConferenceWOBC);
						req.getSession().setAttribute("DlgcOutbCallConferenceStorage", conferenceStorage);
					} catch (DlgcConferenceStorageMgrException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else {
					log.error("referenceConferenceWOBC is NULL can execute doInvite request.");
				}

			} catch (DlgcConferenceStorageMgrException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	@Override
	protected void doAck(SipServletRequest request) {
		try {

			conferenceStorage = (DlgcOutbCallConferenceStorage) request.getSession().getAttribute("DlgcOutbCallConferenceStorage");
			referenceConferenceWOBC = conferenceStorage.getConferenceInfo();
			if ( referenceConferenceWOBC != null ) 
				referenceConferenceWOBC.doAck(request);	

			try {
				conferenceStorage.saveConference(referenceConferenceWOBC);
				request.getSession().setAttribute("DlgcOutbCallConferenceStorage", conferenceStorage);
			} catch (DlgcConferenceStorageMgrException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (DlgcConferenceStorageMgrException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}


	/**
	 * Only called when Outbound call 180 or 200 response
	 */
	@Override
	protected void doResponse(SipServletResponse response)
	throws ServletException, IOException
	{
		try {
			SipApplicationSession sas = DlgcOutbCallConferenceStorage.loadSas();
			conferenceStorage = (DlgcOutbCallConferenceStorage) sas.getAttribute("DlgcOutbCallConferenceStorage");
			referenceConferenceWOBC = conferenceStorage.getConferenceInfo();
			if ( referenceConferenceWOBC != null ) {
				if (response.getMethod().equals("INVITE"))
				{
					if (response.getStatus() == SipServletResponse.SC_OK)
					{
						referenceConferenceWOBC.doInviteResponse(response);		//this 200 OK this only comes if we are doing outbound calls
					}
				}
				else if (response.getMethod().equals("BYE"))
				{
					if (response.getStatus() == SipServletResponse.SC_OK)
					{
						referenceConferenceWOBC.removeParticipant(response.getRequest().getSession());
					}
				}		
				try {
					conferenceStorage.saveConference(referenceConferenceWOBC);
					response.getSession().setAttribute("DlgcOutbCallConferenceStorage", conferenceStorage);

				} catch (DlgcConferenceStorageMgrException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				log.error("doResponse cant get reference conference Object");
			}
		} catch (DlgcConferenceStorageMgrException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		try {
			conferenceStorage = (DlgcOutbCallConferenceStorage) arg0.getSession().getAttribute("DlgcOutbCallConferenceStorage");
			referenceConferenceWOBC = conferenceStorage.getConferenceInfo();
			if ( referenceConferenceWOBC != null ) {
				// Send 200 OK in response to CANCEL request
				arg0.createResponse(SipServletResponse.SC_OK).send();
				if (arg0.getSession().isValid()) {
					referenceConferenceWOBC.removeParticipant(arg0.getSession());
					arg0.getSession().invalidate();
				}
				if (arg0.getApplicationSession().isValid()) {
					arg0.getApplicationSession().invalidate();
				}
				log.info("["+arg0.getCallId()+"] RECEIVED CANCEL REQUEST [DONE]");
				try {
					conferenceStorage.saveConference(referenceConferenceWOBC);
					arg0.getSession().setAttribute("DlgcOutbCallConferenceStorage", conferenceStorage);
				} catch (DlgcConferenceStorageMgrException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				log.error("doCancel:: error getting reference conference object");
			}
		} catch (DlgcConferenceStorageMgrException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	/**
	 * Process incoming BYE request
	 */
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,IOException {

		boolean confReleased = false;
		try {
			conferenceStorage = (DlgcOutbCallConferenceStorage) request.getSession().getAttribute("DlgcOutbCallConferenceStorage");

			referenceConferenceWOBC = conferenceStorage.getConferenceInfo();
			if ( referenceConferenceWOBC != null ) {

				request.createResponse(SipServletResponse.SC_OK).send();

				//referenceConference.removeParticipant(request.getSession());

				/* BEGIN testing purposes */
				//if ( ( this.referenceConference.getNumberOfParticipants() > 0 ) && (this.referenceConference.mixerRdy == true ) ){
				//	log.debug("doBye:: releasing conference mixer.");
				//	this.referenceConference.confMediaMixer.release();
				//	this.referenceConference = null;
				//	this.referenceConference.mixerRdy = false;
				//}
				/* END testing purposes */

				confReleased = referenceConferenceWOBC.removeParticipant(request.getSession());


				try {
					if ( confReleased == false ) {
						conferenceStorage.saveConference(referenceConferenceWOBC);
						request.getSession().setAttribute("DlgcOutbCallConferenceStorage", conferenceStorage);
					}
				} catch (DlgcConferenceStorageMgrException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//if ( request.getSession().isValid() )
				//	request.getSession().getApplicationSession().setAttribute("conference", referenceConference);
			}else {
				log.error("doCancel:: error getting reference conference object");
			}
		} catch (DlgcConferenceStorageMgrException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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

	public static SipSessionsUtil  getSSU()
	{
		SipSessionsUtil ssu = (SipSessionsUtil) instance.getServletContext().getAttribute(SIP_SESSIONS_UTIL);
		return ssu;
	}


	private void initApplicationSipProperties() throws MsControlException 
	{
		emApplicationIpAddrStr = demoPropertyObj.getProperty(applicationSipAddressPropertyId);
		if ( emApplicationIpAddrStr == null ) {
			String msg = "Dialogic Conference DEMO invalid Application IP Address - please enter this property in the property file";
			log.error(msg);
			throw ( new MsControlException(msg) );
		}

		String appPort = demoPropertyObj.getProperty(applicationSipPortPropertyId);
		if ( appPort == null) {
			String msg = "Dialogic Conference DEMO invalid Port value - please enter this property in the property file";
			log.error(msg);
			throw ( new MsControlException(msg) );
		} else {
			emApplicationPortInt = Integer.valueOf(appPort);	
		}

		externalSipTOAIpAddrStr = demoPropertyObj.getProperty(applicationSipTOAAddressPropertyId);
		if ( externalSipTOAIpAddrStr == null ) {
			String msg ="DlgcReferenceConferenceWithOutBCallServlet DEMO invalid Out Bound Call End Point IP Address - please enter this property in the property file";
			throw ( new MsControlException(msg) );
		}

		String externalSipTOAPort = demoPropertyObj.getProperty(applicationSipTOAPortPropertyId);
		if ( externalSipTOAPort == null) {
			String msg ="DlgcReferenceConferenceWithOutBCallServlet DEMO Out Bound Call End Point invalid Port value - please enter this property in the property file";
			log.error(msg);
			throw ( new MsControlException(msg) );
		} else 
			externalSipTOAPortInt = Integer.valueOf(externalSipTOAPort);	

		externalSipTOAUsername = demoPropertyObj.getProperty(applicationSipTOAUsername);
		if ( externalSipTOAUsername == null ) {
			String msg = "DlgcReferenceConferenceWithOutBCallServlet DEMO invalid Out Bound Call End Point TUA Username - please enter this property in the property file";
			log.error(msg);
			throw ( new MsControlException(msg) );
		}


		//last two attributes are not used in this demo
		propertyEarlyUserURI = demoPropertyObj.getProperty(applicationUserEarlyUriPropertyId);
		if ( propertyEarlyUserURI != null ) {
			log.debug("DlgcReferenceConferenceWithOutBCallServlet DEMO using user URI early= " + propertyEarlyUserURI );
		}


		propertyWebRtcUserURI = demoPropertyObj.getProperty(applicationUserWebRtcUriPropertyId);
		if ( propertyWebRtcUserURI != null ) {
			log.debug("DlgcReferenceConferenceWithOutBCallServlet DEMO using user  URI webrtc= " + propertyWebRtcUserURI );
		}

		videoLayoutFileName = demoPropertyObj.getProperty(videoLayoutFileAttributeId);
		if ( videoLayoutFileName == null) {
			log.debug("VideoLayout Property Filename not specify in property file");
		} else {
			log.debug("VideoLayout Property Filename: " + videoLayoutFileName);
		}
		
		videoLayoutFileMap = new HashMap<String,String>();
		
		String nVLayoutFiles = demoPropertyObj.getProperty(videoLayoutFileAttributeId+".filesize");
		
		if (nVLayoutFiles!=null)
		{
			
			for (Integer i = 0; i< Integer.parseInt(nVLayoutFiles); i++)
			{
				String key = "*7"+i.toString();;
				videoLayoutFileMap.put(key, demoPropertyObj.getProperty(videoLayoutFileAttributeId+".file."+i.toString()));
			}
		}
		
		String apiSyncMode = demoPropertyObj.getProperty("demos.api.sync.mode");
		if ( apiSyncMode != null ) {
			if ( apiSyncMode.compareToIgnoreCase("true") ==0) {
				apiSyncModeProperty = true;
				log.debug("DlgcReferenceConferenceWithOutBCallServlet DEMO using user  API synchronous mode true" );
			}else {
				apiSyncModeProperty = false;
				log.debug("DlgcReferenceConferenceWithOutBCallServlet DEMO using user  API synchronous mode false" );
			}
		}
		
	}

	
	public void servletInitialized(SipServletContextEvent evt){

		String sName = evt.getSipServlet().getServletName();

		if( sName.equalsIgnoreCase("DlgcSipServlet") )
		{
			dlgcSipServletLoaded = true;
			log.debug(" DlgcReferenceConferenceWithOutBCallServlet::servletInitialized DlgcSipServlet loaded");			
		}
		else if( sName.equalsIgnoreCase("DlgcReferenceConferenceWithOutBCallServlet") ) 
			dlgcReferenceConferenceWithOutBCallServletInitCalled =true;
		if( dlgcReferenceConferenceWithOutBCallServletInitCalled && dlgcSipServletLoaded)
		{
			if ( servletInitializedFlag == false ) {
				log.debug("Entering DlgcReferenceConferenceWithOutBCallServlet::servletInitialized servletName: " + sName);			
				servletInitializedFlag = true;
				synchronized (isReady)
				{
					try {
						try
						{
							instance =this;
							sip289factory = (SipFactory)getServletContext().getAttribute(SipServlet.SIP_FACTORY);
							Driver dlgcDriver = (Driver) DriverManager.getDriver(dlgcDriverName);
							theMediaSessionFactory = dlgcDriver.getFactory(null);
							demoPropertyObj = new DlgcDemoProperty(this.getClass());

							sipSessionsUtil = (SipSessionsUtil) getServletContext().getAttribute(SIP_SESSIONS_UTIL);

							conferenceStorage = new  DlgcOutbCallConferenceStorage( this );

							SipApplicationSession sas = DlgcOutbCallConferenceStorage.loadSas();
							sas.setAttribute("DlgcOutbCallConferenceStorage", conferenceStorage);

							sipServletHandler = this.getServletName();
							initApplicationSipProperties();

							if ( referenceConferenceWOBC == null ) {
								log.debug("DlgcReferenceConferenceServletWOBC::doInvite: creating conference object");
								//referenceConferenceWOBC = new DlgcReferenceConferenceWOBC(this);
								referenceConferenceWOBC =  DlgcReferenceConferenceWOBC.createNewConference(this,conferenceStorage);
								try {
									conferenceStorage.saveConference(referenceConferenceWOBC);
								} catch (DlgcConferenceStorageMgrException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}	
						}
						catch (Exception e)
						{
							throw new ServletException(e);
						}
						DlgcReferenceConferenceConstantsWOBC .recordConferenceSession = false;

					} catch (Exception e) {
						log.error("Error in servletInitialized",e.toString());
						e.printStackTrace();
					}					
					// Servlet is ready to take calls
					isReady = Boolean.TRUE;
				}
			} else {
				log.debug("DlgcReferenceConferenceWithOutBCallServlet::servletInitialized(): already servletInitialized was called...debouncing " + sName);
			}

		}
	}


}


