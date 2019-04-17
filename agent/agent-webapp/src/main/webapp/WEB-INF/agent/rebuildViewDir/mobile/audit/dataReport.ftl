<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="大数据报告申请" footerIndex=3 navBar="hidden">
    <@sugar.capsule css=['audit'] />
<div class="crmList-box resources-box">
    <div class="res-top fixed-head">
        <div class="return js-return"><a href="javascript:void(0)"><i class="return-icon"></i>返回</a></div>
        <span class="return-line"></span>
        <span class="res-title">大数据报告申请</span>
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
                            <div class="adjustmentExamine-box">
                                <div class="adjust-head js-itemBtn" data-sid="${item.apply.id!0}">
                                    <div class="right">${item.apply.createDatetime?string("MM-dd")}</div>
                                    <div class="">${item.apply.accountName!'123'}</div>
                                </div>
                                <div class="adjust-content">
                                    <p class="pattern">学科：
                                        <#if item.apply.subject?? && item.apply.subject == 1>
                                            小学英语
                                        </#if>
                                        <#if item.apply.subject?? && item.apply.subject == 2>
                                            小学数学
                                        </#if>
                                    </p>
                                    <p class="grade">区域/学校：
                                        <#if item.apply.reportLevel == 1>${item.apply.cityName!}/${item.apply.countyName!}
                                        <#elseif item.apply.reportLevel == 2>${item.apply.countyName!}
                                        <#elseif item.apply.reportLevel == 3>${item.apply.schoolName!}（${item.apply.schoolId!}）
                                        </#if>
                                    </p>
                                    <p class="reason">时间维度：
                                        <#if item.apply.reportType?? && item.apply.reportType == 1>学期报告
                                            <#if item.apply.reportTerm?? && item.apply.reportTerm == 1>2016年9-12月
                                            <#elseif item.apply.reportTerm?? && item.apply.reportTerm == 2>2017年1-6月
                                            </#if>
                                        <#elseif item.apply.reportType?? && item.apply.reportType == 2>月度报告${item.apply.reportMonth!''}
                                        </#if>
                                    </p>

                                    <p class="reason">样本校：
                                        <#if item.apply.sampleSchoolName??>
                                        ${item.apply.sampleSchoolName!''}(${item.apply.sampleSchoolId!''})
                                        <#else>
                                            无
                                        </#if>
                                    </p>
                                </div>
                                <div class="adjust-side">
                                    <textarea class="textarea_${item.apply.workflowId!0}" placeholder="请填写处理意见"></textarea>
                                    <div class="btn">
                                        <a href="javascript:void(0);" class="white_btn js-submit" data-info="驳回成功" data-result="2" data-aid="${item.apply.workflowId!0}">驳回</a>
                                        <a href="javascript:void(0);" class="white_btn orange js-submit" data-info="确认成功" data-result="1" data-aid="${item.apply.workflowId!0}">同意</a>
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
        var reqData={
            processResult: data.result ,
            workflowId    : data.aid ,
            processNote :$('.textarea_'+data.aid).val()
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
        location.href = "/mobile/audit/done_list.vpage?processResult="+$(this).data('index')+"&workflowType=7";
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