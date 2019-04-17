<#-- @ftlvariable name="wishProduct" type="com.voxlearning.utopia.service.reward.mapper.RewardProductDetail" -->
<#--根据角色获取相应积分-->
<#if (currentUser.userType) == 3>
    <#assign userIntegralftl = currentStudentDetail.userIntegral.usable userType="STUDENT"/>
<#elseif (currentUser.userType) == 1>
    <#assign userIntegralftl = currentTeacherDetail.userIntegral.usable userType="TEACHER"/>
<#elseif (currentUser.userType) == 8>
    <#assign userIntegralftl = currentResearchStaffDetail.userIntegral.usable userType="RSTAFF"/>
</#if>
<#macro userInfo>
    <div class="p_m_info">
        <span class="span1"></span>
        <span class="span-inline">${(currentUser.profile.realname)!''}</span>
        <i class="w-gold-icon ${((userType == "STUDENT")!false)?string("w-bean-yellow-icon", "w-gold-icon-8")}"></i>
        <span class="span-inline">${userIntegralftl!0}</span>
    </div>
</#macro>

<#macro leftMessageInfo>

    <div class="p_notice">
        <div class="p_notice_bgt">
            <span class="p_notice_laba"></span>
            <span class="p_notice_p">客服通知</span>
        </div>
        <div class="p_notice_con">
            <#if userType == 'STUDENT'>${pageBlockContentGenerator.getPageBlockContentHtml('RewardIndex', 'RewardIndexStudentPlacard')}</#if>
            <#if userType == 'TEACHER'>${pageBlockContentGenerator.getPageBlockContentHtml('RewardIndex', 'RewardIndexTeacherPlacard')}
                <#if (currentTeacherDetail.isPrimarySchool())!false>
                <br/><br/>教学用品中心实行阶梯包邮制度：如当月累计兑换实物奖品不足500园丁豆，需额外使用200园丁豆兑换包邮服务一次（下月发货时自动扣除，余额不足200园丁豆，则全部扣除）；如累计实物奖品超过500园丁豆，则自动包邮。
                <#elseif (currentTeacherDetail.isJuniorTeacher())!false>
                <br/><br/>教学用品中心实行阶梯包邮制度：如当月累计兑换实物奖品不足5000学豆，需额外使用2000学豆兑换包邮服务一次（下月发货时自动扣除，余额不足2000学豆，则全部扣除）；如累计实物奖品超过5000学豆，则自动包邮。
                </#if>
            </#if>
            <#if userType == 'RSTAFF'>${pageBlockContentGenerator.getPageBlockContentHtml('RewardIndex', 'RewardIndexRstaffPlacard')}</#if>
        </div>
        <div class="p_notice_bgb"></div>
    </div>
</#macro>