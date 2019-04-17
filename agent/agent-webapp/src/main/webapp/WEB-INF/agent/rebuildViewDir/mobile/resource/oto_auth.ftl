<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="快乐学扫描权限设置" pageJs="" footerIndex=4>
    <@sugar.capsule css=['school']/>
<style>
    body{
        background-color:rgb(241, 242, 245)
    }
</style>
<#if schoolKlxPrivilegeInfo?? && schoolKlxPrivilegeInfo?size gt 0>
<div class="permissionSetting-box">
    <div class="set_title">阅卷及答题卡相关权限：</div>
    <div class="set_content">
        <div class="list">
            <div class="left">填涂号位数</div>
            <div class="right">
    <#if schoolKlxPrivilegeInfo.scanNumberDigit?has_content>${schoolKlxPrivilegeInfo.scanNumberDigit!"未设定"}<#else>未设定</#if>
            </div>
        </div>
        <div class="list">
            <div class="left">阅读机权限</div>
            <div class="right">
                <span class="font"><#if schoolKlxPrivilegeInfo.scanMachineFlag?? && schoolKlxPrivilegeInfo.scanMachineFlag>已开通<#else>未开通，开通请邮件销运</#if></span>
            </div>
        </div>
        <div class="list">
            <div class="left">校本题库权限</div>
            <div class="right">
                <#if  schoolKlxPrivilegeInfo.questionBankFlag?? && schoolKlxPrivilegeInfo.questionBankFlag>
                    <span class="font orange">已开通</span>
                    <a href="javascript:void(0);" class="btn js-btn gray" data-type="1" data-name="questionBankFlag" data-opend="false" data-info="校本题库权限">取消</a>
                <#else>
                    <span class="font">未开通</span>
                    <a href="javascript:void(0);" class="btn js-btn" data-type="1" data-name="questionBankFlag" data-opend="true" data-info="校本题库权限">开通</a>
                </#if>
            </div>
        </div>
        <div class="list">
            <div class="left">题卡合一答题卡权限</div>
            <div class="right">
                <#if schoolKlxPrivilegeInfo.questionCardFlag?? && schoolKlxPrivilegeInfo.questionCardFlag>
                    <span class="font orange">已开通</span>
                    <a href="javascript:void(0);" class="btn js-btn gray" data-type="1" data-name="questionCardFlag" data-opend="false" data-info="题卡合一答题卡权限">取消</a>
                <#else>
                    <span class="font">未开通</span>
                    <a href="javascript:void(0);" class="btn js-btn" data-type="1" data-name="questionCardFlag" data-opend="true" data-info="题卡合一答题卡权限">开通</a>
                </#if>
            </div>
        </div>
        <div class="list">
            <div class="left">条形码答题卡权限</div>
            <div class="right">
                <#if schoolKlxPrivilegeInfo.barcodeAnswerQuestionFlag?? && schoolKlxPrivilegeInfo.barcodeAnswerQuestionFlag>
                    <span class="font orange">已开通</span>
                    <a href="javascript:void(0);" class="btn js-btn gray" data-type="1" data-name="barcodeAnswerQuestionFlag" data-opend="false" data-info="条形码答题卡权限">取消</a>
                <#else>
                    <span class="font">未开通</span>
                    <a href="javascript:void(0);" class="btn js-btn" data-type="1" data-name="barcodeAnswerQuestionFlag" data-opend="true" data-info="条形码答题卡权限">开通</a>
                </#if>
            </div>
        </div>
        <div class="list">
            <div class="left">A3答题卡权限</div>
            <div class="right">
                <#if schoolKlxPrivilegeInfo.a3AnswerQuestionFlag?? && schoolKlxPrivilegeInfo.a3AnswerQuestionFlag>
                    <span class="font orange">已开通</span>
                    <a href="javascript:void(0);" class="btn js-btn gray" data-type="1" data-name="a3AnswerQuestionFlag" data-opend="false" data-info="A3答题卡权限">取消</a>
                <#else>
                    <span class="font">未开通</span>
                    <a href="javascript:void(0);" class="btn js-btn" data-type="1" data-name="a3AnswerQuestionFlag" data-opend="true" data-info="A3答题卡权限">开通</a>
                </#if>
            </div>
        </div>
        <div class="list">
            <div class="left">手写答题卡权限</div>
            <div class="right">
                <#if schoolKlxPrivilegeInfo.manualAnswerQuestionFlag?? && schoolKlxPrivilegeInfo.manualAnswerQuestionFlag>
                    <span class="font orange">已开通</span>
                    <a href="javascript:void(0);" class="btn js-btn gray" data-type="1" data-name="manualAnswerQuestionFlag" data-opend="false" data-info="手写答题卡权限">取消</a>
                <#else>
                    <span class="font">未开通</span>
                    <a href="javascript:void(0);" class="btn js-btn" data-type="1" data-name="manualAnswerQuestionFlag" data-opend="true" data-info="手写答题卡权限">开通</a>
                </#if>
            </div>
        </div>
    </div>
