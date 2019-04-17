<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='增值Task' page_num=24>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<style>
    #download-into-excel {display: none;}
</style>
<span class="span9" id="main_container02">
    年级：<select class="input-xlarge" v-model="grades_key" @change="changeData" id="grade-select">
            <option v-for="item in grades" :value="item.key" :data-number="item.number">{{item.value}}</option>
         </select>
    &nbsp;
    学期：<select class="input-xlarge" v-model="terms_key" @change="changeData" id="term-select">
            <option v-for="item in terms" :value="item.key" >{{item.value}}</option>
         </select>
    &nbsp;
    科目：<select class="input-xlarge" v-model="subjects_key" @change="changeData" id="subject-select">
            <option v-for="item in subjects" :value="item.key" >{{item.value}}</option>
         </select>
    &nbsp;&nbsp;&nbsp;&nbsp;
    <button class="btn btn-info" @click="addTemplate">添加模版</button>
    <table class="table table-hover table-striped table-bordered">
        <thead>
            <tr>
                <th><input type="checkbox" ></th>
                <th style="width:18%">id</th>
                <th style="width:14%">标题</th>
                <th style="width:14%">副标题</th>
                <th>状态</th>
                <th>环境</th>
                <th>排序</th>
                <th>操作</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="item in templates">
                <td><input type="checkbox" @click="effectIt(item.id)" class="click-listen"></td>
                <td>{{item.id}}</td>
                <td>{{item.title}}</td>
                <td>{{item.lable}}</td>
                <td>{{item.status}}</td>
                <td>{{item.env}}</td>
                <td>{{item.rank}}</td>
                <td>
                <button class="btn btn-info" @click="upData(item.id)">更新</button>
                <button class="btn btn-danger" @click="deleteDtat(item.id)">删除</button>
                </td>
            </tr>
        </tbody>
    </table>
    <div style="text-align: right;" v-if="hasItemSelected">
        <a class="btn btn-info" id="download-into-excel-btn" @click="downloadIntoExcel">导出到Excel</a>
    </div>

    <table id="download-into-excel">
        <thead>
            <tr>
                <th>模板id - 年级</th>
                <th>学期 科目 标题</th>
            </tr>
        </thead>
        <tbody id="download-into-excel-body"></tbody>
    </table>
</span>
    <script>
        let vm = new Vue({
            el: '#main_container02',
            data: {
                grades:JSON.parse('${json_encode(grades)!"[]"}'),
                terms:JSON.parse('${json_encode(terms)!"[]"}'),
                subjects:JSON.parse('${json_encode(subjects)!"[]"}'),
                grades_key:'FIRST_GRADE',
                terms_key:'LAST_TERM',
                subjects_key:'ENGLISH',
                templates:[],
                checkedItem:[]
            },
            computed: {
                hasItemSelected: function () {
                    return this.checkedItem.length > 0;
                }
            },
            methods: {
                initData () {
                    let self = this;
                    let num1 = window.location.href.lastIndexOf('#1');
                    let num2 = window.location.href.lastIndexOf('#2');
                    let num3 = window.location.href.lastIndexOf('#3');
                    if (num1>-1&&num2>-1&&num3>-1) {
                        let href = window.location.href;
                        self.grades_key=href.slice(num1+2,num2);
                        self.subjects_key=href.slice(num2+2,num3);
                        self.terms_key=href.slice(num3+2,href.length);
                    }
                    $.post('loadassignmenttemplate.vpage',{
                        grade:self.grades_key,
                        subject:self.subjects_key,
                        terms:self.terms_key
                    },function(res){
                        if(res.success) {
                            self.templates=res.templates;
                        }
                    })
                },
                addTemplate () {
                    location.href = 'upsertpage.vpage';
                },
                upData (id) {
                    location.href = 'upsertpage.vpage?id=' + id;
                },
                changeData () {
                    let self = this;
                    window.location.href = window.location.pathname + "#1" + self.grades_key +"#2" + self.subjects_key +"#3"+self.terms_key;
                    self.initData();
                },
                deleteDtat (id) {
                    let self = this;
                    if (!confirm("确定要删除吗？")) {
                        return false;
                    }
                    $.post('removeassignmenttemplate.vpage',{templateId:id},function(res){
                        if(res.success) {
                            alert("操作成功");
                            self.initData();
                        }else {
                            alert(res.info);
                        }
                    })
                },
                effectIt: function (id) {
                    let idIndex = this.checkedItem.indexOf(id);
                    if (idIndex >= 0) {
                        this.checkedItem.splice(id, 1)
                    } else {
                        // 选中该checkbox
                        this.checkedItem.push(id)
                    }
                },
                downloadIntoExcel () {
                    const gradeNumber = $('#grade-select').find("option:selected").attr('data-number');
                    const termStr = $('#term-select').find("option:selected").html();
                    const subjectStr = $('#subject-select').find("option:selected").html();
                    let outHtml = '';
                    $(".click-listen").each(function () {
                        if ($(this).prop('checked')) {
                            outHtml += '<tr>';
                            outHtml += '<td>';
                            outHtml += $(this).parent('td').next().text() + '-' + gradeNumber;
                            outHtml += '</td>';
                            outHtml += '<td>';
                            outHtml += termStr + subjectStr + $(this).parent('td').next().next().text();
                            outHtml += '</td>';
                            outHtml += '</tr>';
                        }
                    });
                    $('#download-into-excel-body').html(outHtml);
                    // 使用outerHTML属性获取整个table元素的HTML代码（包括<table>标签），然后包装成一个完整的HTML文档，设置charset为urf-8以防止中文乱码
                    const html = "<html><head><meta charset='utf-8' /></head><body>" + document.getElementById("download-into-excel").outerHTML + "</body></html>";
                    // 实例化一个Blob对象，其构造函数的第一个参数是包含文件内容的数组，第二个参数是包含文件类型属性的对象
                    const blob = new Blob([html], { type: "application/vnd.ms-excel" });
                    const a = document.getElementById("download-into-excel-btn");
                    // 利用URL.createObjectURL()方法为a元素生成blob URL
                    a.href = URL.createObjectURL(blob);
                    // 设置文件名
                    a.download = "Assignment.xls";
                }
            },
            created() {
                this.initData();
            }
        });
    </script>
</@layout_default.page>