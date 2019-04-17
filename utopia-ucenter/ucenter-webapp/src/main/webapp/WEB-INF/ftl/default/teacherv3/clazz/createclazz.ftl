<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
<script id="t:创建班级" type="text/html">
    <div class="w-base-title">
        <h3>我教的班级</h3>
        <div class="w-base-ext">
            <span class="w-bast-ctn">
                <i class="w-teach-spot-icon"></i>请让学生通过老师<#if mobile?has_content>手机</#if>号
                <span class="w-red w-ft-large">
                    <#--双号时有mobile 如果手机号为空 则显示学号-->
                    <#if mobile?has_content>
                        ${mobile!''}
                    <#else>
                        ${currentUser.id!}
                    </#if>
                </span>加入你的班级做作业
            </span>
        </div>
    </div>
    <div class="w-base-container" style="position: relative">
        <!--//start-->
        <div class="t-addclass-case">
            <dl>
                <dt>选择学制：</dt>
                <dd class="clear">
                    <% for(var i = 5; i < 7; i++){ %>
                        <p <% if(schoolLength == "P" + i){ %>class="active"<% } %>>
                            <a data-schoollength="P<%= i %>" href="javascript:void (0)"><%= i %>年制</a>
                        </p>
                    <% } %>
                </dd>
                <dt>选择年级：</dt>
                <dd class="clear">
                    <div class="w-border-list t-homeworkClass-list">
                        <ul>
                            <% if(schoolLength == "P6"){ %>
                                <% for(var i = 1; i < 7; i++){ %>
                                    <li class="v-level <% if(level == i){ %>active<% } %>" data-level="<%= i %>" style="width: 84px;"><%= i %>年级</li>
                                <% } %>
                            <% }else{ %>
                                <% for(var i = 1; i < 6; i++){ %>
                                    <li class="v-level <% if(level == i){ %>active<% } %>" data-level="<%= i %>" style="width: 84px;"><%= i %>年级</li>
                                <% } %>
                            <% } %>
                            <% if(level != null){ %>
                            <li class="v-parent pull-down" style="width:629px;">
                                <% for(var i = 1; i < 11; i++){ %>
                                    <%var tempClazzName%>
                                    <% for(var j = 0; j < clazzName.length; j++){ %>
                                        <% if(clazzName[j] == i + '班'){ %>
                                            <%tempClazzName = clazzName[j]%>
                                        <% } %>
                                    <% } %>
                                    <p style="border:none;" data-clazzname="<%= i %>班" title="可多选" class="<% if(tempClazzName == i + '班'){ %>active<% } %>">
                                        <span class="w-checkbox"></span>
                                        <span class="w-icon-md"><%= i %>班 <span class="w-red" <% if(tempClazzName != i + '班'){ %>style="display: none;"<% } %>>可多选</span></span>
                                    </p>
                                <% } %>
                                <div class="define" style="padding: 10px;">
                                    <input type="text" placeholder="其他班级名称" class="v-auto-clazzName before" value="" style="width: 90px;">
                                    <span style="position: absolute; left: 106px; _left: 100px; top: 20px;">班</span>
                                    <a class="v-auto-addClazz w-btn w-btn-mini" href="javascript:void(0);" style="padding: 10px 20px 9px; width: auto; display:none;">添加</a>
                                    <span class="info-text">如果有其他班级名称请输入</span>
                                </div>
                            </li>
                            <% } %>
                        </ul>
                    </div>
                </dd>
                <% if(clazzName.length > 0){ %>
                <dt>我的班级：
                    <div style="text-align: center; line-height: 22px;">(共<%=clazzName.length%>个班)</div>
                </dt>
                <dd class="clear thisClass">
                    <% for(var i = 0; i < clazzName.length; i++){ %>
                        <p><a data-clazzname="<%=clazzName[i]%>" href="javascript:void (0)"><i class="w-icon w-icon-32"></i><%=clazzName[i]%></a></p>
                    <% } %>
                    <div class="w-clear"></div>
                </dd>
                <%}%>
            </dl>
            <div class="t-pubfooter-btn">
                <a class="v-next w-btn w-btn-small" href="javascript:void(0);" data-step-id="18" data-step-content="clazz-click-next">下一步</a>
            </div>
        </div>
        <!--end//-->
    </div>
