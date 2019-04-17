gulp = require 'gulp'

## 引入组件
gutil        = require 'gutil'
uglify       = require 'gulp-uglify'
rename       = require 'gulp-rename'
sourcemaps   = require 'gulp-sourcemaps'
sass         = require 'gulp-sass'
amdOptimize  = require 'amd-optimize'
concat       = require 'gulp-concat'
gulpif       = require 'gulp-if'
autoprefixer = require 'gulp-autoprefixer'
jstConcat    = require 'gulp-jst-concat'
coffee       = require 'gulp-coffee'
ignore       = require 'gulp-ignore'
rimraf       = require 'gulp-rimraf'
runSequence  = require 'run-sequence'

###配置文件###
config       = {
  app: 'app'
  dist: 'public'
  temp: '.tmp'
}
###coffee script compiler###
gulp.task 'coffee-compiler', ->
  gulp.src "#{config.app}/coffee/{,**/}*.coffee"
  .pipe coffee({bare:true}).on 'error',gutil.log
  # .pipe uglify()
  .pipe gulp.dest "#{config.dist}/js"

###js min###
#gulp.task 'js-min',['coffee-compiler'], ->
#  gulp.src "#{config.app}/scripts/{,**/}*.js"
#  .pipe uglify()
#  .pipe gulp.dest "#{config.temp}/scripts"

### sass-compiler###
###   nested (default), compact, compressed, or expanded ###
gulp.task 'sass-compiler', ->
 gulp.src "#{config.app}/sass/{,**}/*.{scss,sass}"
 .pipe(sass({outputStyle:'compressed'}).on('error',sass.logError))
 .pipe autoprefixer()
 .pipe gulp.dest "#{config.dist}/css"
### clean ###
#gulp.task 'clean', ->
#  gulp.src ["#{config.dist}/{,**/}*.{js,css}",".tmp"], { read: false }
#  .pipe ignore("m1.js")
#  .pipe rimraf({force:true})
### build ###
gulp.task 'build', ->
  runSequence 'sass-compiler','coffee-compiler',->
    console.log 'finished'

gulp.task 'build:watch', ->
	gulp.watch ["#{config.app}/coffee/{,**/}*.coffee","#{config.app}/sass/{,**}/*.{scss,sass}"], ->
		runSequence 'sass-compiler','coffee-compiler',->
		    console.log 'dev finished'