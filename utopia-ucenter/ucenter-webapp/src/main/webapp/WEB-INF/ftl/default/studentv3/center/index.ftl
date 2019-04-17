<#import "module.ftl" as center>
<@center.studentCenter>
    <div class="t-center-box w-fl-right">
        <span class="center-rope"></span>
        <span class="center-rope center-rope-1"></span>
        <div class="t-center-data">
            <div class="tc-person v-studentVoxLogRecord" data-op="fixAvatar" id="studentImg" >
                <a href="javascript:void(0);"><span><img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>" width="120" height="120"></span></a>
                <div class="ics">可在APP端修改头像</div>
            </div>
            <h3 class="title">${(currentUser.profile.realname)!}</h3>
            <div class="tc-font w-ag-center" style="position: relative;">
                <span>学号：${(currentUser.id)!}</span>
                <span>学豆：${currentStudentDetail.userIntegral.usable}</span>
            </div>
            <div id="studentInfo" class="w-form-table">
                <h2><a class="w-change-btn w-fl-right" onclick="$17.atongji('个人中心-基本信息-修改我的资料','/student/center/information.vpage');" href="javascript:void (0);" title="修改">修改</a>我的资料</h2>
                <dl>
                    <dt>姓　　名：</dt>
                    <dd>${(currentUser.profile.realname)!''}&nbsp;</dd>
                    <dt>性　　别：</dt>
                    <dd><#if (currentUser.profile.gender) == 'M'>男<#elseif (currentUser.profile.gender) =='F'>女</#if>&nbsp;
                    </dd>
                    <dt>出生日期：</dt>
                    <dd>${(currentUser.profile.year)!}年${(currentUser.profile.month)!}月${(currentUser.profile.day)!}日&nbsp;</dd>
                    <dt>学　　校：</dt>
                    <dd>${(currentStudentDetail.studentSchoolName)!}&nbsp;</dd>
                    <dt>班　　级：</dt>
                    <dd>${(currentStudentDetail.clazzLevel.description)!}${(currentStudentDetail.clazz.className)!}&nbsp;</dd>
                </dl>
                <h2>账号安全</h2>
                <dl>
                    <dt>密　　码：</dt>
                    <dd><a class="w-change-btn w-fl-right" onclick="$17.atongji('个人中心-基本信息-修改密码','/student/center/account.vpage?updateType=password');" href="javascript:void (0);">修改</a>* * * * *</dd>
                    <dt>手 机 号：</dt>
                    <dd>
                        <a class="w-change-btn w-fl-right" onclick="$17.atongji('个人中心-基本信息-修改手机号','/student/center/account.vpage?updateType=mobile');" href="javascript:void (0);"> <#if mobile?has_content>修改<#else>绑定</#if></a>
                        ${mobile!'未绑定手机号'}&nbsp;
                    </dd>
                    <dt>邮　　箱：</dt>
                    <dd>
                        <a class="w-change-btn w-fl-right" onclick="$17.atongji('个人中心-基本信息-修改邮箱','/student/center/account.vpage?updateType=email');" href="javascript:void (0);"><#if email?has_content>修改<#else>绑定</#if></a>
                        ${email!'未绑定邮箱'}&nbsp;
                    </dd>
                </dl>
            </div>
        </div>
        <iframe style="display: none;" src="/ucenter/avatar.vpage?avatar_cancel=parent.Avatar_Cancel&avatar_callback=parent.Avatar_callback" width="100%" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>
    </div>

<script id="T:修改异常用户姓名" type="text/html">
    <div class="modifyname-box">
        <p class="tips">系统检测您的姓名存在异常，仅支持2-10位中文名字</p>
        <div class="input-box">
            <span>请修改姓名：</span>
            <input type="text" placeholder="请输入中文姓名" maxlength="10" value="${(currentUser.profile.realname)!}" id="modifyedName">
        </div>
        <p class="tips error" id="errorName"></p>
    </div>
</script>

<script type="text/javascript">
    $(function(){
        var needSupplementName = '${(isSupplementName!false)?string}'; // 需要修改姓名
        if (needSupplementName === 'true') {
            $.prompt(template('T:修改异常用户姓名', {}), {
                focus: 0,
                title: "系统提示",
                buttons: {"确定": true},
                loaded: function () {
                    $('.jqiclose').hide(); // 隐藏关闭按钮
                },
                submit: function (e, v) {
                    if (v) {
                        e.preventDefault();
                        var inputName = $.trim($('#modifyedName').val());
                        if (!/^[\u2E80-\uFE4F]+([·•][\u2E80-\uFE4F]+)*$/.test(inputName)) {
                            $('#errorName').text('您输入的姓名不符合规范，请输入2-10位中文名字');
                            return false;
                        }
                        App.postJSON('/ucenter/changName.vpage', {
                            userName: inputName
                        }, function(res){
                            if(res.success){
                                $.prompt.close();
                                setTimeout(function () {
                                    $17.alert(res.info, function(){
                                        window.location.href = "/index.vpage";
                                    });
                                }, 10);
                            }else{
                                $('#errorName').text(res.info);
                            }
                        });
                    }
                }
            })
        }

        var studentImg = $("#studentImg"),
            is_staging = window.location.href.indexOf("staging.17zuoye.net") > -1;

        if (is_staging){
            studentImg.find(".ics").text("点击修改头像");
        }

        studentImg.on("click",function(){
            if (is_staging){
                $("#studentInfo").hide();
                $("iframe").show();
            }
        });

        studentImg.hover(function(){
            $(this).find(".ics").show()
        }, function(){
            $(this).find(".ics").hide()
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
</@center.studentCenter>
