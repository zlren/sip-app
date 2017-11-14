/*
 * DIALOGIC CONFIDENTIAL
 *
 * Copyright 2010 Dialogic Corporation. All Rights Reserved.
 * The source code contained or described herein and all documents related to
 * the source code (collectively "Material") are owned by Dialogic Corporation 
 * or its suppliers or licensors ("Dialogic"). 
 *
 * BY DOWNLOADING, ACCESSING, INSTALLING, OR USING THE MATERIAL YOU AGREE TO BE
 * BOUND BY THE TERMS AND CONDITIONS DENOTED HERE AND ANY ADDITIONAL TERMS AND
 * CONDITIONS SET FORTH IN THE MATERIAL. Title to the Material remains with 
 * Dialogic. The Material contains trade secrets and proprietary and 
 * confidential information of Dialogic. The Material is protected by worldwide
 * Dialogic copyright(s) and applicable trade secret laws and treaty provisions.
 * No part of the Material may be used, copied, reproduced, modified, published, 
 * uploaded, posted, transmitted, distributed, or disclosed in any way without
 * prior express written permission from Dialogic Corporation.
 *
 * No license under any applicable patent, copyright, trade secret or other 
 *intellectual property right is granted to or conferred upon you by disclosure
 * or delivery of the Material, either expressly, by implication, inducement, 
 * estoppel or otherwise. Any license under any such applicable patent, 
 * copyright, trade secret or other intellectual property rights must be express
 * and approved by Dialogic Corporation in writing.
 *
 * You understand and acknowledge that the Material is provided on an 
 * AS-IS basis, without warranty of any kind.  DIALOGIC DOES NOT WARRANT THAT 
 * THE MATERIAL WILL MEET YOUR REQUIREMENTS OR THAT THE SOURCE CODE WILL RUN 
 * ERROR-FREE OR UNINTERRUPTED.  DIALOGIC MAKES NO WARRANTIES, EXPRESS OR 
 * IMPLIED, INCLUDING, WITHOUT LIMITATION, ANY WARRANTY OF NON-INFRINGEMENT, 
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  DIALOGIC ASSUMES NO 
 * RISK OF ANY AND ALL DAMAGE OR LOSS FROM USE OR INABILITY TO USE THE MATERIAL. 
 * THE ENTIRE RISK OF THE QUALITY AND PERFORMANCE OF THE MATERIAL IS WITH YOU.  
 * IF YOU RECEIVE ANY WARRANTIES REGARDING THE MATERIAL, THOSE WARRANTIES DO NOT 
 * ORIGINATE FROM, AND ARE NOT BINDING ON DIALOGIC.
 *
 * IN NO EVENT SHALL DIALOGIC OR ITS OFFICERS, EMPLOYEES, DIRECTORS, 
 * SUBSIDIARIES, REPRESENTATIVES, AFFILIATES AND AGENTS HAVE ANY LIABILITY TO YOU 
 * OR ANY OTHER THIRD PARTY, FOR ANY LOST PROFITS, LOST DATA, LOSS OF USE OR 
 * COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES, OR FOR ANY INDIRECT, 
 * SPECIAL OR CONSEQUENTIAL DAMAGES RELATING TO THE MATERIAL, UNDER ANY CAUSE OF
 * ACTION OR THEORY OF LIABILITY, AND IRRESPECTIVE OF WHETHER DIALOGIC OR ITS 
 * OFFICERS, EMPLOYEES, DIRECTORS, SUBSIDIARIES, REPRESENTATIVES, AFFILIATES AND 
 * AGENTS HAVE ADVANCE NOTICE OF THE POSSIBILITY OF SUCH DAMAGES.  THESE 
 * LIMITATIONS SHALL APPLY NOTWITHSTANDING THE FAILURE OF THE ESSENTIAL PURPOSE 
 * OF ANY LIMITED REMEDY.  IN ANY CASE, DIALOGIC'S AND ITS OFFICERS', 
 * EMPLOYEES', DIRECTORS', SUBSIDIARIES', REPRESENTATIVES', AFFILIATES' AND 
 * AGENTS' ENTIRE LIABILITY RELATING TO THE MATERIAL SHALL NOT EXCEED THE 
 * AMOUNTS OF THE FEES THAT YOU PAID FOR THE MATERIAL (IF ANY). THE MATERIALE 
 * IS NOT FAULT-TOLERANT AND IS NOT DESIGNED, INTENDED, OR AUTHORIZED FOR USE IN 
 * ANY MEDICAL, LIFE SAVING OR LIFE SUSTAINING SYSTEMS, OR FOR ANY OTHER 
 * APPLICATION IN WHICH THE FAILURE OF THE MATERIAL COULD CREATE A SITUATION 
 * WHERE PERSONAL INJURY OR DEATH MAY OCCUR. Should You or Your direct or 
 * indirect customers use the MATERIAL for any such unintended or unauthorized 
 * use, You shall indemnify and hold Dialogic and its officers, employees, 
 * directors, subsidiaries, representatives, affiliates and agents harmless 
 * against all claims, costs, damages, and expenses, and attorney fees and 
 * expenses arising out of, directly or indirectly, any claim of product 
 * liability, personal injury or death associated with such unintended or 
 * unauthorized use, even if such claim alleges that Dialogic was negligent 
 * regarding the design or manufacture of the part.
 */

