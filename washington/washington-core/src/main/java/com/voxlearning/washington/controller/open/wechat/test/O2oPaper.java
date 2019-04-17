package com.voxlearning.washington.controller.open.wechat.test;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shiwei.liao on 2015/6/23.
 */
public class O2oPaper {
    public static void main(String[] args) throws IOException {
        String[] rewardKey = new String[]{"TYPE", "TITLE", "LEVEL", "PART_TITLE", "PART_ID", "IMG_NAME"};
        String excelFilePath = "C:\\Users\\Administrator\\Desktop\\paper.xls";
        String jsonFilePath = "D:\\codeBase\\py-tools\\upload_content\\otoanswerimport\\paper_json.json";
        File file = new File(excelFilePath);
        String[][] result = O2oAnswer.getData(file, 2);
        String lastType = "";
        String lastLevel = "";
        Integer laseIndex = -1;
        List<Map<String, Object>> paperMapList = new ArrayList<>();
        for (int i = 0; i < result.length; i++) {
            Map<String, Object> map;
            List<Map<String, Object>> partMapList;
            if ((StringUtils.equalsIgnoreCase(result[i][0],lastType)) &&(StringUtils.equalsIgnoreCase(result[i][2],lastLevel))) {
                map = paperMapList.get(laseIndex);
                partMapList = (List) map.get("PART_LIST");
            } else {
                map = new HashMap<>();
                partMapList = new ArrayList<>();
                laseIndex++;
                lastType = result[i][0];
                lastLevel = result[i][2];
                map.put("PART_LIST", partMapList);
                paperMapList.add(map);
                for (int j = 0; j < 3; j++) {
                    map.put(rewardKey[j], result[i][j]);
                }
            }
            Map<String, Object> mm = new HashMap<>();
            for (int ii = 3; ii < result[i].length - 1; ii++) {
                mm.put(rewardKey[ii], result[i][ii]);
            }
            partMapList.add(mm);
        }
        String jsonStr = JsonUtils.toJsonPretty(paperMapList);
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
