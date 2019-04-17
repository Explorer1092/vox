<#import "../layout_new_no_group.ftl" as layout>
<#assign groupName="work_record">
<@layout.page title="学校详情">

<div id="contaier">
    <#include "../work_record/region_tree.ftl">
    <div class="mobileCRM-V2-header">
        <div class="inner">
            <div class="box">
                <#if (clueType!"")=="newClue">
                    <a href="/mobile/performance/index.vpage" class="headerBack">&lt;&nbsp;返回</a>
                <#else>
                    <a href="javascript:window.history.back()" class="headerBack">&lt;&nbsp;返回</a>
                </#if>
               <#if !canModification?? || canModification>
                    <a href="javascript:void(0);" class="headerBtn"><#if (clueType!"")=="newClue">创建<#else>提交</#if></a>
                </#if>
                <div class="headerText"><#if (clueType!"")=="newClue">添加学校<#elseif (clueType!"")=="schoolDetail">
                    学校详情<#else>编辑学校信息</#if></div>
            </div>
        </div>
    </div>
    <form action="save_school_clue.vpage" method="POST" id="save-school-detail" enctype="multipart/form-data">
        <#include "school_clue.ftl">
        <#include "../../rebuildViewDir/mobile/school/information_entry.ftl">
        <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
            <ul class="mobileCRM-V2-list js-gradeList">
                <li id="gradeDistribution">
                    <div class="link link-ico">
                        <div class="side-fl">年级分布</div>
                        <#if clueType?? && clueType =="editClue">
                            <div class="side-fl side-orange">&nbsp;*</div>
                        </#if>
                    </div>
                </li>
                <#include "grade_distribution_info.ftl">
            </ul>

        <#--实在不得已的方式-->
            <#assign GradeList = [1,2,3,4,5,6,7,8,9]>
            <#list GradeList as list>
                <#assign listKey = "grade"+list+"StudentCount">
                <#if schoolClue[listKey]??>
                    <input type="hidden" value="${schoolClue[listKey]}" index="${list}" class="js-gradeValue">
                </#if>
            </#list>
        </div>
        <#if branchSchool?? && gradeDistribution?? && gradeDistribution>
            <#include "../../rebuildViewDir/mobile/school/branch_school_list_info.ftl">
        </#if>
        <input type="hidden" id="schoolId" name="schoolId" value="${(schoolClue.schoolId)!''}">
        <input type="hidden" id="clueId" name="clueId" value="${(schoolClue.id)!''}">
        <input type="hidden" id="clueType" name="clueType" value="${clueType!''}">
    </form>
