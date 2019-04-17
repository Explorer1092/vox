/**
* @description 辅助本地实时编译 sass的编译
* @date 2016-02-19 16:56:33
* @author luwei.li
*/

"use strict";

const BASEDIR = "../washington/washington-webapp/src/main/webapp/public/",
	CSSBASEPATH = `${BASEDIR}skin/`,
	MOBILEPATH = `${CSSBASEPATH}/mobile/`;

var gulp = require('gulp'),
	sass = require('gulp-sass'),
	group = require('gulp-group-files');

var sassFiles = {
	common : {
		src: `${MOBILEPATH}/common/*.scss`
	},

	// TODO  用一个更统一的方式管理通用模块
	middleSchool : {
		src: `${MOBILEPATH}/student/junior/scss/skin.scss`
	},
	ugc : {
		src: `${MOBILEPATH}/student/ugc/scss/skin.scss`
	}
};

Object.keys(sassFiles).forEach(function(key){
	sassFiles[key].dest = sassFiles[key].src.replace(/[\w*]+.scss$/, '').replace('scss/', '') + "style";
});

gulp.task('sass:compile', function (){
	return group(
		sassFiles,
		function (key, fileset){
			return gulp.src(fileset.src)
			.pipe(sass().on('error', sass.logError))
			.pipe(gulp.dest(fileset.dest));
		}
	)();
});

gulp.task('sass:watch', function (){
	gulp.watch(
		'../**/*.scss',
		['sass:compile']
	);
});

gulp.task(
	'default',
	['sass:compile']
);
