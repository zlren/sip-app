/**
 * DIALOGIC CONFIDENTIAL      
 * Copyright (C) 2005-2014 Dialogic Corporation. All Rights Reserved.
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
 

package testing.jmc_conference;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.media.mscontrol.mixer.MediaMixer;
import javax.servlet.sip.SipApplicationSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Note this class must be stateless in order for the application
//to work in a cluster environment
public class DlgcConferenceStorageMgr implements Serializable
{
	private static final String SAS_CONFERENCE_ID ="DialogicConference";
	private static final String SAS_CONFERENCE_MAP_ID ="CONFERENCE_MAP";
	private JMCConferenceServlet jcs = null;
	
	private static Logger log = LoggerFactory.getLogger(DlgcConferenceStorageMgr.class);
	
	public enum DlgcConferenceStorageMgrExceptionTypes
	{
		NULL_APP_SESSION_FOUND, EMPTY_MAP_FOUND
	}
	
	class DlgcConferenceStorageMgrException extends Exception
	{
		
		public DlgcConferenceStorageMgrException(DlgcConferenceStorageMgrExceptionTypes t, String message) {
			super(message);
			exType = t;
		}
		
		public DlgcConferenceStorageMgrExceptionTypes getType() {
			return exType;
		}
		
		
		protected DlgcConferenceStorageMgrExceptionTypes  exType;
	}
	
	class DlgcConferenceInfo implements Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 748493287391L;
		public DlgcConferenceInfo(URI confUri, MediaMixer conf)
		{
			conference = conf;
			confMsUri = confUri;
		}
		
		public MediaMixer getConference() {
			return conference;
		}
		
		public URI getURI() {
			return confMsUri;
		}
		
		private MediaMixer conference;
		private URI confMsUri;
	}
	
	public  DlgcConferenceStorageMgr(JMCConferenceServlet jcs)
	{
		this.jcs = jcs;
	}
	
	synchronized public SipApplicationSession loadSas() throws DlgcConferenceStorageMgrException 
	{
		try {
			SipApplicationSession sas = jcs.sipSessionsUtil.getApplicationSessionByKey(SAS_CONFERENCE_ID, true);
			if (sas.getExpirationTime() != 0) {
				sas.setExpires(0);
			}
			return sas;
		} catch ( NullPointerException nullEx) {
			throw new DlgcConferenceStorageMgrException(DlgcConferenceStorageMgrExceptionTypes.NULL_APP_SESSION_FOUND, 
													    "loadSas() Exception: Could Nof find ApplicationSession Key call to sessionUtil returns null.");
		}
		
	}
	

	@SuppressWarnings("unchecked")
	synchronized public  void saveConference( URI msURI, String pin, MediaMixer conference ) throws DlgcConferenceStorageMgrException
	{
		
		SipApplicationSession sas = loadSas();
		Map<String, DlgcConferenceInfo> confMap = ( Map<String,DlgcConferenceInfo>)sas.getAttribute(SAS_CONFERENCE_MAP_ID);
		if (confMap == null ) {
			//create it only once
			confMap = new HashMap<String, DlgcConferenceInfo>();
		}
		DlgcConferenceInfo confInfo = new DlgcConferenceInfo(msURI,conference);
		confMap.put(pin, confInfo);
		sas.setAttribute(SAS_CONFERENCE_MAP_ID, confMap);
		
	}
	
	@SuppressWarnings("unchecked")
	synchronized public DlgcConferenceInfo getConferenceInfo(String pin) throws DlgcConferenceStorageMgrException
	{
		
		SipApplicationSession sas = loadSas();
		DlgcConferenceInfo confInfo = null;
		
		Map<String, DlgcConferenceInfo> confMap = ( Map<String,DlgcConferenceInfo>)sas.getAttribute(SAS_CONFERENCE_MAP_ID);
		
		
		if (confMap == null ) {
			throw new DlgcConferenceStorageMgrException(DlgcConferenceStorageMgrExceptionTypes.EMPTY_MAP_FOUND,
														"getConferenceInfo Exception: conference Map returned null...");
		} else {
			confInfo = confMap.get(pin);
			return confInfo;
		}	
	} 
	
	
	
	@SuppressWarnings("unchecked")
	synchronized public void removeConference(String pin) throws DlgcConferenceStorageMgrException
	{
		SipApplicationSession sas = loadSas();
		Map<String, DlgcConferenceInfo> confMap = ( Map<String,DlgcConferenceInfo>)sas.getAttribute(SAS_CONFERENCE_MAP_ID);
		if (confMap == null ) {
			throw new DlgcConferenceStorageMgrException(DlgcConferenceStorageMgrExceptionTypes.EMPTY_MAP_FOUND,
														"getConferenceInfo Exception: conference Map returned null...");
		} else {
			DlgcConferenceInfo confInfo = confMap.get(pin);
			if ( confInfo != null ) {
					confMap.remove(pin);
					sas.setAttribute(SAS_CONFERENCE_MAP_ID, confMap);
				}
			}
	} 

}
	