</div>
    <div class="permissionSetting-box">
        <#if schoolKlxPrivilegeInfo.subjects?? && schoolKlxPrivilegeInfo.subjects?size gt 0>
            <div class="set_title">学科扫描权限：</div>
            <#list schoolKlxPrivilegeInfo.subjects as subjects>
                <div class="set_content">
                    <div class="list">
                        <div class="left">${subjects.name!""}</div>
                        <div class="right">
                            <#if subjects.opened?? && subjects.opened>
                                <span class="font orange">已开通</span>
                                <#if subjects.showOpenBtn?? && subjects.showOpenBtn>
                                    <a href="javascript:void(0);" class="btn js-btn gray" data-type="2" data-name="${subjects.value!""}" data-opend="false" data-info="${subjects.name!""}">取消</a>
                                </#if>
                            <#else>
                                <span class="font">未开通</span>
                                <#if subjects.showOpenBtn?? && subjects.showOpenBtn>
                                    <a href="javascript:void(0);" class="btn js-btn" data-type="2" data-name="${subjects.value!""}" data-opend="true" data-info="${subjects.name!""}">开通</a>
                                </#if>
                            </#if>
                        </div>
                    </div>
                </div>
            </#list>
        </#if>
    </div>
</#if>
<div class="schoolParticular-pop" style="display: none;" id="repatePane">
    <div class="inner">
        <h1 class="warning_message"></h1>
        <p class="info"></p>
        <div class="btn">
            <a href="javascript:void(0);" class="white_btn">取消</a>
            <a href="javascript:void(0);" class="submitBtn">确定</a>
        </div>
    </div>
</div>
<#--<div class="schoolParticular-pop" style="" id="repatePane">-->
    <#--<div class="inner">-->
        <#--<h1 class="warning_message"></h1>-->
        <#--<p class="info"></p>-->
        <#--<div class="btn">-->
            <#--<a href="javascript:void(0);" class="white_btn">取消</a>-->
            <#--<a href="javascript:void(0);" class="submitBtn">确定</a>-->
        <#--</div>-->
    <#--</div>-->
<#--</div>-->
<script>
    var _this;
    $(document).on("click",".js-btn",function(){
         _this = $(this);
        $('.warning_message').html("确定取消/开启" + $(this).data("info") +"？")
        $("#repatePane").show();
        $('.white_btn').on("click",function(){
            $("#repatePane").hide();
        });
    });
    $(".submitBtn").on("click",function(){
        $("#repatePane").hide();
        $.post("oto_auth_set.vpage",{schoolId:${schoolId!0},authType:_this.data("type"),authName:_this.data("name"),opened:_this.data("opend")},function(res){
            if(res.success){
                _this.toggleClass('gray');
                if(_this.html()  == "取消"){
                    _this.html("开通");
                    _this.siblings().removeClass("orange").html("未开通");
                    _this.data("opend",true);
                }else if(_this.html() == "开通"){
                    _this.html("取消");
                    _this.siblings().addClass("orange").html("已开通");
                    _this.data("opend",false);
                }
            }else{
                AT.alert(res.info);
            }
        });
    });
</script>
</@layout.page>
