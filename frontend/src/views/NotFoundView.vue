<script setup>
import { useLocaleStore } from '../stores/locale'
const locale = useLocaleStore()
</script>
<template><section class="not-found page-shell"><div><p>404 / EC-01</p><h1>{{ locale.t('notFound.title') }}</h1><span>{{ locale.t('notFound.description') }}</span><RouterLink to="/">{{ locale.t('notFound.back') }}</RouterLink></div></section></template>
<style scoped>
.not-found { display:grid; min-height:68vh; padding:64px 0 82px; place-items:center; }.not-found>div { width:min(560px,100%); padding:46px; text-align:center; background:var(--paper); border:1px solid var(--line); }.not-found p { margin:0 0 10px; color:var(--red); font-size:9px; font-weight:750; letter-spacing:.16em; }.not-found h1 { margin:0; font-size:clamp(38px,6vw,64px); letter-spacing:-.055em; }.not-found span { display:block; margin:15px auto 26px; color:var(--muted); font-size:12px; line-height:1.7; }.not-found a { display:inline-flex; min-height:40px; align-items:center; padding:10px 17px; color:var(--white); font-size:10px; font-weight:750; background:var(--ink); text-decoration:none; }
</style>
