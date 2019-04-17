/*
* create by chunbao.cai on 2018-5-4
* 薯条英语公众号
* -- 广告
*
* */
define(["jquery","logger","../../public/lib/vue/vue.min.js","../../public/lib/weixin/jweixin-1.0.0.js"],function($,logger,Vue,wx){

    function getParams(name){
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURI(r[2]); return null;
    }

    var vm = new Vue({
        el:'#new_be',
        data:{
            tab: 'introduce',
            dialog_show: false,
            sign:'0_0',
            leastPrice: null,
            beginDate:'1545580800000',
            price: '--',
            surplusInit: '--',
            surplus: '--',
            productId:'',
            productName:'',
            courseData:[
                {
                    name:'初到美国插班生活体验(G1)',
                    info:[
                        {
                            "name":"高级班",
                            "productId":"5bf4cafeac745961f1c87f26",
                            "productName":"初到美国插班生活体验(下)第1期",
                            "courses":50,
                            "price":599,
                            "originalPrice":799,
                            "beginDate":1545580800000,
                            "paid":false
                        },
                        {
                            "name":"保过班",
                            "productId":"5bf8c504ac745963494f167c,5bf8c570ac745963494f16e7",
                            "productName":"初到美国插班生活体验第2期",
                            "courses":100,
                            "price":1198,
                            "originalPrice":1598,
                            "beginDate":1551628800000,
                            "paid":false
                        }
                    ]
                },
                {
                    name:'成为寄宿家庭一员(G2)',
                    info:[
                        {
                            "name":"高级班",
                            "productId":"5bf4cb32ac745961f1c87f60",
                            "productName":"成为寄宿家庭一员(下)第1期",
                            "courses":50,
                            "price":599,
                            "originalPrice":799,
                            "beginDate":1545580800000,
                            "paid":false
                        },
                        {
                            "name":"保过班",
                            "productId":"5bf8c5d8ac745963494f1740,5bf8c60c8edbc8546dcc4ae9",
                            "productName":"成为寄宿家庭一员第2期",
                            "courses":100,
                            "price":1198,
                            "originalPrice":1598,
                            "beginDate":1551628800000,
                            "paid":false
                        }
                    ]
                },
                {
                    name:'在美国的第一个暑假(G3)',
                    info:[
                        {
                            "name":"高级班",
                            "productId":"5bf4cb5a8edbc8310e25e233",
                            "productName":"在美国的第一个暑假(下)第1期",
                            "courses":62,
                            "price":699,
                            "originalPrice":999,
                            "beginDate":1550419200000,
                            "paid":false
                        },
                        {
                            "name":"保过班",
                            "productId":"5bf8c659ac745963494f1783,5bf8c813ac745963494f1922",
                            "productName":"在美国的第一个暑假第2期",
                            "courses":124,
                            "price":1398,
                            "originalPrice":1998,
                            "beginDate":1551628800000,
                            "paid":false
                        }
                    ]
                }
            ]
        },
        methods:{
            switchTab: function(e) {
                this.tab = e.target.id;
            },
            show_dialog: function(){
                if(this.productId){
                    if(this.surplus !== 0 && this.surplus !== '--') {
                        this.buy();
                    }
                }else{
                    this.dialog_show = true;
                    this.productId = this.courseData[0].info[0].productId;
                    this.productName = this.courseData[0].info[0].productName;
                    this.surplus = this.courseData[0].info[0].surplus;
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'newmainad_purchasebutton_click',
                        s0: '唤起选择产品弹窗',
                        s1: this.productName
                    });
                }
            },
            close_dialog: function(){
                this.dialog_show = false;
                this.price = this.leastPrice;
                this.sign = '0_0';
                this.productId = '';
                this.productName = '';
                this.surplus = this.surplusInit;
            },
            change_course: function(index,sub_index){
                this.sign = index+'_'+sub_index;
                this.beginDate = this.courseData[index].info[sub_index].beginDate;
                this.originalPrice = this.courseData[index].info[sub_index].originalPrice;
                this.surplus = this.courseData[index].info[sub_index].surplus;
                this.price = this.courseData[index].info[sub_index].price;
                this.courseType = this.courseData[index].info[sub_index].name;
                this.productId = this.courseData[index].info[sub_index].productId;
                this.productName = this.courseData[index].info[sub_index].productName;
            },
            buy:function(){
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'newmainad_purchasebutton_click',
                    s0: '购买',
                    s1: this.productName
                });
                window.location.href = "/chips/order/create.vpage?productId=" + this.productId + "&productName=" + this.productName;
            },
            blank:function(){}
        },
        filters:{
            normalTime:function(inputTime){
                var date = new Date();
                date.setTime(inputTime);
                var year = date.getFullYear();
                var month = date.getMonth() + 1;
                var day = date.getDate();

                return year+"年"+month+"月"+day+"日";
            }
        },
        created: function() {
            var _this = this;
            $.get("/chips/order/officialProduct/load.vpage",{
                type:'prod2'
            }, function(res) {
                if(res.success){
                    var g1 = [], g2 = [], g3 = [], leastPrice = res.productList[0].price;
                    res.productList.forEach(function(e){
                        if(leastPrice > e.price) {
                            leastPrice = e.price;
                        }
                        if(e.grade === 1) {
                            g1.push(e);
                        }else if (e.grade === 2) {
                            g2.push(e);
                        }else if (e.grade === 3) {
                            g3.push(e);
                        }
                    });
                    _this.courseData[0].info = g1;
                    _this.courseData[1].info = g2;
                    _this.courseData[2].info = g3;
                    _this.surplus = _this.surplusInit = res.surplus;
                    _this.leastPrice = leastPrice;
                    _this.price = leastPrice;
                }else{
                    alert(res.info);
                }
            })
        },
        mounted: function() {
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'newmainad_load'
            });
            var tab = document.getElementById('tab');
            var middle = document.getElementById('middle');
            $(document).on("scroll",function(){
                if(middle.getBoundingClientRect().y <= 0) {
                    $(tab).addClass('sticky');
                    $(middle).css('padding-top', '2rem');
                }else {
                    $(middle).css('padding-top', '0rem');
                    $(tab).removeClass('sticky')
                }
            });
        }
    });

});