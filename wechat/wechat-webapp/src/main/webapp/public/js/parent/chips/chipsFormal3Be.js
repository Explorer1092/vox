define(["../../../public/lib/vue/vue.min.js", "logger"],function(Vue, logger){
    var vm = new Vue({
        el: '#formal-advertisement',
        data: {
            type: type,
            grade: type === 'prod3' ? 2 : 3,
            originalPrice: '--',
            price: '--',
            productId: null,
            clientX: 0,
            clientY: 0,
            moveX: 0,
            moveY: 0,
            endX: 0,
            cssText: 'transition:none;transform:translateX(0%)',
            sign:0
        },
        created: function(){
            var thiz = this;
            $.get("/chips/order/officialProduct/load.vpage",{
                type: this.type
            }, function(res) {
                if(res.success){
                    thiz.originalPrice = res.productList[0].originalPrice;
                    thiz.price = res.productList[0].price;
                    thiz.productId = res.productList[0].productId;
                    thiz.productName = res.productList[0].productName;
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'mainbelowad_load',
                        s0: thiz.productName
                    });
                }else{
                    alert(res.info);
                }
            })
        },
        computed:{
            imgs: function() {
                if(this.grade === 2) {
                    return [
                        '/public/images/parent/chips/formal_3/G2/8.png?v=6af5258020',
                        '/public/images/parent/chips/formal_3/G2/1.png?v=6af5258020',
                        '/public/images/parent/chips/formal_3/G2/2.png?v=6af5258020',
                        '/public/images/parent/chips/formal_3/G2/3.png?v=6af5258020',
                        '/public/images/parent/chips/formal_3/G2/4.png?v=6af5258020',
                        '/public/images/parent/chips/formal_3/G2/5.png?v=6af5258020',
                        '/public/images/parent/chips/formal_3/G2/6.png?v=6af5258020',
                        '/public/images/parent/chips/formal_3/G2/7.png?v=6af5258020'
                    ];
                }else {
                    return [
                        '/public/images/parent/chips/formal_3/G3/8.png?v=6af5258020',
                        '/public/images/parent/chips/formal_3/G3/1.png?v=6af5258020',
                        '/public/images/parent/chips/formal_3/G3/2.png?v=6af5258020',
                        '/public/images/parent/chips/formal_3/G3/3.png?v=6af5258020',
                        '/public/images/parent/chips/formal_3/G3/4.png?v=6af5258020',
                        '/public/images/parent/chips/formal_3/G3/5.png?v=6af5258020',
                        '/public/images/parent/chips/formal_3/G3/6.png?v=6af5258020',
                        '/public/images/parent/chips/formal_3/G3/7.png?v=6af5258020'
                    ];
                }
            },
            nextSign0: function(){
                var _this = this;
                var tmp = _this.sign+1;
                if(tmp>7){
                    tmp = 0;
                }
                return tmp;
            },
            nextSign1: function(){
                var _this = this;
                var tmp = _this.nextSign0+1;
                if(tmp>7){
                    tmp = 0;
                }
                return tmp;
            },
            nextSign2: function(){
                var _this = this;
                var tmp = _this.nextSign1+1;
                if(tmp>7){
                    tmp = 0;
                }
                return tmp;
            },
            nextSign3: function(){
                var _this = this;
                var tmp = _this.nextSign2+1;
                if(tmp>7){
                    tmp = 0;
                }
                return tmp;
            }
        },
        methods: {
            buy: function() {
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'mainad_purchasebutton_click',
                    s0: this.productName
                });
                window.location.href = "/chips/order/create.vpage?productId=" + this.productId + "&productName=" + this.productName;
            },
            next: function() {
                var _this = this;
                _this.sign++;
                if(_this.sign > 9){
                    _this.sign = 0
                }
            },
            prev: function() {
                var _this = this;
                _this.sign--;
                if(_this.sign < 0){
                    _this.sign = 9
                }
            },
            handleTouchStart: function() {
                var _this = this;
                _this.clientX = event.changedTouches[0].pageX;
                _this.clientY = event.changedTouches[0].pageY;
            },
            handleTouchMove: function() {
                var _this = this;
                _this.moveX = event.changedTouches[0].pageX;
                _this.moveY = event.changedTouches[0].pageY;
                var imgW = document.getElementsByClassName("second")[0].children[0].offsetWidth;
                var percent = Math.ceil((_this.moveX - _this.clientX) / imgW * 100);
                _this.cssText = 'transition:none;transform:translateX(' + percent + '%)';
                document.getElementsByClassName("second")[0].children[0].style.cssText = _this.cssText;
            },
            handleTouchEnd: function() {
                var _this = this;
                _this.endX = event.changedTouches[0].pageX;
                _this.cssText = 'transition:0.3s;transform:translateX(0%)';
                document.getElementsByClassName("second")[0].children[0].style.cssText = _this.cssText;
                var condition = _this.endX - _this.clientX;

                if (condition > 50) {
                    _this.prev();
                } else if (condition < -50) {
                    _this.next();
                } else {
                }
            }
        }
    });
});
