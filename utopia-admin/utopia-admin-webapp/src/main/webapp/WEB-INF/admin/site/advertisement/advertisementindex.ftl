<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="Advertisement Management" page_num=4>
<div id="main_container" class="span9">
    <legend>
        <a href="advertiserindex.vpage">广告主管理</a>
        &nbsp;&nbsp;&nbsp;&nbsp;
        广告管理
        <a id="add_advertiser_btn" href="addadvertisement.vpage" type="button" class="btn btn-info" style="float: right">增加</a>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="frm" class="form-horizontal" method="post" action="advertisementindex.vpage" >
                    广告ID：<input type="text" class="input-xlarge" placeholder="广告ID" name="id" value="${id!}">
                    &nbsp;&nbsp;&nbsp;&nbsp;
                    广告名称：<input type="text" class="input-xlarge" placeholder="广告名称" name="adName" value="${adName!}">
                    &nbsp;&nbsp;&nbsp;&nbsp;
                    机构名称：<input type="text" class="input-xlarge" placeholder="机构名称" name="advertiserName" value="${advertiserName!}">
                    <br>
                    <br>
                    广告形式：
                    <select class="input-small" name="adCooperationType">
                        <option value="" selected>任意</option>
                        <#if cooperationTypes??>
                            <#list cooperationTypes as cooperationType>
                                <option value="${cooperationType}" <#if adCooperationType == cooperationType>selected</#if>>${cooperationType}</option>
                            </#list>
                        </#if>
                    </select>
                    创建时间：
                    <input id="startDate" type="text" class="input-small" placeholder="开始时间" name="startDate" value="${startDate!}">~
                    <input id="endDate" type="text" class="input-small" placeholder="结束时间" name="endDate" value="${endDate!}">
                    &nbsp;&nbsp;&nbsp;&nbsp;
                    <button type="submit" class="btn btn-primary">查询</button>

                </form>
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>广告ID</td>
                        <td>广告名称</td>
                        <td width="160px">广告说明</td>
                        <td>机构名称</td>
                        <td>广告类型</td>
                        <td>广告合作形式</td>
                        <td>人数限制</td>
                        <td>预算</td>
                        <td>状态</td>
                        <td>创建日期</td>
                        <td>操作</td>
                        <td>城市校区设置</td>
                        <td>特殊键（可空）</td>
                        <td>上传物料</td>
                    </tr>
                    <#if advertisementList?? >
                        <#list advertisementList as ad >
                            <tr>
                                <td>${ad.id!}</td>
                                <td>${ad.name!}</td>
                                <td>${ad.description!}</td>
                                <td>${ad.advertiserName!}</td>
                                <td>${ad.type!}</td>
                                <td>${ad.cooperationType!}</td>
                                <td>${ad.personCountLimit!}</td>
                                <td>${ad.budget!}</td>
                                <td id="status_${ad.id!}">${ad.status!}</td>
                                <td>${ad.createDatetime?string('yyyy-MM-dd HH:mm:ss')!}</td>
                                <td>
                                    <a href="addadvertisement.vpage?adId=${ad.id!}">编辑</a>
                                    <a id="delete_${ad.id!}" href="javascript:void(0);">删除</a>
                                    <#if ad.status == '上线'><a id="online_${ad.id}" href="javascript:void(0);" status="2">下线</a>
                                    <#else><a id="online_${ad.id}" href="javascript:void(0);" status="1">上线</#if></a>
                                </td>
                                <td>
                                    <a href="adregionindex.vpage?adId=${ad.id!}">设置</a>
                                </td>
                                <td>${ad.specialKey!}</td>
                                <td>
                                    <a href="admaterialindex.vpage?adId=${ad.id!}">上传</a>
                                </td>
                            </tr>
                        </#list>
                    </#if>
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

        $('[id^="online_"]').on('click', function(){

            var status = $(this).attr("status");
            var op;
            if(status == '1'){
                op = "上线";
            }else{
                op = "下线";
            }
            if(!confirm("确定要"+op+"？")){
                return false;
            }
            var id = parseInt($(this).attr("id").substr("online_".length));
            $.post("setonline.vpage",{
                id:id,
                status:parseInt(status)
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    status = (status == '2')?1:2;
                    $("#online_"+id).text((op == '上线')?"下线":"上线");
                    $("#status_"+id).text((op == '上线')?"上线":"下线");
                    $("#online_"+id).attr("status",status);
                }
            });
        });

        $('[id^="delete_"]').on('click', function(){
            if(!confirm("确定要删除吗？")){
                return false;
            }
            var id = $(this).attr("id").substring("delete_".length);
            $.post('deladvertisement.vpage',{
                id:id
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            });
        });
    });
</script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-prompts-alert.js"></script>
</@layout_default.page>