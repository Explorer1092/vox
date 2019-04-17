<@sugar.capsule js=["alert", "template"] css=["plugin.alert", "new_teacher.widget"] />
<script type="text/javascript">
    $(function(){
        function areaTo(link, target, val){
            target.html("<option value='"+ val +"'>"+ val +"</option>");
            $.getJSON(link, function(data){
                if(val == "请选择学校"){
                    $.each(data.rows, function(){
                        target.append("<option data-title='" + this.state + "' value='" + this.id + "'>" + this.cname + "</option>");
                    });
                }else{
                    $.each(data, function(){
                        target.append("<option data-title='" + this.state + "' value='" + this.id + "'>" + this.text + "</option>");
                    });
                }
            });
        }

        function verificationForm(items, infoId){
            var isSuccess = false;
            items.each(function(index){

                if(items.eq(index).attr("data-type") == "name" && !$17.isCnString(items.eq(index).val())){
                    infoId.html(items.eq(index).attr("data-message"));
                    isSuccess = true;
                    return false;
                }

                if(items.eq(index).attr("data-type") == "userId" && !$17.isBlank(items.eq(index).val()) && !$17.isNumber(items.eq(index).val()) ){
                    infoId.html(items.eq(index).attr("data-message"));
                    isSuccess = true;
                    return false;
                }

                if(items.eq(index).attr("data-type") == "mobile" && !$17.isMobile(items.eq(index).val())){
                    infoId.html(items.eq(index).attr("data-message"));
                    isSuccess = true;
                    return false;
                }

                if(items.eq(index).hasClass("ty_3") && (items.eq(index).val() == "请选择学校" || $17.isBlank(items.eq(index).val()))){
                    infoId.html(items.eq(index).attr("data-message"));
                    isSuccess = true;
                    return false;
                }
            });

            return isSuccess;
        }

        //找回账号密码提示框
        $(document).on("click", ".js-clickServerPopup", function(){
            var $userType = $(this).attr("data-usertype");
            var $origin = $(this).attr("data-origin");

            var _tTime = new Date().getHours();
            if(_tTime < 8 || _tTime >= 21){
                window.open('${(ProductConfig.getMainSiteBaseUrl())!''}/redirect/qiyukf/index.vpage?type=student&questionType=question_account_ps&origin=' + $origin, '','width=856,height=519');
                return false;
            }

            var statesHtml = {
                state0 : {
                    title : "客服重置密码",
                    html : template("T:serverInfo", {userType : $userType ? $userType : ""}),
                    buttons: {"提交" : true},
                    submit : function(e, v){
                        if(v){
                            var success = verificationForm($("#serverForm [data-type]"), $("#serverDataInfo"));

                            if(success){
                                $.prompt.goToState('state1', true);
                                return false;
                            }

                            var $serverForm = $("#serverForm");
                            var $serverInfo = "";
                            $serverInfo += "姓名：" + $serverForm.find("[data-type='name']").val() + '|';
                            if($serverForm.find("[data-type='userId']").val() != ""){
                                $serverInfo += "账号：" + $serverForm.find("[data-type='userId']").val() + '|';
                            }
                            $serverInfo += "手机号：" + $serverForm.find("[data-type='mobile']").val() + '|';
                            $serverInfo += "学校：" + $serverForm.find("[data-type='school']").val();

                            if(!$17.isBlank($serverForm.find("[data-type='className']").val())){
                                $serverInfo += "|班级：" + $serverForm.find("[data-type='className']").val();
                            }

                            window.open('${(ProductConfig.getMainSiteBaseUrl())!''}/redirect/qiyukf/index.vpage?type=student&questionType=question_account_ps&serverInfo='+ encodeURIComponent($serverInfo) + '&origin=' + $origin, '','width=856,height=519');
                        }
                    }
                },state1: {
                    html:'<div id="serverDataInfo"></div>',
                    buttons: {"知道了": 0 },
                    submit:function(e,v,m,f){
                        e.preventDefault();
                        $.prompt.goToState('state0');
                    }
                }
            };

            $.prompt(statesHtml);
        });


        //其它提示框
        $(document).on("click",".js-clickServerPopupOne", function(){
            var url = window.location.href,
                terminal = '';

            if (url.indexOf('junior-teacher') > -1) {
                terminal = 'pc.17juniortea';
            } else if (url.indexOf('student') > -1) {
                terminal = 'pc.17student';
            } else if (url.indexOf('teacher') > -1) {
                terminal = 'pc.17teacher';
            }
            openServerPopup($(this),terminal);

        });
        /*$(document).on("click",".js-clickLeftTypeBtn2", function(){
            openServerPopup();
        });*/

        //在线交谈 提示框
        function openServerPopup(_this, terminal) {
            var $userType = _this.attr("data-usertype");
            var $questionType = _this.attr("data-questiontype");
            var $origin = _this.attr("data-origin");

            var _tTime = new Date().getHours();
            if(_tTime < 8 || _tTime >= 21){
                window.open('${(ProductConfig.getMainSiteBaseUrl())!''}/redirect/qiyukf/index.vpage?type=' + (!$17.isBlank($questionType) ? ('&questionType=' + $questionType) : '') + terminal + '&origin=' + $origin, '','width=856,height=519');
                return false;
            }
            var statesHtml = {
                state0 : {
                    title : "在线客服",
                    html : template("T:在线客服反馈", {}),
                    buttons: {"提交" : true},
                    submit : function(e, v){
                        if(v){
                            var $serverForm = $("#serverFormFeedback");
                            var $serverDataInfo = $("#serverDataInfoOne");
                            // var $serverInfo = "";
                            // var $userId = $serverForm.find("[data-type='userId']");
                            // var $describe = $serverForm.find("[data-type='describe']");

                            /*if( !$17.isNumber($userId.val()) ){
                                $serverDataInfo.html($userId.attr("data-message"));
                                $.prompt.goToState('state1', true);
                                return false;
                            }

                            if( $17.isBlank($describe.val()) || ($describe.val().length < 5 || $describe.val().length > 100)){
                                if($describe.val().length > 100){
                                    $serverDataInfo.html("提交的问题需少于100个字");
                                }else{
                                    $serverDataInfo.html($describe.attr("data-message"));
                                }
                                $.prompt.goToState('state1', true);
                                return false;
                            }*/

                            <#--$serverInfo += "账号：" + '${(currentUser.id)!}' + '|';-->
                            // $serverInfo += "描述问题：" + $describe.val();

                            window.open('${(ProductConfig.getMainSiteBaseUrl())!''}/redirect/qiyukf/index.vpage?type='+ $userType + (!$17.isBlank($questionType) ? ('&questionType=' + $questionType) : '') + '&origin=' + $origin, '','width=856,height=519');
                        }
                    }
                },state1: {
                    html:'<div id="serverDataInfoOne"></div>',
                    buttons: {"知道了": 0 },
                    submit:function(e,v,m,f){
                        e.preventDefault();
                        $.prompt.goToState('state0');
                    }
                }
            };
            $.prompt(statesHtml);

        };

        //所在省
        $(document).on("change", "#areaBox select", function(){
            var $thatVal = $(this).val();
            var $areaBox = $("#areaBox");

            switch ($(this).attr("class")){
                case "w-int ty_0" :
                    $areaBox.find(".ty_1").html("<option value='市'>市</option>");
                    $areaBox.find(".ty_2").html("<option value='区'>区</option>");
                    $areaBox.find(".ty_3").html("<option>请选择学校</option>");
                    if($thatVal != "所在省"){
                        if($thatVal=="110000" || $thatVal == "120000" || $thatVal == "310000" || $thatVal=="500000"){
                            $areaBox.find(".ty_1").hide();
                            areaTo("/map/nodes.vpage?id=" + $thatVal, $areaBox.find(".ty_2"), "区");
                        }else{
                            $areaBox.find(".ty_1").show();
                            areaTo("/map/nodes.vpage?id=" + $thatVal, $areaBox.find(".ty_1"), "市");
                        }
                    }
                    break;
                case "w-int ty_1" :
                    $areaBox.find(".ty_2").html("<option value='区'>区</option>");
                    $areaBox.find(".ty_3").html("<option>请选择学校</option>");
                    if($thatVal != "市"){
                        areaTo("/map/nodes.vpage?id=" + $thatVal, $areaBox.find(".ty_2"), "区");
                    }
                    break;
                case "w-int ty_2" :
                    $areaBox.find(".ty_3").html("<option>请选择学校</option>");
                    if($thatVal != "区"){
                        areaTo("/school/areaschool.vpage?area=" + $thatVal, $areaBox.find(".ty_3"), "请选择学校");
                    }
                    break;
                case "w-int ty_3" :
                    $areaBox.parent().removeClass("err");
                    break;
            }
        });
    });
