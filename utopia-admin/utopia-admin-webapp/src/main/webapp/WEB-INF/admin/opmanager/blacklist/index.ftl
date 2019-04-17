<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="第三方应用管理" page_num=9>
<script src="//cdn.17zuoye.com/public/plugin/jquery/jquery-1.7.1.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/tablesorter/jquery.tablesorter.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/tablesorter.css" rel="stylesheet">

<div id="main_container" class="span9">
    <div>
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation" class="active"><a href="/opmanager/blacklist/index.vpage">个人及学校名单</a></li>
            <li role="presentation"><a href="/opmanager/blacklist/regions.vpage">地区黑名单</a></li>
        </ul>
        <ul class="inline">
            <li>
                <select id="blacklistType" name="blacklistType" style="width:250px">
                    <option value="studentBlackList" selected="selected">--学生个人黑名单--</option>
                    <option value="parentBlackList">--家长个人黑名单--</option>
                    <option value="parentAdBlackList">--家长通广告黑名单--</option>
                    <option value="whiteUserList">--付费白名单用户--</option>
                    <option value="paymentLimitWhiteUserList">--支付限额白名单用户--</option>
                    <option value="schoolBlackList">--学校黑名单(按shift多列排序)--</option>
                    <option value="schoolGrayList">--学校灰名单--</option>
                    <option value="studentGrayList">--学生灰名单--</option>
                </select>
            </li>
            <li>
                <div class="control-group">
                    <button type="button" class="btn btn-primary btn-lg" data-toggle="modal" data-target="#myModal">
                        新增
                    </button>
                </div>
            </li>
        </ul>
    </div>

    <ul class="inline">
        <table id="studentBlackList" class="table table-bordered" style="display: none">
            <tr>
                <th>用户id</th>
                <th>用户姓名</th>
                <th>创建时间</th>
                <th>描述</th>
                <th>操作</th>
            </tr>
            <#if studentUserIds ?? >
                <#list studentUserIds as studentUser >
                    <tr>
                        <td>${studentUser.userId?default("")}</td>
                        <td>${studentUser.username?default("")}</td>
                        <td>${studentUser.createTime?default("")}</td>
                        <td>${studentUser.desc?default("")}</td>
                        <td><a href="javascript:void(0)" data-tagId='${studentUser.tagId?default("")}'
                               class="tag-delete">删除</a>
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>

        <table id="whiteUserList" class="table table-bordered" style="display: none">
            <tr>
                <th>用户id</th>
                <th>用户姓名</th>
                <th>创建时间</th>
                <th>描述</th>
                <th>操作</th>
            </tr>
            <#if whiteUserIds ?? >
                <#list whiteUserIds as midUser >
                    <tr>
                        <td>${midUser.userId?default("")}</td>
                        <td>${midUser.username?default("")}</td>
                        <td>${midUser.createTime?default("")}</td>
                        <td>${midUser.desc?default("")}</td>
                        <td><a href="javascript:void(0)" data-tagId='${midUser.tagId?default("")}'
                               class="tag-delete">删除</a>
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>

        <table id="paymentLimitWhiteUserList" class="table table-bordered" style="display: none">
            <tr>
                <th>用户id</th>
                <th>用户姓名</th>
                <th>创建时间</th>
                <th>描述</th>
                <th>操作</th>
            </tr>
            <#if paymentLimitWhiteUserIds ?? >
                <#list paymentLimitWhiteUserIds as midUser >
                    <tr>
                        <td>${midUser.userId?default("")}</td>
                        <td>${midUser.username?default("")}</td>
                        <td>${midUser.createTime?default("")}</td>
                        <td>${midUser.desc?default("")}</td>
                        <td><a href="javascript:void(0)" data-tagId='${midUser.tagId?default("")}'
                               class="tag-delete">删除</a>
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>

        <table id="parentBlackList" class="table table-bordered" style="display: none">
            <tr>
                <th>家长id</th>
                <th>家长名称</th>
                <th>创建时间</th>
                <th>描述</th>
                <th>操作</th>
            </tr>
            <#if parentUserIds ?? >
                <#list parentUserIds as user >
                    <tr>
                        <td>${user.userId?default("")}</td>
                        <td>${user.username?default("")}</td>
                        <td>${user.createTime?default("")}</td>
                        <td>${user.desc?default("")}</td>
                        <td><a href="javascript:void(0)" data-tagId='${user.tagId?default("")}'
                               class="tag-delete">删除</a></td>
                    </tr>
                </#list>
            </#if>
        </table>

        <table id="parentAdBlackList" class="table table-bordered" style="display: none">
            <tr>
                <th>家长id</th>
                <th>家长名称</th>
                <th>创建时间</th>
                <th>描述</th>
                <th>操作</th>
            </tr>
            <#if parentAdUserIds ?? >
                <#list parentAdUserIds as user >
                    <tr>
                        <td>${user.userId?default("")}</td>
                        <td>${user.username?default("")}</td>
                        <td>${user.createTime?default("")}</td>
                        <td>${user.desc?default("")}</td>
                        <td><a href="javascript:void(0)" data-tagId='${user.tagId?default("")}'
                               class="tag-delete">删除</a></td>
                    </tr>
                </#list>
            </#if>
        </table>

        <table id="schoolBlackList" class="tablesorter table table-bordered" style="display: none">
            <thead>
            <tr>
                <th>学校id</th>
                <th>学校名称</th>
                <th>创建时间</th>
                <th>省份</th>
                <th>地区</th>
                <th>描述</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
                <#if schools ?? >
                    <#list schools as school >
                    <tr>
                        <td>${school.schoolId?default("")}</td>
                        <td>${school.schoolName?default("")}</td>
                        <td>${school.createTime?default("")}</td>
                        <td>${school.provinceName?default("")}</td>
                        <td>${school.cityName?default("")}</td>
                        <td>${school.desc?default("")}</td>
                        <td><a href="javascript:void(0)" data-tagId='${school.tagId?default("")}'
                               class="tag-delete">删除</a></td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>

        <table id="schoolGrayList" class="tablesorter table table-bordered" style="display: none">
            <thead>
            <tr>
                <th>学校id</th>
                <th>学校名称</th>
                <th>创建时间</th>
                <th>省份</th>
                <th>地区</th>
                <th>描述</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
                <#if graySchools ?? >
                    <#list graySchools as school >
                    <tr>
                        <td>${school.schoolId?default("")}</td>
                        <td>${school.schoolName?default("")}</td>
                        <td>${school.createTime?default("")}</td>
                        <td>${school.provinceName?default("")}</td>
                        <td>${school.cityName?default("")}</td>
                        <td>${school.desc?default("")}</td>
                        <td><a href="javascript:void(0)" data-tagId='${school.tagId?default("")}'
                               class="tag-delete">删除</a></td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>

        <table id="studentGrayList" class="table table-bordered" style="display: none">
            <tr>
                <th>用户id</th>
                <th>用户姓名</th>
                <th>创建时间</th>
                <th>描述</th>
                <th>操作</th>
            </tr>
            <#if grayUsers ?? >
                <#list grayUsers as studentUser >
                    <tr>
                        <td>${studentUser.userId?default("")}</td>
                        <td>${studentUser.username?default("")}</td>
                        <td>${studentUser.createTime?default("")}</td>
                        <td>${studentUser.desc?default("")}</td>
                        <td><a href="javascript:void(0)" data-tagId='${studentUser.tagId?default("")}'
                               class="tag-delete">删除</a>
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>
    </ul>
