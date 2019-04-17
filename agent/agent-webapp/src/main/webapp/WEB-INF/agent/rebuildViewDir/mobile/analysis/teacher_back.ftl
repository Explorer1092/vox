<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="不回流老师" pageJs="" footerIndex=1>
    <@sugar.capsule css=['analysis','res']/>
<div style="background:#f1f2f3">
    <p style="font-size:65%;padding:1.5%" >不回流老师：老师名下认证学生数量≥15，且上月布置过作业，本月至今未布置作业，建议每月10日后关注</p>
    <#if teacherList?has_content && teacherList?size gt 0>
        <#list teacherList as main>
            <div class="noflowback-list" nameId="${main.teacherId!0}">
                <div class="nfb-title">
                    <div class="nfb-right">认证学生:${main.authStudentCount!0}</div>
                    <div class="nfb-name">
                    ${main.teacherName!'--'}
                        <span class="icon-box">
                            <#if main.isMath()!false><i class="icon-shu"></i></#if>
                            <#if main.isEnglish()!false><i class="icon-ying"></i></#if>
                            <#if main.isChinese()!false><i class="icon-yu"></i></#if>
                        </span>
                    </div>
                </div>
                <#if main.teacherClazzInfoList?has_content && main.teacherClazzInfoList?size gt 0>
                    <#list main.teacherClazzInfoList as class>
                        <span class="nfb-column">${class.formalizeClazzName()!'--'}<#if class_has_next>、</#if></span>
                    </#list>
                </#if>
            </div>
        </#list>
    <#else>
        <div style="background:#fff;text-align:center;margin-top:2rem;width:100%;height:3rem;padding-top:2rem;font-size:70%" > 暂无数据 </div>
    </#if>

</div>
<script>
    function refresh(){

        url = location.href;

        var once = url.split("#");

        if (once[1] != 1) {

            url += "#1";

            self.location.replace(url);

            window.location.reload();

        }
    }

    setTimeout('refresh()', 100);
    $(document).on('click','.noflowback-list',function(){
        var nameId = $(this).attr('nameId');
        openSecond( '/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId='+nameId)
    });
</script>
</@layout.page>