<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="金刚位管理" page_num=4>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<div id="main_container" class="span9">
    <legend>
        <a href="index.vpage">金刚位显示设置</a>&nbsp;&nbsp;
        <a href="${requestContext.webAppContextPath}/site/classify/index.vpage" style="color: #0C0C0C">分类设置</a>&nbsp;&nbsp;
        <a id="add_advertiser_btn" href="position.vpage" type="button" class="btn btn-info" style="float: right">增加</a>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="frm" class="form-horizontal" method="get" action="index.vpage" >
                    标题名称：
                    <input id="mainTitle" name="mainTitle" value="${mainTitle!''}" autocomplete="true"/>
                    &nbsp;
                    所属类型：
                    <select class="selectpicker" id="itemId" name="itemId">
                        <option value="" selected>选择类型</option>
                        <#list types as term>
                            <option value="${term!}">${term!}</option>
                        </#list>
                    </select>
                    &nbsp;
                    所属状态：
                    <select class="input-small" id="undercarriage" name="undercarriage">
                        <option value="" selected>选择任意一项</option>
                        <option value="false">上架</option>
                        <option value="true">下架</option>
                    </select>
                    &nbsp;
                    <button type="submit" class="btn btn-primary">查询</button>
                </form>
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>标题名称</td>
                        <td>副标题名称</td>
                        <td>标签名称</td>
                        <td>标签颜色</td>
                        <td>副标签名称</td>
                        <td>副标签颜色</td>
                        <td>所属类型</td>
                        <td>跳转类型</td>
                        <td>展示位置</td>
                        <td>展示顺序</td>
                        <td>登录后查看</td>
                        <td>创建时间</td>
                        <td style="width: 175px;">操作</td>
                    </tr>
                    <#if positionList?? >
                        <#list positionList as pl>
                            <tr>
                            <td>${pl.mainTitle!}</td>
                            <td>${pl.subheading!}</td>
                            <td>${pl.lable!}</td>
                            <td>${pl.labelColor!}</td>
                            <td>${pl.tagText!}</td>
                            <td>${pl.tagTextColor!}</td>
                            <td>${pl.classify!}</td>
                            <td>${pl.functionType!}</td>
                            <td><#if pl.disAddress?? && pl.disAddress=='MP'>主位<#else>副位</#if></td>
                            <td>${pl.order!}</td>
                            <td><#if pl.loginStatus?? && pl.loginStatus==true>是<#else>否</#if></td>
                            <td>${pl.createTime!?string('yyyy-MM-dd HH:mm:ss')!}</td>
                            <td>
                        <a class="btn btn-success" href="position.vpage?id=${pl.id!}&undercarriage=${pl.undercarriage?string('true','false')}">编辑</a>
                        <a class="btn btn-primary" id="disenable_${pl.id}" ref="${pl.undercarriage?string('true','false')}" href="javascript:void(0);"><#if pl.undercarriage?? && pl.undercarriage==true>上架<#else>下架</#if></a>
                        <a class="btn btn-danger delete" id="delete_${pl.id!}" href="javascript:void(0);">删除</a>
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

    function selectType() {
        $.get('type.vpage', {
        }, function (data) {
            for (var i in data) {
                var temp = data[i];
                $("#itemId").append(new Option(temp, temp));
            }
        })
    }

    $(function(){
        $('[id^="disenable_"]').on('click', function () {
            var undercarriage = $(this).attr("ref");
            var id = $(this).attr("id").substring("disenable_".length);
            if (undercarriage == 'true') {
                if (!confirm("确定要上架吗？")) {
                    return false;
                }
            }
            if (undercarriage == 'false') {
                if (!confirm("确定要下架吗？")) {
                    return false;
                }
            }
            $.post('endisable.vpage', {
                id:id,
                undercarriage:undercarriage
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