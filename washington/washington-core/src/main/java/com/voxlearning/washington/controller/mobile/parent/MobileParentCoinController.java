package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.galaxy.service.coin.api.CoinTypeBufferLoaderClient;
import com.voxlearning.galaxy.service.coin.api.DPCoinLoader;
import com.voxlearning.galaxy.service.coin.api.constant.CoinOperationType;
import com.voxlearning.galaxy.service.coin.api.entity.Coin;
import com.voxlearning.galaxy.service.coin.api.entity.CoinHistory;
import com.voxlearning.galaxy.service.coin.api.entity.CoinType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author malong
 * @since 2018/06/02
 */
@Controller
@RequestMapping(value = "parentMobile/coin/")
@Slf4j
public class MobileParentCoinController extends AbstractMobileParentController {
    @ImportService(interfaceClass = DPCoinLoader.class)
    private DPCoinLoader dpCoinLoader;
    @Inject
    private CoinTypeBufferLoaderClient coinTypeBufferLoaderClient;

    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getInfo() {
        Long studentId = getRequestLong("sid");
        if (studentId == 0L) {
            return MapMessage.errorMessage("学生id错误");
        }
        Student student = studentLoaderClient.loadStudent(studentId);
        if (student == null) {
            return MapMessage.errorMessage("获取学生信息错误");
        }
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("未登录");
        }
        MapMessage message = MapMessage.successMessage();
        try {
            Integer totalCount = 0;
            Coin coin = dpCoinLoader.loadCoin(studentId);
            if (coin != null) {
                totalCount = coin.getTotalCount();
            }
            message.add("totalCount", totalCount);
            List<CoinHistory> histories = dpCoinLoader.loadHistories(studentId);
            message.add("histories", getCoinHistories(histories));
            message.add("studentName", student.fetchRealname());
            return message;
        } catch (Exception ex) {
            logger.error("get student coin info error, sid:{}", studentId);
            return MapMessage.errorMessage("系统异常");
        }
    }

    private List<Map<String, Object>> getCoinHistories(List<CoinHistory> histories) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CoinHistory history : histories) {
            CoinType coinType = coinTypeBufferLoaderClient.getCoinType(history.getType());
            if (coinType != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", coinType.getName());
                map.put("count", CoinOperationType.increase == coinType.getOpType() ? "+" + history.getCount() : "-" + history.getCount());
                map.put("date", DateUtils.dateToString(history.getCreateTime(), "yyyy-MM-dd"));
                list.add(map);
            }
        }
        return list;
    }
}
