<template>
  <div class="page">
    <el-card>
      <el-form :inline="true" @submit.prevent="onSearch">
        <el-form-item label="所属应用">
          <el-select v-model="appId" placeholder="全部" clearable filterable style="width: 180px" @change="onAppChange">
            <el-option v-for="a in apps" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属微服务">
          <el-select v-model="query.microserviceId" placeholder="全部" clearable filterable style="width: 180px">
            <el-option v-for="m in msList" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="名称/编码/路径" clearable @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar"><el-button type="primary" @click="openCreate">新建服务</el-button></div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column label="服务名称" prop="name">
          <template #default="{ row }"><el-link type="primary" @click="$router.push(`/services/${row.id}`)">{{ row.name }}</el-link></template>
        </el-table-column>
        <el-table-column label="编码" prop="code" width="140" />
        <el-table-column label="请求方式" width="100">
          <template #default="{ row }">
            <el-tag :type="methodTag(row.httpMethod)">{{ row.httpMethod }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="路径" prop="servicePath" />
        <el-table-column label="分类" prop="category" width="100" />
        <el-table-column label="所属微服务" prop="microserviceName" width="140" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/services/${row.id}`)">详情</el-button>
            <el-popconfirm title="确认删除？" @confirm="onDelete(row)">
              <template #reference><el-button link type="danger">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pager" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData" @current-change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" title="新建服务" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属应用" prop="formAppId">
          <el-select v-model="formAppId" placeholder="请选择" filterable style="width:100%" @change="onFormAppChange">
            <el-option v-for="a in apps" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属微服务" prop="microserviceId">
          <el-select v-model="form.microserviceId" placeholder="请先选择应用" filterable style="width:100%" :disabled="!formAppId">
            <el-option v-for="m in formMsList" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="服务编码" prop="code"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="请求方式" prop="httpMethod">
          <el-select v-model="form.httpMethod" style="width: 140px">
            <el-option label="GET" value="GET" /><el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" /><el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务路径" prop="servicePath"><el-input v-model="form.servicePath" placeholder="/api/v1/xxx" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.category" /></el-form-item>
        <el-form-item label="功能说明"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { pageServices, createService, deleteService, type SvcPageQuery } from '@/api/service'
import { listSimpleApplications } from '@/api/application'
import { listMicroservicesByApp } from '@/api/microservice'
import type { ApplicationSimpleVO, MicroserviceSimpleVO, ServiceVO } from '@/types'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const tableData = ref<ServiceVO[]>([])
const total = ref(0)
const apps = ref<ApplicationSimpleVO[]>([])
const msList = ref<MicroserviceSimpleVO[]>([])
const appId = ref<string | undefined>(undefined)
const query = reactive<SvcPageQuery>({ pageNum: 1, pageSize: 20, keyword: '',
  microserviceId: route.query.microserviceId ? String(route.query.microserviceId) : undefined })

async function loadData() {
  loading.value = true
  try { const res = await pageServices(query); tableData.value = res.list; total.value = res.total }
  finally { loading.value = false }
}
async function loadApps() { apps.value = await listSimpleApplications() }
async function onAppChange() { query.microserviceId = undefined; msList.value = appId.value ? await listMicroservicesByApp(appId.value) : [] }
function onSearch() { query.pageNum = 1; loadData() }
function onReset() { query.keyword = ''; query.microserviceId = undefined; appId.value = undefined; msList.value = []; query.pageNum = 1; loadData() }
function methodTag(m: string) {
  return ({ GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' } as any)[m] || 'info'
}

const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const formAppId = ref<string | undefined>(undefined)
const formMsList = ref<MicroserviceSimpleVO[]>([])
const form = reactive<any>({ microserviceId: undefined, name: '', code: '', httpMethod: 'POST', servicePath: '', category: '', description: '' })
const rules: FormRules = {
  microserviceId: [{ required: true, message: '请选择微服务', trigger: 'change' }],
  name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入服务编码', trigger: 'blur' },
    { pattern: /^[A-Za-z][A-Za-z0-9_]{0,63}$/, message: '字母开头，字母数字下划线', trigger: 'blur' }],
  httpMethod: [{ required: true, message: '请选择请求方式', trigger: 'change' }],
  servicePath: [{ required: true, message: '请输入服务路径', trigger: 'blur' }]
}
async function onFormAppChange() {
  form.microserviceId = undefined
  formMsList.value = formAppId.value ? await listMicroservicesByApp(formAppId.value) : []
}
function openCreate() {
  Object.assign(form, { microserviceId: undefined, name: '', code: '', httpMethod: 'POST', servicePath: '', category: '', description: '' })
  // 如果搜索栏已选应用，预填到弹窗
  formAppId.value = appId.value
  formMsList.value = [...msList.value]
  if (query.microserviceId) form.microserviceId = query.microserviceId
  dialogVisible.value = true
}
async function onSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const res = await createService({ ...form, inputs: [], outputs: [] })
    ElMessage.success('创建成功')
    dialogVisible.value = false
    router.push(`/services/${res.service.id}`)
  } finally { saving.value = false }
}
async function onDelete(row: ServiceVO) { await deleteService(row.id); ElMessage.success('删除成功'); loadData() }

onMounted(async () => { await loadApps(); loadData() })
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
.pager { margin-top: 12px; justify-content: flex-end; }
</style>
