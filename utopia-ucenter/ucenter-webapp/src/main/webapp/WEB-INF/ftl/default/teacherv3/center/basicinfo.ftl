<div>
    <div class="t-center">
        <div class="m-person-info">
            <dl>
                <dt>
                <div class="avatar">
                    <#--<div class="back"></div>-->
                    <div class="image">
                        <span class="camera"></span>
                        <a id="teacherImg" href="javascript:void(0);"><img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>" width="138" height="138"></a>
                    </div>
                </div>
                <p class="w-ft-big" style="padding-top: 30px;">${(currentUser.profile.realname)!}</p>
                <p class="w-ft-well">学号：${(currentUser.id)!}</p>
                </dt>
                <dd>
                    <div class="assets">
                        <p class="gold w-fl-left" style="padding-bottom: 0;">
                            <#if ((currentTeacherDetail.isPrimarySchool())!false) || ((currentTeacherWebGrayFunction.isAvailable("MSIntegral", "Mall"))!false) >
                                <a href="/teacher/center/index.vpage#/teacher/center/mygold.vpage"><span class="count w-blue">${(currentTeacherDetail.userIntegral.usable)!}</span> <span class="w-show"><@ftlmacro.garyBeansText/></span></a>
                            <#else>
                                <span class="count w-blue"></span>
                            </#if>
                        </p>
                        <#if currentUser.fetchCertificationState() == "SUCCESS" >
                            <p class="gift w-fl-left">
                                <span class="w-icon-public w-icon-authVip"></span>
                                <span>认证</span>
                            </p>
                        <#else>
                            <p class="gift w-fl-left">
                                <a href="/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage"><span class="w-ft-large w-blue" style="line-height: 40px;">未认证</span></a>
                            </p>
                        </#if>
                    </div>
                </dd>
            </dl>
        </div>
    </div>
</div>

<div id="teacherinfo" class="w-base">
    <div class="w-base-title">
        <h3>我的资料</h3>
        <div class="w-base-right w-base-more">
            <a href="#/teacher/center/myprofile.vpage"><span class="w-icon-md">修改资料</span><span class="w-icon-arrow w-icon-arrow-lRight"></span></a>
        </div>
    </div>
    <div class="t-center">
        <ul>
            <li>所在学校：${(userShippingAddressMapper.schoolName)!}</li>
            <li class="tc-gray" style="float:left;width:550px;overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">学校地址：${(userShippingAddressMapper.provinceName)!} ,
            ${(userShippingAddressMapper.cityName)!}${(userShippingAddressMapper.countyName)!}
            ${(userShippingAddressMapper.detailAddress)!}
            </li>
            <li style="background-color: #f8f8f8;"><span style="display: inline-block; margin-left: 40px;">邮编：${(userShippingAddressMapper.postCode)!}</span></li>
            <li style="display: none;">配送方式：
                <#if userShippingAddressMapper.logisticType?? && userShippingAddressMapper.logisticType == "express">
                    普通快递（市、县可到达）
                </#if>
                <#if userShippingAddressMapper.logisticType?? && userShippingAddressMapper.logisticType == "ems">
                    邮局（乡、镇、村可到达）
                </#if>
            </li>
        </ul>
    </div>
</div>

<div id="securitycenter" class="w-base">
    <div class="w-base-title">
        <h3>账号安全</h3>
        <div class="w-base-right w-base-more">
            <a href="#/teacher/center/securitycenter.vpage"><span class="w-icon-md">修改设置</span><span class="w-icon-arrow w-icon-arrow-lRight"></span></a>
        </div>
    </div>
    <div class="t-center">
        <ul>
            <li>密 &nbsp码：**********</li>
            <li class="tc-gray">手机号：${(mobile)!}</li>
            <#if (currentTeacherDetail.isPrimarySchool())!false>
                <li>邮 &nbsp箱：${(email)!}</li>
            </#if>
        </ul>
    </div>
</div>

<iframe class="vox17zuoyeIframe" style="display: none;" src="/ucenter/avatar.vpage?avatar_cancel=parent.Avatar_Cancel&avatar_callback=parent.Avatar_callback" width="100%" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>
<script type="text/javascript">
    $(function(){
        LeftMenu.changeMenu();
        LeftMenu.focus("basicInfo");

        $("#teacherImg").on("click",function(){
            $("#teacherinfo").hide();
            $("#securitycenter").hide();
            $(".vox17zuoyeIframe").show();
        });
    });

    // 头像上传回调
    function Avatar_callback(data){
        data = eval("(" + data + ")");
        if ( $17.isBlank( data ) ) {
            setTimeout(function(){ window.location.reload(); }, 200);
        } else if ( data ){
            var dataInfo = "上传成功";
            if (!data.success){
                dataInfo = data.info;
            }
            $.prompt("<div style='text-align: center;'>" + dataInfo + "</div>", {
                title: "系统提示",
                buttons: { "知道了": true },
                close: function(){
                    window.location.reload();
                }
            });
        }
    }

    //关闭上传头像弹窗
    function Avatar_Cancel(){
        window.location.reload();
    }
</script>