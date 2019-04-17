<style type="text/css">
    .alert_vox {
        background-color: #fcf8e3;
        border: 1px solid #fbeed5;
        border-radius: 4px;
        margin-bottom: 20px;
        padding: 8px 35px 8px 14px;
        text-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
    }
</style>
<script type="text/html" id="t:20150401忘记密码">
    <div style="height: 290px;">
        <div class="alert_vox text_center">
            <p id="password_info_box" style="line-height: 30px; text-align: center;">
                <b style="color: red;">安全信息提示</b> <br/>
                你很久没输入过账号与密码了，请输入账号与密码
            </p>
        </div>

        <div class="w-form-table" style="padding: 0px !important;">
            <dl>
                <dt style="width: 150px;">账号 ：</dt>
                <dd>
                    <input id="studentToken" maxlength="30" type="text" class="w-int" placeholder="你的17作业账号" value="">
                    <span class="msgInfo" style="color: red; font-size: 12px;"></span>
                </dd>
                <dt style="width: 150px;">密码 ：</dt>
                <dd>
                    <input id="studentPassword" maxlength="30" type="password" placeholder="密码" class="w-int" value="">
                    <span class="msgInfo" style="color: red; font-size: 12px;"></span>
                </dd>
                <dd style="margin-left: 38%;">
                    <a id="submit_but" class="w-btn-dic w-btn-green-new" href="javascript:void(0);">确定</a>
                </dd>
            </dl>
        </div>
    </div>
</script>
<script id="t:20150401忘记密码错误" type="text/html">
    <div style="text-align: center;">
        <p class="spacing_vox" style="line-height: 25px;">
            <span style="font-size: 16px"><b>你忘记了吗？赶快去下载你的账号与密码吧</b></span> <br/>
            <span class="spacing_vox" style="font-size:12px">点击“立即下载”后，会下载到你的电脑上，注意查看</span>
        </p>

        <p class="spacing_vox">
            <a id="download_but" href="/clazz/fetchaccount.vpage?fromPopup=<%= from %>" target="_blank"
               class="w-btn-dic w-btn-green-new">立即下载</a>
        </p>

        <p class="spacing_vox ">
            <span class="text_gray_9">要记住你的账号与密码哦～</span>
        </p>
    </div>
</script>
<script type="text/javascript">
    var RmPassword = null;

    $(function(){
        RmPassword = new $17.Model({
            autoRun    : true,
            from       : "测试",
            _errCounter: 0,
            states     : {
                state     : {
                    title   : "提示",
                    html    : template("t:20150401忘记密码", {}),
                    position: { width: 500, height: 720 },
                    buttons : {}
                },
                stateError: {
                    title  : "提示",
                    html   : template("t:20150401忘记密码错误", { from : this.from }),
                    buttons: {}
                }
            }
        });
        RmPassword.extend({
            show: function(){
                var $rmp = this;

                $.prompt($rmp.states, {
                    loaded: function(){
                        $17.tongji('学生-密码强化-弹窗-' + $rmp.from);

                        $("#studentToken").focus();
                    },
                    zIndex: 3001
                });
            },
            init: function(option){
                var $rmp = this;

                if(typeof option === "string"){
                    $rmp.from = option;
                    $rmp.show();
                }

                if(typeof option === "object"){
                    $.extend($rmp, options);

                    if($rmp.autoRun){
                        $rmp.show();
                    }
                }

                //密码提交
                $("#submit_but").on("click", function(){
                    var studentToken = $("#studentToken");
                    var studentPassword = $("#studentPassword");

                    if($17.isBlank(studentToken.val())){
                        studentToken.siblings('span.msgInfo').text('账号不能为空');
                        studentToken.focus();
                        return false;
                    }else{
                        studentToken.siblings('span.msgInfo').text('');
                    }

                    if($17.isBlank(studentPassword.val())){
                        studentPassword.siblings('span.msgInfo').text('密码不能为空');
                        studentPassword.focus();
                        return false;
                    }else{
                        studentPassword.siblings('span.msgInfo').text('');
                    }

                    $.post("/student/verifystudentpwd.vpage", {
                        token   : studentToken.val(),
                        password: studentPassword.val()
                    }, function(data){
                        if(data.success){
                            $.prompt.close();
                            $17.tongji('学生-密码强化-设置成功-' + $rmp.from + "-" + $rmp._errCounter);
                        }else{
                            $rmp._errCounter++;

                            //重复输入账号与密码2次后 进入到下载账号密码页面
                            if($rmp._errCounter == 2){
                                $17.tongji('学生-密码强化-下载密码页-' + $rmp.from);
                                $.prompt.goToState('stateError');

                                return false;
                            }
                            $('#password_info_box').addClass('text_red').html('输入错误，请重新输入');

                            studentToken.val("");
                            studentPassword.val("")
                        }
                    });
                });

                $("#download_but").on('click', function(){
                    $17.tongji('学生-密码强化-下载账号密码-' + $rmp.from);
                    $.prompt.close();
                });
            }
        });
    });
</script>