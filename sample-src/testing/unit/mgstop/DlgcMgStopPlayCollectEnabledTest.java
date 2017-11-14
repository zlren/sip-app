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
import java.net.URI;

import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.mediagroup.signals.SignalDetector;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.resource.RTC;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



//MSC-129 Restcomm request to add mg.stop() PlayCollect
public class DlgcMgStopPlayCollectEnabledTest extends DlgcMgStopBaseTestCase{
	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory.getLogger(DlgcMgStopPlayCollectEnabledTest.class);
	SdpEventListener <SdpPortManagerEvent> mySdpEventListener = null;

	public DlgcMgStopPlayCollectEnabledTest(DlgcMgStopTest mainServlet) {
		super(mainServlet);
		mySdpEventListener = new  SdpEventListener<SdpPortManagerEvent>(this); 
	}

	public void invite(SipServletRequest req) {
		log.debug("Entering DlgcMgStopPlayCollectEnabledTest::invite");
		SipSession ss = req.getSession();
		ss.setAttribute("TEST_CASE", this);
		try {
			super.executeInvite(req, mySdpEventListener);
			MediaGroup mg = (MediaGroup) ss.getAttribute("MEDIAGROUP");
			mg.getPlayer().addListener(new PlayerEventListener(myMainServlet));
		} catch (ServletException e) {
			log.error(e.toString());
		} catch (IOException e) {
			log.error(e.toString());
		} catch (MsControlException e) {
			log.error(e.toString());
		}
		log.debug("Leaving DlgcMgStopPlDlgcMgStopPlayCollectEnabledTestayerOnlyEnabledTest::invite");
	}
	
	@Override
	//Called by the base class listener
	public boolean onSdpEvent(SdpPortManagerEvent anEvent) 
	{
		log.debug("Entering DlgcMgStopPlayCollectEnabledTest::onSdpEvent");
		boolean status = super.onSdpEvent(anEvent);
		log.debug("Entering DlgcMgStopPlayCollectEnabledTest::onSdpEvent");
		return status;
	}
	
	@Override
	//Called by the base class doAck
	//This indicates to the unit test that the connection is ready
	//and it can proceed with the detail stop test.
	public void ackResult(SipServletRequest req)
	{
		log.debug("Entering DlgcMgStopPlayCollectEnabledTest() ");

		SipSession ss = req.getSession();
		MediaGroup mg = (MediaGroup) ss.getAttribute("MEDIAGROUP");
		NetworkConnection nc = (NetworkConnection) ss.getAttribute("NETWORK_CONNECTION");
		try {
			mg.join(Joinable.Direction.DUPLEX, nc);
			MediaSession mediaSession = (MediaSession) ss.getAttribute("MEDIA_SESSION");
			mg.getSignalDetector().flushBuffer();			//sets the clear buffer flag when the request is sent to XMS
			log.debug("DlgcMgStopPlayCollectEnabledTest calling play and collect ");
			playAndCollect(mg);
			log.debug("DlgcMgStopPlayCollectEnabledTest Sleep for N seconds before calling mg.stop() all ");
			try {
				Thread.sleep(4000);
				log.debug("DlgcMgStopPlayCollectEnabledTest mg.stop() ");
				mg.stop();
				log.debug("DlgcMgStopPlayCollectEnabledTest ackResult() - Returned from mg.stop() ");
				terminateSession(mediaSession);
			} catch (InterruptedException e) {
				log.error(e.toString());
			}
		} catch (MsControlException e1) {
			log.error(e1.toString());
		}
		
		
		log.debug("Leaving DlgcMgStopPlayCollectEnabledTest ackResult() ");
	}

