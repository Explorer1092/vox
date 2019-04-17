<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='部门' page_num=5>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-user"></i> 检查</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>

        <div class="box-content row-fluid">
            <div id="loadingDiv" style="text-align: center;">
                <img src="/public/images/loading.gif" alt="正在加载……">
                <p style="margin-top: -25px;font-size: 18px;">正在加载……</p>
            </div>
            <div class="span6">
                <div id="targetData"></div>
                <div id="noneGroupData"></div>
            </div>
            <div class="span5">
                <div class="row-fluid">
                    <div class="span12">
                        <div id="invalidUserSchoolData"></div>
                        <div id="rightGroupData"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<#--调整部门弹窗-->
<div id="userUpdateDep_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">选择新部门及角色</h4>
            </div>
            <div class="modal-body">
                <div class="control-group">
                    <div class="row-fluid">
                        <div class="span2">
                            <label for="">新部门:</label>
                        </div>
                        <div id="useUpdateDep_con_dialog" class="span10"></div>
                    </div>
                </div>
                <div class="control-group">
                    <div class="row-fluid">
                        <div class="span2">
                            <label for="">新角色:</label>
                        </div>
                        <div id="newRoleListSelect" class="span10"></div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="updateDepSubmitBtn" type="button" class="btn btn-large btn-primary">确定</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>
