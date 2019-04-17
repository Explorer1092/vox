/*
 冬令营实验专题页--预约报名 2016-1-5 11:37:58
 */
define(['jquery', 'getVerifyCodeModal', '$17', "jbox",'logger'], function ($, getVerifyCodeModal, $17, jbox,logger) {
    var mobile = $('#mobile'), verifyCode = $('#verifyCode'), email = $('#email'), reservationBtn = $("#reservationBtn");

    //验证码
    new getVerifyCodeModal({
        phoneNoInputId: "mobile",
        btnId: "verifyCodeBtn",
        url: "/parent/trustee/sendtrusteesmscode.vpage",
        cid: reservationBtn.data('cid') || '0',
        countSeconds: 120,
        btnClass: "disabled",
        btnCountingText: "秒后重新获取",
        warnText: "验证码已发送，如未收到请2分钟后再试"
    });

    //初始化
    mobile.keyup();

    reservationBtn.on('click',function(){
        var $this = $(this);
        var shop_id = $this.data('shop_id');

        if(!$17.isMobile(mobile.val())){
            $17.jqmHintBox("请输入正确手机号");
            mobile.focus();
            return false;
        }

        if($17.isBlank(verifyCode.val())){
            $17.jqmHintBox("请输入验证码");
            verifyCode.focus();
            return false;
        }

        if(!$17.isEmail(email.val())){
            $17.jqmHintBox("请输入正确邮箱");
            email.focus();
            return false;
        }
        $17.loadingStart();
        $.post("/parent/trustee/verifycode.vpage",{
            mobile: mobile.val(),
            code : verifyCode.val(),
            email: email.val(),
            shopId : shop_id
        }, function(data){
            console.info(data);
            if(data.success){
                location.href = "/parent/trustee/reservepay.vpage?shopId="+shop_id;
            }else{
                $17.jqmHintBox(data.info);
            }
            $17.loadingEnd();
        }).fail(function(){
            $17.loadingEnd();
        });

    });

    //统计
    var shopId = $17.getQuery("shopId") || 0, shopName = '';
    if (shopId == 11) {
        shopName = 'sy';
    } else if (shopId == 12) {
        shopName = 'bj';
    } else if (shopId == 13) {
        shopName = 'cbs';
    }
    logger.log({
        "module": "wintercamp",
        "op": "detail_view_" + shopName
    });

    $('.detailTjBtn').on('click', function () {
        var $this = $(this);
        var name = $this.data('name');
        logger.log({
            "module": "wintercamp",
            "op": "detail_right_menu_click_" + shopName + "_" + name
        });
        setTimeout(function () {
            location.href = $this.data('url');
        }, 300);
    });
});
