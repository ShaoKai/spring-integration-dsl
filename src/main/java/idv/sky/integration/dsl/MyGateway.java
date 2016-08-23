package idv.sky.integration.dsl;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

import java.util.concurrent.Future;

import static idv.sky.integration.dsl.IntegrationFlowConfig.*;

@MessagingGateway
public interface MyGateway {

    @Gateway(requestChannel = CH_INBOUND)
    void sendRequest(String message);

    @Gateway(requestChannel = CH_OUTBOUND_SINGLE_THREADED)
    String longRunningSingleThreaded(String message);

    @Gateway(requestChannel = CH_OUTBOUND_MULTI_THREADED, replyTimeout = 5000)
    String longRunningMultiThreaded(String message);

    @Gateway(requestChannel = CH_OUTBOUND_MULTI_THREADED_TIMEOUT, replyTimeout = 500) // multithreaded consumer
    String longRunningMultiThreadedTimeout(String message);

    @Gateway(requestChannel = CH_OUTBOUND_MULTI_THREADED_ASYNC, replyTimeout = 5000)
    Future<String> longRunningMultiThreadedAsync(String message);
}