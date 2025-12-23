<template>
  <el-container class="layout-root">
    <el-aside width="220px" class="layout-aside">
      <div class="logo">复式记账</div>
      <el-menu :router="true" default-active="/dashboard">
        <el-menu-item index="/dashboard">仪表盘</el-menu-item>
        <el-menu-item index="/accounts">账户</el-menu-item>
        <el-menu-item index="/transactions">交易</el-menu-item>
        <el-menu-item index="/reports">报表</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <span>GnuCash 风格复式记账系统</span>
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts"></script>

<style scoped>
.layout-root {
  height: 100vh;
}
.layout-aside {
  background-color: #1f2933;
  color: #fff;
  display: flex;
  flex-direction: column;
}
.logo {
  padding: 16px;
  font-weight: bold;
  font-size: 18px;
  text-align: center;
}
.layout-header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #e5e7eb;
}
.layout-main {
  padding: 16px;
  background-color: #f5f7fa;
}
</style>












