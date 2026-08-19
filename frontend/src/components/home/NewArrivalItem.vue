<script setup>
import { computed } from 'vue'
import fallbackImage from '../../assets/products/chair.png'

const props = defineProps({ product: { type: Object, required: true } })
const imageUrl = computed(() => props.product.coverUrl || props.product.image || fallbackImage)
const price = computed(() => Number(props.product.minPrice ?? props.product.price ?? 0))
const formatPrice = (value) => `$${Number(value).toFixed(2)}`
</script>

<template>
  <RouterLink class="arrival-item" :to="`/products/${product.id}`">
    <div class="arrival-item__image"><img :src="imageUrl" :alt="product.name" loading="lazy" /></div>
    <div class="arrival-item__copy">
      <h3>{{ product.name }}</h3>
      <strong>{{ formatPrice(price) }}</strong>
    </div>
  </RouterLink>
</template>

<style scoped>
.arrival-item { display: flex; min-width: 0; align-items: center; gap: 13px; padding: 0 18px; color: inherit; border-right: 1px solid var(--line); text-decoration: none; }
.arrival-item:last-child { border-right: 0; }
.arrival-item__image { width: 70px; height: 58px; flex: 0 0 70px; overflow: hidden; background: var(--paper); }
.arrival-item__image img { width: 100%; height: 100%; object-fit: cover; }
.arrival-item__copy { min-width: 0; }
.arrival-item h3 { overflow: hidden; margin: 0 0 6px; font-size: 11px; font-weight: 650; text-overflow: ellipsis; white-space: nowrap; }
.arrival-item strong { font-size: 10px; font-weight: 720; }
</style>
