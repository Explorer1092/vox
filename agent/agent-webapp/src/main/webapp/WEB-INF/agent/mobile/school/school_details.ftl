<#import "../layout_new_no_group.ftl" as layout>
<#assign groupName="work_record">
<@layout.page title="学校详情">

<div id="contaier">
    <#include "../work_record/region_tree.ftl">
    <div class="mobileCRM-V2-header">
        <div class="inner">
            <div class="box">
                <a href="javascript:window.history.back()" class="headerBack">&lt;&nbsp;返回</a>
                <a href="javascript:void(0);" class="headerBtn">提交</a>
                <div class="headerText">学校详情</div>
            </div>
        </div>
    </div>
    <form action="save_school_detail.vpage" method="POST" id="save-school-detail" enctype="multipart/form-data">
        <#include "school_clue.ftl">
        <#include "../../rebuildViewDir/mobile/school/information_entry.ftl">
        <input type="hidden" id="schoolId" name="schoolId" value="${(schoolClue.schoolId)!''}">
        <input type="hidden" id="clueId" name="clueId" value="${(schoolClue.id)!''}">
    </form>
</div>
<div id="areaTree"></div>
<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=stpdH3wKubAUFfjRZ8ELoN2A"></script>
<script type="text/javascript">
    $(function () {
        var selects = ['schoolPhase', 'schoolType', 'externOrBoarder', 'englishStartGrade', 'schoolingLength'];
        for (var i = 0; i < selects.length; i++) {
            addChange($("#" + selects[i]));
        }
        function addChange(elem) {
            elem.change(function () {
                var name = elem.attr("name");
                $("div[name='" + name + "']").html($("#" + name + " option:selected").html());
                if (name == "schoolPhase") {
                    checkPerfectInformation();
                }
                if (name == "schoolingLength") {
                    checkSchoolingLength();
                }
                schoolDetailsFlushValue(name, elem.val());
            });
        }

        window.regionTreeOK = function (name, code) {
            $("#regionName").attr("value", name);
            $("#regionCode").attr("value", code);
            schoolDetail.regionCode = code;
            $("#contaier").show();
            $("#areaTree").hide();
        };
        window.regionTreeReturn = function () {
            $("#contaier").show();
            $("#areaTree").hide();
        };
        translateCallback();
    })

    function checkPerfectInformation() {
        $("#five_year_medical").hide();
        $("#six_year_medical").hide();
        $("#three_year_medical").hide();
        $("#four_year_medical").hide();
        $("#englishStartGrade_li").hide();
        if ($("#schoolPhase").val() == 1) {
            $("#five_year_medical").show();
            $("#six_year_medical").show();
            $("#englishStartGrade_li").show();
        } else {
            $("#three_year_medical").show();
            $("#four_year_medical").show();
            $("#englishStartGrade_li").hide();
        }
    }

    function translateCallback() {
        var lng = $(".js-lng").val();
        var lat = $(".js-lat").val();
        var geoc = new BMap.Geocoder();
        var pt = new BMap.Point(lng, lat);
        geoc.getLocation(pt, function (rs) {
            var addComp = rs.addressComponents;
            var gpsAddress = addComp.province + addComp.city + addComp.district + addComp.street + addComp.streetNumber;
            $("#address").val(gpsAddress);
        });
    }
    $("#regionName").click(function () {
        $("#contaier").hide();
        $("#areaTree").html("");
        $("#areaTree").show();
        $.ajax({
            url: "/mobile/common/user_regions.vpage",
            type: "POST",
            success: function (data) {
                var content = template("regionTreeTest", {regionTree: data});
                $("#areaTree").html(content);
            }
        })
    });
    var schoolDetail = {
        "schoolingLength": $("#schoolingLength").val(),
        "gradeDistribution": $("#gradeDistribution").val(),
        "englishStartGrade": $("#englishStartGrade").val(),
        "externOrBoarder": $("#externOrBoarder").val(),
        "schoolId": $("#schoolId").val(),
        "clueId": $("#clueId").val(),
        "schoolPhase": $("#schoolPhase").val(),
        "regionCode": $("#regionCode").val(),
        "schoolName": $("#schoolName").val(),
        "shortName": $("#shortName").val(),
        "schoolType": $("#schoolType").val(),
        "photoUrl": $("#photoUrl").val()
    }
    $("#photoShow").on("click", function () {
        $.post('save_school_detail_session.vpage', schoolDetail, function (data) {
            if (data.success) {
                window.location.href = 'school_clue_photo.vpage?returnUrl=new_school_clue.vpage';
            } else {
                alert(data.info);
            }
        });
    });
    $("#schoolName").on("click", function () {
        if (mustParameters()) {
            $.post('save_school_detail_session.vpage', schoolDetail, function (data) {
                if (data.success) {
                    window.location.href = 'edit_school_name.vpage?nameMode=fullName&schoolName=' + schoolDetail.schoolName;
                } else {
                    alert(data.info);
                }
            });
        }
    });
    $("#shortName").on("click", function () {
        if (mustParameters()) {
            $.post('save_school_detail_session.vpage', schoolDetail, function (data) {
                if (data.success) {
                    window.location.href = 'edit_school_name.vpage?nameMode=shortName&schoolName=' + schoolDetail.shortName;
                } else {
                    alert(data.info);
                }
            });
        }
    });
    $("#gradeDistribution").on("click", function () {
        if (mustParameters1()) {
            $.post('save_school_detail_session.vpage', schoolDetail, function (data) {
                if (data.success) {
                    window.location.href = 'grade_distribution.vpage?returnUrl=new_school_clue.vpage';
                } else {
                    alert(data.info);
                }
            });
        }
    });

    function schoolDetailsFlushValue(elemId, value) {
        switch (elemId) {
            case "schoolPhase":
                schoolDetail.schoolPhase = value;
                break;
            case "schoolType":
                schoolDetail.schoolType = value;
                break;
            case "schoolingLength":
                schoolDetail.schoolingLength = value;
                break;
            case "englishStartGrade":
                schoolDetail.englishStartGrade = value;
                break;
            case "externOrBoarder":
                schoolDetail.externOrBoarder = value;
                break;
        }
    }

    function checkSchoolingLength() {
        $("#six_year").hide();
        if ($("#schoolingLength").val() == "2" || $("#schoolingLength").val() == "4") {
            $("#six_year").show();
        }
    }

    $(".headerBtn").click(function () {
        if (validate()) {
            $("#save-school-detail").submit();
        }
    });

    function validate() {
        if (blankString($("#schoolPhase option:selected").val())) {
            alert("请选择学校阶段");
            return false;
        }
        if (blankString($("#regionCode").val())) {
            alert("请选择所属区域");
            return false;
        }
        if (blankString($("#schoolName").val())) {
            alert("请输入学校名称");
            return false;
        }
        if (blankString($("#shortName").val())) {
            alert("请输入学校简称");
            return false;
        }
        if (blankString($("#schoolType option:selected").val())) {
            alert("请选择学校类型");
            return false;
        }
        if (blankString($("#schoolingLength option:selected").val())) {
            alert("请选择学校学制");
            return false;
        }

        if (blankString($("#gradeDistribution").val())) {
            alert("请选择年级分布");
            return false;
        }
        if (blankString($("#englishStartGrade option:selected").val())) {
            alert("请选择英语起始年级");
            return false;
        }
        if (blankString($("#externOrBoarder option:selected").val())) {
            alert("请选择走读/寄宿");
            return false;
        }
        if ($("#schoolPhase option:selected").val() == 1 && ($("#schoolingLength option:selected").val() == 3 || $("#schoolingLength option:selected").val() == 4)){
            alert("学制与阶段不匹配");
            return false;
        }
        if ($("#schoolPhase option:selected").val() == 2 && ($("#schoolingLength option:selected").val() == 1 || $("#schoolingLength option:selected").val() == 2)){
            alert("学制与阶段不匹配");
            return false;
        }
            return true;
    }
    function mustParameters() {
        if (blankString($("#schoolPhase option:selected").val())) {
            alert("请选择学校阶段");
            return false;
        }
        if (blankString($("#regionCode").val())) {
            alert("请选择所属区域");
            return false;
        }
        return true;
    }

    function mustParameters1() {
        if (blankString($("#schoolingLength option:selected").val())) {
            alert("请选择学校学制");
            return false;
        }
        return true;
    }
</script>
</@layout.page>