<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='薯条英语老师管理' page_num=26>

<style>
    .form-group {
        margin: 5px 0;
        display: inline-block;
    }

    .form-group .mylabel {
        width: 150px;
        text-align: right;
    }

    .table-responsive {
        min-height: .01%;
        overflow-x: auto
    }

    @media screen and (max-width: 767px) {
        .table-responsive {
            width: 100%;
            margin-bottom: 15px;
            overflow-y: hidden;
            -ms-overflow-style: -ms-autohiding-scrollbar;
            border: 1px solid #ddd
        }

        .table-responsive > .table {
            margin-bottom: 0
        }

        .table-responsive > .table > tbody > tr > td, .table-responsive > .table > tbody > tr > th, .table-responsive > .table > tfoot > tr > td, .table-responsive > .table > tfoot > tr > th, .table-responsive > .table > thead > tr > td, .table-responsive > .table > thead > tr > th {
            white-space: nowrap
        }

        .table-responsive > .table-bordered {
            border: 0
        }

        .table-responsive > .table-bordered > tbody > tr > td:first-child, .table-responsive > .table-bordered > tbody > tr > th:first-child, .table-responsive > .table-bordered > tfoot > tr > td:first-child, .table-responsive > .table-bordered > tfoot > tr > th:first-child, .table-responsive > .table-bordered > thead > tr > td:first-child, .table-responsive > .table-bordered > thead > tr > th:first-child {
            border-left: 0
        }

        .table-responsive > .table-bordered > tbody > tr > td:last-child, .table-responsive > .table-bordered > tbody > tr > th:last-child, .table-responsive > .table-bordered > tfoot > tr > td:last-child, .table-responsive > .table-bordered > tfoot > tr > th:last-child, .table-responsive > .table-bordered > thead > tr > td:last-child, .table-responsive > .table-bordered > thead > tr > th:last-child {
            border-right: 0
        }

        .table-responsive > .table-bordered > tbody > tr:last-child > td, .table-responsive > .table-bordered > tbody > tr:last-child > th, .table-responsive > .table-bordered > tfoot > tr:last-child > td, .table-responsive > .table-bordered > tfoot > tr:last-child > th {
            border-bottom: 0
        }
    }

    .table_box {
        max-height: 700px;
    }

    .table_box table tr td {
        white-space: nowrap;
    }

    .table_box table tbody tr td {
        white-space: nowrap;
    }
</style>

<div id="main_container" class="span9">
    <legend>薯条英语教师管理
        <button type="button" id="create" class="btn btn-primary pull-right">新建</button>
    </legend>
    <div id="data_table_journal">
        <div class="table-responsive table_box">
            <table class="table table-striped table-bordered">
                <tr>
                    <td>姓名</td>
                    <#--<td>微信号</td>-->
                    <#--<td>二维码</td>-->
                    <td>头像</td>
                    <td>操作</td>
                </tr>
                <#if teacherList?? && teacherList?size gt 0>
                    <#list teacherList as e >
                        <tr>
                            <td style="width:150px;">${e.name!}</td>
                            <#--<td>${e.wxCode!}</td>-->
                            <#--<td>-->
                                <#--<#if e.qrImage??>-->
                                    <#--<img src="${e.qrImage!}" style="width: 200px;height: 200px">-->
                                <#--</#if>-->
                            <#--</td>-->
                            <td >
                                <#if e.headPortrait?? && e.headPortrait != "">
                                    <img src="${e.headPortrait!}" style="width: 200px;height: 200px">
                                </#if>
                            </td>
                            <td>
                                <button type="button" class="btn btn-primary" onclick="modifyClick('${e.id!}')">
                                    编辑
                                </button>
                                <button type="button" class="btn btn-primary" onclick="del('${e.id!}')">
                                    删除
                                </button>
                            </td>
                        </tr>
                    </#list>
                <#else >
                    <tr>
                        <td colspan="13"><strong>暂无数据</strong></td>
                    </tr>
                </#if>
            </table>
        </div>
    </div>

    <script type="text/javascript">

        function modifyClick(id) {
//            console.log(id)
            window.location.href = "/chips/ai/teacher/create.vpage?id=" + id;
        };

        function del(id) {
            console.log(id)
            $.ajax({
                type: 'get',
                url: 'delete.vpage?id='+id,
                success: function (data) {
                    if (data.success) {
                        alert("删除成功");
                        window.location.reload();
                    } else {
                        alert("删除失败:" + data.info);
                    }
                },
                error: function (msg) {
                    alert("保存失败！");
                }
            });

        };

        $(function () {
            $("#create").on('click', function () {
                window.location.href = "/chips/ai/teacher/create.vpage";
            });
        });
    </script>
</@layout_default.page>