</div>
<!-- Modal -->
<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title" id="myModalLabel"> 增加名单</h4>
            </div>
            <div class="modal-body">
                <div class="form-group">
                    <select name="addBlacklistType" id="addBlacklistType">
                        <option value="AfentiBlackListUsers" selected="selected">黑名单学生账号</option>
                        <option value="ParentBlackListUsers">黑名单家长账号</option>
                        <option value="ParentAdBlackUsers">家长广告黑名单账号</option>
                        <option value="PaymentWhiteListUsers">付费白名单账号</option>
                        <option value="PaymentLimitWhiteListUsers">支付限额白名单账号</option>
                        <option value="AfentiBlackListSchools">黑名单学校id</option>
                        <option value="PaymentGrayListSchools">灰名单学校id</option>
                        <option value="PaymentGrayListUsers">灰名单学生id</option>
                    </select><input type="text" name="tagValue" id="tagValue" placeholder="请以','或空白符隔开"/>
                </div>
                <div class="form-group"><textarea id="tagComment" name="tagComment" placeholder="详细描述"></textarea></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" id="addBlacklistBtn" class="btn btn-primary">添加</button>
            </div>
        </div>
    </div>
</div>


<script type="text/javascript">
    var schoolModel = $('#schoolBlackList');
    var studentModel = $('#studentBlackList');
    var parentModel = $('#parentBlackList');
    var whiteModel = $('#whiteUserList');
    var payLimitWhiteModel = $('#paymentLimitWhiteUserList');
    var graySchoolModel = $('#schoolGrayList');
    var grayUsersModel = $('#studentGrayList');
    //删除行
    function deleteTag(tagId, tagNode) {
        if (!confirm("确定要删除吗吗?")) {
            return false;
        }
        $.get("deleteTag.vpage", {tagId: tagId}, function (result) {
            if (result.success) {
                $(tagNode).parent().parent().remove();
                alert("删除成功，(缓存影响最长可能需要1个小时生效)");
            } else {
                alert(result.info);
            }
        });
    }
    //初始化显示模块
    function initBlackData(blacklistType) {
        schoolModel.hide();
        studentModel.hide();
        parentModel.hide();
        whiteModel.hide();
        payLimitWhiteModel.hide();
        graySchoolModel.hide();
        grayUsersModel.hide();
        $("#" + blacklistType).show();
    }

    function addTagValueToBlacklist() {
        $.post("addTagValue.vpage", {
                    blackType: $("#addBlacklistType").val(),
                    tagValue: $("#tagValue").val(),
                    tagComment: $("#tagComment").val()
                },
                function (result) {
                    if (result.success) {
                        alert("增加成功(缓存影响最长可能需要1个小时生效)");
                        location.reload();
                    } else {
                        alert(result.info);
                    }

                });

    }
    $("#schoolBlackList").tablesorter();
    $("#schoolBlackList").tablesorter({headers: {1: {sorter: "num"}}});
    $("#schoolBlackList").tablesorter({headers:{5:{sorter:false},6:{sorter:false}}});
    $(function () {
        initBlackData($("#blacklistType").val());

        $(".tag-delete").on('click', function () {
            deleteTag($(this).attr("data-tagId"), $(this));
        });

        $("#blacklistType").change(function () {
            initBlackData($(this).val());
        });

        $("#addBlacklistBtn").on("click", function () {
            addTagValueToBlacklist();
        });
    });
</script>
</@layout_default.page>