	void playAndCollect(MediaGroup mg)
	{
		log.debug("Entering DlgcMgStopPlayCollectEnabledTest playAndCollect() ");

		
		URI prompt = URI.create("file:////var/lib/xms/media/en_US/verification/demoJSR309/player/intro.wav");
		Parameters mgParms = mg.createParameters();
		Integer testPropNumOfSign = new Integer(-1); 
		String  testPropPattern="777";
		RTC[] rtcs = new RTC[1];
		rtcs[0] = MediaGroup.SIGDET_STOPPLAY;			//barge in

		mgParms.put(SignalDetector.PATTERN[1], testPropPattern);		
		mg.setParameters(mgParms);
		Parameter[] detectDigitPattern = { SignalDetector.PATTERN[1] }; 		//redefine the pattern 1 value
		Parameters collectOptions	= myMainServlet.mscFactory.createParameters();
		URI[] prompts = { prompt };
		collectOptions.put(SignalDetector.PROMPT, prompts);

		try {
			log.debug("DlgcMgStopPlayCollectEnabledTest:: calling receiveSignals");
			mg.getSignalDetector().receiveSignals(testPropNumOfSign, detectDigitPattern, rtcs, collectOptions);
		} catch (MsControlException e) {
			log.error(e.toString());
		}  
		
		log.debug("Leaving DlgcMgStopPlayCollectEnabledTest playAndCollect() ");

	}
	
	private class PlayerEventListener implements MediaEventListener<PlayerEvent>,  Serializable
	{

		private static final long serialVersionUID = 1;
		protected DlgcMgStopTest myServlet;

		//private boolean asyncCreated = false;

		public PlayerEventListener(DlgcMgStopTest servlet) {
			myServlet = myMainServlet;
		}

		@Override
		public void onEvent(PlayerEvent event)
		{
			log.debug("DlgcMgStopPlayCollectEnabledTest::PlayerEventListener::onEvent()");
			log.debug("   EVENT TYPE : " + event.getEventType());
			log.debug("    QUALIFIER : " + event.getQualifier());
			log.debug("  CHANGE TYPE : " + event.getChangeType());
			log.debug("   PLAY INDEX : " + event.getIndex());
			log.debug("  PLAY OFFSET : " + event.getOffset());
			log.debug("        ERROR : " + event.getError());
			log.debug("   ERROR TEXT : " + event.getErrorText());

			if ( event.isSuccessful() ) {
				log.debug("Received Event is successful");
			} else {
				log.error("Received Event is NOT successful");
			}

		}
	}
	
class MySignalDetectorListener implements MediaEventListener<SignalDetectorEvent>, Serializable {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void onEvent(SignalDetectorEvent anEvent) {
			
			//note using the JSR309 qualifier RTC_TRIGGERED not standard way of using this qualifier
			//there were not other way to map the IPMS condition using JSR 309 SPEC
			//so we decided VZ and Dialogic to use this for this purpose
			if ( (anEvent.getEventType() == SignalDetectorEvent.SIGNAL_DETECTED) && (anEvent.getQualifier() == SignalDetectorEvent.RTC_TRIGGERED) ) 
			{
				
				log.info("MySignalDetectorListener DTMF WAS ENABLED IN THE MEDIA SERVER");
				return;
				
			}
			
			if ( anEvent.getQualifier() == SignalDetectorEvent.DURATION_EXCEEDED) {
				log.debug("MySignalDetectorListener:: DURATION_EXCEEDED");
			} else if ( anEvent.getQualifier() == SignalDetectorEvent.INITIAL_TIMEOUT_EXCEEDED) {
				log.debug("MySignalDetectorListener:: INITIAL_TIMEOUT_EXCEEDED");
			} else if ( anEvent.getQualifier() == SignalDetectorEvent.INTER_SIG_TIMEOUT_EXCEEDED) {
				log.debug("MySignalDetectorListener:: INTER_SIG_TIMEOUT_EXCEEDED");
			} else {
				log.debug("MySignalDetectorListener:: other: qualifer " + anEvent.getQualifier().toString() );
			}
			
			log.info("MySignalDetectorListener ReceiveSignals terminated with: "+anEvent);
			log.info("MySignalDetectorListener ReceiveSignals terminated Event: "+anEvent.getEventType().toString());

			// In this example, the collected DTMFs are just logged.
			// In real life they could be returned in a signaling parameter, or propagated to a JSP
			log.info("MySignalDetectorListener DTMF Collected: "+anEvent.getSignalString());
			String qualString = anEvent.getQualifier().toString();
			log.info("MySignalDetectorListener Qualifier: "+ qualString );
			log.info("MySignalDetectorListener ReceiveSignals with Error Type (if any): "+anEvent.getError().toString());
			log.info("MySignalDetectorListener ReceiveSignals with Error String (if any): "+anEvent.getErrorText());
			
		}
	}		
}
