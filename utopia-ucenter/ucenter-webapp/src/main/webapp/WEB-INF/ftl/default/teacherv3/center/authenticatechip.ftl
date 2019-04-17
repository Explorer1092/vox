<div id="user_info_box" class="w-base">
    <#if userShippingAddressMapper?exists>
        <div class="w-base-title">
            <p style=" color:#f93; text-align: center; padding:10px;"><b>请您确认以下信息是否准确无误，如有问题，请返回个人中心修改</b></p>
        </div>
        <div class="t-security">
            <div class="myData">
                <div class="w-form-table">
                    <dl>
                        <dt>姓名: </dt>
                        <dd>${currentUser.profile.realname!}</dd>
                    </dl>
                    <dl>
                        <dt>所在地区：</dt>
                        <dd>${userShippingAddressMapper.provinceName!} ${userShippingAddressMapper.cityName!} ${userShippingAddressMapper.countyName!}</dd>
                    </dl>
                    <dl>
                        <dt>街道地址：</dt>
                        <dd>${userShippingAddressMapper.detailAddress!}</dd>
                    </dl>
                    <dl>
                        <dt>邮政编码：</dt>
                        <dd>${(userShippingAddressMapper.postCode)!"无"}</dd>
                    </dl>
                    <dl>
                        <dt> 联系电话：</dt>
                        <dd>${userShippingAddressMapper.sensitivePhone!}</dd>
                    </dl>
                </div>
            </div>
        </div>
    <#else>
        暂无内容
    </#if>
</div>
