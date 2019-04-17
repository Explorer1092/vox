<#import "../../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<style>
    input[type='text'] {
        -webkit-appearance: none;
        background-color: #FFF;
        border-radius: 4px;
        border: 1px solid #DCDFE6;
        box-sizing: border-box;
        color: #606266;
        display: inline-block;
        font-size: inherit;
        height: 30px;
        line-height: 40px;
        outline: 0;
        padding: 0 15px;
        -webkit-transition: border-color .2s cubic-bezier(.645, .045, .355, 1);
        transition: border-color .2s cubic-bezier(.645, .045, .355, 1);
        width: 100px;
    }
</style>
<div class="span9" id="app">
    <fieldset>
        <legend>组件化课程内容模板管理</legend>
    </fieldset>
    <el-form :inline="true" :model="templateForm" class="demo-form-inline">
        <el-form-item label="模板ID:">
            <el-input v-model="templateForm.template_id"></el-input>
        </el-form-item>
        <el-form-item label="模板名称:">
            <el-input v-model="templateForm.template_name"></el-input>
        </el-form-item>
        <el-form-item label="模板皮肤类型:">
            <el-select v-model="templateForm.template_type" placeholder="请选择">
                <el-option label="全部" value=''></el-option>
                <el-option label="国学文本类型" value='SinologyText'></el-option>
                <el-option label="英语绘本ID类型" value='PicBookId'></el-option>
                <el-option label="书本音频类型" value='BookAudio'></el-option>
                <el-option label="图文阅读类型" value='ImgTextReading'></el-option>
                <el-option label="书本图文类型" value='BookImgText'></el-option>
            </el-select>
        </el-form-item>
        <el-form-item label="创建人:">
            <el-input v-model="templateForm.create_user"></el-input>
        </el-form-item>
        <el-button type="primary" @click="search()" size="mini">查询</el-button>
    </el-form>
    <el-button type="primary" @click="create" size="small" style="margin-bottom: 20px;">新建模板</el-button>
    <el-table
            :data="tableData"
            border
            stripe
            style="width: 100%">
        <el-table-column
                prop="id"
                label="模板ID"
                width="100">
        </el-table-column>
        <el-table-column
                prop="name"
                label="模板名称">
        </el-table-column>
        <el-table-column
                prop="type"
                :formatter="typeFormat"
                label="模板皮肤类型"
                width="140">
        </el-table-column>
        <el-table-column
                prop="create"
                label="创建人"
                width="160">
        </el-table-column>
        <el-table-column
                width="180"
                align="center"
                label="操作">
            <template scope="scope">
                <el-button v-show="scope.row.status" @click="handel(scope.row,'publish')" type="text" size="small">
                    发布
                </el-button>
                <el-button @click="handel(scope.row,'detail')" type="text" size="small">详情</el-button>
                <el-button @click="handel(scope.row,'edit')" type="text" size="small">编辑</el-button>
                <el-button @click="handel(scope.row,'log')" type="text" size="small">日志</el-button>
            </template>
        </el-table-column>
    </el-table>
    <div style="float: right;margin-top: 30px;" v-if="page.total > page.maxNumber">
        <el-pagination
                background
                layout="prev, pager, next"
                :page-size="page.maxNumber"
                :total="page.total"
                @current-change="search">
        </el-pagination>
    </div>
</div>
<script type="text/javascript">
    new Vue({
        el: '#app',
        data() {
            return {
                // TODO 替换掉 tableData
                tableData: [],
                templateForm: {
                    template_id: '',
                    template_name: '',
                    template_type: '',
                    create_user: ''
                },
                // 分页
                page: {
                    total: 0,
                    current: 1,
                    maxNumber: 10,
                }
            }
        },
        mounted() {
            this.$nextTick(() => {
                this.search();
            })
        },
        methods: {
            handel(row, index) {
                let templateId = row.id;
                switch (index) {
                    case 'detail':
                        console.log('跳转到详情');
                        window.open("/opmanager/studyTogether/newTemplate/newTemplate_info_page.vpage?edit=0&templateId=" + templateId);
                        break;
                    case 'edit':
                        console.log('跳转到编辑');
                        window.open("/opmanager/studyTogether/newTemplate/newTemplate_info_page.vpage?edit=1&templateId=" + templateId);
                        break;
                    case 'log':
                        console.log('跳转到日志');
                        window.open("/opmanager/studyTogether/template/change_log_list.vpage?change_log_type=NEW_TEMPLATE&template_id=" + templateId);
                        break;
                    default:
                        console.log('default');
                }
            },
            search(val) {
                // 查询和跳转
                let form = Object.assign({page: val || this.page.current}, this.templateForm)
                $.ajax({
                    url: '/opmanager/studyTogether/newTemplate/get_newTemplate_list.vpage',
                    data: form,
                    type: "GET",
                    success: (data) => {
                        // 回调处理
                        this.tableData = data.new_template_list;
                        this.page.total = data.total_count;
                        this.page.current = data.page;
                    }
                })
            },
            create() {
                window.location.href = '/opmanager/studyTogether/newTemplate/newTemplate_info_page.vpage?edit=1';
            },
            typeFormat(row) {
                let obj = {
                    SinologyText: '国学文本类型',
                    PicBookId: '英语绘本ID类型',
                    BookAudio: '书本音频类型',
                    ImgTextReading: '图文阅读类型',
                    BookImgText: '书本图文类型'
                }
                return obj[row.type]
            }

        }
    })
</script>
</@layout_default.page>