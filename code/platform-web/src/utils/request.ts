import axios, { type AxiosInstance } from 'axios'
import { ElMessage } from 'element-plus'

export interface Result<T> {
  code: number
  message: string
  data: T
  traceId?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

const service: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 30000
})

service.interceptors.response.use(
  (response) => {
    const res = response.data as Result<unknown>
    if (res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res as unknown as Promise<Result<unknown>> as any
  },
  (error) => {
    const msg = error?.response?.data?.message || error.message || '网络异常'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default service
