<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='运营信息' page_num=26>
<style>
    .table-responsive {
        min-height: .01%;
        overflow-x: auto
    }

    @media screen and (max-width: 767px) {
        .table-responsive {
            width: 100%;
            margin-bottom: 15px;
            overflow-y: hidden;
            -ms-overflow-style: -ms-autohiding-scrollbar;
            border: 1px solid #ddd
        }

        .table-responsive > .table {
            margin-bottom: 0
        }

        .table-responsive > .table > tbody > tr > td, .table-responsive > .table > tbody > tr > th, .table-responsive > .table > tfoot > tr > td, .table-responsive > .table > tfoot > tr > th, .table-responsive > .table > thead > tr > td, .table-responsive > .table > thead > tr > th {
            white-space: nowrap
        }

        .table-responsive > .table-bordered {
            border: 0
        }

        .table-responsive > .table-bordered > tbody > tr > td:first-child, .table-responsive > .table-bordered > tbody > tr > th:first-child, .table-responsive > .table-bordered > tfoot > tr > td:first-child, .table-responsive > .table-bordered > tfoot > tr > th:first-child, .table-responsive > .table-bordered > thead > tr > td:first-child, .table-responsive > .table-bordered > thead > tr > th:first-child {
            border-left: 0
        }

        .table-responsive > .table-bordered > tbody > tr > td:last-child, .table-responsive > .table-bordered > tbody > tr > th:last-child, .table-responsive > .table-bordered > tfoot > tr > td:last-child, .table-responsive > .table-bordered > tfoot > tr > th:last-child, .table-responsive > .table-bordered > thead > tr > td:last-child, .table-responsive > .table-bordered > thead > tr > th:last-child {
            border-right: 0
        }

        .table-responsive > .table-bordered > tbody > tr:last-child > td, .table-responsive > .table-bordered > tbody > tr:last-child > th, .table-responsive > .table-bordered > tfoot > tr:last-child > td, .table-responsive > .table-bordered > tfoot > tr:last-child > th {
            border-bottom: 0
        }
    }

    .wh {
        width: 100px;
        text-align: center;
        white-space: nowrap;
        text-overflow: ellipsis; /* for IE */
        overflow: hidden;
    }

    .widSix {
        width: 60px;
    }

    .widNine {
        width: 90px;
    }

    .widEight {
        width: 80px;
    }
    .widNo {
        width: 120px;
    }

    .table_box {
        height:650px;
    }

    .table_box table tbody tr td {
        white-space: nowrap;
    }

    .form-group {
        margin: 5px 0;
        display: inline-block;
    }

    .form-group .mylabel {
        width: 150px;
        text-align: right;
    }

    .wx-num-btn, .wx-name-btn {
        border-bottom: dashed 1px #8c8c8c;
        cursor: pointer;
    }
