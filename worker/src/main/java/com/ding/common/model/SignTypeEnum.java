package com.ding.common.model;

/**
 * @author yanou
 * @date 2022年10月24日 6:05 下午
 */
public enum SignTypeEnum {
    /**
     * 上班打卡
     */
    ON_DUTY("OnDuty", "上班"),
    /**
     * 下班打卡
     */
    OFF_DUTY("OffDuty", "下班");

    String dec;

    String title;

    SignTypeEnum(String dec, String title) {
        this.dec = dec;
        this.title = title;
    }

    public String getDec() {
        return dec;
    }

    public String getTitle() {
        return title;
    }
}
