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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.JoinEvent;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.join.Joinable.Direction;
import javax.media.mscontrol.join.JoinEventListener;

import javax.media.mscontrol.mixer.MediaMixer;


import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;

import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




//import testing.quick_conference.DlgcQuickConferenceParticipant;

/*
 * In a bridge conference scenario only two legs can be joined in full duplex
 * other legs can be join but in listening only (i.e. direction RECV ONLY )
 */

public class DlgcEMBridgeConference implements Serializable 
{
	private static final long serialVersionUID = 716700045L;
	private static Logger log = LoggerFactory.getLogger(DlgcEMBridgeConference.class);
	
	enum ConferenceState {
			NOT_IN_CONF,
			CONF_PEND,
			IN_CONF
	};
	
	enum ConferenceMode {
		BRIDGE_MODE,
		MIXER_MODE
	};
	
	private ConferenceState myConfState;
	ConferenceMode  myConfMode;
	
	MediaMixer 					confMediaMixer = null; 
	DlgcEMBridgeServlet  		myControlServlet = null;
	MediaSession				myMediaSession = null;
	boolean						mixerRdy = false;
	
	List<DlgcEMBridgeParticipant> participantList = null; 
	
	
	public DlgcEMBridgeConference(DlgcEMBridgeServlet servlet) 
	{	
		myControlServlet 	= servlet;
		myConfState = ConferenceState.NOT_IN_CONF;
		myConfMode = ConferenceMode.BRIDGE_MODE;
		
		
	//	try {
	//		myMediaSession = servlet.theMediaSessionFactory.createMediaSession();
	//	} catch (MsControlException e1) {
			// TODO Auto-generated catch block
		//	e1.printStackTrace();
	//	}
		
		participantList = new ArrayList<DlgcEMBridgeParticipant> ();
	}
	
	public ConferenceState getState()
	{
		return myConfState;
	}
	
	public DlgcEMBridgeServlet getControlServlet()
	{
		return myControlServlet;
	}
	
	/*
	 * In a bridge conference scenario only two legs can be joined in full duplex
	 * other legs can be join but in listening only (i.e. direction RECV ONLY )
	 */
	public void addOUAParticipant(SipServletRequest req) 
	{
		
		SipSession mySipSession = req.getSession();
		
		if (req.isInitial()) {
			try {
				int numOfParticipants = participantList.size();
				if (numOfParticipants < 2 ) {
					DlgcEMBridgeParticipant participant = new DlgcEMBridgeParticipant(this, mySipSession, "OUA-PARTICIPANT");
					mySipSession.setAttribute("INITIAL_INVITE_REQUEST", req);
					
					if ( participant != null) {
						participantList.add(participant);
						//add first participant in this demo test we will connect the first participant leg to the MS
						participant.connectLeg(req);
					} else
						log.error("Error getting participant during addNewParticipant Method and request is  initial request");
				} else {
					log.error( "Error only two participants in a bridge is allowed...exceeded number of allowed participants" );
				}
			} catch (Exception e) {
				log.error("Cannot add new participant:", e);
				
			}
		}
		else {
			log.error("Error getting participant during addNewParticipant Method and request is not initial request");
		}
	}
	
		
	public void addNewTUAParticipant() 
	{
		try {
			DlgcEMBridgeParticipant participant = new DlgcEMBridgeParticipant(this, null,"TUA-PARTICIPANT");	

			if ( participant != null) {
				participantList.add(participant);
				participant.externalTua.createTUAInviteRequest(null);
			} else
				log.error("Error getting participant during addNewParticipant Method and request is  initial request");

		} catch (Exception e) {
			log.error("Cannot add new participant:", e.toString());

		}
	}
	
		
	public void removeParticipantFromConferenceList(DlgcEMBridgeParticipant participant) 
	{
		this.participantList.remove(participant);
	}
	
	public void addParticipantFromConferenceList(DlgcEMBridgeParticipant participant) 
	{
		this.participantList.add(participant);
	}
	
	
	
	public void setConfState( ConferenceState state ) {
		myConfState = state;
	}
	
