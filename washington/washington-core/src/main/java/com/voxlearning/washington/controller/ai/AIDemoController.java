package com.voxlearning.washington.controller.ai;

import com.voxlearning.alps.lang.util.MapMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author songtao
 * @since 2018/1/17
 */
@Controller
@RequestMapping("/aidemo")
public class AIDemoController extends AbstractAiController {

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage index() {
        return MapMessage.errorMessage("产品已经下线");
    }

    @RequestMapping(value = "rank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rank() {
        return MapMessage.errorMessage("产品已经下线");
    }

    @RequestMapping(value = "questions.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage questions() {
        return MapMessage.errorMessage("产品已经下线");
    }

    @RequestMapping(value = "processresult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage processresult() {
        return MapMessage.errorMessage("产品已经下线");
    }


    @RequestMapping(value = "common/feedback.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage feedback() {
        return MapMessage.errorMessage("产品已经下线");
    }

    @RequestMapping(value = "report.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage report() {
        return MapMessage.errorMessage("产品已经下线");
    }

    @RequestMapping(value = "correct/aiaudio.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage fetchcorrectAIAudio() {
        return MapMessage.errorMessage("产品已经下线");
    }
}
