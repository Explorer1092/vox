<#--新老师-->
<#macro notAuto>
    <@sugar.capsule js=["flexslider"] css=["plugin.flexslider"] />
    <@app.css href="public/skin/teacherv3/css/mentor.css" />
    <div class="right-consult-default">
        <a href="javascript:void(0);" class="js-clickRightConsult zx-teacher" title="遇到问题点我帮你"></a>
        <div class="yesMentor-box" style="display: none;">
            <dl class="js-clickRightConsult" style="cursor: pointer;">
                <dt class="at"></dt>
                <dd class="name"></dd>
            </dl>
            <a href="javascript:void(0);" class="js-clickRightConsult w-btn w-btn-mini" title="咨询老师">咨询老师</a>
        </div>
    </div>
    <script type="text/javascript">
        $(function(){
            var teacherItemBox  =$("#teacherItemBox");
            var tempMentorContentData = {};
            //初始化menter
            getMentor();
            function getMentor(){
                $.get("/teacher/mentor/mentor.vpage", {}, function(data){
                    tempMentorContentData = data;
                    if(data.success && data.mentor){
                        $(".yesMentor-box").show().siblings().hide();
                        if(data.mentor.img){
                            $(".yesMentor-box .at").html("<img src='<@app.avatar href="/"/>"+ data.mentor.img +"'/>");
                        }else{
                            $(".yesMentor-box .at").html("<img src='<@app.avatar href=""/>'/>");
                        }
                        $(".yesMentor-box .name").html(data.mentor.name);
                    }
                });
            }

            //有问题点击咨询
            $(document).on({
                click: function(){
                    var data = tempMentorContentData;
                    //加载老师列表
                    if(data.success){
                        if(data.mentor){
                            $.prompt( template("T:已选择认证老师", {item: data.mentor}) , {
                                title: "选择成功！",
                                buttons: {},
                                position: {width: 700}
                            });
                        }else{
                            if(data.mentorList.length < 1){
                                window.open('/redirector/onlinecs_new.vpage?type=teacher&question_type=question_other_pt','','width=856,height=519');
                                return false;
                            }

                            $.prompt( template("T:认证老师列表", { item : data.mentorList }) , {
                                title: "Hi~我们是认证老师，点我可以帮助你！",
                                buttons: {},
                                position: {width: 700}
                            });

                            //flexsbider滚动
                            setTimeout(function(){
                                $(".js-chooseAutoList").flexslider({
                                    animation : "slide",
                                    animationLoop : true,
                                    slideshow : false,
                                    slideshowSpeed: 4000, //展示时间间隔ms
                                    animationSpeed: 400, //滚动时间ms
                                    itemWidth : 80,
                                    direction : "horizontal",//水平方向
                                    minItems : 5,
                                    maxItems : 5,
                                    move : 5
                                });
                            },200);
                        }
                    }
                }
            }, '.js-clickRightConsult');

            var recordMentorId;
            //点选老师
            $(document).on({
                click : function(){
                    var $this = $(this);
                    $this.addClass("active").siblings().removeClass("active");
                    $(".click-mentor-submit").show().siblings("p").hide();
                    recordMentorId = $this.attr("data-user-id");
                }
            }, ".click-select-mentor-teacher");

            $(document).on("click", ".click-mentor-submit a", function(){
                if(recordMentorId != null){
                    $.post("/teacher/mentor/choosementor.vpage", { mentorId : recordMentorId}, function(data){
                        if(data.success){
                            //成功
                            $.get("/teacher/mentor/mentor.vpage", {}, function(data) {
                                if (data.success) {
                                    if (data.mentor) {
                                        $.prompt( template("T:已选择认证老师", {item: data.mentor}) , {
                                            title: "选择成功！",
                                            buttons: {},
                                            position: {width: 700}
                                        });
                                        getMentor();
                                    }
                                }
                            });
                        }else{
                            $17.alert(data.info);
                        }
                    });
                }else{
                    alert("请选中老师！");
                }
            });

            //点击留言
            var $teacherId = null;
            var $teacherName = null;
            $(document).on("click", ".click-teacher-message", function(){
                var $this = $(this);
                    $teacherId = $this.attr("data-teacher-id");
                    $teacherName = $this.attr("data-teacher-name");

                $.prompt(template("T:message", {teacherId : $teacherId}), {
                    title: "给" + $teacherName + "老师留言",
                    buttons: {},
                    position: {width: 700},
                    loaded : function(){

                    }
                });
            });

            $(document).on("keyup", "#dataInfoMessage", function(){
                $(this).siblings(".init").hide();
            });

            $(document).on("click", ".click-send-message", function(){
                var contentId = $("#dataInfoMessage");

                if($17.isBlank(contentId.val())){
                    contentId.siblings(".init").show();
                    return false;
                }

                $.post("/teacher/mentor/mmnotice.vpage", {
                    receiverId : $teacherId,
                    payload : contentId.val()
                }, function(data){
                    if(data.success){
                        //发送成功;
                        $.prompt.close();
                        $17.alert("发送成功");
                    }else{
                        $17.alert(data.info);
                    }
                });
            });
        });
    </script>
    <#--//template-->
    <script type="text/html" id="T:message">
        <div style="padding: 0; text-align: center; float: right; margin: -20px -5px 0;">
            <a class="w-btn w-btn-small click-send-message" style="width: 120px;" href="javascript:void(0);" data-teacher-id="<%=teacherId%>">发送</a>
            <p style="padding-top: 10px;"><a class="w-blue" href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/message/index.vpage" target="_blank">查看发给我的消息</a></p>
        </div>
        <textarea name="message" class="w-int" id="dataInfoMessage" style="width: 507px; height: 150px;margin-top: -20px;border-radius: 8px;color:#8e9696;padding:10px 10px;" maxlength="140" placeholder="请提问..."></textarea>
        <div class="init" style="color: #f00; clear: both; padding: 4px 0 0; display: none;">请输入留言</div>
        <div style="clear: both; height: 20px;"></div>
    </script>
    <script type="text/html" id="T:认证老师列表">
        <div class="choose-teacherUsed-box" style=" margin: -40px -20px 0;">
            <div class="ra_list">
                <%if(item.length > 0){%>
                <#--<p class="title">Hi！我是已认证的老师，点击我，我就来帮你！</p>-->
                <#--<div id="homework_card_tab_box">-->
                    <#--<div class="icon icon_arrow_before icon_disable prevBtn" data-c_p_page="0"> < </div>-->
                    <#--<div class="icon icon_arrow_back nextBtn" data-c_n_page="1"> > </div>-->
                <#--</div>-->
                <div class="choose-teacherList-con flexslider js-chooseAutoList" >
                    <ul style="width: <%=item.length * 60%>px; position: relative;" id="homework_list_box" class="slides">
                        <%for(var i = 0; i < item.length; i++){%>
                        <li class="actor practice-block click-select-mentor-teacher" data-user-id="<%=item[i].id%>" data-user-name="<%=item[i].name%>" data-user-img="<%if(item[i].img){%><@app.avatar href="<%=item[i].img%>"/><%}else{%><@app.avatar href=""/><%}%>">
                            <i><img width="40" height="40" src="<%if(item[i].img){%><@app.avatar href="<%=item[i].img%>"/><%}else{%><@app.avatar href=""/><%}%>"></i>
                            <strong style="margin-top: 16px;"><%=item[i].name%></strong>
                            <%if(item[i].isAmbassador){%><span class="amb-icon" title="校园大使"></span><%}%>
                            <div class="selected-box"><div class="back"></div><span class="selected"></span></div>
                        </li>
                        <%}%>
                    </ul>
                    <div class="w-clear"></div>
                </div>
                <%}else{%>
                    <div style="padding: 20px 0; text-align: center;">本校暂无认证老师</div>
                <%}%>
            </div>
        </div>
        <div class="w-ag-center">
            <p style="line-height: 41px;">一起作业挺好的，推荐你使用！有任何问题可以随时联系我！</p>
            <div style="display: none;" class="click-mentor-submit"><a href="javascript:void(0);" class="w-btn w-btn-small">选择Ta</a></div>
        </div>
    </script>
    <script type="text/html" id="T:已选择认证老师">
        <div class="choose-teacherUsed-box" style=" margin: -40px -20px 0;">
            <div class="ra_list">
                <div class="dialog">
                    <p class="title">Hi！让我来帮助你吧，有任何问题可以随时来联系我！</p>
                    <span class="arrow">
                        ◆
                        <span class="arrow-in">◆</span>
                    </span>
                </div>

                <div class="choose-teacherList-con" style="width: 280px;">
                    <dl>
                        <dt>
                         <span class="actor" style="cursor: default;">
                            <i><img width="40" height="40" src="<%if(item.img){%><@app.avatar href="<%=item.img%>"/><%}else{%><@app.avatar href=""/><%}%>"></i>
                            <strong><%=item.name%>的电话：<%=item.mobile%></strong>
                        </span>
                        </dt>
                    </dl>
                </div>

            </div>
            <div style="text-align: center;" >
                <a href="javascript:void(0);" class="w-btn w-btn-small click-teacher-message" data-teacher-id="<%=item.id%>" data-teacher-name="<%=item.name%>">给TA发消息</a>
            </div>
        </div>
    </script>
    <script type="text/html" id="T:认证老师个人信息">
        <div class="choose-teacherHelp-alert" style=" margin: -40px -20px 0;">
            <div class="choose-teacherList-con">
                <dl>
                    <dt>
                 <span class="actor">
                    <i><img width="40" height="40" src="<%=userImg%>"></i>
                    <strong><%=userName%></strong>
                </span>
                    </dt>
                    <dd>
                        <p>Hi，让我来帮助你吧，有任何问题可以随时联系我！</p>
                    <span class="arrow">
                        ◆
                        <span class="arrow-in">◆</span>
                    </span>
                    </dd>
                </dl>
            </div>
            <div class="ra_list">
                <div class="t-pubfooter-btn">
                    <a href="javascript:void(0);" class="w-btn w-btn-small click-mentor-submit">给他发消息</a>
                </div>
            </div>
        </div>
    </script>
