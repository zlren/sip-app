/**
 * DIALOGIC CONFIDENTIAL      
 * Copyright (C) 2005-2012 Dialogic Corporation. All Rights Reserved.
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

package testing.bridge_conference;

public class DlgcBridgeConstants {

	// Store play prompts
	final static String PROMPT_CONFERENCE_SIZE = "file:////var/snowshore/prompts/conference_size.wav";
	final static String PROMPT_WELCOME_TO_CONFERENCE = "file:////var/snowshore/prompts/welcome_to_conf.wav";
	
	public final static String XMS_PROMPT_CONFERENCE_SIZE = "file:////var/lib/xms/media/en_US/verification/snow/prompts/conference_size.wav";
	final static String XMS_PROMPT_WELCOME_TO_CONFERENCE = "file:////var/lib/xms/media/en_US/verification/snow/prompts/welcome_to_conf.wav";
	final static String XMS_PROMPT_CONFERENCE_MUTE = "file:////var/lib/xms/media/en_US/verification/snow/prompts/muted.wav";
	final static String XMS_PROMPT_CONFERENCE_UNMUTE = "file:////var/lib/xms/media/en_US/verification/snow/prompts/unmuted.wav";
	public final static String XMS_MUSIC_NBLUES = "file:////var/lib/xms/media/en_US/verification/snow/prompts/nilesBlues.wav";
	//public final static String XMS_PROMPT_JMC_MENU = "file:////var/lib/xms/media/en_US/verification/snow/prompts/jmcmenu.wav";
	public final static String XMS_PROMPT_JMC_MENU = "file:////var/lib/xms/media/en_US/verification/demoJSR309/BridgeConference/intro.wav";
	

	final static String XMS_PROMPT_PARKED_FROM_CONFERENCE = "file:////var/lib/xms/media/en_US/verification/snow/prompts/jmclegunjoin.wav";
	final static String XMS_PROMPT_UNPARKED_FROM_CONFERENCE = "file:////var/lib/xms/media/en_US/verification/snow/prompts/jmclegrejoin.wav";
	
	
	 final static String PROMPT_SAY_YOUR_NAME = "file:////opt/snowshore/prompts/gmvoices/en_US/susan/msg064";
	 final static String PROMPT_CONFERENCE_MUTE = "file:////var/snowshore/prompts/muted.wav";
	 final static String PROMPT_CONFERENCE_UNMUTE = "file:////var/snowshore/prompts/unmuted.wav";
	
	// Store record filenames for participant name and conference session
	 final static String FILE_PREFIX_RECORDED_NAME = "file:////tmp/myName"; 
	 final static String FILE_PREFIX_RECORDED_CONF = "file:////tmp/confrec";
	 final static String FILE_SUFFIX_RECORDED = ".ulaw";
	
	// Store prompt prefixes for speech synthesis
	 final static String PROMPT_PREFIX_DATE = "http://localhost/cgi-bin/phrase.cgi?locale=en_US&type=date&subtype=mdy&value=";
	 final static String PROMPT_PREFIX_TIME = "http://localhost/cgi-bin/phrase.cgi?locale=en_US&type=time&subtype=t12&value=";
	 final static String PROMPT_PREFIX_DURATION = "http://localhost/cgi-bin/phrase.cgi?locale=en_US&type=duration&subtype=3661&value=";
	 final static String PROMPT_PREFIX_NUMBER = "http://localhost/snowshore/phrase.cgi?locale=en_US&type=number&subtype=crd&value=";
		
	// Store valid caller options while in a conference
	 public final static String PARTICIPANT_OPTION_PLAY_MENU = "*00";
	 public final static String PARTICIPANT_OPTION_MUTE_TOGGLE = "*01";
	 public final static String PARTICIPANT_OPTION_UNJOIN_CONFERENCE = "*02";
	 public final static String PARTICIPANT_OPTION_REJOIN_CONFERENCE_USING_MIXER  = "*03";
	 public final static String PARTICIPANT_OPTION_REJOIN_CONFERENCE_USING_MIXER_APDATER = "*04";
	 public final static String PARTICIPANT_OPTION_PLAY_NBLUES = "*05";
	 public final static String PARTICIPANT_OPTION_GET_CONFERENCE_SIZE = "*06";
	 public final static String PARTICIPANT_OPTION_STOP_PLAY = "*99";
	
	
	//FR6008 For testing teh MixerAdapter to setup DTMF Clamping yes/no
	//Using MixerAdapter sets DTMF clamping to yes; using Mixer only sets DTMF clamping to no
	//JMC June 2011
	
	
	 public final static String[] participantOptions = { 
		PARTICIPANT_OPTION_MUTE_TOGGLE, 
		PARTICIPANT_OPTION_GET_CONFERENCE_SIZE,
		PARTICIPANT_OPTION_UNJOIN_CONFERENCE,
		PARTICIPANT_OPTION_REJOIN_CONFERENCE_USING_MIXER,
		PARTICIPANT_OPTION_REJOIN_CONFERENCE_USING_MIXER_APDATER,
		PARTICIPANT_OPTION_PLAY_NBLUES,
		PARTICIPANT_OPTION_PLAY_MENU,
		PARTICIPANT_OPTION_STOP_PLAY
	};   
	
	
	
	// Servlet behavior options
	//FR6008
	//if set to true, the initial join to the conference is done using the MixerAdapter dmtf clamp yes
	//Use this constant to est FR6008 DTMF CLAMPING via the Mixer Adapter.
	//JMC June 2011
	static boolean confJoinUseMixerAdapter=true;    //default should be true	
	static Boolean ipmsMediaServerType = false;	
	
	//static boolean mixerConfirmRequired = true;		// Option to confirm mixer resource
	
}
