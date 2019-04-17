<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="广告运营管理平台" page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/advertisement/advertisement.css" rel="stylesheet">
<div id="main_container" class="span9">
    <legend>
        <strong>广告管理</strong>&nbsp;&nbsp;&nbsp;&nbsp;
        <a href="slotindex.vpage">广告位信息</a> &nbsp;&nbsp;&nbsp;&nbsp;
        <a href="adarrangement.vpage">广告排期管理</a> &nbsp;&nbsp;&nbsp;&nbsp;
        <a id="add_advertiser_btn" href="addetail.vpage" type="button" class="btn btn-info" style="float: right">添加广告</a>
    </legend>
    <div>
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation" class="active"><a href="#complex-query" aria-controls="complex-query" role="tab" data-toggle="tab"><strong>自定义查询</strong></a></li>
            <li role="presentation"><a href="#simple-query" aria-controls="simple-query" role="tab" data-toggle="tab"><strong>快速查询</strong></a></li>
        </ul>
        <div class="tab-content">
            <div role="tabpanel" class="tab-pane active" id="complex-query">
                <form id="ad-query" class="form-horizontal">
                    <input type="hidden" id="pageNum" name="page" value="1"/>
                    <ul class="inline">
                        <li>
                            <label>广告位：&nbsp;
                                <select id="slotId" name="slotId">
                                    <option value="">所有广告位</option>
                                    <#if slotList??>
                                        <#list slotList as slot><option value = ${slot.id!}>${slot.name!}</option></#list>
                                    </#if>
                                </select>
                            </label>
                        </li>
                        <li>
                            <label>创建人：&nbsp;
                                <select id="creator" name="creator">
                                    <option value="">所有创建人</option>
                                    <#if creatorList??>
                                        <#list creatorList as c><option value = ${c!}>${c!}</option></#list>
                                    </#if>
                                </select>
                            </label>
                        </li>
                        <li>
                            <label>业务：&nbsp;
                                <select id="businessCategory" name="businessCategory" style="width: 100px">
                                    <option value="">所有</option>
                                    <#if categoryList??>
                                        <#list categoryList as c><option value = ${c.name()!}>${c.name()!}</option></#list>
                                    </#if>
                                </select>
                            </label>
                        </li>
                        <li>
                            <label>类型：&nbsp;
                                <select id="type" name="type" style="width: 100px">
                                    <option value="">所有</option>
                                    <#if typeList??>
                                        <#list typeList as t><option value = ${t.name()!}>${t.getDesc()!}</option></#list>
                                    </#if>
                                </select>
                            </label>
                        </li>
                        <br>
                        <li>
                            <label>开始日期：&nbsp;
                                <input id="startDate" name="start" type="text" value="" placeholder="2016-01-01" style="width: 100px">
                            </label>
                        </li>
                        <li>
                            <label>结束日期：&nbsp;
                                <input id="endDate" name="end" type="text" value="" placeholder="2016-12-31" style="width: 100px">
                            </label>
                        </li>
                        <li>
                            <label>上线状态：&nbsp;
                                <select id="status" name="status" style="width: 100px">
                                    <option value=-1>所有状态</option>
                                    <#if statusList??>
                                        <#list statusList as s><option value = ${s.key!}>${s.value!}</option></#list>
                                    </#if>
                                </select>
                            </label>
                        </li>
                        <li>
                            <label>审核状态：&nbsp;
                                <select id="auditStatus" name="audit" style="width: 100px">
                                    <option value=-1>所有状态</option>
                                    <option value=0>草稿</option>
                                    <option value=10>待审核</option>
                                    <option value=20>审核完成</option>
                                </select>
                            </label>
                        </li>
                        <li>
                            <label>展示状态：&nbsp;
                                <select id="expireStatus" name="expireStatus" style="width: 100px">
                                    <option value=-1>所有状态</option>
                                    <option value=0 selected>使用中</option>
                                    <option value=1>已结束</option>
                                </select>
                            </label>
                        </li>
                        <li>
                            <a id="queryBtn" class="btn btn-primary" href="javascript:void(0);"><i class="icon-search icon-white"></i> 查  询</a>
                        </li>
                    </ul>
                </form>
            </div>

            <div role="tabpanel" class="tab-pane" id="simple-query">
                <ul class="inline">
                    <li>
                        <label>广告ID或编码：&nbsp;
                            <input id="adCode" name="adCode" type="text" class="input">
                        </label>
                    </li>
                    <li>
                        <button id="adid-btn" class="btn btn-primary">
                            <i class="icon-search icon-white"></i> 查  询
                        </button>
                    </li>
                    <li>
                        <button id="create-self-btn" class="btn btn-success">
                            <i class="icon-leaf icon-white"></i> 自己创建的广告
                        </button>
                    </li>
                    <li>
                        <input type="hidden" value="" id="auditor">
                        <button id="audit-self-btn" class="btn btn-info">
                            <i class="icon-cog icon-white"></i> 自己审核的广告
                        </button>
                    </li>
                </ul>
            </div>

        </div>
    </div>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th width="50px">ID</th>
                        <th>广告名称</th>
                        <th>广告编码</th>
                        <th>所属广告位</th>
                        <th width="90px">开始时间</th>
                        <th width="90px">结束时间</th>
                        <th>优先级</th>
                        <th>创建人</th>
                        <th>广告状态</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody id="adShowList"></tbody>
                </table>
                <ul class="pager" id="pager">
                    <li class="pager-query" id="homeBtn" data-page="1"><a href="javascript:void(0);" title="首页">首页</a></li>
                    <li class="pager-query disabled" id="prevBtn" data-page="1"><a href="javascript:void(0);" title="上一页">&lt;</a></li>
                    <li class="disabled"><a>第 <span id="currentPage">1</span> 页</a></li>
                    <li class="disabled"><a>共 <span id="totalPage">1</span> 页</a></li>
                    <li class="pager-query disabled" id="nextBtn" data-page="1"><a href="javascript:void(0);" title="下一页">&gt;</a></li>
                </ul>
            </div>
        </div>
    </div>
