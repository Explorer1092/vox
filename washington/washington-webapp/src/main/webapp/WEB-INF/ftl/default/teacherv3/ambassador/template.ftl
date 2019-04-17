<script type="text/html" id="T:帮助同科目老师认证" xmlns="http://www.w3.org/1999/html">
    <#--帮助同科目老师认证-->
    <style>
        .invite-auth-box{ font-size: 16px; height: 80px; text-align: center; overflow: hidden; height: 100%;}
        .invite-auth-box li{ float: left; width: 50%; margin:0;}
        .invite-auth-box li p{ padding-bottom: 20px; line-height: 150%;}
        .invite-auth-box li a{ }
    </style>
    <div>
    <ul class="invite-auth-box">
        <li>
            <p>方法一：<br/>邀请同科目老师认证</p>
            <a href="/teacher/invite/index.vpage?ref=headMenu" class="w-btn w-btn-green w-btn-small">去邀请</a>
        </li>
        <li>
            <p>方法二：<br/>帮助同科目老师认证</p>
            <a href="/teacher/invite/activateteacher.vpage" class="w-btn w-btn-small">去帮助</a>
        </li>
    </ul>
    </div>
</script>
<script type="text/html" id="T:大使积分列表">
    <#--<p style="font-size: 14px; color: #f00; padding: 0 0 15px; text-align: center;">金牌大使享1.5倍积分，银牌大使享1.2倍积分。</p>-->
    <div class="w-table w-table-border" style="margin-top: -10px;">
        <table id="detailTableList">
            <thead>
            <tr>
                <td class="bg" style="width: 130px;">获取时间</td><td class="bg" style="width: 90px;">经验值</td><td class="bg">来源</td>
            </tr>
            </thead>
            <tbody>
            <%if(myScoreHistory.length > 0){%>
                <%for(var i = 0; i < myScoreHistory.length; i++){%>
                <tr>
                    <td><%=myScoreHistory[i].createDatetime%></td>
                    <td><%=myScoreHistory[i].score%></td>
                    <td><%=myScoreHistory[i].scoreType%></td>
                </tr>
                <%}%>
            <%}else{%>
            <tr>
                <td colspan="3"><div style="padding: 30px 0; text-align: center;">暂无数据</div></td>
            </tr>
            <%}%>
            </tbody>
        </table>
        <div class="system_message_page_list message_page_list" style="padding:0 0 10px;"></div>
    </div>
</script>
<script type="text/html" id="T:大使规则列表">
    <p style="font-size: 14px; color: #f00; padding: 0 0 15px; text-align: center;">大使福利的发放、线上线下活动的参与资格、优秀大使的评选等均会参考大使积分</p>
    <div class="w-table w-table-border">
        <table>
            <thead>
                <tr>
                    <td>获得积分的方法</td>
                    <td>基础积分</td>
                </tr>
            </thead>
            <tbody>
                <tr><td>给一个班布置作业或测验，有20名以及以上学生完成</td><td>＋2</td></tr>
                <tr><td>在微信上给一个班布置作业或测验，有20名以及以上学生完成</td><td>＋4</td></tr>
                <tr><td>使用智慧课堂，奖励5名及以上同学</td><td>＋4</td></tr>
                <tr><td>邀请老师完成认证</td><td>＋20</td></tr>
                <tr><td>帮助一名老师成功认证</td><td>＋20</td></tr>
                <tr><td>每个学生家长绑定微信</td><td>＋5</td></tr>
                <tr><td>论坛发帖 回帖（每周最多加2分）</td><td>＋1</td></tr>
            </tbody>
        </table>
    </div>
</script>
<script type="text/html" id="T:大使任务奖励规则">
    <p style="font-size: 14px; color: #f00; padding: 0 0 15px; text-align: center;">本月完成任一等级任务，下月享受对应等级福利</p>
    <div class="w-table w-table-border">
        <table>
            <thead>
                <tr>
                    <td>本月任务</td>
                    <td>下月奖励</td>
                </tr>
            </thead>
            <tbody>
                <tr><td>金牌任务</td><td>金牌大使荣誉＋享1.5倍大使积分</td></tr>
                <tr><td>银牌任务</td><td>银牌大使荣誉＋享1.2倍大使积分</td></tr>
                <tr><td>铜牌任务</td><td>铜牌大使荣誉</td></tr>
            </tbody>
        </table>
    </div>
    <p style="font-size: 14px; color: #666; padding: 15px 0 0; text-align: center;">注：1.5倍积分表示在获得一定积分的基础上，额外奖励0.5倍积分。</p>
</script>
<script type="text/html" id="T:辞任大使">
    <textarea class="w-int" id="resignationContent" style="height: 100px; width: 95%; font-size: 16px;" placeholder="请写明辞任理由，100字以内" maxlength="100"></textarea>
