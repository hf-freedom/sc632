<template>
  <div id="app">
    <el-container style="height: 100vh">
      <el-header style="background: #001529; color: white; display: flex; align-items: center; padding: 0 20px">
        <h1 style="margin: 0; font-size: 20px">项目里程碑风险管理系统</h1>
        <el-menu
          style="margin-left: 30px; background: #001529"
          mode="horizontal"
          :default-active="$route.path"
          router
          text-color="#fff"
          active-text-color="#ffd04b"
        >
          <el-menu-item index="/">项目列表</el-menu-item>
          <el-menu-item index="/risk-todo">风险待办中心</el-menu-item>
          <el-menu-item index="/statistics">数据统计</el-menu-item>
        </el-menu>
      </el-header>
      <el-main style="background: #f0f2f5">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
#app {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
}
</style>
