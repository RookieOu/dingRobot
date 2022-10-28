package com.ding.common;

import com.ding.common.model.SignTypeEnum;
import com.ding.utils.ListUtil;
import com.ding.utils.TimeUtils;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiAttendanceListRequest;
import com.dingtalk.api.request.OapiUserListidRequest;
import com.dingtalk.api.response.OapiAttendanceListResponse;
import com.dingtalk.api.response.OapiUserListidResponse;
import com.taobao.api.ApiException;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author yanou
 * @date 2022年10月24日 5:08 下午
 */
@Service
public class UserService {

    public List<String> getUserList(String accessToken) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/user/listid");
        OapiUserListidRequest req = new OapiUserListidRequest();
        req.setDeptId(414464114L);
        OapiUserListidResponse rsp = client.execute(req, accessToken);
        if (rsp.getErrcode() != 0) {
            return new ArrayList<>();
        } else {
            return rsp.getResult().getUseridList();
        }
    }

    public List<String> getRemindList(SignTypeEnum type, String accessToken) throws ApiException {
        // 获取access_token
        List<String> userList = getUserList(accessToken);
        // 通过调用接口获取考勤打卡结果
        DingTalkClient clientDingTalkClient = new DefaultDingTalkClient("https://oapi.dingtalk.com/attendance/list");
        OapiAttendanceListRequest requestAttendanceListRequest = new OapiAttendanceListRequest();
        // 查询考勤打卡记录的起始工作日
        String startTime = TimeUtils.getDateYMDHIS(TimeUtils.getGivenHourMillisFromNow(TimeUtils.getBizMillis(), 0, 10));
        requestAttendanceListRequest.setWorkDateFrom(startTime);
        // 查询考勤打卡记录的结束工作日
        String endTime = TimeUtils.getDateYMDHIS(TimeUtils.getGivenHourMillisFromNow(TimeUtils.getBizMillis(), 0, 19));
        requestAttendanceListRequest.setWorkDateTo(endTime);
        int groupCount = userList.size() / 10 + 1;
        List<List<String>> group = ListUtil.averageAssign(userList, groupCount);
        // 表示获取考勤数据的起始点
        requestAttendanceListRequest.setOffset(0L);
        // 表示获取考勤数据的条数，最大不能超过50条。
        requestAttendanceListRequest.setLimit(10L);
        List<OapiAttendanceListResponse.Recordresult> allResult = new ArrayList<>();
        for (List<String> oneGroup : group) {
            // 员工在企业内的userid列表，最多不能超过50个。
            requestAttendanceListRequest.setUserIdList(oneGroup);
            OapiAttendanceListResponse response = null;
            response = clientDingTalkClient.execute(requestAttendanceListRequest, accessToken);
            allResult.addAll(response.getRecordresult());
        }
        List<String> remindList = new ArrayList<>();
        for (OapiAttendanceListResponse.Recordresult record : allResult) {
            if (Objects.equals(record.getCheckType(), type.getDec())) {
                if (Objects.equals(record.getTimeResult(), "NotSigned")) {
                    remindList.add(record.getUserId());
                }
            }
        }
        return remindList;
    }
}
