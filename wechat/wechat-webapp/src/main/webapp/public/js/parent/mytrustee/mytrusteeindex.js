/*
 * Created by free on 2015/12/30
 */
define(["jquery","$17","flexslider","knockout","logger","jbox"],function($,$17,flexslider,knockout,logger){
    $17.loadingStart();
    var viewModel = {
        bid: knockout.observable(0),
        banners: knockout.observableArray([]), //banner列表
        branchs: knockout.observableArray([]), //托管机构列表
        noTrustee: knockout.observable(false), //未开通托管服务
        showBanner: knockout.observable(false), //显示banner
        showTrusteeList: knockout.observable(false) //显示机构列表
    };

    knockout.applyBindings(viewModel);

    var childNum = $(".js-childList").find("ul>li").length;

    //渲染banner和机构列表
    var displayTrustee = function(result) {
        var branchList = result.branchs;
        for(var i= 0;i<branchList.length;i++){
            if(branchList[i].distance > 1000 ){
                branchList[i].distance = (branchList[i].distance)/1000 +"k";
            }
            if(!branchList[i].distance){
                branchList[i].distance = "0";
            }
        }
        viewModel.branchs(branchList);

        //对banner进行缓存，监测之前的banner如果有值不再重复渲染（TODO-对比值一致）
        dispalyBanner(result.banners);
        viewModel.noTrustee(false);
        viewModel.showTrusteeList(true);
        setTimeout(function(){
            //过滤无效tag,TO-DO：有待优化 这种无效数据不应该出现，粗暴的隐藏
            $.each($(".js-tag"),function(n,item){
                if(!$(item).html().length){
                    $(item).hide();
                }
            });
        },200);
    };

    //渲染孩子姓名
    var dispalyChildrenName = function(name) {
        var sname = name ? name : $("#singleChildName").val();
        $(".js-stuName").html(sname);
    };

    //渲染banner
    var dispalyBanner = function(banners){
        if($("ul.slides").find("li").length == 0){
            if(banners.length != 0){
                viewModel.showBanner(true);
                viewModel.banners(banners);
            }else{
                viewModel.showBanner(false);
            }
        }
    };

    //孩子无托管班对应数据
    var displayNoneTrusteeService = function(result){
        //如果有某个孩子没有托管班，banner还是显示，下方显示变换
        if(childNum > 1){
            dispalyBanner(result.banners);
        }else{
            viewModel.showBanner(false);
            $(".js-noneTrusteeService").removeClass("emptys");
        }

        viewModel.noTrustee(true);
        viewModel.showTrusteeList(false);
    };
    //渲染托管班banner，详情列表
    var displayTrusteeService = function (result){
        //机构详情
        if(result.branchs.length != 0 ){
            displayTrustee(result);
        }else{
            //无托管机构开通
            displayNoneTrusteeService(result);
        }
    } ;

    //获取单个孩子班级信息
    var getSingleChildClassInfo = function(sid,sname){
        $.post("loadbranchs.vpage",{studentId:sid},function(result){
            if(result.success){
                displayTrusteeService(result);
                dispalyChildrenName(sname);
                $17.loadingEnd();
            }else{
                $17.jqmHintBox('获取孩子附近托管机构失败');
                $17.loadingEnd();
            }
            console.log(result);
        });
    };

    /****************事件交互***********/
    //选中孩子头像
    $(document).on("click",".js-childItem",function(){
        $(this).addClass('select');
        $(this).siblings('li').removeClass('select');
        var sid = $(this).attr("data-sid");
        $("#singleChild").val(sid);
        var sname = $(this).children('p').html();
        getSingleChildClassInfo(sid,sname);

        setTimeout(function(){
            displayFlexslider();
        }, 200);
    });

    if(childNum){
        //默认选中第一个孩子
        $($('div.js-childList>ul>li')[selectIndex]).click();
    }else{
        var singleId= $("#singleChild").val();
        var singleName = $("#singleChildName").val();
        getSingleChildClassInfo(singleId,singleName);
    }

    var displayFlexslider = function(){
        if($("ul.slides").find("li").length>0){
            $(".flexslider").flexslider({
                animation:"fade",
                direction:"horizontal",
                touch: true,
                animationLoop: true,
                easing: "swing",
                controlNav: false,
                directionNav: false
            });
        }
    };

    //选中托管机构,进入机构详情页面
    $(document).on("click",".js-trusteeDetailItem",function(){
        var bid = $(this).attr("data-bid");
        var sid = $("#singleChild").val();
        setTimeout(function(){
            location.href = "branchdetail.vpage?sid="+sid+"&bid="+bid;
        },200);
        var name = $(this).find("div.line01").html();
        var price = $(this).find('span.js-price').html();
        var distance = $(this).find('span.js-distance').html();
        var tags = [];
        $.each($(this).find('a.js-tag'),function(i,item){
            tags.push($(item).html());
        });
        $17.tongjiTrustee("列表页",name+bid,distance+price);
        logger.log({
            module: 'mytrustee_branch_list',
            op: 'branch_list_click',
            branchId: bid,
            distance: distance,
            tag: tags.join(','),
            lowPrice: price
        });
    });

    //banner广告位
    $(document).on("click",".js-imgLinkBtn",function(){
        var link = $(this).attr("data_href");
        var srcList = $(this).children("img").attr("src").split("@")[0].split("/");
        var imgName = srcList[srcList.length-1];
        setTimeout(function(){
            location.href = link;
        },200);
        $17.tongjiTrustee("列表页","banner广告-"+imgName,"站内广告");
    });

    ga('trusteeTracker.send', 'pageview');

    setTimeout(function(){
        var studentId = $($(".js-childList").find('li')[0]).data("sid");
        var listNumber = $(".js-trusteeDetailItem").length;

        logger.log({
            module: 'mytrustee_branch_list',
            op: 'mytrustee_pv',
            studentId: studentId,
            listNumber: listNumber
        });
    },200);


});