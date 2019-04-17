package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;


/**
 * @author Hailong Yang
 * @version 0.1
 * @since 2015/10/22
 */
@Slf4j
@Controller
@RequestMapping(value = "/v1/parent/flower")
public class ParentFlowerApiController extends AbstractParentApiController {

    @Inject private FlowerServiceClient flowerServiceClient;

    //送花
    @RequestMapping(value = "sendflower.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendFlower() {
        return failMessage(ApiConstants.RES_RESULT_UNSUPPORT_ANSWER_EXAM);
    }


}
