<#import "../layout/webview.layout.ftl" as temp />
<@temp.page
title="数学作业活动"
bodyClass=""
pageJs=["weektask"]
pageJsFile={"weektask" : "public/script/project/weektask"}
pageCssFile={"index" : ["public/skin/project/activity/weektask/css/skin"]}>
<#include "../layout/project.header.ftl"/>
<div data-bind="visible: isLoaded" style="display:none;">
    <div class="headerBox">
        <div class="banner">
            <!--活动规则入口-->
            <a href="javascript:void(0)" class="ruleLink" data-bind="click: openActivDetail.bind($data)">活动规则</a>
            <div class="timeBox">
                <p>每周可得10元流量哦~</p>
                <p class="time">活动时间：2018.3.5—2018.4.8</p>
            </div>
            <!--ko if:participate-->
            <div class="timeBox2">
                <p class="time">参加活动时间：<!--ko text:participateDate --><!--/ko--></p>
            </div>
            <!--/ko-->
        </div>
    </div>
    <div class="mainBox">
        <div class="cTitle">累计完成</div>
        <div class="contentBox">
            <!--ko ifnot:participate-->
            <div class="cProgressNo">您还没有参加活动</div>
            <!--/ko-->
            <!--ko if:participate-->
            <div class="cProgress">
                <ul class="weekBox" data-bind="foreach:progressAll">
                    <li data-bind="css:{'complete':status == '1','active':status == '0'}">
                        <div class="week" data-bind="text:weekName"></div>
                        <p class="state">
                            <!--ko if:status == '1' -->已完成<!--/ko-->
                            <!--ko if:status == '-1' -->未完成<!--/ko-->
                            <!--ko if:status == '-2' -->未参加<!--/ko-->
                            <!--ko if:status == '0' -->进行中<!--/ko-->
                            <!--ko if:status == '9' -->未开始<!--/ko-->
                        </p>
                    </li>
                </ul>
                <div class="cName">你的称号：<!--ko text:nickName--><!--/ko--></div>
            </div>
            <!--/ko-->
        </div>
        <div class="cTitle">本周进展</div>
        <div class="contentBox">
            <!--ko ifnot:participate-->
            <div class="cProgressNo">您还没有参加活动</div>
            <!--/ko-->
            <!--ko if:participate-->
            <div class="cSchedule" data-bind="foreach:progressList,visible:progressList().length>0" style="display:none">
                <div class="scheduleBox hideMn" data-bind="visible:homeworkList.length>0" style="display:none">
                    <div class="hd" data-bind="click:$root.scheduleButton.bind($data)">
                        <!--ko text:clazzName--><!--/ko-->（共<!--ko text:studentCount--><!--/ko-->人）
                        <div class="fr">作业进度<!--ko text:status --><!--/ko-->/3次</div>
                    </div>
                    <ul class="mn">
                        <li class="tHd">
                            <div class="column01">布置作业</div>
                            <div class="column02">完成人数</div>
                            <div class="column03">作业进度</div>
                        </li>
                        <!--ko foreach: homeworkList -->
                        <li data-bind="css:{'complete':accomplished}">
                            <div class="column01" data-bind="text: assignTimeStr"></div>
                            <div class="column02" data-bind="text: accomplishCount"></div>
                            <div class="column03"><!--ko if: accomplished-->完成<!--/ko--><!--ko ifnot: accomplished-->未完成<!--/ko--></div>
                        </li>
                        <!--/ko-->
                    </ul>
                </div>
                <div class="cProgressNo" data-bind="visible:homeworkList.length==0" style="display:none">本周您还没有任何进展</div>
            </div>
            <div class="cProgressNo" data-bind="visible:progressList().length==0" style="display:none">本周您还没有任何进展</div>
            <!--/ko-->
        </div>
        <!--ko if:participate -->
        <div class="footerBtn">
            <a href="javascript:void(0)" class="btn" data-bind="click: $root.AssignmentBtn">去布置作业</a>
        </div>
        <!--/ko-->
        <!--ko ifnot:participate -->
        <div class="footerBtn">
            <a href="javascript:void(0)" class="btn" data-bind="click:joinActiv">参加活动</a>
        </div>
        <!--/ko-->
    </div>
    <div class="rulesPopup" style="display: none" data-bind="visible: activDetail">
        <div class="popInner" data-bind="click: stopPropagation.bind($data)">
            <div class="cTitle tTitle">活动规则</div>
            <div class="pMain">
                <div class="pTxt">
                    <p class="title">活动时间:</p>
                    <p>2018/3/5-2018/4/8</p>
                </div>
                <div class="pTxt">
                    <p class="title">活动对象:</p>
                    <p>小学认证数学老师</p>
                </div>
                <div class="pTxt">
                    <p class="title">活动条件及奖励:</p>
                    <p>小学认证数学老师在参加活动期间，每周至少为一个班布置并检查3次以上有效作业（布置班级人数超过20人的作业为有效作业），每次有效作业不少于20人完成，作业完成时间在每周日23:59:59之前，第二周即可获得10元流量奖励，最多可获得50元流量奖励。</p>
                </div>
                <div class="pTxt">
                    <p class="title">奖励发放:</p>
                    <p>在活动第二周开始后每周一到周三发放，活动持续五周。</p>
                </div>
                <div class="copyRight">〝本次活动最终解释权归一起作业所有〞</div>
            </div>

        </div>
    </div>
</div>
<#include "../layout/project.footer.ftl"/>
</@temp.page>