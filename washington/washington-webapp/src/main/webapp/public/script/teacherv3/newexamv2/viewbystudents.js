/**
 * Created by dell on 2017/10/26.
 * 按学生查看
 */
;(function(){
	var defaultOptions = {
		clazzId : null,
		examId  : null
	};
	function ViewByStudents(options){
		this.opt = $.extend(true,{},defaultOptions,options);
		this.stResult = ko.observable({});
		this.init();
	}
	ViewByStudents.prototype = {
		constructor : ViewByStudents,
		name : "ViewByStudents",
		init : function(){
			var self = this;
			var paramObj = {
				clazzId : self.opt.clazzId,
				examId  : self.opt.examId
			};
			$.get("/teacher/newexam/report/newstudents.vpage",paramObj).done(function(res){
				if(res.success){
					self.stResult(res);
					$17.voxLog({
						module : "m_yJO2o3u3",
						op     : "o_3AxcIdnE",
						s0     : self.opt.examId
					});
				}else{
					$17.alert(res.info || "查询失败");
					res.errorCode !== "200" && $17.voxLog({
						module : "API_REQUEST_ERROR",
						op     : "API_STATE_ERROR",
						s0     : "/teacher/newexam/report/newstudents.vpage",
						s1     : JSON.stringify(res),
						s2     : JSON.stringify(paramObj),
						s3     : $uper.env
					});
				}
			}).fail(function(jqXHR,textStatus,error){
				$17.alert(error);
			});
		},
		viewStudentHref : function(student){
			var self = this;
			return '/newexamv2/viewstudent.vpage?' + $.param({
					examId  : self.opt.examId,
					userId  : student.userId,
					from    : "teacher_history"
				});
		},
        download: function () {
            var self = this;
            if(this.stResult().issue){
                window.open("/teacher/newexam/report/downloadexamreport.api?clazzId="+ self.opt.clazzId +"&newExamId="+ self.opt.examId);
            }else {
                var time = self.stResult().issueTime || "XXXX-XX-XX HH:MM";
                $17.alert("成绩请在"+ time +"之后下载");
            }
        }
	};

	$17.newexamv2 = $17.newexamv2 || {};
	$17.extend($17.newexamv2, {
		getViewByStudents  : function(options){
			return new ViewByStudents(options);
		}
	});
}());