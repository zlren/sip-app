/*
 * DIALOGIC CONFIDENTIAL
 *
 * Copyright 2010 Dialogic Corporation. All Rights Reserved.
 * The source code contained or described herein and all documents related to
 * the source code (collectively "Material") are owned by Dialogic Corporation 
 * or its suppliers or licensors ("Dialogic"). 
 *
 * BY DOWNLOADING, ACCESSING, INSTALLING, OR USING THE MATERIAL YOU AGREE TO BE
 * BOUND BY THE TERMS AND CONDITIONS DENOTED HERE AND ANY ADDITIONAL TERMS AND
 * CONDITIONS SET FORTH IN THE MATERIAL. Title to the Material remains with 
 * Dialogic. The Material contains trade secrets and proprietary and 
 * confidential information of Dialogic. The Material is protected by worldwide
 * Dialogic copyright(s) and applicable trade secret laws and treaty provisions.
 * No part of the Material may be used, copied, reproduced, modified, published, 
 * uploaded, posted, transmitted, distributed, or disclosed in any way without
 * prior express written permission from Dialogic Corporation.
 *
 * No license under any applicable patent, copyright, trade secret or other 
 *intellectual property right is granted to or conferred upon you by disclosure
 * or delivery of the Material, either expressly, by implication, inducement, 
 * estoppel or otherwise. Any license under any such applicable patent, 
 * copyright, trade secret or other intellectual property rights must be express
 * and approved by Dialogic Corporation in writing.
 *
 * You understand and acknowledge that the Material is provided on an 
 * AS-IS basis, without warranty of any kind.  DIALOGIC DOES NOT WARRANT THAT 
 * THE MATERIAL WILL MEET YOUR REQUIREMENTS OR THAT THE SOURCE CODE WILL RUN 
 * ERROR-FREE OR UNINTERRUPTED.  DIALOGIC MAKES NO WARRANTIES, EXPRESS OR 
 * IMPLIED, INCLUDING, WITHOUT LIMITATION, ANY WARRANTY OF NON-INFRINGEMENT, 
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  DIALOGIC ASSUMES NO 
 * RISK OF ANY AND ALL DAMAGE OR LOSS FROM USE OR INABILITY TO USE THE MATERIAL. 
 * THE ENTIRE RISK OF THE QUALITY AND PERFORMANCE OF THE MATERIAL IS WITH YOU.  
 * IF YOU RECEIVE ANY WARRANTIES REGARDING THE MATERIAL, THOSE WARRANTIES DO NOT 
 * ORIGINATE FROM, AND ARE NOT BINDING ON DIALOGIC.
 *
 * IN NO EVENT SHALL DIALOGIC OR ITS OFFICERS, EMPLOYEES, DIRECTORS, 
 * SUBSIDIARIES, REPRESENTATIVES, AFFILIATES AND AGENTS HAVE ANY LIABILITY TO YOU 
 * OR ANY OTHER THIRD PARTY, FOR ANY LOST PROFITS, LOST DATA, LOSS OF USE OR 
 * COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES, OR FOR ANY INDIRECT, 
 * SPECIAL OR CONSEQUENTIAL DAMAGES RELATING TO THE MATERIAL, UNDER ANY CAUSE OF
 * ACTION OR THEORY OF LIABILITY, AND IRRESPECTIVE OF WHETHER DIALOGIC OR ITS 
 * OFFICERS, EMPLOYEES, DIRECTORS, SUBSIDIARIES, REPRESENTATIVES, AFFILIATES AND 
 * AGENTS HAVE ADVANCE NOTICE OF THE POSSIBILITY OF SUCH DAMAGES.  THESE 
 * LIMITATIONS SHALL APPLY NOTWITHSTANDING THE FAILURE OF THE ESSENTIAL PURPOSE 
 * OF ANY LIMITED REMEDY.  IN ANY CASE, DIALOGIC'S AND ITS OFFICERS', 
 * EMPLOYEES', DIRECTORS', SUBSIDIARIES', REPRESENTATIVES', AFFILIATES' AND 
 * AGENTS' ENTIRE LIABILITY RELATING TO THE MATERIAL SHALL NOT EXCEED THE 
 * AMOUNTS OF THE FEES THAT YOU PAID FOR THE MATERIAL (IF ANY). THE MATERIALE 
 * IS NOT FAULT-TOLERANT AND IS NOT DESIGNED, INTENDED, OR AUTHORIZED FOR USE IN 
 * ANY MEDICAL, LIFE SAVING OR LIFE SUSTAINING SYSTEMS, OR FOR ANY OTHER 
 * APPLICATION IN WHICH THE FAILURE OF THE MATERIAL COULD CREATE A SITUATION 
 * WHERE PERSONAL INJURY OR DEATH MAY OCCUR. Should You or Your direct or 
 * indirect customers use the MATERIAL for any such unintended or unauthorized 
 * use, You shall indemnify and hold Dialogic and its officers, employees, 
 * directors, subsidiaries, representatives, affiliates and agents harmless 
 * against all claims, costs, damages, and expenses, and attorney fees and 
 * expenses arising out of, directly or indirectly, any claim of product 
 * liability, personal injury or death associated with such unintended or 
 * unauthorized use, even if such claim alleges that Dialogic was negligent 
 * regarding the design or manufacture of the part.
 */

