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

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.Value;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.mediagroup.CodecConstants;
import javax.media.mscontrol.mediagroup.FileFormatConstants;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.Player;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.mediagroup.Recorder;
import javax.media.mscontrol.mediagroup.RecorderEvent;
import javax.media.mscontrol.mediagroup.SpeechDetectorConstants;
import javax.media.mscontrol.mediagroup.signals.SignalDetector;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.resource.RTC;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vendor.dialogic.javax.media.mscontrol.msml.RootType;
import com.vendor.dialogic.javax.media.mscontrol.msmlProtocol.DlgcSipB2BUAMSMLProtocol;

import testing.DlgcTest;

public class DlgcRecorderTest extends DlgcTest
{
	
	private static final long serialVersionUID = 1;
	
	Integer iRecMinDuration;
	Integer iRecMaxDuration;
	Integer iRecInitialTimeout;
	Integer iRecFinalTimeout;
	Boolean bRecSilenceTerminationFlag;
	
	//video parameters
	Value vRecVideoCodecs=null;
	Value vRecAudioCodecs=null;
	Value vRecFileFormat=null;
	String sRecVideoProfile=null;
	String sRecVideoLevel=null;
	String sRecVideoWidth=null;
	String sRecVideoHeight=null;
	String sRecVideoFramerate=null;
	Integer iRecVideoMaxBitRate=null;
	Integer iRecAudioClockRate=null;
	
	static public  final Map<String, Value> supportedFileFormatTable = new HashMap<String,Value>();
	
	static {
		supportedFileFormatTable.put("INFERRED",FileFormatConstants.INFERRED);
		supportedFileFormatTable.put("RAW",FileFormatConstants.RAW);
		supportedFileFormatTable.put("WAV",FileFormatConstants.WAV);
		//supportedFileFormatTable.put("mp4v_es",CodecConstants.MP4V_ES);
	}
	
static public  final Map<String, Value> supportedVideoCodecTable = new HashMap<String,Value>();
	
	static {
		supportedVideoCodecTable.put("h263",CodecConstants.H263);
		supportedVideoCodecTable.put("h263_1998",CodecConstants.H263_1998);
		supportedVideoCodecTable.put("h264",CodecConstants.H264);
		supportedVideoCodecTable.put("mp4v_es",CodecConstants.MP4V_ES);
	}
	
	static public  final Map<String,Value> supportedAudioCodecTable = new HashMap<String,Value>();
	
	static {
		//msmlSupportedCodecTable.put(CodecConstants.ALAW_PCM_48K, new DlgcSipB2BUAMSMLProtocol.CodecData(8,6,"g711alaw"));
		supportedAudioCodecTable.put("ALAW_PCM_64K",CodecConstants.ALAW_PCM_64K);
		supportedAudioCodecTable.put("MULAW_PCM_64K",CodecConstants.MULAW_PCM_64K);
		supportedAudioCodecTable.put("LINEAR_16BIT_128K",CodecConstants.LINEAR_16BIT_128K);
		supportedAudioCodecTable.put("LINEAR_16BIT_256K",CodecConstants.LINEAR_16BIT_256K);
		supportedAudioCodecTable.put("LINEAR_8BIT_64K",CodecConstants.LINEAR_8BIT_64K);
		supportedAudioCodecTable.put("AMR",CodecConstants.AMR);
		supportedAudioCodecTable.put("AMR_WB",CodecConstants.AMR_WB);

		//
		//msmlSupportedCodecTable.put(CodecConstants.ADPCM_16K_G726, new DlgcSipB2BUAMSMLProtocol.CodecData(8,2,"g726"));
		//msmlSupportedCodecTable.put(CodecConstants.ADPCM_32K_G726, new DlgcSipB2BUAMSMLProtocol.CodecData(8,4,"g726"));
		//msmlSupportedCodecTable.put(CodecConstants.G729_A, new DlgcSipB2BUAMSMLProtocol.CodecData(8,16,"g729a"));
		
	}
	
	@Override
	public void init() throws ServletException {
		try {
			super.init();
			myServletLoaded = true;
		} catch (Exception e) {
			throw new ServletException(
					"Cannot initialize DlgcRecorderTest due to internale service error",
					e);
		}
	}
	
