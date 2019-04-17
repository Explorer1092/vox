/*
 * Created by free on 2016/01/05
 */
define(["jquery","$17","wx","logger"],function($,$17,wx,logger){
    //当前经验约定压缩比例
    var imgSize = "@1e_1c_0o_0l_700h_80q";

    /**************** weChat config***********/
    wx.config({
        debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
        appId: wechatConfig.appId, // 必填，公众号的唯一标识
        timestamp: wechatConfig.timestamp, // 必填，生成签名的时间戳
        nonceStr: wechatConfig.noncestr, // 必填，生成签名的随机串
        signature: wechatConfig.signature,// 必填，签名，见附录1
        jsApiList: ['chooseImage','previewImage'] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
    });

    wx.ready(function(){
        $(document).on("click",".js-imgItem",function(){
            var currentImg = $(this).attr('src').split("@")[0]+imgSize;
            wx.previewImage({
                current: currentImg, // 当前显示图片的http链接
                urls: imgLinks // 需要预览的图片http链接列表
            });
            var imgName = $(this).siblings('p').html();

            $17.tongjiTrustee("机构相册页",imgName);
            logger.log({
                module: 'mytrustee_branch_album',
                op: 'branch_album_image_'+imgName
            });
        })
    });

    var imgLinks = [],imgs = $("img.js-imgItem");

    $.each(imgs,function(i,item){
        var itemSrc = $(item).attr('src');

        imgLinks.push(itemSrc.split("@")[0]+imgSize);
    });

    ga('trusteeTracker.send', 'pageview');
    logger.log({
        module: 'mytrustee_branch_album',
        op: 'branch_album_pv'
    });
});