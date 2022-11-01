package com.ding.common;

import com.ding.common.model.SignTypeEnum;
import com.ding.utils.ListUtil;
import com.ding.utils.TimeUtils;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiAttendanceGetleavestatusRequest;
import com.dingtalk.api.request.OapiAttendanceListRequest;
import com.dingtalk.api.request.OapiAttendanceScheduleListbyusersRequest;
import com.dingtalk.api.request.OapiUserListidRequest;
import com.dingtalk.api.response.OapiAttendanceGetleavestatusResponse;
import com.dingtalk.api.response.OapiAttendanceListResponse;
import com.dingtalk.api.response.OapiAttendanceScheduleListbyusersResponse;
import com.dingtalk.api.response.OapiUserListidResponse;
import com.taobao.api.ApiException;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;

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
        Map<String, OapiAttendanceListResponse.Recordresult> allResult = new HashMap<>();
        List<String> restUsers = new ArrayList<>();
        for (List<String> oneGroup : group) {
            restUsers.addAll(getTodayRestUsers(accessToken, ListUtil.toStringWithSeparator(oneGroup, ",")));
            // 员工在企业内的userid列表，最多不能超过50个。
            requestAttendanceListRequest.setUserIdList(oneGroup);
            OapiAttendanceListResponse response = null;
            response = clientDingTalkClient.execute(requestAttendanceListRequest, accessToken);
            for (OapiAttendanceListResponse.Recordresult result : response.getRecordresult()) {
                if (Objects.equals(result.getCheckType(), type.getDec())) {
                    allResult.put(result.getUserId(), result);
                }
            }
        }
        List<String> remindList = new ArrayList<>();
        for (String user : userList) {
            //今日休息员工不发送提醒
            if (restUsers.contains(user)) {
                continue;
            }
            //如果当前打卡记录中没有该员工的记录，则证明其还未打卡
            if (!allResult.containsKey(user)) {
                remindList.add(user);
                continue;
            }
            if (Objects.equals(allResult.get(user).getCheckType(), type.getDec())) {
                //如果是未打卡状态，需要发送提醒
                if (Objects.equals(allResult.get(user).getTimeResult(), "NotSigned")) {
                    remindList.add(allResult.get(user).getUserId());
                }
            }
        }
        return remindList;
    }

    /**
     * 获取今日请假休息，或无排班的员工
     * @param token
     * @param userIds
     * @return
     * @throws ApiException
     */
    public Collection<String> getTodayRestUsers(String token, String userIds) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/attendance/schedule/listbyusers");
        OapiAttendanceScheduleListbyusersRequest req = new OapiAttendanceScheduleListbyusersRequest();
        req.setOpUserId("0118440567661238539");
        req.setUserids(userIds);
        long clock9 = TimeUtils.getGivenHourMillisFromNow(TimeUtils.getBizMillis(), 0, 9);
        long clock20 = TimeUtils.getGivenHourMillisFromNow(TimeUtils.getBizMillis(), 0, 20);

        req.setFromDateTime(clock9);
        req.setToDateTime(clock20);
        OapiAttendanceScheduleListbyusersResponse rsp = client.execute(req, token);
        //获取请假状态
        DingTalkClient hClient = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/attendance/getleavestatus");
        OapiAttendanceGetleavestatusRequest hReq = new OapiAttendanceGetleavestatusRequest();
        hReq.setUseridList(userIds);
        hReq.setStartTime(clock9);
        hReq.setEndTime(clock20);
        hReq.setOffset(0L);
        hReq.setSize(10L);
        OapiAttendanceGetleavestatusResponse hRsp = hClient.execute(hReq, token);
        Set<String> holiday = hRsp.getResult().getLeaveStatus()
                .stream()
                .filter(x -> x.getDurationPercent() != 0)
                .map(OapiAttendanceGetleavestatusResponse.LeaveStatusVO::getUserid)
                .collect(Collectors.toSet());

        return rsp.getResult().stream()
                .filter(x -> (x.getShiftId() == 0 || holiday.contains(x.getUserid())))
                .map(OapiAttendanceScheduleListbyusersResponse.TopScheduleVo::getUserid)
                .collect(Collectors.toSet());
    }
}
