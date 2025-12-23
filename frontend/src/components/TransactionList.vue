<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>交易列表</span>
        <el-button type="primary" link @click="refresh">刷新</el-button>
      </div>
    </template>

    <el-table
      :data="store.list"
      v-loading="store.loading"
      size="small"
      border
      height="360"
    >
      <el-table-column prop="tradeDate" label="日期" width="170">
        <template #default="{ row }">
          {{ formatDate(row.tradeDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="摘要" min-width="200" />
      <el-table-column prop="reference" label="参考号" width="140" />
      <el-table-column label="借方合计" width="120">
        <template #default="{ row }">
          {{ calcDebit(row).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="贷方合计" width="120">
        <template #default="{ row }">
          {{ calcCredit(row).toFixed(2) }}
        </template>
      </el-table-column>
    </el-table>

    <div class="mt-2">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="store.total"
        layout="total, prev, pager, next"
        @current-change="refresh"
      />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import dayjs from 'dayjs';
import { useTransactionStore } from '@/stores/transactionStore';
import type { Transaction } from '@/types/accounting';

const store = useTransactionStore();

const currentPage = ref(1);
const pageSize = ref(20);

const formatDate = (val: string) => dayjs(val).format('YYYY-MM-DD HH:mm');

const calcDebit = (txn: Transaction) =>
  txn.splits
    .filter((s) => s.direction === 'DEBIT' && s.amount != null)
    .reduce((sum, s) => sum + (s.amount || 0), 0);

const calcCredit = (txn: Transaction) =>
  txn.splits
    .filter((s) => s.direction === 'CREDIT' && s.amount != null)
    .reduce((sum, s) => sum + (s.amount || 0), 0);

const refresh = async () => {
  await store.fetchTransactions({
    page: currentPage.value - 1,
    size: pageSize.value,
  });
};

onMounted(refresh);
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.mt-2 {
  margin-top: 8px;
}
</style>












