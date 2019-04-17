package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.Assertions;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.ai.api.TobbitMathScoreService;
import com.voxlearning.utopia.service.ai.api.TobbitMathService;
import com.voxlearning.utopia.service.ai.cache.manager.TobbitCacheManager;
import com.voxlearning.utopia.service.ai.component.KsyunComponent;
import com.voxlearning.utopia.service.ai.component.OcrComponent;
import com.voxlearning.utopia.service.ai.constant.TobbitScoreType;
import com.voxlearning.utopia.service.ai.data.OcrImageDto;
import com.voxlearning.utopia.service.ai.entity.TobbitMathAuthUser;
import com.voxlearning.utopia.service.ai.entity.TobbitMathHistory;
import com.voxlearning.utopia.service.ai.entity.TobbitMathShareHistory;
import com.voxlearning.utopia.service.ai.impl.persistence.TobbitMathAuthUserDao;
import com.voxlearning.utopia.service.ai.impl.persistence.TobbitMathHistoryDao;
import com.voxlearning.utopia.service.ai.impl.persistence.TobbitMathShareHistoryDao;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import com.voxlearning.utopia.service.wechat.api.entities.UserMiniProgramCheck;
import com.voxlearning.utopia.service.wechat.client.MiniProgramServiceClient;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = TobbitMathService.class)
@Slf4j
public class TobbitMathServiceImpl implements TobbitMathService {

    @Inject
    private TobbitCacheManager tobbitCacheManager;

    @Inject
    private OcrComponent ocrComponent;

    @Inject
    private KsyunComponent ksyunComponent;

    @Inject
    private TobbitMathHistoryDao tobbitMathHistoryDao;

    @Inject
    private TobbitMathShareHistoryDao tobbitMathShareHistoryDao;

    @Inject
    private UserLoaderClient userLoaderClient;

    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @Inject
    private WechatLoaderClient wechatLoaderClient;
    @Inject
    private MiniProgramServiceClient miniProgramServiceClient;

    @Inject
    private TobbitMathScoreService tobbitMathScoreService;

    @Inject
    private TobbitMathAuthUserDao tobbitMathAuthUserDao;

    @AlpsQueueProducer(queue = "honeycomb.thirdparty.course.topic", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer fenchaoProducer;

    @AlpsQueueProducer(config = "Deadpool", queue = "utopia.tobbit.math.mp.user.score", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer esProducer;

    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1024));


    private static final List<String> DAYS_AGO = Arrays.asList("今天", "昨天", "2天前", "3天前");
    private static final String DATE_PATTERN = "yyyy-MM-dd";


    @Override
    public MapMessage identify(String openId, Long uid, byte[] bytes, String sys) {


        if (bytes.length > 0x2fffff) {
            return MapMessage.errorMessage("图片大小超过3M了，请减小图片大小");
        }

        String md5 = DigestUtils.md5Hex(bytes);
        String json;

        OcrImageDto dto;

        if (!tobbitCacheManager.existOcrCache(md5)) {
            MapMessage result = ocrComponent.mathOcrApi(bytes, openId, uid, sys);
            if (!result.isSuccess()) {
                return result;
            }
            dto = (OcrImageDto) result.get("data");
            boolean pass = ksyunComponent.passImage(Collections.singletonList(dto.getImg_url()));
            if (!pass) {
                dto.setBlocked(true);
            }
            // update cache
            json = JsonUtils.toJson(dto);
            tobbitCacheManager.setOcrCache(md5, json);
        } else {
            json = tobbitCacheManager.getOcrCache(md5);
            dto = JsonUtils.fromJson(json, OcrImageDto.class);
            if (dto == null) {
                tobbitCacheManager.expireOcrCache(md5);
            }
        }

        if (dto == null) {
            return MapMessage.errorMessage("图片好像有些问题，再试试吧");
        }

        if (dto.blocked) {
            return MapMessage.errorMessage("图片包含敏感信息，请检查");
        }

        String qid = "";

        // IF openId
        if (StringUtils.isNotBlank(openId)) {
            qid = _saveHistory(openId, uid, dto);
        }
        dto.setPersist(true);
        dto.setQid(qid);

        // hidden info
        dto.setOrigin_json(null);


        MapMessage mm = MapMessage.successMessage();
        // add score
        if (uid != null) {
            boolean s = tobbitMathScoreService.addScore(uid, TobbitScoreType.CHECK);
            if (s) {
                mm.add("score", TobbitScoreType.CHECK.json());
            }
            // callback
            spreadActiveUser(openId, uid);
        }


        return mm.add("data", dto);
    }


