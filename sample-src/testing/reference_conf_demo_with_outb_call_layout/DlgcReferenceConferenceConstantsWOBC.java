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



public class DlgcReferenceConferenceConstantsWOBC {

		
	// Store record filenames for participant name and conference session
	 final static String FILE_PREFIX_RECORDED_NAME = "file:////tmp/myName"; 
	 final static String FILE_PREFIX_RECORDED_CONF = "file:////tmp/confrec";
	 final static String FILE_SUFFIX_RECORDED = ".ulaw";
	
	// Store valid caller options while in a conference
	 final static String PARTICIPANT_OPTION_PLAY_MENU = "*00";
	 final static String PARTICIPANT_OPTION_MUTE_TOGGLE = "*01";
	 final static String PARTICIPANT_OPTION_UNJOIN_CONFERENCE = "*02";
	 final static String PARTICIPANT_OPTION_REJOIN_CONFERENCE_USING_MIXER  = "*03";
	 final static String PARTICIPANT_OPTION_PLAY_NBLUE = "*05";
	 final static String PARTICIPANT_OPTION_GET_CONFERENCE_SIZE = "*06";
	 final static String PARTICIPANT_OPTION_STOP_PLAY = "*99";
	 final static String CONFERENCE_OPTION_RECORD = "*77";
	 final static String CONFERENCE_OPTION_STOP_RECORD = "*88";
	 final static String CREATE_PARTICIPANT_OUTBOUND_CALL = "*44";
	 final static String SET_LAYOUT= "*70";
	 final static String SET_LAYOUT_1 = "*71";
	 final static String SET_LAYOUT_2 = "*72";
	 final static String SET_LAYOUT_3 = "*73";
	 final static String SET_LAYOUT_4 = "*74";
	 final static String SET_LAYOUT_5 = "*75";
	 final static String SET_LAYOUT_6 = "*76";
	
	 final static String[] participantOptions = { 
		PARTICIPANT_OPTION_MUTE_TOGGLE, 
		PARTICIPANT_OPTION_GET_CONFERENCE_SIZE,
		PARTICIPANT_OPTION_UNJOIN_CONFERENCE,
		PARTICIPANT_OPTION_REJOIN_CONFERENCE_USING_MIXER,
		PARTICIPANT_OPTION_PLAY_NBLUE,
		PARTICIPANT_OPTION_PLAY_MENU,
		PARTICIPANT_OPTION_STOP_PLAY,
		CONFERENCE_OPTION_RECORD,
		CONFERENCE_OPTION_STOP_RECORD,
		CREATE_PARTICIPANT_OUTBOUND_CALL,
		SET_LAYOUT,
		SET_LAYOUT_1,
		SET_LAYOUT_2,
		SET_LAYOUT_3,
		SET_LAYOUT_4,
		SET_LAYOUT_5,
		SET_LAYOUT_6
	};   
	
	static boolean confJoinUseMixerAdapter=true;    //default should be true	
	static Boolean ipmsMediaServerType = false;	
	static boolean recordConferenceSession = false;	// Option to record entire conference session
	
}
