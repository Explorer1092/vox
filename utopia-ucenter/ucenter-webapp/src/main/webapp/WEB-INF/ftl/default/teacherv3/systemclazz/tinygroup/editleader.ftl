<script>
    window.location.replace("/");
</script>
<#--
<#import "loyout.ftl" as temp />
<@temp.tinyGroup title="调整组长">
&lt;#&ndash;//start&ndash;&gt;
    <div id="RenderingTemplate"></div>
    <div class="t-pubfooter-btn">
        <a class="v-next w-btn w-btn-small v-appointedLeader-submit" href="javascript:void(0);">任命小组长</a>
    </div>
&lt;#&ndash;end//&ndash;&gt;
<script type="text/javascript">
    $(function(){
        var batchClassItem = ${info!};
        var recordTempGroups = 1;//记录当前班级组

        function RenderingTemplate(){
            $("#RenderingTemplate").html( template("T:所有组员", {data : batchClassItem}) );

            setTimeout(function(){
                $(".v-groupMenuBtn[data-index='"+recordTempGroups+"']").click();
            }, 300);
        }
        //初始化组员
        RenderingTemplate();

        //选择学生
        $(document).on("click", ".v-clickSelectUserName", function(){
            var $this = $(this);
            var $userId = $this.attr("data-id");


            if($this.hasClass("active") || $this.hasClass("active")){
                return false;
            }

            $this.addClass("active").siblings().removeClass("active");

            batchClassItem.tinyGroups[recordTempGroups-1].leaderId = $userId;
            console.info(recordTempGroups)
            $(".v-groupMenuBtn[data-index='"+recordTempGroups+"']").attr("data-leaderid", $userId);
        });

        //切换分组
        $(document).on("click", ".v-groupMenuBtn", function(){
            var $this = $(this);
            var $index = $this.attr("data-index")*1;

            if($this.hasClass("active") || $17.isBlank($index)){ return false; }

            var $tinyGroupId = $this.attr("data-tinygroupid");
            var $tinyGroupName = $this.attr("data-tinygroupname");
            var $leaderId = $this.attr("data-leaderid");
            var $remainder = ($index + 6 - $index%6);
            var rest = batchClassItem.all;

            recordTempGroups = $index;

            $this.addClass("active").siblings(".v-groupMenuBtn").removeClass("active");

            if($remainder > batchClassItem.tinyGroups.length){
                $remainder = batchClassItem.tinyGroups.length;
            }

            if($index%6 == 0){
                $remainder = $index;
            }

            $this.siblings(".v-parent").remove();
            $(".v-groupMenuBtn[data-index='"+ $remainder +"']").after(template("T:单个组员", {rest: rest, groupId : batchClassItem.groupId, tinyGroupId: $tinyGroupId, tinyGroupName: $tinyGroupName, leaderId : $leaderId}));
        });

        $(document).on("click", ".v-appointedLeader-submit", function(){
            var $this = $(this);

            $.prompt(template("T:确定调整小组长吗", {}), {
                focus : 1,
                title : "系统提示",
                buttons: {"取消" : false, "确定": true },
                position: {width: 460},
                submit : function(e, v){
                    if(v){
                        var message = $("#sendStudentMessage").val();

                        for(var i = 0; i < batchClassItem.tinyGroups.length; i++){
                            delete batchClassItem.tinyGroups[i].tinyGroupName;
                        }

                        App.postJSON("rtgl.vpage", {
                            groupId : batchClassItem.groupId,
                            json : batchClassItem.tinyGroups,
                            message : message
                        }, function(data){
                            if(data.success){
                                $17.alert("调整成功！");
                            }else{
                                $17.alert(data.info);
                            }
                        });
                    }
                }
            });
        });
    });
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
<script type="text/html" id="T:所有组员">
    <style>
        .t-homeworkClass-list li{ margin-top: 7px;}
    </style>
    <%var tinyGroups = data.tinyGroups, rest = data.all, _tempGroupdId = data.groupId%>
    <div class="t-addclass-case t-addClass-case-new" style="padding: 15px 0;">
        <div style="background-color: #f9fcfe;">
            <dl>
                <dd class="clear" style="margin: 0 0 0 42px;">
                    <div class="w-border-list t-homeworkClass-list">
                        <ul>
                            <%for(var i=0; i < tinyGroups.length; i++){%>
                            <%var defaultName = (tinyGroups[i].tinyGroupName ? tinyGroups[i].tinyGroupName :  '未命名组')%>
                                <li class="v-groupMenuBtn" data-tinygroupid="<%=tinyGroups[i].tinyGroupId%>" data-tinygroupname="<%=defaultName%>" data-leaderid="<%=tinyGroups[i].leaderId%>" data-index="<%=(i+1)%>"><%=defaultName%></li>
                            <%}%>
                        </ul>
                    </div>
                </dd>
            </dl>
        </div>
        <div class="w-clear"></div>
    </div>
</script>
<script type="text/html" id="T:单个组员">
    <li class="v-parent pull-down" style="width:689px;">
        <%if(rest.length > 0){%>
            <%for(var i=0; i < rest.length; i++){%>
            <p style="border:none;" class="v-clickSelectUserName <%=(leaderId == rest[i].userId ? 'active':'')%>" data-id="<%=rest[i].userId%>" data-index="<%=i%>" data-name="<%=rest[i].userName%>">
                <span class="w-radio"></span>
                <span class="w-icon-md"><%=(rest[i].userName ? rest[i].userName : rest[i].userId)%></span>
            </p>
            <%}%>
        <%}else{%>
            <div style="padding: 30px; color: #999;">没有组员</div>
        <%}%>
    </li>
</script>
</@temp.tinyGroup>-->