</#macro>

<#--校园大使模板-->
<#macro yesAuto>
<@app.css href="public/skin/teacherv3/css/mentor.css" />
<div class="w-base">
    <div class="w-base-title">
        <h3>我的阶段奖励</h3>
        <div class="w-base-ext">
            <span class="w-bast-ctn" style="color: #667284;">最多可同时帮助 <span class="w-orange">4</span> 名老师，帮助任务有效期 <span class="w-orange">10</span> 天。</span>
        </div>
    </div>
    <div class="w-base-container">
        <#--//start-->
            <div id="teacherItemBox"></div>
        <#--end//-->
    </div>
</div>

<script type="text/html" id="T:获取未认证老师">
    <div class="helped-teacher-box">
        <ul>
            <%for(var i = 0; i < item.length; i++){%>
                <%if(i < 4){%>
                <li>
                    <div class="ht-info">
                        <span class="im"><img src="<%if(item[i].userImg){%><@app.avatar href="<%=item[i].userImg%>"/><%}else{%><@app.avatar href=""/><%}%>" alt=""/></span>
                        <p><%=item[i].userName%>（<%=item[i].userId%>）</p>
                        <p>电话：<%=item[i].mobile%></p>
                    </div>
                    <#--进度类型-->
                    <%if(item[i].category == "MENTOR_AUTHENTICATION"){%>
                    <div class="helpedStep-teacher-progress">
                        <div class="stepflex stepflexSmall">
                            <dl class="<%if(item[i].login){%>done<%}else{%>error<%}%>">
                                <dt>
                                    <span class="s-num sf-icon"></span>
                                </dt>
                                <dd class="s-text">账号登录</dd>
                            </dl>
                            <dl class="<%if(item[i].clazzFlag){%>done<%}else{%><%if(item[i].login){%>error<%}%><%}%>">
                                <dt>
                                    <span class="s-num sf-icon"></span>
                                </dt>
                                <dd class="s-text">创建班级</dd>
                            </dl>
                            <dl class="<%if(item[i].studentLoginFlag){%>done<%}else{%><%if(item[i].clazzFlag){%>error<%}%><%}%>">
                                <dt>
                                    <span class="s-num sf-icon"></span>
                                </dt>
                                <dd class="s-text">无学生登录</dd>
                            </dl>
                            <dl class="<%if(item[i].hkFlag){%>done<%}else{%><%if(item[i].studentLoginFlag){%>error<%}%><%}%>">
                                <dt>
                                    <span class="s-num sf-icon"></span>
                                </dt>
                                <dd class="s-text">检查作业</dd>
                            </dl>
                            <dl class="last <%if(item[i].hkFlag){%>error<%}%>">
                                <dt>
                                    <span class="s-num sf-icon"></span>
                                </dt>
                                <dd class="s-text">完成认证</dd>
                            </dl>
                        </div>
                    </div>
                    <%}%>
                    <#--可指导TA邀请新生-->
                    <%if(item[i].category == "MENTOR_NEW_ST_COUNT"){%>
                        <div class="helpedStep-teacher-progress">
                            <div class="stepflex stepflexSmall" style="text-align: center; padding: 15px 0; line-height: 20px;">
                                <%if(item[i].stCount >= 0 && item[i].stCount < 30){%>
                                    还差<%=(30-item[i].stCount)%>名学生达到30人
                                <%}%>
                                <%if(item[i].stCount >= 30 && item[i].stCount < 60){%>
                                    还差<%=(60-item[i].stCount)%>名学生达到60人
                                <%}%>
                                <%if(item[i].stCount >= 60 && item[i].stCount < 90){%>
                                    还差<%=(90-item[i].stCount)%>名学生达到90人
                                <%}%>
                                <br/>（我的帮助奖励：100园丁豆）
                            </div>
                        </div>
                    <%}%>
                    <#--限时帮助：期末回馈计划-->
                    <%if(item[i].category == "MENTOR_TERM_END"){%>
                        <div class="helpedStep-teacher-progress">
                            <div class="stepflex stepflexSmall" style="text-align: center; padding: 15px 0; line-height: 20px;">
                                <%if(item[i].stCount >= 0 && item[i].stCount < 30){%>
                                    还差<%=(30-item[i].stCount)%>名学生达到30人
                                <%}%>
                                <%if(item[i].stCount >= 30 && item[i].stCount < 60){%>
                                    还差<%=(60-item[i].stCount)%>名学生达到60人
                                <%}%>
                                <%if(item[i].stCount >= 60 && item[i].stCount < 90){%>
                                    还差<%=(90-item[i].stCount)%>名学生达到90人
                                <%}%>
                                <br/>（我的帮助奖励：100园丁豆）
                            </div>
                        </div>
                    <%}%>
                    <div class="ht-btn">
                        <a href="javascript:void(0);" class="w-btn w-btn-mini w-btn-mBlue click-teacher-message" data-teacher-id="<%=item[i].userId%>" data-teacher-name="<%=item[i].userName%>">留言</a>
                        <a href="javascript:void(0);" class="w-btn w-btn-mini w-border-blue <%=(item[i].category == 'MENTOR_AUTHENTICATION' ? 'click-help-ta' : 'click-help-kta')%>" data-category="<%=item[i].category%>" data-index="<%=i%>">帮助TA(剩<%=item[i].daysLeft%>天)</a>
                    </div>
                </li>
                <%}%>
            <%}%>
            <%if(item.length < 4){%>
            <li style="cursor: pointer;" class="click-see-detail">
                <div class="add-new-icon"></div>
                <div class="add-new-font">
                    你可在以下老师中<br/>选择要帮助的对象
                </div>
            </li>
            <%}%>
        </ul>
    </div>
