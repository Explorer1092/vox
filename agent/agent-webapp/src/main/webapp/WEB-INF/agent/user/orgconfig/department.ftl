<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='部门' page_num=5>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-user"></i> 部门管理</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>

        <div class="box-content row-fluid">
            <div class="span3">
                <div>
                    <input type="search" maxlength="20" placeholder="搜索用户" id="dpTreeSearch">
                </div>
                <div id="departmentPeopleTree"></div>


            </div>
            <div class="span8">
                <div class="detailHeaderContainer">

                </div>
                <div class="detailContentContainer">

                </div>

            </div>
        </div>
    </div>
    <!--/span-->

</div>

<#--页面弹窗-->
<#include "departmentdialog.ftl">

<#--页面模板-->
<#include "departmentTemp.ftl">
<div id="apply_history01" class="modal hide fade" style="position:absolute;width:45%;margin-left:-25%;">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>物料预算调整记录</h3>
    </div>
    <div class="modal-body01" ></div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">关 闭</button>
    </div>
</div>

<script type="text/html" id="alertBox01">
        <div id="evaluate_table">
            <table>
                <thead>
                <tr>
                    <th class="sorting" style="width: 100px;">日期</th>
                    <th class="sorting" style="width: 100px;">操作人</th>
                    <th class="sorting" style="width: 100px;">调整金额</th>
                    <th class="sorting" style="width: 150px;">备注</th>
                    <th class="sorting" style="width: 100px;">调整前</th>
                    <th class="sorting" style="width: 100px;">调整后</th>
                </tr>
                </thead>
                <tbody>
                <%if(budgetChangeRecords && budgetChangeRecords.length>0){%>

                    <%for(var i = 0 ; i < budgetChangeRecords.length ; i++){%>
                <tr style="padding:5px 0">
                    <%var data = budgetChangeRecords[i]%>
                    <th><%=data.createTime%></th>
                    <th><%=data.operatorName%></th>
                    <th>
                        <%if(data.afterCash >= data.preCash){%>+<%}else{%>-<%}%><%=data.quantity%>
                    </th>
                    <th><%=data.comment%></th>
                    <th><%=data.preCash%></th>
                    <th><%=data.afterCash%></th>
                </tr>
                    <%}%>

                <%}%>
                </tbody>
            </table>
        </div>
</script>


<div id="apply_history02" class="modal hide fade" style="position:absolute;width:45%;margin-left:-25%;">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>余额变动记录</h3>
    </div>
    <div class="modal-body02" ></div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">关 闭</button>
    </div>
</div>

<script type="text/html" id="alertBox02">
    <div id="evaluate_table">
        <table>
            <thead>
            <tr>
                <th class="sorting" style="width: 100px;">日期</th>
                <th class="sorting" style="width: 100px;">操作人</th>
                <th class="sorting" style="width: 100px;">变动金额</th>
                <th class="sorting" style="width: 250px;">备注</th>
                <th class="sorting" style="width: 100px;">调整前</th>
                <th class="sorting" style="width: 100px;">调整后</th>
            </tr>
            </thead>
            <tbody>
            <%if(balanceChangeRecords && balanceChangeRecords.length>0){%>

                <%for(var i = 0 ; i < balanceChangeRecords.length ; i++){%>
            <tr style="padding:5px 0">

                <%var data = balanceChangeRecords[i]%>
                <th><%=data.createTime%></th>
                <th><%=data.operatorName%></th>
                <th>
                    <%if(data.afterCash >= data.preCash){%>+<%}else{%>-<%}%><%=data.quantity%>
                </th>
                <th><%=data.comment%></th>
                <th><%=data.preCash%></th>
                <th><%=data.afterCash%></th>

                <%}%>
            </tr>
            <%}%>
            </tbody>
        </table>
    </div>
