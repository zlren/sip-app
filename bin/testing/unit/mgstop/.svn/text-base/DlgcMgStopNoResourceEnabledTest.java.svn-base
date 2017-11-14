/**
 * DIALOGIC CONFIDENTIAL
 * Copyright (C) 2005-2015 Dialogic Corporation. All Rights Reserved.
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

package testing.unit.mgstop;

import java.io.IOException;

import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.resource.Resource;
import javax.media.mscontrol.resource.ResourceContainer;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This is one of many test conducted for mediaGroup.stop()
 * In this test, we test when a media group is created but no resource is activated.
 * Note each of these sub tests always creates a media session plus a network connection
 */
public class DlgcMgStopNoResourceEnabledTest extends DlgcMgStopBaseTestCase
{
	SdpEventListener <SdpPortManagerEvent> mySdpEventListener = null;
	public DlgcMgStopNoResourceEnabledTest(DlgcMgStopTest mainServlet) {
		super(mainServlet);
		mySdpEventListener = new  SdpEventListener<SdpPortManagerEvent>(this);
	}

	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory.getLogger(DlgcMgStopNoResourceEnabledTest.class);
	
	@Override
	public void invite(SipServletRequest req) {
		log.debug("Entering DlgcMgStopNoResourceEnabledTest::invite");
		SipSession ss = req.getSession();
		ss.setAttribute("TEST_CASE", this);
		try {
			super.executeInvite(req, mySdpEventListener);
		} catch (ServletException e) {
			log.error(e.toString());
		} catch (IOException e) {
			log.error(e.toString());
		} catch (MsControlException e) {
			log.error(e.toString());
		}
		log.debug("Leaving DlgcMgStopNoResourceEnabledTest::invite");
	}
	
	@Override
	//Called by the base class listener
	public boolean onSdpEvent(SdpPortManagerEvent anEvent) 
	{
		log.debug("Entering DlgcMgStopNoResourceEnabledTest::onSdpEvent");
		boolean status = super.onSdpEvent(anEvent);
		log.debug("Entering DlgcMgStopNoResourceEnabledTest::onSdpEvent");
		return status;
	}
	
	@Override
	//Called by the base class doAck
	//This indicates to the unit test that the connection is ready
	//and it can proceed with the detail stop test.
	public void ackResult(SipServletRequest req)
	{
		log.debug("Entering DlgcMgStopNoResourceEnabledTest:ackResult() ");
		//here you can do what ever you need to do after the connection between
		// the phone and the media server has been established
		//for this test we will just simply to a mg.stop() - note mg has no active resources
		SipSession ss = req.getSession();
		MediaGroup mg = (MediaGroup) ss.getAttribute("MEDIAGROUP");
		log.debug("DlgcMgStopNoResourceEnabledTest:calling mg.stop() ");
		mg.stop();
		log.debug("DlgcMgStopNoResourceEnabledTest:ackResult() - Returned from mg.stop() ");
		MediaSession mediaSession = (MediaSession) ss.getAttribute("MEDIA_SESSION");
		terminateSession(mediaSession);
		log.debug("Leaving DlgcMgStopNoResourceEnabledTest:ackResult() ");
	}
	
	
	
}
