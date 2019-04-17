<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>

<@layout.page title="访后未布置作业老师" pageJs="" footerIndex=3 navBar="hidden">
    <@sugar.capsule css=['new_home','notice']/>
<style>
    body{
        background-color:rgb(241, 242, 245) ;
    }
</style>
<div class="flow" style="background-color:#f1f2f5;">
    <#if list?? && list?size gt 0>
        <#list list as an>
            <div class="examineNotice-box">
                <div class="examineTitle">
                ${an.schoolName!''}<#if an.visitTime?has_content && an.visitTime?size gt 0><#list an.visitTime as visit>【${visit!""}】</#list></#if>
                </div>
                <div class="examineSide">
                    <div class="subTitle">
                        <#if an.teacherInfo?has_content && an.teacherInfo?size gt 0><#list an.teacherInfo as teacherInfo>${teacherInfo.teacherName!""}<#if teacherInfo_has_next>、</#if></#list></#if>
                    </div>
                </div>
            </div>
        </#list>
    <#else >
        <div style="width:100%;height:5rem;text-align: center;line-height:5rem;background: #fff">
            暂无数据
        </div>
    </#if>
</div>
<script>

    $('.subTitle').each(function(){
        var pattern = $(this).html().trim();
        $(this).html(pattern.replace(/\n/g, '<br />'));
    });
    $(document).ready(function(){
        var notifyIdsStr = "";
        $("input[name='js-ipt']").each(function(){
            notifyIdsStr += $(this).val() + ",";
        });
    });
</script>
</@layout.page>
