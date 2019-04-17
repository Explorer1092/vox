<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="Advertisement Management" page_num=4>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<div id="main_container" class="span9">
    <legend>
        <a href="index.vpage">学分体系管理</a>&nbsp;&nbsp;
        <a href="search_credit.vpage" style="color: #0C0C0C">学分详情查询</a>&nbsp;&nbsp;
        <a href="credit_logs.vpage" style="color: #0C0C0C">学分记录查询</a>&nbsp;&nbsp;
        <a id="add_advertiser_btn" href="add.vpage" type="button" class="btn btn-info" style="float: right">增加</a>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="frm" class="form-horizontal" method="post" action="index.vpage" >
                    <input type="hidden" id="pageNumber" name="pageNumber" value="${pageNumber!''}"/>
                    行为终端：
                    <select class="input-small" id="itemId" name="itemId" onchange="selectType()">
                        <option value="" selected>选择任意一项</option>
                        <#list terms as term>
                            <option value="${term!}">${term!}</option>
                        </#list>
                    </select>
                    &nbsp;
                    行为类型：
                    <select class="input-small" id="itemTypeId" name="itemTypeId">
                        <option value="" selected>选择任意一项</option>
                    </select>
                    &nbsp;&nbsp;&nbsp;&nbsp;
                    创建时间：
                    <input id="startDate" type="text" class="input-small" placeholder="开始时间" name="startDate" value="${startDate!}">~
                    <input id="endDate" type="text" class="input-small" placeholder="结束时间" name="endDate" value="${endDate!}">
                    &nbsp;&nbsp;&nbsp;&nbsp;
                    <button type="submit" class="btn btn-primary">查询</button>
                </form>
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>所属业务</td>
                        <td>行为名称</td>
                        <td>学分名称</td>
                        <td>行为终端</td>
                        <td>学科</td>
                        <td>行为分值</td>
                        <td>创建时间</td>
                        <td style="width: 175px;">操作</td>
                    </tr>
                    <#if achivementlist?? >
                        <#list achivementlist as al >
                            <tr>
                            <td>${al.bussType!}</td>
                            <td>${al.name!}</td>
                            <td>${al.itemType!}</td>
                            <td>${al.term!}</td>
                            <td>${al.subject!}</td>
                            <td>${al.value!}</td>
                            <td>${al.createTime!?string('yyyy-MM-dd HH:mm:ss')!}</td>
                            <td>
                            <a class="btn btn-success" href="add.vpage?id=${al.id!}">编辑</a>
                            <a class="btn btn-primary" id="disenable_${al.id}" ref="${al.disable?string('true','false')}" href="javascript:void(0);"><#if al.disable?? && al.disable==true>启用<#else>禁用</#if></a>
                            <a class="btn btn-danger delete" id="delete_${al.id!}" href="javascript:void(0);">删除</a>
                            </td>
                            </tr>
                        </#list>
                    </#if>
                </table>
                <ul class="pager">
                    <#if (hasPrevious!)>
                        <li><a href="javascript::void()" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
                    <#else>
                        <li class="disabled"><a href="javascript::void()">上一页</a></li>
                    </#if>
                    <#if (hasNext!)>
                        <li><a href="javascript::void()" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
                    <#else>
                        <li class="disabled"><a href="javascript::void()">下一页</a></li>
                    </#if>
                    <li>当前第 ${pageNumber!} 页 |</li>
                    <li>共 ${totalPages!} 页</li>
                </ul>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">

    function pagePost(pageNumber){
        $("#pageNumber").val(pageNumber);
        $("#frm").submit();
    }

    function selectType() {
        //选中终端
        var term = $("#itemId").val();
        $("#itemTypeId").find("option:not(:first)").remove();
        $.get('type.vpage', {
            term:term
        }, function (data) {
            for (var i in data) {
                var temp = data[i];
                $("#itemTypeId").append(new Option(temp, temp));
            }
        })
    }

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

        $('[id^="disenable_"]').on('click', function () {
            var type = $(this).attr("ref");
            var id = $(this).attr("id").substring("disenable_".length);
            if (type == 'true') {
                if (!confirm("确定要启用吗？")) {
                    return false;
                }
            }
            if (type == 'false') {
                if (!confirm("确定要禁用吗？")) {
                    return false;
                }
            }
            $.post('endisable.vpage', {
                id:id,
                type:type
            }, function (data) {
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            })
        });

        $('[id^="delete_"]').on('click', function() {
            if(!confirm("确定要删除吗？")) {
                return false;
            }
            var id = $(this).attr("id").substring("delete_".length);
            $.post('del.vpage', {
                id:id
            },function(data) {
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