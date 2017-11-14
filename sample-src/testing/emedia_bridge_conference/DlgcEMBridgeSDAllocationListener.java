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
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.JoinEvent;
import javax.media.mscontrol.join.JoinEventListener;
import javax.media.mscontrol.resource.AllocationEvent;
import javax.media.mscontrol.resource.AllocationEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DlgcEMBridgeSDAllocationListener implements AllocationEventListener , Serializable 
{


	private static final long serialVersionUID = 3231029800128881L;
	protected DlgcEMBridgeParticipant 		participant;
	private static Logger log = LoggerFactory.getLogger(DlgcEMBridgeAllocListener.class);
	
	public DlgcEMBridgeSDAllocationListener(DlgcEMBridgeParticipant p)
	{
		participant = p;
	}
	
	@Override
	public void onEvent(AllocationEvent theEvent) {
		log.debug("Entering DlgcEMBridgeSDAllocationListener::onEvent");
		
		EventType joinEvType = theEvent.getEventType();
		
		log.debug("DlgcEMBridgeSDAllocationListener::Source" + theEvent.getEventType() );
		log.debug("DlgcEMBridgeSDAllocationListener::Source" + theEvent.getSource().toString());
		log.debug("DlgcEMBridgeSDAllocationListener::ErrorText" + theEvent.getErrorText());
		//MediaSession myMediaSession = ((NetworkConnection)(theEvent.getSource())).getMediaSession();
		//SipSession mySipSession = (SipSession) myMediaSession.getAttribute("SIP_SESSION");
		
		if ( joinEvType == AllocationEvent.ALLOCATION_CONFIRMED )
		{
			log.debug("DlgcEMBridgeSDAllocationListener Signal Generator was created successfully" );
		} else {
			log.debug("DlgcEMBridgeSDAllocationListener Signal Generator not created...error" );
		}
			
		try {
			participant.getPresentState().enablingAsyncDtmfResponse( participant );
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}

