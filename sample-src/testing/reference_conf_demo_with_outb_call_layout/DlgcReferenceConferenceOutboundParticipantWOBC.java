
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

import javax.media.mscontrol.MsControlException;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Remember the Conference room The parent for DlgcReferenceConferenceParticipant
 *  - Has its own MediaSession, and a MediaMixer
 */

public class DlgcReferenceConferenceOutboundParticipantWOBC extends DlgcReferenceConferenceParticipantWOBC {


	private static final long serialVersionUID = 1L;
	
	
	public static Logger log = LoggerFactory.getLogger(DlgcReferenceConferenceOutboundParticipantWOBC.class);
	protected DlgcOutBound  outBoundCaller;
	
	public DlgcReferenceConferenceOutboundParticipantWOBC(DlgcReferenceConferenceWOBC conference, DlgcOutbCallConferenceStorage cfStorage) 
	{
			super(conference, cfStorage);
			outBoundCaller = new DlgcOutBound(this, conference.getControlServlet());
	}
	
	
	//Ask Media Server to generate an SDP offer to be used for early media
	//public void connectLegNoSDP() throws ServletException, IOException
	@Override
	public void generateSdpOfferToMS() throws ServletException, IOException
	{
		log.debug("Entering DlgcReferenceConferenceOutboundParticipantWOBC::generateSdpOfferToMS()");
		try {
			log.debug("call askMediaServerToGenerateSDP()");
			presentState.askMediaServerToGenerateSDP(this);
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug("Leaving DlgcReferenceConferenceOutboundParticipantWOBC::generateSdpOfferToMS()");

	}
	
		
	@Override
	public void makeOutboundCall( String msAnsweredSDPString)
	{
		this.outBoundCaller.makeOutboundCall(msAnsweredSDPString);
		
	}

	
		
	@Override
	public void release() throws MsControlException
	{
		if (clientEndPointSipSession != null && clientEndPointSipSession.isValid()) 
		{
			log.info("["+clientEndPointSipSession.getCallId()+"] calling mediaSession.release() for NC");
			ms.release();
			log.info("["+clientEndPointSipSession.getCallId()+"] calling mediaSession.release() for NC done");		
			if (clientEndPointSipSession.isValid()) {
				clientEndPointSipSession.invalidate();
			}
			if (clientEndPointSipSession.getApplicationSession().isValid()) {
				clientEndPointSipSession.getApplicationSession().invalidate();
			}
		}
	}
	
	@Override
	protected void sendBye() 
	{
		// Need to check the state of the SipSession in order to make sure user
		// did not already terminate session from their end.
		Boolean releasing = (Boolean)clientEndPointSipSession.getAttribute("RELEASING");
		if (clientEndPointSipSession != null && clientEndPointSipSession.isValid() && clientEndPointSipSession.getState() != SipSession.State.TERMINATED && !releasing)
		{
			clientEndPointSipSession.setAttribute("RELEASING", true);
			log.info("["+clientEndPointSipSession.getCallId()+"] SENDING BYE");
			try {
				SipServletRequest req = clientEndPointSipSession.createRequest("BYE");
				req.send();
			}
			catch (IOException e) {
				log.error("Unable to send BYE to user agent: "+e);
			}
		}
	}
	
	@Override
	public void terminate(Exception e) {
		
		log.info("["+clientEndPointSipSession.getCallId()+"] *TERMINATE EXCEPTION* -> "+e);
		e.printStackTrace();
		sendBye();
		try {
			release();
		} catch (MsControlException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Override
	public void processSipPhoneAck(SipServletRequest request) throws MsControlException 
	{
		log.debug("Entering Outbound Caller Leg: processSipPhoneAck() " );
		presentState.processSipPhoneAckAnswer(this, localSdp);
		presentState.enableAsyncDtmf(this);	
		log.debug("Leaving Outbound Caller Leg: processSipPhoneAck() " );

	}	
	
	@Override
	public void processSipPhone200Invite(SipServletResponse response) throws MsControlException 
	{
		log.debug("Entering processSipPhoneAck() " );
		presentState.outboundCallResponse(this, response);
		presentState.enableAsyncDtmf(this);	
		log.debug("Leaving processSipPhoneAck() " );

	}
	
	
}