</style>
<div id="main_container" class="span9">
    <div class="row-fluid">
        <div class="span12">
            <#--<h2>${clazzName}</h2>-->
                <div>
                    <h2 style="float: left">${clazzName}</h2>
                    <form id="frm1" class="form form-inline form-horizontal" style="float: right;">
                        <div class="form-group">
                            <input type="hidden" name="productId" value="${productId!}">
                            <label for="" class="mylabel">班级(Class)：</label>
                            <select id="clazzId" data-init='false' name="clazzId" class="multiple district_select">
                                <option value="">----请选择----</option>
                                <#if clazzOptionList?size gt 0>
                                    <#list clazzOptionList as e >
                                        <option value="${e.value!}" <#if e.selected>selected</#if>>${e.desc!}</option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                        <div class="form-group" style="text-align: center;width: 150px;">
                            <button type="button" id="find" class="btn btn-info">查询</button>
                        </div>
                    </form>
                </div>
            <table class="table table-bordered">
                <tr>
                    <td>班级ID(Class ID)</td>
                    <td>${clazz.clazzId!}</td>
                    <td>课程(Book)</td>
                    <td>${clazz.bookName!}</td>
                </tr>
                <tr>
                    <td>班主任(Teacher)</td>
                    <td>${clazz.clazzTeacherName!}</td>
                    <td>产品(Product)</td>
                    <td>${clazz.productName!}</td>
                </tr>
                <tr>
                    <td>用户上限(Limitation)</td>
                    <td>${clazz.userLimitation!}</td>
                    <td>用户数(Count)</td>
                    <td>${clazz.userCount!}</td>
                </tr>
                <tr>
                    <td>建立时间(Built-up Time)</td>
                    <td>${clazz.createTime!}</td>
                    <td></td>
                    <td></td>
                </tr>
            </table>
            <div class="well" style="font-size: 14px;">
                <form id="frm" class="form form-inline form-horizontal" action="/chips/chips/clazz/manager/operationInfo.vpage">
                    <div class="form-group">
                        <input type="hidden" id="pageNumber" name="pageNumber" value="${pageNumber!}">
                        <input type="hidden" id="productId" name="productId" value="${productId!}">
                        <input type="hidden" id="wxAdd" name="wxAdd" value="${wxAdd!}">
                        <input type="hidden" id="epWxAdd" name="epWxAdd" value="${epWxAdd!}">
                        <input type="hidden" id="wxLogin" name="wxLogin" value="${wxLogin!}">
                        <input type="hidden" id="wxCodeShowType" name="wxCodeShowType" value="${wxCodeShowType!}">
                        <input type="hidden" id="wxNickName" name="wxNickName" value="${wxNickName!}">
                        <label class="mylabel">用户Id(User ID)：</label>
                        <input type="text" name="userId" id="userId"
                               placeholder="模糊搜索" <#if userId ??> value=${userId!} </#if>>
                        <input type="hidden" id="clazzId" name="clazzId" value="${clazzId!}"/>
                    </div>
                    <div class="form-group">
                        <label class="mylabel">定级(Grading)：</label>
                        <select id="grading" data-init='false' name="grading" class="multiple district_select">
                            <option value="">----请选择----</option>
                            <#if gradingOptionList?size gt 0>
                                <#list gradingOptionList as e >
                                    <option value="${e.value!}" <#if e.selected>selected</#if>>${e.desc!}</option>
                                </#list>
                            </#if>
                        </select>
                    </div>
                    <div class="form-group">
                        <input type="number" name="jztConsumerMin" class="input-small" value="${jztConsumerMin!}"/>
                        &nbsp;<&nbsp;家长通消费(Consumption/JZT)&nbsp;<&nbsp;
                        <input type="number" name="jztConsumerMax" class="input-small" value="${jztConsumerMax!}"/>
                    </div>
                    <div class="form-group">
                        <input type="number" name="serviceScoreMin" class="input-small" value="${serviceScoreMin!}"/>
                        &nbsp;<&nbsp;服务价值&nbsp;<&nbsp;
                        <input type="number" name="serviceScoreMax" class="input-small" value="${serviceScoreMax!}"/>
                    </div>
                    <div class="form-group" style="text-align: center;width: 150px;">
                        <button type="button" id="filter" class="btn btn-primary">筛 选(Filter)</button>
                    </div>
                    <div class="form-group" style="text-align: center;width: 150px;">
                        <button type="button" id="export" class="btn btn-primary">导 出(Export)</button>
                    </div>

                </form>
            </div>
        </div>
    </div>
    <ul class="nav nav-tabs" role="tablist">
        <li role="presentation">
            <a href="basicInfo.vpage?clazzId=${clazz.clazzId!}&productId=${productId!}&dataType=0">基础信息(Basic)</a>
        </li>
        <li role="presentation" class="active">
            <a href="operationInfo.vpage?clazzId=${clazz.clazzId!}&productId=${productId!}&wxAdd=2&wxLogin=2&wxCodeShowType=2&wxNickName=2<#if userId ??>&userId=${userId!}</#if>">运营信息(Operation)</a>
        </li>
        <li role="presentation">
            <a href="userScore.vpage?clazzId=${clazz.clazzId!}&productId=${productId!}<#if userId ??>&userId=${userId!}</#if>">用户成绩(User)</a>
        </li>
        <li role="presentation">
            <a href="generalInfo.vpage?clazzId=${clazz.clazzId!}&productId=${productId!}<#if userId ??>&userId=${userId!}</#if>">综合信息(General)</a>
        </li>
    </ul>
    <div id="tableDiv" class="table-responsive table_box">
        <table class="table table-condensed  table-hover table-striped table-bordered" style="table-layout:fixed">
            <tr>
                <td class="wh widSix" title="姓名">姓名</td>
                <td class="wh widEight" title="用户ID">用户ID</td>
                <td class="wh widNo" title="电话">电话</td>
                <td class="wh" title="是否加微信" style="width: 100px;">
                    <span>是否加微信</span><br />
                    <select style="width: 80px;" onchange="wxAddStatusChange(event.target.value)">
                        <option value="2" <#if wxAdd?? && wxAdd == 2>selected</#if>>全部</option>
                        <option value="1" <#if wxAdd?? && wxAdd == 1>selected</#if>>是</option>
                        <option value="0" <#if wxAdd?? && wxAdd == 0>selected</#if>>否</option>
                    </select>
                </td>
                <td class="wh" title="是否加企业微信" style="width: 100px;">
                    <span>是否加企业微信</span><br />
                    <select style="width: 80px;" onchange="epWxAddStatusChange(event.target.value)">
                        <option value="2" <#if epWxAdd?? && epWxAdd == 2>selected</#if>>全部</option>
                        <option value="1" <#if epWxAdd?? && epWxAdd == 1>selected</#if>>是</option>
                        <option value="0" <#if epWxAdd?? && epWxAdd == 0>selected</#if>>否</option>
                    </select>
                </td>

                <td class="wh" title="微信号" style="width: 100px;">
                    <span>微信号</span><br />
                    <select style="width: 80px;" onchange="wxCodeShowTypeStatusChange(event.target.value)">
                        <option value="2" <#if wxCodeShowType?? && wxCodeShowType == 2>selected</#if>>全部</option>
                        <option value="1" <#if wxCodeShowType?? && wxCodeShowType == 1>selected</#if>>已填写</option>
                        <option value="0" <#if wxCodeShowType?? && wxCodeShowType == 0>selected</#if>>未填写</option>
                    </select>
                </td>

                <td class="wh" title="微信昵称" style="width: 100px;">
                    <span>微信昵称</span><br />
                    <select style="width: 80px;" onchange="wwxNickNameStatusChange(event.target.value)">
                        <option value="2" <#if wxNickName?? && wxNickName == 2>selected</#if>>全部</option>
                        <option value="1" <#if wxNickName?? && wxNickName == 1>selected</#if>>已填写</option>
                        <option value="0" <#if wxNickName?? && wxNickName == 0>selected</#if>>未填写</option>
                    </select>
                </td>

                <td class="wh widSix" title="是否登录公众号" style="width: 100px;">
                    <span>是否登录公众号</span><br/>
                    <select style="width: 80px;" onchange="wxLoginStatusChange(event.target.value)">
                        <option value="2" <#if wxLogin?? && wxLogin == 2>selected</#if>>全部</option>
                        <option value="1" <#if wxLogin?? && wxLogin == 1>selected</#if>>是</option>
                        <option value="0" <#if wxLogin?? && wxLogin == 0>selected</#if>>否</option>
                    </select>
                </td>
                <td class="wh" style="width: 60px;" title="服务价值">用户活跃</td>
                <td class="wh" style="width: 60px;" title="服务价值">服务价值</td>
                <td class="wh widEight" title="家长通消费">家长通消费</td>
                <td class="wh widEight" title="完成率">完成率</td>
                <td class="wh widEight"  title="最新成绩">最新成绩</td>
                <td class="wh" colspan="2">操作</td>
            </tr>
            <#if operatingList?? && operatingList?size gt 0>
                <#list operatingList as operInfo >
                    <tr>
                        <td class="wh widSix" title="${operInfo.userName!}" style="cursor: pointer" onclick="userNameClick('${operInfo.userId!}')">${operInfo.userName!}</td>
                        <#--<td >${operInfo.userId!}</td>-->
                        <td class="wh widSix" title="${operInfo.userId!}"><span id="id-${operInfo.userId!}" onclick="copyToClipBoard(${operInfo.userId!})">${operInfo.userId!}</span></td>

                        <td class="wh">
                            <#if operInfo.userId??>
                                <button type="button" id="query_user_phone_${operInfo.userId!''}" class="btn btn-info">查 看</button>
                            </#if>
                        </td>
                        <td class="wh">
                            <input type="checkbox" onclick="checkboxOnclick(this)" value="${operInfo.userId!}"
                                   <#if operInfo.wxAdd?? && operInfo.wxAdd == true>checked</#if>>
                        </td>
                        <td class="wh">
                            <input type="checkbox" onclick="wpWxAddCheckboxOnclick(this)" value="${operInfo.userId!}"
                                   <#if operInfo.epWxAdd?? && operInfo.epWxAdd == true>checked</#if>>
                        </td>
                        <td class="wh">
                            <#if operInfo.wechatNumber?? && operInfo.wechatNumber != "">
                            <span class="wx-num-btn" onclick="wxNumEdit('${operInfo.userId!}', '${operInfo.wechatNumber!}')">
                                ${operInfo.wechatNumber}
                            </span>
                            <#else >
                            <span class="wx-num-btn" onclick="wxNumEdit('${operInfo.userId!}', '')">
                                未填写
                            </span>
                            </#if>
                        </td>
                        <td class="wh">
                            <#if operInfo.wxName??>
                            <span class="wx-name-btn" onclick="wxNameEdit('${operInfo.userId!}', '${operInfo.wxName!}')">
                                ${operInfo.wxName}
                            </span>
                            <#else >
                            <span class="wx-name-btn" onclick="wxNameEdit('${operInfo.userId!}', '')">
                                未填写
                            </span>
                            </#if>
                        </td>
                        <#if operInfo.registeredInWeChatSubscription?? && operInfo.registeredInWeChatSubscription == true>
                            <td class="wh" >是</td>
                        <#else >
                            <td class="wh" title="">否</td>
                        </#if>
                        <td class="wh" title="${operInfo.articleViewNum!}">${operInfo.articleViewNum!}</td>
                        <td class="wh" title="${operInfo.serviceScore!}">${operInfo.serviceScore!}</td>
                        <td class="wh" title="${operInfo.consumption_JZT!}">${operInfo.consumption_JZT!}</td>
                        <td class="wh widNine">
                            <#list operInfo.scoreSimpleInfos as simpleInfo >
                                <#if operInfo.scoreSimpleInfos?size gt 1>
                                    <span title="${simpleInfo.bookName}">${simpleInfo.finishedNum!} / ${simpleInfo.totalNum!}</span><br />
                                <#else>
                                    ${simpleInfo.finishedNum!} / ${simpleInfo.totalNum!}
                                </#if>
                            </#list>
                        </td>
                        <td class="wh widSix">
                            <#list operInfo.scoreSimpleInfos as simpleInfo >
                                <#if operInfo.scoreSimpleInfos?size gt 1>
                                <span title="${simpleInfo.bookName}">
                                    <#if simpleInfo.recentlyScore == -1>
                                        未完成
                                    <#else>
                                    <a href="/chips/user/question/index.vpage?userId=${operInfo.userId!}&productId=${productId!}&unitId=${simpleInfo.recentlyUnitId!}">${simpleInfo.recentlyScore!}</a>
                                    </#if>
                                    <br />
                                </span>
                                <#else >
                                    <#if simpleInfo.recentlyScore == -1>
                                        未完成
                                    <#else>
                                        <a href="/chips/user/question/index.vpage?userId=${operInfo.userId!}&bookId=${simpleInfo.bookId!}&unitId=${simpleInfo.recentlyUnitId!}">${simpleInfo.recentlyScore!}</a>
                                    </#if>
                                </#if>
                            </#list>
                        </td>
                        <td>
                            <button type="button" userId="${operInfo.userId!}" onclick="selectClick(${operInfo.userId!})"
                                    class="btn btn-primary">查询
                            </button>
                        </td>
                        <td>
                            <#list operInfo.scoreSimpleInfos as simpleInfo >
                                <#if operInfo.scoreSimpleInfos?size gt 1>
                                <button id="video-btn-${operInfo.userId!}-${simpleInfo.bookId!}" type="button" userId="${operInfo.userId!}"
                                        onclick="videoClick(${operInfo.userId!}, '${simpleInfo.bookId!}')"
                                        class="btn btn-primary" title="${simpleInfo.bookName!}"
                                >
                                    视频
                                </button>
                                <br />
                                <#else>
                                <button id="video-btn-${operInfo.userId!}-${simpleInfo.bookId!}" type="button" userId="${operInfo.userId!}"
                                        onclick="videoClick(${operInfo.userId!}, '${simpleInfo.bookId!}')"
                                        class="btn btn-primary"
                                >
                                    视频
                                </button>
                                </#if>
                            </#list>

                        </td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
    <ul class="pager">
        <#if (pageData.hasPrevious())>
            <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
        <#else>
            <li class="disabled"><a href="#">上一页</a></li>
        </#if>
        <#if (pageData.hasNext())>
            <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
        <#else>
            <li class="disabled"><a href="#">下一页</a></li>
        </#if>
        <li>当前第 ${pageNumber!} 页 |</li>
        <li>共 ${pageData.totalPages!} 页</li>
        <#--<li>|共 ${total !} 条</li>-->
    </ul>
