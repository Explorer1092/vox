/*
* create by chunbao.cai on 2018-5-4
* 薯条英语公众号
* -- 广告
*
* */
define(["../../../public/lib/vue/vue.min.js","jquery", "../../public/lib/weixin/jweixin-1.0.0.js", "../../public/lib/swiper/js/swiper.js"],function(Vue,$,wx){
    var vm = new Vue({
        el: '#personalWrap',
        data:{
            obj:[],
            arr:[],
            img:'',
            count:'',
            headerImgList:[],
            titleBar: ['已浏览好友','未付款好友','邀请购买成功好友'],
            num: 0
        },
        methods:{
            shareEarn:function(){
                window.location.href = '/chips/center/invite_award_activity.vpage';
            }

        },
        mounted:function(){
        },
        created: function() {
            // var obj = [];
            var request = (function (){
                var param = {};
                var arr = window.location.search.slice(1).split("&");
                for (var i = 0, len = arr.length; i < len; i++) {
                    var nv = arr[i].split("=");
                    param[nv[0]] = nv[1];
                }
                return param;
                // var textArr=['已浏览好友','未付款好友','邀请购买成功好友'];
                // var text=document.getElementById('titleBar');
                // var peoplenNum=document.getElementById('people_num');
                // text.innerHTML=textArr[obj[0][1]];
                // peoplenNum.innerHTML=decodeURI(obj[1][1]);
            })();
            this.num = request.num;
            var url = "/chips/center/invite_award_detail.vpage?activityType=" + request.activityType + "&type="+ request.num;
            // var url="/chips/center/invite_award_detail.vpage?type="+obj[0][1];
            var _this = this;
            $.get(url, function(res) {
                if(res.success){
                    _this.headerImgList=res.data;
                }else{
                    alert(res.info);
                }
            });
        }
    });


});