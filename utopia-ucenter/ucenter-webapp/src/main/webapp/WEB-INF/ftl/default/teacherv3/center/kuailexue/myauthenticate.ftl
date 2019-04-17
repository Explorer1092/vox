<div class="class-module mt-20">
    <div class="module-head bg-f6 clearfix">
        <div class="title">我的认证</div>
    </div>
    <div class="accountSecurity-column">
        <div class="acs-right acs-list">
            <div class="acs-image"><img src="<@app.link href="public/skin/teacherv3/images/personal/image06.png"/>"></div>
            <div class="acs-title">
                <p class="t-1">认证老师特权：</p>
                <p class="t-2"><span>1.可使用校本题库功能</span><span>2.每日下载试卷限制放宽至100套</span></p>
            </div>
        </div>
        <div class="acs-left">认证状态：
            <#if (currentUser.fetchCertificationState() == "SUCCESS")!false>
                <span class="icon-current"></span>
                <span class="green_fontBtn">已认证</span>
            <#else>
                <span class="icon-noCer"></span>
                <span class="fontPink">未认证</span>
            </#if>
        </div>
    </div>
</div>
<div class="class-module mt-20">
    <div class="module-head bg-f6 clearfix">
        <div class="title">认证条件（满足以下任意一条即可获得认证）</div>
    </div>
    <div class="accountSecurity-box spaceBorder">
        <div class="acs-list">
            <div class="acs-image"><img src="<@app.link href="public/skin/teacherv3/images/personal/image03.png"/>"></div>
            <div class="acs-title">
                <p class="t-1">客服同学认证</p>
                <p class="t-2">请联系17作业客服，客服会审核您的身份
                    <#if (currentUser.fetchCertificationState() != "SUCCESS")!true>
                        <a target="_blank" href="http://onlinecs.17zuoye.com/WebCall4/index.action?param=a0f489f580dae71b094a1ee0f74a622def8c6a842fe1c54064a8ce28ab7e6860a60f8ebb5731f88430d516d7ff902ff96321293889be8f9e00b4cc4fb560a6551ef6bfca4b458b4bdedd7f7ed3a3f7e46e8de59cfaecfd54aebf6649992ad8cddb673b49b18e2ea8c3953b141ee49e6a&avatar=http://cdn-portrait.17zuoye.cn/upload/images/avatar/avatar_normal.gif" class="green_fontBtn" style="font-size: 12px">「点这里」</a>
                    </#if>
                </p>
            </div>
        </div>
        <div class="acs-list">
            <div class="acs-image"><img src="<@app.link href="public/skin/teacherv3/images/personal/image04.png"/>"></div>
            <div class="acs-title">
                <p class="t-1">校本题库管理员认证</p>
                <p class="t-2">请联系您所在学校的校本题库管理员，一键发送申请
                    <#if (currentUser.fetchCertificationState() != "SUCCESS")!true>
                        「<a id="v-sendApplication" href="javascript:;" class="green_fontBtn" style="font-size: 12px">点这里」</a>
                    </#if>
                </p>
            </div>
        </div>
        <div class="acs-list">
            <div class="acs-image"><img src="<@app.link href="public/skin/teacherv3/images/personal/image05.png"/>"></div>
            <div class="acs-title">
                <p class="t-1">扫描试卷</p>
                <p class="t-2">一份试卷被超过20个学生作答</p>
            </div>
        </div>
    </div>
</div>

<script id="t:申请提交成功" type="text/html">
    <div class="w-base">
        <div class="t-security">
            <ul>
                <li>
                    <div class="ts-note" style="width: 95%; height: 88px;">
                        <h3><i class="w-icon-public w-icon-success"></i>您的认证申请已成功提交！</h3>
                        <p style="border-top:solid 1px #ccc;font:14px/24px arial; color:#39f;">
                            请拨打客服电话<b> <@ftlmacro.hotline phoneType="teacher"/></b> <br>
                            一起作业的工作人员会跟您核实信息处理您的申请并反馈结果
                        </p>
                    </div>

                </li>
            </ul>
            <div class="w-clear"></div>
        </div>
    </div>
</script>
<script type="text/javascript">
    $(function(){
        LeftMenu.changeMenu();
        LeftMenu.focus("authentication");

        $("#v-sendApplication").on("click", function () {
            $.post('/teacher/center/sendApplication2Admin.vpage', function (data) {
                if (data.success) {
                    $17.alert(data.info);
                } else {
                    $17.alert("你的学校暂时没有校本题库管理员，请尝试其他认证方式");
                }
            })
        });

        //申请认证
        $("#applyAuth").on("click",function(){
            if($(this).isFreezing()){
                return false;
            }
            $(this).freezing();
            $.get('/teacher/center/authenticatechip.vpage',function(data){
                $(this).thaw();
                $.prompt(data, {
                    title: "确认信息",
                    position: { width: 700 },
                    buttons: {"修改信息" : false,"确定" : true},
                    submit : function(e,v,m,f){
                        e.preventDefault();
                        if(v){
                            $.get("/teacher/center/authenticatesubmit.vpage", function(data){
                                if(!data.success){
                                    alert(data.info);
                                    return false;
                                }else{
                                    $.prompt(template("t:申请提交成功", {}), {
                                        title: "系统提示",
                                        buttons: { "知道了": true },
                                        position:{width : 500},
                                        submit : function(e,v,m,f){
                                            setTimeout(function(){
                                                location.reload();
                                            },200);
                                        }
                                    });
                                    return false;
                                }
                            });
                        }else{
                            location.href = "#/teacher/center/myprofile.vpage";
                        }
                    }
                });
            });

        });

    });
</script>