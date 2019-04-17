package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.galaxy.service.coin.api.DPCoinLoader;
import com.voxlearning.galaxy.service.coin.api.DPCoinService;
import com.voxlearning.galaxy.service.coin.api.constant.CoinOperationType;
import com.voxlearning.galaxy.service.coin.api.entity.CoinType;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author malong
 * @since 2018/06/01
 */
@Controller
@RequestMapping(value = "opmanager/cointype")
@Slf4j
public class CoinTypeManager extends AbstractAdminSystemController {
    @ImportService(interfaceClass = DPCoinLoader.class)
    private DPCoinLoader dpCoinLoader;
    @ImportService(interfaceClass = DPCoinService.class)
    private DPCoinService dpCoinService;

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String getList(Model model) {
        int page = getRequestInt("page", 1);
        if (page < 0) {
            page = 1;
        }
        Pageable pageable = new PageRequest(page - 1, 20);
        List<CoinType> types = dpCoinLoader.loadCoinTypesFromDB().getUninterruptibly()
                .stream()
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());
        Page<CoinType> typePage = PageableUtils.listToPage(types, pageable);

        List<String> opTypes = new ArrayList<>();
        for (CoinOperationType opType : CoinOperationType.values()) {
            opTypes.add(opType.name());
        }
        model.addAttribute("typePage", typePage);
        model.addAttribute("opTypes", opTypes);
        model.addAttribute("currentPage", typePage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", typePage.getTotalPages());
        model.addAttribute("hasPrev", typePage.hasPrevious());
        model.addAttribute("hasNext", typePage.hasNext());

        return "opmanager/cointype/list";
    }

    @RequestMapping(value = "addType.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addType() {
        Integer id = getRequestInt("id", -1);
        String name = getRequestString("name");
        Integer count = getRequestInt("count");
        String opType = getRequestString("opType");
        Integer weekLimitCount = getRequestInt("weekLimitCount", -1);
        Integer monthLimitCount = getRequestInt("monthLimitCount", -1);
        String desc = getRequestString("desc");
        boolean manual = getRequestBool("manual");

        //id自动增长
        if (id < 0) {
            List<CoinType> coinTypes = dpCoinLoader.loadCoinTypesFromDB().getUninterruptibly();
            if (coinTypes.stream().anyMatch(coinType -> name.equals(coinType.getName()))) {
                return MapMessage.errorMessage("学习币类型已经存在");
            }
            CoinType coinType = coinTypes.stream()
                    .sorted((o1, o2) -> o2.getId().compareTo(o1.getId()))
                    .findFirst()
                    .orElse(null);
            if (coinType == null) {
                id = 1;
            } else {
                id = coinType.getId() + 1;
            }
        }

        CoinType type = new CoinType();
        type.setId(id);
        type.setName(name);
        type.setCount(count);
        type.setOpType(CoinOperationType.valueOf(opType));
        type.setWeekLimitCount(weekLimitCount);
        type.setMonthLimitCount(monthLimitCount);
        type.setDesc(desc);
        type.setManual(manual);
        try {
            CoinType upsert = dpCoinService.upsertCoinType(type).getUninterruptibly();
            if (upsert == null) {
                return MapMessage.errorMessage("保存学习币类型失败");
            }
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存学习币类型失败");
        }
        //增加或者修改学习币类型的时候，清楚批量操作的模板文件的缓存
        CacheSystem.CBS.getCache("persistence").delete("COIN_TEMPLATE_FILE");
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "getType.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getType() {
        Integer id = getRequestInt("id");
        CoinType type = dpCoinLoader.loadCoinTypeFromDB(id).getUninterruptibly();
        if (type == null) {
            return MapMessage.errorMessage("学习币类型不存在");
        }
        return MapMessage.successMessage().add("type", type);
    }
}
