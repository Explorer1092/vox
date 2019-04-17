<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="老师包班申请" footerIndex=4  navBar="hidden">
<@sugar.capsule css=['res'] />
<div class="crmList-box resources-box">
    <div class="fixed-head" style="background:#fff">
        <!--筛选选项-->
        <div class="c-opts gap-line tab-head c-flex c-flex-3">
            <span class="the">待审核(<#if pendingList??>${pendingList?size!0}<#else>0</#if>)</span>
            <span>已通过(<#if successList??>${successList?size!0}<#else>0</#if>)</span>
            <span>已驳回(<#if rejectList??>${rejectList?size!0}<#else>0</#if>)</span>
        </div>
    </div>

    <div class="tab-main">
        <#--待审核-->
        <div>
            <#if pendingList??>
                <#list pendingList as item>
                    <div class="record-item" style="background:#fff;padding:.2rem 1rem 0 1rem;margin-top:.5rem">
                        <p><var style="font-style: normal;font-size:0.75rem;">${item.teacherName!}老师</var>(${item.teacherId!})
                            <span class="icon-box">
                                <#switch item.currentSubject>
                                    <#case "CHINESE"><i class="icon-yu"></i><#break/>
                                    <#case "ENGLISH"><i class="icon-ying"></i><#break/>
                                    <#case "MATH"><i class="icon-shu"></i><#break/>
                                </#switch>
                            </span>
                            申请教 <span class="">${item.applySubject.getValue()!}学科,${item.clazzName!}</span>
                        </p>
                        <p>审核人员：<span style="color: #ff7d5a;font-size: .75rem;"><#if userManager??>${userManager.realName!}</#if></span></p>
                    </div>
                </#list>
            </#if>
        </div>
        <#--已通过-->
        <div>
            <#if successList??>
                <#list successList as item>
                    <div class="record-item" style="background:#fff;padding:.2rem 1rem 0 1rem;margin-top:.5rem">
                        <p><var style="font-style: normal;font-size:0.75rem;">${item.teacherName!}老师</var>(${item.teacherId!})
                            <span class="icon-box">
                                <#switch item.currentSubject>
                                    <#case "CHINESE"><i class="icon-yu"></i><#break/>
                                    <#case "ENGLISH"><i class="icon-ying"></i><#break/>
                                    <#case "MATH"><i class="icon-shu"></i><#break/>
                                </#switch>
                            </span>
                            申请教 <span class="">${item.applySubject.getValue()!}学科,${item.clazzName!}</span>
                        </p>
                        <p>审核人员：<span style="color: #ff7d5a;font-size: .75rem;">${item.auditorName!}</span></p>
                    </div>
                </#list>
            </#if>
        </div>
        <#--已驳回-->
        <div>
            <#if rejectList??>
                <#list rejectList as item>
                    <div class="record-item" style="background:#fff;padding:.2rem 1rem 0 1rem;margin-top:.5rem">
                        <p><var style="font-style: normal;font-size:0.75rem;">${item.teacherName!}老师</var>(${item.teacherId!})
                            <span class="icon-box">
                                <#switch item.currentSubject>
                                    <#case "CHINESE"><i class="icon-yu"></i><#break/>
                                    <#case "ENGLISH"><i class="icon-ying"></i><#break/>
                                    <#case "MATH"><i class="icon-shu"></i><#break/>
                                </#switch>
                            </span>
                             申请教 <span class="">${item.applySubject.getValue()!}学科,${item.clazzName!}</span>
                        </p>
                        <p>审核人员：<span style="color: #ff7d5a;font-size: .75rem;">${item.auditorName!''}</span></p>
                    </div>
                </#list>
            </#if>
        </div>
    </div>
</div>
<script>
    /*--tab切换--*/
    $(".tab-head").children("a,span").on("click",function(){
        var $this=$(this);
        $this.addClass("the").siblings().removeClass("the");
        $(".tab-main").eq(0).children().eq($this.index()).show().siblings().hide();
    });
</script>
</@layout.page>