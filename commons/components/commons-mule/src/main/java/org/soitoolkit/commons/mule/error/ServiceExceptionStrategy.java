/* 
 * Licensed to the soi-toolkit project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The soi-toolkit project licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.soitoolkit.commons.mule.error;

import java.util.Map;

import org.mule.api.MessagingException;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.config.ExceptionHelper;
import org.mule.exception.DefaultMessagingExceptionStrategy;
import org.soitoolkit.commons.mule.api.log.EventLogMessage;
import org.soitoolkit.commons.mule.api.log.EventLogger;
import org.soitoolkit.commons.mule.log.EventLoggerFactory;

/**
 * Base exception handler that catch errors and log them using the event-logger.
 * 
 * @author Magnus Larsson
 *
 */
public class ServiceExceptionStrategy extends DefaultMessagingExceptionStrategy {

	private final EventLogger eventLogger;

	public ServiceExceptionStrategy(MuleContext muleContext) {
		super(muleContext);
		eventLogger = EventLoggerFactory.getEventLogger(muleContext);
	}

	@Override
	protected void logException(Throwable t) {
        
//		super.logException(t);
        
		MuleException muleException = ExceptionHelper.getRootMuleException(t);
        if (muleException != null) {
        	
        	if (muleException instanceof MessagingException) {
        		MessagingException me = (MessagingException)muleException;
            	//eventLogger.logErrorEvent(muleException, me.getMuleMessage(), null, null);
        		EventLogMessage elm = new EventLogMessage();
        		elm.setMuleMessage(me.getMuleMessage());
        		
        		Throwable ex = (me.getCause() == null ? me : me.getCause());

        		eventLogger.logErrorEvent(ex, elm);                

        	} else {
                @SuppressWarnings("unchecked")
				Map<String, Object> info = ExceptionHelper.getExceptionInfo(muleException);
            	//eventLogger.logErrorEvent(muleException, info.get("Payload"), null, null);
        		EventLogMessage elm = new EventLogMessage();
        		//elm.setMuleMessage(message);
        		eventLogger.logErrorEvent(muleException, info.get("Payload"), elm);                
        	}
        	
        } else {
        	//eventLogger.logErrorEvent(t, (Object)null, null, null);
    		EventLogMessage elm = new EventLogMessage();
    		//elm.setMuleMessage(message);		
    		eventLogger.logErrorEvent(t, null, elm);
        }
	}
}
