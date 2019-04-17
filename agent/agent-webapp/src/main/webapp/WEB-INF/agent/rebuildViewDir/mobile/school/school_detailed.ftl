<#import "../../../mobile/layout_new_no_group.ftl" as layout>
<#assign groupName="work_record">
<@layout.page title="学校基础信息">
    <#assign schoolClass2 = (classCount1!0)?int + (classCount2!0)?int + (classCount3!0)?int + (classCount4!0)?int + (classCount5!0)?int + (classCount6!0)?int>
    <#assign schoolClass1 = (classCount1!0)?int + (classCount2!0)?int + (classCount3!0)?int + (classCount4!0)?int + (classCount5!0)?int>
    <#assign schoolClass3 =  (classCount6!0)?int + (classCount7!0)?int + (classCount8!0)?int + (classCount9!0)?int >
    <#assign schoolClass4 =  (classCount7!0)?int + (classCount8!0)?int + (classCount9!0)?int >
    <#assign schoolClass5 =  (classCount10!0)?int + (classCount11!0)?int + (classCount12!0)?int >
    <#assign schoolMain1 = (studentCount1!0)?int + (studentCount2!0)?int + (studentCount3!0)?int + (studentCount4!0)?int + (studentCount5!0)?int>
    <#assign schoolMain2 = (studentCount1!0)?int + (studentCount2!0)?int + (studentCount3!0)?int + (studentCount4!0)?int + (studentCount5!0)?int + (studentCount6!0)?int>
    <#assign schoolMain3 = (studentCount6!0)?int + (studentCount7!0)?int + (studentCount8!0)?int + (studentCount9!0)?int>
    <#assign schoolMain4 = (studentCount7!0)?int + (studentCount8!0)?int + (studentCount9!0)?int>
    <#assign schoolMain5 = (studentCount10!0)?int + (studentCount11!0)?int + (studentCount12!0)?int>
<div id="contaier">
    <div class="mobileCRM-V2-header">
        <div class="inner">
            <div class="box">
                <a href="/mobile/resource/school/card.vpage?schoolId=${schoolId!0}&toSchoolList=toSchoolList" class="headerBack">&lt;&nbsp;返回</a>
                <a href="javascript:void(0);" class="headerBtn"><#if locked?? && !locked>提交</#if></a>
                <div class="headerText">学校基础信息</div>
            </div>
        </div>
    </div>

    <form action="save_school_clue.vpage" method="POST" id="save-school-detail" enctype="multipart/form-data">
        <#include "information_entry.ftl">
        <#include "branch_school_list_info.ftl">
    </form>
</div>
<div id="areaTree"></div>
<script type="text/javascript">

    var level = {};
    var gradeInfo = [];
    $(function () {
        var setAllSelectDisplay = function (elem) {
            var name = elem.attr("name");
            $("div[name='" + name + "Display']").html($("#" + name + " option:selected").html());
        };
        var selects = ['englishStartGrade', 'schoolingLength'];
            for (var i = 0; i < selects.length; i++) {
                addChange($("#" + selects[i]));

                setAllSelectDisplay($("#" + selects[i]));
            }

        //设置select显示值


        function addChange(elem) {
            elem.change(function () {
                var name = elem.attr("name");
                setAllSelectDisplay(elem);
                if (name == "schoolingLength") {
                    checkSchoolingLength();
                }
            });
        }
    });

        $(".save_session").on("click", function () {
            getLevel();
            var data = $(this).attr("data-value");
                $.ajax({
                    type: 'post',
                    url : "save_school_basic_info_session.vpage",
                    dataType:'json',
                    contentType:'application/json;charset=UTF-8',
                    data:JSON.stringify(level),
                    success:function(res){
                        if(res.success){
                            if (data == "add_school") {
                                window.location.href = '/mobile/school_clue/branch_school_list.vpage';
                            } else {
                                window.location.href = 'find_branch_school.vpage?schoolId=' + data;
                            }
                        }else{
                            alert(res.info);
                        }
                    },
                    error:function(){
                        alert("信息保存失败")
                    }
                });
        });

    /*function schoolDetailsFlushValue(elemId, value) {
        switch (elemId) {
            case "schoolingLength":
                schoolDetail.schoolingLength = value;
                break;
            case "englishStartGrade":
                schoolDetail.englishStartGrade = value;
                break;
        }
    }*/
//
//    var modal = {
//        "1": 5,
//        "2": 6,
//        "3": 3,
//        "4": 4
//    };

