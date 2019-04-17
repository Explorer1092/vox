<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='增值Task' page_num=24>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<style>
    #download-into-excel {display: none;}
</style>
<span class="span9" id="main_container">
    类型：<select class="input-xlarge" v-model="choseKey" @change="changeData" id="task-type">
            <option v-for="item in source" :value="item.key">{{item.value}}</option>
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
                <th>周期</th>
                <th>startTime</th>
                <th>endTime</th>
                <th>状态</th>
                <th>环境</th>
                <th>排序</th>
                <th>倍</th>
                <th>操作</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="item in template">
                <td><input type="checkbox" @click="effectIt(item.id)" class="click-listen"></td>
                <td>{{item.id}}<span style="display: none;" v-for="product in item.rule.attributes.products">{{product.level}}</span></td>
                <td>{{item.title}}</td>
                <td>{{item.subtitle}}</td>
                <td>{{item.cycleType}}</td>
                <td>{{item.startTime}}</td>
                <td>{{item.endTime}}</td>
                <td>{{item.status}}</td>
                <td>{{item.env}}</td>
                <td>{{item.rank}}</td>
                <td>{{item.multiple}}</td>
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
                <th>标题</th>
                <th v-if="choseKey!='learning'">类型</th>
            </tr>
        </thead>
        <tbody id="download-into-excel-body"></tbody>
    </table>
</span>
    <script>
        let vm = new Vue({
            el: '#main_container',
            data: {
                source:JSON.parse('${json_encode(sources)!"[]"}'),
                choseKey:'learning',
                template:[],
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
                    let num = window.location.href.lastIndexOf('#');
                    if (num>-1) {
                        let href = window.location.href;
                        self.choseKey=href.slice(num+1,href.length)
                    }
                    $.post('loadtasktemplate.vpage',{source:self.choseKey},function(res){
                        if(res.success) {
                            self.template=res.templates;
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
                    window.location.href = window.location.pathname + "#" + self.choseKey;
                    self.initData();
                },
                deleteDtat (id) {
                    let self = this;
                    if (!confirm("确定要删除吗？")) {
                        return false;
                    }
                    $.post('removetasktemplate.vpage',{templateId:id},function(res){
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
                downloadIntoExcel: function () {
                    const selectedTaskType = $('#task-type').find("option:selected");
                    const taskType = selectedTaskType.val();
                    const taskTypeStr = selectedTaskType.html();
                    let outHtml = '';

                    $(".click-listen").each(function () {
                        if ($(this).prop('checked')) {
                            if (taskType === 'learning') {
                                // 每一条生成6条
                                for (let i = 1; i < 7; i++) {
                                    outHtml += '<tr>';
                                    outHtml += '<td>';
                                    outHtml += $(this).parent('td').next().text() + '-' + i;
                                    outHtml += '</td>';
                                    outHtml += '<td>';
                                    outHtml += $(this).parent('td').next().next().text();
                                    outHtml += '</td>';
                                    outHtml += '</tr>';
                                }
                            } else {
                                let levelSet = new Set([]);
                                $(this).parent('td').next().find('span').each(function () {
                                    levelSet.add($(this).text());
                                });

                                levelSet.forEach(level => {
                                    outHtml += '<tr>';
                                    outHtml += '<td>';
                                    outHtml += $(this).parent('td').next().text() + '-' + level;
                                    outHtml += '</td>';
                                    outHtml += '<td>';
                                    outHtml += $(this).parent('td').next().next().text();
                                    outHtml += '</td>';
                                    outHtml += '<td>';
                                    outHtml += taskTypeStr;
                                    outHtml += '</td>';
                                    outHtml += '</tr>';
                                });
                            }
                        }
                    });
                    $('#download-into-excel-body').html(outHtml);
                    // 使用outerHTML属性获取整个table元素的HTML代码（包括<table>标签），然后包装成一个完整的HTML文档，设置charset为urf-8以防止中文乱码
                    const html = "<html><head><meta charset='utf-8' /></head><body>" + document.getElementById("download-into-excel").outerHTML + "</body></html>";
                    // 实例化一个Blob对象，其构造函数的第一个参数是包含文件内容的数组，第二个参数是包含文件类型属性的对象
                    const blob = new Blob([html], {type: "application/vnd.ms-excel"});
                    const a = document.getElementById("download-into-excel-btn");
                    // 利用URL.createObjectURL()方法为a元素生成blob URL
                    a.href = URL.createObjectURL(blob);
                    // 设置文件名
                    a.download = taskTypeStr + ".xls";
                }
            },
            created() {
               this.initData();
            }
        });

    </script>
</@layout_default.page>