</script>
<script type="text/html" id="T:单个老师信息">
    <div class="helpTa-step-alert">
        <div class="hs-info">
            <span class="h-img"><img src="<%if(item.userImg){%><@app.avatar href="<%=item.userImg%>"/><%}else{%><@app.avatar href=""/><%}%>" alt=""/></span>
            <span><%=item.userName%>（ID:<%=item.userId%>）</span>
            <span>电话：<%=item.mobile%></span>
        </div>
        <div class="hs-schedule">
            <span class="title">TA的进度</span>
            <div class="helpedStep-teacher-progress">
                <div class="stepflex">
                    <dl class="<%if(item.login){%>done<%}else{%>error<%}%>">
                        <dt>
                            <span class="s-num sf-icon"></span>
                        </dt>
                        <dd class="s-text">账号登录</dd>
                    </dl>
                    <dl class="<%if(item.clazzFlag){%>done<%}else{%><%if(item.login){%>error<%}%><%}%>">
                        <dt>
                            <span class="s-num sf-icon"></span>
                        </dt>
                        <dd class="s-text">创建班级</dd>
                    </dl>
                    <dl class="<%if(item.studentLoginFlag){%>done<%}else{%><%if(item.clazzFlag){%>error<%}%><%}%>">
                        <dt>
                            <span class="s-num sf-icon"></span>
                        </dt>
                        <dd class="s-text">无学生登录</dd>
                    </dl>
                    <dl class="<%if(item.hkFlag){%>done<%}else{%><%if(item.studentLoginFlag){%>error<%}%><%}%>">
                        <dt>
                            <span class="s-num sf-icon"></span>
                        </dt>
                        <dd class="s-text">检查作业</dd>
                    </dl>
                    <dl class="last <%if(item.hkFlag){%>error<%}%>">
                        <dt>
                            <span class="s-num sf-icon"></span>
                        </dt>
                        <dd class="s-text">完成认证</dd>
                    </dl>
                </div>
            </div>
        </div>
        <div class="hs-do">
            <ul>
                <li>
                    <p>我可以做：</p>
                </li>
                <li>
                    <p>提醒TA登录</p>
                    <p><span class="info">账号是ID和手机</span></p>
                </li>
                <li>
                    <p>帮TA建班</p>
                    <p><a class="w-btn w-btn-mini w-border-blue click-create-clazz <%if(item.clazzFlag){%>w-btn-disabled<%}%>" data-teacher-mhid="<%=item.mhid%>" href="javascript:void (0);">添加班级</a></p>
                </li>
                <#--<li>-->
                    <#--<p>帮TA下载学生账号</p>-->
                    <#--<p><a class="w-btn w-btn-mini w-border-blue click-down-clazz <%if(item.studentLoginFlag || !item.clazzFlag){%>w-btn-disabled<%}%>" data-teacher-mhid="<%=item.mhid%>" href="javascript:void (0);">下载</a></p>-->
                <#--</li>-->
                <li>
                    <p>提醒TA检查</p>
                    <p><a class="w-btn w-btn-mini w-border-blue click-send-remind <%if(item.hkFlag || !item.studentLoginFlag){%>w-btn-disabled<%}%>" data-type="homework" data-teacher-id="<%=item.userId%>" href="javascript:void (0);">提醒</a></p>
                </li>
                <li>
                    <p>告诉TA认证条件</p>
                    <p><a class="w-btn w-btn-mini w-border-blue click-send-remind <%if(!item.hkFlag){%>w-btn-disabled<%}%>" data-type="certification" data-teacher-id="<%=item.userId%>" href="javascript:void (0);">提醒</a></p>
                </li>
            </ul>
        </div>
    </div>
