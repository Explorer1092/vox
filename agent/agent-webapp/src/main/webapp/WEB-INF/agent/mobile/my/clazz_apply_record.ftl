<#import "../../rebuildViewDir/mobile/layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="包班申请记录" footerIndex=4>
<@sugar.capsule css=['res'] />
<div class="crmList-box resources-box">
    <div class="fixed-head">
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
                    <div class="record-item">
                        <p><var style="font-style: normal;font-size:0.89rem;">${item.teacherName!}老师</var>
                            <span class="icon-box">
                                <#switch item.currentSubject>
                                    <#case "CHINESE"><i class="icon-yu"></i><#break/>
                                    <#case "ENGLISH"><i class="icon-ying"></i><#break/>
                                    <#case "MATH"><i class="icon-shu"></i><#break/>
                                </#switch>
                            </span>
                            (${item.teacherId!}) ${item.schoolName!}
                        </p>
                        <p>申请教 <span class="orange-color">${item.applySubject.getValue()!}</span> 学科</p>
                        <p>班级：${item.clazzName!}</p>
                    </div>
                </#list>
            </#if>
        </div>
        <#--已通过-->
        <div>
            <#if successList??>
                <#list successList as item>
                    <div class="record-item">
                        <p><var style="font-style: normal;font-size:0.89rem;">${item.teacherName!}老师</var>
                            <span class="icon-box">
                                <#switch item.currentSubject>
                                    <#case "CHINESE"><i class="icon-yu"></i><#break/>
                                    <#case "ENGLISH"><i class="icon-ying"></i><#break/>
                                    <#case "MATH"><i class="icon-shu"></i><#break/>
                                </#switch>
                            </span>
                            (${item.teacherId!}) ${item.schoolName!}
                        </p>
                        <p>申请教 <span class="orange-color">${item.applySubject!}</span> 学科</p>
                        <p>班级：${item.clazzName!}</p>
                    </div>
                </#list>
            </#if>
        </div>
        <#--已驳回-->
        <div>
            <#if rejectList??>
                <#list rejectList as item>
                    <div class="record-item">
                        <p><var style="font-style: normal;font-size:0.89rem;">${item.teacherName!}老师</var>
                            <span class="icon-box">
                                <#switch item.currentSubject>
                                    <#case "CHINESE"><i class="icon-yu"></i><#break/>
                                    <#case "ENGLISH"><i class="icon-ying"></i><#break/>
                                    <#case "MATH"><i class="icon-shu"></i><#break/>
                                </#switch>
                            </span>
                            (${item.teacherId!}) ${item.schoolName!}
                        </p>
                        <p>申请教 <span class="orange-color">${item.applySubject!}</span> 学科</p>
                        <p>班级：${item.clazzName!}</p>
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