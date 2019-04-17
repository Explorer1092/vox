<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<#assign shortIconTail = "?x-oss-process=image/resize,w_48,h_48/auto-orient,1">
<@layout.page title="基础数据确认" pageJs="schoolConfirm" footerIndex=4>
    <@sugar.capsule css=['school','photo_pic']/>
<style>
    .nav.tab-head.c-flex.c-flex-5{
        display: none;
    }
</style>
    <a href="javascript:void(0)" class="inner-right js-submit" style="display: none;">提交</a>
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
    /*图片样式*/
    .loading-data {
        display: none;
        position: fixed;
        top: 0;left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0,0,0,.5);
        z-index: 9000;}
    .loading-data .icon {
        position: absolute;
        top: 50%;
        left: 50%;
        margin: -1rem 0 0 -1rem;
        width: 2rem;
        height: 2rem;
        background: url("data:image/png;base64,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") no-repeat;
        background-size: 100% 100%;
        animation: globalLoad 800ms infinite linear;
        -webkit-animation: globalLoad 800ms infinite linear;
    }
    @keyframes globalLoad {
        0% {
            transform: rotate(-360deg);
        }
        100% {
            transform: rotate(0deg);
        }
    }
</style>
<div class="flow">
    <#--<#if phase_value?has_content &&  phase_value != 5>-->
        <#--<div class="item schoolLength" style="position:relative;">-->
            <#--学制-->
            <#--<div class="inner-right js-length">-->
                <#--<#if eduSystemName?has_content>${eduSystemName!""}<#else>请选择</#if>-->
            <#--</div>-->
        <#--</div>-->
    <#--</#if>-->
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
                <select name="" id="englishStartGrade" style="width:100%;line-height:2.5rem;height:2.5rem;position:absolute;left:0;top:0;opacity: 0;border:none;">
                    <option value="0">请选择</option>
                    <option value="1" <#if englishStartGrade?? && englishStartGrade == 1>selected</#if>>一年级</option>
                    <option value="3" <#if englishStartGrade?? && englishStartGrade == 3>selected</#if>>三年级</option>
                </select>
            </div>
        </#if>
    </#if>
</div>
<div class="mobileCRM-V2-list schoolParticular-edit">
    <div class="edit-title">年级分布 <span>（请准确填写各年级班人数和班级数）</span></div>
    <div class="school_length edit-list">
        <ul class="schoolGrade fixLength">
            <#if gradeDataList?? && gradeDataList?size gt 0>
                <#assign clazzNum = 0 ,studentNum = 0>
                <#list gradeDataList as list>
                    <#assign clazzNum = (list.clazzNum!0) + clazzNum>
                    <#assign studentNum = (list.studentNum!0) + studentNum>
                    <li><div class="level" data_value="${list.grade!0}">${list.gradeDesc!''}</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${list.clazzNum!0}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${list.studentNum!0}"/>人</div></li>
                </#list>
            </#if>
            <li><div>合计</div><div>共<input value="${clazzNum!0}" class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" disabled="disabled" value="${studentNum!0}">人</div></li>
        </ul>
    </div>
</div>
<div class="loading-data" id="loading-data"><span class="do_loader icon"></span></div>
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
    var vox = vox || {};
    vox.task = vox.task || {};
    var AT = new agentTool();
    var getRealTimeDetail = function () {
        schoolDetail = {
            "schoolId" : schoolId,
            "eduSystem":  '${eduSystem!''}',
            "englishStartGrade": $("#englishStartGrade").val(),
        };
    };
    $(document).on("ready",function(){
        switch (phase_value)
        {
            case 1 :
                $('.englishStartGrade').show();
                break;
        }

        $(".js-stage").html($("#schoolPhase>option:selected").text());
        $(".js-type").html($("#schoolType>option:selected").text());
        $(document).on('click','.creatDictionary',function(){
            $(this).addClass("show");
            $(".js-submit").click();
        });
        $(document).on("click",".js-submit",function () {
            localStorage.setItem('alertDialog','-1');
            var _this = $(this);

            getRealTimeDetail();
            if($(".creatDictionary").hasClass("show")){
                schoolDetail.confirm = "confirm";
            }
            if(schoolDetail.eduSystem == 0 || schoolDetail.englishStartGrade == 0){
                AT.alert("数据不完整");
                return false;
            }
            _this.removeClass("js-submit");
            var gradeDataJson = [] ;
            for(var i = 0;i< $('.fixLength li').length-1;i++){
                gradeDataJson[i] = {};
                gradeDataJson[i].grade = $('.fixLength li').eq(i).find('.level').attr("data_value");
                gradeDataJson[i].clazzNum = $('.fixLength li').eq(i).find('.banClass').val();
                gradeDataJson[i].studentNum = $('.fixLength li').eq(i).find('.allMan').val();
            }
            schoolDetail.gradeDataJson = gradeDataJson;
            $('#loading-data').show();
            $.ajax({
                type:'POST',
                url:"save_confirm_school.vpage",
                contentType:'application/json;charset=UTF-8',
                data:JSON.stringify(schoolDetail),
                async:true,
                dataType:'json',
                success:function(res){
                    if(res.success){
                        if(!res.appraisalSchool){
                            AT.alert("修改学校信息成功");
                            setTimeout("disMissViewCallBack()",1500);
                        }
                    }else if(!res.success && res.errorCode == "004"){
                        _this.addClass("js-submit");
                        $('#sureWindow').html(template("sureUpdateEduSystem",{res:res}));
                        $('#sureWindow').show();
                    }
                },
                error:function () {
                    _this.addClass("js-submit");
                    AT.alert('保存失败')
                },
                complete:function () {
                    $('#loading-data').hide();
                }
            });
        });
        //学制
        $(document).on("change", "#eduSystem", function () {
            var schoolLengthChange = $('#eduSystem>option:selected').text();
            var schoolStudy = $('#eduSystem>option:selected').val();
            switch (schoolStudy)
            {
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
                default:
                    $('.showLength06').hide().siblings().hide();
            }
        });
        //英语起始年级
        $(document).on("change","#englishStartGrade",function(){
            var schoolEnglish = $('#englishStartGrade>option:selected').text();
            $('.js-english').html(schoolEnglish);
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
    });
    var checkData = function () {
        if($('#englishStartGrade>option:selected').text() == ""){
            AT.alert("")
        }
    }
    var schoolPhase = ${phase_value!0};
//    if(schoolPhase == 1){
//        $('.PRIMARY_SCHOOL').removeAttr('disabled');
//        $('.JUNIOR_SCHOOL').attr('disabled','disabled');
//        $('.SENIOR_SCHOOL').attr('disabled','disabled');
//    }else if(schoolPhase == 2){
//        $('.PRIMARY_SCHOOL').attr('disabled','disabled');
//        $('.JUNIOR_SCHOOL').removeAttr('disabled');
//        $('.SENIOR_SCHOOL').attr('disabled','disabled');
//
//    }else if(schoolPhase == 4){
//        $('.PRIMARY_SCHOOL').attr('disabled','disabled');
//        $('.JUNIOR_SCHOOL').attr('disabled','disabled');
//        $('.SENIOR_SCHOOL').removeAttr('disabled');
//    }else{
//        $('#schoolLength').hide();
//    }
</script>
</@layout.page>