<#import "../../rebuildViewDir/mobile/resource/layout.ftl" as layout>
<@layout.page title="处理包班申请">
<div class="crmList-box resources-box">
    <div class="c-main">
        <div>
            <div class="c-opts gap-line tab-head">
                <span class="the">待处理(<#if pendingList??>${pendingList?size!0}<#else>0</#if>)</span>
                <span>已通过(<#if successList??>${successList?size!0}<#else>0</#if>)</span>
                <span>已驳回(<#if rejectList??>${rejectList?size!0}<#else>0</#if>)</span>
            </div>
            <div class="tab-main">
                <#--待审核-->
                <div>
                    <#if pendingList??>
                        <#list pendingList as item>
                            <div class="apply-item">
                                <p style="color:#7f86ad">申<span style="padding:0 0.36rem;">请</span>人：${item.applicantName}</p>
                                <div>
                                    <span style="float:left;height:2rem;">申请内容：</span>
                                ${item.schoolName}(${item.schoolId})${item.teacherName}<span class="icon-box">
                                    <#switch item.currentSubject>
                                        <#case "CHINESE"><i class="icon-yu"></i><#break/>
                                        <#case "ENGLISH"><i class="icon-ying"></i><#break/>
                                        <#case "MATH"><i class="icon-shu"></i><#break/>
                                    </#switch></span> (${item.teacherId}) 教 ${item.applySubject.getValue()} 学科，班级：${item.clazzName}
                                </div>
                                <div class="btn-box">
                                    <span class="js-reject" data-aid="${item.id}">驳回</span>
                                    <span class="js-agree orange" data-aid="${item.id}">确认</span>
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
                                <p style="color:#7f86ad">申<span style="padding:0 0.36rem;">请</span>人：${item.applicantName}</p>
                                <div>
                                    <span style="float:left;height:2rem;">申请内容：</span>
                                ${item.schoolName}(${item.schoolId})${item.teacherName}
                                    <span class="icon-box">
                                        <#switch item.currentSubject>
                                            <#case "CHINESE"><i class="icon-yu"></i><#break/>
                                            <#case "ENGLISH"><i class="icon-ying"></i><#break/>
                                            <#case "MATH"><i class="icon-shu"></i><#break/>
                                        </#switch>
                                    </span> (${item.teacherId}) 教 <span class="orange-color">${item.applySubject.getValue()}</span> 学科，班级：${item.clazzName}
                            </div>
                        </#list>
                    </#if>
                </div>
                <#--已驳回-->
                <div>
                    <#if rejectList??>
                        <#list rejectList as item>
                            <div class="apply-item">
                                <p style="color:#7f86ad">申<span style="padding:0 0.36rem;">请</span>人：${item.applicantName}</p>
                                <div>
                                    <span style="float:left;height:2rem;">申请内容：</span>
                                ${item.schoolName}(${item.schoolId}) <br>
                                ${item.teacherName}
                                    <span class="icon-box">
                                        <#switch item.currentSubject>
                                            <#case "CHINESE"><i class="icon-yu"></i><#break/>
                                            <#case "ENGLISH"><i class="icon-ying"></i><#break/>
                                            <#case "MATH"><i class="icon-shu"></i><#break/>
                                        </#switch>
                                    </span> (${item.teacherId}) 教 <span class="orange-color">${item.applySubject.getValue()}</span> 学科，班级：${item.clazzName}
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
    //拒绝按钮
    $(".js-AuditOpinionChange").on("click",function(){
        var data=$(this).data();
        var reqData={
            applyId    : data.aid
        };
        $.post("reject_clazz_apply.vpage",reqData,function(res){
            if (res.success) {
                alert("驳回成功");
                window.location.reload();
            } else {
                alert(res.info);
            }
        });
    });
    //同意按钮
    $(".js-agree").on("click",function(){
        var data=$(this).data();
        var reqData={
            applyId    : data.aid
        };
        $.post("approve_clazz_apply.vpage",reqData,function(res){
            if (res.success) {
                alert("确认成功");
                window.location.reload();
            } else {
                alert(res.info);
            }
        });
    });
</script>
</@layout.page>