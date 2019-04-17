<#import "../layout.ftl" as notices>
<@notices.page title='孩子心愿' pageJs="wishNotices">
<style>
    body {
        background-color: #757cfe;
    }
</style>
<div class="pr-box pr-mt">
    <div class="pr-title"><div>孩子心愿</div></div>
    <div class="pr-apply">
        <#if msgs?? && msgs?size gt 0>
            <#list msgs as msg>
                <div class="<#if msg_index%2 == 0 >odd</#if> clearfix">
                    <div class="txt">
                        <p>${msg.createTime!}</p>
                        <p>${msg.content!}</p>
                    </div>
                    <#if !msg.clicked>
                        <#if msg.messageType == 39 || msg.messageType == 40>
                            <a href="javascript:void(0)" class="btn btn-blue btn-jump" data-nid="${msg.nid!0}" data-url="${msg.url!0}">创建目标</a>
                        <#elseif msg.messageType == 41>
                            <a href="javascript:void(0)" class="btn btn-green btn-jump" data-nid="${msg.nid}" data-url="${msg.url!0}">进度+1</a>
                        <#elseif msg.messageType == 42>
                            <a href="javascript:void(0)" class="btn btn-yellow btn-jump" data-nid="${msg.nid!0}" data-url="${msg.url!0}">发放奖励</a>
                        </#if>
                    </#if>
                </div>
            </#list>
        <#else>
            <div class="clearfix" style="line-height: 2rem; text-align: center; color: #999; padding: 3rem 0;">
                孩子还没许愿，快去创建新目标，<br/>给孩子一份惊喜吧！
            </div>
        </#if>
    </div>
</div>
</@notices.page>