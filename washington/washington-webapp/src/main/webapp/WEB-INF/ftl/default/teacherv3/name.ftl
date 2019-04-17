<!-- 老师添加姓名。 -->
<script type="text/html" id="t:添加姓名">
    <div id="require_real_name">
        <div class="alert_vox ">
                <span class="text_small">
                   请填写您的真实姓名，以方便教研员找到您！真实姓名也可以确保您能够收到园丁豆所兑换的奖品！
                </span>
        </div>
        <div class="spacing_vox text_center">
            <label for="form_real_name"><strong>请填写你的真实姓名：</strong></label>
            <input id="form_real_name" name="teacherName" type="text" maxlength="10" style="width: 170px;" class="int_vox" value=""/>
        </div>
    </div>
</script>

<script type="text/javascript">
    $(function(){
        var states = {
            state0: {
                title       : "添加姓名",
                html        : template("t:添加姓名", {}),
                position    : { width: 520, height: 300 , y: 160},
                buttons     : { "提交": true},
                focus       : 0,
                submit:function(e,v,m,f){
                    if(v){
                        var _username = $("#form_real_name").val().replace(/\s+/g, "");

                        if($17.isBlank(_username) || !$17.isCnString(_username)){
                            $.prompt.goToState('stateError');
                            return false;
                        }

                        $.post("/ucenter/resetmyname.vpage", { name: _username }, function(data){
                            if(data.success){
                                $.prompt('姓名填写成功.',{
                                    title   : "添加姓名",
                                    buttons : { "知道了": true },
                                    submit  : function(e,v){
                                        e.preventDefault();
                                        setTimeout(function(){ location.href = "/teacher/index.vpage"; }, 200);
                                    }
                                });
                            }else{
                                $.prompt.goToState('stateFail');
                                return false;
                            }
                        });
                    }
                }
            },
            stateError:{
                title: "添加姓名",
                html : "<div class='text_center'>真实姓名格式不正确，请输入中文.</div>",
                buttons: { "确定" : true },
                focus : 0,
                submit:function(e,v,m,f){
                    if(v){
                        e.preventDefault();
                        $.prompt.goToState('state0');
                    }
                }
            },
            stateFail : {
                title   : "添加姓名",
                html    : '<h4 class="text_gray_6">姓名填写失败.</h4>',
                focus   : 0,
                buttons : { "知道了": true }
            }
        };

        //判断设置姓名每天弹一次
        if($17.getCookieWithDefault("udy${(currentUser.id)!}") != $17.DateUtils("%d")){
            $17.setCookieOneDay("udy${(currentUser.id)!}", $17.DateUtils("%d"), 60);
            $.prompt(states,{
                loaded : function(){
                    $("#form_real_name").focus();
                    $17.backToTop();
                }
            });
        }
    });
</script>