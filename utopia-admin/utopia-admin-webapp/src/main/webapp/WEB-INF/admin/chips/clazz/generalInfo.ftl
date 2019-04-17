<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='综合信息' page_num=26>
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
                        <label class="mylabel">用户Id(User ID)：</label>
                        <input type="text" name="userId"
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
                    <div class="form-group" style="text-align: center;width: 150px;">
                        <button type="button" id="filter" class="btn btn-primary">筛 选(Filter)</button>
                    </div>
                    <div class="form-group" style="text-align: center;width: 150px;">
                        <button type="button" id="export" class="btn btn-primary">导 出(Export)</button>
                    </div>

                    <div class="form-group" style="text-align: center;width: 150px;">
                        <button type="button" id="exportQuestionnaire" class="btn btn-primary">问卷导出</button>
                    </div>
                    <div class="form-group" style="text-align: center;width: 150px;">
                        <button type="button" id="exportMailAddress" class="btn btn-primary">邮寄地址导出</button>
                    </div>
                    <div class="form-group" style="text-align: center;width: 150px;">
                        <button type="button" id="exportOralTest" class="btn btn-primary">口语测试导出</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <ul class="nav nav-tabs" role="tablist">
        <li role="presentation">
            <a href="basicInfo.vpage?clazzId=${clazz.clazzId!}&productId=${productId!}&dataType=0">基础信息(Basic)</a>
        </li>
        <li role="presentation">
            <a href="operationInfo.vpage?clazzId=${clazz.clazzId!}&productId=${productId!}&wxAdd=2&wxLogin=2&wxCodeShowType=2&wxNickName=2<#if userId ??>&userId=${userId!}</#if>">运营信息(Operation)</a>
        </li>
        <li role="presentation">
            <a href="userScore.vpage?clazzId=${clazz.clazzId!}&productId=${productId!}<#if userId ??>&userId=${userId!}</#if>">用户成绩(User)</a>
        </li>
        <li role="presentation" class="active">
            <a href="generalInfo.vpage?clazzId=${clazz.clazzId!}&productId=${productId!}">综合信息(General)</a>
        </li>
    </ul>
    <div id="tableDiv" class="table-responsive table_box" >
        <table class="table table-condensed  table-hover table-striped table-bordered" style="table-layout:fixed">
            <tr>
                <td class="wh widSix" title="姓名(Name)">姓名</br>Name</td>
                <td class="wh widEight" title="用户ID(User ID)">用户ID</br>User ID</td>
                <td class="wh widEight" width="80px" title="报名日期(Register Date)">报名日期</br>Register Date</td>
                <td class="wh widSix" title="购课次数(Purchase Times)">购课次数</br>Purchase Times</td>
                <td class="wh widEight" title="薯条总消费(Consumption/Fries)">薯条总消费</br>Consumption/Fries</td>
                <td class="wh widEight" title="家长通消费(Consumption/JZT)">家长通消费</br>Consumption/JZT</td>
                <td class="wh" style="width: 90px" title="订单来源(Buy From)">订单来源</br>Buy From</td>
                <td class="wh widSix" title="省份(Province)">省份</br>Province</td>
                <td class="wh widSix" title="是否登录公众号(Registered in Wechat Subscription)">是否登录公众号</br>Registered in
                    Wechat Subscription
                </td>
                <td class="wh widSix" title="微信号(Wechat Account)">微信号</br>Wechat Account</td>
                <td class="wh widSix" title="是否进群(Joined Group)">是否进群</br>Joined Group</td>
                <td class="wh widSix" title="学习年限(Duration)">学习年限</br>Duration</td>
                <td class="wh" title="最后活跃(Latest Active)">最后活跃</br>Latest Active</td>
                <td class="wh" title="服务价值(Service Value)">服务价值</br>Service Value</td>
                <td style="width: 200px;" colspan="3">操作</td>
            </tr>
            <#if operatingList?? && operatingList?size gt 0>
                <#list operatingList as operInfo >
                    <tr>
                        <td class="wh widSix" title="${operInfo.userName!}">${operInfo.userName!}</td>
                        <#--<td class="wh widSix" title="${operInfo.userId!}">${operInfo.userId!}</td>-->
                        <td class="wh widSix" title="${operInfo.userId!}"><span id="id-${operInfo.userId!}" onclick="copyToClipBoard(${operInfo.userId!})">${operInfo.userId!}</span></td>

                        <td class="wh widNine" title="${operInfo.registerDate!}">${operInfo.registerDate!}</td>
                        <td class="wh widSix" title="${operInfo.purchaseTimes!}">${operInfo.purchaseTimes!}</td>
                        <td class="wh" title="${operInfo.consumption_Fries!}">${operInfo.consumption_Fries!}</td>
                        <td class="wh" title="${operInfo.consumption_JZT!}">${operInfo.consumption_JZT!}</td>
                        <td class="wh" title="${operInfo.buyFrom!}">${operInfo.buyFrom!}</td>
                        <td class="wh" title="${operInfo.province!}">${operInfo.province!}</td>
                        <#if operInfo.registeredInWeChatSubscription??>
                            <td class="wh" title="${operInfo.registeredInWeChatSubscription ?string("是","否")}">${operInfo.registeredInWeChatSubscription?string("是","否")}</td>
                        <#else ><td class="wh" title=""></td>
                        </#if>
                        <td class="wh" title="${operInfo.wechatNumber!}">${operInfo.wechatNumber!}</td>
                        <#if operInfo.joinedGroup??><td class="wh" title="${operInfo.joinedGroup?string("是","否")}">${operInfo.joinedGroup?string("是","否")}</td>
                        <#else ><td class="wh" title=""></td>
                        </#if>
                        <td class="wh" title="${operInfo.duration!}">${operInfo.duration!}</td>
                        <td class="wh" title="${operInfo.latestActive!}">${operInfo.latestActive!}</td>
                        <td class="wh" title="${operInfo.serviceScore!}">${operInfo.serviceScore!}</td>
                        <td class="wh">
                            <button type="button" userId="${operInfo.userId!}" onclick="selectClick(${operInfo.userId!})"
                                    class="btn btn-primary">查询
                            </button>
                        </td>
                        <td class="wh">
                            <button type="button" userId="${operInfo.userId!}"
                                    onclick="modifyClick(${clazz.clazzId!},${operInfo.userId!})"
                                    class="btn btn-primary">编辑
                            </button>
                        </td>
                        <td class="wh">
                            <button type="button" userId="${operInfo.userId!}"
                                    onclick="changeClick(${clazz.clazzId!},${operInfo.userId!})"
                                    class="btn btn-primary">调级
                            </button>
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

