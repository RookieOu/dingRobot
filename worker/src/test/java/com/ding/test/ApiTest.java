package com.ding.test;

import com.ding.common.UserService;
import com.ding.common.config.TokenConfig;
import com.ding.common.model.SignTypeEnum;
import com.ding.utils.TimeUtils;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.taobao.api.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @author yanou
 * @date 2022年10月28日 4:02 下午
 */
public class ApiTest {

    @Autowired
    TokenConfig tokenConfig;

    @Test
    public void getUserInfoTest() throws ApiException {
    }

    @Test
    public void getAttendance() throws ApiException {
        String token = tokenConfig.getToken();
        List<String> remindList = new UserService().getRemindList(SignTypeEnum.OFF_DUTY, token);
        System.out.println();
    }


    @Test
    public void getUserInfo() throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/get");
        OapiV2UserGetRequest req = new OapiV2UserGetRequest();
        req.setUserid("0118440567661238539");
        req.setLanguage("zh_CN");
        OapiV2UserGetResponse rsp = client.execute(req, tokenConfig.getToken());
        System.out.println(rsp.getResult().getName());
    }

    @Test
    public void sendMessageTest() throws ApiException {
        String mediaId = "@lALPDgCwYPjFfWLNBDnNBDg";


        SignTypeEnum type = SignTypeEnum.ON_DUTY;
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
        request.setUseridList("0118440567661238539");
        client.execute(request, tokenConfig.getToken());
    }
}
