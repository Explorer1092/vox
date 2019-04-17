<script src="${requestContext.webAppContextPath}/public/js/jquery-1.9.1.min.js"></script>
<h1>虚拟学校</h1>
<h3>新建虚拟学校</h3>
<hr width="50%" align="left"/>
<h3>已有虚拟学校</h3>
<hr width="50%" align="left"/>
上传老师信息文件<br>
<hr width="25%" align="left"/>
<form method="post" action="/site/batch/createvirtualteachers.vpage">
    <label>输入生成虚拟老师内容(学校ID，学校名，老师姓名，学科，年级，班级，班级人数，姓名前缀，［学生姓名（可选）］)：</label>
    <br>
    <textarea name="content" cols="45" rows="10" placeholder="10023 山东省教师国陪班 翟雪 ENGLISH 6 1班 30 [张三]"></textarea>
    <br>
    <input class="btn" type="submit" value="提交" />
</form>
添加虚拟班级学生<br>
<hr width="25%" align="left"/>
<form method="post" action="/site/batch/createvirtualstudents.vpage">
    <label>输入生成虚拟老师内容(老师ID,年级,班级,学生姓名)：</label>
    <br>
    <textarea name="content" cols="45" rows="10" placeholder="12345 3 12班 张三"></textarea>
    <br>
    <input class="btn" type="submit" value="提交" />
</form>
<hr width="25%" align="left"/>
<h3>删除</h3>
<hr width="50%" align="left"/>
