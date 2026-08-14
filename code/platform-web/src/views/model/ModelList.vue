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
          <el-input v-model="query.keyword" placeholder="名称/编码" clearable @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar">
        <el-button type="primary" @click="openCreate">新建模型</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column label="模型名称" prop="name">
          <template #default="{ row }">
            <el-link type="primary" @click="$router.push(`/models/${row.id}`)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="编码" prop="code" width="160" />
        <el-table-column label="所属微服务" prop="microserviceName" width="140" />
        <el-table-column label="字段数" prop="fieldCount" width="90" />
        <el-table-column label="描述" prop="description" show-overflow-tooltip />
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="$router.push(`/models/${row.id}`)">详情</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑模型' : '新建模型'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属应用" prop="formAppId" v-if="!isEdit">
          <el-select v-model="formAppId" placeholder="请选择" filterable style="width: 100%" @change="onFormAppChange">
            <el-option v-for="a in apps" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属应用" v-else>
          <el-input :model-value="formAppName" disabled />
        </el-form-item>
        <el-form-item label="所属微服务" prop="microserviceId" v-if="!isEdit">
          <el-select v-model="form.microserviceId" placeholder="请先选择应用" filterable style="width: 100%" :disabled="!formAppId">
            <el-option v-for="m in formMsList" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属微服务" v-else>
          <el-input :model-value="formMsName" disabled />
        </el-form-item>
        <el-form-item label="模型名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="模型编码" prop="code">
          <el-input v-model="form.code" placeholder="字母开头，字母数字下划线" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
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
import { pageModels, createModel, updateModel, deleteModel, type ModelPageQuery } from '@/api/model'
import { listSimpleApplications } from '@/api/application'
import { listMicroservicesByApp } from '@/api/microservice'
import type { ApplicationSimpleVO, MicroserviceSimpleVO, ModelVO } from '@/types'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const tableData = ref<ModelVO[]>([])
const total = ref(0)
const apps = ref<ApplicationSimpleVO[]>([])
const msList = ref<MicroserviceSimpleVO[]>([])
const appId = ref<string | undefined>(undefined)
const query = reactive<ModelPageQuery>({
  pageNum: 1, pageSize: 20, keyword: '',
  microserviceId: route.query.microserviceId ? String(route.query.microserviceId) : undefined
})

async function loadData() {
  loading.value = true
  try {
    const res = await pageModels(query)
    tableData.value = res.list; total.value = res.total
  } finally { loading.value = false }
}
async function loadApps() { apps.value = await listSimpleApplications() }
async function onAppChange() {
  query.microserviceId = undefined
  msList.value = appId.value ? await listMicroservicesByApp(appId.value) : []
}
function onSearch() { query.pageNum = 1; loadData() }
function onReset() { query.keyword = ''; query.microserviceId = undefined; appId.value = undefined; msList.value = []; query.pageNum = 1; loadData() }

const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const editingId = ref<string | undefined>(undefined)
const formAppName = ref('')
const formMsName = ref('')
const formRef = ref<FormInstance>()
const formAppId = ref<string | undefined>(undefined)
const formMsList = ref<MicroserviceSimpleVO[]>([])
const form = reactive<any>({ microserviceId: undefined, name: '', code: '', description: '' })
const rules: FormRules = {
  microserviceId: [{ required: true, message: '请选择微服务', trigger: 'change' }],
  name: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入模型编码', trigger: 'blur' },
    { pattern: /^[A-Za-z][A-Za-z0-9_]{0,63}$/, message: '字母开头，字母数字下划线', trigger: 'blur' }]
}
async function onFormAppChange() {
  form.microserviceId = undefined
  formMsList.value = formAppId.value ? await listMicroservicesByApp(formAppId.value) : []
}
function openCreate() {
  isEdit.value = false
  editingId.value = undefined
  Object.assign(form, { microserviceId: undefined, name: '', code: '', description: '' })
  // 如果搜索栏已选应用，预填到弹窗
  formAppId.value = appId.value
  formMsList.value = [...msList.value]
  if (query.microserviceId) form.microserviceId = query.microserviceId
  dialogVisible.value = true
}
function openEdit(row: ModelVO) {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    microserviceId: row.microserviceId,
    name: row.name,
    code: row.code,
    description: row.description || ''
  })
  const app = apps.value.find(a => a.id === row.applicationId)
  formAppName.value = app?.name || ''
  formMsName.value = row.microserviceName || ''
  dialogVisible.value = true
}
async function onSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (isEdit.value && editingId.value) {
      await updateModel(editingId.value, { name: form.name, description: form.description })
      ElMessage.success('修改成功')
      dialogVisible.value = false
      loadData()
    } else {
      const res = await createModel(form)
      ElMessage.success('创建成功')
      dialogVisible.value = false
      router.push(`/models/${res.id}`)
    }
  } finally { saving.value = false }
}
async function onDelete(row: ModelVO) {
  await deleteModel(row.id); ElMessage.success('删除成功'); loadData()
}
onMounted(async () => {
  await loadApps()
  if (query.microserviceId) {
    // 若带微服务参数进入，尝试回填应用
    const ms = msList.value.find(m => m.id === query.microserviceId)
    if (ms) appId.value = ms.applicationId
  }
  loadData()
})
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
.pager { margin-top: 12px; justify-content: flex-end; }
</style>