</div>
<script type="text/html" id="T:AD_LIST">
    <%if (adList.length > 0 ) {%>
    <%for(var i = 0; i < adList.length; i++) {%>
    <%var ad = adList[i]%>
    <tr>
        <td><%=ad.id%></td>
        <td class="new_td">
            <%if (ad.hasPrivilege) {%>
                <a href="addetail.vpage?adId=<%=ad.id%>" target="_blank"><%=ad.name%></a>
                <%if (ad.hasImg) {%>
                    <a title="预览广告图" onclick="showImg('<%=ad.imgUrl%>')"><i class="icon-search"></i></a>
                <% } %>
            <% } else {%>
                <a href="javascript:void(0);" class="JS-adDisable"><%=ad.name%></a>
            <% } %>
        </td>
        <td><%==ad.adCode%></td>
        <td><%==ad.slot%></td>
        <td><%==ad.startTime%></td>
        <td><%==ad.endTime%></td>
        <td>
            <%if (ad.adjust) {%>
                <a class="ad-priority" href="javascript:void(0);" title="调整优先级"
                   data-adid="<%=ad.id%>" data-adname="<%=ad.name%>" data-slot="<%=ad.slot%>" data-prio="<%=ad.priorityVal%>"><%=ad.priority%></a>
            <% } else {%>
                <%=ad.priority%>
            <% } %>
        </td>
        <td><%=ad.creator%></td>
        <td style="text-align: left;">
             审核状态：<span style="color:<%if (ad.auditStatusValid){ %>green<% } else {%>red <% } %>"><%=ad.auditStatusName%></span>
        <br/>上线状态：<span style="color:<%if (ad.statusValid){ %>green<% } else { %>red<% } %>"><%=ad.statusName%></span>
        <br/>展示状态：<span style="color:<%if (ad.isExpired) { %>red<% } else {%>green<% } %>"><%if (ad.isExpired){ %>已结束<% } else {%>使用中<% } %></span>
        <td>
            <%if (ad.hasPrivilege){ %>
                <a title="查看广告实时数据" href="config/realtimedata.vpage?adId=<%=ad.id%>"  target="_blank">
                    <i class="icon-time"></i>
                </a>
                <a title="查看广告数据详情" href="config/dataindex.vpage?adId=<%=ad.id%>"  target="_blank">
                    <i class="icon-th-list"></i>
                </a>
                <a title="配置投放策略" href="config/adconfig.vpage?adId=<%=ad.id%>"  target="_blank">
                    <i class="icon-cog"></i>
                </a>
                <br>
                <%if (ad.canOp){ %>
                    <%if (ad.canRaise){ %>
                        <a id="raise_up_<%=ad.id%>" href="javascript:void(0);" title="转上级"><i class="icon-envelope"></i></a>
                    <% } %>
                    <a id="approve_<%=ad.id%>" href="javascript:void(0);" title="审批通过"><i class="icon-ok"></i></a>
                    <a id="reject_<%=ad.id%>" href="javascript:void(0);" title="驳回"><i class="icon-remove"></i></a>
                <% } %>
                <%if (ad.auditStatus == 21) { %>
                    <%if (ad.st != 1) { %>
                        <a id="online_<%=ad.id%>" href="javascript:void(0);" title="上线" ><i class="icon-chevron-up"></i></a>
                    <% } else {%>
                        <a id="offline_<%=ad.id%>" href="javascript:void(0);" title="下线" ><i class="icon-chevron-down"></i></a>
                    <% } %>
                <% } %>
            <% } %>
        </td>
    </tr>
    <% } %>
    <% } else { %>
    <tr>
        <td colspan="9" style="text-align: center; color: red;">未查询到广告记录</td>
    </tr>
    <% } %>
