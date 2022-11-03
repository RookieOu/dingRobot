package com.ding.common.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yanou
 * @date 2022年11月03日 3:05 下午
 */
@Component
public class RegulatorConfig {
    private String filePath = "src/main/resources/regulator.json";

    private Map<String, String> leaderMap = new HashMap<>();

    public RegulatorConfig() {
        String s = readJsonFile(filePath);
        JSONObject jsonObject = JSON.parseObject(s);
        for (String leader : jsonObject.keySet()) {
            JSONArray jsonArray = jsonObject.getJSONArray(leader);
            List<String> workers = jsonArray.toJavaList(String.class);
            for (String worker : workers) {
                leaderMap.put(worker, leader);
            }
        }
    }

    private String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);

            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getLeader(String name) {
        return leaderMap.get(name);
    }
}