</script>
<script type="text/html" id="T:所有一键">
    <textarea class="w-int" id="resignationAllContent" style="height: 100px; width: 95%; font-size: 16px;" placeholder="<%=info%>"></textarea>
</script>

<script type="text/html" id="T:选择原因">
    <div class="t-changeclass-alert">
        <div class="class">
            <%if(dataType == "2"){%>
            <div style="font-size: 12px; padding: 0 0 15px;">取消该老师认证后，在本校／本校区TA将永久无法使用网站部分功能，
                同时从本科目的认证老师列表中消失。请选择取消原因：</div>
            <ul class="data-selectContentList">
                <li data-val="转校/转校区" style="cursor: pointer; width: 140px;">
                    <span class="w-radio"></span>
                    转校/转校区
                </li>
                <li data-val="虚假老师" style="cursor: pointer; width: 120px;">
                    <span class="w-radio"></span>
                    虚假老师
                </li>
                <li data-val="同校换科目" style="cursor: pointer;width: 120px;">
                    <span class="w-radio"></span>
                    同校换科目
                </li>
                <li data-val="退休" style="cursor: pointer;width: 120px;">
                    <span class="w-radio"></span>
                    退休
                </li>
                <li data-val="已辞职" style="cursor: pointer;width: 120px;">
                    <span class="w-radio"></span>
                    已辞职
                </li>
            </ul>
            <p class="info" style="text-align: center;color: red;width: 425px;display: none;padding-bottom: 0px;">请选择原因</p>
            <%}else{%>
            <div style="font-size: 12px; padding: 0 0 15px;">暂停认证老师后， TA将从本科目的认证老师列表中暂时消失。 请说明原因：</div>
            <ul class="data-selectContentList">
                <li data-val=" 怀孕" style="cursor: pointer; width: 100px;">
                    <span class="w-radio"></span>
                    怀孕
                </li>
                <li data-val="伤病" style="cursor: pointer; width: 100px;">
                    <span class="w-radio"></span>
                    伤病
                </li>
                <li data-val="其他" style="cursor: pointer; width: 220px; padding-bottom: 2px;">
                    <span class="w-radio"></span>
                    其他
                    <input type="text" value="" placeholder="请说明原因10字以内..." style="width: 140px;" maxlength="10" class="w-int">
                </li>
            </ul>
            <p class="info" style="text-align: center;color: red;width: 425px;display: none;padding-bottom: 0px;">请选择或填写原因</p>
            <%}%>
        </div>
    </div>
</script>
<script type="text/html" id="T:取消/暂停该老师认证">
    <p style="color: #fa7252;font-size: 16px;text-align: center;margin-top: -22px;margin-bottom: 30px;">请确保该老师已经不在本校或本科目任教，再进行以下操作</p>
    <div style="width: 228px;height: 92px;border-right: 1px dotted #dfdfdf;float: left;text-align: center;margin-bottom: 40px;">
        <a href="javascript:void (0)" class="w-btn data-cancelAuth" data-type="2" data-userid="" data-username="" style="background-color: #1abc9c; border: 1px solid #08977b;font-size: 14px;">取消该老师认证</a>
        <span style="display: inline-block;width: 174px;font-size: 12px;text-align: left;line-height: 20px;margin-top: 5px;">当该老师不是本校或本科目老</br>师时，进行此项操作</span>
    </div>
    <div style="width: 228px;height: 92px;float: left;text-align: center;margin-bottom: 40px;">
        <a href="javascript:void (0)" class="w-btn data-cancelAuth" data-type="3" data-userid="" data-username="" style="background-color: #189cfb; border: 1px solid #0979ca;font-size: 14px;">暂停该老师认证</a>
        <span style="display: inline-block;width: 200px;font-size: 12px;text-align: left;line-height: 20px;margin-top: 5px;">当该老师暂时不使用“一起作业”</br>但还在本校任教，进行此项操作</span>
    </div>