</script>
<div id="img-dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">广告素材预览</h4>
            </div>
            <div class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto; text-align: center">
                    <img id="source" alt="广告预览图" src="" style="height: 300px;">
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">关 闭</button>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="priority-dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">广告优先级调整</h4>
            </div>
            <div class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto; text-align: center">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">广告ID:</label>
                        <div class="controls">
                          <label><strong id="prio_adid"></strong></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">广告名称:</label>
                        <div class="controls">
                            <label><strong id="prio_adname"></strong></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">广告位:</label>
                        <div class="controls">
                            <label><strong id="prio_slot"></strong></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">优先级:</label>
                        <div class="controls">
                            <select class="input-large" id="adjust_priority">
                                <option value=0>置顶</option>
                                <#list [1,2,3,4,5,6,7,8,9] as p>
                                    <option value=${p}>Lv.${p}</option>
                                </#list>
                                <option value=10 selected>默认</option>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">关 闭</button>
                    <button id="priority_btn" type="button" class="btn btn-primary">确 定</button>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="reject-ad-dialog" class="modal fade hide">
    <input id="reject-ad-id" type="hidden">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">请填写驳回原因</h4>
            </div>
            <div class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">驳回原因:</label>
                        <div class="controls">
                            <textarea class="input-xlarge" id="reject_comment"></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取 消</button>
                    <button id="reject_btn" type="button" class="btn btn-primary">确 定</button>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="raise-up-dialog" class="modal fade hide">
    <input id="raise-up-id" type="hidden">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">请填写转上级描述</h4>
            </div>
            <div class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">描述:</label>
                        <div class="controls">
                            <textarea class="input-xlarge" id="raise_comment"></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取 消</button>
                    <button id="raise_btn" type="button" class="btn btn-primary">确 定</button>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">

    $(function(){

        var $queryBtn =  $('#queryBtn');

        $("#startDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        $("#endDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        $queryBtn.on('click', function () {
            var params = {
                slotId : $('#slotId').val().trim(),
                creator : $('#creator').val().trim(),
                businessCategory : $('#businessCategory').val().trim(),
                type : $('#type').val().trim(),
                startDate : $('#startDate').val().trim(),
                endDate : $('#endDate').val().trim(),
                status : $('#status').val().trim(),
                auditStatus : $('#auditStatus').val().trim(),
                expireStatus : $('#expireStatus').val().trim(),
                page : $('#pageNum').val()
            };

            console.info(params);

            $.post("adindex.vpage", params, function (res) {
                if (res.success) {
                    $('#adShowList').html(template("T:AD_LIST", {adList:res.adsList}));
                    var prev = $('#prevBtn');
                    var next = $('#nextBtn');
                    if (res.hasPre) {
                        prev.removeClass("disabled");
                        prev.attr("data-page",  res.currentPage - 1);
                    } else {
                        prev.addClass("disabled");
                    }

                    if (res.hasNext) {
                        next.removeClass("disabled");
                        next.attr("data-page",  res.currentPage + 1);
                    } else {
                        next.addClass("disabled");
                    }
                    $('#currentPage').html(res.currentPage);
                    $('#pageNum').val(res.currentPage);
                    $('#totalPage').html(res.totalPage);

                } else {
                    alert(res.info);
                }
            });
        });

        $('#adid-btn').on('click', function () {
            var params = {
                adCode : $('#adCode').val().trim(),
                page : 1
            };
            if (params.adCode == '') {
                alert("请输入广告ID或编码");
                return false;
            }

            queryAdList(params);
        });

        refreshPage();

        $('#create-self-btn').on('click', function () {
            var params = {
                creator : "${requestContext.getCurrentAdminUser().adminUserName!}",
                expireStatus : -1,
                page : 1
            };
            if (params.creator == '') {
                alert("请重新登录");
                return false;
            }

            $('#creator').val(params.creator);
            $('#expireStatus').val(-1);
            queryAdList(params);
        });

        $('#audit-self-btn').on('click', function () {
            var params = {
                auditor : "${requestContext.getCurrentAdminUser().adminUserName!}",
                expireStatus : -1,
                page : 1
            };
            if (params.auditor == '') {
                alert("请重新登录");
                return false;
            }

            $('#auditor').val(params.auditor);
            $('#expireStatus').val(-1);
            queryAdList(params);
        });

        $(document).on('click', ".JS-adDisable", function() {
            alert("对不起，您没有权限查看该广告");
        });

        $(".pager-query").on('click', function() {
            if ($(this).hasClass("disabled")) {
                return;
            }
            var pageNum = $(this).attr("data-page");
            $('#pageNum').val(pageNum);
            $('#queryBtn').click();
        });

        // 转上级功能
        $(document).on('click', "a[id^='raise_up_']", function () {
            var id = $(this).attr("id").substring("raise_up_".length);
            $('#raise-up-id').val(id);
            $('#raise-up-dialog').modal('show');

        });

        $('#raise_btn').on('click', function() {
            var id = $('#raise-up-id').val();
            var comment = $('#raise_comment').val().trim();
            $.post('raiseup.vpage', {adId: id, comment: comment}, function(data){
                if(data.success) {
                    alert("转上级成功，请等待审核");
                    refreshPage();
                } else {
                    alert(data.info);
                }
            });
        });

        // 批准功能
        $(document).on('click', "a[id^='approve_']", function () {
            if (!confirm('已确定该广告的优先级？')) {
                return false;
            }
            var id = $(this).attr("id").substring("approve_".length);
            $.post('approvead.vpage', {adId: id}, function(data){
                if(data.success) {
                    alert("批准成功！");
                    refreshPage();
                } else {
                    alert(data.info);
                }
            });
        });

        // 驳回功能
        $(document).on('click', "a[id^='reject_']", function () {
            var id = $(this).attr("id").substring("reject_".length);
            $('#reject-ad-id').val(id);
            $('#reject-ad-dialog').modal('show');
        });

        $('#reject_btn').on('click', function () {
            if (!confirm('是否确认驳回操作？')) {
                return false;
            }
            var id = $('#reject-ad-id').val();
            var comment = $('#reject_comment').val();
            if (comment == '') {
                alert("请填写驳回理由");
                return false;
            }
            $.post('rejectad.vpage',{adId: id, comment: comment}, function(data){
                if (data.success){
                    alert("驳回成功！");
                    refreshPage();
                } else {
                    alert(data.info);
                }
            });
        });

        // 上线
        $(document).on('click', "[id^='online_']", function() {
            if (!confirm("是否确认上线？")) {
                return false;
            }
            var adId = $(this).attr("id").substring("online_".length);
            $.post('adonline.vpage', {adId: adId}, function(data) {
                if(data.success) {
                    alert("上线成功");
                    refreshPage();
                } else {
                    alert(data.info);
                }
            });
        });

        // 下线
        $(document).on('click', "[id^='offline_']", function() {
            if (!confirm("下线后的广告如再次上线需要重新审核，是否确定下线？")) {
                return false;
            }
            var adId = $(this).attr("id").substring("offline_".length);
            $.post('adoffline.vpage', {adId: adId}, function(data) {
                if(data.success) {
                    alert("下线成功！");
                    refreshPage();
                } else {
                    alert(data.info);
                }
            });
        });

        // 调整优先级
        $(document).on('click', '.ad-priority', function() {
            var $this = $(this);
            $('#prio_adid').html($this.data().adid);
            $('#prio_adname').html($this.data().adname);
            $('#prio_slot').html($this.data().slot.split("<br/>").join("-"));
            $('#adjust_priority').val($this.data().prio);
            $('#priority-dialog').modal("show");
        });

        $('#priority_btn').on('click', function() {
           var param = {
               adId: $('#prio_adid').html().trim(),
               priority: $('#adjust_priority').val()
           };
            $.post('adpriority.vpage', param, function(res) {
                if(res.success) {
                    alert("优先级修改成功");
                    $('#priority-dialog').modal("hide");
                    refreshPage();
                } else {
                    alert(res.info);
                }
            });
        });
    });

    function showImg(url) {
        var src = "${prePath?string}";
        $('#source').attr("src", src + url);
        $('#img-dialog').modal('show');
    }

    function queryAdList(params) {
        $.post("adindex.vpage", params, function (res) {
            if (res.success) {
                $('#adShowList').html(template("T:AD_LIST", {adList:res.adsList}));
                var prev = $('#prevBtn');
                var next = $('#nextBtn');
                if (res.hasPrev) {
                    prev.removeClass("disabled");
                    prev.attr("data-page",  res.currentPage - 1);
                } else {
                    prev.addClass("disabled");
                }

                if (res.hasNext) {
                    next.removeClass("disabled");
                    next.attr("data-page",  res.currentPage + 1);
                } else {
                    next.addClass("disabled");
                }
                $('#currentPage').html(res.currentPage);
                $('#pageNum').val(res.currentPage);
                $('#totalPage').html(res.totalPage);

            } else {
                alert(res.info);
            }
        });
    }

    function refreshPage() {
//        window.location.reload();
        $('#queryBtn').click();
    }

</script>
</@layout_default.page>