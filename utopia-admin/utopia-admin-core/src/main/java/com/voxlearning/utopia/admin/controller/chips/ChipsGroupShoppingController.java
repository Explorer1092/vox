package com.voxlearning.utopia.admin.controller.chips;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.util.ChipsWechatShareUtil;
import com.voxlearning.utopia.service.ai.api.ChipsOrderLoader;
import com.voxlearning.utopia.service.ai.entity.ChipsGroupShopping;
import com.voxlearning.utopia.service.user.api.entities.ParentExtAttribute;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chips/group/shopping")
public class ChipsGroupShoppingController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = ChipsOrderLoader.class)
    private ChipsOrderLoader chipsOrderLoader;

    @Inject
    private ParentLoaderClient parentLoaderClient;

    @AlpsQueueProducer(queue = "utopia.chips.create.group.queue")
    private MessageProducer createGroupProducer;

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return "/";
        }
        List<ChipsGroupShopping> res = chipsOrderLoader.loadGroupShoppingListForCrm();
        int pageNumber = getRequestInt("pageNumber", 1);
        Pageable pageable = new PageRequest(pageNumber - 1, 10);
        Page<Map<String, Object>> pageData = PageableUtils.listToPage(converList(res), pageable);
        model.addAttribute("pageData", pageData);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("total", res != null ? res.size() : 0);
        return "chips/groupshoppinglist";
    }

    private List<Map<String, Object>> converList(List<ChipsGroupShopping> res) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(res)) {
            Set<Long> userIds = res.stream().map(ChipsGroupShopping::getSponsor).collect(Collectors.toSet());
            Map<Long, ParentExtAttribute> userMap = parentLoaderClient.loadParentExtAttributes(userIds);
            res.forEach(e -> {
                Map<String, Object> bean = new HashMap<>();
                bean.put("id", e.getId());
                ParentExtAttribute parentExt = userMap.get(e.getSponsor());
                bean.put("user", (parentExt != null && StringUtils.isNotBlank(parentExt.getWechatNick())? parentExt.getWechatNick() : "") +"(" + e.getSponsor() + ")");
                bean.put("link", ChipsWechatShareUtil.getWechatDomain() + "/chips/center/formal_group_buy.vpage?origin=invite&code=" + e.getCode());
                bean.put("createDate", DateUtils.dateToString(e.getCreateTime()));
                list.add(bean);
            });
        }
        return list;
    }

    @RequestMapping(value = "add.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage add() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请登录");
        }

        Map<String, Object> message = new HashMap<>();
        Long userId = -1L;
        message.put("U", userId);
        int number = 2;
        message.put("N", number);
        createGroupProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
        return MapMessage.successMessage();
    }
}
