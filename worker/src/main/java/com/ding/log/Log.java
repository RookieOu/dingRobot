package com.ding.log;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * @author yanou
 * @date 2022年10月24日 6:38 下午
 */
public class Log {


    private Log() {
    }

    /**
     * 系统日志
     */
    public static final Logger APPLICATION = LogManager.getLogger("APPLICATION");



    public static final Logger SYS = LogManager.getLogger("SYS");


    public static final Logger REST = LogManager.getLogger("REST");
}
