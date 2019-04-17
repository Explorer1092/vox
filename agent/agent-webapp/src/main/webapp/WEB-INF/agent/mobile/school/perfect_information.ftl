<#import "../layout_new_no_group.ftl" as layout>
<#assign groupName="work_record">
<@layout.page title="完善信息">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:void(0)" class="headerBack">&lt;&nbsp;返回</a>
            <a href="javascript:void(0);" class="headerBtn">提交</a>

            <div class="headerText">完善信息</div>
        </div>
    </div>
</div>
<form action="save_perfect_information.vpage" method="POST" id="save-perfect-information" enctype="multipart/form-data">
    <#include "../../rebuildViewDir/mobile/school/information_entry.ftl">
    <input type="hidden" id="schoolId" name="schoolId" value="${(schoolClue.schoolId)!''}">
    <input type="hidden" id="clueId" name="clueId" value="${(schoolClue.id)!''}">
    <input type="hidden" id="schoolPhase" name="schoolPhase" value="${(schoolClue.schoolClue.nicePhase.level)!''}">
</form>
    <#if errorMessage?? && errorMessage!="" >
    <div id="errorMessage" class="mobileCRM-V2-box mobileCRM-V2-info ">
    ${errorMessage!""}
    </div>
    </#if>
<script type="text/javascript">
    $(function () {
        var selects = ['externOrBoarder', 'englishStartGrade', 'schoolingLength'];
        for (var i = 0; i < selects.length; i++) {
            addChange($("#" + selects[i]));
        }
        function addChange(elem) {
            elem.change(function () {
                var name = elem.attr("name");
                $("div[name='" + name + "']").html($("#" + name + " option:selected").html());
                if (name == "schoolingLength") {
                    checkSchoolingLength();
                }
                informationFlushValue(name, elem.val());
            })
        }
    })

    function checkSchoolingLength() {
        $("#six_year").hide();
        if ($("#schoolingLength").val() == "2" || $("#schoolingLength").val() == "4") {
            $("#six_year").show();
        }
    }
    var information = {
        "schoolingLength": $("#schoolingLength").val(),
        "gradeDistribution": $("#gradeDistribution").val(),
        "englishStartGrade": $("#englishStartGrade").val(),
        "externOrBoarder": $("#externOrBoarder").val(),
        "schoolId": $("#schoolId").val(),
        "clueId": $("#clueId").val(),
        "schoolPhase": $("#schoolPhase").val()
    };

    $("#gradeDistribution").on("click", function () {
        if (mustParameters()) {
            $.post('save_Information_session.vpage', information, function (data) {
                if (data.success) {
                    window.location.href = 'grade_distribution.vpage?returnUrl=perfect_information.vpage'
                } else {
                    alert(data.info);
                }
            });
        }
    });

    function informationFlushValue(elemId, value) {
        switch (elemId) {
            case "schoolingLength":
                information.schoolingLength = value;
                break;
            case "englishStartGrade":
                information.englishStartGrade = value;
                break;
            case "externOrBoarder":
                information.externOrBoarder = value;
                break;

        }
    }

    $(".headerBtn").click(function () {
        if (validate()) {
            $("#save-perfect-information").submit();
        }
    });

    $(".headerBack").click(function () {
        $.post('save_Information_draft.vpage', information, function (data) {
            if (data.success) {
                window.location.href = "/mobile/school_clue/user_clues.vpage";
            } else {
                alert(data.info);
            }
        });
    });

    function validate() {
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
        return true;
    }

    function mustParameters() {
        if (blankString($("#schoolingLength option:selected").val())) {
            alert("请选择学校学制");
            return false;
        }
        return true;
    }
</script>
</@layout.page>