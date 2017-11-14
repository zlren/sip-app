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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MediaEvent;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.networkconnection.SdpPortManagerException;
import javax.media.mscontrol.resource.Resource;
import javax.media.mscontrol.resource.ResourceContainer;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DlgcMgStopBaseTestCase  implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory.getLogger(DlgcMgStopBaseTestCase.class);
	DlgcMgStopTest myMainServlet = null;
	
	public DlgcMgStopBaseTestCase( DlgcMgStopTest mainServlet) {
		myMainServlet = mainServlet;
	}
	
	public void invite(SipServletRequest req) {
		log.debug("Entering DlgcMgStopBaseTestCase:invite() ");
		
		log.debug("Leaving DlgcMgStopBaseTestCase:invite() ");

	}
	
	//doResponse
	public void response(SipServletResponse response)
	{
		log.debug("Entering DlgcMgStopBaseTestCase:response() ");
		
		log.debug("Leaving DlgcMgStopBaseTestCase:response() ");
	}
	
	
	//This method to be override by the detail
	//specific unit test class...
	//This indicates to the unit test that the connection is ready
	//and it can proceed with the detail stop test.
	public void ackResult(SipServletRequest req)
	{
		log.debug("Entering DlgcMgStopBaseTestCase:ackResult() ");
		
		log.debug("Leaving DlgcMgStopBaseTestCase:ackResult() ");
	}
	
	public void ack(SipServletRequest req)
	{
		log.debug("Entering DlgcMgStopBaseTestCase:ack() ");
		SipSession sipSession = req.getSession();
		NetworkConnection networkConnection = (NetworkConnection) sipSession.getAttribute("NETWORK_CONNECTION");
		try
		{
			byte[] remoteSdp = req.getRawContent();
			if (remoteSdp != null)
			{
				//the processSdpAnswer is for the scenario that the original client invite
				//request has a null sdp to start with... This is not supported in this
				//unit test
				networkConnection.getSdpPortManager().processSdpAnswer(remoteSdp);
			}
			ackResult(req);
		}
		catch (MsControlException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		log.debug("Leaving DlgcMgStopBaseTestCase:ack() ");
	}
	
	class SdpEventListener<T extends MediaEvent<?>> implements MediaEventListener<T>, Serializable 
	{
		DlgcMgStopBaseTestCase actualTest = null;
		private static final long serialVersionUID = 1;
		public SdpEventListener(DlgcMgStopBaseTestCase parent)
		{
			actualTest = parent;
		}
		
		public void onEvent(T anEvent) 
		{
			log.debug( " Entering DlgcMgStopBaseTestCase::SdpEventListener ");
			actualTest.onSdpEvent((SdpPortManagerEvent) anEvent);
			log.debug( " Leaving DlgcMgStopBaseTestCase::SdpEventListener ");

		}
	}
		
	protected void executeInvite(final SipServletRequest req, SdpEventListener<SdpPortManagerEvent> sdpListener) throws ServletException, IOException, MsControlException
	{
		log.debug("Entering DlgcMgStopTest::executeInvite");
		NetworkConnection networkConnection = null;
		if (req.isInitial())
		{
			try 
			{
				MediaSession mediaSession = myMainServlet.mscFactory.createMediaSession();
				Parameters pmap = mediaSession.createParameters();

				Integer stimeout = new Integer(5000);
				pmap.put(MediaSession.TIMEOUT, stimeout);
				mediaSession.setParameters(pmap);
				networkConnection = mediaSession.createNetworkConnection(NetworkConnection.BASIC);
				//not needed for this unit test sample just using it to demonstrate 
				Parameters sdpConfiguration = mediaSession.createParameters();
				Map<String,String>  configurationData = new HashMap<String,String>();
				configurationData.put("SIP_REQ_URI_USERNAME", "msml=777");
				networkConnection.setParameters(sdpConfiguration);

				networkConnection.getSdpPortManager().addListener(sdpListener);
				MediaGroup mediaGroup = mediaSession.createMediaGroup(MediaGroup.PLAYER_RECORDER_SIGNALDETECTOR);
				SipSession sipSession = req.getSession();
				URI msURI = mediaSession.getURI();
				sipSession.setAttribute("MSURI", msURI);
				log.debug("DlgcMgStopTest::executeInvite.... Setting mediaSession");
				sipSession.setAttribute("MEDIA_SESSION", mediaSession);
				sipSession.setAttribute("NETWORK_CONNECTION", networkConnection);
				sipSession.setAttribute("MEDIAGROUP", mediaGroup);
				mediaSession.setAttribute("SIP_SESSION", sipSession);
				mediaSession.setAttribute("SIP_REQUEST", req);
				mediaSession.setAttribute("NETWORK_CONNECTION", networkConnection);
			}
			catch (MsControlException e)
			{
				req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
			}
		}
		else
		{
			log.error("executeInvite in wroing state... ignoring request");
			throw new MsControlException("executeInvite::doInvite in wroing state... ignoring request");
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

		log.debug("Leaving DlgcMgStopTest::executeInvite()");

	}

	public boolean onSdpEvent(SdpPortManagerEvent anEvent) 
	{
		log.debug("Entering onSdpEvent received event =" + anEvent.getEventType().toString() );
		boolean status = true;		//no error
		Resource r = (Resource)anEvent.getSource();
		ResourceContainer container = r.getContainer();
		MediaSession ms = container.getMediaSession();
		if ( anEvent.getEventType() == SdpPortManagerEvent.NETWORK_STREAM_FAILURE ) {
			log.debug("Releasing the following component: " + container.toString() + " due to Media Server down.");
			terminateSession(ms); 
			status =false;
		}else {
			//SDP ANSWERD
			SipSession sipSession = (SipSession)ms.getAttribute("SIP_SESSION");
			log.debug("sending 200 ok to sip session");
			try {
				SdpPortManager sdpMgr = ((NetworkConnection)container).getSdpPortManager();
				send200_OK(ms,sipSession, sdpMgr);
			} catch (MsControlException e) {
				log.error("Exception: " + e.toString());
			}
			log.debug("Leaving onSdpEvent  received event =" + anEvent.getEventType().toString() );	
		}
		
		return status;

	}

	
	void terminateSession(SipSession session)
	{
		log.debug("Entering DlgcMgStopTest::terminateSession()");

		if (session != null)
		{
			log.debug("DlgcMgStopTest::terminateSession using Media Session release.... Getting mediaSession");
			MediaSession mediaSession = (MediaSession) session.getAttribute("MEDIA_SESSION");
			mediaSession.release();
			session.invalidate();
		}
		log.debug("Leaving DlgcMgStopTest::terminateSession()");

		
	}
	
	void terminateSession(MediaSession ms)
	{
		log.debug("Entering DlgcMgStopTest::terminateSession(ms)");
		SipSession session = (SipSession) ms.getAttribute("SIP_SESSION");
		if (session != null)
		{
			log.debug("DlgcMgStopTest::terminateSession using Media Session Release .... release media session and sending bye to phone session.");
			ms.release();
			this.sendBye(session, ms);
		}
		log.debug("Leaving DlgcMgStopTest::terminateSession(ms)");

	}
	
	static void send200_OK(MediaSession ms, SipSession sipSession, SdpPortManager sdp) 
	{
		log.debug("Entering DlgcMgStopTest: send200_OK back to the phone");
		SipServletRequest req = (SipServletRequest)ms.getAttribute("SIP_REQUEST");

		try {
			SipServletResponse response = req.createResponse(SipServletResponse.SC_OK);
			try {
				response.setContent(sdp.getMediaServerSessionDescription(), "application/sdp");
				response.send();
			} catch (Exception e1) {
				log.error("Exception: " + e1.toString());
			}
		} catch (Exception e) {
			log.error("Exception: " + e.toString());
		}
		log.debug("Leaving DlgcMgStopTest: send200_OK back to the phone");

	}
	
	public class DlgcSdpPortEventListener implements MediaEventListener<SdpPortManagerEvent> , Serializable
	{

		private static final long serialVersionUID = 1L;

		@Override
		public void onEvent(SdpPortManagerEvent event)
		{
			log.info("sdpPortEventListener::onEvent()");
			SdpPortManager sdp = event.getSource();
			SipServletRequest request = null;
			String desc=null;
			try {
				if ( event.getError() != MediaErr.RESOURCE_UNAVAILABLE ) {
					desc = sdp.getMediaServerSessionDescription().toString();
					if (desc !=null)
						log.error("DlgcSdpPortEventListener:: desc: " + desc);
				}
				else {
					log.error("Can't connect to Media Server...Media Server maybe down");
					log.error(event.getErrorText());
					MediaSession ms = sdp.getMediaSession();
					SipSession session = (SipSession) ms.getAttribute("SIP_SESSION");
					request = session.createRequest("BYE");
					try
					{
						request.send();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			} catch (SdpPortManagerException e1) {
				log.debug(e1.toString());
			}
			
			MediaSession ms = sdp.getMediaSession();
			SipSession session = (SipSession) ms.getAttribute("SIP_SESSION");
			
			if (session.isValid())
			{
				
				session.removeAttribute("UNANSWERTED_INVITE");
				if (event.getEventType().equals(SdpPortManagerEvent.ANSWER_GENERATED))
				{
					request = (SipServletRequest) session.getAttribute("UNANSWERED_INVITE");
					
					if (event.isSuccessful())
					{
						SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
						try
						{
							response.setContent(sdp.getMediaServerSessionDescription(), "application/sdp");
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
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						log.error("ANSWER GENERATED WITH " + event.getError().toString() + " FAILURE - " + event.getErrorText());
						try
						{
							request.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}
				else if (event.getEventType().equals(SdpPortManagerEvent.NETWORK_STREAM_FAILURE))
				{
					log.info("DlgcSdpPortEventListener::onEvent() - NETWORK STREAM FAILURE: " + event.getErrorText());
					
					request = session.createRequest("BYE");
					try
					{
						//log.debug("Media Server descriptor: " + (sdp.getMediaServerSessionDescription()).toString() );
						request.send();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	//send bye to phone
	protected void sendBye(SipSession sipSession, MediaSession mediaSession) {

		SipServletRequest bye = sipSession.createRequest("BYE");
		log.debug("Entering DlgcMgStopTest::sendBye()");
		try 
		{
			bye.send();
		} 
		catch (Exception e1) 
		{
			log.error("Terminating: Cannot send BYE: "+e1);
		}
		log.debug("Leaving DlgcMgStopTest::sendBye()");


	}

}
