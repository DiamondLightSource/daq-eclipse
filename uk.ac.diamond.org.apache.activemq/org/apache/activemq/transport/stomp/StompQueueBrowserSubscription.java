/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.transport.stomp;

import java.io.IOException;

import javax.jms.JMSException;

import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.MessageAck;
import org.apache.activemq.command.MessageDispatch;
import org.apache.activemq.command.TransactionId;

public class StompQueueBrowserSubscription extends StompSubscription {

    public StompQueueBrowserSubscription(ProtocolConverter stompTransport, String subscriptionId, ConsumerInfo consumerInfo, String transformation) {
        super(stompTransport, subscriptionId, consumerInfo, transformation);
    }

    @Override
    void onMessageDispatch(MessageDispatch md, String ackId) throws IOException, JMSException {

        if (md.getMessage() != null) {
            super.onMessageDispatch(md, ackId);
        } else {
            StompFrame browseDone = new StompFrame(Stomp.Responses.MESSAGE);
            browseDone.getHeaders().put(Stomp.Headers.Message.SUBSCRIPTION, this.getSubscriptionId());
            browseDone.getHeaders().put(Stomp.Headers.Message.BROWSER, "end");
            browseDone.getHeaders().put(Stomp.Headers.Message.DESTINATION,
                    protocolConverter.findTranslator(null).convertDestination(protocolConverter, this.destination));
            browseDone.getHeaders().put(Stomp.Headers.Message.MESSAGE_ID, "0");

            protocolConverter.sendToStomp(browseDone);
        }
    }

    @Override
    public MessageAck onStompMessageNack(String messageId, TransactionId transactionId) throws ProtocolException {
        throw new ProtocolException("Cannot Nack a message on a Queue Browser Subscription.");
    }

}
