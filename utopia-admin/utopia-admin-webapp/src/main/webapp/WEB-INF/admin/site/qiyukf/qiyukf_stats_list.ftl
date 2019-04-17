<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='七鱼数据统计' page_num=4>
<#if error?? && error?has_content>
<h1>${error}</h1>
<#else>
<style>
    span { font: "arial"; }
</style>

<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>七鱼数据统计(${showCount!""})</legend>
            <ul class="nav nav-tabs" role="tablist">
                <li role="presentation" <#if tab?? && tab == 1>class="active"</#if>>
                    <a href="/site/qiyukf/stats/query.vpage">七鱼会话数据</a>
                </li>
                <li role="presentation" <#if tab?? && tab == 2>class="active"</#if>>
                    <a href="/site/qiyukf/stats/query.vpage?tab=2">七鱼呼叫通话记录</a>
                </li>
            </ul>
        </fieldset>

        <div id = "activityTable" >
            <!-- 会话数据 -->
            <#if tab?? && tab == 1>
            <form id="ad-query" class="form-horizontal" method="get"
                  action="">
                <div style="margin-bottom: 10px">
                                <span style="white-space: nowrap;">
                                    <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
                                    <input type="hidden" id="tab" name="tab" value="1"/>
                                    <input type="hidden" id="isExport" name="isExport"/>
                                    会话ID： <input type="text" id="sessionId" name="sessionId" value="${sessionId!""}"/>
                                    会话开始时间
                                    <span>
                                        <input id="sessionTimeStart" name="sessionTimeStart" style="width: 12em;" <#if sessionTimeStart?has_content>value="${(sessionTimeStart?html)!''}" </#if> data-role="date" data-inline="true" type="text"/>
                                        ---
                                        <input id="sessionTimeEnd" name="sessionTimeEnd" style="width: 12em;" <#if sessionTimeEnd?has_content>value="${(sessionTimeEnd?html)!''}" </#if> data-role="date" data-inline="true" type="text"/>
                                    </span>
                                 </span>

                </div>

                <button type="button" class="btn btn-primary" onclick="queryChange()">查询</button>
                &nbsp;
                <button type="button" class="btn" onclick="resetQuery()">重置</button>
                &nbsp;
                <button type="button" class="btn btn-success" onclick="exportQuery()">导出(默认导出昨天的数据)</button>
                <div style="font-size: 14px; margin-top: 10px">
                    <div>
                        <span style="color: red;">会话关闭原因：</span>
                        0= 客服关闭
                        1=用户离开
                        2=用户不说话 自动关 闭了
                        3=机器人会话转接到人工
                        4=客服离开
                        5=客服主动将会话转出
                        6=管理员强势接管会
                        话，或访客再次申请其他客服
                        7=访客关闭
                    </div>
                </div>
            </form>
            <table class="table table-bordered">
                <tr>
                    <th>会话ID</th>
                    <th>开始时间</th>
                    <th>结束时间</th>
                    <th>会话分类</th>
                    <th>满意度值</th>
                    <th>满意度评价内容</th>
                    <th>关闭原因</th>
                    <th>来自分流组名</th>
                    <th>来自客服</th>
                    <th>客服ID</th>
                    <th>客服名字</th>
                    <th>访客ID</th>
                    <th>访客17ID</th>
                    <th>访客手机号</th>
                    <th>来源类型</th>
                    <th>客服会话条数</th>
                    <th>访客会话条数</th>
                    <th>平均会话响应时间(S)</th>
                </tr>
                <#if records??>
                    <#list records as content>
                        <tr>
                            <td>${content.id!}</td>
                            <td>${content.startTime?number?number_to_datetime}</td>
                            <td><#if content.endTime??>${content.endTime?number?number_to_datetime}</#if></td>
                            <td>${content.category!}</td>
                            <td>${content.evaluation!}</td>
                            <td>${content.evaluationRemark!}</td>
                            <td>${content.closeReason!}</td>
                            <td>${content.fromGroup!}</td>
                            <td>${content.fromStaff!}</td>
                            <td>${content.staffId!}</td>
                            <td>${content.staffName!}</td>
                            <td>${content.userId!}</td>
                            <td>${content.foreignId!}</td>
                            <td>
                                 <#if content.callOutNum?has_content>
                                    <button id = "queryMobile${content.mobile!}" class="btn btn-primary">查看</button>
                                 </#if>
                            </td>
                            <td>${content.fromType!}</td>
                            <td>${content.staffMessageNum!}</td>
                            <td>${content.userMessageNum!}</td>
                            <td>${content.replayAvgTime!}</td>
                        </tr>
                    </#list>
                </#if>
            </table>
            </#if>
            <!-- 通话数据 -->
            <#if tab?? && tab == 2>

            <form id="ad-query" class="form-horizontal" method="get"
                  action="">
                <div style="margin-bottom: 10px">
                                <span style="white-space: nowrap;">
                                    <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
                                    <input type="hidden" id="tab" name="tab" value="2"/>
                                    <input type="hidden" id="isExport" name="isExport"/>
                                    ID： <input type="text" id="sessionId" name="sessionId" value="${sessionId!""}"/>
                                    通话开始时间
                                    <span>
                                        <input id="sessionTimeStart" name="sessionTimeStart" style="width: 12em;" <#if sessionTimeStart?has_content>value="${(sessionTimeStart?html)!''}" </#if> data-role="date" data-inline="true" type="text"/>
                                        ---
                                        <input id="sessionTimeEnd" name="sessionTimeEnd" style="width: 12em;" <#if sessionTimeEnd?has_content>value="${(sessionTimeEnd?html)!''}" </#if> data-role="date" data-inline="true" type="text"/>
                                    </span>
                                    被叫号码
                                    <input type="text" id="callOutNum" name="callOutNum" value="${callOutNum!""}"/>
                                    呼入号码
                                    <input type="text" id="callInNum" name="callInNum" value="${callInNum!""}"/>
                                    </span>
                                 </span>

                </div>

                <button type="button" class="btn btn-primary" onclick="queryChange()">查询</button>
                &nbsp;
                <button type="button" class="btn" onclick="resetQuery()">重置</button>
                &nbsp;
                <button type="button" class="btn btn-success" onclick="exportQuery()">导出(默认导出昨天的数据)</button>
                <div style="font-size: 14px; margin-top: 10px">
                    <div>
                        <span style="color: red;">电话状态：</span>
                        0-客服未接听，1-接通，2-外呼未接通（占线），
                        3-外呼未接通（无法接通），4-外呼未接通（无
                        人应答），5-访客 IVR 中放弃，6-访客队列中
                        放弃，7-访客排队超时，8-客服不在服务时间，
                        9-无客服在线，10-电话转接成功
                    </div>
                    <div>
                        <span style="color: red;">呼叫方向：</span>
                        1-呼入（客服为被叫），2-呼出（客服为主叫)
                    </div>
                </div>
            </form>
            <table class="table table-bordered">
                <tr>
                    <th>会话ID</th>
                    <th>开始时间</th>
                    <th>接通时间</th>
                    <th>等待时长</th>
                    <th>通话时长</th>
                    <th>呼叫方向</th>
                    <th>被叫号码</th>
                    <th>呼入号码</th>
                    <th>电话状态</th>
                    <th>服务评价</th>
                    <th>录音地址</th>
                    <th>客服ID</th>
                    <th>客服姓名</th>
                    <th>客服坐席号码</th>
                </tr>
                <#if records??>
                    <#list records as content>
                        <tr>
                            <td>${content.sessionId!}</td>
                            <td>${content.startTime?number?number_to_datetime}</td>
                            <td><#if content.connectionStartTime?? && content.connectionStartTime != 0>${(content.connectionStartTime?number)?number_to_datetime}</#if></td>
                            <td>${content.waitingDuration!}</td>
                            <td>${content.callDuration!}</td>
                            <td>${content.direction!}</td>
                            <td>
                                <#if content.callOutNum?has_content>
                                    <button id = "queryMobile${content.callOutNum!}" class="btn btn-primary">查看</button>
                                </#if>
                            </td>
                            <td>
                                <#if content.callInNum?has_content>
                                    <button id = "queryMobile${content.callInNum!}" class="btn btn-primary">查看</button>
                                </#if>
                            </td>
                            <td>${content.status!}</td>
                            <td>${content.evaluation!}</td>
                            <td>${content.recordUrl!}</td>
                            <td>${content.staffId!}</td>
                            <td>${content.staffName!}</td>
                            <td>${content.staffNum!}</td>
                        </tr>
                    </#list>
                </#if>
            </table>

            </#if>
            <#if hasPrev??>
                <ul class="pager">
                    <li><a href="javaScript:;" onclick="pagePost(0)" title="Pre">首页</a></li>
                        <#if hasPrev>
                            <li><a href="javaScript:;" onclick="pagePost(${currentPage-1})" title="Pre">&lt;</a></li>
                        <#else>
                            <li class="disabled"><a href="javaScript:;">&lt;</a></li>
                        </#if>
                    <li class="disabled"><a>第 ${currentPage+ 1} 页</a></li>
                    <li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a></li>
                        <#if hasNext>
                            <li><a href="javaScript:;" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a></li>
                        <#else>
                            <li class="disabled"><a href="javaScript:;">&gt;</a></li>
                        </#if>
                </ul>
            </#if>
        </div>
    </div>
