
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

import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaConfigException;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.MsControlFactory;
import javax.media.mscontrol.join.Joinable.Direction;
import javax.media.mscontrol.mediagroup.MediaGroup;

import javax.media.mscontrol.mediagroup.signals.SignalDetector;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.mediagroup.signals.SignalGenerator;
import javax.media.mscontrol.mediagroup.signals.SignalGeneratorEvent;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.spi.Driver;
import javax.media.mscontrol.spi.DriverManager;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.Parameters;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/********************************
 * 
 * 
 * @author jmcruz2
 * Demo test  purpose :  To test the Signal Generation API and implementation.
 * How this demo works:  Two Soft phones are required. Only two connections are allowed by this application.
 * 						 The first soft phone that connects to the application represents the Signal Detector Network Connection...i.e. detects DTMF
 * 						 The second soft phone that connects receives the Generate DTMF Signals.
 * 						 If only one soft phone is used while trying to generate DTMF Signals, the system will log this error. 
 * 						 Once you have connected both soft phones, the user can press digits on the soft phone that represents the signal detector...always the first to connect.
 * 						 These digits are internally buffered and are used during the signal generation. That is; any signal that is detected are later on generated.
 * 						 To trigger the DTMF signal generation of the internal DTMF collect buffer, press the "#" DTMF digit to trigger the Signal generation.
 * 						 Those digits previously pressed are generated and you should be able to hear the tones on the second soft phone.
 * 						  
 * 						 After the tones are generated, the internal DTMF buffer is cleared. You can start the test cycle again.
 * 						 Note that the DTMF digit "*" is used by the application to disconnect both soft phone. Once the application disconnects both soft phones,
 * 						 the user can run the test again by starting all the way from the top.
 * 
 * Notes:				 In addition, this test illustrate how to write a Async Continuous DTMF detection.
 *
 */


public class DlgcSigGenTest extends SipServlet implements Serializable
{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3595585876135606540L;

	protected static String DialogicDriverName = "com.dialogic.dlg309";
	
	public final static String WAITING_FOR_MEDIA_SERVER = "WAITING_FOR_MEDIA_SERVER";
	public final static String WAITING_FOR_ACK = "WAITING_FOR_ACK";
	public final static String WAITING_FOR_MEDIA_SERVER_2 = "WAITING_FOR_MEDIA_SERVER_2"; // (only if the initial INVITE had no SDP offer)
	public final static String DIALOG = "DIALOG";
	public final static String BYE_SENT = "BYE_SENT";
	
	public final static String SIG_DETECTOR="SIG_DETECTOR";
	public final static String SIG_GEN="SIG_GEN"; 
	
	private final static int NC_DETECTOR=1;
	private final static int NC_GENERATOR=2;
	private final static int NC_OUT_OF_BOUND=3;
	
	protected MySignalGeneratorListener sigGenListener;
	protected MySignalDetectorListener  sigDectListener;
	protected SignalGenerator			sigGenObj;
	
	protected int ncCounter;
	protected MsControlFactory mscFactory;
	protected Configuration<MediaGroup> mediaGroupConfig;
	
	private Parameters collectOptions;
	private static String dtmfBuffer;
	private MediaSession mediaSession;

	@Override
	public void init(ServletConfig cfg) throws ServletException
	{
		super.init(cfg);	
		try
		{
			Driver dialogicDriver = DriverManager.getDriver(DialogicDriverName);
			mscFactory = dialogicDriver.getFactory(null) ;
			
			// This sets up the path that will be used internally in our jsr309 implementation to 
			// load dynamically classes.
			//mscFactory.setPathName("com.vendor.dlgimpl");
			
			//load property from file. 
			//Properties prop = new MyPropertyUtility().Load(log); 
            
			// create the Media Session Factory
			//mediaSessionFactory = mscFactory.createMediaSessionFactory(prop);
			
			dtmfBuffer = new String();
			
			// Setup the options for receiveSignals
			collectOptions = mscFactory.createParameters();
			
			//Initialize 
			//Indicate that we want to do DTMF Async Continous Detection
			//note all timeouts are default to FOREEVER
			//These parameters must be set in order to trigger the forever detection
			collectOptions.put(SignalDetector.BUFFERING, Boolean.FALSE );
			EventType[] arrayEnabledEvents = {SignalDetectorEvent.SIGNAL_DETECTED};
			collectOptions.put(SignalDetector.ENABLED_EVENTS, arrayEnabledEvents);	
		}
		catch (Exception e)
		{
			throw new ServletException(e);
		}
		
		//mediaGroupConfig = MediaGroup.PLAYER_RECORDER_SIGNALDETECTOR;
		mediaGroupConfig = MediaGroup.PLAYER_RECORDER_SIGNALDETECTOR_SIGNALGENERATOR;
		ncCounter = NC_DETECTOR;
		log.info("SignalGenerationServlet initialized...");
	}
	
