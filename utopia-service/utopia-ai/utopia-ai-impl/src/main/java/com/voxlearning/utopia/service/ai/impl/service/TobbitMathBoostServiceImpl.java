package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringEscapeUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.service.ai.api.TobbitMathBoostService;
import com.voxlearning.utopia.service.ai.api.TobbitMathService;
import com.voxlearning.utopia.service.ai.cache.manager.TobbitBoostCacheManager;
import com.voxlearning.utopia.service.ai.entity.TobbitMathAddress;
import com.voxlearning.utopia.service.ai.entity.TobbitMathBoostBill;
import com.voxlearning.utopia.service.ai.entity.TobbitMathBoostHistory;
import com.voxlearning.utopia.service.ai.entity.TobbitMathOralBook;
import com.voxlearning.utopia.service.ai.impl.persistence.TobbitMathAddressDao;
import com.voxlearning.utopia.service.ai.impl.persistence.TobbitMathBoostBillDao;
import com.voxlearning.utopia.service.ai.impl.persistence.TobbitMathBoostHistoryDao;
import com.voxlearning.utopia.service.ai.impl.persistence.TobbitMathOralBookDao;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramApi;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import com.voxlearning.utopia.service.wechat.client.MiniProgramServiceClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = TobbitMathBoostService.class)
@Slf4j
public class TobbitMathBoostServiceImpl implements TobbitMathBoostService {

    @Inject
    private TobbitBoostCacheManager tobbitBoostCacheManager;

    @Inject
    private TobbitMathBoostBillDao tobbitMathBoostBillDao;
    @Inject
    private TobbitMathBoostHistoryDao tobbitMathBoostHistoryDao;
    @Inject
    private TobbitMathAddressDao tobbitMathAddressDao;
    @Inject
    private TobbitMathOralBookDao tobbitMathOralBookDao;

    @Inject
    private TobbitMathService tobbitMathService;

    @Inject
    private MiniProgramServiceClient miniProgramServiceClient;

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private static final String MESSAGE_BOOST_REMIND_TEMPLATE_ID = "FDv8DrpeqaA4WfWjQto9__YxlF3cDNNc6rLKYvFCJNc";


    @Override
    public boolean isOnline() {
        return tobbitBoostCacheManager.isOpen();
    }


    @Override
    public MapMessage status(String openId, Long uid, String bid) {


        if (StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage("没有权限");
        }


        int pcount = tobbitBoostCacheManager.boostPCountLimit();
        long timeLimit = tobbitBoostCacheManager.boostTimeLimitSeconds();
        String endDate = tobbitBoostCacheManager.boostEndDate();
        long timeRemain = timeLimit;


        int status = 0; // -1:closed, 0:normal, 1:in-progress, 2:joined, 3: expired, 4: finished
        boolean isNew = false;

        List boostList = new ArrayList();
        String bookName = "";
        String bookCover = "";
        String bookElec = "";
        String boostId = "";


        //~ CHECK
        // Open?
        if (!tobbitBoostCacheManager.isOpen()) {
            status = -1; // closed
        }


        String proessId = openId;

        TobbitMathBoostBill bill;
        if (StringUtils.isNotBlank(bid)) {
            bill = tobbitMathBoostBillDao.load(bid);
            proessId = bill.getOpenId();
        } else {
            bill = loadBoostBill(openId);
        }

        // In progress boost?
        boolean inProgress = tobbitBoostCacheManager.isInProgress(proessId);

        if (inProgress) {
            status = 1;
        }

        if (bill != null) {
            if (inProgress) {
                _checkAppendBoostDone(bill);
            }
            boostId = bill.getId();
            int sta = bill.getStatus();
            if (sta > 0) {
                if (openId.equals(bill.getOpenId())) {
                    status = 2; // joined
                } else {
                    status = 4; // finished
                }

            } else {

                if (!inProgress) {
                    status = 3; // expired
                }
            }

            List<TobbitMathBoostHistory> list = tobbitMathBoostHistoryDao.loadByBoostId(bill.getId(), pcount);

            if (list.size() > 0) {
                list.forEach(x -> {
                    x.setVersion(null);
                    x.setId(null);
                    x.setOpenId(null);
                    x.setBid(null);
                });
            }

            boostList = list;

            TobbitMathOralBook book = tobbitMathOralBookDao.load(bill.getBid());
            if (null != book) {
                bookName = String.format("《托比口算本》%s", book.getGradeName());
                bookCover = book.getCover();
                bookElec = book.getElec();
            }

            timeRemain = tobbitBoostCacheManager.getBoostTimeRemain(bill.getId());

        }

        if (tobbitBoostCacheManager.allowOldUser()) {
            isNew = true;
        } else {
            isNew = tobbitMathService.isNewUser(openId);
        }

        Map<String, Object> map = new HashMap<>();

        map.put("stat", status);
        map.put("isNew", isNew);
        map.put("pcount", pcount);
        map.put("timeLimit", timeLimit);
        map.put("endDate", endDate);
        map.put("bookName", bookName);
        map.put("bookCover", bookCover);
        map.put("bookElec", bookElec);
        map.put("boostId", boostId);
        map.put("timeRemain", timeRemain);
        map.put("boostList", boostList);

        return MapMessage.successMessage().add("data", map);
    }


