<script>
    window.location.replace("/");
</script>
<#--
<#import "loyout.ftl" as temp />
<@temp.tinyGroup title="调整${temp.curSubjectText!}小组">
&lt;#&ndash;//start content&ndash;&gt;
<div id="RenderingTemplate"></div>
&lt;#&ndash;content end//&ndash;&gt;
<script type="text/javascript">
    $(function(){
        var allGroupCrewList = [];
        var batchClassItem = {};
        var recordTempGroups = -1;//记录当前班级组
        var recordTempStudent = {ids:[], names:[]};//记录被选择的学生

        function RenderingTemplate(){
            $.get("/teacher/clazz/tinygroup/getcrew.vpage", { clazzId : ${clazzId!0}, subject : "${temp.curSubject!}" }, function(data){
                if(data.success){
                    var noUsersFlag = true;
                    var allGroup = data.groups.leaders;
                    batchClassItem = data.groups;

                    batchClassItem.allGroupCrewList = allGroup.concat(data.groups.members);

                    if(!data.groups.groupId){
                        noUsersFlag = false;
                    }

                    $("#RenderingTemplate").html( template("T:所有组员", {data : batchClassItem, noUsersFlag : noUsersFlag}) );

                    setTimeout(function(){
                        $(".v-groupMenuBtn[data-index='"+recordTempGroups+"']").click();
                    }, 300);
                }
            });
        }

        //初始化组员
        RenderingTemplate();

        //选择学生
        $(document).on("click", ".v-clickSelectUserName", function(){
            var $this = $(this);
            var $userId = $this.attr("data-id");
            var $userName = $this.attr("data-name");

            if($this.hasClass("active")){
                recordTempStudent.ids.splice($.inArray($userId, recordTempStudent.ids), 1);
                recordTempStudent.names.splice($.inArray($userName, recordTempStudent.names), 1);

                $(".v-studentAllCheckBox").removeClass("active");
                $this.removeClass("active");
            }else{
                recordTempStudent.ids.push($userId);
                recordTempStudent.names.push($userName);

                $this.addClass("active");
            }
        });

        //全选学生
        $(document).on("click", ".v-studentAllCheckBox", function(){
            var $this = $(this);

            if($this.hasClass("active")){
                allCheckBox(true);
                $this.removeClass("active");
            }else{
                allCheckBox(false);
                $this.addClass("active");
            }

            function allCheckBox(b){
                var $thisCheck = $(".v-clickSelectUserName");

                recordTempStudent = {ids:[], names:[]};

                if(b){
                    $thisCheck.removeClass("active");
                }else{
                    $thisCheck.addClass("active");
                    $thisCheck.each(function(){
                        var $that = $(this);
                        var $userId = $that.attr("data-id");
                        var $userName = $that.attr("data-name");

                        recordTempStudent.ids.push($userId);
                        recordTempStudent.names.push($userName);
                    });
                }
            }
        });

        //删除组
        $(document).on("click", ".v-deleteGroup", function(){
            var $this = $(this);

            $.prompt("<div style='text-align: center;'>确定删除 <span style='color: #189cfb; font-size: 18px;'>"+$this.attr("data-name")+"</span> 吗？删除后该组组长、组员将变成未分组</div>", {
                title : "移动学生",
                focus: 1,
                buttons : {"取消" : false, "确定": true},
                submit : function(e, v){
                    if(v){
                        $.post("/teacher/clazz/tinygroup/deltg.vpage", {
                            groupId : $this.attr("data-groupid"),
                            tinyGroupId : $this.attr("data-id"),
                            subject : "${temp.curSubject!}"
                        }, function(data){
                            if(data.success){
                                recordTempGroups = 0;
                                RenderingTemplate();
                            }else{
                                $17.alert(data.info);
                            }
                        });
                    }
                }
            });
        });

        //设置小组长
        $(document).on("click", ".v-editLeader", function(){
            var $this = $(this);

            if(recordTempStudent.ids.length > 1){
                $17.alert("不能任命多个小组长");
                return false;
            }

            if(recordTempStudent.ids.length < 1){
                $17.alert("请选择要任命的小组长");
                return false;
            }

            $.prompt(template("T:确定调整小组长吗", {}), {
                focus : 1,
                title : "系统提示",
                buttons: {"取消" : false, "确定": true },
                position: {width: 460},
                submit : function(e, v){
                    if(v){
                        var message = $("#sendStudentMessage").val();

                        $.post("/teacher/clazz/tinygroup/rtgl.vpage", {
                            groupId : $this.attr("data-groupid"),
                            tinyGroupId : $this.attr("data-id"),
                            leaderId : recordTempStudent.ids.join(),
                            message : message,
                            subject : "${temp.curSubject!}"
                        }, function(data){
                            if(data.success){
                                RenderingTemplate();
                            }else{
                                $17.alert(data.info);
                            }
                        });
                    }
                }
            });
        });

        //选择移动组别
        $(document).on("click", ".v-clazzGroups-select .current", function(){
            var $this = $(this);
            var $sibUl = $this.siblings("ul");

            if(recordTempStudent.ids < 1){
                $17.alert("请选择需要移动到其他组的学生！");
                return false;
            }

            $sibUl.show();

            //点击移动
            $sibUl.find("li").on("click", function(){
                var $thisLi = $(this);
                $sibUl.hide();

                if(recordTempStudent.ids < 1){
                    $17.alert("请选择需要移动到其他组的学生！");
                    return false;
                }

                $this.find(".content").text($thisLi.text());

                $.post("/teacher/clazz/tinygroup/rtgm.vpage", {
                    groupId : batchClassItem.groupId,
                    to : $thisLi.attr("data-tinygroupid"),
                    studentIds : recordTempStudent.ids.join(),
                    subject : "${temp.curSubject!}"
                }, function(data){
                    if(data.success){
                        RenderingTemplate();
                    }else{
                        $17.alert(data.info);
                    }
                });
            });

            //离开
            $(document).on("mouseleave", ".v-clazzGroups-select", function(){
                $sibUl.hide();
            });
        });

        //切换分组
        $(document).on("click", ".v-groupMenuBtn", function(){
            var $this = $(this);
            var $index = $this.attr("data-index")*1;

            if($this.hasClass("active") || $17.isBlank($index)){ return false; }

            var $tinyGroupId = $this.attr("data-tinygroupid");
            var $tinyGroupName = $this.attr("data-tinygroupname");
            var $remainder = ($index + 5 - $index%6);
            var rest = batchClassItem.rest;//默认未分组学生

            recordTempStudent = {ids:[], names:[]};
            recordTempGroups = $index;
            $this.addClass("active").siblings(".v-groupMenuBtn").removeClass("active");
            $this.siblings().find(".gs-edit").hide();
            if($remainder > batchClassItem.tinyGroups.length){
                $remainder = batchClassItem.tinyGroups.length;
            }

            //单组学生
            if($index > 0){
                rest = batchClassItem.tinyGroups[$index-1].tinyGroupMembers;
            }

            //全部学生
            if($index == -1){
                rest = batchClassItem.allGroupCrewList;
                rest = arrayExchangePosition(rest);
            }

            $(".v-groupSignBox").html(template("T:单个组员", {rest: rest, batchClassItem : batchClassItem, tinyGroupId: $tinyGroupId, tinyGroupName: $tinyGroupName}));
        });

        //设置组长置顶
        function arrayExchangePosition(item){
            var isLeader = [];
            var noLeader = [];

            if(!$17.isBlank(item) && item.length > 0){
                for(var i = 0; i < item.length; i++){
                    if(item[i].isLeader){
                        isLeader.push(item[i]);
                    }else{
                        noLeader.push(item[i]);
                    }
                }

                item = isLeader.concat(noLeader);
            }

            return item;
        }

        //编辑组名
        $(document).on("click", ".v-editGroupTag", function(){
            var $that = $(this);
            $(".v-editGroupNameTag").parent().hide();
            $that.siblings().find("div[data-flag='edit']").show();

            //更改组名
            $(document).on("click", ".v-editGroupNameTag", function(){
                var $this = $(this);
                var $info = "确定更改组名？";
                var newNameVal = $this.siblings("input").val();
                var defName = $this.attr("data-defname");

                if(newNameVal.length > 6){
                    $17.alert("请输入6位以内的组名！");
                    return false;
                }

                if(defName == newNameVal || newNameVal == ""){
                    //没有做修改名
                    $this.parent().hide();
                    return false;
                }

                $.prompt("<div class='w-ag-center'>"+$info+"</div>", {
                    focus: 1,
                    title: "系统提示",
                    buttons: { "取消": false, "确定": true },
                    position: {width: 500},
                    submit : function(e, v){
                        if(v){
                            var newNameVal = $this.siblings("input").val();
                            var defName = $this.attr("data-defname");

                            if(defName != newNameVal){
                                $.post("/teacher/clazz/tinygroup/rtgn.vpage", {
                                    tinyGroupId : $this.attr("data-groupid"),
                                    tinyGroupName : newNameVal,
                                    subject : "${temp.curSubject}"
                                }, function(data){
                                    if(data.success){
                                        RenderingTemplate();
                                    }else{
                                        $17.alert(data.info);
                                    }
                                });
                            }
                        }
                    }
                });
            });
        });
    });
