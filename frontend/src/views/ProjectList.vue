<template>
  <div class="project-list">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px">
      <h2>项目列表</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        新建项目
      </el-button>
    </div>

    <el-row :gutter="20" style="margin-bottom: 25px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 36px; font-weight: bold; color: #409eff">{{ statistics.totalProjects || 0 }}</div>
            <div style="color: #666; margin-top: 5px; font-size: 14px">项目总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 36px; font-weight: bold; color: #f56c6c">{{ statistics.delayedProjects || 0 }}</div>
            <div style="color: #666; margin-top: 5px; font-size: 14px">延期项目</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 36px; font-weight: bold; color: #e6a23c">{{ statistics.totalTasks || 0 }}</div>
            <div style="color: #666; margin-top: 5px; font-size: 14px">总任务数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 36px; font-weight: bold; color: #67c23a">{{ statistics.completedTasks || 0 }}</div>
            <div style="color: #666; margin-top: 5px; font-size: 14px">已完成任务</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-bottom: 30px">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: bold">项目延期率</span>
              <el-tag type="danger" size="small">{{ (statistics.delayRate || 0).toFixed(1) }}%</el-tag>
            </div>
          </template>
          <el-progress 
            :percentage="Math.round(statistics.delayRate || 0)" 
            color="#f56c6c" 
            :stroke-width="15"
          />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: bold">任务完成率</span>
              <el-tag type="success" size="small">{{ (statistics.taskCompletionRate || 0).toFixed(1) }}%</el-tag>
            </div>
          </template>
          <el-progress 
            :percentage="Math.round(statistics.taskCompletionRate || 0)" 
            color="#67c23a" 
            :stroke-width="15"
          />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: bold">风险关闭率</span>
              <el-tag type="primary" size="small">{{ (statistics.riskClosureRate || 0).toFixed(1) }}%</el-tag>
            </div>
          </template>
          <el-progress 
            :percentage="Math.round(statistics.riskClosureRate || 0)" 
            color="#409eff" 
            :stroke-width="15"
          />
        </el-card>
      </el-col>
    </el-row>

    <h3 style="margin-bottom: 15px; color: #333">所有项目</h3>
    <el-row :gutter="20">
      <el-col :span="8" v-for="project in projects" :key="project.id">
        <el-card shadow="hover" style="margin-bottom: 20px">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>{{ project.name }}</span>
              <el-button type="primary" link @click="goToDetail(project.id)">查看详情</el-button>
            </div>
          </template>
          <p style="color: #666; margin-bottom: 10px">{{ project.description }}</p>
          <div style="display: flex; justify-content: space-between; font-size: 12px; color: #999">
            <span>开始: {{ project.startDate }}</span>
            <span>结束: {{ project.endDate }}</span>
          </div>
          <el-progress 
            :percentage="Math.round(project.overallProgress || 0)" 
            style="margin-top: 15px"
            :color="getProgressColor(project.overallProgress)"
          />
          <div style="margin-top: 10px; display: flex; gap: 10px">
            <el-tag size="small">任务: {{ project.tasks?.length || 0 }}</el-tag>
            <el-tag size="small" type="warning">里程碑: {{ project.milestones?.length || 0 }}</el-tag>
            <el-tag size="small" type="danger">风险: {{ project.risks?.length || 0 }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showCreateDialog" title="新建项目" width="500px">
      <el-form :model="newProject" label-width="80px">
        <el-form-item label="项目名称">
          <el-input v-model="newProject.name" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="newProject.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="newProject.startDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="newProject.endDate" type="date" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createProject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()
const projects = ref([])
const statistics = ref({})
const showCreateDialog = ref(false)
const newProject = ref({
  name: '',
  description: '',
  startDate: '',
  endDate: ''
})

const loadProjects = async () => {
  try {
    const res = await api.getProjects()
    projects.value = res.data
  } catch (e) {
    ElMessage.error('加载项目失败')
  }
}

const loadStatistics = async () => {
  try {
    const res = await api.getStatistics()
    statistics.value = res.data
    console.log('统计数据:', res.data)
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

const createProject = async () => {
  try {
    await api.createProject(newProject.value)
    ElMessage.success('创建成功')
    showCreateDialog.value = false
    newProject.value = { name: '', description: '', startDate: '', endDate: '' }
    loadProjects()
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

const goToDetail = (id) => {
  router.push(`/project/${id}`)
}

const getProgressColor = (progress) => {
  if (progress >= 80) return '#67c23a'
  if (progress >= 50) return '#409eff'
  return '#e6a23c'
}

onMounted(() => {
  loadProjects()
  loadStatistics()
})
</script>
