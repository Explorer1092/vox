<#assign hasShowTip = ((teachClazzs?size lt 1)!true)/>
<div class="t-clazzSwitch-box">
    <div class="mc-hd">
        <div class="hd-left fl-main" id="helpHoverContent" style="width: auto;">
            <p>
                <#if (currentTeacherDetail.subjects?size gt 1)!false>
                    我的${curSubjectText!((currentTeacherDetail.subject)!'')}班级
                <#else>
                    创建我的班级
                </#if>
                <#include "../../block/switchsubjcet.ftl"/>
            </p>
        </div>
        <div class="hd-right">
            <#--<span class="manage">管理班级</span>-->
            <ul>
                <#if hasShowTip>
                    <#if isFakeTeacher!false>
                        <li class="v-clickIsFakeMenu">创建/退出班级</li><#--假老师点击弹窗 - #33420-->
                    <#else>
                        <li <#--style="display: none;"--> class="v-clickClazzMenu" data-type="create">创建/退出班级</li>
                    </#if>
                <#else>
                    <#if isFakeTeacher!false>
                        <li class="v-clickIsFakeMenu">创建/退出班级</li><#--假老师点击弹窗 - #33420-->
                    <#else>
                        <li class="v-clickClazzMenu" data-type="join">加入班级</li>
                        <li class="v-clickClazzMenu" data-type="create">创建/退出班级</li>
                    </#if>
                </#if>

                <#if sendApplications?has_content>
                <li class="v-clickClazzMenu" data-type="request">我的请求<#if (sendApplicationsCount gt 0)!false><span class="w-icon-arrow w-icon-redInfo">${sendApplicationsCount!0}</span></#if></li>
                </#if>
            </ul>
        </div>
    </div>
    <#--content- start-->
    <ul id="receiveApplications">
        <#--创建班级-->
        <li data-type="create" style="display: none;"><div id="Anchor"></div></li>
        <#--我的请求-->
        <#if sendApplications?has_content>
        <li data-type="request" style="display: none;">
            <@allSendApplicationsList sendApplications=sendApplications/>
        </li>
        </#if>
        <li data-type="join" style="display: none;">
            <@teacherJoinClazz/>
        </li>
    </ul>
    <#--end-->
</div>

<script id="t:创建班级" type="text/html">
    <div class="w-base-container">
        <%var notClazzFlag=false, onSelectClazz=0%>
        <% for(var k = 0; k < clazzName.length; k++){ %>
            <%if(clazzName[k].clazzs.length > 0){%>
                <% for(var i = 0, clazzLevel = clazzName[k].clazzs; i < clazzLevel.length; i++){ %>
                    <%notClazzFlag = true; onSelectClazz += 1;%>
                <%}%>
            <%}%>
        <%}%>
        <#--start-->
        <#if !hasShowTip>
            <#--<div class="t-myClass-title">
                    <span class="info">我共执教：
                    &lt;#&ndash;minusPlusInputEvent()&ndash;&gt;
                        <a class="w-btn w-btn-mini vox-minus-btn" href="javascript:void (0)" style="width: 26px;">-</a>
                        <input id="v-actualTeachClazzCount" type="text" value="<%=classesCount%>" class="w-int v-count-int" maxlength="2" style="text-align: center; width: 30px;">
                        <a class="w-btn w-btn-mini vox-plus-btn" href="javascript:void (0)" style="width: 26px;">+</a>
                        个班级
                    </span>
                <span class="arrow">我已选 <span><%=onSelectClazz%></span> 个班级</span>
            </div>-->
        </#if>
        <div class="t-addclass-case t-addClass-case-new" style="padding: 0;">
            <div style="background-color: #f9fcfe; padding-top: 15px;">
                <dl style="line-height:41px;">
                    <dt style="padding-top: 0">我的班级：</dt>
                    <dd style="padding-bottom: 0">
                        <% if(!notClazzFlag){ %>
                            <div style="color:#848d8d">你还没有创建班级哟~</div>
                        <% }else{ %>
                        <div class="t-myClass-content" style="border:0;line-height:35px;">
                        <% for(var k = 0; k < clazzName.length; k++){ %>
                            <% for(var i = 0, clazzLevel = clazzName[k].clazzs; i < clazzLevel.length; i++){ %>
                                <a data-level="<%=clazzName[k].clazzLevel%>" data-id="<%=clazzLevel[i].id%>" data-clazzname="<%=clazzLevel[i].name%>" class="cl-title v-clickSelectClazzname" ><%=clazzName[k].clazzLevel%>年级<%=clazzLevel[i].name%> <span class="close">×</span></a>
                            <% } %>
                        <% } %>
                        </div>
                        <% } %>
                    </dd>
                </dl>

                <dl>
                    <#--<dt>选择学制：</dt>
                    <dd class="clear">
                    <% for(var i = 5; i < 7; i++){ %>
                        <p <% if(schoolLength == "P" + i){ %>class="active"<% } %>>
                            <a class="v-clickSchoolLength" data-schoollength="P<%= i %>" href="javascript:void (0)"><%= i %>年制</a>
                        </p>
                    <% } %>
                    </dd>-->
                    <dt>选择年级：</dt>
                    <dd class="clear">
                        <div class="w-border-list t-homeworkClass-list">
                            <ul>
                                <% if(schoolLength == "P6"){ %>
                                    <%var schoolCount = 7%>
                                <% }else{ %>
                                    <%var schoolCount = 6%>
                                <% } %>

                                <% for(var i = 1; i < schoolCount; i++){ %>
                                <li class="v-level <% if(level == i){ %>active<% } %>" data-level="<%= i %>" style="width: 84px;"><%= i %>年级</li>
                                <% } %>

                                <% if(level != null){ %>
                                <li class="v-parent pull-down" style="width:629px;">
                                    <%if(levelClazz.length > 0){%>
                                    <% for(var i = 0; i < levelClazz.length; i++){ %>
                                        <p style="border:none;" data-clazzname="<%= levelClazz[i].name %>" data-level="<%= level %>" data-id="<%= levelClazz[i].id %>" title="可多选" class="<%=(levelClazz[i].checked ? 'active':'')%> v-clickSelectClazzname">
                                            <span class="w-checkbox"></span>
                                            <span class="w-icon-md"><%= levelClazz[i].name %> <span class="w-red" style="display: none;">可多选</span></span>
                                        </p>
                                    <% } %>
                                    <%}else{%>
                                    <div style="padding: 30px 0;">暂无班级</div>
                                    <%}%>
                                </li>
                                <% } %>
                            </ul>
                        </div>
                    </dd>
                </dl>
                <div style="padding: 0 47px 20px 0; margin-top: -5px; text-align: right; color: #666; clear: both;">
                    <div style="float: left; color: #f00; padding-left: 100px;text-align: left;">
                    <#if .now lte "2017-08-10 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss")>
                        系统会在8月10号统一升年级（例：现在创建二年级1班，8月10号后自动升到三年级1班）
                    <#else>
                        <#--新学期马上要开始了，请选择班级在新学年的年级哦！-->
                    </#if>
                    </div>
                    <br/><br/>
                    没有找到你的班级？点此
                    <a href="javascript:void(0);" class="w-blue v-clickService" onclick="window.open('${ProductConfig.getMainSiteBaseUrl()}/redirector/onlinecs_new.vpage?type=teacher&question_type=question_class_pt&origin=PC-创建班级','','width=856,height=519');">联系客服添加</a>
                </div>
            </div>
            <div class="t-pubfooter-btn" style="position: relative;">
                <div style="background: url(<@app.link href='public/skin/teacherv3/images/clazzsuccess/selectclazz-info-back.png'/>) no-repeat 0 0; width: 177px; height: 44px; position: absolute; right: 105px; top: 6px;"></div>
                <a class="v-next w-btn w-btn-small" href="javascript:void(0);" style="width: 160px;">确定</a>
            </div>
        </div>
        <#--end-->
    </div>
