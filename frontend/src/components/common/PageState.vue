<script setup>
defineProps({ kind: { type: String, default: 'loading' }, title: { type: String, required: true }, actionLabel: { type: String, default: '' } })
defineEmits(['action'])
</script>
<template><div class="page-state" :class="`page-state--${kind}`" role="status"><span v-if="kind === 'loading'" class="page-state__spinner" aria-hidden="true" /><strong>{{ title }}</strong><button v-if="actionLabel" type="button" @click="$emit('action')">{{ actionLabel }}</button></div></template>
<style scoped>
.page-state { display: grid; min-height: 220px; padding: 44px 20px; place-items: center; place-content: center; gap: 14px; color: var(--muted); text-align: center; background: var(--white); border: 1px solid var(--line); }
.page-state strong { font-size: 12px; font-weight: 600; }.page-state--error strong { color: #8c2a20; }.page-state button { min-height: 38px; padding: 9px 16px; color: var(--white); font-weight: 700; background: var(--ink); border: 1px solid var(--ink); cursor: pointer; }
.page-state__spinner { width: 24px; height: 24px; border: 2px solid var(--line); border-top-color: var(--ink); border-radius: 50%; animation: spin .75s linear infinite; }@keyframes spin { to { transform: rotate(360deg); } }@media (prefers-reduced-motion: reduce) { .page-state__spinner { animation: none; } }
</style>
