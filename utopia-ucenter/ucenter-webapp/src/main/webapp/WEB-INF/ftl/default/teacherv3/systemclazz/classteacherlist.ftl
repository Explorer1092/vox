<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
    <div class="newSemesterRemind-con-box">
        <div id="ClassTeacherList"><#--clazz list--></div>
    </div>
    <script type="text/javascript">
        $(function(){
            LeftMenu.focus("clazzmanager");

            //班级列表
            var currentSelect = {
                clazzId : null,
                clazzName : null,
                teacherName : null,
                teacherId : null
            };

            //渲染班级列表
            var clazzList = ${clazzList!'[]'};
            var ClassTeacherList = $("#ClassTeacherList");
            ClassTeacherListInit();
            function ClassTeacherListInit(){
                ClassTeacherList.html( template("T:转班班级列表ClassTeacherList", {clazzList : clazzList}) );
            }

            //自动切换到当前班级
            if( !$17.isBlank($17.getQuery("currentClazzId")) ){
                setTimeout(function(){
                    $(".v-rollOut[data-clazz-id='"+ $17.getQuery("currentClazzId") +"']").click();
                }, 100);
            }

            //展开老师列表
            var authTeacher = ${authenticatedTeacherList!'[]'};
            var noAuthTeacher = ${unauthenticatedTeahcerList!'[]'};
            ClassTeacherList.on("click", ".v-rollOut", function(){
                var $this = $(this);
                var $parentsLi = $this.parents("li");

                //收起
                if($this.hasClass("dis")){
                    $this.removeClass("dis");
                    $parentsLi.find(".teacherListBox").html("").hide();
                    $parentsLi.find(".nt-class .nsc-arrow").html("▶");
                    $this.text($this.attr("title"));
                    return false;
                }

                currentSelect.teacherId = null;
                currentSelect.clazzName = $this.attr("data-clazz-name");
                currentSelect.clazzId = $this.attr("data-clazz-id");
                //渲染老师列表
                $this.addClass("dis");
                $parentsLi.siblings().find(".teacherListBox").html("").hide();
                $parentsLi.siblings().find(".nt-class .nsc-arrow").html("▶");
                $parentsLi.siblings().find(".v-rollOut").removeClass("dis");
                $parentsLi.siblings().each(function(){
                    var $eachLi = $(this).find(".v-rollOut");
                    $eachLi.text($eachLi.attr("title"));
                });

                $this.text("收起");
                $parentsLi.find(".nt-class .nsc-arrow").text("▼");
                $parentsLi.find(".teacherListBox").html(template("T:本校所有老师列表", {
                    clazzId : currentSelect.clazzId,
                    authTeacher : authTeacher,
                    noAuthTeacher: noAuthTeacher
                })).show();

                //是否在可见区
                setTimeout(function(){
                    flagPopupBox($(document).scrollTop(), ".teacherAutoRollOutClazz");
                });

                $("html, body").animate({ scrollTop: $this.offset().top - 22 }, 300);
            });

            //选择老师
            ClassTeacherList.on("click", ".v-selectTeacher", function(){
                var $this = $(this);

                currentSelect.teacherName = $this.attr("data-teacher-name");
                currentSelect.teacherId = $this.attr("data-teacher-id");
                $this.parents(".allTeacherList").find(".v-selectTeacher").removeClass("w-radio-current");
                $this.addClass("w-radio-current");
            });

            //查看更多老师
            ClassTeacherList.on("click", ".v-moreNoAuthTeacher", function(){
                var $this = $(this);
                $this.parents("dd").hide();
                $this.parents("dd").siblings(".noAuthTeacherList").show();

                $17.tongji("老师端-新转班页-查看更多老师-btn");
                //是否在可见区
                flagPopupBox($(document).scrollTop(), ".teacherAutoRollOutClazz");
            });

            //我还教
            ClassTeacherList.on("click", ".v-iAlsoTeach", function(){
                var $this = $(this);
                var $clazzId = $this.attr("data-clazz-id");

                if($17.isBlank($clazzId)){
                    return false;
                }

                $17.tongji("老师端-新转班页-我还教-btn");
                $.get("/teacher/clazz/alteration/remainthesame.vpage", {clazzId : $clazzId}, function(data){
                    if(data.success){
                        var $parentsLi = $this.parents("li");

                        clazzList[parseInt($parentsLi.attr("data-index"))].state = "RETAIN";
                        ClassTeacherListInit();
//                        $this.siblings(".w-btn").text("重新转班").attr("class", "w-gray v-rollOut").after('<a class="w-btn w-btn-well w-btn-disabled" style="cursor: default;" href="javascript:void (0);">已处理</a>');
//                        $this.remove();
                    }else{
                        $17.alert(data.info);
                    }
                });
            });

            //转给Ta(不想找)
            ClassTeacherList.on("click", ".v-notExistTeacher", function(){
                var $this = $(this);
                var newTeacherMobile = $this.parents("dl").find(".newTeacherMobile");
                var newTeacherName = $this.parents("dl").find(".newTeacherName");
                var $postData = {
                    clazzId : currentSelect.clazzId
                };

                if($17.isBlank(currentSelect.clazzId) || $this.hasClass("dis")){
                    return false;
                }

                if( $17.isBlank(newTeacherMobile.val()) && $17.isBlank(newTeacherName.val()) ){
                    newTeacherMobile.addClass("w-int-error");
                    newTeacherName.addClass("w-int-error");
                    return false;
                }

                newTeacherName.removeClass("w-int-error");
                newTeacherMobile.removeClass("w-int-error");

                if( !$17.isBlank(newTeacherMobile.val()) && !$17.isMobile(newTeacherMobile.val()) ){
                    newTeacherMobile.addClass("w-int-error");
                    return false;
                }

                if( !$17.isBlank(newTeacherName.val()) ){
                    $postData.name = newTeacherName.val();
                }

                if( !$17.isBlank(newTeacherMobile.val()) ){
                    $postData.mobile = newTeacherMobile.val();
                }

                $17.tongji("老师端-新转班页-不想找-转给Ta-btn");
                $this.addClass("dis");
                $.post("/teacher/clazz/alteration/findteacher.vpage", $postData, function(data){
                    var $parentsLi = $this.parents("li");
                    if(data.success){
                        if( data.type == "TEACHER_FOUND"){
                            currentSelect.teacherName = null;
                            currentSelect.teacherId = null;
                            $("html, body").animate({ scrollTop: $this.parents("li").offset().top - 22 }, 300);
                            $parentsLi.find(".teacherListBox").html(template("T:returnTeacherList", {teachersList : data.teachers})).show();
                        }

                        if( data.type == "NO_TEACHER_FOUND"){
                            $17.alert("未找到此老师！请填写姓名或更换一个手机号。");
                        }

                        if( data.type == "CREATE"){
                            $("html, body").animate({ scrollTop: $this.parents("li").offset().top - 22 }, 300);
                            clazzList[parseInt($parentsLi.attr("data-index"))].state = "HANDOVER";
                            $parentsLi.find(".teacherListBox").html("<div class='newSemesterRemind-title-box'><p>此用户未使用一起作业，系统已创建账号发送至ta的手机，请提醒ta来登录接管吧！</p><div class='nsc-btn'><a class='w-btn w-btn-small v-closePopupBox' href='javascript:void (0);'>知道了</a></div></div>").show();
                        }
                    }else{
                        $parentsLi.find(".teacherListBox").html("<div class='newSemesterRemind-title-box'><p>"+data.info+"</p><div class='nsc-btn'><a class='w-btn w-btn-small v-closePopupBox' href='javascript:void (0);'>知道了</a></div></div>").show();
                    }
                    $this.removeClass("dis");
                });
            });

            //转给Ta本校已有老师
            ClassTeacherList.on("click", ".v-existTeacher", function(){
                var $this = $(this);

                if($17.isBlank(currentSelect.clazzId)){
                    return false;
                }

                if($17.isBlank(currentSelect.teacherId)){
                    $17.alert("请选择老师");
                    return false;
                }

                $.prompt("<div class='w-ag-center'>您确定将“"+currentSelect.clazzName+"”转给“"+currentSelect.teacherName+"”老师吗？</div>", {
                    focus: 1,
                    title: "系统提示",
                    buttons: { "取消": false, "确定": true },
                    position: {width: 500},
                    submit : function(e, v){
                        if(v){
                            $17.tongji("老师端-新转班页-已经有老师-转给Ta-btn");
                            $.get("/teacher/clazz/alteration/sendhandoverapplication.vpage", {clazzId : currentSelect.clazzId, teacherId : currentSelect.teacherId}, function(data){
                                if(data.success){
//                                    location.href = "/teacher/clazz/alteration/classteacherlist.vpage?currentClazzId="+currentSelect.clazzId;
                                    var $parentsLi = $this.parents("li");
                                    clazzList[parseInt($parentsLi.attr("data-index"))].state = "HANDOVER";
                                    $parentsLi.find(".teacherListBox").html("<div class='newSemesterRemind-title-box'><p>您已成功给" + currentSelect.teacherName + "老师发送转让班级通知，等待对方老师同意！</p><div class='nsc-btn'><a class='w-btn w-btn-small v-closePopupBox' href='javascript:void (0);'>知道了</a></div></div>").show();
                                }else{
                                    $17.alert(data.info);
                                }
                            });
                        }
                    }
                });
            });

            //点击没有找到
            ClassTeacherList.on("click", ".v-prevExistTeacher", function(){
                var $this = $(this);
                var $parentsLi = $this.parents("li");

                $parentsLi.find(".v-rollOut").removeClass("dis");
                $parentsLi.find(".v-rollOut").click();
                $17.tongji("老师端-新转班页-没找到-btn");
            });

            //关闭Container
            ClassTeacherList.on("click", ".v-closePopupBox", function(){
                var $this = $(this);

//                $this.parents("li").find(".v-rollOut").click();
                ClassTeacherListInit();
                $17.tongji("老师端-新转班页-知道了-btn");
            });

            //是否显示在可见区
            $(window).scroll(function(){
                var $thisTop = $(this).scrollTop();
                flagPopupBox($thisTop, ".teacherAutoRollOutClazz");
            });

            function flagPopupBox(docTop, objBox){
                if($(objBox).length > 0){
                    var hBodyHeight = $("html, body").height();
                    var objBoxHeight = $(objBox).offset().top;

                    if(objBoxHeight + 100 > (docTop + hBodyHeight) && $17.isBlank($(".teacherAutoRollOutClazz-popup").html())){
                        $(".teacherAutoRollOutClazz-popup").html( $(objBox).find("dl").clone() ).show();
                    }

                    if(objBoxHeight + 100 < (docTop + hBodyHeight)){
                        $(".teacherAutoRollOutClazz-popup").html("").hide();
                    }
                }
            }
        });
    </script>
    <script type="text/html" id="T:转班班级列表ClassTeacherList">
        <#if .now < '2015-03-11 00:00:00'?datetime('yyyy-MM-dd HH:mm:ss')>
        <div class="dropDownBox_tip" style="position: static;">
            <div style="padding: 7px 15px; width: auto; font-size: 14px; line-height: 140%; text-align: center;" class="tip_content">
                <span style="color: #333;">提醒：班级成功转出后，新作业奖励（包括假期作业未发放的奖励）均由新老师获得。</span>
            </div>
        </div>
        </#if>
        <ul>
            <%for(var i = 0; i < clazzList.length; i++){%>
                <#--没有任何操作-->
                <%if(clazzList[i].state == "NOTHING"){%>
                    <li class="nsr-list-box" data-index="<%=i%>">
                        <div class="nsc-tips">
                            <p class="nt-con">
                                <a class="w-blue v-iAlsoTeach" data-clazz-id="<%=clazzList[i].clazzId%>" href="javascript:void (0);">我还教</a>
                                <a class="w-btn w-btn-well v-rollOut" href="javascript:void (0);" data-clazz-id="<%=clazzList[i].clazzId%>" data-clazz-name="<%=clazzList[i].clazzName%>" title="转给新老师">转给新老师</a>
                            </p>
                            <span class="nt-class"><i class="nsc-arrow">▶</i><%=clazzList[i].clazzName%></span>
                        </div>
                        <div class="teacherListBox" style="display: none;"></div>
                    </li>
                <%}%>
            <%}%>
            <%for(var i = 0; i < clazzList.length; i++){%>
                <#--已经发过申请-->
                <%if(clazzList[i].state == "HANDOVER"){%>
                    <li class="nsr-list-box" data-index="<%=i%>">
                        <div class="nsc-tips">
                            <p class="nt-con">
                                <a class="w-gray v-rollOut" data-clazz-id="<%=clazzList[i].clazzId%>" data-clazz-name="<%=clazzList[i].clazzName%>" href="javascript:void (0);" title="重新转班">重新转班</a>
                                <a class="w-btn w-btn-well w-btn-green" href="/teacher/clazz/alteration/unprocessedapplication.vpage?recordEnter=vTabB">已发送申请</a>
                            </p>
                            <span class="nt-class"><i class="nsc-arrow">▶</i><%=clazzList[i].clazzName%></span>
                        </div>
                        <div class="teacherListBox" style="display: none;"></div>
                    </li>
                <%}%>
            <%}%>
                <#--点击过我还教-->
            <%for(var i = 0; i < clazzList.length; i++){%>
                <%if(clazzList[i].state == "RETAIN"){%>
                <li class="nsr-list-box" data-index="<%=i%>">
                    <div class="nsc-tips">
                        <p class="nt-con">
                            <a class="w-gray v-rollOut" data-clazz-id="<%=clazzList[i].clazzId%>" data-clazz-name="<%=clazzList[i].clazzName%>" href="javascript:void (0);" title="重新转班">重新转班</a>
                            <a class="w-btn w-btn-well w-btn-disabled" style="cursor: default;" href="javascript:void (0);" data-clazz-id="<%=clazzList[i].clazzId%>" data-clazz-name="<%=clazzList[i].clazzName%>">已处理</a>
                        </p>
                        <span class="nt-class"><i class="nsc-arrow">▶</i><%=clazzList[i].clazzName%></span>
                    </div>
                    <div class="teacherListBox" style="display: none;"></div>
                </li>
                <%}%>
            <%}%>
        </ul>
    </script>
    <script type="text/html" id="T:本校所有老师列表">
        <%if(authTeacher.length > 0 || noAuthTeacher.length > 0){%>
        <div class="newSemesterChange-box">
            <div class="nsc-con">
                <dl class="allTeacherList">
                    <dt>选择同校老师：</dt>
                    <%if(authTeacher.length > 0){%>
                        <dd>
                            <#--认证列表-->
                            <%for(var i = 0, list = authTeacher; i < list.length; i++){%>
                            <p data-teacher-id="<%=list[i].id%>" data-teacher-name="<%=list[i].profile.realname%>" class="v-selectTeacher" title="已认证"><span class="w-radio"></span><%=list[i].teacherName%>（<%=list[i].teacherId%>）老师[<%=list[i].subjectName%>] <i class="w-icon-public w-icon-authVip"></i></p>
                            <%}%>
                        </dd>
                    <%}%>
                    <%if(noAuthTeacher.length > 0){%>
                        <dd class="noAuthTeacherList" <%if(authTeacher.length > 0){%>style="display: none;"<%}%> >
                            <#--非认证列表-->
                            <%for(var i = 0, list = noAuthTeacher; i < list.length; i++){%>
                                <p data-teacher-id="<%=list[i].id%>" data-teacher-name="<%=list[i].profile.realname%>" class="v-selectTeacher" title="非认证"><span class="w-radio"></span><%=list[i].profile.realname%>（<%=list[i].id%>）老师[<%=list[i].subject%>]</p>
                            <%}%>
                        </dd>
                        <%if(authTeacher.length > 0){%>
                        <dd class="pl">没找到？<a class="w-blue v-moreNoAuthTeacher" href="javascript:void (0);">查看全部</a></dd>
                        <%}%>
                    <%}%>
                </dl>
            </div>
            <div class="nsc-btn"><a class="w-btn w-btn-small v-existTeacher" href="javascript:void (0);" title="转给Ta" data-clazz-id="<%=clazzId%>">转给Ta</a></div>
        </div>
        <%}%>

        <div style="position: absolute; top: 100%; margin-top: -104px;">
            <div class="teacherAutoRollOutClazz-popup newSemesterYellow-box" style="display: none;"></div>
        </div>
    </script>

    <script type="text/html" id="T:returnTeacherList">
        <div class="newSemesterTeacher-box">
            <div class="cus-con allTeacherList">
                <%for(var i = 0, list = teachersList; i < list.length; i++){%>
                 <span data-teacher-id="<%=list[i].teacherId%>" data-teacher-name="<%=list[i].teacherName%>" class="actor v-selectTeacher">
                    <i class="icon-s-card">
                        <%if(list[i].teacherImg == ""){%>
                        <img width="80" height="80" src="<@app.avatar href=''/>">
                        <%}else{%>
                        <img width="80" height="80" src="<@app.avatar href='<%=list[i].teacherImg%>'/>">
                        <%}%>
                    </i>
                    <strong>
                        <i class="w-radio"></i>
                        <i><%=list[i].teacherName%>（<%=list[i].teacherId%>）</i>
                        <%if(list[i].auth){%>
                            <i class="w-icon-public w-icon-authVip"></i>
                        <%}%>
                    </strong>
                </span>
                <%}%>
            </div>
            <div class="cus-btn">
                <a class="w-btn w-btn-small w-btn-green v-prevExistTeacher" href="javascript:void (0);" title="没找到">没找到</a>
                <a class="w-btn w-btn-small v-existTeacher" href="javascript:void (0);" title="确定">确定</a>
            </div>
        </div>
    </script>
</@shell.page>