<#--调整部门模板-->
<script id="updateDepDialogTemp" type="text/x-handlebars-template">
    <select name="newRoleName_dialog" id="newRoleName_dialog">
        <option value="0">请选择</option>
        {{#each roleList}}
        <option value="{{uroleId}}">{{uroleName}}</option>
        {{/each}}
    </select>
</script>
<#--no group user table-->
<script id="noGroupTemp" type="text/x-handlebars-template">
    <h3>未分配部门的用户</h3>
    <table class="table table-bordered table-striped">
        <thead>
        <tr>
            <th>序号</th>
            <th>账号</th>
            <th>姓名</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody>
        {{#each emptyUserList}}
        <tr>
            <td>{{addOne @index}}</td>
            <td>{{accountName}}</td>
            <td>{{realName}}</td>
            <td>
                <button class="btn btn-mini btn-primary js-deployDepartBtn" type="button" data-uid="{{id}}">分配部门</button>
                <button class="btn btn-mini btn-danger js-closeAccountBtn" type="button" data-uid="{{id}}">关闭账号</button>
            </td>
        </tr>
        {{else}}
        <tr>
            <td colspan="5" style="text-align: center;">暂无</td>
        </tr>
        {{/each}}
        </tbody>
    </table>
</script>
<#--大区目标与分区之和不一致或分区目标小于专员之和-->
<script id="targetTemp" type="text/x-handlebars-template">
    <h3>大区目标与分区之和不一致或分区目标小于专员之和</h3>
    <table class="table table-bordered table-striped">
        <thead>
        <tr>
            <th>序号</th>
            <th>描述</th>
        </tr>
        </thead>
        <tbody>
        {{#each checkAgentPerformanceGoalResult}}
        <tr>
            <td>{{addOne @index}}</td>
            <td>({{this}})</td>
        </tr>
        {{else}}
        <tr>
            <td colspan="5" style="text-align: center;">暂无</td>
        </tr>
        {{/each}}
        </tbody>
    </table>
</script>
<#--invalidUserSchoolTemp table-->
<script id="invalidUserSchoolTemp" type="text/x-handlebars-template">
    <h3>无效的用户学校列表</h3>
    <table class="table table-bordered table-striped">
        <thead>
        <tr>
            <th>序号</th>
            <th>姓名</th>
            <th>学校</th>
            <th>备注</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody>
        {{#each invalidUserSchoolList}}
        <tr>
            <td>{{addOne @index}}</td>
            <td>{{userName}}({{userId}})</td>
            <td>{{schoolName}}({{schoolId}})</td>
            <td>{{comment}}</td>
            <td>
                <button class="btn btn-mini btn-danger js-delBtn" type="button" data-uid="{{userId}}" data-sid="{{schoolId}}">删除</button>
            </td>
        </tr>
        {{else}}
        <tr>
            <td colspan="5" style="text-align: center;">暂无</td>
        </tr>
        {{/each}}
        </tbody>
    </table>
</script>

<#--right table-->
<script id="rightGroupTemp" type="text/x-handlebars-template">
    <h3>权限问题</h3>
    <table class="table table-bordered table-striped">
        <thead>
        <tr>
            <th>序号</th>
            <th>描述</th>
        </tr>
        </thead>
        <tbody>
        {{#each checkGroupResultList}}
        <tr>
            <td>{{addOne @index}}</td>
            <td>{{this}}</td>
        </tr>
        {{else}}
        <tr>
            <td colspan="5" style="text-align: center;">暂无</td>
        </tr>
        {{/each}}
        </tbody>
    </table>
</script>
<script type="text/javascript">
    $(function(){

        var userId = 0;
        //初始化窗口
        $(".span2.main-menu-span").hide();
        $("#content").removeClass("span10").addClass("span12").css("marginLeft",0);

        //渲染模板
        var renderPage = function(tempSelector,data,container){
            var source   = $(tempSelector).html();
            var template = Handlebars.compile(source);

            $(container).html(template(data));
        };

        //注册索引加一的helper
        Handlebars.registerHelper("addOne",function(index){
            //返回+1之后的结果
            return index+1;
        });

        //注册比较helper
        Handlebars.registerHelper('compare', function(left, operator, right, options) {
            if (arguments.length < 3) {
                throw new Error('Handlerbars Helper "compare" needs 2 parameters');
            }
            var operators = {
                '==':     function(l, r) {return l == r; },
                '===':    function(l, r) {return l === r; },
                '!=':     function(l, r) {return l != r; },
                '!==':    function(l, r) {return l !== r; },
                '<':      function(l, r) {return l < r; },
                '>':      function(l, r) {return l > r; },
                '<=':     function(l, r) {return l <= r; },
                '>=':     function(l, r) {return l >= r; },
                'typeof': function(l, r) {return typeof l == r; }
            };

            if (!operators[operator]) {
                throw new Error('Handlerbars Helper "compare" doesn\'t know the operator ' + operator);
            }

            var result = operators[operator](left, right);

            if (result) {
                return options.fn(this);
            } else {
                return options.inverse(this);
            }
        });

        var loadPage = function(){
            $.get("checkGroupData.vpage",function(res){
                if(res.success){
                    renderPage("#noGroupTemp",res,"#noneGroupData");
                    if(res.checkAgentPerformanceGoalResult){
                        renderPage("#targetTemp",res,"#targetData");
                    }
                    renderPage("#rightGroupTemp",res,"#rightGroupData");
                    renderPage("#invalidUserSchoolTemp",res,"#invalidUserSchoolData");
                    $("#loadingDiv").hide();
                }else{
                    alert(res.info);
                }
            });
        };

        loadPage();


        //分配部门
        $(document).on("click",".js-deployDepartBtn",function(){
            userId = $(this).data('uid');
            var getTheRoleList =  function(event,tree) {
                var node = tree.node;
                var key = node.key;
                if(tree.targetType != "expander") {
                    if (node.children && node.children.length != 0) {
                        node.setExpanded(true);
                    }
                    $.get("/user/orgconfig/getGroupRoleList.vpage?groupId="+key,function(res){
                        if(res.success){
                            renderPage("#updateDepDialogTemp",res,"#newRoleListSelect");
                        }else{
                            alert(res.info);
                        }
                    });
                }
            };

            $("#userUpdateDep_dialog").modal('show');
            $("#useUpdateDep_con_dialog").fancytree("destroy");
            $("#newRoleName_dialog").remove();
            $("#useUpdateDep_con_dialog").fancytree({
                source: {
                    url: "/user/orgconfig/getNewDepartmentTree.vpage",
                    cache:true
                },
                checkbox: false,
                autoCollapse:true,
                click: getTheRoleList,
                selectMode: 1
            });
        });

        //提交调整部门和角色
        $(document).on("click","#updateDepSubmitBtn",function(){
            var newGid,roleId;

            var tree = $("#useUpdateDep_con_dialog").fancytree("getTree");
            if(tree.getActiveNode()){
                newGid = tree.getActiveNode().key;
                roleId = $("#newRoleName_dialog").val();
                if(roleId != 0){
                    $.post("addGroupUser.vpage",{
                        groupId:newGid,
                        roleId:roleId,
                        userId:userId
                    },function(res){
                        if(res.success){
                            alert("分配部门和角色成功");
                            userId = 0;
                            $("#userUpdateDep_dialog").modal("hide");

                            loadPage();
                        }else{
                            alert(res.info);
                        }
                    })
                }else{
                    alert("请选择新角色");
                    return false;
                }
            }else{
                alert("请选择新部门");
                return false;
            }

        });


        //关闭账号
        $(document).on("click",".js-closeAccountBtn",function(){
            if(confirm("你确定要关闭该账号？")){
                var uid = $(this).data("uid");
                $.post("/user/orgconfig/closeUserAccount.vpage",{agentUserId:uid},function(res){
                    if(res.success){
                        alert("关闭该账号成功");
                        loadPage();
                    }else{
                        alert(res.info);
                    }
                });
            }
        });

        $(document).on("click",".js-delBtn",function(){
            var sid = $(this).data("sid");
            var uid = $(this).data("uid");
            var _self = this;
            $.post("removeAgentAccountSchool.vpage",{
                agentUserId:uid,
                schoolIds:sid
            },function(res){
                if(res.success){
                    $($(_self).parents("tr")).remove();
                }else{
                    alert(res.info);
                }
            });
        });
    });
</script>
</@layout_default.page>
