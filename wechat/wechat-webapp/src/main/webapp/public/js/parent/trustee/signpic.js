/**
 * Created by Free on 2015/11/13.
 */
define(["jquery","$17", "knockout","wx","jbox"],function($,$17,knockout,wx){
    /****************变量声明***********/
    var needPay = $(".js-needPay").html();
    var recordId = recordJson.id;

    var needPayVal = true;
    if(needPay == 'false'){
        needPayVal = false;
    }

    wx.config({
        debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
        appId: wechatConfig.appId, // 必填，公众号的唯一标识
        timestamp: wechatConfig.timestamp, // 必填，生成签名的时间戳
        nonceStr: wechatConfig.noncestr, // 必填，生成签名的随机串
        signature: wechatConfig.signature,// 必填，签名，见附录1
        jsApiList: ['chooseImage','uploadImage','hideAllNonBaseMenuItem','showMenuItems'] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
    });
    wx.ready(function(){
            wx.hideAllNonBaseMenuItem();
            wx.showMenuItems({
                menuList: ['menuItem:favorite'] // 要显示的菜单项
            });
        }
    );

    var signPicModalAndView = {
        collectPage: collectPage,
        toPay: toPay,
        firstClick: picMapJson.first.canUpload,
        secondClick: picMapJson.second.canUpload,
        thirdClick: picMapJson.third.canUpload,
        firstFileName: picMapJson.first.fileName || "",
        secondFileName: picMapJson.second.fileName || "",
        thirdFileName: picMapJson.third.fileName || ""
    };

    /****************方法声明***********/
    function collectPage () {
        $(".activity-lead").hide();
        $(".activity-lead").show();

        if(needPayVal){
            $17.tongjiTrustee("B预约成功页面","B点击收藏此页");
        }else{
            $17.tongjiTrustee("A预约成功页面","A点击收藏此页");
        }
    }

    function toPay() {
        if(needPayVal){
            $17.tongjiTrustee("B预约成功页面","B去付款");
        }else{
            $17.tongjiTrustee("A预约成功页面","A去付款");
        }
        location.href ="skupay.vpage";
    }

    function changeImageToDisable (index) {
        switch (index) {
            case 1:
                signPicModalAndView.firstClick(false);
                break;
            case 2:
                signPicModalAndView.secondClick(false);
                break;
            case 3:
                signPicModalAndView.thirdClick(false);
                break;
        }
    }

    /****************事件交互***********/
    knockout.applyBindings(signPicModalAndView);

    $(document).on("click",".activity-lead",function(){
        $(".activity-lead").hide();
    });

    //上传图片
    $(document).on('click',".upload-pic",function(){
        //通过jsapi选择图片和上传图片
        var index=$(this).attr('data-imgIndex');
        var imageBox,picKey;

        imageBox = $("div[data-imgIndex = "+index+"]");

        if(index == "1"){
            picKey = "firstPic";
        }
        if(index == "2"){
            picKey = "secondPic";
        }
        if(index == "3"){
            picKey = "thirdPic";
        }

        wx.chooseImage({
            count: 1, // 默认9
            sizeType: ['compressed'], // 可以指定是原图还是压缩图，默认二者都有
            sourceType: ['album', 'camera'], // 可以指定来源是相册还是相机，默认二者都有
            success: function (res) {
                var localIds = res.localIds; // 返回选定照片的本地ID列表，localId可以作为img标签的src属性显示图片
                var localId=localIds[0];
                wx.uploadImage({
                    localId: localId, // 需要上传的图片的本地ID，由chooseImage接口获得
                    isShowProgressTips: 1, // 默认为1，显示进度提示
                    success: function (res) {
                        var serverId = res.serverId; // 返回图片的服务器端ID
                        $.post('uploadimg.vpage',{recordId:recordId,mediaId:serverId,picKey:picKey},function(data){
                            if(data.success){
                                imageBox.show().html('<img src="'+localId+'"/>'+'<span class="success order_icon"></span>');
                                imageBox.parent('div.sign-list').addClass("signed");
                                changeImageToDisable(index);
                            }else{
                                $17.jqmHintBox(data.msg);
                            }
                        })
                    }
                });
            }
        });

        if(needPayVal){
            $17.tongjiTrustee("B预约成功页面","B点击签到"+index);
        }else{
            $17.tongjiTrustee("A预约成功页面","A点击签到"+index);
        }

    });

    ga('trusteeTracker.send', 'pageview');
});