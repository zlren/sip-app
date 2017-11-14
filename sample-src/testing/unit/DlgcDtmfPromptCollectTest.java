
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
import java.util.Properties;

import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.signals.SignalDetector;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.resource.RTC;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.DlgcTest;




public class DlgcDtmfPromptCollectTest extends  DlgcTest {
	
	private static final String detectorTestProperty ="DlgcPromptCollectDemo.test";
	private static final String detectOnlyTest ="detectOnlyTest";		
	private static final String detectPropmtCollectTest ="detectPromptCollectTest";			//default
	//private static final String detectCollectWithPatternTest ="detectCollectWithPatternTest"; replaced by tckPatternTest
	
	//Dec 11 TCK Style Pattern
	private static final String tckPatternTest ="tckPatternTest";
	
	protected Configuration<MediaGroup> configuration;
	protected URI						prompt;
	protected URI						prompt2;
	protected URI						prompt3;
	protected URI						prompt4;
	protected URI						prompt5;
	protected URI						prompt6;
	protected URI						prompt7;
	protected URI						prompt8;
	protected URI						prompt9;
	protected URI						prompt10;

	
	private static Logger log = LoggerFactory.getLogger(DlgcDtmfPromptCollectTest.class);

	
	private static final long serialVersionUID = 1;

	// Listener for MediaGroup events
	private MySignalDetectorListener sigDetListener;
	
	// The options to receiveSignals
	private Parameters collectOptions;
	
	private String detectionTestType = null;
	
	private static final String testPropNumOfSignName = "DlgcPromptCollectDemo.signalDetector.number_of_signals";
	private Integer testPropNumOfSign = new Integer(4); 
	
	private static final String testPropPatternName = "DlgcPromptCollectDemo.signalDetector.match_pattern";
	private String  testPropPattern="770";
	
	private static final String initialDigitTimeoutName = "DlgcPromptCollectDemo.signalDetector.initial_digit_timeout";
	private Integer  initialDigitTimeout= new Integer("5000");	//5 sec default
	
	private static final String interDigitTimeoutName = "DlgcPromptCollectDemo.signalDetector.inter_digit_timeout";
	private Integer  interDigitTimeout= new Integer("5000");	//5 sec default
	
	private static final String interMaxDurationName = "DlgcPromptCollectDemo.signalDetector.max_duration";
	private Integer  interMaxDuration= new Integer("10000");	//10sec default
	
	
	private static final String loopCounterName= "DlgcPromptCollectDemo.signalDetector.loopCounter";
	static Integer loopCounter = new Integer(1);
	
	@Override
	public void servletInitialized(SipServletContextEvent evt){

	/*	String sName = evt.getSipServlet().getServletName();
		if( sName.equalsIgnoreCase("DlgcSipServlet") )
		{
			dlgcSipServletLoaded = true;
			log.debug(" DlgcDtmfPromptCollectTest::servletInitialized DlgcSipServlet loaded");			
			return;
		}

		if( (sName.equalsIgnoreCase("DlgcDtmfPromptCollectTest") ) && dlgcSipServletLoaded)
		{
			if ( servletInitializedFlag == false ) {
				log.debug("Entering DlgcDtmfPromptCollectTest::servletInitialized servletName: " + sName);			
				servletInitializedFlag = true;
				initDriver();
				myServletInitialized(evt);
			} else {
				log.debug("DlgcDtmfPromptCollectTest::servletInitialized(): already servletInitialized was called...debouncing " + sName);
			}
		}
		*/
		String sName = evt.getSipServlet().getServletName();


		if ( ( platform != null ) && (platform.compareToIgnoreCase(DlgcTest.TELESTAX_PLATFORM) == 0 ) || ( platform != null ) && (platform.compareToIgnoreCase(DlgcTest.ORACLE_PLATFORM) == 0 )) {
			if( sName.equalsIgnoreCase("DlgcSipServlet") )
			{
				dlgcSipServletLoaded = true;
				log.debug(" DLgcDtmfPromptCollectTest::servletInitialized DlgcSipServlet loaded");			
			}
			
			else if( sName.equalsIgnoreCase("DlgcPlayerTest") ) 
					playerServletInitCalled =true;

			if( playerServletInitCalled && dlgcSipServletLoaded)
			{
				if ( servletInitializedFlag == false ) {
					log.debug("Entering DlgcPlayerTest::servletInitialized servletName: " + sName);			
					servletInitializedFlag = true;
					initDriver();
					myServletInitialized(evt);
				} else {
					log.debug("DLgcDtmfPromptCollectTest::servletInitialized(): already servletInitialized was called...debouncing " + sName);
				}
			}

		} else { //TROPO Framework

			if( sName.equalsIgnoreCase("DlgcSipServlet") )
			{
				dlgcSipServletLoaded = true;
				log.debug(" DlgcPlayerTest::servletInitialized DlgcSipServlet loaded");			
				return;
			}

			if( (sName.equalsIgnoreCase("DlgcDtmfPromptCollectTest") ) && dlgcSipServletLoaded)
			{
				if ( servletInitializedFlag == false ) {
					log.debug("Entering DLgcDtmfPromptCollectTest::servletInitialized servletName: " + sName);			
					servletInitializedFlag = true;
					initDriver();
					myServletInitialized(evt);
				} else {
					log.debug("DLgcDtmfPromptCollectTest::servletInitialized(): already servletInitialized was called...debouncing " + sName);
				}
			}
		}
	}
	
