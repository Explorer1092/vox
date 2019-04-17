<#import "../layout/layout.ftl" as temp />
<#macro studentCenter currentPage='index'>
    <@temp.page pageName='center' clazzName='t-center-bg'>
        <div class="t-center-container">
            <div class="t-center-slide w-fl-left">
                <div class="ts-center">
                    <ul>
                        <li <#if currentPage == ''>class="active"</#if> >
                            <a class="w-gray" onclick="$17.atongji('个人中心-返回极算','${(ProductConfig.getMainSiteBaseUrl())!''}' + '/redirector/apps/go.vpage?app_key=Shensz');" href="javascript:void (0);">返回首页</a>
                        </li>

                        <li <#if currentPage == 'index'>class="active"</#if> >
                            <a class="w-gray" onclick="$17.atongji('个人中心-基本信息','/student/center/index.vpage');" href="javascript:void (0);">基本信息</a>
                        </li>

                        <li <#if currentPage == 'information'>class="active"</#if>>
                            <a class="w-gray" onclick="$17.atongji('个人中心-我的资料','/student/center/information.vpage');" href="javascript:void (0);">我的资料</a>
                        </li>

                        <li <#if currentPage == 'account'>class="active"</#if>>
                            <a class="w-gray" onclick="$17.atongji('个人中心-账号安全','/student/center/account.vpage');" href="javascript:void (0);">账号安全</a>
                        </li>

                        <#--<li <#if currentPage == 'integral'>class="active"</#if>>
                            <a class="w-gray" onclick="$17.atongji('个人中心-我的学豆','/student/center/integral.vpage');" href="javascript:void (0);">我的学豆</a>
                        </li>-->

                        <#--<#if (balance gt 0)!false>
                            <li <#if currentPage == 'recharging'>class="active"</#if>>
                                <a class="w-gray v-studentVoxLogRecord" onclick="$17.atongji('个人中心-我的作业币','${(ProductConfig.getMainSiteBaseUrl())!''}/student/center/recharging.vpage');" href="javascript:void (0);" data-op="my-recharge">我的作业币</a>
                            </li>
                        </#if>-->
                    </ul>
                </div>
            </div>
            <#nested />
            <div class="w-clear"></div>
        </div>
    </@temp.page>
</#macro>




