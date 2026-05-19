import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export default {
  getProjects: () => api.get('/projects'),
  getProject: (id) => api.get(`/projects/${id}`),
  createProject: (data) => api.post('/projects', data),
  updateProjectDates: (id, data) => api.put(`/projects/${id}/dates`, data),
  
  addPhase: (projectId, data) => api.post(`/projects/${projectId}/phases`, data),
  updatePhaseDates: (phaseId, data) => api.put(`/projects/phases/${phaseId}`, data),
  addTask: (projectId, data) => api.post(`/projects/${projectId}/tasks`, data),
  updateTaskProgress: (taskId, data) => api.put(`/projects/tasks/${taskId}/progress`, data),
  addMilestone: (projectId, data) => api.post(`/projects/${projectId}/milestones`, data),
  addMember: (projectId, data) => api.post(`/projects/${projectId}/members`, data),
  deactivateMember: (memberId) => api.post(`/projects/members/${memberId}/deactivate`),
  addLeave: (memberId, data) => api.post(`/projects/members/${memberId}/leave`, data),
  getRisks: (projectId) => api.get(`/projects/${projectId}/risks`),
  handleRisk: (riskId, data) => api.post(`/projects/risks/${riskId}/handle`, data),
  getTodos: (projectId) => api.get(`/projects/${projectId}/todos`),
  completeTodo: (todoId) => api.put(`/projects/todos/${todoId}/complete`),
  getStatistics: () => api.get('/projects/statistics'),
  getManpowerStats: (projectId) => api.get(`/projects/${projectId}/manpower-stats`),
  getLeaveRecords: (projectId) => api.get(`/projects/${projectId}/leave-records`)
}
