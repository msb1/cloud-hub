package com.toptech.hubdataflow.handler;

import com.toptech.hubdataflow.broker.ReactorKafka;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

@Component
public class WebSocketDataHandler implements WebSocketHandler {

    @Autowired
    private ReactorKafka reactorKafka;

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    @Override
    @NonNull
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        // return Mono.empty();
        return webSocketSession.send(reactorKafka.getMessageFlux()
                .map(webSocketSession::textMessage))
                .and(webSocketSession.receive()
                        .map(WebSocketMessage::getPayloadAsText).log());
        // TODO: add second map method to process received messages from websockets (log for now)
    }
}
