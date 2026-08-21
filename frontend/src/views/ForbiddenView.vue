<script setup>
import { useLocaleStore } from '../stores/locale'

const locale = useLocaleStore()
const t = (key) => locale.t(key)
</script>

<template>
  <section class="forbidden page-shell">
    <div>
      <p>403 / EC-01</p>
      <h1>{{ t('forbidden.title') }}</h1>
      <span>{{ t('forbidden.description') }}</span>
      <RouterLink to="/products">{{ t('forbidden.back') }}</RouterLink>
    </div>
  </section>
</template>

<style scoped>
.forbidden { display: grid; min-height: 68vh; padding: 64px 0 82px; place-items: center; }
.forbidden > div { width: min(560px, 100%); padding: 46px; text-align: center; background: var(--paper); border: 1px solid var(--line); }
.forbidden p { margin: 0 0 10px; color: var(--red); font-size: 9px; font-weight: 750; letter-spacing: .16em; }
.forbidden h1 { margin: 0; font-size: clamp(38px, 6vw, 64px); letter-spacing: -.055em; }
.forbidden span { display: block; margin: 15px auto 26px; color: var(--muted); font-size: 12px; line-height: 1.7; }
.forbidden a { display: inline-flex; min-height: 40px; align-items: center; padding: 10px 17px; color: var(--white); font-size: 10px; font-weight: 750; background: var(--ink); text-decoration: none; }
</style>
