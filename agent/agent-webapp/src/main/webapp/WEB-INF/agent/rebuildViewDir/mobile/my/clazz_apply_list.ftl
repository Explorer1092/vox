<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="处理包班申请">
<@sugar.capsule css=['res'] />
<div class="crmList-box resources-box">
    <div class="res-top fixed-head">
        <div class="return"><a href="javascript:window.history.back()"><i class="return-icon"></i>返回</a></div>
        <span class="return-line"></span>
        <span class="res-title">处理包班申请</span>
    </div>
    <div class="c-main">
        <div>
            <div class="c-opts gap-line tab-head c-flex c-flex-3">
                <span class="the">待处理(<#if pendingList??>${pendingList?size!0}<#else>0</#if>)</span>
                <span>已通过(<#if successList??>${successList?size!0}<#else>0</#if>)</span>
                <span>已驳回(<#if rejectList??>${rejectList?size!0}<#else>0</#if>)</span>
            </div>
            <div class="tab-main" style="clear:both">
                <#--待审核-->
                <div>
                    <#if pendingList??>
                        <#list pendingList as item>
                            <div class="apply-item clearfix">
                                <p style="color:#7f86ad">申<span style="padding:0 0.36rem;">请</span>人：${item.applicantName}</p>
                                <div>
                                    <span style="float:left;height:2rem;">申请内容：</span>
                                    <div style="overflow:hidden;">
                                        ${item.schoolName!''}(${item.schoolId!0})${item.teacherName!''}<span class="icon-box">
                                        <#switch item.currentSubject>
                                            <#case "CHINESE"><i class="icon-yu"></i><#break/>
                                            <#case "ENGLISH"><i class="icon-ying"></i><#break/>
                                            <#case "MATH"><i class="icon-shu"></i><#break/>
                                        </#switch></span> (${item.teacherId!0}) 教 <#if item.applySubject??>${item.applySubject.getValue()!''}</#if> 学科，班级：${item.clazzName!''}
                                    </div>
                                </div>
                                <div class="btn-box c-flex c-flex-2">
                                    <div>
                                        <span class="js-reject btn-stroke fix-width center" data-aid="${item.id!0}">驳回</span>
                                    </div>
                                    <div>
                                        <span class="js-agree the btn-stroke fix-width center" data-aid="${item.id!0}">确认</span>
                                    </div>
                                </div>
                            </div>
                        </#list>
                    </#if>
                </div>
                <#--已通过-->
                <div>
                    <#if successList??>
                        <#list successList as item>
                            <div class="apply-item">
                                <p style="color:#7f86ad">申<span style="padding:0 0.36rem;">请</span>人：${item.applicantName!''}</p>
                                <div>
                                    <span style="float:left;height:2rem;">申请内容：</span>
                                ${item.schoolName!''}(${item.schoolId!0})${item.teacherName!''}
                                    <span class="icon-box">
                                        <#switch item.currentSubject>
                                            <#case "CHINESE"><i class="icon-yu"></i><#break/>
                                            <#case "ENGLISH"><i class="icon-ying"></i><#break/>
                                            <#case "MATH"><i class="icon-shu"></i><#break/>
                                        </#switch>
                                    </span> (${item.teacherId!0}) 教 <span class="orange-color"><#if item.applySubject??>${item.applySubject.getValue()!''}</#if></span> 学科，班级：${item.clazzName!''}
                                </div>
                            </div>
                        </#list>
                    </#if>
                </div>
                <#--已驳回-->
                <div>
                    <#if rejectList??>
                        <#list rejectList as item>
                            <div class="apply-item">
                                <p style="color:#7f86ad">申<span style="padding:0 0.36rem;">请</span>人：${item.applicantName!''}</p>
                                <div>
                                    <span style="float:left;height:2rem;">申请内容：</span>
                                ${item.schoolName!''}(${item.schoolId!0}) <br>
                                ${item.teacherName!''}
                                    <span class="icon-box">
                                        <#switch item.currentSubject>
                                            <#case "CHINESE"><i class="icon-yu"></i><#break/>
                                            <#case "ENGLISH"><i class="icon-ying"></i><#break/>
                                            <#case "MATH"><i class="icon-shu"></i><#break/>
                                        </#switch>
                                    </span> (${item.teacherId!0}) 教 <span class="orange-color"><#if item.applySubject??>${item.applySubject.getValue()!''}</#if></span> 学科，班级：${item.clazzName!''}
                                </div>
                            </div>
                        </#list>
                    </#if>
                </div>
            </div>
        </div>
    </div>
</div>
<script>

    var AT = new agentTool();
    //拒绝按钮
    $(".js-AuditOpinionChange").on("click",function(){
        var data=$(this).data();
        var reqData={
            applyId    : data.aid
        };
        $.post("/mobile/resource/teacher/reject_clazz_apply.vpage",reqData,function(res){
            if (res.success) {
                AT.alert("驳回成功");
                window.location.reload();
            } else {
                AT.alert(res.info);
            }
        });
    });
    //同意按钮
    $(".js-agree").on("click",function(){
        var data=$(this).data();
        var reqData={
            applyId    : data.aid
        };
        $.post("/mobile/resource/teacher/approve_clazz_apply.vpage",reqData,function(res){
            if (res.success) {
                AT.alert("确认成功");
                window.location.reload();
            } else {
                AT.alert(res.info);
            }
        });
    });
</script>
</@layout.page>