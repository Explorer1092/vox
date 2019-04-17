<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<#--<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>-->
<style>
    span {
        font: "arial";
    }
</style>
<div id="main_container" class="span9">
    <div>
        <form id="s_form" action="?" method="post" class="form-horizontal">
            <fieldset>
                <legend>微信FAQ审核</legend>
            </fieldset>
            <ul class="inline">
                <li>
                    <label>
                        <#list wechatTypes as wechatType>
                            <#if type == wechatType.type>
                                ${wechatType.desc!}
                            <#else>
                                <a href="?type=${wechatType.type}">${wechatType.desc!}</a>
                            </#if>
                        </#list>
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>选择要查看的FAQ：
                        <select name="cid">
                            <#list catalogs as catalog>
                                <option <#if cid == catalog.id>selected="selected" </#if>
                                        value="${catalog.id }">${catalog.name! }</option>
                            </#list>
                        </select>
                    </label>
                </li>
                <li>
                    <label><input type="radio" <#if status == 0>checked</#if> class="approved" name="status" value="0">
                        草稿</label>
                </li>

                <li>
                    <label><input type="radio" <#if status == 1>checked</#if> class="approved" name="status" value="1">
                        已发布</label>
                </li>

                <li>
                    <label><input type="radio" <#if status == -1>checked</#if> class="approved" name="status"
                                  value="-1"> 全部</label>
                </li>


                <li>
                    <button type="submit" id="submit" class="btn btn-primary">查询</button>
                </li>
                <li>
                    <a href="toeditpage.vpage?type=${type}" type="button" class="btn btn-info" target="_blank">添加</a>
                </li>
                <input type="hidden" name="type" value="${type}">
            </ul>
        </form>
    </div>

    <#if faqs??>
        <div class="row-fluid">
            <div class="span12">
                <div class="well">
                    <input type="button" class="btn btn-success" value="发布" id="approveButton"/>
                    <input type="button" class="btn btn-warning" value="下线" id="rejectButton"/>
                    <input type="button" class="btn btn-danger" value="删除" id="removeButton"/>

                    <div id="data_table_journal">
                        <table class="table table-striped table-bordered so_checkboxs" so_checkboxs_values="">
                            <tr>
                                <td><input type="checkbox" class="so_checkbox_all"></td>
                                <td>ID</td>
                                <td>创建时间</td>
                                <td>标题</td>
                                <td>简介</td>
                                <td>标题图片</td>
                                <td>关键词</td>
                                <td>状态</td>
                                <td>内容</td>
                                <td>操作</td>
                            </tr>
                            <#list faqs as faq >
                                <tr>
                                    <td><input name="faqId" type="checkbox" class="so_checkbox" value="${faq.id!}"></td>
                                    <td>${faq.id!}</td>
                                    <td>${faq.createDatetime!}</td>
                                    <td>${faq.title!}</td>
                                    <td>${faq.description!}</td>
                                    <td>${faq.picUrl!}</td>
                                    <td>${faq.keyWord!}</td>
                                    <td>
                                        <#if faq.status == 'draft'>草稿
                                        <#elseif faq.status == 'published'>已发布
                                        </#if>
                                    </td>
                                    <td><input type="button" id="preview_${faq.id }"
                                               data-view_html="${faq.content?html}" value="预览"
                                               class="btn btn-success viewBut"></td>
                                    <td><a href="toeditpage.vpage?id=${faq.id }&type=${type}" type="button"
                                           class="btn btn-info">编辑</a></td>
                                </tr>
                            </#list>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </#if>
</div>

<div id="add_dialog" class="modal fade hide" style="width: 60%; left: 40%;">
    <div class="modal-dialog">
        <div class="modal-content" style="padding: 10px 10px;">


        </div>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        $("input.so_checkbox_all").on("click", function () {
            if (!$("input.so_checkbox_all").is(':checked')) {
                $("input.so_checkbox_all").attr("checked", false);
                $("input.so_checkbox").attr("checked", false);
                $("table.so_checkboxs").attr("so_checkboxs_values", "");
            } else {
                $("input.so_checkbox").attr("checked", true);
                var so_checkboxs_values = [];
                $("input.so_checkbox:checked").each(function () {
                    so_checkboxs_values.push($(this).val());
                });
                $("table.so_checkboxs").attr("so_checkboxs_values", so_checkboxs_values.toString());
            }
        });

        $("input.so_checkbox").on("click", function () {
            if ($("input.so_checkbox").size() == $("input.so_checkbox:checked").size()) {
                $("input.so_checkbox_all").attr("checked", true);
            } else {
                $("input.so_checkbox_all").attr("checked", false);
            }
            var so_checkboxs_values = [];
            $("input.so_checkbox:checked").each(function () {
                so_checkboxs_values.push($(this).val());
            });
            $("table.so_checkboxs").attr("so_checkboxs_values", so_checkboxs_values.toString());
        });

        $('#removeButton').on('click', function () {
            var faqIds = $("table.so_checkboxs").attr("so_checkboxs_values").split(",");
            if (faqIds.length == 0) {
                alert("请至少选择一条数据");
                return false;
            }
            var postData = {
                faqIds: faqIds.toString()
            };

            $.post("${requestContext.webAppContextPath}/site/wechatfaq/removefaq.vpage", postData, function (data) {
                if (data.success) {
                    alert("操作成功");
                    $('#submit').trigger('click');
                } else {
                    alert(data.info);
                }
            });
        });

        $('#approveButton').on('click', function () {
            var faqIds = $("table.so_checkboxs").attr("so_checkboxs_values").split(",");
            if (faqIds.length == 0) {
                alert("请至少选择一条数据");
                return false;
            }
            var postData = {
                faqIds: faqIds.toString()
            };

            $.post("${requestContext.webAppContextPath}/site/wechatfaq/approvefaq.vpage", postData, function (data) {
                if (data.success) {
                    alert("操作成功");
                    $('#submit').trigger('click');
                } else {
                    alert(data.info);
                }
            });
        });

        $('#rejectButton').on('click', function () {
            var faqIds = $("table.so_checkboxs").attr("so_checkboxs_values").split(",");
            if (faqIds.length == 0) {
                alert("请至少选择一条数据");
                return false;
            }
            var postData = {
                faqIds: faqIds.toString()
            };

            $.post("${requestContext.webAppContextPath}/site/wechatfaq/rejectfaq.vpage", postData, function (data) {
                if (data.success) {
                    alert("操作成功");
                    $('#submit').trigger('click');
                } else {
                    alert(data.info);
                }
            });
        });

        $('.viewBut').on('click', function () {
            var viewHtml = $(this).attr('data-view_html');
            $('#add_dialog').modal('show').find('.modal-content').html(viewHtml.replace(/\.\.\/\.\.\/static/g, 'http://wx.17zuoye.com/static'));
        });

    });
</script>

</@layout_default.page>