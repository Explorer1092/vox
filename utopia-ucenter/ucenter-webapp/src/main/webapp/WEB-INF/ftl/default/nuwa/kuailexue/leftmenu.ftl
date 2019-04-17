<#--510000 四川,500000重庆, 440300深圳市-->
<#--<#assign holidayGrayFlag = [510000, 500000]?seq_contains(currentTeacherDetail.rootRegionCode!0) || [440300]?seq_contains(currentTeacherDetail.cityCode!0)/>-->
<div id="201408071646_menu_template" ></div>
<script id="t:201408071646_menu_template" type="text/html">
    <% for(var i = 0; i < menuInfo.length; i++){ %>
        <% if (menuInfo[i].text == '返回首页') { %>
            <div class="nav-home <%if((isOpen && target == menuInfo[i].name[0]) || (menuInfo[i].isFocus && !isOpen)){%><%}%><% if(menuInfo[i].child.length > 0) {%> <% } %>">
                <a data-menuname="<%= menuInfo[i].name[0] %>" href="<%= menuInfo[i].url %>" <% if(menuInfo[i].isTarget){ %>target="_blank"<% } %> >
                <span><%== menuInfo[i].text %></span>
                </a>
        <% } else { %>
            <div class="nav-mode">
                <a class="sort <%= menuInfo[i].style%>" data-menuname="<%= menuInfo[i].name[0] %>" href="<%= menuInfo[i].url %>" <% if(menuInfo[i].isTarget){ %>target="_blank"<% } %> >
                    <span><%== menuInfo[i].text %></span>
                </a>
        <% } %>
                <% if(menuInfo[i].child.length > 0){ %>
                    <% for(var j = 0; j < menuInfo[i].child.length; j++){ %>
                        <a class="link <% if(menuInfo[i].child[j].isFocus){ %> active <% } %>" data-menuname="<%= menuInfo[i].child[j].name[0] %>" title="<%= menuInfo[i].child[j].text %>" href="<%= menuInfo[i].child[j].url %>"
                            <% if(menuInfo[i].child[j].isTarget){ %> target="_blank"<% } %>>
                                <span style="overflow: hidden; text-overflow: ellipsis;white-space: nowrap;"><%== menuInfo[i].child[j].text %></span>
                        </a>
                    <% } %>
                <% } %>
            </div>
    <% } %>
</script>

<script id="T:修改异常用户姓名" type="text/html">
    <div class="modifyname-box">
        <p class="tips">系统检测您的姓名存在异常，仅支持2-10位中文名字</p>
        <div class="input-box">
            <span>请修改姓名：</span>
            <input type="text" placeholder="请输入中文姓名" maxlength="10" value="${(currentUser.profile.realname)!}" id="modifyedName">
        </div>
        <p class="tips error" id="errorName"></p>
    </div>
</script>

<#--神算子来源用户弹窗提示-->
<#include "ssztip.ftl" />

<script type="text/javascript">
    var LeftMenu = null;
    // 从神算子来源的，返回首页跳转回极算(wiki:37411959,班级管理：kuailexue/clazzindex.ftl、个人中心：kuailexue/index.ftl 共用此ftl，故在此处理)
    var fromShensz = '${(isShensz!false)?string}'; // 极算用户
    var needSupplementName = '${(isSupplementName!false)?string}'; // 需要修改姓名
    var indexLink = "/"; // 返回首页链接

    // 检测用户姓名异常
    function checkNameError () {
        if (needSupplementName === 'true') {
            $.prompt(template("T:修改异常用户姓名", {}), {
                focus: 0,
                title: "系统提示",
                buttons: {"确定": true},
                loaded: function () {
                    $('.jqiclose').hide(); // 隐藏关闭按钮
                },
                submit: function (e, v) {
                    if (v) {
                        e.preventDefault();
                        var inputName = $.trim($('#modifyedName').val());
                        if (!$17.isChinaString(inputName)) {
                            $('#errorName').text('您输入的姓名不符合规范，请输入2-10位中文名字');
                            return false;
                        }
                        App.postJSON('/ucenter/changName.vpage', {
                            userName: inputName
                        }, function(res){
                            if(res.success){
                                $.prompt.close();
                                setTimeout(function () {
                                    $17.alert(res.info, function(){
                                        window.location.href = "/index.vpage";
                                    });
                                }, 10);
                            }else{
                                $('#errorName').text(res.info);
                            }
                        });
                    }
                }
            });
        }
    }
    // 极算提示弹窗截止日期
    function frontofAlertDeadLine () {
        var deadLineTime = new Date('06/30/2018 23:59:59').getTime();
        var nowTime = new Date().getTime();
        if (nowTime < deadLineTime) return true;
        else return false;
    }

    $(function () {
        if (fromShensz === 'true') {
            indexLink = "${(ProductConfig.getMainSiteBaseUrl())!''}" + "/redirector/apps/go.vpage?app_key=Shensz"; // 跳转回极算首页
            if (!$17.getCookieWithDefault("is_show_ssz_teacher_tip")  && frontofAlertDeadLine()) {
                $('#jisuan-template').show(); // 展示神算及弹窗
            } else {
                checkNameError();
            }
        } else {
            checkNameError();
        }

        LeftMenu = {
            isOpen: false,
            parents: [],
            beforeTarget: null,
            menuInfo: [{
                name: ["main"],
                text: "返回首页",
                isTarget: false,
                isFocus: false,
                style: "w-icon-10",
                child: [],
                url: indexLink
            },{
                name: ["clazzmanager"],
                text: "班级管理",
                isTarget: false,
                isFocus: false,
                style: "manage",
                child: [
                <#if teachClazzs?? && teachClazzs?size gt 0>
                    <#list teachClazzs as clazz>
                        {
                            name: ["${clazz.clazzId!}"],
                            text: "${clazz.clazzName!}",
                            isTarget: false,
                            isFocus: false,
                            child: [],
                            url: "#/teacher/clazz/clazzsdetail.vpage?clazzId=${clazz.clazzId!}"
                        }<#if (clazz_index + 1) lt teachClazzs?size>,</#if>
                    </#list>
                </#if>
                ],
                url: "/teacher/systemclazz/clazzindex.vpage"
            }],
            changeMenu: function () {
                this.menuInfo = [
                    {
                        name: ["main"],
                        text: "返回首页",
                        isTarget: false,
                        isFocus: false,
                        style: "w-icon-10",
                        child: [],
                        url: indexLink
                    },
                    {
                        name: ["basicInfo"],
                        text: "个人中心",
                        isTarget: false,
                        isFocus: false,
                        style: "personal",
                        child: [
                            {
                                name: ["myprofile"],
                                text: "我的资料",
                                isTarget: false,
                                isFocus: false,
                                style: "w-icon-21",
                                child: [],
                                url: "#/teacher/center/myprofile.vpage"
                            },
                            {
                                name: ["safety"],
                                text: "账号安全",
                                isTarget: false,
                                isFocus: false,
                                style: "w-icon-22",
                                child: [],
                                url: "#/teacher/center/securitycenter.vpage"
                            },
//                            {
//                                name: ["authentication"],
//                                text: "我的认证",
//                                isTarget: false,
//                                isFocus: false,
//                                style: "w-icon-24",
//                                child: [],
//                                url: "#/teacher/center/myauthenticate.vpage"
//                            },
                            {
                                name: ["message"],
                                text: "消息中心",
                                isTarget: false,
                                isFocus: false,
                                style: "",
                                child: [],
                                url: "#/teacher/message/index.vpage"
                            }
                        ],
                        url: "/teacher/center/index.vpage"
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
            }
        };
    });
</script>