<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="不活跃老师" pageJs="">
    <@sugar.capsule css=['res','home']/>
<style>
    .yearCard-box{margin:.5rem 0;padding:.5rem .75rem;background-color:#fff}
    .yearCard-box .year-column .left .name{font-size:.7rem;color:#636880}
    .yearCard-box .year-column .right .hide_btn{float:right;font-size:.7rem;color:#ff7d5a}
    .yearCard-box .year-content{padding:.375rem 0}
    .yearCard-box .year-content .right{float:right}
    .yearCard-box .year-content .right .frequent_btn{width:2.925rem;height:1.05rem;line-height:1.05rem;display:inline-block;text-align:center;font-size:.55rem;color:#fff;background-color:#ff7d5a;border-radius:1.5rem}
    .yearCard-box .year-content ul{width:70%;overflow:hidden}
    .yearCard-box .year-content ul li{float:left;width:50%;font-size:.6rem;color:#636880}
    .yearCard-box .year-content ul li .font{font-size:.8rem}
    .yearCard-box .year-content ul li .font .orange{display:inline-block;color:#ff7d5a}
    .yearCard-box .year-side{padding:.5rem 0 0 0;border-top:.05rem dashed #f0eff5;font-size:.5rem;color:#9199bb}
    .yearCard-box .year-side span{display:inline-block;padding:0 .75rem 0 0;text-overflow:ellipsis;overflow:hidden;white-space:nowrap}
    .yearCard-box .year-side span.area{width:5.875rem}
</style>
<div class="primary-box">
    <div class="schoolRecord-box" style="background:none">
        <div class="subTitle">不活跃老师：1、上月布置过作业；2、本月布置 < 上月布置； 3、本月布置 < 3 </div>
    </div>
    <div id="teacher-list" class="c-list">
        <#if unActiveTeachers?? && unActiveTeachers?size gt 0>
            <#list unActiveTeachers as item>
                <div class="teacher yearCard-box" data-sid="${item.teacherId!0}" style="cursor: pointer;">
                    <div class="year-column">
                        <div class="left">
                            <p class="name" id="detail<%= data[i].teacherId%>">${item.realName!0}(${item.teacherId!0})
                                <span>
                                    <#if item.isSchoolQuizBankAdmin?? && item.isSchoolQuizBankAdmin><i class="icon-guan"></i></#if>
                                    <#if item.subjectLeaderFlag?? && item.subjectLeaderFlag><i class="icon-zu"></i></#if>
                                    <#if item.subjects?? && item.subjects?size gt 0>
                                        <#list item.subjects as list>
                                            <i class="icon-${list!""}"></i>
                                        </#list>
                                    </#if>
                                </span>
                                <span class="icon-box">
                                    <#if item.isAmbassador?? && item.isAmbassador><i class="icon-shi"></i></#if>
                                    <#if item.isKLXTeacher?? && item.isKLXTeacher>
                                        <#if item.authState?? && item.authState == 1>
                                            <i class="icon-zheng"></i>
                                        </#if>
                                    </#if>
                                </span>
                                <span>
                                    <#if item.isRecentRegister?? && item.isRecentRegister><i class="icon-new"></i></#if>
                                </span>
                            </p>
                        </div>
                        <div style="float:right;font-size:.75rem;margin:.5rem .5rem 0 0">本月：<span style="color:#ff7d5a;">${item.tmHwSc!0}</span></div>
                    </div>
                    <div class="year-side">
                        <span>${item.regStuCount!0}注册</span><span>${item.authStudentCount!0}认证</span><span>带${item.clazzCount!0}个班</span>
                    </div>
                </div>
            </#list>
            <#else>
                <div style="width:100%;height:5rem;line-height:5rem;text-align:center;background:#fff;font-size:.75rem">
                    无不活跃老师
                </div>
        </#if>
    </div>
</div>
<script src="/public/rebuildRes/js/mobile/home/sortTable.js"></script>

<script>
    $(document).on("click",".teacher",function () {
        window.location.href = "/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=" + $(this).data("sid");
    });
    $(document).on("click",".sortable",function () {
        var colIndex = $(this).index();
        var table = $(this).closest("table");
        $(this).addClass("active").siblings().removeClass("active");
        sortTable(table, colIndex);
    });

</script>
</@layout.page>
