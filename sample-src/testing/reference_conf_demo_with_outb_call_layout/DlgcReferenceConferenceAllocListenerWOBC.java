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

import java.io.Serializable;

import javax.media.mscontrol.EventType;
import javax.media.mscontrol.resource.AllocationEvent;
import javax.media.mscontrol.resource.AllocationEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DlgcReferenceConferenceAllocListenerWOBC implements AllocationEventListener, Serializable
{

	private static final long serialVersionUID = 1;
	protected DlgcReferenceConferenceParticipantWOBC 		participant;
	private static Logger log = LoggerFactory.getLogger(DlgcReferenceConferenceAllocListenerWOBC.class);
	
	
	@Override
	public void onEvent(AllocationEvent theEvent) {
		log.debug("Entering DlgcReferenceConferenceAllocListener::onEvent");
		
		EventType joinEvType = theEvent.getEventType();
		
		log.debug("DlgcReferenceConferenceAllocListener::Source" + theEvent.getEventType() );
		log.debug("DlgcReferenceConferenceAllocListener::Source" + theEvent.getSource().toString());
		log.debug("DlgcReferenceConferenceAllocListener::ErrorText" + theEvent.getErrorText());
		
		
		if ( joinEvType == AllocationEvent.ALLOCATION_CONFIRMED )
		{
			log.debug("DlgcReferenceConferenceAllocListener Non Control Leg Conference was created successfully" );
		} else {
			log.debug("DlgcReferenceConferenceAllocListener Non Control Leg Conference not created...error" );
		}
			
		
	}
	
}
