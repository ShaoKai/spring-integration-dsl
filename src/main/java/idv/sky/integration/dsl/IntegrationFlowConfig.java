package idv.sky.integration.dsl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.messaging.support.GenericMessage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static idv.sky.integration.dsl.IntegrationFlowConfig.GatewayBehavior.*;

@Configuration
public class IntegrationFlowConfig {
    public final static String CH_INBOUND = "inbound";
    public final static String CH_INBOUND_SINGLE_THREADED = "i1";
    public final static String CH_INBOUND_MULTI_THREADED = "i2";
    public final static String CH_INBOUND_MULTI_THREADED_TIMEOUT = "i3";
    public final static String CH_INBOUND_MULTI_THREADED_ASYNC = "i4";
    public final static String CH_OUTBOUND_SINGLE_THREADED = "o1";
    public final static String CH_OUTBOUND_MULTI_THREADED = "o2";
    public final static String CH_OUTBOUND_MULTI_THREADED_TIMEOUT = "o3";
    public final static String CH_OUTBOUND_MULTI_THREADED_ASYNC = "o4";
    public final static String CH_STDOUT = "stdout";
    private static final Logger logger = LoggerFactory.getLogger(IntegrationFlowConfig.class);
    @Autowired
    private MyGateway myGateway;

    static String getThreadNameAndDelay(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Thread.currentThread().getName();
    }

    @Bean
    public IntegrationFlow inboundFlow() {
        return IntegrationFlows.from(CH_INBOUND)
//                .routeToRecipients(r -> r.recipient(CH_STDOUT + 0, selector -> (selector.getPayload().equals("0")))
//                                         .recipient(CH_STDOUT + 1, selector -> (selector.getPayload().equals("1"))))
//                .route("payload", spec -> spec.channelMapping("0", CH_STDOUT+0)
//                                              .channelMapping("1", CH_STDOUT+1))
                .routeToRecipients(r -> r.recipient(CH_INBOUND_SINGLE_THREADED, selector -> (selector.getPayload().toString().endsWith(SINGLE_THREADED.name())))
                        .recipient(CH_INBOUND_MULTI_THREADED, selector -> (selector.getPayload().toString().endsWith(MULTI_THREADED.name())))
                        .recipient(CH_INBOUND_MULTI_THREADED_TIMEOUT, selector -> (selector.getPayload().toString().endsWith(MULTI_THREADED_TIMEOUT.name())))
                        .recipient(CH_INBOUND_MULTI_THREADED_ASYNC, selector -> (selector.getPayload().toString().endsWith(MULTI_THREADED_ASYNC.name()))))
//                .recipient(CH_STDOUT + 0)
                .get();
    }

    @Bean
    public IntegrationFlow s_inboundFlow() {
        return IntegrationFlows.from(CH_INBOUND_SINGLE_THREADED)
                .handle(m -> {
                    String message = ((GenericMessage) m).getPayload().toString();
                    logger.info("Receiving message : {}", message);
                    logger.info("Operation done    : {} - {}", message, myGateway.longRunningSingleThreaded(message));
                })

                .get();
    }

    @Bean
    public IntegrationFlow s_outboundFlow() {
        return IntegrationFlows.from(CH_OUTBOUND_SINGLE_THREADED)
                .transform(m -> getThreadNameAndDelay(1000))

                .get();
    }

    @Bean
    public IntegrationFlow m_inboundFlow() {
        return IntegrationFlows.from(CH_INBOUND_MULTI_THREADED)
                .channel(MessageChannels.executor(Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("M (%d)").build())))
                .handle(m -> {
                    String message = ((GenericMessage) m).getPayload().toString();
                    logger.info("Receiving message : {}", message);
                    logger.info("Operation done    : {} - {}", message, myGateway.longRunningMultiThreaded(message));
                })
                .get();
    }

    @Bean
    public IntegrationFlow m_outboundFlow() {
        return IntegrationFlows.from(CH_OUTBOUND_MULTI_THREADED)
                .transform(m -> getThreadNameAndDelay(1000))
                .get();
    }

    @Bean
    public IntegrationFlow mt_inboundFlow() {
        return IntegrationFlows.from(CH_INBOUND_MULTI_THREADED_TIMEOUT)
                .channel(MessageChannels.executor(Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("Timeout (%d)").build())))
                .handle(m -> {
                    String message = ((GenericMessage) m).getPayload().toString();
                    logger.info("Receiving message : {}", message);
                    logger.info("Operation done    : {} - {}", message, myGateway.longRunningMultiThreadedTimeout(message));
                })
                .get();
    }

    @Bean
    public IntegrationFlow mt_outboundFlow() {
        return IntegrationFlows.from(CH_OUTBOUND_MULTI_THREADED_TIMEOUT)
                .channel(MessageChannels.executor(Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("Timeout Consumer (%d)").build())))
                .transform(m -> getThreadNameAndDelay(5000))
                .get();
    }

    @Bean
    public IntegrationFlow async_inboundFlow() {
        return IntegrationFlows.from(CH_INBOUND_MULTI_THREADED_ASYNC)
                .channel(MessageChannels.executor(Executors.newScheduledThreadPool(2, new ThreadFactoryBuilder().setNameFormat("Async (%d)").build())))
                .handle(m -> {
                    String message = ((GenericMessage) m).getPayload().toString();
                    logger.info("Receiving message : {}", message);
                    Future<String> future = myGateway.longRunningMultiThreadedAsync(message);
                    try {
                        logger.info("Operation done    : {} - {}", message, future.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                })
                .get();
    }

    @Bean
    public IntegrationFlow async_outboundFlow() {
        return IntegrationFlows.from(CH_OUTBOUND_MULTI_THREADED_ASYNC)
                .transform(m -> getThreadNameAndDelay(5000))
                .get();
    }

    @Bean
    public IntegrationFlow stdoutFlow0() {
        return IntegrationFlows.from(CH_STDOUT + 0)
                .handle(msg -> logger.info("stdout0 : {} ", msg))
                .get();
    }

    @Bean
    public IntegrationFlow stdoutFlow1() {
        return IntegrationFlows.from(CH_STDOUT + 1)
                .handle(msg -> logger.info("stdout1 : {} ", msg))
                .get();
    }

    enum GatewayBehavior {
        SINGLE_THREADED, MULTI_THREADED, MULTI_THREADED_TIMEOUT, MULTI_THREADED_ASYNC
    }
}
