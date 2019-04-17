/*
* create by chunbao.cai on 2018-5-4
* 薯条英语公众号
* -- 广告
*
* */
define(["jquery","logger"],function($,logger){
    $(function(){
        var cnv = document.getElementById("canvas");
        var ctx = cnv.getContext("2d");
        var imgObj = new Image();
        var imgCode = new Image();
        $(imgObj).on("load error",function(){
            cnv.height = $(".shareBox").height()*2;
            cnv.width = $(".shareBox").width()*2;
            ctx.drawImage(imgObj,0,0,cnv.width,cnv.height);

            $(imgCode).on("load error",function(){
                var codeX = Math.ceil(cnv.width*295/750);
                var codeY = Math.ceil(cnv.height*977/1334);
                var codeW = Math.ceil(cnv.width*160/750);
                var codeH = Math.ceil(cnv.height*160/1334);
                ctx.drawImage(imgCode,codeX,codeY,codeW,codeH);

                // 写字
                ctx.font = "25px 微软雅黑";
                ctx.fillStyle = "#ff6a7c";
                ctx.textAlign="center";
                ctx.fillText(window.canvas.dataset.nickname, cnv.width/2, cnv.height*0.67);
                ctx.fillText("邀请你一起学口语", cnv.width/2, cnv.height*0.70);

                $(".bgImg").attr("src",cnv.toDataURL("image/jpeg"))
            });
        });

        imgObj.src = "/public/images/parent/chips/invite_bg.png";
        imgCode.src = $("#qrcodeImg").attr('src');

        wx.ready(function(){
            wx.hideAllNonBaseMenuItem();
            wx.hideOptionMenu();
        });

        // 	推荐页面_生成邀请卡按钮_被点击
        logger.log({
            module: 'm_XzBS7Wlh',
            op: 'recommendpage_createbutton_click'
        });


        //邀请卡页面_被加载 打点
        logger.log({
            module: 'm_XzBS7Wlh',
            op: 'recommendcard_load'
        });


    })
});