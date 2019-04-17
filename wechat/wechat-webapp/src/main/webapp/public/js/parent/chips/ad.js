/*
* create by chunbao.cai on 2018-5-4
* 薯条英语公众号
* -- 广告
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js","../../public/lib/weixin/jweixin-1.0.0.js"],function($,logger,Vue,wx){


    $(function(){

        if(window.navigator.userAgent.indexOf('vivo') > -1){
            $("#video_img").show();
            $("#vid").hide()
        }else{
            $("#video_img").hide();
            $("#vid").show()
        }

        $(".listenBtn").click(function(){
            // 购买_购买广告页_79元购买按钮_被点击 打点
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'purchase_adpage_probation_click'
            });
        });

        $(".buy-btn").click(function(){
            // var _this = $(this);
            // window.location.href = "/chips/center/reservepay.vpage?productId="+_this.data('productid')+"&inviter="+_this.data('inviter');
            // 购买_购买广告页_79元购买按钮_被点击 打点
            setTimeout(function(){
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'purchase_adpage_purchasebutton_click'
                });
            },100)
        });

        $("#ad").scroll(function(){
            var h = $(this).height();
            var sh = $(this)[0].scrollHeight;
            var st =$(this)[0].scrollTop;
            if(h+st>=sh){
                //购买_购买广告页_底屏
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'purchase_adpage_bottom_screen'
                });
            }
        });


        //页面加载 打点
        logger.log({
            module: 'm_XzBS7Wlh',
            op: 'purchase_adpage_load'
        });
    });

    var vm = new Vue({
        el:'#ad',
        data:{
            imgs: [
                "/public/images/parent/chips/10.png",
                "/public/images/parent/chips/1.png",
                "/public/images/parent/chips/2.png",
                "/public/images/parent/chips/3.png",
                "/public/images/parent/chips/4.png",
                "/public/images/parent/chips/5.png",
                "/public/images/parent/chips/6.png",
                "/public/images/parent/chips/7.png",
                "/public/images/parent/chips/8.png",
                "/public/images/parent/chips/9.png",
            ],
            clientX: 0,
            clientY: 0,
            moveX: 0,
            moveY: 0,
            endX: 0,
            cssText: 'transition:none;transform:translateX(0%)',
            sign:0
        },
        computed:{
            nextSign0:function(){
                var _this = this;
                var tmp = _this.sign+1;
                if(tmp>9){
                    tmp = 0;
                }
                return tmp;
            },
            nextSign1:function(){
                var _this = this;
                var tmp = _this.nextSign0+1;
                if(tmp>9){
                    tmp = 0;
                }
                return tmp;
            },
            nextSign2:function(){
                var _this = this;
                var tmp = _this.nextSign1+1;
                if(tmp>9){
                    tmp = 0;
                }
                return tmp;
            },
            nextSign3:function(){
                var _this = this;
                var tmp = _this.nextSign2+1;
                if(tmp>9){
                    tmp = 0;
                }
                return tmp;
            },
        },
        methods:{
            next:function() {
                var _this = this;
                _this.sign++;
                if(_this.sign > 9){
                    _this.sign = 0
                }
            },
            prev:function() {
                var _this = this;
                _this.sign--;
                if(_this.sign < 0){
                    _this.sign = 9
                }
            },
            handleTouchStart:function() {
                var _this = this;
                // document.body.addEventListener('touchmove', _this.preventDefault, false);
                _this.clientX = event.changedTouches[0].pageX;
                _this.clientY = event.changedTouches[0].pageY;
            },
            handleTouchMove:function() {
                var _this = this;
                _this.moveX = event.changedTouches[0].pageX;
                _this.moveY = event.changedTouches[0].pageY;
                if (Math.abs(_this.moveY - _this.clientY) > 50) {
                    // document.body.removeEventListener('touchmove', _this.preventDefault, false);
                }
                var imgW = document.getElementsByClassName("second")[0].children[0].offsetWidth;
                var percent = Math.ceil((_this.moveX - _this.clientX) / imgW * 100);
                _this.cssText = "transition:none;transform:translateX("+percent+"%)";
                document.getElementsByClassName("second")[0].children[0].style.cssText = _this.cssText;
            },
            handleTouchEnd:function() {
                var _this = this;
                // document.body.removeEventListener('touchmove', _this.preventDefault, false);
                _this.endX = event.changedTouches[0].pageX;
                _this.cssText = "transition:0.3s;transform:translateX(0%)";
                document.getElementsByClassName("second")[0].children[0].style.cssText = _this.cssText;
                var condition = _this.endX - _this.clientX;

                if (condition > 50) {
                    _this.prev();
                    //购买_购买广告页_课程特色人物_被点击
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'purchase_adpage_curriculum_characteristics_click',
                        s0:_this.sign
                    });
                } else if (condition < -50) {
                    _this.next();
                    //购买_购买广告页_课程特色人物_被点击
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'purchase_adpage_curriculum_characteristics_click',
                        s0:_this.sign
                    });
                } else {
                }
            },
            preventDefault:function(e) {
                e.preventDefault()
            },
            play:function(){
                var myVideo = document.getElementById('vid');
                myVideo.play()
            }
        },
        created:function(){},
        mounted:function(){
            if(window.navigator.userAgent.indexOf('vivo') > -1 || window.navigator.userAgent.indexOf('VIVO') > -1){
                $("#video_img").show();
                $("#vid").remove();
            }else{
                var myVideo = document.getElementById('vid');
                myVideo.addEventListener("canplay", function() {
                    myVideo.play();
                },false);
                document.addEventListener("WeixinJSBridgeReady", function() {
                    myVideo.play();
                }, false);
                myVideo.play();
                wx.ready(function(){
                    myVideo.play();
                })
            }

            var startTime = new Date().getTime();
            var isFirst = true;
            var isMiddle = true;
            var isFooter = true;
            window.addEventListener("touchmove",function(){
                var adElem = $("#ad");
                var screenHt = window.screen.height;

                if(adElem.scrollTop() > screenHt && isFirst){
                    isFirst = false;
                    console.log(new Date().getTime() - startTime);
                    // 购买_购买广告页_滑过第1屏
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'purchase_adpage_1stscroll',
                        s0:new Date().getTime() - startTime
                    });
                    startTime = new Date().getTime();
                }else if(adElem.scrollTop() > screenHt*2 && isMiddle){
                    isMiddle = false;
                    console.log(new Date().getTime() - startTime);
                    // 	购买_购买广告页_滑过第2屏
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'purchase_adpage_2ndscroll',
                        s0:new Date().getTime() - startTime
                    });
                    startTime = new Date().getTime();
                }

            });

            $("#ad").scroll(function(){
                var h = $(this).height();
                var sh = $(this)[0].scrollHeight;
                var st =$(this)[0].scrollTop;
                if(h+st>=sh-10 && isFooter){

                    isFooter = false;
                    console.log(new Date().getTime() - startTime);

                    // 	购买_购买广告页_底屏
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'purchase_adpage_bottom_screen'
                    });

                    // 购买_购买广告页_滑到底屏
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'purchase_adpage_lastscroll',
                        s0:new Date().getTime() - startTime
                    });
                }
            });

            //	购买_购买广告页_首屏_被点击
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'purchase_adpage_first_screen_click'
            });
        }
    })

});