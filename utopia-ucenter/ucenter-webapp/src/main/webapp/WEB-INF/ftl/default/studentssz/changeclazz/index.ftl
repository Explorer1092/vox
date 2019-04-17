<#import "../layout/layout.ftl" as temp />

<@temp.page pageName='center' clazzName='t-center-bg'>
    <div class="t-center-container">
        <div class="t-changeclazz">
            <div class="changeclazz-title">更换班级</div>
            <div class="changeclaz-progress">
                <div class="progress-onebox active">
                    <span>1</span>输入老师号
                </div>
                <div class="progress-onebox">
                    <span>2</span>选择班级
                </div>
                <div class="progress-onebox">
                    <span>3</span>确认信息
                </div>
            </div>
            <div class="changeclazz-mainbox">
                <div class="step1-box step-box">
                    <div class="input-box validateTeacherId">
                        <label for="">老师号：</label>
                        <input id="changeclazz-teacherid" class="w-int require" data-label="老师号" type="text" placeholder="请输入老师号">
                        <span class="w-form-misInfo w-form-info-error"></span>
                    </div>
                    <div class="btn-box">
                        <div class="next-btn" id="step-one-next">下一步</div>
                    </div>
                </div>
                <div class="step2-box step-box">
                    <div class="clazz-title" id="school-teacher-info"></div>
                    <p class="choice-tip">选择所在年级和班级：</p>
                    <div class="clazz-content" id="changeclazz-content">
                        <#--<div class="clazz-box">七年级英语1班</div>-->
                    </div>
                    <div class="btn-box">
                        <div class="previous-btn" id="step-two-previous">上一步</div>
                        <div class="next-btn" id="step-two-next">下一步</div>
                    </div>
                </div>
                <div class="step3-box step-box validateSureInfoForm">
                    <div class="form-box">
                        <label for="">姓名：</label>
                        <p class="info">${(currentUser.profile.realname)!''}</p>
                    </div>
                    <div class="form-box" id="scanFormBox">
                        <label for="">考试填涂号：</label>
                        <input id="changeclazz-scannumber" class="w-int" data-label="考试填涂号" type="text" placeholder="请输入考试填涂号（选填）">
                        <span class="w-form-misInfo w-form-info-error"></span>
                    </div>
                    <div class="form-box">
                        <label for="">验证码：</label>
                        <input id="changeclazz-verifycode" class="code-input w-int require" data-label="验证码" type="text" placeholder="请输入验证码">
                        <div id="changeclazz-sendcode" class="send-code"><span>发送验证码</span></div>
                        <span class="w-form-misInfo w-form-info-error"></span>
                        <p class="send-tip" id="send-success" style="display: none">验证码已发送至手机：${mobile!''}</p>
                    </div>
                    <div class="btn-box">
                        <div class="previous-btn" id="step-three-previous">上一步</div>
                        <div class="next-btn" id="step-three-next">确定</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
<script>
    $(function () {
        var chocieTeacherId = null; // 选择的老师
        var choiceClazzId = null; // 选择的班级
        $('.step-box').eq(0).addClass('active'); // 默认展示第一个

        // 第一段 下一步
        $('#step-one-next').on('click', function () {
            var success = validate(".validateTeacherId");
            chocieTeacherId = $.trim($('#changeclazz-teacherid').val());
            if (success) {
                $.get('/student/center/getTeacherList.vpage', {
                    id: chocieTeacherId
                }, function (data) {
                    if (data.success) {
                        setStepTwoInfo(data);
                        moveStep(1);
                        // 老师所属学校未开通阅卷机权限的不展示填涂号
                        if (data.scanMachineFlag) {
                            $('#scanFormBox').show();
                        } else {
                            $('#scanFormBox').hide();
                        }
                    } else {
                        $17.alert(data.info);
                    }
                });
            }
        });
        // 第二段 上一步
        $('#step-two-previous').on('click', function () {
            moveStep(0);
        });
        // 第二段 下一步
        $('#step-two-next').on('click', function () {
            if (!choiceClazzId) {
                $17.alert('请先选择所在年级和班级');
                return;
            }
            moveStep(2);
        });
        // 第三段 上一步
        $('#step-three-previous').on('click', function () {
            moveStep(1);
        });
        // 第三段 下一步
        $('#step-three-next').on('click', function () {
            var success = validate(".validateSureInfoForm");
            var scanNumber = $.trim($('#changeclazz-scannumber').val());
            var captchaCode = $.trim($('#changeclazz-verifycode').val());
            if (success) {
                $.post('/student/center/changeclazz.vpage', {
                    classId: choiceClazzId,
                    teacherId: chocieTeacherId,
                    scanNumber: scanNumber,
                    captchaCode: captchaCode,
                    mobile: "${mobile!''}"
                }, function (data) {
                    if (data.success) {
                        $17.alert('班级更换成功！', function () {
                            setTimeout(function () {
                                location.href = '${(ProductConfig.getMainSiteBaseUrl())!''}' + '/redirector/apps/go.vpage?app_key=Shensz&return_url=/pc#!/my-class';
                            }, 200);
                        });
                    } else {
                        $17.alert(data.info);
                    }
                });
            }
        });
        // 选择班级
        $(document).on('click', '.changeclazz-choiceclazz', function () {
            $(this).addClass('active').siblings().removeClass('active');
            choiceClazzId = $(this).attr('data-clazzid');
        });
        // 发送验证码
        $('#changeclazz-sendcode').on('click', function () {
            var $this = $(this);
            $.post('/student/center/changeclazz/verification.vpage', {
            }, function (data) {
                if (data.success) {
                    $('#send-success').show();
                    getSMSVerifyCode($this, data);
                } else {
                    $17.alert(data.info);
                }
            });
        });
        // 设置第二段信息
        function setStepTwoInfo (data) {
            $('#school-teacher-info').text(data.schoolName + ' ' + data.teacherName + ' (' + data.teacherSubject + ')' );
            var clazzContentInfo = "";
            for (var i = 0, len = data.clazzList.length; i < len; i++) {
                clazzContentInfo += "<div class='clazz-box changeclazz-choiceclazz' data-clazzid=" + data.clazzList[i].clazzId + " data-clazztype=" + data.clazzList[i].creatorType + ">" + data.clazzList[i].clazzName + "</div>";
            }
            $('#changeclazz-content').html(clazzContentInfo);
        }
        // 前进或后退
        function moveStep (index) {
            $('.progress-onebox').removeClass('active').eq(index).addClass('active');
            $('.step-box').removeClass('active').eq(index).addClass('active');
        }
    });
</script>
</@temp.page>
