new Vue({
    el:'#page-index',
    data:{
        categoryOneShow: false,
        categoryList: [
            {
                name: '教师等级特权专区',
                id: 1,
                child: [
                    {
                        name: '高级',
                        id: 2,
                    },
                    {
                        name: '特级',
                        id: 3,
                    }
                ]
            },
            {
                name: '公益专区',
                id: 1,
                child: [
                    {
                        name: '高级',
                        id: 2,
                    },
                    {
                        name: '特级',
                        id: 3,
                    }
                ]
            },
            {
                name: '一起专属',
                id: 1,
                child: [
                    {
                        name: '高级',
                        id: 2,
                    },
                    {
                        name: '特级',
                        id: 3,
                    }
                ]
            },
            {
                name: '大家都喜欢',
                id: 1,
                child: [
                    {
                        name: '高级',
                        id: 2,
                    },
                    {
                        name: '特级',
                        id: 3,
                    }
                ]
            },
        ]
    },
    created:function(){
        var _this = this;
    },
    methods:{
        category_all_button: function() {
            console.log(9)
            var vm = this;
            vm.categoryOneShow = true;
        }
    },

});