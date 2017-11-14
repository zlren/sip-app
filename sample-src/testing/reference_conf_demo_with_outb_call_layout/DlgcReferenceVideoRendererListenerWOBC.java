package testing.reference_conf_demo_with_outb_call_layout;

import java.io.Serializable;


import javax.media.mscontrol.MediaEvent;
import javax.media.mscontrol.MediaEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DlgcReferenceVideoRendererListenerWOBC  <T extends MediaEvent<?>> implements MediaEventListener<T>, Serializable {

	private static final long serialVersionUID = 1;
	transient protected DlgcReferenceConferenceParticipantWOBC 		participant;
	transient String participantId = null;
	private static Logger log = LoggerFactory.getLogger(DlgcReferenceMixerMediaListenerWOBC.class);
	
	public DlgcReferenceVideoRendererListenerWOBC()
	{
		
	}
	
	@Override
	public void onEvent(T theEvent) {
		log.info("Entering DlgcReferenceVideoRendererListenerWOBC::onEvent");
		
		//EventType joinEvType = theEvent.getEventType();
		log.info("DlgcReferenceVideoRendererListenerWOBC::Type " + theEvent.getEventType() );
		log.info("DlgcReferenceVideoRendererListenerWOBC::Source " + theEvent.getSource().toString());
		log.info("DlgcReferenceVideoRendererListenerWOBC::ErrorText " + theEvent.getErrorText());			
		
	}
	
	
	
	
}
//Mixer/StreamGroup.__Any__