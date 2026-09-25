<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="赛事ID" prop="competitionId">
        <el-input v-model="queryParams.competitionId" placeholder="请输入" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="获奖队伍" prop="teamName">
        <el-input v-model="queryParams.teamName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="获奖等级" prop="award">
        <el-input v-model="queryParams.award" placeholder="请输入" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['saidazi:achievement:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['saidazi:achievement:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['saidazi:achievement:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="achievementList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="60" />
      <el-table-column label="赛事标题" align="left" prop="competitionTitle" show-overflow-tooltip />
      <el-table-column label="获奖队伍" align="center" prop="teamName" width="140" />
      <el-table-column label="获奖等级" align="center" prop="award" width="100" />
      <el-table-column label="队员" align="center" prop="members" show-overflow-tooltip />
      <el-table-column label="作品简介" align="left" prop="introduction" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="140" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['saidazi:achievement:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['saidazi:achievement:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="achievementRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="赛事ID" prop="competitionId">
          <el-input v-model="form.competitionId" placeholder="请输入关联赛事ID" />
        </el-form-item>
        <el-form-item label="获奖队伍" prop="teamName">
          <el-input v-model="form.teamName" placeholder="请输入获奖队伍名称" />
        </el-form-item>
        <el-form-item label="获奖等级" prop="award">
          <el-input v-model="form.award" placeholder="如：一等奖/金奖/银奖" />
        </el-form-item>
        <el-form-item label="队员姓名" prop="members">
          <el-input v-model="form.members" placeholder="队员姓名，逗号分隔" />
        </el-form-item>
        <el-form-item label="作品简介" prop="introduction">
          <el-input v-model="form.introduction" type="textarea" :rows="4" placeholder="请输入作品简介" />
        </el-form-item>
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

<script setup name="Achievement">
import { listAchievement, getAchievement, delAchievement, addAchievement, updateAchievement } from "@/api/saidazi/achievement"

const { proxy } = getCurrentInstance()

const achievementList = ref([])
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
  queryParams: { pageNum: 1, pageSize: 10, competitionId: null, teamName: null, award: null },
  rules: {
    competitionId: [{ required: true, message: "赛事ID不能为空", trigger: "blur" }],
    teamName: [{ required: true, message: "获奖队伍不能为空", trigger: "blur" }],
    award: [{ required: true, message: "获奖等级不能为空", trigger: "blur" }],
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listAchievement(queryParams.value).then(response => {
    achievementList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() { open.value = false; reset() }
function reset() {
  form.value = { id: null, competitionId: null, teamName: null, award: null, introduction: null, members: null }
  proxy.resetForm("achievementRef")
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}
function handleAdd() { reset(); open.value = true; title.value = "新增往届成果" }
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getAchievement(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改往届成果"
  })
}
function submitForm() {
  proxy.$refs["achievementRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateAchievement(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList() })
      } else {
        addAchievement(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList() })
      }
    }
  })
}
function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('确认删除选中的往届成果？').then(() => delAchievement(_ids))
    .then(() => { getList(); proxy.$modal.msgSuccess("删除成功") }).catch(() => {})
}
getList()
</script>