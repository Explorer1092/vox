<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<#assign shortIconTail = "?x-oss-process=image/resize,w_48,h_48/auto-orient,1">
<@layout.page title="字典表调整申请" pageJs="dictSchoolApply" footerIndex=4 >
    <@sugar.capsule css=['school','photo_pic']/>
        <a style="display:none;" href="javascript:void(0)" class="inner-right js-submit">提交</a>
    <div class="flow" style="padding:.1rem 1rem">
        <div style="font-size:.65rem;color:red">
            提示：每月后5天提交的申请当月不一定能审核完成，如需计算在当月业绩中一定要提前申请！
        </div>
    </div>
<#if needCompleteExtInfo?? && needCompleteExtInfo>
    <div class="flow" style="background:#fff;margin-top:.5rem">
        <#if schoolLevel?? && schoolLevel != 5>
            <#if needEduSystem?? && needEduSystem>
                <div class="item schoolLength" style="margin:0 0 0 1rem;padding:0 1rem 0 0">
                    学制
                    <div class="inner-right js-length">
                        请选择
                    </div>
                    <select id="eduSystem" style="width:100%;line-height:2.5rem;height:2.5rem;position:absolute;left:0;top:0;opacity: 0;border:none;">
                        <option value="0">请选择</option>
                        <#if schoolLevel?has_content>
                            <#if schoolLevel = 1>
                                <option value="P5">小学五年制</option>
                                <option value="P6">小学六年制</option>
                            <#elseif schoolLevel = 2>
                                <option value="J3">中学三年制</option>
                                <option value="J4">中学四年制</option>
                            <#elseif schoolLevel = 4>
                                <option value="S3">高中三年制</option>
                                <option value="S4">高中四年制</option>
                            </#if>
                        </#if>
                    </select>
                </div>
            </#if>
        </#if>
        <#if schoolLevel?? && schoolLevel == 1>
            <#if needEnglishStartGrade?? && needEnglishStartGrade>
                <div class="item englishStartGrade"  style="margin:0 0 0 1rem;padding:0 1rem 0 0;border:0">
                    英语起始年级
                    <div class="inner-right js-englishGrade">
                        请选择
                    </div>
                    <select name="" id="englishStartGrade" style="width:100%;line-height:2.5rem;height:2.5rem;position:absolute;left:0;top:0;opacity: 0;border:none;">
                        <option value="0">请选择</option>
                        <option value="1">一年级</option>
                        <option value="3">三年级</option>
                    </select>
                </div>
            </#if>
        </#if>
    </div>

    <#if needGradeInfo?? && needGradeInfo>
        <div class="mobileCRM-V2-list schoolParticular-edit">
            <#if gradeDataList?? && gradeDataList?size gt 0>
                <div class="edit-title js-distribution">年级分布 <span>（请准确填写各年级班人数和班级数）</span></div>
                <div class="school_length edit-list">
                    <ul class="showLength schoolGrade fixLength" style="">
                        <#if gradeDataList?? && gradeDataList?size gt 0>
                            <#assign clazzNum = 0 ,studentNum = 0>
                            <#list gradeDataList as list>
                                <#assign clazzNum = (list.clazzNum!0) + clazzNum>
                                <#assign studentNum = (list.studentNum!0) + studentNum>
                                <li><div class="level" data_value="${list.grade!0}">${list.gradeDesc!''}</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="banClass" type="tel" value="${list.clazzNum!''}"/>班</div><div>共<input <#if locked?? && locked>disabled="disabled"</#if> class="allMan" type="tel" value="${list.studentNum!''}"/>人</div></li>
                            </#list>
                            <li><div>合计</div><div>共<input value="${clazzNum!0}" style="color:#636880" class="classGrade" disabled="disabled">班</div><div>共<input value="${studentNum!0}" class="gradeNum" style="color:#636880" disabled="disabled">人</div></li>
                        </#if>
                    </ul>
                </div>
            </#if>
        </div>
    </#if>
