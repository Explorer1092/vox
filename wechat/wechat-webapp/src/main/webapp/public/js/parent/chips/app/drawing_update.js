define(["jquery", "../../../public/lib/vue/vue.min.js", "logger"],function($, Vue, logger){
	var unitId = '', userVideo = '', optionText = '', userName = 'TA';
	function getParams(name){
		var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
		var r = window.location.search.substr(1).match(reg);
		if (r != null) return decodeURI(r[2]); return null;
	}
	var drawingTaskId = getParams('t'), submitable = true;
	$('.task-modal-close').on('click', function(e){
		$('.task-modal').hide();
		$('.video-wrapper').hide();
		$('#fake-poster').show();
		$('.option').removeClass('right').removeClass('wrong').removeClass('active').show();
		$('#toast').hide();
		$('#submit').show();
		submitable = true;
	});
    var vm = new Vue({
        el:'#drawing_update',
        data:{
            friendsList:[],
            preview:getParams('preview'),
            qrUrl:''
        },
        methods:{
            intoFriend:function(item){
                location.href='/chips/task/friend_detaile.vpage?user='+item.user+'&avatar='+item.avatar+'&uname='+item.uname;
            },
            // 我也要学习课程
            learnCourse:function(){
               location.href=this.qrUrl;
            }
        },
        created:function(){
            var _this=this
            $.get('/chips/task/drawing/detail.vpage?drawingTaskId=' + drawingTaskId, function(result){
                if(result.success) {
                    unitId = result.unitId;
                    // m_XzBS7Wlh	分享链接页面_被加载	sharelink_load	unitid
                    logger.log({
                        module: 'm_XzBS7Wlh',
                        op: 'sharelink_load',
                        s0: unitId,
                        s1: '',
                        s2: '',
                        s3: getParams("u")
                    });
                    userVideo = result.userVideo;
                    userName = result.userName || 'Ta';
                    _this.qrUrl=result.qrUrl;
                    $('#avatar').attr('src', result.userAvatar || 'https://cdn-portrait.17zuoye.cn/upload/images/avatar/avatar_normal.png');
                    $('#day').text(result.studyNum);
                    $('#sentence').text(result.sentenceNum);
                    $('#use-name').text(result.userName || '我');
                    $('#video-poster').attr('src', result.cover).on('load', function(){
                        $('#video').attr({poster: result.cover, src: userVideo}).height($('#video-poster').height());
                    });
                    _this.friendsList=result.joiner;
                    title = "挑战英语对话的第"+ result.studyNum + "天：" + result.cardTitle;
                    var st = null;
                    $('#qrUrl').attr('src', "/chips/qrcode.vpage?url=" + encodeURIComponent(result.qrUrl) + "&icon=http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png&color=-710775296").on('touchstart', function(){
                        st = setTimeout(function(){
                            // m_XzBS7Wlh	分享页面_二维码_被长按	sharepage_qrcode_longpress	unitid	 	 	 	按住时间大于1s算作被长按
                            logger.log({
                                module: 'm_XzBS7Wlh',
                                op: 'sharepage_qrcode_longpress',
                                s0: unitId,
                                s1:'',
                                s2:'',
                                s3: getParams("u")
                            });
                        }, 1000);
                    }).on('touchend', function(){
                        clearTimeout(st);
                    });
                    var video = document.getElementById('video');
                    video.addEventListener("ended",function(){
                        $('#fake-poster').show();
                        $('#video-wrapper').hide();
                        $('.task-modal').show();
                        // m_XzBS7Wlh	回答问题页面_被加载	answerquestionspage_load	unitid
                        logger.log({
                            module: 'm_XzBS7Wlh',
                            op: 'answerquestionspage_load',
                            s0: unitId,
                            s1: '',
                            s2: '',
                            s3: getParams("u")
                        });
                    });
                    video.addEventListener("play",function(){
                        $('#fake-poster').hide();
                        $('#video-wrapper').show();

                        // m_XzBS7Wlh	分享页面_视频播放按钮_被点击	sharepage_videoplaybutton_click	unitid	视频url
                        logger.log({
                            module: 'm_XzBS7Wlh',
                            op: 'sharepage_videoplaybutton_click',
                            s0: unitId,
                            s1: userVideo,
                            s2: '',
                            s3: getParams("u")
                        });
                    });
                    $('#playBtn').on('click', function(){
                        var src = $("#video").attr("src");
                        if (src == null || src == "" ) {
                            alert("视频正在处理中，请稍后");
                            return;
                        }
                        if (src == "#" ) {
                            alert("视频正在审核中，请稍后");
                            return;
                        }
                        if (src == "*" ) {
                            alert("视频违规，已删除");
                            return;
                        }
                        $('#fake-poster').hide();
                        $('#video-wrapper').show();
                        video.play();
                    });
                    var html = '';
                    result.choiceOptions.forEach(function(e){
                        html += '<div class="option">' + e + '</div>'
                    });
                    $('#options').html(html);
                    $('.option').on('click', function(e){
                        e.stopPropagation();
                        $('.option').removeClass('active');
                        $(this).addClass('active');
                    });
                    $('#submit').click(function(e){
                        e.stopPropagation();
                        if(!submitable) {
                            return;
                        }
                        var userAnswer = $('.option.active').text();
                        if(!userAnswer) {
                            alert('请选择答案');
                            return;
                        }
                        submitable = false;
                        $.post('/chips/task/drawing/todo.vpage', {
                            userAnswer: userAnswer,
                            drawingTaskId: drawingTaskId
                        }, function(res){
                            if(res.success) {
                                $('.option.active').addClass(res.master ? 'right' : 'wrong').siblings('.option').addClass(!res.master ? 'right' : 'wrong');

                                if(res.master) {
                                    $('#toast #msg01').text('回答正确');
                                    $('#toast #msg02').text('成功帮助' + userName + '获得能量值！');
                                    $('.option.wrong').hide();
                                }else {
                                    $('#toast #msg01').text('回答错误');
                                    $('#toast #msg02').text('再努力一下！');
                                    $('.option.right').hide();
                                }
                                $('#toast').show();
                                $('#submit').hide();


                                // m_XzBS7Wlh	回答问题页面_提交_被点击	answerquestionspage_submit_click	unitid	提交文本text	正误（true/false)
                                logger.log({
                                    module: 'm_XzBS7Wlh',
                                    op: 'answerquestionspage_submit_click',
                                    s0: unitId,
                                    s1: userAnswer,
                                    s2: res.master,
                                    s3: getParams("u")
                                });
                            }else {
                                alert(res.info);
                                location.reload();
                            }
                        });
                    });
                }
            });
        }

    });
	document.body.onunload = function(){
		// m_XzBS7Wlh	分享页面_关闭按钮 _被点击	sharepage_closebutton_click	unitid
		logger.log({
			module: 'm_XzBS7Wlh',
			op: 'sharepage_closebutton_click',
			s0: unitId,
			s1: '',
			s2: '',
            s3: getParams("u")
		});
	};
});