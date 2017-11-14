/*
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
import javax.media.mscontrol.join.JoinEvent;
import javax.media.mscontrol.join.JoinEventListener;
import javax.media.mscontrol.resource.AllocationEvent;
import javax.media.mscontrol.resource.AllocationEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DlgcEMMixerAllocListener implements AllocationEventListener , Serializable
{
	private static final long serialVersionUID = 77800129088881L;
	DlgcEMBridgeConference   conf = null;
	private static Logger log = LoggerFactory.getLogger(DlgcEMMixerAllocListener.class);
	
	public DlgcEMMixerAllocListener( DlgcEMBridgeConference conference)
	{
		conf = conference;
	}
	
	@Override
	public void onEvent(AllocationEvent theEvent) {
		
		log.debug("Entering DlgcEMMixerAllocListener::onEvent");
		
		
		log.debug("DlgcEMMixerAllocListener::EventType" + theEvent.getEventType() );
		log.debug("DlgcEMMixerAllocListener::Source" + theEvent.getSource().toString());
		log.debug("DlgcEMMixerAllocListener::ErrorText" + theEvent.getErrorText());
		
		if ( theEvent.getEventType() == AllocationEvent.ALLOCATION_CONFIRMED )
		{
			log.debug("DlgcEMMixerAllocListener Mixer Conference was created successfully" );
			conf.unjoinBridge();
		} else {
			log.debug("DlgcEMMixerAllocListener Mixer  Conference not created...error" );
		}
		
	}

}






