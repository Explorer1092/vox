<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
<script id="t:添加老师" type="text/html">
    <div class="w-base">
        <div class="w-base-title">
            <h3><%= clazzName %></h3>
            <div class="w-base-right w-base-switch">
                <ul>
                    <li id="v-notAccount" <% if(showType == "notAccount"){ %>class="active"<% } %>>
                        <a href="javascript:void(0);">
                            <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                            对方没有一起作业号
                        </a>
                    </li>
                    <li id="v-hasAccount" <% if(showType == "hasAccount"){ %>class="active"<% } %>>
                        <a href="javascript:void(0);">
                            <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                            对方已有一起作业号
                        </a>
                    </li>
                </ul>
            </div>
        </div>
        <% if(showType == "notAccount"){ %>
            <div class="w-base-container">
                <div class="t-transfer-form">
                    <div class="w-form-table">
                        <dl>
                            <dt>请输入要添加的教师姓名：</dt>
                            <dd>
                                <input type="text" class="v-invite-teachername w-int" value="<%= teacherName %>">
                                <% if(tnerror == "true"){ %>
                                    <span class="w-form-misInfo w-form-info-error"><i class="w-icon-public w-icon-error"></i><strong class="info">请填写正确的老师姓名</strong></span>
                                <% } %>
                            </dd>
                            <dt>请输入该教师的手机号码：</dt>
                            <dd>
                                <input type="text" class="v-invite-teachermobile w-int" value="<%= teacherMobile %>">
                                <% if(tmerror == "true"){ %>
                                    <span class="w-form-misInfo w-form-info-error"><i class="w-icon-public w-icon-error"></i><strong class="info">请填写正确的手机号</strong></span>
                                <% } %>
                            </dd>
                            <dd class="form-btn center">
                                <a class="w-btn w-btn-green w-btn-small" href="javascript:history.back();">取消</a>
                                <a class="w-btn w-btn-small <% if(redy == "false"){ %>w-btn-disabled<% }else{ %>v-send-invite<% } %>" data-clazzid="${clazzId!}" data-subject="${subject!}" href="javascript:void(0);">邀请</a>
                            </dd>
                        </dl>
                    </div>
                </div>
            </div>
        <% } %>
        <% if(showType == "hasAccount"){ %>
            <div class="w-base-container">
                <!--//start-->
                <div class="t-transfer-case">
                    <div class="v-first-names w-base-switch w-base-switch-mini">
                        <ul>
                            <li <% if(listType == "ALL"){ %>class="active"<% } %> data-shownametype="ALL">
                                <a href="javascript:void(0);">
                                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                    全部
                                </a>
                            </li>
                            <li <% if(listType == "ABCD"){ %>class="active"<% } %> data-shownametype="ABCD">
                                <a href="javascript:void(0);">
                                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                    ABCD
                                </a>
                            </li>
                            <li <% if(listType == "EFGH"){ %>class="active"<% } %> data-shownametype="EFGH">
                                <a href="javascript:void(0);">
                                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                    EFGH
                                </a>
                            </li>
                            <li <% if(listType == "IJKL"){ %>class="active"<% } %> data-shownametype="IJKL">
                                <a href="javascript:void(0);">
                                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                    IJKL
                                </a>
                            </li>
                            <li <% if(listType == "MNOP"){ %>class="active"<% } %> data-shownametype="MNOP">
                                <a href="javascript:void(0);">
                                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                    MNOP
                                </a>
                            </li>
                            <li <% if(listType == "QRST"){ %>class="active"<% } %> data-shownametype="QRST">
                                <a href="javascript:void(0);">
                                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                    QRST
                                </a>
                            </li>
                            <li <% if(listType == "UVWX"){ %>class="active"<% } %> data-shownametype="UVWX">
                                <a href="javascript:void(0);">
                                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                    UVWX
                                </a>
                            </li>
                            <li <% if(listType == "YZ"){ %>class="active"<% } %> data-shownametype="YZ">
                                <a href="javascript:void(0);">
                                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                    YZ
                                </a>
                            </li>
                            <li <% if(listType == "OTHER"){ %>class="active"<% } %> data-shownametype="OTHER">
                                <a href="javascript:void(0);">
                                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                    其他
                                </a>
                            </li>
                        </ul>
                    </div>
                </div>
                <div class="t-class-enter">
                    <% if(authenticated.length > 0){ %>
                        <dl class="add">
                            <dt>认证老师:</dt>
                            <dd>
                                <% for(var i = 0; i < authenticated.length; i++){ %>
                                    <p><span class="w-radio<% if(targetId == authenticated[i].teacherId){ %> w-radio-current<% } %>" data-teacherid="<%= authenticated[i].teacherId %>" data-teachername="<%= authenticated[i].teacherName %>"></span><%= authenticated[i].teacherName %> 老师 （<%= authenticated[i].teacherId %>）[<%= authenticated[i].subjectName %>]</p>
                                    <% if(i % 2 != 0 && i < authenticated.length - 2){ %></dd><dd><% } %>
                                <% } %>
                            </dd>
                        </dl>
                    <% } %>
                    <% if(unauthenticated.length > 0){ %>
                        <dl class="add">
                            <dt>非认证老师:</dt>
                            <dd>
                                <% for(var i = 0; i < unauthenticated.length; i++){ %>
                                    <p><span class="w-radio<% if(targetId == unauthenticated[i].teacherId){ %> w-radio-current<% } %>" data-teacherid="<%= unauthenticated[i].teacherId %>" data-teachername="<%= unauthenticated[i].teacherName %>"></span><%= unauthenticated[i].teacherName %> 老师 （<%= unauthenticated[i].teacherId %>）[<%= unauthenticated[i].subjectName %>]</p>
                                    <% if(i % 2 != 0 && i < unauthenticated.length - 2){ %></dd><dd><% } %>
                                <% } %>
                            </dd>
                        </dl>
                    <% } %>
                </div>
                <% if(authenticated.length == 0 && unauthenticated.length == 0){ %>
                    <div class="w-noData-block">
                        暂无数据
                    </div>
                <% } %>
                <% if(targetName != ""){ %>
                    <div class="t-transfer-box">
                        <p class="title">你要添加老师：<%= targetName %></p>
                        <p>
                            <a class="w-btn w-btn-green w-btn-small" href="javascript:history.back();">取消</a>
                            <a class="v-sendinviteapplication w-btn w-btn-small" href="javascript:void(0);">同意</a>
                        </p>
                    </div>
                <% } %>
            </div>
        <% } %>
    </div>
