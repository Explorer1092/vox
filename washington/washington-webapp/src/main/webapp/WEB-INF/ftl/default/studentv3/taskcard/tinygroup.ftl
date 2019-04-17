<#--任命小组长卡片-->
<#if (data.mathtgl)!false>
<li class="practice-block">
    <div class="practice-content">
        <h4>
            <span class="w-discipline-tag w-discipline-tag-9">任命卡</span>
        </h4>
        <div class="no-content" style="margin: 20px 20px; text-align: left;">
            <p class="n-3">
                恭喜你：<br/>你被老师任命为"数学小组长"，快去看看你的使命与特权吧！
            </p>
        </div>
        <div class="pc-btn">
            <a class="w-btn w-btn-orange" href="/student/tinygroup/index.vpage?subject=MATH"  target="_blank">去看看</a>
        </div>
    </div>
</li>
</#if>
<#if (data.englishtgl)!false>
<li class="practice-block">
    <div class="practice-content">
        <h4>
            <span class="w-discipline-tag w-discipline-tag-9">任命卡</span>
        </h4>
        <div class="no-content" style="margin: 20px 20px; text-align: left;">
            <p class="n-3">
                恭喜你：<br/>你被老师任命为"英语小组长"，快去看看你的使命与特权吧！
            </p>
        </div>
        <div class="pc-btn">
            <a class="w-btn w-btn-orange" href="/student/tinygroup/index.vpage?subject=ENGLISH"  target="_blank">去看看</a>
        </div>
    </div>
</li>
</#if>
<#--加入小组 (data.addtg)!false-->
<#if (data.addtg)!false>
<li class="practice-block">
    <div class="practice-content" style="background-image: url(<@app.link href='public/skin/studentv3/images/tinygroup/tinygroupCard-icon.png'/>)">
        <div class="pc-btn">
            <a class="w-btn w-btn-orange v-joinTeacherGroupBtn" href="javascript:void(0);">加入小组</a>
        </div>
    </div>
</li>
<@addTgTinyGroupJs/>
</#if>
<#macro addTgTinyGroupJs>
<script type="text/javascript">
    $(function(){
        var $postList = [];
        var $math = 0;
        var $english = 0;
        var $chinese = 0;
        $(document).on("click", ".v-groupInnerSelect", function(){
            var $this = $(this);
            var $thisSubject = $this.attr("data-subject");

            if($17.isBlank($thisSubject)){
                return false;
            }

            if($thisSubject == "ENGLISH"){
                $english = $this.attr("data-id");
            }

            if($thisSubject == "MATH"){
                $math = $this.attr("data-id");
            }

            if($thisSubject == "CHINESE"){
                $chinese = $this.attr("data-id");
            }

            $this.addClass("active").siblings().removeClass("active");
        });

        $(document).on("click", ".v-joinTeacherGroupBtn", function(){
            $postList = [];
            $math = 0;
            $english = 0;
            $chinese = 0;

            $.post("/student/tinygroup/sjtgpp.vpage", {}, function(data){
                if(data.success && data.info.length > 0){
                    var submitFlag = false;
                    for(var i = 0; i < data.info.length; i++){
                        if(data.info[i].leaders.length > 0){
                            submitFlag = true;
                        }
                    }
                    $.prompt(template("T:joinTeacherGroupInner", {data : data.info}), {
                        focus : 1,
                        title: "加入小组",
                        buttons: {"取消" :false, "确定": submitFlag },
                        position: {width: 550},
                        loaded : function(){
                            $(document).on("click", ".js-wantGrouper", function(){
                                $(this).parents(".t-checkGroup-mn-box").hide().siblings().show();
                            });
                        },
                        submit : function(e, v){
                            if(v){
                                if($english != 0){
                                    $postList.push($english);
                                }

                                if($math != 0){
                                    $postList.push($math);
                                }

                                if($chinese != 0){
                                    $postList.push($chinese);
                                }

                                if($postList.length < 1){
                                    return false;
                                }

                                $.post("/student/tinygroup/sjtg.vpage", {
                                    tgids : $postList.join()
                                }, function(data){
                                    $17.alert(( $17.isBlank(data.result[0]) ? '操作失败' : data.result[0] ) + "<br/>" + ( $17.isBlank(data.result[1]) ? '' : data.result[1] ) + "<br/>可在“班级空间”查看小组详情", function(){
                                        location.reload();
                                    });
                                });
                            }
                        }
                    });
                }else{
                    $17.alert("你已经加入小组");
                }
            });
        });

        $(document).on("click", ".wantGroupLeader-button", function(){
            var $this = $(this);
            var groupId = $this.siblings(".groupId").text();

            $.post("/student/tinygroup/sctg.vpage", {groupId:groupId}, function(data){
                if(data.success){
                    $17.alert("报名成功！你现在是本周的"+ $this.attr("data-text") +"小组长了！快去任务卡片处看看你的使命与特权吧！");
                }else{
                    if(data.info.indexOf("绑定") > 0){
                        $.prompt("<div class='w-ag-center'>"+ data.info +"</div>", {
                            focus : 1,
                            title: "系统提示",
                            buttons: {"取消" :false, "去绑定": true },
                            position: {width: 500},
                            submit : function(e, v){
                                if(v){
                                    setTimeout(function(){
                                        location.href = "${(ProductConfig.getUcenterUrl())!''}/student/center/account.vpage";
                                    }, 200);
                                }
                            }
                        });
                    }else{
                        $17.alert(data.info);
                    }
                }
            });
        });
    });