</div>
<div id="areaTree"></div>
<script type="text/javascript">


    $(function () {
        var setAllSelectDisplay = function (elem) {
            var name = elem.attr("name");
            $("div[name='" + name + "Display']").html($("#" + name + " option:selected").html());
        };
        <#if canModification?? && !canModification>
            $("#schoolPhase").attr("disabled", true);
            $("#schoolType").attr("disabled",true);
            $("#externOrBoarder").attr("disabled", true);
            $("#schoolingLength").attr("disabled", true);
            $("#englishStartGrade").attr("disabled", true);
        <#else>
            var selects = ['schoolPhase', 'schoolType', 'externOrBoarder', 'englishStartGrade', 'schoolingLength'];
            for (var i = 0; i < selects.length; i++) {
                addChange($("#" + selects[i]));

                setAllSelectDisplay($("#" + selects[i]));
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
        </#if>
        //设置select显示值


        function addChange(elem) {
            elem.change(function () {
                var name = elem.attr("name");
                setAllSelectDisplay(elem);

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
    });
    var schoolDetail = {};
    var getRealTimeDetail = function () {
        var schooleNameVal = $("#schoolName").html();
        var schooleShortNameVal = $("#shortName").html();
        if (schooleNameVal == "请填写") {
            schooleNameVal = "";
        }
        if (schooleShortNameVal == "请填写") {
            schooleShortNameVal = "";
        }
        schoolDetail = {
            "clueType": $("#clueType").val(),
            "schoolingLength": $("#schoolingLength").val(),
            "gradeDistribution": $("#gradeDistribution").val(),
            "englishStartGrade": $("#englishStartGrade").val(),
            "externOrBoarder": $("#externOrBoarder").val(),
            "schoolId": $("#schoolId").val(),
            "clueId": $("#clueId").val(),
            "schoolPhase": $("#schoolPhase").val(),
            "regionCode": $("#regionCode").val(),
            "schoolName": schooleNameVal,
            "shortName": schooleShortNameVal,
            "schoolType": $("#schoolType").val(),
            "photoUrl": $("#photoUrl").val()
        };
    };
        <#if !canModification?? || canModification>
        $("#photoShow").on("click", function () {
            getRealTimeDetail();
            $.post('save_school_clue_session.vpage', schoolDetail, function (data) {
                if (data.success) {
                    window.location.href = 'school_clue_photo.vpage?returnUrl=<#if (clueType!"")=="newClue">new_school_clue.vpage<#elseif (clueType!"")=="againApply">again_apply_school_clue.vpage<#elseif (clueType!"")=="modification">modification_school_clue.vpage<#else >new_school_detail.vpage?schoolId=${(schoolClue.schoolId)!''}</#if>';
                } else {
                    //可以在这里删除年级分布的list
                }
            });
        });
        $("#schoolName").on("click", function () {
            getRealTimeDetail();
            if (mustParameters()) {
                $.post('save_school_clue_session.vpage', schoolDetail, function (data) {
                    if (data.success) {
                        window.location.href = 'edit_school_name.vpage?nameMode=fullName&schoolName=' + schoolDetail.schoolName;
                    } else {
                        alert(data.info);
                    }
                });
            }
        });
        $("#shortName").on("click", function () {
            getRealTimeDetail();
            if (mustParameters()) {
                $.post('save_school_clue_session.vpage', schoolDetail, function (data) {
                    if (data.success) {
                        window.location.href = 'edit_school_name.vpage?nameMode=shortName&schoolName=' + schoolDetail.shortName;
                    } else {
                        alert(data.info);
                    }
                });
            }
        });
        $("#gradeDistribution").on("click", function () {
            var schoolingLength = $("#schoolingLength").val();
            getRealTimeDetail();
            if (mustParameters1()) {
                $.post('save_school_clue_session.vpage', schoolDetail, function (data) {
                    if (data.success) {
                        window.location.href = 'grade_distribution.vpage?schoolingLength=' + schoolingLength + '&returnUrl=<#if (clueType!"")=="newClue">new_school_clue.vpage<#elseif (clueType!"")=="againApply">again_apply_school_clue.vpage<#elseif (clueType!"")=="modification">modification_school_clue.vpage<#else >new_school_detail.vpage?schoolId=${(schoolClue.schoolId)!''}</#if>'
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        $("#addBranchSchool").on("click", function () {
            getRealTimeDetail();
            $.post('save_school_clue_session.vpage', schoolDetail, function (data) {
                if (data.success) {
                    window.location.href = '/mobile/school_clue/branch_school_list.vpage';
                } else {
                    alert(data.info);
                }
            });
        });
        </#if>
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

    var modal = {
        "1": 5,
        "2": 6,
        "3": 3,
        "4": 4
    };

    function checkPerfectInformation() {
        $("#schoolingLength").empty();
        var spv = $("#schoolPhase").val();
        if (spv == 1) {
            $("#schoolingLength").append('<option value="0">请选择</option><option value="1">小学五年制</option><option value="2">小学六年制</option>');
            $("#englishStartGrade_li").show();
        } else {
            $("#schoolingLength").append('<option value="0">请选择</option><option value="3">中学三年制</option><option value="4">中学四年制</option>');
        }

        $("#schoolingLength").val("0");
        $('[name="schoolingLengthDisplay"]').html($("#schoolingLength option:selected").text());
    }

    var changePrimaryState = function () {
        $("#schoolingLength").append('<option value="0">请选择</option><option value="1">小学五年制</option><option value="2">小学六年制</option>');
        $("#englishStartGrade_li").show();
    };

    var changeMiddleState = function () {
        $("#schoolingLength").append('<option value="0">请选择</option><option value="3">中学三年制</option><option value="4">中学四年制</option>');
        //控制english start grade
        $("#englishStartGrade_li").hide();
    };

    function checkSchoolingLength() {
        var slv = $("#schoolingLength option:selected").val();

        $('#englishStartGrade>option[value="6"]').remove();
        if (slv == "2" || slv == "4") {
            $('#englishStartGrade').append('<option value="6">六年级</option>');
        }

        $("#schoolingLength").empty();

        if (slv == "1" || slv == "2") {
            $("#schoolPhase").val("1");
            changePrimaryState();

        } else if (slv == "3" || slv == "4") {
            $("#schoolPhase").val("2");
            changeMiddleState();
        } else {
            if ($("#schoolPhase option:selected").val() == 1) {
                changePrimaryState();
            } else {
                changeMiddleState();
            }
        }

        $("#schoolingLength").val(slv);
        if (slv == 0) {
            $('[name="schoolingLengthDisplay"]').html("请选择");
        } else {
            $('[name="schoolingLengthDisplay"]').html($("#schoolingLength option:selected").text());
        }
        $('[name="schoolPhaseDisplay"]').html($("#schoolPhase option:selected").text());

        //清除学制缓存
        $.post("drop_grade_distribution.vpage", {}, function (data) {
            if (data.success) {
                $("#gradeDistribution").siblings('li').hide();
            }
        });
    }
        <#if !canModification?? || canModification>
        $(".headerBtn").click(function () {
            getRealTimeDetail();
            if (validate()) {
                if (confirm("提交后，学校基础信息将不可修改！！！是否确认？")) {
                    $.post("/mobile/school_clue/upsert_school_clue.vpage", schoolDetail, function (result) {
                        if (result.success) {
                            window.history.back();
                        } else {
                            alert(result.info);
                        }
                    });

                }
            }
        });
        </#if>

function validate() {
 if (blankStringOrZero($("#schoolPhase option:selected").val())) {
     alert("请选择学校阶段");
     return false;
 }
 if (blankString($("#regionCode").val())) {
     alert("请选择所属区域");
     return false;
 }
 if (blankString($("#schoolName").html())) {
     alert("请输入学校名称");
     return false;
 }
 if (blankString($("#shortName").html())) {
     alert("请输入学校简称");
     return false;
 }
 if (blankStringOrZero($("#schoolType option:selected").val())) {
     alert("请选择学校性质");
     return false;
 }
 if (blankStringOrZero($("#schoolingLength option:selected").val())) {
     alert("请选择学校学制");
     return false;
 }

 //学校编辑页未认证学校如果有照片就一定要有经纬度
 <#if (clueType!"")=="newClue">
     if (blankString($("#photoUrl").val())) {
         alert("请添加学校正门照片");
         return false;
     }
     if (blankString($("#lat").val()) || blankString($("#lng").val())) {
         alert("请上传带有经纬度的照片");
         return false;
     }
 </#if>

 //验证阶段和学制
 if (blankString($("#gradeDistribution").val())) {
     alert("请选择年级分布");
     return false;
 }

 var mustBranchSchoolCount = 0;

 //年级分布
 if (true) {
     var grvs = $(".js-gradeValue"), leastGrade = false;
     $.each(grvs, function (i, item) {
         if (!($(item).val() == "" || $(item).val() == "0")) {
             leastGrade = true;
             mustBranchSchoolCount++;
         }
     });
     if (!leastGrade) {
         alert("请选择年级分布");
         return leastGrade;
     }
 }

 //分校验证
 var slv = $("#schoolingLength option:selected").val();
 var bsItem = $(".js-branchSchoolItem").length;

 if ((mustBranchSchoolCount < parseInt(modal[slv]))) {
     if (bsItem <= 0) {
         //alert("学制和年级分布不一致，请添加分校");
         //return false;
     }
 }

 if (blankStringOrZero($("#englishStartGrade option:selected").val()) && $("#schoolPhase option:selected").val() == 1) {
     alert("请选择英语起始年级");
     return false;
 }
 if (blankStringOrZero($("#externOrBoarder option:selected").val())) {
     alert("请选择走读/寄宿");
     return false;
 }
 if ($("#schoolPhase option:selected").val() == 1 && ($("#schoolingLength option:selected").val() == 3 || $("#schoolingLength option:selected").val() == 4)) {
     alert("学制与阶段不匹配");
     return false;
 }
 if ($("#schoolPhase option:selected").val() == 2 && ($("#schoolingLength option:selected").val() == 1 || $("#schoolingLength option:selected").val() == 2)) {
     alert("学制与阶段不匹配");
     return false;
 }
 return true;
}
function mustParameters() {
 if (blankStringOrZero($("#schoolPhase option:selected").val())) {
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
 if (blankStringOrZero($("#schoolingLength option:selected").val())) {
     alert("请选择学校学制");
     return false;
 }
 return true;
}

var splInitVal = $("#schoolPhase").val();

if (splInitVal == 0 || splInitVal == 2) {
 $("#englishStartGrade_li").hide();
}

var slInitVal = $("#schoolingLength").val();

if (slInitVal == "1" || slInitVal == "2") {
 $('#schoolingLength>option[value="3"]').remove();
 $('#schoolingLength>option[value="4"]').remove();
} else if (slInitVal == "3" || slInitVal == "4") {
 $('#schoolingLength>option[value="1"]').remove();
 $('#schoolingLength>option[value="2"]').remove();
} else if (slInitVal == "0") {
 if (splInitVal == 1) {
     $('#schoolingLength>option[value="3"]').remove();
     $('#schoolingLength>option[value="4"]').remove();
 } else if (splInitVal == 2) {
     $('#schoolingLength>option[value="1"]').remove();
     $('#schoolingLength>option[value="2"]').remove();
 }
}

</script>
</@layout.page>