package testing.unit;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.signals.SignalDetector;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.mixer.MediaMixer;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.networkconnection.SdpPortManagerException;
import javax.media.mscontrol.resource.AllocationEvent;
import javax.media.mscontrol.resource.AllocationEventListener;
import javax.media.mscontrol.resource.RTC;
import javax.servlet.ServletException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionsUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.DlgcTest;

import com.vendor.dialogic.javax.media.mscontrol.DlgcMediaSessionProxy;
import com.vendor.dialogic.javax.media.mscontrol.DlgcProxy;



public class DlgcConferenceTest extends DlgcTest
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1;
	public static Integer ncCount =0;
	@Override
	public void init() throws ServletException
	{
		super.init();
		prompt = URI.create("file:////opt/snowshore/prompts/generic/en_US/new_number.ulaw"); 
		sessionsUtil = (SipSessionsUtil) getServletContext().getAttribute(SIP_SESSIONS_UTIL);
					
	}
	
	@Override
	public void servletInitialized(SipServletContextEvent evt){

		String sName = evt.getSipServlet().getServletName();
		
		if( sName.equalsIgnoreCase("DlgcSipServlet") )
		{
			dlgcSipServletLoaded = true;
			log.debug(" DlgcConferenceTest::servletInitialized DlgcSipServlet loaded");			
			return;
		}

		if( (sName.equalsIgnoreCase("DlgcConferenceTest") ) && dlgcSipServletLoaded)
		{
			if ( servletInitializedFlag == false ) {
				log.debug("Entering DlgcConferenceTest::servletInitialized servletName: " + sName);			
				servletInitializedFlag = true;
				initDriver();
				//myServletInitialized(evt);
			} else {
				log.debug("DlgcConferenceTest::servletInitialized(): already servletInitialized was called...debouncing " + sName);
			}
		}
	}
	
	
	@Override
	public void doInvite(final SipServletRequest request)
	{
		try
		{
			Integer i = ncCount + 1;
			log.debug("^^^^^^^^^^^^^^^^^ DlgcConferenceTest NC # " + i.toString() + " ^^^^^^^^^^^^^^^^^^^^^");
		
			MediaSession ms = mscFactory.createMediaSession();
			NetworkConnection nc = ms.createNetworkConnection(NetworkConnection.BASIC);
			MediaGroup mg = ms.createMediaGroup(MediaGroup.PLAYER_SIGNALDETECTOR);
			
			SipSession session = request.getSession();
			
			ms.setAttribute("SIP_SESSION", session);
			ms.setAttribute("NETWORK_CONNECTION", nc);
			ms.setAttribute("MEDIA_GROUP", mg);
			ms.setAttribute("REQUEST", request);
			
			session.setAttribute("MEDIA_SESSION", ms);
			session.setAttribute("NETWORK_CONNECTION", nc);
			
			log.debug("UUUUUUUUUUUUUUUU NC# " + i.toString() + " SAS= " + ((DlgcProxy)nc).getProxyId() + " UUUUUUUUU" );
			log.debug("UUUUUUUUUUUUUUUU NC# " + i.toString()  + " SASOBJ = " + ((DlgcProxy)nc).getProxySAS() + " UUUUUU");
			ncCount++;
			//session.setAttribute("INVITE", request);
			nc.getSdpPortManager().addListener(new DlgcSdpPortEventListener());
			mg.getSignalDetector().addListener(new DlgcSigDetEventListener()); 
			
			nc.join(Joinable.Direction.DUPLEX, mg);
			
			byte[] remoteSdp = request.getRawContent();
			
			if (remoteSdp == null)
			{
				nc.getSdpPortManager().generateSdpOffer();
			}
			else
			{
				session.setAttribute("ACTION", "CollectDigits");
				nc.getSdpPortManager().processSdpOffer(remoteSdp);
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
	}
	
	
	
	@Override
	protected void doResponse(SipServletResponse response)
		throws ServletException, IOException
	{
		if (response.getMethod().equals("INVITE"))
		{
			if (response.getStatus() == SipServletResponse.SC_OK)
			{
				try
				{
					NetworkConnection nc = (NetworkConnection) response.getRequest().getSession().getAttribute("NETWORK_CONNECTION");
					byte[] remoteSdp = response.getRawContent();
					if (remoteSdp != null)
					{
						response.getSession().setAttribute("RESPONSE", response);
						nc.getSdpPortManager().processSdpAnswer(remoteSdp);
					}
				}
				catch (MsControlException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private class DlgcSigDetEventListener implements MediaEventListener<SignalDetectorEvent>, Serializable
	{
		
			
		SipApplicationSession loadSas()
		{
			//SipSessionsUtil sessionsUtil = (SipSessionsUtil) getServletContext().getAttribute(SIP_SESSIONS_UTIL);
			//occas5SessionsUtilAdapter = (SipSessionsUtilAdapter) getServletContext().getAttribute("javax.servlet.sip.SipSessionsUtil");
			SipApplicationSession sas = sessionsUtil.getApplicationSessionByKey("JOHNCRUZ", true);
			//SipApplicationSession sas = occas5SessionsUtilAdapter.getApplicationSessionByKey("JOHNCRUZ", true);
			return sas;
		} 
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 4674138283285971868L;

		@SuppressWarnings("unchecked")
		@Override
		public void onEvent(SignalDetectorEvent event)
		{
			log.debug("RRRRRRR DlgcSigDetEventListener event  RRRRR " );
			if (event.isSuccessful())
			{
				if (event.getEventType().equals(SignalDetectorEvent.RECEIVE_SIGNALS_COMPLETED))
				{
					log.debug("RRRRRRR DlgcSigDetEventListener receive  RECEIVE_SIGNALS_COMPLETED RRRRR " );
					event.getSource().stop();
					
					//
					//problem creating mixer and joining in listener.... 
					//the MediaSession retrieved from the event is the MediaSessionImpl 
					//while the NC retrieve using getAttribute from the session which returns a NCProxy...
					//then join does not work because we are trying to join a Mixer created from a MSImpl with NCProxy...
					//join generates class exception due to the NCProxy...expecting the NCImpl
					
					MediaSession ms = event.getSource().getMediaSession();
					NetworkConnection nc = (NetworkConnection) ms.getAttribute("NETWORK_CONNECTION");
					
					//SipSession session = (SipSession)ms.getAttribute("SIP_SESSION");
					
					sAs = loadSas();
					MixerMap = (Map<String, MediaMixer>) sAs.getAttribute("CONFERENCE_MAP");
				
					
					if ( MixerMap == null) {
						MixerMap = new HashMap<String, MediaMixer>();
					}
						
					String pin = event.getSignalString();
					
					log.debug("RRRRRRR DlgcSigDetEventListener pin = " + pin + " RRRRR");
					if (pin != null)
					{
						try
						{
							MediaMixer mx = MixerMap.get(pin);
							
							if (mx == null)
							{
								// create new mixer.
								Parameters params = ms.createParameters();
								params.put(MediaMixer.MAX_PORTS, 2);
								
								log.debug(" *********************************** " );
								log.debug("[****************CreateMediaMixer***************************]");
								mx = ms.createMediaMixer(MediaMixer.AUDIO, params);
								mx.addListener(new MixerAllocationEventListener());
								mx.confirm();
								MixerMap.put(pin, mx);
								sAs.setAttribute("CONFERENCE_MAP", MixerMap);
								
								log.debug("[*****************Done Creating MIXER ***********************************] " );
							} else {
								log.debug("PPPPP Found Conference # " + pin.toString() + " PPPPPPP");
								log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>> Calling MX Join NC <<<<<<<<<<<<<<<<<<<<<<<<<<");
								mx.join(Joinable.Direction.DUPLEX, nc);
							}
						}
						catch (MsControlException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}	
	}
	
	private class MixerAllocationEventListener implements AllocationEventListener, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 17564463L;

		@Override
		public void onEvent(AllocationEvent anEvent) {
			// Check if mixer confirmation was successful
			log.debug(" MIXER ALLOCATION EVENT: "+anEvent.getEventType());
			
			
			if (anEvent.getEventType().equals(AllocationEvent.ALLOCATION_CONFIRMED)) {
					MediaMixer   mx = (MediaMixer) anEvent.getSource();
					MediaSession ms = anEvent.getSource().getMediaSession();
					log.debug("MixerAllocationEventListener::onEvent SignalDetection Before mediaSession.getProxyId (sasId) = " +   ((DlgcMediaSessionProxy)ms).getProxyId() );
					NetworkConnection nc = (NetworkConnection) ms.getAttribute("NETWORK_CONNECTION");
								
					log.info(" RECEIVED ALLOCATION CONFIRMED FOR CONFERENCE: ");
					/* 
					 * Add conference owner (first participant) to conference and send indication to
					 * first participant that the conference was confirmed
					 */
					try {									
						log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>> Calling MX Join NC <<<<<<<<<<<<<<<<<<<<<<<<<<");
						mx.join(Joinable.Direction.DUPLEX, nc);
					} catch (MsControlException e) {
						e.printStackTrace();
					}
				
					
			}
			else if (anEvent.getEventType().equals(AllocationEvent.IRRECOVERABLE_FAILURE)) {
				//myParticipants.clear();
				//release();
				log.error("Can't enter conference...IRRECOVERABLE_FAILURE ");
			}
			
		}

		
	}
	
	
	public class DlgcSdpPortEventListener implements MediaEventListener<SdpPortManagerEvent>, Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 5742674704860593132L;

		@Override
		public void onEvent(SdpPortManagerEvent event)
		{	
			SdpPortManager sdp = event.getSource();
			MediaSession ms = sdp.getMediaSession();
			SipSession session = (SipSession) ms.getAttribute("SIP_SESSION");
			
			if (session.isValid())
			{
				SipServletRequest request = (SipServletRequest) ms.getAttribute("REQUEST");
				
				if (event.getEventType().equals(SdpPortManagerEvent.ANSWER_GENERATED))
				{
					log.debug("IIIII SdpPortManagerEvent ANSWER_GENERATED IIIII");
					SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
					try
					{
						response.setContent(sdp.getMediaServerSessionDescription(), "application/sdp");
						response.send();
						
						
						String action = (String) session.getAttribute("ACTION");
						if (action.equals("CollectDigits"))
						{
							session.removeAttribute("ACTION");
							MediaGroup mg = (MediaGroup) ms.getAttribute("MEDIA_GROUP");
							if (mg != null)
							{	
								//Parameters parameters = mscFactory.createParameters();
								Parameters parameters = ms.createParameters();
								parameters.put(SignalDetector.PROMPT, prompt);
								mg.getSignalDetector().receiveSignals(4, null, new RTC[]{MediaGroup.SIGDET_STOPPLAY}, parameters);
								
							}
						}				
					}
					catch (UnsupportedEncodingException e)
					{
						e.printStackTrace();
					}
					catch (SdpPortManagerException e)
					{
						e.printStackTrace();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					catch (MsControlException e)
					{
						e.printStackTrace();
					}	
				}
				else if (event.getEventType().equals(SdpPortManagerEvent.ANSWER_PROCESSED))
				{
					log.debug("IIIII SdpPortManagerEvent ANSWER_PROCESSED IIIII");
					SipServletResponse response = (SipServletResponse) session.getAttribute("RESPONSE");
					if (response != null)
					{
						try
						{
							response.createAck().send();
							//add here john
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}
				else if (event.getEventType().equals(SdpPortManagerEvent.UNSOLICITED_OFFER_GENERATED))
				{
					log.debug("IIIII SdpPortManagerEvent UNSOLICITED_OFFER_GENERATED IIIII");
					// Need to send a re-Invite.
					SipServletMessage reInviteMessage = session.createRequest("INVITE");
					try
					{
						byte[] sessionDesc = sdp.getMediaServerSessionDescription();
						reInviteMessage.setContent(sessionDesc, "application/sdp");
						reInviteMessage.send();
					}
					catch (SdpPortManagerException e)
					{
						e.printStackTrace();
					}
					catch (UnsupportedEncodingException e)
					{
						e.printStackTrace();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					
				}
			}
		}
	}
	
		
	private URI prompt = null;
	private Map<String, MediaMixer> MixerMap = null;
	private static Logger log = LoggerFactory.getLogger(DlgcConferenceTest.class);
	public static SipSessionsUtil sessionsUtil;
//	public static SipSessionsUtilAdapter occas5SessionsUtilAdapter;
	private transient SipApplicationSession sAs = null;
	
}
