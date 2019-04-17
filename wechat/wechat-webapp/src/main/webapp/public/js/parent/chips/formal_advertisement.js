define(["../../../public/lib/vue/vue.min.js", "logger"],function(Vue, logger){
    var vm = new Vue({
        el:'#formal-advertisement',
        data: {},
        mounted: function() {
        	logger.log({
                module: 'm_XzBS7Wlh',
                op: 'mainbelowad_load',
                s0: productName
            });
        },
        methods: {
            buy: function(){
            	logger.log({
					module: 'm_XzBS7Wlh',
					op: 'mainbelowad_load',
					s0: productName
				});
                window.location.href = "/chips/order/create.vpage?productId=" + productId;
            }
        }
    });
});
