define(["jquery", "../../../public/lib/vue/vue.min.js", "logger"],function($, Vue, logger){
	var unitId = '', userVideo = '', optionText = '', userName = 'TA';
	function getParams(name){
		var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
		var r = window.location.search.substr(1).match(reg);
		if (r != null) return decodeURI(r[2]); return null;
	}
	var drawingTaskId = getParams('t'), submitable = true;
    // 激励体系 - 图鉴 - 用户好友能量
    var vm = new Vue({
        el:'#friend_energy',
        data:{
            friendsList:[],
            buy:true,
            uname:getParams('uname'),
            avatar:getParams('avatar'),
            drawingList:[],
            totalEnergy:'',
            totalFinish:'',
            totalCard:'',
            user:getParams('user')
        },
        methods:{
            inviteFriend:function(){
                location.href='/chips/center/invite_award_activity.vpage'
            }
        },
        created:function(){
            var _this=this;
            $.get('/chips/task/drawing/userinfo.vpage?user='+_this.user,function(result){
                _this.buy=result.buy;
                _this.drawingList=result.drawingList;
                _this.totalEnergy=result.totalEnergy;
                _this.totalFinish=result.totalFinish;
                _this.totalCard=result.totalCard;
            })
        }
    });
});