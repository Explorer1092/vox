<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='订单管理' page_num=14>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-search"></i> 订单管理</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                &nbsp;
            </div>
        </div>

        <div class="box-content">
            <form class="form-horizontal">
                <fieldset>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">开始日期</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="startDate" name="startDate">
                        </div>
                    </div>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">结束日期</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="endDate" name="endDate">
                        </div>
                    </div>
                    <#if !requestContext.getCurrentUser().isBusinessDeveloper()>
                        <div class="control-group span3">
                            <label class="control-label" for="selectError3">申请人</label>
                            <div class="controls">
                                <input type="text" class="input-small" id="creator" name="creator">
                            </div>
                        </div>
                    </#if>
                </fieldset>
                <fieldset>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">订单编号</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="orderId" name="orderId">
                        </div>
                    </div>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">审核状态</label>
                        <div class="controls">
                            <select id="applyStatus" class="input-small">
                                <option value=""></option>
                                <#list applyStatus as apply>
                                    <option value="${apply}">${apply.desc}</option>
                                </#list>
                            </select>
                        </div>
                    </div>
                    <div class="control-group span3">
                        <div class="controls">
                            <button type="button" id="search_btn" class="btn btn-success">查询</button>
                            <button type="button" id="download_btn" class="btn btn-success" onclick="exportClick();">导出</button>
                        </div>
                    </div>
                </fieldset>
            </form>
            <div id="topRegisterDataTab" class="dataTables_wrapper">
                <table id="datatable" class="table table-striped table-bordered bootstrap-datatable datatable dataTable">
                    <thead id="tableHead">
                    <tr>
                        <th class="sorting" style="width: 50px;">申请日期</th>
                        <th class="sorting" style="width: 50px;">订单编号</th>
                        <th class="sorting" style="width: 50px;">部门</th>
                        <th class="sorting" style="width: 50px;">申请人</th>
                        <th class="sorting" style="width: 50px;">购买商品</th>
                        <th class="sorting" style="width: 50px;">订单金额</th>
                        <th class="sorting" style="width: 50px;">收货信息</th>
                        <th class="sorting" style="width: 50px;">支付方式</th>
                        <th class="sorting" style="width: 50px;">备注</th>
                        <th class="sorting" style="width: 50px;">状态</th>
                        <th class="sorting" style="width: 50px;">操作</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>
<script type="text/html" id="applyList">


    <%if(res.applyMapList.length>0){%>
    <%for(var i=0;i < res.applyMapList.length;i++){%>
    <%var orderInfo = res.applyMapList[i]%>
    <tr class="odd tbody01">
        <td class="center  sorting_1"><%=orderInfo.orderTime%></td>
        <td class="center  sorting_1"><%=orderInfo.id%></td>
        <td class="center  sorting_1"><%=orderInfo.groupName%></td>
        <td class="center  sorting_1"><%=orderInfo.creatorName%></td>
        <td class="center  sorting_1"><%=orderInfo.orderProducts.join("\r\n")%></td>
        <td class="center  sorting_1"><%=orderInfo.orderAmount%></td>
        <td class="center  sorting_1">
            <%=orderInfo.consigneeInfo.join("\r\n")%>
        </td>
        <td class="center  sorting_1">
            <%if(orderInfo.paymentMode == 1){%>
                物料费用
            <%}else if(orderInfo.paymentMode == 2){%>
                城市支持费用
            <%}else if(orderInfo.paymentMode == 3){%>
                自付
            <%}%>
        </td>
        <td class="center  sorting_1"><%=orderInfo.orderNotes%></td>
        <td class="center  sorting_1"><%=orderInfo.applyStatus%></td>
    </tr>
    <%}%>
    <%}%>
</script>
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
            changeYear: false
        });

        $("#endDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false
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
            var dataObj = {};
            dataObj.startDate   = $('#startDate').val();
            dataObj.endDate     = $('#endDate').val();
            dataObj.creator     = $('#creator').val();
            dataObj.orderId     = $('#orderId').val();
            dataObj.applyStatus = $('#applyStatus option:selected').val();
            var index = layer.load(1, {
                shade: [0.1,'#fff'] //0.1透明度的白色背景
            });
            $.get("list.vpage",dataObj,function (res) {
                if(res.success){
                    var dataTableList = [];
                    for(var i=0;i < res.applyMapList.length;i++){
                        res.applyMapList[i].orderTime = new Date(res.applyMapList[i].orderTime).Format("yyyy-MM-dd");
                        if(res.applyMapList[i].paymentMode == 1){
                            res.applyMapList[i].paymentMode = '物料费用';
                        }else if(res.applyMapList[i].paymentMode == 2){
                            if(res.applyMapList[i].costMonthStr){
                                res.applyMapList[i].paymentMode = '城市支持费用' +'('+ (res.applyMapList[i].costMonthStr || '') + ')' ;
                            }else{
                                res.applyMapList[i].paymentMode = '城市支持费用';
                            }
                        }else if(res.applyMapList[i].paymentMode == 3){
                            res.applyMapList[i].paymentMode = '自付';
                        }
                        var item = res.applyMapList[i];
                        var operator = '<td class="center  sorting_1"><a href="/apply/view/apply_datail.vpage?applyType='+item.applyType+'&applyId='+item.id+'">'+'查看详情'+'</a></td>';
                        var arr = [item.orderTime, item.id, item.groupName, item.creatorName, item.orderProducts.join("\r\n"), item.orderAmount, item.consigneeInfo.join("\r\n"), item.paymentMode, item.orderNotes,item.applyStatus, operator];
                        dataTableList.push(arr);
                    }
                    $("#applyListInfo").html(template("applyList",{res:res}));
                    var reloadDataTable = function () {
                        var table = $('#datatable').dataTable();
                        table.fnClearTable();
                        table.fnAddData(dataTableList); //添加添加新数据
                    };
                    console.log(dataTableList);
                    setTimeout(reloadDataTable(),0);
                }else{
                    alert(res.info)
                }
                layer.close(index);
            });
        });
    });

    function exportClick() {
        var dataObj = {};
        dataObj.startDate   = $('#startDate').val();
        dataObj.endDate     = $('#endDate').val();
        dataObj.creator     = $('#creator').val();
        dataObj.orderId     = $('#orderId').val();
        dataObj.applyStatus = $('#applyStatus option:selected').val();
        window.location.href = "exportOrder.vpage?"+parseParam(dataObj);
    }

    var parseParam=function(param, key){
        var paramStr="";
        if(param instanceof String||param instanceof Number||param instanceof Boolean){
            paramStr+="&"+key+"="+encodeURIComponent(param);
        }else{
            $.each(param,function(i){
                var k=key==null?i:key+(param instanceof Array?"["+i+"]":"."+i);
                paramStr+='&'+parseParam(this, k);
            });
        }
        return paramStr.substr(1);
    };
</script>
</@layout_default.page>
