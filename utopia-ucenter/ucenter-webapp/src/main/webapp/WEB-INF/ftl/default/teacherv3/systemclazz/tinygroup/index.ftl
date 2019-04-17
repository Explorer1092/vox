<script>
    window.location.replace("/");
</script>
<#--
<#import "loyout.ftl" as temp />
<@temp.tinyGroup title="${temp.curSubjectText!}小组奖励">
<@sugar.capsule js=["flexslider"] css=["plugin.flexslider"] />
<div class="w-table" style="position: relative; top:-1px;">
    <table id="checkboxs">
        <thead>
        <tr>
            <th>组名</th>
            <th>组长</th>
            <th title="该组本周完成作业、测验的人数">本周作业人数</th>
            <th title="该组本周完成作业、测验的次数总和">作业次数</th>
            <th>小组人数</th>
            <th>小组奖励</th>
            <th>最佳小组</th>
        </tr>
        </thead>
        <tbody>
            <#assign isBestGroupFlag=false isBestGroupName=""/>
            <#list tinyGroups as tg>
            <tr class="<#if tg_index%2 != 0>odd</#if>">
                <td>
                    <#assign defaultName = (tg.tinyGroupName?has_content)?string("${tg.tinyGroupName}", "${tg.leaderName!'---'}组")/>
                    ${defaultName}
                </td>
                <td>${tg.leaderName!'---'}</td>
                <th>${tg.weekCount!0}</th>
                <th>${tg.hwCount!0}</th>
                <th>${tg.studentCount!0}</th>
                <td>
                    <a class="v-sendBeans" data-groupid="${tg.tinyGroupId}">奖励学豆</a>
                </td>
                <td>
                    <#if tg.isBestGroup>
                        最佳
                        <#assign isBestGroupFlag=true isBestGroupName=defaultName/>
                    <#else>
                        <a href="javascript:void(0);" class="v-setGoodGroup" data-type="set" data-groupid="${tg.tinyGroupId}" data-groupname="${defaultName}">设置最佳</a>
                    </#if>
                </td>
            </tr>
            </#list>
        </tbody>
    </table>
