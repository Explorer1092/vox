<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
<script id="t:我收到的请求" type="text/html">
    <!--w-base template-->
    <div class="w-base-title">
        <h3>班级</h3>
        <div class="w-base-right w-base-switch">
            <ul>
                <li class="v-tab-a active">
                    <a href="javascript:void(0);">
                        <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                        我收到的请求
                    </a>
                </li>
                <li class="v-tab-b">
                    <a href="javascript:void(0);">
                        <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                        我发出的请求
                    </a>
                </li>
                <li class="v-tab-c">
                    <a href="javascript:void(0);">
                        <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                        操作记录
                    </a>
                </li>
            </ul>
        </div>
    </div>
    <div class="w-base-container">
        <!--我收到的请求-->
        <#if unprocessedApplicationSendIn?? && (unprocessedApplicationSendIn?size > 0)>
            <div class="w-table">
                <table>
                    <thead>
                        <tr>
                            <td style="width: 330px">申请内容</td>
                            <td>班级</td>
                            <td>日期</td>
                            <td style="width: 80px">操作</td>
                        </tr>
                    </thead>
                    <tbody>
                        <#list unprocessedApplicationSendIn as msg>
                        <tr <#if msg_index % 2 == 0>class="odd"</#if>>
                            <th>${msg.message!}</th>
                            <th style="width: 25%;">${msg.className} C${msg.classId}</th>
                            <th style="width: 15%;">${msg.datetime}</th>
                            <th style="width: 30%;">
                                <#switch msg.type>
                                    <#case "TAKE_OVER">
                                        <a data-url="agreetakeoverapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small btn_mark_primary"><strong>同 意</strong></a>
                                        <a data-url="rejecttakeoverapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small"><strong>拒 绝</strong></a>
                                        <#break />
                                    <#case "DELEGATE">
                                        <a data-url="agreedelegateapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small btn_mark_primary"><strong>同 意</strong></a>
                                        <a data-url="rejectdelegateapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small"><strong>拒 绝</strong></a>
                                        <#break />
                                    <#case "INVITE">
                                        <a data-url="approveinviteapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small btn_mark_primary"><strong>同 意</strong></a>
                                        <a data-url="rejectinviteapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small"><strong>拒 绝</strong></a>
                                        <#break />
                                    <#case "JOIN">
                                        <a data-url="approvejoinapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small btn_mark_primary"><strong>同 意</strong></a>
                                        <a data-url="rejectjoinapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small"><strong>拒 绝</strong></a>
                                        <#break />
                                    <#case "HAND_OVER">
                                        <a data-url="approvehandoverapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small btn_mark_primary"><strong>同 意</strong></a>
                                        <a data-url="rejecthandoverapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small"><strong>拒 绝</strong></a>
                                        <#break />
                                    <#case "SUBSTITUTE">
                                        <a data-url="agreesubstituteapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small btn_mark_primary"><strong>同 意</strong></a>
                                        <a data-url="rejectsubstituteapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small"><strong>拒 绝</strong></a>
                                        <#break />
                                    <#case "LINK">
                                        <a data-url="approvelinkapp" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small btn_mark_primary"><strong>同 意</strong></a>
                                        <a data-url="rejectlinkapp" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small"><strong>拒 绝</strong></a>
                                        <#break />
                                    <#case "REPLACE">
                                        <a data-url="approvereplaceapp" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small btn_mark_primary"><strong>同 意</strong></a>
                                        <a data-url="rejectreplaceapp" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small"><strong>拒 绝</strong></a>
                                        <#break />
                                    <#case "TRANSFER">
                                        <a data-url="approvetransferapp" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small btn_mark_primary"><strong>同 意</strong></a>
                                        <a data-url="rejecttransferapp" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small"><strong>拒 绝</strong></a>
                                        <#break />
                                </#switch>
                            </th>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        <#else>
            <div class="w-noData-block"> 暂无数据 </div>
        </#if>
    </div>
