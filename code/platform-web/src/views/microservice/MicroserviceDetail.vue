<template>
  <div class="page" v-loading="loading">
    <el-page-header @back="$router.back()" :content="ms.name || '微服务详情'" />
    <el-card class="info-card" v-if="ms.id">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="微服务名称">{{ ms.name }}</el-descriptions-item>
        <el-descriptions-item label="编码">{{ ms.code }}</el-descriptions-item>
        <el-descriptions-item label="所属应用">{{ ms.applicationName }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ ms.version }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="ms.status === 1 ? 'success' : 'info'">{{ ms.status === 1 ? '启用' : '停用' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述">{{ ms.description || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-row :gutter="16" class="summary">
        <el-col :span="8">
          <el-statistic title="模型数" :value="summary.modelCount" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="服务数" :value="summary.serviceCount" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="编排数" :value="summary.orchestrationCount" />
        </el-col>
      </el-row>
    </el-card>

    <el-card class="info-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="模型" name="models">
          <el-button type="primary" size="small" @click="$router.push({ path: '/models', query: { microserviceId: ms.id } })">管理模型</el-button>
          <el-table :data="models" border stripe style="margin-top: 12px">
            <el-table-column label="名称" prop="name">
              <template #default="{ row }"><el-link type="primary" @click="$router.push(`/models/${row.id}`)">{{ row.name }}</el-link></template>
            </el-table-column>
            <el-table-column label="编码" prop="code" width="160" />
            <el-table-column label="字段数" prop="fieldCount" width="90" />
            <el-table-column label="创建时间" prop="createTime" width="170" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="服务" name="services">
          <el-button type="primary" size="small" @click="$router.push({ path: '/services', query: { microserviceId: ms.id } })">管理服务</el-button>
          <el-table :data="services" border stripe style="margin-top: 12px">
            <el-table-column label="名称" prop="name">
              <template #default="{ row }"><el-link type="primary" @click="$router.push(`/services/${row.id}`)">{{ row.name }}</el-link></template>
            </el-table-column>
            <el-table-column label="编码" prop="code" width="140" />
            <el-table-column label="请求方式" prop="httpMethod" width="100" />
            <el-table-column label="路径" prop="servicePath" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="服务编排" name="orchestrations">
          <el-button type="primary" size="small" @click="$router.push({ path: '/orchestrations', query: { microserviceId: ms.id } })">管理编排</el-button>
          <el-table :data="orchestrations" border stripe style="margin-top: 12px">
            <el-table-column label="名称" prop="name">
              <template #default="{ row }"><el-link type="primary" @click="$router.push(`/orchestrations/${row.id}`)">{{ row.name }}</el-link></template>
            </el-table-column>
            <el-table-column label="编码" prop="code" width="140" />
            <el-table-column label="节点数" prop="nodeCount" width="90" />
            <el-table-column label="连线数" prop="edgeCount" width="90" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getMicroservice, getMicroserviceSummary } from '@/api/microservice'
import { pageModels } from '@/api/model'
import { pageServices } from '@/api/service'
import { pageOrchestrations } from '@/api/orchestration'
import type { MicroserviceVO, MicroserviceSummaryVO, ModelVO, ServiceVO, OrchestrationVO } from '@/types'

const route = useRoute()
const loading = ref(false)
const ms = ref<Partial<MicroserviceVO>>({})
const summary = ref<MicroserviceSummaryVO>({ modelCount: 0, serviceCount: 0, orchestrationCount: 0 })
const activeTab = ref('models')
const models = ref<ModelVO[]>([])
const services = ref<ServiceVO[]>([])
const orchestrations = ref<OrchestrationVO[]>([])

async function load() {
  loading.value = true
  const id = Number(route.params.id)
  try {
    ms.value = await getMicroservice(id)
    summary.value = await getMicroserviceSummary(id)
    models.value = (await pageModels({ microserviceId: id, pageSize: 100 })).list
    services.value = (await pageServices({ microserviceId: id, pageSize: 100 })).list
    orchestrations.value = (await pageOrchestrations({ microserviceId: id, pageSize: 100 })).list
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.info-card { margin-top: 12px; }
.summary { margin-top: 16px; }
</style>
