<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<#assign shortIconTail = "?x-oss-process=image/resize,w_48,h_48/auto-orient,1">
<@layout.page title="编辑学校信息" pageJs="updateSchool" footerIndex=4>
    <@sugar.capsule css=['school','photo_pic']/>
<style>
    .nav.tab-head.c-flex.c-flex-5{
        display: none;
    }
</style>
<style>
    .mobileCRM-V2-info{
        font-size:.75rem
    }
    .school_length input{
        width:10%;
        font-size:.75rem;
        text-align:center;
    }
    .school_length span{
        margin-left:13%;
    }
    .school_length ul li {
        width:100%;
        text-align:center;
    }
</style>
<div class="flow">
    <div class="schoolParticular-box" style="margin-bottom:0">
        <div class="particular-image">
            <div class="left"><p class="p-1">照片 <a class="schooleImageDetail" onclick="openSecond('/mobile/school_clue/photodesc.vpage')"></a></p></div>
            <div class="right photoSchoolShortIcon" id="photoShow">
                <a href="javascript:void(0);">
                    <img src="<#if photoUrl??>${(photoUrl)!''}${shortIconTail}</#if>">
                </a>
            </div>
        </div>
        <div class="particular-image">
            <div class="left">位置</div>
            <div class="right"><#if address??>${address!''}</#if></div>
        </div>
    </div>
</div>
<p style="padding:0.4rem 0 0.4rem 0.8rem;background:#f6f6f6;color:#9199bb;text-align:left;font-size: 60%;"></p>

<div class="flow">
    <#if phase_value?has_content &&  phase_value != 5>
        <div class="item schoolLength" style="position:relative;">
            学制
            <div class="inner-right js-length">
                <#if eduSystemName??>${eduSystemName!""}<#else>请选择</#if>
            </div>
            <#if locked?? && !locked>
                <select id="eduSystem"
                        style="width:100%;line-height:2.5rem;height:2.5rem;position:absolute;left:0;top:0;opacity: 0;border:none;">
                    <option value="0">请选择</option>
                    <#list eduSystemTypes as data>
                        <option class="${data.group!""}" <#if data.selected?? && data.selected>selected</#if>
                                value="${data.code!''}">${data.name!''}</option>
                    </#list>
                </select>
            </#if>
        </div>
    </#if>
    <#if phase_value?has_content>
        <#if phase_value == 1>
            <div class="item englishStartGrade" style="position:relative;">
                英语起始年级
                <div class="inner-right js-english">
                    <#if englishStartGrade??&& englishStartGrade == 1>一年级
                    <#elseif englishStartGrade??&& englishStartGrade == 3>三年级
                    <#else>请选择
                    </#if>
                </div>
                <#if locked?? && !locked>
                    <select name="" id="englishStartGrade" style="width:100%;line-height:2.5rem;height:2.5rem;position:absolute;left:0;top:0;opacity: 0;border:none;">
                        <option value="0">请选择</option>
                        <option value="1" <#if englishStartGrade?? && englishStartGrade == 1>selected</#if>>一年级</option>
                        <option value="3" <#if englishStartGrade?? && englishStartGrade == 3>selected</#if>>三年级</option>
                    </select>
                </#if>
            </div>
        </#if>
    </#if>
</div>
<div class="mobileCRM-V2-list schoolParticular-edit">
    <div class="edit-title">年级分布 <span>（请准确填写各年级班人数和班级数）</span></div>
    <div class="school_length edit-list">
        <ul class="schoolGrade">
        </ul>
    </div>
</div>
<script type="text/html" id="sureUpdateEduSystem">
    <div class="inner">
        <p class="info"><%=res.info%></p>
        <div class="btn">
            <a href="javascript:void(0);" class="white_btn">否</a>
            <a href="javascript:void(0);" class="creatDictionary" >是</a>
        </div>
    </div>
</script>
<div class="schoolParticular-pop" style="display:none;" id="sureWindow">

</div>

    <#--<#include "branch_school_list_info.ftl"/>-->
<div class="schoolParticular-pop" style="display: none;" id="repatePane">
    <div class="inner">
        <h1>保存成功！</h1>
        <p class="info">注：该学校未鉴定，依据本次提交的照片已自动生成鉴定学校申请</p>
        <div class="btn">
            <a href="javascript:void(0);" class="submitBtn">我知道了</a>
        </div>
    </div>
