<template>
  <div class="page" v-loading="loading">
    <el-page-header @back="$router.back()" :content="detail.metadata?.name || '元数据详情'">
      <template #extra>
        <el-button type="primary" :loading="saving" @click="onSaveItems">保存项</el-button>
        <el-button type="danger" @click="onAddItem">新增项</el-button>
      </template>
    </el-page-header>

    <el-card class="info-card" v-if="detail.metadata?.id">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="名称">{{ detail.metadata.name }}</el-descriptions-item>
        <el-descriptions-item label="编码">{{ detail.metadata.code }}</el-descriptions-item>
        <el-descriptions-item label="所属应用">{{ detail.metadata.applicationName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.metadata.status === 1 ? 'success' : 'info'">{{ detail.metadata.status === 1 ? '启用' : '停用' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detail.metadata.description || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="info-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="元数据项" name="items">
          <el-table :data="items" border stripe>
            <el-table-column label="项编码" width="160">
              <template #default="{ row }"><el-input v-model="row.itemCode" size="small" /></template>
            </el-table-column>
            <el-table-column label="项名称" width="180">
              <template #default="{ row }"><el-input v-model="row.itemName" size="small" /></template>
            </el-table-column>
            <el-table-column label="项值" width="180">
              <template #default="{ row }"><el-input v-model="row.itemValue" size="small" /></template>
            </el-table-column>
            <el-table-column label="排序" width="100">
              <template #default="{ row }"><el-input-number v-model="row.sortOrder" size="small" :min="0" controls-position="right" style="width:90px" /></template>
            </el-table-column>
            <el-table-column label="启用" width="80">
              <template #default="{ row }"><el-switch v-model="row.status" :active-value="1" :inactive-value="0" /></template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ $index }">
                <el-button link type="danger" @click="onRemoveItem($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="引用查看" name="refs">
          <el-table :data="refs" border stripe>
            <el-table-column label="模型" prop="modelName" />
            <el-table-column label="字段名" prop="fieldName" width="140" />
            <el-table-column label="显示名" prop="displayName" width="140" />
            <el-table-column label="所属微服务" prop="microserviceName" width="160" />
          </el-table>
          <el-empty v-if="!refs.length" description="暂无引用" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMetadata, batchSaveItems } from '@/api/metadata'
import type { MetadataDetailVO, MetadataItemVO, MetadataRefVO } from '@/types'

const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const detail = reactive<MetadataDetailVO>({} as MetadataDetailVO)
const items = ref<MetadataItemVO[]>([])
const refs = ref<MetadataRefVO[]>([])
const deletedItemIds = ref<string[]>([])
const activeTab = ref('items')

async function load() {
  loading.value = true
  const id = String(route.params.id)
  try {
    const res = await getMetadata(id)
    Object.assign(detail, res)
    items.value = (res.items || []).map(i => ({ ...i }))
    refs.value = res.references || []
  } finally { loading.value = false }
}
function onAddItem() {
  items.value.push({ id: null, metadataId: detail.metadata!.id, itemCode: '', itemName: '', itemValue: '', sortOrder: items.value.length, status: 1 })
}
function onRemoveItem(index: number) {
  const it = items.value[index]
  if (it.id) { deletedItemIds.value.push(it.id) }
  items.value.splice(index, 1)
}
async function onSaveItems() {
  saving.value = true
  try {
    await batchSaveItems(detail.metadata!.id, { items: items.value, deletedItemIds: deletedItemIds.value })
    ElMessage.success('保存成功')
    deletedItemIds.value = []
    await load()
  } finally { saving.value = false }
}
onMounted(load)
</script>

<style scoped>
.info-card { margin-top: 12px; }
</style>
