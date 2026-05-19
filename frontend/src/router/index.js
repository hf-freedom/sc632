import { createRouter, createWebHistory } from 'vue-router'
import ProjectList from '../views/ProjectList.vue'
import ProjectDetail from '../views/ProjectDetail.vue'
import Statistics from '../views/Statistics.vue'
import RiskTodoList from '../views/RiskTodoList.vue'

const routes = [
  { path: '/', name: 'ProjectList', component: ProjectList },
  { path: '/project/:id', name: 'ProjectDetail', component: ProjectDetail, props: true },
  { path: '/statistics', name: 'Statistics', component: Statistics },
  { path: '/risk-todo', name: 'RiskTodoList', component: RiskTodoList }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
