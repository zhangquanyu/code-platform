<template>
  <div class="page" v-loading="loading">
    <el-page-header @back="$router.back()" :content="detail.model?.name || '模型详情'">
      <template #extra>
        <el-button type="primary" :loading="saving" @click="onSaveFields">保存字段</el-button>
        <el-button @click="previewVisible = true">预览结构</el-button>
        <el-button type="danger" @click="onAddField">新增字段</el-button>
      </template>
    </el-page-header>

    <el-card class="info-card" v-if="detail.model?.id">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="模型名称">{{ detail.model.name }}</el-descriptions-item>
        <el-descriptions-item label="编码">{{ detail.model.code }}</el-descriptions-item>
        <el-descriptions-item label="所属微服务">{{ detail.model.microserviceName }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="3">{{ detail.model.description || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="info-card">
      <template #header><span>字段管理</span></template>
      <el-table :data="fields" border stripe row-key="id">
        <el-table-column label="字段名" width="150">
          <template #default="{ row }"><el-input v-model="row.name" size="small" /></template>
        </el-table-column>
        <el-table-column label="显示名" width="150">
          <template #default="{ row }"><el-input v-model="row.displayName" size="small" /></template>
        </el-table-column>
        <el-table-column label="数据类型" width="130">
          <template #default="{ row }">
            <el-select v-model="row.fieldType" size="small" @change="onTypeChange(row)">
              <el-option v-for="t in fieldTypes" :key="t" :label="t" :value="t" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="长度" width="90">
          <template #default="{ row }"><el-input-number v-model="row.length" size="small" :min="0" controls-position="right" style="width:80px" /></template>
        </el-table-column>
        <el-table-column label="必填" width="60">
          <template #default="{ row }"><el-switch v-model="row.isRequired" :active-value="1" :inactive-value="0" /></template>
        </el-table-column>
        <el-table-column label="主键" width="60">
          <template #default="{ row }"><el-switch v-model="row.isPrimary" :active-value="1" :inactive-value="0" /></template>
        </el-table-column>
        <el-table-column label="唯一" width="60">
          <template #default="{ row }"><el-switch v-model="row.isUnique" :active-value="1" :inactive-value="0" /></template>
        </el-table-column>
        <el-table-column label="索引" width="60">
          <template #default="{ row }"><el-switch v-model="row.isIndex" :active-value="1" :inactive-value="0" /></template>
        </el-table-column>
        <el-table-column label="默认值" width="120">
          <template #default="{ row }"><el-input v-model="row.defaultValue" size="small" /></template>
        </el-table-column>
        <el-table-column label="关联元数据" width="160">
          <template #default="{ row }">
            <el-select v-model="row.metadataId" size="small" clearable :disabled="row.fieldType !== 'ENUM'" placeholder="选择元数据">
              <el-option v-for="m in metadataList" :key="m.id" :label="m.name" :value="m.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="排序" width="90">
          <template #default="{ row }"><el-input-number v-model="row.sortOrder" size="small" :min="0" controls-position="right" style="width:80px" /></template>
        </el-table-column>
        <el-table-column label="说明" min-width="150">
          <template #default="{ row }"><el-input v-model="row.fieldComment" size="small" /></template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ $index }">
            <el-button link type="danger" @click="onRemoveField($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="previewVisible" title="模型结构预览" width="720px">
      <el-table :data="fields" border>
        <el-table-column label="字段名" prop="name" />
        <el-table-column label="显示名" prop="displayName" />
        <el-table-column label="类型" prop="fieldType" />
        <el-table-column label="主键" width="60"><template #default="{ row }">{{ row.isPrimary ? '是' : '-' }}</template></el-table-column>
        <el-table-column label="必填" width="60"><template #default="{ row }">{{ row.isRequired ? '是' : '-' }}</template></el-table-column>
        <el-table-column label="说明" prop="fieldComment" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getModel, batchSaveFields, updateModel } from '@/api/model'
import { listSimpleApplications } from '@/api/application'
import { getMicroservice } from '@/api/microservice'
import { listMetadataByApp } from '@/api/metadata'
import type { ModelDetailVO, ModelFieldVO, MetadataSimpleVO } from '@/types'

const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const previewVisible = ref(false)
const detail = reactive<ModelDetailVO>({} as ModelDetailVO)
const fields = ref<ModelFieldVO[]>([])
const metadataList = ref<MetadataSimpleVO[]>([])
const fieldTypes = ['TEXT', 'LONGTEXT', 'INTEGER', 'DECIMAL', 'DATE', 'DATETIME', 'BOOLEAN', 'ENUM']
const deletedFieldIds = ref<number[]>([])

async function load() {
  loading.value = true
  const id = Number(route.params.id)
  try {
    const res = await getModel(id)
    Object.assign(detail, res)
    fields.value = res.fields.map(f => ({ ...f }))
    // 加载同应用下的元数据（供枚举字段选择）
    if (res.model?.applicationId) {
      metadataList.value = await listMetadataByApp(res.model.applicationId)
    }
  } finally { loading.value = false }
}

function onTypeChange(row: ModelFieldVO) {
  if (row.fieldType !== 'ENUM') { row.metadataId = null }
}
function onAddField() {
  fields.value.push({
    id: null, modelId: detail.model!.id, name: '', displayName: '', fieldType: 'TEXT',
    length: null, precision: null, isRequired: 0, isPrimary: 0, isUnique: 0, isIndex: 0,
    defaultValue: null, metadataId: null, metadataName: null, sortOrder: fields.value.length, fieldComment: null
  })
}
function onRemoveField(index: number) {
  const f = fields.value[index]
  if (f.id) { deletedFieldIds.value.push(f.id) }
  fields.value.splice(index, 1)
}

async function onSaveFields() {
  saving.value = true
  try {
    await batchSaveFields(detail.model!.id, {
      fields: fields.value,
      deletedFieldIds: deletedFieldIds.value
    })
    ElMessage.success('字段保存成功')
    deletedFieldIds.value = []
    await load()
  } finally { saving.value = false }
}

onMounted(load)
</script>

<style scoped>
.info-card { margin-top: 12px; }
</style>
