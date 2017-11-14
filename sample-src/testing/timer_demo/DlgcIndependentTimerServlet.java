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
package testing.timer_demo;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DlgcIndependentTimerServlet extends SipServlet implements TimerListener, SipServletListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory.getLogger(DlgcIndependentTimerServlet.class);
	public static TimerService msTimerService;
	private static int counter = 0;
	private int timerInterval = 5000;
	public String timerId = null;

	@Override
	public void timeout(ServletTimer timer) {
		// TODO Auto-generated method stub
		final String timerID = timer.getId();	
		
		log.debug("timeout:Timer id:::" + timerID+"----"+timer.getInfo());
		log.debug("Enter DlgcIndependentTimerServlet:timeout() generated by timerID: " + timerID );
		
		if (counter < 5){
			start();
			counter++;
		}
		
	}

	@Override
	public void init(ServletConfig cfg) throws ServletException
	{
		super.init(cfg);
		log.debug("Leaving DlgcIndependentTimerServlet::init");
	}
	@Override
	public void servletInitialized(SipServletContextEvent arg0) {
		// TODO Auto-generated method stub
		log.debug("Entering DlgcIndependentTimerServlet::servletInitialized");
		msTimerService = (TimerService) getServletContext().getAttribute(TIMER_SERVICE);
		start();

	}
	public void start()
	{
		log.debug("DlgcIndependentTimerServlet::start()::counter: " + counter);
		try{
			String name = "timer_demo"+counter;
			SipApplicationSession  sas = createSipApplicationSession();
			ServletTimer ts = DlgcIndependentTimerServlet.getTimerService().createTimer(sas, this.timerInterval, false, name);
			this.timerId = ts.getId();
			log.debug("Start:Timer id:::" + this.timerId+"----"+ts.getInfo());
		}
		catch(Exception e){
			e.printStackTrace();
			log.error("Exception in start: " + e);
		}


	}
	public SipApplicationSession createSipApplicationSession(){
		ServletContext ctx = this.getServletContext();
		SipFactory factory = (SipFactory) ctx.getAttribute(SIP_FACTORY);
		SipApplicationSession appSession = factory.createApplicationSession();
		return appSession;
	}
	
	public static TimerService getTimerService()
	{
		return msTimerService;
	}

}