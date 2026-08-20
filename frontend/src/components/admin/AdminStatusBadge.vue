<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: [String, Number], default: '' },
  labels: { type: Object, default: () => ({}) },
})

const normalized = computed(() => String(props.status ?? ''))
const text = computed(() => props.labels[normalized.value] || normalized.value || '—')
const tone = computed(() => {
  if (['ON_SHELF', 'ENABLED', 'NORMAL', 'PAID', 'SHIPPED', 'COMPLETED', '1', '2', '3'].includes(normalized.value)) return 'positive'
  if (['PENDING_PAYMENT', '0'].includes(normalized.value)) return 'pending'
  return 'muted'
})
</script>

<template>
  <span class="admin-status" :class="`admin-status--${tone}`">{{ text }}</span>
</template>

<style scoped>
.admin-status { display: inline-flex; align-items: center; min-height: 24px; padding: 4px 9px; font-size: 9px; font-weight: 750; letter-spacing: .07em; border: 1px solid transparent; border-radius: 99px; white-space: nowrap; }
.admin-status--positive { color: #2f6548; background: #edf5ef; border-color: #cfe2d4; }
.admin-status--pending { color: #8b6413; background: #fbf4df; border-color: #ead9a7; }
.admin-status--muted { color: #655e59; background: var(--paper); border-color: var(--line); }
</style>
