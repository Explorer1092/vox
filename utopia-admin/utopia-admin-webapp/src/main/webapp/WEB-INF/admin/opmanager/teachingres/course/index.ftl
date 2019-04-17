<#import "../../../layout_default.ftl" as layout_default />
<#import "../../../mizar/pager.ftl" as pager />
<@layout_default.page page_title="教学资源--同步课件" page_num=9>
<link rel="stylesheet" href="https://cdn.bootcss.com/element-ui/2.6.1/theme-chalk/index.css">
<link rel="stylesheet" href="${requestContext.webAppContextPath}/public/css/opmanager/teachingres/course/index.css">
<div id="courseContainer" class="span9" v-cloak>
    <legend>
        <h3>课件管理</h3>
    </legend>
    <div class="titleBox">
        <div class="title">
            <h4>信息检索</h4>
            <a href="/opmanager/teacher_resource/course/edit.vpage" class="btn btn-primary">
                <i class="icon-plus icon-white"></i> 添 加
            </a>
            <a href="javascript:void(0);" class="btn btn-primary" v-on:click="requestListInfo(true)">
                <i class="icon-search icon-white"></i> 查 询
            </a>
        </div>
        <ul class="inline titleContent">
            <li class="shortLi">
                <label for="">学科：</label><select name="subject" v-model="choiceSubjectEnglishName" v-on:change="requestBookInfo()">
                    <option value="">全部</option>
                    <option value="CHINESE">语文</option>
                    <option value="MATH">数学</option>
                    <option value="ENGLISH">英语</option>
                </select>
            </li>
            <li class="shortLi">
                <label for="">年级：</label><select name="clazzLevel" v-model="choiceClazzLevelId" v-on:change="requestBookInfo()">
                    <option value="">全部</option>
                    <option value="1">1年级</option>
                    <option value="2">2年级</option>
                    <option value="3">3年级</option>
                    <option value="4">4年级</option>
                    <option value="5">5年级</option>
                    <option value="6">6年级</option>
                </select>
            </li>
            <li class="shortLi">
                <label for="">册：</label><select name="clazzLevel" v-model="choiceTermId" v-on:change="requestBookInfo()">
                    <option value="">全部</option>
                    <option value="1">上册</option>
                    <option value="2">下册</option>
                </select>
            </li>
            <li class="shortLi">
                <label for="">教材：</label><select name="book" v-model="choiceBookId" v-on:change="requestListInfo(true)">
                    <option value="">全部</option>
                    <option v-bind:value="book.id" v-for="book in bookList">{{book.name}}</option>
                </select>
            </li>
            <li class="shortLi">
                <label for="">状态：</label><select name="subject" v-model="choiceOnlineStatus" v-on:change="requestListInfo(true)">
                    <option value="">全部</option>
                    <option value="true">上线</option>
                    <option value="false">下线</option>
                </select>
            </li>
            <li class="shortLi">
                <label for="">来源：</label><select name="subject" v-model="choiceSource" v-on:change="requestListInfo(true)">
                    <option value="">全部</option>
                    <option value="0">课件大赛</option>
                    <option value="1">一起作业</option>
                </select>
            </li>
            <br>
            <li class="longLi">
                <label for="titleInput">标题：</label><input type="text" placeholder="请输入文章标题" name="" id="titleInput" v-model="inputTitle" v-on:keyup.enter="requestListInfo(true)">
            </li>
            <li class="longLi">
                <label for="idInput">ID：</label><input type="text" placeholder="请输入文章ID" name="" id="idInput" v-model="inputId" v-on:keyup.enter="requestListInfo(true)">
            </li>
            <br>
            <li class="longLi otherTip"><label for="idInput">提示：</label><span>ID查询优先级最高。</span></li>
        </ul>
    </div>
    <div class="mainBox">
        <h4 class="title">课件列表</h4>
        <div class="mainContent">
            <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>ID</th>
                <th style="width:30%">标题</th>
                <th>学科</th>
                <th>年级</th>
                <th>教材版本</th>
                <th>状态</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
                <template v-if="courseList.length">
                    <tr v-for="course in courseList">
                        <td>{{course.id}}</td>
                        <td style="width:30%">{{course.title}}</td>
                        <td>{{subjectObj[course.subject]}}</td>
                        <td>{{course.clazzLevel}}年级</td>
                        <td>{{course.bookShortPublisher}}</td>
                        <td>{{course.online ? '上线' : '下线'}}</td>
                        <td>
                            <a href="javascript:void(0);" class="btn btn-info" v-on:click="previewCourse(course)">
                                <i class="icon-zoom-in icon-white"></i> 预 览
                            </a>
                            <a href="javascript:void(0);" class="btn btn-info" v-on:click="editCourse(course)">
                                <i class="icon-pencil icon-white"></i> 编 辑
                            </a>
                            <a href="javascript:void(0);"
                               class="btn"
                               v-bind:class="'btn-' + (course.online ? 'danger' : 'primary')"
                               v-on:click="onlineCourse(course)">
                                <i class="icon-white"
                                   v-bind:class="'icon-' + (course.online ? 'remove' : 'plus')"></i> {{course.online ? '下 线' : '上 线'}}
                            </a>
                        </td>
                    </tr>
                </template>
                <tr v-else>
                    <td colspan="7" style="text-align: center;"><strong>{{!loadingFlag ? 'Loading Data，Please wait a moment.' : 'No Data Found'}}</strong></td>
                </tr>
            </tbody>
        </table>
        <el-pagination
            v-if="courseList.length"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageIndex"
            :page-sizes="[10, 20, 30, 50, 100]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalSize">
        </el-pagination>
        </div>
    </div>
</div>
<script>
    var mainSiteBaseUrl = "${(ProductConfig.getMainSiteBaseUrl())!''}";
</script>

<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<#--<script src="${requestContext.webAppContextPath}/public/js/vue.debug.js"></script>-->
<script src="https://cdn.bootcss.com/element-ui/2.6.1/index.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/opmanager/teachingres/course/index.js"></script>
</@layout_default.page>