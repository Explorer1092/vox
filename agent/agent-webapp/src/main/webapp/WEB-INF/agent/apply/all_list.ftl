<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='全部申请' page_num=3>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-search"></i> 全部申请</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                &nbsp;
            </div>
        </div>

        <div class="box-content">
            <form id="query_form"  action="all_list.vpage" method="get" class="form-horizontal">
                <fieldset>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">申请类型</label>
                        <div class="controls">
                            <select id="applyType" name="applyType">
                                <option value="" <#if !selectType?has_content>selected="selected" </#if>>请选择</option>
                                <#if applyTypeList??>
                                    <#list applyTypeList as item>
                                        <option value="${item!}" <#if selectType?has_content && selectType == item>selected="selected" </#if>>${item.desc!}</option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">开始日期</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="startDate" name="startDate" value="${startDate!}">
                        </div>
                    </div>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">结束日期</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="endDate" name="endDate" value="${endDate!}">
                        </div>
                    </div>
                    <div class="control-group span3">
                        <div class="controls">
                            <button type="submit" id="search_btn" class="btn btn-success">查询</button>
                            <button type="button" id="download_btn" class="btn btn-success" onclick="exportApplyExcel();">下载</button>
                        </div>
                    </div>
                </fieldset>
            </form>

            <div id="topRegisterDataTab" class="dataTables_wrapper">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable">
                    <thead id="tableHead">
                    <#if selectType?has_content && selectType == "AGENT_MODIFY_DICT_SCHOOL">
                    <tr>
                        <th class="sorting" style="width: 50px;">申请日期</th>
                        <th class="sorting" style="width: 50px;">大区</th>
                        <th class="sorting" style="width: 50px;">部门</th>
                        <th class="sorting" style="width: 50px;">申请人</th>
                        <th class="sorting" style="width: 50px;">操作类型</th>
                        <th class="sorting" style="width: 50px;">学校ID</th>
                        <th class="sorting" style="width: 50px;">学校名称</th>
                        <th class="sorting" style="width: 50px;">阶段</th>
                        <th class="sorting" style="width: 50px;">调整原因</th>
                        <th class="sorting" style="width: 50px;">状态</th>
                        <th class="sorting" style="width: 50px;">操作</th>
                    </tr>
                    </#if>
                    <#if selectType?has_content && selectType == "AGENT_MATERIAL_APPLY">
                    <tr>
                        <th class="sorting" style="width: 50px;">申请日期</th>
                        <th class="sorting" style="width: 50px;">大区</th>
                        <th class="sorting" style="width: 50px;">部门</th>
                        <th class="sorting" style="width: 50px;">申请人</th>
                        <th class="sorting" style="width: 50px;">购买商品</th>
                        <th class="sorting" style="width: 50px;">订单金额</th>
                        <th class="sorting" style="width: 50px;">备注记录</th>
                        <th class="sorting" style="width: 50px;">收获信息</th>
                        <th class="sorting" style="width: 50px;">支付方式</th>
                        <th class="sorting" style="width: 50px;">状态</th>
                        <th class="sorting" style="width: 50px;">操作</th>
                    </tr>
                    </#if>
                        <#if selectType?has_content && selectType == "AGENT_UNIFIED_EXAM_APPLY">
                        <tr>
                            <th class="sorting" style="width: 50px;">申请日期</th>
                            <th class="sorting" style="width: 50px;">申请人</th>
                            <th class="sorting" style="width: 50px;">分区</th>
                            <th class="sorting" style="width: 50px;">级别</th>
                            <th class="sorting" style="width: 50px;">学科</th>
                            <th class="sorting" style="width: 50px;">年级</th>
                            <th class="sorting" style="width: 50px;">统考名称</th>
                            <th class="sorting" style="width: 50px;">审核状态</th>
                            <th class="sorting" style="width: 50px;">录入状态</th>
                            <th class="sorting" style="width: 50px;">操作</th>
                        </tr>
                        </#if>
                        <#if selectType?has_content && selectType =="AGENT_DATA_REPORT_APPLY">
                        <tr>
                            <th class="sorting" style="width: 50px;">申请日期</th>
                            <th class="sorting" style="width: 50px;">大区</th>
                            <th class="sorting" style="width: 50px;">部门</th>
                            <th class="sorting" style="width: 50px;">申请人</th>
                            <th class="sorting" style="width: 50px;">级别</th>
                            <th class="sorting" style="width: 50px;">申请学科</th>
                            <th class="sorting" style="width: 50px;">时间维度</th>
                            <th class="sorting" style="width: 50px;">状态</th>
                            <th class="sorting" style="width: 50px;">操作</th>
                        </tr>
                        </#if>
                    </thead>
                    <tbody>
                        <#if applyList??>
                            <#if selectType?has_content && selectType == "AGENT_MODIFY_DICT_SCHOOL">
                                <#list applyList?sort_by("createDatetime")?reverse as apply>
                                <tr class="odd tbody01">
                                    <td class="center  sorting_1">${apply.createDatetime?string("yyyy-MM-dd")}</td>
                                    <td class="center  sorting_1">${apply.parentGroupName!}</td>
                                    <td class="center  sorting_1">${apply.groupName!}</td>
                                    <td class="center  sorting_1">${apply.accountName!}</td>
                                    <td class="center  sorting_1">
                                        <#if apply.modifyType??>
                                            <#if apply.modifyType == 1>添加学校
                                            <#elseif apply.modifyType == 2>删除学校
                                            <#elseif apply.modifyType == 3>业务变更
                                            </#if>
                                        </#if>
                                    </td>
                                    <td class="center  sorting_1">${apply.schoolId!}</td>
                                    <td class="center  sorting_1">${apply.schoolName!}</td>
                                    <td class="center  sorting_1">
                                        <#if apply.schoolLevel??>
                                            <#if apply.schoolLevel == 1>小学
                                            <#elseif apply.schoolLevel == 2>初中
                                            <#elseif apply.schoolLevel == 4>高中
                                            <#elseif apply.schoolLevel == 5>学前
                                            </#if>
                                        </#if>
                                    </td>
                                    <td class="center  sorting_1">${apply.comment!}</td>
                                    <td class="center  sorting_1">${apply.applyStatus!}</td>
                                    <td class="center  sorting_1"><a href="/apply/view/apply_datail.vpage?applyType=${apply.applyType!}&applyId=${apply.id!}">查看详情</a></td>
                                </tr>
                                </#list>
                            </#if>
                            <#if selectType?has_content && selectType == "AGENT_UNIFIED_EXAM_APPLY">
                                <#list applyList?sort_by("createDatetime")?reverse as apply>
                                <tr class="odd tbody01">
                                    <td class="center  sorting_1">${apply.createDatetime?string("yyyy-MM-dd")}</td>
                                    <td class="center  sorting_1">${apply.accountName!}</td>
                                    <td class="center  sorting_1">${apply.cityGroupName!}</td>
                                    <td class="center  sorting_1">
                                        <#if apply.regionLeve??>
                                            <#if apply.regionLeve == 'city'>市级
                                            <#elseif apply.regionLeve == 'country'>区级
                                            <#elseif apply.regionLeve == 'school'>校级
                                            </#if>
                                        </#if></td>
                                    <td class="center  sorting_1">
                                        <#if apply.subject??>
                                            <#if apply.subject == '101'>小学语文
                                            <#elseif apply.subject == '102'>小学数学
                                            <#elseif apply.subject == '103'>小学英语
                                            <#elseif apply.subject == '201'>初中语文
                                            <#elseif apply.subject == '202'>初中数学
                                            <#elseif apply.subject == '203'>初中英语
                                            <#else>暂无学科
                                            </#if>
                                        </#if></td>
                                    <td class="center  sorting_1">
                                        <#if apply.gradeLevel??>
                                            <#if apply.gradeLevel == 1>一年级
                                            <#elseif apply.gradeLevel == 2>二年级
                                            <#elseif apply.gradeLevel == 3>三年级
                                            <#elseif apply.gradeLevel == 4>四年级
                                            <#elseif apply.gradeLevel == 5>五年级
                                            <#elseif apply.gradeLevel == 6>六年级
                                            <#elseif apply.gradeLevel == 7>七年级
                                            <#elseif apply.gradeLevel == 8>八年级
                                            <#elseif apply.gradeLevel == 9>九年级
                                            </#if>
                                        </#if></td>
                                    <td class="center  sorting_1">${apply.unifiedExamName}</td>
                                    <td class="center  sorting_1">${apply.unifiedExamStatus.desc!}</td>
                                    <td class="center  sorting_1">${apply.entryStatus.desc!}</td>
                                    <td class="center  sorting_1"><a href="/apply/view/apply_datail.vpage?applyType=${apply.applyType!}&applyId=${apply.id!}">查看详情</a></td>
                                </tr>
                                </#list>
                            </#if>
                            <#if selectType?has_content && selectType == "AGENT_MATERIAL_APPLY">
                                <#list applyList?sort_by("createDatetime")?reverse as apply>
                                <tr class="odd tbody01">
                                    <td class="center  sorting_1">${apply.orderTime?string("yyyy-MM-dd")}</td>
                                    <td class="center  sorting_1">${apply.parentGroupName!}</td>
                                    <td class="center  sorting_1">${apply.groupName!}</td>
                                    <td class="center  sorting_1">${apply.accountName!}</td>
                                    <td class="center  sorting_1">
                                        <#list apply.orderProducts as products >
                                        ${products!""}<br>
                                        </#list>
                                    </td>
                                    <td class="center  sorting_1">${apply.orderAmount!}</td>
                                    <td class="center  sorting_1">${apply.orderNotes!}</td>
                                    <td class="center  sorting_1">
                                        <#list apply.consigneeInfo as info >
                                        ${info!""}<br>
                                        </#list>
                                    </td>
                                    <td class="center  sorting_1">
                                        <#if apply.paymentMode??>
                                            <#if apply.paymentMode == 1>物料费用
                                            <#elseif apply.paymentMode == 2>城市支持费用
                                            <#elseif apply.paymentMode == 3>自付
                                            </#if>
                                        </#if></td>
                                    <td class="center  sorting_1"><#if apply.applyStatus??>${apply.applyStatus!''}</#if></td>
                                    <td class="center  sorting_1"><a href="/apply/view/apply_datail.vpage?applyType=${apply.applyType!}&applyId=${apply.id!}">查看详情</a></td>
                                </tr>
                                </#list>
                            </#if>
                            <#if selectType?has_content && selectType =="AGENT_DATA_REPORT_APPLY">
                                <#list applyList?sort_by("createDatetime")?reverse as apply>
                                <tr class="odd tbody01">
                                    <td class="center  sorting_1">${apply.createDatetime?string("yyyy-MM-dd")}</td>
                                    <td class="center  sorting_1">${apply.parentGroupName!}</td>
                                    <td class="center  sorting_1">${apply.groupName!}</td>
                                    <td class="center  sorting_1">${apply.accountName!}</td>
                                    <td class="center  sorting_1">
                                        <#if apply.reportLevel??>
                                            <#if apply.reportLevel ==1> 市级
                                            <#elseif apply.reportLevel ==2>区级
                                            <#elseif apply.reportLevel ==3>校级
                                            </#if>
                                        </#if>
                                    </td>
                                    <td class="center  sorting_1">
                                        <#if apply.subject??>
                                            <#if apply.subject == 1> 小学英语
                                            <#elseif apply.subject == 2> 小学数学
                                            </#if>
                                        </#if>
                                    </td>
                                    <td class="center  sorting_1">${apply.timeDimensionality!""}</td>
                                    <td class="center  sorting_1">${apply.applyStatus!}</td>
                                    <td class="center  sorting_1"><a
                                            href="/apply/view/apply_datail.vpage?applyType=${apply.applyType!}&applyId=${apply.id!}">查看详情</a>
                                    </td>
                                </tr>
                                </#list>
                            </#if>
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

        $('#search_btn').on('click',function(){
            var startDate = $('#startDate').val();
            var endDate = $('#endDate').val();

            if (startDate == '') {
                alert("请选择开始日期!");
                return false;
            }

            if (endDate == '') {
                alert("请选择结束日期!");
                return false;
            }

            if (startDate > endDate) {
                alert("开始时间不能大于结束时间!");
                return false;
            }

            return true;
        });
    });

    function exportApplyExcel(){
        var applyType = $('#applyType').val()
        var startDate = $('#startDate').val();
        var endDate = $('#endDate').val();

        if (startDate == '') {
            alert("请选择开始日期!");
            return;
        }

        if (endDate == '') {
            alert("请选择结束日期!");
            return;
        }

        if (startDate > endDate) {
            alert("开始时间不能大于结束时间!");
            return;
        }

        window.location.href = "download.vpage?applyType=" + applyType + "&startDate=" + startDate + "&endDate=" + endDate;
    }
</script>
</@layout_default.page>
