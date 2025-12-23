import { defineStore } from 'pinia';
import http from '@/utils/http';
import type { Transaction, CreateTransactionRequest, PaginatedResult } from '@/types/accounting';

interface TransactionState {
  list: Transaction[];
  total: number;
  page: number;
  size: number;
  loading: boolean;
}

export const useTransactionStore = defineStore('transaction', {
  state: (): TransactionState => ({
    list: [],
    total: 0,
    page: 0,
    size: 20,
    loading: false,
  }),
  actions: {
    async createTransaction(payload: CreateTransactionRequest) {
      const { data } = await http.post<Transaction>('/api/transactions', payload);
      await this.fetchTransactions({ page: 0, size: this.size });
      return data;
    },
    async fetchTransactions(params?: { page?: number; size?: number }) {
      this.loading = true;
      try {
        const { data } = await http.get<PaginatedResult<Transaction>>('/api/transactions', {
          params: {
            page: params?.page ?? this.page,
            size: params?.size ?? this.size,
          },
        });
        this.list = data.content;
        this.total = data.totalElements;
        this.page = data.number;
        this.size = data.size;
      } finally {
        this.loading = false;
      }
    },
  },
});












