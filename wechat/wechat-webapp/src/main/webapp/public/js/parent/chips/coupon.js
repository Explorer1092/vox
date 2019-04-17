/*
* create by chunbao.cai on 2018-5-31
* 薯条英语公众号
* -- 对话实录
*
* */
define(["jquery","logger","../../public/lib/vue/vue.js"],function($,logger,Vue){

    var vm = new Vue({
        el:'#coupon',
        data:{
            toast:false,
            toast_txt:'',
            withdraw_cash_popup:false,
            sign:"NotUsed",
            couponIds:[],
            coupon_all:{
                "NotUsed":[
                    // {
                    //     "couponUserRefId":1,
                    //     "couponName":"小U直减券",
                    //     "couponUserStatus":"NotUsed",
                    //     "createDate":1528344378000,
                    //     "effectiveEndTime":1530115199000,
                    //     "couponId":"5ac1ea55e92b1b38e25cd927",
                    //     "effectiveDateStr":"2018.06.07 - 2018.06.27"
                    // },
                    // {
                    //     "couponUserRefId":2,
                    //     "couponName":"小U直减券",
                    //     "couponUserStatus":"NotUsed",
                    //     "createDate":1528344378000,
                    //     "effectiveEndTime":1530115199000,
                    //     "couponId":"5ac1ea55e92b1b38e25cd927",
                    //     "effectiveDateStr":"2018.06.07 - 2018.06.27"
                    // },
                    // {
                    //     "couponUserRefId":3,
                    //     "couponName":"小U直减券",
                    //     "couponUserStatus":"NotUsed",
                    //     "createDate":1528344378000,
                    //     "effectiveEndTime":1530115199000,
                    //     "couponId":"5ac1ea55e92b1b38e25cd927",
                    //     "effectiveDateStr":"2018.06.07 - 2018.06.27"
                    // },
                    // {
                    //     "couponUserRefId":4,
                    //     "couponName":"小U直减券",
                    //     "couponUserStatus":"NotUsed",
                    //     "createDate":1528344378000,
                    //     "effectiveEndTime":1530115199000,
                    //     "couponId":"5ac1ea55e92b1b38e25cd927",
                    //     "effectiveDateStr":"2018.06.07 - 2018.06.27"
                    // },
                    // {
                    //     "couponUserRefId":5,
                    //     "couponName":"小U直减券",
                    //     "couponUserStatus":"NotUsed",
                    //     "createDate":1528344378000,
                    //     "effectiveEndTime":1530115199000,
                    //     "couponId":"5ac1ea55e92b1b38e25cd927",
                    //     "effectiveDateStr":"2018.06.07 - 2018.06.27"
                    // },
                    // {
                    //     "couponUserRefId":6,
                    //     "couponName":"小U直减券",
                    //     "couponUserStatus":"NotUsed",
                    //     "createDate":1528344378000,
                    //     "effectiveEndTime":1530115199000,
                    //     "couponId":"5ac1ea55e92b1b38e25cd927",
                    //     "effectiveDateStr":"2018.06.07 - 2018.06.27"
                    // },
                    // {
                    //     "couponUserRefId":7,
                    //     "couponName":"小U直减券",
                    //     "couponUserStatus":"NotUsed",
                    //     "createDate":1528344378000,
                    //     "effectiveEndTime":1530115199000,
                    //     "couponId":"5ac1ea55e92b1b38e25cd927",
                    //     "effectiveDateStr":"2018.06.07 - 2018.06.27"
                    // },
                    // {
                    //     "couponUserRefId":8,
                    //     "couponName":"小U直减券",
                    //     "couponUserStatus":"NotUsed",
                    //     "createDate":1528344378000,
                    //     "effectiveEndTime":1530115199000,
                    //     "couponId":"5ac1ea55e92b1b38e25cd927",
                    //     "effectiveDateStr":"2018.06.07 - 2018.06.27"
                    // },
                    // {
                    //     "couponUserRefId":9,
                    //     "couponName":"小U直减券",
                    //     "couponUserStatus":"NotUsed",
                    //     "createDate":1528344378000,
                    //     "effectiveEndTime":1530115199000,
                    //     "couponId":"5ac1ea55e92b1b38e25cd927",
                    //     "effectiveDateStr":"2018.06.07 - 2018.06.27"
                    // },
                    // {
                    //     "couponUserRefId":10,
                    //     "couponName":"小U直减券",
                    //     "couponUserStatus":"NotUsed",
                    //     "createDate":1528344378000,
                    //     "effectiveEndTime":1530115199000,
                    //     "couponId":"5ac1ea55e92b1b38e25cd927",
                    //     "effectiveDateStr":"2018.06.07 - 2018.06.27"
                    // },
                    // {
                    //     "couponUserRefId":11,
                    //     "couponName":"小U直减券",
                    //     "couponUserStatus":"NotUsed",
                    //     "createDate":1528344378000,
                    //     "effectiveEndTime":1530115199000,
                    //     "couponId":"5ac1ea55e92b1b38e25cd927",
                    //     "effectiveDateStr":"2018.06.07 - 2018.06.27"
                    // },
                    // {
                    //     "couponUserRefId":12,
                    //     "couponName":"小U直减券",
                    //     "couponUserStatus":"NotUsed",
                    //     "createDate":1528344378000,
                    //     "effectiveEndTime":1530115199000,
                    //     "couponId":"5ac1ea55e92b1b38e25cd927",
                    //     "effectiveDateStr":"2018.06.07 - 2018.06.27"
                    // }
                ],
                "Used":[],
                "Expired":[]
            }
        },
        computed:{
            coupon_list:function(){
                var _this = this;
                console.log(_this.coupon_all[_this.sign]);
                return _this.coupon_all[_this.sign]
            }
        },
        filters:{
            time:function(t){
                var myDate = new Date(t);
                var y = myDate.getFullYear();
                var m = myDate.getMonth();
                var d = myDate.getDate();
                return y + "." + (m+1) + "." + d;
            }
        },
        methods:{
            change_tab:function(sign){
                var _this = this;
                _this.sign = sign;
                console.log(_this.sign)
            },
            com_toast:function(txt){
                var _this = this;
                _this.toast = true;
                _this.toast_txt = txt;
                setTimeout(function(){
                    _this.toast = false;
                },3000)
            },
            use_coupon:function(){
                // var _this = this;
                // _this.com_toast('请等待课程开放后再使用优惠券购买')
                window.location.href = '/chips/center/robinnormal.vpage'
            },
            withdraw_cash:function(){
                var _this = this;
                if(_this.couponIds.length>=10){
                    _this.withdraw_cash_popup = true;
                }else{
                    _this.com_toast('优惠券大于10张才能提现哦')
                }

            },
            get_cash:function(){
                var _this = this;
                $.post('/chips/coupon/redpack.vpage',{
                    couponIds:_this.couponIds.join(',')
                }).then(function(res){
                    _this.com_toast(res.info)
                })
            },
            close_popup:function(){
                var _this = this;
                _this.withdraw_cash_popup = false
            },
            toggleSelect:function(id){
                var _this = this;
                var index = _this.couponIds.indexOf(id);
                if(index === -1){
                    _this.couponIds.push(id)
                }else{
                    _this.couponIds.splice(index,1)
                }
            },
            select_all:function(){
                var _this = this;

                if(_this.couponIds.length === _this.coupon_all['NotUsed'].length){
                    _this.couponIds = []
                }else{
                    _this.coupon_all['NotUsed'].forEach(function(item){
                        if(_this.couponIds.indexOf(item.couponUserRefId) === -1){
                            _this.couponIds.push(item.couponUserRefId)
                        }
                    })
                }
            }
        },
        created:function(){
            var _this = this;
            $.get('/chips/center/mycoupon.vpage').then(function(res){
                if(res.success){
                     _this.coupon_all = res;
                }else{
                    _this.com_toast(res.info)
                }
            })
        }
    })

});