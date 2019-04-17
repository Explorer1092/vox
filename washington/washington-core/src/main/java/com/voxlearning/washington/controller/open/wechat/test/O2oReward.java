package com.voxlearning.washington.controller.open.wechat.test;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shiwei.liao on 2015/6/19.
 */
public class O2oReward {
    public static void main(String[] args) throws IOException {
        String[] rewardKey = new String[]{"TYPE", "LEVEL", "PART", "INTEGRAL", "ACT_ID"};
        String excelFilePath = "C:\\Users\\Administrator\\Desktop\\reward.xls";
        String jsonFilePath = "D:\\codeBase\\py-tools\\upload_content\\otoanswerimport\\reward_json.json";
        File file = new File(excelFilePath);
        String[][] result = O2oAnswer.getData(file, 1);
        List<Map<String, Object>> rewardMapList = new ArrayList<>();
        for (int i = 0; i < result.length; i++) {
            Map<String, Object> map = new HashMap<>();
            Map<String,Object> reward = new HashMap<>();
            for (int j = 0; j < result[i].length - 1; j++) {
                //前4列固定不变
                if (j < 3) {
                    map.put(rewardKey[j], result[i][j]);
                    //Pk套装
                } else{
                    reward.put(rewardKey[j],result[i][j]);
                }
            }
            map.put("reward", reward);
            rewardMapList.add(map);
        }
        String jsonStr = JsonUtils.toJsonPretty(rewardMapList);
        File jsonFile = new File(jsonFilePath);
        if (!jsonFile.exists()) {
            jsonFile.createNewFile();
        } else {
            jsonFile.delete();
            jsonFile.createNewFile();
        }
        OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(jsonFile), "UTF-8");
        BufferedWriter writer = new BufferedWriter(write);
        writer.write(jsonStr);
        writer.close();
    }
}