</div>

<script>
    $('[id^="sessionTime"]').datepicker({
        dateFormat: 'yy-mm-dd',  //日期格式，自己设置
        monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate: new Date(),
        numberOfMonths: 1,
        changeMonth: false,
        changeYear: false,
        onSelect: function (selectedDate) {
        }
    });

    $('[id^="queryMobile"]').on("click", function () {
        var that = $(this);
        $.ajax({
            url: '/site/qiyukf/stats/queryMobile.vpage',
            type: 'GET',
            data: {"mobile":that.attr("id").substring("queryMobile".length)},
            success:function (data) {
                if (data.success) {
                    var parent = that.parent();
                    parent.empty();
                    parent.html(data.mobile)
                }
                console.log(data)
            }
        })
    });

    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#ad-query").submit();
    }

    function queryChange() {
        $("#pageNum").val(0);
        $("#ad-query").submit();
    }

    function exportQuery() {
        $("#pageNum").val(0);
        $("#isExport").val(true);
        $("#ad-query").submit();
        $("#isExport").val(false);
    }

    function resetQuery() {
        var tab = $("#tab").val();
        $(':input','#ad-query')
                .not(':button,:submit,:reset')   //将myform表单中input元素type为button、submit、reset排除
                .val('')  //将input元素的value设为空值
                .removeAttr('checked')
                .removeAttr('checked');
        $("#tab").val(tab);
        $("#ad-query").submit();
    }
</script>
</#if>

</@layout_default.page>
