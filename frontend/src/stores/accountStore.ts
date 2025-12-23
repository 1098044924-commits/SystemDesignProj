import { defineStore } from 'pinia';
import type { Account } from '@/types/accounting';
import http from '@/utils/http';

interface AccountState {
  accounts: Account[];
  loading: boolean;
}

export const useAccountStore = defineStore('account', {
  state: (): AccountState => ({
    accounts: [],
    loading: false,
  }),
  actions: {
    // 预留：如果后端将来提供 /api/accounts/list 之类接口，可在此实现账户列表查询。
    async fetchAccounts() {
      this.loading = true;
      try {
        // 示例：const { data } = await http.get<Account[]>('/api/accounts');
        // this.accounts = data;
        this.accounts = [];
      } finally {
        this.loading = false;
      }
    },
  },
});