</script>
<script type="text/html" id="T:所有组员">
    <style>
        .t-homeworkClass-list li{ margin-top: 7px;}
    </style>
    <%if(noUsersFlag){%>
        <%var tinyGroups = data.tinyGroups, rest = data.rest, _tempGroupdId = data.groupId%>
        <div class="w-sideMenuBox">
            <div class="w-sideMenuBox-left">
                <ul class="sd-ul">
                    <li class="sd-li v-groupMenuBtn" data-index="-1" data-tinygroupid="-1" data-tinygroupname="全部学生">
                        <span class="sd-arrow"><span class="sd-arrow-inr">◆</span></span>
                        <div class="sd-icon" style="display: block;"><span class="w-icon w-icon-13"></span></div>
                        <div class="sd-title">全部学生 (<%=data.allGroupCrewList.length%>)</div>
                    </li>
                    <li class="sd-li v-groupMenuBtn" data-index="0" data-tinygroupid="0" data-tinygroupname="未分组">
                        <span class="sd-arrow"><span class="sd-arrow-inr">◆</span></span>
                        <div class="sd-icon" style="display: block;"><span class="w-icon w-icon-38"></span></div>
                        <div class="sd-title">未分组 (<%=rest.length%>)</div>
                    </li>
                    <%for(var i=0; i < tinyGroups.length; i++){%>
                    &lt;#&ndash;<%=(tinyGroups[i].tinyGroupMembers[0].userName)%>&ndash;&gt;
                    <%var defaultName = (tinyGroups[i].tinyGroupName ? tinyGroups[i].tinyGroupName : (tinyGroups[i].tinyGroupMembers[0].userName + '组') )%>
                    <li class="sd-li v-groupMenuBtn" data-tinygroupid="<%=tinyGroups[i].tinyGroupId%>" data-tinygroupname="<%=defaultName%>" data-index="<%=(i+1)%>">
                        <span class="sd-arrow"><span class="sd-arrow-inr">◆</span></span>
                        <div class="sd-icon v-editGroupTag"><span class="w-icon w-icon-blue w-icon-3"></span></div>
                        <div class="sd-title">
                            <%=defaultName%>(<%=tinyGroups[i].tinyGroupMembers.length%>)
                            <div class="gs-edit" data-flag="edit" style="display: none; position: absolute; background-color: #fff;width: 142px;top: 0; left: 8px; text-indent: 0; z-index: 3;">
                                <input style="width: 65px;" class="w-int" type="text" value="<%=tinyGroups[i].tinyGroupName%>" placeholder="改组名" maxlength="6">
                                <a style="width: 45px;" class="w-btn w-btn-mini v-editGroupNameTag" href="javascript:void (0);" data-groupid="<%=tinyGroups[i].tinyGroupId%>" data-defname="<%=tinyGroups[i].tinyGroupName%>">确定</a>
                            </div>
                        </div>
                    </li>
                    <%}%>
                    <li class="sd-li">
                        <a href="/teacher/clazz/tinygroup/create.vpage?clazzId=${clazzId!0}&subject=${temp.curSubject!}"><div class="sd-title">创建新组 +</div></a>
                    </li>
                </ul>
            </div>
            <div class="w-sideMenuBox-right v-groupSignBox">&lt;#&ndash;单个组员 content&ndash;&gt;</div>
        </div>
    <%}else{%>
        <div class="w-gray" style="padding: 40px 0; text-align: center;">暂时没有组员可调整</div>
        <div class="t-pubfooter-btn">
            <a class="v-next w-btn w-btn-small w-btn-green" style="margin: 0;" href="/teacher/clazz/tinygroup/index.vpage?clazzId=${clazzId}&subject=${temp.curSubject!}">返回</a>
        </div>
    <%}%>