    @Override
    public MapMessage appendBoost(String bid, String openId, String name, String avatar) {

        if (StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage("未授权");
        }
        if (StringUtils.isBlank(bid)) {
            return MapMessage.errorMessage("无效事件");
        }

        //~ CHECK
        // Open?
        if (!tobbitBoostCacheManager.isOpen()) {
            return MapMessage.errorMessage("活动已结束");
        }
        // Time limit
        if (tobbitBoostCacheManager.getBoostTimeRemain(bid) < 0) {
            return MapMessage.errorMessage("助力已过期");
        }
        // Is new User
        if (!tobbitBoostCacheManager.allowOldUser()) {
            if (!tobbitMathService.isNewUser(openId)) {
                return MapMessage.errorMessage("您是老朋友啦，无法参与助力");
            }
        }

        //~ Process
        TobbitMathBoostHistory po = new TobbitMathBoostHistory();
        po.setAvatar(avatar);
        po.setName(name);
        po.setBid(bid);
        po.setOpenId(openId);
        tobbitMathBoostHistoryDao.insert(po);

        if (po.getId() == null) {
            return MapMessage.errorMessage("网络繁忙，请稍后再试");
        }


        // Check boost done
        TobbitMathBoostBill bill = tobbitMathBoostBillDao.load(bid);
        _checkAppendBoostDone(bill);

        // WeChat auth info
        tobbitMathService.appendAuthUser(openId, name, avatar);

        return MapMessage.successMessage();
    }


    @Override
    public MapMessage newBoost(String openId, Long uid, String bookId, String name, String tel, String city, String addr) {

        if (StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage("未授权");
        }

        //~ CHECK
        // Open?
        if (!tobbitBoostCacheManager.isOpen()) {
            return MapMessage.errorMessage("活动已结束");
        }
        // In progress boost?
        if (tobbitBoostCacheManager.isInProgress(openId)) {
            return MapMessage.errorMessage("有活动正在发起中");
        }
        // Book id valid?
        TobbitMathOralBook book = tobbitMathOralBookDao.load(bookId);
        if (book == null) {
            return MapMessage.errorMessage("口算本信息有误");
        }
        // Book remain valid?
        if (tobbitBoostCacheManager.getBookRemain(book.getGrade()) < 1) {
            return MapMessage.errorMessage("口算本已经领取完了");
        }
        // Already joined
        boolean joined = isAlreadyJoined(openId);
        if (joined) {
            return MapMessage.errorMessage("您已经发起过助力啦");
        }

        //~ Process
        String addrId = appendAddress(openId, uid, name, tel, city, addr);

        if (StringUtils.isBlank(addrId)) {
            return MapMessage.errorMessage("收货信息有误");
        }

        TobbitMathBoostBill po = new TobbitMathBoostBill();
        po.setAid(addrId);
        po.setBid(bookId);
        po.setOpenId(openId);
        po.setUid(uid);
        po.setStatus(0);
        tobbitMathBoostBillDao.insert(po);

        if (po.getId() == null) {
            return MapMessage.errorMessage("网络繁忙，请稍后再试");
        }

        //~ Cache
        // new
        tobbitBoostCacheManager.newBoostBill(openId, po.getId(), JsonUtils.toJson(po));
        // decr book count
        tobbitBoostCacheManager.decrBook(book.getGrade());
        return MapMessage.successMessage().add("data", po.getId());

    }


