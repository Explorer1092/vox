<script type="text/html" id="t:gifts">
    <div class="my-space-gift-container">
        <div class="giftMain">
            <ul class="unstyled_vox">
                <li>
                    <h2 class="text_small">赠送给：</h2>
                    <div class="giftSelect">
                        <div class="w-select">
                            <div class="current" id="select_send_object_but" style="width:400px;"><span class="content" id="name_show_box">请选择赠送的老师或同学</span><span class="content" style="width: 40px; display: none;" id="count_show_box">--</span><span class="w-icon-detail w-icon-arrow"></span></div>
                        </div>
                        <div id="select_send_object_box" class="pop" style="display: none;">
                            <div class="tab">
                                <span id="close_select_send_object" class="close">×</span>
                                <ul id="user_type_list_box">
                                    <li data-user_type='teacher' <%if(userType == 'teacher'){%>class="active"<%}%>>老师</li>
                                    <li data-user_type='student' <%if(userType == 'student'){%>class="active"<%}%>>同学</li>
                                </ul>
                            </div>
                            <ul id="teacher_list_box" class="content" <%if(userType != 'teacher'){%>style="display: none;"<%}%>>
                                <#if teachers??>
                                    <#list teachers as t>
                                        <li data-user_id="${t.id!''}" data-user_name="${(t.profile.realname)!''}老师" style="width: 120px;">
                                            <span class="checkboxs <%if(userId == ${t.id!''}){%>checkboxs_active<%}%>"  data-user_name="${(t.profile.realname)!''}老师"></span>
                                            ${(t.subject.value)!}老师：${(t.profile.realname)!}
                                        </li>
                                    </#list>
                                </#if>
                                <#if teachers?? && teachers?size == 1>
                                    <li id="show_button" style="width: 140px; padding: 9px 0 9px; text-align: center;">
                                        <a href="/student/invite/register.vpage" target="_blank">邀请其他科目老师？</a>
                                    </li>
                                </#if>
                            </ul>

                            <ul id="student_list_box" class="content" <%if(userType != 'student'){%>style="display: none;"<%}%>>
                            <#if classmates?? && classmates?has_content>
                                <#list classmates as t>
                                    <li data-user_id="${t.id!''}" data-user_name="${(t.profile.realname)!''}">
                                        <span class="checkboxs <%if(userId == ${t.id!''}){%>checkboxs_active<%}%>"  data-user_name="${(t.profile.realname)!''}"></span>
                                        ${(t.profile.realname)!''}
                                    </li>
                                </#list>
                            <#else>
                                <span style="padding: 8px 0 0 10px;; color: #666666; position: absolute">你还没有同班同学哦！</span>
                            </#if>
                            </ul>
                        </div>
                    </div>
                    <span class='init'></span>
                </li>
                <li>
                    <h2 class="text_small">选择礼物：</h2>
                    <div class="giftTabGift">
                        <div class="tab">
                            <ul id="gift_type_list_box">
                                <li class="active" dataurl="/student/gift/list.vpage?category=TEACHER&currentPage=0">感谢老师</li>
                                <li dataurl="/student/gift/list.vpage?category=BLESSING&currentPage=0">表达友情</li>
                                <li dataurl="/student/gift/list.vpage?category=FESTIVAL&currentPage=0">节日祝福</li>
                                <li dataurl="/student/gift/list.vpage?category=BIRTHDAY&currentPage=0">生日祝福</li>
                                <li dataurl="/student/gift/list.vpage?category=THANKS&currentPage=0">答谢</li>
                            </ul>
                        </div>
                        <div class="content">
                            <ul id="gift_list_box" class="listbox" dataurl="/student/gift/list.vpage?category=TEACHER&currentPage=0">
                            <#-- 礼物显示区 -->
                            </ul>
                            <div class="clear"></div>
                        </div>
                    </div>
                    <span class='init'></span>
                </li>
                <li>
                    <h2 class="text_small">赠言：</h2>
                    <textarea id="content_box" name="content" placeholder="填写你想对TA说的赠言，只有对方能看到你的赠言。（0~50字哦）" cols="" rows="" class="int_vox" style="width:400px; height:60px;"></textarea>
                    <span class='init'></span>
                </li>
                <li>
                    <a href="javascript:void(0);" class="w-btn w-btn-green v-studentVoxLogRecord" data-op="sendGift" id="send_gift_but">送出礼物</a>
                    <span class="init spacing_vox_tb" style="display:inline-block;"></span>
                </li>
            </ul>
        </div>
    </div>
