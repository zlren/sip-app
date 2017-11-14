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

import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.resource.AllocationEvent;
import javax.media.mscontrol.resource.AllocationEventListener;
import javax.media.mscontrol.resource.ResourceContainer;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.DlgcTest;

public class DlgcCallControlTest extends DlgcTest
{
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
	
	private class DlgcAllocationEventListener implements AllocationEventListener , Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 4633218747426251274L;

		@Override
		public void onEvent(AllocationEvent event)
		{
			if (event.getEventType().equals(AllocationEvent.IRRECOVERABLE_FAILURE))
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
	
	@Override
	public void servletInitialized(SipServletContextEvent evt){

		String sName = evt.getSipServlet().getServletName();
		if( sName.equalsIgnoreCase("DlgcSipServlet") )
		{
			dlgcSipServletLoaded = true;
			log.debug(" DlgcCallControlTest::servletInitialized DlgcSipServlet loaded");			
			return;
		}

		if( (sName.equalsIgnoreCase("DlgcCallControlTest") ) && dlgcSipServletLoaded)
		{
			if ( servletInitializedFlag == false ) {
				log.debug("Entering DlgcCallControlTest::servletInitialized servletName: " + sName);			
				servletInitializedFlag = true;
				initDriver();
				//myServletInitialized(evt);
			} else {
				log.debug("DlgcCallControlTest::servletInitialized(): already servletInitialized was called...debouncing " + sName);
			}
		}
	}

	/*@Override
	public void init(ServletConfig cfg)
		throws ServletException
	{
		super.init(cfg);
		
		try
		{
			Driver dlgcDriver = DriverManager.getDriver(dlgcDriverName);
			mscFactory = dlgcDriver.getFactory(null);
			speListener = new sdpPortEventListener();
		}
		catch (Exception e)
		{
			throw new ServletException(e);
		}
	}*/
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8455647898994483737L;

	@Override
	public void doBye(final SipServletRequest req)
		throws ServletException, IOException
	{
		/*MediaSession ms = (MediaSession) req.getSession().getAttribute("MEDIA_SESSION");
		if (ms != null)
		{
			ms.release();
		}
		
		req.createResponse(SipServletResponse.SC_OK).send();
		releaseSession(req.getSession());*/
		
		super.doBye(req);
		
		
	}
	
	@Override
	public void doInvite(final SipServletRequest req)
		throws ServletException, IOException
	{
		/*log.info("doInvite");
		
		NetworkConnection networkConnection = null;
		SipSession sipSession = req.getSession();
		
		if (req.isInitial())
		{
			// We have a new call.
			try 
			{
				MediaSession mediaSession = mscFactory.createMediaSession();
				networkConnection = mediaSession.createNetworkConnection(NetworkConnection.BASIC);
				
				sipSession.setAttribute("MEDIA_SESSION", mediaSession);
				sipSession.setAttribute("NETWORK_CONNECTION", networkConnection);
				
				mediaSession.setAttribute("SIP_SESSION", sipSession);
				mediaSession.setAttribute("SIP_REQUEST", req);
				mediaSession.setAttribute("NETWORK_CONNECTION", networkConnection);
				
				networkConnection.getSdpPortManager().addListener(speListener);
				
			}
			catch (MsControlException e)
			{
				req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
			}
		}
		else
		{
			// Re-invite on existing call.
			networkConnection = (NetworkConnection) sipSession.getAttribute("NETWORK_CONNECTION");
		}*/
		super.doInvite(req);
		ResourceContainer rc = (ResourceContainer) req.getSession().getAttribute("NETWORK_CONNECTION");
		if (rc != null)
		{
			rc.addListener(new DlgcAllocationEventListener());
		}
		//doModify(req, networkConnection);
	}
	
	/*private void doModify(SipServletRequest req, NetworkConnection conn)
		throws ServletException, IOException
	{
		try
		{
			req.getSession().setAttribute("UNANSWERED_INVITE", req);
			//req.getSession().setAttribute("STATE", )
			
			byte[] remoteSdp = req.getRawContent();
			
			if (remoteSdp == null)
			{
				conn.getSdpPortManager().generateSdpOffer();
			}
			else
			{
				conn.getSdpPortManager().processSdpOffer(remoteSdp);
			}
		} 
		catch (MsControlException e)
		{
			req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
		}
	}*/

	/*private class sdpPortEventListener implements MediaEventListener<SdpPortManagerEvent>
	{
		@Override
		public void onEvent(SdpPortManagerEvent event)
		{
			log.info("sdpPortEventListener::onEvent()");
			
			SdpPortManager sdp = event.getSource();
			SipSession session = (SipSession) sdp.getMediaSession().getAttribute("SIP_SESSION");
			NetworkConnection nc = (NetworkConnection) sdp.getMediaSession().getAttribute("NETWORK_CONNECTION");
			
			if (session.isValid())
			{
				SipServletRequest request = (SipServletRequest) session.getAttribute("UNANSWERED_INVITE");
				session.removeAttribute("UNANSWERTED_INVITE");
				if (event.getEventType().equals(SdpPortManagerEvent.ANSWER_GENERATED))
				{
					SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
					try
					{
						response.setContent(nc.getSdpPortManager().getMediaServerSessionDescription(), "application/sdp");
						response.send();
					}
					catch (UnsupportedEncodingException e)
					{
						e.printStackTrace();
					}
					catch (SdpPortManagerException e)
					{
						e.printStackTrace();
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
		}
	}*/
	
	/*protected void releaseSession(SipSession session)
	{
		try
		{
			session.invalidate();
			session.getApplicationSession().invalidate();
		}
		catch (Exception e)
		{	
		}
	}*/

	private static Logger log = LoggerFactory.getLogger(DlgcCallControlTest.class);
}
