package com.ding.common;

import com.ding.common.config.TokenConfig;
import com.ding.common.model.SignTypeEnum;
import com.ding.log.Log;
import com.ding.schedule.ScheduleService;
import com.ding.utils.ListUtil;
import com.ding.utils.TimeUtils;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiMediaUploadRequest;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.dingtalk.api.response.OapiMediaUploadResponse;
import com.dingtalk.api.response.OapiMessageCorpconversationAsyncsendV2Response;
import com.taobao.api.ApiException;
import com.taobao.api.FileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        scheduleService.scheduleAtHIS(() -> {
            try {
                send(userService.getRemindList(SignTypeEnum.ON_DUTY), SignTypeEnum.ON_DUTY);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }, 10, 30, 0);
        scheduleService.scheduleAtHIS(() -> {
            try {
                send(userService.getRemindList(SignTypeEnum.OFF_DUTY), SignTypeEnum.OFF_DUTY);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }, 20, 0, 0);
    }

    /**
     * @param type
     * @throws ApiException
     */
    public void sendToLeader(SignTypeEnum type) throws ApiException {
        String accessToken = tokenConfig.getToken();
        List<String> remindList = userService.getRemindList(type);
        Map<String, List<String>> leaderMap = userService.getLeaderMap(remindList, accessToken);
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2");
        OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
        request.setAgentId(1999256841L);
        request.setToAllUser(false);
        if (leaderMap.isEmpty()) {
            Log.SYS.info("no need to remind leader");
            return;
        }
        for (String leader : leaderMap.keySet()) {
            request.setUseridList(leader);
            OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
            msg.setMsgtype("markdown");
            msg.setMarkdown(new OapiMessageCorpconversationAsyncsendV2Request.Markdown());
            msg.getMarkdown().setText(" JAP打卡机器人提醒\n 截止" + TimeUtils.getDateYMDHIS(TimeUtils.getBizMillis())
                    + " ,员工:**" + ListUtil.toStringWithSeparator(leaderMap.get(leader), ",") +
                    "**还没完成" + type.getTitle() + "打卡,辛苦提醒及时打卡!");
            msg.getMarkdown().setTitle("# 员工未打卡提醒");
            request.setMsg(msg);
            client.execute(request, accessToken);
            Log.SYS.info("send message to leader");
        }

    }

    public void send(List<String> userIds, SignTypeEnum type) throws ApiException {
        String accessToken = tokenConfig.getToken();
        if (userIds.isEmpty()) {
            Log.SYS.info("no user need to be reminded");
            return;
        }
        String mediaId = "@lALPDeC27WC8zI_M2Mzq";
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
        String userIdString = ListUtil.toStringWithSeparator(userIds, ",");
        request.setUseridList(userIdString);
        Log.SYS.info("send {} remind to {}", type.getDec(), ListUtil.toStringWithSeparator(userService.getName(userIds, accessToken), ","));
        client.execute(request, accessToken);
    }

    public void send(String userId, SignTypeEnum type) throws ApiException {
        List<String> idsList = new ArrayList<>(1);
        idsList.add(userId);
        send(idsList, type);
    }


    public String uploadPng(String path) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/media/upload");
        OapiMediaUploadRequest req = new OapiMediaUploadRequest();
        req.setType("image");
        FileItem item = new FileItem(path);
        req.setMedia(item);
        OapiMediaUploadResponse rsp = client.execute(req, tokenConfig.getToken());
        return rsp.getMediaId();
    }
}