    @Override
    public MapMessage atBot(Long uid, List<String> latex) {

        if (latex == null || latex.isEmpty()) {
            return MapMessage.successMessage().add("data", Collections.emptyList());
        }


        StringBuilder sb = new StringBuilder();
        latex.forEach(sb::append);

        String md5 = DigestUtils.md5Hex(sb.toString());
        String json;

        if (!tobbitCacheManager.existBotCache(md5)) {
            MapMessage result = ocrComponent.mathBotApi(uid, latex);
            if (!result.isSuccess()) {
                return MapMessage.successMessage().add("data", Collections.emptyList());
            }
            // update cache
            json = result.getInfo();
            tobbitCacheManager.setBotCache(md5, json);
        } else {
            json = tobbitCacheManager.getBotCache(md5);
        }
        return MapMessage.successMessage().add("data", JsonUtils.fromJson(json));
    }

    @Override
    public MapMessage load(String id) {
        Assertions.notBlank(id, "id must not be empty");
        TobbitMathHistory po = tobbitMathHistoryDao.load(id);
        if (po == null) {
            return MapMessage.errorMessage("未找到");
        }

        // hidden info
        po.setOpenId(null);
        po.setUid(null);
        po.setDisabled(null);
        po.setOriginJson(null);
        return MapMessage.successMessage().add("data", po);
    }