</script>
<script id="t:我发出的请求" type="text/html">
    <!--w-base template-->
    <div class="w-base-title">
        <h3>班级</h3>
        <div class="w-base-right w-base-switch">
            <ul>
                <li class="v-tab-a">
                    <a href="javascript:void(0);">
                        <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                        我收到的请求
                    </a>
                </li>
                <li class="v-tab-b active">
                    <a href="javascript:void(0);">
                        <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                        我发出的请求
                    </a>
                </li>
                <li class="v-tab-c">
                    <a href="javascript:void(0);">
                        <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                        操作记录
                    </a>
                </li>
            </ul>
        </div>
    </div>
    <div class="w-base-container">
        <#if unprocessedApplicationSendOut?? && (unprocessedApplicationSendOut?size > 0)>
            <div class="w-table">
                <table>
                    <thead>
                        <tr>
                            <td style="width: 330px">申请内容</td>
                            <td>班级</td>
                            <td>状态</td>
                            <td style="width: 80px">操作</td>
                        </tr>
                    </thead>
                    <tbody>
                        <#list unprocessedApplicationSendOut as msg>
                            <tr>
                                <th>${msg.message}</th>
                                <th style="width: 25%;">${msg.className} C${msg.classId}</th>
                                <th style="width: 20%;">等待对方同意</th>
                                <th style="width: 15%;">
                                    <#switch msg.type>
                                        <#case "TAKE_OVER">
                                            <a data-url="canceltakeoverapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small"><strong>取 消</strong></a>
                                            <#break />
                                        <#case "DELEGATE">
                                            <a data-url="canceldelegateapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small"><strong>取 消</strong></a>
                                            <#break />
                                        <#case "INVITE">
                                            <a data-url="cancelinviteapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small"><strong>取 消</strong></a>
                                            <#break />
                                        <#case "JOIN">
                                            <a data-url="canceljoinapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small"><strong>取 消</strong></a>
                                            <#break />
                                        <#case "HAND_OVER">
                                            <a data-url="cancelhanvoverapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small"><strong>取 消</strong></a>
                                            <#break />
                                        <#case "SUBSTITUTE">
                                            <a data-url="cancelsubstituteapplication" data-recordid="${msg.recordId}" data-clazzname="${msg.className}" data-subject="${msg.teacher.subject.value}" data-teachername="${msg.teacher.profile.realname}" href="javascript:void(0);" class="btn_mark btn_mark_small"><strong>取 消</strong></a>
                                            <#break />
                                    </#switch>
                                </th>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        <#else>
            <div class="w-noData-block"> 暂无数据 </div>
        </#if>
    </div>
</script>
<script id="t:操作记录" type="text/html">
    <!--w-base template-->
    <div class="w-base-title">
        <h3>班级</h3>
        <div class="w-base-right w-base-switch">
            <ul>
                <li class="v-tab-a">
                    <a href="javascript:void(0);">
                        <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                        我收到的请求
                    </a>
                </li>
                <li class="v-tab-b">
                    <a href="javascript:void(0);">
                        <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                        我发出的请求
                    </a>
                </li>
                <li class="v-tab-c active">
                    <a href="javascript:void(0);">
                        <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                        操作记录
                    </a>
                </li>
            </ul>
        </div>
    </div>
    <div class="w-base-container">
        <div class="w-table">
            <table>
                <thead>
                    <tr>
                        <td style="width: 200px;">日期</td>
                        <td>操作记录</td>
                    </tr>
                </thead>
                <tbody id="historyChip"></tbody>
            </table>
            <div class="historyChip message_page_list"></div>
        </div>
    </div>
</script>
<div id="showInfo" class="w-base"></div>
<div id="showInfoNoContent" class="w-base" style="display: none; margin-top: -15px; text-align: center; padding: 15px; border-top: 1px solid #eee; ">
    <a class="w-btn w-btn-small w-btn-green" href="${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/index.vpage">返回首页</a>
    <a class="w-btn w-btn-small" href="/teacher/clazz/createclazz.vpage?step=showtip&type=hasApplication">添加新班级</a>
</div>
<@sugar.capsule js=["clazz.unprocessedapplication"] />
<script type="text/javascript">
    $(function(){
        var ShowInfo = {
            tabA : {
                tempInfo    : {
                    tempId  : "t:我收到的请求"
                },
                eventConfig  : {

                }
            },
            tabB : {
                tempInfo    : {
                    tempId  : "t:我发出的请求"
                },
                eventConfig : {

                }
            },
            tabC : {
                tempInfo    : {
                    tempId  : "t:操作记录"
                },
                eventConfig : {

                }
            },
            eventConfig : {
                "li.v-tab-a -> click" : function(){
                    $("#showInfo").html(template(ShowInfo.tabA.tempInfo.tempId, {}));
                    $17.delegate(ShowInfo.eventConfig);
                },
                "li.v-tab-b -> click" : function(){
                    $("#showInfo").html(template(ShowInfo.tabB.tempInfo.tempId, {}));
                    $17.delegate(ShowInfo.eventConfig);
                },
                "li.v-tab-c -> click" : function(){
                    $("#showInfo").html(template(ShowInfo.tabC.tempInfo.tempId, {}));
                    $17.delegate(ShowInfo.eventConfig);

                    loadHistory(1);
                },
                "a[data-url] -> click"  : actionManager
            },
            init : function(){
                $("#showInfo").html(template(ShowInfo.tabA.tempInfo.tempId, {}));

                $17.delegate(ShowInfo.eventConfig);

                if($17.getQuery("recordEnter") == "vTabB"){
                    $("li.v-tab-b").click();
                }
            }
        };

        ShowInfo.init();

        //showTip Enter
        if($17.getQuery("step") == "showtip"){
            $("#showTipOptBack").show();
            $("#showInfoNoContent").show().addClass("w-opt-back-content");
            $("#showInfo").addClass("w-opt-back-content");
        }

        LeftMenu.focus("clazzmanager");

        function loadHistory(currentPage){
            $("#historyChip").load("/teacher/clazz/alteration/checkhistory.vpage?currentPage=" + currentPage, function(){
                $(".historyChip").page({
                    total           : $("#___pageInformation").text(),
                    current         : currentPage,
                    autoBackToTop   : false,
                    jumpCallBack    : function(index){
                        loadHistory(index);
                    }
                });
            });
        }
    });
</script>
</@shell.page>