//    function checkPerfectInformation() {
//        $("#schoolingLength").empty();
//        var spv = $("#schoolPhase").val();
//        if (spv == 1) {
//            $("#schoolingLength").append('<option value="0">请选择</option><option value="1">小学五年制</option><option value="2">小学六年制</option>');
//            $("#englishStartGrade_li").show();
//        } else {
//            $("#schoolingLength").append('<option value="0">请选择</option><option value="3">中学三年制</option><option value="4">中学四年制</option>');
//        }
//
//        $("#schoolingLength").val("0");
//        $('[name="schoolingLengthDisplay"]').html($("#schoolingLength option:selected").text());
//    }

    var changePrimaryState = function () {
        $("#schoolingLength").append('<option value="0">请选择</option><option value="1">小学五年制</option><option value="2">小学六年制</option>');
        $("#englishStartGrade_li").show();
    };
//
    var changeMiddleState = function () {
        $("#schoolingLength").append('<option value="0">请选择</option><option value="3">中学三年制</option><option value="4">中学四年制</option>');
        $('schoolingLength04').show();
        //控制english start grade
        $("#englishStartGrade_li").hide();
    };


    var gradeLevel = function(){
        var slv = $("#schoolingLength option:selected").val();


        //显示年级分布
        if(slv == '0'){
            $('.showLength01').addClass('noLength').siblings().addClass('noLength');
        }else if(slv == '1'){
            $('.showLength01').addClass("fixLength").removeClass('noLength').siblings().addClass('noLength').removeClass('fixLength');
        }else if(slv == '2'){
            $('.showLength02').addClass("fixLength").removeClass('noLength').siblings().addClass('noLength').removeClass('fixLength');
        }else if(slv == '3'){
            $('.showLength03').addClass("fixLength").removeClass('noLength').siblings().addClass('noLength').removeClass('fixLength');
        }else if(slv == '4'){
            $('.showLength04').addClass("fixLength").removeClass('noLength').siblings().addClass('noLength').removeClass('fixLength');
        }else if(slv == '5'){
            $('.showLength05').addClass("fixLength").removeClass('noLength').siblings().addClass('noLength').removeClass('fixLength');
        }
    };
        gradeLevel();
    function checkSchoolingLength() {
        var slv = $("#schoolingLength option:selected").val();
        gradeLevel();

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
//        $.post("drop_grade_distribution.vpage", {}, function (data) {
//            if (data.success) {
//                $("#gradeDistribution").siblings('li').hide();
//            }
//        });
    }
        $(".headerBtn").click(function () {
            //getRealTimeDetail();
            getLevel();
            //小学6年级

            $.ajax({
                    type: 'post',
                    url : "save_school_basic_info.vpage",
                    dataType:'json',
                    contentType:'application/json;charset=UTF-8',
                    data:JSON.stringify(level),
                    success:function(res){
                        if(res.success){
                            alert('信息保存成功');
                            window.location.href="/mobile/resource/school/card.vpage?schoolId=${schoolId!0}&toSchoolList=toSchoolList";
                        }else{
                            alert(res.info);
                        }
                    },
                    error:function(){
                        alert("信息保存失败")
                    }
                });

//            if (validate()) {
//                if (confirm("提交后，学校基础信息将不可修改！！！是否确认？")) {
//                    $.post("/mobile/school_clue/upsert_school_clue.vpage", schoolDetail, function (result) {
//                        if (result.success) {
//                            window.history.back();
//                        } else {
//                            alert(result.info);
//                        }
//                    });
//
//                }
//            }
        });
    function getLevel() {
        gradeInfo =
                [{
                    level: $('.fixLength a').eq(0).attr('data_value'),
                    allMan: $('.fixLength .allMan').eq(0).val(),
                    banClass: $('.fixLength .banClass').eq(0).val()
                }
                    , {
                    level: $('.fixLength a').eq(1).attr('data_value'),
                    allMan: $('.fixLength .allMan').eq(1).val(),
                    banClass: $('.fixLength .banClass').eq(1).val()
                }
                    , {
                    level: $('.fixLength a').eq(2).attr('data_value'),
                    allMan: $('.fixLength .allMan').eq(2).val(),
                    banClass: $('.fixLength .banClass').eq(2).val()
                }
                    , {
                    level: $('.fixLength a').eq(3).attr('data_value'),
                    allMan: $('.fixLength .allMan').eq(3).val(),
                    banClass: $('.fixLength .banClass').eq(3).val()
                }
                    , {
                    level: $('.fixLength a').eq(4).attr('data_value'),
                    allMan: $('.fixLength .allMan').eq(4).val(),
                    banClass: $('.fixLength .banClass').eq(4).val()
                }
                    , {
                    level: $('.fixLength a').eq(5).attr('data_value'),
                    allMan: $('.fixLength .allMan').eq(5).val(),
                    banClass: $('.fixLength .banClass').eq(5).val()
                }];

        level = {
            "schoolingLength": $("#schoolingLength").val(),
            "englishStartGrade": $("#englishStartGrade").val(),
            "gradeInfo": gradeInfo
        };
    }

    <#--function validate() {-->
        <#--if (blankStringOrZero($("#schoolPhase option:selected").val())) {-->
            <#--alert("请选择学校阶段");-->
            <#--return false;-->
        <#--}-->
        <#--if (blankString($("#regionCode").val())) {-->
            <#--alert("请选择所属区域");-->
            <#--return false;-->
        <#--}-->
        <#--if (blankString($("#schoolName").html())) {-->
            <#--alert("请输入学校名称");-->
            <#--return false;-->
        <#--}-->
        <#--if (blankString($("#shortName").html())) {-->
            <#--alert("请输入学校简称");-->
            <#--return false;-->
        <#--}-->
        <#--if (blankStringOrZero($("#schoolType option:selected").val())) {-->
            <#--alert("请选择学校性质");-->
            <#--return false;-->
        <#--}-->
        <#--if (blankStringOrZero($("#schoolingLength option:selected").val())) {-->
            <#--alert("请选择学校学制");-->
            <#--return false;-->
        <#--}-->

        <#--//学校编辑页未认证学校如果有照片就一定要有经纬度-->
        <#--<#if (clueType!"")=="newClue">-->
            <#--if (blankString($("#photoUrl").val())) {-->
                <#--alert("请添加学校正门照片");-->
                <#--return false;-->
            <#--}-->
            <#--if (blankString($("#lat").val()) || blankString($("#lng").val())) {-->
                <#--alert("请上传带有经纬度的照片");-->
                <#--return false;-->
            <#--}-->
        <#--</#if>-->

        <#--//验证阶段和学制-->
        <#--if (blankString($("#gradeDistribution").val())) {-->
            <#--alert("请选择年级分布");-->
            <#--return false;-->
        <#--}-->

        <#--var mustBranchSchoolCount = 0;-->

        <#--//年级分布-->
        <#--if (true) {-->
            <#--var grvs = $(".js-gradeValue"), leastGrade = false;-->
            <#--$.each(grvs, function (i, item) {-->
                <#--if (!($(item).val() == "" || $(item).val() == "0")) {-->
                    <#--leastGrade = true;-->
                    <#--mustBranchSchoolCount++;-->
                <#--}-->
            <#--});-->
            <#--if (!leastGrade) {-->
                <#--alert("请选择年级分布");-->
                <#--return leastGrade;-->
            <#--}-->
        <#--}-->

        <#--//分校验证-->
        <#--var slv = $("#schoolingLength option:selected").val();-->
        <#--var bsItem = $(".js-branchSchoolItem").length;-->


        <#--if ((mustBranchSchoolCount < parseInt(modal[slv]))) {-->
            <#--if (bsItem <= 0) {-->
                <#--//alert("学制和年级分布不一致，请添加分校");-->
                <#--//return false;-->
            <#--}-->
        <#--}-->

        <#--if (blankStringOrZero($("#englishStartGrade option:selected").val()) && $("#schoolPhase option:selected").val() == 1) {-->
            <#--alert("请选择英语起始年级");-->
            <#--return false;-->
        <#--}-->
        <#--if (blankStringOrZero($("#externOrBoarder option:selected").val())) {-->
            <#--alert("请选择走读/寄宿");-->
            <#--return false;-->
        <#--}-->
        <#--if ($("#schoolPhase option:selected").val() == 1 && ($("#schoolingLength option:selected").val() == 3 || $("#schoolingLength option:selected").val() == 4)) {-->
            <#--alert("学制与阶段不匹配");-->
            <#--return false;-->
        <#--}-->
        <#--if ($("#schoolPhase option:selected").val() == 2 && ($("#schoolingLength option:selected").val() == 1 || $("#schoolingLength option:selected").val() == 2)) {-->
            <#--alert("学制与阶段不匹配");-->
            <#--return false;-->
        <#--}-->
        <#--return true;-->
    <#--}-->
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