<script type="text/javascript">
    function pagePost(pageNumber) {
        $("#pageNumber").val(pageNumber);
        $("#frm").attr('action', "/chips/chips/clazz/manager/generalInfo.vpage");
        $("#frm").submit();
    }

    function selectClick(userId) {
        window.location.href = "/chips/user/ai/detail.vpage?userId=" + userId;
    };

    function modifyClick(clazzId, userId) {
        window.location.href = "/chips/chips/clazz/userInfoModify.vpage?userId=" + userId + "&clazzId=" + clazzId;
    };
    function changeClick(clazzId, userId) {
        window.location.href = "/chips/chips/clazz/userInfoClazzChange.vpage?userId=" + userId + "&clazzId=" + clazzId;
    };
    $(function () {
        $("#filter").on('click', function () {
            $("#frm").attr('action', "/chips/chips/clazz/manager/generalInfo.vpage");
            $("#frm").submit();
        });
        $("#find").on('click', function () {
            $("#frm1").attr('action', "/chips/chips/clazz/manager/generalInfo.vpage");
            $("#frm1").submit();
        });
        $("#export").on('click', function () {
            $("#frm").attr('action', "/chips/chips/clazz/operationInfoExport.vpage");
            $("#frm").submit();
        });
        $("#exportQuestionnaire").on('click', function () {
            $("#frm").attr('action', "/chips/chips/clazz/questionnaireInfoExport.vpage");
            $("#frm").submit();
        });
        $("#exportOralTest").on('click', function () {
            $("#frm").attr('action', "/chips/chips/clazz/oralTestScheduleExport.vpage");
            $("#frm").submit();
        });
        $("#exportMailAddress").on('click', function () {
            $("#frm").attr('action', "/chips/chips/clazz/mailAddressExport.vpage");
            $("#frm").submit();
        });
    });
    function copyToClipBoard(id) { //复制到剪切板
        const range = document.createRange();
        range.selectNode(document.getElementById('id-' + id));

        const selection = window.getSelection();
        if(selection.rangeCount > 0) selection.removeAllRanges();
        selection.addRange(range);
        document.execCommand("Copy");
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
</script>
</@layout_default.page>