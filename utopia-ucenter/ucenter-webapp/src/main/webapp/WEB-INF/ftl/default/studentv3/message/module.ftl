<#import "../layout/layout.ftl" as temp />
<#macro messagePage title="通知">
    <@temp.page pageName=''>
        <div class="t-center-container">
            <div class="t-center-slide w-fl-left">
                <span class="leaf leaf-1"></span>
                <span class="leaf leaf-2"></span>
                <span class="leaf leaf-3"></span>
                <span class="leafcope"></span>
                <span class="ts-top"></span>
                <div class="ts-center">
                    <h2 class="w-gray">消息中心</h2>
                    <dl class="tc-box">
                        <dt><img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>"></dt>
                        <dd>
                            <h5 class="w-gray">${(currentUser.profile.realname)!''}</h5>
                            <p class="w-gray"><span class="w-icon w-icon-beans"></span>${currentStudentDetail.userIntegral.usable}</p>
                        </dd>
                    </dl>
                    <ul>
                        <li class="active">
                            <a class="w-gray" onclick="$17.atongji('消息中心-通知','/student/message/index.vpage');" href="javascript:void (0);">通知</a>
                        </li>
                        <#-- 20170322 下线留言板功能-->
                        <#--<li <#if title == "留言板">class="active"</#if>>-->
                            <#--<a class="w-gray" onclick="$17.atongji('消息中心-留言板','/student/conversation/index.vpage');" href="javascript:void (0);">留言板</a>-->
                        <#--</li>-->
                    </ul>
                </div>
                <div class="ts-bottom"></div>
            </div>
            <#nested />
            <div class="w-clear"></div>
        </div>
    </@temp.page>
</#macro>