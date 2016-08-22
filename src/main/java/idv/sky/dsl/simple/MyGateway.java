package idv.sky.dsl.simple;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;


@MessagingGateway
public interface MyGateway {
    String CHANNEL_INBOUND = "inbound";
    String CHANNEL_LONG_RUNNING_SINGLE_THREADED = "t1";
    String CHANNEL_LONG_RUNNING_MULTI_THREADED = "t2";
    String CHANNEL_LONG_RUNNING_MULTI_THREADED_TIMEOUT = "t3";

    String CHANNEL_STDOUT_FORWARD = "stdout_forward";

    @Gateway(requestChannel = CHANNEL_INBOUND)
    void sendRequest(String message);

    @Gateway(requestChannel = CHANNEL_LONG_RUNNING_SINGLE_THREADED, replyTimeout = 100) // useless in single thread
    String longRunningSingleThreaded(String message);

    @Gateway(requestChannel = CHANNEL_LONG_RUNNING_MULTI_THREADED, replyTimeout = 500)
    String longRunningMultiThreaded(String message);

    @Gateway(requestChannel = CHANNEL_LONG_RUNNING_MULTI_THREADED_TIMEOUT, replyTimeout = 500)
    String longRunningMultiThreadedTimeout(String message);
}