</script>
<script type="text/html" id="T:joinTeacherGroupInner">
    <style>
        .t-checkGroup-mn-box{ font-size: 14px; color: #333; border: 1px dashed #61d6d8; width: 480px; margin: 0 auto -1px; background-color: #f2fcff; padding: 0 0 25px 0;}
        .t-checkGroup-mn-box .tips{ width: 65px; height: 30px; background-color: #ff9789; color: #fff; position: relative; left: -1px; top: -1px; display: inline-block; text-align: center; line-height: 30px;margin-right: 10px;}
        .t-checkGroup-mn-box .want-tab{font-size: 12px; float: right;line-height: 30px; margin-right: 20px; color: #00b4f3;}
        .t-checkGroup-mn-box .tips-blue{ background-color: #85dcf9;}
        .t-checkGroup-mn-box .tips-green{ background-color: #8fe874;}
        .t-checkGroup-mn-box .cp{ text-align: center; padding: 14px 0;}
        .t-checkGroup-mn-box ul{ overflow:hidden; *zoom: 1; padding:30px 0 10px 33px;}
        .t-checkGroup-mn-box ul li{ width: 106px; text-align: left; padding-bottom: 14px; float: left; cursor: pointer; overflow: hidden; white-space: nowrap; text-overflow: ellipsis;}
        .t-checkGroup-mn-box ul li.active .w-radio-3-current{ background-position: -48px -52px;}
        .t-checkGroup-mn-box .foot-info{ color: #f98972; font-size: 12px; text-align: center;}
        .t-checkGroup-mn-box .join-btn{ font-size: 14px; background-color: #f47766;color: #fff; display: block; width: 135px; height: 40px;text-align: center;line-height: 40px; margin: 20px auto 0;border-radius: 15px;}
        .t-checkGroup-notice{ font-size: 12px;line-height: 18px;  margin-bottom: 12px;margin-left: 5px; }
        .t-checkGroup-notice p{text-indent: 2em;}
    </style>
    <%var rotate = false%>
    <%for(var i = 0; i < data.length; i++){%>
        <%if(data[i].rotate){%>
            <%rotate = true%>
        <%}%>
    <%}%>
    <%if(rotate){%>
        <div class="t-checkGroup-notice">
            <div>亲爱的同学们：</div>
            <p>
                班级小组长开始报名啦！如果你是个活跃的小伙伴，就来报名参与吧~建立自己
                的小组，督促同学加入小组做作业，老师会奖励你们组哦~<a href="javascript: void(0);">更多学豆、更多荣誉</a>在等
                着你~
            </p>
        </div>
    <%}%>
    <%for(var i = 0; i < data.length; i++){%>
        <%if(true){%>
            <%var subjectName = "英语", tipsBackClass="", leaders = data[i].leaders
                if(data[i].subject == "MATH"){subjectName = "数学"; tipsBackClass="tips-blue";}
                if(data[i].subject == "CHINESE"){subjectName = "语文"; tipsBackClass="tips-green";}
            %>
            <%var remainder = data[i].total - leaders.length; remainder = (remainder <= 0 ? 0 : remainder)%>
                <div>
                    <#--加入小组长-->
                    <div class="t-checkGroup-mn-box" style="display: <%=( data[i].rotate ? 'none' : 'block')%>;">
                        <span class="tips <%=tipsBackClass%>"><%=subjectName%></span>
                        <%if(data[i].rotate){%>
                            <a  href="javascript: void(0);" class="js-wantGrouper want-tab" data-subject="<%=data[i].subject%>">我要当<%=subjectName%>小组长</a>
                        <%}%>
                        <%if(data[i].leaders.length > 0 || remainder == 0){%>
                            <#--<p class="cp"><%=subjectName%>老师为你们班分了<%=subjectName%>小组，请选择老师为你指定的小组长！</p>-->
                            <ul>
                                <%for(var d = 0; d < leaders.length; d++){%>
                                <li data-id="<%=leaders[d].tinyGroupId%>" data-subject="<%=data[i].subject%>" class="v-groupInnerSelect" style="width: 140px;"><i class="w-spot w-radio-3-current"></i><%=(leaders[d].tinyGroupName ? leaders[d].tinyGroupName : '未命名组')%>(<%=leaders[d].name ? leaders[d].name : leaders[d].id%>)</li>
                                <%}%>
                            </ul>
                            <%if(remainder==0){%>
                                <div class="foot-info">选择组长姓名，点击“确定”加入小组</div>
                            <%}else{%>
                                <div class="foot-info">小提示：选择后就不可以更换了哦！</div>
                            <%}%>
                        <%}else{%>
                            <div style="text-align: center; line-height: 100px;"><%=subjectName%>老师还没有为你们班分小组，快去请老师分组吧！</div>
                        <%}%>
                    </div>
                    <#--申请小组长-->
                    <div class="t-checkGroup-mn-box" style="display: <%=(data[i].rotate ? 'block' : 'none')%>;">
                        <span class="tips <%=tipsBackClass%>"><%=subjectName%></span>
                        <span class="num">本周剩余名额<%=remainder%>人</span>
                        <%if(leaders.length > 0 && remainder > 0){%>
                            <a href="javascript: void(0);" class="js-wantGrouper want-tab"  data-subject="<%=data[i].subject%>">我要加入小组</a>
                        <%}%>
                        <%if(remainder > 0){%>
                        <a  href="javascript: void(0);" class="wantGroupLeader-button join-btn" data-text="<%=subjectName%>">我要当<%=subjectName%>小组长</a>
                        <%}%>
                        <span class="groupId" style="display: none;"><%=data[i].groupId%></span>
                        <%if(leaders.length > 0){%>
                            <%if(remainder <= 0){%>
                            <ul>
                                <%for(var d = 0; d < leaders.length; d++){%>
                                <li data-id="<%=leaders[d].tinyGroupId%>" data-subject="<%=data[i].subject%>" class="v-groupInnerSelect" style="width: 140px;"><i class="w-spot w-radio-3-current"></i><%=(leaders[d].tinyGroupName ? leaders[d].tinyGroupName : '未命名组')%>(<%=leaders[d].name ? leaders[d].name : leaders[d].id%>)</li>
                                <%}%>
                            </ul>
                            <p class="hasCheckedStudents" style="text-align: center;  margin-top: 22px;">选择组长姓名，点击“确认”加入小组！</p>
                            <%}else{%>
                                <p class="hasCheckedStudents" style="text-align: center;  margin-top: 22px;">
                                    <span>已报名同学：</span>
                                    <%for(var d = 0; d < leaders.length; d++){%>
                                    <span style="display: inline-block; margin-right: 10px;"><%=leaders[d].name ? leaders[d].name : leaders[d].id%></span>
                                    <%}%>
                                </p>
                            <%}%>
                        <%}else{%>
                            <p class="hasCheckedStudents" style="text-align: center;  margin-top: 22px;">还没有同学当选小组长，快来报名吧！</p>
                        <%}%>
                        <div class="foot-info">小组长报名<span style='font-size: 10px'>（每周一开启）</span></div>
                    </div>
                </div>
            <#--<%}%>-->
        <%}%>
    <%}%>

</script>
</#macro>