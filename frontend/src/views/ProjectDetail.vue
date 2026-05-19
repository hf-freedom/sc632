<template>
  <div class="project-detail">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px">
      <div>
        <h2>{{ project?.name || '项目详情' }}</h2>
        <p style="color: #666; margin-top: 5px">{{ project?.description }}</p>
      </div>
      <div>
        <el-button type="warning" size="small" @click="scanDelayedTasks">
          <el-icon><AlarmClock /></el-icon>
          扫描延期任务
        </el-button>
        <el-button type="primary" size="small" @click="refreshData" style="margin-left: 10px">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button @click="$router.push('/')" style="margin-left: 10px">返回列表</el-button>
      </div>
    </div>

    <el-card style="margin-bottom: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>项目概览</span>
          <el-button type="primary" link size="small" @click="showDateDialog = true">调整计划日期</el-button>
        </div>
      </template>
      <div style="display: flex; gap: 40px; margin-bottom: 15px">
        <div>
          <span style="color: #666">开始日期：</span>
          <span>{{ project?.startDate || '-' }}</span>
        </div>
        <div>
          <span style="color: #666">结束日期：</span>
          <span>{{ project?.endDate || '-' }}</span>
        </div>
        <div>
          <span style="color: #666">项目周期：</span>
          <span>{{ calculateProjectDays() }} 天</span>
        </div>
      </div>
      <div style="display: flex; align-items: center; gap: 15px; margin-bottom: 20px">
        <span style="color: #666">整体进度：</span>
        <el-progress :percentage="Math.round(project?.overallProgress || 0)" style="flex: 1" />
        <span>{{ Math.round(project?.overallProgress || 0) }}%</span>
      </div>
      
      <el-divider />
      
      <div style="margin-bottom: 20px">
        <h4 style="margin: 0 0 15px 0">项目统计</h4>
        <el-row :gutter="15">
          <el-col :span="6">
            <el-card size="small" shadow="never">
              <div style="text-align: center">
                <div style="font-size: 24px; font-weight: bold; color: #666">{{ project?.phases?.length || 0 }}</div>
                <div style="color: #999; font-size: 12px; margin-top: 3px">阶段数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card size="small" shadow="never">
              <div style="text-align: center">
                <div style="font-size: 24px; font-weight: bold; color: #409eff">{{ project?.tasks?.length || 0 }}</div>
                <div style="color: #999; font-size: 12px; margin-top: 3px">总任务</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card size="small" shadow="never">
              <div style="text-align: center">
                <div style="font-size: 24px; font-weight: bold; color: #67c23a">{{ getCompletedTasks() }}</div>
                <div style="color: #999; font-size: 12px; margin-top: 3px">已完成</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card size="small" shadow="never">
              <div style="text-align: center">
                <div style="font-size: 24px; font-weight: bold; color: #f56c6c">{{ getDelayedTasks() }}</div>
                <div style="color: #999; font-size: 12px; margin-top: 3px">已延期</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-row :gutter="15" style="margin-top: 15px">
          <el-col :span="8">
            <el-card size="small" shadow="never">
              <template #header>
                <div style="display: flex; justify-content: space-between; align-items: center">
                  <span style="font-size: 12px; font-weight: bold">任务完成率</span>
                  <el-tag size="mini" type="success">{{ getTaskCompletionRate() }}%</el-tag>
                </div>
              </template>
              <el-progress :percentage="getTaskCompletionRate()" :stroke-width="10" />
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card size="small" shadow="never">
              <template #header>
                <div style="display: flex; justify-content: space-between; align-items: center">
                  <span style="font-size: 12px; font-weight: bold">风险总数</span>
                  <el-tag size="mini" type="warning">{{ project?.risks?.length || 0 }}</el-tag>
                </div>
              </template>
              <div style="display: flex; justify-content: space-around; text-align: center">
                <div>
                  <div style="font-size: 20px; font-weight: bold; color: #409eff">{{ getClosedRisks() }}</div>
                  <div style="font-size: 12px; color: #999">已关闭</div>
                </div>
                <div>
                  <div style="font-size: 20px; font-weight: bold; color: #f56c6c">{{ getOpenRisks() }}</div>
                  <div style="font-size: 12px; color: #999">进行中</div>
                </div>
                <div>
                  <div style="font-size: 20px; font-weight: bold; color: #67c23a">{{ getRiskClosureRate() }}%</div>
                  <div style="font-size: 12px; color: #999">关闭率</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card size="small" shadow="never">
              <template #header>
                <div style="display: flex; justify-content: space-between; align-items: center">
                  <span style="font-size: 12px; font-weight: bold">团队人力</span>
                  <el-tag size="mini" type="info">{{ project?.members?.length || 0 }}人</el-tag>
                </div>
              </template>
              <div style="display: flex; justify-content: space-around; text-align: center">
                <div>
                  <div style="font-size: 20px; font-weight: bold; color: #67c23a">{{ getActiveMembers() }}</div>
                  <div style="font-size: 12px; color: #999">在职</div>
                </div>
                <div>
                  <div style="font-size: 20px; font-weight: bold; color: #e6a23c">{{ getOnLeaveMembers() }}</div>
                  <div style="font-size: 12px; color: #999">请假</div>
                </div>
                <div>
                  <div style="font-size: 20px; font-weight: bold; color: #f56c6c">{{ getInactiveMembers() }}</div>
                  <div style="font-size: 12px; color: #999">离职</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <el-divider />
      
      <div style="border-top: 1px solid #eee; padding-top: 20px">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px">
          <h4 style="margin: 0">项目时间线</h4>
          <el-button type="primary" link size="small" @click="showPhaseEditDialog = true">
            <el-icon><Edit /></el-icon>
            调整阶段日期
          </el-button>
        </div>
        
        <div v-if="project?.phases && project.phases.length > 0">
          <div style="position: relative; height: 60px; background: #f5f7fa; border-radius: 4px; margin-bottom: 10px">
            <div 
              v-for="(phase, index) in project.phases" 
              :key="phase.id"
              style="position: absolute; top: 10px; height: 40px; border-radius: 4px; display: flex; align-items: center; justify-content: center; color: white; font-size: 12px; font-weight: bold; cursor: pointer; transition: all 0.3s"
              :style="{
                left: calculatePhaseLeft(phase) + '%',
                width: calculatePhaseWidth(phase) + '%',
                background: getPhaseColor(index)
              }"
              @click="selectPhase(phase)"
            >
              <span style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap; padding: 0 5px">{{ phase.name }}</span>
            </div>
          </div>
          
          <div style="display: flex; justify-content: space-between; font-size: 12px; color: #999">
            <span>{{ project.startDate }}</span>
            <span>{{ getMidDate() }}</span>
            <span>{{ project.endDate }}</span>
          </div>
          
          <el-divider style="margin: 15px 0" />
          
          <div>
            <div style="font-size: 14px; font-weight: bold; margin-bottom: 10px">阶段详情</div>
            <el-row :gutter="15">
              <el-col :span="8" v-for="(phase, index) in project.phases" :key="phase.id">
                <el-card shadow="hover" size="small" :style="{ borderLeft: `4px solid ${getPhaseColor(index)}` }">
                  <div style="font-weight: bold; margin-bottom: 8px">{{ phase.name }}</div>
                  <div style="font-size: 12px; color: #666">
                    <div>开始：{{ phase.startDate || '未设置' }}</div>
                    <div>结束：{{ phase.endDate || '未设置' }}</div>
                    <div>周期：{{ calculatePhaseDays(phase) }} 天</div>
                    <div>占比：{{ calculatePhaseWidth(phase).toFixed(1) }}%</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </div>
        
        <el-empty v-else description="暂无阶段数据，请先添加项目阶段" />
      </div>
    </el-card>

    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="阶段管理" name="phases">
        <div style="margin-bottom: 15px">
          <el-button type="primary" size="small" @click="showPhaseDialog = true">
            <el-icon><Plus /></el-icon>
            添加阶段
          </el-button>
        </div>
        <el-table :data="project?.phases || []" border v-loading="loading">
          <el-table-column prop="name" label="阶段名称" width="150" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="startDate" label="开始日期" width="120" />
          <el-table-column prop="endDate" label="结束日期" width="120" />
        </el-table>
        <el-empty v-if="!loading && (!project?.phases || project.phases.length === 0)" description="暂无阶段数据，请添加阶段" style="margin-top: 40px" />
      </el-tab-pane>

      <el-tab-pane label="任务管理" name="tasks">
        <div style="margin-bottom: 15px">
          <el-button type="primary" size="small" @click="showTaskDialog = true">
            <el-icon><Plus /></el-icon>
            添加任务
          </el-button>
        </div>
        <el-table :data="project?.tasks || []" border v-loading="loading">
          <el-table-column prop="name" label="任务名称" width="180" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="进度" width="150">
            <template #default="{ row }">
              <el-progress :percentage="row.progress" :stroke-width="8" />
            </template>
          </el-table-column>
          <el-table-column prop="startDate" label="开始日期" width="120" />
          <el-table-column prop="endDate" label="结束日期" width="120" />
          <el-table-column prop="estimatedHours" label="预估工时" width="100" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="updateProgress(row)">更新进度</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && (!project?.tasks || project.tasks.length === 0)" description="暂无任务数据，请添加任务" style="margin-top: 40px" />
      </el-tab-pane>

      <el-tab-pane label="里程碑" name="milestones">
        <div style="margin-bottom: 15px">
          <el-button type="primary" size="small" @click="showMilestoneDialog = true">
            <el-icon><Plus /></el-icon>
            添加里程碑
          </el-button>
        </div>
        <el-table :data="project?.milestones || []" border v-loading="loading">
          <el-table-column prop="name" label="里程碑名称" width="200" />
          <el-table-column prop="plannedDate" label="计划日期" width="120" />
          <el-table-column prop="delayDays" label="延期天数" width="100">
            <template #default="{ row }">
              <span v-if="row.delayDays > 0" style="color: #f56c6c; font-weight: bold">{{ row.delayDays }} 天</span>
              <span v-else style="color: #67c23a">正常</span>
            </template>
          </el-table-column>
          <el-table-column prop="achieved" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.achieved ? 'success' : 'warning'" size="small">
                {{ row.achieved ? '已完成' : '进行中' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && (!project?.milestones || project.milestones.length === 0)" description="暂无里程碑数据" style="margin-top: 40px" />
      </el-tab-pane>

      <el-tab-pane label="团队成员" name="members">
        <div style="margin-bottom: 20px">
          <el-row :gutter="20">
            <el-col :span="4">
              <el-card shadow="hover" style="text-align: center">
                <div style="font-size: 28px; font-weight: bold; color: #67c23a">{{ manpowerStats.totalMembers || 0 }}</div>
                <div style="color: #999; font-size: 12px; margin-top: 5px">总成员数</div>
              </el-card>
            </el-col>
            <el-col :span="4">
              <el-card shadow="hover" style="text-align: center">
                <div style="font-size: 28px; font-weight: bold; color: #409eff">{{ manpowerStats.availableMembers || 0 }}</div>
                <div style="color: #999; font-size: 12px; margin-top: 5px">可用成员</div>
              </el-card>
            </el-col>
            <el-col :span="4">
              <el-card shadow="hover" style="text-align: center">
                <div style="font-size: 28px; font-weight: bold; color: #e6a23c">{{ manpowerStats.onLeaveMembers || 0 }}</div>
                <div style="color: #999; font-size: 12px; margin-top: 5px">请假中</div>
              </el-card>
            </el-col>
            <el-col :span="4">
              <el-card shadow="hover" style="text-align: center">
                <div style="font-size: 28px; font-weight: bold; color: #909399">{{ manpowerStats.inactiveMembers || 0 }}</div>
                <div style="color: #999; font-size: 12px; margin-top: 5px">已离职</div>
              </el-card>
            </el-col>
            <el-col :span="4">
              <el-card shadow="hover" style="text-align: center">
                <div style="font-size: 28px; font-weight: bold; color: #f56c6c">{{ manpowerStats.gapHours || 0 }}</div>
                <div style="color: #999; font-size: 12px; margin-top: 5px">缺口工时(小时)</div>
              </el-card>
            </el-col>
            <el-col :span="4">
              <el-card shadow="hover" style="text-align: center">
                <div style="font-size: 28px; font-weight: bold; color: #67c23c">{{ manpowerStats.totalEstimatedHours || 0 }}</div>
                <div style="color: #999; font-size: 12px; margin-top: 5px">预计总工时</div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <el-tabs type="border-card" v-model="memberTab">
          <el-tab-pane label="成员列表" name="list">
            <div style="margin-bottom: 15px">
              <el-button type="primary" size="small" @click="showMemberDialog = true">
                <el-icon><Plus /></el-icon>
                添加成员
              </el-button>
              <el-button type="info" size="small" @click="loadManpowerStats" style="margin-left: 10px">
                <el-icon><Refresh /></el-icon>
                刷新数据
              </el-button>
            </div>
            <el-table :data="manpowerStats.memberWorkload || []" border v-loading="loading">
              <el-table-column prop="memberName" label="姓名" width="120" />
              <el-table-column prop="assignedHours" label="分配工时" width="120">
                <template #default="{ row }">
                  <span :style="{ color: row.workloadLevel === 'OVERLOAD' ? '#f56c6c' : (row.workloadLevel === 'NORMAL' ? '#67c23a' : '#909399') }">
                    {{ row.assignedHours }}h
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="workloadLevel" label="负载状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.workloadLevel === 'OVERLOAD' ? 'danger' : (row.workloadLevel === 'NORMAL' ? 'success' : 'info')" size="small">
                    {{ row.workloadLevel === 'OVERLOAD' ? '过载' : (row.workloadLevel === 'NORMAL' ? '正常' : '空闲') }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="active" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.active ? (row.onLeave ? 'warning' : 'success') : 'danger'" size="small">
                    {{ row.active ? (row.onLeave ? '请假中' : '在职') : '已离职' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220">
                <template #default="{ row }">
                  <el-button v-if="row.active" type="warning" link size="small" @click="addLeaveById(row.memberId, row.memberName)">请假登记</el-button>
                  <el-button v-if="row.active" type="danger" link size="small" @click="confirmDeactivate(row.memberId, row.memberName)">标记离职</el-button>
                  <span v-if="!row.active" style="color: #999; font-size: 12px">已离职</span>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!loading && (!manpowerStats.memberWorkload || manpowerStats.memberWorkload.length === 0)" description="暂无团队成员数据" style="margin-top: 40px" />
          </el-tab-pane>

          <el-tab-pane label="请假记录" name="leave">
            <el-table :data="leaveRecords" border v-loading="loading">
              <el-table-column prop="memberName" label="成员姓名" width="120" />
              <el-table-column prop="startDate" label="开始日期" width="120" />
              <el-table-column prop="endDate" label="结束日期" width="120" />
              <el-table-column prop="reason" label="请假原因" />
              <el-table-column label="天数" width="80">
                <template #default="{ row }">
                  {{ calculateLeaveDays(row.startDate, row.endDate) }}天
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!loading && leaveRecords.length === 0" description="暂无请假记录" style="margin-top: 40px" />
          </el-tab-pane>

          <el-tab-pane label="任务分配情况" name="tasks">
            <el-table :data="manpowerStats.taskAssignments || []" border v-loading="loading">
              <el-table-column prop="taskName" label="任务名称" width="200" />
              <el-table-column prop="assigneeName" label="负责人" width="120">
                <template #default="{ row }">
                  <span v-if="row.assigneeName">{{ row.assigneeName }}</span>
                  <el-tag v-else type="info" size="small">未分配</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="estimatedHours" label="预计工时" width="100" />
              <el-table-column prop="status" label="任务状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="成员状态" width="100">
                <template #default="{ row }">
                  <el-tag v-if="row.assigneeName" :type="!row.memberActive ? 'danger' : (row.onLeave ? 'warning' : 'success')" size="small">
                    {{ !row.memberActive ? '已离职' : (row.onLeave ? '请假中' : '正常') }}
                  </el-tag>
                  <span v-else>-</span>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!loading && (!manpowerStats.taskAssignments || manpowerStats.taskAssignments.length === 0)" description="暂无任务数据" style="margin-top: 40px" />
          </el-tab-pane>
        </el-tabs>
      </el-tab-pane>

      <el-tab-pane label="风险管理" name="risks">
        <el-table :data="risks || []" border v-loading="loading">
          <el-table-column prop="title" label="风险标题" width="200" />
          <el-table-column prop="level" label="风险等级" width="100">
            <template #default="{ row }">
              <el-tag :type="getRiskType(row.level)" size="small">{{ row.level }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column prop="description" label="风险描述" />
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleRisk(row)">处理</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && (!risks || risks.length === 0)" description="暂无风险数据" style="margin-top: 40px" />
      </el-tab-pane>

      <el-tab-pane label="待办事项" name="todos">
        <el-table :data="todos || []" border v-loading="loading">
          <el-table-column prop="title" label="待办事项" width="200" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="dueDate" label="截止日期" width="180" />
          <el-table-column prop="completed" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.completed ? 'success' : 'warning'" size="small">
                {{ row.completed ? '已完成' : '待处理' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button 
                v-if="!row.completed"
                type="success" 
                link 
                size="small" 
                @click="completeTodo(row.id)"
              >
                完成
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && (!todos || todos.length === 0)" description="暂无待办事项" style="margin-top: 40px" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="showDateDialog" title="调整项目日期" width="500px">
      <el-form :model="dateForm" label-width="100px">
        <el-form-item label="开始日期">
          <el-date-picker v-model="dateForm.startDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="dateForm.endDate" type="date" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDateDialog = false">取消</el-button>
        <el-button type="primary" @click="updateProjectDates">确认更新</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showPhaseDialog" title="添加阶段" width="500px">
      <el-form :model="newPhase" label-width="100px">
        <el-form-item label="阶段名称">
          <el-input v-model="newPhase.name" placeholder="请输入阶段名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="newPhase.description" type="textarea" :rows="3" placeholder="请输入阶段描述" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="newPhase.startDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="newPhase.endDate" type="date" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPhaseDialog = false">取消</el-button>
        <el-button type="primary" @click="addPhase">确认添加</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showPhaseEditDialog" title="调整阶段日期" width="500px">
      <el-form label-width="100px">
        <el-form-item label="阶段名称" disabled>
          <span>{{ selectedPhase?.name }}</span>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="selectedPhase.startDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="selectedPhase.endDate" type="date" style="width: 100%" />
        </el-form-item>
      </el-form>
      <div v-if="selectedPhase && selectedPhase.startDate && selectedPhase.endDate" style="background: #f5f7fa; padding: 15px; border-radius: 4px; margin-bottom: 15px">
        <div style="color: #666; margin-bottom: 5px">
          <span>阶段周期：</span>
          <span style="font-weight: bold; color: #409eff">{{ calculatePhaseDays(selectedPhase) }} 天</span>
        </div>
        <div style="color: #666">
          <span>占项目周期比例：</span>
          <span style="font-weight: bold; color: #67c23a">{{ calculatePhaseWidth(selectedPhase).toFixed(1) }}%</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPhaseEditDialog = false">取消</el-button>
        <el-button type="primary" @click="savePhaseDates">确认调整</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showTaskDialog" title="添加任务" width="600px">
      <el-form :model="newTask" label-width="120px">
        <el-form-item label="任务名称">
          <el-input v-model="newTask.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="newTask.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="所属阶段">
          <el-select v-model="newTask.phaseId" style="width: 100%" placeholder="请选择所属阶段">
            <el-option 
              v-for="phase in project?.phases || []" 
              :key="phase.id" 
              :label="phase.name" 
              :value="phase.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="前置任务">
          <el-select v-model="newTask.prerequisiteTaskIds" multiple style="width: 100%" placeholder="请选择前置任务">
            <el-option 
              v-for="task in project?.tasks || []" 
              :key="task.id" 
              :label="task.name" 
              :value="task.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="newTask.startDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="newTask.endDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预计工时">
          <el-input-number v-model="newTask.estimatedHours" :min="1" :max="1000" />
          <span style="margin-left: 10px; color: #999">小时</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTaskDialog = false">取消</el-button>
        <el-button type="primary" @click="addTask">确认添加</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showMilestoneDialog" title="添加里程碑" width="500px">
      <el-form :model="newMilestone" label-width="120px">
        <el-form-item label="里程碑名称">
          <el-input v-model="newMilestone.name" placeholder="请输入里程碑名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="newMilestone.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="计划日期">
          <el-date-picker v-model="newMilestone.plannedDate" type="date" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showMilestoneDialog = false">取消</el-button>
        <el-button type="primary" @click="addMilestone">确认添加</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showMemberDialog" title="添加成员" width="500px">
      <el-form :model="newMember" label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="newMember.name" placeholder="请输入成员姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="newMember.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item label="角色">
          <el-input v-model="newMember.role" placeholder="请输入成员角色" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showMemberDialog = false">取消</el-button>
        <el-button type="primary" @click="addMember">确认添加</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showProgressDialog" title="更新任务进度" width="500px">
      <el-form label-width="120px">
        <el-form-item label="任务名称" disabled>
          <span>{{ currentTask?.name }}</span>
        </el-form-item>
        <el-form-item label="当前进度">
          <el-slider 
            v-model="progressValue" 
            :marks="{ 0: '0%', 25: '25%', 50: '50%', 75: '75%', 100: '100%' }"
            :step="5"
          />
          <div style="text-align: center; font-size: 24px; font-weight: bold; color: #409eff">
            {{ progressValue }}%
          </div>
        </el-form-item>
        <el-form-item label="实际开始日期">
          <el-date-picker v-model="progressForm.actualStartDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="实际结束日期">
          <el-date-picker v-model="progressForm.actualEndDate" type="date" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showProgressDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProgress">确认更新</el-button>
      </template>
    </el-dialog>

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

    <el-dialog v-model="showLeaveDialog" title="请假登记" width="500px">
      <el-form label-width="100px">
        <el-form-item label="成员姓名" disabled>
          <span>{{ currentMember?.name }}</span>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="leaveRecord.startDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="leaveRecord.endDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="请假原因">
          <el-input v-model="leaveRecord.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLeaveDialog = false">取消</el-button>
        <el-button type="primary" @click="saveLeave">确认登记</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AlarmClock, Refresh, Plus, Edit } from '@element-plus/icons-vue'
import api from '../api'

const props = defineProps(['id'])
const project = ref(null)
const risks = ref([])
const todos = ref([])
const loading = ref(false)
const activeTab = ref('phases')
const memberTab = ref('list')
const manpowerStats = ref({})
const leaveRecords = ref([])

const showDateDialog = ref(false)
const showPhaseDialog = ref(false)
const showPhaseEditDialog = ref(false)
const showTaskDialog = ref(false)
const selectedPhase = ref(null)
const showMilestoneDialog = ref(false)
const showMemberDialog = ref(false)
const showProgressDialog = ref(false)
const showRiskDialog = ref(false)
const showLeaveDialog = ref(false)

const dateForm = ref({ startDate: '', endDate: '' })
const newPhase = ref({ name: '', description: '', startDate: '', endDate: '' })
const newTask = ref({ name: '', description: '', phaseId: '', prerequisiteTaskIds: [], startDate: '', endDate: '', estimatedHours: 8 })
const newMilestone = ref({ name: '', description: '', plannedDate: '' })
const newMember = ref({ name: '', email: '', role: '' })
const progressValue = ref(0)
const currentTask = ref(null)
const currentRisk = ref(null)
const currentMember = ref(null)
const progressForm = ref({ actualStartDate: '', actualEndDate: '' })
const riskAction = ref({ action: '', result: '' })
const leaveRecord = ref({ startDate: '', endDate: '', reason: '' })

const loadProject = async () => {
  loading.value = true
  try {
    console.log('正在加载项目数据...')
    const res = await api.getProject(props.id)
    project.value = res.data
    console.log('项目加载完成:', project.value)
    console.log('任务列表:', project.value?.tasks)
  } catch (e) {
    console.error('加载项目失败:', e)
    ElMessage.error('加载项目失败')
  } finally {
    loading.value = false
  }
}

const loadRisks = async () => {
  try {
    const res = await api.getRisks(props.id)
    risks.value = res.data
    console.log('风险列表:', risks.value)
  } catch (e) {
    console.error('加载风险失败:', e)
  }
}

const loadTodos = async () => {
  try {
    const res = await api.getTodos(props.id)
    todos.value = res.data
    console.log('待办列表:', todos.value)
  } catch (e) {
    console.error('加载待办失败:', e)
  }
}

const loadManpowerStats = async () => {
  try {
    const res = await api.getManpowerStats(props.id)
    manpowerStats.value = res.data
    console.log('人力统计:', manpowerStats.value)
  } catch (e) {
    console.error('加载人力统计失败:', e)
  }
}

const loadLeaveRecords = async () => {
  try {
    const res = await api.getLeaveRecords(props.id)
    leaveRecords.value = res.data.map(record => {
      const member = project.value?.members?.find(m => m.id === record.memberId)
      return { ...record, memberName: member?.name || '未知' }
    })
    console.log('请假记录:', leaveRecords.value)
  } catch (e) {
    console.error('加载请假记录失败:', e)
  }
}

const refreshData = async () => {
  await Promise.all([loadProject(), loadRisks(), loadTodos()])
  await Promise.all([loadManpowerStats(), loadLeaveRecords()])
  ElMessage.success('数据已刷新')
}

const calculateLeaveDays = (startDate, endDate) => {
  if (!startDate || !endDate) return 0
  const start = new Date(startDate)
  const end = new Date(endDate)
  return Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1
}

const calculateProjectDays = () => {
  if (!project.value?.startDate || !project.value?.endDate) return 0
  return calculateLeaveDays(project.value.startDate, project.value.endDate)
}

const getCompletedTasks = () => {
  if (!project.value?.tasks) return 0
  return project.value.tasks.filter(t => t.status === 'COMPLETED').length
}

const getDelayedTasks = () => {
  if (!project.value?.tasks) return 0
  return project.value.tasks.filter(t => t.status === 'DELAYED').length
}

const getTaskCompletionRate = () => {
  if (!project.value?.tasks || project.value.tasks.length === 0) return 0
  return Math.round((getCompletedTasks() / project.value.tasks.length) * 100)
}

const getClosedRisks = () => {
  if (!project.value?.risks) return 0
  return project.value.risks.filter(r => r.status === 'CLOSED').length
}

const getOpenRisks = () => {
  if (!project.value?.risks) return 0
  return project.value.risks.filter(r => r.status !== 'CLOSED').length
}

const getRiskClosureRate = () => {
  if (!project.value?.risks || project.value.risks.length === 0) return 0
  return Math.round((getClosedRisks() / project.value.risks.length) * 100)
}

const getActiveMembers = () => {
  if (!project.value?.members) return 0
  return project.value.members.filter(m => m.active).length
}

const getOnLeaveMembers = () => {
  if (!project.value?.members) return 0
  return project.value.members.filter(m => m.active && m.leaveRecords && m.leaveRecords.length > 0).length
}

const getInactiveMembers = () => {
  if (!project.value?.members) return 0
  return project.value.members.filter(m => !m.active).length
}

const calculatePhaseLeft = (phase) => {
  if (!project.value?.startDate || !phase.startDate) return 0
  const projectStart = new Date(project.value.startDate)
  const phaseStart = new Date(phase.startDate)
  const projectDays = calculateProjectDays()
  const daysFromStart = Math.ceil((phaseStart - projectStart) / (1000 * 60 * 60 * 24))
  return projectDays > 0 ? (daysFromStart / projectDays) * 100 : 0
}

const calculatePhaseWidth = (phase) => {
  if (!project.value?.startDate || !project.value?.endDate || !phase.startDate || !phase.endDate) return 0
  const projectDays = calculateProjectDays()
  const phaseDays = calculatePhaseDays(phase)
  return projectDays > 0 ? (phaseDays / projectDays) * 100 : 0
}

const calculatePhaseDays = (phase) => {
  if (!phase.startDate || !phase.endDate) return 0
  return calculateLeaveDays(phase.startDate, phase.endDate)
}

const getPhaseColor = (index) => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#00d4ff', '#722ed1', '#eb2f96']
  return colors[index % colors.length]
}

const getMidDate = () => {
  if (!project.value?.startDate || !project.value?.endDate) return '-'
  const projectStart = new Date(project.value.startDate)
  const projectEnd = new Date(project.value.endDate)
  const midTime = (projectStart.getTime() + projectEnd.getTime()) / 2
  const midDate = new Date(midTime)
  return midDate.toISOString().split('T')[0]
}

const selectPhase = (phase) => {
  selectedPhase.value = { ...phase }
  showPhaseEditDialog.value = true
}

const savePhaseDates = async () => {
  try {
    await api.updatePhaseDates(selectedPhase.value.id, {
      startDate: selectedPhase.value.startDate,
      endDate: selectedPhase.value.endDate
    })
    ElMessage.success('阶段日期调整成功')
    showPhaseEditDialog.value = false
    loadProject()
  } catch (e) {
    ElMessage.error('调整失败')
  }
}

const getStatusType = (status) => {
  const map = { PENDING: 'info', IN_PROGRESS: 'primary', COMPLETED: 'success', DELAYED: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { PENDING: '待开始', IN_PROGRESS: '进行中', COMPLETED: '已完成', DELAYED: '已延期' }
  return map[status] || status
}

const getRiskType = (level) => {
  const map = { LOW: 'success', MEDIUM: 'warning', HIGH: 'danger', CRITICAL: 'danger' }
  return map[level] || 'info'
}

const updateProjectDates = async () => {
  try {
    await api.updateProjectDates(props.id, dateForm.value)
    ElMessage.success('项目日期已更新')
    showDateDialog.value = false
    loadProject()
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

const addPhase = async () => {
  try {
    await api.addPhase(props.id, newPhase.value)
    ElMessage.success('添加阶段成功')
    showPhaseDialog.value = false
    newPhase.value = { name: '', description: '', startDate: '', endDate: '' }
    loadProject()
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

const addTask = async () => {
  try {
    console.log('准备添加任务:', newTask.value)
    await api.addTask(props.id, newTask.value)
    ElMessage.success('添加任务成功')
    showTaskDialog.value = false
    newTask.value = { name: '', description: '', phaseId: '', prerequisiteTaskIds: [], startDate: '', endDate: '', estimatedHours: 8 }
    loadProject()
  } catch (e) {
    console.error('添加任务失败:', e)
    ElMessage.error('添加失败')
  }
}

const addMilestone = async () => {
  try {
    await api.addMilestone(props.id, newMilestone.value)
    ElMessage.success('添加里程碑成功')
    showMilestoneDialog.value = false
    newMilestone.value = { name: '', description: '', plannedDate: '' }
    loadProject()
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

const addMember = async () => {
  try {
    await api.addMember(props.id, newMember.value)
    ElMessage.success('添加成员成功')
    showMemberDialog.value = false
    newMember.value = { name: '', email: '', role: '' }
    loadProject()
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

const updateProgress = (task) => {
  currentTask.value = task
  progressValue.value = task.progress
  progressForm.value = { actualStartDate: task.actualStartDate, actualEndDate: task.actualEndDate }
  showProgressDialog.value = true
}

const saveProgress = async () => {
  try {
    await api.updateTaskProgress(currentTask.value.id, {
      progress: progressValue.value,
      actualStartDate: progressForm.value.actualStartDate,
      actualEndDate: progressForm.value.actualEndDate
    })
    ElMessage.success('进度更新成功')
    showProgressDialog.value = false
    loadProject()
    loadRisks()
    loadTodos()
  } catch (e) {
    ElMessage.error(e.response?.data?.error || '更新失败')
  }
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
    loadRisks()
  } catch (e) {
    ElMessage.error('处理失败')
  }
}

const completeTodo = async (todoId) => {
  try {
    await api.completeTodo(todoId)
    ElMessage.success('已完成')
    loadTodos()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const addLeave = (member) => {
  currentMember.value = member
  leaveRecord.value = { startDate: '', endDate: '', reason: '' }
  showLeaveDialog.value = true
}

const addLeaveById = (memberId, memberName) => {
  currentMember.value = { id: memberId, name: memberName }
  leaveRecord.value = { startDate: '', endDate: '', reason: '' }
  showLeaveDialog.value = true
}

const saveLeave = async () => {
  try {
    await api.addLeave(currentMember.value.id, leaveRecord.value)
    ElMessage.success('请假登记成功')
    showLeaveDialog.value = false
    loadProject()
    loadRisks()
    loadManpowerStats()
    loadLeaveRecords()
  } catch (e) {
    ElMessage.error('登记失败')
  }
}

const confirmDeactivate = async (memberId, memberName) => {
  try {
    await ElMessageBox.confirm(
      `确定要将成员"${memberName}"标记为离职吗？标记后该成员的任务会产生人力缺口风险。`,
      '确认标记离职',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await api.deactivateMember(memberId)
    ElMessage.success('已标记为离职')
    loadProject()
    loadRisks()
    loadManpowerStats()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const deactivateMember = async (memberId) => {
  try {
    await api.deactivateMember(memberId)
    ElMessage.success('操作成功')
    loadProject()
    loadRisks()
    loadManpowerStats()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const scanDelayedTasks = async () => {
  ElMessage.info('正在扫描即将延期的任务...')
  await refreshData()
  ElMessage.success('扫描完成')
}

onMounted(() => {
  refreshData()
})
</script>

<style scoped>
.project-detail {
  padding: 20px;
}
</style>