</script>
<script id="t:加入班级" type="text/html">
    <div class="w-base-title">
        <h3>加入班级</h3>
        <div class="w-base-ext">
            <span class="w-bast-ctn">
                <a class="v-prev w-blue" href="javascript:void (0);" onclick="$17.tongji('老师端-加入班级-修改班级');">修改班级</a>
            </span>
        </div>
    </div>
    <div class="w-base-container">
<!--//start-->
<div class="t-addclass-box">
    <%for(var al = 0, addList = base.addList; al < addList.length; al++){%>
        <%if(al == joinIndex){%>
        <div style="<%if(al != joinIndex){%>display:none;<%}%>" class="data-clazzSingle" index="<%=al%>">
            <h3 class="w-ag-center" style="line-height: 60px; height: 60px; font-size:20px;"><%= addList[al].fullName %>已存在，是否加入？</h3>
            <#--已认证老师-->
            <%if(addList[al].validClazzs){%>
            <ul class="v-clazzs ta-clazzs">
                <% for(var i = 0, validClazzs = addList[al].validClazzs; i < validClazzs.length; i++){ %>
                <li data-clazzid="<%= validClazzs[i].clazzId %>" data-name-string="<%= validClazzs[i].nameStr %>" data-teacher-name="<%if(validClazzs[i].teachers.length > 0){%><%= validClazzs[i].teachers[0].teacherName %><%}%>" <% if(joinClazzId == validClazzs[i].clazzId){ %>class="active"<% } %>>
                <p><%= validClazzs[i].clazzName %> 学生 <span class="w-red"><%= validClazzs[i].studentCount %></span>人</p>
                <% for(var j = 0; j < validClazzs[i].teachers.length; j++){ %>
                <p>
                    <%= validClazzs[i].teachers[j].teacherName %>  老师（<%= validClazzs[i].teachers[j].teacherSubjectName %>）
                    <% if(validClazzs[i].teachers[j].teacherAuth){ %>
                    <span class="w-icon-public w-icon-authVip" title="认证老师"></span>
                    <% } %>
                </p>
                <% } %>
                <div class="info-box">
                    <div class="too">
                        <p><%== validClazzs[i].nameStr %></p>
                    </div>
                    <span class="arrow">▼</span>
                </div>
                </li>
                <% } %>
            </ul>
            <div class="w-clear"></div>
            <%}%>
            <#--未认证老师-->
            <%if(addList[al].invalidClazzs.length > 0){%>
            <div>
                <%if(addList[al].validClazzs && invalidShow == "hide"){%><p class="title v-check-invalid" style="margin-top: 20px;"><a href="javascript:void(0);" class="w-blue">查看其他<%=addList[al].invalidClazzs.length%>个班级 ▼</a></p><%}%>
                <p class="title" style="margin-top: 20px; <%if(addList[al].validClazzs && invalidShow == "hide"){%>display: none;<%}%>" data-id="uplink">下列班级15天内无老师登录，加入申请可能得不到处理哦！</p>
                <ul class="v-clazzs ta-clazzs" <%if(addList[al].validClazzs && invalidShow == "hide"){%>style="display: none;"<%}%> data-id="clazzList">
                <% for(var i = 0, invalidClazzs = addList[al].invalidClazzs; i < addList[al].invalidClazzs.length; i++){ %>
                <li data-clazzid="<%=invalidClazzs[i].clazzId %>" data-name-string="<%=invalidClazzs[i].nameStr %>" data-teacher-name="<%if(invalidClazzs[i].teachers.length > 0){%><%= invalidClazzs[i].teachers[0].teacherName %><%}%>" <% if(joinClazzId == invalidClazzs[i].clazzId){ %>class="active"<% } %>>
                <p><%=invalidClazzs[i].clazzName %> 学生 <span class="w-red"><%=invalidClazzs[i].studentCount %></span>人</p>
                <% for(var j = 0; j <invalidClazzs[i].teachers.length; j++){ %>
                <p>
                    <%=invalidClazzs[i].teachers[j].teacherName %>  老师（<%=invalidClazzs[i].teachers[j].teacherSubjectName %>）
                </p>
                <% } %>
                <div class="info-box">
                    <div class="too">
                        <p><%==invalidClazzs[i].nameStr %></p>
                    </div>
                    <span class="arrow">▼</span>
                </div>
                </li>
                <% } %>
                </ul>
            </div>
            <%}%>
            <div class="t-pubfooter-btn" style="padding: 10px 0;">
                <p class="data-info" style="color:#f00; height:32px; text-align: center; <% if(joinClazzId != ''){ %>display:none;<% } %>">请选择班级!</p>
                <a class="v-not-found w-btn w-btn-green w-btn-small" href="javascript:void(0);" data-type="force" data-index="<%=al%>" style="width: 140px;">不加入</a>
                <a class="v-join-btn w-btn w-btn-small" data-index="<%=al%>" href="javascript:void(0);" onclick="$17.tongji('老师端-加入班级-加入班级');">加入</a>
            </div>
        </div>
        <%}%>
    <%}%>

    <div style="clear:both; padding: 0 0 20px; text-align:center;">
        <#--<p>没有我要加入的班级，去<a href="javascript:void(0);" class="w-blue v-next" data-type="force" data-step-id="21" data-step-content="clazz-alreadyClass-addNewClass">创建班级</a></p>-->
        <p>如遇到问题请拨打客服热线：<@ftlmacro.hotline phoneType="teacher"/></p>
    </div>
