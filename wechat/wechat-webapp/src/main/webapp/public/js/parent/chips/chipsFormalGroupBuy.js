define(["../../../public/lib/vue/vue.min.js", "../../public/lib/swiper/js/swiper.js", "logger"],function(Vue, Swiper, logger){
    var vm = new Vue({
        el: '#formal_group_buy',
        data: {
            type: 'default',
            tab: 'introduce',
            dialogShow: false,
            productList: [],
            groupList: [],
            sliceGroupList: [],
            sliceIndex: 0,
            productId: '',
            productName: '',
            buyBtnText: '立即开团',
            oprationTrace: [],
            partnerInfo: {},
            directGroupList: [],
            groupSuccess: false
        },
        methods: {
            switchTab: function(e) {
            	if(e.target.id) {
            		this.tab = e.target.id;
            	}
            },
            onDirectGroup: function(code) {
                location.href = '/chips/center/formal_group_buy.vpage?origin=invite&code=' + code;
            },
            onGroup: function(e) {
                var $target = $(e.target)
                if($target.hasClass('group-purchase-btn')) {
                    if($target.data('code')) {
                        this.onBuy($target.data('code'));
                    }
                }
            },
            onBuy: function(groupCode, e) {
                if(e) e.stopPropagation();
                this.oprationTrace.push(groupCode);

                if(!this.dialogShow) {
                    this.dialogShow = true;
                    this.buyBtnText = '立即成团';
                    return;
                }
                if(!this.productId) {
                    return;
                }
                if(this.type === 'invite') {
                    this.buy(this.param().code);
                    return;
                }
                if(!groupCode) {
                    this.buy();
                }else {
                    this.buy(this.oprationTrace[0] || groupCode);
                }
            },
            buy: function(groupCode) {
                var url = "/chips/order/create.vpage?productId=" + this.productId + "&productName=" + this.currentProduct.productName;
                if(groupCode) {
                    url += ('&group=' + groupCode);
                }
                window.location.href = url;
            },
            closeDialog: function() {
                this.dialogShow = false;
                this.groupCode = null;
                this.buyBtnText = this.type === 'invite' ? '立即成团' : '立即开团';
                this.oprationTrace = [];
            },
            onSelectProduct: function(productId) {
                this.productId = productId;
            },
            param: function() {
                var param = {};
                location.search.replace('?', '').split('&').forEach(function(e){
                    param[e.split('=')[0]] = e.split('=')[1];
                });
                return param;
            },
            blank: function() {}
        },
        computed: {
            groupByGrade: function() {
                var groupByGrade = [{
                    name: '初到美国插班生活体验(G1)',
                    products: []
                }, {
                    name: '成为寄宿家庭一员(G2)',
                    products: []
                }, {
                    name: '在美国的第一个暑假(G3)',
                    products: []
                }];
                var g1 = [], g2 = [], g3 = [];
                this.productList.forEach(function(e){
                    switch(e.grade){
                        case 1:
                            groupByGrade[0].products.push(e);
                            break;
                        case 2:
                            groupByGrade[1].products.push(e);
                            break;
                        case 3:
                            groupByGrade[2].products.push(e);
                            break;
                    }
                });
                return groupByGrade;
            },
            leastPrice: function() {
                var leastPrice = {
                    discountPrice: (this.productList[0] && this.productList[0].discountPrice) || 0,
                    originalPrice: (this.productList[0] && this.productList[0].originalPrice) || 0
                }
                this.productList.forEach(function(e){
                    if(e.discountPrice < leastPrice.discountPrice) {
                        leastPrice.discountPrice = e.discountPrice;
                    }
                    if(e.originalPrice < leastPrice.originalPrice) {
                        leastPrice.originalPrice = e.originalPrice;
                    }
                });
                return leastPrice;
            },
            productMap: function() {
                var map = {};
                this.productList.forEach(function(e){
                    map[e.productId] = e;
                });
                return map;
            },
            currentProduct: function() {
                return this.productMap[this.productId] || {};
            }
        },
        filters: {
            normalTime: function(inputTime) {
                if(!inputTime) {
                    return '--年--月--日';
                }
                var date = new Date();
                date.setTime(inputTime);
                var year = date.getFullYear();
                var month = date.getMonth() + 1;
                var day = date.getDate();
                return year + '年' + month + '月' + day + '日';
            },
            timeFormat: function(s) {
                function format(a) {
                    if(a <= 0) {
                        return '00';
                    }
                    if(a < 10) {
                        return '0' + a;
                    }
                    return a + '';
                }

                var hour = Math.floor(s/3600);
                var minute = Math.floor((s % 3600)/60);
                var second = s % 60;
                return format(hour) + ':' + format(minute) + ':' + format(second);
            }
        },
        created: function() {
            var thiz = this;
            if(this.param().origin === 'invite') {
                this.type = 'invite';
                this.buyBtnText = '立即成团';
                $.get('/chips/group/sponsor.vpage', {
                    groupCode: thiz.param().code
                }, function(res) {
                    thiz.partnerInfo = res;
                    thiz.groupSuccess = res.groupSuccess;
                    if(thiz.groupSuccess) {
                       $.get('/chips/group/shopping/list.vpage', {}, function(res){
                            thiz.directGroupList = res.groupList.slice(0, 2);
                            if(thiz.directGroupList.length) {
                                setInterval(function() {
                                    thiz.directGroupList.forEach(function(e){
                                        if(e.surplusTime <= 0) {
                                            return;
                                        }
                                        e.surplusTime -= 1;
                                    });
                                }, 1000);
                            }else {
                                var $second = $('#auto-direct-second');
                                var sec = 3;
                                $second.text(sec--);
                                var secInterval = setInterval(function(){
                                    if(sec <= 0) {
                                        clearInterval(secInterval);
                                        location.href = '/chips/center/formal_group_buy.vpage';
                                    }
                                    $second.text(sec--);
                                }, 1000);
                            }
                        }); 
                    }
                    var interval = setInterval(function() {
                        thiz.partnerInfo.surplusTime -= 1;
                        if(thiz.partnerInfo.surplusTime <= 0) {
                            clearInterval(interval);
                        }
                    }, 1000);
                });
            }else {
                $.get('/chips/group/shopping/list.vpage', {}, function(res){
                    if(res.success) {
                        thiz.groupList = res.groupList;
                        setTimeout(function(){
                            var mySwiper = new Swiper('.swiper-container', {
                                autoplay: 2000,
                                direction : 'vertical',
                                loop: thiz.groupList.length > 2,
                                slidesPerView: 2,
                                slidesPerGroup: 2
                            });
                            if(thiz.groupList.length === 1) {
                                $('.swiper-container').height('3.5rem').find('.user-item').css('marginTop', '0.6rem');
                            }        
                        }, 0);
                        if(thiz.groupList.length) {
                            setInterval(function() {
                                thiz.groupList.forEach(function(e){
                                    if(e.surplusTime <= 0) {
                                        return;
                                    }
                                    e.surplusTime -= 1;
                                });
                            }, 1000);
                        }
                    }else {
                        alert(res.info);
                    }
                });
            }

            $.get("/chips/order/officialProduct/load.vpage",{
                type: 'prod7'
            }, function(res) {
                if(res.success){
                    thiz.productList = res.productList;
                }else{
                    alert(res.info);
                }
            })
        },
        mounted: function() {
            var tab = document.getElementById('tab');
            var middle = document.getElementById('middle');
            $(document).on("scroll",function(){
                if(middle.getBoundingClientRect().top <= 0 || middle.getBoundingClientRect().y <= 0) {
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