</script>
<script>
    $(function(){
        //记录被选中的学生
        var recordSelectStudent = {
            userIdList : [],
            userNameList : [],
            userType : "teacher"
        };
        var currentHistoryId = 0;

        //选择要送礼物的老师
        $(document).on("click", "#teacher_list_box li:not(#show_button)", function(){
            sendToTeacherOrStudent($(this), recordSelectStudent);
        });

        //选择要送礼物的学生
        $(document).on("click", "#student_list_box li", function(){
            sendToTeacherOrStudent($(this), recordSelectStudent);
        });

        $(document).on("click", ".send-gift-button", function(){
            var $this = $(this);
            recordSelectStudent.userIdList = [];
            recordSelectStudent.userNameList = [];

            if(!$17.isBlank($this.attr("data-student_id"))){
                recordSelectStudent.userType = "student";
                recordSelectStudent.userIdList.push($this.attr("data-student_id"));
            }else{
                recordSelectStudent.userType = "teacher";
                if( !$17.isBlank($this.attr("data-teacher_id")) ){
                    recordSelectStudent.userIdList.push($this.attr("data-teacher_id"));
                }
            }

            //是否为答谢
            if( !$17.isBlank($this.attr("data-history_id")) ){
                currentHistoryId = $this.attr("data-history_id");
            }

            $.prompt(template("t:gifts", {
                userId : recordSelectStudent.userIdList.join(),
                userType : recordSelectStudent.userType
            }), {
                title: "选择礼物",
                buttons : {},
                position : { width : 820 },
                loaded : function(){
                    $("#gift_list_box").load($("#gift_type_list_box li.active").attr('dataurl'));

                    //选择答谢
                    var activeUserClick = $("#select_send_object_box").find("span.checkboxs_active");
                    if(activeUserClick.length > 0 || !$17.isBlank($this.attr("data-history_id"))){
                        recordSelectStudent.userNameList.push($("#select_send_object_box").find("span.checkboxs_active").attr("data-user_name"));
                        if(activeUserClick.closest("#teacher_list_box").length > 0){
                            recordSelectStudent.userType = "teacher";
                        }
                        $("#name_show_box").html(recordSelectStudent.userNameList.join()).siblings(".w-icon-detail").hide();
                        $("#select_send_object_but").attr("id", "");
                    }
                }
            });
        });

        //下拉框显示or隐藏
        $(document).on("click", "#select_send_object_but", function(){
            $("#select_send_object_box").slideToggle(100);
        });

        //关闭下拉框
        $(document).on("click", "#close_select_send_object", function(){
            $("#select_send_object_box").slideUp(100);
        });


        $(document).on("click",function(e){
            if($(e.target).closest("#select_send_object_box").length == 0 && $(e.target).closest(".giftSelect .w-select").length == 0){
                $("#select_send_object_box").slideUp(100);
            }
        });

        //送出礼物
        $(document).on("click", "#send_gift_but", function(){
            var gift = $("#gift_list_box li p.send-gift-correct-side");
            var giftId = gift.data("gift_id");
            var silver = gift.data("silver");
            var content = $("#content_box").val().replace(/\"/g, "");

            if(recordSelectStudent.userIdList.length < 1){
//                $17.alert("选择要赠送礼物的老师或者同学.");
                $("#select_send_object_but").addClass("current-error");
                $("#select_send_object_box").slideDown(100);
                return false;
            }

            if($17.isBlank(giftId)){
                $17.alert("选择要赠送的礼物.");
                return false;
            }

            if(content.length > 50){
                $17.alert("你填写的赠言太长了，记着要在50字以内哦！");
                return false;
            }

            var url = recordSelectStudent.userType == "student" ? "/student/gift/studentsendgifttostudent.vpage" : "/student/gift/studentsendgifttoteacher.vpage" ;
            var enterPaymentPassword = "<div class='spacing_vox'>支付密码：<input type='password' id='enterPaymentPassword' class='w-int' /><div style='margin: 5px 0 0 72px;'><a href='${(ProductConfig.getUcenterUrl())!}/student/center/account.vpage?updateType=paymentpassword' style='color: #0000ff;'>忘记支付密码？</a></div></div>";
            var data = {
                receiverId: recordSelectStudent.userIdList.join(),
                giftId: giftId,
                postscript: content
            };

            if( !$17.isBlank(currentHistoryId) ){
                data.historyId = currentHistoryId;
            }

            if(silver > 0){
                $.prompt("将花费<strong class='w-orange'>"+ (silver * recordSelectStudent.userIdList.length) +"</strong>学豆赠送礼物给：<div style='margin-bottom: 12px; color: #666;'>"+ recordSelectStudent.userNameList.join("、") + "</div>" <#if hasPaymentPassword?? && hasPaymentPassword>+enterPaymentPassword</#if>,{
                    title : "提示",
                    focus: 1,
                    buttons: { "取消": false, "确定": true },
                    submit : function(e,v){
                        e.preventDefault();
                        if(v){
                            data.paymentPassword = $("#enterPaymentPassword").val();
                            $.prompt.close();
                            postMessage(data,url);
                        }else{
                            $.prompt.close();
                        }
                    }
                });
            }else{
                postMessage(data,url);
            }
        });

        //选择送花对象
        $(document).on("click", "#user_type_list_box li", function(){
            var $this = $(this);
            var userType = $(this).data('user_type');
            var showNameBox = $("#name_show_box");
            var tBox = $("#teacher_list_box");
            var sBox = $("#student_list_box");

            if($this.hasClass("active")){
                return false;
            }

            $this.addClass("active").siblings().removeClass("active");

            showNameBox.html("请选择");
            recordSelectStudent.userIdList = [];
            recordSelectStudent.userNameList = [];
            recordSelectStudent.userType = userType;

            if(userType == 'teacher'){
                tBox.show();
                sBox.hide();
                sBox.find("li .checkboxs").removeClass("checkboxs_active");
            }else{
                tBox.hide();
                sBox.show();
                tBox.find("li .checkboxs").removeClass("checkboxs_active");
            }
        });

        //礼物类型
        $(document).on("click", "#gift_type_list_box li", function(){
            var $this = $(this);
            $this.addClass('active').siblings().removeClass('active');
            $("#gift_list_box").load($this.attr('dataurl'));
        });

        //初始化选择
        function initializationMethod(){
            recordSelectStudent.userIdList = [];
            recordSelectStudent.userNameList = [];

            $("#name_show_box").text("请选择赠送的老师或同学");
            $("#count_show_box").text("--").hide();
            $("#select_send_object_box li .checkboxs").removeClass("checkboxs_active");
        }

        //选择要送礼物的老师or学生
        function sendToTeacherOrStudent($this, recordSelectStudent){
            var $thisChildren = $this.find(".checkboxs");
            var studentId = $this.attr("data-user_id");
            var studentName = $this.attr("data-user_name");
            var showNameBox = $("#name_show_box");
            var countShowBox = $("#count_show_box");
            var selectSendObject = $("#select_send_object_but");

            if($thisChildren.hasClass("checkboxs_active")){
                $thisChildren.removeClass("checkboxs_active");
                recordSelectStudent.userIdList.splice($.inArray(studentId, recordSelectStudent.userIdList), 1);
                recordSelectStudent.userNameList.splice($.inArray(studentName, recordSelectStudent.userNameList), 1);
            }else{
                $thisChildren.addClass("checkboxs_active");
                recordSelectStudent.userIdList.push(studentId);
                recordSelectStudent.userNameList.push(studentName);
            }

            if(recordSelectStudent.userIdList.length < 1){
                showNameBox.html("请选择赠送的老师或同学");
                countShowBox.hide().text('--');
            }else{
                showNameBox.html(recordSelectStudent.userNameList.join('、'));
                countShowBox.show().text(recordSelectStudent.userIdList.length + "位");
                selectSendObject.removeClass("current-error");
            }
        }

        //数据提交
        function postMessage(data, url){
            $.post(url, data, function(data){
                if(data.success){
                    initializationMethod();
                    $("#content_box").val('');
                    $17.alert("礼物赠送成功！<#if hasPaymentPassword?? && !hasPaymentPassword><div class='spacing_vox text_center'>为了您的学豆消费安全，建议<a href='${(ProductConfig.getUcenterUrl())!}/student/center/account.vpage?updateType=paymentpassword'>设置支付密码</a></div></#if>");
                    $17.tongji("学生-礼物赠送成功","学生-礼物赠送成功");
                }else{
                    //errorType 没有分级 暂时用这种
                    if(data.info == "您的学豆不足！"){
                        $.prompt("你的学豆不够啦～<br/>好好完成作业，赢取更多学豆吧！",{
                            title : "提示" ,
                            buttons: {"知道了" : true}
                        });
                    }else{
                        if(data.info == "支付密码错误"){
                            $17.alert(data.info + "<a href='${(ProductConfig.getUcenterUrl())!}/student/center/account.vpage?updateType=paymentpassword' style='display: inline-block; margin-left: 20px'>忘记支付密码？</a>");
                        }else{
                            $17.alert(data.info);
                        }
                    }
                }
            });
        }
    });
</script>