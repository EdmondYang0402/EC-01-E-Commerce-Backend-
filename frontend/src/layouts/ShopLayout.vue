<script setup>
import AppHeader from '../components/common/AppHeader.vue'
import AppFooter from '../components/common/AppFooter.vue'
</script>

<template>
  <div class="shop-layout">
    <AppHeader />
    <main><RouterView /></main>
    <AppFooter />
  </div>
</template>