    @Override
    public MapMessage oralBookList() {

        List<TobbitMathOralBook> books = tobbitBoostCacheManager.loadAllOralBook();
        if (books.isEmpty()) {
            List<TobbitMathOralBook> list = tobbitMathOralBookDao.load();
            list.forEach(x -> {
                x.setDisabled(null);
                x.setUpdateTime(null);
                x.setCreateTime(null);
                x.setVersion(null);
            });
            books = list;
            tobbitBoostCacheManager.updateAllOralBook(list);
        }

        // 库存
        Map<Integer, Integer> remainMap = tobbitBoostCacheManager.getBookRemain();
        books.forEach(x -> {
            Integer remain = remainMap.get(x.getGrade());
            x.setRemainCount(remain == null ? 0 : remain);
        });
        return MapMessage.successMessage().add("data", books);
    }


    @Override
    public MapMessage scrollingList() {
        List list = tobbitBoostCacheManager.loadScrolling();
        return MapMessage.successMessage().add("data", list);
    }



    @Override
    public void addOralBooksDoNotCallIfYouConfused(List<TobbitMathOralBook> books) {
        if (books.size() > 0) {
            tobbitMathOralBookDao.inserts(books);
            tobbitBoostCacheManager.updateAllOralBook(null);
        }
    }

    @Override
    public void cleanOralBooksDoNotCallIfYouConfused() {
        List<TobbitMathOralBook> list = tobbitMathOralBookDao.query();
        Set<String> ids = list.stream().map(TobbitMathOralBook::getId).collect(Collectors.toSet());
        if (ids.size() > 0) {
            tobbitMathOralBookDao.removes(ids);
            tobbitBoostCacheManager.updateAllOralBook(null);
        }

    }

    @Override
    public List<TobbitMathOralBook> loadOralBooksDoNotCallIfYouConfused() {
        return tobbitMathOralBookDao.load();
    }



    private boolean isAlreadyJoined(String openId) {
        List<TobbitMathBoostBill> list = tobbitMathBoostBillDao.loadByOpenId(openId, 10);
        return list.stream().anyMatch(x -> x.getStatus() == 1);
    }

    private TobbitMathBoostBill loadBoostBill(String openId) {
        List<TobbitMathBoostBill> list = tobbitMathBoostBillDao.loadByOpenId(openId, 10);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }


    private void _checkAppendBoostDone(TobbitMathBoostBill bill) {
        if (bill == null) {
            return;
        }
        String bid = bill.getId();

        if (0 == bill.getStatus()) {
            List<TobbitMathBoostHistory> list = tobbitMathBoostHistoryDao.loadByBoostId(bid, 10);
            if (list.size() >= tobbitBoostCacheManager.boostPCountLimit()) {
                // Done boost cycle
                bill.setStatus(1);
                tobbitMathBoostBillDao.upsert(bill);
                // Update cache
                tobbitBoostCacheManager.finishBoostBill(bid, bill.getOpenId());
                // Async call
                asyncProcess(() -> boostFinishCallBack(bid));
            }
        }


    }


    private void asyncProcess(Runnable runnable) {
        EXECUTOR_SERVICE.submit(runnable);
    }


    private String appendAddress(String openId, Long uid, String name, String tel, String city, String addr) {

        if (StringUtils.isAnyBlank(openId, name, tel, city, addr)) {
            return "";
        }
        name = StringUtils.nameFilter(name);
        tel = StringUtils.nameFilter(tel);
        city = StringUtils.nameFilter(city);
        addr = StringUtils.nameFilter(addr);


        TobbitMathAddress po = new TobbitMathAddress();
        po.setAddr(addr);
        po.setCity(city);
        po.setName(name);
        po.setTel(tel);
        po.setOpenId(openId);
        po.setUid(uid);
        tobbitMathAddressDao.save(po);

        return po.getId();
    }