</script>
<div id="Anchor"></div>
<@sugar.capsule js=["clazz.inviteteacherlist"] />
<script type="text/javascript">
    var Invite = {
        tempInfo : {
            name : "t:添加老师",
            base : {
                clazzId         : "${clazzId!}",    //班级ID，用于表单提交
                clazzName       : "${clazzName!}",  //班级名称，用于界面展示
                showType        : "notAccount",     //显示类型，控制Tab切换
                teacherName     : "",               //第一个Tab，教师名字
                teacherMobile   : "",               //第一个Tab，教师手机号
                tnerror         : "false",          //第一个Tab，名字错误控制器
                tmerror         : "false",          //第一个Tab，手机号错误控制器
                redy            : "false",          //第一个Tab，提交按钮是否就绪
                listType        : "ALL",            //姓名显示群落
                authenticated   : [],               //认证老师列表
                unauthenticated : [],               //非认证老师列表,
                targetId        : "",               //要转让的老师ID
                targetName      : ""                //要转让的老师
            }
        },
        dataBase : {
            authenticatedTeacherList    : ${authenticatedTeacherList!'[]'},
            unauthenticatedTeahcerList  : ${unauthenticatedTeahcerList!'[]'}
        },
        eventConfig: {
            "#v-notAccount -> click"            : notAccount_click,
            "#v-hasAccount -> click"            : hasAccount_click,
            ".v-invite-teachername -> blur"     : teachername_blur,
            ".v-invite-teachermobile -> blur"   : teachermobile_blur,
            ".v-send-invite -> click"           : send_invite_btn_click,
            ".v-first-names li -> click"        : first_names_click,
            ".w-radio -> click"                 : teacher_radio_click,
            ".v-sendinviteapplication -> click" : sendinviteapplication_click
        },
        refresh: function(){
            $("#Anchor").html(template(this.tempInfo.name, this.tempInfo.base));
            $17.delegate(this.eventConfig);
        },
        init: function(){
            this.refresh();
        }
    };

    $(function(){
        Invite.init();

        LeftMenu.focus("clazzmanager");
    });
</script>
</@shell.page>