<#--510000 四川,500000重庆, 440300深圳市-->
<#--<#assign holidayGrayFlag = [510000, 500000]?seq_contains(currentTeacherDetail.rootRegionCode!0) || [440300]?seq_contains(currentTeacherDetail.cityCode!0)/>-->
<div id="201408071646_menu_template" class="m-side-menu"></div>
<script id="t:201408071646_menu_template" type="text/html">
    <ul>
        <% for(var i = 0; i < menuInfo.length; i++){ %>
        <li class="<%if((isOpen && target == menuInfo[i].name[0]) || (menuInfo[i].isFocus && !isOpen)){%>active<%}%><% if(menuInfo[i].child.length > 0 && menuInfo[i].text != '我的班级'){ %> pull-down<% } %>">
            <a data-menuname="<%= menuInfo[i].name[0] %>" href="<%= menuInfo[i].url %>" <% if(menuInfo[i].isTarget){
            %>target="_blank"<% } %> class="one-level"><span
                class="w-icon <%= menuInfo[i].style %><% if(menuInfo[i].isFocus){ %> w-icon-blue<% } %>"></span><span
                class="w-icon-md <% if(menuInfo[i].isFocus && !isOpen){ %>w-blue<% } %>"><%== menuInfo[i].text %></span>
                <%if(menuInfo[i].text == "我的班级"){%><#if (data.pendingApplicationCount)?? && data.pendingApplicationCount gt 0><span class='w-icon-arrow w-icon-redInfo'>${data.pendingApplicationCount}</span></#if><%}%>
            </a>
            <% if(menuInfo[i].child.length > 0){ %>
            <div class="pull-level">
                <% for(var j = 0; j < menuInfo[i].child.length; j++){ %>
                <% if (menuInfo[i].text != "我的班级") { %>
                    <a style="height: 16px; line-height: 16px;" data-menuname="<%= menuInfo[i].child[j].name[0] %>" title="<%= menuInfo[i].child[j].text %>" href="<%= menuInfo[i].child[j].url %>"
                <% } else { %>
                    <a style="height: 16px; line-height: 16px;" class="v-cm-main <%if(menuInfo[i].child[j].isFocus){ %>current<%}%>" data-menuname="<%= menuInfo[i].child[j].name[0] %>" title="<%= menuInfo[i].child[j].text %>" href="#<%= menuInfo[i].child[j].url %>"
                <% } %>
                <% if(menuInfo[i].child[j].isFocus){ %> class="current"<% } %><% if(menuInfo[i].child[j].isTarget){ %>
                target="_blank"<% } %>>
                    <span style='display:inline-block;white-space: nowrap;overflow: hidden; text-overflow: ellipsis; width: 130px;'><%== menuInfo[i].child[j].text %></span>
                </a>
                <% } %>
            </div>
            <% } %>
        </li>
        <% } %>
    </ul>
</script>

<#--神算子来源用户弹窗提示-->
<#include "./errornametip.ftl" />

<script type="text/javascript">
var LeftMenu = null;

$(function () {
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
            url: "/"
        },{
            name: ["clazzmanager"],
            text: "我的班级",
            isTarget: false,
            isFocus: false,
            style: "w-icon-13",
            child: [
        <#if teachClazzs?? && teachClazzs?size gt 0>
            <#list teachClazzs as clazz>
                {
                    name: ["${clazz.clazzId!}"],
                    text: "${clazz.clazzName!}",
                    isTarget: false,
                    isFocus: false,
                    child: [],
                    url: "/teacher/clazz/clazzsdetail.vpage?clazzId=${clazz.clazzId!}"
                }<#if (clazz_index + 1) lt teachClazzs?size>,</#if>
            </#list>
        </#if>
            ],
            url: "/teacher/systemclazz/clazzindex.vpage"
        }],
        changeMenu: function () {
            this.menuInfo = [{
                    name: ["main"],
                    text: "返回首页",
                    isTarget: false,
                    isFocus: false,
                    style: "w-icon-10",
                    child: [],
                    url: "/"
                },
                {
                    name: ["basicInfo"],
                    text: "个人中心",
                    isTarget: false,
                    isFocus: false,
                    style: "w-icon-11",
                    child: [],
                    url: "/teacher/center/index.vpage"
                },
                {
                    name: ["myprofile"],
                    text: "我的资料",
                    isTarget: false,
                    isFocus: false,
                    style: "w-icon-21",
                    child: [],
                    url: "#/teacher/center/myprofile.vpage"
                },
                <#if (currentTeacherDetail.isPrimarySchool())!false>
                {
                    name: ["mylevel"],
                    text: "教师等级",
                    isTarget: false,
                    isFocus: false,
                    style: "w-icon-29",
                    child: [],
                    url: "#/teacher/center/mylevel.vpage"
                },
                </#if>
                {
                    name: ["safety"],
                    text: "账号安全",
                    isTarget: false,
                    isFocus: false,
                    style: "w-icon-22",
                    child: [],
                    url: "#/teacher/center/securitycenter.vpage"
                },
                <#if ((currentTeacherDetail.isPrimarySchool())!false)
                    || ((currentTeacherWebGrayFunction.isAvailable("MSIntegral", "Mall"))!false) >
                {
                    name: ["wallet"],
                    text: "我的<@ftlmacro.garyBeansText/>",
                    isTarget: false,
                    isFocus: false,
                    style: "w-icon-23",
                    child: [],
                    url: "#/teacher/center/mygold.vpage"
                },
                </#if>
                {
                    name: ["authentication"],
                    text: "我的认证",
                    isTarget: false,
                    isFocus: false,
                    style: "w-icon-24",
                    child: [],
                    url: "#/teacher/center/myauthenticate.vpage"
                },
                {
                    name: ["message"],
                    text: "消息中心",
                    isTarget: false,
                    isFocus: false,
                    style: "",
                    child: [],
                    url: "#/teacher/message/index.vpage"
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