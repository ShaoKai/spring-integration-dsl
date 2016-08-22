package idv.sky.dsl.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyService {
    private static final Logger logger = LoggerFactory.getLogger(MyService.class);
    @Autowired
    MyGateway myGateway;

    public void longRunningOperation(String message) {
        logger.info("============================");
        logger.info("Receiving message : {}", message);
        logger.info("Operation start");

        String result;

        if(message.endsWith(GatewayBehavior.SINGLE_THREADED.name())){
            result = myGateway.longRunningSingleThreaded(message);
        }else if (message.endsWith(GatewayBehavior.MULTI_THREADED.name())){
            result = myGateway.longRunningMultiThreaded(message);
        }else{
            result = myGateway.longRunningMultiThreadedTimeout(message);
        }
        logger.info("Operation done    : {}", result);
    }

    public void sendRequest(String message) {
        myGateway.sendRequest(message);
    }


    enum GatewayBehavior {
        SINGLE_THREADED, MULTI_THREADED, MULTI_THREADED_TIMEOUT
    }
}
