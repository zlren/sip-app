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
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.mediagroup.FileFormatConstants;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.Recorder;
import javax.media.mscontrol.mediagroup.RecorderEvent;
import javax.media.mscontrol.mediagroup.SpeechDetectorConstants;
import javax.media.mscontrol.mediagroup.signals.SignalDetector;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.resource.RTC;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DlgcMgStopRecorderEnabledTest extends DlgcMgStopBaseTestCase 
{
	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory.getLogger(DlgcMgStopRecorderEnabledTest.class);
	SdpEventListener <SdpPortManagerEvent> mySdpEventListener = null;
	private static final String PROMPT_FILE_NAME_WAV = "file:////var/lib/xms/media/en_US/verification/demoJSR309/recorderTest/intro.wav";
	private String RECORD_FILE_NAME = "file:////tmp/mgstopRecord.ulaw";

	
	public DlgcMgStopRecorderEnabledTest(DlgcMgStopTest mainServlet) {
		super(mainServlet);
		mySdpEventListener = new  SdpEventListener<SdpPortManagerEvent>(this);
	}
	

	@Override
	public void invite(SipServletRequest req) {
		log.debug("Entering DlgcMgStopRecorderEnabledTest::invite");
		SipSession ss = req.getSession();
		ss.setAttribute("TEST_CASE", this);
		try {
			super.executeInvite(req, mySdpEventListener);
			MediaGroup mg = (MediaGroup) ss.getAttribute("MEDIAGROUP");
			mg.getRecorder().addListener(new RecorderEventListener(myMainServlet));
		} catch (ServletException e) {
			log.error(e.toString());
		} catch (IOException e) {
			log.error(e.toString());
		} catch (MsControlException e) {
			log.error(e.toString());
		}
		log.debug("Leaving DlgcMgStopRecorderEnabledTest::invite");
	}
	
	@Override
	//Called by the base class listener
	public boolean onSdpEvent(SdpPortManagerEvent anEvent) 
	{
		log.debug("Entering DlgcMgStopRecorderEnabledTest::onSdpEvent");
		boolean status = super.onSdpEvent(anEvent);
		log.debug("Entering DlgcMgStopRecorderEnabledTest::onSdpEvent");
		return status;
	}
	
	@Override
	//Called by the base class doAck
	//This indicates to the unit test that the connection is ready
	//and it can proceed with the detail stop test.
	public void ackResult(SipServletRequest req)
	{
		log.debug("Entering DlgcMgStopRecorderEnabledTest::ackResult ");
		//here you can do what ever you need to do after the connection between
		// the phone and the media server has been established
		//for this test we will record
		//during the the recording it wait about 20 seconds then
		//call mg.stop() while recording
		SipSession ss = req.getSession();
		MediaGroup mg = (MediaGroup) ss.getAttribute("MEDIAGROUP");
		NetworkConnection nc = (NetworkConnection) ss.getAttribute("NETWORK_CONNECTION");
		try {
			mg.join(Joinable.Direction.DUPLEX, nc);
			MediaSession mediaSession = (MediaSession) ss.getAttribute("MEDIA_SESSION");
			log.debug("DlgcMgStopRecorderEnabledTest:ackResult:start recording ");
			record(mg);
			log.debug("DlgcMgStopRecorderEnabledTest:ackResult:Sleep for 20 seconds before calling mg.stop() all ");
			try {
				Thread.sleep(20000);
				log.debug("DlgcMgStopRecorderEnabledTest:ackResult:calling mg.stop() ");
				mg.stop();
				log.debug("DlgcMgStopRecorderEnabledTest:ackResult:ackResult() - Returned from mg.stop() ");
				terminateSession(mediaSession);
			} catch (InterruptedException e) {
				log.error(e.toString());
			}
		} catch (MsControlException e1) {
			log.error(e1.toString());
		}
		log.debug("Leaving DlgcMgStopRecorderEnabledTest:ackResult() ");
	}

	void record(MediaGroup mg)
	{
		log.debug("Entering DlgcMgStopRecorderEnabledTest::record() ");
		Parameters parameters = myMainServlet.mscFactory.createParameters();

		URI prompt = URI.create("file:////var/lib/xms/media/en_US/verification/main_menu.wav");
		URI prompt2 = URI.create(PROMPT_FILE_NAME_WAV);
		URI[] twoPrompts = { prompt, prompt2 };
		parameters.put(Recorder.PROMPT, twoPrompts);

		
		parameters.put(Recorder.APPEND, Boolean.FALSE);
			
		parameters.put(Recorder.MIN_DURATION, new Integer(5000)); 	//any recording over this time constitutes a valid recording.
		parameters.put(Recorder.MAX_DURATION, new Integer(30000));  //30 seconds recording
		//parameters.put(Recorder.AUDIO_CODEC, CodecConstants.ALAW_PCM_64K); 
		//parameters.put(Recorder.FILE_FORMAT, FileFormatConstants.WAV);
		//parameters.put(Recorder.AUDIO_CODEC, CodecConstants.LINEAR_16BIT_128K); 
		parameters.put(Recorder.SILENCE_TERMINATION_ON, new Boolean(Boolean.TRUE));
		parameters.put(SpeechDetectorConstants.FINAL_TIMEOUT, 3000);   //3 sec   default is 4 secs if value not set
		parameters.put(SpeechDetectorConstants.INITIAL_TIMEOUT, 6000);   //6 sec   default is 9 secs if value not set - need MSML to publish new schema for it to work June 2013
		
		//parameters.put(Recorder.VIDEO_CODEC, CodecConstants.H263); //video support no 100% ready remove for now
	
		//note implementation is only on Pattern[0] only
		//enables pattern for Recorder to detect only use in MSML XMS
		//If #000 in this case is detected, the recording will stop
		parameters.put(SignalDetector.PATTERN[0], "#000");		
		parameters.put(SignalDetector.INTER_SIG_TIMEOUT, new Integer(10000));	//10 sec
		
		parameters.put(Recorder.FILE_FORMAT, FileFormatConstants.INFERRED);
		
		/**if (vRecAudioCodecs!=null)
			parameters.put(Recorder.AUDIO_CODEC, vRecAudioCodecs);
		if (iRecAudioClockRate!=null)
			parameters.put(Recorder.AUDIO_CLOCKRATE, iRecAudioClockRate);
		***/
		
		try
		{
			RTC[] rtcs = new RTC[1];
			rtcs[0] = MediaGroup.SIGDET_STOPPLAY;		//play barge in ONLY RTC SUPPORTED
			URI recordingDestURI = URI.create(this.RECORD_FILE_NAME);
			Recorder testRecorder = mg.getRecorder();
			log.debug("DlgcMgStopRecorderEnabledTest:: calling start record");
			testRecorder.record(recordingDestURI, rtcs, parameters);
			log.debug("DlgcMgStopRecorderEnabledTest:: returned from start record");	
		}
		catch (MsControlException e)
		{
			e.printStackTrace();
		} 
	}
	
	private class RecorderEventListener implements MediaEventListener<RecorderEvent>,  Serializable
	{

		private static final long serialVersionUID = 1;
		protected DlgcMgStopTest myServlet;

		public RecorderEventListener(DlgcMgStopTest servlet) {
			myServlet = myMainServlet;
		}

		@Override
		public void onEvent(RecorderEvent event)
		{
			log.debug("RecorderEventListener::onEvent()");
			log.debug("   EVENT TYPE : " + event.getEventType());
			log.debug("    QUALIFIER : " + event.getQualifier());
			log.debug("        ERROR : " + event.getError());
			log.debug("   ERROR TEXT : " + event.getErrorText());

			if ( event.isSuccessful() ) {
				log.debug("Received Recorder Event is successful");
			} else {
				log.error("Received Recorder Event is NOT successful");
			}

		}
	}
}