</div>
<script type="text/html" id="T:小组奖励Popup">
    <style>
        .Q-groupReward-popup{}
        .Q-groupReward-popup .flexslider{ box-shadow: none; margin: 10px 0; }
        .Q-groupReward-popup .flexslider .flex-viewport{ margin: 0 46px;}
        .Q-groupReward-popup .flexslider .slides li{ float: left; cursor: pointer;}
        .Q-groupReward-popup .flexslider .slides li .img{width: 65px; height: 65px; background-color: #eee; border-radius: 100px; margin: 0 auto; padding: 3px; overflow: hidden;}
        .Q-groupReward-popup .flexslider .slides li .img img{width: 65px; height: 65px;}
        .Q-groupReward-popup .flexslider .slides li .name{ white-space: nowrap; overflow: hidden; width: 100%; text-overflow: ellipsis; line-height: 34px;}
        .Q-groupReward-popup .flexslider .slides li.active .img{ border: 3px solid #97d2fd; padding: 0;}
        .Q-groupReward-popup .flexslider .flex-control-nav{ display: none;}
        .Q-groupReward-popup .select-box{ padding:20px 0 10px 40px;}
        .Q-groupReward-popup .select-box a{ display: inline-block; padding: 0 20px;}
    </style>
    <div class="Q-groupReward-popup" style="margin-top: -40px;">
        <div class="select-box">
            <a href="javascript:void (0)" data-type="ALL" class="v-selectGroupType"><span class="w-checkbox"></span> 全组</a>
            <a href="javascript:void (0)" data-type="YES" class="v-selectGroupType"><span class="w-checkbox"></span> 已做</a>
            <a href="javascript:void (0)" data-type="NO" class="v-selectGroupType"><span class="w-checkbox"></span> 未做</a>
        </div>
        <div style="height: 120px; overflow: hidden;">
            <div class="flexslider">
                <ul class="slides">
                    <%for(var i = 0; i < students.length; i++){%>
                    <li data-userid="<%=students[i].studentId%>" data-finished="<%=students[i].finished.toString()%>" class="v-selectSendRewardSt">
                        <div class="img">
                            <%if(students[i].studentImg){%>
                            <img src="<@app.avatar href='<%=students[i].studentImg%>'/>"/>
                            <%}else{%>
                            <img src="<@app.avatar href=''/>"/>
                            <%}%>
                        </div>
                        <div class="name"><%=students[i].studentName ? students[i].studentName : students[i].studentId%></div>
                    </li>
                    <%}%>
                </ul>
            </div>
            <div style="text-align: center;width: 100%; line-height: 20px; color: #999;">还没有学生</div>
        </div>
        <div class="w-addSub-int" style="padding-left: 60px;">
            <p style="color: #4e5656; padding-bottom: 10px;">给选中的学生发学豆</p>
        &lt;#&ndash;minusPlusInputEvent()&ndash;&gt;
            <a class="w-btn w-btn-mini vox-minus-btn" href="javascript:void (0)" style="margin: 0;">-</a>
            <input class="w-int vox-count-int" maxlength="3" type="text" value="0" style="width: 100px;">
            <a class="w-btn w-btn-mini vox-plus-btn" href="javascript:void (0)">+</a>
            <p style="color: #4e5656; padding-top: 10px;">给
                <span class="v-studentCount" style="color: #39f;">0</span> 名学生每人发 <span class="v-studentCount1" style="color: #39f;">0</span>
                学豆，需要消耗您 <span class="v-studentCount2" style="color: #39f;">0</span> 园丁豆
            </p>
        </div>

        <div style="clear: both; color: #f00; display: none; padding: 10px 0 0 60px;" class="v-groupRewardInfo"></div>
    </div>
</script>
<script type="text/javascript">
    $(function(){
        window.location.replace('/');
        //设置或取消最佳小组
        var isBestGroupFlag = ${isBestGroupFlag?string};
        var isBestGroupName = "${isBestGroupName}";
        $(document).on("click", ".v-setGoodGroup", function(){
            var $this = $(this);
            var $groupname = $this.attr("data-groupname");

            var $info = "是否设置<span class='w-blue w-ft-big'>"+$groupname+"</span>为最佳小组？";

            if(isBestGroupFlag){
                $info = ("是否取消<span class='w-blue w-ft-big'>"+isBestGroupName+"</span>的最佳小组称号，设置<span class='w-blue w-ft-big'>"+$groupname+"</span>为最佳小组？");
            }

            $.prompt("<div class='w-ag-center'>"+$info+"</div>", {
                focus: 1,
                title: "系统提示",
                buttons: { "取消": false, "确定": true },
                position: {width: 500},
                submit : function(e, v){
                    if(v){
                        $.post("/teacher/clazz/tinygroup/rbtg.vpage", {
                            tinyGroupId : $this.attr("data-groupid"),
                            subject : "${temp.curSubject!}"
                        }, function(data){
                            if(data.success){
                                $17.alert("设置成功！小组成员可以在班级空间中，使用最佳小组奖励气泡！", function(){
                                    location.reload();
                                });
                            }else{
                                $17.alert(data.info);
                            }
                        });
                    }
                }
            });
        });

        //选择奖励组类型
        var tempSelectStudents = [];
        $(document).on("click", ".v-selectGroupType", function(){
            var $this = $(this);
            var $thisType = $this.data("type");

            if($this.hasClass("active")){
                $this.removeClass("active").siblings().removeClass("active");
                $(".v-selectSendRewardSt").removeClass("active");
                tempSelectStudents = [];
            }else{
                $this.addClass("active").siblings().removeClass("active");
                if($thisType == "YES"){
                    tempSelectStudents = pullValue("[data-finished='true']");
                }else{
                    if($thisType == "NO"){
                        tempSelectStudents = pullValue("[data-finished='false']");
                    }else{
                        tempSelectStudents = pullValue("");
                        $this.siblings().addClass("active");
                    }
                }
            }

            studentCount(tempSelectStudents, $(".vox-count-int").val());
        });

        function pullValue(flag){
            var item = [];
            $(".v-selectSendRewardSt").removeClass("active");
            $(".v-selectSendRewardSt" + flag).each(function(){
                $(this).addClass("active");
                item.push($(this).attr("data-userid"))
            });
            return item;
        }

        function studentCount(count, beans){
            $(".v-studentCount").text(count.length);
            $(".v-studentCount1").text(beans);
            $(".v-studentCount2").text(beans/5 * count.length);
        }

        //选择需要奖励的学生
        $(document).on("click", ".v-selectSendRewardSt", function(){
            var $this = $(this);
            var $studentId = $this.attr("data-userid");

            if($this.hasClass("active")){
                $this.removeClass("active");
                tempSelectStudents.splice($.inArray($studentId, tempSelectStudents), 1);
            }else{
                $this.addClass("active");
                tempSelectStudents.push($studentId);
            }
            studentCount(tempSelectStudents, $(".vox-count-int").val());
        });

        //奖励组长
        $(document).on("click", ".v-sendBeans", function(){
            var $this = $(this);
            var $groupId = $this.attr("data-groupid");
            var $authFlag = ${((currentUser.fetchCertificationState() != "SUCCESS")!false)?string};
                tempSelectStudents = [];
            if($authFlag){
                $17.alert("只有<span class='w-blue w-ft-big'>认证老师</span>才可以奖励学豆。");
                return false;
            }

            $.post("/teacher/clazz/tinygroup/rtgmwip.vpage", {tinyGroupId : $groupId, subject : "${temp.curSubject!}"}, function(data){
                if(data.success){
                    $.prompt(template("T:小组奖励Popup", {students: data.students}) , {
                        focus: 1,
                        title: "小组奖励",
                        buttons: { "取消": false, "确定": true },
                        position: {width: 550},
                        loaded : function(){
                            $(".flexslider").flexslider({
                                animation : "slide",
                                itemWidth : 90,
                                direction : "horizontal",//水平方向
                                minItems : 5,
                                slideshow : false,
                                touch: true //是否支持触屏滑动
                            });
                        },
                        submit : function(e, v){
                            if(v){
                                var $beansVal = $(".vox-count-int").val();

                                if(tempSelectStudents.length < 1){
                                    groupRewardInfo("请选择奖励学生");
                                    return false;
                                }

                                if($beansVal < 5){
                                    groupRewardInfo("请输入奖励学豆个数");
                                    return false;
                                }

                                function groupRewardInfo(info){
                                    $(".v-groupRewardInfo").text(info).slideDown();
                                    setTimeout(function(){
                                        $(".v-groupRewardInfo").slideUp();
                                    }, 4000);
                                }

                                $.post("/teacher/clazz/tinygroup/brtgmwi.vpage", {
                                    tinyGroupId : $groupId,
                                    studentIds : tempSelectStudents.join(","),
                                    count : $beansVal,
                                    subject : "${temp.curSubject!}"
                                }, function(data){
                                    if(data.success){
//                                        $this.parent().html("已奖励"+$beansVal+"学豆");
                                        $17.alert("奖励发放成功");
                                    }else{
                                        $17.alert(data.info);
                                    }
                                });
                            }
                        }
                    });
                }else{
                    $17.alert(data.info);
                }
            });
        });

        //设置奖励学豆数
        $17.minusPlusInputEvent({
            multiple : 5
        }, function(opt){
            studentCount(tempSelectStudents, $(opt.intCount).val());
        });
    });
</script>
</@temp.tinyGroup>-->
