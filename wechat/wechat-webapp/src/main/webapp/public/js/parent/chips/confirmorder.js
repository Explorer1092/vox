/*
* create by chunbao.cai on 2018-5-4
* 薯条英语公众号
* -- 广告
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js","../../public/lib/weixin/jweixin-1.0.0.js"],function($,logger,Vue){
    function getParams(name){
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURI(r[2]); return null;
    }
    var duration = getParams('duration') || 'noinfo';
    var inviter = getParams('inviter') || 'noinfo';
    var app_inviter = getParams('app_inviter') || 'noinfo';
    var vm = new Vue({
        el:'#confirmorder',
        data:{
            price:0,
            productName:'',
            discountPrice:0,
            createDate:'',
            coupons:[],
            select_coupon:false,
            iscoupon:true,
            couponNum:0,
            couponIndex:0
        },
        computed:{},
        components:{
            "coupon":{
                template:'#coupon',
                data:function(){
                    return {
                        sign:0
                    }
                },
                computed:{
                    couponPrice:function(){
                        var _this = this;
                        if(_this.sign >= 0){
                            if(_this.couponarr.length > 0){
                                return _this.couponarr[_this.sign].typeValue
                            }
                        }else{
                            return 0
                        }

                    }
                },
                props:['couponarr'],
                methods:{
                    confirm:function(){
                        var _this = this;
                        _this.$emit("confirm",_this.sign)
                    },
                    choseCoupon:function(index){
                        if(this.sign === index){
                            this.sign = -1;
                        }else{
                            this.sign = index;
                        }
                    }
                },
                created:function(){
                    console.log(this.sign)
                }
            }
        },
        methods:{
            select:function(){
                this.select_coupon = !this.select_coupon;
            },
            usecoupon:function(index){
                var _this = this;
                _this.select_coupon = !_this.select_coupon;
                _this.couponIndex = index;
                if(index >= 0){
                    //使用了优惠券
                    _this.iscoupon = true;
                    _this.discountPrice = _this.coupons[index].discountPrice;
                    _this.couponNum = _this.coupons[index].typeValue;
                }else{
                    //不使用优惠券
                    _this.iscoupon = false;
                    _this.discountPrice = _this.price;
                }

            },
            pay:function(){
                // m_XzBS7Wlh  确认支付按钮被点击   click_invitation_payment_page  一年以下（less）一年以上（more）分享人userid
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'click_invitation_payment_page',
                    s0: duration,
                    s1: inviter || app_inviter
                });
                var _this = this;

                var orderId = $("#orderid").val();
                if (_this.coupons.length > 0 && _this.couponIndex >= 0 && _this.couponIndex < _this.coupons.length) {
                    var refId = _this.coupons[_this.couponIndex].couponUserRefId;
                    var couponId = _this.coupons[_this.couponIndex].couponId;
                    $.post("/chips/order/relatedcouponorder.vpage",{
                        orderId:orderId,
                        refId:refId,
                        couponId:couponId,
                    },function(res){
                        if(res.success){
                            window.location.href = "/chips/order/confirm.vpage?orderId="+orderId;
                        }else{
                            alert(res.info)
                        }
                    });
                } else {
                    window.location.href = "/chips/order/confirm.vpage?orderId="+orderId;
                }
            }
        },
        created:function(){
            // m_XzBS7Wlh  确认支付页面被加载   load_invitation_payment_page  一年以下（less）一年以上（more）分享人userid
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'load_invitation_payment_page',
                s0: duration,
                s1: inviter || app_inviter
            });
            var _this = this;
            $.get("/chips/order/detail.vpage",{
                orderId:$("#orderid").val()
            },function(res){
                console.log(res)
                if(res.success){
                    _this.price = res.price;
                    _this.productName = res.productName;
                    _this.discountPrice = res.discountPrice;
                    _this.createDate = res.createDate;
                    _this.coupons = res.coupons;
                    _this.iscoupon = res.coupons.length > 0;
                    _this.couponNum = res.coupons.length > 0 ? res.coupons[0].typeValue : 0;
                }else{
                    alert(res.info)
                }
            })
        }
    })

});