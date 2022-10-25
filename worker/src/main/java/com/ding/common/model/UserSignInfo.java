package com.ding.common.model;

/**
 * @author yanou
 * @date 2022年10月24日 6:05 下午
 */
public class UserSignInfo {
    private SignTypeEnum type;

    long time;

    public UserSignInfo(SignTypeEnum type, Long time) {
        this.type = type;
        this.time = time;
    }

    public SignTypeEnum getType() {
        return type;
    }

    public long getTime() {
        return time;
    }
}
