<script setup>
import { computed } from 'vue'
import { useLocaleStore } from '../../stores/locale'
import { normalizeOrderStatus } from '../../utils/orderStatus'
const props = defineProps({ status: { type: [String, Number], required: true } })
const locale = useLocaleStore()
const code = computed(() => normalizeOrderStatus(props.status))
const steps = computed(() => code.value === 4 ? [{ key: 'created', active: true }, { key: 'cancelled', active: true }] : ['created', 'paid', 'shipped', 'completed'].map((key, index) => ({ key, active: code.value >= index })))
</script>
<template><ol class="order-timeline" :class="{ 'is-cancelled': code === 4 }" :aria-label="locale.t('orderTimeline.label')"><li v-for="step in steps" :key="step.key" :class="{ active: step.active }"><i aria-hidden="true" /><span>{{ locale.t(`orderTimeline.${step.key}`) }}</span></li></ol></template>
<style scoped>
.order-timeline { display: grid; grid-template-columns: repeat(4,1fr); margin: 28px 0; padding: 0; list-style: none; }.order-timeline.is-cancelled { grid-template-columns: repeat(2,1fr); }.order-timeline li { position: relative; display: grid; gap: 9px; color: var(--muted); font-size: 10px; }.order-timeline li::after { position: absolute; top: 5px; right: 10px; left: 10px; height: 1px; background: var(--line); content: ''; }.order-timeline li:last-child::after { display: none; }.order-timeline i { z-index: 1; width: 11px; height: 11px; background: var(--white); border: 2px solid var(--line); border-radius: 50%; }.order-timeline li.active { color: var(--ink); font-weight: 700; }.order-timeline li.active i,.order-timeline li.active::after { background: var(--ink); border-color: var(--ink); }
@media (max-width:520px) { .order-timeline,.order-timeline.is-cancelled { gap: 14px; grid-template-columns: 1fr; }.order-timeline li { grid-template-columns: 14px 1fr; align-items:center; }.order-timeline li::after { top:10px; bottom:-15px; left:5px; width:1px; height:auto; } }
</style>
