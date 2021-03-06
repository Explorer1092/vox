// Generated by CoffeeScript 1.10.0
(function() {
  var baseDir, coffee, concat, cssImport, cssmin, ftlDir, gulp, jasmine, jshint, mizarBaseDir, mizarRepoDir, modify, paths, rename, repoDir, repoftlDir, runSequence, ucenterBaseDir, ucenterRepoDir, uglify, wechatBaseDir, wechatRepoDir;

  gulp = require("gulp");

  runSequence = require("run-sequence");

  jasmine = require("gulp-jasmine");

  concat = require("gulp-concat");

  uglify = require("gulp-uglify");

  rename = require("gulp-rename");

  jshint = require("gulp-jshint");

  cssImport = require("gulp-cssimport");

  cssmin = require("gulp-cssmin");

  coffee = require("gulp-coffee");

  modify = require("gulp-modify");

  baseDir = "washington/washington-webapp/src/main/webapp/public/";

  repoDir = "build-repo/build-__stage__/washington-webapp/webroot/public/";

  ftlDir = "washington/washington-webapp/src/main/webapp/WEB-INF/ftl/";

  repoftlDir = "build-repo/build-__stage__/washington-webapp/webroot/WEB-INF/ftl/";

  wechatBaseDir = "wechat/wechat-webapp/src/main/webapp/public/";

  wechatRepoDir = "build-repo/build-__stage__/wechat-webapp/webroot/public/";

  ucenterBaseDir = "utopia-ucenter/ucenter-webapp/src/main/webapp/public/";

  ucenterRepoDir = "build-repo/build-__stage__/ucenter-webapp/webroot/public/";

  mizarBaseDir = "mizar/mizar-webapp/src/main/webapp/public/";

  mizarRepoDir = "build-repo/build-__stage__/mizar-webapp/webroot/public/";

  paths = {
    base: [baseDir + "script/base/core/*.coffee", baseDir + "script/base/modules/*.coffee"],
    test: [baseDir + "script/base/test/*.js"]
  };

  gulp.task("watch", function() {
    var i, len, path, ref;
    ref = paths.base;
    for (i = 0, len = ref.length; i < len; i++) {
      path = ref[i];
      gulp.watch(path, (function() {
        return runSequence("base", "jasfile", "test");
      }));
    }
    return gulp.watch(paths.test, function() {
      return runSequence("jasfile", "test");
    });
  });

  gulp.task("base", function() {
    return gulp.src(paths.base).pipe(concat("$8.coffee")).pipe(gulp.dest(repoDir + "script/base/")).pipe(coffee()).pipe(concat("$8.js")).pipe(gulp.dest(repoDir + "script/base/")).pipe(rename({
      extname: ".min.js"
    })).pipe(uglify()).pipe(gulp.dest(repoDir + "script/base/"));
  });

  gulp.task("jasfile", function() {
    return gulp.src(paths.test).pipe(concat("jastest.min.js")).pipe(uglify()).pipe(gulp.dest(repoDir + "/script/base/"));
  });

  gulp.task("test", function() {
    return gulp.src(repoDir + "/script/base/jastest.min.js").pipe(jasmine());
  });

  gulp.task("baseing", function() {
    return runSequence("base", "jasfile", "test", "watch");
  });

  gulp.task("script", function() {
    return gulp.src([baseDir + "script/*.js", "!" + baseDir + "script/*.min.js", baseDir + "script/**/*.js", "!" + baseDir + "script/**/*.min.js", "!" + baseDir + "script/base/**/*.js"])
	//.pipe(jshint())
	.pipe(rename({
      extname: ".min.js"
    })).pipe(uglify({
      mangle: false
    })).on('error', function(err) {
      console.info("uglify-min===");
      console.info(err.message);
      return this.end();
    }).pipe(gulp.dest(repoDir + "script/"));
  });

  gulp.task("css", function() {
    return gulp.src([baseDir + "skin/**/*.css", "!" + baseDir + "skin/**/*.min.css"]).pipe(rename({
      extname: ".min.css"
    })).pipe(cssImport()).pipe(cssmin({
      compatibility: "ie7",
      keepSpecialComments: "*,_"
    })).pipe(gulp.dest(repoDir + "skin/"));
  });

  gulp.task("plugin", function() {
    gulp.src([baseDir + "plugin/loader.js", baseDir + "plugin/template.js", baseDir + "plugin/log.js", baseDir + "plugin/downloadApp.js", baseDir + "plugin/jquery.flashswf.js", baseDir + "plugin/json2.js", baseDir + "plugin/fastClick.js", baseDir + "plugin/jquery-utils/jquery-utils.js"]).pipe(rename({
      extname: ".min.js"
    })).pipe(uglify({
      mangle: false
    })).pipe(gulp.dest(repoDir + "plugin/"));
    gulp.src([baseDir + "plugin/chart/*.js"]).pipe(rename({
      extname: ".min.js"
    })).pipe(uglify({
      mangle: false
    })).pipe(gulp.dest(repoDir + "plugin/chart/"));
    gulp.src([baseDir + "plugin/jquery-impromptu/jquery-impromptu.js"]).pipe(rename({
      extname: ".min.js"
    })).pipe(uglify({
      mangle: false
    })).pipe(gulp.dest(repoDir + "plugin/jquery-impromptu/"));
    gulp.src([baseDir + "plugin/jquery-utils/jquery-utils.js"]).pipe(rename({
      extname: ".min.js"
    })).pipe(uglify({
      mangle: false
    })).pipe(gulp.dest(repoDir + "plugin/jquery-utils/"));
    return gulp.src([baseDir + "plugin/jquery-datepicker/css/*.css", "!" + baseDir + "plugin/jquery-datepicker/css/*.min.css"]).pipe(rename({
      extname: ".min.css"
    })).pipe(cssImport()).pipe(cssmin({
      compatibility: "ie7",
      keepSpecialComments: "*,_"
    })).pipe(gulp.dest(repoDir + "plugin/jquery-datepicker/css/"));
  });

  gulp.task("wechatSource", function() {
    gulp.src([wechatBaseDir + "js/*.js", "!" + wechatBaseDir + "js/*.min.js", wechatBaseDir + "js/**/*.js", "!" + wechatBaseDir + "js/**/*.min.js"])
	//.pipe(jshint())
	.pipe(rename({
      extname: ".min.js"
    })).pipe(uglify({
      mangle: false
    })).on('error', function(err) {
      console.info("uglify-min===");
      console.info(err.message);
      return this.end();
    }).on('error', function(err) {
      console.info("uglify-min===");
      console.info(err.message);
      return this.end();
    }).pipe(gulp.dest(wechatRepoDir + "js/"));
    return gulp.src([wechatBaseDir + "css/*.css", "!" + wechatBaseDir + "css/*.min.css", wechatBaseDir + "css/**/*.css", "!" + wechatBaseDir + "css/**/*.min.css"]).pipe(rename({
      extname: ".min.css"
    })).pipe(cssImport()).pipe(cssmin({
      compatibility: "or"
    })).pipe(gulp.dest(wechatRepoDir + "css/"));
  });

  gulp.task("ucenterSource", function() {
    gulp.src([ucenterBaseDir + "script/*.js", "!" + ucenterBaseDir + "script/*.min.js", ucenterBaseDir + "script/**/*.js", "!" + ucenterBaseDir + "script/**/*.min.js"])
	//.pipe(jshint())
	.pipe(rename({
      extname: ".min.js"
    })).pipe(uglify({
      mangle: false
    })).on('error', function(err) {
      console.info("uglify-min===");
      console.info(err.message);
      return this.end();
    }).pipe(gulp.dest(ucenterRepoDir + "script/"));
    gulp.src([ucenterBaseDir + "skin/*.css", "!" + ucenterBaseDir + "skin/*.min.css", ucenterBaseDir + "skin/**/*.css", "!" + ucenterBaseDir + "skin/**/*.min.css"]).pipe(rename({
      extname: ".min.css"
    })).pipe(cssImport()).pipe(cssmin({
      compatibility: "ie7",
      keepSpecialComments: "*,_"
    })).pipe(gulp.dest(ucenterRepoDir + "skin/"));
    gulp.src([ucenterBaseDir + "plugin/template.js", ucenterBaseDir + "plugin/jquery.flashswf.js"]).pipe(rename({
      extname: ".min.js"
    })).pipe(uglify({
      mangle: false
    })).pipe(gulp.dest(ucenterRepoDir + "plugin/"));
    gulp.src([ucenterBaseDir + "plugin/jquery-impromptu/jquery-impromptu.js"]).pipe(rename({
      extname: ".min.js"
    })).pipe(uglify({
      mangle: false
    })).pipe(gulp.dest(ucenterRepoDir + "plugin/jquery-impromptu/"));
    return gulp.src([ucenterBaseDir + "plugin/jquery-datepicker/css/*.css", "!" + ucenterBaseDir + "plugin/jquery-datepicker/css/*.min.css"]).pipe(rename({
      extname: ".min.css"
    })).pipe(cssImport()).pipe(cssmin({
      compatibility: "ie7",
      keepSpecialComments: "*,_"
    })).pipe(gulp.dest(ucenterRepoDir + "plugin/jquery-datepicker/css/"));
  });

  gulp.task("mizarSource", function() {
    gulp.src([mizarBaseDir + "script/**/*.js"])
	//.pipe(jshint())
	.pipe(rename({
      extname: ".min.js"
    })).pipe(uglify({
      mangle: false
    })).on('error', function(err) {
      console.info("uglify-min===");
      console.info(err.message);
      return this.end();
    }).pipe(gulp.dest(mizarRepoDir + "script/"));
    return gulp.src([mizarBaseDir + "skin/**/*.css"]).pipe(rename({
      extname: ".min.css"
    })).pipe(cssImport()).pipe(cssmin({
      compatibility: "ie7",
      keepSpecialComments: "*,_"
    })).pipe(gulp.dest(mizarRepoDir + "skin/"));
  });

  gulp.task("markiframe", function() {
    return gulp.src(ftlDir + "default/**/*.ftl").pipe(modify({
      fileModifier: function(file, contents) {
        return contents.replace(/<iframe y?q?i?f?\s?/g, '<iframe yqif ');
      }
    })).pipe(gulp.dest(repoftlDir + "default/"));
  });

  gulp.task("default", ["script", "css", "plugin", "markiframe", "wechatSource", "ucenterSource", "mizarSource"]);
  //gulp.task("default", function() {
    //return runSequence("script", "css", "plugin", "markiframe", "wechatSource", "ucenterSource", "mizarSource");
  //});

}).call(this);
