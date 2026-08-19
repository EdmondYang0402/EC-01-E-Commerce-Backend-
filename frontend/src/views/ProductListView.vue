<script setup>
import { computed, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import ProductCard from '../components/home/ProductCard.vue'
import { errorMessage } from '../services/http'
import { useProductStore } from '../stores/products'
import { useShopStore } from '../stores/shop'
import { useLocaleStore } from '../stores/locale'

const products = useProductStore()
const shop = useShopStore()
const locale = useLocaleStore()
const { records, total, loading } = storeToRefs(products)
const t = (key, params) => locale.t(key, params)
const page = ref(1)
const size = 12
const keyword = ref('')
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size)))

const loadProducts = async () => {
  try {
    await products.fetchPage({
      page: page.value,
      size,
      keyword: keyword.value.trim() || undefined,
    })
  } catch (error) {
    ElMessage.error(errorMessage(error, t('message.productsFailed')))
  }
}

const search = () => {
  page.value = 1
  loadProducts()
}

const changePage = (nextPage) => {
  if (nextPage < 1 || nextPage > totalPages.value || nextPage === page.value) return
  page.value = nextPage
  loadProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(loadProducts)
</script>

<template>
  <section class="catalog page-shell">
    <div class="catalog__heading">
      <div>
        <p class="eyebrow">{{ t('catalog.eyebrow') }}</p>
        <h1>{{ t('catalog.title') }}</h1>
        <p>{{ t('catalog.count', { count: total }) }}</p>
      </div>
      <form class="search" @submit.prevent="search">
        <el-input v-model="keyword" clearable :placeholder="t('catalog.searchPlaceholder')" :aria-label="t('catalog.searchPlaceholder')" />
        <el-button native-type="submit" :loading="loading">{{ t('catalog.search') }}</el-button>
      </form>
    </div>

    <p v-if="loading" class="state">{{ t('catalog.loading') }}</p>
    <p v-else-if="!records.length" class="state">{{ t('catalog.empty') }}</p>
    <div v-else class="catalog__grid">
      <ProductCard
        v-for="product in records"
        :key="product.id"
        :product="product"
        :favorite="shop.isFavorite(product.id)"
        @favorite="shop.toggleFavorite"
      />
    </div>

    <nav v-if="totalPages > 1" class="pagination" :aria-label="t('catalog.pages')">
      <button type="button" :disabled="page === 1" @click="changePage(page - 1)">{{ t('catalog.previous') }}</button>
      <span>{{ t('catalog.pageOf', { page, total: totalPages }) }}</span>
      <button type="button" :disabled="page === totalPages" @click="changePage(page + 1)">{{ t('catalog.next') }}</button>
    </nav>
  </section>
</template>

<style scoped>
.catalog { min-height: 65vh; padding-top: 52px; padding-bottom: 72px; }
.catalog__heading { display: flex; align-items: flex-end; justify-content: space-between; gap: 28px; margin-bottom: 34px; padding-bottom: 24px; border-bottom: 1px solid var(--line); }
.eyebrow { margin: 0 0 8px !important; color: var(--red) !important; font-size: 10px !important; font-weight: 700; letter-spacing: .14em; text-transform: uppercase; }
.catalog h1 { margin: 0; font-size: clamp(34px, 4vw, 58px); letter-spacing: -.05em; }
.catalog__heading p { margin: 10px 0 0; color: var(--muted); font-size: 12px; }
.search { display: flex; width: min(420px, 100%); gap: 8px; }
.search .el-button { color: white; background: var(--ink); border-color: var(--ink); }
.catalog__grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 20px; }
.state { padding: 64px 0; color: var(--muted); text-align: center; }
.pagination { display: flex; align-items: center; justify-content: center; gap: 18px; margin-top: 40px; font-size: 12px; }
.pagination button { padding: 9px 15px; background: var(--white); border: 1px solid var(--line); cursor: pointer; }
.pagination button:disabled { cursor: not-allowed; opacity: .45; }
@media (max-width: 900px) { .catalog__grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 620px) { .catalog__heading { align-items: stretch; flex-direction: column; } .search { width: 100%; } }
</style>
