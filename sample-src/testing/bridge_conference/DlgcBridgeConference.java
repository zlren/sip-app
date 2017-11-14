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

package testing.bridge_conference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.Joinable.Direction;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * In a bridge conference scenario only two legs can be joined in full duplex
 * other legs can be join but in listening only (i.e. direction RECV ONLY )
 */

public class DlgcBridgeConference implements Serializable 
{
	private static final long serialVersionUID = 1286700045L;
	private static Logger log = LoggerFactory.getLogger(DlgcBridgeConference.class);
	
	enum ConferenceState {
			NOT_IN_CONF,
			CONF_PEND,
			IN_CONF
	};
	
	private ConferenceState myConfState;
	
	//protected MediaMixer 		confMediaMixer = null; no media mixer in this unit test
	DlgcBridgeServlet  myControlServlet = null;
	MediaSession				myMediaSession = null;
	boolean						mixerRdy = false;
	
	List<DlgcBridgeParticipant> participantList = null; 
	
	@SuppressWarnings("static-access")
	public DlgcBridgeConference(DlgcBridgeServlet servlet) 
	{	
		myControlServlet 	= servlet;
		myConfState = ConferenceState.NOT_IN_CONF;
		try {
			myMediaSession = servlet.theMediaSessionFactory.createMediaSession();
		} catch (MsControlException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		participantList = new ArrayList<DlgcBridgeParticipant> ();
	}
	
	public DlgcBridgeServlet getControlServlet()
	{
		return myControlServlet;
	}
	
	/*
	 * In a bridge conference scenario only two legs can be joined in full duplex
	 * other legs can be join but in listening only (i.e. direction RECV ONLY )
	 */
	public void addNewParticipant(SipServletRequest req, String name) 
	{
		
		SipSession mySipSession = req.getSession();
		
		if (req.isInitial()) {
			try {
				DlgcBridgeParticipant participant = new DlgcBridgeParticipant(this, mySipSession,name);	
				if ( participant != null) {
					participantList.add(participant);
					participant.connectLeg(req);
				} else
					log.error("Error getting participant during addNewParticipant Method and request is  initial request");
			} catch (Exception e) {
				log.error("Cannot add new participant:", e);
				
			}
		}
		else {
			log.error("Error getting participant during addNewParticipant Method and request is not initial request");
		}
		
		
	}
	
	public void processSipPhoneAck( SipServletRequest req)
	{
		SipSession session = req.getSession();
		DlgcBridgeParticipant participant = (DlgcBridgeParticipant)session.getAttribute("PARTICIPANT");
		
		try {
			participant.processSipPhoneAck(req);
			session.setAttribute("PARTICIPANT",participant);
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		


	public void removeParticipant(SipServletResponse response) 
	{
		SipSession mySipSession = response.getRequest().getSession();
		DlgcBridgeParticipant participant = (DlgcBridgeParticipant)mySipSession.getAttribute("PARTICIPANT");
		releaseParticipant(participant);
	}	
	
	public void removeParticipantFromConferenceList(DlgcBridgeParticipant participant) 
	{
		this.participantList.remove(participant);
		if ( participantList.size() < 2 ) {
			this.myConfState = ConferenceState.NOT_IN_CONF;
			DlgcBridgeParticipant partner = participant.bridgePartner;
			if ( partner != null )
				partner.setState(partner.previousState, DlgcBridgeParticipantState.dlgcParticipantDtmfEnabledState);
		}
	}
	
	public void addParticipantFromConferenceList(DlgcBridgeParticipant participant) 
	{
		this.participantList.add(participant);
	}
	
	public void removeParticipant(SipSession mySipSession ) 
	{
		DlgcBridgeParticipant participant = (DlgcBridgeParticipant)mySipSession.getAttribute("PARTICIPANT");
		releaseParticipant(participant);
	}	
	
	public void releaseParticipant(DlgcBridgeParticipant participant) 
	{

		log.debug("Entering releaseParticipant" );
		if ( participant != null ) {
			try {
				participant.release();
			} catch (MsControlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			participantList.remove(participant);
		}
		log.debug("Bridge Conference number of participant is " + (new Integer(participantList.size())).toString() );
		log.debug("Leaving releaseParticipant" );
	}
	
	public void setConfState( ConferenceState state ) {
		myConfState = state;
	}
	
	public ConferenceState getConfState()
	{
		return myConfState;
	}
	
	public DlgcBridgeParticipant findADestinationParticipant( DlgcBridgeParticipant source)
	{
		DlgcBridgeParticipant dest = null;
		
		Iterator< DlgcBridgeParticipant > parIt = participantList.iterator();
		
		DlgcBridgeParticipant findDest = null;
		while ( parIt.hasNext() ) 
		{
			findDest = parIt.next();
			if ( findDest.equals(source) == false )
			{
				dest = findDest;
				break;
			}
		}
		return dest;
	}
	
	public void bridgeLeg(DlgcBridgeParticipant sourceParticipant)
	{
		DlgcBridgeParticipant destParticipant = findADestinationParticipant( sourceParticipant );
		if ( destParticipant != null ) {
			try {
				sourceParticipant.bridgeLegs(destParticipant, Direction.DUPLEX);
			} catch (MsControlException e) {
				// TODO Auto-generated catch block
				log.error("Fail to bridgeLegs exception=" + e);
				e.printStackTrace();
			}
			
		}else {
			log.error("Can't join participant into a bridge conference - no destination participant found...");
		}
		
	}
	
}

