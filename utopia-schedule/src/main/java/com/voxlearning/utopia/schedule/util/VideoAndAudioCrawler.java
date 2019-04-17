package com.voxlearning.utopia.schedule.util;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.schedule.entity.AudioCrawlerResult;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by jiang wei on 2017/7/10.
 */
@Slf4j
public class VideoAndAudioCrawler {

    private static String json_url = "http://www.ximalaya.com/tracks/";

    //随机UA
    public static String[] user_agent_list = {
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/22.0.1207.1 Safari/537.1",
            "Mozilla/5.0 (X11; CrOS i686 2268.111.0) AppleWebKit/536.11 (KHTML, like Gecko) Chrome/20.0.1132.57 Safari/536.11",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1092.0 Safari/536.6",
            "Mozilla/5.0 (Windows NT 6.2) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1090.0 Safari/536.6",
            "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/19.77.34.5 Safari/537.1",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.9 Safari/536.5",
            "Mozilla/5.0 (Windows NT 6.0) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.36 Safari/536.5",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1063.0 Safari/536.3",
            "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1063.0 Safari/536.3",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_0) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1063.0 Safari/536.3",
            "Mozilla/5.0 (Windows NT 6.2) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1062.0 Safari/536.3",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1062.0 Safari/536.3",
            "Mozilla/5.0 (Windows NT 6.2) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1061.1 Safari/536.3",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1061.1 Safari/536.3",
            "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1061.1 Safari/536.3",
            "Mozilla/5.0 (Windows NT 6.2) AppleWebKit/536.3 (KHTML, like Gecko) Chrome/19.0.1061.0 Safari/536.3",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.24 (KHTML, like Gecko) Chrome/19.0.1055.1 Safari/535.24",
            "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/535.24 (KHTML, like Gecko) Chrome/19.0.1055.1 Safari/535.24"
    };

    public static ArrayList<String> radomUA = new ArrayList<>(Arrays.asList(user_agent_list));


    public static List<String> generateXiMaLaYaUrl(String list_url) {
        if (StringUtils.isBlank(list_url)) {
            return Collections.emptyList();
        }
        List<String> soundIds = new ArrayList<>();
        try {
            Document document = Jsoup.connect(list_url).get();
            Elements album_soundlist = document.body().getElementsByClass("album_soundlist");
            if (CollectionUtils.isNotEmpty(album_soundlist)) {
                album_soundlist.forEach(e -> {
                    Elements li = e.getElementsByTag("li");
                    if (CollectionUtils.isNotEmpty(li)) {
                        li.forEach(o -> {
                            soundIds.add(o.attr("sound_id"));
                        });
                    }
                });
            }

        } catch (IOException e) {
            log.warn("fail parse ximalaya_url");
        }
        return soundIds;
    }


    public static List<AudioCrawlerResult> downloadAnduploadXiMaLaYa(List<String> soundIds) {
        if (CollectionUtils.isEmpty(soundIds)) {
            return Collections.emptyList();
        }
        Collections.shuffle(radomUA);
        Map<String, String> headers = new HashMap<>();
        headers.put("Cache-Control", "no-cache");
        headers.put("Accept", "application/json,text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Encoding", "gzip");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8");
        List<AudioCrawlerResult> returnList = new CopyOnWriteArrayList<>();
        soundIds.forEach(e -> {
            headers.put("User-Agent", radomUA.get(0));
            AudioCrawlerResult audioCrawlerResult = new AudioCrawlerResult();
            String real_url = json_url + e + ".json";
            AlpsHttpResponse execute = HttpRequestExecutor.defaultInstance().get(real_url).contentType("application/json;charset=utf-8").headers(headers).execute();
            String result_json = execute.getResponseString();
            if (StringUtils.isBlank(result_json)) {
                return;
            }
            Map<String, String> resultMap = JsonUtils.fromJsonToMapStringString(result_json);
            String play_path = resultMap.get("play_path");
            String play_path_32 = resultMap.get("play_path_32");
            String play_path_64 = resultMap.get("play_path_64");
//            MapMessage mapMessage = null;
//            try {
//                mapMessage = uploadFileTo17contentVideo(play_path, "class");
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//            if (mapMessage.isSuccess()) {
//                String fileUrl = SafeConverter.toString(mapMessage.get("fileUrl"));
            List<String> urlList = new ArrayList<>();
            urlList.add(play_path);
            urlList.add(play_path_32);
            urlList.add(play_path_64);
            audioCrawlerResult.setId(resultMap.get("id"));
            audioCrawlerResult.setTitle(resultMap.get("title"));
            audioCrawlerResult.setReal_url(urlList);
            audioCrawlerResult.setReal_url_32(play_path_32);
            audioCrawlerResult.setReal_url_64(play_path_64);
//                returnMap.put("file_url", fileUrl);
//            }
            returnList.add(audioCrawlerResult);

        });

        return returnList;
    }

    public static AudioCrawlerResult downloadByJson(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Cache-Control", "no-cache");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Encoding", "gzip");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8");
        AudioCrawlerResult audioCrawlerResult = new AudioCrawlerResult();
        AlpsHttpResponse execute = HttpRequestExecutor.defaultInstance().get(url).contentType("application/json;charset=utf-8").headers(headers).execute();
        String result_json = execute.getResponseString();
        if (StringUtils.isBlank(result_json)) {
            return null;
        }
        Map<String, String> resultMap = JsonUtils.fromJsonToMapStringString(result_json);
        String play_path = resultMap.get("play_path");
        List<String> urlList = new ArrayList<>();
        urlList.add(play_path);
        audioCrawlerResult.setId(resultMap.get("id"));
        audioCrawlerResult.setTitle(resultMap.get("title"));
        audioCrawlerResult.setReal_url(urlList);
        return audioCrawlerResult;
    }

//    /*
//     * 随机生成国内IP地址
//     */
//    public static String getRandomIp(){
//
//        //ip范围
//        int[][] range = {{607649792,608174079},//36.56.0.0-36.63.255.255
//                {1038614528,1039007743},//61.232.0.0-61.237.255.255
//                {1783627776,1784676351},//106.80.0.0-106.95.255.255
//                {2035023872,2035154943},//121.76.0.0-121.77.255.255
//                {2078801920,2079064063},//123.232.0.0-123.235.255.255
//                {-1950089216,-1948778497},//139.196.0.0-139.215.255.255
//                {-1425539072,-1425014785},//171.8.0.0-171.15.255.255
//                {-1236271104,-1235419137},//182.80.0.0-182.92.255.255
//                {-770113536,-768606209},//210.25.0.0-210.47.255.255
//                {-569376768,-564133889}, //222.16.0.0-222.95.255.255
//        };
//
//        Random rdint = new Random();
//        int index = rdint.nextInt(10);
//        return num2ip(range[index][0]+new Random().nextInt(range[index][1]-range[index][0]));
//    }
//
//    /*
//         * 将十进制转换成ip地址
//         */
//    public static String num2ip(int ip) {
//        int [] b=new int[4] ;
//        String x = "";
//
//        b[0] = (int)((ip >> 24) & 0xff);
//        b[1] = (int)((ip >> 16) & 0xff);
//        b[2] = (int)((ip >> 8) & 0xff);
//        b[3] = (int)(ip & 0xff);
//        x=Integer.toString(b[0])+"."+Integer.toString(b[1])+"."+Integer.toString(b[2])+"."+Integer.toString(b[3]);
//
//        return x;
//    }
}