	public ConferenceState getConfState()
	{
		return myConfState;
	}
	
	public DlgcEMBridgeParticipant findADestinationParticipant( DlgcEMBridgeParticipant source)
	{
		DlgcEMBridgeParticipant dest = null;
		
		Iterator< DlgcEMBridgeParticipant > parIt = participantList.iterator();
		
		DlgcEMBridgeParticipant findDest = null;
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
	
	
	
	public void bridgeLeg(DlgcEMBridgeParticipant sourceParticipant)
	{
		DlgcEMBridgeParticipant destParticipant = findADestinationParticipant( sourceParticipant );
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
	
	//In this demo test the second participant is treated as early media that is
	//here we ask the MS to generate a SDP offer.
	//Note Emulating initial no SDP; however, we do get an SDP from sip phone ...
	//save the SDP for later on.
	public void setupTUALeg()
	{
		DlgcEMBridgeParticipant tua = new DlgcEMBridgeParticipant(this, null, "TUA-PARTICIPANT");	
		participantList.add(tua);	
		try {
			//tua.connectLegNoSDP();
			tua.generateSdpOfferToMS();
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setBridgePartners()
	{
		log.debug("Entering DlgcEMBridgeConference::setBridgePartners");
		int participantCnt = participantList.size();
		if ( participantCnt == 2 ) {
			DlgcEMBridgeParticipant oua = participantList.get(0);
			DlgcEMBridgeParticipant tua = participantList.get(1);
			oua.setBridgePartner(tua);
			tua.setBridgePartner(oua);
		} else {
			log.error("DlgcEMBridgeConference::setBridgePartners(): Major error trying to setBridgePartners ; however, there are less than two legs available.");
		}
		
	}
	
	public void joinToBridge() throws MsControlException
	{
		log.debug("Entering DlgcEMBridgeConference::joinToBridge");
		int participantCnt = participantList.size();
		if ( participantCnt == 2 ) {
			DlgcEMBridgeParticipant oua = participantList.get(0);
			DlgcEMBridgeParticipant tua = participantList.get(1);
			if ( (oua==null) || tua== null ) {
			  String msg = "DlgcEMBridgeConference::joinToBridge(): Major error trying to join two call legs that have null references";
			  log.error(msg);
			  throw (new MsControlException(msg));
			}
			oua.bridgeLegs(tua,Direction.DUPLEX);
		} else {
			log.error("DlgcEMBridgeConference::joinToBridge(): Major error trying to join two call legs; however, there are less than two legs available.");
		}
	}
	
	//John need to work in release correctly in a full conference...
	//this is not working
	public void destroyActiveConference(SipSession legAskingOutSipSession)
	{
		DlgcEMBridgeParticipant participantAskingOut = (DlgcEMBridgeParticipant)legAskingOutSipSession.getAttribute("PARTICIPANT");
		DlgcEMBridgeParticipant ouaParticipant = null;
		DlgcEMBridgeParticipant tuaParticipant = null;
		
			
		if ( participantAskingOut.myName.compareToIgnoreCase("OUA-PARTICIPANT") == 0)  {
			ouaParticipant =  participantAskingOut;
			tuaParticipant = participantAskingOut.bridgePartner;
			ouaParticipant.setDestroyRequest(true);
			tuaParticipant.setDestroyRequest(false);
			
		} else {
			tuaParticipant =  participantAskingOut;
			ouaParticipant = participantAskingOut.bridgePartner;
			ouaParticipant.setDestroyRequest(false);
			tuaParticipant.setDestroyRequest(true);
		}
		
		
		
		//release the associated media session
		try {
			
						
			tuaParticipant.release();
			ouaParticipant.release();		
			
			if (tuaParticipant.mySipSession.isValid()) {
				tuaParticipant.mySipSession.invalidate();
			}
			
			
			if (ouaParticipant.mySipSession.isValid()) {
				ouaParticipant.mySipSession.invalidate();
			}
					
			if ( this.myConfMode == DlgcEMBridgeConference.ConferenceMode.MIXER_MODE ) {
				//release the mixer
				this.myMediaSession.release();
			}
			
			this.myConfState = DlgcEMBridgeConference.ConferenceState.NOT_IN_CONF;
			
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void enableDigitDetectionsOnAllLegs()
	{
		log.debug("Entering DlgcEMBridgeConference::enableDigitDetectionsOnAllLegs - note doing nothing..code has been removed");
		/*log.debug("Entering DlgcEMBridgeConference::enableDigitDetectionsOnAllLegs");
		BridgedLegs bl = getBridgedLegs();
		if ( bl == null) {
			log.error("Not enought bridge legs - leg count less than 2");
		}else {
			try {
				log.debug("Enabling OUA Leg Digit Detections");
				bl.oua.enableAsyncDtmf();
				log.debug("Enabling TUA Leg Digit Detections");
				bl.tua.enableAsyncDtmf();
			} catch (MsControlException e) {
				e.printStackTrace();
			}
		}
		*/
	}
	
	public void transferBridgeToFullConference()
	{
		log.debug("DlgcEMBridgeConference::transferBridgeToFullConference...");
		
		if ( this.myConfMode == DlgcEMBridgeConference.ConferenceMode.MIXER_MODE )
			return;
		
		BridgedLegs bl = getBridgedLegs();
		if ( bl == null) {
			log.error("Not enought bridge legs - leg count less than 2");
		}else {
			try {
				myMediaSession = DlgcEMBridgeServlet.theMediaSessionFactory.createMediaSession();
				confMediaMixer = myMediaSession.createMediaMixer(MediaMixer.AUDIO);
				myMediaSession.setAttribute("MEDIA_MIXER", confMediaMixer);
				confMediaMixer.addListener(new DlgcEMMixerJoinEventListener());
				confMediaMixer.addListener(new DlgcEMMixerAllocListener(this));
				this.myConfMode = DlgcEMBridgeConference.ConferenceMode.MIXER_MODE;
				confMediaMixer.confirm();
				
			} catch (MsControlException e1) {				
				e1.printStackTrace();
			}
		}
	}
	
	public void unjoinBridge()
	{
		log.debug("DlgcEMBridgeConference::unjoinBridge...");
		BridgedLegs bl = getBridgedLegs();
		if ( bl == null) {
			log.error("Not enought bridge legs - leg count less than 2");
		}else {
			mixerRdy = true;
			bl.oua.unjoinConference();
		}
	}
	
	class BridgedLegs implements Serializable 
	{
		private static final long serialVersionUID = 1L;
		public DlgcEMBridgeParticipant oua;
		public DlgcEMBridgeParticipant tua =null;
	}
	
	private BridgedLegs getBridgedLegs()
	{
		log.debug("Entering DlgcEMBridgeConference::getBridgeLegs");
		BridgedLegs bl = null;
		int participantCnt = participantList.size();
		if ( participantCnt == 2 ) {
			log.debug("Enabling OUA Leg Digit Detections");
			bl = new BridgedLegs();
			bl.oua = participantList.get(0);
			bl.tua = participantList.get(1);
		} else {
			log.error("DlgcEMBridgeConference::getBridgeLegs(): Major error trying to optain bridged legs however, there are less than two legs available.");
		}
		return bl;
	}
	
	public void joinFullMixerconference()
	{
		log.debug("DlgcEMBridgeConference::joinFullMixerconference..."); 
		BridgedLegs bl = getBridgedLegs();
		if ( bl == null) {
			log.error("Not enought bridge legs - leg count less than 2");
		}else {
			mixerRdy = true;
			try {
				bl.oua.setState(bl.oua.presentState, DlgcEMBridgeParticipantState.dlgcParticipantJoiningMixerConfState);
				bl.oua.nc.join(Joinable.Direction.DUPLEX, this.confMediaMixer);
				
				/***bl.tua.setState(bl.oua.presentState, DlgcEMBridgeParticipantState.dlgcParticipantJoiningMixerConfState);
				bl.tua.nc.join(Joinable.Direction.DUPLEX, this.confMediaMixer); ****/
			} catch (MsControlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}

