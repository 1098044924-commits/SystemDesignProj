import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import DashboardPage from '@/views/DashboardPage.vue';
import AccountsPage from '@/views/AccountsPage.vue';
import TransactionsPage from '@/views/TransactionsPage.vue';
import ReportsPage from '@/views/ReportsPage.vue';

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', component: DashboardPage },
  { path: '/accounts', component: AccountsPage },
  { path: '/transactions', component: TransactionsPage },
  { path: '/reports', component: ReportsPage },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;












