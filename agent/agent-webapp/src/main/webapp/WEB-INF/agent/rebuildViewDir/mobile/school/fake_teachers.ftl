<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="老师判假申请" pageJs="" footerIndex=2 navBar="hidden">
    <@sugar.capsule css=['custSer']/>
<div class="crmList-box">
    <div class="fixed-head">
        <div class="res-top">
            <div class="return"><a href="javascript:window.history.back()"><i class="return-icon"></i>返回</a></div>
            <span class="return-line"></span>
            <span class="res-title">老师判假申请</span>
        </div>
        <div class="c-opts gap-line tab-head c-flex c-flex-3">
            <span class="the">待审核(${(fakeTeachers.WAIT)?size!0})</span>
            <span>已同意(${(fakeTeachers.PASS)?size!0})</span>
            <span>已驳回(${(fakeTeachers.REJECT)?size!0})</span>
        </div>
    </div>

    <div class="tab-main">
        <#--待审核-->
        <div class="c-list">
            <#if (fakeTeachers.WAIT)?has_content>
                <#list fakeTeachers.WAIT as fake>
                    <div class="clearfix js-itemBtn">
                        <p class="name">${fake.teacherName!} (${fake.teacherId!})</p>
                        <p>
                        <div class="personalInfo">审核人员：<span style="color: #ff7d5a;font-size: .75rem;">张立宁、方炎培</span></div>
                        </p>
                    </div>
                </#list>
            </#if>
        </div>
        <!--已通过-->
        <div class="c-list">
            <#if (fakeTeachers.PASS)?has_content>
                <#list fakeTeachers.PASS as fake>
                    <div class="clearfix js-itemBtn">
                        <p class="name">${fake.teacherName!} (${fake.teacherId!})</p>
                        <p>
                            <div class="personalInfo">审核人员：<span style="color: #ff7d5a;font-size: .75rem;">${fake.reviewerName!}</span></div>
                        </p>
                    </div>
                </#list>
            </#if>
        </div>
        <!--已驳回-->
        <div class="c-list">

            <#if (fakeTeachers.REJECT)?has_content>
                <#list fakeTeachers.REJECT as fake>
                    <div class="clearfix js-itemBtn">
                        <p class="name">${fake.teacherName!} (${fake.teacherId!})</p>
                        <p>
                        <div class="personalInfo">审核人员：<span style="color: #ff7d5a;font-size: .75rem;">${fake.reviewerName!}</span></div>
                        </p>
                        <p>
                            <div>驳回原因：${fake.reviewNote!}</div>
                        </p>
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

    $(document).on("click",".js-reasonBtn",function(){
        var id = $(this).data("sid");
        $("#view-" + id).toggle("fast");
    });

</script>
</@layout.page>