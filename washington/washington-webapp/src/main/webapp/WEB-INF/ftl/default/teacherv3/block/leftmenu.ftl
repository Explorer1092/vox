<div id="201408071646_menu_template" class="m-side-menu"></div>
<script id="t:201408071646_menu_template" type="text/html">
    <ul>
        <% for(var i = 0; i < menuInfo.length; i++){ %>
        <li class="<%if((isOpen && target == menuInfo[i].name[0]) || (menuInfo[i].isFocus && !isOpen)){%>active<%}%><% if(menuInfo[i].child.length > 0){ %> pull-down<% } %>">
            <a data-menuname="<%= menuInfo[i].name[0] %>" href="<%= menuInfo[i].url %>"
                <% if(menuInfo[i].isTarget){ %>target="_blank"<% } %>

                class="one-level"
            >
                <span class="w-icon <%= menuInfo[i].style %><% if(menuInfo[i].isFocus){ %> w-icon-blue<% } %>"></span>
                <span class="w-icon-md <% if(menuInfo[i].isFocus && !isOpen){ %>w-blue<% } %>"><%== menuInfo[i].text %></span>
                <%if(menuInfo[i].text == "我的班级"){%><#if (data.pendingApplicationCount)?? && data.pendingApplicationCount gt 0><span class='w-icon-arrow w-icon-redInfo'>${(data.pendingApplicationCount)!}</span></#if><%}%>
                <#if (data.showHomeworkGuide)!false>
                    <%if(menuInfo[i].text == "作业"){%><strong class="w-info-small" style="position: absolute;"><i class="small-arrow small-arrow-bot">◆</i>布置作业在这里</strong><%}%>
                </#if>
                <%if(menuInfo[i].text == "课堂资源"){%><span class="w-icon-public w-icon-new"></span><%}%>
                <#-- ResourcePlatform 2015.11.03 Sir0xb -->
                <%if(menuInfo[i].text == "资源平台"){%><span class="w-icon-public w-icon-new"></span><%}%>
            </a>
            <% if(menuInfo[i].child.length > 0){ %>
            <div class="pull-level">
                <% for(var j = 0; j < menuInfo[i].child.length; j++){ %>
                    <a data-menuname="<%= menuInfo[i].child[j].name[0] %>" href="<%= menuInfo[i].child[j].url %>"
                        <% if(menuInfo[i].child[j].isFocus){ %> class="current"<% } %>
                        <% if(menuInfo[i].child[j].isTarget){ %><% } %>
                        <% if(menuInfo[i].subject != null && menuInfo[i].subject != ""){ %>data-subject="<%== menuInfo[i].subject%>"<% } %>
                    >
                    <%= menuInfo[i].child[j].text %>
                    <%if(menuInfo[i].child[j].text == "布置假期作业"){%><span class="w-icon-public w-icon-new"></span><%}%>
                    <%if(menuInfo[i].child[j].text == "资源平台"){%><span class="w-icon-public w-icon-new"></span><%}%>
                    <#if (currentTeacherDetail.subject == "MATH" && currentTeacherWebGrayFunction.isAvailable("New", "mathhomework") && currentUser.fetchCertificationState() == "SUCCESS")!false>
                        <%if(menuInfo[i].child[j].text == "布置作业"){%><strong class="w-info-small" style="position: absolute;margin-left:10px;"><i class="small-arrow small-arrow-bot" style="left:-6px;top:2px;">◆</i>更新了</strong><%}%>
                    <#elseif (currentTeacherDetail.subject == "CHINESE")!false>
                        <%if(menuInfo[i].child[j].text == "布置作业"){%><span class="w-icon-public w-icon-new"></span><%}%>
                    </#if>
                    </a>
                <% } %>
            </div>
            <% } %>
        </li>
        <% } %>
    </ul>
</script>
<script type="text/javascript">
var LeftMenu = null;

$(function () {
    var refSource = "";

    LeftMenu = {
        isOpen: false,
        parents: [],
        beforeTarget: null,
        menuInfo: [
            {
                name: ["main"],
                text: "首页",
                isTarget: false,
                isFocus: false,
                style: "w-icon-2",
                child: [],
                url: "/"+refSource
            },
            <#assign subjectList = [(currentTeacherDetail.subject)!] >
            <#if currentTeacherDetail.subjects??>
                <#assign subjectList = (currentTeacherDetail.subjects)![] >
            </#if>
            <#list subjectList as subject>
            {
                name: ["${subject!}_homeworkparent", "${subject!}_homeworkhistory", "${subject!}_termreview","goal","${subject!}_vacation","${subject!}_vacationhistory"],
                text: "${subject.value!}作业",
                isTarget: false,
                isFocus: false,
                style: "w-icon-3",
                subject: "${subject!''}",
                child: [
                    {
                        name: ["${subject!}_homework"],
                        text: "布置作业",
                        isTarget: false,
                        isFocus: false,
                        child: [],
                        url: "javascript:void(0);"
                    }
                    <#if ((subject == "MATH" && currentTeacherWebGrayFunction.isAvailable("mockexam", "unittest"))!false)>
                        ,{
                        name: ["${subject!}_mockexam"],
                        text: "布置单元检测",
                        isTarget: false,
                        isFocus: false,
                        child: [],
                        url: "/teacher/newexam/assign.vpage?subject=${subject!}"
                    }
                    </#if>
                    <#if (ftlmacro.devTestSwitch || currentTeacherWebGrayFunction.isAvailable("TermReview", "BlackList"))>
                        ,{
                        name: ["${subject!}_termreview"],
                        text: "布置期末复习",
                        isTarget: false,
                        isFocus: false,
                        child: [],
                        url: "javascript:void(0);"
                    }
                    </#if>
                    <#--<#if ((subject == "ENGLISH")!false) && (ftlmacro.devTestSwitch || currentTeacherWebGrayFunction.isAvailable("TermReview", "NewExam"))>
                        , {
                        name: ["${subject!}_newexam"],
                        text: "期末∙专项测试",
                        isTarget: false,
                        isFocus: false,
                        child: [],
                        url: "javascript:void(0);"
                    }
                    </#if>-->
                    <#if (ftlmacro.devTestSwitch || currentTeacherWebGrayFunction.isAvailable("TeacherVacationHW", "WhiteList")) && (.now lt "2019-02-01 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss"))!false>
                        ,{
                        name: ["${subject!}_vacation"],
                        text: "布置假期作业",
                        isTarget: false,
                        isFocus: false,
                        child: [],
                        url: "/teacher/vacation/index.vpage?subject=${subject!}"
                    }
                    </#if>
                    ,{
                        name: ["${subject!}_homeworkhistory"],
                        text: "作业报告",
                        isTarget: false,
                        isFocus: false,
                        child: [],
                        url: "javascript:void(0);"
                    },{
                        name: ["${subject!}_early_homeworkhistory"],
                        text: "历史学期作业",
                        isTarget: false,
                        isFocus: false,
                        child: [],
                        url: "javascript:void(0);"
                    }
                    <#if (ftlmacro.devTestSwitch || currentTeacherWebGrayFunction.isAvailable("TeacherVacationHW", "WhiteList")) && (.now lt "2019-04-01 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss"))!false>
                        ,{
                        name: ["${subject!}_vacationhistory"],
                        text: "假期作业报告",
                        isTarget: false,
                        isFocus: false,
                        child: [],
                        url: "/teacher/vacation/report/list.vpage?subject=${subject!}"
                    }
                    </#if>
                    <#if (subjectList?size > 1) && ((subject == "ENGLISH")!false)>
                    ,{
                        name: ["listening"],
                        text: "听力材料",
                        isTarget: true,
                        isFocus: false,
                        style: "w-icon-31",
                        child: [],
                        <#if (subject == "ENGLISH" && (currentUser.regionCode == 110112 || currentUser.cityCode == 370700))!false>
                            url: "javascript:void(0);"
                        <#else>
                            url: "/tts/listening.vpage" + refSource
                        </#if>
                    }
                    </#if>
                ],
                url: "javascript:void(0);"
            },
            </#list>
            <#list subjectList as subject>
                <#if ((subject == "ENGLISH")!false)>
                    {
                        name: ["clazzresource"],
                        text: "课堂资源",
                        isTarget: false,
                        isFocus: false,
                        child: [],
                        url: "/teacher/clazzresource/index.vpage?log=leftMenu&subject=${subject}",
                        style: "w-icon-19"
                    },
                </#if>
            </#list>
            <#if (ftlmacro.devTestSwitch || currentTeacherWebGrayFunction.isAvailable("TeachingResource", "daite"))>
                    {
                        name: ["teachingresource"],
                        text: "开始上课",
                        isTarget: true,
                        isFocus: false,
                        child: [],
                        url: "/teacher/teachingresource/index.vpage",
                        style:"w-icon-31"
                    },
            </#if>
            {
                name: ["clazzmanager"],
                text: "我的班级",
                isTarget: true,
                isFocus: false,
                style: "w-icon-13",
                child: [],
                url: "${(ProductConfig.getUcenterUrl())!''}/teacher/systemclazz/clazzindex.vpage" + refSource
            },{
                name: ["smartclazz"],
                text: "智慧课堂",
                isTarget: false,
                isFocus: false,
                isIcon: $17.getCookieWithDefault("SMTCNEW"),//临时设定
                style: "w-icon-12",
                child: [],
                url: "javascript:void(0);"
            }
            <#if (subjectList?size == 1) && ((currentTeacherDetail.subject == "ENGLISH")!false)>
                ,{
                name: ["listening"],
                text: "听力材料",
                isTarget: true,
                isFocus: false,
                style: "w-icon-31",
                child: [],
                <#if (subject == "ENGLISH" && (currentUser.regionCode == 110112 || currentUser.cityCode == 370700))!false>
                    url: "javascript:void(0);"
                <#else>
                    url: "/tts/listening.vpage" + refSource
                </#if>
            }
            </#if>
            <#if (currentTeacherDetail.isPrimarySchool())!false>
                ,{
                name: ["resource"],
                text: "资源管理",
                isTarget: false,
                isFocus: false,
                style: "w-icon-5",
                url: "javascript:void(0);",
                child: [
                    <#if (currentTeacherDetail.subject)?? && currentTeacherDetail.subject == "ENGLISH" && currentTeacherWebGrayFunction.isAvailable("ResourceSquare", "Open")>
                    {
                        name: ["resource_ground"],
                        text: "资源广场",
                        isTarget: true,
                        isFocus: false,
                        child: [],
                        url: "javascript:void(0);"
                    },
                    </#if>
                    {
                        name: ["my_resource"],
                        text: "我的资源",
                        isTarget: true,
                        isFocus: false,
                        child: [],
                        url: "javascript:void(0);"
                    }
                ]
            }
            </#if>
            <#if ([130800]?seq_contains(currentTeacherDetail.cityCode!0) && (currentTeacherDetail.subject)?? && currentTeacherDetail.subject == "ENGLISH")>
            ,{
                name: ["teacherBBS"],
                text: "承德专区",
                isTarget: true,
                isFocus: false,
                style: "w-icon-16",
                child: [],
                url: "${ProductConfig.getMainSiteBaseUrl()}/ucenter/bbspartner.vpage?url=${ProductConfig.getBbsSiteBaseUrl()}%2fopen.php%3fmod%3dregister&urlInput=${ProductConfig.getBbsSiteBaseUrl()}%2fforum.php%3Fmod%3Dforumdisplay%26fid%3D60"
            }
            <#--<#else>
            ,{
                name: ["teacherBBS"],
                text: "教师论坛",
                isTarget: true,
                isFocus: false,
                style: "w-icon-16",
                child: [],
                url: "/ucenter/bbspartner.vpage?url=${ProductConfig.getBbsSiteBaseUrl()}/open.php?mod=register&teacherType=2"
            }-->
            </#if>
        ],
        changeMenu: function () {
            this.menuInfo = [
                {
                    name: ["main"],
                    text: "首页",
                    isTarget: false,
                    isFocus: false,
                    style: "w-icon-2",
                    child: [],
                    url: "/"
                },
                {
                    name: ["basicInfo"],
                    text: "基本信息",
                    isTarget: false,
                    isFocus: false,
                    style: "w-icon-20",
                    child: [],
                    url: "${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage"+refSource
                },
                {
                    name: ["myprofile"],
                    text: "我的资料",
                    isTarget: false,
                    isFocus: false,
                    style: "w-icon-21",
                    child: [],
                    url: "${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myprofile.vpage"+refSource
                },
                {
                    name: ["mylevel"],
                    text: "教师等级",
                    isTarget: false,
                    isFocus: false,
                    style: "w-icon-29",
                    child: [],
                    url: "${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/mylevel.vpage"+refSource
                },
                {
                    name: ["safety"],
                    text: "账号安全",
                    isTarget: false,
                    isFocus: false,
                    style: "w-icon-22",
                    child: [],
                    url: "${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/securitycenter.vpage"+refSource
                },
                {
                    name: ["wallet"],
                    text: "我的园丁豆",
                    isTarget: false,
                    isFocus: false,
                    style: "w-icon-23",
                    child: [],
                    url: "${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/mygold.vpage"+refSource
                },
                {
                    name: ["authentication"],
                    text: "我的认证",
                    isTarget: false,
                    isFocus: false,
                    style: "w-icon-24",
                    child: [],
                    url: "${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage"+refSource
                }
            ];
        },
        focus: function (name) {
            //找到爸爸们
            if (this.parents.length == 0) {
                for (var i = 0, l = this.menuInfo.length; i < l; i++) {
                    if (this.menuInfo[i].child.length > 0) {
                        this.parents.push(this.menuInfo[i].name[0]);
                    }
                }
            }

            if ($.inArray(name, this.parents) != -1) {
                if ($17.isBlank(this.beforeTarget) || this.beforeTarget != name) {
                    this.beforeTarget = name;
                    this.isOpen = true;
                } else {
                    this.isOpen = !this.isOpen;
                }
            } else {
                for (var i = 0, l = this.menuInfo.length; i < l; i++) {
                    if (this.menuInfo[i].child.length == 0) {
                        this.menuInfo[i].isFocus = name == this.menuInfo[i].name[0];
                        this.beforeTarget = this.menuInfo[i].isFocus ? name : this.beforeTarget;
                        this.isOpen = this.menuInfo[i].isFocus ? false : this.isOpen;
                    } else {
                        for (var j = 0, jl = this.menuInfo[i].child.length; j < jl; j++) {
                            this.menuInfo[i].child[j].isFocus = name == this.menuInfo[i].child[j].name[0];
                            this.beforeTarget = this.menuInfo[i].child[j].isFocus ? this.menuInfo[i].name[0] : this.beforeTarget;
                            this.isOpen = this.menuInfo[i].child[j].isFocus ? true : this.isOpen;
                        }
                    }
                }
            }

            $("#201408071646_menu_template").html(template("t:201408071646_menu_template", {
                isOpen: this.isOpen,
                menuInfo: this.menuInfo,
                target: this.beforeTarget
            }));

            $("a[data-menuname]").on("click", function () {
                var s = $(this).attr('href');

                if(s.substr(0, 11) == 'javascript:') {
                    LeftMenu.focus($(this).attr("data-menuname"));

                    switch($(this).attr("data-menuname")){
                        case "resource_ground":
                            $17.tongji("老师端-首页-教师空间-资源-资源广场");
                            setTimeout(function(){
                                // 老师资源管理页面资源广场打开新窗口
                                window.open('/redirector/apps/go.vpage?app_key=ResourcePlatform' + refSource);
                            }, 200);
                            break;
                        case "my_resource":
                            $17.tongji("老师端-首页-教师空间-资源-我的资源");
                            <#-- ResourcePlatform 2015.11.03 Sir0xb -->
                            <#--<#if currentTeacherDetail.subject != "ENGLISH" || !currentTeacherWebGrayFunction.isAvailable("ResourcePlatform", "Open")>-->
                            setTimeout(function(){
                                // redmine 34800
                                // 老师资源管理页面我的资源打开新窗口
                                //location.href = "/teacher/resource/list.vpage" + refSource;
                                window.open("/teacherMobile/teachingres/myresources.vpage" + refSource);
                            }, 200);
                            <#--</#if>-->
                            break;
                        case "listening":
                            $17.tongji("老师端-首页-教师空间-听力材料");
                            setTimeout(function(){
                                location.href = "/tts/listening.vpage" + refSource;
                            }, 200);
                            break;
                        case "clazzmanager":
                            $17.tongji("老师端-首页-教师空间-我的班级");
                            break;
                        case "smartclazz":
                            $17.tongji("老师端-首页-教师空间-智慧课堂");
                            //设置NewIcon隐藏
                            if(!$17.getCookieWithDefault("SMTCNEW") ||  $17.getCookieWithDefault("SMTCNEW") < 3){
                                var smartCount = 1;
                                if($17.getCookieWithDefault("SMTCNEW")){
                                    smartCount = smartCount + $17.getCookieWithDefault("SMTCNEW") * 1;
                                }
                                $17.setCookieOneDay("SMTCNEW", smartCount, 60);
                            }
                            setTimeout(function(){
                                location.href = "/teacher/smartclazz/list.vpage" + refSource;
                            }, 200);
                            break;
                        case "ENGLISH_homework":
                        case "MATH_homework":
                        case "CHINESE_homework":
                            var sub = $(this).attr('data-subject');
                            $17.tongji(getSubjectText(sub) + '布置作业',($uper.userAuth == 'true' ? '认证':'未认证') ,'左侧导航');
                            $17.voxLog({
                                module: "m_H1VyyebB",
                                op : "navigation_subject_assign_click",
                                s0 : sub
                            });
                            setTimeout(function(){
                                location.href = "/teacher/new/homework/batchassignhomework.vpage?subject=" + sub + refSource;
                            }, 200);
                            break;
                        case "ENGLISH_newexam":

                            var sub = $(this).attr('data-subject');
                            $17.tongji(getSubjectText(sub) + '模考');
                            $17.voxLog({
                                module: "m_H1VyyebB",
                                op : "navigation_subject_assign_click",
                                s0 : sub
                            });
                            setTimeout(function(){
                                location.href = "/teacher/newexam/independent/index.vpage?subject=" + sub;
                            }, 200);

                            break;
                        case "ENGLISH_homeworkhistory":
                        case "MATH_homeworkhistory":
                        case "CHINESE_homeworkhistory":
                            var sub = $(this).attr('data-subject');
                            $17.tongji(getSubjectText(sub) + "-左侧作业菜单-作业历史");
                            $17.voxLog({
                                module: "m_Odd245xH",
                                op : "navigation_subject_report_click",
                                s0 : sub
                            });
                            setTimeout(function(){
                                location.href = "/teacher/new/homework/report/list.vpage?subject=" + sub + refSource;
                            }, 200);
                            break;
                        case "ENGLISH_early_homeworkhistory":
                        case "MATH_early_homeworkhistory":
                        case "CHINESE_early_homeworkhistory":
                            var sub = $(this).attr('data-subject');
                            setTimeout(function(){
                                location.href = "/teacher/new/homework/report/earlylist.vpage?subject=" + sub + refSource;
                            }, 200);
                            break;
                        case "ENGLISH_termreview":
                        case "MATH_termreview":
                        case "CHINESE_termreview":
                            var _subject = $(this).attr('data-subject');
                            setTimeout(function(){
                                location.href = "/teacher/termreview/index.vpage?subject=" + _subject + "&log=leftMenu";
                            },200);
                            break;
                    }
                } else {
                    //什么都别做，不能因为dom操作等打断连接跳转操作，否则IE下链接点不进去
                }
            });
        }
    };

    function getSubjectText(subject) {
        switch (subject) {
            case "ENGLISH":
                return "英语";
            case "MATH":
                return "数学";
            case "CHINESE":
                return "语文";
            default:
                return "${(currentTeacherDetail.getSubject().getValue())!}";
        }
    }
});
</script>