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
import java.net.URI;
import java.util.LinkedList;
import java.util.Queue;

import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.JoinEvent;
import javax.media.mscontrol.join.JoinEventListener;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.Player;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.resource.RTC;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.DlgcTest;

public class DlgcAsyncPlayerTest extends DlgcTest 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8719091922024701887L;

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
		parameters.put(Player.REPEAT_COUNT, 2);
		parameters.put(Player.INTERVAL, 2000);
		parameters.put(Player.MAX_DURATION, 10000);
		
		try
		{
			mediaGroup.getPlayer().play(prompt, RTC.NO_RTC, parameters);
			uriQueue.add(prompt);
		}
		catch (MsControlException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void doInvite(final SipServletRequest req)
		throws ServletException, IOException
	{
		log.info("doInvite");
		
		NetworkConnection networkConnection = null;
		
		if (req.isInitial())
		{
			try 
			{
				MediaSession mediaSession = mscFactory.createMediaSession();
				networkConnection = mediaSession.createNetworkConnection(NetworkConnection.BASIC);
				MediaGroup mediaGroup = mediaSession.createMediaGroup(MediaGroup.PLAYER);
				
				SipSession sipSession = req.getSession();
				sipSession.setAttribute("MEDIA_SESSION", mediaSession);
				sipSession.setAttribute("NETWORK_CONNECTION", networkConnection);
				sipSession.setAttribute("MEDIAGROUP", mediaGroup);
				
				mediaSession.setAttribute("SIP_SESSION", sipSession);
				mediaSession.setAttribute("SIP_REQUEST", req);
				mediaSession.setAttribute("NETWORK_CONNECTION", networkConnection);
				
				networkConnection.getSdpPortManager().addListener(speListener);
				mediaGroup.getPlayer().addListener(new PlayerEventListener());
				mediaGroup.addListener(new JoinEventHandler());
				
				mediaGroup.joinInitiate(Joinable.Direction.DUPLEX, networkConnection, (Serializable)mediaSession);
			}
			catch (MsControlException e)
			{
				req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
			}
		}
		else
		{
			
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

	@Override
	public void init(ServletConfig cfg)
		throws ServletException
	{
		super.init(cfg);
		myServletLoaded = true;

		prompt = URI.create("file:////opt/snowshore/prompts/generic/en_US/contact_provider.ulaw");
		prompt2 = URI.create("file:////opt/snowshore/prompts/generic/en_US/circuit_busy.ulaw");
		prompt3 = URI.create("file:////opt/snowshore/prompts/generic/en_US/dial_operator.ulaw");
	}
	
	@Override
	public void servletInitialized(SipServletContextEvent evt){

		String sName = evt.getSipServlet().getServletName();
		
		if( sName.equalsIgnoreCase("DlgcSipServlet") )
		{
			dlgcSipServletLoaded = true;
			log.debug(" DlgcAsyncPlayerTest::servletInitialized DlgcSipServlet loaded");			
			return;
		}

		if( (sName.equalsIgnoreCase("DlgcAsyncPlayerTest") ) && dlgcSipServletLoaded)
		{
			if ( servletInitializedFlag == false ) {
				log.debug("Entering DlgcAsyncPlayerTest::servletInitialized servletName: " + sName);			
				servletInitializedFlag = true;
				initDriver();
				//myServletInitialized(evt);
			} else {
				log.debug("DlgcRecorderTest::DlgcAsyncPlayerTest(): already servletInitialized was called...debouncing " + sName);
			}
		}
	}
	
	private class PlayerEventListener implements MediaEventListener<PlayerEvent>, Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -6948053425989674370L;

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
			
			uriQueue.poll();
			
			if (uriQueue.isEmpty())
			{
				MediaSession mediaSession = event.getSource().getMediaSession();
				SipSession session = (SipSession) mediaSession.getAttribute("SIP_SESSION");
				MediaGroup mg = (MediaGroup) session.getAttribute("MEDIAGROUP");
				if (mg != null)
				{
					NetworkConnection nc = (NetworkConnection) mediaSession.getAttribute("NETWORK_CONNECTION");
					try
					{
						mg.unjoinInitiate(nc, (Serializable)mediaSession);
					}
					catch (MsControlException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private class JoinEventHandler implements JoinEventListener, Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 3290940441675987601L;

		@Override
		public void onEvent(JoinEvent event)
		{
			try
			{
				MediaSession ms = (MediaSession) event.getContext();
				if (event.getEventType().equals(JoinEvent.JOINED))
				{
					//MediaSession ms = (MediaSession) event.getContext();
					if (ms != null)
					{
						SipServletRequest request = (SipServletRequest) ms.getAttribute("SIP_REQUEST");
						NetworkConnection nc = (NetworkConnection) ms.getAttribute("NETWORK_CONNECTION");
						if (nc != null)
						{
							request.getSession().setAttribute("UNANSWERED_INVITE", request);
							byte[] remoteSdp = request.getRawContent();
							if (remoteSdp == null)
							{
								nc.getSdpPortManager().generateSdpOffer();
							}
							else
							{
								nc.getSdpPortManager().processSdpOffer(remoteSdp);
							}
						}
					}
				}
				else if (event.getEventType().equals(JoinEvent.UNJOINED))
				{
					//MediaSession
					SipSession session = (SipSession) ms.getAttribute("SIP_SESSION");
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
			catch (MsControlException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
		}
		
	}
	
	protected Configuration<MediaGroup> configuration;
	protected URI						prompt;
	protected URI						prompt2;
	protected URI						prompt3;
	
	//chage uriQue to static for serialization may have synchronization problem
	static private Queue<URI> uriQueue = new LinkedList<URI>();
	
	private static Logger log = LoggerFactory.getLogger(DlgcAsyncPlayerTest.class);

}
