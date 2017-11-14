
/*DIALOGIC CONFIDENTIAL
 

 Copyright 2010 Dialogic Corporation. All Rights Reserved.
 The source code contained or described herein and all documents related to
 the source code (collectively "Material") are owned by Dialogic Corporation 
 or its suppliers or licensors ("Dialogic"). 

 BY DOWNLOADING, ACCESSING, INSTALLING, OR USING THE MATERIAL YOU AGREE TO BE
 BOUND BY THE TERMS AND CONDITIONS DENOTED HERE AND ANY ADDITIONAL TERMS AND
 CONDITIONS SET FORTH IN THE MATERIAL. Title to the Material remains with 
 Dialogic. The Material contains trade secrets and proprietary and 
 confidential information of Dialogic. The Material is protected by worldwide
 Dialogic copyright(s) and applicable trade secret laws and treaty provisions.
 No part of the Material may be used, copied, reproduced, modified, published, 
 uploaded, posted, transmitted, distributed, or disclosed in any way without
 prior express written permission from Dialogic Corporation.
 
 No license under any applicable patent, copyright, trade secret or other 
 intellectual property right is granted to or conferred upon you by disclosure
 or delivery of the Material, either expressly, by implication, inducement, 
 estoppel or otherwise. Any license under any such applicable patent, 
 copyright, trade secret or other intellectual property rights must be express
 and approved by Dialogic Corporation in writing.

 You understand and acknowledge that the Material is provided on an 
 AS-IS basis, without warranty of any kind.  DIALOGIC DOES NOT WARRANT THAT 
 THE MATERIAL WILL MEET YOUR REQUIREMENTS OR THAT THE SOURCE CODE WILL RUN 
 ERROR-FREE OR UNINTERRUPTED.  DIALOGIC MAKES NO WARRANTIES, EXPRESS OR 
 IMPLIED, INCLUDING, WITHOUT LIMITATION, ANY WARRANTY OF NON-INFRINGEMENT, 
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  DIALOGIC ASSUMES NO 
 RISK OF ANY AND ALL DAMAGE OR LOSS FROM USE OR INABILITY TO USE THE MATERIAL. 
 THE ENTIRE RISK OF THE QUALITY AND PERFORMANCE OF THE MATERIAL IS WITH YOU.  
 IF YOU RECEIVE ANY WARRANTIES REGARDING THE MATERIAL, THOSE WARRANTIES DO NOT 
 ORIGINATE FROM, AND ARE NOT BINDING ON DIALOGIC.

 IN NO EVENT SHALL DIALOGIC OR ITS OFFICERS, EMPLOYEES, DIRECTORS, 
 SUBSIDIARIES, REPRESENTATIVES, AFFILIATES AND AGENTS HAVE ANY LIABILITY TO YOU 
 OR ANY OTHER THIRD PARTY, FOR ANY LOST PROFITS, LOST DATA, LOSS OF USE OR 
 COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES, OR FOR ANY INDIRECT, 
 SPECIAL OR CONSEQUENTIAL DAMAGES RELATING TO THE MATERIAL, UNDER ANY CAUSE OF
 ACTION OR THEORY OF LIABILITY, AND IRRESPECTIVE OF WHETHER DIALOGIC OR ITS 
 OFFICERS, EMPLOYEES, DIRECTORS, SUBSIDIARIES, REPRESENTATIVES, AFFILIATES AND 
 AGENTS HAVE ADVANCE NOTICE OF THE POSSIBILITY OF SUCH DAMAGES.  THESE 
 LIMITATIONS SHALL APPLY NOTWITHSTANDING THE FAILURE OF THE ESSENTIAL PURPOSE 
 OF ANY LIMITED REMEDY.  IN ANY CASE, DIALOGIC'S AND ITS OFFICERS', 
 EMPLOYEES', DIRECTORS', SUBSIDIARIES', REPRESENTATIVES', AFFILIATES' AND 
 AGENTS' ENTIRE LIABILITY RELATING TO THE MATERIAL SHALL NOT EXCEED THE 
 AMOUNTS OF THE FEES THAT YOU PAID FOR THE MATERIAL (IF ANY). THE MATERIALE 
 IS NOT FAULT-TOLERANT AND IS NOT DESIGNED, INTENDED, OR AUTHORIZED FOR USE IN 
 ANY MEDICAL, LIFE SAVING OR LIFE SUSTAINING SYSTEMS, OR FOR ANY OTHER 
 APPLICATION IN WHICH THE FAILURE OF THE MATERIAL COULD CREATE A SITUATION 
 WHERE PERSONAL INJURY OR DEATH MAY OCCUR. Should You or Your direct or 
 indirect customers use the MATERIAL for any such unintended or unauthorized 
 use, You shall indemnify and hold Dialogic and its officers, employees, 
 directors, subsidiaries, representatives, affiliates and agents harmless 
 against all claims, costs, damages, and expenses, and attorney fees and 
 expenses arising out of, directly or indirectly, any claim of product 
 liability, personal injury or death associated with such unintended or 
 unauthorized use, even if such claim alleges that Dialogic was negligent 
 regarding the design or manufacture of the part.

**********/


