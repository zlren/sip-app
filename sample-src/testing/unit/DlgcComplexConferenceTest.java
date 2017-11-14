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
import javax.media.mscontrol.mediagroup.Player;
import javax.media.mscontrol.mediagroup.PlayerEvent;
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
import javax.media.mscontrol.resource.ResourceContainer;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.DlgcTest;

public class DlgcComplexConferenceTest extends DlgcTest
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1;

	@Override
	public void init() throws ServletException
	{
		super.init();
		
		
			prompt = URI.create("file:////opt/snowshore/prompts/generic/en_US/new_number.ulaw");
			prompt2 = URI.create("file:////opt/snowshore/prompts/generic/en_US/service_outage.ulaw");
		
	}
	
	@Override
	public void servletInitialized(SipServletContextEvent evt){

		String sName = evt.getSipServlet().getServletName();
		if( sName.equalsIgnoreCase("DlgcSipServlet") )
		{
			dlgcSipServletLoaded = true;
			log.debug(" DlgcComplexConferenceTest::servletInitialized DlgcSipServlet loaded");			
			return;
		}

		if( (sName.equalsIgnoreCase("DlgcComplexConferenceTest") ) && dlgcSipServletLoaded)
		{
			if ( servletInitializedFlag == false ) {
				log.debug("Entering DlgcComplexConferenceTest::servletInitialized servletName: " + sName);			
				servletInitializedFlag = true;
				initDriver();
				//myServletInitialized(evt);
			} else {
				log.debug("DlgcComplexConferenceTest::servletInitialized(): already servletInitialized was called...debouncing " + sName);
			}
		}
	}
	
	@Override
	public void doInvite(final SipServletRequest request)
	{
		try
		{
			MediaSession ms = mscFactory.createMediaSession();
			NetworkConnection nc = ms.createNetworkConnection(NetworkConnection.BASIC);
			MediaGroup mg = ms.createMediaGroup(MediaGroup.PLAYER_SIGNALDETECTOR);
			MediaGroup mg2 = ms.createMediaGroup(MediaGroup.PLAYER);
			
			
			SipSession session = request.getSession();
			
			
			ms.setAttribute("SIP_SESSION", session);
			ms.setAttribute("NETWORK_CONNECTION", nc);
			ms.setAttribute("MEDIA_GROUP", mg);
			ms.setAttribute("MEDIA_GROUP_2", mg2);
			ms.setAttribute("REQUEST", request);
			
			session.setAttribute("NETWORK_CONNECTION", nc);
			
			session.setAttribute("MEDIASESSION", ms);
			

			
			nc.getSdpPortManager().addListener(new DlgcSdpPortEventListener());
			nc.addListener(new DlgcAllocationEventListener());
			mg.addListener(new DlgcAllocationEventListener());
			mg.getSignalDetector().addListener(new DlgcSigDetEventListener());
			mg.getPlayer().addListener(new PlayerEventListener());
			mg2.addListener(new DlgcAllocationEventListener());
			mg2.getPlayer().addListener(new PlayerEventListener());
			
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
					
					MediaSession ms = (MediaSession) response.getSession().getAttribute("MEDIASESSION");
					NetworkConnection nc = (NetworkConnection) ms.getAttribute("NETWORK_CONNECTION");
					
					byte[] remoteSdp = response.getRawContent();
					if (remoteSdp != null)
					{
						response.getSession().setAttribute("RESPONSE", response);
						nc.getSdpPortManager().processSdpAnswer(remoteSdp);
						ms.setAttribute("NETWORK_CONNECTION", nc);
						
						//note needed to get this to work with serialization
						SipSession session = (SipSession)ms.getAttribute("SIP_SESSION");
						session.setAttribute("MEDIASESSION", ms);
					}
				}
				catch (MsControlException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private class DlgcAllocationEventListener implements AllocationEventListener , Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 4633218747426251274L;

		@Override
		public void onEvent(AllocationEvent event)
		{
			
			
			if (event.getEventType().equals(AllocationEvent.ALLOCATION_CONFIRMED))
			{
				MediaMixer mx = (MediaMixer) event.getSource();
				MediaSession ms = event.getSource().getMediaSession();
				NetworkConnection nc = (NetworkConnection) ms.getAttribute("NETWORK_CONNECTION");
				
				try
				{
					mx.join(Joinable.Direction.DUPLEX, nc);
					MediaGroup mg = (MediaGroup) ms.getAttribute("MEDIA_GROUP_2");
					mx.join(Joinable.Direction.DUPLEX, mg);
					
					//note needed to get this to work with serialization
					SipSession session = (SipSession)ms.getAttribute("SIP_SESSION");
					session.setAttribute("MEDIASESSION", ms);
					
					
				}
				catch (MsControlException e)
				{
					e.printStackTrace();
				}
			}
			else if (event.getEventType().equals(AllocationEvent.IRRECOVERABLE_FAILURE))
			{
				ResourceContainer rc = event.getSource();
				if (rc != null)
				{
					log.debug("IRRECOVERABLE_FAILURE recieved on : " + rc.getClass().toString());
					rc.release();
				}
			}
		}
		
	}
	
	private class DlgcSigDetEventListener implements MediaEventListener<SignalDetectorEvent>, Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2867430359476819467L;

		@Override
		public void onEvent(SignalDetectorEvent event)
		{
			if (event.isSuccessful())
			{
				if (event.getEventType().equals(SignalDetectorEvent.RECEIVE_SIGNALS_COMPLETED))
				{
					event.getSource().stop();
					MediaSession ms = event.getSource().getMediaSession();
					
					String pin = event.getSignalString();
					if (pin != null)
					{
						try
						{
							//phase 3 serialization
							//MixerMap = (HashMap<String, MediaMixer>) ms.getAttribute("ActiveConferences");						
							
							MediaMixer mx = MixerMap.get(pin);
							if (mx == null)
							{
								// create new mixer.
								//log.debug("************New conference created with pin# = " + pin + "***********");
								mx = ms.createMediaMixer(MediaMixer.AUDIO);
								mx.addListener(new DlgcAllocationEventListener());
								ms.setAttribute("MEDIA_MIXER", mx);			
								MixerMap.put(pin, mx);
								
								//note needed to get this to work with serialization
								SipSession session = (SipSession)ms.getAttribute("SIP_SESSION");
								session.setAttribute("MEDIASESSION", ms);
								
								mx.confirm();
							}
							else
							{
								log.debug("<<<<<<<<<<<<<< Enter existing conference  with pin# = " + pin + ">>>>>>>>>>>>>>>>>>");
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
	
	public class DlgcSdpPortEventListener implements MediaEventListener<SdpPortManagerEvent>, Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -5394942031164984561L;

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
								Parameters parameters = mscFactory.createParameters();
								parameters.put(SignalDetector.PROMPT, prompt);
								mg.getSignalDetector().receiveSignals(4, null, new RTC[]{MediaGroup.SIGDET_STOPPLAY}, parameters);
								
								//note needed to get this to work with serialization
								session.setAttribute("MEDIASESSION", ms);
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
					SipServletResponse response = (SipServletResponse) session.getAttribute("RESPONSE");
					if (response != null)
					{
						try
						{
							response.createAck().send();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						
						MediaGroup mg = (MediaGroup) ms.getAttribute("MEDIA_GROUP");
						
						try
						{
							
							
							Parameters params = mg.createParameters();
							
							params.put(Player.MAX_DURATION, 10000);
							mg.getPlayer().play(prompt2, null, params);
							
							//note needed to get this to work with serialization
							session.setAttribute("MEDIASESSION", ms);
							
						}
						catch (MsControlException e)
						{
							e.printStackTrace();
						}
					}
				}
				else if (event.getEventType().equals(SdpPortManagerEvent.UNSOLICITED_OFFER_GENERATED))
				{
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
				else if (event.getEventType().equals(SdpPortManagerEvent.NETWORK_STREAM_FAILURE))
				{
					//log.info("DlgcSdpPortEventListener::onEvent() - NETWORK STREAM FAILURE: " + event.getErrorText());
					
					try
					{
						session.createRequest("BYE").send();
						sdp.getContainer().release();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private class PlayerEventListener implements MediaEventListener<PlayerEvent>, Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 8857627757822371720L;

		@Override
		public void onEvent(PlayerEvent event)
		{
			count++;
			String countStr =  (new Integer(count)).toString();
			log.debug("******Completed Play Count " + countStr + " *****");
			
			log.info("PlayerEventListener::onEvent() ");
			log.info("   EVENT TYPE : " + event.getEventType());
			log.info("    QUALIFIER : " + event.getQualifier());
			log.info("  CHANGE TYPE : " + event.getChangeType());
			log.info("   PLAY INDEX : " + event.getIndex());
			log.info("  PLAY OFFSET : " + event.getOffset());
			log.info("        ERROR : " + event.getError());
			log.info("   ERROR TEXT : " + event.getErrorText());
			
			MediaSession ms = event.getSource().getMediaSession();
			NetworkConnection nc = (NetworkConnection) ms.getAttribute("NETWORK_CONNECTION");
			MediaMixer mx = (MediaMixer) ms.getAttribute("MEDIA_MIXER");			
			
			
			
			try
			{
				
				if (count == 1)
				{
					MediaGroup mg2 = (MediaGroup) ms.getAttribute("MEDIA_GROUP_2");
					
					//Parameters params = mscFactory.createParameters(); generates null reference
					Parameters params= ms.createParameters();
					
					params.put(Player.MAX_DURATION, 10000);
					mg2.getPlayer().play(prompt2, RTC.NO_RTC, params);
				}
				else if (count == 2)
				{
					mx.unjoin(nc);
					//Parameters params = mscFactory.createParameters();
					Parameters params= ms.createParameters();
					params.put(Player.MAX_DURATION, 10000);
					event.getSource().play(prompt2, RTC.NO_RTC, params);
				}
				else if (count == 3)
				{
					mx.join(Joinable.Direction.DUPLEX, nc);
					//Parameters params = mscFactory.createParameters();
					Parameters params= ms.createParameters();
					params.put(Player.MAX_DURATION, 10000);
					event.getSource().play(prompt2, RTC.NO_RTC, params);
				}
				else if (count == 4)
				{
					//mx.join(Joinable.Direction.RECV, nc);
					nc.unjoin(mx);

					//Parameters params = mscFactory.createParameters();
					Parameters params= ms.createParameters();
					params.put(Player.MAX_DURATION, 10000);
					event.getSource().play(prompt2, RTC.NO_RTC, params);
				}
				else if (count == 5)
				{
					mx.join(Joinable.Direction.SEND, nc);
					//Parameters params = mscFactory.createParameters();
					Parameters params= ms.createParameters();
					params.put(Player.MAX_DURATION, 10000);
					event.getSource().play(prompt2, RTC.NO_RTC, params);
				}
			}
			catch (MsControlException e)
			{
				e.printStackTrace();
			}
			
			//note needed to get this to work with serialization
			SipSession session = (SipSession)ms.getAttribute("SIP_SESSION");
			session.setAttribute("MEDIASESSION", ms);
		}
		
		
	}
	
	
	private int count = 0;
	private URI prompt = null;
	private URI prompt2 = null;
	static private Map<String, MediaMixer> MixerMap = new HashMap<String, MediaMixer>();
	
	private static Logger log = LoggerFactory.getLogger(DlgcComplexConferenceTest.class);
}
