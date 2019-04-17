package com.voxlearning.utopia.agent.mockexam.controller;

/**
 * 授权码
 *
 * @author xiaolei.li
 * @version 2018/8/17
 */
public interface ResourceCode {


    interface Operation {
        String PLAN_FORQUERY = "95c61cb937444b01";
        String PLAN_QUERY = "58a8a9359b014ba8";
        String PLAN_FORCREATE = "b48bb9a39654460a";
        String PLAN_CREATE = "b48bb9a39654460a";
        String PLAN_WITHDRAW = "971b75bb425c4b1e";
        String PLAN_SUBMIT = "fc084d2f874241ec";
        String PLAN_FORUPDATE = "9a54ac0d916a46e1";
        String PLAN_UPDATE = "9a54ac0d916a46e1";
        String PLAN_AUDIT = "75ea6e27d7634207";
        String PLAN_ONLINE = "e2c4653508e54264";
        String PLAN_OFFLINE = "e2c4653508e54264";
        String EXAM_SCORE_QUERY = "f248c9edcad04809";
        String EXAM_MAKEUP = "d7056310b03e48e7";
        String EXAM_REPLENISH = "483df64eee694e1d";
        String PAPER_OPEN = "d37c10cc3de14096";
        String REFER_REGION = "c2ede01d4e004b25";
        String REFER_SCHOOL = "3d714f3f950e46f7";
        String REFER_BOOK = "7ddc2c3bb309480e";
    }

    interface PageElement {
        String MOCK_EXAM = "0b2bde6ed5f14bd5";
        String PLAN_MANAGER = "eea319942b0a4024";
        String PAPER_MANAGER = "67b9c58ba23445b4";
        String ADMIN_AND_DEV = "2f0436cbb9784429";
        String ADMIN = "ba3622da4871493c";
        String DEV = "48e60ffda050414c";
    }
}
