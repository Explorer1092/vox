package com.voxlearning.enanalyze.controller;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.enanalyze.ViewBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 测试
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Controller
@RequestMapping(value = "/enanalyze/article")
public class TestController {

    /**
     * 客户端上传图片提交字符识别
     *
     * @param url url
     * @return
     */
    @RequestMapping(value = "url.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage ocr(@RequestParam(name = "url") String url) {
        return ViewBuilder.success("url");
    }
}
