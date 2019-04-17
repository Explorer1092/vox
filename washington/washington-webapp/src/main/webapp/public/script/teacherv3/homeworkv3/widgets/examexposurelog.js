(function($17) {
	"use strict";
	$17.extend($17, {
		examExposureLog : function(opt){
			var defaultOpt = {
				subject : null,
				homeworkType : null,
				packageId    : null,
				examId       : null,
				clazzGroups  : ""  //逗号分隔字符串
			};
			var newOpt = $.extend(true,{},defaultOpt,opt);
			$17.voxLog({
				module: "m_H1VyyebB",
				op : "homework_form_preview_loaded",
				s0 : newOpt.subject,
				s1 : newOpt.homeworkType,
				s2 : newOpt.packageId,
				s3 : newOpt.examId,
				s4 : newOpt.clazzGroups
			});
		}
	});
}($17));