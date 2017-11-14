/* DIALOGIC CONFIDENTIAL
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DlgcConferenceMonitor implements Serializable 
{
	
	public enum TYPE_OF_EXCEPTIONS { MSCONTROL_EXCEPTION, UNSUPPORTED_EXCEPTION, JOIN_EXCEPTION };
	
	private TYPE_OF_EXCEPTIONS exType = TYPE_OF_EXCEPTIONS.MSCONTROL_EXCEPTION;
	private int WAIT_TIMEOUT = 5; 	//5 seconds
	private static final long serialVersionUID = 1L;
	private boolean operationCompleted = false;
	private String sStatus = null;
	private boolean bStatus = false;
	transient private final Lock lock=new ReentrantLock();
	transient private Condition operationCompletedCondition = lock.newCondition();
	private String whyAndWhat;
		
	 private static Logger log = LoggerFactory.getLogger(DlgcConferenceMonitor.class);

	 public DlgcConferenceMonitor(String whyWhat)
	 {
		 exType = TYPE_OF_EXCEPTIONS.MSCONTROL_EXCEPTION;

		 log.debug("DlgcConferenceMonitor CTOR: wait timeout set to: (sec): " + WAIT_TIMEOUT);
		 if ( whyWhat == null  )
			 whyAndWhat ="Not set by caller... unknow" ;
		 else
			 whyAndWhat = whyWhat;
	 }
	
	public boolean waitForRequestCompletion()
	{
		log.debug("DlgcConferenceMonitor::waitForRequestCompletion:: Entering");
		lock.lock();
			try {
				while ( operationCompleted == false ) {
					operationCompletedCondition.await(WAIT_TIMEOUT, TimeUnit.SECONDS);
					if  ( operationCompleted == false )
					{
						sStatus = "Error Timeout Error";
						log.error("DlgcConferenceMonitor::waitForRequestCompletion():: test - out of wait with status: " + sStatus + " Executing What: " + this.whyAndWhat);
						break;
					}
				}
				} catch (InterruptedException e) {
					log.error("SYNC_2_ASYNC DlgcConferenceMonitor::waitForRequestCompletion Exception: " + e.toString() + " Executing What: " + this.whyAndWhat);
					sStatus = e.toString();
					bStatus = false;
				}
				finally {
					lock.unlock();
				}
		return bStatus;
	}
	
	public void setExceptionType(TYPE_OF_EXCEPTIONS tex) {
		this.exType = tex;
	}
	
	public TYPE_OF_EXCEPTIONS getExceptionType()
	{
		return this.exType;
	}
	
	public void notifyRequestCompleted(boolean bs, String ss)
	{
		log.debug("DlgcConferenceMonitor::notifyRequestCompleted Entering");
		lock.lock();
		try {
			this.bStatus = bs;
			this.sStatus = ss;
			operationCompleted = true;
			log.debug("DlgcConferenceMonitor::notifyRequestCompleted:: " + this.whyAndWhat);
			this.operationCompletedCondition.signal();
			log.debug("DlgcConferenceMonitor::notifyRequestCompleted:: completed notify() with status: " + this.sStatus);

		} finally {
			lock.unlock();
		}
		log.debug("DlgcConferenceMonitor::notifyRequestCompleted Exiting");

	}
	
	public boolean getStatus()
	{
		return bStatus;
	}
	
	public String getStatusString()
	{
		return sStatus;
	}

	
}


