/*
* create by chunbao.cai on 2018-5-3
* 薯条英语公众号
* -- 登录
*
* */
define(["jquery","logger"],function($,logger){
    $(function(){
        $('body').css({'height':$(window).height()});
        // 获取验证码
        function handle_code(){
            $("#get_code").off("click");
            var time_num = 60; //倒计时的时间
            var cid = $(".signPhone").data("cid");
            var mobile = $(".signPhone").val();
            if(/^1\d{10}/.test(mobile)){
                $.ajax({
                    url: '/signup/chips/verifiedlogin/sendsmscode.vpage',
                    type: 'POST',
                    data: {
                        cid:cid,
                        mobile:mobile
                    },
                    dataType:"json",
                    success: function (data) {
                        //请求成功
                        if(data.success){
                            window.get_code.classList.add('active'); //倒计时的时候，需要显示的样式
                            window.get_code.innerHTML = '60秒';
                            code_interval = setInterval(function(){  //倒计时的函数
                                time_num = time_num - 1;
                                if(time_num == 0){
                                    window.clearInterval(code_interval);
                                    window.get_code.innerHTML = '获取验证码';
                                    window.get_code.classList.remove('active');
                                    window.get_code.addEventListener("click", handle_code, false);  //如果倒计时结束，在添加上点击事件
                                }else{
                                    window.get_code.innerHTML = time_num+'秒';
                                }
                            },1000);
                        }else{
                            alert(data.info);
                            window.get_code.addEventListener("click", handle_code, false);  //如果倒计时结束，在添加上点击事件
                        }
                    },
                    error:function(err){
                        console.log(err);
                        window.get_code.addEventListener("click", handle_code, false);  //如果倒计时结束，在添加上点击事件
                    }
                });
            }else{
                alert("请输入正确手机号")
            }
        }
        window.get_code.addEventListener("click", handle_code, false);  //如果倒计时结束，在添加上点击事件

        // 登录
        $(".signBtn").click(function(){
            var code = $(".signPassword").val();
            var mobile = $(".signPhone").val();
            $.ajax({
                url: '/signup/chips/verifiedlogin.vpage',
                type: 'POST',
                data: {
                    code:code,
                    mobile:mobile
                },
                dataType:"json",
                success: function (data) {
                    if(data.success) {
                        var url = $("#returnUrl").val();
                        if (url == null || url.trim() == '') {
                            location.reload();
                        } else {
                            window.location.href = url;
                        }
                    }else{
                        alert(data.info)
                    }
                },
                error:function(err){
                    console.log(err)
                }
            });
        })


        // 登录页_被加载
        logger.log({
            module: 'm_XzBS7Wlh',
            op: 'login_load'
        });

    })

});