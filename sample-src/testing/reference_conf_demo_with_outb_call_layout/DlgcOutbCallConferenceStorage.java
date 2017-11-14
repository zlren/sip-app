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



import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipSessionsUtil;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DlgcOutbCallConferenceStorage implements Serializable
{

	private static final long serialVersionUID = 1L;
	private static final String SAS_CONFERENCE_ID ="DialogicConferenceOutBoundCallDemo";
	private static final String SAS_CONFERENCE_MAP_ID ="CONFERENCE_MAP";
	
	private static Logger log = LoggerFactory.getLogger(DlgcOutbCallConferenceStorage.class);
	
	public enum DlgcConferenceStorageMgrExceptionTypes
	{
		NULL_APP_SESSION_FOUND, EMPTY_MAP_FOUND
	}
	
	class DlgcConferenceStorageMgrException extends Exception
	{
		
	
		private static final long serialVersionUID = 1L;


		public DlgcConferenceStorageMgrException(DlgcConferenceStorageMgrExceptionTypes t, String message) {
			super(message);
			exType = t;
		}
		
		public DlgcConferenceStorageMgrExceptionTypes getType() {
			return exType;
		}
		
		
		protected DlgcConferenceStorageMgrExceptionTypes  exType;
	}
	
	
	
	public  DlgcOutbCallConferenceStorage(DlgcReferenceConferenceWithOutBCallServlet servletOwner)
	{
		SipSessionsUtil ssu = DlgcReferenceConferenceWithOutBCallServlet.getSSU();
		SipApplicationSession sas = null;
		sas = ssu.getApplicationSessionByKey(SAS_CONFERENCE_ID, true);
		if (sas.getExpirationTime() != 0) {
			sas.setExpires(0);
		}
	}
	
	static public SipApplicationSession loadSas() throws DlgcConferenceStorageMgrException 
	{
		try {
			SipSessionsUtil ssu = DlgcReferenceConferenceWithOutBCallServlet.getSSU();
			SipApplicationSession sas = null;
			sas = ssu.getApplicationSessionByKey(SAS_CONFERENCE_ID, false);
			return sas;
		} catch ( NullPointerException nullEx) {
			log.debug("loadSas() Could Nof find ApplicationSession Key call to sessionUtil returns null- Ignoring");
			return null;
		}
		
	}
	
	synchronized public  void saveConference( DlgcReferenceConferenceWOBC conference ) throws DlgcConferenceStorageMgrException
	{
		
		//Override conference info
		SipApplicationSession sas = loadSas();
		if ( sas != null ) {
			if ( conference != null )
				sas.setAttribute(SAS_CONFERENCE_MAP_ID, conference);
		}
		
	}
	

	synchronized public DlgcReferenceConferenceWOBC getConferenceInfo() throws DlgcConferenceStorageMgrException
	{
		SipApplicationSession sas = loadSas();
		if ( sas != null ) {
			DlgcReferenceConferenceWOBC confInfo = (DlgcReferenceConferenceWOBC)sas.getAttribute(SAS_CONFERENCE_MAP_ID);
			return confInfo;
		} else {
			return null;
		}
	} 
	
	
	synchronized public void removeConference() throws DlgcConferenceStorageMgrException
	{
		SipApplicationSession sas = loadSas();
		if ( sas != null ) {
			DlgcReferenceConferenceWOBC confInfo = (DlgcReferenceConferenceWOBC)sas.getAttribute(SAS_CONFERENCE_MAP_ID);
			if ( confInfo != null ) {
				sas.removeAttribute(SAS_CONFERENCE_MAP_ID);
			}
		}
	} 

}
	


