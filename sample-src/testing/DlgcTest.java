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

package testing;

import java.io.IOException;
import java.io.Serializable;

import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.MsControlFactory;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.spi.Driver;
import javax.media.mscontrol.spi.DriverManager;
import javax.media.mscontrol.spi.PropertyInfo;
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
import java.util.Properties;

/**
 *
 * @class DlgcTest
 * @brief Dialogic base test class.
 * 
 */
public abstract class DlgcTest extends SipServlet implements Serializable, SipServletListener { // 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static protected DlgcDemoProperty demoPropertyObj;
	
	String targetMediaServer = null;
	
	protected Boolean myServletLoaded = false;
	protected String platform = null;

	protected static final String ORACLE_PLATFORM = "ORACLE";
	protected static final String TELESTAX_PLATFORM = "TELESTAX";
	protected static final String TROPO_PLATFORM = "TROPO";

	protected boolean servletInitializedFlag = false;
	protected Boolean playerServletInitCalled = false;
	static protected Boolean dlgcSipServletLoaded = false;
	
	protected ServletConfig cfg;
	
	protected static String dlgcDriverName = "com.dialogic.dlg309";
	public MsControlFactory mscFactory;
//	transient protected DlgcSdpPortEventListener speListener;
	transient protected Driver dlgcDriver = null;
	
	private static Logger log = LoggerFactory.getLogger(DlgcTest.class);

	@Override
	public void init(ServletConfig cfg) throws ServletException {
		super.init(cfg);
		this.cfg = cfg;
		myServletLoaded = false;
		demoPropertyObj = new DlgcDemoProperty(this.getClass());
		platform = this.getWebServerPlatform();
		targetMediaServer = demoPropertyObj.getProperty("player.test.regex.ms");
		if (targetMediaServer != null) {
			log.debug("targetMediaServer: " + targetMediaServer);
		}
			
	}

	protected void myServletInitialized(SipServletContextEvent arg0) {

	}

	protected String getWebServerPlatform() {

		String platform = System.getenv("APPSERVER_PLATFORM");
		if (platform == null) {
			log.warn((new StringBuilder()).append("Environment Variable: ").append("APPSERVER_PLATFORM")
					.append(" not provided").toString());
			log.warn("Assuming OCCAS WEB Application Server Platform");
			platform = ORACLE_PLATFORM;
		}
		log.info("APPSERVER_PLATFORM set to: " + platform);
		return platform;
	}