</script>
<script type="text/html" id="T:单个老师信息-获取奖励">
    <div class="helpTa-step-alert">
        <div class="hs-info">
            <span class="h-img"><img src="<%if(item.userImg){%><@app.avatar href="<%=item.userImg%>"/><%}else{%><@app.avatar href=""/><%}%>" alt=""/></span>
            <span><%=item.userName%>（ID:<%=item.userId%>）</span>
            <span>电话：<%=item.mobile%></span>
        </div>
        <div class="hs-schedule">
            <span class="title">TA的情况</span>
            <div class="helpedStep-teacher-progress">
                <div class="stepflex">
                    <dl class="<%if(item.stCount >= 30){%>done<%}else{%>error<%}%>" style="width: 110px;">
                        <dt>
                            <span class="s-num sf-icon"></span>
                        </dt>
                        <dd class="s-text" style="width: 110px; left: -54px;">30人<%if(item.stCount < 30){%>(当前<%=item.stCount%>人)<%}%></dd>
                    </dl>
                    <dl class="<%if(item.stCount >= 60){%>done<%}else{%><%if(item.stCount >= 30 && item.stCount < 60){%>error<%}%><%}%>" style="width: 110px;">
                        <dt>
                            <span class="s-num sf-icon"></span>
                        </dt>
                        <dd class="s-text" style="width: 110px; left: -54px;">60人<%if(item.stCount >= 30 && item.stCount < 60){%>(当前<%=item.stCount%>人)<%}%></dd>
                    </dl>
                    <dl class="last <%if(item.stCount >= 90){%>done<%}else{%><%if(item.stCount >= 60 && item.stCount < 90){%>error<%}%><%}%>">
                        <dt>
                            <span class="s-num sf-icon"></span>
                        </dt>
                        <dd class="s-text" style="width: 110px; left: -54px;">90人<%if(item.stCount >= 60 && item.stCount < 90){%>(当前<%=item.stCount%>人)<%}%></dd>
                    </dl>
                    <%if(item.stCount < 90){%>
                        <div style="float: right; color: #4e5656; padding: 10px 20px 0 0;">还需要 <span class="w-orange"><%=(90-item.stCount)%></span> 名新认证学生</div>
                    <%}%>
                </div>
            </div>
        </div>
        <div class="hs-do">
            <ul>
                <li>
                    <p>我可以做：</p>
                </li>
                <li style="text-align: left;">
                    <p>1、提升现有班级的使用人数(目前共<%=item.clazzCount%>个班级，共<%=item.stCount%>人)</p>
                    <p>2、是否有其他的在教班级还未使用？</p>
                </li>
            </ul>
        </div>
    </div>