</div>
<div class="schoolParticular-pop" style="display:none;" id="clearWin">

</div>
<script type="text/html" id="">
    <%if(gradeDataList.length > 0){%>
        <%=gradeDataList%>
        <%if((eduSystem == 'J4' || eduSystem == 'S4') && gradeDataList.length == 3){%>
            <li class="grade_info"><div class="level" data-grade="<%=lastGrade[eduSystem].grade%>"><%=lastGrade[eduSystem].gradeDesc%></div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel"/>人</div></li>
        <%}%>
        <%for(var i = 0;i< eduSystemObj[eduSystem];i++){%>
            <%var list = gradeDataList[i]%>
            <%if(list){%>
                <li class="grade_info"><div class="level" data-grade="<%=list.grade%>"><%=list.gradeDesc%></div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="<%=list.clazzNum%>"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="<%=list.studentNum%>"/>人</div></li>
            <%}%>
        <%}%>
        <%if(eduSystem == 'P6' && gradeDataList.length == 5){%>
            <li class="grade_info"><div class="level" data-grade="<%=lastGrade[eduSystem].grade%>"><%=lastGrade[eduSystem].gradeDesc%></div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value=""/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value=""/>人</div></li>
        <%}%>
        <li><div>合计</div><div>共<input class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" disabled="disabled">人</div></li>
    <%}%>
</script>
<script type="text/html" id="schoolGrade">
    <%if(gradeDataList.length > 0){%>
        <%=gradeDataList%>
        <%if((eduSystem == 'J4' || eduSystem == 'S4') && gradeDataList.length == 3){%>
            <li class="grade_info"><div class="level" data-grade="<%=lastGrade[eduSystem].grade%>"><%=lastGrade[eduSystem].gradeDesc%></div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel"/>人</div></li>
        <%}%>
        <%if ((eduSystem == 'J3' && gradeDataList.length == 4) ||eduSystem == 'S3' && gradeDataList.length == 4 ){%>
            <%for(var i = 1;i<= eduSystemObj[eduSystem];i++){%>
                <%var list = gradeDataList[i]%>
                <%if(list){%>
                    <li class="grade_info">
                        <div class="level" data-grade="<%=list.grade%>"><%=list.gradeDesc%></div>
                        <div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="<%=list.clazzNum%>"/>班</div>
                        <div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="<%=list.studentNum%>"/>人</div>
                    </li>
                <%}%>
            <%}%>
        <%}else{%>
            <%for(var i = 0;i< eduSystemObj[eduSystem];i++){%>
                <%var list = gradeDataList[i]%>
                <%if(list){%>
                    <li class="grade_info"><div class="level" data-grade="<%=list.grade%>"><%=list.gradeDesc%></div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="<%=list.clazzNum%>"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="<%=list.studentNum%>"/>人</div></li>
                <%}%>
            <%}%>
        <%}%>
        <%if(eduSystem == 'P6' && gradeDataList.length == 5){%>
        <li class="grade_info"><div class="level" data-grade="<%=lastGrade[eduSystem].grade%>"><%=lastGrade[eduSystem].gradeDesc%></div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value=""/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value=""/>人</div></li>
        <%}%>
        <li><div>合计</div><div>共<input class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" disabled="disabled">人</div></li>
    <%}%>
</script>
<script>
    var phase_value = ${phase_value!0};
    var eduSystem = '${eduSystem!''}';
    $(document).on('click','.white_btn',function(){
        $('#sureWindow').hide();
        $(".creatDictionary").removeClass("show")
    });

