package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.ai.api.UploadVideoToOOS;
import com.voxlearning.utopia.service.ai.constant.ChipsActiveServiceType;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.impl.persistence.*;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderPaymentHistory;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import org.apache.http.message.BasicHeader;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guangqing
 * @since 2019/3/26
 */
@Named
@ExposeServices({
        @ExposeService(interfaceClass = UploadVideoToOOS.class, version = @ServiceVersion(version = "20190326"))
})
public class UploadVideoToOOSImpl implements UploadVideoToOOS {

    @Inject
    private ChipsKeywordVideoDao chipsKeywordVideoDao;
    @Inject
    private ChipsKeywordPrototypeDao chipsKeywordPrototypeDao;
    @Inject
    private ChipsEncourageVideoDao chipsEncourageVideoDao;

    @StorageClientLocation(storage = "17-pmc")
    private StorageClient storageClient;

    @Inject
    private AIUserVideoDao userVideoDao;

    @Inject
    private ChipsActiveServiceRecordDao chipsActiveServiceRecordDao;
    /**
     * 关键词点评视频
     * 文件名 #word#.mp4
     * @param path
     */
    public MapMessage updateKeyWordVideo(String path) {
        File file = new File(path);
        if (file.isFile()) {
            return MapMessage.errorMessage("not directory: " + path);
        }
        String suffix = "mp4";
        String pathPrefix = "ai-teacher/chips";
//        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
//            pathPrefix = pathPrefix + "/test/video/" + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
//        } else {
            pathPrefix = pathPrefix + "/pro/video/" + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
//        }
        File[] files = file.listFiles();
        int count = 0;
        for (File f : files) {
            String name = f.getName();
            System.out.println(name);
            String fileName = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + suffix;
            try {
                FileInputStream fileInputStream = new FileInputStream(f);
                String realName = storageClient.upload(fileInputStream, fileName, pathPrefix);
                String fileUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
                String keyword = name.substring(0, name.indexOf(".mp4"));
                System.out.println(fileUrl);
                ChipsKeywordVideo v = new ChipsKeywordVideo();
                v.setDisabled(false);
                v.setId(keyword.toLowerCase());
                v.setVideo(fileUrl);
                chipsKeywordVideoDao.upsert(v);
                count++;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return MapMessage.successMessage().add("成功处理数量",count);
    }

    /**
     * 单词列表的语态、时态等变形反查
     * @param path
     */
    public MapMessage updateKeyWordStem(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            return MapMessage.errorMessage("path is not file: " + path);
        }
        int count = 0;
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br=new BufferedReader(fr);
            String str;
            while ((str = br.readLine()) != null) {
                String[] split = str.split(" ");
//                System.out.println(split[0] + "-" + split[1]);
                ChipsKeywordPrototype p = new ChipsKeywordPrototype();
                p.setDisabled(false);
                p.setId(split[0].trim().toLowerCase());
                p.setPrototype(split[1].trim().toLowerCase());
                chipsKeywordPrototypeDao.upsert(p);
                count ++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return MapMessage.successMessage().add("成功处理条数", count);
    }

    private static Map<String, String> dayUnitTypeMap = new HashMap<>();

    static {
        dayUnitTypeMap.put("1", "role_play_unit");//角色扮演
        dayUnitTypeMap.put("2", "special_consolidation");//专项巩固
        dayUnitTypeMap.put("3", "dialogue_practice");//对话实战
        dayUnitTypeMap.put("4", "review_unit");//复习单元
        dayUnitTypeMap.put("5", "mock_test_unit_1");//模考单元1
        dayUnitTypeMap.put("6", "mock_test_unit_2");//模考单元2
        dayUnitTypeMap.put("10", "review_unit");//复习单元
        dayUnitTypeMap.put("11", "mock_test_unit_1");//模考单元1
        dayUnitTypeMap.put("12", "mock_test_unit_2");//模考单元2
    }

    /**
     * 鼓励语上传
     */
    public MapMessage updateEncourage(String path) {
        File file = new File(path);
        if (file.isFile()) {
            return MapMessage.errorMessage("path is not directory: " + path);
        }
        String suffix = "mp4";
        String pathPrefix = "ai-teacher/chips";
//        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
//            pathPrefix = pathPrefix + "/test/video/" + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
//        } else {
            pathPrefix = pathPrefix + "/pro/video/" + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
//        }
        int count = 0;
        File[] files = file.listFiles();
        for (File f : files) {
            String name = f.getName();
            System.out.println(name);
            String fileName = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + suffix;
            try {
                FileInputStream fileInputStream = new FileInputStream(f);
                String realName = storageClient.upload(fileInputStream, fileName, pathPrefix);
                String fileUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
                String keyword = name.substring(0, name.indexOf(".mp4"));
                String[] split = keyword.split("_");
                String ce = split[0];//外教:0 ; 中教 : 1
                String day = split[1];//第几天
                String level = split[2];//外教的level >=85; < 85;
                if ("0".equals(day)) {//全F的day用0，导入时判断掉
                    String[] arr = {"4","5","6"};
                    for (String d : arr) {
                        ChipsEncourageVideo v = new ChipsEncourageVideo();
                        v.setDisabled(false);
                        v.setType("1");
                        v.setUnitType(dayUnitTypeMap.get(d));
                        v.setLevel("N");
                        v.setVideo(fileUrl);
                        v.setId(ChipsEncourageVideo.genId("1", dayUnitTypeMap.get(d), "N"));
                        chipsEncourageVideoDao.upsert(v);
                        count++;
                    }
                } else {
                    String val = dayUnitTypeMap.get(day);
                    if (val == null) {
                        continue;
                    }
                    ChipsEncourageVideo v = new ChipsEncourageVideo();
                    v.setDisabled(false);
                    v.setType(ce);
                    v.setUnitType(val);
                    v.setLevel(level);
                    v.setVideo(fileUrl);
                    v.setId(ChipsEncourageVideo.genId(ce, val, level));
                    chipsEncourageVideoDao.upsert(v);
                    count++;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return MapMessage.successMessage().add("成功处理条数", count);
    }

    public MapMessage upsertChipsKeywordPrototype(String id, String prototype) {
        ChipsKeywordPrototype p = new ChipsKeywordPrototype();
        p.setId(id);
        p.setPrototype(prototype);
        p.setDisabled(false);
        chipsKeywordPrototypeDao.upsert(p);
        return MapMessage.successMessage();
    }

    public MapMessage upsertChipsKeywordVideo(String id, String video) {
        ChipsKeywordVideo p = new ChipsKeywordVideo();
        p.setId(id);
        p.setVideo(video);
        p.setDisabled(false);
        chipsKeywordVideoDao.upsert(p);
        return MapMessage.successMessage();
    }
    public MapMessage upsertChipsEncourageVideo(String id,String video, String type, String unitType, String level) {
        ChipsEncourageVideo p = new ChipsEncourageVideo();
        p.setId(id);
        p.setVideo(video);
        p.setType(type);
        p.setUnitType(unitType);
        p.setLevel(level);
        p.setDisabled(false);
        chipsEncourageVideoDao.upsert(p);
        return MapMessage.successMessage();
    }

    public MapMessage exportChipsEncourageVideo() {
        List<ChipsEncourageVideo> list = chipsEncourageVideoDao.query();
        for (ChipsEncourageVideo p : list) {
            chipsEncourageVideo(p);
        }
        return MapMessage.successMessage();
    }

    public MapMessage exportChipsKeywordVideo() {
        List<ChipsKeywordVideo> list = chipsKeywordVideoDao.query();
        for (ChipsKeywordVideo p : list) {
            chipsKeywordVideo(p);
        }
        return MapMessage.successMessage();
    }

    public MapMessage exportChipsKeywordPrototype() {
        List<ChipsKeywordPrototype> list = chipsKeywordPrototypeDao.query();
        for (ChipsKeywordPrototype p : list) {
            chipsKeywordPrototype(p);
        }
        return MapMessage.successMessage();
    }

    private void chipsEncourageVideo(ChipsEncourageVideo p) {
        //查学校
        String requestUrl = "http://10.6.15.122:1889/?group=alps-hydra-staging&service=com.voxlearning.utopia.service.ai.api.UploadVideoToOOS&method=upsertChipsEncourageVideo&version=20190326";
        String requestParams = "{\"paramValues\":[\""+ p.getId() + "\",\"" + p.getVideo() + "\",\"" + p.getType() +  "\",\"" + p.getUnitType() + "\",\"" + p.getLevel() + "\"]}";
        System.out.println(requestParams);
        String response = HttpRequestExecutor.defaultInstance()
                .post(requestUrl)
                .headers(new BasicHeader("Content-Type", "application/json"))
                .json(requestParams)
                .execute().getResponseString();
//        return StringUtils.isNotBlank(response) ? JsonUtils.fromJsonToList(response, Long.class).size() : 0;
    }

    private void chipsKeywordVideo(ChipsKeywordVideo p) {
        //查学校
        String requestUrl = "http://10.6.15.122:1889/?group=alps-hydra-staging&service=com.voxlearning.utopia.service.ai.api.UploadVideoToOOS&method=upsertChipsKeywordVideo&version=20190326";
        String requestParams = "{\"paramValues\":[\""+ p.getId() + "\",\"" + p.getVideo()  + "\"]}";
        System.out.println(requestParams);
        String response = HttpRequestExecutor.defaultInstance()
                .post(requestUrl)
                .headers(new BasicHeader("Content-Type", "application/json"))
                .json(requestParams)
                .execute().getResponseString();
//        return StringUtils.isNotBlank(response) ? JsonUtils.fromJsonToList(response, Long.class).size() : 0;
    }
    private void chipsKeywordPrototype(ChipsKeywordPrototype p) {
        //查学校
        String requestUrl = "http://10.6.15.122:1889/?group=alps-hydra-staging&service=com.voxlearning.utopia.service.ai.api.UploadVideoToOOS&method=upsertChipsKeywordPrototype&version=20190326";
        String requestParams = "{\"paramValues\":[\""+ p.getId() + "\",\"" + p.getPrototype() + "\"]}";
        System.out.println(requestParams);
        String response = HttpRequestExecutor.defaultInstance()
                .post(requestUrl)
                .headers(new BasicHeader("Content-Type", "application/json"))
                .json(requestParams)
                .execute().getResponseString();
//        return StringUtils.isNotBlank(response) ? JsonUtils.fromJsonToList(response, Long.class).size() : 0;
    }

    @Inject
    private ChipsEnglishClassUserRefPersistence userRefPersistence;
    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    private ChipsEnglishClassPersistence classPersistence;
    @Inject
    private ChipsActiveServiceRecordDao recordDao;

    public  Map<Long, Object> paidAmount(Long clazzId, Double lessThan) {
        List<ChipsEnglishClassUserRef> userRefList = userRefPersistence.loadByClassId(clazzId);
        ChipsEnglishClass clazz = classPersistence.load(clazzId);
        Map<Long, Object> paidMap = new HashMap<>();
        BigDecimal max = BigDecimal.valueOf(lessThan);
        for (ChipsEnglishClassUserRef userRef : userRefList) {
            List<UserOrder> userOrders = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), userRef.getUserId());
            UserOrder userOrder = userOrders.stream().filter(e -> e.getProductId().equals(clazz.getProductId())).findFirst().orElse(null);
            if (userOrder == null) {
                paidMap.put(userRef.getUserId(), 0);
                continue;
            }
            List<UserOrderPaymentHistory> paymentHistories = userOrderLoaderClient.loadUserOrderPaymentHistoryList(userRef.getUserId())
                    .stream()
                    .filter(e -> StringUtils.equals(e.getOrderId(), userOrder.getId()))
                    .collect(Collectors.toList());

            UserOrderPaymentHistory paymentHistory = paymentHistories
                    .stream()
                    .filter(o -> o.getPaymentStatus() == PaymentStatus.Paid)
                    .findFirst().orElse(null);
            if (paymentHistory == null) {
                paidMap.put(userRef.getUserId(), 0);
                continue;
            }
            if (paymentHistory.getPayAmount().compareTo(max) < 0) {
                paidMap.put(userRef.getUserId(), paymentHistory.getPayAmount());
            }
        }

        return paidMap;
    }

    @Override
    public MapMessage statNoRemarkVideo(Long clazzId, String unitId) {
        List<ChipsEnglishClassUserRef> userRefList = userRefPersistence.loadByClassId(clazzId);
        if (CollectionUtils.isEmpty(userRefList)) {
            return MapMessage.successMessage();
        }
        List<String> idList = userRefList.stream().map(e -> "SERVICE-" + clazzId + "-" + e.getUserId() + "-" + unitId).collect(Collectors.toList());
        Map<String, ChipsActiveServiceRecord> loads = recordDao.loads(idList);
        List<ChipsActiveServiceRecord> collect = idList.stream().filter(e -> loads.get(e) != null).map(e -> loads.get(e)).collect(Collectors.toList());
        List<ChipsActiveServiceRecord> notUserVideoIdList = collect.stream().filter(e -> StringUtils.isBlank(e.getVideoUrl())).collect(Collectors.toList());
        List<String> temp = notUserVideoIdList.stream().map(e -> toString(e)).collect(Collectors.toList());
        MapMessage message = MapMessage.successMessage();
        message.add("userCount", userRefList.size());
        message.add("notRemarkCount", temp.size());
        message.add("notRemarkUser", temp);
        return message;
    }

    private String toString(ChipsActiveServiceRecord record) {
        record.getUserVideoId();
        record.getExamineStatus();
        record.getCreateDate();
        StringBuffer sb = new StringBuffer();
        sb.append("userId:").append(record.getUserId());
        sb.append(";userVideoId:").append(record.getUserVideoId());
        sb.append(";examineStatus:").append(record.getExamineStatus());
        sb.append(";createDate:").append(record.getCreateDate());
        return sb.toString();

    }

    @Override
    public MapMessage statNotFullRemarkVideo(Long clazzId, String unitId) {
        List<ChipsEnglishClassUserRef> userRefList = userRefPersistence.loadByClassId(clazzId);
        if (CollectionUtils.isEmpty(userRefList)) {
            return MapMessage.successMessage();
        }
        List<String> idList = userRefList.stream().map(e -> "SERVICE-" + clazzId + "-" + e.getUserId() + "-" + unitId).collect(Collectors.toList());
        Map<String, ChipsActiveServiceRecord> loads = recordDao.loads(idList);
        List<String> collect = idList.stream().filter(e -> loads.get(e) != null).collect(Collectors.toList());
        Set<Long> set = new HashSet<>();
        for (String s : collect) {
            String[] split = s.split("-");
            long userId = Long.parseLong(split[2]);
            List<AIUserVideo> list = userVideoDao.loadByUserIdAndUnitId(userId, split[3]);
            if (CollectionUtils.isEmpty(list)) {
                set.add(userId);
            }
        }

        MapMessage message = MapMessage.successMessage();
        message.add("userCount", userRefList.size());
        message.add("notUserVideoCount", set.size());
        message.add("notUserVideoUser", set);
        return message;
    }

    public MapMessage removeKeywordVideo(String keyword) {
        chipsKeywordVideoDao.remove(keyword);
        return MapMessage.successMessage();
    }

    public MapMessage removeKeywordPrototype(String keyword) {
        chipsKeywordPrototypeDao.remove(keyword);
        return MapMessage.successMessage();
    }

    public MapMessage removeEncourageVideo(String id) {
        chipsEncourageVideoDao.remove(id);
        return MapMessage.successMessage();
    }

    public MapMessage finishButNoUserVideo(Long clazzId, String unitId) {
        List<ChipsActiveServiceRecord> allRecordList = chipsActiveServiceRecordDao.loadByClassId(ChipsActiveServiceType.SERVICE, clazzId);
        List<ChipsActiveServiceRecord> unitRecordList = allRecordList.stream().filter(e -> e.getUnitId().equals(unitId)).collect(Collectors.toList());
        List<ChipsActiveServiceRecord> notUserVideoList = unitRecordList.stream().filter(e -> CollectionUtils.isEmpty(userVideoDao.loadByUserIdAndUnitId(e.getUserId(), e.getUnitId()))).collect(Collectors.toList());
        MapMessage message = MapMessage.successMessage();
        message.add("finishCount", unitRecordList.size());
        message.add("noUserVideoCount", notUserVideoList.size());
        message.add("noUserVideoUser", notUserVideoList.stream().map(e-> e.getUserId()).collect(Collectors.toList()));
        return message;
    }
}
