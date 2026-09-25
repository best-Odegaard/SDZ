<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="4" :xs="12">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-num">{{ data.competitionCount || 0 }}</div>
            <div class="stat-label">赛事总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4" :xs="12">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-num">{{ data.userCount || 0 }}</div>
            <div class="stat-label">注册用户</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4" :xs="12">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-num">{{ data.teamCount || 0 }}</div>
            <div class="stat-label">组队数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4" :xs="12">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-num">{{ data.postCount || 0 }}</div>
            <div class="stat-label">社区帖子</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4" :xs="12">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-num">{{ data.achievementCount || 0 }}</div>
            <div class="stat-label">往届成果</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top:20px">
      <template #header><span>赛事分类分布</span></template>
      <el-table :data="data.categoryStat || []" border>
        <el-table-column label="分类" prop="category" align="center" />
        <el-table-column label="数量" prop="count" align="center" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="SaidaziDashboard">
import { getDashboard } from "@/api/saidazi/dashboard"

const data = ref({})

function load() {
  getDashboard().then(res => { data.value = res.data || {} })
}
load()
</script>

<style scoped>
.stat-card { text-align: center; padding: 10px 0; }
.stat-num { font-size: 28px; font-weight: 600; color: #409EFF; }
.stat-label { font-size: 14px; color: #909399; margin-top: 6px; }
</style>