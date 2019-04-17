$(function(){
    var htmlBody = $("html");
    var signInLoginBox = $("#signInLoginBox");

    if(navigator.userAgent.toLowerCase().match(/chrome/) != null || navigator.userAgent.toLowerCase().match(/safari/) != null){
        htmlBody = $("body");
    }

    $.stellar({ verticalScrolling : false});

    //小白云
    var cloud_1 = $(".cloud1");
    var cloud_2 = $(".cloud2");
    var cloud_3 = $(".cloud3");
    var cloud_1_count = 400;
    var cloud_2_count = 0;
    var cloud_3_count = 600;
    setInterval(function(){
        cloud_1_count++;
        cloud_2_count++;
        cloud_3_count++;

        if(cloud_1_count * 0.4 > 860){
            cloud_1_count = 0;
        }

        if(cloud_2_count * 1.2 > 1020){
            cloud_2_count = 0;
        }

        if(cloud_3_count * 1.4 > 1500){
            cloud_3_count = 0;
        }

        cloud_1.css({ "background-position": "-"+ cloud_1_count * 0.4 +"px 0"});
        cloud_2.css({ "background-position": "-"+ cloud_2_count * 1.2 +"px 0"});
        cloud_3.css({ "background-position": "-"+ cloud_3_count * 1.4 +"px 0"});
    }, 50);

    //快乐的小风车
    /*var kiteIdx = $("#kiteIdx");
    function jiaodu(){
        this.angle || (this.angle = 345);
        kiteIdx.css({
            "transform"             : "rotate(" + this.angle + "deg)",
            "-o-transform"          : "rotate(" + this.angle + "deg)",
            "-webkit-transform"     : "rotate(" + this.angle + "deg)",
            "-moz-transform:rotate" : "rotate(" + this.angle + "deg)"
        });
        this.angle = this.angle == 0 ? 345 : this.angle - 15;
    }

    var timer = setInterval(jiaodu, 50);

    kiteIdx.bind("mouseover", function(){
        clearInterval(timer);
        timer = setInterval(jiaodu, 15);
    });

    kiteIdx.bind("mouseleave", function(){
        clearInterval(timer);
        timer = setInterval(jiaodu, 50);
    });*/

    //小飞机
    var planeBox = $(".plane-box");
    planeBox.animate({ left : "+=380px"}, 5000, 'linear');
    setInterval(function(){
        planeBox.animate({ bottom : "440px", left: "600px"}, 1000, 'linear');
        planeBox.animate({ bottom : "420px"}, 1000, 'linear');
    }, 1000);

    //气球1
    var balloonOne = $(".balloon-one");
    setInterval(function(){
        balloonOne.animate({ bottom : "510px"}, 2000, 'linear');
        balloonOne.animate({ bottom : "465px"}, 2000, 'linear');
    }, 1000);

    //气球2
    var balloonTwo = $(".balloon-two");
    setInterval(function(){
        balloonTwo.animate({ bottom : "510px"}, 4000, 'linear');
        balloonTwo.animate({ bottom : "460px", right: "80px"}, 4000, 'linear');
        balloonTwo.animate({ bottom : "400px", right: "60px"}, 4000, 'linear');
        balloonTwo.animate({ bottom : "460px", right: "40px"}, 4000, 'linear');
    }, 1000);

    //收起展开登录
    signInLoginBox.find(".show-hide").on("click", function(){
        var $this = $(this);
        if(signInLoginBox.attr("data-tg") == "hide"){
            signInLoginBox.animate({ top : "0"}, 200);
            signInLoginBox.attr("data-tg", "show");

            $this.find(".i-text").text("收起");
            $this.find(".i-arrow").removeClass("i-arrow-hide");
            $17.tongji("登录页-展开")
        }else{
            signInLoginBox.animate({ top : "-220px"}, 300);
            signInLoginBox.attr("data-tg", "hide");

            $this.find(".i-text").text("展开");
            $this.find(".i-arrow").addClass("i-arrow-hide");
            $17.tongji("登录页-收起")
        }
    });

    //左右切换
    var key = 1;
    var switchRight = $("#switchRight");
    var switchLeft = $("#switchLeft");
    leftRightBtn(key);

    switchRight.on("click", function(){
        if(key < 11){
            key += 2;
            htmlBody.animate({ scrollLeft: 780 * key - $(window).width()/2}, 400);
            leftRightBtn(key);
        }
    });

    switchLeft.on("click", function(){
        if(key > 1){
            key -= 2;
            htmlBody.animate({ scrollLeft: 780 * key - $(window).width()/2}, 400);
            leftRightBtn(key);
        }
    });

    function leftRightBtn(key){
        if(key == 11){
            switchRight.hide();
            $("#rightNav").show();
        }else{
            switchRight.show();
            $("#rightNav").hide();
        }

        if(key == 1){
            switchLeft.hide();
        }else{
            switchLeft.show();
        }
    }

    var screenValue = 1500;
    $(window).scroll(function(){
        var $thisLeft = $(this).scrollLeft();
        if($thisLeft > 1200 && signInLoginBox.attr("data-tg") != "hide"){
            signInLoginBox.find(".show-hide").click();
        }

        if($thisLeft < 700 && signInLoginBox.attr("data-tg") == "hide"){
            signInLoginBox.find(".show-hide").click();
        }

        if($thisLeft < screenValue*0.7){
            key = 1;
        }else if($thisLeft >= (screenValue*0.8) && $thisLeft < screenValue*1.5){
            key = 3;
        }else if($thisLeft > screenValue*2 && $thisLeft < screenValue*2.5){
            key = 5;
        }else if($thisLeft > screenValue*3 && $thisLeft < screenValue*3.5){
            key = 7;
        }else if($thisLeft > screenValue*4 && $thisLeft < screenValue*4.5){
            key = 9;
        }else if($thisLeft > screenValue*4.8){
            key = 11;
        }

        leftRightBtn(key);
    });

    //鼠标滚动 and 进入第一频居中
    htmlBody.bind({
        keydown : function(event) {
            if (event.keyCode == 39) {
                $("#switchRight").click();
            }
            if (event.keyCode == 37) {
                $("#switchLeft").click();
            }
        },
        mousewheel : function(event){
            if(event.originalEvent.wheelDelta > 0){
                htmlBody.animate({ scrollLeft: "-=30" }, 1);
            }else{
                htmlBody.animate({ scrollLeft: "+=30" }, 1);
            }
            return false;
        },
        DOMMouseScroll : function(event){
            if(event.originalEvent.detail > 0){
                htmlBody.animate({ scrollLeft: "-=30" }, 1);
            }else{
                htmlBody.animate({ scrollLeft: "+=30" }, 1);
            }

            return false;
        }
    }).animate({ scrollLeft: 780 - $(window).width()/2}, 400);

    //按钮事件
    signInLoginBox.find(".sign-btn").hover(function(){
        $(this).addClass("sign-btn-hover");
    },function(){
        $(this).removeClass("sign-btn-hover");
    }).mousedown(function(){
            $(this).addClass("sign-btn-active");
        }).mouseup(function(){
            $(this).removeClass("sign-btn-active");
        });

    signInLoginBox.find(".reg-btn").hover(function(){
        $(this).addClass("reg-btn-hover");
    },function(){
        $(this).removeClass("reg-btn-hover");
    }).mousedown(function(){
        $(this).addClass("reg-btn-active");
    }).mouseup(function(){
        $(this).removeClass("reg-btn-active");
    });
});