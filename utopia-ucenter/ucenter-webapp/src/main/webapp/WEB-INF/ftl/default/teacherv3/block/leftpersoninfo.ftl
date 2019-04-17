<div class="m-person-infoTwo">
    <#if (showLevelUpTip)!false>
        <div class="lv-keyInfo" style="position: relative; z-index: 2;">
            <div class="k-inner v-clickUpgradeInfo PNG_24"></div>
            <script type="text/javascript">
                $(function(){
                    $(document).on("click", ".v-clickUpgradeInfo", function(){
                        $.get("${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/recordlevelup.vpage", {}, function(){location.href = "/reward/index.vpage";});
                    });
                });
            </script>
        </div>
    </#if>
    <dl>
        <dt class="parson-avatar">
            <a href="/teacher/center/index.vpage?ref=newIndex"><img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>" width="80" height="80"/></a>
        </dt>
        <dd class="parson-info">
            <div class="name" style="padding-top: 10px;">
                <#if (currentUser.profile.realname)?? && currentUser.profile.realname != "">
                    ${(currentUser.profile.realname)!}
                <#else>
                    <a href="/teacher/center/index.vpage#/teacher/center/myprofile.vpage?ref=newIndex" class="w-red">设置姓名</a>
                </#if>
            </div>
            <#if (currentTeacherDetail.isPrimarySchool())!false>
                <div style="height: 3px; overflow: hidden;"></div>
                <div class="privilege">
                    <#if currentUser.fetchCertificationState() == "SUCCESS" >
                        <a href="/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage?ref=newIndex" target="_blank" class="auth"><span class="w-icon-public w-icon-new-authVip" title="已认证">已认证</span></a>
                    <#else>
                        <a href="/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage?ref=newIndex" class="auth" title="未认证"><span class="w-icon-public w-icon-new-authVip-dis">未认证</span></a>
                    </#if>
                    <#if ((currentTeacherDetail.subject == "CHINESE" && currentTeacherDetail.schoolAmbassador) || currentTeacherDetail.subject != "CHINESE")!false>
                        <span class="w-icon-public <#if (currentTeacherDetail.schoolAmbassador)!false>w-icon-new-authAmb<#else>w-icon-new-authAmb-dis</#if>" title="校园大使">校园大使</span>
                    </#if>
                    <a href="/teacher/center/index.vpage#/teacher/center/mygold.vpage?ref=newIndex" style="display: inline-block;"><span class="w-icon-public w-icon-gold PNG_24"></span>${(currentTeacherDetail.userIntegral.usable)!0}
                        <i class="w-orange-tips PNG_24 w-ft-well w-ag-center"><@ftlmacro.garyBeansText/></i>
                    </a>
                </div>
            <#else>
                <div class="privilege">
                    <#if currentUser.fetchCertificationState() == "SUCCESS" >
                        <a href="/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage?ref=newIndex" target="_blank" class="auth"><span class="w-icon-public w-icon-new-authVip" title="已认证">已认证</span></a>
                    <#else>
                        <a href="/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage?ref=newIndex" class="auth" title="未认证"><span class="w-icon-public w-icon-new-authVip-dis">未认证</span></a>
                    </#if>
                    <#if (currentTeacherWebGrayFunction.isAvailable("MSIntegral", "Mall"))!false >
                    <a href="/teacher/center/index.vpage#/teacher/center/mygold.vpage?ref=newIndex" style="display: inline-block;"><span class="w-icon-public w-icon-gold PNG_24"></span>${(currentTeacherDetail.userIntegral.usable)!0}
                        <i class="w-orange-tips PNG_24 w-ft-well w-ag-center"><@ftlmacro.garyBeansText/></i>
                    </a>
                    </#if>
                </div>
            </#if>
        </dd>
        <dd style="clear: both; padding: 0; margin: 0; width: 100%;"></dd>
    </dl>
</div>
<!--主菜单-->
<#include "leftmenu.ftl" />
<!--m-synthesize-->
<div class="m-synthesize">
    <#--客服-->
    <div class="sever-info m-side-width">
        <div class="w-btn w-btn-gray w-btn-block">
            <p class="tel">
                <#if (currentTeacherDetail.isPrimarySchool())!false>
                    <span><@ftlmacro.hotline/></span>
                <#else>
                    <span style="font-size: 20px;"><@ftlmacro.hotline phoneType="junior"/></span>
                </#if>
                <span class="text">客服时间：8:00-22:00</span>
            </p>
            <div class="s-help">
                <#if (currentTeacherDetail.isPrimarySchool())!false>
                    <a href="javascript:void(0);" class="w-btn w-btn-small on-line w-btn-green message_right_sidebar">
                        <span class="w-icon w-icon-white w-icon-25"></span>
                        <span class="w-icon-md">反馈建议</span>
                    </a>
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/kf/index.vpage?menu=teacher" class="w-btn w-btn-small on-line" target="_blank">
                        <span class="w-icon w-icon-white w-icon-15"></span>
                        <span class="w-icon-md">帮助中心</span>
                    </a>
                <#else>
                    <a href="javascript:void(0);" class="w-btn w-btn-small on-line w-btn-green message_right_sidebar">
                        <span class="w-icon w-icon-white w-icon-25"></span>
                        <span class="w-icon-md">反馈建议</span>
                    </a>
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/kf/junior.vpage?menu=teacher" class="w-btn w-btn-small on-line" target="_blank">
                        <span class="w-icon w-icon-white w-icon-15"></span>
                        <span class="w-icon-md">帮助中心</span>
                    </a>
                </#if>
            </div>
        </div>
    </div>
</div>
<div style="height: 80px;"><#--占位--></div>
