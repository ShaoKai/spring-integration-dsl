package idv.sky.dsl.simple;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static idv.sky.dsl.simple.MyGateway.*;

/**
 * Sync Gateway - single-threaded
 * <p>
 * <p>
 * If a component downstream is still running
 * (e.g., infinite loop or a very slow service), then setting reply-timeout has
 * no effect and Gateway method call will not return until such downstream service exits
 * (e.g., return or exception).
 * </p>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class SyncGatewayTest {


    @Autowired
    MyService myService;

    @Test
    public void test() {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            AtomicInteger counter = new AtomicInteger();
            for (int i = 0; i < 5; i++) {
                myService.sendRequest(counter.incrementAndGet() + " : " + MyService.GatewayBehavior.SINGLE_THREADED);
            }

            counter = new AtomicInteger();
            for (int i = 0; i < 5; i++) {
                myService.sendRequest(counter.incrementAndGet() + " : " + MyService.GatewayBehavior.MULTI_THREADED);
            }

            counter = new AtomicInteger();
            for (int i = 0; i < 5; i++) {
                myService.sendRequest(counter.incrementAndGet() + " : " + MyService.GatewayBehavior.MULTI_THREADED_TIMEOUT);
            }

            latch.await(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Configuration
    @IntegrationComponentScan(basePackages = {"idv.sky.dsl.simple"})
    @ComponentScan(basePackages = {"idv.sky.dsl.simple"})
    @EnableIntegration
    static class ContextConfiguration {
        @Autowired
        MyService myService;

        @Bean
        public IntegrationFlow forwardFlow() {
            return IntegrationFlows.from(CHANNEL_INBOUND)
//                    .wireTap(CHANNEL_STDOUT_FORWARD)
                    .handle(m -> myService.longRunningOperation(((GenericMessage) m).getPayload().toString()))
                    .get();
        }

        @Bean
        public IntegrationFlow singleThreadFlow() {
            return IntegrationFlows.from(CHANNEL_LONG_RUNNING_SINGLE_THREADED)
                    .transform(m -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return Thread.currentThread().getName();
                    })
                    .get();
        }

        @Bean
        public IntegrationFlow multipleThreadFlow() {
            return IntegrationFlows.from(CHANNEL_LONG_RUNNING_MULTI_THREADED)
                    .channel(MessageChannels.executor(Executors.newScheduledThreadPool(3, new ThreadFactoryBuilder().setNameFormat("M (%d)").build())))
                    .transform(m -> {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return Thread.currentThread().getName();
                    })
                    .get();
        }

        @Bean
        public IntegrationFlow multipleThreadTimeoutFlow() {
            return IntegrationFlows.from(CHANNEL_LONG_RUNNING_MULTI_THREADED_TIMEOUT)
                    .channel(MessageChannels.executor(Executors.newScheduledThreadPool(2, new ThreadFactoryBuilder().setNameFormat("Timeout (%d)").build())))
                    .transform(m -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return Thread.currentThread().getName();
                    })
                    .get();
        }
    }
}
