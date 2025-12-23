<template>
  <el-row :gutter="16">
    <el-col :span="8">
      <el-card shadow="never">
        <template #header>资产负债表</template>
        <el-button type="primary" size="small" @click="loadBalanceSheet">刷新</el-button>
        <el-table
          v-if="balanceSheet"
          :data="[
            ...balanceSheet.assets,
            ...balanceSheet.liabilities,
            ...balanceSheet.equity,
          ]"
          size="small"
          border
          class="mt-2"
        >
          <el-table-column prop="accountCode" label="代码" width="100" />
          <el-table-column prop="accountName" label="名称" />
          <el-table-column prop="amount" label="金额" width="120" />
        </el-table>
      </el-card>
    </el-col>

    <el-col :span="8">
      <el-card shadow="never">
        <template #header>损益表</template>
        <el-button type="primary" size="small" @click="loadIncome">刷新</el-button>
        <el-table
          v-if="incomeStatement"
          :data="[...incomeStatement.incomes, ...incomeStatement.expenses]"
          size="small"
          border
          class="mt-2"
        >
          <el-table-column prop="accountCode" label="代码" width="100" />
          <el-table-column prop="accountName" label="名称" />
          <el-table-column prop="amount" label="金额" width="120" />
        </el-table>
      </el-card>
    </el-col>

    <el-col :span="8">
      <el-card shadow="never">
        <template #header>试算平衡表</template>
        <el-button type="primary" size="small" @click="loadTrial">刷新</el-button>
        <el-table
          v-if="trialBalance"
          :data="trialBalance.rows"
          size="small"
          border
          class="mt-2"
        >
          <el-table-column prop="accountCode" label="代码" width="100" />
          <el-table-column prop="accountName" label="名称" />
          <el-table-column prop="debit" label="借方" width="100" />
          <el-table-column prop="credit" label="贷方" width="100" />
        </el-table>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import http from '@/utils/http';
import type {
  BalanceSheetResponse,
  IncomeStatementResponse,
  TrialBalanceResponse,
} from '@/types/reports';

const balanceSheet = ref<BalanceSheetResponse | null>(null);
const incomeStatement = ref<IncomeStatementResponse | null>(null);
const trialBalance = ref<TrialBalanceResponse | null>(null);

const loadBalanceSheet = async () => {
  const { data } = await http.get<BalanceSheetResponse>('/api/reports/balance-sheet');
  balanceSheet.value = data;
};

const loadIncome = async () => {
  const { data } = await http.get<IncomeStatementResponse>('/api/reports/income-statement');
  incomeStatement.value = data;
};

const loadTrial = async () => {
  const { data } = await http.get<TrialBalanceResponse>('/api/reports/trial-balance');
  trialBalance.value = data;
};
</script>

<style scoped>
.mt-2 {
  margin-top: 8px;
}
</style>












