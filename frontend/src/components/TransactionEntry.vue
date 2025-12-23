<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>录入交易</span>
        <el-tag :type="isBalanced ? 'success' : 'danger'">
          借：{{ debitTotal.toFixed(2) }} / 贷：{{ creditTotal.toFixed(2) }}
        </el-tag>
      </div>
    </template>

    <el-form :model="form" label-width="80px">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="日期">
            <el-date-picker
              v-model="form.tradeDate"
              type="datetime"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="参考号">
            <el-input v-model="form.reference" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="已核对">
            <el-switch v-model="form.cleared" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="摘要">
        <el-input v-model="form.description" />
      </el-form-item>
    </el-form>

    <el-table :data="form.splits" size="small" border class="mt-2">
      <el-table-column label="借/贷" width="80">
        <template #default="{ row }">
          <el-select v-model="row.direction" style="width: 70px">
            <el-option label="借" value="DEBIT" />
            <el-option label="贷" value="CREDIT" />
          </el-select>
        </template>
      </el-table-column>

      <el-table-column label="账户" min-width="200">
        <template #default="{ row }">
          <el-select
            v-model="row.accountId"
            filterable
            placeholder="选择账户"
            style="width: 100%"
          >
            <el-option
              v-for="acc in accountStore.accounts"
              :key="acc.id"
              :label="acc.code + ' ' + acc.name"
              :value="acc.id"
            />
          </el-select>
        </template>
      </el-table-column>

      <el-table-column label="金额" width="140">
        <template #default="{ row }">
          <el-input-number
            v-model="row.amount"
            :step="0.01"
            :min="0"
            controls-position="right"
          />
        </template>
      </el-table-column>

      <el-table-column label="备注" min-width="160">
        <template #default="{ row }">
          <el-input v-model="row.memo" />
        </template>
      </el-table-column>

      <el-table-column label="操作" width="80">
        <template #default="{ $index }">
          <el-button type="danger" link @click="removeSplit($index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="mt-2">
      <el-button @click="addSplit">新增分录</el-button>
      <el-button
        type="primary"
        :disabled="!isBalanced || form.splits.length < 2"
        @click="onSubmit"
      >
        保存
      </el-button>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed, reactive } from 'vue';
import dayjs from 'dayjs';
import { ElMessage } from 'element-plus';
import type { CreateTransactionRequest, Split } from '@/types/accounting';
import { useAccountStore } from '@/stores/accountStore';
import { useTransactionStore } from '@/stores/transactionStore';

const accountStore = useAccountStore();
const transactionStore = useTransactionStore();

if (!accountStore.accounts.length) {
  accountStore.fetchAccountTree();
}

const createEmptySplit = (): Split => ({
  accountId: null,
  amount: null,
  direction: 'DEBIT',
  memo: '',
});

const form = reactive<CreateTransactionRequest>({
  tradeDate: dayjs().toISOString(),
  description: '',
  reference: '',
  cleared: false,
  splits: [createEmptySplit(), createEmptySplit()],
});

const debitTotal = computed(() =>
  form.splits
    .filter((s) => s.direction === 'DEBIT' && s.amount != null)
    .reduce((sum, s) => sum + (s.amount || 0), 0),
);

const creditTotal = computed(() =>
  form.splits
    .filter((s) => s.direction === 'CREDIT' && s.amount != null)
    .reduce((sum, s) => sum + (s.amount || 0), 0),
);

const isBalanced = computed(
  () => debitTotal.value === creditTotal.value && debitTotal.value > 0,
);

const addSplit = () => {
  form.splits.push(createEmptySplit());
};

const removeSplit = (index: number) => {
  form.splits.splice(index, 1);
};

const onSubmit = async () => {
  if (!isBalanced.value) {
    ElMessage.error('借贷未平衡');
    return;
  }
  if (form.splits.some((s) => !s.accountId || !s.amount)) {
    ElMessage.error('请填写完整账户和金额');
    return;
  }

  await transactionStore.createTransaction({
    ...form,
    tradeDate: dayjs(form.tradeDate).toISOString(),
  });
  ElMessage.success('交易已保存');
  form.description = '';
  form.reference = '';
  form.splits = [createEmptySplit(), createEmptySplit()];
};
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












