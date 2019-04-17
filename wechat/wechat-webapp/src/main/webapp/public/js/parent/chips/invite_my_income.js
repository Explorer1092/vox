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
            flag:true,
            drawableAcount: '',
            todayAcount: '',
            totalAcount: '',
            img:'https://v.17zuoye.cn/ai-teacher/chips/test/image/2018/12/20181204155915894612.jpeg',//个人收入中心页-头像
            friendList:false,
            titleBar:['已浏览好友','未付款好友','邀请购买成功好友'],
            count:'',
            headerImgList:[],
            invitNum:'',
            paidNum:'',
            noPaidNum:'',
        },
        methods:{
            shareFriend:function(num){

                var _this=this;
                $('#titleBar').html(this.titleBar[num]);
                _this.friendList=true;
                var url = "/chips/center/invite_award_detail.vpage?userId="+"268948"+"&type="+ num;
                $.get(url, function(res) {
                    if(res.success){
                        _this.count=res.count;
                        _this.headerImgList=res.data;
                    }else{
                        alert(res.info);
                    }
                });
            },
            shareEarn:function(){
                window.location.href = '/chips/center/invite_award_activity.vpage';
            },
            extractMoney:function(){
                alert('你推荐的好友上课结束时才可以提现哦')

            }
        },
        created: function() {
            var _this=this;
            var url = "/chips/center/invite_award_myreward.vpage?userId="+"268948";
            $.get(url, function(res) {
                if(res.success){
                    _this.image=res.img;
                    _this.invitNum=res.invitNum;
                    _this.noPaidNum=res.noPaidNum;
                    _this.paidNum=res.paidNum;
                    _this.drawableAcount=res.drawableAcount;
                    _this.totalAcount=res.totalAcount;
                    _this.todayAcount=res.todayAcount;
                    _this.flag=res.flag;
                    console.log( _this.totalAcount)
                }else{
                    alert(res.info);
                }
            });
        }
    });


});