</div>
<!--end//-->
    </div>
</script>
<script id="t:设置学生" type="text/html">
    <div class="">
        <div class="w-base-title">
            <h3>确认我的任教班级</h3>
            <div class="w-base-ext">
                <span class="w-bast-ctn">
                    <a class="v-prev w-blue" href="javascript:void (0);" onclick="$17.tongji('老师端-创建班级-方式一输入名单-修改班级');" data-step-id="26" data-step-content="clazz-click-createMethodsOne-back">修改班级</a>
                </span>
            </div>
        </div>
        <div class="w-base-container">
            <div class="w-table w-table-border-bot w-table-pad20">
                <table>
                    <thead>
                        <tr>
                            <td style="width:39%;">班级</td>
                            <td>学生数</td>
                        </tr>
                    </thead>
                    <tbody>
                        <% for(var i = 0; i < clazzName.length; i++){ %>
                        <tr class="<%if(i%2 > 0){%>odd<%}%>">
                            <td><%==clazzName[i].classLevel%>年级<%==clazzName[i].clazzName%></td>
                            <td>
                                <a class="v-minus-btn w-btn w-btn-mini<% if(minus_disabled){ %> w-btn-disabled<% } %>" data-index="<%=i%>" href="javascript:void (0)" style="width: 25px;">-</a>
                                <input class="v-student-num w-int" type="text" value="<%= clazzNum %>" data-index="<%=i%>" style="width: 50px;">
                                <a class="v-plus-btn w-btn w-btn-mini<% if(plus_disabled){ %> w-btn-disabled<% } %>" data-index="<%=i%>" href="javascript:void (0)" style="width: 25px;">+</a>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
            <%if(addList.length > 0){%>
            <div style="padding: 15px;">
                已向以下班级发送加入申请：
                <% for(var i = 0; i < addList.length; i++){ %>
                <span style="display:inline-block; margin-right: 10px;"><%=addList[i].clazzLevel%>年级<%=addList[i].clazzName%></span>
                <%}%>
            </div>
            <%}%>
            <div class="t-pubfooter-btn" style="margin-left: 328px; text-align: left;">
                <a class="v-create-btn w-btn w-btn-small" href="javascript:void(0);" data-step-id="28" data-guide="new" data-step-content="clazz-click-createMethodsTwo-back">生成学生账号</a>
            </div>
        </div>
    </div>
</script>
<script type="text/html" id="t:添加班级成功页面">
<div class="w-base">
    <div class="w-base-title">
        <h3>提示</h3>
    </div>
    <div class="w-base-container">
    <#--//start-->
        <div style="margin: 0 auto; width:670px;">
            <p class="w-green" style="text-align: center; padding: 30px; font-size: 20px;">创建班级成功！</p>
            <div>
                <div class="t-download-step"></div>
                您可能需要：
                <a href="javascript:void(0);" class="w-blue data-ImportStudentName" data-clazzid="<%=clazzIds%>">导入班级名单</span></a>
                <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/clazz/downloadletter.vpage" target="_blank" class="w-blue" style="display: inline-block; margin-left: 20px;">下载家长使用说明</a>
            </div>
            <div class="w-ag-center" style="padding: 50px 0 20px;">
                <a href="/teacher/clazz/batchdownload.vpage?clazzIds=<%=clazzIds%>" target="_blank" class="w-btn w-btn-green w-btn-small" >下载学生账号</a>
                <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/homework/batchassignhomework.vpage" class="w-btn w-btn-small">布置作业</a>
            </div>
        </div>
    <#--end//-->
    </div>
</div>
</script>
<div class="step-container-complete" style="display: none; z-index: 155;" data-title="立即布置作业 ，终身免费使用">
    <div class="step-container-textInfo-0">
        <div class="card_tab">
            <a href="javascript:void(0);" class="card-arrow prev prev-dis"></a>
            <a href="javascript:void(0);" class="card-arrow next"></a>
        </div>
        <div class="item">
            <ul style="width: 3200px;">
                <li><img src="<@app.link href='public/skin/teacherv3/images/notauthindex/clazz-popup-back_1.jpg'/>" width="640" height="400"></li>
                <#if (currentTeacherDetail.subject == "MATH")!false>
                    <li><img src="<@app.link href='public/skin/teacherv3/images/notauthindex/clazz-popup-back_3.jpg'/>" width="640" height="400"></li>
                <#else>
                    <li><img src="<@app.link href='public/skin/teacherv3/images/notauthindex/clazz-popup-back_2.jpg'/>" width="640" height="400"></li>
                </#if>
                <li><img src="<@app.link href='public/skin/teacherv3/images/notauthindex/clazz-popup-back_4.jpg'/>" width="640" height="400"></li>
                <li><img src="<@app.link href='public/skin/teacherv3/images/notauthindex/clazz-popup-back_5.jpg'/>" width="640" height="400"></li>
            </ul>
        </div>
        <div class="btn">
            <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/index.vpage?step=showtip&index=1" onclick="$17.tongji('1.注册流程2—主流程—开始体验');">立即体验</a>
        </div>
    </div>
    <script type="text/javascript">
        function containerComplete(){
            var idx		= $(".step-container-complete");				//id

            $17.tongji("注册流程-创建班级-痛点-1");
            //左点击
            var index = 0;
            var recordInterval = setInterval(timeCard, 3000);

            function timeCard(){
                if(index < 3){
                    index++;
                    idx.find("ul").animate({ "margin-left" : "-" + (index*640) + "px" }, 200);
                    idx.find(".prev").removeClass("prev-dis");
                    $17.tongji("注册流程-创建班级-痛点-" + (index+1));
                    if(index == 3){
                        idx.find(".next").addClass("next-dis");
                        clearInterval(recordInterval);
                    }
                }
            }

            idx.find(".prev, .next").on("click", function(){
                var $this = $(this);

                if($this.hasClass("prev-dis") || $this.hasClass("prev-dis")){
                    return false;
                }

                if($this.hasClass("prev")){
                    if(index > 0){
                        index--;
                    }

                    if(index == 0){
                        $this.addClass("prev-dis");
                    }
                }

                if($this.hasClass("next")){
                    if(index < 3){
                        index++;
                        $17.tongji("注册流程-创建班级-痛点-" + (index+1));
                    }

                    if(index == 3){
                        $this.addClass("next-dis");
                    }
                }

                if(index > 0){
                    $this.siblings(".prev").removeClass("prev-dis");
                }

                if(index < 3){
                    $this.siblings(".next").removeClass("next-dis");
                }

                idx.find("ul").animate({ "margin-left" : "-" + (index*640) + "px" }, 200);
            });
        }
    </script>
</div>
<div id="Anchor" class="w-base"></div>
<#--<#include "../block/batchAddStudentName.ftl"/>-->
<@sugar.capsule js=["clazz.createclazz"] />
<script type="text/javascript">
    var ClazzInfo = {
        tempInfo: {
            name : "t:创建班级",
            base : {
                schoolId     : ${schoolId!0},
                schoolLength : "${eduSystem!'P6'}",
                level        : null,
                clazzName    : [],
                step : $17.getQuery("step")
            }
        },
        eventConfig : {
            "[data-schoollength] -> click"     : schoolLength_click,
            "[data-level] -> click"            : level_click,
            "[data-clazzname] -> click"        : clazzName_click,
            "[data-clazzname] -> mouseenter"        : clazzName_mouseenter,
            "[data-clazzname] -> mouseleave"        : clazzName_mouseleave,
            ".v-auto-addClazz -> click"          : autoAddClazz_click,
            ".v-auto-clazzName -> focus"          : autoAddClazz_focus,
            ".v-next -> click"                  : next_button_click
        },
        setClazzName : function(clazzName){
            if($.inArray(clazzName, this.tempInfo.base.clazzName) > -1 ){
                this.tempInfo.base.clazzName.splice($.inArray(clazzName, this.tempInfo.base.clazzName), 1)
            }else{
                var clazzCount = ${clazzCount!0};

                if(ClazzInfo.tempInfo.base.clazzName.length > 5){
                    $17.alert("一次最多添加6个班级!")
                    return false;
                }

                if(ClazzInfo.tempInfo.base.clazzName.length > (7 - clazzCount)){
                    $17.alert("你已有 <strong>"+clazzCount+"</strong> 个班级，不能添加新班级了！<br/>如有问题请联系客服：400-160-1717。");
                    return false;
                }

                this.tempInfo.base.clazzName.push(clazzName);
            }
        },
        refresh : function(){
            $("#Anchor").html(template(this.tempInfo.name, this.tempInfo.base));

            $17.delegate(this.eventConfig);
        },
        init : function(){
            this.refresh();

            $(document).on("mouseover", "ul.v-clazzs li", function(){
                //IE6 - 当前Tips顶层
                $(this).find(".info-box").show().end().css({zIndex : 6});
            }).on("mouseout", "ul.v-clazzs li", function(){
                $(this).find(".info-box").hide().end().removeAttr("style");
            });
        }
    };

    var JoinClazz = {
        tempInfo : {
            name        : "t:加入班级",
            joinClazzId : null,
            joinHasFlag : false,
            joinIndex : 0,
            base        : null,
            invalidShow : "hide"
        },
        eventConfig : {
            "ul.v-clazzs li -> click"       : select_join_clazz,
            "a.v-prev -> click"             : prev_click,
            ".v-next -> click"                  : next_button_click,
            ".v-not-found -> click"          : not_found_clazz,
            ".v-join-btn -> click"          : join_btn_click,
            ".v-check-invalid -> click"    : check_invalid_click
        },
        refresh : function(){
            $("#Anchor").html(template(this.tempInfo.name, this.tempInfo));
//            $("#AnchorJoinBox").html(template(this.tempInfo.name, this.tempInfo));
            $17.delegate(this.eventConfig);

            //添加班级页-已有班级-进入
            $17.voxLog({
                module: _tempLogModule,
                op : "clazz-alreadyClass-load",
                step : 19
            });
        }
    };

    var StudentInfo = {
        tempInfo : {
            name : "t:设置学生",
            base : {
                clazzNum    : 60,
                clazzNumMax : 80,
                clazzIdArr : [],
                clazzName   : [],
                addList : [],
                stepFlag : $17.getQuery("step") || ""
            }
        },
        eventConfig : {
            "input.v-student-num -> keyup"   : clazzmax_keyup,
            "input.v-student-num -> focus"   : clazzmax_focus,
            "a.v-minus-btn -> click"        : minus_click,
            "a.v-plus-btn -> click"         : plus_click,
            "a.v-prev -> click"             : prev_click,
            ".v-create-btn -> click"        : create_btn_click,
            ".v-join-clazz -> click"        : next_button_click,
            "#batch_student_name -> click"  : function(){ $('.batch_student_text').hide(); },
            ".batch_student_text -> click"  : function(){ $('.batch_student_text').hide(); }
        },
        refresh : function(){
            $("#Anchor").html(template(this.tempInfo.name, this.tempInfo.base));

            $17.delegate(this.eventConfig);

            $("html, body").animate({ scrollTop: 0 }, 200);

            //添加班级页-创建新班级-进入
            $17.voxLog({
                module: _tempLogModule,
                op : "clazz-addNewClass-load",
                step : 24
            });
        }
    };

    $(function(){
        ClazzInfo.init();

        LeftMenu.focus("clazzmanager");

        $(document).on("click", ".v-download-clazz", function(){
            $17.voxLog({
                module: "common",
                op : "downloadletter"
            });
        });

        //showTip Enter
        if($17.getQuery("step") == "showtip"){
            $("#showTipOptBack").show();
            $("#Anchor").addClass("w-opt-back-content");
        }

        StudentInfo.tempInfo.base.clazzNum = 1;

        //添加班级页-进入
        $17.voxLog({
            module: _tempLogModule,
            op : "showtip-click-addClass",
            step : 14
        });
    });
</script>
</@shell.page>