<#import "../layout.ftl" as msgcenter>
<@msgcenter.page title='消息中心' pageJs="msgcenter">
<@sugar.capsule css=['jbox'] />

<div class="main">
    <#if !notices?has_content>
        <h2 class="title_info_box title_info_green_box">暂无作业消息</h2>
    <#else>
        <h2 class="title_info_box title_info_green_box">最新消息(最近3天)</h2>
        <div class="task_mu_tips">
            <ul>
                <#list notices as notice>
                    <li>
                        <div class="ts_block js-msgClick" data-href="${notice.url!''}">
                            <div class="tm-box">
                                <div class="tip">
                                    <span class="know_homework_tip know_homework_tip_blue">
                                        ${notice.title!''}
                                        <i class="red">●</i>
                                    </span>
                                    <p>
                                        <span class="time">${notice.createTime!""}</span>
                                    </p>
                                </div>
                                <p class="font">
                                    ${notice.content!""}
                                </p>
                                <p class="btn">
                                    <span>${notice.tip!""}</span>
                                </p>
                            </div>
                        </div>
                    </li>
                </#list>
            </ul>
        </div>
    </#if>
</div>
</@msgcenter.page>