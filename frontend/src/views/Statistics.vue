<template>
  <div class="statistics">
    <h2 style="margin-bottom: 20px">数据统计</h2>
    
    <el-row :gutter="20" style="margin-bottom: 30px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 48px; color: #409eff; font-weight: bold">{{ stats?.totalProjects || 0 }}</div>
            <div style="color: #666; margin-top: 10px">项目总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 48px; color: #e6a23c; font-weight: bold">{{ stats?.totalTasks || 0 }}</div>
            <div style="color: #666; margin-top: 10px">任务总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 48px; color: #67c23a; font-weight: bold">{{ stats?.completedTasks || 0 }}</div>
            <div style="color: #666; margin-top: 10px">已完成任务</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 48px; color: #f56c6c; font-weight: bold">{{ stats?.delayedProjects || 0 }}</div>
            <div style="color: #666; margin-top: 10px">延期项目</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>项目延期率</template>
          <div style="text-align: center; padding: 20px 0">
            <el-progress 
              type="circle" 
              :percentage="Math.round(stats?.delayRate || 0)" 
              color="#f56c6c"
              :width="150"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>任务完成率</template>
          <div style="text-align: center; padding: 20px 0">
            <el-progress 
              type="circle" 
              :percentage="Math.round(stats?.taskCompletionRate || 0)" 
              color="#67c23a"
              :width="150"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>风险关闭率</template>
          <div style="text-align: center; padding: 20px 0">
            <el-progress 
              type="circle" 
              :percentage="Math.round(stats?.riskClosureRate || 0)" 
              color="#409eff"
              :width="150"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const stats = ref(null)

const loadStatistics = async () => {
  try {
    const res = await api.getStatistics()
    stats.value = res.data
  } catch (e) {
    console.error('加载统计数据失败')
  }
}

onMounted(() => {
  loadStatistics()
})
</script>
