<#import "../../../layout/webview.layout.ftl" as temp />
<@temp.page
title="集勋章，得奖励"
bodyClass=""
pageJs=["labourday"]
pageJsFile={"labourday" : "public/script/project/labourday"}
pageCssFile={"index" : ["public/skin/project/activity/labourday/css/skin"]}>
<#include "../../../layout/project.header.ftl"/>
<div data-bind="visible: isLoaded" style="display:none;">
    <div class="laborHeader">
        <div class="hdInfo">
            <div class="title"><span class="time">活动时间：4月27日-5月13日</span></div>
            <div class="ruleLink" data-bind="click: openActivDetail.bind($data)">活动规则</div>
        </div>
    </div>
    <div class="laborWrap">
        <div class="laborBoard">
            布置并检查1次作业，20人完成，即可点亮一个勋章。
        </div>
        <div class="laborMain">
            <div class="laborBox diff">
                <div class="laborState" data-bind="css:{'disabled': progressNumber() < 1}">
                    <div class="proIcon proIcon01"></div>
                </div>
                <div class="laborState" data-bind="css:{'disabled': progressNumber() < 2}">
                    <div class="proIcon proIcon02"></div>
                </div>
                <div class="laborState" data-bind="css:{'disabled': progressNumber() < 3}">
                    <div class="proIcon proIcon03"></div>
                </div>
                <#--<div class="laborPro"></div>-->
                <div class="stateTxt">
                    活动期间，集满3个勋章，即可获得<span class="num"><!--ko text:awardMoney --><!--/ko-->元</span>布置作业流量费！
                </div>
            </div>
            <div class="laborBox">
                <div class="laborTag">布置情况</div>
                <div class="tipsTxt">每日24点更新</div>
                <div class="laborTable">
                    <!--ko ifnot:participate-->
                    <p class="emptyData">您还没有参加活动</p>
                    <!--/ko-->
                    <!--ko if:participate-->
                    <ul data-bind="visible:homeworkList().length>0" style="display:none;">
                        <li class="li-hd">
                            <div class="time">布置日期</div>
                            <div class="grade">班级</div>
                            <div class="num">完成人数</div>
                        </li>
                        <!--ko foreach: homeworkList -->
                        <li>
                            <div class="time" data-bind="text: timeStr.split(' ')[0]"></div>
                            <div class="grade" data-bind="text: clazzName"></div>
                            <div class="num" data-bind="text: accomplishCount, css:{'txtGreen':accomplishCount >= 20}"></div>
                        </li>
                        <!--/ko-->
                    </ul>
                    <p class="emptyData" data-bind="visible:homeworkList().length===0" style="display:none;">未布置作业</p>
                    <!--/ko-->
                </div>
            </div>
        </div>
        <div class="laborFooter">
        <div class="inner">
            <!--ko if:participate -->
            <span class="signUp-btn" data-bind="click:AssignmentBtn">去布置作业</span>
            <!--/ko-->
            <!--ko ifnot:participate -->
            <span class="signUp-btn" data-bind="click:joinActiv">立即报名</span>
            <!--/ko-->
        </div>
    </div>
    </div>
    <!--规则弹窗-->
    <div class="rulesPopup" style="display: none" data-bind="visible: activDetail">
        <div class="contestRules">
            <div class="closeBtn" data-bind="click: closeDialog"></div>
            <div class="laborTag">活动规则</div>
            <div class="ruleBox">
                <div class="ruleTxt">
                    <p class="ruleLabel">活动时间</p>
                    <p>4月27日-5月13日</p>
                </div>
                <div class="ruleTxt">
                    <p class="ruleLabel">活动对象</p>
                    <p>部分小学数学老师</p>
                </div>
                <div class="ruleTxt">
                    <p class="ruleLabel">活动条件及奖励</p>
                    <p>活动期间给任意班级布置并检查1次作业，单班单次作业完成人数不少于20人即获得1枚勋章，累计3枚勋章获得<!--ko text:awardMoney --><!--/ko-->元布置作业流量费。<span class="txtRed">当天给多个班级布置作业计算为一次</span></p>
                </div>
                <div class="ruleTxt">
                    <p class="ruleLabel">奖励发放</p>
                    <p>条件达成后72小时内发送</p>
                </div>
                <div class="ruleTxt">
                    <p class="ruleLabel">勋章更新时间</p>
                    <p>每天24点更新</p>
                </div>
            </div>
        </div>
    </div>
</div>
    <#include "../../../layout/project.footer.ftl"/>
</@temp.page>