    private void _appendScrolling(String name, String city) {
        // For scrolling
        String province = "北京市";
        String[] cityArray = city.split(",");
        if (cityArray.length > 0) {
            province = cityArray[0];
            if (province.length() > 4) {
                province = province.substring(0, 4) + "*";
            }
        }

        String cname = name;
        if (name.length() > 3) {
            cname = name.substring(0, 3);
        }

        // escape html
        province = StringEscapeUtils.escapeHtml4(province);
        cname = StringEscapeUtils.escapeHtml4(cname);
        tobbitBoostCacheManager.appendScrolling(province, cname);
    }


    private void boostFinishCallBack(String bid) {
        TobbitMathBoostBill po = tobbitMathBoostBillDao.load(bid);
        if (po == null) {
            return;
        }
        TobbitMathAddress address = tobbitMathAddressDao.load(po.getAid());
        TobbitMathOralBook book = tobbitMathOralBookDao.load(po.getBid());

        // Send notification
        sendWeChatMessage(address, book);

        // Append Scrolling
        if (address != null) {
            _appendScrolling(address.getName(), address.getCity());
        }

    }

    private void sendWeChatMessage(TobbitMathAddress address, TobbitMathOralBook book) {


        if (address == null) {
            return;
        }
        String openId = address.getOpenId();
        String name = address.getName();

        if (book == null) {
            return;
        }

        String bookName = String.format("《托比口算本-%s%s》", book.getPublisher(), book.getGradeName());


        String formId = miniProgramServiceClient.getUserMiniProgramCheckService().useNoticeFormId(openId, MiniProgramType.TOBBIT);

        if (StringUtils.isBlank(formId)) {
            return;
        }

        Map<Object, Object> param = new HashMap<>();
        param.put("touser", openId);
        param.put("template_id", MESSAGE_BOOST_REMIND_TEMPLATE_ID);
        param.put("page", "pages/index/main?origin=BOOST-MSG");
        param.put("form_id", formId);
        param.put("emphasis_keyword", "");

        Map<String, Object> data = new HashMap<>();


        HashMap<String, Object> v1 = new HashMap<>();
        v1.put("value", bookName);
        HashMap<String, Object> v2 = new HashMap<>();
        v2.put("value", name);
        HashMap<String, Object> v3 = new HashMap<>();
        v3.put("value", "活动截止后，统一发货");
        HashMap<String, Object> v4 = new HashMap<>();
        v4.put("value", "托比口算小程序可以批改各种口算题，不止是《托比口算本》哦！");

        data.put("keyword1", v1);
        data.put("keyword2", v2);
        data.put("keyword3", v3);
        data.put("keyword4", v4);

        param.put("data", data);


        _messageSend(param, 2);


    }


    private void _messageSend(Map param,int tries) {

        try {
            tries--;

            if (tries <0) {
                return;
            }

            String accessToken = miniProgramServiceClient.getUserMiniProgramCheckService().getAccessToken(MiniProgramType.TOBBIT);
            String url = MiniProgramApi.MESSAGE_SEND.url(accessToken);

            AlpsHttpResponse resp = HttpRequestExecutor.defaultInstance().post(url).json(param).
                    socketTimeout(30000).execute();


            String result = resp.getResponseString();


            _MpResp obj = JsonUtils.fromJson(result, _MpResp.class);


            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "mod1", param.get("touser"),
                    "mod2", url,
                    "mod3", JsonUtils.toJson(param),
                    "mod4", result,
                    "mod5", resp.getStatusCode(),
                    "mod6", Optional.ofNullable(obj).map(x -> x.errcode).orElse(-1),
                    "op", "MP_BOOST_SUCCESS_NOTIFICATION"
            ));

            if (resp.getStatusCode() == 200 && obj != null) {
                if (obj.errcode == 0) {
                    return;
                } else if (obj.errcode == 40001) {
                    miniProgramServiceClient.getUserMiniProgramCheckService().getAccessTokenNoCache(MiniProgramType.TOBBIT); // Refresh token

                }
                log.error("Get mini program push message error, code: {} ,message: {}", obj.errcode, obj.errmsg);
            }

            _messageSend(param, tries);


        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    @Data
    private static class _MpResp{
        Integer errcode;
        String errmsg;
    }



}
