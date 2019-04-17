<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="学校增长情况" pageJs="">
    <@sugar.capsule css=['home']/>
<div class="primary-box">
    <#if RegionData??>
    <div class="c-opts gap-line">
        <#list RegionData?keys as key>
            <#assign contentItem = RegionData[key]>
        <span class="js-tab <#if key_index = 0 >active</#if>" data-index="${key}"><#if key == "Region">大区<#elseif key == "City">分区<#elseif key == "GroupCity">城市</#if></span>
        </#list>
        <span class="js-tab" data-index="BusinessDeveloperData">专员</span>
    </div>
    </#if>
    <div>
        <#if RegionData?? || BusinessDeveloperData??>
        <#--城市-->
            <div class="schoolRecord-box show_town view-box">
                <div class="srd-module">
                    <div class="mTable" style="display: block">
                        <#if RegionData??>
                        <#list RegionData?keys as key>
                            <#assign contentItem = RegionData[key]>
                                <table class="table_${key!0}" cellpadding="0" cellspacing="0" style="display: none">
                                    <thead>
                                    <tr>
                                        <td><#if key == "Region">大区<#elseif key == "City">分区<#elseif key == "GroupCity">城市</#if></td>
                                        <td class="sortable">学校总数</td>
                                        <td class="sortable">对比上月月活有增长的学校数</td>
                                        <td class="sortable">增长学校占比</td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                        <#list contentItem as item>
                                        <tr>
                                            <td>${item.name!""}</td>
                                            <td class="1">${item.allSchoolCount!0}</td>
                                            <td class="2">${item.increaseSchoolCount!0}</td>
                                            <td class="3" data-info="<#if item.allSchoolCount?? && item.allSchoolCount != 0><#else>-1</#if>"><#if item.allSchoolCount?? && item.allSchoolCount != 0>${((item.increaseSchoolCount!0)*100/(item.allSchoolCount!0))?string("#.##")}%<#else>-</#if></td>
                                        </tr>
                                        </#list>
                                    </tbody>
                                </table>
                        </#list>
                            </#if>
                        <#if BusinessDeveloperData??>
                        <#if BusinessDeveloperData?? && BusinessDeveloperData?size gt 0>
                            <table class="table_BusinessDeveloperData" cellpadding="0" cellspacing="0" style="display: none">
                                <thead>
                                <tr>
                                    <td>姓名</td>
                                    <td class="sortable">学校总数</td>
                                    <td class="sortable">对比上月月活有增长的学校数</td>
                                    <td class="sortable">增长学校占比</td>
                                </tr>
                                </thead>
                                <tbody>
                                    <#list BusinessDeveloperData as item>
                                    <tr class="js-item" data-id="${item.id!0}" data-idtype="${item.idType!""}">
                                        <td>${item.name!""}</td>
                                        <td class="1">${item.allSchoolCount!0}</td>
                                        <td class="2">${item.increaseSchoolCount!0}</td>
                                        <td class="3" data-info="<#if item.allSchoolCount?? && item.allSchoolCount != 0><#else>-1</#if>"><#if item.allSchoolCount?? && item.allSchoolCount != 0>${((item.increaseSchoolCount!0)*100/(item.allSchoolCount!0))?string("#.##")}%<#else>-</#if></td>
                                    </tr>
                                    </#list>
                                </tbody>
                            </table>
                        </#if>
                        </#if>
                    </div>
                </div>
            </div>
        </#if>
    </div>
</div>
<script src="/public/rebuildRes/js/mobile/home/sortTable.js"></script>

<script>
    $(document).on("click",".js-tab",function () {
        $(this).addClass("active").siblings().removeClass("active");
        $(".table_"+$(this).data("index")).show().siblings().hide();
    });
    $(document).on("click",".js-item",function () {
        openSecond("/mobile/resource/school/school_mau_increase_detail.vpage?id="+$(this).data("id")+"&idType="+$(this).data("idtype"));
    });
    $(document).on("click",".sortable",function () {
        var colIndex = $(this).index();
        var table = $(this).closest("table");
        $(this).addClass("active").siblings().removeClass("active");
        sortTable(table, colIndex);
    });
    $(".js-tab.active").click();

</script>
</@layout.page>