</script>
<script type="text/html" id="T:message">
    <div style="padding: 0; text-align: center; float: right;">
        <a class="w-btn w-btn-small click-send-message" style="width: 115px;" href="javascript:void(0);" data-teacher-id="<%=teacherId%>">发送</a>
        <p style="padding-top: 10px;"><a class="w-blue" href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/message/index.vpage" target="_blank">查看发给我的消息</a></p>
    </div>
    <textarea name="message" class="w-int" id="dataInfoMessage" style="width: 300px; height: 70px;" maxlength="140" >.</textarea>
    <div class="init" style="color: #f00; clear: both; padding: 4px 0 0; display: none;">请输入留言</div>
    <div style="clear: both; height: 20px;"></div>
</script>
<script type="text/javascript">
    $(function(){
        var teacherItemBox  =$("#teacherItemBox");
        var recordTempData = [];
        //加载老师列表
        $.get("/teacher/mentor/mentoringlist.vpage", {}, function(data){
            if(data.success){
                recordTempData = data.teacherList;
                teacherItemBox.html( template("T:获取未认证老师", { item : data.teacherList }) );
            }
        });

        //点击帮助TA - 查看详情进度
        $(document).on("click", ".click-help-ta", function(){
            if(recordTempData.length > 0){
                var $index = $(this).attr("data-index");
                if($17.isBlank($index)){
                    return false;
                }
                $.prompt( template("T:单个老师信息", {
                    item : recordTempData[parseInt($index)]
                }), {
                    title: "帮助TA",
                    buttons: {},
                    position: {width: 620}
                });
            }
        });

        //点击帮助TA - 查看详情进度
        $(document).on("click", ".click-help-kta", function(){
            if(recordTempData.length > 0){
                var $index = $(this).attr("data-index");
                if($17.isBlank($index)){
                    return false;
                }
                $.prompt( template("T:单个老师信息-获取奖励", {
                    item : recordTempData[parseInt($index)]
                }), {
                    title: "帮助TA",
                    buttons: {},
                    position: {width: 620}
                });
            }
        });


        //点击留言
        var $teacherId = null;
        var $teacherName = null;
        $(document).on("click", ".click-teacher-message", function(){
            var $this = $(this);
                $teacherId = $this.attr("data-teacher-id");
                $teacherName = $this.attr("data-teacher-name");

            $.prompt(template("T:message", {teacherId : $teacherId}), {
                title: "给" + $teacherName + "老师留言",
                buttons: {},
                position: {width: 700},
                loaded : function(){

                }
            });
        });

        $(document).on("keyup", "#dataInfoMessage", function(){
            $(this).siblings(".init").hide();
        });

        $(document).on("click", ".click-send-message", function(){
            var contentId = $("#dataInfoMessage");

            if($17.isBlank(contentId.val())){
                contentId.siblings(".init").show();
                return false;
            }

            $.post("/teacher/mentor/mmnotice.vpage", {
                receiverId : $teacherId,
                payload : contentId.val()
            }, function(data){
                if(data.success){
                    //发送成功;
                    $.prompt.close();
                    $17.alert("发送成功");
                }else{
                    $17.alert(data.info);
                }
            });
        });

        //提醒
        $(document).on("click", ".click-send-remind", function(){
            var $this = $(this);
            var $teacherId = $this.attr("data-teacher-id");
            var $type = $this.attr("data-type");

            if($this.hasClass("w-btn-disabled") || $17.isBlank($teacherId) || $17.isBlank($type)){
                return false;
            }

            var $content = "提醒TA登录布置或检查作业 。";
            if($this.attr("data-type") == "certification"){
                $content = "认证条件：帮助 8名学生完成 3 次作业。";
            }

            //提醒成功
            $.post("/teacher/mentor/mmnoticehwc.vpage", {
                receiverId : $teacherId,
                type : $type
            }, function(data){
                if(data.success){
                    //发送成功;
                    $.prompt.close();
                    $17.alert("<p style='font-size: 18px; padding: 0 0 15px; font-weight: bold;'>已发送消息！</p>"+$content);
                }else{
                    $17.alert(data.info);
                }
            });
        });

        //添加班级
        $(document).on("click", ".click-create-clazz", function(){
            var $this = $(this);
            teacherClazzMhId = $this.attr("data-teacher-mhid");

            if($this.hasClass("w-btn-disabled") || $17.isBlank(teacherClazzMhId)){
                return false;
            }
            $("html, body").animate({ scrollTop: 0 }, 200);
            $.prompt.close();

            $("#createShowTips").show();
            $(".w-opt-back").show();
        });

        //下载班级名单
//        $(document).on("click", ".click-down-clazz", function(){
//            var $this = $(this);
//            var $mhId = $this.attr("data-teacher-mhid");
//
//            if($this.hasClass("w-btn-disabled")){
//                return false;
//            }
//
//            //提醒成功
//            location.href = "/teacher/mentor/mmbatchdownload.vpage?mhid="+$mhId;
//
//            return false;
//        });

        //取消添加班级
        $(document).on("click", ".v-cancel", function(){
            $.prompt.close();
            $("#createShowTips").hide();
            $(".w-opt-back").hide();
            schoolLength_click();
        });
    });
</script>
<div id="createShowTips" style="display: none;">
    <div class="w-opt-back-content" style=" position: absolute; top: 20%; left: 50%; margin-left: -370px;  width: 740px;">
        <#include "../invite/mentorcreateclazz.ftl"/>
    </div>
</div>
</#macro>
