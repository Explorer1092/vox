<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="个人及学校黑名单管理" page_num=24>
<style>
    form {
        box-shadow: #0C0C0C;
    }

    .table {
        background: #FFFFFF;
        box-shadow: #0C0C0C;
    }
</style>
<div id="main_container" class="span9">

    <div>
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation" class="active"><a href="/equator/config/blacklist/index.vpage">个人及学校黑名单管理</a></li>
            <li role="presentation"><a href="/equator/config/blacklist/regionindex.vpage">地区黑名单管理</a></li>
        </ul>
    </div>

    <form class="form-horizontal" action="index.vpage" method="get" id="queryForm">
        黑名单类型:
        <select id="type" name="type">
            <#if typeList??>
                <#list typeList?keys as key>
                    <option value="${key}"
                            <#if specType?? && specType == key>selected</#if>>${typeList[key]!''}
                    </option>
                </#list>
            </#if>
        </select>
        黑名单模块类型:
        <select id="module" name="module">
            <#if moduleList??>
                <#list moduleList?keys as key>
                    <option value="${key}"
                            <#if specModule?? && specModule == key>selected</#if>>${moduleList[key]!''}
                    </option>
                </#list>
            </#if>
        </select>
        目标值:
        <input type="text" id="tagValue" name="tagValue" value="${specTagValue!''}" autofocus="autofocus"
               placeholder="请输入目标值"/>
        <button type="submit" class="btn btn-info">查询</button>

        <button type="button" class="btn btn-info insert">新增</button>
    </form>

    <#if blackLists?? &&  blackLists?size gt 0>
        <table class="table table-hover table-striped table-bordered">
            <thead>
            <tr>
                <#if isStudent??>
                    <th>学生ID</th>
                    <th>学生姓名</th>
                <#else>
                    <th>学校ID</th>
                    <th>学校名称</th>
                </#if>
                <th>模块类型</th>
                <th>创建时间</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
                <#list blackLists as blackList>
                <tr>
                    <td>${blackList.id}</td>
                    <td>${blackList.name}</td>
                    <td>${blackList.module.desc}</td>
                    <td>${blackList.createTime}</td>
                    <td>
                        <button class="btn btn-warning remove"
                                data-type="${blackList.type}"
                                data-module="${blackList.module.value}"
                                data-id="${blackList.id}"
                        >
                            删除
                        </button>
                    </td>
                </tr>
                </#list>
            </tbody>
        </table>
    </#if>
</div>
<div id="insert_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>新增</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>黑名单类型:</dt>
                    <dd>
                        <#if isStudent??>
                            <select class="insertType">
                                <#if studentTypeList??>
                                    <#list studentTypeList?keys as key>
                                        <option value="${key}">${studentTypeList[key]!''}</option>
                                    </#list>
                                </#if>
                            </select>
                        <#else>
                            <select class="insertType">
                                <#if schoolTypeList??>
                                    <#list schoolTypeList?keys as key>
                                        <option value="${key}">${schoolTypeList[key]!''}</option>
                                    </#list>
                                </#if>
                            </select>
                        </#if>
                    </dd>
                </li>
                <li>
                    <dt>黑名单模块类型:</dt>
                    <dd>
                        <select class="insertModule">
                            <#if moduleList??>
                                <#list moduleList?keys as key>
                                    <option value="${key}">${moduleList[key]!''}</option>
                                </#list>
                            </#if>
                        </select>
                    </dd>
                </li>
                <li>
                    <dt>
                        <#if isStudent??>
                            学生ID
                        <#else>
                            学校ID
                        </#if>
                        :</dt>
                    <dd><input class="insertId" type="text" value=""/></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="insert_dialog_confirm_btn" class="btn btn-primary">确定</button>
        <button id="insert_dialog_cancel_btn" class="btn btn-primary">取消</button>
    </div>
</div>
<script>
    $(function () {
        $('.insert').click(function () {
            $('#insert_dialog').modal('show');
        });

        $('#insert_dialog_cancel_btn').click(function () {
            $('#insert_dialog').modal('hide');
        });

        $('.remove').click(function () {
            let type = $(this).attr('data-type');
            let module = $(this).attr('data-module');
            let tagValue = $(this).attr('data-id');
            $.post("remove.vpage", {
                type: type,
                module: module,
                tagValue: tagValue,
            }, function (data) {
                if (data.success) {
                    alert("删除成功!");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });

        $('#insert_dialog_confirm_btn').click(function () {
            let type = $('.insertType').find('option:selected').val();
            let module = $('.insertModule').find('option:selected').val();
            let tagValue = $('.insertId').val();

            console.log(tagValue);

            let checkResult = false;
            $.when($.post("check.vpage", {
                type: type,
                module: module,
                tagValue: tagValue,
            }, function (data) {
                if (data.success) {
                    checkResult = data.result;
                }
            })).then(function () {
                // 如果有排斥
                if (checkResult) {
                    if (type == "Black_Student") {
                        alert("用户对应类型、对应模块下已存在白名单，请先删除白名单！");
                        return
                    }
                    if (type == "White_Student")
                        if (!confirm("用户对应类型、对应模块下已存在黑名单，当前操作会覆盖黑名单！是否保存？")) {
                            return;
                        }
                }

                $.post("insert.vpage", {
                    type: type,
                    module: module,
                    tagValue: tagValue,
                }, function (data) {
                    if (data.success) {
                        alert("添加成功!");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            });

        });
    });
</script>
</@layout_default.page>