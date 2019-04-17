package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_ERROR_NEED_UPGRADE;

/**
 * @author xinxin
 * @since 3/6/17.
 */
@Controller
@RequestMapping(value = "/v1/parent/chat")
public class ParentChatApiController extends AbstractParentApiController {

    /**
     * 反转聊天群禁言状态
     * 参数：
     * chatGroupId 聊天群id
     * memberId 成员id，可选，为空表示全部群成员
     * <p>
     * 说明：
     * <ul>
     * <li>1、如果群已被一键禁言，则群成员默认被禁言，后续新加入的成员也默认被禁言</li>
     * <li>2、如果群已被一键禁言，则成员不能被单独取消禁言</li>
     * <li>3、如果群未被一键禁言，则群成员可被单独禁言或取消禁言</li>
     * </ul>
     * </p>
     */
    @RequestMapping(value = "/group/sendavailable.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendAvailable() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }

    /**
     * 解散群
     * 参数：
     * chatGroupId 群id
     * <p>
     * 注：只有管理员才可以解散群
     */
    @RequestMapping(value = "/group/close.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage groupClose() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }

    /**
     * 修改群成员昵称
     */
    @RequestMapping(value = "/member/changenick.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeNick() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }
}
