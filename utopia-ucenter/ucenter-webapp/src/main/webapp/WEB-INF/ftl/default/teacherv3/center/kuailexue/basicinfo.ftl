<div class="personalData-head">
    <#--<#if currentUser.fetchCertificationState() == "SUCCESS" >-->
        <#--<a href="#/teacher/center/myauthenticate.vpage" class="green_fontBtn fr">已认证</a>-->
    <#--<#else>-->
        <#--<a href="#/teacher/center/myauthenticate.vpage" class="green_fontBtn fr">未认证</a>-->
    <#--</#if>-->
    <div <#if !(isShensz!false)>id="teacherImg"</#if> class="ped-image">
        <img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>"><!--此处放头像-->
        <#if !(isShensz!false)><span class="icon-camera"></span></#if>
    </div>
    <div class="ped-name">${(currentUser.profile.realname)!}</div>
    <div class="ped-name">老师号:${(currentUser.id)!}</div>
</div>

<div id="teacherinfo" class="class-module mt-20">
    <div class="module-head bg-f6 clearfix">
        <div class="title">我的资料</div>
        <a href="#/teacher/center/myprofile.vpage" class="green_fontBtn fr">修改资料 &gt;</a>
    </div>
    <div class="personalData-box">
        <ul>
            <li class="bg-white">所在学校：${(userShippingAddressMapper.schoolName)!}</li>
            <li>学校地址：${(userShippingAddressMapper.provinceName)!} ,
            ${(userShippingAddressMapper.cityName)!}${(userShippingAddressMapper.countyName)!}
            ${(userShippingAddressMapper.detailAddress)!}</li>
        </ul>
    </div>
</div>

<div id="securitycenter" class="class-module mt-20">
    <div class="module-head bg-f6 clearfix">
        <div class="title">账号安全</div>
        <a href="#/teacher/center/securitycenter.vpage" class="green_fontBtn fr">修改设置 &gt;</a>
    </div>
    <div class="personalData-box">
        <ul>
            <li class="bg-white">密码：********</li>
            <li>手机号：${(mobile)!}</li>
        </ul>
    </div>
</div>

<iframe class="vox17zuoyeIframe" style="display: none;" src="/ucenter/avatar.vpage?avatar_cancel=parent.Avatar_Cancel&avatar_callback=parent.Avatar_callback" width="100%" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>
<script type="text/javascript">
    LeftMenu.changeMenu();
    LeftMenu.focus("basicInfo");

    $(function(){
        $("#teacherImg").on("click",function(){
            $("#teacherinfo").hide();
            $("#securitycenter").hide();
            $("iframe").show();
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