	@Override
	protected void myServletInitialized(SipServletContextEvent arg0 )
	{
		try
		{
			String prop = null;
			configuration = MediaGroup.PLAYER_SIGNALDETECTOR;
			sigDetListener = new MySignalDetectorListener();
			// Setup the options for receiveSignals
			collectOptions = mscFactory.createParameters();
			prompt = URI.create("file:////var/lib/xms/media/en_US/verification/demoJSR309/dtmfPromptCollect/intro.wav");
			prompt2 = URI.create("file:////var/lib/xms/media/en_US/verification/demoJSR309/player/intro.wav");
			
			prompt3 = URI.create("file:////var/lib/xms/media/en_US/verification/demoJSR309/dtmfPromptCollect/intro.wav");
			prompt4 = URI.create("file:////var/lib/xms/media/en_US/verification/demoJSR309/player/intro.wav");
			
			prompt5 = URI.create("file:////var/lib/xms/media/en_US/verification/demoJSR309/dtmfPromptCollect/intro.wav");
			prompt6 = URI.create("file:////var/lib/xms/media/en_US/verification/demoJSR309/player/intro.wav");
			
			prompt7 = URI.create("file:////var/lib/xms/media/en_US/verification/demoJSR309/dtmfPromptCollect/intro.wav");
			prompt8 = URI.create("file:////var/lib/xms/media/en_US/verification/demoJSR309/player/intro.wav");
			
			prompt9 = URI.create("file:////var/lib/xms/media/en_US/verification/demoJSR309/dtmfPromptCollect/intro.wav");
			prompt10 = URI.create("file:////var/lib/xms/media/en_US/verification/demoJSR309/player/intro.wav");
			
	

			detectionTestType = demoPropertyObj.getProperty(detectorTestProperty);
			if ( detectionTestType == null ) {
				detectionTestType = detectPropmtCollectTest;
			}
			
			prop = demoPropertyObj.getProperty(testPropNumOfSignName);
			if ( prop != null ) {
				testPropNumOfSign = new Integer(prop);
			}
			
			prop = demoPropertyObj.getProperty(testPropPatternName);
			if ( prop != null ) {
				testPropPattern = new String(prop);
			}
			
			prop = demoPropertyObj.getProperty(initialDigitTimeoutName);
			if ( prop != null ) {
				initialDigitTimeout = new Integer(prop);
			}
			
			prop = demoPropertyObj.getProperty(interDigitTimeoutName);
			if ( prop != null ) {
				interDigitTimeout = new Integer(prop);
			}
			
			prop = demoPropertyObj.getProperty(interMaxDurationName);
			if ( prop != null ) {
				interMaxDuration = new Integer(prop);
			}
			
			prop = demoPropertyObj.getProperty(this.loopCounterName);
			if ( prop != null ) {
				this.loopCounter = new Integer(prop);
				if ( this.loopCounter == 0 ) 
					this.loopCounter =1;
			}else {
				this.loopCounter = new Integer(1);
			}
			
			log.debug("************************* DTMF UNIT TEST SETUP VALUES ********************************");
			log.debug("Property Name: " + detectorTestProperty + " Value: " + detectionTestType.toString());
			log.debug("Property Name: " + testPropNumOfSignName + " Value: " + testPropNumOfSign.toString());
			log.debug("Property Name: " + testPropPatternName + " Value: " + testPropPattern.toString());
			log.debug("Property Name: " + initialDigitTimeoutName + " Value: " + initialDigitTimeout.toString());
			log.debug("Property Name: " + interDigitTimeoutName + " Value: " + interDigitTimeout.toString());
			log.debug("Property Name: " + interMaxDurationName + " Value: " + interMaxDuration.toString());
			log.debug("Property Name: " + loopCounterName + " Value: " + loopCounter.toString());
			log.debug("**************************************************************************************");

			
			
			//collectOptions.put(Player.AUDIO_CODEC, CodecConstants.MULAW_PCM_64K); 
			collectOptions.put(SignalDetector.INITIAL_TIMEOUT, initialDigitTimeout);	
			collectOptions.put(SignalDetector.INTER_SIG_TIMEOUT, interDigitTimeout);	
			collectOptions.put(SignalDetector.MAX_DURATION, interMaxDuration );   //not supported by MSML June 2013
			
			
		}catch (Exception ex) {
			log.debug(ex.toString() );
			//throw new ServletException(ex);
		}
	}
	

