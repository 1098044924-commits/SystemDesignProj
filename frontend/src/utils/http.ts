import axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosError,
  type AxiosResponse,
} from 'axios';
import { ElMessage } from 'element-plus';

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';

const http: AxiosInstance = axios.create({
  baseURL,
  timeout: 10000,
});

http.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    const token = localStorage.getItem('access_token');
    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error),
);

http.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: AxiosError<any>) => {
    const { response } = error;
    if (!response) {
      ElMessage.error('网络错误');
      return Promise.reject(error);
    }
    const { status, data } = response;
    if ((data as any)?.message) {
      ElMessage.error((data as any).message);
    } else if (status === 401) {
      ElMessage.error('未认证，请登录');
    } else if (status === 403) {
      ElMessage.error('无权限');
    } else if (status >= 500) {
      ElMessage.error('服务器错误');
    } else {
      ElMessage.error('请求失败');
    }
    return Promise.reject(error);
  },
);

export default http;