</script>
<script type="text/javascript">
    var serviceTypeList = {};
    template.helper('Date', Date);
    function ChangeDateFormat(jsondate) {
        var date = new Date(parseInt(jsondate, 10));
        var month = date.getMonth() + 1 < 10 ? "0" + (date.getMonth() + 1) : date.getMonth() + 1;
        var currentDate = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();

        return date.getFullYear()+ "年"+ month+ "月"+ currentDate+ "日";
    }

    $(document).on('click','.type1',function(){
        var UserId = $('#userDetailId').val();
//        $('.modal-body01').html("");
        $("#apply_history01").modal('show');
    });
    $(document).on('click','.type2',function(){
        $("#apply_history02").modal('show');
    });
    $(function(){
        $(document).on("click","#cityBudgetInfoBtn",function () {
            window.location.href = "cityBudgetInfo.vpage?groupId="+$(this).data("id");
        });
        //初始化窗口
        $(".span2.main-menu-span").hide();
        $("#content").removeClass("span10").addClass("span12").css("marginLeft",0);

        //初始化form验证flag
        var validateFlag = true;
        var currentKey,currentParentKey,reFreshType;

        var region_icon = ""; //初始化大区徽章
        var region_icon_show = false; //初始化大区节点可见徽章

        //点击部门树
        var clickDepartmentTree = function(event,tree){
            var node = tree.node;
            var key = node.key;
            var type = node.data.type;

            if(tree.targetType != "expander"){
                if(node.children && node.children.length != 0){
                    node.setExpanded(true);
                }
                if(type == "user"){
                    var parentGroupKey = node.parent.key;
                    renderUserContent(key,parentGroupKey);
                }else if(type == "group"){
                    renderGroupContent(key);
                }else{
                    alert("未知类型的数据");
                }

            }
        };

        var initDepartTree = function(){
            //初始化树
            $("#departmentPeopleTree").fancytree({
                extensions: ["filter"],
                source: {
                    url: "/user/orgconfig/groupTree.vpage",
                    cache:true
                },
                checkbox: false,
                selectMode: 1,
                click:clickDepartmentTree,
                autoCollapse:true,
                focusOnSelect:true,
                icon:false,
                init:function(node,tree){
                    var tree = $("#departmentPeopleTree").fancytree("getTree");
                    tree.visit(function(node){
                        var nodeTitle = node.title;
                        var type = node.data.type;
                        node.icon = false;
                        if(type == "user"){
                            node.setTitle('<i class="icon-user"></i>'+nodeTitle);
                        }else if(type == "group"){
                            node.setTitle('<i class="icon-home"></i>'+nodeTitle);
                        }
                    });
                },
                loadChildren:function(){
                    console.log(currentKey);
                    if(currentKey){
                        var tree = $("#departmentPeopleTree").fancytree("getTree");
                        tree.visit(function(node){
                            var type = node.data.type;
                            if(node.key == currentKey && type == reFreshType){
                                node.setExpanded(true);
                                node.setActive(true);
                                if(type == "user"){
                                    renderUserContent(node.key,currentParentKey);
                                }else if(type == "group"){
                                    renderGroupContent(node.key);
                                }
                                currentKey = currentParentKey = reFreshType = null;
                            }
                        });
                    }
                }

            });
        };

        initDepartTree();

        var groupRoleList = [];//部门级别列表
        //渲染用户详情
        var renderUserContent = function(key,parentKey){
            //用户时渲染页面
            $.post("/user/orgconfig/userAccountsDetail.vpage",{agentUserId:key,agentGroupId:parentKey},function(res){
                if(res.success){
                    groupRoleList = res.groupRoleList;
                    renderDepartment("#userDetailHeaderTemp",res,".detailHeaderContainer");
                    renderDepartment("#userDetailContentTemp",res,".detailContentContainer");
                    if(res.budgetChangeRecords && res.budgetChangeRecords.length > 0){
                        for(var i=0;i < res.budgetChangeRecords.length;i++){
                            res.budgetChangeRecords[i].createTime = new Date(res.budgetChangeRecords[i].createTime).Format("yyyy-MM-dd");
                        }
                    }
                    if(res.balanceChangeRecords && res.balanceChangeRecords.length > 0){
                        for(var j=0;j < res.balanceChangeRecords.length;j++){
                            res.balanceChangeRecords[j].createTime = new Date(res.balanceChangeRecords[j].createTime).Format("yyyy-MM-dd");
                        }
                    }
                    var main_html01 = template('alertBox01', {budgetChangeRecords:res.budgetChangeRecords});
                    $('.modal-body01').html(main_html01);
                    var main_html02 = template('alertBox02', {balanceChangeRecords:res.balanceChangeRecords});
                    $('.modal-body02').html(main_html02);
                    if(res.thisUserIsManager){
                        renderDepartment("#regionTableTemp",res,"#userDetailRegionTable");
                    }else{
                        renderDepartment("#dialogSchoolTemp",{theUserIsManageAble:res.theUserIsManageAble,schoolData:res.agentGroupSchoolInfo},"#userDetailRegionTable");
                    }
                }
            });
        };
        var groupRoleName = "";
        //渲染部门详情
        var renderGroupContent = function(key){
            window.currentGroupId = key;
            $.post("/user/orgconfig/departmentDetail.vpage",{agentGroupId:key},function(res){
                if(res.success){
                    groupRoleList = res.groupRoleList;
                    serviceTypeList = res.serviceTypeList;
                    /*大区徽章,修改信息并没请求接口,这里用全局*/
                    if(res.groupRole && res.groupRole == "Region"){
                        region_icon = res.logo;
                        region_icon_show = true;
                        res['regionLogo'] = true;
                        res['hasImage'] = false;
                        var imageTail = '?x-oss-process=image/resize,w_180,h_180/auto-orient,1';


                        if(res.logo){
                            res['hasImage'] = true;
                            res['logoUrl'] = res.logo + imageTail;
                        }else{
                            res['hasImage'] = false;
                        }
                    }else{
                        region_icon_show = false;
                        res['regionLogo'] = false;
                    }

                    renderDepartment("#departmentDetailHeaderTemp",res,".detailHeaderContainer");
                    renderDepartment("#departmentDetailContentTemp",res,".detailContentContainer");
                    renderDepartment("#regionTableTemp",res,".js-regionAreaDiv");
                    /*if(res.responsibleGeneral){
                        renderDepartment("#responsibleGeneralTemp",res,".responsible-general-table");
                    }*/
                    //业绩目标渲染
                    groupRoleName = res.groupRole;
                    if(res.groupRole != "Country"){
                        var thisMonth = new Date().Format("yyyyMM");
                        renderPerformance(key,thisMonth);
                    }
                }
            });
        };


        //业绩目标渲染，monthType：1，本月目标；2，下月目标
        var renderPerformance=function (groupId,monthDate) {
            var params = {groupId:groupId,month:monthDate};
            $.post("/kpi/budget/budget_data_list.vpage",params,function(res){
                if(res.success){
                    res.groupRoleName = groupRoleName;
                    res.monthType = new Date().Format("yyyyMM");
                    if(res.monthType == res.groupBudget.month){
                        res.month = 1;
                    }else{
                        res.month = 2;
                    }
                    //业绩目标渲染
                    if(res){
                        $('.agentPerformanceGoalTemp-general-table').html(template("agentPerformanceGoalTemp",{res:res}));
                        var budgetNum0 = 0;
                        var budgetNum1 = 0;
                        var budgetNum2 = 0;
                        var budgetNum3 = 0;
                        for(var i=0;i<$(".budget_0").length;i++){
                            budgetNum0 += (($(".budget_0").eq(i).html()||0) - 0);
                            budgetNum1 += (($(".budget_1").eq(i).html()||0) - 0);
                            budgetNum2 += (($(".budget_2").eq(i).html()||0) - 0);
                            budgetNum3 += (($(".budget_3").eq(i).html()||0) - 0);
                        }
                        $('.budget_sum_0').html(Math.floor(($('.budgetSummary_0').html()||0) - budgetNum0));
                        $('.budget_sum_1').html(Math.floor(($('.budgetSummary_1').html()||0) - budgetNum1));
                        $('.budget_sum_2').html(Math.floor(($('.budgetSummary_2').html()||0) - budgetNum2));
                        $('.budget_sum_3').html(Math.floor(($('.budgetSummary_3').html()||0) - budgetNum3));
                    }
                }
            });
        }

        window.chooseMonthType = function (monthType) {
            if(monthType == 1){
                monthType = new Date().Format("yyyyMM")
            }else{
                var date = new Date().Format("yyyy-MM-dd")
                var arr = date.split('-');
                var year = arr[0]; //获取当前日期的年份
                var month = arr[1]; //获取当前日期的月份
                var day = arr[2]; //获取当前日期的日
                var days = new Date(year, month, 0);
                days = days.getDate(); //获取当前日期中的月的天数
                var year2 = year;
                var month2 = parseInt(month) + 1;
                if (month2 == 13) {
                    year2 = parseInt(year2) + 1;
                    month2 = 1;
                }
                var day2 = day;
                var days2 = new Date(year2, month2, 0);
                days2 = days2.getDate();
                if (day2 > days2) {
                    day2 = days2;
                }
                if (month2 < 10) {
                    month2 = '0' + month2;
                }

                var t2 = year2 +""+ month2;
                monthType = t2.trim();
            }
            renderPerformance(window.currentGroupId,monthType);
        }
        //渲染模板
        var renderDepartment = function(tempSelector,data,container){
            var source   = $(tempSelector).html();
            var template = Handlebars.compile(source);

            $(container).html(template(data));
        };

        //初始化时间控件
        var initDataPicker = function(startSec,endSec){

            $(startSec).datepicker({
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

            $(endSec).datepicker({
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

        };

        //每次选择地区获取已选择的数据
        var getAreaFunc = function(item,arr){
            item.forEach(function(i){
                if(i.selected){
                    arr.push({data:i.data,cityName:i.title,key:i.key})
                } else if(i.children && i.children.length>0){
                    getAreaFunc(i.children,arr)
                }
            })
        };

        //设置状态 显示数据用
        var setFlagFunc = function (item) {
            item.forEach(function(i){
                if(i.children && i.children.length>0){
                    var num = 0,len = i.children.length;
                    for(var j=0;j<len;j++){
                        if(i.children[num].selected){
                            num++
                        }
                    }
                    if(num == len){
                        i.selected = true;
                    }
                    setFlagFunc(i.children)
                }
                i.selectFlag = i.selected;
            })
        };

        //显示右边数据
        var viewDepartment = function () {
            var subDialogTree = $("#dialogAreaTree");
            var selectTree = subDialogTree.fancytree("getTree").rootNode.children;
            selectArr = [];
            getAreaFunc(selectTree,selectArr);
            renderDepartment("#dialogAreaSchoolTemp",{groupRegionData:selectArr},"#dialogAreaSchool");
        };

        var selectArr = [];
        //初始化负责区域dialog数据
        var initSubDialogData = function(gid){
            var  subDialogTree = $("#dialogAreaTree");
            renderDepartment("#dialogAreaSchoolTemp",{groupRegionData:[]},"#dialogAreaSchool");//清空右边数据
            $.get("/user/orgconfig/getDepartmentRange.vpage?agentGroupId="+gid,function (res) {
                setFlagFunc(res);
                subDialogTree.fancytree("destroy");
                subDialogTree.fancytree({
                    extensions: ["filter"],
                    source: res,
                    checkbox: true,
                    selectMode: 3,
                    autoCollapse:true,
                    select:function () {
                        viewDepartment();
                    },
                    init:function(){
                        var tree = $("#dialogAreaTree").fancytree("getTree");
                        tree.visit(function(node){
                            if(node.data.selectFlag){
                                node.setSelected(true);
                            }
                        });
                    }
                });
                viewDepartment();
            });
        };

        //验证添加数据
        var validateAddFormData = function(){
            validateFlag = true;
            //input值
            var formInputs = $("#addDepartmentForm").find('input.js-needed');

            $.each(formInputs,function(i,item){
                if($(item).val() == ""){
                    validateFlag = false;
                    alert($(item).data("needinfo"));
                    return false;
                }
            });

            //验证角色
            var roleType = $("#role_name").val();
            if(roleType == 0 ){
                validateFlag = false;
                alert("请选择角色");
            }

            //合同日期验证
            var contractStartDate = $("#contract_start_date").val();
            var contractEndDate = $("#contract_end_date").val();
            if(contractEndDate != ""){
                if(contractStartDate > contractEndDate){
                    validateFlag = false;
                    alert("合同结束日期不能晚于开始日期");
                }
            }
            return validateFlag;
        };

        var refreshPage = function(){
            $("#departmentPeopleTree").fancytree("destroy");
            initDepartTree();
        };

        //初始化选择学校数据
        var initSelectSchoolData = function(uid,gid){
            renderDepartment("#chooseSchoolTableTemp","","#schoolTable");
        };

        //逗号分隔验证
        var valiteSplitByicon = function(str){
            var flag = false;
            var ex = /[0-9]+(,[0-9]+)*/g;
            var strWords = str.replace(/\s/g,"");
            if(strWords == strWords.match(ex)[0]){
                flag = true
            }
            return flag;
        };

        //注册索引加一的helper
        Handlebars.registerHelper("addOne",function(index){
           //返回+1之后的结果
           return index+1;
        });

        //注册中小学类型helper
        Handlebars.registerHelper("schoolType",function(index){
           if(index == 1){
               return "小学";
           }else if(index == 2){
               return "初中";
           }else if(index == 4){
               return "高中";
           } else if (index == 5) {
               return "学前";
           }
        });

        //包含千分位的预算表达的helper
        Handlebars.registerHelper("money",function(number){
            if(number){
                return (number + '').replace(/\d{1,3}(?=(\d{3})+(\.\d*)?$)/g, '$&,')+"元"
            }else{
                return "0元"
            }
        });

        //注册比较helper
        Handlebars.registerHelper('compare', function(left, operator, right, options) {
            if (arguments.length < 3) {
                throw new Error('Handlerbars Helper "compare" needs 2 parameters');
            }
            var operators = {
                '==':     function(l, r) {return l == r; },
                '===':    function(l, r) {return l === r; },
                '||':    function(l, r) {return l || r; },
                '&&':    function(l, r) {return l && r; },
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

        Handlebars.registerHelper("compare2",function(x1,x2,options){
            if(x1 == x2){
                return options.fn(this);
            }else{
                return options.inverse(this);
            }
        });

        /*添加用户*/
        $(document).on("click","#addDepBtn",function(){
            var groupId = $(this).data("dpid");
            $.post("/user/orgconfig/userRegistration.vpage",{agentGroupId:groupId},function(res){
                if(res.success){
                    renderDepartment("#addDepartmentUserTemp",res,".detailContentContainer");

                    initDataPicker("#contract_start_date","#contract_end_date");
                }else{

                }
            });
        });
        /**导入业绩*/
        $(document).on("click","#importAgentPerformanceGoalBtn",function(){
            renderDepartment("#importAgentPerformanceGoalTemp",null,".detailContentContainer");
        });

        /**导出业绩*/
        $(document).on("click","#exportAgentPerformanceGoalBtn",function(){
            var groupId = $(this).data("dpid");
            renderDepartment("#exportAgentPerformanceGoalTemp",{groupId:groupId},".detailContentContainer");
            $("#month").datepicker({
                dateFormat: 'yymm',  //日期格式，自己设置
                monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
                defaultDate: new Date(),
                prevText: '上月',         // 前选按钮提示
                nextText: '下月',
                showButtonPanel: true,        // 显示按钮面板
                minDate: new Date('2018-01-01'),
                showMonthAfterYear: true,
                currentText: "本月",  // 当前日期按钮提示文字
                closeText: "关闭",
                numberOfMonths: 1,
                changeMonth: true,
                changeYear: true,
                onSelect: function (selectedDate) {
                },
                onClose: function (dateText, inst) {// 关闭事件
                    var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
                    var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
                    $(this).datepicker('setDate', new Date(year, month, 1));
                }
            });
        });



        /*用户头像*/
        $(document).on("change","#fileUnUse",function(){

            if ($("#fileUnUse").val() != '') {
                var formData = new FormData();
                var file = $('#fileUnUse')[0].files[0];
                formData.append('file', file);
                formData.append('file_size', file.size);
                formData.append('file_type', file.type);
                $.ajax({
                    url: '/file/upload.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            $("#perviewIcon").html("");
                            $("#perviewIcon").append("<img src='"+data.fileUrl+"?x-oss-process=image/resize,w_100,h_100/auto-orient,1'/>");
                            alert("上传成功");
                        } else {
                            alert("上传失败");
                        }
                    }
                });
            }
        });

        $(document).on("click", "#update_usable_cash_amount", function () {
            var formInputs = $("#updateUsableCashAmountForm").find('input');
            var postData = {};
            $.each(formInputs, function (i, item) {
                if (item.name) {
                    postData[item.name] = item.value;
                }
            });
            var uid = $("#updateUsableCashAmountBtn").data("uid");
            var pid = $("#updateUsableCashAmountBtn").data("gpid");
            postData["usableCashAmountOpt"] = $("#usableCashAmountOpt").val();
            postData["userId"] = uid;
            postData["pid"] = pid;
            if (postData["usableCashAmount"] == "") {
                alert("调整金额为空");
                return;
            }
            if (postData["usableCashAmountCause"] == "") {
                alert("调整原因不能为空")
            }
            $.post("/user/orgconfig/update_usable_cash_amount.vpage", postData, function (res) {
                if (res.success) {
                    currentKey = res.userId;
                    currentParentKey = res.parentId;
                    reFreshType = "user";
                    refreshPage();
                } else {
                    alert(res.info);
                }
            })
        });

        //添加用户提交
        $(document).on("click","#add_sys_user_btn",function(){
            if(validateAddFormData()){
                var formInputs = $("#addDepartmentForm").find('input');
                var postData = {};
                $.each(formInputs,function(i,item){
                    if(item.name){
                        postData[item.name] = item.value;
                    }
                });

                var roleType = $("#role_name").val();
                var desc = $("#introDesc").val();
                postData["roleType"] = roleType;
                postData["userComment"] = desc;
                if($("#perviewIcon>img").attr('src')){
                    postData["avatar"] = $("#perviewIcon>img").attr('src').split("?")[0];
                }else{
                    postData["avatar"] = "";
                }

                //物料预算
                postData["budgetOpt"] = $("#budgetOpt").val();
                var budgetVal = $("#materielBudget").val();
                if(!budgetVal){
                    postData["materielBudget"] = 0;
                }else{
                    postData["materielBudget"] = budgetVal;
                    var adjust_cause = $("#adjust_cause").val();
                    if(adjust_cause && adjust_cause.length <100){
                        postData["adjust_cause"] = adjust_cause;
                    }else{
                        alert("物料预算调整原因必填!");
                        return;
                    }
                }
                // 是否开通蜂巢账号
                postData["bindHoneycomb"] = $('input[name="bindHoneycomb"]:checked').val();
                $.post('/user/orgconfig/addUserAccounts.vpage',postData,function (res){
                    if(res.success){
                        alert("添加用户成功");
                        currentKey = res.userId;
                        currentParentKey = res.parentId;
                        reFreshType = "user";
                        refreshPage();
                    }else{
                        alert(res.info);
                    }
                });
            }
        });

        //添加子部门
        $(document).on("click","#addSubDepBtn",function(){
            var gpid = $(this).data("dpid");
            $.get("/user/orgconfig/entryDepartmentInfo.vpage?agentGroupId="+gpid,function(res){
                if(res.success){
                    res['sub_department_region_icon_show'] = false;

                    if(res.pGroupRole == "Country"){
                        res['sub_department_region_icon_show'] = true;
                    }

                    renderDepartment("#addSubDepartmentTemp",res,".detailContentContainer");

                }else{
                    alert(res.info);
                }
            });
        });
        $(document).on('change','.js-groupLevelChoice',function () {
            $('input[name="businessType"]').prop('checked',false);
        });
        $(document).on("change","#sub_department_region_icon",function(){

            if ($("#sub_department_region_icon").val() != '') {
                var formData = new FormData();
                var file = $('#sub_department_region_icon')[0].files[0];
                if(file.size <= 10*1024*1024 && file.type.indexOf('image') != -1){
                    formData.append('file', file);
                    formData.append('file_size', file.size);
                    formData.append('file_type', file.type);
                    $.ajax({
                        url: '/file/upload.vpage',
                        type: 'POST',
                        data: formData,
                        processData: false,
                        contentType: false,
                        success: function (data) {
                            if (data.success) {
                                var imageTail = '?x-oss-process=image/resize,w_180,h_180/auto-orient,1';
                                $("#sub_department_region_icon_perview").html("<img src='"+data.fileUrl+imageTail+"'/>");
                                alert("上传成功");
                            } else {
                                alert(date.info);
                            }
                        }
                    });
                }else{
                    alert("请上传小于10M的图片");
                }
            }
        });

        //创建子部门
        $(document).on("click","#createSubDepartmentBtn",function(){
            var pdpId = $("#parentDepartment_id").val();
            var postData = {};
            var subFormFlag = true;
            $.each($(".js-subPostData"),function(i,item){
                if(item.value == "" && $(item).data("alertinfo")){
                    subFormFlag = false;
                    alert($(item).data("alertinfo"));
                    return false;
                }else{
                    postData[item.name] = item.value;
                }
            });
            var arr = [];
            if($('input[name="businessType"]:checked').length == 0){
                alert('业务类型为必选项');
                subFormFlag = false;
            }
            $.each($('input[name="businessType"]:checked'),function (i,item){
                arr.push(item.value);
            });
            postData['serviceTypeStr'] = arr.toString();
            if($("#sub_department_region_icon_perview>img").attr('src')){
                postData["logo"] = $("#sub_department_region_icon_perview>img").attr('src').split("?")[0];
            }else{
                postData["logo"] = "";
            }

            if(subFormFlag){
                $.post("/user/orgconfig/addSubDepartment.vpage",postData,function(res){
                    if(res.success){
                        alert("添加子部门成功");
                        currentKey = res.groupId;
                        reFreshType = "group";
                        refreshPage();
                    }else{
                        alert(res.info);
                    }
                });
            }
        });

        //删除部门
        $(document).on("click","#delDepBtn",function(){
            var groupId = $(this).data("dpid");
            if (confirm("确定删除该部门？")==true){
                $.post("/user/orgconfig/removeDepartment.vpage",{agentGroupId:groupId},function(res){
                    if(res.success){
                        alert("删除部门成功");
                        location.reload();
                    }else{
                        alert(res.info);
                    }
                });
            }
        });

        //搜索树中用户
        $(document).on("keyup","#dpTreeSearch",function(e){
            var userName = $(this).val().trim("");
            var tree = $("#departmentPeopleTree").fancytree("getTree");
            if(!blankString(userName)){
                tree.applyFilter(userName);
            }else{
                tree.clearFilter();
            }
        });

        //学校单项删除
        $(document).on("click",".js-delSchoolItemBtn",function(){
            var sid = $(this).data("sid");
            var sname = $(this).data("sname");
            var uid = $("#userDetailId").val();
            var _self = this;
            if (confirm("是否确认删除" + "【" + sname + "(" + sid + ")】？")) {
                $.post("removeAgentAccountSchool.vpage", {
                    agentUserId: uid,
                    schoolIds: sid
                }, function (res) {
                    if (res.success) {
                        alert("删除成功");
                        $($(_self).parents("tr")).remove();
                    } else {
                        alert(res.info);
                    }
                });
            }
        });

        //检测手机格式
        $(document).on("keyup","#phone_no",function(){
            var phoneNo = $(this).val();
            var type = $(this).data("type");

            if(type != "edit"){
                $.post("/user/orgconfig/mobileRechecking.vpage",{tel:phoneNo},function(res){
                   if(!res.success){
                       $(".js-telInfo").html(res.info).show();
                       validateFlag = false;
                    }else{
                       $(".js-telInfo").hide();
                       validateFlag = true;
                   }
                });
            }else{
                if(phoneNo.length != 11){
                    validateFlag = false;
                }
            }
        });

        //检测账号合法性
        $(document).on("keyup","#account_name",function(){
            var account_name = $(this).val();

            $.post("/user/orgconfig/accountNameRechecking.vpage",{accountName:account_name},function(res){
               if(!res.success){
                   $(".jjs-accountInfo").html(res.info).show();
                   validateFlag = false;
                }else{
                   $(".js-accountInfo").hide();
                   validateFlag = true;
               }
            });
        });

        //调整负责区域
        $(document).on("click","#updateAreaBtn",function(){
            var groupId = $(this).data("dpid");
            initSubDialogData(groupId);
            $("#addDepartment_dialog").modal('show');

        });

        //部门修改信息
        $(document).on("click","#editInfoDepBtn",function(){
            var groupId = $(this).data("dpid");
            var gName = $(this).data("gname");
            var gDesc = $(this).data("gdesc");
            var gRoleId = $(this).data("roleid");
            var icon_link = "";
            if(region_icon){
                icon_link = region_icon+'?x-oss-process=image/resize,w_180,h_180/auto-orient,1';
            }
            //把gRoleId赋值给每一项 用于比较是否相等 选中部门级别
            groupRoleList.filter(function (item) {
                return item.gRoleId = gRoleId;
            });
            renderDepartment("#editInfoDialogTemp",{
                groupId:groupId,
                gRoleId:gRoleId,
                depGroupName:gName,
                depGroupDesc:gDesc,
                iconLink:icon_link,
                groupRoleList:groupRoleList,
                region_icon_show:region_icon_show,
                serviceTypeList:serviceTypeList
            },"#editInfoDialog");

            $("#editDepInfo_dialog").modal('show');
        });

//        上传大区徽章
        $(document).on("change","#region_icon",function(){
            if ($("#region_icon").val() != '') {
                var formData = new FormData();
                var file = $('#region_icon')[0].files[0];
                if(file.size <= 10*1024*1024 && file.type.indexOf('image') != -1){
                    formData.append('file', file);
                    formData.append('file_size', file.size);
                    formData.append('file_type', file.type);
                    $.ajax({
                        url: '/file/upload.vpage',
                        type: 'POST',
                        data: formData,
                        processData: false,
                        contentType: false,
                        success: function (data) {
                            if (data.success) {
                                var imageTail = '?x-oss-process=image/resize,w_180,h_180/auto-orient,1';
                                $("#region_icon_perview").html("<img src='"+data.fileUrl+imageTail+"'/>");
                                alert("上传成功");
                            } else {
                                alert(date.info);
                            }
                        }
                    });
                }else{
                    alert("请上传小于10M的图片");
                }
            }
        });

        //修改部门信息提交
        $(document).on("click","#editDepSubmitBtn",function(){
            var subFormFlag = true;
            var depGName = $("#editDepName").val();
            var depDesc = $("#editDepDesc").val();
            var roleId = $("#departmentLevel").val();
            var gpid = $("#editInfoDepBtn").data("dpid");

            var postData = {
                groupName: depGName,
                description: depDesc,
                roleId: roleId,
                groupId: gpid
            };
            var arr = [];
            if($('input[name="businessType"]:checked').length == 0){
                alert('业务类型为必选项');
                subFormFlag = false;
            }
            $.each($('input[name="businessType"]:checked'),function (i,item){
                arr.push(item.value);
            });
            postData['serviceTypeStr'] = arr.toString();
            if($("#region_icon_perview>img").attr('src')){
                postData["logo"] = $("#region_icon_perview>img").attr('src').split("?")[0];
            }else{
                postData["logo"] = "";
            }
            if(subFormFlag){
                $.post("/user/orgconfig/modificationDepartmentInfo.vpage",postData,function(res){
                    if(res.success){
                        alert("修改成功");
                        $("#editDepInfo_dialog").modal('hide');
                        currentKey = gpid;
                        reFreshType = "group";
                        refreshPage();
                    }else{
                        alert(res.info);
                    }
                });
            }
        });

        //选择权限区域确定
        $(document).on("click","#chooseRegionBtn",function(){
            var groupId = $("#updateAreaBtn").data("dpid");

                var updateRegion = function(){
                    var reList = [];
                    $.each(selectArr,function(i,item){
                        reList.push({
                            regionCode:item.key
                        })
                    });

                    $.ajax({
                        url:"/user/orgconfig/saveDepartmentRegion.vpage",
                        type:"POST",
                        dataType:"json",
                        data:JSON.stringify({agentGroupId:groupId,responsibleRegion:reList}),
                        success:function(res){
                            if(res.success){
                                alert("调整区域成功");
                                $("#addDepartment_dialog").modal('hide');
                                currentKey = groupId;
                                reFreshType = "group";
                                refreshPage();
                            }else{
                                alert(res.info);
                            }
                        },
                        error:function(e){

                        }
                    });
                };

                var confirmMsg = "区域权限调整影响较大，是否确认？";
                if(confirm(confirmMsg)){
                    updateRegion();
                }
        });

        //用户修改信息
        $(document).on("click","#editUserBtn",function(){
            var userId = $(this).data("uid");
            $.post("/user/orgconfig/userAccountInfo.vpage",{userId:userId},function(res){
                if(res.success){
                    res["type"] = "editInfo";
                    renderDepartment("#addDepartmentUserTemp",res,".detailContentContainer");
                    initDataPicker("#contract_start_date","#contract_end_date");
                }else{
                    alert(res.info);
                }
            });
        });

        // 用户可用余额修改
        $(document).on("click","#updateUsableCashAmountBtn",function(){
            var userId = $(this).data("uid");
            $.post("/user/orgconfig/userAccountInfo.vpage",{userId:userId},function(res){
                if(res.success){
                    renderDepartment("#updateUsableCashAmount",res,".detailContentContainer");
                }else{
                    alert(res.info);
                }
            });
        });

        //用户修改信息提交
        $(document).on("click","#edit_userInfo_btn",function(){
            var pid = $("#editUserBtn").data("gpid");
            if(validateAddFormData()) {
                var formInputs = $("#addDepartmentForm").find('input');
                var postData = {};
                $.each(formInputs, function (i, item) {
                    if (item.name) {
                        postData[item.name] = item.value;
                    }
                });

                var desc = $("#introDesc").val();
                var userId = $("#editUserBtn").data("uid");
                postData["userComment"] = desc;
                postData["userId"] = userId;
                postData["userId"] = userId;
                if($("#perviewIcon>img").attr('src')){
                    postData["avatar"] = $("#perviewIcon>img").attr('src').split("?")[0];
                }else{
                    postData["avatar"] = "";
                }

                //物料预算
                postData["budgetOpt"] = $("#budgetOpt").val();
                var budgetVal = $("#materielBudget").val();
                if(!budgetVal){
                    postData["materielBudget"] = 0;
                }else{
                    postData["materielBudget"] = budgetVal;
                    var adjust_cause = $("#adjust_cause").val();
                    if(adjust_cause && adjust_cause.length <100){
                        postData["adjust_cause"] = adjust_cause;
                    }else{
                        alert("物料预算调整原因必填!");
                        return;
                    }

                }
                // 是否开通蜂巢账号
                postData["bindHoneycomb"] = $('input[name="bindHoneycomb"]:checked').val();
                $.post("/user/orgconfig/modificationUserAccountInfo.vpage",postData,function(res){
                    if(res.success){
                        alert("修改信息成功");
                        validateFlag = true;
                        currentKey = userId;
                        currentParentKey = pid;
                        reFreshType = "user";
                        refreshPage();
                    }else{
                        alert(res.info);
                        validateFlag = false;
                    }
                });
            }
        });

        //调整角色弹窗
        $(document).on("click","#updateRoleBtn",function(){
            var groupId = $(this).data("gpid");
            var userId=$(this).data("uid");
            $.get("/user/orgconfig/getGroupRoleList.vpage",{groupId:groupId,agentUserId:userId},function(res){
                if(res.success){
                    if(res.roleList.length != 0){
                        renderDepartment("#updateUserRoleTemp",res,".userRoleList_dialog");
                        $("#userUpdateRole_dialog").modal("show");
                    }else{
                        alert("当前无可调整角色");
                    }
                }else{
                    alert(res.info);
                }
            });

        });

        //调整角色提交
        $(document).on("click","#updateRoleSubmitBtn",function(){
            var userId = $("#updateRoleBtn").data("uid");
            var groupId = $("#updateRoleBtn").data("gpid");
            var roleId = $("#updateUserRoleSel").val();
            if(roleId == 0){
                alert("请选择角色");
            }else{
                $.post("/user/orgconfig/changeRoleForUser.vpage",{
                    groupId:groupId,
                    userId:userId,
                    roleId:roleId
                },function(res){
                    if(res.success){
                        alert("调整角色成功");
                        $("#userUpdateRole_dialog").modal("hide");
                        currentKey = userId;
                        currentParentKey = groupId;
                        reFreshType = "user";
                        refreshPage();
                    }else{
                        alert(res.info);
                    }
                });
            }
        });

        //添加学校dialog
        $(document).on("click","#addSchoolBtn",function(){
            var uid = $(this).data("uid");
            var gid = $(this).data("gpid");
            $("#selectSids").val("");
            initSelectSchoolData(uid,gid);
            $("#region_select_dialog").modal('show');
            $("#alertInfoInDialog").hide();
        });

        //添加学校Btn
        $(document).on("click","#addSchooleBtn",function(){
            var schoolIds = $("#selectSids").val();
            var agentUerId = $("#addSchoolBtn").data("uid");
            var agentGroupId = $("#addSchoolBtn").data("gpid");

            if(schoolIds.length != 0 && valiteSplitByicon(schoolIds)){
                $.get("/user/orgconfig/searchSchoolsBySchoolIdList.vpage?schoolIds="+schoolIds+"&agentUerId="+agentUerId+"&agentGroupId="+agentGroupId,function(res){
                    if(res.success){
                        var dataTemp = {},errorList=[];
                        if(res.searchResult && res.searchResult.dataList){
                            dataTemp.schoolData = res.searchResult.dataList;
                            dataTemp.totalNo = res.searchResult.dataList.length || [];
                            errorList = res.searchResult.invaildSchoolIdList || [];
                        }
                        renderDepartment('#chooseSchoolTableTemp',dataTemp,"#schoolTable");
                        if(errorList.length != 0){
                            $("#alertInfoInDialog").show();
                            var tempHtml = '<p>有'+errorList.length+'所学校不在字典表或不属于此部门，无法添加！</p>'+
                                            '<p>'+errorList.join(",")+'</p>';
                            $("#alertInfoInDialog").html(tempHtml);
                        }else{
                            $("#alertInfoInDialog").hide();
                        }
                    }else{
                        alert(res.info);
                    }
                })
            }else{
                alert("请输入学校ID,并以英文格式逗号分隔");
            }
        });

        //选择学校提交
        $(document).on("click","#add_school_submit_btn",function(){
            var schoolTds = $("#region_select_dialog").find('td.js-schoolIds');
            if(schoolTds.length != 0){
                var uid = $("#addSchoolBtn").data("uid");
                var gid = $("#addSchoolBtn").data("gpid");
                var schoolList = [],infoList = [];
                var submit = function() {
                    $.post("saveUserSchoolDataList.vpage",{
                        schoolIds: schoolList.join(','),
                        agentUserId: uid,
                        agentGroupId: gid
                    },function(res){
                        if(res.success){
                            if(res.messageInfoList && res.messageInfoList.length != 0){
                                var alertInfo = [];
                                for(var i = 0; i<res.messageInfoList.length;i++){
                                    alertInfo.push(res.messageInfoList[i]);
                                }
                                alertInfo.join("<br>");
                                alert(alertInfo.join("\n"));
                            }else{
                                alert("添加学校成功");
                            }
                            $("#region_select_dialog").modal("hide");
                            currentKey = uid;
                            currentParentKey = gid;
                            reFreshType = "user";
                            refreshPage();
                        }else{
                            alert(res.info);
                        }
                    });
                };

                $.each($(".js-enableFlag"),function(index,item){
                    if(infoList.indexOf($(item).html()) == -1){
                        infoList.push($(item).html());
                    }
                });
                $.each(schoolTds,function(index,item){
                    schoolList.push($(item).html());
                });

                if(infoList.length){
                    if(confirm(infoList.join(",")+'将失去部分学校的权限,是否确认?')){
                        submit();
                    }
                }else{
                    submit();
                }

            }else{
                alert("无可添加学校");
            }
        });

        //调整部门
        $(document).on("click","#updateDepBtn",function(){

            var getTheRoleList =  function(event,tree) {
                var node = tree.node;
                var key = node.key;
                if(tree.targetType != "expander") {
                    if (node.children && node.children.length != 0) {
                        node.setExpanded(true);
                    }
                    $.get("/user/orgconfig/getGroupRoleList.vpage?groupId="+key,function(res){
                        if(res.success){
                            renderDepartment("#updateDepDialogTemp",res,"#newRoleListSelect");
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

        //修改业绩目标
        $(document).on("click",".aBtn",function(){
            var month = $(this).data("month");
            var groupId = $(this).data("regiongroupid");
            var groupOrUser = $(this).data("grouporuser");
            var userId = $(this).data("userid");
            var data = {
                month:month,
                groupOrUser:groupOrUser,
                groupId : groupId,
                userId : userId
            };
            $.get("/kpi/budget/budget_detail.vpage",data,function(res){
                if(res.success){
                    $("#changeTarget_dialog").modal("show");
                    $('#changeTarget_con_dialog').html(template("changeTargetDialogTemp",{res:res}))
                }
            })
        });
        $(document).on("click",".history_btn",function(){
            var month = $(this).data("month");
            var groupId = $(this).data("regiongroupid");
            var type = $(this).data("type");
            var userId = $(this).data("userid");
            var data = {
                month:month,
                groupOrUser:type,
                groupId : groupId,
                userId : userId
            };
            $.get("/kpi/budget/budget_record_list.vpage",data,function(res){
                if(res.success){
                    for(var i = 0;i <res.recordList.length;i++){
                        res.recordList[i].updateTime = new Date(res.recordList[i].updateTime).Format("yyyyMMdd hh:mm");
                    }
                    $("#targetHistory_dialog").modal("show");
                    $('#targetHistory_con_dialog').html(template("TargetHistoryDialogTemp",{res:res.recordList}));
                }else{
                    alert(res.info || "暂无修改记录");
                }
            })
        });
        $(document).on("click",".sure_btn",function(){
            var month = $(this).data("month");
            var groupId = $(this).data("regiongroupid");
            var region = $(this).data("region");
            if(confirm("是否确认" + region + "的目标，确认后对应人员将无法再调整！")){
                $.post("/kpi/budget/confirm_budget.vpage",{month:month,groupId:groupId},function(res){
                    if(res.success){
                        alert("确认成功");
                        renderPerformance(window.currentGroupId,month);
                    }else{
                        alert(res.info || "暂无修改记录");
                    }
                })
            }
        });
        $(document).on("click",'#changeTargetSubmitBtn',function(){
            var data = {};
            data.month = $('.month').data("month");
            data.groupOrUser = $('.groupOrUser').data("type");
            data.groupId = $('input[name="groupId"]').val();
            data.userId = $('input[name="userId"]').val();
            var kpiBudgetData = {};
            for(var i = 0;i < $('.kpiBudgetData').length; i++){
                var keyName = $('.kpiBudgetData').eq(i).data("info");
                var valName = $('.kpiBudgetData').eq(i).val();
                kpiBudgetData[keyName] = valName;
            }
            data.kpiBudgetData = JSON.stringify(kpiBudgetData);
            data.comment = $('.changeReason').val();
            $.post("/kpi/budget/update_budget.vpage",data,function(res){
                if(res.success){
                    alert("修改业绩目标成功");
                    renderPerformance(window.currentGroupId,$('.month').data("month"));
                    $("#changeTarget_dialog").modal("hide");
                }else{
                    alert(res.info);
                }
            });
        });
        //提交调整部门和角色
        $(document).on("click","#updateDepSubmitBtn",function(){
            var uid = $("#updateDepBtn").data("uid");
            var oldGroupId = $("#updateDepBtn").data("gpid");
            var newGid,roleId;

            var tree = $("#useUpdateDep_con_dialog").fancytree("getTree");
            if(tree.getActiveNode()){
                newGid = tree.getActiveNode().key;
                roleId = $("#newRoleName_dialog").val();
                if(roleId != 0){
                    $.post("/user/orgconfig/changeGroupForUser.vpage",{
                        oldGroupId:oldGroupId,
                        newGroupId:newGid,
                        roleId:roleId,
                        userId:uid
                    },function(res){
                        if(res.success){
                            alert("调整部门及角色成功");
                            $("#userUpdateDep_dialog").modal("hide");
                            currentKey = uid;
                            currentParentKey = newGid;
                            reFreshType = "user";

                            refreshPage();
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

        //重置密码
        $(document).on("click","#resetUserPsdBtn",function(){
            var uid = $(this).data("uid");
            var gid = $(this).data("gpid");
            if(confirm("确定要重置密码吗？")){
                $.post("/user/orgconfig/resetAccountPassword.vpage",{
                    agentUserId:uid,
                    agentGroupId:gid
                },function(res){
                    if(res.success){
                        alert("重置密码成功");
                        currentKey = uid;
                        currentParentKey = gid;
                        reFreshType = "user";
                        refreshPage();
                    }else{
                        alert(res.info);
                    }
                });
            }
        });

        //关闭账号
        $(document).on("click","#closeAccountBtn",function(){
            var uid = $(this).data("uid");
            var gid = $(this).data("gpid");
            if(confirm("确定要关闭该账号吗？")){
                $.post("/user/orgconfig/closeUserAccount.vpage",{
                    agentUserId:uid,
                    agentGroupId:gid
                },function(res){
                    if(res.success){
                        alert("关闭该账号成功");
                        currentKey = gid;
                        reFreshType = "group";
                        refreshPage();
                    }else{
                        alert(res.info);
                    }
                });
            }
        });

        //库存
        $(document).on("change","#materielBudget",function(){
            var text = $(this).val();
            if(!validNumber(text)){
                $(".js-budgetInfo").html("请填写数字").show();
                $("#materielBudget").val(0);
                validateFlag = false;
            }else{
                validateFlag = true;
            }
        });
        //保存
        $(document).on("click",'.js-isSaveBtn',function () {
            iSave();
        });
        var iSave = function() {
            $("div.alert-info").hide();
            $("div.alert-error").hide();
            var sourceFile = $("#sourceFile").val();
            if (blankString(sourceFile)) {
                alert("请上传excel！");
                return;
            }
            var fileParts = sourceFile.split(".");
            var fileExt = fileParts.length < 2 ? null : fileParts[fileParts.length - 1].toLowerCase();
            if (fileExt != "xls" && fileExt != "xlsx") {
                alert("请上传正确格式的excel！");
                return;
            }

            var formElement = document.getElementById("importSchoolDict");
            var postData = new FormData(formElement);

            $("#loadingDiv").show();

            $.ajax({
                url: "/kpi/budget/import_budget.vpage",
                type: "POST",
                data: postData,
                processData: false,  // 告诉jQuery不要去处理发送的数据
                contentType: false,   // 告诉jQuery不要去设置Content-Type请求头
                success: function (res) {
                    $("#loadingDiv").hide();
                    if (res.success) {
                        alert("上传成功");
                        renderPerformance(window.currentGroupId,window.currentMonthType);
                    } else {
                        var error = res.errorList;
                        setInfo(error, "alert-error", "error-panel");
                    }
                },
                error: function (e) {
                    console.log(e);
                    $("#loadingDiv").hide();
                }
            });
        }
    });

</script>
<#---导入业绩目标-->
<script type="text/javascript">


    function alertSchoolInfo(allDealSchoolCount, addCount, updateCount, groupChangeIds) {
        var info = "上传成功，本次操作共计" + allDealSchoolCount + "所学校";
        if (addCount > 0) {
            info += ",其中新添加" + addCount + "所"
        }
        if (updateCount > 0) {
            info += ",更新" + updateCount + "所"
        }
        if (groupChangeIds && groupChangeIds.length > 0) {
            info += ",以下"+(groupChangeIds.length)+"所学校所属部门被变更："+(groupChangeIds.join(","))
        }
        info += "。";
        alert(info);
    }
    function alertPerformanceInfo1(allDealSchoolBudgetCount,allOperateMapInsert,allOperateMapUpdate, monthAddMap, monthUpdateMap, monthSet1,monthSet2) {
        alert("上传成功，本次操作共计" + allDealSchoolBudgetCount + "条学校数据，新添加"+ allOperateMapInsert +"条，更新"+ allOperateMapUpdate +"条。\n其中，" + loadMonthData2(monthAddMap, monthUpdateMap, monthSet1,monthSet2));
    }

    function loadMonthData2(monthAddMap, monthUpdateMap, monthSet1,monthSet2) {
        var result = "";
        result += monthAddMap.name  + (monthAddMap.insert+monthAddMap.update) + "条，新添加" + monthAddMap.insert + "条，更新" + monthAddMap.update + "条。\n" + monthUpdateMap.name  + (monthUpdateMap.insert+monthUpdateMap.update) + "条，新添加" + monthUpdateMap.insert + "条，更新" + monthUpdateMap.update + "条。\n" + monthSet1.name  + (monthSet1.insert+monthSet1.update) + "条，新添加" + monthSet1.insert + "条，更新" + monthSet1.update + "条。\n"+ monthSet2.name  + (monthSet2.insert+monthSet2.update) + "条，新添加" + monthSet2.insert + "条，更新" + monthSet2.update + "条。"
        return result;
    }

    function setInfo(info, classEle, idEle) {
        resInfo = getInfo(info);
        if (resInfo) {
            $("div." + classEle).show();
            $("#" + idEle).html(resInfo);
        }
    }

    function getInfoNoBr(info) {
        if (info) {
            var res = "";
            info.forEach(function (e) {
                res += (e + ",");
            });
            return res;
        }
        return false;
    }

    function getInfo(info) {
        if (info) {
            var res = "";
            info.forEach(function (e) {
                res += (e + "<br/>");
            });
            return res;
        }
        return false;
    }
</script>
</@layout_default.page>
