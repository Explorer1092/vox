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

    .select-width input {
        width: 200px;
    }
</style>
<div class="span9" id="app">
    <fieldset>
        <legend>学习环节管理</legend>
    </fieldset>
    <el-form :inline="true" :model="linkForm" class="demo-form-inline">
        <el-form-item label="学习环节ID:">
            <el-input v-model="linkForm.linkId"></el-input>
        </el-form-item>
        <el-form-item label="学习环节名称:">
            <el-input v-model="linkForm.likeName"></el-input>
        </el-form-item>
        <#--<el-form-item label="学习环节状态:">-->
        <#--<el-select v-model="linkForm.linkStatus" placeholder="请选择">-->
        <#--<el-option label="全部" value='0'></el-option>-->
        <#--<el-option label="草稿" value='1'></el-option>-->
        <#--<el-option label="发布" value='2'></el-option>-->
        <#--</el-select>-->
        <#--</el-form-item>-->
        <#--<el-form-item label="模板ID:">-->
        <#--<el-input v-model="linkForm.id"></el-input>-->
        <#--</el-form-item>-->
        <#--<el-form-item label="模板名称:">-->
        <#--<el-input v-model="linkForm.name"></el-input>-->
        <#--</el-form-item>-->
        <el-form-item label="环节分类">
            <el-select v-model="linkForm.selectType" placeholder="请选择" class="select-width">
                <el-option label="全部" value=''></el-option>
                <el-option label="视频分类" value='1'></el-option>
                <el-option label="音频分类" value='2'></el-option>
                <el-option label="选择练习" value='3'></el-option>
            </el-select>
        </el-form-item>
        <el-form-item label="学习环节类型:">
            <el-select v-model="linkForm.type" placeholder="请选择" class="select-width">
                <el-option label="全部" value=''></el-option>
                <el-option label="直接上传音频" value='DIRECT_AUDIO'></el-option>
                <el-option label="直接上传视频" value='DIRECT_VIDEO'></el-option>
                <el-option label="内容库音视频轻交互" value='INDIRECT_VIDEO'></el-option>
                <el-option label="单选无解析" value='SINGLE_WITHOUT_RESOLUTION'></el-option>
                <el-option label="单选有解析" value='SINGLE_HAS_RESOLUTION'></el-option>
                <el-option label="一题多问" value='ONE_QUESTION_MORE_ASK'></el-option>
            </el-select>
        </el-form-item>
        <el-form-item label="创建人:">
            <el-input v-model="linkForm.createUser"></el-input>
        </el-form-item>
        <el-form-item>
            <el-button type="primary" @click="search()">查询</el-button>
        </el-form-item>
    </el-form>
    <el-button type="primary" @click="create" size="small" style="margin-bottom: 20px;">新建环节</el-button>
    <el-table
            :data="tableData"
            border
            stripe
            style="width: 100%">
        <el-table-column
                prop="id"
                label="学习环节ID"
                width="100">
        </el-table-column>
        <el-table-column
                prop="name"
                label="学习环节名称">
        </el-table-column>
        <#--<el-table-column-->
        <#--prop="id"-->
        <#--label="模板ID">-->
        <#--</el-table-column>-->
        <#--<el-table-column-->
        <#--prop="name"-->
        <#--label="模板名称">-->
        <#--</el-table-column>-->
        <#--<el-table-column-->
        <#--prop="linkStatus"-->
        <#--label="学习环节状态">-->
        <#--</el-table-column>-->
        <el-table-column
                prop="type"
                :formatter="selectFormat"
                label="环节分类"
                width="160">
        </el-table-column>
        <el-table-column
                prop="type"
                :formatter="typeFormat"
                label="学习环节类型">
        </el-table-column>
        <el-table-column
                prop="createUser"
                label="创建人"
                width="160">
        </el-table-column>
        <el-table-column
                width="180"
                align="center"
                label="操作">
            <template scope="scope">
                <#--<el-button v-show="scope.row.linkStatus" @click="handel(scope.row,'publish')" type="text" size="small">发布</el-button>-->
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
                linkForm: {
                    id: '',
                    linkId: '',
                    likeName: '',
                    name: '',
                    linkStatus: '',
                    createUser: '',
                    type: '',
                    selectType: ''
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
            this.search();
        },
        methods: {
            handel(row, index) {
                let linkId = row.id;
                switch (index) {
                    // case 'publish':
                    //     console.log('发布逻辑');
                    //     break;
                    case 'detail':
                        console.log('跳转到详情');
                        window.open("/opmanager/studyTogether/link/info.vpage?edit=0&linkId=" + linkId);
                        break;
                    case 'edit':
                        console.log('跳转到编辑');
                        window.open("/opmanager/studyTogether/link/info.vpage?edit=1&linkId=" + linkId);
                        break;
                    case 'log':
                        console.log('跳转到日志');
                        window.open("/opmanager/studyTogether/template/change_log_list.vpage?change_log_type=LEARN_LINK&template_id=" + linkId);
                        break;
                    default:
                        console.log('default');
                }
            },
            search(val) {
                // 查询和跳转
                let form = Object.assign({page: val || this.page.current}, this.linkForm)
                $.ajax({
                    url: '/opmanager/studyTogether/link/index_data.vpage',
                    data: form,
                    type: "GET",
                    success: (data) => {
                        // 回调处理
                        this.tableData = data.content;
                        this.page.total = data.totalCount;
                        this.page.current = data.currentPage;
                    }
                })
            },
            create() {
                window.location.href = '/opmanager/studyTogether/link/info.vpage?edit=1';
            },
            typeFormat(row) {
                let obj = {
                    DIRECT_AUDIO: '直接上传音频',
                    DIRECT_VIDEO: '直接上传视频',
                    INDIRECT_VIDEO: '内容库轻交互音视频',
                    SINGLE_WITHOUT_RESOLUTION: '单选无解析',
                    SINGLE_HAS_RESOLUTION: '单选有解析',
                    ONE_QUESTION_MORE_ASK: '一题多问'
                };
                return obj[row.type]
            },
            selectFormat(row) {
                let obj = {
                    DIRECT_AUDIO: '音频分类',
                    DIRECT_VIDEO: '视频分类',
                    INDIRECT_VIDEO: '视频分类',
                    SINGLE_WITHOUT_RESOLUTION: '选择练习',
                    SINGLE_HAS_RESOLUTION: '选择练习',
                    ONE_QUESTION_MORE_ASK: '选择练习'
                };
                return obj[row.type]
            }
        }
    })
</script>
</@layout_default.page>