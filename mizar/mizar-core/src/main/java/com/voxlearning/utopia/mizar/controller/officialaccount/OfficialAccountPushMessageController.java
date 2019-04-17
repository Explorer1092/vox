package com.voxlearning.utopia.mizar.controller.officialaccount;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccounts;
import com.voxlearning.utopia.service.mizar.consumer.service.OfficialAccountsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author peng.zhang.a
 * @since 16-12-16
 */
@Controller
@RequestMapping(value = "/basic/officialaccount")
public class OfficialAccountPushMessageController extends AbstractMizarController {

    @Inject OfficialAccountsServiceClient officialAccountsServiceClient;
    @Inject UserLoaderClient userLoaderClient;

    private List<OfficialAccounts> getAccountList() {

        List<String> accountKeys = getCurrentUser().getOfficialAccountKeyList();
        if (CollectionUtils.isEmpty(accountKeys))
            return Collections.emptyList();

        return accountKeys.stream()
                .filter(key -> StringUtils.isNotEmpty(key))
                .map(key -> officialAccountsServiceClient.loadAccountByKey(key))
                .filter(c -> c != null)
                .collect(Collectors.toList());
    }

    /**
     * 推送消息首页
     */
    @RequestMapping(value = "pushmessage/index.vpage", method = RequestMethod.GET)
    public String pushmessage(Model model) {
        List<OfficialAccounts> accounts = getAccountList();
        if (accounts == null)
            model.addAttribute("errmsg", "公众号不能为空");
        model.addAttribute("accounts", accounts);
        model.addAttribute("resultMsg", getRequestString("resultMsg"));
        model.addAttribute("errmsg", getRequestString("errmsg"));
        return "basic/officialaccount/pushmsg";
    }

    /**
     * 提交推送消息
     */
    @RequestMapping(value = "pushmessage/submit.vpage", method = RequestMethod.POST)
    public String submit(Model model) {
        Long accountId = getRequestLong("accountId");
        OfficialAccounts accounts = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
        String title = getRequestString("title");
        String content = getRequestString("content");
        String userId = getRequestString("userIds");
        String url = getRequestString("url");
        if (accounts == null || StringUtils.isBlank(title)
                || StringUtils.isBlank(content) || StringUtils.isBlank(userId)
                || StringUtils.isBlank(url)) {
            model.addAttribute("errmsg", "请求参数不能为空");
            return "redirect:/basic/officialaccount/pushmessage/index.vpage";
        }

        // 发送支付成功公众号消息
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("accountsKey", accounts.getAccountsKey());
        Set<Long> userIds = new HashSet<>();
        String[] splits = userId.split("[\n]");
        for (String split : splits) {
            Long uid = SafeConverter.toLong(split, 0);
            if (uid != 0) {
                userIds.add(uid);
            }
        }
        if (userIds.size() > 1000) {
            model.addAttribute("errmsg", "不能超过1000行数据");
        } else {

            Map<Long, User> users = userLoaderClient.loadUsers(userIds);
            if (MapUtils.isEmpty(users)) {
                model.addAttribute("errmsg", "所有用户都不是有效用户");
            } else if (users.values().size() != userIds.size()) {
                Set<Long> set = users.keySet();
                Long midUserId = userIds.stream().filter(p -> !set.contains(p)).findFirst().orElse(0L);
                model.addAttribute("errmsg", midUserId + "信息不存在");
            } else {
                User midUser = users.values().stream().filter(p -> !p.isParent()).findFirst().orElse(null);
                if (midUser != null) {
                    model.addAttribute("errmsg", midUser.getId() + "不是家长");
                } else {
                    MapMessage mapMessage = officialAccountsServiceClient.sendMessage(new ArrayList<>(userIds), title, content, url, JsonUtils.toJson(extInfo), false);
                    if (mapMessage.isSuccess()) {
                        model.addAttribute("resultMsg", "发送成功");
                    }
                }
            }
        }
        return "redirect:/basic/officialaccount/pushmessage/index.vpage";
    }
}