</#if>
    <div class="flow" style="background:#fff;margin-top:.5rem">
        <#if !requestContext.getCurrentUser().isBusinessDeveloper()>
            <div class="item" style="margin:0 0 0 1rem;padding:0 1rem 0 0">
                学校负责人
                <div class="inner-right js-manager">
                    暂不分配
                </div>
                <select id="schoolManager" name="phase" style="width:100%;line-height:2.5rem;height:2.5rem;position:absolute;left:0;top:0;opacity: 0;border:none;">
                    <option value="0" selected>暂不分配</option>
                    <#if requestContext.getCurrentUser().isBusinessDeveloper()><option value="${requestContext.getCurrentUser().userId}" selected>${requestContext.getCurrentUser().realName}</option></#if>
                    <#if bdUserList?? && bdUserList?size gt 0>
                        <#list bdUserList as data>
                            <option <#if data.selected?? && data.selected>selected</#if> value="${data.id!0}">${data.realName!''}</option>
                        </#list>
                    </#if>
                </select>
            </div>
        </#if>
        <#if schoolLevel?? && (schoolLevel == 2 || schoolLevel == 4)>
            <div class="item schoolRankDiv" style="margin:0 0 0 1rem;padding:0 1rem 0 0" >
                学校等级
                <div class="inner-right js-rank">
                    请选择
                </div>
                <select id="schoolRank" name="phase" style="width:100%;line-height:2.5rem;height:2.5rem;position:absolute;left:0;top:0;opacity: 0;border:none;">
                    <option value="A">A：名校</option>
                    <option value="B">B：重点校</option>
                    <option value="C">C：普通校</option>
                    <option value="D">D：只做英语online作业学校</option>
                    <option value="E">E类学校</option>
                </select>
            </div>
        </#if>
    </div>
<div class="vir-content">
    <div class="vir-title"><i class="titleIco ico01"></i>申请原因</div>
    <div class="text">
        <textarea name="flow" id="flow" maxlength="100" class="js-need" data-einfo="请填写申请原因" placeholder="请点击填写，限100字" ></textarea>
    </div>
</div>
<script type="text/html" id="schoolGradeInfo">
    <div class="edit-title js-distribution">年级分布 <span>（请准确填写各年级班人数和班级数）</span></div>
    <div class="school_length edit-list">
        <ul class="showLength schoolGrade fixLength" style="">
            <%for(var i=0;i < gradeArr.length;i++){%>
                <li><div class="level" data_value="<%=gradeArr[i].schoolVal%>"><%=gradeArr[i].schoolLevel%></div><div>共<input class="banClass" value=""/>班</div><div>共<input class="allMan" value=""/>人</div></li>
            <%}%>
            <li><div>合计</div><div>共<input style="color:#636880" class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" style="color:#636880" disabled="disabled">人</div></li>
        </ul>
    </div>
