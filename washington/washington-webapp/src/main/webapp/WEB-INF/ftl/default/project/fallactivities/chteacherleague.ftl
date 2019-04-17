<#import "../../layout/project.module.ftl" as temp />
<@temp.page header="false" title="教师联盟，邀请有礼">
    <@app.css href="public/skin/project/fallactivities/css/invitegifts.css"/>
    <@sugar.capsule js=[ "alert","voxLogs","ko","chineseteacherleague"]/>
<style>
    body{
        background: #7ad7dd;
    }
</style>
    <#include "../../layout/project.header.ftl"/>

<div class="wrapper">
    <div class="s-banner-2">
        <img src="<@app.link href='public/skin/project/fallactivities/images/banner_chineseteacher-v3.jpg'/>" alt="">
    </div>
    <div class="g-section">
        <div class="item-box">
            <div class="item-list">
                <h4>马上邀请</h4>
                <div class="inpt-box">
                    <div class="inpt-name">
                        <p class="l-txt">老师姓名：</p>
                        <div class="inptBox">
                            <!-- isShow 显示 -->
                            <span class="isShow"></span>
                            <div class="enter-box">
                                <input type="text" maxlength="5" class="JS-teacherName" data-bind="event:{keyup: inputKeyUp}"/>
                            </div>
                        </div>
                    </div>
                    <div class="inpt-name">
                        <p class="l-txt">老师手机：</p>
                        <div class="inptBox">
                            <!-- isShow 显示 -->
                            <span class="isShow"></span>
                            <div class="enter-box">
                                <input type="text" maxlength="11" class="JS-teacherMobile" data-bind="event:{keyup: inputKeyUp}"/>
                            </div>
                        </div>
                    </div>
                    <div class="inpt-name inpt-name-2">
                        <p class="l-txt">验证码：</p>
                        <div class="inptBox">
                            <!-- isShow 显示 -->
                            <span class="isShow"></span>
                            <div class="enter-box">
                                <input type="text" maxlength="4" class="JS-teacherCaptcha" data-bind="text: captchaInput,event:{keyup: inputKeyUp}"/>
                            </div>
                            <div class="codeImg">
                                <img id="captchaImage" src=""/>
                            </div>
                            <div class="refresh" data-bind="click: changeCode">看不清，换一张</div>
                        </div>
                    </div>
                </div>
                <div class="btn-send">
                    <span data-bind="click: inviteTeacher">发送邀请</span>
                </div>
            </div>
        </div>
        <div class="item-box">
            <div class="item-list">
                <div style="height:360px;overflow-y: auto">
                    <h3>奖励规则</h3>
                    <div class="context">
                        <div class="prompt-txt">邀请语文新老师在15天内双方达成认证，双方各得20元布置作业流量费！</div>
                        <div class="prompt-txt">被邀请人达成认证时邀请人已认证，双方将在72小时内获得奖励；</div>
                        <div class="prompt-txt">被邀请人达成认证时邀请人未认证，邀请人达成认证后72小时内获得奖励。</div>
                    </div>
                    <h3 style="padding-top:20px;">活动规则</h3>
                    <div class="context">
                        <div>
                            <p class="txt"><i>1.</i>参加活动时间：2018/4/9-2018/6/30</p>
                            <p class="txt"><i>2.</i>活动对象：小学语文老师</p>
                            <p class="txt"><i>3.</i>活动奖励及条件：小学老师邀请小学语文新老师，新老师注册成功15天内双方达成认证，双方各得20元布置作业流量费！</p>
                        </div>
                    </div>
                    <h3 style="padding-top:20px;">注意事项</h3>
                    <div class="context">
                        <div>
                            <p class="txt"><i>1.</i>活动下线时间为2018年6月30日24:00，在2018年6月30日发出的邀请，最晚可以在2018年7月15日前完成任务。</p>
                            <p class="txt"><i>2.</i>邀请双方老师必须在被邀请老师注册后15天内完成认证，如未满足要求，无法获得奖励。</p>
                            <p class="txt"><i>3.</i>被邀请老师必须是小学语文老师。</p>
                        </div>
                    </div>
                    <div class="state">本次活动最终解释权归一起作业所有</div>
                </div>

            </div>
        </div>
        <div class="item-box">
            <div class="item-list">
                <h4 class="title" style="padding-top:28px;">活动进度 <!--ko if:particTime--><span style="font-size:12px;">（参加活动时间：<!--ko text: particTime --><!--/ko-->）</span><!--/ko--></h4>
                <div class="to-auth" data-bind="visible: authenticate() != 'SUCCESS'" style="display:none;"><span>您还未认证，无法获得奖励，赶快去认证吧！</span><a href="javascript:void(0);" data-bind="click: $root.howToAuthBtn">如何认证<i></i></a></div>
                <div class="to-auth" data-bind="visible: authenticate() == 'SUCCESS'" style="display:none;"><span>您已邀请了<!--ko text:invitedTeacherNum--><!--/ko-->名老师，累计获得话费<!--ko text:totalAwardNum--><!--/ko-->元！</span>（奖励72小时内到账）</div>
            <#--<div class="to-auth" data-bind="visible: authenticate() == 'SUCCESS'" style="height:20px;line-height:20px;padding-top:0;">1-9号节假日暂停发放话费奖励，预计10号恢复</div>-->
                <div class="tHead" data-bind="visible:successList().length > 0" style="display:none;margin-top:8px;">
                    <i class="tHead-i">姓名<br/>（手机号）</i>
                    <i>认证状态</i>
                <#--<i>新学生数</i>-->
                    <i>剩余天数</i>
                    <i>话费奖励</i>
                </div>
                <div class="list-studInfo">
                    <ul data-bind="visible:successList().length > 0" style="display:none;">
                        <!--ko foreach: successList()-->
                        <li class="item">
                            <div class="item-box-2 item-box-3">
                                <p data-bind="attr:{style: (inviteTeacherName && inviteTeacherMobile) ? 'line-height: 20px;padding-top:5px;':''}"><!--ko text: inviteTeacherName--><!--/ko--><!--ko if: (inviteTeacherName && inviteTeacherMobile)--><br/><!--/ko--><!--ko text: inviteTeacherMobile--><!--/ko--></p>
                            </div>
                            <div class="item-box-2">
                                <p data-bind="text: invitedAuthenticate,css:{'z-state': invitedAuthenticate == '未认证'}"></p>
                            </div>
                        <#--<div class="item-box-2">-->
                        <#--<p data-bind="text: finishHomeworkStudentNum"></p>-->
                        <#--</div>-->
                            <div class="item-box-2">
                                <p data-bind="text: lastDays"></p>
                            </div>
                            <div class="item-box-2">
                                <p data-bind="text: award"></p>
                            </div>
                        </li>
                        <!--/ko-->
                    </ul>
                    <div style="display:none; text-align:center;margin-top:10px;font-size:16px;" data-bind="visible:successList().length == 0">还没有成功邀请老师，快去提醒他们注册吧！</div>
                </div>
            </div>
        </div>
        <div class="item-box">
            <div class="item-list">
                <h4 class="title" style="padding-top:28px;">&nbsp;&nbsp;</h4>
                <a class="to-auth-2" href="javascript:void(0);" data-bind="visible:failureList().length == 0" style="display:none;">您还未邀请老师，快去发出邀请吧！</a>
                <a class="to-auth-2" href="javascript:void(0);" data-bind="visible:failureList().length > 0" style="display:none;">您邀请的老师还有<!--ko text:inviteFailedNum--><!--/ko-->名未成功，快去提醒TA吧！</a>
                <div class="tHead-2" data-bind="visible:failureList().length > 0" style="display:none;margin-top: 8px;">
                    <i>姓名(手机号)</i>
                    <i>邀请状态</i>
                </div>
                <div class="list-studInfo-2">
                    <ul data-bind="visible:failureList().length > 0" style="display:none;">
                        <!--ko foreach: failureList()-->
                        <li class="item-2">
                            <div class="tName">
                                <p><!--ko text: inviteTeacherName--><!--/ko-->（<!--ko text: inviteTeacherMobile--><!--/ko-->）</p>
                            </div>
                            <div class="z-invite">
                                <div class="item-tab">
                                    <p class="txt">
                                        <span class="num" data-bind="html: inviteStatus"></span>
                                    </p>
                                </div>
                            </div>
                        </li>
                        <!--/ko-->
                    </ul>
                    <div style="display:none; text-align:center;margin-top:10px;font-size:16px;" data-bind="visible:failureList().length == 0">没有更多老师，在上方输入老师信息，马上邀请！</div>
                </div>
            </div>
        </div>
    </div>
    <div data-bind="template: { name: 'alertDialog', data: alertDialogData },visible:alertDialogFlag"></div>
    <script type="text/html" id="alertDialog">
        <div class="m-popup">
            <!-- 弹窗 已发送邀请 -->
            <!-- ko if:state == 'message' -->
            <div class="beenSent-box">
                <p>已向该老师发送邀请短信，</p>
                <div>请当面提醒该老师，注册时记得填写您的ID或手机号哦~</div>
                <div>给予新老师帮助，您可以更快得到邀请奖励！</div>
                <div class="btn-confirm"><a href="javascript:void(0);" data-bind="click: $root.closeDialog">我知道了</a></div>
            </div>
            <!--/ko-->
            <!-- ko if:state == 'isRegister' -->
            <div class="beenSent-box">
                <p>该手机号已注册一起作业，请换个老师邀请吧！</p>
                <div class="btn-confirm" style="margin-top:30px;"><a href="javascript:void(0);" data-bind="click: $root.closeDialog">我知道了</a></div>
            </div>
            <!--/ko-->
            <!-- 弹窗-如何认证 -->
            <!-- ko if:state == 'auth' -->
            <div class="popIner">
                <h4>认证规则</h4>
                <div class="context">
                    <p>1、设置姓名并绑定手机</p>
                    <p>2、至少3名学生，每人绑定了手机</p>
                    <p class="tag-txt">满足条件后如未变为认证状态，<br>请注意接听客服来电。</p>
                </div>
                <div class="confirm-btn">
                    <a class="btn" href="javascript:void(0);" data-bind="click: $root.closeDialog">确定</a>
                </div>
                <div class="close" data-bind="click: $root.closeDialog"></div>
            </div>
            <!--/ko-->
        </div>
    </script>
</@temp.page>

<#include "../../layout/project.footer.ftl"/>