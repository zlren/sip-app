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

import javax.media.mscontrol.EventType;
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
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.resource.RTC;
import javax.media.mscontrol.resource.ResourceEvent;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





public class DlgcMgStopAsyncDetectorEnabledTest extends DlgcMgStopBaseTestCase
{
	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory.getLogger(DlgcMgStopAsyncDetectorEnabledTest.class);
	SdpEventListener <SdpPortManagerEvent> mySdpEventListener = null;
	MySignalDetectorListener sigDetListener = null;
	Parameters collectOptions =null;
	
	public DlgcMgStopAsyncDetectorEnabledTest(DlgcMgStopTest mainServlet) {
		super(mainServlet);
		mySdpEventListener = new  SdpEventListener<SdpPortManagerEvent>(this);
		sigDetListener = new MySignalDetectorListener(this);		
		collectOptions = myMainServlet.mscFactory.createParameters();

		// Initialize 
		// Indicate that we want to do DTMF Async Continous Detection
		// note all timeouts are default to FOREEVER
		// These parameters must be set in order to trigger the forever detection

		collectOptions.put(SignalDetector.BUFFERING, Boolean.FALSE );
		EventType[] arrayEnabledEvents = {SignalDetectorEvent.SIGNAL_DETECTED} ;
		collectOptions.put(SignalDetector.ENABLED_EVENTS, arrayEnabledEvents);	
	}
	@Override
	public void invite(SipServletRequest req) {
		log.debug("Entering DlgcMgStopAsyncDetectorEnabledTest::invite");
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
		log.debug("Leaving DlgcMgStopAsyncDetectorEnabledTest::invite");
	}
	
	@Override
	//Called by the base class listener
	public boolean onSdpEvent(SdpPortManagerEvent anEvent) 
	{
		log.debug("Entering DlgcMgStopAsyncDetectorEnabledTest::onSdpEvent");
		boolean status = super.onSdpEvent(anEvent);
		log.debug("Entering DlgcMgStopAsyncDetectorEnabledTest::onSdpEvent");
		return status;
	}
	
	@Override
	//Called by the base class doAck
	//This indicates to the unit test that the connection is ready
	//and it can proceed with the detail stop test.
	public void ackResult(SipServletRequest req)
	{
		log.debug("Entering DlgcMgStopAsyncDetectorEnabledTest ackResult() ");
		//here you can do what ever you need to do after the connection between
		// the phone and the media server has been established
		//for this test we will call play announcement
		//during the announcement that is wait about 3 seconds then
		//call mg.stop() while announcement is playing
		SipSession ss = req.getSession();
		MediaGroup mg = (MediaGroup) ss.getAttribute("MEDIAGROUP");
		NetworkConnection nc = (NetworkConnection) ss.getAttribute("NETWORK_CONNECTION");
		try {
			mg.join(Joinable.Direction.DUPLEX, nc);
			MediaSession mediaSession = (MediaSession) ss.getAttribute("MEDIA_SESSION");
			log.debug("DlgcMgStopAsyncDetectorEnabledTest play a long announcement ");
			playAnnouncement(mg);
			log.debug("DlgcMgStopAsyncDetectorEnabledTest calling async detect");
			detect(mg,ss);
			log.debug("DlgcMgStopAsyncDetectorEnabledTest Sleep for 8 seconds before calling mg.stop() all ");
			try {
				Thread.sleep(10000);
				log.debug("DlgcMgStopPlayerOnlyEnabledTest calling mg.stop() ");
				mg.stop();
				log.debug("DlgcMgStopAsyncDetectorEnabledTest ackResult() - Returned from mg.stop() ");
				terminateSession(mediaSession);
			} catch (InterruptedException e) {
				log.error(e.toString());
			}
		} catch (MsControlException e1) {
			log.error(e1.toString());
		}
		log.debug("Leaving DlgcMgStopAsyncDetectorEnabledTest ackResult() ");
	}
	
	public void detect(MediaGroup mg, SipSession sipSession)
	{
		try {
			SignalDetector sg = mg.getSignalDetector();
			log.debug("Calling DlgcMgStopAsyncDetectorEnabledTest receiveSignals() ");
			sg.receiveSignals(-1, null, null, collectOptions);
			log.debug("Returning from DlgcMgStopAsyncDetectorEnabledTest receiveSignals() ");
		} catch (Exception e) {
			log.debug(e.toString());
			terminateSession(sipSession);
		}
	}
	
	void playAnnouncement(MediaGroup mg)
	{
		URI prompt = URI.create("file:////var/lib/xms/media/en_US/verification/demoJSR309/player/intro.wav");
		Parameters parameters = myMainServlet.mscFactory.createParameters();
		parameters.put(Player.REPEAT_COUNT, 10);
		parameters.put(Player.INTERVAL, 2000);
		parameters.put(Player.MAX_DURATION, Player.FOR_EVER);
		try
		{
			Player player = mg.getPlayer();
			player.play(prompt, RTC.NO_RTC, parameters);
		}
		catch (MsControlException e)
		{
			log.error(e.toString());
		}
	}

	class MySignalDetectorListener implements MediaEventListener<SignalDetectorEvent> , Serializable{
		
		private static final long serialVersionUID = 1L;
		DlgcMgStopBaseTestCase actualTest = null; 
		
		public MySignalDetectorListener(DlgcMgStopBaseTestCase parent)
		{
			actualTest = parent;
		}
		
		@Override
		public void onEvent(SignalDetectorEvent anEvent) 
		{
			log.debug("DlgcMgStopAsyncDetectorEnabledTest::MySignalDetectorListener::ReceiveSignals Event: "+anEvent);
			String dtmf = anEvent.getSignalString();		
		    if (anEvent.getEventType() == SignalDetectorEvent.SIGNAL_DETECTED) {
				
				log.debug("Collected: "+dtmf);
				log.debug("DlgcMgStopAsyncDetectorEnabledTest::MySignalDetectorListener::Digit Collected: "+ dtmf);
			}

			if (anEvent.getQualifier() == ResourceEvent.STOPPED)
			{
				log.debug("DlgcMgStopAsyncDetectorEnabledTest::MySignalDetectorListener::Terminating DtmfAsyncCollectionServlet due to a Stop Request");
				log.debug("DlgcMgStopAsyncDetectorEnabledTest::MySignalDetectorListener:: Doing Nada! with this event");
			}
			
		}
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
			log.debug("PlayerEventListener::onEvent()");
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

}



