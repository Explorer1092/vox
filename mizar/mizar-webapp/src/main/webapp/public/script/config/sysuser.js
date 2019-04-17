/*-----------用户管理相关-----------*/
define(["jquery","prompt","datetimepicker","paginator","jqform","template","fancytree"],function($){
    //配置中文日历插件
    $.fn.datetimepicker.dates['zh'] = {
        days:       ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六","星期日"],
        daysShort:  ["日", "一", "二", "三", "四", "五", "六","日"],
        daysMin:    ["日", "一", "二", "三", "四", "五", "六","日"],
        months:     ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月","十二月"],
        monthsShort:  ["1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"],
        meridiem:    ["上午", "下午"],
        //suffix:      ["st", "nd", "rd", "th"],
        today:       "今天"
    };
    /*------------------------列表页------------------------------*/
    $("#index-filter").on("click",function(){
        $("#index-form").submit();
    });

    $("#index-add").on("click",function(){
        location.href = "addindex.vpage";
    });

    /*分页插件*/
    var paginator = $('#paginator');
    var pages = $(".one-page");
    var currentPage = 1;
    if (paginator.length > 0) {
        paginator.jqPaginator({
            totalPages: pages.length,
            visiblePages: 10,
            currentPage: 1,
            first: '<li class="first"><a href="javascript:void(0);">首页<\/a><\/li>',
            prev: '<li class="prev"><a href="javascript:void(0);">上一页<\/a><\/li>',
            next: '<li class="next"><a href="javascript:void(0);">下一页<\/a><\/li>',
            last: '<li class="last"><a href="javascript:void(0);">尾页<\/a><\/li>',
            page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',
            onPageChange: function (num) {
                pages.eq(num - 1).addClass("displayed").siblings().removeClass("displayed");
                currentPage = num;
            }
        });
    }

    $(".role-checkbox").on("click",function(){
        var checkbox = $(".role-checkbox:checked");
        var roles = [];
        checkbox.each(function(){
            roles.push($(this).attr("data-value"));
        });
        if(roles.length>0){
            for(var v in roles){
                if(roles[v]==40){
                    $("#officialAccountDiv").show();
                    break;
                }
                $("#officialAccountDiv").hide();
            }
        }else{
            $("#officialAccountDiv").hide();
        }
    });

    $(document).on('click','.delete_userSchool',function(){
        var _this = $(this);
        var schoolId = $(this).closest('tr').find('.schoolId').html();
        if(addSchool){
            $(this).closest('tr').remove();
            if($(this).closest('table').find('tr').length ==1){
                $('.addInfantSchool').hide();
            }
        }else{
            $.post("/config/user/deleteuserschool.vpage",{schoolId:schoolId,userId:getQueryString("id")},function(res){
                if(res.success){
                    _this.closest('tr').remove();
                }else{
                    alert(res.info);
                }
            });
        }
    });
    $("a[id^='delete_user_']").on('click',function(){
        var id = $(this).attr("id").substring("delete_user_".length);
        if(!confirm("确定要关闭此用户?")){
            return false;
        }
        $.post('deluser.vpage',{
            id:id
        },function(data){
            if(!data.success){
                alert(data.info);
            }else{
                location.href = "index.vpage";
            }
        });
    });

    /*------------------------保存功能权限------------------------------*/
    var requireInputs = $(".require");
    function isEmptyInput(){
        var isTrue = false;
        requireInputs.each(function(){
            if($(this).val() == ''){
                $(this).addClass("error").val("请填写"+$(this).attr("data-title"));
                isTrue = true;
            }
        });
        return isTrue;
    }
    $("#save-btn").on("click",function(){

        if(isEmptyInput()){
            return false;
        }
        /*var roles = $(".role-checkbox");
        if(roles.length > 0){
            var roles = [];
            $(".role-checkbox:checked").each(function(){
                roles.push($(this).attr("data-value"));
            });
            $("[name='roles']").val(roles.join(','));
        }*/

        var roleGroups = [],schoolList = [];
        var roleGroupTree = $("#role-tree").fancytree("getTree");
        var selectedNodes = roleGroupTree.getSelectedNodes();
        $.each(selectedNodes,function(index,node){
            roleGroups.push(node.key);
        });
        //如果是新建或编辑学前学校，那么需要遍历新学校ID
        var jsonStr = "[";
        for(var i = 0;i<$(".schoolList").length;i++){
            var schoolId = $(".schoolList .schoolId:eq("+i+")").html();
            jsonStr += "{\"schoolId\":\""+schoolId+"\",";
            var startTime = $(".schoolList .startTime:eq("+i+")").val();
            startTime = startTime.replace(/-/g,"");
            jsonStr +="\"contractStartMonth\":\""+startTime+"\",";
            var endTime = $(".schoolList .endTime:eq("+i+")").val();
            endTime = endTime.replace(/-/g,"");
            jsonStr +="\"contractEndMonth\":\""+endTime+"\"},";
        }
        jsonStr = jsonStr.substring(0,jsonStr.length-1)+"]";
        console.info(jsonStr);
        $("[name='schoolsJson']").val(jsonStr);
        /*function fun1(){*/

       /* }*/
        $("[name='roleGroupIds']").val(roleGroups.join(','));
        $("#detail-form").ajaxSubmit(function(res){
            if(res.success){
                location.href = "/config/user/index.vpage";
            }else{
                $.prompt("<div style='text-align:center;'>"+(res.info||"保存失败！")+"</div>", {
                    title: "错误提示",
                    buttons: { "确定": true },
                    focus : 1,
                    useiframe:true
                });
            }
        });
    });

    // $(".op-status").on("click",function(){
    //     var $this = $(this);
    //     var data = {
    //         gid : $this.attr("data-gid"),
    //         status : $this.attr("data-status")
    //     };
    //     $.post("/goods/changestatus.vpage",data,function(res){
    //         if(res.success){
    //             location.reload();
    //         }else{
    //             $.prompt("<div style='text-align:center;'>"+(res.info||"状态更新失败！")+"</div>", {
    //                 title: "错误提示",
    //                 buttons: { "确定": true },
    //                 focus : 1,
    //                 useiframe:true
    //             });
    //         }
    //     });
    // });

    $(document).on("focus",".require.error",function(){
        $(this).removeClass('error').val('');
    });

    var officialAccountsList = [];
    if(window.pageOfficialAccountsList){
        officialAccountsList = pageOfficialAccountsList;
        $('#officialAccountBox').html(template("officialAccount_item", {list : officialAccountsList}));
    }

    $(document).on("click", "#AddingAgenciesBtn", function () {
        $.prompt("<div style='text-align:center;'>输入逗号分割的shopId<br /><div class='input-control' style='margin:20px auto 0;width:300px;'><textarea class='new-mark' id='AddingAgenciesContent' style='resize: none;width:300px;' placeholder='AAA1,BBB2,CCC3'></textarea></div></div>", {
            title: $(this).html(),
            buttons: {"取消": false, "确定": true},
            submit: function (e, v) {
                if (v) {
                    var AddingAgenciesContent = $("#AddingAgenciesContent");

                    if(AddingAgenciesContent.val() == ''){
                        return false;
                    }
                    $.post('/config/user/addusershop.vpage', {
                        id: getQueryString("id"),
                        shopIds: AddingAgenciesContent.val(),
                        schoolIds: AddingAgenciesContent.val()
                    }, function (data) {
                        if(data.success){
                            //成功
                            location.reload();
                        }else{
                            $.prompt((data.info || "输入错误"), {
                                title: "提示",
                                buttons: {"知道了": false}
                            });
                        }
                    });
                }
            }
        });
    });
    $(document).on("click", "#AddingSchoolBtn", function () {
        var ignoreFlag = $(this).data('ignore');
        $.prompt("<div style='text-align:center;'>输入逗号分割的schoolId<br /><div class='input-control' style='margin:20px auto 0;width:300px;'><textarea class='new-mark' id='AddingAgenciesContent' style='resize: none;width:300px;' placeholder='AAA1,BBB2,CCC3'></textarea></div></div>", {
            title: $(this).html(),
            buttons: {"取消": false, "确定": true},
            submit: function (e, v) {
                if (v) {
                    var AddingAgenciesContent = $("#AddingAgenciesContent");

                    if(AddingAgenciesContent.val() == ''){
                        return false;
                    }
                    var AddingArr = [];
                    var AddedArr = [];
                    $('.schoolId').each(function(){
                        AddedArr.push($(this).data('id'));
                    });
                    AddingArr.push(AddingAgenciesContent.val().substring(','));
                    for(var i = 0;i < AddingArr.length;i++ ){
                        for(var j=0;j< AddedArr.length;j++){
                            if(AddingArr[i] == AddedArr[j]){
                            alert('学校ID有重复，请检查');
                            return false;
                            }
                        }
                    }
                    $.post("/config/user/finduserschool.vpage", {
                        userId: getQueryString("id"),
                        ignore: ignoreFlag,
                        schoolIds: AddingAgenciesContent.val()
                    }, function (data) {
                        if(data.success){
                            //成功
                            var errorIds = "",schoolContent =  "" ,notInfantIds = "" ,bindedIdMap = "";
                            if(data.errorIds.length >= 1){
                                errorIds = "学校id：" + data.errorIds.join(',') + '未找到<br/>';
                            }
                            if(data.notInfantIds.length >= 1){
                                notInfantIds = "学校id：" + data.notInfantIds.join(',') + '不是学前学校，无法添加<br/>';
                            }
                            for(var key in data.bindedIdMap){
                                bindedIdMap += "学校id：" + key + '已经被账号' + data.bindedIdMap[key] +'绑定，请勿重复添加<br/>';
                            }
                            schoolContent = errorIds + notInfantIds + bindedIdMap;
                            if(schoolContent != ''){
                                $.prompt(("<p style='text-align:center;'>" + schoolContent + "</p>"), {
                                    title: "提示",
                                    buttons: {"知道了": false}
                                });
                            }
                            var contentHtml = template("schoolList", {res:data.successSchools});
                            $("#schoolManager tbody").append(contentHtml);
                            var year = new Date().getFullYear();
                            var month = new Date().getMonth()+1;
                            var currentMonth;
                            if(month >=10){
                                currentMonth = month;
                            }else{
                                currentMonth = "0" + month ;
                            }
                            $('.newAddSchool .startTime').val(year +"-" + currentMonth);
                            $('.newAddSchool .endTime').val(year + 1 +"-" + currentMonth);
                            $('.addInfantSchool').show();
                            startTime($('.startTime'));
                            endTime($('.endTime'));
                        }else{

                        }
                    });
                }
            }
        });
    });
    $(document).on("click", "#addOfficialAccountBtn", function () {
        $.prompt("<div style='text-align:center;'>输入逗号分割的启用了的公众号标识<br /><div class='input-control' style='margin:20px auto 0;'><textarea class='new-mark' id='officialAccountContent' style='resize: none; width: 100%;' placeholder='AAA1,BBB2,CCC3'></textarea></div></div>", {
            title: "添加公众号",
            buttons: {"取消": false, "确定": true},
            submit: function (e, v) {
                if (v) {
                    var officialAccountContent = $("#officialAccountContent");

                    if(officialAccountContent.val() == ''){
                        return false;
                    }
                    $.post("/config/user/addofficialaccount.vpage", {
                        id: getQueryString("id"),
                        officialAccountKeys: officialAccountContent.val()
                    }, function (data) {
                        if(data.success){
                            //成功
                            //location.reload();
                            if(data.addList){

                               var dataList = data.addList;
                                var addAccountsList = [];
                                for(var dataIndex in dataList){
                                    var obj =  dataList[dataIndex];
                                    officialAccountsList.push({
                                        officialAccountKey : obj.accountsKey
                                    });

                                    addAccountsList.push(obj.accountsKey);
                                }

                                var content = "";
                                if(dataList && dataList.length > 0)
                                    content = "成功添加公众号：" + addAccountsList.join(",") + "<br>";
                                if(data.offlineAccounts && data.offlineAccounts.length > 0)
                                    content += "存在下线的公众号：" + data.offlineAccounts.join(",") + "<br>";
                                if(data.existAccounts && data.existAccounts.length > 0)
                                    content += "存在已关联过的公众号：" + data.existAccounts.join(",") + "<br>";
                                if(data.errorAccounts && data.errorAccounts.length > 0)
                                    content += "错误的公众号：" + data.errorAccounts.join(",") + "<br>";

                                $.prompt(content, {
                                    title: "提示",
                                    buttons: {"确定": false}
                                });

                                $('#officialAccountBox').html(template("officialAccount_item", {list : officialAccountsList}));
                            }
                        }else{
                            $.prompt((data.info || "输入错误"), {
                                title: "提示",
                                buttons: {"知道了": false}
                            });
                        }
                    });

                }
            }
        });
    });

    $("a[id^='delete_usershop_']").on('click',function(){
        var shopId = $(this).attr("id").substring("delete_usershop_".length);
        if(!confirm("确定要去掉此机构?")){
            return false;
        }
        $.post('delusershop.vpage',{
            id: getQueryString("id"),
            shopId:shopId
        },function(data){
            if(!data.success){
                alert(data.info);
            }else{
                window.location.reload(true);
            }
        });
    });

    $(document).on('click','.OfficialAccountDeleteBtn',function () {
        var index = $(this).data('index');
        var officialAccountKey =$(this).data('accountkey');
        officialAccountsList.splice(index,1);
        $.post('deluserofficialaccount.vpage',{
            id: getQueryString("id"),
            officialAccountKey:officialAccountKey
        },function(data){
            if(!data.success){
                alert(data.info);
            }else{
                $('#officialAccountBox').html(template("officialAccount_item", {list : officialAccountsList}));
            }
        });


    });

    /***删除公众号***/
    $("a[id^='delete_OfficialAccount_']").on('click',function(){
        var officialAccountId = $(this).attr("id").substring("delete_OfficialAccount_".length);
        if(!confirm("确定要去掉此公众号?")){
            return false;
        }
        $.post('deluserofficialaccount.vpage',{
            id: getQueryString("id"),
            officialAccountId:officialAccountId
        },function(data){
            if(!data.success){
                alert(data.info);
            }else{
                window.location.reload(true);
            }
        });
    });

    //Get Query
    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }

    if($("#role-tree").length > 0){
        var userId = $("[name='id']").val();
        $("#role-tree").fancytree({
            source: {
                url: "/config/user/loadroletree.vpage?userId=" + userId,
            },
            checkbox: true,
            selectMode: 2,
            icon:false,
            init:function(node,tree){
                var tree = $("#role-tree").fancytree("getTree");
                tree.visit(function(node){
                    var nodeTitle = node.title;
                    var type = node.data.type;
                    var key = node.key;
                    node.icon = false;
                    if(type && type == "role"){
                        node.setTitle('<i class="fa fa-user" data-key="'+key+'">'+nodeTitle+'</i>');
                    }else{
                        node.setTitle('<i class="fa fa-cube" data-key="'+key+'">'+nodeTitle+'</i>');
                    }
                });
            },
            select: function(event, data) {
                var selectedNodes = data.tree.getSelectedNodes();
                $.each(selectedNodes,function(index,node){
                    var type = node.data.type;
                    var roleType = node.key.split("-")[1];
                    if(type && type == 'role' && roleType == '40'){
                        $("#officialAccountDiv").show();
                        return false;
                    }else
                        $("#officialAccountDiv").hide();
                });
            }
        });
    }
    endTime($('.endTime'));
    /*日期插件*/
    function startTime(obj) {
        obj.datetimepicker({
            language: 'zh',
            format: 'yyyy-mm',
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            numberOfMonths: 1,
            defaultDate: new Date(),
            startDate: new Date(),
            changeMonth: false,
            changeYear: false,
            minView: 3,
            startView: 3,
            autoclose: true,
            forceParse: false
        }).on('changeDate',function(ev){
            var starttime=$(this).val();
            starttime.replace(/-/g,"");
            var endTime = $(this).parent().siblings().find('.endTime');
            endTime.datetimepicker('setStartDate',starttime);
            endTime.datetimepicker('hide');
        });
    }
    function endTime(obj){
        obj.datetimepicker({
            language: 'zh',
            format: 'yyyy-mm',
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            minView:3,
            startDate:new Date(),
            startView:3,
            autoclose:true,
            forceParse:false
        }).on('changeDate',function(ev){
            var startTime = $(this).parent().siblings().find('.startTime');
            var endTimeVal=$(this).val();
            startTime.datetimepicker('setEndDate',endTimeVal);
            startTime.datetimepicker('hide');
        }).on('show',function(){
            var startTimeVal = $(this).parent().siblings().find('.startTime').val();
            $(this).datetimepicker('setStartDate',startTimeVal);
        });
    }
});
