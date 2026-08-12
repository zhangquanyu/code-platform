import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    redirect: '/applications',
    children: [
      {
        path: 'applications',
        name: 'ApplicationList',
        component: () => import('@/views/application/ApplicationList.vue'),
        meta: { title: '应用管理' }
      },
      {
        path: 'applications/:id',
        name: 'ApplicationDetail',
        component: () => import('@/views/application/ApplicationDetail.vue'),
        meta: { title: '应用详情' }
      },
      {
        path: 'microservices',
        name: 'MicroserviceList',
        component: () => import('@/views/microservice/MicroserviceList.vue'),
        meta: { title: '微服务管理' }
      },
      {
        path: 'microservices/:id',
        name: 'MicroserviceDetail',
        component: () => import('@/views/microservice/MicroserviceDetail.vue'),
        meta: { title: '微服务详情' }
      },
      {
        path: 'models',
        name: 'ModelList',
        component: () => import('@/views/model/ModelList.vue'),
        meta: { title: '模型管理' }
      },
      {
        path: 'models/:id',
        name: 'ModelDetail',
        component: () => import('@/views/model/ModelDetail.vue'),
        meta: { title: '模型详情' }
      },
      {
        path: 'metadata',
        name: 'MetadataList',
        component: () => import('@/views/metadata/MetadataList.vue'),
        meta: { title: '元数据管理' }
      },
      {
        path: 'metadata/:id',
        name: 'MetadataDetail',
        component: () => import('@/views/metadata/MetadataDetail.vue'),
        meta: { title: '元数据详情' }
      },
      {
        path: 'services',
        name: 'ServiceList',
        component: () => import('@/views/service/ServiceList.vue'),
        meta: { title: '服务管理' }
      },
      {
        path: 'services/:id',
        name: 'ServiceDetail',
        component: () => import('@/views/service/ServiceDetail.vue'),
        meta: { title: '服务详情' }
      },
      {
        path: 'orchestrations',
        name: 'OrchestrationList',
        component: () => import('@/views/orchestration/OrchestrationList.vue'),
        meta: { title: '服务编排' }
      },
      {
        path: 'orchestrations/:id',
        name: 'OrchestrationDetail',
        component: () => import('@/views/orchestration/OrchestrationDetail.vue'),
        meta: { title: '编排详情' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
