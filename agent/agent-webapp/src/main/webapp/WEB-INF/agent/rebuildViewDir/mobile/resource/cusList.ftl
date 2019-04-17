<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="客服协助记录" pageJs="cusList" footerIndex=4  navBar="hidden">
    <@sugar.capsule css=['custSer']/>
<div class="crmList-box">
    <div class="fixed-head">
        <#--<div class="c-head">-->
            <#--<span>客服协助记录</span>-->
            <#--<div class="return"><a href="javascript:window.history.back()"><i class="return-icon"></i>返回</a></div>-->
        <#--</div>-->
        <div class="c-opts gap-line tab-head c-flex c-flex-3">
            <span class="the">跟进中（<#if followingList??>${followingList?size!0}</#if>）</span>
            <span>已完成（<#if finishedList??>${finishedList?size!0}</#if>）</span>
            <span>已过期（<#if expiredList??>${expiredList?size!0}</#if>）</span>
        </div>
    </div>

    <div class="tab-main">
        <!--跟进中-->
        <div class="c-list">
            <#if followingList?? && followingList?size gt 0>
                <#list followingList as fl>
                    <#if fl.taskDetail?has_content>
                        <#if fl.taskDetail.category == "TEACHER_GIFT">
                            <div class="pa-content">
                                <dl class="pa-list">
                                    <dt><img src="${fl.taskDetail.picUrl!"#"}" alt=""></dt>
                                    <dd style="background: no;">
                                        <p class="name">${fl.taskDetail.category.value!''} <span style="color: #ff7d5a;position: absolute;right: .01rem;">${fl.taskDetail.status.value!''}</span> </p>
                                        <p>${fl.taskDetail.content!''}</p>
                                        <p>
                                            <a style="float: right;" class="btn-stroke fix-padding js-completeBtn" data-id="${fl.taskDetail.id!'0'}">完成</a>
                                        </p>
                                    </dd>
                                </dl>
                            </div>
                        <#else>
                            <div class="clearfix">
                                <p class="name">${fl.taskDetail.category.value!''} <span style="color: #ff7d5a;position: absolute;right: 1rem;">${fl.taskDetail.status.value!''}</span> </p>
                                <p>${fl.taskDetail.content!''}</p>
                            </div>
                        </#if>
                    </#if>
                </#list>
            </#if>
        </div>
        <!--完成-->
        <div class="c-list">
            <#if finishedList?? && finishedList?has_content>
                <#list finishedList as fl>
                    <#if fl.taskDetail?has_content>
                        <div class="clearfix">
                            <p class="name">${fl.taskDetail.category.value!''} <span style="color: #ff7d5a;position: absolute;right: 1rem;">${fl.taskDetail.status.value!''}</span> </p>
                            <p>${fl.taskDetail.content!''}</p>
                            <#if fl.taskRecord?? && fl.taskRecord?size gt 0>
                            <p style="color: #ee5f5b;">
                                客服处理意见:
                                <#list fl.taskRecord as tr>
                                    ${tr.content!""} <#if tr_has_next>,</#if>
                                </#list>
                            </p>
                            </#if>
                        </div>
                    </#if>
                </#list>
            </#if>
        </div>
        <!--已拒绝-->
        <div class="c-list">
            <#if expiredList?? && expiredList?has_content>
                <#list expiredList as fl>
                    <#if fl.taskDetail?has_content>
                        <div class="clearfix">
                            <p class="name">${fl.taskDetail.category.value!''} <span style="color: #ff7d5a;position: absolute;right: 1rem;">${fl.taskDetail.status.value!''}</span> </p>
                            <p>${fl.taskDetail.content!''}</p>
                        </div>
                    </#if>
                </#list>
            </#if>
        </div>
    </div>

</div>
<script>
    var AT = new agentTool();
    /*--tab切换--*/
    $(".tab-head").children("a,span").on("click",function(){
        var $this=$(this);
        $this.addClass("the").siblings().removeClass("the");
        $(".tab-main").eq(0).children().eq($this.index()).show().siblings().hide();
    });


    $(document).on("click",".js-completeBtn",function(){
        var id = $(this).data("id");
        if(confirm("确定要完成该任务吗?")){
            $.post("finish_task_detail.vpage",{
                taskDetailId:id
            },function(res){
                if(res.success){
                    AT.alert("操作成功");
                    location.reload();
                }else{
                    AT.alert(res.info);
                }
            });
        }
    })
</script>
</@layout.page>