<#assign hasShowTip = ((teachClazzs?size lt 1)!true)/>
    <div class="title-bar clearfix">
        <div class="switch clearfix">
            <#if hasShowTip>
                <a <#--style="display:none" -->class="v-clickClazzMenu setup" data-type="create"><span>创建/管理班级</span></a>
            <#else>
                <a class="v-clickClazzMenu add" data-type="join"><em></em><span>加入班级</span></a>
                <a class="v-clickClazzMenu setup" data-type="create"><em></em><span>创建/管理班级</span></a>
            </#if>
            <#if sendApplications?has_content>
                <a href="javascript:;" class="v-clickClazzMenu request" data-type="request"><em></em><span>已有请求</span><#if (sendApplicationsCount gt 0)!false><div class="w-icon-arrow w-icon-redInfo">${sendApplicationsCount!0}</div></#if></a>
            </#if>
        </div>
        <div class="title" id="helpHoverContent">
            <p>我的班级</p>
        </div>
    </div>

    <#--content- start-->
    <ul id="receiveApplications">
    <#--创建班级-->
        <li data-type="create" style="display: none;"><div id="Anchor" class="t-clazzSwitch-box"></div></li>
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

<script id="t:创建班级" type="text/html">
    <style>
        .w-select:hover ul{display: block !important;}
    </style>
    <% privateLevels = ["六年级", "七年级", "八年级", "九年级", "", "高一", "高二", "高三"]%>
    <#if (currentTeacherDetail.isSeniorTeacher())!false>
        <#if eduSystem == 'S4'>
            <%var minLevel = 9%>
            <%var maxLevel = 13%>
            <%var allLevels = ["九年级", "", "高一", "高二", "高三"]%>
            <#--说明：此处九年级新增的level在原先代码的基础上应当为10，但由于后端的clazzLevel为10的值已经被其他占用，故使用9，此时中间中断一位，
                故allLevels中第2位增加一位空值占用，在使用minLevel、maxLevel、allLevels的地方增加非10判断-->
        <#else>
            <%var minLevel = 11%>
            <%var maxLevel = 13%>
            <%var allLevels = ["高一", "高二", "高三"]%>
        </#if>
    <#else >
        <#if eduSystem == 'J4'>
            <%var minLevel = 6%>
            <%var maxLevel = 9%>
            <%var allLevels = ["六年级", "七年级", "八年级", "九年级"]%>
        <#else>
            <%var minLevel = 7%>
            <%var maxLevel = 9%>
            <%var allLevels = ["七年级", "八年级", "九年级"]%>
        </#if>
    </#if>
    <div class="class-module">
        <%var notClazzFlag=false, onSelectClazz=0%>
        <% for(var k = 0; k < clazzName.length; k++){ %>
            <%if(clazzName[k].clazzs.length > 0){%>
                <% for(var i = 0, clazzLevel = clazzName[k].clazzs; i < clazzLevel.length; i++){ %>
                    <#--为10时跳过，跳过原因：45行-->
                    <% if (clazzName[k].clazzLevel != 10) { %>
                        <%notClazzFlag = true; onSelectClazz += 1;%>
                    <%}%>
                <%}%>
            <%}%>
        <%}%>
        <% for(var s = 6; s <= 13; s++){ %>
            <%if(walkingClazz[s] && walkingClazz[s].length > 0 && s != 10){%>
                <% for(var h = 0, items = walkingClazz[s]; h < items.length; h++){ %>
                    <%notClazzFlag = true; onSelectClazz += 1;%>
                <%}%>
            <%}%>
        <%}%>
        <#--start-->
        <div class="module-head clearfix">
            <div class="title">创建班级</div>
            <select class="v-changeClazzType">
                <option value="ABM" <%if(clazzType == "ABM"){%>selected="true"<%}%>>行政班</option>
                <option value="WLC" <%if(clazzType == "WLC"){%>selected="true"<%}%>>教学班</option>
            </select>
            <div class="prom">可选择创建教学班</div>
            <div class="global-ques center">
                <div class="text">学生根据自己的兴趣愿望，选择相应的老师和科目形成的班级</div>
            </div>
        </div>
        <div class="module-head clearfix">
            <div class="title middle">选择班级</div>
            <div class="list clearfix">
                <% if(!notClazzFlag){ %>
                    <div style="color:#848d8d">你还没有创建班级哟~</div>
                <% }else{ %>
                    <% for(var k = 0; k < clazzName.length; k++){ %>
                        <% for(var i = 0, clazzLevel = clazzName[k].clazzs; i < clazzLevel.length; i++){ %>
                            <#--为10时跳过，跳过原因：45行-->
                            <% if (clazzName[k].clazzLevel != 10) { %>
                                <a class="class v-clickSelectClazzname" data-level="<%=clazzName[k].clazzLevel%>" data-id="<%=clazzLevel[i].id%>" data-clazztype="ABM" data-clazzname="<%=clazzLevel[i].name%>" class="cl-title v-clickSelectClazzname" ><%=privateLevels[clazzName[k].clazzLevel-6]%><%=clazzLevel[i].name%> </a>
                            <% } %>
                        <% } %>
                    <% } %>

                    <% for(var s = 6; s <= 13; s++){ %>
                        <%if(walkingClazz[s] && walkingClazz[s].length > 0 && s != 10){%>
                            <% for(var h = 0, items = walkingClazz[s]; h < items.length; h++){ %>
                                <a class="class v-clickSelectWalkingClazz" data-level="<%= s %>" data-clazzname="<%=items[h].name%>"  data-clazztype="WLC" class="cl-title v-clickSelectWalkingClazz" ><%=privateLevels[s-6]%><%=items[h].name%></a>
                            <%}%>
                        <%}%>
                    <%}%>
                <% } %>
            </div>
        </div>
        <div class="module-head clearfix">
            <div class="title middle">选择班级</div>
            <%if(clazzType == "WLC"){%>
                <div class="switch-tab clearfix">
                    <% for(var i = minLevel; i <= maxLevel; i++){ %>
                        <#--为10时跳过，跳过原因：45行-->
                        <% if (i != 10) { %>
                            <a class="v-level tab <% if(level == i){ %>active<% } %>" data-level="<%= i %>" style="width: 84px;"><%= allLevels[i-minLevel] %></a>
                        <% } %>
                    <% } %>
                </div>
                <div class="switch-main">
                    <div class="clearfix" style="display: block">
                        <%if(levelClazz.length > 0){%>
                            <% for(var i = 0; i < levelClazz.length; i++){ %>
                                <%var isExist = false%>
                                <%var $clazzName = levelClazz[i].name %>
                                <%if(walkingClazz[level]){%>
                                    <% for(var h = 0, items = walkingClazz[level]; h < items.length; h++){ %>
                                        <%if(items[h].name == $clazzName){%><%var isExist = true%><%break%><%}%>
                                    <%}%>
                                <%}%>
                                <a class="label icon <%=(isExist ? 'active':'')%> v-clickSelectWalkingClazz" style="border:none; width: 140px;" data-clazzname="<%= levelClazz[i].name %>" data-clazztype="WLC" data-level="<%= level %>" data-type="walking" data-id="<%= levelClazz[i].id %>" title="可多选" >
                                    <span class="w-icon-md" style="width: 110px;"><%= levelClazz[i].name %> <span class="w-red" style="display: none;">可多选</span></span>
                                </a>
                            <% } %>
                        <%}else{%>
                            <div style="padding: 30px 0;">暂无班级</div>
                        <%}%>
                    </div>
                </div>
            <%}else{%>
                <div class="switch-tab clearfix">
                    <% for(var i = minLevel; i <= maxLevel; i++){ %>
                        <#--为10时跳过，跳过原因：45行-->
                        <% if (i != 10) { %>
                            <a class="v-level tab <% if(level == i){ %>active<% } %>" data-level="<%= i %>" style="width: 84px;"><%= allLevels[i-minLevel] %></a>
                        <% } %>
                    <% } %>
                </div>
                <div class="switch-main">
                    <div class="clearfix" style="display: block">
                        <% if(level != null){ %>
                            <%if(levelClazz.length > 0){%>
                                <% for(var i = 0; i < levelClazz.length; i++){ %>
                                    <a class="label icon <%=(levelClazz[i].checked ? 'active':'')%> v-clickSelectClazzname" data-clazzname="<%= levelClazz[i].name %>" data-clazztype="ABM" data-level="<%= level %>" data-id="<%= levelClazz[i].id %>" title="可多选" class="">
                                        <span class="w-icon-md"><%= levelClazz[i].name %> <span class="w-red" style="display: none;">可多选</span></span>
                                    </a>
                                <% } %>
                            <%}else{%>
                                <div style="padding: 30px 0;">暂无班级</div>
                            <%}%>
                        <% } %>
                    </div>
                </div>
            <%}%>
            <div style=" color: #f00; padding: 10px 0 0;">
                <#if .now lte "2017-08-11 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss")>
                    系统会在8月10日统一升年级，请您选择本学期的年级！
                <#else>
                    <#--新学期马上要开始了，请选择班级在新学年的年级哦！-->
                </#if>
                </div>
            <div class="module-foot"><a href="javascript:;" class="btn v-next ">确定</a><div class="text">选好后点“确定</div></div>
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
        div.jqi .jqiclose { color: #fff; border: 0 none; height: 34px; line-height: 30px; }
        div.jqi .jqititle { padding: 5px 0 5px 20px; color: #fff; font-size: 16px; line-height: 24px; background-color: #599d41; border: 0 none; }
        div.jqi .jqimessage .t-pubfooter-btn { padding: 0 0 30px; }
        .t-applyClazz-addMore-box .abt-content dl.active { border-color: #599d41; background-color: #e3eedf; }
        .w-btn,.w-btn:hover { color: #fff; background-color: #599d41; border-color: #4e8b39; }
        .w-btn-well { padding: 5px 0; width: 98px; font-size: 14px; line-height: 20px; }
        .w-border-blue { border-color: #599d41; }
        .w-btn-cyan,.w-btn-green,.w-btn-cyan:hover,.w-btn-green:hover { color: #5d5f5f; background-color: #f6f6f6; border-color: #ebebeb; }
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
    var tempLevelClazz = {
        "ABM":{},
        "WLC":{}
    };

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
                clazzType : "ABM",
                artScienceType : "UNKNOWN",
                level        : null,
                levelClazz : [],
                walkingClazz : {},
                clazzName    : [],
                classesCount : 0,
                initClazzIds : [],
                initWalkingClazzNameIdMap : {}
            }
        },
        eventConfig : {
            ".v-changeClazzType -> change"                : clazzType_change,
            ".v-changeArtScience -> change"               : artScience_change,
            ".v-level -> click"                         : level_click,
            ".v-clickSelectClazzname -> click"          : clazzName_click,
            ".v-clickSelectClazzname -> mouseenter"     : clazzName_mouseEnter,
            ".v-clickSelectClazzname -> mouseleave"     : clazzName_mouseLeave,
            ".v-clickSelectWalkingClazz -> click"       : walkingClazzName_click, //添加教学班
            ".v-clickSelectWalkingClazz -> mouseenter"  : clazzName_mouseEnter,
            ".v-clickSelectWalkingClazz -> mouseleave"  : clazzName_mouseLeave,
            ".v-next -> click"                          : next_button_click
        },
        clazzCount : function(){
            var clazzAllCount = 0;
            var cl = this.tempInfo.base.clazzName;
            var wcl = this.tempInfo.base.walkingClazz;
            //行政班count
            for(var b = 0; b < cl.length; b++) {
                clazzAllCount = clazzAllCount + cl[b].clazzs.length;
            }
            //教学班count
            for(var i in wcl) {
                clazzAllCount = clazzAllCount + wcl[i].length;
            }
            return clazzAllCount;
        },
        setClazzName : function(level, obj){
            var $this = this;
            var $indexKey = 0;
            var cl = this.tempInfo.base.clazzName;
            var clazzAllCount = $this.clazzCount();
            var maxClazzCount = ${((curSubject == "ENGLISH")!false)?string('8', '4')};
            var $currentABM = 'ABM';

            for(var t= 0, tcl = tempLevelClazz[$currentABM][level]; t < tcl.length; t++){
                if(tcl[t].id == obj.id){
                    if(clazzAllCount >= maxClazzCount && !tcl[t].checked){
                        $17.alert("最多教 <b class='w-red'>"+ maxClazzCount +"</b> 个班级");
                        return false;
                    }

                    if( tcl[t].checked ){
                        $indexKey = t;
                        if ($.inArray(Number(obj.id), $this.tempInfo.base.initClazzIds) > -1) {
                            $.prompt("<div class='w-ag-center'>不再担任这个班的${curSubjectText}老师？</div>", {
                                focus: 1,
                                title: "系统提示",
                                buttons: {"不教了": true, "继续教": false},
                                position: {width: 500},
                                submit: function (e, v) {
                                    if (v) {
                                        tempLevelClazz[$currentABM][level][$indexKey].checked = false;
                                        lookLevel();
                                        $this.refresh();
                                    }
                                }
                            });
                        } else {
                            tempLevelClazz[$currentABM][level][$indexKey].checked = false;
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
            var $this = this;

            <#if (currentTeacherDetail.isSeniorTeacher())!false>
                <#if eduSystem == 'S4'>
                    clazz_click(9, function(){});
                <#else>
                    clazz_click(11, function(){});
                </#if>
            <#else >
                <#if eduSystem == 'J4'>
                    clazz_click(6, function(){});
                <#else>
                    clazz_click(7, function(){});
                </#if>
            </#if>
            //获取班级
            // 0b0000000011000010
            $.get("/teacher/systemclazz/chooseclazz.vpage", {infoType : 194}, function(data){
                if(data.success){
                    $this.tempInfo.base.clazzName = data.teachClazzs;
                    /*$this.tempInfo.base.classesCount = data.actualTeachClazzCount;*/
                    if(data.teachWalkingClazzs){
                        $this.tempInfo.base.walkingClazz = data.teachWalkingClazzs;
                        for (var key in data.teachWalkingClazzs) {
                            var levelClazz = data.teachWalkingClazzs[key];
                            for (var c in levelClazz) {
                                var obj = levelClazz[c];
                                $this.tempInfo.base.initWalkingClazzNameIdMap[obj.name] = obj.id;
                            }
                        }
                    }
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

    //选择创建班级类型
    function clazzType_change(){
        var $this = $(this);

        ClazzInfo.tempInfo.base.clazzType = $this.val();

        <#if (currentTeacherDetail.isSeniorTeacher())!false>
            clazz_click(11, function(){});
        <#else>
            clazz_click(7, function(){});
        </#if>
        return false;
    }

    //选择文理科
    function artScience_change(){
        var $this = $(this);

        ClazzInfo.tempInfo.base.artScienceType = $this.val();
        ClazzInfo.refresh();
        return false;
    }

    //年级被点
    function level_click(){
        ClazzInfo.tempInfo.base.level = $(this).attr("data-level");

        if(tempLevelClazz[ClazzInfo.tempInfo.base.clazzType][ClazzInfo.tempInfo.base.level]){
            ClazzInfo.tempInfo.base.levelClazz = tempLevelClazz[ClazzInfo.tempInfo.base.clazzType][ClazzInfo.tempInfo.base.level];
            ClazzInfo.refresh();
        }else{
            // 0b0000000011000001
            var infoType = ClazzInfo.tempInfo.base.clazzType == "ABM" ? 129 : 65;
            $.get("/teacher/systemclazz/chooseclazz.vpage", {infoType : infoType, clazzLevel: ClazzInfo.tempInfo.base.level}, function(data){
                if(data.success){
                    tempLevelClazz[ClazzInfo.tempInfo.base.clazzType][ClazzInfo.tempInfo.base.level] = data.clazzs;
                    ClazzInfo.tempInfo.base.levelClazz = tempLevelClazz[ClazzInfo.tempInfo.base.clazzType][ClazzInfo.tempInfo.base.level];
                    ClazzInfo.refresh();
                }
            });
        }

        return false;
    }

    //点击班级
    function clazz_click(level, callback, clazzType){
        if($17.getQuery("step") == "showtip"){
            $17.voxLog({
                module : "newTeacherRegStep",
                op : "class-clickClazzList"
            });
        }

        ClazzInfo.tempInfo.base.level = level;
        var $currentClazzType = ClazzInfo.tempInfo.base.clazzType;

        if(!$17.isBlank(clazzType)){
            $currentClazzType = clazzType
        }

        if(tempLevelClazz[$currentClazzType][ClazzInfo.tempInfo.base.level]){
            if($17.isBlank(clazzType)){
                ClazzInfo.tempInfo.base.levelClazz = tempLevelClazz[$currentClazzType][ClazzInfo.tempInfo.base.level];
            }

            callback();
            ClazzInfo.refresh();
        }else{
            // 0b0000000010000001
            var infoType = $currentClazzType == "ABM" ? 129 : 65;
            $.get("/teacher/systemclazz/chooseclazz.vpage", {infoType : infoType, clazzLevel: ClazzInfo.tempInfo.base.level}, function(data){
                if(data.success){
                    tempLevelClazz[$currentClazzType][ClazzInfo.tempInfo.base.level] = data.clazzs;

                    if($17.isBlank(clazzType)){
                        ClazzInfo.tempInfo.base.levelClazz = tempLevelClazz[$currentClazzType][ClazzInfo.tempInfo.base.level];
                    }
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
        var $clazzType = $this.attr("data-clazztype");

        clazz_click($level, function(){
            ClazzInfo.setClazzName($level, {
                id : $this.attr("data-id"),
                name : $this.attr("data-clazzname")
            });
        }, $clazzType);
        return false;
    }

    //添加教学班级
    function walkingClazzName_click(){
        var $this = $(this);
        var $level = $this.attr("data-level");
        var $clazzName = $this.attr("data-clazzname");
        var $walkingIds = ClazzInfo.tempInfo.base.walkingClazz;
        var maxClazzCount = ${((curSubject == "ENGLISH")!false)?string('8', '4')};

        if($17.isBlank($level) || $17.isBlank($clazzName)){
            return false;
        }

        if($17.getQuery("step") == "showtip"){
            $17.voxLog({
                module : "newTeacherRegStep",
                op : "class-walkingClazz"
            });
        }

        //exist remove
        var $existClazzs = new nameIsExist($clazzName, $walkingIds[$level]);
        if($existClazzs.isExist){
            if ($walkingIds[$level][$existClazzs.indexs].id != 0) {
                $.prompt("<div class='w-ag-center'>不再担任这个班的${curSubjectText}老师？</div>", {
                    focus: 1,
                    title: "系统提示",
                    buttons: {"不教了": true, "继续教": false},
                    position: {width: 500},
                    submit: function (e, v) {
                        if (v) {
                            $walkingIds[$level].splice($existClazzs.indexs, 1);
                            ClazzInfo.refresh();
                        }
                    }
                });
            } else {
                $walkingIds[$level].splice($existClazzs.indexs, 1);
                ClazzInfo.refresh();
            }
        }else{
            if(!$this.hasClass("active") && ClazzInfo.clazzCount() >= maxClazzCount){
                $17.alert("最多教 <b class='w-red'>"+maxClazzCount+"</b> 个班级");
                return false;
            }

            //notExist add object
            if(!$walkingIds[$level]){
                $walkingIds[$level] = [];
            }

            var $id = ClazzInfo.tempInfo.base.initWalkingClazzNameIdMap[$clazzName];
            if ($id === undefined) {
                $id = 0;
            }
            setName({id : 0, name : $clazzName}, $walkingIds[$level]);
            ClazzInfo.refresh();
        }

        //设置班级
        function setName(obj, items){
            items.push(obj);
        }

        //是否存在Array
        function nameIsExist(clazzName, items){
            this.isExist = false;
            this.indexs = null;

            if(items){
                for(var i = 0; i < items.length; i++){
                    if(items[i].name == clazzName){
                        this.isExist = true;
                        this.indexs = i;
                        break;
                    }
                }
            }
            return this;
        }
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

        if (ClazzInfo.clazzCount() < 1) {
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
        /*, walkingClazz : ClazzInfo.tempInfo.base.walkingClazz*/
        App.postJSON("/teacher/systemclazz/findclazzinfo.vpage", {clazzIds: ClazzInfo.tempInfo.base.clazzName}, function(data){
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
                    App.postJSON("/teacher/systemclazz/adjustclazzs.vpage", {
                        adjustClazzs: ClazzInfo.tempInfo.adjustClazzs,
                        adjustWalkingClazzs : ClazzInfo.tempInfo.base.walkingClazz,
                        /*actualTeachClazzCount: actualTeachClazzCount,*/
                        artScienceType: ClazzInfo.tempInfo.base.artScienceType}, function(data){
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
            App.postJSON("/teacher/systemclazz/adjustclazzs.vpage", {newClazzs : ClazzInfo.tempInfo.newClazzs, adjustClazzs : ClazzInfo.tempInfo.adjustClazzs, adjustWalkingClazzs : ClazzInfo.tempInfo.base.walkingClazz, /*actualTeachClazzCount: actualTeachClazzCount,*/groupClazzInfo:ClazzInfo.tempInfo.takeoverGroups}, function(data){
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
        <#if (currentTeacherDetail.isSeniorTeacher())!false>
            var infoMessage = "创建成功！请选择班级文理科，并及时导入学生哦！";
        <#else>
            var infoMessage = "创建成功！请及时导入学生哦！";
        </#if>
        //第一次布置作业
        if($17.getQuery("step") == "showtip" && ${(hasShowTip!false)?string}){
                $.prompt(template("T:教学生如何使用Popup", {}), {
                    classes: {
                        prompt: 'bs-schoolHelpPopup',
                        title : 'w-hide',
                        close: 'w-hide'
                    },
                    title: "如何添加学生？",
                    buttons: {},
                    position: {width: 600}
                });

                $17.voxLog({
                    module : "newTeacherRegStep",
                    op : "class-clickGoToJunior",
                    type : "teacher"
                });
        }else{
            $17.alert(infoMessage, function(){
                location.href = "/teacher/systemclazz/clazzindex.vpage";
            });
        }
    }

    $(function(){
        ClazzInfo.init();

        $(".v-clickClazzMenu").on("click", function(){
            // 判断是否是第三方，是则禁止操作
            if (isThirdParty == 'true') {
                isThirdPartyTip();
                return false;
            }

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
                $this.removeClass("active");
            }else{
                $this.addClass("active").siblings().removeClass("active");
                $("#receiveApplications").find("li[data-type='"+ $thisType +"']").show().siblings().hide();
            }
        });

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

        if($17.getQuery("step") == "showtip" && ${(hasShowTip)?string}) {
            //还没有创建班级
            $("#showTipOptBack").show();
            $(".t-clazzSwitch-box").addClass("w-opt-back-content");
            $(".v-clickClazzMenu[data-type='create']").click();

            $17.voxLog({
                module: "newTeacherRegStep",
                op: "class-load",
                type: "teacher"
            });
        }else{
            $("#showTipOptBack").hide();
        }
    });
</script>

<#--加入班级功能-->
<#macro teacherJoinClazz>
<div class="class-module">
    <div class="module-title">下面这些班级有老师、学生使用，可以申请加入：</div>
    <div class="module-switch">
        <div class="clearfix">
            <#if (currentTeacherDetail.isSeniorTeacher())!false>
                <#if eduSystem == 'S4'>
                    <#assign levelNames=["九年级", "", "高一", "高二", "高三"]>
                    <#list 9..13 as num >
                        <#--为10时跳过，跳过原因：45行-->
                        <#if num != 10 >
                            <a href="javascript:;" class="tab v-levelClickShowBox" data-level="${num}">${levelNames[num - 9]}</a>
                        </#if>
                    </#list>
                <#else>
                    <#assign levelNames=["高一", "高二", "高三"]>
                    <#list 11..13 as num >
                        <a href="javascript:;" class="tab v-levelClickShowBox" data-level="${num}">${levelNames[num - 11]}</a>
                    </#list>
                </#if>
            <#else>
                <#if eduSystem == 'J4'>
                    <#assign levelNames=["六年级", "七年级", "八年级", "九年级"]>
                    <#list 6..9 as num >
                        <a href="javascript:;" class="tab v-levelClickShowBox" data-level="${num}">${levelNames[num - 6]}</a>
                    </#list>
                <#else>
                    <#assign levelNames=["七年级", "八年级", "九年级"]>
                    <#list 7..9 as num >
                        <a href="javascript:;" class="tab v-levelClickShowBox" data-level="${num}">${levelNames[num - 7]}</a>
                    </#list>
                </#if>
            </#if>
        </div>
        <div class="clearfix">
            <div id="teacherJoinClazzList-box"></div>
        </div>
    </div>
    <div class="module-foot border"><div class="text">申请后，对方同意就能看到学生名单。</div></div>
</div>
<script type="text/javascript">
    $(function(){
        var userMobile = "${mobile!}";//当前年级
        var userAuthSuccess = ${((currentUser.fetchCertificationState() == "SUCCESS")!false)?string};//当前年级
        var currentLevel = 1;//当前年级
        var currentClassList = [];//当前年级列表
        //点击年级
        $(document).on("click", ".v-levelClickShowBox", function(){
            var $this = $(this);
            var $level = $this.attr("data-level");

            if($17.isBlank($level)){
                return false;
            }

            currentLevel = $level;
            $("#teacherJoinClazzList-box").html("<div style='height:131px; line-height:120px; text-align: center;'><span style='font-size:16px;'>加载中...</span></div>");
            $this. addClass("active").siblings().removeClass("active");
            $.get("/teacher/systemclazz/findclazzinfobygrade.vpage", {clazzLevel : $level}, function(data){
                if(data.success){
                    currentClassList = data.clazzs;
                    if(data.groupInfosList == null){
                        data.groupInfosList = [];
                    }
                    if(data.clazzInfoList == null){
                        data.clazzInfoList = [];
                    }
                    ClazzInfo.tempInfo.groupInfosList = data.groupInfosList;
                    //使用data.clazzInfoList = []; 可以 暂时关闭功能
                    $("#teacherJoinClazzList-box").html( template("T:teacherJoinClazzList-box", {clazzList : data.clazzs, noTeacherClazzList:data.clazzInfoList}) );
                }
            });
        });

        //点击申请加入
        $(document).on({
            mouseenter : function(){
                $(this).find(".hover-box").show();
            },
            mouseleave : function(){
                $(this).find(".hover-box").hide();
            },
            click : function(){
                var $this = $(this);
                var $id = $this.attr("data-id");
                var $request = $this.data("request");
                var $index = $this.attr("data-index");

                if($request){
                    //取消
                    $.prompt("<div style='text-align: center;'>确定取消申请吗？</div>", {
                        focus : 1,
                        title: "系统提示",
                        buttons: { "取消": false, "确定": true },
                        position: {width: 500},
                        submit : function(e, v){
                            if(v){
                                App.postJSON("/teacher/systemclazz/cancelclazzapps.vpage", currentClassList[$index], function(data){
                                    if(data.success){
                                        currentClassRefresh();
                                    }else{
                                        $17.alert(data.info);
                                    }
                                });
                            }
                        }
                    });
                }else{
                    //非认证老师或没有手机号
                    if(!userMobile && !userAuthSuccess){
                        $17.alert('您还没有绑定手机 <a class="w-btn w-btn-green w-btn-mini" href="/teacher/center/index.vpage#/teacher/center/securitycenter.vpage" target="_blank" onclick="$.prompt.close();">立即绑定手机</a>');
                        return false;
                    }

                    //加入
                    $.prompt( template("T:发送申请提示", {userAuthSuccess : userAuthSuccess, userMobile : userMobile}), {
                        focus : 1,
                        title: "系统提示",
                        buttons: { "取消": false, "发送申请": true },
                        position: {width: 500},
                        submit : function(e, v){
                            if(v){
                                function sendVer(){
                                    App.postJSON("/teacher/systemclazz/requestjoinclazz.vpage", currentClassList[$index], function(data){
                                        if(data.success){
                                            if(data.join){
                                                $17.alert("申请加入成功", function(){
                                                    $.prompt.close();
                                                    location.href = "/teacher/systemclazz/clazzindex.vpage";
                                                });
                                            }else{
                                                $17.alert("已发出申请！<br><br>请当面提醒对方老师同意你加入班级", function(){
                                                    currentClassRefresh();
                                                    $.prompt.close();
                                                });
                                            }
                                        }else{
                                            $17.alert(data.info);
                                        }
                                    });
                                }

                                //认证老师
                                if(userAuthSuccess){
                                    sendVer();
                                }else{
                                    var $smsCode = $(".v-smsCode");

                                    if( !$17.isNumber($smsCode.val()) ){
                                        $smsCode.addClass("w-int-error");
                                        return false;
                                    }

                                    $.post("/teacher/systemclazz/verifyTCACode.vpage", { code : $smsCode.val()}, function(data){
                                        if(data.success){
                                            sendVer();
                                        }else{
                                            $smsCode.siblings(".errorMsg").show().find(".info").text(data.info);
                                        }
                                    });
                                }
                                return false;
                            }
                        }
                    });
                }
            }
        }, ".v-selectClazzTeacher");

        //点击创建班级
        $(document).on("click", ".v-clickCreateClazz", function(){
            $(".v-clickClazzMenu[data-type='create']").click();
        });

        //输入
        $(document).on("keyup", ".v-smsCode", function(){
            $(this).removeClass("w-int-error").siblings(".errorMsg").hide();
        });

        //发送验证码
        $(document).on("click", ".v-getSmsCode", function(){
            var $this = $(this);

            $.post("/teacher/systemclazz/sendTCACode.vpage", { mobile : userMobile}, function(data){
                if(data.success){
                    $17.getSMSVerifyCode($this, data);
                }else{
                    $this.siblings("span").html(data.info);
                }
            })
        });

        function currentClassRefresh(){
            $(".v-levelClickShowBox[data-level='"+currentLevel+"']").click();
        }

        //设置班级数
        $17.minusPlusInputEvent({
            box : "#Anchor"
        }, function(opt){
            ClazzInfo.tempInfo.base.classesCount = opt.currentCount;
        });

        $(document).on({
            mouseleave : function(){
                $(this).find(".w-info-small").css({ display : 'none'});
            },
            mouseenter : function(){
                $(this).find(".w-info-small").css({ display : 'inline-block'});
            }
        }, ".js-hoverInfo");

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
    <div class="clearfix">
        <%for(var i = 0; i < noTeacherClazzList.length; i++){%>
            <div class="list hover v-takeovergroup" data-clazzId="<%=clazzInfoList[i].clazzId%>">
                <span class="tag">文</span>
                <span class="layer"><em>接管</em></span>
                <div><%=noTeacherClazzList[i].clazzName%></div>
                <div>无<%=noTeacherClazzList[i].subjectName%>老师</div>
            </div>
        <%}%>
        <%for(var i = 0; i < clazzList.length; i++){%>
            <div class="list hover v-selectClazzTeacher <%if(clazzList[i].hasRequest){%>active<%}%>" data-id="<%=clazzList[i].clazzId%>" data-index="<%=i%>" data-request="<%=clazzList[i].hasRequest.toString()%>">
                <%if(clazzList[i].hasRequest){%>
                    <span class="layer"><em>取消申请</em></span>
                <%}else{%>
                    <span class="layer"><em>申请加入</em></span>
                <%}%>
                <%if (clazzList[i].artScienceType == "SCIENCE") {%><span class="tag">理</span><%}%>
                <%if (clazzList[i].artScienceType == "ART") {%><span class="tag">文</span><%}%>
                <%if (clazzList[i].artScienceType == "ARTSCIENCE") {%><span class="tag">不分文理</span><%}%>
                <div><%=clazzList[i].name%></div>
                <%var teachers = clazzList[i].teachers%>
                <%for(var t in teachers){%>
                    <%if(teachers[t].subject != "语文"){%>
                        <div><%=teachers[t].subject%>：<%=(teachers[t].name ? teachers[t].name.substring(0, 1) : '--')%>老师</div>
                    <%}%>
                <%}%>
            </div>
        <%}%>
        <a href="javascript:;" class="list add v-clickCreateClazz">
            <div class="icon"></div>
            <div>创建新班级</div>
        </a>
    </div>
</script>
<script type="text/html" id="T:发送申请提示">
    <div style='text-align: center;'>给老师发送申请，当面提醒Ta登录并同意申请</div>
    <%if(!userAuthSuccess){%>
        <div class="w-form-table">
            <div style="text-align: center; padding: 20px 0 10px;">验证手机号,并发送请求：<%=userMobile%> <a class="w-btn w-btn-mini v-getSmsCode" href="javascript:void(0);" style="width: 150px;"><span>免费获取验证码</span></a></div>
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

<@sugar.capsule js=["ZeroClipboard"] css=[] />
<script type="text/html" id="T:教学生如何使用Popup">
    <style>
        /*reg-program-copy*/
        .reg-program-copy{margin: -30px auto 0;}
        .reg-program-copy .title{padding:10px;}
        .reg-program-copy .title h5{font-size:14px;font-weight: normal;line-height: 22px;}
        .reg-program-copy .title h5 span{color:#fa7252;}
        .reg-program-copy .paragraph p{font-size:14px;line-height: 24px;}
        .reg-program-copy .paragraph p a{color:#189cfb;}
        .reg-program-copy .btn{text-align: center;margin:20px 0 20px 0;}
    </style>
    <div class="reg-program-copy">
        <div style="font-size: 25px; color: #4e5656; text-align: center; padding: 20px;font-weight:600">班级设置成功!</div>
        <div class="title">
            <h3>即将进入班级管理页进行导入学生操作~</h3>
        </div>
        <p style="text-align: center; padding: 10px;margin: 30px">
            <a class="btn" href="${(ProductConfig.getUcenterUrl())!''}/teacher/systemclazz/clazzindex.vpage"
               style="font-size: 18px;padding: 10px 52px;background-color: #39f;color: white;border-radius: 8px;">确定</a>
        </p>
    </div>
</script>
</#macro>