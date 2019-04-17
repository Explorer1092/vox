define(['jquery', "$17", "getVerifyCodeModal", "logger", "swiper", 'hotcss', 'jbox'], function ($, $17, getVerifyCodeModal, logger) {
    document.body.addEventListener('touchstart', function () {
    });


    swiper = new Swiper('.swiper-container', {
        pagination: '.swiper-pagination',
        paginationClickable: true,
        direction: 'vertical'
    });

    $(".avatar-list").on("click", function () {
        $(".avatar-list").removeClass("on");
        $(this).addClass("on");
    });

    logger.log({
        s0: !!LoggerProxy && LoggerProxy.openId,
        module: 'ustalk',
        op: 'ustalk_promot_pv',
        userId: logger.getCookie('uid'),
        channel: $17.getQuery("gray")
    })


    new getVerifyCodeModal({
        phoneNoInputId: "pmobile",
        btnId: "sendverifycode",
        url: "/parent/activity/sendustalksmscode.vpage",
        cid: "0",
        countSeconds: 60,
        btnClass: 'num-btn-disabled',
        btnCountingText: "秒后重新获取",
        warnText: "验证码已发送，如未收到请1分钟后再试",
        gaCallBack: function () {

        }
    });


    /*$("#sendverifycode").on("click", function () {
     if($(this).attr("disabled")){
     return false;
     }else{
     //send verify code
     var mobile=$("#pmobile").val();
     $("#sendverifycode").attr("disabled","disabled");
     setTimeout(function(){
     $("#sendverifycode").removeAttr("disabled");
     },60000);
     $.post("/parent/activity/sendustalksmscode.vpage",{mobile:mobile}, function (data) {
     console.log(data);
     })
     }
     });*/

    $(".submit-btn").on("click", function () {
        // 提交用户预定数据
        var pmobile = $("#pmobile").val();
        var studentSelected = $(".avatar-list.on")
        var studentName = studentSelected.data("sname");
        var studentId = studentSelected.data("sid");
        var gray = $17.getQuery("gray");
        var verifyCode = $("#verifycode").val();
        if(!$17.isMobile(pmobile)){
            $17.jqmHintBox("请输入正确手机号");
            return false;
        }
        if(verifyCode.length!=4){
            $17.jqmHintBox("请输入正确的验证码");
            return false;
        }
        $.post("/parent/activity/ustalkpromotsubscribe.vpage", {
            pmobile: pmobile,
            studentName: studentName,
            studentId: studentId,
            gray: gray,
            verifyCode: verifyCode
        }, function (data) {
            if (data.success) {
                $(".alert-box").show();
                $(".result-icon").removeClass("false");
                $(".result-tit").html("申领成功");
                $(".result-txt").html("顾问老师将在24小时内为您安排评测，<br>请保持电话畅通");

            } else {
                $(".alert-box").show();
                $(".result-icon").addClass("false");
                $(".result-tit").html("申领失败");
                $(".result-txt").html("您的信息填写有误，" + data.info);
            }
        });
    });
    var designWidth = 720;
    var oTit = document.getElementById('title');
    var oPage3 = document.getElementById('page3');
    var oC = document.getElementById('c1');
    var gd = oC.getContext('2d');
    var N = 6;
    var pool = [];
    var timer = null;
    var fontS = parseInt(document.documentElement.style.fontSize);

    var clientWidth = document.documentElement.clientWidth || document.body.clientWidth;
    var clientHeight = document.documentElement.clientHeight || document.body.clientHeight;
    oC.width = clientWidth;
    oC.height = clientHeight - (oTit.offsetTop + oTit.offsetHeight);
    console.log(oTit.offsetTop);
    console.log(oTit.offsetHeight);

    var arg = [
        {dx: resize(284), dy: resize(0)},
        {dx: resize(512), dy: resize(147)},
        {dx: resize(50), dy: resize(218)},
        {dx: resize(221), dy: resize(291)},
        {dx: resize(54), dy: resize(530)},
        {dx: resize(395), dy: resize(648)}
    ];

    function rnd(n, m) {
        return n + Math.random() * (m - n);
    }

    function rndColor() {
        return 'rgb(' + rnd(0, 256) + ', ' + rnd(0, 256) + ', ' + rnd(0, 256) + ')';
    }

    function getDis(x1, y1, x2, y2) {

    }

    function calc(num) {
        var result = 0;
        while (result == 0) {
            result = (0 - rnd(0, num)) + (rnd(0, num));
        }
        return result;
    }

    function getPos(obj) {
        var l = 0;
        var t = 0;
        while (obj) {
            l += obj.offsetLeft;
            t += obj.offsetTop;
            obj = obj.offsetParent;
        }
        return {left: l, top: t};
    }

    function resize(px) {
        return hotcss.px2rem(px, designWidth) * fontS;
    }

    function Ball(canvas, options, image) {
        this.oC = canvas;
        this.gd = this.oC.getContext('2d');
        this.dx = options.dx || 0;
        this.dy = options.dy || 0;
        this.r = this.r || 0;
        this.speed = options.speed || 1;
        this.speedX = calc(this.speed);
        this.speedY = calc(this.speed);
        this.color = options.color || 'red';
        this.image = image;
    }

    Ball.prototype.draw = function () {
        this.gd.save();
        this.gd.beginPath();
        this.gd.drawImage(this.image, 0, 0, this.image.width, this.image.height, this.dx, this.dy, resize(this.image.width), resize(this.image.height));

        this.gd.closePath();
        this.gd.restore();
    };
    Ball.prototype.move = function () {
        this.dx += this.speedX;
        this.dy += this.speedY;
        if (this.dx <= 0 || this.dx >= this.oC.width - this.r * 2)this.speedX *= -1;
        if (this.dy <= 0 || this.dy >= this.oC.height - this.r * 2)this.speedY *= -1;
    };
    Ball.prototype.collTest = function (ball2) {
        if (ball2 == this)return false;
        var a = (this.dx + this.r) - (ball2.dx + ball2.r);
        var b = (this.dy + this.r) - (ball2.dy + ball2.r);
        var dis = Math.sqrt(a * a + b * b);
        return dis <= (this.r + ball2.r);
    };

    function createBalls() {
        for (var i = 0; i < N; i++) {
            var oImage = new Image();
            oImage.src = '../../public/images/parent/activity/ustalk/ball' + i + '.png';
            (function (index) {
                oImage.onload = function () {
                    var ball = new Ball(oC, {speed: 0.5}, this);
                    ball.dx = arg[index].dx;
                    ball.dy = arg[index].dy;
                    ball.r = resize(this.width / 2);
                    pool.push(ball);
                    if (pool.length == N) {
                        timer = setInterval(function () {
                            gd.clearRect(0, 0, oC.width, oC.height);
                            for (var i = 0; i < pool.length; i++) {
                                for (var j = 0; j < pool.length; j++) {
                                    if (pool[i].collTest(pool[j])) {
                                        pool[i].speedX *= -1;
                                        pool[i].speedY *= -1;
                                    }
                                }
                                pool[i].draw();
                                pool[i].move();
                            }
                        }, 16);
                    }
                };
            })(i);
        }
    }

    createBalls();
});