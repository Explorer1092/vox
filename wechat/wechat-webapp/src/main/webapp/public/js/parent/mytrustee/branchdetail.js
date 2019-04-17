/*
 * Created by free on 2015/12/30
 */
define(["jquery","$17","wx","logger"],function($,$17,wx,logger){
    var bid = $17.getQuery("bid");
    var sid = $17.getQuery("sid");

    ////预览相册延期推出
    ////当前经验约定压缩比例
    //var imgSize = "@1e_1c_0o_0l_390h_520w_80q";
    //
    ///**************** weChat config***********/
    //wx.config({
    //    debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
    //    appId: wechatConfig.appId, // 必填，公众号的唯一标识
    //    timestamp: wechatConfig.timestamp, // 必填，生成签名的时间戳
    //    nonceStr: wechatConfig.noncestr, // 必填，生成签名的随机串
    //    signature: wechatConfig.signature,// 必填，签名，见附录1
    //    jsApiList: ['chooseImage','previewImage'] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
    //});
    //
    //wx.ready(function(){
    //    $(document).on("click",".js-imgItem",function(){
    //        var currentImg = $(this).attr('src').split("@")[0]+imgSize;
    //        wx.previewImage({
    //            current: currentImg, // 当前显示图片的http链接
    //            urls: imgLinks // 需要预览的图片http链接列表
    //        });
    //        var imgName = $(this).siblings('p').html();
    //
    //        $17.tongjiTrustee("机构相册页",imgName);
    //        logger.log({
    //            module: 'mytrustee_branch_album',
    //            op: 'branch_album_image_'+imgName
    //        });
    //    })
    //});
    //
    //var imgLinks = [],imgs = $("img.js-imgItem");
    //
    //$.each(imgs,function(i,item){
    //    var itemSrc = $(item).attr('src');
    //    imgLinks.push(itemSrc.split("@")[0]+imgSize);
    //});


    /****************事件交互***********/
    //tab展示切换
    $(document).on("click",".js-descTab>li",function(){
        if(!$(this).hasClass("active")){
            $(this).addClass("active").siblings('li').removeClass("active");
        }
        if($(this).data("type") == "groupDesc"){
            $(".main-trustee").show();
            $(".main-major").hide();
            $17.tongjiTrustee("机构详情页","机构介绍tab",bid);
        }else{
            $(".main-trustee").hide();
            $(".main-major").show();
            $17.tongjiTrustee("机构详情页","服务介绍tab",bid);
            logger.log({
                module: 'mytrustee_branch_detail',
                op: 'service_desc_tab_click',
                branchId: bid
            });
        }
    });

    ////节流控制
    //var throttle = function(method,context){
    //    clearTimeout(method.tId);
    //    method.tId=setTimeout(function(){
    //        method.call(context);
    //    },100);
    //};
    //
    //var tabDiv = $(".js-descTab");
    //var tabParDiv = $(".mtd-intro");
    ////滚动tab
    //var scrollTabfun = function(){
    //    if($(window).scrollTop() > tabParDiv.offset().top){
    //        tabDiv.addClass("h-fixTop");
    //    }else{
    //        tabDiv.removeClass("h-fixTop");
    //    }
    //};
    //
    ////监控tab吸顶
    //$(window).on("scroll",function(){
    //    throttle(scrollTabfun,window);
    //});


    //去了解课程
    $(document).on("click",".js-knowClassBtn",function(){
        $("li[data-type='courseDesc']").addClass("active").siblings("li").removeClass("active");
        $(".main-trustee").hide();
        $(".main-major").show();
        $17.tongjiTrustee("机构详情页","机构介绍底部-去了解课程",bid);
        logger.log({
            module: 'mytrustee_branch_detail',
            op: 'to_know_classBtn_click',
            branchId: bid
        });
    });

    //base info 展示切换
    $(document).on("click",".js-contentMoreBtn",function(){
        $(this).parents('div.mi-section').toggleClass("slideUp");
        var gid = $(this).parents('div.mi-section').find(".js-buyServiceItem").data("gid");
        $17.tongjiTrustee("机构详情页","课程介绍展开",gid);
        logger.log({
            module: 'mytrustee_branch_detail',
            op: 'sku_show_more_btn_click',
            goodId: gid
        });
    });

    $(document).on("click",".js-moreTagBtn",function(){
        $(this).parents("div.evaluation").toggleClass("slideUp");
        $17.tongjiTrustee("机构详情页","机构标签展开",bid);
        logger.log({
            module: 'mytrustee_branch_detail',
            op: 'tag_show_more_btn_click',
            branchId: bid
        });
    });

    //机构相册
    $(document).on("click",".js-imagesBtn",function(){
        setTimeout(function(){
            location.href = "branchalbum.vpage?bid="+bid+"&sid="+sid;
        },200);
        $17.tongjiTrustee("机构详情页","顶部banner相册",bid);
        logger.log({
            module: 'mytrustee_branch_detail',
            op: 'branch_detail_album_view',
            branchId: bid
        });
    });

    //购买服务或者预约体验
    $(document).on("click",".js-buyServiceItem",function(){
       if($(this).hasClass("disabled")){
           return false;
       }else{
           var gid = $(this).attr("data-gid");
           var skuName = $($(this).parents(".mi-section").find('h3>span')[0]).html();
           setTimeout(function(){
                location.href = "createorder.vpage?gid="+gid+"&sid="+sid;
           },200);
           $17.tongjiTrustee("机构详情页",skuName,gid);
           logger.log({
               module: 'mytrustee_branch_detail',
               op: 'buy_service_btn_click',
               branchId: bid,
               goodId : gid
           });
       }
    });

    //过滤无效tag,TO-DO：有待优化 这种无效数据不应该出现，粗暴的隐藏
    $.each($('div.label>span'),function(i,item){
        if($(item).html().length == 0){
            $(item).hide();
        }
    });

    if($('div.label').children("span").length < 5){
        $('span.js-moreTagBtn').hide();
    }

    $.each($('div.mi-section').find("div.mi-main"),function(i,item){
        if($(item).html().length < 75) {
            $(item).parents('div.mi-section').find("span.js-contentMoreBtn").hide();
        }
    });

    ga('trusteeTracker.send', 'pageview');
    logger.log({
        module: 'mytrustee_branch_detail',
        op: 'branch_detail_pv',
        branchId: bid
    });
});