package testing.unit;

import java.io.IOException;

import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.Joinable.Direction;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mixer.MediaMixer;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.DlgcTest;

public class DlgcJoinTest extends DlgcTest 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1;


	@Override
	public void init(ServletConfig cfg)
		throws ServletException
	{
		super.init(cfg);
	}
	
	@Override
	public void servletInitialized(SipServletContextEvent evt){

		String sName = evt.getSipServlet().getServletName();
		if( sName.equalsIgnoreCase("DlgcSipServlet") )
		{
			dlgcSipServletLoaded = true;
			log.debug(" DlgcJoinTest::servletInitialized DlgcSipServlet loaded");			
			return;
		}

		if( (sName.equalsIgnoreCase("DlgcJoinTest") ) && dlgcSipServletLoaded)
		{
			if ( servletInitializedFlag == false ) {
				log.debug("Entering DlgcJoinTest::servletInitialized servletName: " + sName);			
				servletInitializedFlag = true;
				initDriver();
				//myServletInitialized(evt);
			} else {
				log.debug("DlgcJoinTest::servletInitialized(): already servletInitialized was called...debouncing " + sName);
			}
		}
	}
	
	@Override
	public void doInvite(final SipServletRequest req)
		throws ServletException, IOException
	{
		try
		{
			MediaSession ms = mscFactory.createMediaSession();
			NetworkConnection nc1 = ms.createNetworkConnection(NetworkConnection.BASIC);
			NetworkConnection nc2 = ms.createNetworkConnection(NetworkConnection.BASIC);
			MediaGroup mg1 = ms.createMediaGroup(MediaGroup.PLAYER_RECORDER_SIGNALDETECTOR);
			MediaGroup mg2 = ms.createMediaGroup(MediaGroup.PLAYER_RECORDER_SIGNALDETECTOR);
			MediaMixer mx1 = ms.createMediaMixer(MediaMixer.AUDIO);
			MediaMixer mx2 = ms.createMediaMixer(MediaMixer.AUDIO);
			
			int errorCount = 0;
			
			///
			// Network Connection is primary Joinee.
			
			// NC to NC
			
			try
			{
				nc1.join(Direction.DUPLEX, nc2);
				log.error("NC [DUPLEX] NC - should not be allowed");
				errorCount++;
				
			}
			catch (MsControlException e)
			{
				// EXPECTED ERROR
				log.info("NC [DUPLEX] NC : EXPECTED ERROR - " + e.getMessage());
			}
			
			try
			{
				nc1.join(Direction.SEND, nc2);
				log.error("NC [SEND] NC - should not be allowed");
				errorCount++;
			}
			catch (MsControlException e)
			{
				// EXPECTED ERROR
				log.info("NC [SEND] NC : EXPECTED ERROR - " + e.getMessage());
			}
			
			try
			{
				nc1.join(Direction.RECV, nc2);
				log.error("NC [RECV] NC - should not be allowed");
				errorCount++;
			}
			catch (MsControlException e)
			{
				// EXPECTED ERROR
				log.info("NC [RECV] NC : EXPECTED ERROR - " + e.getMessage());
			}
			
			// NC to MG
			
			try
			{
				nc1.join(Direction.DUPLEX, mg1);
			}
			catch (MsControlException e)
			{
				log.error("NC [DUPLEX] MG : " + e.getMessage());
				errorCount++;
			}
			
			try
			{
				nc1.join(Direction.SEND, mg1);
				log.error("NC [SEND] MG - should not be allowed");
				errorCount++;
			}
			catch (MsControlException e)
			{
				// EXPECTED ERROR
				log.info("NC [SEND] MG : EXPECTED ERROR - " + e.getMessage());
			}
			
			try
			{
				nc1.join(Direction.RECV, mg1);
				log.error("NC [RECV] MG - should not be allowed");
				errorCount++;
			}
			catch (MsControlException e)
			{
				// EXPECTED ERROR
				log.info("NC [RECV] MG : EXPECTED ERROR - " + e.getMessage());
			}
			
			// NC to MX
			
			try
			{
				nc1.join(Direction.DUPLEX, mx1);
			}
			catch (MsControlException e)
			{
				log.error("NC [DUPLEX] MX : " + e.getMessage());
				errorCount++;
			}
			
			try
			{
				nc1.join(Direction.SEND, mx1);
			}
			catch (MsControlException e)
			{
				//log.error("NC [SEND] MX : " + e.getMessage());
				log.info("NC [SEND] MX : EXPECTED ERROR - " + e.getMessage());
				//errorCount++;
			}
			
			try
			{
				nc1.join(Direction.RECV, mx1);
			}
			catch (MsControlException e)
			{
				log.error("NC [RECV] MX : " + e.getMessage());
				errorCount++;
			}
			
			///
			// Media Group primary joinee.
			
			// MG to NC
			
			try
			{
				mg1.join(Direction.DUPLEX, nc1);
			}
			catch (MsControlException e)
			{
				log.error("MG [DUPLEX] NC : " + e.getMessage());
				errorCount++;
			}
			
			try
			{
				mg1.join(Direction.SEND, nc1);
				log.error("MG [SEND] NC - should not be allowed");
				errorCount++;
			}
			catch (MsControlException e)
			{
				log.info("MG [SEND] NC : EXPECTED ERROR - " + e.getMessage());
			}
			
			try
			{
				mg1.join(Direction.RECV, nc1);
				log.error("MG [RECV] NC - should not be allowed");
				errorCount++;
			}
			catch (MsControlException e)
			{
				log.info("MG [RECV] NC : EXPECTED ERROR - " + e.getMessage());
			}
			
			// MG to MG
			
			try
			{
				mg1.join(Direction.DUPLEX, mg2);
				log.error("MG [DUPLEX] MG - should not be allowed");
				errorCount++;
			}
			catch (MsControlException e)
			{
				log.info("MG [DUPLEX] MG : EXPECTED ERROR - " + e.getMessage());
			}
			
			try
			{
				mg1.join(Direction.SEND, mg2);
				log.error("MG [SEND] MG - should not be allowed");
				errorCount++;
			}
			catch (MsControlException e)
			{
				log.info("MG [SEND] NC : EXPECTED ERROR - " + e.getMessage());
			}
			
			try
			{
				mg1.join(Direction.RECV, mg2);
				log.error("MG [RECV] MG - should not be allowed");
				errorCount++;
			}
			catch (MsControlException e)
			{
				log.info("MG [RECV] MG : EXPECTED ERROR - " + e.getMessage());	
			}
			
			// MG to MX
			
			try
			{
				mg1.join(Direction.DUPLEX, mx1);
			}
			catch (MsControlException e)
			{
				log.error("MG [DUPLEX] MX : " + e.getMessage());
				errorCount++;
			}
			
			try
			{
				mg1.join(Direction.SEND, mx1);
			}
			catch (MsControlException e)
			{
				log.info("MG [SEND] MX : EXPECTED ERROR - " + e.getMessage());
				//log.error("MG [SEND] MX : " + e.getMessage());
				//errorCount++;
			}
			
			
			try
			{
				mg1.join(Direction.RECV, mx1);
			}
			catch (MsControlException e)
			{
				log.error("MG [RECV] MX : " + e.getMessage());
				errorCount++;
			}
			
			///
			// Media Mixer is primary Joinee.
			
			// MX to NC
			
			try
			{
				mx1.join(Direction.DUPLEX, nc1);
			}
			catch (MsControlException e)
			{
				log.error("MX [DUPLEX] NC : " + e.getMessage());
				errorCount++;
			}
			
			try
			{
				mx1.join(Direction.SEND, nc1);
			}
			catch (MsControlException e)
			{
				log.error("MX [SEND] NC : " + e.getMessage());
				errorCount++;
			}
			
			try
			{
				mx1.join(Direction.RECV, nc1);
			}
			catch (MsControlException e)
			{
				log.info("MX [RECV] NC : EXPECTED ERROR - " + e.getMessage());
				//log.error("MX [RECV] NC : " + e.getMessage());
				//errorCount++;
			}
			
			// MX to MG
			
			try
			{
				mx1.join(Direction.DUPLEX, mg1);
			}
			catch (MsControlException e)
			{
				log.error("MX [DUPLEX] MG : " + e.getMessage());
				errorCount++;
			}
			
			try
			{
				mx1.join(Direction.SEND, mg1);
			}
			catch (MsControlException e)
			{
				log.error("MX [SEND] NC : " + e.getMessage());
				errorCount++;
			}
			
			try
			{
				mx1.join(Direction.RECV, mg1);
			}
			catch (MsControlException e)
			{
				log.info("MX [RECV] MG : EXPECTED ERROR - " + e.getMessage());
				//log.error("MX [RECV] MG : " + e.getMessage());
				//errorCount++;
			}
			
			// MX to MX
			
			try
			{
				mx1.join(Direction.DUPLEX, mx2);
				log.error("MX [DUPLEX] MX - should not be allowed");
				errorCount++;
			}
			catch (MsControlException e)
			{
				log.info("MX [DUPLEX] MX : EXPECTED ERROR - " + e.getMessage());
			}
			
			try
			{
				mx1.join(Direction.SEND, mx2);
				log.error("MX [SEND] MX - should not be allowed");
				errorCount++;
			}
			catch (MsControlException e)
			{
				log.info("MX [SEND] MX : EXPECTED ERROR - " + e.getMessage());
			}
			
			try
			{
				mx1.join(Direction.RECV, mx2);
				log.error("MX [RECV] MX - should not be allowed");
				errorCount++;
			}
			catch (MsControlException e)
			{
				log.info("MX [RECV] MX : EXPECTED ERROR - " + e.getMessage());
			}
			
			if (errorCount != 0)
			{
				log.error("TEST FAILED !!");
			}
			else
			{
				log.info("TEST PASSED ...");
			}
		}
		catch (MsControlException e)
		{
			e.printStackTrace();
		}
	}

	
	private static Logger log = LoggerFactory.getLogger(DlgcJoinTest.class);
}
