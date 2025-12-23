<template>
  <el-row :gutter="16">
    <el-col :span="12">
      <TransactionEntry />
    </el-col>
    <el-col :span="12">
      <TransactionList />
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import TransactionEntry from '@/components/TransactionEntry.vue';
import TransactionList from '@/components/TransactionList.vue';
</script>