	@Override
	public void init(ServletConfig cfg)
	throws ServletException
	{
		super.init(cfg);
		myServletLoaded = true;
	}


	class MySignalDetectorListener implements MediaEventListener<SignalDetectorEvent>, Serializable {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void onEvent(SignalDetectorEvent anEvent) {
			
			//note using the JSR309 qualifier RTC_TRIGGERED not standard way of using this qualifier
			//there were not other way to map the IPMS condition using JSR 309 SPEC
			//so we decided VZ and Dialogic to use this for this purpose
			if ( (anEvent.getEventType() == SignalDetectorEvent.SIGNAL_DETECTED) && (anEvent.getQualifier() == SignalDetectorEvent.RTC_TRIGGERED) ) 
			{
				
				log.info("MySignalDetectorListener DTMF WAS ENABLED IN THE MEDIA SERVER");
				return;
				
			}
			
			if ( anEvent.getQualifier() == SignalDetectorEvent.DURATION_EXCEEDED) {
				log.debug("MySignalDetectorListener:: DURATION_EXCEEDED");
			} else if ( anEvent.getQualifier() == SignalDetectorEvent.INITIAL_TIMEOUT_EXCEEDED) {
				log.debug("MySignalDetectorListener:: INITIAL_TIMEOUT_EXCEEDED");
			} else if ( anEvent.getQualifier() == SignalDetectorEvent.INTER_SIG_TIMEOUT_EXCEEDED) {
				log.debug("MySignalDetectorListener:: INTER_SIG_TIMEOUT_EXCEEDED");
			} else {
				log.debug("MySignalDetectorListener:: other: qualifer " + anEvent.getQualifier().toString() );
			}
			
			log.info("MySignalDetectorListener ReceiveSignals terminated with: "+anEvent);
			log.info("MySignalDetectorListener ReceiveSignals terminated Event: "+anEvent.getEventType().toString());

			// In this example, the collected DTMFs are just logged.
			// In real life they could be returned in a signaling parameter, or propagated to a JSP
			log.info("MySignalDetectorListener DTMF Collected: "+anEvent.getSignalString());
			String qualString = anEvent.getQualifier().toString();
			log.info("MySignalDetectorListener Qualifier: "+ qualString );
			log.info("MySignalDetectorListener ReceiveSignals with Error Type (if any): "+anEvent.getError().toString());
			log.info("MySignalDetectorListener ReceiveSignals with Error String (if any): "+anEvent.getErrorText());

			
			// Release the call and terminate
			MediaSession mediaSession = anEvent.getSource().getMediaSession();
			SipSession sipSession = (SipSession) mediaSession.getAttribute("SIP_SESSION");
		
