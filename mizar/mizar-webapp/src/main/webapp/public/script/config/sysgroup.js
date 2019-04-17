/*-----------用户管理相关-----------*/
define(["jquery","prompt","datetimepicker","paginator","jqform","template"],function($){

    /*------------------------列表页------------------------------*/
    $("#index-filter").on("click",function(){
        $("#index-form").submit();
    });

    $("#index-add").on("click",function(){
        location.href = "update.vpage";
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

    /*$(".role-checkbox").on("click",function(){
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
    });*/

    $("a[id^='delete_user_']").live('click',function(){
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

        var roles = $(".role-checkbox");
        if(roles.length > 0){
            var roles = [];
            $(".role-checkbox:checked").each(function(){
                roles.push($(this).attr("data-value"));
            });
            $("[name='roles']").val(roles.join(','));
        }

        $("#detail-form").ajaxSubmit(function(res){
            if(res.success){
                location.href = "/config/group/index.vpage";
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

    if(window.userList){
        $('#users-table').html(template("users_item",{users:window.userList}));
    }

    $(document).on('click','a.del-user-btn',function(){
        var userId = $(this).data("userid");
        var userName = $(this).parent().siblings().eq(1).html();
        var groupId = $("input[name=id]").val();

        $.prompt(('<div style="text-align: center">确认移除用户"'+userName+'"?</div>'), {
            title: "提示",
            buttons: {"取消": false,"确认":true},
            submit:function(e,v){
                if(v){
                    $.post("/config/group/removeuser.vpage",{
                        groupId:groupId,
                        userId:userId
                    },function(data){
                        if(data.success) {
                            if(data.users) {
                                $('#users-table').html(template("users_item", {users: data.users}));
                                if(data.users.length <= 0){
                                    $("#users-list").hide();
                                }
                            }
                        }
                    });
                }
            }
        });

    });

    $(document).on("click", "#AddingAgenciesBtn", function () {
        $.prompt("<div style='text-align:center;'>输入逗号分割的机构ID<br /><div class='input-control' style='margin:20px auto 0;width:300px;'><textarea class='new-mark' id='AddingAgenciesContent' style='resize: none;width:300px;' placeholder='AAA1,BBB2,CCC3'></textarea></div></div>", {
            title: "添加机构",
            buttons: {"取消": false, "确定": true},
            submit: function (e, v) {
                if (v) {
                    var AddingAgenciesContent = $("#AddingAgenciesContent");

                    if(AddingAgenciesContent.val() == ''){
                        return false;
                    }

                    $.post("/config/user/addusershop.vpage", {
                        id: getQueryString("id"),
                        shopIds: AddingAgenciesContent.val()
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

    $(document).on("click","#add-user-btn",function(){
       $.prompt(
           "<div style='text-align:center;'>输入逗号分割的用户账户名<br />" +
            "<div class='input-control' style='margin:20px auto 0;'>" +
                "<textarea class='new-mark' id='add-user-content' style='resize: none; width: 100%;' placeholder='admin1,test1,cs'>" +
                "</textarea>" +
            "</div>" +
           "</div>",
           {
               title:"添加用户",
               buttons:{"取消":false,"确定":true},
               submit:function(e,v){
                   if(v){
                        var addUserContent = $("textarea#add-user-content").val();
                       if(!addUserContent || addUserContent.trim() == '')
                            return false;

                        $.post("/config/group/adduser.vpage",{
                            "groupId":$("input[name=id]").val(),
                            "userIds":addUserContent
                        },function(data){
                            if(data.success){
                                $('#users-table').html(template("users_item",{users:data.users}));
                            }else{
                                $.prompt((data.info || "输入错误"), {
                                    title: "提示",
                                    buttons: {"知道了": false}
                                });
                            }
                        });
                   }
               }
           }
       );
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

    $("a[id^='delete_usershop_']").live('click',function(){
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

    $(document).on("click",".close-group-btn",function(){
        var groupId = $(this).data("id");
        var gruopName = $(this).parent().siblings().eq(0).html();
        $.prompt("<div style='text-align:center'>确认关闭'"+gruopName+"'?</div>", {
            title: "提示",
            buttons: {"取消": false, "确定": true},
            submit:function(e,v){
                if(v){
                    $.post("/config/group/close.vpage",{"groupId":groupId},function(data){
                        if(!data.success)
                            alert(data.info);
                        else
                            window.location.reload(true);
                    });
                }
            }
        });
    });

    /***删除公众号***/
    $("a[id^='delete_OfficialAccount_']").live('click',function(){
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
});