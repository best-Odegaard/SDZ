<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入赛事标题" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择" clearable style="width:120px">
          <el-option label="校内" :value="0" />
          <el-option label="全国" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="请选择" clearable style="width:140px">
          <el-option label="学科竞赛" value="学科竞赛" />
          <el-option label="创新创业" value="创新创业" />
          <el-option label="体育竞技" value="体育竞技" />
          <el-option label="文艺活动" value="文艺活动" />
          <el-option label="其他" value="其他" />
        </el-select>
      </el-form-item>
      <el-form-item label="难度" prop="difficulty">
        <el-select v-model="queryParams.difficulty" placeholder="请选择" clearable style="width:120px">
          <el-option label="入门" :value="1" />
          <el-option label="进阶" :value="2" />
          <el-option label="高阶" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width:120px">
          <el-option label="草稿" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="已结束" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['saidazi:competition:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['saidazi:competition:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['saidazi:competition:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['saidazi:competition:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="competitionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="60" />
      <el-table-column label="赛事标题" align="left" prop="title" show-overflow-tooltip />
      <el-table-column label="类型" align="center" prop="type" width="70">
        <template #default="scope">{{ scope.row.type === 0 ? '校内' : '全国' }}</template>
      </el-table-column>
      <el-table-column label="分类" align="center" prop="category" width="100" />
      <el-table-column label="难度" align="center" prop="difficulty" width="70">
        <template #default="scope">{{ {1:'入门',2:'进阶',3:'高阶'}[scope.row.difficulty] }}</template>
      </el-table-column>
      <el-table-column label="主办单位" align="center" prop="organizer" width="130" show-overflow-tooltip />
      <el-table-column label="报名截止" align="center" prop="regEndTime" width="160" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="{0:'info',1:'success',2:'danger'}[scope.row.status]">{{ {0:'草稿',1:'已发布',2:'已结束'}[scope.row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="140" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['saidazi:competition:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['saidazi:competition:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 添加/修改赛事对话框 -->
    <el-dialog :title="title" v-model="open" width="720px" append-to-body>
      <el-form ref="competitionRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="赛事标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入赛事标题" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择">
                <el-option label="校内" :value="0" />
                <el-option label="全国" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择">
                <el-option label="学科竞赛" value="学科竞赛" />
                <el-option label="创新创业" value="创新创业" />
                <el-option label="体育竞技" value="体育竞技" />
                <el-option label="文艺活动" value="文艺活动" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="form.difficulty" placeholder="请选择">
                <el-option label="入门" :value="1" />
                <el-option label="进阶" :value="2" />
                <el-option label="高阶" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学院标签" prop="collegeTag">
              <el-input v-model="form.collegeTag" placeholder="校内赛事填写学院" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主办单位" prop="organizer">
              <el-input v-model="form.organizer" placeholder="请输入主办单位" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="报名开始" prop="regStartTime">
              <el-date-picker v-model="form.regStartTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width:100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="报名截止" prop="regEndTime">
              <el-date-picker v-model="form.regEndTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width:100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="比赛时间" prop="matchTime">
              <el-date-picker v-model="form.matchTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width:100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="封面图URL" prop="coverUrl">
              <el-input v-model="form.coverUrl" placeholder="封面图地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择">
                <el-option label="草稿" :value="0" />
                <el-option label="已发布" :value="1" />
                <el-option label="已结束" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="赛事简介" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入赛事简介" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="报名方式" prop="regInfo">
              <el-input v-model="form.regInfo" type="textarea" :rows="3" placeholder="报名方式说明（含网址）" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Competition">
import { listCompetition, getCompetition, delCompetition, addCompetition, updateCompetition } from "@/api/saidazi/competition"

const { proxy } = getCurrentInstance()

const competitionList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: null,
    type: null,
    category: null,
    difficulty: null,
    status: null,
  },
  rules: {
    title: [{ required: true, message: "赛事标题不能为空", trigger: "blur" }],
    type: [{ required: true, message: "请选择赛事类型", trigger: "change" }],
  }
})
const { queryParams, form, rules } = toRefs(data)

/** 查询列表 */
function getList() {
  loading.value = true
  listCompetition(queryParams.value).then(response => {
    competitionList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() { open.value = false; reset() }

function reset() {
  form.value = {
    id: null, title: null, type: 0, category: null, difficulty: 1,
    collegeTag: null, organizer: null, regStartTime: null, regEndTime: null,
    matchTime: null, description: null, regInfo: null, coverUrl: null, status: 0
  }
  proxy.resetForm("competitionRef")
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() { reset(); open.value = true; title.value = "新增赛事" }

function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getCompetition(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改赛事"
  })
}

function submitForm() {
  proxy.$refs["competitionRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateCompetition(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addCompetition(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('确认删除选中的赛事？').then(() => {
    return delCompetition(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download('saidazi/competition/export', { ...queryParams.value }, `competition_${new Date().getTime()}.xlsx`)
}

getList()
</script>