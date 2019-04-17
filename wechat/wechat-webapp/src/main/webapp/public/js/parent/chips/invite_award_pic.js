define(["../../../public/lib/vue/vue.min.js","jquery","logger","../../../public/js/utils/clipboard/clipboard.js", "../../public/lib/swiper/js/swiper.js"],function(Vue,$,logger,ClipboardJS){
    function getParams(name){
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURI(r[2]); return null;
    }
    logger.log({
        module: 'm_XzBS7Wlh',
        op: 'invitation_adpage_load',
        s0:getParams("or") === 'scan' ? 'recognition' : 'invition'
    });
    var timeOutEvent=0;
    $(function(){
        $("#shareFriend").on({
            touchstart: function(e){
                timeOutEvent = setTimeout("longPress()",500);
                e.preventDefault();
            },
            touchmove: function(){
                clearTimeout(timeOutEvent);
                timeOutEvent = 0;
            },
            touchend: function(){
                clearTimeout(timeOutEvent);
                if(timeOutEvent!=0){
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'invitation_card_send',
                        s0:''
                    });
                }
                return false;
            }
        })
    });

    function longPress(){
        timeOutEvent = 0;
    }

    function drawImage(index) {
        var cnv = document.getElementById("canvas" + index);
        var ctx = cnv.getContext("2d");
        var imgObj = new Image();
        var imgCode = new Image();
        var imgHeader=new Image();
        $(imgObj).on("load error",function(){
            cnv.width = imgObj.width;
            cnv.height = imgObj.height;
            ctx.drawImage(imgObj,0,0,cnv.width,cnv.height);
            
            $(imgCode).on("load error",function(){
                var codeX = cnv.width*0.09;
                var codeY = cnv.height*0.85;
                var codeW = imgCode.width;
                var codeH = imgCode.height;
                if(index === '02') {
                    codeX -= 4;
                }
                ctx.drawImage(imgCode,codeX,codeY,codeW,codeH);
                $(".adver_img" + index).attr("src",cnv.toDataURL("image/jpeg"));
                $(imgHeader).on("load error",function(){
                    var avatarCanvas = document.createElement('canvas');
                    avatarCanvas.width = 100;
                    avatarCanvas.height = 100;
                    var avatarCtx = avatarCanvas.getContext('2d');
                    avatarCtx.beginPath();
                    avatarCtx.arc(50, 50, 50, 0, Math.PI * 2, false);
                    avatarCtx.clip();
                    avatarCtx.drawImage(imgHeader, 0, 0);

                    var codeX = cnv.width*0.05;
                    var codeY = cnv.height*0.02;
                    var codeW = imgHeader.width;
                    var codeH = imgHeader.height;
                    ctx.drawImage(avatarCanvas,codeX,codeY,codeW,codeH);
                    $(".adver_img" + index).attr("src",cnv.toDataURL("image/jpeg"));
                });
                imgHeader.width = cnv.width * 0.12;
                imgHeader.height = cnv.width * 0.12;
                imgHeader.src = '/chips/open/image.vpage?url=' + avatar;
                // imgHeader.src = "https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83epfvLpggicjz8fsiaREG5HcBKeHiaKyCEgaWNwoLTDPlWiaFibdGasYGQA0vB5wECpRibzprb8NsSbhQfOA/132";
            });
            imgCode.width = cnv.width*0.25;
            imgCode.height = cnv.width*0.25;
            imgCode.src="/chips/qrcode.vpage?url=" + qrcodeUrl;
        });
        imgObj.src = "/public/images/parent/chips/invite_award/adversiting" + index + ".png";
    }
    
    drawImage('01');
    drawImage('02');
    var vm = new Vue({
        el: "#inviteAwardWrap",
        data: {
            inviteWordBox: false,
            inviteText: [
                '薯条英语真的太棒了！省时省事孩子还学得快，比线下英语教学效率高。现在，他们开设了体验课，只需要9.9 ，你们可以体验一下，购课还可获得旅行手账和优惠券，真的太值了',
                '原来英语这么学真的管用！薯条英语这个课程做的太棒了，推荐给你们！现在体验课只需要9.9元，购课送手账，打卡还返50元优惠券，真的太值了，快来体验吧！',
                '孩子最近在薯条英语上学习，英语提高特别快！特别是英语外教和视频对话，让孩子找到了学习的乐趣。现在他们有体验课，只要9.9，还有旅行手帐和代金券赠送，快来体验吧！',
            ],
            invieText:'',
            num:'0'
        },
        methods: {

            inviteWord: function () {
                this.inviteWordBox = true;
            },
            changeInvite: function () {
                var _this = this;
                var code = randomCode(0, 2);
                function randomCode(begin, end) {
                    _this.num = Math.round(Math.random() * (end - begin) + begin);
                }
            },
            copyText:function(){
                this.inviteWordBox = false;
                var copyBtn = new ClipboardJS('#copyBtn');
                copyBtn.on("success",function(e){
                    alert('复制成功');
                });
                copyBtn.on("error",function(e){
                    alert('复制失败，请再次尝试')
                });
            },
            scrollText:function(){
                var oDiv = document.getElementById('scroll');
                var oUl = document.getElementById('scrollUl');
                var aLi = oDiv.getElementsByTagName('li');
                var speed = -1;
                var timer = null;
                oUl.innerHTML += oUl.innerHTML;
                oUl.style.width = aLi[0].offsetWidth * aLi.length + 'px';
                function step() {
                     oUl.style.left = oUl.offsetLeft + speed + 'px';
                    if(oUl.offsetLeft < - oUl.offsetWidth / 2){
                        oUl.style.left = '0';
                    }else if(oUl.offsetLeft > 0){
                        oUl.style.left = - oUl.offsetWidth / 2 + 'px';
                    }
                    requestAnimationFrame(step);
                }
                requestAnimationFrame(step);
            }
        },
        created: function () {
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'invitation_adpage_load',
                s0:''
            });
        },
        mounted: function() {
            this.scrollText();
        }
    });
    var mySwiper = new Swiper('.swiper-container', {
        pagination: '.swiper-pagination',
        loop: true,
        autoplay: false,
    });
})