package testing.unit;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.signals.SignalDetector;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.resource.AllocationEvent;
import javax.media.mscontrol.resource.AllocationEventListener;
import javax.media.mscontrol.resource.ResourceEvent;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.DlgcDemoProperty;
import testing.DlgcTest;

//the dtmf pressed in a soft phone are detected on by one
//and display by the callback
//Press "0" to terminate


 public class DlgcDtmfAsyncTest extends DlgcTest 
 {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1;


	private static final String TerminateDTMFCollection = new String("0");
	//public final static String FLUSH_BUFFER_PLEASE="*";
	

	// Listener for MediaGroup events
	private MySignalDetectorListener sigDetListener;
	
	
	// The options to receiveSignals
	private Parameters collectOptions;
	static Boolean demoUseSyncApi =false;

	@Override
	public void init(ServletConfig cfg) throws ServletException
	{
		super.init(cfg);
		String prop = demoPropertyObj.getProperty("DlgcAsyncDtmfDemo.use.syncapi");
		
		if ( prop != null ) {
			if ( prop.compareTo(prop) == 0)
				demoUseSyncApi = new Boolean(true);
		}
		
		log.debug("DlgcDtmfAsyncTest::init: DlgcAsyncDtmfDemo.use.syncapi attribute set to: " + demoUseSyncApi.toString());

	}
	
	@Override
	public void servletInitialized(SipServletContextEvent evt){

		String sName = evt.getSipServlet().getServletName();


		if ( ( platform != null ) && (platform.compareToIgnoreCase(DlgcTest.TELESTAX_PLATFORM) == 0 ) || ( platform != null ) && (platform.compareToIgnoreCase(DlgcTest.ORACLE_PLATFORM) == 0 )) {
			if( sName.equalsIgnoreCase("DlgcSipServlet") )
			{
				dlgcSipServletLoaded = true;
				log.debug(" DlgcDtmfAsyncTest::servletInitialized DlgcSipServlet loaded");			
			}
			
			else if( sName.equalsIgnoreCase("DlgcDtmfAsyncTest") ) 
					playerServletInitCalled =true;

			if( playerServletInitCalled && dlgcSipServletLoaded)
			{
				if ( servletInitializedFlag == false ) {
					log.debug("Entering DlgcPlayerTest::servletInitialized servletName: " + sName);			
					servletInitializedFlag = true;
					initDriver();
					myServletInitialized(evt);
				} else {
					log.debug("DlgcDtmfAsyncTest::servletInitialized(): already servletInitialized was called...debouncing " + sName);
				}
			}

		} else { //TROPO Framework

			if( sName.equalsIgnoreCase("DlgcSipServlet") )
			{
				dlgcSipServletLoaded = true;
				log.debug(" DlgcPlayerTest::servletInitialized DlgcSipServlet loaded");			
				return;
			}

			if( (sName.equalsIgnoreCase("DlgcDtmfAsyncTest") ) && dlgcSipServletLoaded)
			{
				if ( servletInitializedFlag == false ) {
					log.debug("Entering DlgcDtmfAsyncTest::servletInitialized servletName: " + sName);			
					servletInitializedFlag = true;
					initDriver();
					myServletInitialized(evt);
				} else {
					log.debug("DlgcDtmfAsyncTest::servletInitialized(): already servletInitialized was called...debouncing " + sName);
				}
			}
		}

	}
	
	@Override
	protected void myServletInitialized(SipServletContextEvent arg0 )
	{
		try
		{
			//Setup Detector callback
			configuration = MediaGroup.PLAYER_SIGNALDETECTOR;
			sigDetListener = new MySignalDetectorListener();

			// Setup the options for receiveSignals
			collectOptions = mscFactory.createParameters();

			// Initialize 
			// Indicate that we want to do DTMF Async Continous Detection
			// note all timeouts are default to FOREEVER
			// These parameters must be set in order to trigger the forever detection

			collectOptions.put(SignalDetector.BUFFERING, Boolean.FALSE );
			EventType[] arrayEnabledEvents = {SignalDetectorEvent.SIGNAL_DETECTED} ;
			collectOptions.put(SignalDetector.ENABLED_EVENTS, arrayEnabledEvents);	
		} catch (Exception e)
		{
//			throw new ServletException(e);
			log.error("Error: "+e);
		}
	}
	
	@Override
	public void doAck(SipServletRequest req)
	{
		SipSession sipSession = req.getSession();
		runDialog( sipSession);
	}
	
	
	public void runDialog(SipSession sipSession)
	{
		
		try {
			
			MediaGroup mg = (MediaGroup)sipSession.getAttribute("MEDIAGROUP"); 
			//setup detector callback
			//mg.getSignalDetector().addListener(sigDetListener);
			//triggers DTMF Single Digit Continuous Detection 
			SignalDetector sg = mg.getSignalDetector();
			sg.receiveSignals(-1, null, null, collectOptions);

			//sipSession.setAttribute("MEDIAGROUP", mg);
			
			
		} catch (Exception e) {
			// Clean up media session
			e.printStackTrace();
			terminateSession(sipSession);
			return;
		}
	}
	
	@Override
	public void doInvite(final SipServletRequest req)
		throws ServletException, IOException
	{
		log.info("doInvite");
		
		NetworkConnection networkConnection = null;
		
		if (req.isInitial())
		{
			try 
			{
				MediaSession mediaSession = mscFactory.createMediaSession();
				networkConnection = mediaSession.createNetworkConnection(NetworkConnection.BASIC);
				MediaGroup mediaGroup = mediaSession.createMediaGroup(configuration);
				
				SipSession sipSession = req.getSession();
				sipSession.setAttribute("MEDIA_SESSION", mediaSession);
				sipSession.setAttribute("NETWORK_CONNECTION", networkConnection);
				sipSession.setAttribute("MEDIAGROUP", mediaGroup);
				
				mediaSession.setAttribute("SIP_SESSION", sipSession);
				mediaSession.setAttribute("SIP_REQUEST", req);
				mediaSession.setAttribute("NETWORK_CONNECTION", networkConnection);
				mediaSession.setAttribute("MEDIAGROUP", mediaGroup);
				
//				networkConnection.getSdpPortManager().addListener(speListener);
				
				SignalDetector sg = mediaGroup.getSignalDetector();
				sg.addListener(sigDetListener);
				
				SDAllocListener allocListener = new SDAllocListener(this);
				mediaGroup.addListener(allocListener);
				
				mediaGroup.join(Joinable.Direction.DUPLEX, networkConnection);
			}
			catch (MsControlException e)
			{
				req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
			}
		}
				
		try
		{
			req.getSession().setAttribute("UNANSWERED_INVITE", req);
			
			byte[] remoteSdp = req.getRawContent();
			
			if (remoteSdp == null)
			{
				networkConnection.getSdpPortManager().generateSdpOffer();
			}
			else
			{
				networkConnection.getSdpPortManager().processSdpOffer(remoteSdp);
			}
		} 
		catch (MsControlException e)
		{
			req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
		}
	}
	
	public void sendBye(SipSession sipSession, MediaSession mediaSession) {
		
			SipServletRequest bye = sipSession.createRequest("BYE");
			log.info("Inside terminiate method");
			try 
			{
				bye.send();
			} 
			catch (Exception e1) 
			{
				log.error("Terminating: Cannot send BYE: "+e1);
			}
		
	}
	
	

	@Override
	protected void doResponse(SipServletResponse response)
	throws IOException, ServletException
	{
		SipSession session = response.getSession();
		if (response.getRequest().getMethod().equals("BYE"))
		{
			terminateSession(session);
		}
	}
	
	
	
	private void terminateSession(SipSession session)
	{
		if (session != null)
		{
			MediaSession mediaSession = (MediaSession) session.getAttribute("MEDIA_SESSION");
			mediaSession.release();
			//session.invalidate();
			//session.getApplicationSession().invalidate();
		}
		
	}
	
	
	//This is the Detector Callback function.
	//Receives DTMF events
	class MySignalDetectorListener implements MediaEventListener<SignalDetectorEvent> , Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -3115982824192364150L;

		@Override
		public void onEvent(SignalDetectorEvent anEvent) 
		{
			log.info("ReceiveSignals terminated with: "+anEvent);
			
			//note using the JSR309 qualifier RTC_TRIGGERED not standard way of using this qualifier
			//there were not other way to map the IPMS condition using JSR 309 SPEC
			//so we decided VZ and Dialogic to use this for this purpose
			if ( (anEvent.getEventType() == SignalDetectorEvent.SIGNAL_DETECTED) && (anEvent.getQualifier() == SignalDetectorEvent.RTC_TRIGGERED) ) 
			{
				
				log.info("DTMF WAS ENABLED IN THE MEDIA SERVER");
				return;
			}
			
			String dtmf = anEvent.getSignalString();
			
			//MediaGroup mg = (MediaGroup)sipSession.getAttribute("MEDIAGROUP");
			//The above mg has been replaced with the following getContainer based on the Event getSource
			//Getting the mg from the sipSession in an Event driven system seems not to work
			//when serialization is enabled.
			//JMC August 2010 Important to understand coding style for serialization mode
			
			//This also works MediaGroup mg = (MediaGroup)ms.getAttribute("MEDIAGROUP");
			//another way to get media group
			MediaGroup mg = (MediaGroup) anEvent.getSource().getContainer();
			
			
			MediaSession ms = anEvent.getSource().getMediaSession();
			
			SipSession sipSession = (SipSession) ms.getAttribute("SIP_SESSION");		
			
			
			SignalDetector detector =null;
			try {
				detector = mg.getSignalDetector();
			} catch (MsControlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		    if (anEvent.getEventType() == SignalDetectorEvent.SIGNAL_DETECTED) {
				
				log.info("Collected: "+dtmf);
			}

			if (anEvent.getEventType() == SignalDetectorEvent.FLUSH_BUFFER_COMPLETED)
			{
				//we just received the flush buffer event completion
				//Check to see if the buffer was flushed by the IP MS System
				log.debug("Got flush buffer event");
				//promptCollectDigits(sipSession,ms,mg);	
			} else if (anEvent.getQualifier() == ResourceEvent.STOPPED)
			{
				log.debug("Terminating DtmfAsyncCollectionServlet due to a Stop Request");
				//Stop digit collection and play default prompt to alert the user
				//to start entering DTMF
				//playPrompt( sipSession, ms, mg);
				log.debug("Disconnect from softphone...getting ready to terminate.");
				sendBye(sipSession, ms); 
				
			} else if ( dtmf.equals( DlgcDtmfAsyncTest.TerminateDTMFCollection))
			{	
				//try
				//{
					//Digit zero was pressed indicates to stop detector from
					//collecting
					//SignalDetector detector = mg.getSignalDetector();
					detector.stop();
					
					if ( demoUseSyncApi ) {
						log.debug("DlgcDtmfAsyncTest:: releasing demo using synchronous stop");
						SipSession session = (SipSession) ms.getAttribute("SIP_SESSION");
						
						terminateSession(session);
						sendBye(session, ms);
					}
			}
			
			/***********not supported for this release
			else if ( dtmf.equals( DlgcDtmfAsyncTest.FLUSH_BUFFER_PLEASE ))
			{
				try
				{
					SignalDetector detector = mg.getSignalDetector();
					detector.flushBuffer();
				} catch (MsControlException ex)
				{
					log.error(ex);
				}
			}
			**************/
			
		}
	}		
	
	
	public class SDAllocListener implements AllocationEventListener, Serializable
	{

		private static final long serialVersionUID = 1;
		
		
		protected DlgcDtmfAsyncTest myParent = null;
		
		public SDAllocListener( DlgcDtmfAsyncTest parent) {
			myParent = parent;
		}
		
		@Override
		public void onEvent(AllocationEvent theEvent) {
			DlgcDtmfAsyncTest.log.debug("Entering SDAllocListener::onEvent");
			
			EventType joinEvType = theEvent.getEventType();
			
			DlgcDtmfAsyncTest.log.debug("SDAllocListener::EventType: " + theEvent.getEventType() );
			DlgcDtmfAsyncTest.log.debug("SDAllocListener::Source: " + theEvent.getSource().toString());
			DlgcDtmfAsyncTest.log.debug("DlgcReferenceConferenceAllocListener::ErrorText: " + theEvent.getErrorText());
			
			
			if ( joinEvType == AllocationEvent.ALLOCATION_CONFIRMED )
			{
				log.debug("SDAllocListener Signal Detector allocated" );
			} else {
				log.error("SDAllocListener Signal Detector error allocating detector" );
				
				MediaGroup mg = (MediaGroup)theEvent.getSource();
				MediaSession ms = mg.getMediaSession();
				SipSession session = (SipSession) ms.getAttribute("SIP_SESSION");
				myParent.sendBye(session, ms);
				myParent.terminateSession(session);
			}
				
			
		}
		
	}

	
	
	
	protected boolean compareState(SipSession sipSession, String state)
	{
		return state.equals((String) sipSession.getAttribute("STATE"));
	}

	protected void setState(SipSession sipSession, String state)
	{
		//log.info(new StringBuilder().append("Setting state to: ").append(state).toString());
		sipSession.setAttribute("STATE", state);
	}
	
	protected Configuration<MediaGroup> configuration;
	protected URI						prompt;
	
	
	//setup Logger
	private static Logger log = LoggerFactory.getLogger(DlgcDtmfAsyncTest.class);
}


