package com.ding;

import com.ding.log.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.CountDownLatch;


/**
 * @author yanou
 * @date 2022年10月24日 3:58 下午
 */
@SpringBootApplication
public class Main {

    public static void main(String[] args){
        SpringApplication.run(Main.class, args);
        Log.SYS.info("server start.");
    }

    @PreDestroy
    public void stop() {
        Log.SYS.info("server stopped.");
    }
}