</script>
<script type="text/javascript">
    var level = {};
    var schoolId = ${schoolId!0};
    var gradeInfo = [];
    var vox = vox || {};
    vox.task = vox.task || {};
    var AT = new agentTool();
    //年级分布求和
    var class_num = function(inClassName,outClassName){
        var num = 0;
        $('.schoolGrade').find(inClassName).each(function(){
            if($(this).val() != ''){
                num += parseInt($(this).val());
            }
        });
        $('.schoolGrade').find(outClassName).val(num);
    };
    var get_school_grade = function (schoolStudy) {
        $.get("/mobile/resource/school/get_grade_basic_data_list.vpage?schoolId=${schoolId!0}",function (res) {
            if(res.success){
                res.eduSystemObj = {
                    P6:6,
                    P5:5,
                    J3:3,
                    J4:4,
                    S3:3,
                    S4:4,
                    I4:4
                };
                    res.lastGrade = {
                        P6:{
                            grade:6,
                            gradeDesc:'六年级'
                        },
                        J4:{
                            grade:6,
                            gradeDesc:'六年级'
                        },
                        S4:{
                            grade:6,
                            gradeDesc:'九年级'
                        }
                    };
                res.eduSystem = schoolStudy;
                $('.schoolGrade').html(template("schoolGrade",res));
                console.log(res.eduSystem)
                class_num('.banClass','.classGrade');
                class_num('.allMan','.gradeNum');
            }
        });
    };
    var getRealTimeDetail = function () {
        var gradeDataJson = [] ;
        for(var i = 0;i< $('.grade_info').length;i++){
            gradeDataJson[i] = {};
            gradeDataJson[i].grade = $('.grade_info').eq(i).find('.level').data("grade");
            gradeDataJson[i].clazzNum = $('.grade_info').eq(i).find('.banClass').val();
            gradeDataJson[i].studentNum = $('.grade_info').eq(i).find('.allMan').val();
        }
        schoolDetail = {
            "schoolId" : schoolId,
            "eduSystem": $("#eduSystem").val(),
            "englishStartGrade": $("#englishStartGrade").val(),
            "phase": $("#schoolPhase").val(),
            "regionCode": $("#regionCode").val(),
            "schoolType": $("#schoolType").val(),
            "photoUrl": $("#photoUrl").val(),
            "gradeDataJson":gradeDataJson
        };
    };
    $(document).on("ready",function(){
        reloadCallBack();

        $(".js-stage").html($("#schoolPhase>option:selected").text());
        $(".js-type").html($("#schoolType>option:selected").text());
        //$(".js-length").html($("#eduSystem>option:selected").text());
        $(document).on('click','.creatDictionary',function(){
            $(this).addClass("show");
            $(".js-submit").click();
        });
        $(document).on("click",".js-submit",function () {
            getRealTimeDetail();
            var _this = $(this);
            $("#clearWin").show();
            if($(".creatDictionary").hasClass("show")){
                schoolDetail.confirm = "confirm";
            }
            _this.removeClass("js-submit");
            $.ajax({
                type:'POST',
                url:"save_update_school.vpage",
                contentType:'application/json;charset=UTF-8',
                data:JSON.stringify(schoolDetail),
                dataType:'json',
                success:function(res){
                    if(res.success){
                        if(!res.appraisalSchool){
                            AT.alert("修改学校信息成功");
                            setTimeout("window.history.back()",1500);
                        }else{
                            $("#repatePane").show();
                            $("#clearWin").hide();
                        }
                    }else if(!res.success && res.errorCode == "004"){
                        _this.addClass("js-submit");
                        $('#sureWindow').html(template("sureUpdateEduSystem",{res:res}));
                        $('#sureWindow').show();
                        $("#clearWin").hide();
                    }
                },
                error:function () {
                    AT.alert('保存失败');
                    $("#clearWin").hide();
                    _this.addClass("js-submit");
                }
            });
        });

        $(document).on("click",".js-name",function(){
            getRealTimeDetail();
            $.ajax({
                type:'POST',
                url:"save_school_info_session.vpage",
                contentType:'application/json;charset=UTF-8',
                data:JSON.stringify(schoolDetail),
                async:false,
                dataType:'json',
                success:function(res){
                    if(res.success){
                        window.location.href = "school_name.vpage";
                    }else{
                        AT.alert(res.info)
                    }

                },
                error:function (res) {
                    AT.alert('保存失败')
                }
            });
        });
        get_school_grade('${eduSystem!''}');
        $(document).on("change","#schoolPhase",function(){
            var data = $("#schoolPhase>option:selected").text();
            var phase_school = $("#schoolPhase>option:selected").val();
            switch (phase_school)
            {
                case '4' :
                    $('.schoolLength').hide();
                    $('.englishStartGrade').hide();
                    break;
                default:
                    $('.schoolLength').show();
                    $('.englishStartGrade').show();
                    break;
            }
            $(".js-stage").html(data);
            $.post("choice_phase.vpage", {phase: data}, function (res) {
                if (res.success) {
                    $("#schoolName").html("请填写");
                }
            })
        });
        //学制
        $(document).on("change", "#eduSystem", function () {
            var schoolLengthChange = $('#eduSystem>option:selected').text();
            var schoolStudy = $('#eduSystem>option:selected').val();
            get_school_grade(schoolStudy);
            $('.js-length').html(schoolLengthChange);
        });
        //英语起始年级
        $(document).on("change","#englishStartGrade",function(){
            var schoolEnglish = $('#englishStartGrade>option:selected').text();
            $('.js-english').html(schoolEnglish);
        });
        var chooseSchoolName = function(nameType) {
            var phase = $("#schoolPhase").val();
            var schoolLength = $('#schoolLength').val();
            var schoolPhase = {
                phase: phase,
                schoolingLength:schoolLength

            };
            $.ajax({
                type:'POST',
                url:"save_school_info_session.vpage",
                contentType:'application/json;charset=UTF-8',
                data:JSON.stringify(schoolPhase),
                async:false,
                dataType:'json',
                success:function(res){
                    if(res.success){
//                        window.location.href = "school_name.vpage?nameType=" + nameType;
                    }else{
                        AT.alert(res.info)
                    }

                },
                error:function (res) {
                    AT.alert('保存失败')
                }
            });

        };

        $(document).on('change','.banClass',function(){
            class_num('.banClass','.classGrade');
        });
        $(document).on('change','.allMan',function(){
            class_num('.allMan','.gradeNum');
        });
        $(document).on("click","#regionName",function () {
            var rCode = $("#regionCode").val();
            window.location.href = "/mobile/work_record/load_region_page.vpage?type=newSchool&regionCode=" + rCode;
        });
    });
    $(document).on("click",'.submitBtn',function(){
        window.history.back();
    });
    //照片
    $("#photoShow").on("click", function () {
        getRealTimeDetail();
        $.ajax({
            type:'POST',
            url:"save_school_info_session.vpage",
            contentType:'application/json;charset=UTF-8',
            data:JSON.stringify(schoolDetail),
            async:false,
            dataType:'json',
            success:function(res){
                if(res.success){
                    openSecond('/mobile/school_clue/school_photo_page.vpage?schoolId=${schoolId!0}');
                }else{
                    AT.alert(res.info)
                }

            },
            error:function (res) {
                AT.alert('保存失败')
            }
        });
    });
    var schoolPhase = ${phase_value!0};
    if(schoolPhase == 1){
        $('.PRIMARY_SCHOOL').removeAttr('disabled');
        $('.JUNIOR_SCHOOL').attr('disabled','disabled');
        $('.SENIOR_SCHOOL').attr('disabled','disabled');
    }else if(schoolPhase == 2){
        $('.PRIMARY_SCHOOL').attr('disabled','disabled');
        $('.JUNIOR_SCHOOL').removeAttr('disabled');
        $('.SENIOR_SCHOOL').attr('disabled','disabled');

    }else if(schoolPhase == 4){
        $('.PRIMARY_SCHOOL').attr('disabled','disabled');
        $('.JUNIOR_SCHOOL').attr('disabled','disabled');
        $('.SENIOR_SCHOOL').removeAttr('disabled');
    }else{
        $('#schoolLength').hide();
    }
    //添加分校
    $(".save_session").on("click", function () {
        getRealTimeDetail();
        var data = $(this).attr("data-value");
        $.ajax({
            type: 'post',
            url : "save_school_info_session.vpage",
            dataType:'json',
            contentType:'application/json;charset=UTF-8',
            data:JSON.stringify(schoolDetail),
            success:function(res){
                if(res.success){
                    if (data == "add_school") {
                        window.location.href = '/mobile/school_clue/branch_school_list.vpage?schoolId=${schoolId!0}';
                    } else {
                        window.location.href = 'find_branch_school.vpage?schoolId=${schoolId!0}&branchSchoolId=' + data;
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
</script>
</@layout.page>