	@Override
    public void doInvite(final SipServletRequest req)
		throws ServletException,IOException
	{
		SipSession sipSession = req.getSession();
		NetworkConnection conn=null;
		if (req.isInitial())
		{
			// New Call
			try
			{
				// Create new media session and store in SipSession
				MyNetworkConnectionListener ncListener = null;
				
				if (ncCounter == NC_DETECTOR)
				{
					ncCounter=NC_GENERATOR;
					mediaSession = mscFactory.createMediaSession();
					mediaSession.setAttribute("SIP_REQUEST", req);
					sipSession.setAttribute("MEDIA_SESSION", mediaSession);
					mediaSession.setAttribute("SIP_SESSION", sipSession);
					
					conn = mediaSession.createNetworkConnection(NetworkConnection.BASIC);
					ncListener = new MyNetworkConnectionListener();
					sipSession.setAttribute("CONN_TYPE", SIG_DETECTOR);
					conn.getSdpPortManager().addListener(ncListener);
					sipSession.setAttribute("NETWORK_CONNECTION", conn);
					MediaGroup mg = (MediaGroup) mediaSession.createMediaGroup(mediaGroupConfig);
					sipSession.setAttribute("MEDIAGROUP", mg);
					doJoin(sipSession, mg, (NetworkConnection)sipSession.getAttribute("NETWORK_CONNECTION"));
				}
				else if (ncCounter == NC_GENERATOR)
				{
					ncCounter = NC_OUT_OF_BOUND;
					mediaSession.setAttribute("SIP_SESSION_SIG_GEN", sipSession);
					mediaSession.setAttribute("SIP_REQUEST_SIG_GEN", req);
					sipSession.setAttribute("MEDIA_SESSION", mediaSession);
					
					conn = mediaSession.createNetworkConnection(NetworkConnection.BASIC);
					ncListener = new MyNetworkConnectionListener();
					sipSession.setAttribute("CONN_TYPE", SIG_GEN);
					conn.getSdpPortManager().addListener(ncListener);
					sipSession.setAttribute("NETWORK_CONNECTION", conn);
					MediaGroup mg = (MediaGroup) mediaSession.createMediaGroup(mediaGroupConfig);
					sipSession.setAttribute("MEDIAGROUP", mg);
					mediaSession.setAttribute("MEDIA_GROUP_SIG_GEN", mg);
					doJoin(sipSession, mg, (NetworkConnection)sipSession.getAttribute("NETWORK_CONNECTION"));
				}
				else
				{
					//don't allow
					log.error("Only two Network Connection allowed in this demo.");
					req.createResponse(SipServletResponse.SC_DECLINE).send();
				}req.getSession().setAttribute("UNANSWERED_INVITE", req);

				// set state
				setState(req.getSession(), WAITING_FOR_MEDIA_SERVER);
			}
			
			catch (MediaConfigException e)
			{
				req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR).send();
				return;
			}
			catch (MsControlException e)
			{
				// Probably out of resources, or other media server problem.  send 503
				req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
				return;
			}
		} 
		else
		{
			// Existing call.  This is an re-INVITE
			// Get NetworkConnection from SipSession
			
			conn = (NetworkConnection) sipSession.getAttribute("NETWORK_CONNECTION");
		}
		doModify(req, conn);
	}
	
	@Override
	protected void doAck(SipServletRequest req)
		throws ServletException, IOException
	{
		SipSession sipSession = req.getSession();
		// Get NetworkConnection from SipSession
		NetworkConnection conn = (NetworkConnection) sipSession.getAttribute("NETWORK_CONNECTION");
		
		String connType = (String)sipSession.getAttribute("CONN_TYPE");
		try
		{
			byte[] remoteSdp = req.getRawContent();
			if (remoteSdp != null)
			{
				conn.getSdpPortManager().processSdpAnswer(remoteSdp);
				setState(sipSession, WAITING_FOR_MEDIA_SERVER_2);
			}
		}
		catch (MsControlException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}		

		

		if ( compareState(sipSession, WAITING_FOR_ACK))
		{
			if (connType.equals(SIG_DETECTOR))
			{
				runDetectorDialog(sipSession);
			}
			else if (connType.equals(SIG_GEN))
			{
				setState(sipSession, DIALOG);
				MediaGroup mg = (MediaGroup)sipSession.getAttribute( "MEDIAGROUP");
				try
				{
					sigGenObj = (SignalGenerator) mg.getSignalGenerator();
				} 
				catch (Exception ex)
				{
					log.error("Can't get signal generation object: " + ex ); 
				}
			} 
			else
			{
				log.error("Invalid Connection Type terminating session");
				terminate(sipSession, mediaSession);
			}
		} 
	}

	@Override
    public void doBye(final SipServletRequest req)
		throws ServletException,IOException
	{
		
	}
	
	protected void doResponse(SipServletResponse resp) throws IOException, ServletException
	{
		SipSession sipSession= resp.getSession();
		
		if (compareState(sipSession, BYE_SENT))
		{	
			releaseSession(sipSession);  
		}
	}
	
	 class MySignalGeneratorListener implements MediaEventListener<SignalGeneratorEvent>, Serializable
	 {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4318307716230676561L;

		@Override
		public void onEvent(SignalGeneratorEvent mediaevent)
		{
			// TODO Auto-generated method stub
			log.debug("got MySignalGeneratorListener event");
			dtmfBuffer = new String(); 	//clear string	
		}
	}		
	
	 class MySignalDetectorListener implements MediaEventListener<SignalDetectorEvent>, Serializable
	 {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1020947315613047403L;

		@Override
		public void onEvent(SignalDetectorEvent mediaevent) 
		{
			log.info("ReceiveSignals terminated with: "+mediaevent);
			// In this example, the collected DTMFs are just logged.
			MediaSession ms = mediaevent.getSource().getMediaSession();
			SipSession sipSession = (SipSession) ms.getAttribute("SIP_SESSION");		
			//MediaGroup mg = (MediaGroup)sipSession.getAttribute("MEDIAGROUP"); 
			
			String dtmf = mediaevent.getSignalString();
			log.info("Collected: "+dtmf);
			
			if (dtmf.equals("*"))
			{
				//user request to terminate this will hang both sessions.
				terminate(sipSession, ms);
			} 
			else if (dtmf.equals("#"))
			{
				if (dtmfBuffer.length() == 0)
				{
					log.error("Bummer -Can't generate DTMF signals since singal buffer is empty. Ignoring signal generation command.");
				}
				else
				{
					//send the string buffer to signal generator
					runDtmfGenDialog(sipSession, dtmfBuffer);
					dtmfBuffer = new String();
				}
			} 
			else
			{
				//add new entered digit to buffer.
				dtmfBuffer += dtmf;
			}
		}
	}		
	
	protected void doJoin(SipSession sipSession, MediaGroup mg, NetworkConnection nc)
		throws MsControlException
	{
		mg.join(Direction.DUPLEX, nc);
	}
	
	protected void doModify(SipServletRequest req, NetworkConnection conn) throws IOException 
	{
		
		try
		{
			byte[] remoteSdp = req.getRawContent();
			req.getSession().setAttribute("UNANSWERED_INVITE", req);

			// set state
			setState(req.getSession(), WAITING_FOR_MEDIA_SERVER);
			
			if (remoteSdp == null)
			{
				conn.getSdpPortManager().generateSdpOffer();
			}
			else
			{
				conn.getSdpPortManager().processSdpOffer(remoteSdp);
			}
		} 
		catch (MsControlException e)
		{
			req.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
			return;
		}
		
		
	}
	
	protected void setState(SipSession sipSession, String state)
	{
		
		sipSession.setAttribute("STATE", state);
	}
	
	protected boolean compareState(SipSession sipSession, String state)
	{
		return state.equals((String) sipSession.getAttribute("STATE"));
	}
	
	protected void runDetectorDialog(SipSession sipSession) 
	{	
		try 
		{	
			//MediaSession ms = (MediaSession)sipSession.getAttribute("MEDIA_SESSION");
			MediaGroup mg = (MediaGroup)sipSession.getAttribute("MEDIAGROUP");
			//setup detector callback
			MySignalDetectorListener sigDetListener = new MySignalDetectorListener();
			mg.getSignalDetector().addListener(sigDetListener);
			//triggers DTMF Single Digit Continuous Detection 
			mg.getSignalDetector().receiveSignals(-1, null, null, collectOptions);
			setState(sipSession, DIALOG);
		} 
		catch (Exception e)
		{
			// Clean up media session
			MediaSession mediaSession = (MediaSession)sipSession.getAttribute("MEDIA_SESSION");
			log.info("Inside runDialog some exeption in the play calling terminate.");
			terminate(sipSession, mediaSession);
			return;
		}
	}
	
	protected void runDtmfGenDialog(SipSession sipSession, String dtmfBuffer) 
	{
		try
		{
			if (this.compareState(sipSession, DIALOG))
			{
				//we have received the ack so we can generate signal	
				if (sigGenObj == null)
				{
					//if you got here is because only one soft phone was connected.
					log.error("ignoring generating dtmf buffer - Need to run another call (Network Connection) to received DTMF- Terminating Call.");
					terminate(sipSession, mediaSession);
				} 
				else 
				{
					MediaGroup mg = (MediaGroup)mediaSession.getAttribute("MEDIA_GROUP_SIG_GEN");
					MySignalGeneratorListener sigGenListener = new MySignalGeneratorListener();
					mg.getSignalGenerator().addListener(sigGenListener);
					Parameters params = mscFactory.createParameters();
					params.put(SignalGenerator.SIGNAL_LENGTH, 200);
					log.error("Sending dtmfBuffer = " + dtmfBuffer);
					sigGenObj.emitSignals(dtmfBuffer, null, params);
				}
			} 
			else
			{
				log.error("ignoring generating dtmf buffer since the Network Connection has not received the ack from client.");
			}	
		} 
		catch (Exception e)
		{
			// Clean up media session
			MediaSession mediaSession = (MediaSession)sipSession.getAttribute("MEDIA_SESSION");
			log.info("Inside runDialog some exeption in the play calling terminate.");
			terminate(sipSession, mediaSession);
			return;
		}
		
	}
	
	protected void terminate(SipSession sipSession, MediaSession mediaSession) {
		SipServletRequest bye = sipSession.createRequest("BYE");
		log.info("Inside terminiate method");
		ncCounter=NC_DETECTOR;
		//dtmfBuffer.clear();
		dtmfBuffer = new String();
		
		try {
			terminateSigGenConnection();
			bye.send();
			// Clean up media session
			mediaSession.release();
			setState(sipSession, BYE_SENT);
		} catch (Exception e1) {
			log.error("Terminating: Cannot send BYE: "+e1);
		}	
		
		
	}
	
	
	protected void terminateSigGenConnection() {
		log.info("Inside terminateSigGenConnection method");
		
		SipSession sipSession = (SipSession)mediaSession.getAttribute("SIP_SESSION_SIG_GEN");
		if (sipSession == null )
			return;
		SipServletRequest bye = sipSession.createRequest("BYE");	
		try {
			bye.send();
			setState(sipSession, BYE_SENT);
		} catch (Exception e1) {
			log.error("Terminating: terminateSigGenConnection Cannot send BYE: "+e1);
		}		
	}
	
	protected void releaseSession( SipSession sipSession )
	{
		log.debug("Inside releaseSession. calling invalidate after bye send");
			
		try {
			sipSession.invalidate(); 
			sipSession.getApplicationSession().invalidate();
		} catch (Exception e) {
			log.warn("invalidate exception", e);
		}
	    
	}
	
	private class MyNetworkConnectionListener implements MediaEventListener<SdpPortManagerEvent>, Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 3664522667468275792L;

		public void onEvent(SdpPortManagerEvent event)
		{
			// Get network connection
			SdpPortManager sdp = event.getSource();
			
			SipSession sipSession;

			if (ncCounter == NC_GENERATOR ) //implies we have setup the SIG GEN and are waiting for reply here
			{
				 sipSession = (SipSession) mediaSession.getAttribute("SIP_SESSION");
			}
			else //else waiting for Sig Gen replied
			{
				 sipSession = (SipSession) mediaSession.getAttribute("SIP_SESSION_SIG_GEN");
			}

			SipServletRequest req = (SipServletRequest) sipSession.getAttribute("UNANSWERED_INVITE");
			sipSession.removeAttribute("UNANSWERED_INVITE");

			try
			{
				if (event.isSuccessful())
				{
					if (event.getEventType().equals(SdpPortManagerEvent.ANSWER_GENERATED))
					{
						SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
						resp.setContent(sdp.getMediaServerSessionDescription(), "application/sdp");
						resp.send();
						setState(sipSession, WAITING_FOR_ACK);
					}
					//else
					//{
					//	req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "Unsupported Media ype").send();
					//	conn.release();
					//}
				}
				else
				{
					if (event.getError().equals(SdpPortManagerEvent.SDP_NOT_ACCEPTABLE))
					{
						req.createResponse(SipServletResponse.SC_NOT_ACCEPTABLE_HERE).send();
					}
					else if (event.getError().equals(SdpPortManagerEvent.RESOURCE_UNAVAILABLE))
					{
						req.createResponse(SipServletResponse.SC_BUSY_HERE).send();
					}
					else
					{
						req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR).send();
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				// Clean up
				sipSession.invalidate();
				mediaSession.release();
			}
		}
	}
	
	//setup Logger
	private static Logger log = LoggerFactory.getLogger(DlgcSigGenTest.class);
}


