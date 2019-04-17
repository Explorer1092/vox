<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<#assign shortIconTail = "?x-oss-process=image/resize,w_48,h_48/auto-orient,1">
<#assign register_stu_have = register_stu?? && register_stu?has_content >
<#assign auth_stu_have = auth_stu?? && auth_stu?has_content >
<#assign single_act_stu_have = single_act_stu?? && single_act_stu?has_content >
<#assign double_act_stu_have = double_act_stu??&& double_act_stu?has_content  >
<@layout.page title="填写进校记录" pageJs="add_intoschool" footerIndex=4 navBar="hidden">
<@sugar.capsule css=['school']/>
    <div class="head fixed-head" style="display: none;">
        <a href="javascript:void(0);" class="inner-right js-submitSchoolSRecord" style="display: none;">提交</a>
    </div>
    <div class="flow">
        <div class="item tip">
            必填
                <span class="inner-right">
                ${workTime?string("yyyy-MM-dd")!''}
                </span>
        </div>
        <div class="item">
            学校名称
            <div class="inner-right js-visitSchoolBtn">
                <#if schoolName??>${schoolName!''}<#else>请选择</#if>
            </div>
            <input hidden type="text" id="schoolId" name="schoolId" value="<#if schoolId??>${schoolId!''}</#if>" class="js-need js-postData"  data-einfo="请选择拜访学校"/>
        </div>
        <div class="item GPS">
            请打开GPS
            <#if signType??&&(signType == 1|| signType==2)>
                <div class="inner-right btn-stroke fix-padding disabled">
                    签到成功
                </div>
            <#else>
                <div class="inner-right btn-stroke fix-padding js-signBtn">
                    签到
                </div>
            </#if>
            <input type="hidden" id="lat" name="lat" value="" class="js-need" data-einfo="请签到获取学校位置"/>
            <input type="hidden" id="lng" name="lng" value="" class=""/>
        </div>
        <div class="item tip js-imageItem" style="display: none;">
            <i class="icon tips"></i>提示：此位置距离学校过远，请拍摄学校正门照片！
            <div class="photo clearfix">
                <div class="shot">
                    <i class="icon img"></i>拍正门照
                </div>
                <div class="pick">
                    <div class="file" id="getSchoolGate"><img width="100%" height="100%" src="<#if photoUrl??>${photoUrl!""}${shortIconTail}</#if>"></div>
                    点此添加照片
                </div>
                <input type="hidden" id="photoUrl" name="photoUrl" value="<#if photoUrl??>${photoUrl!""}</#if>">
            </div>
        </div>
        <div class="item tip">
            <i class="icon theme"></i>拜访主题
            <div class="c-main clearfix mainTitle"
                 style="padding-bottom: 0.25rem;">
                <div class="btn-stroke fix-padding" data-type="11">促进注册</div>
                <div class="btn-stroke fix-padding" data-type="12">促进签约</div>
                <div class="btn-stroke fix-padding" data-type="13">促进月活</div>
                <div class="btn-stroke fix-padding" data-type="14">寻求介绍</div>
                <div class="btn-stroke fix-padding" data-type="15">确认基本信息</div>
            </div>
        </div>
        <div class="item tip t-visit clearfix js-visitTeacherBtn">
            <i class="icon visit"></i>拜访老师
            <div class="inner-right">
                <div class="t-list" id="visitTeachListDiv">
                    <#if visitTeacherList?? && visitTeacherList?size gt 0>
                        <#list visitTeacherList as vt>
                        <#if vt.teacherName??>
                            ${vt.teacherName!""}<#if vt_has_next>，</#if>
                        <#else>
                            ${vt.teacherId!''}<#if vt_has_next>，</#if>
                        </#if>
                        </#list>
                    <#else>
                        请选择
                    </#if>
                </div>
            </div>
        </div>
        <div class="item tip">
            选填
        </div>
        <div class="item visit-result" >
            <i class="icon effect"></i>
            备忘信息（可不填）
            <span class="inner-right js-writeVisitDetail">
                请填写
            </span>
        </div>
        <#if schoolMemorandumInfo?? && (schoolMemorandumInfo!"")!="">
            <div class="item">
                学校备忘录
                <span class="inner-right" style="background: none;word-break: break-all"> ${schoolMemorandumInfo!""}</span>
            </div>
        </#if>
        <#if visitTeacherList??>
            <#if visitTeacherList?size gt 0>
                <#list visitTeacherList as vt>
                    <#if vt.visitInfo?? && vt.visitInfo != "">
                        <div class="item" data-tid="${vt.teacherId!''}" style="border-top: .05rem solid #f0eff5;">
                            ${vt.teacherName!""}
                            <span class="inner-right" style="background: none;word-break: break-all">${vt.visitInfo!""}</span>
                        </div>
                    </#if>
                </#list>
            </#if>
        </#if>
    </div>
    <div id="confirmSignDialog" style="display:none">
           <div class="clazz-popup">
            <div class="text">
                提示:您的位置距离学校过远!
            </div>
            <div class="popup-btn">
                <a href="javascript:void(0);" class="js-remove" id="reSignBtn">重新签到</a>
                <a href="javascript:void(0);" style="background:#ff7d5a;color:#fff;border-bottom-right-radius:0.2rem;" class="js-submit" id="getSchoolGateImageBtn">拍正门照</a>
            </div>
        </div>
        <div class="popup-mask js-remove"></div>
    </div>


