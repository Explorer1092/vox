define(["jquery","$17"], function($,$17) {
    var timer;
    function btnCountdown($btn) {
        clearInterval(timer);
        $btn.addClass('btn_disable');
        var countDown = 180;

        var timer = setInterval(function () {
            if (countDown) {
                $btn.text((countDown > 10 ? countDown : (' ' + countDown)) + '秒');
                countDown--;
            } else {
                clearInterval(timer);
                $btn.removeClass('btn_disable').text('发送验证码');
            }
        }, 1000);
        return timer;
    }

    function btnStopCountdown($btn) {
        clearInterval(timer);
        $btn.removeClass('btn_disable').text('发送验证码');
    }

    var $btn = $('#getVerifyCodeBtn');
    var url = $btn.data('requrl'),mobile = $('#mobile'),cid = $btn.data('cid') || 0;
    function sendSMS () {
        btnStopCountdown($btn, timer);
        timer = btnCountdown($btn);
        $.post(url,{mobile: mobile.val(),cid : cid}, function (data) {
            if(data.success){
                $17.msgTip('发送成功');
            }else{
                $17.msgTip(data.info);
                btnStopCountdown($btn, timer);
            }
        });
    }

    mobile.on('keyup input',function(){
        if($17.isMobile(mobile.val())){
            $btn.removeClass('btn_disable');
        }else{
            $btn.addClass('btn_disable');
        }
    });

    $btn.on('click',function(){
        var $this = $(this);
        if($this.hasClass('btn_disable')){
            $this.focus();
        }else{
            sendSMS();
        }
    });
});