</script>
<script type="text/javascript">
    $(function(){
        //一键提醒
        $(document).on("click", ".data-keyReminder", function(){
            $.post("/ambassador/remindteacher.vpage", {}, function(data){
                $17.alert(data.info);
            });
            /*$.prompt(template("T:所有一键", {info : "嗨，这个月还没布置作业，或者布置了但完成的学生不多？是不是有什么问题，我愿意为你解答！"}), {
                title: "给本月所有未布置有效作业老师发消息",
                buttons: { "确定" : true},
                position: {width: 500},
                submit : function(e, v){
                    if(v){
                        var $content = $("#resignationAllContent");

                        if( $17.isBlank($content.val()) ){
                            $content.focus();
                            return false;
                        }

                        $.post("/ambassador/remindteacher.vpage", {}, function(data){
                            $17.alert(data.info);
                        });
                    }
                }
            });*/
        });

        //一键点赞
        $(document).on("click", ".data-likeKeyPoint", function(){
            $.prompt("<div style='text-align: center;'>校园大使赞了您：这个月布置作业辛苦了，有付出就会有回报！<p style='font-size: 18px;'>您确定要发送？</p></div>", {
                focus : 1,
                title: "给本月已布置有效作业的老师点赞",
                buttons: {"取消" : false, "确定" : true},
                position: {width: 500},
                submit : function(e, v){
                    if(v){
                        $.post("/ambassador/praiseteacher.vpage", {}, function(data){
                            $17.alert(data.info);
                        });
                    }
                }
            });
        });

        //点击获取二维码
        var tempcGetAmbWeChat = "";
        $(document).on("click", ".data-clickGetAmbWeChat", function(){
            var $this = $(this);

            if( !$17.isBlank(tempcGetAmbWeChat) ){
                popupCode(tempcGetAmbWeChat);
                return false;
            }

            $.get("/ambassador/qrcode.vpage", {}, function(data){
                tempcGetAmbWeChat = data.qrcode_url;
                popupCode(tempcGetAmbWeChat);
            });

            function popupCode(url){
                $.prompt("<div class='w-ag-center'><img src='" + url + "' width='200'/><p>微信扫一扫<br/>关注一起作业校园大使</p></div>", {
                    title: "关注一起作业校园大使",
                    buttons: {},
                    position: {width: 500}
                });
            }
        });

        //T:辞任大使
        $(document).on("click", ".data-resignationSubmit", function(){
            var $content = $("#resignationContent");
            var popupTemp = {
                step_1 : {
                    html : template("T:辞任大使", {}),
                    title: "辞任大使",
                    buttons: { "确定" : true},
                    position: {width: 500},
                    submit : function(e, v){
                        if(v){
                            $content = $("#resignationContent");

                            if( $17.isBlank($content.val()) ){
                                $content.focus();
                                return false;
                            }

                            $.prompt.goToState('step_2');
                            return false;
                        }
                    }
                },
                step_2 : {
                    html : "辞任后，30天内无法再次申请大使，您确定要辞任？",
                    title: "系统提示",
                    buttons: {"取消" : false, "确定" : true},
                    position: {width: 500},
                    submit : function(e, v){
                        if(v){
                            $.post("/ambassador/resignation.vpage", { reason : $content.val() }, function(data){
                                $17.alert(data.info)
                            });
                        }else{
                            $.prompt.goToState('step_1');
                            return false;
                        }
                    }
                }
            };

            $.prompt(popupTemp);
        });

        //T:帮助同科目老师认证
        $(document).on("click", ".data-helpAuth", function(){
            $.prompt(template("T:帮助同科目老师认证", {}), {
                title: "帮助同科目老师认证",
                buttons: {},
                position: {width: 500}
            });
        });

        //T:大使规则列表
        $(document).on("click", ".data-clickLlValue", function(){
            $.prompt(template("T:大使规则列表", {}), {
                title: "大使积分规则",
                buttons: { "知道了": false},
                position: {width: 600}
            });
        });


        //T:大使任务奖励规则
        $(document).on("click", ".data-clickRewardValue", function(){
            $.prompt(template("T:大使任务奖励规则", {}), {
                title: "大使任务奖励规则",
                buttons: { "知道了": false},
                position: {width: 500}
            });
        });

        //大使积分列表
        $(document).on("click", ".data-clickScore", function(){
            $.post("/ambassador/ambassadorscorehistory.vpage", {}, function(data){
                $.prompt(template("T:大使积分列表", { myScoreHistory : data.myScoreHistory}), {
                    title: "经验值明细",
                    buttons: {},
                    position: {width: 650},
                    loaded: function(){
                        pageList( $("#detailTableList tbody tr") );
                    }
                });
            });
        });

        //分页
        function pageList(ids){
            var detailLists = ids;
            var len = detailLists.length;
            var pageSize = 5;
            var pageCount = Math.ceil(len/pageSize);
            var regularExp=/\d+/;
            var currentPage = 1;
            var goto = function(iCur) {
                var currentPage = iCur;
                if (regularExp.test(currentPage)){
                    if (currentPage > 0 && currentPage < (pageCount + 1)) {
                        for (var i = 0; i < len; i++) {
                            detailLists.eq(i).hide();
                        }
                    }
                    var totalNum = pageSize * currentPage < len ? pageSize * currentPage : len;
                    for (var i = (currentPage - 1) * pageSize; i < totalNum; i++) {
                        detailLists.eq(i).show();
                    }
                }
                if(currentPage==1){
                    $("a[v='prev']").attr("class", "disable");
                }else{
                    $("a[v='prev']").attr("class", "enable");
                }
                if(currentPage==pageCount){
                    $("a[v='next']").attr("class", "disable");
                }else{
                    $("a[v='next']").attr("class", "enable");
                }
                $("a[data='"+currentPage+"']").addClass("this").siblings().removeClass("this");
            };

            if(pageCount>1){
                $(".message_page_list").append("<a v=\"prev\" href=\"javascript:void(0);\" class=\"disable\"><span>上一页</span></a>");
                for(var i = 1 ; i<=pageCount; i++){
                    if(i==1){
                        $(".message_page_list").append("<a href=\"javascript:void(0);\"  class=\"this\" data=\""+i+"\"><span>"+i+"</span></a>");
                    }else{
                        $(".message_page_list").append("<a href=\"javascript:void(0);\" data=\""+i+"\"><span>"+i+"</span></a>");
                    }
                }
                $(".message_page_list").append("<a v=\"next\" href=\"javascript:void(0);\" class=\"enable\"><span>下一页</span></a>");
                goto(currentPage);
            }
            $(".message_page_list a").click(function(){
                var currentPage = $(this).find('span').text();
                goto(currentPage);
            });
            $("a[v='prev']").click(function(){
                currentPage = (currentPage-1)>=1?(currentPage-1):1;
                goto(currentPage);
            });
            $("a[v='next']").click(function(){
                currentPage = (currentPage+1)<=pageCount?(currentPage+1):pageCount;
                goto(currentPage);
            });
        }


        //查看更多
        $(document).on("click", ".data-teacherCertification", function(){
            var $this = $(this);
            $this.hide();
            $this.siblings(".campusModule").show();
        });

        //取消/暂停该认证老师
        var $datauserid = "";
        var $datausername = "";
        $(document).on("click", ".data-cancelorstopAuth", function(){
            var $this = $(this);
            $.prompt(template("T:取消/暂停该老师认证", { }), {
                title: "取消/暂停该认证老师",
                buttons: { },
                position:{width : 500},
                loaded : function(){
                    $datauserid = $this.attr("data-userid");
                    $datausername = $this.attr("data-username");
                }
            });
        });

        //申请取消/暂停该老师认证
        var selectContent = "";
        $(document).on("click", ".data-cancelAuth", function(){
            selectContent = "";
            var $this = $(this);
            var $dataType = $this.attr("data-type");
            var $popupTitle = "暂停老师认证";
            var $popupSubmitInfo = "提交成功后，我们将和该老师进行确认。如果情况真实，将在7日内暂停该认证老师。您是否要继续？";
            if($17.isBlank($dataType)){
                return false;
            }

            if($dataType == "2"){
                $popupTitle = "取消老师认证";
                $popupSubmitInfo = "提交成功后，我们将和该老师进行确认。如果情况真实，将在7日内取消该认证老师。您是否要继续？";
            }

            var $popupHtml = {
                state0: {
                    html : template("T:选择原因", { dataType : $dataType}),
                    title: $popupTitle,
                    focus : 1,
                    buttons : { "取消": false, "提交" : true},
                    position : {width : 500},
                    submit : function(e,v){
                        if(v){
                            e.preventDefault();
                            if(selectContent){
                                $.prompt.goToState('submitInfo');
                            }else{
                                $("p.info").css("display","block");
                            }
                        }
                    }
                },
                submitInfo: {
                    html : "<div class='w-ag-center'>" + $popupSubmitInfo + "</div>",
                    title: "",
                    buttons : { "取消": false, "确定" : true},
                    focus : 1,
                    submit : function(e,v){
                        if(v){
                            $.post("/teacher/invite/reportTeacher.vpage", {
                                type : $dataType,
                                teacherId : $datauserid,
                                teacherName : $datausername,
                                reason : selectContent
                            }, function(data){
                                if(data.success){
                                    $17.alert(data.info);
                                }else{
                                    $17.alert(data.info);
                                }

                            });
                        }else{
                            e.preventDefault();
                            $.prompt.goToState('state0');
                        }
                    }
                }
            };

            $.prompt($popupHtml);
        });
        $(document).on("click", ".data-selectContentList li", function(){
            var $that = $(this);
            $that.addClass("active").siblings().removeClass("active");
            $that.find(".w-radio").addClass("w-radio-current");
            $that.siblings().find(".w-radio").removeClass("w-radio-current");

            selectContent = $that.attr("data-val");
            if(selectContent == "其他"){
                selectContent = "";
                $that.find("input").change(function(){
                    selectContent = $that.find("input").val();
                });
            }
            $("p.info").css("display","none");
        });

    });
</script>