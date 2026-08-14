<template>
  <div class="page">
    <el-card>
      <el-form :inline="true" @submit.prevent="onSearch">
        <el-form-item label="所属应用">
          <el-select v-model="query.applicationId" placeholder="全部" clearable filterable style="width: 180px">
            <el-option v-for="a in apps" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="名称/编码" clearable @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" /><el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar"><el-button type="primary" @click="openCreate">新建元数据</el-button></div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column label="名称" prop="name">
          <template #default="{ row }">
            <el-link type="primary" @click="$router.push(`/metadata/${row.id}`)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="编码" prop="code" width="160" />
        <el-table-column label="所属应用" prop="applicationName" width="140" />
        <el-table-column label="项数" prop="itemCount" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/metadata/${row.id}`)">详情</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑元数据' : '新建元数据'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属应用" prop="applicationId">
          <el-select v-model="form.applicationId" placeholder="请选择" filterable :disabled="isEdit" style="width:100%">
            <el-option v-for="a in apps" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="编码" prop="code"><el-input v-model="form.code" :disabled="isEdit" /></el-form-item>
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
import { useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { pageMetadata, createMetadata, updateMetadata, deleteMetadata, updateMetadataStatus, type MetaPageQuery } from '@/api/metadata'
import { listSimpleApplications } from '@/api/application'
import type { ApplicationSimpleVO, MetadataVO } from '@/types'

const route = useRoute()
const loading = ref(false)
const tableData = ref<MetadataVO[]>([])
const total = ref(0)
const apps = ref<ApplicationSimpleVO[]>([])
const query = reactive<MetaPageQuery>({ pageNum: 1, pageSize: 20, keyword: '', status: undefined,
  applicationId: route.query.applicationId ? String(route.query.applicationId) : undefined })

async function loadData() {
  loading.value = true
  try { const res = await pageMetadata(query); tableData.value = res.list; total.value = res.total }
  finally { loading.value = false }
}
async function loadApps() { apps.value = await listSimpleApplications() }
function onSearch() { query.pageNum = 1; loadData() }
function onReset() { query.keyword = ''; query.status = undefined; query.applicationId = undefined; query.pageNum = 1; loadData() }

const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<any>({ applicationId: undefined, name: '', code: '', description: '' })
const rules: FormRules = {
  applicationId: [{ required: true, message: '请选择应用', trigger: 'change' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入编码', trigger: 'blur' },
    { pattern: /^[A-Za-z][A-Za-z0-9_]{0,63}$/, message: '字母开头，字母数字下划线', trigger: 'blur' }]
}
function openCreate() {
  isEdit.value = false
  Object.assign(form, { id: undefined, applicationId: query.applicationId, name: '', code: '', description: '' })
  dialogVisible.value = true
}
function openEdit(row: MetadataVO) { isEdit.value = true; Object.assign(form, row); dialogVisible.value = true }
async function onSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (isEdit.value && form.id) {
      await updateMetadata(form.id, { name: form.name, description: form.description })
      ElMessage.success('更新成功')
    } else {
      await createMetadata({ applicationId: form.applicationId, name: form.name, code: form.code, description: form.description })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false; loadData()
  } finally { saving.value = false }
}
async function toggleStatus(row: MetadataVO) { await updateMetadataStatus(row.id, row.status === 1 ? 0 : 1); ElMessage.success('状态已更新'); loadData() }
async function onDelete(row: MetadataVO) { await deleteMetadata(row.id); ElMessage.success('删除成功'); loadData() }

onMounted(() => { loadApps(); loadData() })
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
.pager { margin-top: 12px; justify-content: flex-end; }
</style>
