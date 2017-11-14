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

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.mediagroup.CodecConstants;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.Recorder;
import javax.media.mscontrol.mediagroup.RecorderEvent;
import javax.media.mscontrol.mixer.MediaMixer;
import javax.media.mscontrol.mixer.MixerAdapter;
import javax.media.mscontrol.resource.RTC;
import javax.media.mscontrol.resource.video.VideoLayout;
import javax.media.mscontrol.resource.video.VideoRenderer;
import javax.media.mscontrol.resource.video.VideoRendererEvent;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.reference_conf_demo_with_outb_call_layout.DlgcOutbCallConferenceStorage.DlgcConferenceStorageMgrException;



public class DlgcReferenceConferenceWOBC implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory.getLogger(DlgcReferenceConferenceWOBC.class);  
	protected DlgcReferenceConferenceStateWOBC	presentState;
	protected DlgcReferenceConferenceStateWOBC	previousState;
	
	protected MediaMixer 		confMediaMixer = null;
	protected MixerAdapter 		confMediaMixerAdapter = null;
	DlgcReferenceConferenceWithOutBCallServlet  myControlServlet = null;
	MediaSession				myMediaSession = null;
	MediaGroup					confMediaGroup=null;
	boolean						mixerRdy = false;
	boolean						mediaRdy = false;
	List<DlgcReferenceConferenceParticipantWOBC> participantList = null; 
	String conf_record_file=null;
	int	numOfParticiapant;
	
	protected static DlgcReferenceConferenceWOBC singletonConference = null;  
	protected static  DlgcOutbCallConferenceStorage myConferenceStorage = null;
	protected VideoRenderer confVideoRenderer = null;
	
	/****
	 * 
	 * @param servlet
	 * The DlgcReferenceConferenceWOBC class contains a list of conference participants. It 
	 * manages the conference current and previous state.
	 * It is associated to the Media Mixer. It creates a MediaSession which represents the
	 * conference SIP control leg to the Media Server.
	 * This conference uses the Non-Dedicated Control Leg. That is it uses one of the call legs 
	 * sip session to the Media Server to send and receive Media Server conferencing messages.
	 */
	
	@SuppressWarnings("static-access")
	private DlgcReferenceConferenceWOBC(DlgcReferenceConferenceWithOutBCallServlet servlet) 
	{	
		myControlServlet 	= servlet;
		presentState  		= DlgcReferenceConferenceStateWOBC.dlgcInitialStateWOBC ;
		previousState 		= DlgcReferenceConferenceStateWOBC.dlgcInitialStateWOBC ;
		numOfParticiapant = 0;
		
		try {
			myMediaSession = servlet.theMediaSessionFactory.createMediaSession();
		} catch (MsControlException e1) {
			log.debug(e1.toString());
		}
		try {
			
			//Read from the connector property file if conference is a AUDIO or VIDEO/AUDIO conference
			//Properties props = DlgcReferenceConferenceWithOutBCallServlet.theMediaSessionFactory.getProperties();
			
			String mxMode = DlgcReferenceConferenceWithOutBCallServlet.demoPropertyObj.getProperty("DlgcAvLayoutConferenceDemo.media.mixer.mode");
			String confSize = DlgcReferenceConferenceWithOutBCallServlet.demoPropertyObj.getProperty("DlgcAvLayoutConferenceDemo.media.mixer.video.size");
			conf_record_file = DlgcReferenceConferenceWithOutBCallServlet.demoPropertyObj.getProperty("DlgcAvLayoutConferenceDemo.media.mixer.conf.recordfile");
			
			if ( conf_record_file == null ) {
				conf_record_file="file:////tmp/defautAudioRec.ulaw";
			}
			Configuration<MediaMixer> mc=null;
			VideoLayout vLayout = null;
		
			if (confSize==null)
				confSize = "CIF";
			
			if (mxMode==null)
				mc = MediaMixer.AUDIO;
			else
			{
				if (mxMode.equalsIgnoreCase("AUDIO_VIDEO"))
					mc = MediaMixer.AUDIO_VIDEO;
				else if (mxMode.equalsIgnoreCase("AUDIO_VIDEO_RENDERING"))
				{
					mc = MediaMixer.AUDIO_VIDEO_RENDERING;
					vLayout = servlet.getVideoLayout("*70");
				}
				else
					mc = MediaMixer.AUDIO;
					
			}
			confMediaMixer= myMediaSession.createMediaMixer(mc);
			
			/*log.info(servlet.theMediaSessionFactory.getPresetLayouts(1)[0].marshall());
			log.info(servlet.theMediaSessionFactory.getPresetLayouts(2)[0].marshall());
			log.info(servlet.theMediaSessionFactory.getPresetLayouts(2)[1].marshall());
			log.info(servlet.theMediaSessionFactory.getPresetLayouts(4)[0].marshall());
			log.info(servlet.theMediaSessionFactory.getPresetLayouts(6)[0].marshall());
			log.info(servlet.theMediaSessionFactory.getPresetLayouts(8)[0].marshall());
			log.info(servlet.theMediaSessionFactory.getPresetLayouts(9)[0].marshall());
			log.info(servlet.theMediaSessionFactory.getPresetLayouts(10)[0].marshall());*/
			
			
			
			if (vLayout == null ) {
				log.debug("Video Layout is set to null... not using video layout") ;
			} else {
				log.debug("Getting Mixer Video Renderer since user has defined a video layout...");
				confVideoRenderer = confMediaMixer.getResource(VideoRenderer.class);
				if ( confVideoRenderer == null ) {
					log.debug("Sorry the mixer has returned a null for the VideoRenderer.. can't apply video layout...");
				} else {
					log.debug("Found both videoLayout and VideoRender... setting Mixer video renderer layout...");
					
					
					confVideoRenderer.addListener(new DlgcReferenceVideoRendererListenerWOBC<VideoRendererEvent>());
					confVideoRenderer.setLayout(vLayout);
				}
			}
			
			//Test Use MIXER ADAPTER ENALE THIS LINE
			confMediaMixerAdapter = confMediaMixer.createMixerAdapter(MixerAdapter.DTMF_CLAMP);
			myMediaSession.setAttribute("MEDIA_MIXER_ADAPTER", confMediaMixerAdapter);
			
			confMediaMixer.addListener(new DlgcReferenceConferenceAllocListenerWOBC());
			confMediaGroup = myMediaSession.createMediaGroup(MediaGroup.PLAYER_RECORDER_SIGNALDETECTOR);
			myMediaSession.setAttribute("CONFERENCE_VIDEO_SIZE", confSize);
			myMediaSession.setAttribute("MEDIA_MIXER", confMediaMixer);
		
			myMediaSession.setAttribute("MEDIA_GROUP", confMediaGroup);
			DlgcReferenceMixerMediaListenerWOBC<RecorderEvent> l = new DlgcReferenceMixerMediaListenerWOBC<RecorderEvent>();
			confMediaGroup.getRecorder().addListener(l);
			confMediaGroup.join(Joinable.Direction.DUPLEX,confMediaMixer);
			

			mediaRdy = true;
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			log.debug(e.toString());
		}
		
		participantList = new ArrayList<DlgcReferenceConferenceParticipantWOBC> ();
	}
	
	public static DlgcReferenceConferenceWOBC createNewConference( DlgcReferenceConferenceWithOutBCallServlet servlet, DlgcOutbCallConferenceStorage conferenceStorage ) {
		
		if ( singletonConference == null ) {
			DlgcReferenceConferenceWOBC.myConferenceStorage = conferenceStorage;
			singletonConference =  new DlgcReferenceConferenceWOBC(servlet);
			return singletonConference;
		} else
			return singletonConference;
	}
	
	public static void destoryConference() {
		
		if ( singletonConference != null ) {
			try {
				DlgcReferenceConferenceWOBC.myConferenceStorage.removeConference();
			} catch (DlgcConferenceStorageMgrException e) {
				e.printStackTrace();
			} finally {
				singletonConference = null;
				DlgcReferenceConferenceWOBC.myConferenceStorage = null;
			}
			
		}
		
	}
	
	public DlgcReferenceConferenceWithOutBCallServlet getControlServlet()
	{
		return myControlServlet;
	}
	
	

	//Call leg has arrived as an Invite the ReferenceServlet is requesting
	//to add it to the conference
	public void addNewParticipant(SipServletRequest req, DlgcOutbCallConferenceStorage cfstorage) 
	{
		SipSession mySipSession = req.getSession();

		if (req.isInitial()) {
			try {
				DlgcReferenceConferenceParticipantWOBC participant = new DlgcReferenceConferenceParticipantWOBC(this, mySipSession, cfstorage);	
				if ( participant != null) {
					participantList.add(participant);	

					participant.joinConference(req);

				} else
					log.error("Error getting participant during addNewParticipant Method and request is  initial request");
			} catch (Exception e) {
				log.error("Cannot create MediaSession or MediaSessionFactory :", e);

			}
		}else {
			log.debug("addNewParticipant:: is not initial request no doing anything");
		}
	}
	
	
	public void addParticipantToConferenceList( DlgcReferenceConferenceParticipantWOBC participant) {
		if ( participant != null) {
			participantList.add(participant);	
		}else {
			log.error("Cant add a null participant to the conference");
		}
	}
	
	public boolean record()
	{
		boolean bRet=true;
		if (participantList.size()>0 && this.mediaRdy)
		{
			//Parameters parameters = DlgcReferenceConferenceServlet.theMediaSessionFactory.createParameters();
			Parameters parameters = myMediaSession.createParameters();
			//parameters.put(Recorder.PROMPT, URI.create(PROMPT_FILE_NAME_WAV));
			parameters.put(Recorder.APPEND, Boolean.FALSE);
			parameters.put(Recorder.MAX_DURATION, new Integer(300000));  //30 seconds recording
			parameters.put(Recorder.AUDIO_CODEC, CodecConstants.ALAW_PCM_64K); 
			parameters.put(Recorder.AUDIO_CODEC, CodecConstants.LINEAR_16BIT_128K); 
			
			//parameters.put(Recorder.VIDEO_CODEC, CodecConstants.H263); //video support no 100% ready remove for now
		
			//note implementation is only on Pattern[0] only
			//parameters.put(SignalDetector.PATTERN[0], "#000");		//enables pattern for Recorder to detect only use in MSML XMS
			//parameters.put(SignalDetector.INITIAL_TIMEOUT, new Integer(20000));	//20 sec
			//parameters.put(SignalDetector.INTER_SIG_TIMEOUT, new Integer(10000));	//10 sec
		
			//RTC rtcStop = new RTC(SignalDetector.DETECTION_OF_ONE_SIGNAL, Recorder.STOP);  //not needed... can be ignore since RTC is not implemented
			
			try
			{
				RTC[] rtcs = new RTC[1];
				rtcs[0] = MediaGroup.SIGDET_STOPPLAY;
				//mediaGroup.getRecorder().record(URI.create(RECORD_FILE_NAME), new RTC[]{rtcStop}, parameters);
				URI recordingDestURI = URI.create(conf_record_file);//without ext is video/audio, with ext is audio only
				Recorder testRecorder = confMediaGroup.getRecorder();
				testRecorder.record(recordingDestURI, null, parameters);
			}
			catch (MsControlException e)
			{
				e.printStackTrace();
				bRet=false;
			}
		}
		else
			bRet = false;
		
		if (bRet)
			this.mediaRdy=false;
			
		return bRet;
	}
	
	public void setVideoLayout(String videolayoutStyle)
	{
		VideoLayout vlayout= myControlServlet.getVideoLayout(videolayoutStyle);
		if (confVideoRenderer != null)
		{
			try
			{
				confVideoRenderer.setLayout(vlayout);
			}
			catch (MsControlException e)
			{
				log.debug("setVideoLayout: catch exeption= "+e.getMessage());
			}
		}
		else
			log.debug("video renderer obj = null");
	}
	
	public boolean stopRecord()
	{
		boolean bRet=true;
		try
		{
			confMediaGroup.getRecorder().stop();
			if ( DlgcReferenceConferenceWithOutBCallServlet.apiSyncModeProperty ) {
				log.debug("DlgcReferenceConferenceWithOutBCallServlet returned from synchronous stopRecord");
			}else {
				log.debug("DlgcReferenceConferenceWithOutBCallServlet returned from asynchronous stopRecord");
			}	
		}
		catch (MsControlException e)
		{
			log.error(e.toString());
			bRet=false;
		}
		this.mediaRdy = true;
		return bRet;
	}
	
	synchronized public int getNumberOfParticipants()
	{
		return participantList.size();
	}
	
	synchronized public DlgcReferenceConferenceParticipantWOBC findParticipant(String sipSessionId )
	{
		DlgcReferenceConferenceParticipantWOBC foundParticipant = null;
		
		if ( sipSessionId == null )
			return null;
		
		for ( int i = 0; i < participantList.size(); i++) {
			foundParticipant = participantList.get(i);
			if ( foundParticipant.clientSSID.compareToIgnoreCase(sipSessionId) == 0 ) {
				break;
			}else {
				foundParticipant = null;
			}
		}
		return foundParticipant;
	}
	
	//200 ok  responses
	public  void doInviteResponse(SipServletResponse response)
	{
		SipSession mySipSession = response.getSession();
		String ssId = mySipSession.getId();
		log.debug("Entering doResponse() response: " + response.toString() );
		try
		{	
			byte[] localSdp;
			try {
				localSdp = response.getRawContent();
				if (localSdp != null)
				{
					response.getSession().setAttribute("RESPONSE", response);
					
					DlgcReferenceConferenceParticipantWOBC participant = this.findParticipant(ssId);
					if ( participant != null ) {
						log.debug("localSdp = " + localSdp.toString() + " calling participant.processSipPhone200Invite(localSdp)" );
						participant.localSdp = localSdp;
						participant.processSipPhone200Invite(response);
					} else {
						log.error("Could find the participant in the conference for the outbound call response...");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		catch (MsControlException e)
		{
			e.printStackTrace();
		}
		log.debug("Leaving doResponse() ");
	}

	
	//Ack  responses
	public void doAck(SipServletRequest request)
	{
		log.debug("Entering doAck() ");
		SipSession mySipSession = request.getSession();
		String ssId = mySipSession.getId();
		log.debug("Entering doAck() request" );
		try
		{	
			DlgcReferenceConferenceParticipantWOBC participant = this.findParticipant(ssId);
			if ( participant != null ) {
				participant.processSipPhoneAck(request);
			} else {
				log.error("Could find the participant in the conference for the outbound call response");
			}
		}
		catch (MsControlException e)
		{
			e.printStackTrace();
		}
		log.debug("Leaving doAck() ");
	}
	
	public void removeParticipant(SipServletResponse response) 
	{
		SipSession mySipSession = response.getRequest().getSession();
		DlgcReferenceConferenceParticipantWOBC  participant = (DlgcReferenceConferenceParticipantWOBC )mySipSession.getAttribute("PARTICIPANT");
		
		releaseParticipant(participant);
	}	
	
	public void removeParticipantFromConferenceList(DlgcReferenceConferenceParticipantWOBC  participant) 
	{
		this.participantList.remove(participant);
	}
	
	
	
	public boolean removeParticipant(SipSession mySipSession ) 
	{
		boolean confReleased = false;
		DlgcReferenceConferenceParticipantWOBC  participant = this.findParticipant(mySipSession.getId());
		confReleased = releaseParticipant(participant);
		return confReleased;
	}	
	
	@SuppressWarnings("finally")
	public boolean releaseParticipant(DlgcReferenceConferenceParticipantWOBC  participant) 
	{

		log.debug("DlgcReferenceConferenceWOBC:: Entering releaseParticipant" );
		
		boolean confReleased = false;
		
		if ( participant != null ) {
			try {
				if ( participantList.size() == 1) {
					log.debug("releaseParticipant "); //and conference");
					participantList.remove(participant);
					participant.release();
					destoryConference();
					confReleased = true;
				} else {
					log.debug("releaseParticipant only");
					participantList.remove(participant);
					participant.release();
				}
			} catch (MsControlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				log.debug("DlgcReferenceConferenceWOBC:: with exception - number of participant is " + (new Integer(participantList.size())).toString() );
				log.debug("DlgcReferenceConferenceWOBC:: with exception - Leaving releaseParticipant" );
				return confReleased;
			}
		} else {
			log.debug("DlgcReferenceConferenceWOBC:: number of participant is " + (new Integer(participantList.size())).toString() );
			log.debug("DlgcReferenceConferenceWOBC:: Leaving releaseParticipant" );
			return confReleased;
		}
		
	
		
		
	}

	public void createOutBoundCallParticipant(DlgcReferenceConferenceParticipantWOBC dtmfParticipantReceiver, DlgcOutbCallConferenceStorage cfStorage) 
	{
		
		log.debug("Entering createOutBoundCallParticipant() ");
		
		DlgcReferenceConferenceParticipantWOBC outBoundParticipant = new DlgcReferenceConferenceOutboundParticipantWOBC(this, cfStorage);	
		if ( outBoundParticipant != null) {
			participantList.add(outBoundParticipant);	
			try {
				log.debug("createOutBoundCallParticipant()::generateSdpOfferToMS() ");
				outBoundParticipant.generateSdpOfferToMS();
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else
			log.error("Error getting participant during addNewParticipant Method and request is  initial request");
		
		log.debug("Leaving createOutBoundCallParticipant() ");

		
	}

	
	
	
}
