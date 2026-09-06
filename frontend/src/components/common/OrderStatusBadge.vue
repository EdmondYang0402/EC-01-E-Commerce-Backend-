<script setup>
import { computed } from 'vue'
import { useLocaleStore } from '../../stores/locale'
import { normalizeOrderStatus, orderStatusKey } from '../../utils/orderStatus'
const props = defineProps({ status: { type: [String, Number], required: true } })
const locale = useLocaleStore()
const code = computed(() => normalizeOrderStatus(props.status))
const label = computed(() => locale.t(orderStatusKey(props.status)))
</script>
<template><span class="order-status" :class="`order-status--${code}`">{{ label }}</span></template>
<style scoped>
.order-status { display: inline-flex; width: max-content; min-height: 24px; align-items: center; padding: 4px 9px; color: #655e59; font-size: 10px; font-weight: 750; background: var(--paper); border: 1px solid var(--line); border-radius: 99px; }.order-status--0 { color: #8b6413; background: #fbf4df; border-color: #ead9a7; }.order-status--1,.order-status--2 { color: #174f79; background: #edf4fa; border-color: #cbddea; }.order-status--3 { color: #2f6548; background: #edf5ef; border-color: #cfe2d4; }
</style>