			Integer lc = (Integer)sipSession.getAttribute("loopCounterName");
			if ( lc == 1 )
				sendBye( sipSession, mediaSession);
			else {
				lc--;
				sipSession.setAttribute("loopCounterName", lc);
				runDialog(sipSession);
			}
			//June 25th Syniverse Comment bye for testing
			//sendBye( sipSession, mediaSession);
			
			
		}
	}		
	
	
	/*
	
	private static final String detectCollectWithPatternTest ="detectCollectWithPatternTest";

	
	private String detectionTestType = null;
	 */

	//Dec 11 2014 
	public void runDialog(SipSession sipSession)
	{
		try
		{
			
			Integer lc = (Integer)sipSession.getAttribute("loopCounterName");		
			
			log.debug("DlgcDtmfPromptCollectTest: calling receiveSignals() loopCounter: " + lc.toString() );
			
			MediaGroup mg = (MediaGroup)sipSession.getAttribute("MEDIAGROUP");
			RTC[] rtcs = new RTC[1];
						
			//Oracle conference way of doing bargeIn
			//rtcs[0] = new RTC( SignalDetector.DETECTION_OF_ONE_SIGNAL,  Player.STOP  );
			
			//HP RI way of doing RTC
			rtcs[0] = MediaGroup.SIGDET_STOPPLAY;			//barge in
			
			//Force play and collect to flush the buffer before starting the prompt
			//rtcs[1] = new RTC( Player.PLAY_START,  SignalDetector.FLUSH_BUFFER );
					
			//Add in FLUSH_BUFFER NOV 24 2014
			//note flushBuffer set a flag in the connector to clear buffer in the xms when the next receiveSignals request 
			//is executed... Please Note that after the receiveSignals is called the internal flag inside the connector is 
			//clear automatically - Thus if the application wishes to clear db it must call it every timer for each receiveSignals call
			//this is an extension to the Dialogic Connector - meaning it does not follow standards.
			mg.getSignalDetector().flushBuffer();
			
			
			//Dec 11 TCK Style Pattern
			/*
			 * setting up signals to a positive value you cant control max value; since max value = #signals
			 * also the only you can pass in in the pattern when setting signal positive is a single rtk value
			 * other samples: set min as 1, max as 20 and rtk as '#'.
			 * Note: RTK value can't be part of the pattern matching string  
			 * 			why -1			min=2;max=5;rtk=3	min=2;max=5;rtk=3 [333] No Match returns 3
			 *   #signals    match pattern 			msg sent to XMS(digits=)	enter 	outcome
			 *     5			45					min=1,max=5,rtk=4			[456] 	matches on 4  note rtk only takes one digit
			 *     5            #                   min=1,max=5,rtk=#           [12#]   matches 12#  (min is always 1 can't control min)
			 *     3			#					min=1,max=3,rtk=#			[258]   match found 258#
			 *     5			#					min=1,max=5,rtk=#			[123456] no match 123456
			 *     3			xxxx				min=1;max=3:rtk=xxxx		[7785] no match 7785
			 *     5			min=2;max=5;rtk=#	min=1,max=5;rtk=min=2;max=5;rtk=#  NOT GOOD... NOT VALID
			 *     0			min=1;max=5;rtk=#	min=1;max=5;rtk=#			[12345#]  Match found 12345#  note returns max + rtk
			 *     0			min=1;max=5;rtk=#	min=1;max=5;rtk=#			[123456]  No Match found 123456 note returns max + rtk
			 *     -1			min=1;max=5;rtk=#	min=1;max=5;rtk=#			[12345#]  Match found 12345# note returns max + rtk
			 *     -1			min=1;max=5;rtk=#	min=1;max=5;rtk=34			[234] Match found 23  RTK only allows 1 digit
			 *     -1			123					digits=123					[123] Match found 123
			 *     -1			xxxx				digits=xxxx					[1234] 
			 *     -1			min=2;max=5;rtk=3	min=2;max=5;rtk=3			[333] No Match returns 3  because RTK value can't be part of the pattern matching string
			 */
			if ( (detectionTestType.compareToIgnoreCase(tckPatternTest) == 0 )  ){ 
				//same as detectCollectWithPatternTest... tckPatternTest will replace detectCollectWithPatternTest
				log.debug("DlgcDtmfPromptCollectTest: EXECUTING  COLLECT WITH TCK PATTERN: " + testPropPattern);
				
				//Using method passing pattern via the parameter list
				//log.debug("DlgcDtmfPromptCollectTest: Using parameter list to pass in to the connector the pattern approach");
				//collectOptions.put(SignalDetector.PATTERN[1], testPropPattern);		
				//Parameter[] detectDigitPattern = { SignalDetector.PATTERN[1] }; 		//redefine the pattern 1 value
				
				//using method chaning the pattern
				log.debug("DlgcDtmfPromptCollectTest: Changing the Connector pattern table approach");
				Parameters mgParms = mg.createParameters();
				mgParms.put(SignalDetector.PATTERN[1], testPropPattern);		
				mg.setParameters(mgParms);
				Parameter[] detectDigitPattern = { SignalDetector.PATTERN[1] }; 		//redefine the pattern 1 value
			}else if ( (detectionTestType.compareToIgnoreCase(detectOnlyTest) == 0 )  ){
					//just match four digits
				log.debug("DlgcDtmfPromptCollectTest: EXECUTING DETECTION ONLY LOOKING FOR 4 DIGITS");
				//basically number of singnals => xxx   if num of singnal is 3 then we get xxx , 4 xxxx etc
				//Add in FLUSH_BUFFER NOV 24 2014
				mg.getSignalDetector().receiveSignals(testPropNumOfSign, null, rtcs, collectOptions);	// only uses numofSignal
				
			} else if ( (detectionTestType.compareToIgnoreCase(detectPropmtCollectTest) == 0 )  ){
				// match pattern   
				//collectOptions.put(SignalDetector.PROMPT, prompt);
				//Parameter[] detectDigitPattern = { SignalDetector.PATTERN[7], SignalDetector.PATTERN[7], SignalDetector.PATTERN[0] };  //match digit 770
				//mg.getSignalDetector().receiveSignals(testPropNumOfSign, detectDigitPattern, rtcs, collectOptions);   
				
				Parameters mgParms = mg.createParameters();
				mgParms.put(SignalDetector.PATTERN[1], testPropPattern);		
				mg.setParameters(mgParms);
				Parameter[] detectDigitPattern = { SignalDetector.PATTERN[1] }; 		//redefine the pattern 1 value
				//log.debug("DlgcDtmfPromptCollectTest: EXECUTING DEFAULT - PROMPT AND COLLECT ONLY LOOKING TO MATCH 770");
				// match pattern   
				//Add multiple prompts
				URI[] twoPrompts = { prompt, prompt2, prompt3,  prompt4, prompt5, prompt6, prompt7, prompt8, prompt9, prompt10 };
				collectOptions.put(SignalDetector.PROMPT, twoPrompts);
				
				//single prompt comment twoPrompt above if you one to test only one prompt
				//collectOptions.put(SignalDetector.PROMPT, prompt);
				
				collectOptions.put(SignalDetector.PATTERN[1], testPropPattern);	

				mg.getSignalDetector().receiveSignals(testPropNumOfSign, detectDigitPattern, rtcs, collectOptions);   
				
				
			} 
			
			
			
			
			//mg.getSignalDetector().receiveSignals(4, null, rtcs, Parameters.NO_PARAMETER);
		} 
		catch (Exception e)
		{
			// Clean up media session
			e.printStackTrace();
			terminateSession(sipSession);
			return;
		}
	}
	
	
	private void terminateSession(SipSession session)
	{
		if (session != null)
		{
			MediaSession mediaSession = (MediaSession) session.getAttribute("MEDIA_SESSION");
			mediaSession.release();
			session.invalidate();
			session.getApplicationSession().invalidate();
		}

	}
	
	
	@Override
	public void doAck(SipServletRequest req)
	{
		SipSession sipSession = req.getSession();
		runDialog( sipSession);
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
				
				networkConnection.getSdpPortManager().addListener(speListener);
				mediaGroup.getSignalDetector().addListener(sigDetListener);
				
				mediaGroup.join(Joinable.Direction.DUPLEX, networkConnection);
				
				Integer lc = (Integer)sipSession.getAttribute("loopCounterName");
				
				if ( lc == null ) {
					lc = new Integer(DlgcDtmfPromptCollectTest.loopCounter);
					sipSession.setAttribute("loopCounterName", lc);
				}
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
	
	protected void sendBye(SipSession sipSession, MediaSession mediaSession) {
		
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
	
	
	
	
}