    @Override
    public MapMessage clean(String openId, Long uid) {
        Assertions.notNull(uid, "找不到用户信息!");
        Assertions.notBlank(openId, "找不到用户信" +
                "息!");
        tobbitMathHistoryDao.disable(openId, uid);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage loadByUid(String openId, Long uid) {

        if (StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage("找不到用户信息!");
        }

        List list;
        String key = openId + "_";
        if (uid != null) {
            key = key + uid;
        }
        if (!tobbitCacheManager.existUserHistoryCache(key)) {

            list = _fetchUserHistory(openId, uid);
            if (list.size() > 0) {
                tobbitCacheManager.setUserHistoryCache(key, JsonUtils.toJson(list));
            }
        } else {
            list = JsonUtils.fromJsonToList(tobbitCacheManager.getUserHistoryCache(key), Map.class);
        }

        return MapMessage.successMessage().add("data", list);

    }

    @Override
    public MapMessage loadUserInfo(String openId, Long uid) {
        Assertions.notNull(uid, "uid must not be empty");


        UserMiniProgramCheck check = miniProgramServiceClient.getUserMiniProgramCheckService().loadByUid(uid, MiniProgramType.TOBBIT);

        int days = Optional.ofNullable(check).map(UserMiniProgramCheck::getChecked).orElse(1);

        String phone = sensitiveUserDataServiceClient.loadUserMobileObscured(uid);
        long count = tobbitMathHistoryDao.count(openId, uid, true);

        long totalScore = tobbitMathScoreService.total(uid);

        Map<String, Object> map = new HashMap<>();
        map.put("phone", phone);
        map.put("days", days);
        map.put("count", count);
        map.put("totalScore", totalScore);
        return MapMessage.successMessage().add("data", map);
    }

    @Override
    public MapMessage share(String openId, Long uid, String qid) {
        Assertions.notBlank(openId);

        TobbitMathShareHistory po = new TobbitMathShareHistory();
        po.setOpenId(openId);
        po.setUid(uid);
        po.setQid(qid);
        tobbitMathShareHistoryDao.insert(po);

        return MapMessage.successMessage();
    }

    @Override
    public boolean isNewUser(String openId) {
        TobbitMathAuthUser po = tobbitMathAuthUserDao.loadByOpenId(openId);
        return po == null;
    }


    @Override
    public void appendAuthUser(String openId, String name, String avatar) {
        if (StringUtils.isBlank(openId)) {
            return;
        }


        TobbitMathAuthUser po = tobbitMathAuthUserDao.loadByOpenId(openId);

        if (po != null) {
            // update name avatar
            if (StringUtils.isNoneBlank(name, avatar)) {
                po.setName(name);
                po.setAvatar(avatar);
                tobbitMathAuthUserDao.upsert(po);
            }

        } else {
            po = new TobbitMathAuthUser();
            po.setOpenId(openId);
            po.setAvatar(avatar);
            po.setName(name);

            tobbitMathAuthUserDao.save(po);
        }
    }



    @Override
    public boolean hasUser(Long uid) {
        UserMiniProgramCheck check = miniProgramServiceClient.getUserMiniProgramCheckService().loadByUid(uid, MiniProgramType.TOBBIT);
        return check != null;

    }



    @Override
    public boolean spreadRegUser(String openId, Long uid,boolean outOfSystem) {

        if (StringUtils.isBlank(openId) || uid == null) {
            return false;
        }

        // Redundant var for read code
        boolean isValid= outOfSystem;

        boolean exist = tobbitCacheManager.existSP(openId);
        if (!exist) {
            return false;
        }



        // ref;source
        String sp = tobbitCacheManager.getSP(openId);

        String[] arr = sp.split(";");
        if (sp.length() < 2) {
            return false;
        }

        String ref = arr[0];
        String source = arr[1];

        // Wiki: http://wiki.17zuoye.net/pages/viewpage.action?pageId=46007314
        Map<String, Object> map = new HashMap<>();
        map.put("messageType", "order");
        map.put("orderReferer", source);
        map.put("userId", uid);
        map.put("activateUserId", uid);
        map.put("courseId", "fa1a05e4-c618-4e5b-955c-ec93051ad4fe");
        map.put("isValid", isValid);

        String json = JsonUtils.toJson(map);
        // send kafka
        sendQueenMessage(json);

        // mark effect sp if isValid true
        if (isValid) {
            tobbitCacheManager.markSpEffect(openId);
        }

        syncSPLog(uid, openId, ref, source,isValid);
        return true;
    }


    private void syncSPLog(long uid, String openId, String ref,String source,boolean isValid) {

//        module: m_pQd5XuaxTB
//        op: events_point_sp
//        s0: openId
//        s1: uid
//        s2: ref
//        s3: source
//        s4: isValid

        Map<String, Object> map = new HashMap<>();
        map.put("module", "m_pQd5XuaxTB");
        map.put("op", "events_point_sp");
        map.put("s0", openId);
        map.put("s1", uid);
        map.put("s2", ref);
        map.put("s3",source);
        map.put("s4",isValid);

        final String json = JsonUtils.toJson(map);

        try {
            EXECUTOR_SERVICE.submit(() -> {
                Message message = Message.newMessage();
                message.withPlainTextBody(json);
                esProducer.produce(message);
            });
        }catch (Exception e) {
            log.error("Tobbit math spread user log queen cause error: {}", e.getMessage());
        }


    }


    @Override
    public boolean markSpUser(String openId, String sp) {

        if (StringUtils.isAnyBlank(openId, sp)) {
            return false;
        }

        boolean isNew = isNewUser(openId);
        // old user return direct
        if (!isNew) {
            return false;
        }

        tobbitCacheManager.markSp(openId, sp);
        return true;

    }

    private boolean spreadActiveUser(String openId, Long uid) {

        // ref;source
        String sp = tobbitCacheManager.getEffectSp(openId);

        String[] arr = sp.split(";");
        if (sp.length() < 2) {
            return false;
        }

        String source = arr[1];

        Map<String, Object> map = new HashMap<>();
        map.put("messageType", "course");
        map.put("activateUserId", uid);
        map.put("lessonId", DateUtils.dateToString(new Date(), "yyyyMMdd"));
        map.put("courseId", "fa1a05e4-c618-4e5b-955c-ec93051ad4fe");

        String json = JsonUtils.toJson(map);

        // send kafka
        sendQueenMessage(json);
        return true;


    }

    private void sendQueenMessage(String json) {
        try {
            EXECUTOR_SERVICE.submit(() -> {
                Message message = Message.newMessage();
                message.withPlainTextBody(json);
                fenchaoProducer.produce(message);
            });
        } catch (Exception e) {
            log.error("Send queen[fengchao] message error: {}", e.getMessage(), e);
        }

    }
    private List<Map<String, Object>> _fetchUserHistory(String openId, Long uid) {

        // Only 3 records if not login
        int limit = 20, nlimit = 3;

        List<TobbitMathHistory> mergeList;
        List<TobbitMathHistory> list = tobbitMathHistoryDao.loadByOpenIdNoUid(openId, limit);


        if (uid == null) {
            mergeList = list.stream().limit(nlimit).collect(Collectors.toList());
        } else {
            List<TobbitMathHistory> ulist = tobbitMathHistoryDao.loadByUid(uid, limit);
            ulist.addAll(list);
            mergeList = ulist.stream().sorted(Comparator.comparing(TobbitMathHistory::getCreateTime).reversed()).limit(limit).collect(Collectors.toList());
        }


        Map<String, List<TobbitMathHistory>> groupMap = new HashMap<>();
        // Grouping
        mergeList.forEach(x -> {
            // Hidden info
            x.setJson(null);
            x.setOriginJson(null);
            x.setUid(null);
            x.setOpenId(null);
            x.setVersion(null);
            x.setDisabled(null);
            String localdate = DateUtils.dateToString(x.getCreateTime(), DATE_PATTERN);
            groupMap.computeIfAbsent(localdate, k -> new ArrayList<>());
            groupMap.get(localdate).add(x);
        });


        List<String> keys = new ArrayList<>(groupMap.keySet());
        keys.sort(Comparator.comparing(LocalDate::parse));
        Collections.reverse(keys);


        List<Map<String, Object>> sortedList = new ArrayList<>();

        LocalDate now = LocalDate.now();
        keys.forEach(x -> {
            LocalDate start = LocalDate.parse(x);
            int days = (int) start.until(now, ChronoUnit.DAYS);

            Map<String, Object> map = new HashMap<>();
            String tname = x;
            if (days < 4) {
                tname = DAYS_AGO.get(days);
            }
            map.put("time", tname);
            map.put("list", groupMap.get(x));
            sortedList.add(map);
        });

        return sortedList;
    }


    private String _saveHistory(String openId, Long uid, OcrImageDto dto) {

        if (dto == null) {
            return "";
        }

        String img = dto.img_url;

        List<OcrImageDto.OcrForms> forms = dto.forms == null ? Collections.emptyList() : dto.forms;

        // 0为错，1为对，2为口算无法批改，3为教辅没有填写答案，4为教辅无法批改
        long errorCount = forms.stream().filter(x -> 0 == SafeConverter.toInt(x.judge, -1)).count();
        long successCount = forms.stream().filter(x -> 1 == SafeConverter.toInt(x.judge, -1)).count();

        String originJson = dto.getOrigin_json();
        // reset info
        dto.origin_json = null;
        String json = JsonUtils.toJson(dto);

        // save record
        TobbitMathHistory po = new TobbitMathHistory();
        po.setUid(uid);
        po.setOpenId(openId);
        po.setImg(img);
        po.setErrorCount((int) errorCount);
        po.setTotalCount((int) (successCount + errorCount));
        po.setJson(json);
        po.setOriginJson(originJson);
        tobbitMathHistoryDao.save(po);
        return po.getId();
    }

}