	protected void initDriver() {
		try {
			dlgcDriver = DriverManager.getDriver(dlgcDriverName);

			if (targetMediaServer != null) {
				Properties factoryProperties = new Properties();
				factoryProperties.setProperty(MsControlFactory.MEDIA_SERVER_URI, targetMediaServer);
				mscFactory = dlgcDriver.getFactory(factoryProperties);
			} else {
				mscFactory = dlgcDriver.getFactory(null);
			}

			// mscFactory = dlgcDriver.getFactory(null);
//			speListener = new DlgcSdpPortEventListener();
		} catch (Exception e) {
			// throw new ServletException(e);
			log.error("Error in servletInitialized", e.toString());
			e.printStackTrace();
		}
	}

//	@Override
//	public void doBye(final SipServletRequest req) throws ServletException, IOException {
//		// req.createResponse(SipServletResponse.SC_OK).send();
//
//		NetworkConnection nc = (NetworkConnection) req.getSession().getAttribute("NETWORK_CONNECTION");
//		// log.debug("UUUUUUUUUUUUUUUU NC# " +
//		// DlgcConferenceTest.ncCount.toString() + " SAS= " +
//		// ((DlgcProxy)nc).getProxyId() + " UUUUUUUUU" );
//		// log.debug("UUUUUUUUUUUUUUUU NC# " +
//		// DlgcConferenceTest.ncCount.toString() + " SASOBJ = " +
//		// ((DlgcProxy)nc).getProxySAS() + " UUUUUU");
//		if (nc != null) {
//			nc.release();
//		}
//		req.createResponse(SipServletResponse.SC_OK).send();
//
//		releaseSession(req.getSession());
//	}

	
//	@Override
//	public void doInvite(final SipServletRequest req) throws ServletException, IOException {
//		log.info("doInvite");
//
//		NetworkConnection networkConnection = null;
//		SipSession sipSession = req.getSession();
//
//		if (req.isInitial()) {
//			// We have a new call.
//			try {
//				MediaSession mediaSession = mscFactory.createMediaSession();
//				networkConnection = mediaSession.createNetworkConnection(NetworkConnection.BASIC);
//
//				sipSession.setAttribute("MEDIA_SESSION", mediaSession);
//				sipSession.setAttribute("NETWORK_CONNECTION", networkConnection);
//
//				mediaSession.setAttribute("SIP_SESSION", sipSession);
//				mediaSession.setAttribute("SIP_REQUEST", req);
//				mediaSession.setAttribute("NETWORK_CONNECTION", networkConnection);
//
//				networkConnection.getSdpPortManager().addListener(speListener);
//
//			} catch (MsControlException e) {
//				req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
//			}
//		} else {
//			// Re-invite on existing call.
//			networkConnection = (NetworkConnection) sipSession.getAttribute("NETWORK_CONNECTION");
//		}
//
//		try {
//			req.getSession().setAttribute("UNANSWERED_INVITE", req);
//
//			byte[] remoteSdp = req.getRawContent();
//
//			if (remoteSdp == null) {
//				networkConnection.getSdpPortManager().generateSdpOffer();
//			} else {
//				networkConnection.getSdpPortManager().processSdpOffer(remoteSdp);
//			}
//		} catch (MsControlException e) {
//			req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
//		}
//	}

//	// fix IPY00091556
//	public void doCancel(final SipServletRequest req) throws ServletException, IOException {
//
//		MediaSession mediaSession = (MediaSession) req.getSession().getAttribute("MEDIA_SESSION");
//		if (mediaSession != null) {
//
//			// log.warn("Inside doBye. calling mediaSession release method");
//			mediaSession.release();
//			releaseSession(req.getSession());
//
//		} else {
//			// Session maybe previously terminated
//			log.warn("MEDIA_SESSION attribute does not exist in SIP Session");
//		}
//		req.createResponse(SipServletResponse.SC_OK).send();
//
//		releaseSession(req.getSession());
//	}
//
//	protected void releaseSession(SipSession session) {
//		// log.debug(" RRRRRRRRRRRRRRRRRRRR DlgcTest: Releasing session and SAS
//		// RRRRRRRRRRRRRRRRRRRR");
//		try {
//			session.invalidate();
//			session.getApplicationSession().invalidate();
//		} catch (Exception e) {
//		}
//	}
//
	public void printDriverInfo(SipServletRequest req) throws ServletException, IOException {
		log.info("doInvite");

		if (req.isInitial()) {
			// test driver getName API
			String driverName = dlgcDriver.getName();
			log.info("Driver Name = " + driverName);

			// test driver property info API
			PropertyInfo[] driverProperties = dlgcDriver.getFactoryPropertyInfo();

			log.info("PropertyInfo array size = " + (new Integer(driverProperties.length)));

			log.info("*********************************************************************");
			for (int i = 0; i < driverProperties.length; i++) {
				PropertyInfo pi = driverProperties[i];
				if (pi != null) {
					log.info(" ");
					log.info("Property Name:           " + pi.name);
					log.info("Property required:       " + (new Boolean(pi.required).toString()));
					log.info("Property Default Value:  " + pi.defaultValue);
					log.info("Property Description:	   " + pi.description);
					log.info("Property Choices: ");
					for (int s = 0; s < pi.choices.length; s++) {
						log.info("Choice[" + (new Integer(s).toString()) + "]: " + pi.choices[s]);
					}
				} else {
					log.info("PropertyInfo null for index: " + (new Integer(i).toString()));
				}
			}
			log.info("*********************************************************************");

		}

	}

	
}