</script>
<script type="text/html" id="T:单个组员">
    <%var groupId = batchClassItem.groupId, tinyGroups = batchClassItem.tinyGroups%>
    <div style="clear:both; height: 25px; padding:10px;">
        <%if(tinyGroupId > 0){%>
        <a class="v-deleteGroup w-blue" href="javascript:void(0);" data-groupid="<%=groupId%>" data-id="<%=tinyGroupId%>" data-name="<%=tinyGroupName%>" style="float: left; margin: 7px 0 0;">删除该组</a>
        <%}%>
        <div class="t-gray-bar-box" style="background-color: #189cfb; border: 1px solid #0979ca; border-radius: 4px; float: right; height: 24px; width: 228px;">
            <span class="move" style="color: #fff; float: left; padding: 5px 5px 0;">移动至</span>
            <div class="v-clazzGroups-select w-select" style="float: right;">
                <div class="current" style="border: none; border-radius: 4px;"><span class="content">请选择小组</span><span class="w-icon w-icon-arrow"></span></div>
                <ul style="display: none; height: 92px;">
                    <%if(tinyGroupId != 0){%>
                        <li data-tinygroupid="0"><a href="javascript:void (0);">未分组</a></li>
                    <%}%>
                    <%for(var i = 0; i < tinyGroups.length; i++){%>
                        <%if(tinyGroups[i].tinyGroupId != tinyGroupId){%>
                            <%var defaultName = (tinyGroups[i].tinyGroupName ? tinyGroups[i].tinyGroupName :  '未命名组')%>
                            <li data-tinygroupid="<%=tinyGroups[i].tinyGroupId%>" ><a href="javascript:void (0);"><%=defaultName%></a></li>
                        <%}%>
                    <%}%>
                </ul>
            </div>
        </div>

        <%if(tinyGroupId > 0){%>
        <a class="w-btn w-btn-mini v-editLeader" data-groupid="<%=groupId%>" data-id="<%=tinyGroupId%>" data-name="<%=tinyGroupName%>" href="javascript:void (0);" style="float: right; margin-right: 10px;">更换小组长</a>
        <%}%>
    </div>
    <div class="w-table w-table-border" style="margin: 0 10px 10px;">
        <table>
            <thead>
                <tr>
                    <th width="45%"><span class="v-studentAllCheckBox" data-index="0" style="cursor: pointer;"><i class="w-checkbox"></i> 学生姓名</span></th>
                    <th width="45%">小组名称</th>
                </tr>
            </thead>
            <tbody>
                <%if(rest.length > 0){%>
                    <%for(var i=0; i < rest.length; i++){%>
                        <tr class="">
                            <td>
                                <p style="margin-left: 110px; cursor:<%if(rest[i].isLeader){%>default<%}else{%>pointer<%}%>;" <%if(!rest[i].isLeader){%>class="v-clickSelectUserName"<%}%> data-id="<%=rest[i].userId%>" data-name="<%=rest[i].userName%>">
                                <%if(rest[i].isLeader){%><span class="w-icon-public w-icon-leader"></span><%}else{%><span class="w-checkbox"></span><%}%>
                                <span class="w-icon-md" style="<%if(rest[i].isLeader){%>width: auto;<%}%>"><%=(rest[i].userName ? rest[i].userName : rest[i].userId)%></span>
                                </p>
                            </td>
                            <th>&lt;#&ndash;<%=(rest[i].tinyGroupName ? rest[i].tinyGroupName : '未分组')%>&ndash;&gt;<%=tinyGroupName%></th>
                        </tr>
                    <%}%>
                <%}else{%>
                    <tr>
                        <td colspan="2">
                            <div style="padding: 30px; color: #999; text-align: center;">暂时没有组员可调整</div>
                        </td>
                    </tr>
                <%}%>
            </tbody>
        </table>
    </div>
</script>
<script type="text/html" id="T:确定调整小组长吗">
    <div style="font-size: 14px;">
        <p style="font-size: 18px;">确定调整小组长吗？</p>
        <p style="padding: 20px 0 10px;">再对被卸任的小组长说点什么吧：</p>
        <div>
            <textarea style="color: #7f96a3; width: 400px; height: 50px; line-height: 25px; font-size: 14px;" class="w-int" id="sendStudentMessage">老师通知：你的小组长任期已满。以后请继续加油哦！</textarea>
        </div>
    </div>
</script>
</@temp.tinyGroup>-->
