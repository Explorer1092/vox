<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<#assign shortIconTail = "?x-oss-process=image/resize,w_48,h_48/auto-orient,1">
<@layout.page title="新建学校" pageJs="newschool" footerIndex=4>
<@sugar.capsule css=['school','photo_pic']/>
<style>.nav.tab-head.c-flex.c-flex-5{display: none;}
</style>
<div hidden><a href="javascript:void(0)" class="inner-right js-submit">创建</a></div>
<#include "addNewSchoolArea.ftl"/>
<#include "addNewSchoolType.ftl"/>
<div class="schoolParticular-pop" style="display: none;" id="repatePane">
</div>
<script>
    var phase_value = ${phase_value!0};
    var showLength = '${eduSystem!''}';
</script>
<script type="text/html" id="creatDictionary">
    <div class="inner">
        <h1>学校创建成功（id：<%=res.schoolId%>）</h1>
        <p class="info">快去邀请老师注册吧~<br><%if(res.appraisalSchool){%>依据本次提交的照片已自动生成鉴定学校申请<%}%><br>继续申请将学校加入字典表请点击“是”</p>
        <div class="btn">
            <a href="javascript:void(0);" class="white_btn">否</a>
            <a href="javascript:void(0);" class="creatDictionary" data-index="<%=res.schoolId%>">是</a>
        </div>
    </div>
