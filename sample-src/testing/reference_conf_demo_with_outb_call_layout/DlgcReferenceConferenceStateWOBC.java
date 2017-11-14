/**
 * DIALOGIC CONFIDENTIAL
 * Copyright (C) 2005-2009 Dialogic Corporation. All Rights Reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DlgcReferenceConferenceStateWOBC implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 77456329876900L;
	protected String stateName = null;

	
	//events
	public void createConference(DlgcReferenceConferenceWOBC quickConference, String remoteSD) 
	{
		log.info("createConference present state not valid");
	}
	
	public void destroyConference(DlgcReferenceConferenceWOBC quickConference)
	{
		log.info("destroyConference present state not valid");
	}
	
	
	
	//static supported states as defined by State Machine Design Pattern
	protected static DlgcInitialStateWOBC 				dlgcInitialStateWOBC = new DlgcInitialStateWOBC();
	protected static DlgcCreatingConfStateWOBC   		dlgcCreatingConfStateWOBC = new DlgcCreatingConfStateWOBC();
	protected static DlgcCreatedConfStateWOBC   		dlgcCreatedConfStateWOBC = new DlgcCreatedConfStateWOBC();
	protected static DlgcDestroyingConfStateWOBC		dlgcDestroyingConfStateWOBC = new DlgcDestroyingConfStateWOBC();
	protected static DlgcDestroyedConfStateWOBC			dlgcDestroyedConfStateWOBC	= new DlgcDestroyedConfStateWOBC();

	public static Logger log = LoggerFactory.getLogger(DlgcReferenceConferenceStateWOBC.class);
	
} //based state

//DlgcInitialState
class DlgcInitialStateWOBC extends DlgcReferenceConferenceStateWOBC
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DlgcInitialStateWOBC()
	{
		stateName = new String("DlgcInitialStateWOBC");
	}

	@Override
	public void createConference(DlgcReferenceConferenceWOBC quickConference, String remoteSD)
	{
		log.info("createConference present state not valid");
	}
}

//DlgcCreatingConfState
class DlgcCreatingConfStateWOBC extends DlgcReferenceConferenceStateWOBC
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DlgcCreatingConfStateWOBC()
	{
		this.stateName = new String("DlgcCreatingConfStateWOBC");
	}
	
}

//DlgcCreatedConfState
class DlgcCreatedConfStateWOBC extends DlgcReferenceConferenceStateWOBC
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DlgcCreatedConfStateWOBC()
	{
		this.stateName = new String("DlgcCreatedConfStateWOBC");
	}
}	//end of class

//DlgcDestroyingConfState
class DlgcDestroyingConfStateWOBC extends DlgcReferenceConferenceStateWOBC
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DlgcDestroyingConfStateWOBC()
	{
		this.stateName = "DlgcDestroyingConfStateWOBC";
	}
}

//DlgcDestroyedConfState
class DlgcDestroyedConfStateWOBC extends DlgcReferenceConferenceStateWOBC 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DlgcDestroyedConfStateWOBC()
	{
		this.stateName = "DlgcDestroyedConfStateWOBC";
	}
	
	
}