<div id="photoSignDialog" style="display:none">
    <div class="clazz-popup">
        <div class="text">
            提示:该学校还没有位置信息，请拍照!
        </div>
        <div class="popup-btn">
            <a href="javascript:void(0);" style="width:100%;background:#ff7d5a;color:#fff;border-bottom-left-radius:0.2rem;border-bottom-right-radius:0.2rem;" class="js-submit" id="getSchoolGateImagePhotoBtn">拍正门照</a>
        </div>
    </div>
    <div class="popup-mask js-remove"></div>
</div>
<div class="schoolParticular-pop" style="display: none;" id="repatePane">
    <div class="inner">
        <h1>提交成功！</h1>
        <p class="info">注：该学校未鉴定，依据本次提交的照片已自动生成鉴定学校申请</p>
        <div class="btn">
            <a href="javascript:void(0);" class="submitBtn">我知道了</a>
        </div>
    </div>
</div>
<div class="schoolParticular-pop" style="display: none;" id="completeSchoolInfo">
    <div class="inner">
        <h1>提示！</h1>
        <p class="info">发现您当前的学校基础数据不全或异常，请先补充或确认学校基础数据</p>
        <div class="btn">
            <a href="javascript:void(0);" class="schoolSubmitBtn">前往确认</a>
        </div>
    </div>
</div>
<script>
    var moduleName = "m_QnJGa29M";
    var userId = "${requestContext.getCurrentUser().getUserId()!0}";
    var AT = new agentTool();
    var workRecordType = "SCHOOL" ;
    <#if schoolId??>
        $.get("/mobile/school_clue/check_school_info_complete.vpage?schoolId=${schoolId!0}",function (res) {
            if(res && res.success && res.complete) {

            }else{
                $("#completeSchoolInfo").show();
            }
        });
    </#if>
    $("#completeSchoolInfo .schoolSubmitBtn").on("click",function () {
        openSecond('/mobile/school_clue/confirm_school_info.vpage?schoolId=${schoolId!0}');
    });
    var selectedType = "";
        <#if workTitle??>
        selectedType = "${workTitle!}";
        </#if>
        var schoolLevel = ${schoolLevel!'0'};
        <#if (schoolLevel!0) != 4>
        $("#selectAgencyId").on("change",function(){
            var type = $(this).val();
            $('#agencyId').val(type);
            $('#agencyDisplay').html($('#selectAgencyId option:selected').text());
        });
        if(selectedType.length != 0){
            $('.mainTitle').children().eq(selectedType-11).addClass("the");
        }
        </#if>
        var submitAble = true;
        $(document).ready(function () {
            reloadCallBack();
            var checkData = function(){

                if(!$("#schoolId").val()){
                    AT.alert("请选择学校");
                    return false;
                }

                if(imageSuccess || $("#photoUrl").val()){
                    imageSuccess = true;
                    signSuccess = true;
                }

                if(!signSuccess){
                    AT.alert("请签到获取学校位置");
                    return false;
                }

                if(!imageSuccess){
                    AT.alert("请拍摄学校正门照片");
                    return false;
                }

                if($(".js-visitTeacherBtn").html().trim() == "请选择"){
                    AT.alert("请选择拜访老师");
                    return false;
                }
                if($(".mainTitle>div.the").length == 0){
                    AT.alert("请选择拜访主题");
                    return false;
                }

                if($('[name="isAgencyClue"]').length != 0){
                    if($('[name="isAgencyClue"]>div.the').length == 0){
                        AT.alert("请选择选择是否为代理提供的线索");
                        return false;
                    }
                }
                return true;
            };
            if(workRecordType == "SCHOOL"){
                reloadCallBack();
            }
            try{
                var setTopBar = {
                    show:true,
                    rightText:"提交",
                    rightTextColor:"ff7d5a",
                    needCallBack:true
                };

                var topBarCallBack = function () {
                    try{
                        if(submitAble){
                            if(checkData()){
                                submitAble = false;
                                var postData = {};
                                $.each($(".js-postData"),function(i,item){
                                    postData[item.name] = $(item).val();
                                });

                                postData["workTitle"] = $(".mainTitle>div.the").data("type");
                                postData["followingTime"] = $("#nextVisitTime").val();
                                postData["isAgencyRegion"] = $('[name="isAgencyClue"]>div.the').attr("data-opvalue");
                                postData["agencyId"]=$("#agencyId").val();

                                $.post("saveSchoolRecord.vpage",postData, function (res) {
                                    submitAble = true;
                                    if(res.success){
                                        if(!res.appraisalSchool){
                                            AT.alert("添加进校记录成功");
                                            setTimeout(disMissViewCallBack(),1500);
                                        }else{
                                            $("#repatePane").show();
                                        }

                                    }else{
                                        AT.alert(res.info);
                                    }
                                })
                            }
                        }else{
                            AT.alert("记录已被提交正在处理,请稍候...");
                            submitAble = true;
                        }
                    }catch(e){alert(e)}
                };
                setTopBarFn(setTopBar,topBarCallBack);
            }catch(e){alert(e)}
        })
</script>
<script src="/public/rebuildRes/js/common/common.js"></script>
</@layout.page>