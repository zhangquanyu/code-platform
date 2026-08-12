<template>
  <div class="page">
    <el-card>
      <el-form :inline="true" @submit.prevent="onSearch">
        <el-form-item label="所属应用">
          <el-select v-model="query.applicationId" placeholder="全部" clearable filterable style="width: 200px">
            <el-option v-for="a in apps" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="名称/编码" clearable @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar">
        <el-button type="primary" @click="openCreate">新建微服务</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column label="微服务名称" prop="name">
          <template #default="{ row }">
            <el-link type="primary" @click="goDetail(row.id)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="编码" prop="code" width="160" />
        <el-table-column label="所属应用" prop="applicationName" width="140" />
        <el-table-column label="版本" prop="version" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row.id)">详情</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-popconfirm title="确认删除？" @confirm="onDelete(row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑微服务' : '新建微服务'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属应用" prop="applicationId">
          <el-select v-model="form.applicationId" placeholder="请选择应用" filterable :disabled="isEdit" style="width: 100%">
            <el-option v-for="a in apps" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="微服务名称" prop="name">
          <el-input v-model="form.name" maxlength="128" />
        </el-form-item>
        <el-form-item label="微服务编码" prop="code">
          <el-input v-model="form.code" maxlength="64" :disabled="isEdit" placeholder="字母开头，字母数字下划线" />
        </el-form-item>
        <el-form-item label="版本" prop="version">
          <el-input v-model="form.version" maxlength="32" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" maxlength="512" />
        </el-form-item>
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
import {
  pageMicroservices, createMicroservice, updateMicroservice,
  deleteMicroservice, updateMicroserviceStatus, type MsPageQuery
} from '@/api/microservice'
import { listSimpleApplications } from '@/api/application'
import type { ApplicationSimpleVO, MicroserviceVO } from '@/types'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const tableData = ref<MicroserviceVO[]>([])
const total = ref(0)
const apps = ref<ApplicationSimpleVO[]>([])
const query = reactive<MsPageQuery>({
  pageNum: 1, pageSize: 20, keyword: '', status: undefined,
  applicationId: route.query.applicationId ? Number(route.query.applicationId) : undefined
})

async function loadData() {
  loading.value = true
  try {
    const res = await pageMicroservices(query)
    tableData.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function loadApps() { apps.value = await listSimpleApplications() }

function onSearch() { query.pageNum = 1; loadData() }
function onReset() {
  query.keyword = ''; query.status = undefined; query.applicationId = undefined; query.pageNum = 1; loadData()
}
function goDetail(id: number) { router.push(`/microservices/${id}`) }

const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<any>({ applicationId: undefined, name: '', code: '', version: '1.0.0', description: '' })
const rules: FormRules = {
  applicationId: [{ required: true, message: '请选择应用', trigger: 'change' }],
  name: [{ required: true, message: '请输入微服务名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入微服务编码', trigger: 'blur' },
    { pattern: /^[A-Za-z][A-Za-z0-9_]{0,63}$/, message: '字母开头，字母数字下划线，最长64', trigger: 'blur' }
  ]
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { id: undefined, applicationId: query.applicationId, name: '', code: '', version: '1.0.0', description: '' })
  dialogVisible.value = true
}
function openEdit(row: MicroserviceVO) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

async function onSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (isEdit.value && form.id) {
      await updateMicroservice(form.id, { name: form.name, version: form.version, description: form.description })
      ElMessage.success('更新成功')
    } else {
      await createMicroservice({ applicationId: form.applicationId, name: form.name, code: form.code, version: form.version, description: form.description })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: MicroserviceVO) {
  await updateMicroserviceStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态已更新')
  loadData()
}
async function onDelete(row: MicroserviceVO) {
  await deleteMicroservice(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => { loadApps(); loadData() })
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
.pager { margin-top: 12px; justify-content: flex-end; }
</style>
