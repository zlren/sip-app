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

import java.io.Serializable;

import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaSession;

import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.resource.AllocationEvent;
import javax.media.mscontrol.resource.AllocationEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Use to test the SdpOfferReject case scenario...

public class DlgcEMBridgeAllocListener implements AllocationEventListener, Serializable
{

	private static final long serialVersionUID = 33800129088881L;
	protected DlgcEMBridgeParticipant 		participant;
	private static Logger log = LoggerFactory.getLogger(DlgcEMBridgeAllocListener.class);
	
	
	@Override
	public void onEvent(AllocationEvent theEvent) {
		log.debug("Entering DlgcBridgeAllocListener::onEvent");
		
		EventType joinEvType = theEvent.getEventType();
		
		log.debug("DlgcEMBridgeAllocListener::Type: " + theEvent.getEventType() );
		log.debug("DlgcEMBridgeAllocListener::Source: " + theEvent.getSource().toString());
		log.debug("DlgcEMBridgeAllocListener::ErrorText: " + theEvent.getErrorText());
		
		if ( joinEvType == AllocationEvent.ALLOCATION_CONFIRMED )
		{
			log.debug("DlgcEMBridgeAllocListener Non Control Leg Conference was created successfully" );
		} else if ( joinEvType == AllocationEvent.IRRECOVERABLE_FAILURE ){
			log.debug("Sdp Offer Rejected...releasing this media session" );
			NetworkConnection nc = (NetworkConnection) theEvent.getSource();
			MediaSession ms = nc.getMediaSession();
			ms.release();
		}
		log.debug("Leaving DlgcBridgeAllocListener::onEvent");	
		
	}
	
}
