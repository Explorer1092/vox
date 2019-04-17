<script type="text/html" id="T:首页动态">
<%if(item.length > 0){%>
    <%for(var i = 0; i < item.length; i++){%>
    <%if(item[i].latestType != "CHECK_HOMEWORK_STUDY_MASTER" && item[i].latestType != "TINY_GROUP_STAR"){%>
    <dl class="t-dynamic-module">
        <dt class="t-dynamic-avatar">
            <#--老师认证-->
            <%if(item[i].latestType == "TEACHER_CERTIFICATED"){%>
                <img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>"/>
            <%}%>
            <#--礼物-->
            <%if(item[i].latestType == "GIFT_S_T"){%>
                <span class="avatar-icon avatar-icon-1"></span>
            <%}%>
            <#--检查作业得园丁豆-->
            <%if(item[i].latestType == "CHECK_HOMEWORK_INTEGRAL"){%>
                <img src="<%if(item[i].users[0].img){%><@app.avatar href='<%=item[i].users[0].img%>'/><%}else{%><@app.avatar href=''/><%}%>" width="25"/>
            <%}%>
            <#--鲜花排行榜-->
            <%if(item[i].latestType == "FLOWER_RECEIVED"){%>
                <span class="avatar-icon avatar-icon-6"></span>
            <%}%>
            <#--本校新注册老师-->
            <%if(item[i].latestType == "NEW_REGISTER_TEACHER"){%>
            <span class="avatar-icon avatar-icon-4"></span>
            <%}%>
            <#--教师节-->
            <%if(item[i].latestType == "TEACHERS_DAY"){%>
            <span class="avatar-icon avatar-icon-8"></span>
            <%}%>
            <#--小组之星-->
            <%if(item[i].latestType == "TINY_GROUP_STAR"){%>
            <span class="avatar-icon avatar-icon-9"></span>
            <%}%>
        </dt>
        <dd class="t-dynamic-infoBox">
            <div class="dynamic-style">
                <#--本校新注册老师-->
                <%if(item[i].latestType == "NEW_REGISTER_TEACHER"){%>
                    <div class="dynamic-style-title">本校新注册老师</div>
                    <div class="dynamic-style-title" style="font-size: 14px;">今日本校新注册老师：</div>
                    <%for(var d = 0, details = item[i].details; d < details.length; d++){%>
                        <div class="dynamic-style-content">
                            <span style="float: right; padding: 0 20px 0 0;">
                                <a href="javascript:void(0);" class="w-blue data-forTaCertification" data-id="<%=details[d].userId%>" data-category="MENTOR_AUTHENTICATION" data-name="<%=details[d].userName%>">帮助TA认证</a>
                                <a href="javascript:void(0);" class="w-blue data-cancelAuth" data-type="1"  data-userid="<%=details[d].userId%>" data-username="<%=details[d].userName%>" style="margin-left: 20px;">举报不是本校老师</a>
                            </span>
                            <span class="w-blue"><%=details[d].userName%></span><%=details[d].userSubject%>老师
                        </div>
                    <%}%>
                <%}%>

                <#--鲜花排行榜-->
                <%if(item[i].latestType == "FLOWER_RECEIVED"){%>
                    <div class="dynamic-style-title">您今日收到<%=item[i].details[0].flowerCount%>朵鲜花</div>
                    <div class="dynamic-style-content dynamic-template-flower">
                        <div class="flower-icon">
                            <p class="flower-count">×<%=item[i].details[0].flowerCount%></p>
                            <#if (!currentTeacherWebGrayFunction.isAvailable("Flower", "Close"))!true>
                            <a onclick="$17.voxLog({module: 'flower_pc',op:'see_rank_button_click'});" href="/teacher/flower/exchange.vpage?ref=flower" target="_blank">
                                <span class="ct-back"></span>
                                <span class="ct-btn">查看鲜花</span>
                            </a>
                            </#if>
                        </div>
                        <p class="flower-content">
                            <%if((item[i].details[0].studentNames != undefined) && (item[i].details[0].studentNames.length > 0)){%>
                                <%for(var m = 0 ; m < item[i].details[0].studentNames.length ; m++){ %>
                                    <%=item[i].details[0].studentNames[m]%>
                                <% } %>
                            <%}%>的家长查收了作业，并感谢您的辛勤工作！
                        </p>
                        <div class="flower-btn">
                            <%if(!true){%>
                            <a style="background-color: #e1f0fc; border-color: #abc1d3;" class="w-btn w-btn-mBlue w-btn-mini thanksBut_flower disabled" href="javascript:void (0);">已感谢</a>
                            <%}else{%>
                            <a data-flower="${(currentUser.profile.realname)!}老师感谢家长们赠送的<%=item[i].details[0].flowerCount%>朵鲜花，感谢家长们对孩子教育的关心与支持" data-activity_date="<%=item[i].details[0].activityDate%>" style="background-color: #e1f0fc; border-color: #abc1d3;" class="w-btn w-btn-mBlue w-btn-mini thanksBut_flower" href="javascript:void (0);">感谢家长</a>
                            <%}%>
                        </div>
                    </div>
                <%}%>

                <#--老师认证-->
                <%if(item[i].latestType == "TEACHER_CERTIFICATED"){%>
                    <div class="dynamic-style-content">
                        同校
                        <%for(var d = 0, users = item[i].users; d < users.length; d++){%>
                        <span class="w-blue"><%=(users[d].name ? users[d].name : users[d].id)%></span>
                        <%}%>
                        老师刚刚通过认证加入一起作业大家庭，请各位老师多多支持哦！
                    </div>
                    <%for(var d = 0, details = item[i].details; d < details.length; d++){%>
                        <%if(details[d].phoneFee || details[d].gold){%>
                            <div class="dynamic-style-content">
                                Ta是<%=(details[d].inviterName ? details[d].inviterName : details[d].inviterId)%>老师邀请的用户，<%=(details[d].inviterName ? details[d].inviterName : details[d].inviterId)%>
                                老师因此获得
                                <%if(details[d].phoneFee){%>
                                    <%=details[d].phoneFee%>话费
                                <%}%>
                                <%if(details[d].gold){%>
                                    <%if(details[d].phoneFee){%>、<%}%><%=details[d].gold%>园丁豆奖励
                                <%}%>
                            </div>
                        <%}%>
                    <%}%>
                <%}%>

                <#--礼物-->
                <%if(item[i].latestType == "GIFT_S_T"){%>
                    <div class="dynamic-style-content">
                        今日
                        <%for(var d = 0, users = item[i].users; d < users.length; d++){%>
                            <%if(d < 3){%>
                        <span class="w-blue"><%=(users[d].name ? users[d].name : users[d].id)%></span><%if(d < 2 && d < users.length - 1){%>、<%}%>
                            <%}%>
                        <%}%>
                        <%if(item[i].users.length > 2){%>等<%}%><%=item[i].users.length%>名同学给您赠送礼物
                    </div>
                    <%for(var d = 0, details = item[i].details; d < details.length; d++){%>
                    <div class="rank-list rank-<%=d%> <%=( d > 2 ? '' : 'rank-show')%>" <%if(d > 2){%>style="display:none;"<%}%>>
                        <div class="dynamic-style-content">
                            <%=(details[d].userName ? details[d].userName : details[d].userId)%> 同学赠送礼物：
                            "<%=details[d].giftName ? details[d].giftName : ''%>"
                        </div>
                    </div>
                    <%}%>
                <%}%>

                <#--检查作业得园丁豆-->
                <%if(item[i].latestType == "CHECK_HOMEWORK_INTEGRAL"){%>
                    <div class="dynamic-style-content">
                        <%for(var d = 0, users = item[i].users; d < users.length; d++){%>
                            <%if(d < 3){%>
                                <span class="w-blue"><%=(users[d].name ? users[d].name : users[d].id)%></span><%if(d < 2 && d < users.length - 1){%>、<%}%>
                            <%}%>
                        <%}%>
                        <%if(item[i].users.length > 2){%>等<%}%> <%=item[i].users.length%> 名老师今日登录网站并检查了作业
                    </div>
                    <%for(var d = 0, details = item[i].details; d < details.length; d++){%>
                    <div class="rank-list rank-<%=d%> <%=( d > 2 ? '' : 'rank-show')%>" <%if(d > 2){%>style="display:none;"<%}%>>
                        <div class="dynamic-style-content">
                            <%=(details[d].userName ? details[d].userName : details[d].userId)%>老师检查了<%=details[d].clazzName%>，<%=details[d].fhwStudentCount%>名学生完成作业。
                        </div>
                    </div>
                    <%}%>
                <%}%>
                <#--小组之星-->
                <#--<%if(item[i].latestType == "TINY_GROUP_STAR"){%>
                <div data-title="小组之星评选结果" style="margin-bottom: -20px;">
                    <style>
                        .groupStarResult{width: 660px;background-color: #f6f9fe;font-size: 14px; overflow: hidden;}
                        .groupStarResult .blue{color: #269ef8;}
                        .groupStarResult .grouper{display:block;width: 73px;height: 73px;margin: 4px auto; border: 2px solid #e0e2e5; overflow: hidden; border-radius: 100px;}
                        .groupStarResult .flexslider{ background: none; border: none; width: 470px; margin: 10px 0 0 25px; box-shadow: none;}
                        .groupStarResult .flexslider .flex-direction-nav a{ background: url("<@app.link href='public/skin/teacherv3/images/resultArrow.png'/>") no-repeat;width: 30px;height: 30px;display: block; top: 45px;}
                        .groupStarResult .flexslider .flex-direction-nav a.flex-prev{background-position: 0 0 ;}
                        .groupStarResult .flexslider .flex-direction-nav a.flex-prev:hover{background-position: 0 -30px;}
                        .groupStarResult .flexslider .flex-direction-nav a.flex-prev:visited{background-position: 0 -60px;}
                        .groupStarResult .flexslider .flex-direction-nav a.flex-next{background-position: -30px 0;}
                        .groupStarResult .flexslider .flex-direction-nav a.flex-next;hover{background-position: -30px -30px;}
                        .groupStarResult .flexslider .flex-direction-nav a.flex-next:visited{background-position:  -30px -60px;}
                        .groupStarResult .flexslider .flex-control-nav{ display: none;}
                        .groupStarResult .btn{width: 118px; position: absolute;top: 30px;right: 30px;}
                        .groupStarResult .resultLi{overflow: hidden;position: relative;}
                        .groupStarResult .more{ background-color: #eff2f6;}
                        .groupStarResult .more .w-turn-page-list{ padding: 0;}
                        .groupStarResult ul li{text-align: center;}
                    </style>
                    <div class="dynamic-style-title">小组之星评选结果</div>
                    <div class="dynamic-style-content">
                        <div class="groupStarResult" style="height: 200px; overflow: hidden;">
                            <%var groupsDetail = item[i].details[0]%>
                            <%for(var d = 0, clazzList = groupsDetail.detail; d < clazzList.length; d++){%>
                                <div data-type="list">
                                    <%if(clazzList[d].rotate){%>
                                    <p style="margin: 10px 0 10px 28px;">由于您没有任命小组长，学生主动报名并产生了上周的<span class="blue">小组之星</span>（作业完成度最好且最活跃的小组）</p>
                                    <%}else{%>
                                        <p style="margin: 10px 0 10px 28px;">以下是上周作业完成度最好、且最活跃的小组之星：</p>
                                    <%}%>
                                    <p style="margin: 0 0 0 28px;">上周  <span class="blue" ><%=clazzList[d].clazzName%></span>  的小组之星：<span class="blue" ><%=clazzList[d].tinyGroupName%></span></p>
                                    <div class="resultLi">
                                        <%if(clazzList[d].rotate){%>
                                            <div class="btn"><a href="${(ProductConfig.getUcenterUrl())!''}/teacher/clazz/tinygroup/editcrew.vpage?clazzId=<%=clazzList[d].clazzId%>" class="w-btn w-btn-small" style="width: 110px;">去任命小组</a></div>
                                        <%}else{%>
                                            <div class="btn"><a href="${(ProductConfig.getUcenterUrl())!''}/teacher/clazz/tinygroup/index.vpage?clazzId=<%=clazzList[d].clazzId%>" class="w-btn w-btn-small" style="width: 110px;">去任命小组</a></div>
                                        <%}%>
                                        <div class="flexslider js-groupStarResultList">
                                            <ul class="slides">
                                            &lt;#&ndash;<%var members = [{sname: "111", simg: ""}, {sname: "22", simg: ""}]%>&ndash;&gt;
                                            <%var members = clazzList[d].members%>
                                            <%for(var e = 0; e < members.length; e++){%>
                                                <li>
                                                    <div class="grouper">
                                                    <%if(members[e].simg){%>
                                                        <img src="<@app.avatar href='<%=members[e].simg%>'/>">
                                                    <%}else{%>
                                                        <img src="<@app.avatar href=''/>">
                                                    <%}%>
                                                    </div>
                                                    <span class="name"><%=members[e].sname%></span>
                                                </li>
                                            <%}%>
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                            <%}%>
                        </div>
                        <%if(groupsDetail.detail.length > 1){%>
                        <div class="t-show-box more js-groupShowList">
                            <div class="w-turn-page-list">
                                <a href="javascript:void (0)" >
                                    查看更多
                                    <span class="w-icon-arrow"></span>
                                </a>
                            </div>
                        </div>
                        <%}%>
                    </div>
                </div>
                <%}%>-->
            </div>

            <#--发布时间-->
            <div class="t-dynamic-last">
                <%if(item[i].users.length > 0 && (item[i].latestType == "GIFT_S_T" || item[i].latestType == "CHECK_HOMEWORK_INTEGRAL")){%>
                <p class="st-item">
                    <%for(var u = 0, users = item[i].users; u < users.length; u++){%>
                    <span class="l-at" title="<%=users[u].name%>"><img src="<%if(users[u].img){%><@app.avatar href='<%=users[u].img%>'/><%}else{%><@app.avatar href=''/><%}%>" width="25"/></span>
                    <%}%>
                    <span class="l-nm">参与</span>
                </p>
                <%}%>
                <span class="date-time"><%=item[i].date%></span>
                <%if( (item[i].latestType == "CHECK_HOMEWORK_STUDY_MASTER" && item[i].details.length > 1) || (item[i].latestType != "CHECK_HOMEWORK_STUDY_MASTER" && item[i].details.length > 3)){%>
                <div class="dynamic-style-show">
                    <a class="more" href="javascript:void (0);" data-title="展开更多">展开更多</a>
                </div>
                <%}%>
            </div>
        </dd>
    </dl>
    <%}%>
<%}%>
<%}else{%>
    <div style="text-align: center; padding: 80px 0; font-size: 16px; color: #999;">暂无动态</div>
<%}%>
</script>

<script type="text/html" id="T:选择原因">
    <div class="t-changeclass-alert">
        <div class="class">
            <div style="font-size: 12px; padding: 0 0 15px;">请选择举报原因，我们收到请求会认真核实情况，如果情况属实，将配合校园大使将该老师转出本校！</div>
            <ul class="data-selectContentList">
                <li data-val="非本校老师" class="active" style="cursor: pointer; width: 140px;">
                    <span class="w-radio w-radio-current"></span>
                    非本校老师
                </li>
                <li data-val="不是真实老师" style="cursor: pointer; width: 120px;">
                    <span class="w-radio"></span>
                    不是真实老师
                </li>
                <li data-val="该账号不再使用" style="cursor: pointer;width: 120px;">
                    <span class="w-radio"></span>
                    该账号不再使用
                </li>
            </ul>
        </div>
    </div>
</script>

<#--Template 加载-->
<@sugar.capsule js=["flexslider"] css=["plugin.flexslider"] />
<script type="text/javascript">
    $(function(){
        var dynamicMoreClickBtn = $("#dynamicMoreClickBtn");

        //获取最近动态
        var dyListPage = {
            currentPage : 1, //当前页
            currentTotalBar : 1, //共多少条
            currentTotalPages : 1 //共多少页面
        };

        loadLatestNews(1);

        //点击展开更从动态
        dynamicMoreClickBtn.on("click", function(){
            if(dyListPage.currentTotalPages > dyListPage.currentPage){
                loadLatestNews(dyListPage.currentPage + 1, "append");
            }
        });

        //点击展开更从动态
        $(document).on("click", ".dynamic-style-show a", function(){
            var $this = $(this);

            if($this.attr("data-title") == "展开更多"){
                $this.attr("data-title", "收起");
                $this.text("收起");
                $this.closest(".t-dynamic-infoBox").find(".rank-list").show();
            }else{
                $this.attr("data-title", "展开更多");
                $this.text("展开更多");
                $this.closest(".t-dynamic-infoBox").find(".rank-list").hide();
                $this.closest(".t-dynamic-infoBox").find(".rank-show").show();
            }
        });

        //送花-感谢家长
        $(document).on('click','.thanksBut_flower',function(){
            var $this = $(this);
            if($this.hasClass('disabled')){return false}
            var flower = $this.data('flower');
            var activity_date = $this.data('activity_date');
            $.prompt("<textarea id='thanksContent' maxlength='500' style='width: 450px; height: 126px; border-radius: 5px; border: 1px solid #606162; line-height: 33px;'>"+flower+"</textarea><p style='color: 747e8d;'>感谢将通过微信和家长通App的班级群发送给家长</p>",{
                title : '感谢家长',
                buttons : {"发送" : true},
                submit : function(e,v){
                    e.preventDefault();
                    if(v){
                        var thanksContent = $('#thanksContent');
                        if($17.isBlank(thanksContent.val())){thanksContent.focus(); return false;}
                        $.post('/teacher/flower/sendflowergratitude.vpage',{content :thanksContent.val(), activityDate : activity_date },function(data){
                            if(data.success){
                                $this.text('已感谢').addClass('disabled');
                                $.prompt.close();
                                $17.tongji('送花PC','首页动态','感谢家长-发送');
                                $17.voxLog({
                                    module: "flower_pc",
                                    op    : "thanks_parent_send_button_click"
                                });
                            }else{
                                $17.alert(data.info);
                            }
                        });
                    }
                }
            });
            $17.voxLog({
                module: "flower_pc",
                op    : "thanks_parent_button_click"
            });
        });

        //帮TA认证
        $(document).on("click", ".data-forTaCertification", function(){
            var $this = $(this);

            if( $17.isBlank($this.attr("data-category")) || $this.hasClass("w-btn-disabled")){
                return false;
            }

            $.post('/teacher/mentor/choosementee.vpage', {
                mentorCategory : $this.attr("data-category"),
                menteeId : $this.attr("data-id")
            }, function(data){
                if(data.success){
                    $.prompt("<div class='w-ag-center'>与该老师建立帮助关系成功</div>", {
                        title: "系统提示",
                        buttons: { "查看帮助详情" : true },
                        position: {width: 500},
                        submit : function(e, v){
                            if(v){
                                location.href = "/teacher/invite/activateteacher.vpage?type=HBYM";
                            }
                        }
                    });
                }else{
                    if(data.type){
                        var opt = {
                            title: "系统提示",
                            buttons: { "查看帮助详情" : true },
                            position: {width: 500},
                            submit : function(e, v){
                                if(v){
                                    location.href = "/teacher/invite/activateteacher.vpage?type="+data.type;
                                }
                            }
                        };

                        if(data.type == "HBYO"){
                            opt.buttons = { "去帮助其他老师" : true };
                        }

                        $.prompt("<div class='w-ag-center'>" + data.info + "</div>", opt);
                    }else{
                        $17.alert(data.info);
                    }
                }
            });
        });

        //申请取消该老师认证
        $(document).on("click", ".data-cancelAuth", function(){
            var selectContent = "非本校老师";
            var $this = $(this);
            var $dataType = $this.attr("data-type");

            if($17.isBlank($dataType)){
                return false;
            }

            $.prompt(template("T:选择原因", { dataType : $dataType}), {
                title: "系统提示",
                focus : 1,
                buttons: { "取消": false, "提交" : true},
                position:{width : 500},
                loaded : function(){
                    $(".data-selectContentList li").on("click", function(){
                        var $that = $(this);
                        $that.addClass("active").siblings().removeClass("active");
                        $that.find(".w-radio").addClass("w-radio-current");
                        $that.siblings().find(".w-radio").removeClass("w-radio-current");

                        selectContent = $that.attr("data-val");
                    });
                },
                submit : function(e, v){
                    if(v){
                        $.post("/teacher/invite/reportTeacher.vpage", {
                            type : $dataType,
                            teacherId : $this.attr("data-userid"),
                            teacherName : $this.attr("data-username"),
                            reason : selectContent
                        }, function(data){
                            $17.alert(data.info);
                        });
                    }
                }
            });
        });

        //动态公用
        function loadLatestNews(num, type){
            $.post("/teacher/teacherlatestnews.vpage", {currentPage : num}, function(data){
                if(data.success){
                    if(type == "append"){
                        $("#realTimeDynamic").append( template("T:首页动态", {item: data.lp.content}) );
                    }else{
                        $("#realTimeDynamic").html( template("T:首页动态", {item: data.lp.content}) );
                    }

//                    dyListPage.currentPage = data.lp.number;
                    dyListPage.currentTotalBar = data.lp.totalElements;
                    dyListPage.currentTotalPages = data.lp.totalPages;

                    if(dyListPage.currentTotalPages > num){
                        dynamicMoreClickBtn.show();
                    }else{
                        dynamicMoreClickBtn.hide();
                    }

                    groupStarResultList();
                }else{
                    $("#realTimeDynamic").html("<div style='text-align: center; padding: 50px;'>加载失败</div>");
                }
            });
        }

        //动态左右滚动
        function groupStarResultList(){
            setTimeout(function(){
                $(".js-groupStarResultList").each(function(){
                    $(this).flexslider({
                        animation : "slide",
                        animationLoop : true,
                        slideshow : false,
                        slideshowSpeed: 4000, //展示时间间隔ms
                        animationSpeed: 400, //滚动时间ms
                        itemWidth : 90,
                        direction : "horizontal",//水平方向
                        minItems : 4,
                        maxItems : 4,
                        move : 4
                    });
                });

                //show list
                $(document).on("click", ".js-groupShowList", function(){
                    $(this).hide().siblings().removeAttr("style");
                });
            }, 200);
        }
    });
</script>