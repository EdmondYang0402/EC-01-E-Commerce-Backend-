<script setup>
import { computed, ref, watch } from 'vue'
const props = defineProps({ src: { type: String, default: '' }, fallback: { type: String, required: true }, alt: { type: String, default: '' }, eager: { type: Boolean, default: false } })
const failed = ref(false)
watch(() => props.src, () => { failed.value = false })
const resolvedSrc = computed(() => !failed.value && props.src ? props.src : props.fallback)
</script>
<template><img :src="resolvedSrc" :alt="alt" :loading="eager ? 'eager' : 'lazy'" @error="failed = true" /></template>
