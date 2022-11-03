package com.ding.test;

import com.ding.common.SendMessageService;
import com.ding.common.UserService;
import com.ding.common.config.TokenConfig;
import com.ding.common.model.SignTypeEnum;
import com.ding.utils.ListUtil;
import com.ding.utils.TimeUtils;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.OapiUserListsimpleResponse;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.dingtalk.api.response.OapiV2UserListResponse;
import com.taobao.api.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;


/**
 * @author yanou
 * @date 2022年10月28日 4:02 下午
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

    @Autowired
    TokenConfig tokenConfig;

    @Autowired
    UserService userService;

    @Autowired
    SendMessageService sendMessageService;

    @Test
    void getUserInfoTest() throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/user/listsimple");
        OapiUserListsimpleRequest req = new OapiUserListsimpleRequest();
        req.setDeptId(414464114L);
        req.setCursor(0L);
        req.setSize(100L);
        req.setOrderField("modify_desc");
        req.setContainAccessLimit(false);
        req.setLanguage("zh_CN");
        OapiUserListsimpleResponse rsp = client.execute(req, tokenConfig.getToken());
        Map<String, String> map = new HashMap<>();
        map.put("由阳", "");
        map.put("夏冬", "");
        map.put("乔远", "");
        map.put("颜欧", "");
        map.put("郭磊02", "");
        for (OapiUserListsimpleResponse.ListUserSimpleResponse listUserResponse : rsp.getResult().getList()) {
            if (map.containsKey(listUserResponse.getName())) {
                map.put(listUserResponse.getName(), listUserResponse.getUserid());
            }
        }
        System.out.println(map);
    }

    @Test
    void getAttendance() throws ApiException {
        String token = tokenConfig.getToken();
        List<String> remindList = userService.getRemindList(SignTypeEnum.OFF_DUTY);
        System.out.println();
    }


    @Test
    void getUserInfo() throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/get");
        OapiV2UserGetRequest req = new OapiV2UserGetRequest();
        req.setUserid("0118440567661238539");
        req.setLanguage("zh_CN");
        OapiV2UserGetResponse rsp = client.execute(req, tokenConfig.getToken());
        System.out.println(rsp.getResult().getName());
    }

    @Test
    void sendMessageTest() throws ApiException {
        String mediaId = "@lALPDeC27WC8zI_M2Mzq";
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

    @Test
    void getRemindListTest() throws ApiException {
        List<String> remindList = userService.getRemindList(SignTypeEnum.ON_DUTY);
        System.out.println();
    }

    @Test
    void restUserTest() throws ApiException {
        userService.getTodayRestUsers("062568262037881162");
    }

    @Test
    void uploadTest() throws ApiException {
        sendMessageService.uploadPng("src/main/java/com/ding/remind.png");
    }

    @Test
    void getLeaderTest() throws ApiException {
        Map<String, List<String>> leaderMap = userService.getLeaderMap(Collections.singletonList("0118440567661238539"), tokenConfig.getToken());
        System.out.println();
    }

    @Test
    void senToLeaderTest() throws ApiException {
//        sendMessageService.sendToleader(tokenConfig.getToken(),SignTypeEnum.OFF_DUTY);
        String accessToken = tokenConfig.getToken();
        SignTypeEnum type = SignTypeEnum.OFF_DUTY;
        Map<String, List<String>> leaderMap = new HashMap<>();
        List<String> worker = new ArrayList<>();
        worker.add("颜欧");
        worker.add("颜欧欧");
        worker.add("颜欧欧欧");
        worker.add("颜欧欧欧欧");
        worker.add("颜欧欧欧欧欧");
        leaderMap.put("0118440567661238539", worker);
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2");
        OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
        request.setAgentId(1999256841L);
        request.setToAllUser(false);
        for (String leader : leaderMap.keySet()) {
            request.setUseridList(leader);
            OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
            msg.setMsgtype("markdown");
            msg.setMarkdown(new OapiMessageCorpconversationAsyncsendV2Request.Markdown());
            msg.getMarkdown().setText(" JAP打卡机器人提醒\n 截止" + TimeUtils.getDateYMDHIS(TimeUtils.getBizMillis())
                    + " ,员工:**" + ListUtil.toStringWithSeparator(leaderMap.get(leader), ",") +
                    "**还没完成" + type.getTitle() + "打卡,辛苦提醒及时打卡!");
            msg.getMarkdown().setTitle("# 员工未打卡提醒");
//            msg.setMsgtype("text");
//            msg.setText(new OapiMessageCorpconversationAsyncsendV2Request.Text());
//            msg.getText().setContent("JAP打卡机器人提醒:截止" + TimeUtils.getDateYMDHIS(TimeUtils.getBizMillis())
//                    + " ,员工\n" + ListUtil.toStringWithSeparator(leaderMap.get(leader), ",")
//                    + "\n" + type.getTitle() + "还没打卡,辛苦提醒及时打卡!");
            request.setMsg(msg);
            client.execute(request, accessToken);
        }
    }
}
