<script setup>
import UiIcon from '../common/UiIcon.vue'
import { useLocaleStore } from '../../stores/locale'

defineProps({ category: { type: Object, required: true } })
const locale = useLocaleStore()
</script>

<template>
  <a class="category-card" href="#featured">
    <span class="category-card__icon" :class="`is-${category.accent}`">
      <svg v-if="category.shape === 'chair'" viewBox="0 0 32 32" aria-hidden="true">
        <path d="M9 5v11h14V5M8 16h16v5H8zM10 21l-2 7m14-7 2 7" />
      </svg>
      <svg v-else-if="category.shape === 'lamp'" viewBox="0 0 32 32" aria-hidden="true">
        <path d="M16 5v3m-8 11h16l-3-9H11zM16 19v8m-5 0h10" />
      </svg>
      <svg v-else-if="category.shape === 'vase'" viewBox="0 0 32 32" aria-hidden="true">
        <path d="M12 5h8m-6 0v5c-1 3-5 5-5 10 0 5 3 7 7 7s7-2 7-7c0-5-4-7-5-10V5" />
      </svg>
      <svg v-else-if="category.shape === 'cup'" viewBox="0 0 32 32" aria-hidden="true">
        <path d="M7 10h17v7c0 5-3 8-8 8s-9-3-9-8zm17 2h2c3 0 3 6-2 6M5 27h20" />
      </svg>
      <svg v-else viewBox="0 0 32 32" aria-hidden="true">
        <path d="M13 4h6v5l3 4v11c0 2-2 4-4 4h-4c-2 0-4-2-4-4V13l3-4zm-3 12h12M13 8h6" />
      </svg>
    </span>
    <span class="category-card__meta">
      <strong>{{ category.name }}</strong>
      <small>{{ locale.t('category.items', { count: category.count }) }}</small>
    </span>
    <UiIcon class="category-card__arrow" name="arrow" :size="18" />
  </a>
</template>

<style scoped>
.category-card { display: grid; grid-template-columns: 44px 1fr auto; align-items: center; gap: 12px; min-width: 0; height: 72px; padding: 11px 16px; color: var(--ink); background: var(--white); border: 1px solid var(--line); text-decoration: none; transition: border-color 160ms ease, transform 160ms ease; }
.category-card:hover { border-color: var(--ink); transform: translateY(-2px); }
.category-card__icon { display: grid; width: 42px; height: 42px; place-items: center; color: white; background: var(--red); border-radius: 50%; }
.category-card__icon.is-blue { background: var(--blue); }
.category-card__icon.is-yellow { background: var(--yellow); }
.category-card__icon.is-black { background: var(--ink); }
.category-card__icon.is-cream { color: var(--ink); background: var(--paper); border: 1px solid var(--line); }
.category-card__icon svg { width: 24px; fill: none; stroke: currentColor; stroke-width: 1.5; stroke-linecap: round; stroke-linejoin: round; }
.category-card__meta { display: flex; min-width: 0; flex-direction: column; gap: 4px; }
.category-card__meta strong { overflow: hidden; font-size: 12px; font-weight: 650; text-overflow: ellipsis; }
.category-card__meta small { color: var(--muted); font-size: 10px; }
.category-card__arrow { color: #555; }
</style>
