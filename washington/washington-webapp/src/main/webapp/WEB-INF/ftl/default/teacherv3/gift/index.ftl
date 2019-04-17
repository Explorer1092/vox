<#import "module.ftl" as temp />
<@temp.page title="赠送礼物">
    <@sugar.capsule js=["jcarousel"] />
    <ul class="unstyled_vox" style="margin: 0 10px;">
        <li>
            <h2 class="text_small">赠送给：</h2>
            <div class="giftSelect">
                <div class="w-select">
                    <div class="current" id="select_send_object_but" style="width: 400px;"><span class="content" id="name_show_box">请选择同学</span><span class="content" style="width: 68px; display: none;" id="count_show_box">--</span><span class="w-icon w-icon-arrow"></span></div>
                </div>
                <div id="students_list_box" class="pop" style="display: none;">
                    <div class="tab">
                        <span class="close" id="close_select_send_object">×</span>
                        <ul id="clazz_list">
                            <#if clazzList?has_content>
                                <#list clazzList as c>
                                    <li class="active" data-clazz_id="${c.clazzId}" title="${c.clazzName!''}"><span>${c.clazzName!''}</span></li>
                                </#list>
                            <#else>
                                <div style="margin-left: 10px;">您还没有任何班级，或者还没有使用学生</div>
                            </#if>
                        </ul>
                    </div>
                    <ul id="students_list" class="content">
                        <#list studentList as s>
                            <li data-clazz_id="${s.clazzId}" data-student_id="${s.studentId}" data-student_name="${s.studentName!''}"><span class="w-checkbox"></span> ${s.studentName}</li>
                        </#list>
                    </ul>
                </div>
            </div>
        </li>
        <li>
            <h2 class="text_small">选择礼物：</h2>
            <div class="giftTabGift">
                <div class="tab">
                    <ul id="gift_type_list_box">
                        <li class="active" dataurl="/teacher/gift/list.vpage?category=FESTIVAL&currentPage=0">节日</li>
                        <#--暂不开放 “祝福”-->
                        <#--<li dataurl="/teacher/gift/list.vpage?category=BLESSING&currentPage=0">祝福</li>-->
                    </ul>
                </div>
                <div class="content">
                    <ul id="gift_list_box" class="listbox app_init_auto_get_html" dataurl="/teacher/gift/list.vpage?category=FESTIVAL&currentPage=0">
                    <#-- 礼物显示区 -->
                    </ul>
                    <div class="clear"></div>
                </div>
            </div>
        </li>
        <li>
            <h2 class="text_small">赠言：</h2>
            <textarea id="postscript_box" name="content" placeholder="他今天做了什么值得表扬的事，在这里告诉全班同学吧～" cols="" rows="" class="w-int" style="width:400px; height:60px;"></textarea>
        </li>
        <li class="w-magT-10">
            <a id="send_gift_but" href="javascript:void(0);" class="w-btn"><strong>送出礼物</strong></a>
        </li>
    </ul>


<script type="text/javascript">
    $(function(){
        //记录被选中的学生
        var recordSelectStudent = {
            userIdList : [],
            userNameList : []
        };

        //年级切换
        var clazzListSize = $("#clazz_list li").length;
        if(clazzListSize > 3){
            $("#clazz_list").jcarousel({scroll:3});
        }

        //下拉框显示or隐藏
        $("#select_send_object_but").click(function(){
            $("#students_list_box").slideToggle(100);
        });

        //关闭下拉框
        $("#close_select_send_object").click(function(){
            $("#students_list_box").slideUp(100);
        });

        $(document).on("click",function(e){
            if($(e.target).closest("#students_list_box").length == 0 && $(e.target).closest(".giftSelect").length == 0){
                $("#students_list_box").slideUp(100);
            }
        });

        //选择要送礼物的学生
        $("#students_list li").click(function(){
            var $this = $(this);
            var $thisChildren = $this.find(".w-checkbox");
            var studentId = $this.attr("data-student_id");
            var studentName = $this.attr("data-student_name");
            var showNameBox = $("#name_show_box");
            var countShowBox = $("#count_show_box");
            var selectSendObject = $("#select_send_object_but");

            if($thisChildren.hasClass("w-checkbox-current")){
                $thisChildren.removeClass("w-checkbox-current");
                recordSelectStudent.userIdList.splice($.inArray(studentId, recordSelectStudent.userIdList), 1);
                recordSelectStudent.userNameList.splice($.inArray(studentName, recordSelectStudent.userNameList), 1);
            }else{
                $thisChildren.addClass("w-checkbox-current");
                recordSelectStudent.userIdList.push(studentId);
                recordSelectStudent.userNameList.push(studentName);
            }

            if(recordSelectStudent.userIdList.length < 1){
                showNameBox.html("请选择同学");
                countShowBox.hide().text('--');
            }else{
                showNameBox.html(recordSelectStudent.userNameList.join('、'));
                countShowBox.show().text(recordSelectStudent.userIdList.length + "位同学");
                selectSendObject.removeClass("current-error");
            }
        });

        //礼物类型
        $("#gift_type_list_box li").click(function(){
            var $this = $(this);
            if($this.hasClass('active')){return false;}
            $this.addClass('active').siblings().removeClass('active');
            $("#gift_list_box").load($this.attr('dataurl'));
        });

        //班级列表
        $("#clazz_list li").on('click', function(){
            var $this = $(this);
            $this.radioClass('active');
            var clazzId = $this.data('clazz_id');
            initializationMethod();
            $("#students_list li").hide();
            $("#students_list li[data-clazz_id="+clazzId+"]").show();
        });
        $("#clazz_list li:first").trigger('click');

        //送出礼物
        $("#send_gift_but").on('click', function(){
            var gift = $("#gift_list_box li p span.radios_active");
            var giftId = gift.data("gift_id");
            var gold = gift.data("gold");
            var content = $("#postscript_box").val().replace(/\"/g, "");

            if(recordSelectStudent.userIdList.length < 1){
//                $17.alert("选择要赠送礼物的同学.");
                $("#select_send_object_but").addClass("current-error");
                $("#students_list_box").slideDown(100);
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

            var url = "/teacher/gift/teachersendgifttostudent.vpage";
            var data = {
                receiverId: recordSelectStudent.userIdList.join(),
                giftId: giftId,
                postscript: content
            };

            if(gold > 0){
                $.prompt("将花费<strong class='w-orange'>"+ (gold * recordSelectStudent.userIdList.length) +"</strong>园丁豆给以下同学赠送礼物：<span>"+ recordSelectStudent.userNameList.join("、") +"</span>",{
                    title : "提示",
                    focus: 1,
                    buttons: { "取消": false, "确定": true },
                    submit : function(e,v){
                        e.preventDefault();
                        if(v){
                            $.prompt.close();
                            giftSubmit(data,url);
                        }else{
                            $.prompt.close();
                        }
                    }
                });
            }else{
                giftSubmit(data,url);
            }
        });

        //数据提交
        function giftSubmit(data, url){
            $.post(url, data, function(data){
                if(data.success){
                    initializationMethod();
                    $17.alert("礼物赠送成功");
                    $17.tongji("老师-礼物赠送成功","老师-礼物赠送成功");
                }else{
                    $17.alert(data.info);
                }
            });
        }

        //初始化选择
        function initializationMethod(){
            recordSelectStudent.userIdList = [];
            recordSelectStudent.userNameList = [];

            $("#students_list li .w-checkbox").removeClass("w-checkbox-current");
            $("#name_show_box").text("请选择同学");
            $("#count_show_box").text("--").hide();
            $("#postscript_box").val('');
        }
    });
</script>
</@temp.page>