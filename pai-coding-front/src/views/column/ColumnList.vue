<template>
  <div class="column-list">
    <ColumnCard v-for="(column, id) in filteredColumns" :key="id" :column="column"></ColumnCard>
  </div>
</template>

<script setup lang="ts">
import type { ColumnVoType } from '@/http/ResponseTypes/ColumnType/ColumnListVoType'
import ColumnCard from '@/components/column/ColumnCard.vue'
import { computed } from 'vue'

const props = defineProps<{
  columns: ColumnVoType[];
}>()

const filteredColumns = computed(() => {
  return props.columns.filter(column => column.count.articleCount !== 0);
});
</script>

<style scoped>
.column-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
</style>
