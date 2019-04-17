<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="处理换班申请" pageJs="" navBar="hidden">
<@sugar.capsule css=['audit']/>
<div class="crmList-box">
    <#--<div class="res-top fixed-head">-->
        <#--<span class="return-line"></span>-->
        <#--<span class="res-title">处理换班申请</span>-->
    <#--&lt;#&ndash;<#if schoolId??>card.vpage?schoolId=${schoolId}<#else>list.vpage</#if>&ndash;&gt;-->
        <#--<div class="return"><a href="javascript:window.history.back()"><i class="return-icon"></i>返回</a></div>-->
    <#--</div>-->
    <div class="c-opts gap-line tab-head c-flex c-flex-2" style="margin-bottom:.5rem">
        <span class="the">未完成（<#if pendingList??>${pendingList?size!0}</#if>）</span>
        <span>已完成<#--（<#if overList??>${overList?size!0}</#if>）--></span>
    </div>

    <div class="tab-main" style="clear:both">
    <#--待审核-->
        <div>
            <#if pendingList?? && pendingList?has_content>
                <p style="font-size:.6rem;background:#fff6ee;padding:0 1rem;margin-bottom:-.5rem;height:1.5rem;line-height:1.5rem">
                    特别提醒：一定要和老师确认清楚再处理。</p>
                <#list pendingList as item>
                    <div class="adjustmentExamine-box">
                        <div class="adjust-content" style="border-bottom:1px dashed #cdd3dc">
                            <#switch item.type>
                                <#case "LINK">
                                    <p class="pattern" style="font-size:.7rem">加入班级
                                        <span style="float:right">
                                            ${(item.createTime!0)?string("MM-dd")}
                                        </span>
                                    </p>
                                    <p class="pattern" style="font-size:.6rem;color:#9199bb">
                                        ${item.schoolName!''}
                                    </p>
                                    <p class="pattern">
                                        <span class="orange-color teacherUrl" data-id="${item.applicantId!}">
                                        ${item.applicantName!}（${item.applicantSubject!}）<#if item.applicantAuthState?? && item.applicantAuthState == 1>
                                            <span class="icon-box"><i class="icon-zheng"></i></span></#if>
                                        </span>申请和
                                        <span
                                            class="orange-color teacherUrl"
                                            data-id="${item.respondentId!}">${item.respondentName!}
                                        （${item.respondentSubject!}）<#if item.respondentAuthState?? && item.respondentAuthState == 1>
                                                <span class="icon-box"><i class="icon-zheng"></i></span></#if>
                                        </span>同时教${item.clazzName!}
                                    </p>
                                    <#break />
                                <#case "REPLACE">
                                    <p class="pattern" style="font-size:.7rem">班级接管<span
                                            style="float:right">${(item.createTime!0)?string("MM-dd")}</span></p>
                                    <p class="pattern" style="font-size:.6rem;color:#9199bb">${item.schoolName!''}</p>
                                    <p class="pattern"><span class="orange-color teacherUrl"
                                                             data-id="${item.applicantId!}">${item.applicantName!}
                                        （${item.applicantSubject!}）<#if item.applicantAuthState?? && item.applicantAuthState == 1>
                                            <span class="icon-box"><i class="icon-zheng"></i></span></#if></span>申请接管<span class="orange-color teacherUrl"
                                                                                    data-id="${item.respondentId!}">${item.respondentName!}
                                        （${item.respondentSubject!}）<#if item.respondentAuthState?? && item.respondentAuthState == 1>
                                            <span class="icon-box"><i class="icon-zheng"></i></span></#if></span>的${item.clazzName!}</p>
                                    <#break />
                                <#case "TRANSFER">
                                    <p class="pattern" style="font-size:.7rem">班级转让<span
                                            style="float:right">${(item.createTime!0)?string("MM-dd")}</span></p>
                                    <p class="pattern" style="font-size:.6rem;color:#9199bb">${item.schoolName!''}</p>
                                    <p class="pattern"><span class="orange-color teacherUrl"
                                                             data-id="${item.applicantId!}">${item.applicantName!}
                                        （${item.applicantSubject!}）<#if item.applicantAuthState?? && item.applicantAuthState == 1>
                                            <span class="icon-box"><i class="icon-zheng"></i></span></#if></span>申请将${item.clazzName!}转让给<span
                                            class="orange-color teacherUrl"
                                            data-id="${item.respondentId!}">${item.respondentName!}
                                        （${item.respondentSubject!}）<#if item.respondentAuthState?? && item.respondentAuthState == 1>
                                            <span class="icon-box"><i class="icon-zheng"></i></span></#if></span></p>
                                    <#break />
                                <#default>
                                    <#break />
                            </#switch>
                        </div>
                        <#if (item.available)!false>
                            <div class="adjust-side">
                                <div class="btn">
                                    <a href="javascript:void(0);" class="js-reject white_btn"
                                       data-aid="${item.recordId!}" data-respondent="${item.respondentId!}"
                                       data-type="${item.type!}">拒绝</a>
                                    <a href="javascript:void(0);" class="js-agree white_btn orange"
                                       data-aid="${item.recordId!}" data-respondent="${item.respondentId!}"
                                       data-type="${item.type!}">同意</a>
                                </div>
                            </div>
                        </#if>
                    </div>
                </#list>
            </#if>
        </div>
        <div>
            <#if overList?? && overList?has_content>
                <#list overList as item>
                    <div class="adjustmentExamine-box">
                        <div class="adjust-content">
                            <#switch item.type>
                                <#case "LINK">
                                    <p class="pattern" style="font-size:.7rem">加入班级<span
                                            style="float:right">${(item.createTime!0)?string("MM-dd")}</span></p>
                                    <p class="pattern"
                                       style="font-size:.6rem;color:#9199bb">${item.schoolName!} <#if item.state?? && item.state == "SUCCESS">
                                        <span style="float:right">已通过</span><#elseif item.state?? && item.state == "REJECT">
                                        <span style="float:right;color:#ff7d5a">已拒绝</span></#if></p>
                                    <p class="pattern"><span class="">${item.applicantName!}
                                        （${item.applicantSubject!}）</span>申请和<span class="">${item.respondentName!}
                                        （${item.respondentSubject!}）</span>同时教${item.clazzName!}</p>
                                    <#break />
                                <#case "REPLACE">
                                    <p class="pattern" style="font-size:.7rem">班级接管<span
                                            style="float:right">${(item.createTime!0)?string("MM-dd")}</span></p>
                                    <p class="pattern"
                                       style="font-size:.6rem;color:#9199bb">${item.schoolName!}  <#if item.state?? && item.state == "SUCCESS">
                                        <span style="float:right">已通过</span><#elseif item.state?? && item.state == "REJECT">
                                        <span style="float:right;color:#ff7d5a">已拒绝</span></#if></p>
                                    <p class="pattern"><span class="">${item.applicantName!}
                                        （${item.applicantSubject!}）</span>申请接管<span class="">${item.respondentName!}
                                        （${item.respondentSubject!}）</span>的${item.clazzName!}</p>
                                    <#break />
                                <#case "TRANSFER">
                                    <p class="pattern" style="font-size:.7rem">班级转让<span
                                            style="float:right">${(item.createTime!0)?string("MM-dd")}</span></p>
                                    <p class="pattern"
                                       style="font-size:.6rem;color:#9199bb">${item.schoolName!}  <#if item.state?? && item.state == "SUCCESS">
                                        <span style="float:right">已通过</span><#elseif item.state?? && item.state == "REJECT">
                                        <span style="float:right;color:#ff7d5a">已拒绝</span></#if></p>
                                    <p class="pattern"><span class="">${item.applicantName!}
                                        （${item.applicantSubject!}）</span>申请将${item.clazzName!}转让给<span
                                            class="">${item.respondentName!}（${item.respondentSubject!}）</span></p>
                                    <#break />
                                <#default>
                                    <#break />
                            </#switch>
                        </div>
                    </div>
                </#list>
            </#if>
        </div>
    </div>
