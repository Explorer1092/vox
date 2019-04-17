<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="处理申请学校" page_num=6>
<div id="loadingDiv" style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 100%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">数据查询中，请稍后……</p>
</div>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i> 处理申请学校</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a href="javascript:void(0);" class="btn btn-primary" id="sure_school">批量确认</a>
            </div>
        </div>
        <div class="box-content">
            <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                   id="DataTables_Table_0"
                   aria-describedby="DataTables_Table_0_info">
                <thead>
                <tr>
                    <th class="sorting"><input type="checkbox" class="all-select">全选</th>
                    <th class="sorting">城市/地区</th>
                    <th class="sorting">学校ID</th>
                    <th class="sorting">学校名称</th>
                    <th class="sorting">学校阶段</th>
                    <th class="sorting">申请人</th>
                    <th class="sorting">申请类型</th>
                    <th class="sorting">操作</th>
                </tr>
                </thead>
                <tbody role="alert" aria-live="polite" aria-relevant="all">
                    <#if data??>
                        <#list data as d>
                        <tr class="odd">
                            <td class="center"><input type="checkbox" class="school-apply-item" value=${d.id!''}
                                                      data-type="${d.modifyType!'--'}"></td>
                            <td class="center">${d.regionName!'--'}</td>
                            <td class="center">${d.schoolId!''}</td>
                            <td class="center">${d.schoolName!'--'}</td>
                            <td class="center">${d.phase!'--'}</td>
                            <td class="center">${d.accountName!'--'}</td>
                            <td class="center">${d.modifyType!'--'}</td>
                            <td class="center ">
                                <a class="btn btn-danger" href="javascript:void(0);" data-value=${d.id!''}
                                   data-school-value="学校id:${d.schoolId!''},${d.schoolName!'--'}"
                                   data-type="${d.modifyType!'--'}">
                                    <i class="icon-trash icon-white"></i>
                                    确 认
                                </a>
                            </td>
                        </tr>
                        </#list>
                    </#if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script type="application/javascript">

    $(".school-apply-item").click(function () {
        var allSelect = true;
        $(".school-apply-item").each(function (index, element) {
            if (!$(element).attr("checked")) {
                allSelect = false;
            }
        });
        if (allSelect) {
            $(".all-select").attr("checked", allSelect);
            $(".all-select").parent("span").addClass("checked");
        } else {
            $(".all-select").attr("checked", false);
            $(".all-select").parent("span").removeClass("checked");
        }
    });

    $(".all-select").click(function () {
        var allSelect = $(this).attr("checked");
        $(".school-apply-item").each(function (index, element) {
            if (allSelect) {
                $(element).attr("checked", allSelect);
                $(element).parent("span").addClass("checked");
            } else {
                $(element).attr("checked", false);
                $(element).parent("span").removeClass("checked");
            }
        });
    });

    $(".btn-danger").bind("click", function () {
        var schoolInfo = $(this).attr("data-school-value");
        var addOrRemove = $(this).attr("data-type") == "添加学校" ? "加入字典表" : $(this).attr("data-type") == "业务变更" ? "业务变更" : "删除字典表";
        if (confirm("是否确认【" + schoolInfo + "】" + addOrRemove)) {
            var data = JSON.stringify([$(this).attr("data-value")]);
            sureApply(data);
        }
    });

    $("#sure_school").bind("click", function () {
        var addCount = 0;
        var removeCount = 0;
        var updateCoount = 0;
        $(".school-apply-item").each(function (index, element) {
            if ($(element).attr("checked")&& $(element).attr("data-type") == "添加学校") {
                addCount++;
            }
            if ($(element).attr("checked") && $(element).attr("data-type") == "删除学校") {
                removeCount++;
            }
            if ($(element).attr("checked") && $(element).attr("data-type") == "业务变更") {
                updateCoount++;
            }
        });
        if (confirm("批处理：添加" + addCount + "所,删除" + removeCount + "所，业务变更 " + updateCoount + "所，是否确认?")) {
            var data = [];
            $(".school-apply-item").each(function (index, element) {
                if($(element).attr("checked")){
                    data.push($(element).val());
                }
            });
            sureApply(JSON.stringify(data));
        }
    });
    function sureApply(data) {
        $.ajax({
            type: 'post',
            url: "dispose_apply_school.vpage",
            dataType: 'json',
            contentType: 'application/json;charset=UTF-8',
            data: data,
            success: success,
            error: function () {
                alert("确认申请失败");
            }
        })
    }

    function success(res) {
        if (res.success) {
            alert(  "失败:" + (res.failed) +
                    "\r\n成功:" + (res.successItem) +
                    "\r\n合计:" + (res.summary));
            window.location.reload();
        } else {
            alert(res.info);
        }
    }
</script>
</@layout_default.page>