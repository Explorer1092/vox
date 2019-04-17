<#-- @ftlvariable name="mobile" type="java.lang.String" -->
<#-- @ftlvariable name="userShippingAddressMapper" type="com.voxlearning.utopia.entity.user.UserShippingAddress" -->
<#-- @ftlvariable name="currentResearchStaffDetail" type="com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaffDetail" -->
<#-- @ftlvariable name="currentUser" type="com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff" -->
<#import "../researchstaffv3.ftl" as com>
<@com.page menuType="normal">
<ul class="breadcrumb_vox">
    <li><a href="/rstaff/center/index.vpage">个人信息</a> <span class="divider">/</span></li>
    <li class="active">查看</li>
</ul>
<div class="testpaperBox">

    <div class="sAvatar row_vox_left text_center" style="width: 120px; padding-top: 30px;">
        <div class="sar spacing_vox_tb row_vox_right">
            <span class="picture" style="border: 1px solid #ddd; display: inline-block; width: 90px;height: 90px;">
                <img id="user_avatar" class="avatar_img" src="<@app.avatar href='${(currentUser.fetchImageUrl())!}'/>" alt="" width="90" height="90">
            </span>
        </div>
    </div>

    <div class="row_vox_left">
        <dl class="horizontal_vox">
            <dt></dt>
        </dl>

        <dl class="horizontal_vox">
            <dt class="text_big">基本信息：</dt>
            <dd>&nbsp;</dd>
            <dt>姓名：</dt>
            <dd>${(currentUser.profile.realname)!"&nbsp;"}</dd>
            <dt>登录号：</dt>
            <dd>${currentUser.id!"&nbsp;"}</dd>
            <dt>地区：</dt>
            <dd>${(currentUser.region.toString())!"&nbsp;"}</dd>
            <#if currentUser.subject == "ENGLISH">
                <dt>园丁豆：</dt>
                <dd>
                    <strong class="text_red">${(currentResearchStaffDetail.userIntegral.usable)!"&nbsp;"}</strong>
                    <#--<em class='icon_rstaff icon_rstaff_8'></em>-->
                </dd>
            <#else>
                <dt>学科：</dt>
                <dd>${(currentUser.subject.value)!}</dd>
            </#if>

        </dl>

        <#if (currentUser.isResearchStaffForCounty() || currentUser.isResearchStaffForCity()) && currentUser.subject != "MATH">
            <dl class="horizontal_vox">
                <dt class="text_big">收货信息：</dt>
                <dd>&nbsp;</dd>
                <dt class="text_normal">所在地区：</dt>
                <dd>${(userShippingAddressMapper.provinceName)!"&nbsp;"} ${(userShippingAddressMapper.cityName)!"&nbsp;"} ${(userShippingAddressMapper.countyName)!"&nbsp;"}</dd>
                <dt class="text_normal">详细地址：</dt>
                <dd>${(userShippingAddressMapper.detailAddress)!"&nbsp;"}</dd>
                <dt class="text_normal">邮政编码：</dt>
                <dd>${(userShippingAddressMapper.postCode)!"&nbsp;"}</dd>
                <dt class="text_normal">联系电话：</dt>
                <dd>${(mobile)!"&nbsp;"}</dd>
                <dt class="text_normal" style="display: none;">配送方式：</dt>
                <dd style="display: none;">${(userShippingAddressMapper.logisticTypeName)!"&nbsp;"}</dd>
            </dl>
        </#if>
    </div>
    <div class="spacing_vox text_right">
        <a class="btn_vox btn_vox_primary" href="/rstaff/center/edit.vpage">
            <strong><span>修改个人信息</span></strong>
        </a>
        <a class="btn_vox btn_vox_primary" href="/rstaff/center/editPassword.vpage">
            <strong><span>修改密码</span></strong>
        </a>

    </div>
    <div class="clear"></div>
</div>
</@com.page>