<template>
  <div class="risk-todo-list">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px">
      <h2>风险待办中心</h2>
      <div>
        <el-button type="warning" @click="scanDelayedTasks">
          <el-icon><AlarmClock /></el-icon>
          扫描即将延期任务
        </el-button>
        <el-button type="primary" @click="refreshData" style="margin-left: 10px">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" style="margin-bottom: 25px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 32px; font-weight: bold; color: #409eff">{{ statistics.totalProjects || 0 }}</div>
            <div style="color: #999; font-size: 12px; margin-top: 5px">项目总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 32px; font-weight: bold; color: #f56c6c">{{ statistics.delayedProjects || 0 }}</div>
            <div style="color: #999; font-size: 12px; margin-top: 5px">延期项目</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 32px; font-weight: bold; color: #e6a23c">{{ risks.length }}</div>
            <div style="color: #999; font-size: 12px; margin-top: 5px">风险总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 32px; font-weight: bold; color: #67c23a">{{ todos.filter(t => t.completed).length }}</div>
            <div style="color: #999; font-size: 12px; margin-top: 5px">已完成待办</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-bottom: 30px">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: bold">全局项目延期率</span>
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
              <span style="font-weight: bold">全局任务完成率</span>
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
              <span style="font-weight: bold">全局风险关闭率</span>
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

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: bold; color: #f56c6c">待处理风险 ({{ risks.filter(r => r.status !== 'CLOSED').length }})</span>
            </div>
          </template>
          <el-empty v-if="risks.filter(r => r.status !== 'CLOSED').length === 0" description="暂无待处理风险" />
          <div v-else style="max-height: 500px; overflow-y: auto">
            <div v-for="risk in risks.filter(r => r.status !== 'CLOSED')" :key="risk.id" 
                 style="padding: 15px; border-bottom: 1px solid #eee; margin-bottom: 10px">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px">
                <strong>{{ risk.title }}</strong>
                <el-tag :type="getRiskType(risk.level)" size="small">{{ risk.level }}</el-tag>
              </div>
              <p style="color: #666; font-size: 14px; margin-bottom: 10px">{{ risk.description }}</p>
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span style="font-size: 12px; color: #999">{{ risk.createdAt }}</span>
                <el-button type="primary" size="small" link @click="handleRisk(risk)">处理风险</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: bold; color: #e6a23c">待办事项 ({{ todos.filter(t => !t.completed).length }})</span>
            </div>
          </template>
          <el-empty v-if="todos.filter(t => !t.completed).length === 0" description="暂无待办事项" />
          <div v-else style="max-height: 500px; overflow-y: auto">
            <div v-for="todo in todos.filter(t => !t.completed)" :key="todo.id"
                 style="padding: 15px; border-bottom: 1px solid #eee; margin-bottom: 10px">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px">
                <strong>{{ todo.title }}</strong>
                <el-tag type="warning" size="small">待处理</el-tag>
              </div>
              <p style="color: #666; font-size: 14px; margin-bottom: 10px">{{ todo.description }}</p>
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span style="font-size: 12px; color: #999">截止: {{ todo.dueDate }}</span>
                <el-button type="success" size="small" link @click="completeTodo(todo.id)">标记完成</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: bold; color: #67c23a">已关闭风险 ({{ risks.filter(r => r.status === 'CLOSED').length }})</span>
            </div>
          </template>
          <el-empty v-if="risks.filter(r => r.status === 'CLOSED').length === 0" description="暂无已关闭风险" />
          <div v-else style="max-height: 400px; overflow-y: auto">
            <div v-for="risk in risks.filter(r => r.status === 'CLOSED')" :key="risk.id"
                 style="padding: 15px; border-bottom: 1px solid #eee; margin-bottom: 10px; background: #f5f7fa">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px">
                <strong>{{ risk.title }}</strong>
                <el-tag type="success" size="small">已关闭</el-tag>
              </div>
              <p style="color: #666; font-size: 14px">{{ risk.description }}</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: bold; color: #409eff">已完成待办 ({{ todos.filter(t => t.completed).length }})</span>
            </div>
          </template>
          <el-empty v-if="todos.filter(t => t.completed).length === 0" description="暂无已完成待办" />
          <div v-else style="max-height: 400px; overflow-y: auto">
            <div v-for="todo in todos.filter(t => t.completed)" :key="todo.id"
                 style="padding: 15px; border-bottom: 1px solid #eee; margin-bottom: 10px; background: #f5f7fa">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px">
                <strong style="text-decoration: line-through; color: #999">{{ todo.title }}</strong>
                <el-tag type="success" size="small">已完成</el-tag>
              </div>
              <p style="color: #999; font-size: 14px">{{ todo.description }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showRiskDialog" title="处理风险" width="500px">
      <el-form label-width="100px">
        <el-form-item label="风险标题" disabled>
          <span>{{ currentRisk?.title }}</span>
        </el-form-item>
        <el-form-item label="处理措施">
          <el-input v-model="riskAction.action" type="textarea" :rows="3" placeholder="请输入处理措施" />
        </el-form-item>
        <el-form-item label="处理结果">
          <el-input v-model="riskAction.result" type="textarea" :rows="3" placeholder="请输入处理结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRiskDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRiskAction">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { AlarmClock, Refresh } from '@element-plus/icons-vue'
import api from '../api'

const risks = ref([])
const todos = ref([])
const statistics = ref({})
const showRiskDialog = ref(false)
const currentRisk = ref(null)
const riskAction = ref({ action: '', result: '' })

const loadStatistics = async () => {
  try {
    const res = await api.getStatistics()
    statistics.value = res.data
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

const loadAllRisks = async () => {
  try {
    const res = await api.getProjects()
    const projects = res.data
    risks.value = []
    todos.value = []
    
    for (const project of projects) {
      try {
        const riskRes = await api.getRisks(project.id)
        const todoRes = await api.getTodos(project.id)
        
        riskRes.data.forEach(r => {
          r.projectName = project.name
          risks.value.push(r)
        })
        
        todoRes.data.forEach(t => {
          t.projectName = project.name
          todos.value.push(t)
        })
      } catch (e) {
        console.error('加载项目风险失败', e)
      }
    }
    console.log('已加载风险:', risks.value.length, '已加载待办:', todos.value.length)
  } catch (e) {
    ElMessage.error('加载风险数据失败')
  }
}

const getRiskType = (level) => {
  const map = { LOW: 'success', MEDIUM: 'warning', HIGH: 'danger', CRITICAL: 'danger' }
  return map[level] || 'info'
}

const handleRisk = (risk) => {
  currentRisk.value = risk
  riskAction.value = { action: '', result: '' }
  showRiskDialog.value = true
}

const saveRiskAction = async () => {
  try {
    await api.handleRisk(currentRisk.value.id, { ...riskAction.value, handlerId: 'admin' })
    ElMessage.success('风险处理成功')
    showRiskDialog.value = false
    loadAllRisks()
  } catch (e) {
    ElMessage.error('处理失败')
  }
}

const completeTodo = async (todoId) => {
  try {
    await api.completeTodo(todoId)
    ElMessage.success('已完成')
    loadAllRisks()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const scanDelayedTasks = async () => {
  try {
    ElMessage.info('正在扫描即将延期的任务...')
    await loadAllRisks()
    ElMessage.success('扫描完成')
  } catch (e) {
    ElMessage.error('扫描失败')
  }
}

const refreshData = () => {
  loadAllRisks()
  loadStatistics()
  ElMessage.success('数据已刷新')
}

onMounted(() => {
  loadAllRisks()
  loadStatistics()
})
</script>

<style scoped>
.risk-todo-list {
  padding: 20px;
}
</style>
