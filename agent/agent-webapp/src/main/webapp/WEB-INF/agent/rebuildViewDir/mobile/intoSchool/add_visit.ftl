<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="添加陪访" pageJs="add_visit" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['new_base','school']/>
    <a href="javascript:void(0)" class="inner-right js-submitVisBtn js-success" style="display:none;">提交</a>
<div class="flow">
    <input id="schoolId" type="text" value="${schoolId!0}" hidden>
    <div class="item GPS">
        请打开GPS
        <#if signType??&&(signType == 1|| signType==2)>
            <div class="inner-right btn-stroke fix-padding disabled">
                签到成功
            </div>
        <#else>
            <div class="inner-right btn-stroke fix-padding js-signBtn js-signBtn1">
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
    <div class="item tip clearfix" style="padding-bottom:1rem;">
        陪访目的
        <div class="inner-right flex js-visTarget" style="padding:0;width:100%;">
            <div data-type="1" class="btn-stroke fix-width" style="width:5.2rem;margin-right:0.5rem;margin-top:.25rem;">专员技能辅导</div>
            <div data-type="2" class="btn-stroke fix-width" style="width:5.2rem;margin-right:0.5rem;margin-top:.25rem;">重点学校跟进</div>
            <div data-type="3" class="btn-stroke fix-width" style="width:5.2rem;margin-top:.25rem;">市场情况了解</div>
        </div>
    </div>
    <div class="visit_box">
        <div class="v_title">你对陪访对象的评价：</div>
        <div class="v_side evaluate">
            <div class="list">
                <p class="subtitle">进校准备充分度</p>
                <ul class="preparationScore">
                    <li><span></span></li>
                    <li><span></span></li>
                    <li><span></span></li>
                    <li><span></span></li>
                    <li><span></span></li>
                </ul>
                <p class="per"></p>
            </div>
            <div class="list">
                <p class="subtitle">产品/话术熟练度</p>
                <ul class="productProficiencyScore">
                    <li><span></span></li>
                    <li><span></span></li>
                    <li><span></span></li>
                    <li><span></span></li>
                    <li><span></span></li>
                </ul>
                <p class="per"></p>
            </div>
            <div class="list">
                <p class="subtitle">结果符合预期度</p>
                <ul class="resultMeetExpectedResultScore">
                    <li><span></span></li>
                    <li><span></span></li>
                    <li><span></span></li>
                    <li><span></span></li>
                    <li><span></span></li>
                </ul>
                <p class="per"></p>
            </div>
        </div>
    </div>
    <div class="item tip" style="overflow: hidden;">
        发现的问题&改进建议：
        <textarea id="visitAdvice" name="visitAdvice" rows="5" class="content js-postData" placeholder="请点击填写..."></textarea>
    </div>
</div>
    <div class="schoolParticular-pop" style="/*display: none;*/" id="completeSchoolInfo">
        <div class="inner">
            <h1>提示！</h1>
            <p class="info">发现您当前的学校基础数据不全或异常，请先补充或确认学校基础数据</p>
            <div class="btn">
                <a href="javascript:void(0);" class="schoolSubmitBtn">前往确认</a>
            </div>
        </div>
    </div>
<script src="/public/rebuildRes/js/mobile/intoSchool/add_intoschool.js"></script>
<script>
    var moduleName = "m_9HYOoJg7";
    var workRecordType = "VISIT" ;
    var userId = "${requestContext.getCurrentUser().getUserId()!0}";
    var ojbArr = ["1分/差","2分/一般","3分/好","4分/很好","5分/标杆"];
    $(document).on("click",".evaluate ul li",function(){
        var index = $(this).index();
        var parent = $(this).parent();
        var parentLi = parent.find("li");
        parentLi.removeClass("active");
        parent.siblings(".per").html(ojbArr[index]);
        for(var i=0;i <= index ;i++){
            parentLi.eq(i).addClass("active");
        }
    });
    var schoolRecordId = "${schoolRecordId!}";
    var AT = new agentTool();
    var alertDialog = AT.getCookie("alertDialog");
    if(alertDialog == -1){
        $("#completeSchoolInfo").hide();
    }
    AT.clearCookie("alertDialog");
    $(document).on("click","#completeSchoolInfo .schoolSubmitBtn",function () {
        openSecond('/mobile/school_clue/confirm_school_info.vpage?schoolId=${schoolId!0}');
    });
</script>
</@layout.page>
