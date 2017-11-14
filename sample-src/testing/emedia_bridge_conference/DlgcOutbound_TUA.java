/**
 * DIALOGIC CONFIDENTIAL
 * Copyright (C) 2005-2012 Dialogic Corporation. All Rights Reserved.
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

package testing.emedia_bridge_conference;


import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vendor.dialogic.javax.media.mscontrol.sip.DlgcSipServlet;

import testing.bridge_conference.DlgcBridgeParticipant;




public class DlgcOutbound_TUA implements Serializable 
{

	private static final long serialVersionUID = 190174333657L;
	protected MediaSession xmsMediaSession = null;
	Address myAppSipFullAddress = null;
	Address tuaSipFullAddress = null;
	SipURI myAppSipURI = null;
	SipURI myTuaSipURI=null;
	static  final String SIP_INVITE_METHOD = "INVITE";
	static  final String SIP_INVITE_SDP_CONTENT_TYPE = "application/sdp";
	DlgcEMBridgeServlet servlet = null;
	byte[] remoteSdp = null;
	SipSession tuaSipSession = null;
	DlgcEMBridgeParticipant participant = null;

	
	private static Logger log = LoggerFactory.getLogger(DlgcOutbound_TUA.class);

	public DlgcOutbound_TUA(DlgcEMBridgeParticipant partLeg, MediaSession session, DlgcEMBridgeServlet myServlet)
	{
		servlet = myServlet;
		xmsMediaSession = session;
		participant = partLeg;
		initializeConnectionAddresses();
	}

	public void sendOutboutCall( String msAnsweredSDP )
	{
		log.debug("KAPANGA [4] TUA => OUT- sendOutboundCall Invite TUA with media server SDP = " + msAnsweredSDP );
		SipServletRequest request = createTUAInviteRequest(msAnsweredSDP);
		
		tuaSipSession = request.getSession();
		if ( tuaSipSession != null ) {
			tuaSipSession.setAttribute("MEDIA_SESSION", xmsMediaSession);  
			tuaSipSession.setAttribute("PARTICIPANT", participant);  
			
		}
		try {
			request.send();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendAck(SipServletResponse response)
	{
		if (tuaSipSession != null && tuaSipSession.isValid() )
		{
			//SipServletResponse resp = (SipServletResponse)tuaSipSession.getAttribute("TUA_INITIAL_200_OK_RESPONSE");
			try {
				SipServletRequest req = response.createAck();
				req.send();
			}
			catch (IOException e) {
				log.error("Unable to send TUA ACK: "+e);
			}
		}
	}


	protected void initializeConnectionAddresses()
	{

		myAppSipURI = DlgcEMBridgeServlet.sip289factory.createSipURI(DlgcEMBridgeServlet.applicationDisplayNamePropertyId, servlet.emApplicationIpAddrStr);
		myAppSipURI.setPort(servlet.emApplicationPortInt);
		myAppSipURI.setUser(DlgcEMBridgeServlet.applicationUserNamePropertyId);	
		myAppSipFullAddress = DlgcEMBridgeServlet.sip289factory.createAddress(myAppSipURI);
		myAppSipFullAddress.setDisplayName(DlgcEMBridgeServlet.applicationDisplayNamePropertyId);

		myTuaSipURI = DlgcEMBridgeServlet.sip289factory.createSipURI(servlet.externalSipTOAUsername, servlet.externalSipTOAIpAddrStr);
		myTuaSipURI.setPort(servlet.externalSipTOAPortInt);
		tuaSipFullAddress = DlgcEMBridgeServlet.sip289factory.createAddress(myTuaSipURI);
		tuaSipFullAddress.setDisplayName(servlet.externalSipTOAUsername);

	}
	
	
	public void setAnsweredSDP(SipServletRequest request, String msAnsweredSDP )
	{
		if (request != null) {
			try {
				request.setContent (msAnsweredSDP, SIP_INVITE_SDP_CONTENT_TYPE);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
	

	SipServletRequest createTUAInviteRequest(String msAnsweredSDP)
	{
			
		SipServletRequest tuaSipInviteRequest = DlgcEMBridgeServlet.sip289factory.createRequest(DlgcEMBridgeServlet.appSipSession, SIP_INVITE_METHOD, myAppSipFullAddress, tuaSipFullAddress);
		
		if (tuaSipInviteRequest != null)
		{
			SipSession requestSipSession = tuaSipInviteRequest.getSession();
			try {
				try {
					if ( msAnsweredSDP != null )
						tuaSipInviteRequest.setContent (msAnsweredSDP, SIP_INVITE_SDP_CONTENT_TYPE);
				} catch (UnsupportedEncodingException e) {
					
					e.printStackTrace();
				}
				requestSipSession.setHandler(servlet.sipServletHandler);
				tuaSipInviteRequest.setRequestURI(myTuaSipURI);
			} catch (ServletException e) {
				e.printStackTrace();
			}
		}
		return tuaSipInviteRequest;
	}	
	
	public void handle183Response(SipServletResponse response)
	{
		try {
			this.participant.presentState.connectedLegResponse( this.participant,  response );
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void handle200Response(SipServletResponse response)
	{
		try {
			this.participant.presentState.connectedLegResponse( this.participant,  response );
			SipServletRequest ackReq = response.createAck();
			try {
				ackReq.send();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendBye()
	{
		if (tuaSipSession != null && tuaSipSession.isValid() )
		{
			//SipServletResponse resp = (SipServletResponse)tuaSipSession.getAttribute("TUA_INITIAL_200_OK_RESPONSE");
			try {
				SipServletRequest byeReq = this.tuaSipSession.createRequest("BYE");
				byeReq.send();
			}
			catch (IOException e) {
				log.error("Unable to send TUA BYE: "+e);
			}
		}
	}
	
}
