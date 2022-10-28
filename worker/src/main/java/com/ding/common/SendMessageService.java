package com.ding.common;

import com.ding.common.config.TokenConfig;
import com.ding.common.model.SignTypeEnum;
import com.ding.log.Log;
import com.ding.schedule.ScheduleService;
import com.ding.utils.TimeUtils;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiMediaUploadRequest;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.dingtalk.api.response.OapiMediaUploadResponse;
import com.taobao.api.ApiException;
import com.taobao.api.FileItem;
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

    @Autowired
    TokenConfig tokenConfig;

    @EventListener(ApplicationStartedEvent.class)
    public void initTask() {
        String token = tokenConfig.getToken();
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
        String mediaId = "@lALPDgCwYPjFfWLNBDnNBDg";
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2");
        OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
        request.setAgentId(1999256841L);
        request.setToAllUser(false);

        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setMsgtype("oa");
        msg.setOa(new OapiMessageCorpconversationAsyncsendV2Request.OA());
        msg.getOa().setHead(new OapiMessageCorpconversationAsyncsendV2Request.Head());
        msg.getOa().getHead().setText(type.getTitle() + "打卡提醒");
        msg.getOa().getHead().setBgcolor("FFBBBBBB");
        msg.getOa().setBody(new OapiMessageCorpconversationAsyncsendV2Request.Body());
        msg.getOa().getBody().setTitle(type.getTitle() + "打卡提醒");
        msg.getOa().getBody().setContent("JAP打卡机器人提醒:截止" + TimeUtils.getDateYMDHIS(TimeUtils.getBizMillis()) + ",您" + type.getTitle() + "还没打卡哦QAQ！");
        msg.getOa().getBody().setImage(mediaId);
        request.setMsg(msg);
        request.setUseridList(genUserList(userIds));
        client.execute(request, accessToken);
    }

    public void send(String userId, String accessToken, SignTypeEnum type) throws ApiException {
        List<String> idsList = new ArrayList<>(1);
        idsList.add(userId);
        send(idsList, accessToken, type);
    }

    private String genUserList(List<String> users) {
        if (users.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (String user : users) {
            result.append(user).append(",");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }

    private void uploadPng(String path) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/media/upload");
        OapiMediaUploadRequest req = new OapiMediaUploadRequest();
        req.setType("image");
        FileItem item = new FileItem(path);
        req.setMedia(item);
        OapiMediaUploadResponse rsp = client.execute(req, tokenConfig.getToken());
        System.out.println(rsp.getBody());
    }
}
