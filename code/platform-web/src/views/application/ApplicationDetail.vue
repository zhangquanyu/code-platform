<template>
  <div class="page" v-loading="loading">
    <el-page-header @back="$router.back()" :content="app.name || '应用详情'" />
    <el-card class="info-card" v-if="app.id">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="应用名称">{{ app.name }}</el-descriptions-item>
        <el-descriptions-item label="编码">{{ app.code }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ app.version }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="app.status === 1 ? 'success' : 'info'">{{ app.status === 1 ? '启用' : '停用' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ app.createTime }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ app.description || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="info-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="微服务" name="microservices">
          <el-button type="primary" size="small" @click="$router.push({ path: '/microservices', query: { applicationId: app.id } })">
            管理微服务
          </el-button>
          <el-table :data="microservices" border stripe style="margin-top: 12px">
            <el-table-column label="名称" prop="name">
              <template #default="{ row }">
                <el-link type="primary" @click="$router.push(`/microservices/${row.id}`)">{{ row.name }}</el-link>
              </template>
            </el-table-column>
            <el-table-column label="编码" prop="code" width="160" />
            <el-table-column label="版本" prop="version" width="100" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="170" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="元数据" name="metadata">
          <el-button type="primary" size="small" @click="$router.push({ path: '/metadata', query: { applicationId: app.id } })">
            管理元数据
          </el-button>
          <el-table :data="metadata" border stripe style="margin-top: 12px">
            <el-table-column label="名称" prop="name">
              <template #default="{ row }">
                <el-link type="primary" @click="$router.push(`/metadata/${row.id}`)">{{ row.name }}</el-link>
              </template>
            </el-table-column>
            <el-table-column label="编码" prop="code" width="160" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="170" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getApplication } from '@/api/application'
import { pageMicroservices } from '@/api/microservice'
import { pageMetadata } from '@/api/metadata'
import type { ApplicationVO, MicroserviceVO, MetadataVO } from '@/types'

const route = useRoute()
const loading = ref(false)
const app = ref<Partial<ApplicationVO>>({})
const activeTab = ref('microservices')
const microservices = ref<MicroserviceVO[]>([])
const metadata = ref<MetadataVO[]>([])

async function load() {
  loading.value = true
  const id = String(route.params.id)
  try {
    app.value = await getApplication(id)
    const msRes = await pageMicroservices({ applicationId: id, pageSize: 100 })
    microservices.value = msRes.list
    const metaRes = await pageMetadata({ applicationId: id, pageSize: 100 })
    metadata.value = metaRes.list
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.info-card { margin-top: 12px; }
</style>
