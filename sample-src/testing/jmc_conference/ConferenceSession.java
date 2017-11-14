/**
 * DIALOGIC CONFIDENTIAL      
 * Copyright (C) 2005-2014 Dialogic Corporation. All Rights Reserved.
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

package testing.jmc_conference;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Vector;

import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.MediaEvent;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.Joinable.Direction;
import javax.media.mscontrol.mediagroup.CodecConstants;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.mediagroup.Recorder;
import javax.media.mscontrol.mediagroup.RecorderEvent;
import javax.media.mscontrol.mediagroup.SpeechDetectorConstants;
import javax.media.mscontrol.mediagroup.signals.SignalDetector;
import javax.media.mscontrol.mixer.MediaMixer;
import javax.media.mscontrol.resource.AllocationEvent;
import javax.media.mscontrol.resource.AllocationEventListener;
import javax.media.mscontrol.resource.RTC;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.jmc_conference.JMCConferenceServlet.State;

//import com.vendor.dialogic.javax.media.mscontrol.sip.DlgcSipReqProxyId;

/**
 * Conference room - Has its own MediaSession, and a MediaMixer
 */
public class ConferenceSession implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Conference MSC objects
	//private final MediaSession myMediaSession;
	//private final MediaMixer myMediaMixer;
	//private final MediaGroup myMediaGroup;
	
	// Conference identifier and participants list 
	private String confId;
	
	/*
	 * John:
	 * If serializaiton is set to on:
	 * 		Regular conference works
	 * 		otherwise the conference 9999 does not work
	 * In order to make the conference 9999 work need to declare the List static
	 */
	private List<SipSession> myParticipants;		
													
	private long conferenceStartTime;
	
	private JMCConferenceServlet jcs;			
	private Boolean conferenceValid = false;
	private MediaSession myMediaSession;
	protected DlgcConferenceMonitor monitor = null;
		
	public static Logger log = LoggerFactory.getLogger(ConferenceSession.class);
	
	public enum RecordingState  { NOT_RECORDING, RECORDING_STARTING_PENDING, RECORDING, STOP_RECODING_PENDING };
	
	RecordingState  recordingState;
	
	// ConferenceSession does not exist, create a new one
	/**
	 * Constructor - Instantiate MSC, add the creator to the Participants list
	 * 
	 * @param confId
	 * @param aParticipant
	 * @throws MsControlException
	 */
	public ConferenceSession(JMCConferenceServlet jcs, String confId, SipSession sipSession)
			throws MsControlException {
		
		this.jcs = jcs;
		recordingState = RecordingState.NOT_RECORDING;
		
		monitor =null;
		// Store the conference ID
		this.confId = confId;
		// Store conference participants in myParticipants
		myParticipants = new Vector<SipSession>();
		
		// Create media session, conference and mediagroup for conference
		myMediaSession = JMCConferenceServlet.theMediaSessionFactory.createMediaSession();
		
		log.debug("DialogicConferenceDemo::ConnferenceSession:: setting Mixer number of ports to 5");
        Parameters options = myMediaSession.createParameters();
        options.put(MediaMixer.MAX_PORTS, 5);
		
		String mxMode = JMCConferenceServlet.demoPropertyObj.getProperty("DlgcMultiConferenceDemo.media.mixer.mode");
		String confSize = JMCConferenceServlet.demoPropertyObj.getProperty("DlgcMultiConferenceDemo.media.mixer.video.size");
		Configuration<MediaMixer> mc=null;
		conferenceValid =false;
		
		this.loadRecorderProperties();
	
		if (confSize==null)
			confSize = "CIF";
		
		if (mxMode==null)
			mc = MediaMixer.AUDIO;
		else
		{
			if (mxMode.equalsIgnoreCase("AUDIO_VIDEO"))
				mc = MediaMixer.AUDIO_VIDEO;
			else
				mc = MediaMixer.AUDIO;
		}
		MediaMixer myMediaMixer= myMediaSession.createMediaMixer(mc,options);
		myMediaSession.setAttribute("CONFERENCE_VIDEO_SIZE", confSize);
		myMediaSession.setAttribute("MEDIA_MIXER", myMediaMixer);
		myMediaSession.setAttribute("PARTICIPANTS", "1");
		
		myMediaMixer.addListener(new MixerAllocationEventListener(this,jcs));
				
		MediaGroup myMediaGroup = myMediaSession.createMediaGroup(MediaGroup.PLAYER_RECORDER_SIGNALDETECTOR);
		myMediaSession.setAttribute("MEDIA_GROUP", myMediaGroup);

		
		myMediaGroup.getRecorder().addListener(new MixerRecorderEventListener(this,jcs));
		myMediaGroup.getPlayer().addListener(new MixerPlayerEventListener(this,jcs));

		// Join the mediagroup to the conference
		myMediaSession.setAttribute("CONFERENCE_OWNER_SIPSESSION", sipSession);
		
		// Initialize conference session times
		conferenceStartTime = System.currentTimeMillis()/1000;
		addParticipant(sipSession);
		
		log.debug("DialogicConferenceDemo::ConnferenceSession:: joining conference media group with mixer");
		myMediaGroup.join(Direction.DUPLEX, myMediaMixer);
		
		
	}

	/**
	 * Add a participant to this conference room
	 * 
	 * @param aParticipant
	 */
	public void addParticipant(SipSession session) {
		log.debug("===========================> ADDING #"+myParticipants.size());
		myParticipants.add(session);
		session.setAttribute("PARTICIPANT_ID", myParticipants.size());
		myMediaSession.setAttribute("CONFERENCE_SESSION", this);
	}

	/**
	 * Remove a participant from this conference room, delete the conference
	 * when there is no more participant
	 * 
	 * @param aParticipant
	 */
	public void removeParticipant(SipSession session) {
		if (myParticipants.contains(session)) {
			myParticipants.remove(session);
		}
		myMediaSession.setAttribute("CONFERENCE_SESSION", this);
	}

	public int getNumParticipant() {
		return myParticipants.size();
	}
	
	public boolean confirm(DlgcConferenceMonitor monitor) {
		this.monitor = monitor;
		MediaMixer theMixer = (MediaMixer) myMediaSession.getAttribute("MEDIA_MIXER");
		boolean confirmWasSent = true;
		if ( theMixer != null ) {
			log.debug("Callig Media Mixer confirm request..must wait for Mixer Allocation Asynchronous Event");
			try {
				theMixer.confirm();
				//synchronize it wait for confirm here
			} catch (MsControlException e) {
				log.debug(e.toString());
				confirmWasSent =false;
			}
		}else{
			confirmWasSent =false;
			log.error("Could not find Media Mixer Objectd to perform confirm request");
		}
		return confirmWasSent;
	}
	
	public SipSession getConferenceOwner() {
		return (SipSession)myMediaSession.getAttribute("CONFERENCE_OWNER_SIPSESSION");
	}

	public MediaSession getMediaSession() {
		return myMediaSession;
	}

	public MediaMixer getMediaMixer() {
		return (MediaMixer)getMediaSession().getAttribute("MEDIA_MIXER");
	}

	public MediaGroup getMediaGroup() {
		return (MediaGroup)getMediaSession().getAttribute("MEDIA_GROUP");
	}

	public SipSession getParticipant(int participantNumber) {
		return myParticipants.get((participantNumber-1));
	}

	public String getConfId() {
		return confId;
	}
	
	private static final String XMS_RECORDING_DIR = "file:////tmp/";

	public void record(SipSession mySipSession,String recordingFileName) throws MsControlException {
		log.debug("Entering ConferenceSession:: record");
		
		if ( recordingState != RecordingState.NOT_RECORDING )
		{
			log.debug("ConferenceSession:: record request rejected since Recording State is not in Not Recording");
			log.debug("ConferenceSession:: This means that the conference is some type of recording state already");
			return;
		}
		
		MediaGroup mixerMG = this.getMediaGroup();
		Parameters parameters = mixerMG.createParameters();

		parameters.put(Recorder.APPEND, Boolean.FALSE);
			
		parameters.put(Recorder.MIN_DURATION, this.iRecMinDuration); 	//any recording over this time constitutes a valid recording.
		parameters.put(Recorder.MAX_DURATION, this.iRecMaxDuration);  //30 seconds recording
		parameters.put(Recorder.AUDIO_CODEC, CodecConstants.ALAW_PCM_64K); 
		parameters.put(Recorder.AUDIO_CODEC, CodecConstants.LINEAR_16BIT_128K); 
		//parameters.put(Recorder.AUDIO_CODEC, CodecConstants.ALAW_PCM_64K); 
		//parameters.put(Recorder.FILE_FORMAT, FileFormatConstants.WAV);
		//parameters.put(Recorder.AUDIO_CODEC, CodecConstants.LINEAR_16BIT_128K); 
		//parameters.put(Recorder.SILENCE_TERMINATION_ON, this.bRecSilenceTerminationFlag);
		//parameters.put(SpeechDetectorConstants.FINAL_TIMEOUT, this.iRecFinalTimeout);   //3 sec   default is 4 secs if value not set
		//parameters.put(SpeechDetectorConstants.INITIAL_TIMEOUT, this.iRecInitialTimeout);   //6 sec   default is 9 secs if value not set - need MSML to publish new schema for it to work June 2013
		
		//parameters.put(Recorder.VIDEO_CODEC, CodecConstants.H263); //video support no 100% ready remove for now
	
		//note implementation is only on Pattern[0] only
		//parameters.put(SignalDetector.PATTERN[0], "#000");		//enables pattern for Recorder to detect only use in MSML XMS
		//parameters.put(SignalDetector.INTER_SIG_TIMEOUT, new Integer(10000));	//10 sec
	
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
			//RTC[] rtcs = new RTC[2];
			//rtcs[0] = MediaGroup.SIGDET_STOPPLAY;		//play barge in
			//rtcs[1] = MediaGroup.SIGDET_STOPRECORD;     //recorder turn key set to # 
			//mediaGroup.getRecorder().record(URI.create(RECORD_FILE_NAME), new RTC[]{rtcStop}, parameters);
			
			recordingState = RecordingState.RECORDING_STARTING_PENDING;
			
			//with extension implies audio only 
			//without extension implies audio/video recording
			String recordingFullName = XMS_RECORDING_DIR + recordingFileName + ".ulaw";
			URI recordingDestURI = URI.create(recordingFullName);
			Recorder testRecorder = mixerMG.getRecorder();
			log.debug("ConferenceSession:: record calling start mg.record");
			testRecorder.record(recordingDestURI, RTC.NO_RTC, parameters);
			this.recordingState = RecordingState.RECORDING;
			log.debug(" ConferenceSession:: record returned from mg.start record");			
		}
		catch (MsControlException e)
		{
			log.debug(e.toString());
			this.recordingState = RecordingState.NOT_RECORDING;
		} 
		
		log.debug("Leaving ConferenceSession:: record");

	}
	
	public void stopRecord(SipSession mySipSession) throws MsControlException {
		log.debug("Entering ConferenceSession:: stopRecord");
		
		if ( recordingState != RecordingState.RECORDING )
		{
			log.debug("ConferenceSession:: stopRecord request rejected since Recording State is not Recording");
			return;
		}
		MediaGroup mixerMG = this.getMediaGroup();

		try
		{
			recordingState = RecordingState.STOP_RECODING_PENDING;
			Recorder testRecorder = mixerMG.getRecorder();
			log.debug("ConferenceSession:: stopRecord calling stop mg.record");
			testRecorder.stop();
			log.debug(" ConferenceSession:: stopRecord returned from mg.stop record");			
		}
		catch (MsControlException e)
		{
			log.debug(e.toString());
			this.recordingState = RecordingState.NOT_RECORDING;
		} 
		
		log.debug("Leaving ConferenceSession:: record");
	}
	
	
	public void play(URI[] uri, RTC artc[], Parameters parameters) throws MsControlException {
		getMediaGroup().getPlayer().play(uri, artc, parameters);
	}
	

	
	
	public void release() {
		log.debug("Entering Conference release() ");
		log.debug( "ConferenceValid = " + conferenceValid );
		if (conferenceValid) {
			log.debug("Conference release() conference");
			conferenceValid = false;
			log.debug("["+confId+"] ACTUALLY RELEASING CONFERENCE MEDIA SESSION");
			jcs.removeConference(confId,myMediaSession.getURI());
			//Releasing the Mixer conference Media Session triggers the following actions at the connector:
			//Connector before releasing the media session will first destroy the conference if the Mixer is in
			//Conference Ready. After destroying the conference, the connector will then release the Media Session
			//MediaMixer mx = (MediaMixer) myMediaSession.getAttribute("MEDIA_MIXER");
			getMediaSession().release();
		}
		myMediaSession.setAttribute("CONFERENCE_SESSION", this);
		log.debug("Leaving Conference release() ");
	}
	
	/**
	 * Status event listener for conference creation verification
	 * This allocation event related to the Media Mixer is only valid
	 * once the mixer.confirm() API is implemented at the connector.
	 */
	private class MixerAllocationEventListener implements AllocationEventListener, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected JMCConferenceServlet servlet =null;
		protected ConferenceSession confSession = null;
		
		public MixerAllocationEventListener( ConferenceSession cs, JMCConferenceServlet servlet)
		{
			confSession = cs;
			this.servlet = servlet;
		}

		@Override
		public void onEvent(AllocationEvent anEvent) {
			// Check if mixer confirmation was successful
			SipSession conferenceOwnerSipSession = (SipSession)myMediaSession.getAttribute("CONFERENCE_OWNER_SIPSESSION");
			log.debug("["+conferenceOwnerSipSession.getCallId()+"] MIXER ALLOCATION EVENT TYPE: "+anEvent.getEventType());
			log.debug("["+conferenceOwnerSipSession.getCallId()+"] MIXER ALLOCATION EVENT ERROR: "+anEvent.getError());
			log.debug("["+conferenceOwnerSipSession.getCallId()+"] MIXER ALLOCATION EVENT ERROR TEXT: "+anEvent.getErrorText());
			log.debug("["+conferenceOwnerSipSession.getCallId()+"] MIXER ALLOCATION EVENT SOURCE: "+anEvent.getSource().getClass().getName());
			
			if ( servlet.useConfirm ) {
				if (anEvent.getEventType().equals(AllocationEvent.ALLOCATION_CONFIRMED)) {
					log.debug("["+conferenceOwnerSipSession.getCallId()+"] RECEIVED ALLOCATION CONFIRMED FOR CONFERENCE: "+confId);
					confSession.setConfirmation(true);
					confSession.monitor.notifyRequestCompleted(true, "conference control leg confirm completed");
				}
				else if (anEvent.getEventType().equals(AllocationEvent.IRRECOVERABLE_FAILURE)) {
					log.error("["+conferenceOwnerSipSession.getCallId()+"] RECEIVED ALLOCATION CONFIRMED FOR CONFERENCE: "+confId + " with error");
					log.error("["+conferenceOwnerSipSession.getCallId()+"] cleaning up participant leg");
					myParticipants.clear();
					release();
				}
			}
			myMediaSession = anEvent.getSource().getMediaSession();
		}
	}
	
	private class MixerRecorderEventListener implements MediaEventListener<RecorderEvent>, Serializable 
	{

		private static final long serialVersionUID = 1L;
		protected JMCConferenceServlet servlet =null;
		protected ConferenceSession confSession = null;

		public MixerRecorderEventListener( ConferenceSession cs, JMCConferenceServlet servlet)
		{
			confSession = cs;
			this.servlet = servlet;
		}

		@Override
		public void onEvent(RecorderEvent anEvent) {
			SipSession conferenceOwnerSipSession = (SipSession)myMediaSession.getAttribute("CONFERENCE_OWNER_SIPSESSION");
			log.debug("["+conferenceOwnerSipSession.getCallId()+"] MixerRecorderEventListener Event Type: "+anEvent.getEventType());
			log.debug("["+conferenceOwnerSipSession.getCallId()+"] MixerRecorderEventListener ERROR: "+anEvent.getError());
			log.debug("["+conferenceOwnerSipSession.getCallId()+"] MixerRecorderEventListener ERROR TEXT: "+anEvent.getErrorText());
			log.debug("["+conferenceOwnerSipSession.getCallId()+"] MixerRecorderEventListener SOURCE: "+anEvent.getSource().getClass().getName());

			if ( anEvent.getEventType() == RecorderEvent.RECORD_COMPLETED ) {
				log.debug("MixerRecorderEventListener RECORDING COMPLETED");
				confSession.recordingState = RecordingState.NOT_RECORDING;
			}
		}
	}
	
	private class MixerPlayerEventListener implements MediaEventListener<PlayerEvent>, Serializable 
	{

		private static final long serialVersionUID = 1L;
		protected JMCConferenceServlet servlet =null;
		protected ConferenceSession confSession = null;

		public MixerPlayerEventListener( ConferenceSession cs, JMCConferenceServlet servlet)
		{
			confSession = cs;
			this.servlet = servlet;
		}

		@Override
		public void onEvent(PlayerEvent anEvent) {
			SipSession conferenceOwnerSipSession = (SipSession)myMediaSession.getAttribute("CONFERENCE_OWNER_SIPSESSION");
			log.debug("["+conferenceOwnerSipSession.getCallId()+"] MixerPlayerEventListener Event Type: "+anEvent.getEventType());
			log.debug("["+conferenceOwnerSipSession.getCallId()+"] MixerPlayerEventListener ERROR: "+anEvent.getError());
			log.debug("["+conferenceOwnerSipSession.getCallId()+"] MixerPlayerEventListener ERROR TEXT: "+anEvent.getErrorText());
			log.debug("["+conferenceOwnerSipSession.getCallId()+"] MixerPlayerEventListener SOURCE: "+anEvent.getSource().getClass().getName());

			//TBD
		}
	}


	public void setConfirmation(boolean b) {
		conferenceValid = b;
	}
	
	public boolean getConfirmation(){
		return conferenceValid;
	}
	
	
	private Integer iRecMinDuration=null;
	private Integer iRecMaxDuration = null;
	private Integer iRecInitialTimeout =null;
	private Integer iRecFinalTimeout =null;
	private Boolean bRecSilenceTerminationFlag =false;
	
	
	//the following are use for video recording 
	private String  vRecVideoCodecs = null; //not used in this demo
	private String sRecVideoProfile = null; //not used in this demo
	private String sRecVideoLevel = null; //not used in this demo
	private String sRecVideoWidth =null; //not used in this demo
	private String sRecVideoHeight = null; //not used in this demo
	private String sRecVideoFramerate = null; //not used in this demo
	private Integer iRecVideoMaxBitRate = null; //not used in this demo
	private String vRecAudioCodecs = null; //not used in this demo
	private String vRecFileFormat = null; //not used in this demo
	private Integer  iRecAudioClockRate = null; //not used in this demo
	
	private void loadRecorderProperties()
	{
		//we need to add to the dlgdemo property file recording attributges... this will be done later 
		//for now hard coded 
		
		//this is recording for audio only
		iRecMinDuration = new Integer(3000);
		iRecMaxDuration	=  new Integer(20000);
		iRecInitialTimeout =  new Integer(7000);
		iRecFinalTimeout= new Integer(4000);
		bRecSilenceTerminationFlag = false;
	}
	

}
