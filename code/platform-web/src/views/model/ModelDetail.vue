<template>
  <div class="page" v-loading="loading">
    <el-page-header @back="$router.back()" :content="detail.model?.name || '模型详情'">
      <template #extra>
        <el-button type="primary" :loading="saving" @click="onSaveAll">保存全部</el-button>
        <el-button @click="previewVisible = true">预览结构</el-button>
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
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="字段管理" name="fields">
          <div class="tab-toolbar">
            <el-button type="primary" @click="onAddField">新增字段</el-button>
          </div>
          <el-table :data="fields" border stripe row-key="id" max-height="600">
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
            <el-table-column label="精度" width="90">
              <template #default="{ row }">
                <el-input-number v-model="row.precision" size="small" :min="0" controls-position="right" style="width:80px" />
              </template>
            </el-table-column>
            <el-table-column label="必填" width="60">
              <template #default="{ row }"><el-switch v-model="row.isRequired" :active-value="1" :inactive-value="0" /></template>
            </el-table-column>
            <el-table-column label="主键" width="60">
              <template #default="{ row }"><el-switch v-model="row.isPrimary" :active-value="1" :inactive-value="0" /></template>
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
        </el-tab-pane>

        <el-tab-pane label="索引管理" name="indexes">
          <div class="tab-toolbar">
            <el-button type="primary" @click="onAddIndex">+ 新增索引</el-button>
          </div>
          <el-table :data="indexes" border stripe row-key="id" max-height="600">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column label="索引名称" width="180">
              <template #default="{ row }"><el-input v-model="row.indexName" size="small" placeholder="索引名称" /></template>
            </el-table-column>
            <el-table-column label="索引栏位" min-width="250">
              <template #default="{ row }">
                <el-select
                  v-model="row.fieldIds"
                  multiple
                  filterable
                  collapse-tags
                  collapse-tags-tooltip
                  size="small"
                  placeholder="选择字段"
                  style="width: 100%"
                >
                  <el-option
                    v-for="f in fields"
                    :key="f.id"
                    :label="f.name"
                    :value="f.id"
                    :disabled="!f.id"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="索引类型" width="140">
              <template #default="{ row }">
                <el-select v-model="row.indexType" size="small" style="width: 100%">
                  <el-option label="普通索引" value="NORMAL" />
                  <el-option label="唯一索引" value="UNIQUE" />
                  <el-option label="全文索引" value="FULLTEXT" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ $index }">
                <el-popconfirm title="确认删除？" @confirm="onRemoveIndex($index)">
                  <template #reference><el-button link type="danger">删除</el-button></template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="indexes.length === 0" description="暂无索引，点击「新增索引」添加" />
        </el-tab-pane>
      </el-tabs>
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
      <el-divider content-position="left">索引</el-divider>
      <el-table :data="indexes" border>
        <el-table-column label="索引名称" prop="indexName" />
        <el-table-column label="类型" prop="indexType" />
        <el-table-column label="字段" prop="fieldNames" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getModel, batchSaveFields, batchSaveIndexes } from '@/api/model'
import { listMetadataByApp } from '@/api/metadata'
import type { ModelDetailVO, ModelFieldVO, ModelIndexVO, MetadataSimpleVO } from '@/types'

const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const previewVisible = ref(false)
const activeTab = ref('fields')
const detail = reactive<ModelDetailVO>({} as ModelDetailVO)
const fields = ref<ModelFieldVO[]>([])
const indexes = ref<ModelIndexVO[]>([])
const metadataList = ref<MetadataSimpleVO[]>([])
const fieldTypes = ['TEXT', 'LONGTEXT', 'INTEGER', 'DECIMAL', 'DATE', 'DATETIME', 'BOOLEAN', 'ENUM']
const deletedFieldIds = ref<string[]>([])
const deletedIndexIds = ref<string[]>([])

async function load() {
  loading.value = true
  const id = String(route.params.id)
  try {
    const res = await getModel(id)
    Object.assign(detail, res)
    fields.value = res.fields.map(f => ({ ...f }))
    indexes.value = (res.indexes || []).map(i => ({ ...i }))
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
    length: null, precision: null, isRequired: 0, isPrimary: 0, isIndex: 0,
    defaultValue: null, metadataId: null, metadataName: null, sortOrder: fields.value.length, fieldComment: null
  })
}

function onRemoveField(index: number) {
  const f = fields.value[index]
  if (f.id) { deletedFieldIds.value.push(f.id) }
  fields.value.splice(index, 1)
}

function onAddIndex() {
  indexes.value.push({
    id: null, modelId: detail.model!.id,
    indexName: '', indexType: 'NORMAL',
    fieldIds: [], fieldNames: ''
  })
}

function onRemoveIndex(index: number) {
  const idx = indexes.value[index]
  if (idx.id) { deletedIndexIds.value.push(idx.id) }
  indexes.value.splice(index, 1)
}

async function onSaveAll() {
  saving.value = true
  try {
    // 校验字段
    const hasPrimary = fields.value.some(f => f.isPrimary === 1)
    if (!hasPrimary) {
      ElMessage.warning('模型必须包含至少一个主键字段')
      activeTab.value = 'fields'
      return
    }
    for (const f of fields.value) {
      if (!f.name?.trim()) {
        ElMessage.warning('字段名不能为空')
        activeTab.value = 'fields'
        return
      }
    }
    // 校验索引
    for (const idx of indexes.value) {
      if (!idx.indexName?.trim()) {
        ElMessage.warning('索引名称不能为空')
        activeTab.value = 'indexes'
        return
      }
      if (!idx.fieldIds || idx.fieldIds.length === 0) {
        ElMessage.warning(`索引「${idx.indexName}」至少需要选择一个字段`)
        activeTab.value = 'indexes'
        return
      }
    }

    const saves: Promise<any>[] = []
    saves.push(batchSaveFields(detail.model!.id, {
      fields: fields.value,
      deletedFieldIds: deletedFieldIds.value
    }))
    saves.push(batchSaveIndexes(detail.model!.id, {
      indexes: indexes.value,
      deletedIndexIds: deletedIndexIds.value
    }))
    await Promise.all(saves)
    ElMessage.success('保存成功')
    deletedFieldIds.value = []
    deletedIndexIds.value = []
    await load()
  } finally { saving.value = false }
}

onMounted(load)
</script>

<style scoped>
.info-card { margin-top: 12px; }
.tab-toolbar { margin-bottom: 12px; }
</style>
