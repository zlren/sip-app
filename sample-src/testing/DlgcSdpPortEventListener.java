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
package testing;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.networkconnection.SdpPortManagerException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DlgcSdpPortEventListener implements MediaEventListener<SdpPortManagerEvent> , Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8210857006791058170L;

	@Override
	public void onEvent(SdpPortManagerEvent event)
	{
		log.info("sdpPortEventListener::onEvent()");
		
		SdpPortManager sdp = event.getSource();
		
		SipServletRequest request = null;
		String desc=null;
		try {
			if ( event.getError() != MediaErr.RESOURCE_UNAVAILABLE )
				desc = sdp.getMediaServerSessionDescription().toString();
			else {
				log.error("Can't connect to Media Server...Media Server maybe down");
				log.error(event.getErrorText());
				//request = (SipServletRequest) session.getAttribute("UNANSWERED_INVITE");
				MediaSession ms = sdp.getMediaSession();
				SipSession session = (SipSession) ms.getAttribute("SIP_SESSION");
				request = session.createRequest("BYE");
				try
				{
					request.send();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			//log.debug("Media Server descriptor: " + desc);
		} catch (SdpPortManagerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		MediaSession ms = sdp.getMediaSession();
		SipSession session = (SipSession) ms.getAttribute("SIP_SESSION");
		
		if (session.isValid())
		{
			
			session.removeAttribute("UNANSWERTED_INVITE");
			if (event.getEventType().equals(SdpPortManagerEvent.ANSWER_GENERATED))
			{
				request = (SipServletRequest) session.getAttribute("UNANSWERED_INVITE");
				
				if (event.isSuccessful())
				{
					SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
					try
					{
						response.setContent(sdp.getMediaServerSessionDescription(), "application/sdp");
						//log.debug("PLAYERPLAYER response = " + response + " PLAYERPLAYER" );
						response.send();
					}
					catch (UnsupportedEncodingException e)
					{
						e.printStackTrace();
					}
					catch (SdpPortManagerException e)
					{
						e.printStackTrace();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					log.error("ANSWER GENERATED WITH " + event.getError().toString() + " FAILURE - " + event.getErrorText());
					try
					{
						request.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
			else if (event.getEventType().equals(SdpPortManagerEvent.NETWORK_STREAM_FAILURE))
			{
				log.info("DlgcSdpPortEventListener::onEvent() - NETWORK STREAM FAILURE: " + event.getErrorText());
				
				request = session.createRequest("BYE");
				try
				{
					//log.debug("Media Server descriptor: " + (sdp.getMediaServerSessionDescription()).toString() );
					request.send();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				//catch (SdpPortManagerException e)
				//{
				//	e.printStackTrace();
				//}
			}
		}
	}
	
	private static Logger log = LoggerFactory.getLogger(DlgcSdpPortEventListener.class);
}
