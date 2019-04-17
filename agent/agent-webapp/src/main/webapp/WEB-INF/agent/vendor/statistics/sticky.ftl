<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='应用留存粘性统计' page_num=7>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-search"></i> 应用留存粘性统计</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                &nbsp;
            </div>
        </div>

        <div class="box-content">
            <form id="query_form"  action="sticky.vpage" method="get" class="form-horizontal">
                <fieldset>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">开始日期</label>
                        <div class="controls">
                            <input type="text" class="input-medium" id="startDate" name="startDate" value="${startDate}">
                        </div>
                    </div>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">结束日期</label>
                        <div class="controls">
                            <input type="text" class="input-medium" id="endDate" name="endDate" value="${endDate}">
                        </div>
                    </div>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">应用名称</label>
                        <div class="controls">
                            <select id="appKey" name="appKey">
                                <option value="" <#if appKey == "">selected="selected" </#if>>请选择</option>
                                <#if appObjs??>
                                    <#list appObjs as app>
                                        <option value="${app.appKey!}" <#if app.appKey == appKey>selected="selected" </#if>>${app.appName!}</option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>
                    <div class="control-group span3">
                        <div class="controls">
                            <button id="btnQuery" type="submit" class="btn btn-success">查询</button>
                        </div>
                    </div>
                </fieldset>
            </form>

            <div id="topRegisterDataTab" class="dataTables_wrapper">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 50px;">日期</th>
                        <th class="sorting" style="width: 100px;">用户数</th>
                        <th class="sorting" style="width: 100px;">1天后</th>
                        <th class="sorting" style="width: 100px;">2天后</th>
                        <th class="sorting" style="width: 100px;">3天后</th>
                        <th class="sorting" style="width: 100px;">4天后</th>
                        <th class="sorting" style="width: 100px;">5天后</th>
                        <th class="sorting" style="width: 100px;">6天后</th>
                        <th class="sorting" style="width: 100px;">7天后</th>
                        <th class="sorting" style="width: 100px;">14天后</th>
                        <th class="sorting" style="width: 100px;">30天后</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if stickyList??>
                            <#list stickyList as day>
                            <tr class="odd">
                                <td class="center  sorting_1">${day.day}</td>
                                <td class="center  sorting_1">${day.count!}</td>
                                <td class="center  sorting_1">${day.sticky_1!}</td>
                                <td class="center  sorting_1">${day.sticky_2!}</td>
                                <td class="center  sorting_1">${day.sticky_3!}</td>
                                <td class="center  sorting_1">${day.sticky_4!}</td>
                                <td class="center  sorting_1">${day.sticky_5!}</td>
                                <td class="center  sorting_1">${day.sticky_6!}</td>
                                <td class="center  sorting_1">${day.sticky_7!}</td>
                                <td class="center  sorting_1">${day.sticky_14!}</td>
                                <td class="center  sorting_1">${day.sticky_30!}</td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){

        $("#startDate").datepicker({
            dateFormat      : 'yymmdd',  //日期格式，自己设置
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
            dateFormat      : 'yymmdd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });
    });

</script>
</@layout_default.page>