	@Override
	public void servletInitialized(SipServletContextEvent evt){

		String sName = evt.getSipServlet().getServletName();


		if ( ( platform != null ) && (platform.compareToIgnoreCase(DlgcTest.TELESTAX_PLATFORM) == 0 ) || ( platform != null ) && (platform.compareToIgnoreCase(DlgcTest.ORACLE_PLATFORM) == 0 )) {
			if( sName.equalsIgnoreCase("DlgcSipServlet") )
			{
				dlgcSipServletLoaded = true;
				log.debug(" DlgcRecorderTest::servletInitialized DlgcSipServlet loaded");			
			}
			
			else if( sName.equalsIgnoreCase("DlgcRecorderTest") ) 
					playerServletInitCalled =true;

			if( playerServletInitCalled && dlgcSipServletLoaded)
			{
				if ( servletInitializedFlag == false ) {
					log.debug("Entering DlgcRecorderTest::servletInitialized servletName: " + sName);			
					servletInitializedFlag = true;
					initDriver();
					myServletInitialized(evt);
				} else {
					log.debug("DlgcRecorderTest::servletInitialized(): already servletInitialized was called...debouncing " + sName);
				}
			}

		} else { //TROPO Framework

			if( sName.equalsIgnoreCase("DlgcSipServlet") )
			{
				dlgcSipServletLoaded = true;
				log.debug(" DlgcRecorderTest::servletInitialized DlgcSipServlet loaded");			
				return;
			}

			if( (sName.equalsIgnoreCase("DlgcRecorderTest") ) && dlgcSipServletLoaded)
			{
				if ( servletInitializedFlag == false ) {
					log.debug("Entering DlgcRecorderTest::servletInitialized servletName: " + sName);			
					servletInitializedFlag = true;
					initDriver();
					myServletInitialized(evt);
				} else {
					log.debug("DlgcRecorderTest::servletInitialized(): already servletInitialized was called...debouncing " + sName);
				}
			}
		}

	}

	
	@Override
	public void doAck(SipServletRequest req)
	{
		SipSession sipSession = req.getSession();
		NetworkConnection networkConnection = (NetworkConnection) sipSession.getAttribute("NETWORK_CONNECTION");
		try
		{
			byte[] remoteSdp = req.getRawContent();
			if (remoteSdp != null)
			{
				networkConnection.getSdpPortManager().processSdpAnswer(remoteSdp);
			}
		}
		catch (MsControlException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		

		MediaGroup mediaGroup = (MediaGroup) sipSession.getAttribute("MEDIAGROUP");
		Parameters parameters = mscFactory.createParameters();

		//two prompt test fix for MSC-112 Provide ability to pass an array of prompts to be played when recording
		URI prompt = URI.create("file:////var/lib/xms/media/en_US/verification/main_menu.wav");
		URI prompt2 = URI.create(PROMPT_FILE_NAME_WAV);
		URI[] twoPrompts = { prompt, prompt2 };
		parameters.put(Recorder.PROMPT, twoPrompts);
		//parameters.put(Recorder.PROMPT, prompt);
		//parameters.put(Recorder.PROMPT, URI.create(PROMPT_FILE_NAME_WAV));

		parameters.put(Recorder.APPEND, Boolean.FALSE);
			
		//parameters.put(Recorder.MIN_DURATION, new Integer(5000)); 	//any recording over this time constitutes a valid recording.
		parameters.put(Recorder.MIN_DURATION, this.iRecMinDuration); 	//any recording over this time constitutes a valid recording.
		//parameters.put(Recorder.MAX_DURATION, new Integer(30000));  //30 seconds recording
		parameters.put(Recorder.MAX_DURATION, this.iRecMaxDuration);  //30 seconds recording
		//parameters.put(Recorder.AUDIO_CODEC, CodecConstants.ALAW_PCM_64K); 
		//parameters.put(Recorder.FILE_FORMAT, FileFormatConstants.WAV);
		//parameters.put(Recorder.AUDIO_CODEC, CodecConstants.LINEAR_16BIT_128K); 
		//parameters.put(Recorder.SILENCE_TERMINATION_ON, new Boolean(Boolean.TRUE));
		parameters.put(Recorder.SILENCE_TERMINATION_ON, this.bRecSilenceTerminationFlag);
		//parameters.put(SpeechDetectorConstants.FINAL_TIMEOUT, 3000);   //3 sec   default is 4 secs if value not set
		parameters.put(SpeechDetectorConstants.FINAL_TIMEOUT, this.iRecFinalTimeout);   //3 sec   default is 4 secs if value not set
		//parameters.put(SpeechDetectorConstants.INITIAL_TIMEOUT, 6000);   //6 sec   default is 9 secs if value not set - need MSML to publish new schema for it to work June 2013
		parameters.put(SpeechDetectorConstants.INITIAL_TIMEOUT, this.iRecInitialTimeout);   //6 sec   default is 9 secs if value not set - need MSML to publish new schema for it to work June 2013
		
		//parameters.put(Recorder.VIDEO_CODEC, CodecConstants.H263); //video support no 100% ready remove for now
	
		//note implementation is only on Pattern[0] only
		parameters.put(SignalDetector.PATTERN[0], "#000");		//enables pattern for Recorder to detect only use in MSML XMS
		parameters.put(SignalDetector.INTER_SIG_TIMEOUT, new Integer(10000));	//10 sec
	
		//RTC rtcStop = new RTC(SignalDetector.DETECTION_OF_ONE_SIGNAL, Recorder.STOP);  //not needed... can be ignore since RTC is not implemented
		
		if (vRecFileFormat!=null)
			parameters.put(Recorder.FILE_FORMAT, vRecFileFormat);
		
		if (vRecAudioCodecs!=null)
			parameters.put(Recorder.AUDIO_CODEC, vRecAudioCodecs);
		if (iRecAudioClockRate!=null)
			parameters.put(Recorder.AUDIO_CLOCKRATE, iRecAudioClockRate);
		
		if (vRecVideoCodecs!=null)
		{
			parameters.put(Recorder.VIDEO_CODEC, vRecVideoCodecs);
			String sVideoFMTP="";
			if (sRecVideoProfile!=null)
				sVideoFMTP+="profile="+sRecVideoProfile;
			if (sRecVideoLevel!=null)
				sVideoFMTP+=";level="+sRecVideoLevel;
			if (sRecVideoWidth!=null)
				sVideoFMTP+=";width="+sRecVideoWidth;
			if (sRecVideoHeight!=null)
				sVideoFMTP+=";height="+sRecVideoHeight;
			if (sRecVideoFramerate!=null)
				sVideoFMTP+=";framerate="+sRecVideoFramerate;
			parameters.put(Recorder.VIDEO_FMTP, sVideoFMTP);
		}
		if (iRecVideoMaxBitRate!=null)
			parameters.put(Recorder.VIDEO_MAX_BITRATE, iRecVideoMaxBitRate);
		
		
		try
		{
			RTC[] rtcs = new RTC[1];
			rtcs[0] = MediaGroup.SIGDET_STOPPLAY;		//play barge in
		//	rtcs[1] = MediaGroup.SIGDET_STOPRECORD;     //recorder turn key set to # 
			//mediaGroup.getRecorder().record(URI.create(RECORD_FILE_NAME), new RTC[]{rtcStop}, parameters);
			URI recordingDestURI = URI.create(this.RECORD_FILE_NAME);
			Recorder testRecorder = mediaGroup.getRecorder();
			log.debug("DlgcRecorderTest:: calling start record");
			testRecorder.record(recordingDestURI, rtcs, parameters);
			log.debug("DlgcRecorderTest:: returned from start record");
			
			//test recorder stop
			//log.debug("calling recorder stop");
			//Thread.sleep(1000);
			//testRecorder.stop();
			//log.debug("returning from      recorder stop");

			

			
		}
		catch (MsControlException e)
		{
			e.printStackTrace();
		} 
	}
	
	private void loadRecorderProperties()
	{
		
		String propString=null;
		
		String testFile = demoPropertyObj.getProperty("record.test.file");
		if ( testFile != null ){
			RECORD_FILE_NAME = testFile;
			log.debug("Using testFile destination as per configuration file: " + RECORD_FILE_NAME);
		} else {
			log.debug("Using testFile destination as per default: " + RECORD_FILE_NAME);
		}
		
		propString = demoPropertyObj.getProperty("DlgcRecorderDemo.record.minDuration");
		if ( propString != null ){
			iRecMinDuration = new Integer(propString);
			log.debug("DlgcRecorderDemo.record.minDuration: " + iRecMinDuration.toString());
		} else {
			iRecMinDuration = new Integer(5000);
			log.debug("DlgcRecorderDemo.record.minDuration default: " + iRecMinDuration.toString());
		}
		propString = demoPropertyObj.getProperty("DlgcRecorderDemo.record.maxDuration");
		if ( propString != null ){
			iRecMaxDuration = new Integer(propString);
			log.debug("DlgcRecorderDemo.record.maxDuration: " + iRecMaxDuration.toString());
		} else {
			iRecMaxDuration = new Integer(30000);
			log.debug("DlgcRecorderDemo.record.maxDuration default: " + iRecMaxDuration.toString());
		}
		propString = demoPropertyObj.getProperty("DlgcRecorderDemo.record.initialTimeout");
		if ( propString != null ){
			iRecInitialTimeout = new Integer(propString);
			log.debug("DlgcRecorderDemo.record.initialTimeout: " + iRecInitialTimeout.toString());
		} else {
			iRecInitialTimeout = new Integer(6000);
			log.debug("DlgcRecorderDemo.record.initialTimeout default: " + iRecInitialTimeout.toString());
		}
		propString = demoPropertyObj.getProperty("DlgcRecorderDemo.record.finalTimeout");
		if ( propString != null ){
			iRecFinalTimeout = new Integer(propString);
			log.debug("DlgcRecorderDemo.record.finalTimeout: " + iRecFinalTimeout.toString());
		} else {
			iRecFinalTimeout = new Integer(3000);
			log.debug("DlgcRecorderDemo.record.finalTimeout default: " + iRecFinalTimeout.toString());
		}
		propString = demoPropertyObj.getProperty("DlgcRecorderDemo.record.silenceTerminationFlag");
		if ( propString != null ){
			bRecSilenceTerminationFlag = new Boolean(propString);
			log.debug("DlgcRecorderDemo.record.silenceTerminationFlag: " + bRecSilenceTerminationFlag.toString());
		} else {
			bRecSilenceTerminationFlag = new Boolean(Boolean.TRUE);
			log.debug("DlgcRecorderDemo.record.silenceTerminationFlag default: " + bRecSilenceTerminationFlag.toString());
		}
		
		propString = demoPropertyObj.getProperty("record.test.video.codecs");
		if ( propString != null )
		{
			vRecVideoCodecs = supportedVideoCodecTable.get(propString);
			log.debug("record.test.video.codecs: " + propString);
		} 
		
		propString = demoPropertyObj.getProperty("record.test.video.profile");
		if ( propString != null )
		{
			sRecVideoProfile = propString;
			log.debug("record.test.video.profile: " + propString);
		} 
		
		propString = demoPropertyObj.getProperty("record.test.video.level");
		if ( propString != null )
		{
			sRecVideoLevel = propString;
			log.debug("record.test.video.level: " + propString);
		} 
		
		propString = demoPropertyObj.getProperty("record.test.video.width");
		if ( propString != null )
		{
			sRecVideoWidth = propString;
			log.debug("record.test.video.width: " + propString);
		} 
		
		propString = demoPropertyObj.getProperty("record.test.video.height");
		if ( propString != null )
		{
			sRecVideoHeight = propString;
			log.debug("record.test.video.height: " + propString);
		} 
		
		propString = demoPropertyObj.getProperty("record.test.video.framerate");
		if ( propString != null )
		{
			sRecVideoFramerate = propString;
			log.debug("record.test.video.framerate: " + propString);
		} 
		
		propString = demoPropertyObj.getProperty("record.test.video.maxbitrate");
		if ( propString != null )
		{
			iRecVideoMaxBitRate = Integer.valueOf(propString);
			log.debug("record.test.video.maxbitrate: " + propString);
		} 
		
		
		propString = demoPropertyObj.getProperty("record.test.audio.codecs");
		if ( propString != null )
		{
			vRecAudioCodecs = supportedAudioCodecTable.get(propString);;
			log.debug("record.test.audio.codecs: " + propString);
		} 
		
		propString = demoPropertyObj.getProperty("record.test.fileformat");
		if ( propString != null )
		{
			vRecFileFormat = supportedFileFormatTable.get(propString);;
			log.debug("record.test.fileformat: " + propString);
		} 
		
		propString = demoPropertyObj.getProperty("record.test.audio.clockrate");
		if ( propString != null )
		{
			iRecAudioClockRate = Integer.valueOf(propString);
			log.debug("record.test.audio.clockrate: " + propString);
		} 
		
		
	}
	
	@Override
	public void doInvite(final SipServletRequest req)
		throws ServletException, IOException
	{
		log.info("doInvite");
		
		NetworkConnection networkConnection = null;
		this.loadRecorderProperties();
	
		
		
		if (req.isInitial())
		{
			try 
			{
				MediaSession mediaSession = mscFactory.createMediaSession();
				networkConnection = mediaSession.createNetworkConnection(NetworkConnection.BASIC);
				MediaGroup mediaGroup = mediaSession.createMediaGroup(MediaGroup.PLAYER_RECORDER_SIGNALDETECTOR);
				
				SipSession sipSession = req.getSession();
				sipSession.setAttribute("MEDIA_SESSION", mediaSession);
				sipSession.setAttribute("NETWORK_CONNECTION", networkConnection);
				sipSession.setAttribute("MEDIAGROUP", mediaGroup);
				
				mediaSession.setAttribute("SIP_SESSION", sipSession);
				mediaSession.setAttribute("SIP_REQUESTnetworkConnection", req);
				mediaSession.setAttribute("NETWORK_CONNECTION", networkConnection);
				
				networkConnection.getSdpPortManager().addListener(speListener);
				mediaGroup.getRecorder().addListener(new RecorderEventListener());
				mediaGroup.getPlayer().addListener(new PlayerEventListener());
				
				mediaGroup.join(Joinable.Direction.DUPLEX, networkConnection);
			}
			catch (MsControlException e)
			{
				req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
			}
		}
		else
		{
			
		}
		
		try
		{
			req.getSession().setAttribute("UNANSWERED_INVITE", req);
			
			byte[] remoteSdp = req.getRawContent();
			
			if (remoteSdp == null)
			{
				networkConnection.getSdpPortManager().generateSdpOffer();
			}
			else
			{
				networkConnection.getSdpPortManager().processSdpOffer(remoteSdp);
			}
		} 
		catch (MsControlException e)
		{
			req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
		}
	}
	
	@Override
	protected void doResponse(SipServletResponse response)
		throws IOException, ServletException
	{
		SipSession session = response.getSession();
		if (response.getRequest().getMethod().equals("BYE"))
		{
			terminateSession(session);
		}
	}
	
	private void terminateSession(SipSession session)
	{
		if (session != null)
		{
			MediaSession mediaSession = (MediaSession) session.getAttribute("MEDIA_SESSION");
			mediaSession.release();
			session.invalidate();
			session.getApplicationSession().invalidate();
		}
		
	}
	
	private class PlayerEventListener implements MediaEventListener<PlayerEvent>, Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7904645633486909974L;

		@Override
		public void onEvent(PlayerEvent event)
		{
			log.info("PlayerEventListener::onEvent()");
			log.info("   EVENT TYPE : " + event.getEventType());
			log.info("    QUALIFIER : " + event.getQualifier());
			log.info("  CHANGE TYPE : " + event.getChangeType());
			log.info("   PLAY INDEX : " + event.getIndex());
			log.info("  PLAY OFFSET : " + event.getOffset());
			log.info("        ERROR : " + event.getError());
			log.info("   ERROR TEXT : " + event.getErrorText());

			if ( event.getEventType() == PlayerEvent.RESUMED ) {
				log.info("Received Player Event: Play Resumed which means Play Started...");
			} else {

				MediaSession mediaSession = event.getSource().getMediaSession();
				SipSession session = (SipSession) mediaSession.getAttribute("SIP_SESSION");
				SipServletRequest request = session.createRequest("BYE");
				try
				{
					request.send();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}	
	}
	
	private class RecorderEventListener implements MediaEventListener<RecorderEvent> ,Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 3280547173385430391L;

		@Override
		public void onEvent(RecorderEvent event)
		{
			log.info("RecorderEventListener::onEvent()");
			log.info("   EVENT TYPE : " + event.getEventType());
			log.info("    QUALIFIER : " + event.getQualifier());
			log.info("     DURATION : " + event.getDuration());
			log.info("        ERROR : " + event.getError());
			log.info("   ERROR TEXT : " + event.getErrorText());

			MediaGroup mediaGroup = event.getSource().getContainer();

			if ( event.getDuration() != 0 ) {


				try
				{
					log.debug("RecorderEventListener::calling play on the just recorded voice mail");
					MediaSession mediaSession = event.getSource().getMediaSession();
					Parameters params = mediaSession.createParameters();
					params.put(Player.MAX_DURATION, 10000);
					params.put(Player.FILE_FORMAT, vRecFileFormat);
					params.put(Player.AUDIO_CODEC, vRecAudioCodecs); 
					
					
					
					mediaGroup.getPlayer().play(URI.create(RECORD_FILE_NAME), RTC.NO_RTC, params);
				}
				catch (MsControlException e)
				{
					e.printStackTrace();
				}
			} else {
				log.debug("RecorderEventListener::not calling play since the recording duration is zero");
				MediaSession mediaSession = event.getSource().getMediaSession();
				SipSession session = (SipSession) mediaSession.getAttribute("SIP_SESSION");
				SipServletRequest request = session.createRequest("BYE");
				try
				{
					request.send();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}	
	}
	
	protected Configuration<MediaGroup> configuration;
	
	private static final String PROMPT_FILE_NAME_WAV = "file:////var/lib/xms/media/en_US/verification/demoJSR309/recorderTest/intro.wav";
	private String RECORD_FILE_NAME = "file:////tmp/name_recorder.ulaw";
	private static Logger log = LoggerFactory.getLogger(DlgcRecorderTest.class);

}