</div>

<div id="wxNumEditModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="myModalLabel">微信号修改</h3>
    </div>
    <div class="modal-body">
        微信号：<input type="text" id="wxNumInput" value="">
        <input type="hidden" id="userIdWxNum" value="">
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
        <button class="btn btn-primary" id="wxNumSave">保 存</button>
    </div>
</div>

<div id="wxNameEditModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="myModalLabel">微信昵称修改</h3>
    </div>
    <div class="modal-body">
        微信昵称：<input type="text" id="wxNameInput" value="">
        <input type="hidden" id="userIdWxName" value="">
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
        <button class="btn btn-primary" id="wxNameSave">保 存</button>
    </div>
</div>

<script type="text/javascript">
    function pagePost(pageNumber) {
        $("#pageNumber").val(pageNumber);
        $("#frm").attr('action', "/chips/chips/clazz/manager/operationInfo.vpage");
        $("#frm").submit();
    }

    function selectClick(userId) {
        window.location.href = "/chips/user/ai/detail.vpage?userId=" + userId;
    };

    function modifyClick(clazzId, userId) {
        window.location.href = "/chips/chips/clazz/userInfoModify.vpage?userId=" + userId + "&clazzId=" + clazzId;
    };
    $(function () {
        $("#filter").on('click', function () {
            $("#frm").attr('action', "/chips/chips/clazz/manager/operationInfo.vpage");
            $("#frm").submit();
        });

        $("#find").on('click', function () {
            $("#frm1").attr('action', "/chips/chips/clazz/manager/operationInfo.vpage");
            $("#frm1").submit();
        });
        $("#export").on('click', function () {
            $("#frm").attr('action', "/chips/chips/clazz/operationInfoNewExport.vpage");
            $("#frm").submit();
        });
    });

    function wxAddStatusChange(status) {
        $("#wxAdd").val(status);
        $("#frm").attr('action', "/chips/chips/clazz/manager/operationInfo.vpage");
        $("#frm").submit();
    }

    function epWxAddStatusChange(status) {
        $("#epWxAdd").val(status);
        $("#frm").attr('action', "/chips/chips/clazz/manager/operationInfo.vpage");
        $("#frm").submit();
    }

    function wxCodeShowTypeStatusChange(status) {
        $("#wxCodeShowType").val(status);
        $("#frm").attr('action', "/chips/chips/clazz/manager/operationInfo.vpage");
        $("#frm").submit();
    }

    function wwxNickNameStatusChange(status) {
        $("#wxNickName").val(status);
        $("#frm").attr('action', "/chips/chips/clazz/manager/operationInfo.vpage");
        $("#frm").submit();
    }

    function wxLoginStatusChange(status) {
        $("#wxLogin").val(status);
        $("#frm").attr('action', "/chips/chips/clazz/manager/operationInfo.vpage");
        $("#frm").submit();
    }

    function checkboxOnclick(checkbox) {
        var userId = checkbox.value;
        var isCheck = checkbox.checked;
        $.ajax({
            url: "/chips/chips/clazz/saveWxAddStatus.vpage",
            type: "POST",
            data: {
                "userId": userId,
                "wxAddStatus": isCheck
            },
            success: function (res) {
                if (res.success) {
                    window.alert("操作成功");
                   window.location.reload();
                } else {
                    alert("操作失败");
                }
            },
            error: function (e) {
                alert("操作失败");
            }
        });
    }

    function wpWxAddCheckboxOnclick(checkbox) {
        var userId = checkbox.value;
        var isCheck = checkbox.checked;
        $.ajax({
            url: "/chips/chips/clazz/saveEpWxAddStatus.vpage",
            type: "POST",
            data: {
                "userId": userId,
                "epWxAddStatus": isCheck
            },
            success: function (res) {
                if (res.success) {
                    window.alert("操作成功");
                    window.location.reload();
                } else {
                    alert("操作失败");
                }
            },
            error: function (e) {
                alert("操作失败");
            }
        });
    }

    function userNameClick(userId) {
        $("#userId").val(userId);
        $("#frm").attr('action', "/chips/chips/clazz/manager/operationInfo.vpage");
        $("#frm").submit();
    }
    function copyToClipBoard(id) { //复制到剪切板
        var range = document.createRange();
        range.selectNode(document.getElementById('id-' + id));

        var selection = window.getSelection();
        if(selection.rangeCount > 0) selection.removeAllRanges();
        selection.addRange(range);
        document.execCommand("Copy");
    }
    function videoClick(userId, bookId) {
        window.open('/chips/user/video/comment/list.vpage?userId=' + userId + '&book=' + bookId + '&unit=');
        $('#video-btn-' + userId + '-' + bookId).css('background', '#11b511');
    }
    $("#tableDiv").scroll(function(){
        var left=$("#tableDiv").scrollLeft();
        var trs=$("#tableDiv table tr");
        trs.each(function(i){
            if(i != 0) {
                $(this).children().eq(0).css({
                    "position": "relative",
                    "top": "0px",
                    "left": left,
                    "background-color": "#f9f9f9"
                });
            }
        });
        var top = $("#tableDiv").scrollTop();
        trs.eq(0).children().each(function (i) {
            if(i != 0) {
                $(this).css({"position": "relative", "top": top, "left": "0px", "background-color": "#f9f9f9"});
            }
        })
        trs.eq(0).children().eq(0).css({"position": "relative", "top": top, "left": left, "background-color": "#f9f9f9", "z-index": 1});
    });

    function wxNumEdit(userId, value) {
        $('#wxNumEditModal').on('shown', function () {
            $('#wxNumInput').val(value);
            $('#userIdWxNum').val(userId);
        });
        $('#wxNumEditModal').modal({keyboard: false});
    }

    function wxNameEdit(userId, value) {
        $('#wxNameEditModal').on('shown', function () {
            $('#wxNameInput').val(value);
            $('#userIdWxName').val(userId);
        });
        $('#wxNameEditModal').modal({keyboard: false});
    }

    $('#wxNumSave').on('click', function () {
        var wxNum = $('#wxNumInput').val();
        if (wxNum === '') {
            window.alert('微信号不能为空');
            return;
        }
        var userId = $('#userIdWxNum').val();
        $.post("/chips/user/ai/userWxNumSave.vpage", {
            userId: userId,
            wxNum: wxNum
        }, function (res) {
            if (res.success === true) {
                $('#frm').attr('action', '/chips/chips/clazz/manager/operationInfo.vpage');
                $('#frm').submit();
            } else {
                window.alert('用户微信号保存失败！');
            }
        });
    });

    $('#wxNameSave').on('click', function () {
        var wxName = $('#wxNameInput').val();
        if (wxName === '') {
            window.alert('微信昵称不能为空');
            return;
        }
        var userId = $('#userIdWxName').val();
        $.post("/chips/user/ai/userWxNameSave.vpage", {
            userId: userId,
            wxName: wxName
        }, function (res) {
            if (res.success === true) {
                $('#frm').attr('action', '/chips/chips/clazz/manager/operationInfo.vpage');
                $('#frm').submit();
            } else {
                window.alert('用户微信昵称保存失败！');
            }

        });
    });

</script>
</@layout_default.page>