</script>
<script>
    var schoolLevel = ${schoolLevel!''};
    var gradeArr;
    var AT = new agentTool();
    var englishStartVal= $('#englishStartGrade>option:selected').val();
    var schoolLengthVal= $('#eduSystem>option:selected').val();
    var schoolManagerVal= $('#schoolManager>option:selected').val();
    $(document).on("change","#schoolManager",function(){
        var schoolManager = $('#schoolManager>option:selected').text();
        schoolManagerVal= $('#schoolManager>option:selected').val();
        $('.js-manager').html(schoolManager);
    });
    $(document).on("change","#englishStartGrade",function(){
        var englishStartGrade = $('#englishStartGrade>option:selected').text();
        englishStartVal= $('#englishStartGrade>option:selected').val();
        $('.js-englishGrade').html(englishStartGrade);
    });
    $(document).on("change","#schoolRank",function(){
        var schoolRank = $('#schoolRank>option:selected').text();
        var schoolRankVal= $('#schoolRank>option:selected').val();
        $('.js-rank').html(schoolRank);
        $('.js-rank').attr("schoolPopularity",schoolRankVal);
    });
    $(document).on("change","#eduSystem",function(){
        var schoolLength = $('#eduSystem>option:selected').text();
        schoolLengthVal= $('#eduSystem>option:selected').val();
        $('.js-length').html(schoolLength);
        gradeInfo();
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
    var getRealTimeDetail = function () {
        schoolDetail = {
            "schoolId" : ${schoolId!0},
            "eduSystem": $("#eduSystem").val(),
            "englishStartGrade": $("#englishStartGrade").val()
        };
    };
    //提交创建
    $(document).on('click','.js-submit',function(){
        var _this = $(this);
        var schoolRankVal =$('.js-rank').attr("schoolPopularity");
        if ((schoolLevel == 2 || schoolLevel == 4) && !schoolRankVal){
            AT.alert('请选择学校等级');
            return;
        }
        getRealTimeDetail();
        var gradeDataJson = [] ;
        for(var i = 0;i< $('.fixLength li').length-1;i++){
            gradeDataJson[i] = {};
            gradeDataJson[i].grade = $('.fixLength li').eq(i).find('.level').attr("data_value");
            gradeDataJson[i].clazzNum = $('.fixLength li').eq(i).find('.banClass').val();
            gradeDataJson[i].studentNum = $('.fixLength li').eq(i).find('.allMan').val();
        }
        schoolDetail.gradeDataJson = gradeDataJson;
        _this.removeClass("js-submit");
        $.ajax({
            type:'POST',
            url:"/mobile/school_clue/update_school_ext_info.vpage",
            contentType:'application/json;charset=UTF-8',
            data:JSON.stringify(schoolDetail),
            async:false,
            dataType:'json',
            success:function(res){
                if(res.success){
                    $.post('/apply/create/submit_dictschool_apply.vpage',{
                        modifyType : '1',
                        schoolId : ${schoolId!0},
                        schoolPopularity : schoolRankVal,
                        comment : $('#flow').val(),
                        targetUserId:schoolManagerVal
                    },function(data){
                        if(!data.success){
                            _this.addClass("js-submit");
                            AT.alert(data.info);
                        }else{
                            AT.alert("提交成功");
                                setTimeout(function () {
                                    disMissViewCallBack()
                                },1500);
                            }
                        });
                }else{
                    _this.addClass("js-submit");
                    AT.alert(res.info)}
            },
            error:function () {
                _this.addClass("js-submit");
                AT.alert('保存失败');
            }
        });
    });
function gradeInfo(){
    switch (schoolLevel){
        case 5:
            gradeArr = [{'schoolLevel':'小班','schoolVal':'51'},{'schoolLevel':'中班','schoolVal':'52'},{'schoolLevel':'大班','schoolVal':'53'},{'schoolLevel':'学前班','schoolVal':'54'}];
            break;
        default:
            switch (schoolLengthVal)
            {
                case 'P5':
                    gradeArr = [{'schoolLevel':'一年级','schoolVal':'1'},{'schoolLevel':'二年级','schoolVal':'2'},{'schoolLevel':'三年级','schoolVal':'3'},{'schoolLevel':'四年级','schoolVal':'4'},{'schoolLevel':'五年级','schoolVal':'5'}];
                    break;
                case 'P6':
                    gradeArr = [{'schoolLevel':'一年级','schoolVal':'1'},{'schoolLevel':'二年级','schoolVal':'2'},{'schoolLevel':'三年级','schoolVal':'3'},{'schoolLevel':'四年级','schoolVal':'4'},{'schoolLevel':'五年级','schoolVal':'5'},{'schoolLevel':'六年级','schoolVal':'6'}];
                    break;
                case 'J3':
                    gradeArr = [{'schoolLevel':'七年级','schoolVal':'7'},{'schoolLevel':'八年级','schoolVal':'8'},{'schoolLevel':'九年级','schoolVal':'9'}];
                    break;
                case 'J4':
                    gradeArr = [{'schoolLevel':'六年级','schoolVal':'6'},{'schoolLevel':'七年级','schoolVal':'7'},{'schoolLevel':'八年级','schoolVal':'8'},{'schoolLevel':'九年级','schoolVal':'9'}];
                    break;
                case 'S3':
                    gradeArr = [{'schoolLevel':'高一','schoolVal':'11'},{'schoolLevel':'高二','schoolVal':'12'},{'schoolLevel':'高三','schoolVal':'13'}];
                    break;
                case 'S4':
                    gradeArr = [{'schoolLevel':'九年级','schoolVal':'9'},{'schoolLevel':'高一','schoolVal':'11'},{'schoolLevel':'高二','schoolVal':'12'},{'schoolLevel':'高三','schoolVal':'13'}];
                    break;
            }
            break;
    }
    var contentHtml = template("schoolGradeInfo", {gradeArr:gradeArr});
    $(".schoolParticular-edit").html(contentHtml);
}
</script>
</@layout.page>
