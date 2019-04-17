/*
* create by chunbao.cai on 2018-5-4
* 薯条英语公众号
* -- 广告
*
* */
define(["jquery", "../../public/lib/weixin/jweixin-1.0.0.js","../../../public/lib/vue/vue.min.js","logger", "../../public/lib/swiper/js/swiper.js"],function($,wx,Vue,logger){
    var url = '/chips/center/invite_award_rolling.vpage';

    var vm = new Vue({
        el: '#inviteAwardWrap',
        data:{
            showShareModal: false,
            phoneData:[]
        },
        methods:{
            invitePoster:function(){
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'create_invitation_poster_button_click',
                    s0:''
                });
            }
        },
        watch:{},
        created: function() {
            var _this= this;
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'invitation_activity_page_load',
                s0:''
            });
            $.get(url, {activityType : activityType},
                function(res) {
                if(res.success){
                    _this.phoneData=res.data;
                }else{
                    alert(res.info);
                }
            });
        }
    });




});