</script>
<script type="text/html" id="T:serverInfo">
    <div class="w-form-table" id="serverForm" style="margin: -30px 0; text-align: left;">
        <div style="margin: 0 50px; padding-bottom: 20px;">为了保障您的账号安全，需要您提供准确的信息，由在线客服帮您找回密码</div>
        <dl>
            <dt>姓名：</dt><dd><input data-type="name" type="text" value="" class="w-int" data-message="请输入正确的姓名" maxlength="6" placeholder="姓名"/></dd>
            <dt>账号：</dt><dd><input data-type="userId" type="text" value="${(currentUser.id)!}" class="w-int" data-message="请输入正确的账号" maxlength="12" placeholder="一起小学登录账号"/></dd>
            <dt>手机号：</dt><dd><input data-type="mobile" type="text" value="" class="w-int" data-message="请输入正确的手机号" maxlength="11" placeholder="重置后的密码将发送到此手机"/></dd>
            <dt>我的学校：</dt>
            <dd>
                <span id="areaBox">
                    <select class="w-int ty_0" style="width: 85px; margin:0 5px 5px;"><option value="所在省">所在省</option><option value="110000">北京</option><option value="120000">天津</option><option data-title="closed" value="130000">河北</option><option data-title="closed" value="140000">山西</option><option data-title="closed" value="150000">内蒙古</option><option data-title="closed" value="210000">辽宁</option><option data-title="closed" value="220000">吉林</option><option data-title="closed" value="230000">黑龙江</option><option data-title="closed" value="310000">上海</option><option data-title="closed" value="320000">江苏</option><option data-title="closed" value="330000">浙江</option><option data-title="closed" value="340000">安徽</option><option data-title="closed" value="350000">福建</option><option data-title="closed" value="360000">江西</option><option data-title="closed" value="370000">山东</option><option data-title="closed" value="410000">河南</option><option data-title="closed" value="420000">湖北</option><option data-title="closed" value="430000">湖南</option><option data-title="closed" value="440000">广东</option><option data-title="closed" value="450000">广西</option><option data-title="closed" value="460000">海南</option><option data-title="closed" value="500000">重庆</option><option data-title="closed" value="510000">四川</option><option data-title="closed" value="520000">贵州</option><option data-title="closed" value="530000">云南</option><option data-title="closed" value="540000">西藏</option><option data-title="closed" value="610000">陕西</option><option data-title="closed" value="620000">甘肃</option><option data-title="closed" value="630000">青海</option><option data-title="closed" value="640000">宁夏</option><option data-title="closed" value="650000">新疆</option><option data-title="closed" value="710000">台湾</option><option data-title="closed" value="810000">香港</option><option data-title="closed" value="820000">澳门</option></select>
                    <select class="w-int ty_1" style="width: auto;  margin:0 5px 5px;">
                        <option value="市">市</option>
                    </select>
                    <select class="w-int ty_2" style="width: auto;  margin:0 5px 5px;">
                        <option value="区">区</option>
                    </select>
                    <select class="w-int ty_3" style="width: auto; margin:0 5px 5px;" data-type="school" data-message="请选择学校">
                        <option value="">请选择学校</option>
                    </select>
                </span>
            </dd>
            <%if(userType == "student"){%>
                <dt>我的班级：</dt><dd><input data-type="className" type="text" value="" class="w-int" data-message="请输入班级" maxlength="16" placeholder="如3年级2班"/></dd>
            <%}%>
        </dl>
    </div>
</script>

<script type="text/html" id="T:在线客服反馈">
    <div class="w-form-table" id="serverFormFeedback" style="margin: -30px 0; text-align: left;">
        <div style="margin: 0 40px; padding-bottom: 20px;">请详细描述您遇到的问题，将有专业的客服为您解答。</div>
        <dl>
            <#--<dt>一起小学学号：</dt><dd><input data-type="userId" type="text" value="${(currentUser.id)!}" class="w-int" data-message="请输入正确的账号" maxlength="12" /></dd>-->
            <dd style="margin: 10px 40px; clear: both;">
                <textarea style="width: 90%; height: 84px; resize: none;" data-type="describe" class="w-int" maxlength="100" data-message="请输入详细描述问题" placeholder="例如：老师推荐了练习，为什么我看不到？"></textarea>
            </dd>
        </dl>
    </div>
</script>
