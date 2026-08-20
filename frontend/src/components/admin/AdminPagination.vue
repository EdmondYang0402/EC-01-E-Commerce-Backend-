<script setup>
import { computed } from 'vue'

const props = defineProps({
  page: { type: Number, required: true },
  size: { type: Number, required: true },
  total: { type: Number, default: 0 },
})

const emit = defineEmits(['change'])
const pages = computed(() => Math.max(1, Math.ceil(props.total / props.size)))
</script>

<template>
  <nav v-if="total > 0" class="admin-pagination" aria-label="后台列表分页">
    <span>共 {{ total }} 条</span>
    <div>
      <button type="button" :disabled="page <= 1" @click="emit('change', page - 1)">上一页</button>
      <strong>{{ page }} / {{ pages }}</strong>
      <button type="button" :disabled="page >= pages" @click="emit('change', page + 1)">下一页</button>
    </div>
  </nav>
</template>

<style scoped>
.admin-pagination { display: flex; align-items: center; justify-content: space-between; gap: 18px; padding-top: 20px; color: var(--muted); font-size: 11px; }
.admin-pagination div { display: flex; align-items: center; gap: 12px; }
.admin-pagination strong { min-width: 52px; color: var(--ink); font-size: 11px; text-align: center; }
.admin-pagination button { padding: 8px 13px; color: var(--ink); background: var(--white); border: 1px solid var(--line); border-radius: 2px; cursor: pointer; }
.admin-pagination button:disabled { cursor: not-allowed; opacity: .4; }
@media (max-width: 520px) { .admin-pagination { align-items: flex-start; flex-direction: column; } }
</style>
