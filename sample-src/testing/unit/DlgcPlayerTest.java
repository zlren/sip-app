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

package testing.unit;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaEvent;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.Player;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.mediagroup.signals.SignalDetector;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.resource.AllocationEvent;
import javax.media.mscontrol.resource.AllocationEventListener;
import javax.media.mscontrol.resource.RTC;
import javax.media.mscontrol.resource.Resource;
import javax.media.mscontrol.resource.ResourceContainer;
import javax.media.mscontrol.resource.ResourceEvent;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.DlgcTest;

public class DlgcPlayerTest extends DlgcTest {

	private DlgcRecorderTest recorderTestServlet = null;

	private static final long serialVersionUID = 1L;

	// C110566 set it to true just to test invalid prompt scenario
	boolean bTestInvalidMediaTarget = false;
	Boolean ipmsMediaServerType = false; // XMS MODE
	Integer loopCount = null;
	Integer loopInterval = null;
	// Boolean asynEnableBool = false; MSC-127 remore
	// Boolean asyncApiListenerBool=false;
	Boolean bTestReinvite = false;
	transient MediaSession activeMediaSession = null;

	private MySignalDetectorListener sigDetListener;
	private Parameters collectOptions;

	@Override
	public void doAck(SipServletRequest req) {
		log.debug("Entering  DlgcPlayerTest::doAck");
		SipSession sipSession = req.getSession();

		log.debug("Entering  DlgcPlayerTest::doAck getAttribute NC");
		NetworkConnection networkConnection = (NetworkConnection) sipSession.getAttribute("NETWORK_CONNECTION");
		MediaSession ms = networkConnection.getMediaSession();

		Boolean isInReinviteState = (Boolean) ms.getAttribute("REINVITE");

		if (isInReinviteState) {
			return;
		}

		try {
			byte[] remoteSdp = req.getRawContent();
			if (remoteSdp != null) {
				networkConnection.getSdpPortManager().processSdpAnswer(remoteSdp);
			}
		} catch (MsControlException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// to test getMediaObject
		// added to test media session get from MS uri
		URI msURI = (URI) sipSession.getAttribute("MSURI");
		log.debug("PLAYER...PLAYER....ACK ACK ACK  MSURI = " + msURI + " PLAYER...PLAYER....ACK ACK ACK");
		mscFactory.getMediaObject(msURI);

		MediaGroup mediaGroup = (MediaGroup) sipSession.getAttribute("MEDIAGROUP");
		Parameters parameters = mscFactory.createParameters();
		// parameters.put(Player.REPEAT_COUNT, 2);
		// parameters.put(Player.INTERVAL, 2000);
		parameters.put(Player.REPEAT_COUNT, loopCount.intValue());
		parameters.put(Player.INTERVAL, loopInterval.intValue());
		parameters.put(Player.MAX_DURATION, Player.FOR_EVER);

		try {
			// mediaGroup.getPlayer().play(prompt, RTC.NO_RTC, parameters);

			Player player = mediaGroup.getPlayer();

			// C110566
			if (bTestInvalidMediaTarget) {
				player.play(invalidPrompt, RTC.NO_RTC, parameters);
			} else {
				RTC[] rtcs = new RTC[1];
				/***
				 * MSC-127 Remove if ( this.asynEnableBool == false ) { rtcs[0]
				 * = MediaGroup.SIGDET_STOPPLAY; //play barge in
				 * log.debug("DlgcPlayerTest Application:: test calling
				 * player.play()"); player.play(prompt, rtcs, parameters); }
				 * else {
				 ***/
				player.play(prompt, RTC.NO_RTC, parameters);
				// }
			}
			// CR48377 MOBICENTS SYNCHRONOUS FIXES - Fix Player Stop
			// enable this part to test player stop
			// log.debug("Testing player stop API after play request returns");
			// try {
			// Thread.sleep(1000);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			// player.stop(true);
			// terminateSession(networkConnection.getMediaSession());
		} catch (MsControlException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doInvite(final SipServletRequest req) throws ServletException, IOException {
		log.debug("doInvite");
		// log.info("TEST JBOSS");
		if (activeMediaSession != null) {
			if (this.bTestReinvite == false) {
				log.debug("DlgcPlayerTest::doInvite: executing None Reinvite request... normal request");
				executeRegularInvite(req);
			} else {
				log.debug("DlgcPlayerTest::doInvite: executing Reinvite request... Reinvite request test");
				executeReinvite(req);
			}
		} else {
			executeRegularInvite(req);
		}
	}

	/***
	 * @Override public void doInvite(final SipServletRequest req) throws
	 *           ServletException, IOException { log.debug("doInvite");
	 * 
	 *           NetworkConnection networkConnection = null;
	 * 
	 *           if ( recorderTestServlet != null ) { log.debug("Calling
	 *           DlgcRecorderTest method"); //
	 *           recorderTestServlet.webRtc_print("DlgcPlayerTest"); }else {
	 *           log.debug("Cant not call DlgcRecorderTest method
	 *           recorderTestServlet is NULL"); }
	 * 
	 *           recorderTestServlet = (DlgcRecorderTest)
	 *           this.cfg.getServletContext().getAttribute("DlgcRecorderTest");
	 * 
	 *           if ( recorderTestServlet != null ) { log.debug("Calling
	 *           DlgcRecorderTest method using getAttribute"); //
	 *           recorderTestServlet.webRtc_print("DlgcPlayerTest"); }else {
	 *           log.debug("Cant not call DlgcRecorderTest using getAttribute
	 *           method recorderTestServlet is NULL"); }
	 * 
	 * 
	 *           if (req.isInitial()) { try { MediaSession mediaSession =
	 *           mscFactory.createMediaSession();
	 * 
	 * 
	 *           Parameters pmap = mediaSession.createParameters();
	 * 
	 *           Integer stimeout = new Integer(5000);
	 *           pmap.put(MediaSession.TIMEOUT, stimeout);
	 *           mediaSession.setParameters(pmap);
	 * 
	 * 
	 *           networkConnection =
	 *           mediaSession.createNetworkConnection(NetworkConnection.BASIC);
	 * 
	 *           //not needed for this unit test sample just using it to
	 *           demonstrate Parameters sdpConfiguration =
	 *           mediaSession.createParameters(); Map<String,String>
	 *           configurationData = new HashMap<String,String>();
	 *           configurationData.put("SIP_REQ_URI_USERNAME", "msml=777");
	 *           //test webrtc=yes //configurationData.put("webrtc", "yes");
	 *           //sdpConfiguration.put(SdpPortManager.SIP_HEADERS,
	 *           configurationData);
	 * 
	 *           networkConnection.setParameters(sdpConfiguration);
	 * 
	 *           SdpEventListener<SdpPortManagerEvent> la = new
	 *           SdpEventListener<SdpPortManagerEvent>();
	 *           networkConnection.getSdpPortManager().addListener(la);
	 *           MediaGroup mediaGroup = null; if ( this.asynEnableBool == true
	 *           ) {
	 * 
	 *           mediaGroup =
	 *           mediaSession.createMediaGroup(MediaGroup.PLAYER_SIGNALDETECTOR);
	 * 
	 *           SignalDetector sg = mediaGroup.getSignalDetector();
	 *           sg.addListener(sigDetListener);
	 * 
	 *           SDAllocListener allocListener = new SDAllocListener();
	 *           mediaGroup.addListener(allocListener); } else { mediaGroup =
	 *           mediaSession.createMediaGroup(MediaGroup.PLAYER); }
	 * 
	 *           SipSession sipSession = req.getSession();
	 * 
	 *           //only use to test factory getMediaObject URI msURI =
	 *           mediaSession.getURI(); sipSession.setAttribute("MSURI", msURI);
	 *           log.debug("PLAYER...PLAYER.... MSURI = " + msURI + "
	 *           PLAYER...PLAYER....");
	 * 
	 * 
	 *           log.debug("DlgcPlayerTest::doInvite.... Setting mediaSession");
	 *           sipSession.setAttribute("MEDIA_SESSION", mediaSession);
	 *           sipSession.setAttribute("NETWORK_CONNECTION",
	 *           networkConnection); sipSession.setAttribute("MEDIAGROUP",
	 *           mediaGroup);
	 * 
	 *           mediaSession.setAttribute("SIP_SESSION", sipSession);
	 *           mediaSession.setAttribute("SIP_REQUEST", req);
	 *           mediaSession.setAttribute("NETWORK_CONNECTION",
	 *           networkConnection);
	 * 
	 *           networkConnection.getSdpPortManager().addListener(speListener);
	 *           mediaGroup.getPlayer().addListener(new
	 *           PlayerEventListener(this));
	 * 
	 *           mediaGroup.join(Joinable.Direction.DUPLEX, networkConnection);
	 * 
	 * 
	 *           } catch (MsControlException e) {
	 *           req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
	 *           } } else {
	 * 
	 *           }
	 * 
	 *           try { req.getSession().setAttribute("UNANSWERED_INVITE", req);
	 * 
	 *           byte[] remoteSdp = req.getRawContent();
	 * 
	 *           if (remoteSdp == null) {
	 *           networkConnection.getSdpPortManager().generateSdpOffer(); }
	 *           else {
	 *           networkConnection.getSdpPortManager().processSdpOffer(remoteSdp);
	 *           } } catch (MsControlException e) {
	 *           req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
	 *           }
	 * 
	 *           }
	 ****/
	public void executeReinvite(final SipServletRequest req) throws ServletException, IOException {
		log.debug("executeReinvite");
		SipSession sipSession2 = req.getSession();
		if (activeMediaSession == null) {

			log.error("executeReinvite: error no acdtiveMediaSession");
			throw new ServletException("executeReinvite: error no acdtiveMediaSession");

		} else {

			// MediaSession mediaSession =
			// (MediaSession)activeMediaSession.getAttribute("MEDIA_SESSION");
			MediaSession mediaSession = activeMediaSession;
			SipSession sipSession = (SipSession) mediaSession.getAttribute("SIP_SESSION_1");

			mediaSession.setAttribute("REINVITE", true);
			NetworkConnection networkConnection = (NetworkConnection) sipSession.getAttribute("NETWORK_CONNECTION");

			Parameters sdpConfiguration = mediaSession.createParameters();
			Map<String, String> configurationData = new HashMap<String, String>();
			configurationData.put("SIP_REQ_URI_USERNAME", "msml=777");
			sdpConfiguration.put(SdpPortManager.SIP_HEADERS, configurationData);
			networkConnection.setParameters(sdpConfiguration);

			URI msURI = mediaSession.getURI();
			sipSession.setAttribute("MSURI", msURI);
			log.debug("PLAYER...PLAYER.... MSURI = " + msURI + " PLAYER...PLAYER....");

			log.debug("DlgcPlayerTest::doInvite.... Setting mediaSession");
			mediaSession.setAttribute("SIP_SESSION_2", sipSession);
			mediaSession.setAttribute("SIP_REQUEST", req);
			mediaSession.setAttribute("NETWORK_CONNECTION", networkConnection);
			sipSession2.setAttribute("NETWORK_CONNECTION", networkConnection);

			try {
				req.getSession().setAttribute("UNANSWERED_INVITE", req);

				byte[] remoteSdp = req.getRawContent();

				if (remoteSdp == null) {
					log.debug("DlgcPlayerTest::generateSdpOffer()");
					networkConnection.getSdpPortManager().generateSdpOffer();
				} else {
					// reinvite
					log.debug("DlgcPlayerTest::processSdpOffer(remoteSdp) - Reinvite");
					networkConnection.getSdpPortManager().processSdpOffer(remoteSdp);
				}
			} catch (MsControlException e) {
				req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
			}
		}
	}

	public void executeRegularInvite(final SipServletRequest req) throws ServletException, IOException {
		log.debug("executeRegularInvite");

		NetworkConnection networkConnection = null;

		if (recorderTestServlet != null) {
			log.debug("Calling DlgcRecorderTest method");
			// recorderTestServlet.webRtc_print("DlgcPlayerTest");
		} else {
			log.debug("Cant not call DlgcRecorderTest method recorderTestServlet is NULL");
		}

		recorderTestServlet = (DlgcRecorderTest) this.cfg.getServletContext().getAttribute("DlgcRecorderTest");

		if (recorderTestServlet != null) {
			log.debug("Calling DlgcRecorderTest method using getAttribute");
			// recorderTestServlet.webRtc_print("DlgcPlayerTest");
		} else {
			log.debug("Cant not call DlgcRecorderTest using getAttribute method recorderTestServlet is NULL");
		}

		if (req.isInitial()) {
			try {
				MediaSession mediaSession = mscFactory.createMediaSession();

				Parameters pmap = mediaSession.createParameters();

				Integer stimeout = new Integer(5000);
				pmap.put(MediaSession.TIMEOUT, stimeout);
				mediaSession.setParameters(pmap);

				networkConnection = mediaSession.createNetworkConnection(NetworkConnection.BASIC);
				mediaSession.setAttribute("REINVITE", false);

				// not needed for this unit test sample just using it to
				// demonstrate
				Parameters sdpConfiguration = mediaSession.createParameters();
				Map<String, String> configurationData = new HashMap<String, String>();
				configurationData.put("SIP_REQ_URI_USERNAME", "msml=777");
				// test webrtc=yes
				// configurationData.put("webrtc", "yes");
				// sdpConfiguration.put(SdpPortManager.SIP_HEADERS,
				// configurationData);

				networkConnection.setParameters(sdpConfiguration);

				SdpEventListener<SdpPortManagerEvent> la = new SdpEventListener<SdpPortManagerEvent>();
				networkConnection.getSdpPortManager().addListener(la);
				MediaGroup mediaGroup = null;

				/***
				 * Remove MSC-127 if ( this.asynEnableBool == true ) {
				 * 
				 * mediaGroup =
				 * mediaSession.createMediaGroup(MediaGroup.PLAYER_SIGNALDETECTOR);
				 * 
				 * SignalDetector sg = mediaGroup.getSignalDetector();
				 * sg.addListener(sigDetListener);
				 * 
				 * SDAllocListener allocListener = new SDAllocListener();
				 * mediaGroup.addListener(allocListener); } else {
				 ***/
				mediaGroup = mediaSession.createMediaGroup(MediaGroup.PLAYER);
				// }

				SipSession sipSession = req.getSession();

				// only use to test factory getMediaObject
				URI msURI = mediaSession.getURI();
				sipSession.setAttribute("MSURI", msURI);
				log.debug("PLAYER...PLAYER.... MSURI = " + msURI + " PLAYER...PLAYER....");

				log.debug("DlgcPlayerTest::doInvite.... Setting mediaSession");
				sipSession.setAttribute("MEDIA_SESSION", mediaSession);
				sipSession.setAttribute("NETWORK_CONNECTION", networkConnection);
				sipSession.setAttribute("MEDIAGROUP", mediaGroup);

				// mediaSession.setAttribute("SIP_SESSION", sipSession);
				mediaSession.setAttribute("SIP_SESSION_1", sipSession);
				mediaSession.setAttribute("SIP_REQUEST", req);
				mediaSession.setAttribute("NETWORK_CONNECTION", networkConnection);

				// networkConnection.getSdpPortManager().addListener(speListener);
				mediaGroup.getPlayer().addListener(new PlayerEventListener(this));

				mediaGroup.join(Joinable.Direction.DUPLEX, networkConnection);

			} catch (MsControlException e) {
				req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
			}
		} else {

		}

		try {
			req.getSession().setAttribute("UNANSWERED_INVITE", req);

			byte[] remoteSdp = req.getRawContent();

			if (remoteSdp == null) {
				networkConnection.getSdpPortManager().generateSdpOffer();
			} else {
				networkConnection.getSdpPortManager().processSdpOffer(remoteSdp);
			}
		} catch (MsControlException e) {
			req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
		}
	}

	@Override
	protected void doResponse(SipServletResponse response) throws IOException, ServletException {
		// SipSession session = response.getSession();

	}

	@Override
	public void doBye(final SipServletRequest req) throws ServletException, IOException {
		log.debug("GOT BYE from phone");
		terminateSession(req.getSession());
	}

	private void terminateSession(SipSession session) {
		if (session != null) {
			log.debug("DlgcPlayerTest::terminateSession using Media Session release.... Getting mediaSession");
			MediaSession mediaSession = (MediaSession) session.getAttribute("MEDIA_SESSION");
			mediaSession.release();
			session.invalidate();
		}

	}

	private void terminateSession(MediaSession ms) {
		log.debug("DlgcPlayerTest::terminateSession.... ");
		// SipSession session = (SipSession) ms.getAttribute("SIP_SESSION");
		SipSession session = (SipSession) ms.getAttribute("SIP_SESSION_1");
		if (session != null) {
			log.debug(
					"SYNC_2_ASYNC DlgcPlayerTest::terminateSession using Media Session Release .... release media session and sending bye to phone session.");
			ms.release();
			this.sendBye(session, ms);
		}

	}

	// send bye to phone
	protected void sendBye(SipSession sipSession, MediaSession mediaSession) {

		SipServletRequest bye = sipSession.createRequest("BYE");
		log.debug("Inside sendBye method");
		try {
			bye.send();
		} catch (Exception e1) {
			log.error("Terminating: Cannot send BYE: " + e1);
		}

	}

	@Override
	public void servletInitialized(SipServletContextEvent evt) {

		String sName = evt.getSipServlet().getServletName();
		log.info("Player的前面前面");

		if ((platform != null) && (platform.compareToIgnoreCase(DlgcTest.TELESTAX_PLATFORM) == 0)
				|| (platform != null) && (platform.compareToIgnoreCase(DlgcTest.ORACLE_PLATFORM) == 0)) {
			
			if (sName.equalsIgnoreCase("DlgcSipServlet")) {
				log.info("一一一");
				dlgcSipServletLoaded = true; // 
				log.info(" DlgcPlayerTest::servletInitialized DlgcSipServlet loaded");
			} else if (sName.equalsIgnoreCase("DlgcPlayerTest")) {
				log.info("二二二");
				playerServletInitCalled = true; //
			} else if (sName.equalsIgnoreCase("DlgcRecorderTest")) {
				log.info("三三三");
				recorderTestServlet = (DlgcRecorderTest) evt.getSipServlet();
				log.info("DlgcPlayerTest::servletInitialized got recorderTEstServlet:: " + recorderTestServlet.toString());
			}

			if (playerServletInitCalled && dlgcSipServletLoaded) {
				log.info("四四四");
				if (servletInitializedFlag == false) {
					log.info("五五五");
					log.info("Entering DlgcPlayerTest::servletInitialized servletName: " + sName);
					servletInitializedFlag = true;
					initDriver();
					myServletInitialized(evt);
				} else {
					log.info("六六六");
					log.info("DlgcPlayerTest::servletInitialized(): already servletInitialized was called...debouncing " + sName);
				}
			}
		} else { // TROPO Framework

//			log.info("这里是else！！！！！！！！！！！！");
//			
//			if (sName.equalsIgnoreCase("DlgcSipServlet")) {
//				dlgcSipServletLoaded = true;
//				log.info(" DlgcPlayerTest::servletInitialized DlgcSipServlet loaded");
//				return;
//			}
//
//			if ((sName.equalsIgnoreCase("DlgcPlayerTest")) && dlgcSipServletLoaded) {
//				if (servletInitializedFlag == false) {
//					log.info("Entering DlgcPlayerTest::servletInitialized servletName: " + sName);
//					servletInitializedFlag = true;
//					initDriver();
//					myServletInitialized(evt);
//				} else {
//					log.info("DlgcPlayerTest::servletInitialized(): already servletInitialized was called...debouncing " + sName);
//				}
//			}
		}

		log.info("Player的后面后面");
	}

	@Override
	protected void myServletInitialized(SipServletContextEvent x) {
		String playerTestPrompt = null;

		// C110566
		invalidPrompt = URI.create("file:////opt/snowshore/prompts/generic/en_US/invalidBogusPlayerPrompt.ulaw");

		// XMS PROMPT
		playerTestPrompt = demoPropertyObj.getProperty("DlgcPlayerDemo.prompt");
		if (playerTestPrompt == null) {
			prompt = URI.create("file:////var/lib/xms/media/en_US/verification/demoJSR309/player/intro.wav");
		} else {
			prompt = URI.create(playerTestPrompt);
			prompt2 = URI.create("file:////opt/snowshore/prompts/generic/en_US/circuit_busy.ulaw");
			prompt3 = URI.create("file:////opt/snowshore/prompts/generic/en_US/dial_operator.ulaw");
		}
		String playCnt = demoPropertyObj.getProperty("DlgcPlayerDemo.loop.count");
		if (playCnt != null) {
			loopCount = new Integer(playCnt);
		} else {
			loopCount = new Integer(2);
		}
		log.debug("DlgcPlayerTest::loopCount = " + loopCount.intValue());

		String loopIntervalString = demoPropertyObj.getProperty("player.test.loop.interval");
		if (loopIntervalString != null) {
			loopInterval = new Integer(loopIntervalString);
		} else {
			loopInterval = new Integer(2000);
		}
		log.debug("DlgcPlayerTest::loopInterval = " + loopInterval.intValue());

		// MSC-123 march 11 2015
		/**
		 * String asyncEnable =
		 * demoPropertyObj.getProperty("DlgcPlayerDemo.async");
		 * 
		 * if ( asyncEnable != null ) { asynEnableBool = new
		 * Boolean(asyncEnable); } log.debug("DlgcPlayerTest::enableAsync = " +
		 * asynEnableBool.toString());
		 ***/

		// String asyncApiListener =
		// demoPropertyObj.getProperty("DlgcPlayerDemo.listener.use.asyncapi");

		// if ( asyncApiListener != null ) {
		// asyncApiListenerBool = new Boolean(asyncApiListener);
		// }
		// log.debug("DlgcPlayerTest::asyncApiListener = " +
		// asyncApiListenerBool.toString());

		String sReinvite = demoPropertyObj.getProperty("demos.test.reinvite");
		if (sReinvite != null) {
			bTestReinvite = new Boolean(sReinvite);
		}
		log.debug("DlgcPlayerTest::demo.test.reinvite = " + bTestReinvite.toString());

	}

	@Override
	public void init(ServletConfig cfg) throws ServletException {
		super.init(cfg);
		myServletLoaded = true;

		/*
		 * if ( asynEnableBool == true ) initAsync(cfg);
		 */
	}

	public void initAsync(ServletConfig cfg) throws ServletException {

		try {
			configuration = MediaGroup.PLAYER_SIGNALDETECTOR;
			sigDetListener = new MySignalDetectorListener();

			collectOptions = mscFactory.createParameters();
			collectOptions.put(SignalDetector.BUFFERING, Boolean.FALSE);
			EventType[] arrayEnabledEvents = { SignalDetectorEvent.SIGNAL_DETECTED };
			collectOptions.put(SignalDetector.ENABLED_EVENTS, arrayEnabledEvents);
		} catch (Exception e) {
			throw new ServletException(e);
		}

	}

	public class SDAllocListener implements AllocationEventListener, Serializable {

		private static final long serialVersionUID = 1;

		public SDAllocListener() {
		}

		@Override
		public void onEvent(AllocationEvent theEvent) {
			log.debug("Entering SDAllocListener::onEvent");

			EventType joinEvType = theEvent.getEventType();

			log.debug("SDAllocListener::EventType: " + theEvent.getEventType());
			log.debug("SDAllocListener::Source: " + theEvent.getSource().toString());
			log.debug("DlgcReferenceConferenceAllocListener::ErrorText: " + theEvent.getErrorText());

			if (joinEvType == AllocationEvent.ALLOCATION_CONFIRMED) {
				log.debug("SDAllocListener Signal Detector allocated");
			} else {
				log.error("SDAllocListener Signal Detector error allocating detector");

			}
		}

	}

	public void doAsync(SipSession sipSession) {
		try {

			MediaGroup mg = (MediaGroup) sipSession.getAttribute("MEDIAGROUP");
			SignalDetector sg = mg.getSignalDetector();
			sg.receiveSignals(-1, null, null, collectOptions);
		} catch (Exception e) {
			// Clean up media session
			e.printStackTrace();
			// terminateSession(sipSession);
			return;
		}

	}

	class MySignalDetectorListener implements MediaEventListener<SignalDetectorEvent>, Serializable {

		private static final long serialVersionUID = 1;

		@Override
		public void onEvent(SignalDetectorEvent anEvent) {
			log.debug("Player:: ReceiveSignals terminated with: " + anEvent);

			if ((anEvent.getEventType() == SignalDetectorEvent.SIGNAL_DETECTED)
					&& (anEvent.getQualifier() == SignalDetectorEvent.RTC_TRIGGERED)) {

				log.debug("DTMF WAS ENABLED IN THE MEDIA SERVER");
				return;
			}

			String dtmf = anEvent.getSignalString();
			MediaGroup mg = (MediaGroup) anEvent.getSource().getContainer();

			MediaSession ms = anEvent.getSource().getMediaSession();

			SipSession sipSession = (SipSession) ms.getAttribute("SIP_SESSION_1");

			SignalDetector detector = null;
			try {
				detector = mg.getSignalDetector();
			} catch (MsControlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (anEvent.getEventType() == SignalDetectorEvent.SIGNAL_DETECTED) {

				log.debug("Collected: " + dtmf);
			}

			if (anEvent.getEventType() == SignalDetectorEvent.FLUSH_BUFFER_COMPLETED) {
				log.debug("Got flush buffer event");
			} else if (anEvent.getQualifier() == ResourceEvent.STOPPED) {
				log.debug("Terminating DtmfAsyncCollectionServlet due to a Stop Request");
				log.debug("Disconnect from softphone...getting ready to terminate.");
				sendBye(sipSession, ms);

			}

		}
	}

	private class SdpEventListener<T extends MediaEvent<?>> implements MediaEventListener<T>, Serializable {

		private static final long serialVersionUID = 1;

		@SuppressWarnings("unchecked")
		public void onEvent(T anEvent) {
			log.debug("Entering SdpEventListener  received event =" + anEvent.getEventType().toString());
			Resource r = (Resource) anEvent.getSource();
			ResourceContainer container = r.getContainer();
			MediaSession ms = container.getMediaSession();
			if (anEvent.getEventType() == SdpPortManagerEvent.NETWORK_STREAM_FAILURE) {
				log.debug("Releasing the following component: " + container.toString() + " due to Media Server down.");
				terminateSession(ms);
			} else {
				// SDP ANSWERD
				Boolean reinviteInProgress = (Boolean) ms.getAttribute("REINVITE");

				if (reinviteInProgress == true) {
					SipSession sipSession1 = (SipSession) ms.getAttribute("SIP_SESSION_1");
					if (sipSession1 != null) {
						log.debug("releasing first leg due to reinvite");
						sendBye(sipSession1, ms);
					}
					ms.removeAttribute("SIP_SESSION_1");
					SipSession sipSession2 = (SipSession) ms.getAttribute("SIP_SESSION_2");
					log.debug("sending 200 ok to sip session 2");
					try {
						SdpPortManager sdpMgr = ((NetworkConnection) container).getSdpPortManager();
						DlgcPlayerTest.send200_OK(ms, sipSession2, sdpMgr);
					} catch (MsControlException e) {
						log.error("Exception: " + e.toString());
					}
				} else {
					SipSession sipSession1 = (SipSession) ms.getAttribute("SIP_SESSION_1");
					log.debug("sending 200 ok to sip session 1");
					try {
						SdpPortManager sdpMgr = ((NetworkConnection) container).getSdpPortManager();
						DlgcPlayerTest.send200_OK(ms, sipSession1, sdpMgr);
					} catch (MsControlException e) {
						log.error("Exception: " + e.toString());
					}
				}

				log.debug("Leaving SdpEventListener  received event =" + anEvent.getEventType().toString());
			}

		}
	}

	static void send200_OK(MediaSession ms, SipSession sipSession, SdpPortManager sdp) {
		log.debug("DlgcPlayerTest: send200_OK back to the phone");
		SipServletRequest req = (SipServletRequest) ms.getAttribute("SIP_REQUEST");

		try {
			SipServletResponse response = req.createResponse(SipServletResponse.SC_OK);
			try {
				response.setContent(sdp.getMediaServerSessionDescription(), "application/sdp");
				response.send();
			} catch (Exception e1) {
				log.error("Exception: " + e1.toString());
			}
		} catch (Exception e) {
			log.error("Exception: " + e.toString());
		}
	}

	private class PlayerEventListener implements MediaEventListener<PlayerEvent>, Serializable {

		private static final long serialVersionUID = 1;
		protected DlgcPlayerTest myServlet;

		private boolean asyncCreated = false;

		public PlayerEventListener(DlgcPlayerTest servlet) {
			myServlet = servlet;
		}

		@Override
		public void onEvent(PlayerEvent event) {
			log.debug("PlayerEventListener::onEvent()");
			log.debug("   EVENT TYPE : " + event.getEventType());
			log.debug("    QUALIFIER : " + event.getQualifier());
			log.debug("  CHANGE TYPE : " + event.getChangeType());
			log.debug("   PLAY INDEX : " + event.getIndex());
			log.debug("  PLAY OFFSET : " + event.getOffset());
			log.debug("        ERROR : " + event.getError());
			log.debug("   ERROR TEXT : " + event.getErrorText());

			// C110566
			if (event.isSuccessful()) {
				log.debug("Received Event is successful");
			} else {
				log.error("Received Event is NOT successful");
			}

			if (event.getEventType() == PlayerEvent.RESUMED) {
				log.debug("Received Player Event: Play Resumed which means Play Started...");

				/**
				 * MSC-127 Remove if ( myServlet.asynEnableBool == true ) { if (
				 * asyncCreated == false ) { MediaSession mediaSession =
				 * event.getSource().getMediaSession(); SipSession session =
				 * (SipSession) mediaSession.getAttribute("SIP_SESSION");
				 * myServlet.doAsync(session); asyncCreated = true; } }
				 ***/

			} else {
				MediaSession mediaSession = event.getSource().getMediaSession();
				log.debug("DlgcPlayerTest::onEvent.... Getting mediaSession");
				// SipSession session = (SipSession)
				// mediaSession.getAttribute("SIP_SESSION");
				log.debug("Calling BYE..Hanging Phone");
				terminateSession(mediaSession);
			}

		}
	}

	protected Configuration<MediaGroup> configuration;
	protected URI prompt;
	protected URI prompt2;
	protected URI prompt3;

	// C110566
	protected URI invalidPrompt;

	// static private Queue<URI> uriQueue = new LinkedList<URI>(); //if you make
	// it transient does not work
	// if not set to static has problem running n number of time,
	// when PlayerEventListener implements Serializable
	// maybe synchronization problems.

	private static Logger log = LoggerFactory.getLogger(DlgcPlayerTest.class);
}