</div>
<div class="schoolParticular-pop" style="display: none;" id="repatePane">
    <div class="inner">
        <h1>处理换班</h1>
        <p class="info">是否确认</p>
        <div class="btn">
            <a href="javascript:void(0);" class="white_btn rejectBtn">否</a>
            <a href="javascript:void(0);" class="js-submit">是</a>
        </div>
    </div>
</div>

<script>
    var AT = new agentTool();
    var data, reqData, status;
    //拒绝按钮
    $(".js-reject").on("click", function () {
        status = 2;
        data = $(this).data();
        reqData = {
            recordId: data.aid,
            respondent: data.respondent,
            alterType: data.type
        };
        $("#repatePane").show();

    });
    //同意按钮
    $(".js-agree").on("click", function () {
        status = 1;
        data = $(this).data();
        reqData = {
            recordId: data.aid,
            respondent: data.respondent,
            alterType: data.type
        };
        $("#repatePane").show();
    });
    $(document).on("click", ".rejectBtn", function () {
        $("#repatePane").hide();
    });
    $(document).on("click", ".js-submit", function () {
        $("#repatePane").hide();
        if (status == 1) {
            $.post("/mobile/resource/school/approve_alter.vpage", reqData, function (res) {
                if (res.success) {
                    status = 0;
                    AT.alert("通过成功");
                    window.location.reload();
                } else {
                    AT.alert(res.info);
                }
            });
        } else if (status == 2) {
            $.post("/mobile/resource/school/reject_alter.vpage", reqData, function (res) {
                if (res.success) {
                    status = 0;
                    AT.alert("拒绝成功");
                    window.location.reload();
                } else {
                    AT.alert(res.info);
                }
            });
        }
    });

    /*--tab切换--*/
    $(".tab-head").children("a,span").on("click", function () {
        var $this = $(this);
        $this.addClass("the").siblings().removeClass("the");
        $(".tab-main").eq(0).children().eq($this.index()).show().siblings().hide();
    });
    $('.teacherUrl').on("click", function () {
        openSecond("/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=" + $(this).data().id);
    });
</script>
</@layout.page>
