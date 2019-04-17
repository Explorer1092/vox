<!--created by pengmin.chen on 2017.11.17-->
<!--由于这次版本不太常规，故记录下来，方便后期别的同学理解-->

## 文件夹介绍
specialteacher 是原教务老师的页面（2017.11.16之前）

specialteacherV2 是教务老师的改版，本次改版以修改界面为主，故增加V2文件夹。

## 新版介绍
原先的教务老师页面分为入口页 和 三个二级页面

* 班级办理
* 老师学生管理
* 个人中心
    
产品需求：将三个二级页面挪到入口页处作为二级tab出现（wiki:http://wiki.17zuoye.net/pages/viewpage.action?pageId=35328557）

## 文件介绍
由于之前是三个二级页面，且由三位同学开发完成（班级管理：wei.feng，老师学生管理：huihui.li，个人中心：pengmin.chen），相对独立。

每个人都维护了一份自己的js，分别是specialteahcer文件中的（app.js、adminapp.js、teachercenterapp.js），且都使用knockout模板绑定，独立时无任何问题。

应最新需求，将三个二级页面挪至入口页，就会涉及到脚本引入的问题，但ko绑定只能同时绑定一次，故无法同时引入三个脚本，但修改脚本不太现实。

尝试过：使用一个ftl入口文件，将三个脚本引入，在每个脚本的开头判断location path，来决定是否继续指定脚本，且对入口文件中的模板实行不使用时remove（）方法，但当页面使用重定向时偶尔会出现模板报错，复现率很低，猜测是缓存引起。

最后选择设置三个入口文件（clazzmanage.ftl、teacherstudent.ftl、teachercenter.ftl），每个入口文件分别对应每个一级tab（原二级页面），每个入口文件中只引入自己的脚本（clazzmanageapp.js、teacherstudentapp.js、teachercenterapp.js）和模板，点击一级tab切换时必须刷新页面进行重载。
