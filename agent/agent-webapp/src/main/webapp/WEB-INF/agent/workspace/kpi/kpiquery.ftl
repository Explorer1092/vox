<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='市场数据查询' page_num=1>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-search"></i> 市场数据查询(注意:2016年3月份以后的数据查询还在开发中，暂不支持)</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a class="btn btn-round" href="javascript:window.history.back();">
                    <i class="icon-chevron-left"></i>
                </a>&nbsp;
            </div>
        </div>

        <div class="box-content">
            <form id="query_form"  action="kpiquery.vpage" method="get" class="form-horizontal">
                <fieldset>
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
                        <label class="control-label" for="selectError3">区域</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="regionNames" readonly="true" value="${regionNames!}" style="cursor: pointer">
                            <input type="hidden" name="regionIds" id="regionIds" value="${regionIds!}"/>
                        </div>
                    </div>
                    <div class="control-group span3">
                        <div class="controls">
                            <button type="submit" id="search_btn" class="btn btn-success">查询</button>
                        </div>
                    </div>
                </fieldset>
            </form>

            <div id="topRegisterDataTab" class="dataTables_wrapper">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 180px;">地区</th>
                        <th class="sorting" style="width: 100px;" title="只要系统中建立了账号，就算做新增注册老师。">老师新增注册 <i class="icon-question-sign"></i></th>
                        <th class="sorting" style="width: 100px;" title="1、填写真实姓名；2、绑定手机；3、8名同学完成3次作业或测验。4、3名同学绑定手机。">老师新增认证<i class="icon-question-sign"></i></th>
                        <th class="sorting" style="width: 90px;">老师使用</th>
                        <th class="sorting" style="width: 100px;" title="注册班级内必须至少有一位学生做过一次作业或测验，则此班所有学生都成为新增注册学生。">学生新增注册<i class="icon-question-sign"></i></th>
                        <th class="sorting" style="width: 100px;" title="所在班级老师完成认证，如果某学生累计完成3次作业（与认证老师科目一致），则此学生成为新增认证学生。">学生新增认证<i class="icon-question-sign"></i></th>
                        <th class="sorting" style="width: 100px;" title="学生新增认证数量里面已经包含学生新增认证(1-2年级)数量">学生新增认证(1-2年级)<i class="icon-question-sign"></i></th>
                        <th class="sorting" style="width: 90px;">学生使用</th>
                        <#if requestContext.getCurrentUser().isCountryManager()>
                            <th class="sorting" style="width: 90px;">在线付费金额</th>
                        </#if>
                    </tr>
                    </thead>
                    <tbody>
                        <#if kpiData??>
                            <#list kpiData?keys as key>
                            <tr class="odd">
                                <td class="center  sorting_1">
                                    <#if kpiData[key].type == "region" ><a href="kpiquery.vpage?startDate=${startDate}&endDate=${endDate}&region=${kpiData[key].regionCode}">${kpiData[key].regionName}</a>
                                    <#else>
                                        ${kpiData[key].regionName}
                                    </#if>
                                </td>
                                <td class="center  sorting_1">
                                    <#if kpiData[key].teacherRegister gt 0>
                                        <#if kpiData[key].type == "region">
                                            <a id="view_reg_teachers"  href="viewregteachers.vpage?startDate=${startDate?replace("-", "")}&endDate=${endDate?replace("-", "")}&region=${kpiData[key].regionCode}" target="_blank">${kpiData[key].teacherRegister!}</a>
                                        <#elseif kpiData[key].type == "school">
                                            <a id="view_reg_teachers"  href="viewregteachers.vpage?startDate=${startDate?replace("-", "")}&endDate=${endDate?replace("-", "")}&school=${kpiData[key].regionCode}" target="_blank">${kpiData[key].teacherRegister!}</a>
                                        <#else>
                                            ${kpiData[key].teacherRegister!}
                                        </#if>
                                    </#if>
                                </td>
                                <td class="center  sorting_1">
                                    <#if kpiData[key].teacherAuth?has_content && kpiData[key].teacherAuth gt 0>
                                        <#if kpiData[key].type == "region">
                                            <a id="view_auth_teachers"  href="viewauthteachers.vpage?startDate=${startDate?replace("-", "")}&endDate=${endDate?replace("-", "")}&region=${kpiData[key].regionCode}" target="_blank">${kpiData[key].teacherAuth!}</a>
                                        <#elseif kpiData[key].type == "school">
                                            <a id="view_auth_teachers"  href="viewauthteachers.vpage?startDate=${startDate?replace("-", "")}&endDate=${endDate?replace("-", "")}&school=${kpiData[key].regionCode}" target="_blank">${kpiData[key].teacherAuth!}</a>
                                        <#else>
                                        ${kpiData[key].teacherAuth!}
                                        </#if>
                                    </#if>
                                </td>
                                <td class="center  sorting_1">
                                    <#if kpiData[key].teacherActive gt 0>
                                        ${kpiData[key].teacherActive!}
                                    </#if>
                                </td>
                                <td class="center  sorting_1">
                                    <#if kpiData[key].studentRegister gt 0>
                                        ${kpiData[key].studentRegister!}
                                    </#if>
                                </td>
                                <td class="center  sorting_1">
                                    <#if kpiData[key].studentAuth gt 0>
                                        ${kpiData[key].studentAuth!}
                                    </#if>
                                </td>
                                <td class="center  sorting_1">
                                    <#if kpiData[key].studentAuthLv gt 0>
                                        ${kpiData[key].studentAuthLv!}
                                    </#if>
                                </td>
                                <td class="center  sorting_1">
                                    <#if kpiData[key].studentActive gt 0>
                                        ${kpiData[key].studentActive!}
                                    </#if>
                                </td>
                                <#if requestContext.getCurrentUser().isCountryManager()>
                                    <td class="center  sorting_1">
                                        <#if kpiData[key].onlinePay gt 0>
                                        ${kpiData[key].onlinePay!}
                                     </#if>
                                    </td>
                                </#if>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<div id="region_select_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">选择查询区域</h4>
            </div>
            <form class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <div id="regiontree" class="controls" style="width: 280px;height: 400px"></div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="region_select_btn" type="button" class="btn btn-primary">确定</button>
                </div>
            </form>
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

        $('#regionNames').on('click',function(){
            var $regiontree = $("#regiontree");
            $regiontree.fancytree('destroy');

            $regiontree.fancytree({
                source: {
                    url: "/common/region/loadregion.vpage",
                    cache:false
                },
                checkbox: true,
                selectMode: 2
            });

            $('#region_select_dialog').modal('show');
        });

        $('#region_select_btn').on('click',function(){
            var regionTree = $("#regiontree").fancytree("getTree");
            var regionNodes = regionTree.getSelectedNodes();
            if(regionNodes == null || regionNodes == "undefined") {
                $('#regionIds').val('');
                $('#regionNames').val('');
                return;
            }

            var selectRegionNameList = new Array();
            var selectRegionIdList = new Array();
            $.map(regionNodes, function(node){
                selectRegionIdList.push(node.key);
                selectRegionNameList.push(node.title);
            });

            $('#regionIds').val(selectRegionIdList.join(','));
            $('#regionNames').val(selectRegionNameList.join(','));

            $('#region_select_dialog').modal('hide');

        });

        $('#search_btn').on('click',function(){
            var startDate = $('#startDate').val();
            var endDate = $('#endDate').val();
            var regionIds = $('#regionIds').val();

            if (startDate == '') {
                alert("请选择开始日期!");
                return false;
            }

            if (endDate == '') {
                alert("请选择结束日期!");
                return false;
            }

            if (regionIds == '') {
                alert("请选择要查询的区域!");
                return false;
            }
            return true;
        });

    });
</script>
</@layout_default.page>
