/*
* create by chunbao.cai on 2018-5-9
* 薯条英语公众号
* -- 支付成功页
*
* */
define(["jquery", "../../../public/js/utils/clipboard/clipboard.js" , "logger"],function($, ClipboardJS, logger){



    $(function(){
        //支付成功页面加载 打点
        logger.log({
            module: 'm_XzBS7Wlh',
            op: 'purchase_successpage_load'
        });

        var wxCodeLength = $('#wxCodeInput').val().length;
        $('#wxCodeInput').css('width', (wxCodeLength * 0.4) + 'rem');

        var copyBtn = new ClipboardJS('#copyWxCode');
        copyBtn.on("success",function(e){
            alert('复制成功');
        });
        copyBtn.on("error",function(e){
            alert('复制失败，请再次尝试')
        });
    });

});