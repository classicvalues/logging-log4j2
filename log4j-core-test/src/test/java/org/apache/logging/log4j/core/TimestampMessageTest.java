/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.core;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.test.appender.ListAppender;
import org.apache.logging.log4j.core.test.junit.LoggerContextSource;
import org.apache.logging.log4j.core.test.junit.Named;
import org.apache.logging.log4j.core.time.Clock;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.message.TimestampMessage;
import org.apache.logging.log4j.plugins.Factory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Confirms that if you log a {@link TimestampMessage} then there are no unnecessary calls to {@link Clock}.
 * <p>
 * See LOG4J2-744.
 * </p>
 */
@LoggerContextSource("log4j2-744.xml")
public class TimestampMessageTest {
    @Factory
    public static Clock poisonClock() {
        return new PoisonClock();
    }

    @Test
    public void testTimestampMessage(final LoggerContext context, @Named("List") final ListAppender list) {
        final Logger log = context.getLogger("TimestampMessageTest");
        log.info((Message) new TimeMsg("Message with embedded timestamp", 123456789000L));
        final List<String> msgs = list.getMessages();
        assertNotNull(msgs);
        assertEquals(1, msgs.size());
        final String NL = System.lineSeparator();
        assertEquals("123456789000 Message with embedded timestamp" + NL, msgs.get(0));
    }

    public static class PoisonClock implements Clock {
        public PoisonClock() {
            super();
            // Breakpoint here for debugging.
        }

        @Override
        public long currentTimeMillis() {
            throw new RuntimeException("This should not have been called");
        }
    }

    static class TimeMsg extends SimpleMessage implements TimestampMessage {
        private static final long serialVersionUID = 1L;
        private final long timestamp;

        public TimeMsg(final String msg, final long timestamp) {
            super(msg);
            this.timestamp = timestamp;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }
    }
}