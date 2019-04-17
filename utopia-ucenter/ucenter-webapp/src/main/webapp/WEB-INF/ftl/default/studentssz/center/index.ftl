<#import "module.ftl" as center>
<@center.studentCenter>
    <div class="t-center-box w-fl-right">
        <div class="t-center-title">个人中心</div>
        <div class="t-center-data">
            <div class="tc-person-info">
                <div class="tc-person v-studentVoxLogRecord" data-op="fixAvatar" id="studentImg" >
                    <a href="javascript:void(0);"><span><img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>" width="120" height="120"></span></a>
                    <#--<div class="ics">可在APP端修改头像</div>-->
                </div>

                <h3 class="title">${(currentUser.profile.realname)!}</h3>

                <div class="tc-font w-ag-center" style="position: relative;">
                    <span>学号：${(currentUser.id)!}</span>
                    <#--<span>学豆：${currentStudentDetail.userIntegral.usable}</span>-->
                </div>
            </div>

            <div id="studentInfo">
                <div class="tc-person-single">
                    <div class="baseinfo-box">
                        <p class="label">我的姓名</p>
                        <p class="info">${(currentUser.profile.realname)!''}</p>
                    </div>
                </div>
                <div class="tc-person-single">
                    <div class="baseinfo-box">
                        <p class="label">所在学校</p>
                        <p class="info">${(currentStudentDetail.studentSchoolName)!}</p>
                    </div>
                </div>
                <div class="tc-person-single" style="display: none;">
                    <div class="baseinfo-box">
                        <p class="label">所在班级</p>
                        <p class="info">${(currentStudentDetail.clazzLevel.description)!}${(currentStudentDetail.clazz.className)!}</p>
                    </div>
                </div>
                <div class="tc-person-single">
                    <div class="baseinfo-box">
                        <p class="label">考试填涂号</p>
                        <p class="info">${(scanNumber)!'无填涂号'}</p>
                        <div class="edit-box" onclick="$17.atongji('个人中心-基本信息-修改填涂号','/student/center/information.vpage?updateType=scannumber');">
                            <span>编辑</span>
                            <i></i>
                        </div>
                    </div>
                </div>
                <div class="tc-person-single" style="display: none;">
                    <div class="baseinfo-box">
                        <p class="label">出生日期</p>
                        <p class="info">${(currentUser.profile.year)!}年${(currentUser.profile.month)!}月${(currentUser.profile.day)!}日</p>
                    </div>
                </div>
                <div class="tc-person-single">
                    <div class="baseinfo-box">
                        <p class="label">手机号</p>
                        <p class="info">${mobile!'未绑定手机号'}</p>
                        <div class="edit-box" onclick="$17.atongji('个人中心-基本信息-修改手机号','/student/center/account.vpage?updateType=mobile');">
                            <span>编辑</span>
                            <i></i>
                        </div>
                    </div>
                </div>
                <div class="tc-person-single" style="display: none;">
                    <div class="baseinfo-box">
                        <p class="label">邮箱</p>
                        <p class="info">${email!'未绑定邮箱'}</p>
                    </div>
                </div>
                <div class="tc-person-single">
                    <div class="baseinfo-box">
                        <p class="label">修改密码</p>
                        <div class="edit-box" onclick="$17.atongji('个人中心-基本信息-修改密码','/student/center/account.vpage?updateType=password');">
                            <span>编辑</span>
                            <i></i>
                        </div>
                    </div>
                </div>
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

<#--神算子来源用户弹窗提示-->
<#include "ssztip.ftl" />

<script type="text/javascript">
    var fromShensz = '${(isShensz!false)?string}'; // 极算用户
    var needSupplementName = '${(isSupplementName!false)?string}'; // 需要修改姓名

    function checkNameError () {
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
                        if (!$17.isChinaString(inputName)) {
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
    }

    // 极算提示弹窗截止日期
    function frontofAlertDeadLine () {
        var deadLineTime = new Date('06/30/2018 23:59:59').getTime();
        var nowTime = new Date().getTime();
        if (nowTime < deadLineTime) return true;
        else return false;
    }

    $(function(){
        if (fromShensz === 'true') {
            if (!$17.getCookieWithDefault("is_show_ssz_student_tip") && frontofAlertDeadLine()) { // 未展示 且在截止时间之前
                $('#jisuan-template').show(); // 展示神算及弹窗
            } else {
                checkNameError();
            }
        } else {
            checkNameError();
        }

        var studentImg = $("#studentImg");
       /* studentImg.on("click",function(){
            $("#studentInfo").hide();
            $("iframe").show();
        });*/

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
