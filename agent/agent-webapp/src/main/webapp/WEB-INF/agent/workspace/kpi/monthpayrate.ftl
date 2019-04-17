<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='月活数据' page_num=1>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-search"></i> 月活数据查询</h2>
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
            <form id="query_form"  action="monthpayrate.vpage" method="post" class="form-horizontal">
                <fieldset>
                    <table>
                        <tr>
                            <td width="180px">区域&nbsp;<input type="text" class="input-small" id="regionNames" readonly="true" value="${regionNames!}" style="cursor: pointer">
                                <input type="hidden" name="regionIds" id="regionIds" value="${regionIds!}"/></td>
                            <td><button type="submit" id="search_btn" class="btn btn-success">查询</button></td>
                        </tr>
                    </table>
                </fieldset>
            </form>
            <br/>
            <div id="topRegisterDataTab" class="dataTables_wrapper">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 100px;" rowspan="2">地区</th>
                        <th class="sorting" style="width: 100px;" rowspan="2">产品</th>
                        <#list monthList as month>
                            <th class="sorting" style="width: 240px;" colspan="3">${month}</th>
                        </#list>
                    </tr>
                    <tr>
                        <#list monthList as month>
                            <th class="sorting" style="width: 80px;" title="按月排重后的付费人数">付费人数<i class="icon-question-sign"></i></th>
                            <th class="sorting" style="width: 80px;">月活人数</th>
                            <th class="sorting" style="width: 80px;">付费率</th>
                        </#list>
                    </tr>
                    </thead>
                    <tbody>
                        <#list data?keys as regionCode>
                            <tr class="odd">
                                <td class="center  sorting_1" rowspan="7">
                                    <#if regionCode?substring(4,6) == '00'>
                                        <a href="monthpayrate.vpage?region=${regionCode}">
                                            ${data[regionCode].region_name!}
                                        </a>
                                    <#else>
                                        ${data[regionCode].region_name!}
                                    </#if>
                                </td>
                                <td>阿分提</td>
                                <#list monthList as month>
                                    <#assign regionKey = regionCode + "_" + month />
                                    <td class="sorting" style="width: 80px;">${(data[regionCode][regionKey].afentiexam_pay_user_num)!0}</td>
                                    <td class="sorting" style="width: 80px;">${(data[regionCode][regionKey].active_user_num)!0}</td>
                                    <#if (data[regionCode][regionKey])?? && data[regionCode][regionKey].active_user_num gt 0 && data[regionCode][regionKey].afentiexam_pay_user_num??>
                                        <#assign payRate = (data[regionCode][regionKey].afentiexam_pay_user_num / data[regionCode][regionKey].active_user_num) * 100 />
                                        <td class="sorting" style="width: 80px;"><span class="label label-important">${payRate?string("###0.00")}%</span></td>
                                    <#else>
                                        <td class="sorting" style="width: 80px;"><span class="label label-important">0%</span></td>
                                    </#if>
                               </#list>
                            </tr>
                            <tr class="odd">
                                <td>沃克大冒险</td>
                                <#list monthList as month>
                                    <#assign regionKey = regionCode + "_" + month />
                                    <td class="sorting" style="width: 80px;">${(data[regionCode][regionKey].walker_pay_user_num)!0}</td>
                                    <td class="sorting" style="width: 80px;">${(data[regionCode][regionKey].active_user_num)!0}</td>
                                    <#if (data[regionCode][regionKey])?? && data[regionCode][regionKey].active_user_num gt 0 && data[regionCode][regionKey].walker_pay_user_num??>
                                        <#assign payRate = (data[regionCode][regionKey].walker_pay_user_num / data[regionCode][regionKey].active_user_num) * 100 />
                                        <td class="sorting" style="width: 80px;"><span class="label label-important">${payRate?string("###0.00")}%</span></td>
                                    <#else>
                                        <td class="sorting" style="width: 80px;"><span class="label label-important">0%</span></td>
                                    </#if>
                                </#list>
                            </tr>
                            <tr class="odd">
                                <td>Picaro</td>
                                <#list monthList as month>
                                    <#assign regionKey = regionCode + "_" + month />
                                    <td class="sorting" style="width: 80px;">${(data[regionCode][regionKey].kaplanpicaro_pay_user_num)!0}</td>
                                    <td class="sorting" style="width: 80px;">${(data[regionCode][regionKey].active_user_num)!0}</td>
                                    <#if (data[regionCode][regionKey])?? && data[regionCode][regionKey].active_user_num gt 0 && data[regionCode][regionKey].kaplanpicaro_pay_user_num??>
                                        <#assign payRate = (data[regionCode][regionKey].kaplanpicaro_pay_user_num / data[regionCode][regionKey].active_user_num) * 100 />
                                        <td class="sorting" style="width: 80px;"><span class="label label-important">${payRate?string("###0.00")}%</span></td>
                                    <#else>
                                        <td class="sorting" style="width: 80px;"><span class="label label-important">0%</span></td>
                                    </#if>
                                </#list>
                            </tr>
                            <tr class="odd">
                                <td>走遍美国</td>
                                <#list monthList as month>
                                    <#assign regionKey = regionCode + "_" + month />
                                    <td class="sorting" style="width: 80px;">${(data[regionCode][regionKey].travelamerica_pay_user_num)!0}</td>
                                    <td class="sorting" style="width: 80px;">${(data[regionCode][regionKey].active_user_num)!0}</td>
                                    <#if (data[regionCode][regionKey])?? && data[regionCode][regionKey].active_user_num gt 0 && data[regionCode][regionKey].travelamerica_pay_user_num??>
                                        <#assign payRate = (data[regionCode][regionKey].travelamerica_pay_user_num / data[regionCode][regionKey].active_user_num) * 100 />
                                        <td class="sorting" style="width: 80px;"><span class="label label-important">${payRate?string("###0.00")}%</span></td>
                                    <#else>
                                        <td class="sorting" style="width: 80px;"><span class="label label-important">0%</span></td>
                                    </#if>
                                </#list>
                            </tr>
                            <tr class="odd">
                                <td>爱儿优</td>
                                <#list monthList as month>
                                    <#assign regionKey = regionCode + "_" + month />
                                    <td class="sorting" style="width: 80px;">${(data[regionCode][regionKey].iandyou100_pay_user_num)!0}</td>
                                    <td class="sorting" style="width: 80px;">${(data[regionCode][regionKey].active_user_num)!0}</td>
                                    <#if (data[regionCode][regionKey])?? && data[regionCode][regionKey].active_user_num gt 0 && data[regionCode][regionKey].iandyou100_pay_user_num??>
                                        <#assign payRate = (data[regionCode][regionKey].iandyou100_pay_user_num / data[regionCode][regionKey].active_user_num) * 100 />
                                        <td class="sorting" style="width: 80px;"><span class="label label-important">${payRate?string("###0.00")}%</span></td>
                                    <#else>
                                        <td class="sorting" style="width: 80px;"><span class="label label-important">0%</span></td>
                                    </#if>
                                </#list>
                            </tr>
                            <tr class="odd">
                            <td>进击的三国</td>
                                <#list monthList as month>
                                    <#assign regionKey = regionCode + "_" + month />
                                    <td class="sorting" style="width: 80px;">${(data[regionCode][regionKey].sanguodmz_pay_user_num)!0}</td>
                                    <td class="sorting" style="width: 80px;">${(data[regionCode][regionKey].active_user_num)!0}</td>
                                    <#if (data[regionCode][regionKey])?? && data[regionCode][regionKey].active_user_num gt 0 && data[regionCode][regionKey].sanguodmz_pay_user_num??>
                                        <#assign payRate = (data[regionCode][regionKey].sanguodmz_pay_user_num / data[regionCode][regionKey].active_user_num) * 100 />
                                        <td class="sorting" style="width: 80px;"><span class="label label-important">${payRate?string("###0.00")}%</span></td>
                                    <#else>
                                        <td class="sorting" style="width: 80px;"><span class="label label-important">0%</span></td>
                                    </#if>
                                </#list>
                            </tr>
                            <tr class="odd">
                            <td>宠物大乱斗</td>
                            <#list monthList as month>
                                <#assign regionKey = regionCode + "_" + month />
                                <td class="sorting" style="width: 80px;">${(data[regionCode][regionKey].petswar_pay_user_num)!0}</td>
                                <td class="sorting" style="width: 80px;">${(data[regionCode][regionKey].active_user_num)!0}</td>
                                <#if (data[regionCode][regionKey])?? && data[regionCode][regionKey].active_user_num gt 0 && data[regionCode][regionKey].petswar_pay_user_num??>
                                    <#assign payRate = (data[regionCode][regionKey].petswar_pay_user_num / data[regionCode][regionKey].active_user_num) * 100 />
                                    <td class="sorting" style="width: 80px;"><span class="label label-important">${payRate?string("###0.00")}%</span></td>
                                <#else>
                                    <td class="sorting" style="width: 80px;"><span class="label label-important">0%</span></td>
                                </#if>
                            </#list>
                        </tr>
                        </#list>
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
