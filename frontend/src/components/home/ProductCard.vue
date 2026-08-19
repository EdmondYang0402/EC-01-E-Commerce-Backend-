<script setup>
import { computed } from 'vue'
import fallbackImage from '../../assets/products/chair.png'
import { useLocaleStore } from '../../stores/locale'
import UiIcon from '../common/UiIcon.vue'

const props = defineProps({
  product: { type: Object, required: true },
  favorite: { type: Boolean, default: false },
})

defineEmits(['favorite'])

const imageUrl = computed(() => props.product.coverUrl || props.product.image || fallbackImage)
const price = computed(() => Number(props.product.minPrice ?? props.product.price ?? 0))
const formatPrice = (value) => `$${Number(value).toFixed(2)}`
const locale = useLocaleStore()
</script>

<template>
  <article class="product-card">
    <div class="product-card__image-wrap">
      <RouterLink :to="`/products/${product.id}`" :aria-label="locale.t('product.view', { name: product.name })">
        <img :src="imageUrl" :alt="product.name" loading="lazy" />
      </RouterLink>
      <button class="favorite" :class="{ 'is-active': favorite }" type="button" :aria-label="locale.t('product.favorite', { name: product.name })" @click="$emit('favorite', product.id)">
        <UiIcon name="heart" :size="17" />
      </button>
    </div>
    <div class="product-card__body">
      <h3><RouterLink :to="`/products/${product.id}`">{{ product.name }}</RouterLink></h3>
      <p>{{ product.subtitle }}</p>
      <div class="product-card__purchase">
        <strong>{{ formatPrice(price) }}</strong>
        <RouterLink class="add-button" :to="`/products/${product.id}`" :aria-label="locale.t('product.view', { name: product.name })">
          <UiIcon name="plus" :size="17" />
        </RouterLink>
      </div>
    </div>
  </article>
</template>

<style scoped>
.product-card { min-width: 0; overflow: hidden; background: var(--white); border: 1px solid var(--line); }
.product-card__image-wrap { position: relative; aspect-ratio: 1.08 / 1; overflow: hidden; background: var(--paper); }
.product-card__image-wrap img { display: block; width: 100%; height: 100%; object-fit: cover; transition: transform 240ms ease; }
.product-card:hover img { transform: scale(1.025); }
.favorite { position: absolute; top: 10px; right: 10px; display: grid; width: 29px; height: 29px; padding: 0; place-items: center; color: var(--ink); background: rgba(255,255,255,.9); border: 1px solid var(--line); border-radius: 50%; cursor: pointer; }
.favorite.is-active { color: white; background: var(--red); border-color: var(--red); }
.favorite.is-active :deep(svg) { fill: currentColor; }
.product-card__body { padding: 13px 14px 12px; border-top: 1px solid var(--line); }
.product-card h3 { overflow: hidden; margin: 0 0 5px; font-size: 12px; font-weight: 680; line-height: 1.2; text-overflow: ellipsis; white-space: nowrap; }
.product-card h3 a { color: inherit; text-decoration: none; }
.product-card p { overflow: hidden; margin: 0; color: var(--muted); font-size: 10px; line-height: 1.3; text-overflow: ellipsis; white-space: nowrap; }
.product-card__purchase { display: flex; align-items: center; justify-content: space-between; margin-top: 12px; }
.product-card__purchase strong { font-size: 13px; font-weight: 720; }
.add-button { display: grid; width: 30px; height: 30px; place-items: center; color: white; background: var(--ink); border-radius: 50%; }
.add-button:hover { color: white; background: var(--red); }
</style>
