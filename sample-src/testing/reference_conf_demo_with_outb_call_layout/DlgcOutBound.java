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
package testing.reference_conf_demo_with_outb_call_layout;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.media.mscontrol.MediaSession;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionsUtil;
import javax.servlet.sip.SipURI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DlgcOutBound implements Serializable 
{

	private static final long serialVersionUID = 1;
	protected MediaSession xmsMediaSession = null;
	Address myAppSipFullAddress = null;
	Address tuaSipFullAddress = null;
	SipURI myAppSipURI = null;
	SipURI myTuaSipURI=null;
	static  final String SIP_INVITE_METHOD = "INVITE";
	static  final String SIP_INVITE_SDP_CONTENT_TYPE = "application/sdp";
	DlgcReferenceConferenceWithOutBCallServlet servlet = null;
	
	transient SipSession tuaSipSession = null;
	String tuaSipSessionID = null;
	String tuaSipApplicationSessionID = null;
	
	DlgcReferenceConferenceParticipantWOBC participant;
		
	private static Logger log = LoggerFactory.getLogger(DlgcOutBound.class);

	public DlgcOutBound(DlgcReferenceConferenceParticipantWOBC partLeg, DlgcReferenceConferenceWithOutBCallServlet s)
	{
		participant = partLeg;
		servlet = s;
		initializeConnectionAddresses();
		
	}
	
	public SipSession loadSipSession()
	{
		SipSessionsUtil ssu = DlgcReferenceConferenceWithOutBCallServlet.getSSU();
		SipApplicationSession sas = ssu.getApplicationSessionById(tuaSipApplicationSessionID);
		tuaSipSession = sas.getSipSession(tuaSipSessionID);
		log.debug("DlgcReferenceConferenceParticipant:: loadSipSession: Application Sip Session: " + tuaSipSession.toString());
		return tuaSipSession;
	}

	public void makeOutboundCall(String msAnsweredSDP  )
	{
		
		log.debug("KAPANGA [4] TUA => OUT- sendOutboundCall Invite TUA with media server SDP = " + msAnsweredSDP );
		SipServletRequest request = createTUAInviteRequest(msAnsweredSDP);
		tuaSipSession = request.getSession();
		
		this.tuaSipSessionID = tuaSipSession.getId();
		this.tuaSipApplicationSessionID = tuaSipSession.getApplicationSession().getId();
		participant.clientSSID = this.tuaSipSessionID;
		participant.clientSASID = this.tuaSipApplicationSessionID ; 
		
		participant.ms.setAttribute("ParticipantId", participant.clientSSID);
		try {
			log.debug("Sending the following outbound call: " + request.toString());
			request.send();
			log.debug("Return from sending the outbound call");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendAck(SipServletResponse response)
	{
		if (tuaSipSession != null && tuaSipSession.isValid() )
		{
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

		SipFactory sf = DlgcReferenceConferenceWithOutBCallServlet.sip289factory;
		String dname = DlgcReferenceConferenceWithOutBCallServlet.applicationDisplayNamePropertyId;
		String myIpAddr = servlet.emApplicationIpAddrStr;
		try {
			myAppSipURI = sf.createSipURI(dname, myIpAddr);
			myAppSipURI.setPort(servlet.emApplicationPortInt);
			myAppSipURI.setUser(DlgcReferenceConferenceWithOutBCallServlet.applicationUserNamePropertyId);	
			myAppSipFullAddress = DlgcReferenceConferenceWithOutBCallServlet.sip289factory.createAddress(myAppSipURI);
			myAppSipFullAddress.setDisplayName(DlgcReferenceConferenceWithOutBCallServlet.applicationDisplayNamePropertyId);

			myTuaSipURI = DlgcReferenceConferenceWithOutBCallServlet.sip289factory.createSipURI(servlet.externalSipTOAUsername, servlet.externalSipTOAIpAddrStr);
			myTuaSipURI.setPort(servlet.externalSipTOAPortInt);
			tuaSipFullAddress = DlgcReferenceConferenceWithOutBCallServlet.sip289factory.createAddress(myTuaSipURI);
			tuaSipFullAddress.setDisplayName(servlet.externalSipTOAUsername);
		}catch(Exception e) {
			e.printStackTrace();
		}

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
		SipApplicationSession appSipSession = DlgcReferenceConferenceWithOutBCallServlet.sip289factory.createApplicationSession();
		SipServletRequest tuaSipInviteRequest = DlgcReferenceConferenceWithOutBCallServlet.sip289factory.createRequest(appSipSession, SIP_INVITE_METHOD, myAppSipFullAddress, tuaSipFullAddress);
		
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
				requestSipSession.setHandler(DlgcReferenceConferenceWithOutBCallServlet.sipServletHandler);
				tuaSipInviteRequest.setRequestURI(myTuaSipURI);
				this.tuaSipSession = requestSipSession;
			} catch (ServletException e) {
				e.printStackTrace();
			}
		}
		return tuaSipInviteRequest;
	}	
	
		
	public void handle200Response(SipServletResponse response)
	{

		SipServletRequest ackReq = response.createAck();
		try {
			ackReq.send();
		} catch (IOException e) {
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

