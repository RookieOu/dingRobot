package com.ding.common;

import com.ding.common.model.SignTypeEnum;
import com.ding.log.Log;
import com.ding.schedule.ScheduleService;
import com.ding.utils.TokenUtil;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.taobao.api.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yanou
 * @date 2022年10月24日 6:47 下午
 */
@Service
public class SendMessageService {
    @Autowired
    ScheduleService scheduleService;

    @Autowired
    UserService userService;

    @EventListener(ApplicationStartedEvent.class)
    public void initTask() {
        // todo key and secrest
        String token = TokenUtil.getToken("", "");
        scheduleService.scheduleAtHIS(() -> {
            try {
                send(userService.getRemindList(SignTypeEnum.ON_DUTY, token), token, SignTypeEnum.ON_DUTY);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }, 10, 10, 0);
        scheduleService.scheduleAtHIS(() -> {
            try {
                send(userService.getRemindList(SignTypeEnum.OFF_DUTY, token), token, SignTypeEnum.OFF_DUTY);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }, 20, 0, 0);
    }

    public void send(List<String> userIds, String accessToken, SignTypeEnum type) throws ApiException {
        if (userIds.isEmpty()) {
            Log.APPLICATION.info("no user need to be reminded");
            return;
        }
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2");
        OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
        //todo agentId
        request.setAgentId(836390886L);
        request.setToAllUser(false);

        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setMsgtype(type.getTitle() + "打卡提醒");
        msg.setText(new OapiMessageCorpconversationAsyncsendV2Request.Text());
        msg.getText().setContent("今天" + type.getTitle() + "还没有打卡哦！");
        request.setMsg(msg);
        for (String userId : userIds) {
            request.setUseridList(userId);
            client.execute(request, accessToken);
        }
    }

    public void send(String userId, String accessToken, SignTypeEnum type) throws ApiException {
        List<String> idsList = new ArrayList<>(1);
        idsList.add(userId);
        send(idsList, accessToken, type);
    }
}
