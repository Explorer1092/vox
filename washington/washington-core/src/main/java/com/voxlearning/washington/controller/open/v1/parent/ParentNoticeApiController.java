package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.washington.controller.open.AbstractParentApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author shiwe.liao
 * @since 2016/4/18
 */
@Controller
@Slf4j
@RequestMapping(value = "/v1/parent/notice")
public class ParentNoticeApiController extends AbstractParentApiController {
}
