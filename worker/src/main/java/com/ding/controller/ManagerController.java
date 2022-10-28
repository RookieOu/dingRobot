package com.ding.controller;

import com.ding.common.SendMessageService;
import com.ding.common.UserService;
import com.ding.common.config.TokenConfig;
import com.ding.common.model.SignTypeEnum;
import com.taobao.api.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

/**
 * @author yanou
 * @date 2022年10月25日 10:28 上午
 */
@RestController
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    SendMessageService sendMessageService;

    @Autowired
    UserService userService;

    @Autowired
    TokenConfig tokenConfig;

    @PostMapping("/send/all")
    public String sendToAll(@RequestBody Map<String, String> params) throws ApiException {
        String type = params.get("type");
        if (StringUtils.isEmpty(type)) {
            return "need type";
        }
        String token = tokenConfig.getToken();
        if (Objects.equals(type, SignTypeEnum.ON_DUTY.getDec())) {
            sendMessageService.send(userService.getRemindList(SignTypeEnum.ON_DUTY, token), token, SignTypeEnum.ON_DUTY);
        } else if (Objects.equals(type, SignTypeEnum.OFF_DUTY.getDec())) {
            sendMessageService.send(userService.getRemindList(SignTypeEnum.OFF_DUTY, token), token, SignTypeEnum.OFF_DUTY);
        } else {
            return "type error";
        }
        return "success";
    }

    @PostMapping("/send/one")
    public String senToOne(@RequestBody Map<String, String> params) throws ApiException {
        String type = params.get("type");
        String id = params.get("id");
        if (StringUtils.isEmpty(type) || StringUtils.isEmpty(id)) {
            return "need type and id";
        }
        String token = tokenConfig.getToken();
        if (Objects.equals(type, SignTypeEnum.ON_DUTY.getDec())) {
            sendMessageService.send(id, token, SignTypeEnum.ON_DUTY);
        } else if (Objects.equals(type, SignTypeEnum.OFF_DUTY.getDec())) {
            sendMessageService.send(id, token, SignTypeEnum.OFF_DUTY);
        } else {
            return "type error";
        }
        return "success";
    }
}