</script>
<script type="text/javascript">
    var level = {};
    var gradeInfo = [];
    var vox = vox || {};
    vox.task = vox.task || {};
    var AT = new agentTool();
    var getRealTimeDetail = function () {
        var schooleNameVal = $("#schoolName").val();
        if (schooleNameVal == "请填写") {
            schooleNameVal = "";
        }
        var externOrBoarder = null;
        if($("#schoolPhase").val() == '2' || $("#schoolPhase").val() == 4){
            externOrBoarder = $("#externOrBoarder").val()
        }
        schoolDetail = {
            "eduSystem": $("#eduSystem").val(),
            "englishStartGrade": $("#englishStartGrade").val(),
            "externOrBoarder": externOrBoarder,//中学走读方式
            "phase": $("#schoolPhase").val(),
            "regionCode": $("#regionCode").val(),
            "schoolName": schooleNameVal,
            "schoolType": $("#schoolType").val(),
            "photoUrl": $("#photoUrl").val(),
            "gradeInfo" :
                    [{
                        level: $('.fixLength .level').eq(0).attr('data_value'),
                        allMan: $('.fixLength .allMan').eq(0).val(),
                        banClass: $('.fixLength .banClass').eq(0).val()
                    }
                        , {
                        level: $('.fixLength .level').eq(1).attr('data_value'),
                        allMan: $('.fixLength .allMan').eq(1).val(),
                        banClass: $('.fixLength .banClass').eq(1).val()
                    }
                        , {
                        level: $('.fixLength .level').eq(2).attr('data_value'),
                        allMan: $('.fixLength .allMan').eq(2).val(),
                        banClass: $('.fixLength .banClass').eq(2).val()
                    }
                        , {
                        level: $('.fixLength .level').eq(3).attr('data_value'),
                        allMan: $('.fixLength .allMan').eq(3).val(),
                        banClass: $('.fixLength .banClass').eq(3).val()
                    }
                        , {
                        level: $('.fixLength .level').eq(4).attr('data_value'),
                        allMan: $('.fixLength .allMan').eq(4).val(),
                        banClass: $('.fixLength .banClass').eq(4).val()
                    }
                        , {
                        level: $('.fixLength .level').eq(5).attr('data_value'),
                        allMan: $('.fixLength .allMan').eq(5).val(),
                        banClass: $('.fixLength .banClass').eq(5).val()
                    }]
        };
    };
    $(document).on("ready",function(){
        reloadCallBack();
        switch (phase_value)
        {
            case 1 :
                switch (showLength)
                {
                    case 'P5':
                        console.log('showLength='+showLength);
                        $('.showLength01').addClass('fixLength').show().siblings().removeClass('fixLength').hide();
                        break;
                    case 'P6':
                        $('.showLength02').addClass('fixLength').show().siblings().removeClass('fixLength').hide();
                        break;
                }
                break;
            case 2 :
                console.log('showLength='+showLength);
                switch (showLength)
                {
                    case 'J3':
                        $('.showLength03').addClass('fixLength').show().siblings().removeClass('fixLength').hide();
                        break;
                    case 'J4':
                        $('.showLength04').addClass('fixLength').show().siblings().removeClass('fixLength').hide();
                        break;
                }
                break;
            case 4 :
                switch (showLength)
                {
                    case 'S3':
                        $('.showLength05').addClass('fixLength').show().siblings().removeClass('fixLength').hide();
                        break;
                    case 'S4':
                        $('.showLength07').addClass('fixLength').show().siblings().removeClass('fixLength').hide();
                        break;
                }
                break;
            case 5 :
                $('.showLength06').addClass('fixLength').show().siblings().removeClass('fixLength').hide();
                break;
        }

        $(".js-stage").html($("#schoolPhase>option:selected").text());
        $(".js-type").html($("#schoolType>option:selected").text());
        $(".js-length").html($("#eduSystem>option:selected").text());
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
                            window.location.href = '/mobile/school_clue/branch_school_list.vpage';
                        } else {
                            window.location.href = 'find_branch_school.vpage?branchSchoolId=' + data;
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
        $(document).on('click','.white_btn',function(){
            disMissViewCallBack();
        });
        $(document).on('click','.creatDictionary',function(){
            window.location.href = "dict_school_apply.vpage?schoolId="+$(this).data('index') ;
        });
        $(document).on("click",".js-submit",function () {
            getRealTimeDetail();
            var gradeData = [];
            for(var i = 0;i< $('.fixLength li').length-1;i++){
                gradeData[i] = {};
                gradeData[i].grade = $('.fixLength li').eq(i).find('.level').attr("data_value");
                gradeData[i].clazzNum = $('.fixLength li').eq(i).find('.banClass').val();
                gradeData[i].studentNum = $('.fixLength li').eq(i).find('.allMan').val();
            }
            schoolDetail.gradeDataJson = gradeData;
            $.ajax({
                type:'POST',
                url:"save_new_school.vpage",
                contentType:'application/json;charset=UTF-8',
                data:JSON.stringify(schoolDetail),
                async:false,
                dataType:'json',
                success:function(res){
                    if(res.success){
                        <#if requestContext.getCurrentUser().isBusinessDeveloper() || requestContext.getCurrentUser().isCityManager()>
                            $("#repatePane").html(template("creatDictionary", {res: res}));
                            $("#repatePane").show();
                        <#else>
                            AT.alert("创建成功，老师可以注册啦！学校ID:" + res.schoolId);
                            setTimeout("disMissViewCallBack()",1500);
                        </#if>
                    }else{
                        AT.alert(res.info)
                    }

                },
                error:function () {
                    AT.alert('保存失败')
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
                        openSecond("/mobile/school_clue/school_name.vpage");
                    }else{
                        AT.alert(res.info)
                    }

                },
                error:function (res) {
                    AT.alert('保存失败')
                }
            });
        });
        function schoolphase(){
            var phase_school = $("#schoolPhase>option:selected").val();
            $("#eduSystemType").show();
            switch (phase_school)
            {
                case '1' :
                    $('.schoolLength').show();
                    $('.englishStartGrade').show();
                    $('.externOrBoarderType').hide();//中学寄宿制
                    $('.PRIMARY_SCHOOL').removeAttr('disabled');
                    $('.JUNIOR_SCHOOL').attr('disabled', 'disabled');
                    $('.SENIOR_SCHOOL').attr('disabled', 'disabled');
                    break;
                case '2' :
                    $('.schoolLength').show();
                    $('.englishStartGrade').hide();
                    $('.externOrBoarderType').show();//中学寄宿制
                    $('.PRIMARY_SCHOOL').attr('disabled', 'disabled');
                    $('.JUNIOR_SCHOOL').removeAttr('disabled');
                    $('.SENIOR_SCHOOL').attr('disabled', 'disabled');
                    break;
                case '4' :
                    $('.schoolLength').show();
                    $('.englishStartGrade').hide();
                    $('.externOrBoarderType').show();//中学寄宿制
                    $('.PRIMARY_SCHOOL').attr('disabled', 'disabled');
                    $('.JUNIOR_SCHOOL').attr('disabled', 'disabled');
                    $('.SENIOR_SCHOOL').removeAttr('disabled');
                    break;
                case '5' :
                    $('.schoolLength').hide();
                    $('.englishStartGrade').hide();
                    $('.externOrBoarderType').hide();//中学寄宿制
                    $('.showLength06').addClass('fixLength').show().siblings().removeClass('fixLength').hide();
                    break;
                default:
                    $('.schoolLength').show();
                    $('.englishStartGrade').show();
                    $('.externOrBoarderType').hide();//中学寄宿制
                    break;
            }
        }
        schoolphase();

        $(document).on("change","#schoolPhase",function(){
            var data = $("#schoolPhase>option:selected").text();
            var val = $("#schoolPhase>option:selected").val();
            $('.js-distribution').show();
            schoolphase();
            if(val == '0'){
                $('.schoolGrade').hide();
            }
            //走读方式
            $('.js-externOrBoarder').html('走读');
            $('#externOrBoarder').val(1);
            //学制
            $(".js-stage").html(data);
            $(".js-length").html('请选择');
            $(".js-name").html("请选择");
            $('#eduSystem').val('0');
            $('#eduSystem').change();
            //英语起始年级
            $("#englishStartGrade").val('0');
            $('.js-english').html('请选择');
            if(val == "5"){
                $('.showLength06').addClass('fixLength').show().siblings().removeClass('fixLength').hide();
            }
        });
        //走读方式
        $(document).on("change","#externOrBoarder",function(){
            var data = $("#externOrBoarder>option:selected").text();
            var val = $("#externOrBoarder>option:selected").val();
            $('.js-externOrBoarder').html(data);
        });
        //学制
        $(document).on("change","#eduSystem",function(){
            var schoolLengthChange = $('#eduSystem>option:selected').text();
            var schoolStudy = $('#eduSystem>option:selected').val();
            $('.js-distribution').show();
            switch (schoolStudy)
            {
                case '0' :
                    $('.schoolGrade').hide().siblings().hide();
                    $('.js-distribution').hide();
                    break;
                case 'P5' :
                    $('.showLength01').addClass('fixLength').show().siblings().removeClass('fixLength').hide();
                    break;
                case 'P6' :
                    $('.showLength02').addClass('fixLength').show().siblings().removeClass('fixLength').hide();
                    break;
                case 'J3' :
                    $('.showLength03').addClass('fixLength').show().siblings().removeClass('fixLength').hide();
                    break;
                case 'J4' :
                    $('.showLength04').addClass('fixLength').show().siblings().removeClass('fixLength').hide();
                    break;
                case 'S3' :
                    $('.showLength05').addClass('fixLength').show().siblings().removeClass('fixLength').hide();
                    break;

                case 'S4' :
                    $('.showLength07').addClass('fixLength').show().siblings().removeClass('fixLength').hide();
                    break;
                default :
                    $('.schoolGrade').removeClass('fixLength').hide();
                    $('.js-distribution').hide();
                    break;
            }
            $('.js-length').html(schoolLengthChange);
        });
        //英语起始年级
        $(document).on("change","#englishStartGrade",function(){
            var schoolEnglish = $('#englishStartGrade>option:selected').text();
            $('.js-english').html(schoolEnglish);
        });
        var chooseSchoolName = function(nameType) {
            var phase = $("#schoolPhase").val();
            var eduSystem = $('#eduSystem').val();
            var schoolPhase = {
                phase: phase,
                eduSystem:eduSystem

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

        $(document).on("click","#regionName",function () {
            var rCode = $("#regionCode").val();
            chooseSchoolName();
            window.location.href = "/mobile/work_record/load_region_page.vpage?type=newSchool&regionCode=" + rCode;
        });
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
                   openSecond('/mobile/school_clue/school_photo_page.vpage?returnUrl=addnewschoolpage.vpage') ;
                }else{
                    AT.alert(res.info)
                }

            },
            error:function (res) {
                AT.alert('保存失败')
            }
        });
    });
    //年级分布求和
    var banClass = 0;
    $(document).on('change','.banClass',function(){
        banClass = 0;
        var schoolGrade = $(this).closest('.schoolGrade');
        schoolGrade.find('.banClass').each(function(){
            if($(this).val() != ''){
                banClass += parseInt($(this).val());
            }
        });
        schoolGrade.find('.classGrade').val(banClass);
    });
    var allMan = 0;
    $(document).on('change','.allMan',function(){
        allMan = 0;
        var schoolGrade = $(this).closest('.schoolGrade');
        schoolGrade.find('.allMan').each(function(){
            if($(this).val() != ''){
                allMan += parseInt($(this).val());
            }
        });
        schoolGrade.find('.gradeNum').val(allMan)
    });
</script>
</@layout.page>