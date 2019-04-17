<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="学校增长情况" pageJs="">
    <@sugar.capsule css=['home']/>
<div class="primary-box">
    <div class="schoolRecord-box" style="padding:.5rem 0;">
        <div class="subTitle">学校月活增长情况：</div>
            <ul class="colList-1">
                <li style="width:33%;"><p style="color:#ff7d5a">${schoolMauIncreaseData["schoolMauIncreaseStatistics"].allSchoolCount!0}</p><div>学校总数</div></li>
                <li style="width:33%;"><p style="color:#ff7d5a">${schoolMauIncreaseData["schoolMauIncreaseStatistics"].increaseSchoolCount!0}</p><div>对比上月月活有增长的学校数</div></li>
                <li style="width:33%;"><p style="color:#ff7d5a"><#if schoolMauIncreaseData["schoolMauIncreaseStatistics"].allSchoolCount?? && schoolMauIncreaseData["schoolMauIncreaseStatistics"].allSchoolCount != 0>${((schoolMauIncreaseData["schoolMauIncreaseStatistics"].increaseSchoolCount!0)*100/(schoolMauIncreaseData["schoolMauIncreaseStatistics"].allSchoolCount!0))?string("#.##")}%<#else>-</#if></p><div>增长学校占比</div></li>
            </ul>
    </div>
    <div>
        <#if schoolMauIncreaseData["schoolMauIncreaseList"]?? && schoolMauIncreaseData["schoolMauIncreaseList"]?size gt 0>
        <#--城市-->
            <div class="schoolRecord-box show_town view-box">
                <div class="subTitle">对比上月月活明细：</div>
                <div class="srd-module">
                    <div class="mTable" style="display: block">
                        <table cellpadding="0" cellspacing="0">
                            <thead>
                            <tr>
                                <td>学校名称</td>
                                <td class="sortable">本月月活</td>
                                <td class="sortable">上月底月活</td>
                                <td class="sortable">差值</td>
                            </tr>
                            </thead>
                            <tbody>
                            <#list schoolMauIncreaseData["schoolMauIncreaseList"] as item>
                                <tr class="js-item" data-id="${item.schoolId!0}">
                                    <td>${item.schoolName!""}</td>
                                    <td class="1">${item.currentMonthMauCount!0}</td>
                                    <td class="2">${item.lastMonthMauCount!0}</td>
                                    <td class="3">${(item.currentMonthMauCount!0)-(item.lastMonthMauCount!0)}</td>
                                </tr>
                            </#list>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </#if>
    </div>
</div>
<script src="/public/rebuildRes/js/mobile/home/sortTable.js"></script>

<script>
    $(document).on("click",".js-item",function () {
        window.location.href = "un_active_teacher_list.vpage?schoolId="+$(this).data("id");
    });
    $(document).on("click",".sortable",function () {
        var colIndex = $(this).index();
        var table = $(this).closest("table");
        $(this).addClass("active").siblings().removeClass("active");
        sortTable(table, colIndex);
    });
</script>
</@layout.page>
