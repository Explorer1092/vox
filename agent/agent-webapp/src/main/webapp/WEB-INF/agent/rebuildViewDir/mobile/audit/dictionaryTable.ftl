<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="字典表调整" footerIndex=3 navBar="hidden">
    <@sugar.capsule css=['audit'] />
<div class="crmList-box resources-box">
    <div class="res-top fixed-head">
        <div class="return js-return"><a href="javascript:void(0)"><i class="return-icon"></i>返回</a></div>
        <span class="return-line"></span>
        <span class="res-title">字典表调整</span>
    </div>
    <div class="c-main">
        <div>
            <div class="c-opts gap-line c-flex c-flex-3" style="margin-bottom:.5rem">
                <span class="the js-todo">待处理</span>
                <span class="js-done" data-index="1">已通过</span>
                <span class="js-done" data-index="2">已驳回</span>
            </div>
            <div class="tab-main" style="clear:both">
            <#--待审核-->
                <div>
                    <#if dataList??>
                        <#list dataList as item>
                            <div class="adjustmentExamine-box" style="margin-top: .5rem">
                                <div class="adjust-head">
                                    <div class="right">${item.apply.createDatetime?string("MM-dd")}</div>
                                    <div class="name">${item.apply.accountName!''}【<#if item.apply.modifyType?? && item.apply.modifyType == 1>添加学校<#elseif item.apply.modifyType?? && item.apply.modifyType == 2>删除学校<#elseif item.apply.modifyType?? && item.apply.modifyType == 3>业务变更</#if>】</div>
                                </div>
                                <div class="adjust-content">
                                    <p class="title">名称：${item.apply.schoolName!''}(${item.apply.schoolId!0})</p>
                                    <p class="stage">阶段：<#if item.apply.schoolLevel??><#if item.apply.schoolLevel == 1>小学<#elseif item.apply.schoolLevel == 2>中学<#elseif item.apply.schoolLevel == 3>高中<#elseif item.apply.schoolLevel == 5>学前</#if></#if>
                                    </p>
                                    <p class="area">区域：${item.apply.regionName!''}</p>
                                    <p class="pattern"><span style="display: inline-block;float: left;">模式：</span><span class="modifyDesc" style="display: inline-block">${item.apply.modifyDesc!''}</span></p>
                                    <p class="grade">等级：<#if item.apply.schoolPopularity?has_content><#if item.apply.schoolPopularity == 'A'>名校</#if><#if item.apply.schoolPopularity == 'B'>重点校</#if><#if item.apply.schoolPopularity == 'C'>普通校</#if><#else>重点校</#if></p>
                                    <p class="reason">调整原因：${item.apply.comment!''}</p>
                                    <#--<p class="info">处理意见：情况属实，同意</p>-->
                                </div>
                                <div class="adjust-side">
                                    <textarea class="textarea_${item.apply.workflowId!0}" placeholder="请填写处理意见"></textarea>
                                    <div class="btn">
                                        <a href="javascript:void(0);" class="white_btn js-submit" data-info="驳回成功" data-result="2" data-aid="${item.apply.workflowId!0}">驳回</a>
                                        <a href="javascript:void(0);" class="white_btn orange js-submit" data-info="确认成功" data-result="1" data-aid="${item.apply.workflowId!0}" data-schoollevel="${item.apply.schoolLevel!0}">同意</a>
                                    </div>
                                </div>
                            </div>
                        </#list>
                    </#if>
                </div>
            </div>
        </div>
    </div>
</div>
<script>
    var AT = new agentTool();
    //拒绝按钮 + 同意按钮
    $(document).on('click','.js-submit',function(){
        var data=$(this).data();
        if($('.textarea_'+data.aid).val() == ''){
            AT.alert('请填写处理意见');
            return false;
        }
        var processUserList = new Array();
        if(data.result == 1 && data.schoollevel== 5){
            processUserList.push({"userPlatform":"agent", "account":"system", "accountName":"系统"});
        }
        var processUsers = JSON.stringify(processUserList);
        var reqData={
            processResult: data.result ,
            workflowId    : data.aid ,
            processNote :$('.textarea_'+data.aid).val(),
            processUsers:processUsers
        };
        $.post("process.vpage",reqData,function(res){
            if (res.success) {
                AT.alert(data.info);
                setTimeout('window.location.reload()',1500);
            } else {
                AT.alert(res.info);
            }
        });
    });
    $(document).on('click','.js-done',function(){
        location.href = "/mobile/audit/done_list.vpage?processResult="+$(this).data('index')+"&workflowType=1";
    });
    $(document).on('click','.js-return',function(){
        location.href = "/mobile/audit/index.vpage";
    });
    $('.modifyDesc').each(function(){
        var pattern = $(this).html().trim();
        $(this).html(pattern.replace(/\n/g, '<br />'));
    });
</script>
</@layout.page>