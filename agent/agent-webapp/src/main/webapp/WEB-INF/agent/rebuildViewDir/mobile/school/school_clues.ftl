<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="鉴定学校" pageJs="" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['custSer']/>
<div class="crmList-box">
    <div class="fixed-head">
        <div class="c-opts gap-line tab-head c-flex c-flex-3">
            <span class="the">审核中(${(schoolClues["待审核"])?size!0})</span>
            <span>已通过</span>
            <span>已驳回</span>
        </div>
    </div>

    <div class="tab-main">
        <!--待审核-->
        <div class="c-list">
        <#if (schoolClues["待审核"])?has_content>
            <#list schoolClues["待审核"] as schoolClue>
                <div class="clearfix js-itemBtn" data-sid="${schoolClue.id!0}">
                <p class="name"><span style="font-size:.75rem;overflow:hidden;">${schoolClue.loadSchoolFullName()!''}</span><span style="font-size:.75rem">(${schoolClue.schoolId!''})</span></p>
                <p><span style="color: #ff7d5a;font-size: .75rem;">方炎培 审核中</span></p>
                </div>
            </#list>
        </#if>
        </div>
        <!--已通过-->
        <div class="c-list">
            <#if (schoolClues["已通过"])?has_content>
                <#list schoolClues["已通过"] as schoolClue>
                    <div class="clearfix js-itemBtn" data-sid="${schoolClue.id!0}">
                        <p class="name"><span style="font-size:.75rem;overflow:hidden;">${schoolClue.loadSchoolFullName()!''}</span><span style="font-size:.75rem">(${schoolClue.schoolId!''})</span></p>
                        <p>审核人员：<span style="color: #ff7d5a;font-size: .75rem;">${schoolClue.reviewerName!""}</span></p>
                        <span class="inner-right"></span>
                    </div>
                </#list>
            </#if>
        </div>
        <!--已驳回-->
        <div class="c-list">
            <#if (schoolClues["已驳回"])?has_content>
                <#list schoolClues["已驳回"] as schoolClue>
                    <div class="clearfix js-itemBtn" data-sid="${schoolClue.id!0}">
                        <p class="name"><span style="font-size:.75rem;overflow:hidden;">${schoolClue.loadSchoolFullName()!''}</span><span style="font-size:.75rem">(${schoolClue.schoolId!''})</span></p>
                        <p>审核人员：<span style="color: #ff7d5a;font-size: .75rem;">${schoolClue.reviewerName!""}</span></p>
                        <p>驳回原因：<span style="color: #ff7d5a;font-size: .75rem;">${schoolClue.reviewNote!""}</span></p>
                        <span class="inner-right"></span>
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

    $(document).on("click",".js-itemBtn",function(){
        var id = $(this).data("sid");
        window.location.href = "school_clue_detail.vpage?id=" + id ;
    });

</script>
</@layout.page>