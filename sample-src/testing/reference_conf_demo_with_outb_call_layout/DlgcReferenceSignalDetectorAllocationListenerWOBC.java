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
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.JoinableContainer;
import javax.media.mscontrol.resource.AllocationEvent;
import javax.media.mscontrol.resource.AllocationEventListener;
import javax.servlet.sip.SipApplicationSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.reference_conf_demo_with_outb_call_layout.DlgcOutbCallConferenceStorage.DlgcConferenceStorageMgrException;


public class DlgcReferenceSignalDetectorAllocationListenerWOBC implements AllocationEventListener ,Serializable 
{


	private static final long serialVersionUID = 1L;
	transient protected  		DlgcReferenceConferenceParticipantWOBC participant;
	transient String participantId = null;
	private static Logger log = LoggerFactory.getLogger(DlgcReferenceSignalDetectorAllocationListenerWOBC.class);
	
	public DlgcReferenceSignalDetectorAllocationListenerWOBC(DlgcReferenceConferenceParticipantWOBC p)
	{
		participant = p;
		if ( p != null )
			participantId = participant.clientSSID;
	}
	
	@Override
	public void onEvent(AllocationEvent theEvent) {
		log.debug("Entering DlgcSignalDetectorAllocationListener::onEvent");
		
		//EventType joinEvType = theEvent.getEventType();
		
		log.debug("DlgcSignalDetectorAllocationListener::Source" + theEvent.getEventType() );
		log.debug("DlgcSignalDetectorAllocationListener::Source" + theEvent.getSource().toString());
		log.debug("DlgcSignalDetectorAllocationListener::ErrorText" + theEvent.getErrorText());
		
		JoinableContainer container = (JoinableContainer)theEvent.getSource();
		MediaSession ms = container.getMediaSession();
		DlgcOutbCallConferenceStorage conferenceStorage = (DlgcOutbCallConferenceStorage) ms.getAttribute("DlgcOutbCallConferenceStorage");
		DlgcReferenceConferenceWOBC conf;
		try {
			conf = conferenceStorage.getConferenceInfo();
			participantId = (String) ms.getAttribute("ParticipantId");
			participant = conf.findParticipant(participantId);
		} catch (DlgcConferenceStorageMgrException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		try {
			SipApplicationSession sas = DlgcOutbCallConferenceStorage.loadSas();
			//this.executeAsync(sas.getId(), participant, theEvent );
			this.execute(sas.getId(), participant, theEvent );

		} catch (DlgcConferenceStorageMgrException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MsControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/*******
	protected void executeAsync(final String sasId, final DlgcReferenceConferenceParticipantWOBC participant, final AllocationEvent theEvent ) throws MsControlException 
	{
		log.debug("DlgcReferenceSignalDetectorAllocationListenerWOBC::Entering executeAsync"); 
		
		if ( sasId == null ) {
			log.error("DlgcReferenceSignalDetectorAllocationListenerWOBC::executeAsync cant execute sasId is NULL..returning immediately.");
		}
	    try {
	    	final SipSessionsUtilAdapter occas5SessionUtil = (SipSessionsUtilAdapter) DlgcReferenceConferenceWithOutBCallServlet.getSSU();
			//final String sasId = this.getProxyId();
	    	final EventType joinEvType = theEvent.getEventType();
	    	
			((WlssSipSessionsUtil)occas5SessionUtil).doAsynchronousAction(sasId, new WlssAsynchronousAction() 
			{
				private static final long serialVersionUID = 1L;

				public void run(SipApplicationSession appSession) {
					if ( joinEvType == AllocationEvent.ALLOCATION_CONFIRMED )
					{
						log.debug("DlgcReferenceSignalDetectorAllocationListenerWOBC Signal Generator was created successfully" );
					} else {
						log.debug("DlgcReferenceSignalDetectorAllocationListenerWOBC Signal Generator not created...error" );
					}
						
					try {
						participant.getPresentState().enablingAsyncDtmfResponse( participant );
						
						try {
							participant.myConference.myControlServlet.conferenceStorage.saveConference(participant.myConference);
						} catch (DlgcConferenceStorageMgrException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					} catch (MsControlException e) {
						e.printStackTrace();
					
					}
				}
			}); 
	    } catch (Exception e) {
		  
	      //throw new MsControlException(appLogId+"Failed to get player. play failed", e);
	    }
		log.debug("DlgcReferenceSignalDetectorAllocationListenerWOBC::Leaving executeAsync"); 

		
	}
	***/
	
	protected void execute(final String sasId, final DlgcReferenceConferenceParticipantWOBC participant, final AllocationEvent theEvent ) throws MsControlException 
	{
		log.debug("DlgcReferenceSignalDetectorAllocationListenerWOBC::Entering execute the non Async way"); 

		if ( sasId == null ) {
			log.error("DlgcReferenceSignalDetectorAllocationListenerWOBC::executeAsync cant execute sasId is NULL..returning immediately.");
		}
		try {
			//final SipSessionsUtilAdapter occas5SessionUtil = (SipSessionsUtilAdapter) DlgcReferenceConferenceWithOutBCallServlet.getSSU();
			//final String sasId = this.getProxyId();
			final EventType joinEvType = theEvent.getEventType();


			if ( joinEvType == AllocationEvent.ALLOCATION_CONFIRMED )
			{
				log.debug("DlgcReferenceSignalDetectorAllocationListenerWOBC Signal Generator was created successfully" );
			} else {
				log.debug("DlgcReferenceSignalDetectorAllocationListenerWOBC Signal Generator not created...error" );
			}

			try {
				participant.getPresentState().enablingAsyncDtmfResponse( participant );

				try {
					participant.myConference.myControlServlet.conferenceStorage.saveConference(participant.myConference);
				} catch (DlgcConferenceStorageMgrException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (MsControlException e) {
				e.printStackTrace();

			}

	} catch (Exception e) {

		//throw new MsControlException(appLogId+"Failed to get player. play failed", e);
	}
	log.debug("DlgcReferenceSignalDetectorAllocationListenerWOBC::Leaving execute he non Async way"); 


}
	
}

