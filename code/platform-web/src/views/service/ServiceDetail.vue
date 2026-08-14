<template>
  <div class="page" v-loading="loading">
    <el-page-header @back="$router.back()" :content="detail.service?.name || '服务详情'">
      <template #extra>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-page-header>

    <el-card class="info-card" v-if="detail.service?.id">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="服务名称"><el-input v-model="form.name" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="编码"><el-input v-model="form.code" disabled /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="请求方式">
              <el-select v-model="form.httpMethod" style="width:140px">
                <el-option label="GET" value="GET" /><el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" /><el-option label="DELETE" value="DELETE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="服务路径"><el-input v-model="form.servicePath" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分类"><el-input v-model="form.category" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="功能说明"><el-input v-model="form.description" type="textarea" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card class="info-card">
      <template #header>
        <div class="param-header">
          <span>入参定义</span>
          <el-button size="small" type="primary" @click="addInput">新增入参</el-button>
        </div>
      </template>
      <el-table :data="inputs" border stripe>
        <el-table-column label="参数名" width="160">
          <template #default="{ row }"><el-input v-model="row.paramName" size="small" /></template>
        </el-table-column>
        <el-table-column label="数据类型" width="140">
          <template #default="{ row }"><el-input v-model="row.dataType" size="small" /></template>
        </el-table-column>
        <el-table-column label="必填" width="80">
          <template #default="{ row }"><el-switch v-model="row.isRequired" :active-value="1" :inactive-value="0" /></template>
        </el-table-column>
        <el-table-column label="默认值" width="140">
          <template #default="{ row }"><el-input v-model="row.defaultValue" size="small" /></template>
        </el-table-column>
        <el-table-column label="排序" width="90">
          <template #default="{ row }"><el-input-number v-model="row.sortOrder" size="small" :min="0" controls-position="right" style="width:80px" /></template>
        </el-table-column>
        <el-table-column label="说明" min-width="150">
          <template #default="{ row }"><el-input v-model="row.paramComment" size="small" /></template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ $index }"><el-button link type="danger" @click="inputs.splice($index, 1)">删除</el-button></template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="info-card">
      <template #header>
        <div class="param-header">
          <span>出参定义</span>
          <el-button size="small" type="primary" @click="addOutput">新增出参</el-button>
        </div>
      </template>
      <el-table :data="outputs" border stripe>
        <el-table-column label="参数名" width="160">
          <template #default="{ row }"><el-input v-model="row.paramName" size="small" /></template>
        </el-table-column>
        <el-table-column label="数据类型" width="140">
          <template #default="{ row }"><el-input v-model="row.dataType" size="small" /></template>
        </el-table-column>
        <el-table-column label="排序" width="90">
          <template #default="{ row }"><el-input-number v-model="row.sortOrder" size="small" :min="0" controls-position="right" style="width:80px" /></template>
        </el-table-column>
        <el-table-column label="说明" min-width="150">
          <template #default="{ row }"><el-input v-model="row.paramComment" size="small" /></template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ $index }"><el-button link type="danger" @click="outputs.splice($index, 1)">删除</el-button></template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getService, updateService } from '@/api/service'
import type { ServiceDetailVO, ServiceParamVO } from '@/types'

const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const detail = reactive<ServiceDetailVO>({} as ServiceDetailVO)
const form = reactive<any>({ name: '', code: '', httpMethod: 'POST', servicePath: '', category: '', status: 1, description: '' })
const inputs = ref<ServiceParamVO[]>([])
const outputs = ref<ServiceParamVO[]>([])

async function load() {
  loading.value = true
  const id = String(route.params.id)
  try {
    const res = await getService(id)
    Object.assign(detail, res)
    Object.assign(form, res.service)
    inputs.value = (res.inputs || []).map(i => ({ ...i }))
    outputs.value = (res.outputs || []).map(o => ({ ...o }))
  } finally { loading.value = false }
}
function addInput() {
  inputs.value.push({ id: null, serviceId: detail.service!.id, paramType: 1, paramName: '', dataType: 'TEXT', isRequired: 1, defaultValue: null, modelFieldId: null, sortOrder: inputs.value.length, paramComment: null })
}
function addOutput() {
  outputs.value.push({ id: null, serviceId: detail.service!.id, paramType: 2, paramName: '', dataType: 'TEXT', isRequired: 0, defaultValue: null, modelFieldId: null, sortOrder: outputs.value.length, paramComment: null })
}
async function onSave() {
  saving.value = true
  try {
    await updateService(detail.service!.id, {
      ...form, microserviceId: detail.service!.microserviceId,
      inputs: inputs.value, outputs: outputs.value
    })
    ElMessage.success('保存成功')
    await load()
  } finally { saving.value = false }
}
onMounted(load)
</script>

<style scoped>
.info-card { margin-top: 12px; }
.param-header { display: flex; justify-content: space-between; align-items: center; }
</style>
