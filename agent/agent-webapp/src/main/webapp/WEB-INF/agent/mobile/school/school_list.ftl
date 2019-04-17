<#import "../layout_new.ftl" as layout>
<@layout.page group="搜索" >
    <#include "school_infos.ftl">
<div id="schoolList"></div>
<script type="text/javascript">
    var schoolInfos = null;
    <#if schoolInfos??>
        schoolInfos = ${schoolInfos};
    </#if>

    var region = false;
    <#if countyCode??>
        region = true;
    </#if>

    var schoolName = null;
    <#if schoolKey??>
        schoolName = "${schoolKey}";
    </#if>

    var contentHtml = template("schoolInfos", {"schoolInfos": schoolInfos, "region": region, "schoolName": schoolName});
    $("#schoolList").html(contentHtml);

    $("form:visible").on("submit", function () {
        var ele = $(this);
        var isSubmit = false;
        var schoolValue = $("#schoolKey").val();
        if (schoolValue) {
            isSubmit = true;
        } else {
            alert("请输入学校名或ID")
        }
        return isSubmit;
    });

    window.searchSchoolSumbimt = function (schoolKey) {
        $("form").attr("action", "school_list.vpage");
        $("form:visible").submit();
    };

    window.schoolItemClick = function (schoolName, sid) {
        openSecond("/mobile/school/school_info.vpage?schoolId=" + sid);
    };

    window.searchSchoolBack = function () {
        window.location.href = "/mobile/school/index.vpage";
    };
</script>
</@layout.page>