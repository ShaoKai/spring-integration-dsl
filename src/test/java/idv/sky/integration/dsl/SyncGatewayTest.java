package idv.sky.integration.dsl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static idv.sky.integration.dsl.IntegrationFlowConfig.GatewayBehavior.*;

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
    private static final Logger logger = LoggerFactory.getLogger(SyncGatewayTest.class);
    @Autowired
    MyGateway myGateway;

    @Test
    public void test() {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            AtomicInteger counter = new AtomicInteger();

//            for (int i = 0; i < 5; i++) {
//                myGateway.sendRequest(String.valueOf(i%2));
//            }
            for (int i = 0; i < 5; i++) {
                myGateway.sendRequest(counter.incrementAndGet() + " : " + SINGLE_THREADED);
            }
            counter = new AtomicInteger();
            for (int i = 0; i < 5; i++) {
                myGateway.sendRequest(counter.incrementAndGet() + " : " + MULTI_THREADED);
            }

            counter = new AtomicInteger();
            for (int i = 0; i < 5; i++) {
                myGateway.sendRequest(counter.incrementAndGet() + " : " + MULTI_THREADED_TIMEOUT);
            }

            counter = new AtomicInteger();
            for (int i = 0; i < 5; i++) {
                myGateway.sendRequest(counter.incrementAndGet() + " : " + MULTI_THREADED_ASYNC);
            }
            latch.await(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Configuration
    @IntegrationComponentScan(basePackages = {"idv.sky.integration.dsl"})
    @ComponentScan(basePackages = {"idv.sky.integration.dsl"})
    @EnableIntegration
    static class ContextConfiguration {

    }
}
