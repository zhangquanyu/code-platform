<template>
  <el-container class="main-layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">开发平台</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#cbd5e1"
        active-text-color="#409eff"
      >
        <el-menu-item index="/applications">
          <el-icon><Files /></el-icon>
          <span>应用管理</span>
        </el-menu-item>
        <el-menu-item index="/microservices">
          <el-icon><Connection /></el-icon>
          <span>微服务管理</span>
        </el-menu-item>
        <el-menu-item index="/models">
          <el-icon><Grid /></el-icon>
          <span>模型管理</span>
        </el-menu-item>
        <el-menu-item index="/metadata">
          <el-icon><Collection /></el-icon>
          <span>元数据管理</span>
        </el-menu-item>
        <el-menu-item index="/services">
          <el-icon><Share /></el-icon>
          <span>服务管理</span>
        </el-menu-item>
        <el-menu-item index="/orchestrations">
          <el-icon><SetUp /></el-icon>
          <span>服务编排</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="title">{{ currentTitle }}</span>
      </el-header>
      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  Files, Connection, Grid, Collection, Share, SetUp
} from '@element-plus/icons-vue'

const route = useRoute()

const activeMenu = computed(() => {
  const path = route.path
  const top = '/' + path.split('/')[1]
  return top
})

const titleMap: Record<string, string> = {
  '/applications': '应用管理',
  '/microservices': '微服务管理',
  '/models': '模型管理',
  '/metadata': '元数据管理',
  '/services': '服务管理',
  '/orchestrations': '服务编排'
}

const currentTitle = computed(() => {
  const top = '/' + route.path.split('/')[1]
  return titleMap[top] || '开发平台'
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
}
.sidebar {
  background-color: #001529;
  color: #fff;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #1f2937;
}
.header {
  background-color: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  padding: 0 20px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.content {
  background-color: #f3f4f6;
  padding: 16px;
}
:deep(.el-menu) {
  border-right: none;
}
</style>