</script>
<script type="text/html" id="T:点击申请加入">
    <style>
        .t-applyClazz-addMore-box{ padding: 0 17px; background-color: #fff;}
        .t-applyClazz-addMore-box p.ad-title{ font-size: 18px; text-align: center; padding: 20px 0;}
        .t-applyClazz-addMore-box .abt-content{}
        .t-applyClazz-addMore-box .abt-content dl{ padding: 12px; background-color: #fff; margin-bottom: 20px; border:1px solid #dfdfdf;}
        .t-applyClazz-addMore-box .abt-content dl .abt-info{ display: none;}
        .t-applyClazz-addMore-box .abt-content dl.active{ border-color:#189cfb; background-color:#edf5fa;  }
        .t-applyClazz-addMore-box .abt-content dl.active .abt-info{display: block;}
        .t-applyClazz-addMore-box .abt-content dl dt{ float: left; width: 70px; padding: 8px 0 0 0;}
        .t-applyClazz-addMore-box .abt-content dl dd{ margin-left: 70px; padding: 5px 0;}
        .t-applyClazz-addMore-box .abt-content dl dd .tip-num{ margin-right: 30px; vertical-align: middle;}
        .t-applyClazz-addMore-box .abt-content dl dd p span{ margin-right: 8px;}
    </style>
    <%for(var i = 0; i < clazzItem.length; i++){%>
    <div class="t-applyClazz-addMore-box" id="applyClazzBox-<%=i%>" style="margin: -40px -20px -20px; display: <%=(i == 0 ? 'block' : 'none')%>;">
        <%if(clazzItem[i].groups.length ==0 || (clazzItem[i].groups.length>0 && clazzItem[i].groups[0].groupId==null)){%>
        <p class="ad-title"><%=clazzItem[i].name%> 已有老师和学生，点击申请加入？</p>
        <%}%>
        <%if(clazzItem[i].groups.length>0 && clazzItem[i].groups[0].groupId!=null){%>
        <p class="ad-title"><%=clazzItem[i].name%> 已有学生，点击进行接管？</p>
        <%}%>

        <div class="abt-content">
            <%for(var a = 0, groups = clazzItem[i].groups; a < groups.length; a++){%>
            <dl class="v-selectClazzsTeacher <%=(a == 0 ? 'active' : '')%>" data-clazzid="<%=clazzItem[i].clazzId%>" data-clazz-index="<%=i%>" data-groups-index="<%=a%>" >
                <%if(clazzItem[i].groupIds==null){%>
                <dt>任课教师 : </dt>
                <%}%>
                <dd>
                    <div class="abt-info" style="position: relative;">
                        <div class="dropDownBox_tip" style="top: -36px;left: -70px;">
                            <span class="arrow arrowBot">◆<span class="inArrow">◆</span></span>
                            <%if(groups[a].groupId==null){%>
                            <div class="tip_content" style="padding: 3px 5px; width: 290px;">当面提醒对方允许你加入，才能看到全部学生</div>
                            <%}%>
                            <%if(groups[a].groupId!=null){%>
                            <div class="tip_content" style="padding: 3px 5px; width: 290px;">接管后，才能看到全部学生</div>
                            <%}%>
                        </div>
                    </div>
                    <%for(var t = 0, teachers = groups[a].teachers; t < teachers.length; t++){%>
                        <span class="tip-num">
                            <%=teachers[t].name%>
                            （
                            <%=(teachers[t].subject == 'ENGLISH' ? '英语' : '')%>
                            <%=(teachers[t].subject == 'MATH' ? '数学' : '')%>
                            <%=(teachers[t].subject == 'CHINESE' ? '语文' : '')%>
                            ）
                            <%if(teachers[t].isAuth){%><i class="w-icon-public w-icon-authVip"></i><%}%>
                        </span>
                    <%}%>
                </dd>
                <dt>学生名单 : </dt>
                <dd>
                    <p>
                        <%for(var s = 0, students = groups[a].students; s < students.length; s++){%>
                        <span><%=(students[s].name ? students[s].name : '---')%></span>
                        <%}%>
                        <#--<%for(var k = 0, klxStudents = groups[a].klxStudents; k < klxStudents.length; k++){%>-->
                        <#--<span><%=(klxStudents[k].name ? klxStudents[k].name : '---')%></span>-->
                        <#--<%}%>-->
                        等
                    </p>
                </dd>
            </dl>
            <%}%>
        </div>
        <div class="t-pubfooter-btn">
            <a class="w-btn w-btn-well w-circular-5 w-btn-cyan w-border-cyan v-joinNewClazzsBtn" data-type="no" data-clazzid="<%=clazzItem[i].clazzId%>" data-clazz-index="<%=i%>" data-groups-index="0" href="javascript:void (0);" data-seq="<%=((i == clazzItem.length - 1) ? 'last' : i)%>" style="margin-right: 38px;">不加入</a>
            <a class="w-btn w-btn-well w-circular-5 w-border-blue v-joinNewClazzsBtn" data-type="yes" data-clazzid="<%=clazzItem[i].clazzId%>" data-clazz-index="<%=i%>" data-groups-index="0" href="javascript:void (0);" data-seq="<%=((i == clazzItem.length - 1) ? 'last' : i)%>">加入</a>
        </div>
    </div>
    <%}%>
</script>

<script type="text/html" id="T:点击申请接管">
    <style>
        .t-applyClazz-takeoverGroup-box{ padding: 0 17px; background-color: #fff;}
        .t-applyClazz-takeoverGroup-box p.ad-title{ font-size: 18px; text-align: center; padding: 20px 0;}
        .t-applyClazz-takeoverGroup-box .abt-content{}
        .t-applyClazz-takeoverGroup-box .abt-content dl{ padding: 12px; background-color: #fff; margin-bottom: 20px; border:1px solid #dfdfdf;}
        .t-applyClazz-takeoverGroup-box .abt-content dl .abt-info{ display: none;}
        .t-applyClazz-takeoverGroup-box .abt-content dl.active{ border-color:#189cfb; background-color:#edf5fa;  }
        .t-applyClazz-takeoverGroup-box .abt-content dl.active .abt-info{display: block;}
        .t-applyClazz-takeoverGroup-box .abt-content dl dt{ float: left; width: 70px; padding: 8px 0 0 0;}
        .t-applyClazz-takeoverGroup-box .abt-content dl dd{ margin-left: 70px; padding: 5px 0;}
        .t-applyClazz-takeoverGroup-box .abt-content dl dd .tip-num{ margin-right: 30px; vertical-align: middle;}
        .t-applyClazz-takeoverGroup-box .abt-content dl dd p span{ margin-right: 8px;}
    </style>
    <div class="t-applyClazz-takeoverGroup-box"  style="margin: -40px -20px -20px; display: 'block';">
        <p class="ad-title"><%=groupInfoItem.name%>  已有学生，点击进行接管？</p>

        <div class="abt-content">
            <%for(var a = 0, groups = groupInfoItem.groups; a < groups.length; a++){%>
            <dl class="v-selectNoteacherGroup <%=(a == 0 ? 'active' : '')%>" data-clazzid="<%=groupInfoItem.clazzId%>" data-groups-index="<%=a%>" >
                <dd>
                    <div class="abt-info" style="position: relative;">
                        <div class="dropDownBox_tip" style="top: -36px;left: -70px;">
                            <span class="arrow arrowBot">◆<span class="inArrow">◆</span></span>
                            <div class="tip_content" style="padding: 3px 5px; width: 290px;">接管后，才能看到全部学生</div>
                        </div>
                    </div>
                </dd>
                <dt>学生名单 : </dt>
                <dd>
                    <p>
                        <%for(var s = 0, students = groups[a].students; s < students.length; s++){%>
                        <span><%=(students[s].name ? students[s].name : '---')%></span>
                        <%}%>
                        <#--<%for(var k = 0, klxStudents = groups[a].klxStudents; k < klxStudents.length; k++){%>-->
                        <#--<span><%=(klxStudents[k].name ? klxStudents[k].name : '---')%></span>-->
                        <#--<%}%>-->
                        等
                    </p>
                </dd>
            </dl>
            <%}%>
        </div>
        <div class="t-pubfooter-btn">
            <a class="w-btn w-btn-well w-circular-5 w-btn-cyan w-border-cyan v-takeoverGroupBtn" data-type="no" data-clazzid="<%=groupInfoItem.clazzId%>"  data-groups-index="0" href="javascript:void (0);"  style="margin-right: 38px;">不接管</a>
            <a class="w-btn w-btn-well w-circular-5 w-border-blue v-takeoverGroupBtn" data-type="yes" data-clazzid="<%=groupInfoItem.clazzId%>"  data-groups-index="0" href="javascript:void (0);" >接管</a>
        </div>
    </div>
</script>

<script type="text/javascript">
    var tempLevelClazz = {};

    var ClazzInfo = {
        tempInfo: {
            adjustClazzs : [],
            newClazzs : [],
            takeoverGroups : [],
            tempdeleteIndexs:[],
            groupInfosList:[],
            groupInfoItem:[],
            name : "t:创建班级",
            base : {
                classesCount : 0,
                schoolLength : "${eduSystem!'P6'}",
                level        : null,
                levelClazz : [],
                clazzName    : [],
                initClazzIds : []
            }
        },
        eventConfig : {
            "v-clickSchoolLength -> click"     : schoolLength_click,
            ".v-level -> click"            : level_click,
            ".v-clickSelectClazzname -> click"        : clazzName_click,
            ".v-clickSelectClazzname -> mouseenter"        : clazzName_mouseEnter,
            ".v-clickSelectClazzname -> mouseleave"        : clazzName_mouseLeave,
            ".v-next -> click"                  : next_button_click
        },
        clazzCount : function(){
            var clazzAllCount = 0;
            var cl = this.tempInfo.base.clazzName;
            for(var b = 0; b < cl.length; b++) {
                for (var d = 0; d < cl[b].clazzs.length; d++) {
                    clazzAllCount++;
                }
            }
            return clazzAllCount;
        },
        setClazzName : function(level, obj){
            var $this = this;
            var $indexKey = 0;
            var cl = this.tempInfo.base.clazzName;
            var clazzAllCount = $this.clazzCount();
            var maxClazzCount = ${((curSubject == "ENGLISH")!false)?string('8', '4')};

            for(var t= 0, tcl = tempLevelClazz[level]; t < tcl.length; t++){
                if(tcl[t].id == obj.id){
                    if(clazzAllCount == maxClazzCount && !tcl[t].checked){
                        $17.alert("最多教 <b class='w-red'>"+maxClazzCount+"</b> 个班级");
                        return false;
                    }

                    if( tcl[t].checked ) {
                        $indexKey = t;
                        if ($.inArray(Number(obj.id), $this.tempInfo.base.initClazzIds) > -1) {
                            $.prompt("<div class='w-ag-center'>不再担任这个班的${curSubjectText}老师？</div>", {
                                focus: 1,
                                title: "系统提示",
                                buttons: {"不教了": true, "继续教": false},
                                position: {width: 500},
                                submit: function (e, v) {
                                    if (v) {
                                        tempLevelClazz[level][$indexKey].checked = false;
                                        lookLevel();
                                        $this.refresh();
                                    }
                                }
                            });
                        } else {
                            tempLevelClazz[level][$indexKey].checked = false;
                            lookLevel();
                            $this.refresh();
                        }
                    }else{
                        tcl[t].checked = true;
                        lookLevel();
                    }
                }
            }

            function lookLevel(){
                //look level flag
                for(var i = 0; i < cl.length; i++){
                    if(cl[i].clazzLevel == level){
                        for(var k = 0, item = cl[i].clazzs; k < item.length; k++){
                            if(item[k].id == obj.id){
                                //delete
                                cl[i].clazzs.splice(k, 1);
                                return false;
                            }
                        }
                        //add
                        cl[i].clazzs.push(obj);
                        return false;
                    }
                }

                //add level
                cl.push({clazzLevel: level, clazzs: [obj]});
            }
        },
        refresh : function(){
            $("#Anchor").html(template(this.tempInfo.name, this.tempInfo.base));

            $17.delegate(this.eventConfig);
        },
        init : function(){
            $17.tongji("新创建班级方式");
            var $this = this;

            clazz_click(1, function(){});
            //获取班级
            // 0b0000000010000011
            $.get("/teacher/systemclazz/chooseclazz.vpage<#if multiSubject>?subject=${curSubject}</#if>", {infoType : 194}, function(data){
                if(data.success){
                    $this.tempInfo.base.clazzName = data.teachClazzs;
                    $this.tempInfo.base.classesCount = data.actualTeachClazzCount;
                    for (var key in data.teachClazzs) {
                        var levelClazz = data.teachClazzs[key].clazzs;
                        for (var c in levelClazz) {
                            $this.tempInfo.base.initClazzIds.push(levelClazz[c].id)
                        }
                    }

                    $this.refresh();
                }
            });
        }
    };

    //学制被点
    function schoolLength_click(){
        $17.tongji("老师端-添加班级-选择学制");

        ClazzInfo.tempInfo.base.schoolLength = $(this).attr("data-schoollength");
        ClazzInfo.tempInfo.base.level = null;
        ClazzInfo.tempInfo.base.clazzName = [];

        ClazzInfo.refresh();
        return false;
    }

    //年级被点
    function level_click(){
        $17.tongji("新创建班级方式-添加班级-选择年级");

        ClazzInfo.tempInfo.base.level = $(this).attr("data-level");

        if(tempLevelClazz[ClazzInfo.tempInfo.base.level]){
            ClazzInfo.tempInfo.base.levelClazz = tempLevelClazz[ClazzInfo.tempInfo.base.level];
            ClazzInfo.refresh();
        }else{
            // 0b0000000010000001
            $.get("/teacher/systemclazz/chooseclazz.vpage<#if multiSubject>?subject=${curSubject}</#if>", {infoType : 129, clazzLevel: ClazzInfo.tempInfo.base.level}, function(data){
                if(data.success){
                    tempLevelClazz[ClazzInfo.tempInfo.base.level] = data.clazzs;
                    ClazzInfo.tempInfo.base.levelClazz = tempLevelClazz[ClazzInfo.tempInfo.base.level];
                    ClazzInfo.refresh();
                }
            });
        }

        return false;
    }

    //点击班级
    function clazz_click(level, callback){
        if($17.getQuery("step") == "showtip"){
            $17.voxLog({
                module : "newTeacherRegStep",
                op : "class-clickClazzList"
            });
        }

        ClazzInfo.tempInfo.base.level = level;

        if(tempLevelClazz[ClazzInfo.tempInfo.base.level]){
            ClazzInfo.tempInfo.base.levelClazz = tempLevelClazz[ClazzInfo.tempInfo.base.level];
            callback();
            ClazzInfo.refresh();
        }else{
            // 0b0000000010000001
            $.get("/teacher/systemclazz/chooseclazz.vpage<#if multiSubject>?subject=${curSubject}</#if>", {infoType : 129, clazzLevel: ClazzInfo.tempInfo.base.level}, function(data){
                if(data.success){
                    tempLevelClazz[ClazzInfo.tempInfo.base.level] = data.clazzs;
                    ClazzInfo.tempInfo.base.levelClazz = tempLevelClazz[ClazzInfo.tempInfo.base.level];
                    callback();
                    ClazzInfo.refresh();
                }
            });
        }

        return false;
    }

    //班级名称被点
    function clazzName_click(){
        var $this = $(this);
        var $level = $this.attr("data-level");

        clazz_click($level, function(){
            ClazzInfo.setClazzName($level, {
                id : $this.attr("data-id"),
                name : $this.attr("data-clazzname")
            });
        });

        $17.tongji("新创建班级方式-添加班级-选择班级");
        return false;
    }

    //经过
    function clazzName_mouseEnter(){
        var $this = $(this);

        if(!$this.hasClass("active")){
            $this.find(".w-red").show();
        }
    }

    //移出
    function clazzName_mouseLeave(){
        var $this = $(this);

        if(!$this.hasClass("active")){
            $this.find(".w-red").hide();
        }
    }

    // 下一步
    function next_button_click() {
        var $this = $(this);
        if ($this.hasClass("w-btn-disabled")) {
            return false;
        }

        if (ClazzInfo.tempInfo.base.clazzName.length < 1 || ClazzInfo.clazzCount() < 1) {
            $17.alert("请选择班级!");
            return false;
        }

        if($17.getQuery("step") == "showtip"){
            $17.voxLog({
                module : "newTeacherRegStep",
                op : "class-clickSubmit"
            });
        }

        $this.addClass("w-btn-disabled").text("设置中...");
        App.postJSON("/teacher/systemclazz/findclazzinfo.vpage<#if multiSubject>?subject=${curSubject}</#if>", {clazzIds: ClazzInfo.tempInfo.base.clazzName}, function(data){
            if(data.success){
                ClazzInfo.tempInfo.adjustClazzs = data.adjustClazzs;
                ClazzInfo.tempInfo.newClazzs = data.newClazzs;
                ClazzInfo.tempInfo.takeoverGroups=[];
                ClazzInfo.tempInfo.tempdeleteIndexs=[];

                if(data.groupStuInfo!=null){
                    for(var i=0;i<data.groupStuInfo.length;i++){
                        var flag = true;
                        for(var j=0;j<ClazzInfo.tempInfo.newClazzs.length;j++){
                            if(ClazzInfo.tempInfo.newClazzs[j].clazzId == data.groupStuInfo[i].clazzId){
                                flag = false;
                                for(var k=0;k<data.groupStuInfo[i].groups.length;k++){
                                    ClazzInfo.tempInfo.newClazzs[j].groups.unshift(data.groupStuInfo[i].groups[k]);
                                }
                            }
                        }
                        if(flag){
                            ClazzInfo.tempInfo.newClazzs.push(data.groupStuInfo[i]);
                        }
                    }
                }

                if(ClazzInfo.tempInfo.newClazzs.length > 0){
                    $.prompt(template("T:点击申请加入", {
                        clazzItem : ClazzInfo.tempInfo.newClazzs
                    }),{
                        title: "系统消息",
                        buttons: {},
                        loaded : function(){
                            $(".v-joinNewClazzsBtn").on("click", newClazzsJoin);
                            $(".v-selectClazzsTeacher").on("click", newClazzsSelectTeacher);
                            $this.removeClass("w-btn-disabled").text("确定");
                        },
                        position: {width: 580}
                    });

                    if($17.getQuery("step") == "showtip"){
                        $17.voxLog({
                            module : "newTeacherRegStep",
                            op : "class-clickAssociate"
                        });
                    }
                }else{
                    /*var actualTeachClazzCount = $("#v-actualTeachClazzCount").val();*/
                    App.postJSON("/teacher/systemclazz/adjustclazzs.vpage<#if multiSubject>?subject=${curSubject}</#if>", {adjustClazzs: ClazzInfo.tempInfo.adjustClazzs/*, actualTeachClazzCount: actualTeachClazzCount*/}, function(data){
                        if(data.success){
                            successGotoUrl(jsonMergeString(ClazzInfo.tempInfo.adjustClazzs, ClazzInfo.tempInfo.newClazzs));
                        } else {
                            $17.alert(data.info || '创建失败！');
                        }
                        $this.removeClass("w-btn-disabled").text("确定");
                    });
                }
            }else{
                $17.alert(data.info);
            }
        });
    }

    function jsonMergeString(a, b){
        var classIds = [];
        if(a){
            for(var i in a){
                classIds.push(a[i]);
            }
        }

        if(b){
            for(var x = 0; x < b.length; x++){
                classIds.push(b[x].clazzId);
            }
        }

        return classIds;
    }
    function newClazzsJoin(){
        var $this = $(this);
        var $clazzId = $this.data("clazzid");
        var $dataType = $this.attr("data-type");
        var $dataSeq = $this.data("seq");

        if($dataType == "no"){
            var clazzs = ClazzInfo.tempInfo.newClazzs;
            for(var i = 0; i < clazzs.length; i++){
                if( clazzs[i].clazzId == $clazzId){
                    ClazzInfo.tempInfo.adjustClazzs.push($clazzId);
                    ClazzInfo.tempInfo.tempdeleteIndexs.push(i);
                }
            }

            if($17.getQuery("step") == "showtip"){
                $17.voxLog({
                    module : "newTeacherRegStep",
                    op : "class-noJoin",
                    type : "teacher"
                });
            }
        }else{
            var $clazzIndex = $this.data("clazz-index");
            var $groupsIndex = $this.data("groups-index");
            var tempClazzs = $.extend(true, [], ClazzInfo.tempInfo.newClazzs);
            var flag = true; // false表示是活跃班级,true表示无老师group
            if(tempClazzs[$clazzIndex].groups[$groupsIndex] !=null && tempClazzs[$clazzIndex].groups[$groupsIndex].teachers && tempClazzs[$clazzIndex].groups[$groupsIndex].teachers.length > 0 &&  tempClazzs[$clazzIndex].groups[$groupsIndex].groupId!=null){
                flag = false;
            }

            if(flag == false){
                ClazzInfo.tempInfo.newClazzs[$clazzIndex].groups = [];
                ClazzInfo.tempInfo.newClazzs[$clazzIndex].groups.push(tempClazzs[$clazzIndex].groups[$groupsIndex]);
                delete ClazzInfo.tempInfo.newClazzs[$clazzIndex].groups[0].students;
            }else{
                ClazzInfo.tempInfo.tempdeleteIndexs.push($clazzIndex);
                var tempInfo = {};
                tempInfo.groupId = tempClazzs[$clazzIndex].groups[$groupsIndex].groupId;
                tempInfo.clazzId = tempClazzs[$clazzIndex].clazzId;
                ClazzInfo.tempInfo.takeoverGroups.push(tempInfo);
            }

            if($17.getQuery("step") == "showtip"){
                $17.voxLog({
                    module : "newTeacherRegStep",
                    op : "class-join",
                    type : "teacher"
                });
            }
        }

        if($dataSeq == "last"){
            for(var i = ClazzInfo.tempInfo.tempdeleteIndexs.length -1;i>=0;i--){
                ClazzInfo.tempInfo.newClazzs.splice(ClazzInfo.tempInfo.tempdeleteIndexs[i],1);
            }

            $.prompt.close();
            /*var actualTeachClazzCount = $("#v-actualTeachClazzCount").val();*/
            App.postJSON("/teacher/systemclazz/adjustclazzs.vpage<#if multiSubject>?subject=${curSubject}</#if>", {newClazzs : ClazzInfo.tempInfo.newClazzs, adjustClazzs : ClazzInfo.tempInfo.adjustClazzs, /*actualTeachClazzCount: actualTeachClazzCount ,*/groupClazzInfo:ClazzInfo.tempInfo.takeoverGroups}, function(data){
                if(data.success){
                    successGotoUrl(jsonMergeString(ClazzInfo.tempInfo.adjustClazzs, ClazzInfo.tempInfo.newClazzs));
                } else {
                    $17.alert(data.info || '创建失败！');
                }
            });
        }else{
            $("#applyClazzBox-" + $dataSeq).hide();
            $("#applyClazzBox-" + ($dataSeq + 1)).show();
        }
    }

    function newClazzsSelectTeacher(){
        var $this = $(this);
        var $clazzId = $this.data("clazzid");
        var $clazzIndex = $this.data("clazz-index");
        var $groupsIndex = $this.data("groups-index");

        if($this.hasClass("active")){
            return false;
        }

        $(".v-joinNewClazzsBtn[data-clazzid='"+$clazzId+"']").attr("data-clazz-index", $clazzIndex).attr("data-groups-index", $groupsIndex);
        $this.addClass("active").siblings().removeClass("active");
    }


    function newTakeoverGroup() {
        var $this = $(this);
        var $clazzId = $this.data("clazzid");
        var $dataType = $this.attr("data-type");
        var $groupsIndex = $this.data("groups-index");
        if($dataType == "no"){
            $.prompt.close();
        }else{
            var tempGroupId=ClazzInfo.tempInfo.groupInfoItem.groups[$groupsIndex].groupId;
            $.ajax({
                url: "/teacher/systemclazz/takeoverclazz.vpage",
                type: "post",
                data: {
                    subject:"${curSubject}",
                    clazzId:$clazzId,
                    groupId:tempGroupId
                },
                success: function (data) {
                    if(data.success){
                        ClazzInfo.tempInfo.adjustClazzs.push($clazzId);
                        successGotoUrl(ClazzInfo.tempInfo.adjustClazzs);
                    }else{
                        $17.alert(data.info, function(){
                            location.reload();
                        });
                    }
                }
            })
            $.prompt.close();
        }
    }

    function selectNoteacherGroup() {
        var $this = $(this);
        var $groupsIndex = $this.data("groups-index");

        if($this.hasClass("active")){
            return false;
        }

        $(".v-takeoverGroupBtn").attr("data-groups-index", $groupsIndex);
        $this.addClass("active").siblings().removeClass("active");
    }

    function successGotoUrl(clazzIds){
        var infoMessage = "班级设置成功！请告诉学生输入你的<#if method == 'MOBILE' && mobile?has_content>手机号<span class='w-red'>${mobile!''}</span><#else>编号<span class='w-red'>${currentUser.id!}</span></#if>加入班级";
        //第一次布置作业
        if($17.getQuery("step") == "showtip" && ${(hasShowTip!false)?string}){
            $17.alert(infoMessage, function(){
                location.href = "${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/index.vpage?step=showtip&index=1";
            });
            $17.voxLog({
                module : "newTeacherRegStep",
                op : "class-clickGotit",
                type : "teacher"
            });
        }else{
            $17.alert(infoMessage, function(){
                location.reload();
                <#--location.href = "/teacher/systemclazz/clazzindex.vpage#/teacher/clazz/managedclazzlist.vpage?subject=${curSubject!}";-->
            });
        }
    }

    $(function(){
        ClazzInfo.init();

        $(".v-clickClazzMenu").on("click", function(){
            var $this = $(this);
            var $thisType = $this.attr("data-type");

            if( $17.isBlank($thisType) ){
                return false;
            }

            if( $thisType == "join" ){
                $(".v-levelClickShowBox:first").click();
            }

            if($("#receiveApplications").find("li[data-type='"+ $thisType +"']").is(":visible") ){
                $("#receiveApplications").find("li[data-type='"+ $thisType +"']").hide();
                $this.removeClass("current");
            }else{
                $this.addClass("current").siblings().removeClass("current");
                $("#receiveApplications").find("li[data-type='"+ $thisType +"']").show().siblings().hide();
                $(".v-level[data-level='" + $('.v-levelClickShowBox.active').data('level') + "']").click();
            }
        });
        if (location.href.indexOf("join") > -1){
            $(".v-clickClazzMenu[data-type='join']").trigger("click");
        }

        //假老师点击弹窗
        $(".v-clickIsFakeMenu").on("click", function(){
            $.prompt("<p style='text-align: center;'>您的账号使用存在异常，该功能受限<br/>如有疑议，请进行申诉</p>", {
                focus: 1,
                title: "系统提示",
                buttons: {"知道了": false, "去申诉": true},
                submit: function(e, v){
                    if(v){
                        window.open ('${(ProductConfig.getMainSiteBaseUrl())!''}/ucenter/teacherfeedback.vpage?type=FAKE', 'feedbackwindow', 'height=500, width=700,top=200,left=450');
                    }
                }
            });
        });

        if($17.getQuery("ref") == "editClazz"){
            setTimeout(function(){
                $(".v-clickClazzMenu[data-type='create']").click();
            }, 200);
        }

        $(document).on("click", ".v-clickService", function(){
            if($17.getQuery("step") == "showtip"){
                $17.voxLog({
                    module : "newTeacherRegStep",
                    op : "class-clickService",
                    type : "teacher"
                });
            }
        });

        //还没有创建班级
        if($17.getQuery("step") == "showtip" && ${(hasShowTip)?string}){
            $("#showTipOptBack").show();
            $(".t-clazzSwitch-box").addClass("w-opt-back-content");
            $(".v-clickClazzMenu[data-type='create']").click();

            $17.voxLog({
                module : "newTeacherRegStep",
                op : "class-load",
                type : "teacher"
            });
        }else{
            $("#showTipOptBack").hide();
        }
    });
</script>

<#--加入班级功能-->
<#macro teacherJoinClazz>
<div class="system-joinClazz">
    <p class="p-hd">下面这些班级有老师、学生使用，可以申请加入：</p>
    <div class="jc-tab">
        <ul>
            <#if eduSystem?? && eduSystem == 'P6'>
                <#list 1..6 as num >
                    <li class="v-levelClickShowBox" data-level="${num}">${num}年级</li>
                </#list>
            <#else>
                <#list 1..5 as num >
                    <li class="v-levelClickShowBox" data-level="${num}">${num}年级</li>
                </#list>
            </#if>
        </ul>
    </div>
    <div id="teacherJoinClazzList-box"></div>
    <div class="jc-footer">
        <p>申请后，对方同意就能看到学生名单。如对方拒绝，你可以：</p>
        <p>1、让学生登录输入你的账号或手机号，设置你为老师。<a href="http://mp.weixin.qq.com/s?__biz=MjM5MzAxMTYwMA==&mid=212606827&idx=4&sn=a903ff07c2df9412cd873d8a3bbd59c4&scene=0&scene=21#wechat_redirect" target="_blank">学生如何操作？</a></p>
        <#if teachClazzs?? && teachClazzs?size gt 0><p>2、验证学生名单，找回学生。<a href="javascript:void(0)" class="data-ImportStudentName">开始验证</a></p></#if>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        var userMobile = "${mobile!}";//当前年级
        var userAuthSuccess = ${((currentUser.fetchCertificationState() == "SUCCESS")!false)?string};//当前年级
        var currentLevel = 1;//当前年级
        var currentClassList = [];//当前年级列表
        //点击年级
        $(".v-levelClickShowBox").on("click", function () {
            var $this = $(this);
            var $level = $this.attr("data-level");

            if ($17.isBlank($level)) {
                return false;
            }

            currentLevel = $level;
            $("#teacherJoinClazzList-box").html("<div style='height:131px; line-height:120px; text-align: center;'>加载中...</div>");
            $this.addClass("active").siblings().removeClass("active");
            $.get("/teacher/systemclazz/findclazzinfobygrade.vpage<#if multiSubject>?subject=${curSubject}</#if>", {clazzLevel: $level}, function (data) {
                if (data.success) {
                    currentClassList = data.clazzs;
                    if(data.groupInfosList == null){
                        data.groupInfosList = [];
                    }
                    if(data.clazzInfoList == null){
                        data.clazzInfoList = [];
                    }
                    ClazzInfo.tempInfo.groupInfosList = data.groupInfosList;
                     //使用 data.clazzInfoList = []; 可以 暂时关闭功能
                    $("#teacherJoinClazzList-box").html(template("T:teacherJoinClazzList-box", {item: data.clazzs,clazzInfoList:data.clazzInfoList}));
                }
            });
        });

        //点击申请加入
        $(document).on({
            mouseenter: function () {
                $(this).find(".hover-box").show();
            },
            mouseleave: function () {
                $(this).find(".hover-box").hide();
            },
            click: function () {
                var $this = $(this);
                var $id = $this.attr("data-id");
                var $request = $this.data("request");
                var $index = $this.attr("data-index");
                var $subject = $this.data('subject') || '';
                var canJoin = $this.data('can_join');
                var verifyCodeSend = false;

                //判断该发出请求的老师姓名是否与申请加入的活跃group内学生姓名一致，如果一致，则弹出失败弹窗
                if(!canJoin){
                    $17.alert("加入失败<br />请当面联系改老师加你进班吧！");
                    return false;
                }

                if ($request) {
                    //取消
                    $.prompt("<div style='text-align: center;'>确定取消申请吗？</div>", {
                        focus: 1,
                        title: "系统提示",
                        buttons: {"取消": false, "确定": true},
                        position: {width: 500},
                        submit: function (e, v) {
                            if (v) {
                                App.postJSON("/teacher/systemclazz/cancelclazzapps.vpage<#if  multiSubject>?subject=${curSubject}</#if>", currentClassList[$index], function (data) {
                                    if (data.success) {
                                        currentClassRefresh();
                                    } else {
                                        $17.alert(data.info);
                                    }
                                });
                            }
                        }
                    });
                } else {
                    //非认证老师或没有手机号
                    if (!userMobile && !userAuthSuccess) {
                        $17.alert('您还没有绑定手机 <a class="w-btn w-btn-green w-btn-mini" href="/teacher/center/index.vpage#/teacher/center/securitycenter.vpage" target="_blank" onclick="$.prompt.close();">立即绑定手机</a>');
                        return false;
                    }

                    //加入
                    $.prompt(template("T:发送申请提示", {
                        userAuthSuccess: userAuthSuccess,
                        userMobile: userMobile,
                        subject: $subject
                    }), {
                        focus: 1,
                        title: "系统提示",
                        buttons: {"取消": false, "发送申请": true},
                        position: {width: 500},
                        submit: function (e, v) {
                            var teacherName = $('#teacherName');
                            if (v) {
                                function sendVer() {
                                    currentClassList[$index].teacherName = teacherName.val();
                                    currentClassList[$index].teacherSubject = $subject;
                                    App.postJSON("/teacher/systemclazz/requestjoinclazz.vpage<#if  multiSubject>?subject=${curSubject}</#if>", currentClassList[$index], function (data) {
                                        if (data.success) {
                                            if (data.join) {
                                                $17.alert("申请加入成功", function () {
                                                    $.prompt.close();
                                                    location.href = "/teacher/systemclazz/clazzindex.vpage";
                                                });
                                            } else {
                                                $17.alert("申请成功<br><br>请当面提醒对方老师通过您的请求", function () {
                                                    currentClassRefresh();
                                                    $.prompt.close();
                                                });
                                            }
                                        } else {
                                            //$17.alert(data.info);
                                            teacherName.siblings(".errorMsg").show().find(".info").text(data.info);
                                        }
                                    });
                                }

                                //认证老师
                                if (userAuthSuccess) {
                                    if ($17.isBlank(teacherName.val()) || !$17.isCnString(teacherName.val())) {
                                        teacherName.siblings(".errorMsg").show().find(".info").text("请填写正确的老师姓名");
                                        teacherName.focus();
                                        return false;
                                    } else {
                                        teacherName.siblings(".errorMsg").hide();
                                    }

                                    sendVer();
                                } else {
                                    var $smsCode = $(".v-smsCode");
                                    if ($17.isBlank(teacherName.val()) || !$17.isCnString(teacherName.val())) {
                                        teacherName.siblings(".errorMsg").show().find(".info").text("请填写正确的老师姓名");
                                        return false;
                                    } else {
                                        teacherName.siblings(".errorMsg").hide();
                                        if (!$17.isNumber($smsCode.val())) {
                                            $smsCode.addClass("w-int-error");
                                            return false;
                                        }
                                        if(!verifyCodeSend){
                                            $.post("/teacher/systemclazz/verifyTCACode.vpage", {code: $smsCode.val()}, function (data) {
                                                if (data.success) {
                                                    verifyCodeSend = data.success;
                                                    $(".v-verify-form").hide();
                                                    sendVer();
                                                    $smsCode.siblings(".errorMsg").hide();
                                                } else {
                                                    $smsCode.siblings(".errorMsg").show().find(".info").text(data.info);
                                                }
                                            });
                                        }else{
                                            sendVer();
                                            $smsCode.siblings(".errorMsg").hide();
                                        }

                                    }
                                }
                                return false;
                            }
                        }
                    });
                }
            }
        }, ".v-selectClazzTeacher");

        //点击创建班级
        $(document).on("click", ".v-clickCreateClazz", function () {
            $(".v-clickClazzMenu[data-type='create']").click();
        });

        //输入
        $(document).on("keyup", ".v-smsCode", function () {
            $(this).removeClass("w-int-error").siblings(".errorMsg").hide();
        });

        //发送验证码
        $(document).on("click", ".v-getSmsCode", function () {
            var $this = $(this);

            $.post("/teacher/systemclazz/sendTCACode.vpage", {mobile: userMobile}, function (data) {
                if (data.success) {
                    $17.getSMSVerifyCode($this, data);
                } else {
                    $this.siblings("span").html(data.info);
                }
            })
        });

        function currentClassRefresh() {
            $(".v-levelClickShowBox[data-level='" + currentLevel + "']").click();
        }

        //设置班级数
        $17.minusPlusInputEvent({
            box : "#Anchor",
            maxCount : 12
        }, function(opt){
            ClazzInfo.tempInfo.base.classesCount = opt.currentCount;
        });

        $(document).on("click", ".v-takeovergroup", function () {
            var $this = $(this);
            var data_clazzId = $this.attr("data-clazzId");
            ClazzInfo.tempInfo.groupInfoItem = null;
            for (var i = 0; i < ClazzInfo.tempInfo.groupInfosList.length; i++) {
                if (data_clazzId == ClazzInfo.tempInfo.groupInfosList[i].clazzId) {
                    ClazzInfo.tempInfo.groupInfoItem = ClazzInfo.tempInfo.groupInfosList[i];
                    break;
                }
            }

            if (ClazzInfo.tempInfo.groupInfoItem != null) {
                $.prompt(template("T:点击申请接管", {
                    groupInfoItem: ClazzInfo.tempInfo.groupInfoItem
                }), {
                    title: "系统消息",
                    buttons: {},
                    loaded: function () {
                        $(".v-takeoverGroupBtn").on("click", newTakeoverGroup);
                        $(".v-selectNoteacherGroup").on("click", selectNoteacherGroup);
                    },
                    position: {width: 580}
                });
            } else {
                $17.alert("已被其他老师接管", function () {
                    location.reload();
                });
            }
        });
    });
</script>
<script type="text/html" id="T:teacherJoinClazzList-box">
    <div class="jc-inner">
        <ul>
            <%for(var i = 0; i < clazzInfoList.length; i++){%>
            <li>
                <h4><%=clazzInfoList[i].clazzName%> </h4>
                无<%=clazzInfoList[i].subjectName%>老师
                <p class="a-padding"><a href="javascript:void(0)" class="add-btn v-takeovergroup" style="background: #0979ca" data-clazzId="<%=clazzInfoList[i].clazzId%>" >接管</a></p>
            </li>
            <%}%>

            <%for(var i = 0; i < item.length; i++){%>
            <li data-id="<%=item[i].clazzId%>" class="v-selectClazzTeacher <%if(item[i].hasRequest){%>active<%}%>" data-can_join="<%= (item[i].canJoin && item[i].canJoin.toString()) || 'true'%>" data-subject="<%= (item[i].teachers && item[i].teachers.length > 0 && item[i].teachers[0].subject) || ''%>" data-index="<%=i%>" data-request="<%=item[i].hasRequest.toString()%>">
                <h4><%=item[i].name%></h4>
                <%var teachers = item[i].teachers%>
                <%for(var t in teachers){%>
                    <%if(teachers.length > 2){%>
                        <%if(teachers[t].subject != "语文"){%>
                        <p><%=teachers[t].subject%>：<%=(teachers[t].name ? teachers[t].name.substring(0, 1) : '--')%>老师</p>
                        <%}%>
                    <%}else{%>
                        <p><%=teachers[t].subject%>：<%=(teachers[t].name ? teachers[t].name.substring(0, 1) : '--')%>老师</p>
                    <%}%>
                <%}%>
                <%if(item[i].hasRequest){%>
                    <div class="hover-box" data-type="取消" style="display: none;">
                        <p>申请中，请等待对方同意</p>
                        <p class="a-padding"><a href="javascript:void(0)" class="add-btn add-btn-red">取消申请</a></p>
                    </div>
                <%}else{%>
                    <div class="hover-box" data-type="加入" style="display: none;">
                        <p class="a-padding"><a href="javascript:void(0)" class="add-btn">申请加入</a></p>
                    </div>
                <%}%>
            </li>
            <%}%>
            <li><a href="javascript:void(0)" class="add-box v-clickCreateClazz"><i class="add-icon"></i><p>创建新班级</p></a></li>
        </ul>
    </div>
</script>
<script type="text/html" id="T:发送申请提示">
    <div class="w-form-table">
        <div style="text-align: center; padding: 20px 0 10px;">
            请填写对方<%=subject%>老师姓名，申请在该班教<%=subject%>
        </div>
        <dl>
            <dd>
                <input id="teacherName" type="text" maxlength="6" class="w-int" value="" placeholder="姓名">
                <span class="w-form-misInfo"><strong class="info">（*必填）</strong></span>
                <span class="w-form-misInfo w-form-info-error errorMsg" style="display: none;"><i class="w-icon-public w-icon-error"></i><strong class="info"></strong></span>
            </dd>
        </dl>
    </div>
    <%if(!userAuthSuccess){%>
        <div class="w-form-table v-verify-form">
            <div style="text-align: center; padding: 20px 0 10px;">
                验证手机号,并发送请求：<%=userMobile%>
                <a class="w-btn w-btn-mini v-getSmsCode" href="javascript:void(0);" style="width: 150px;"><span>免费获取验证码</span></a>
            </div>
            <dl>
                <dt>短信验证码：</dt>
                <dd>
                    <input type="text" maxlength="6" class="w-int v-smsCode" value="" placeholder="请输入收到的短信验证码">
                    <span class="w-form-misInfo"><strong class="info">（*必填）</strong></span>
                    <span class="w-form-misInfo w-form-info-error errorMsg" style="display: none;"><i class="w-icon-public w-icon-error"></i><strong class="info"></strong></span>
                </dd>
            </dl>
        </div>
    <%}%>
</script>
</#macro>