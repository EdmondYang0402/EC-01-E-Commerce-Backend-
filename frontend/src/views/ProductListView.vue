<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProductCard from '../components/home/ProductCard.vue'
import { errorMessage } from '../services/http'
import { useProductStore } from '../stores/products'
import { useCategoryStore } from '../stores/categories'
import { useShopStore } from '../stores/shop'
import { useLocaleStore } from '../stores/locale'

const products = useProductStore()
const categories = useCategoryStore()
const shop = useShopStore()
const locale = useLocaleStore()
const { records, total, loading } = storeToRefs(products)
const { tree, loading: categoryLoading } = storeToRefs(categories)
const route = useRoute()
const router = useRouter()
const t = (key, params) => locale.t(key, params)
const size = 12
const keyword = ref('')
const page = computed(() => Math.max(1, Number(route.query.page) || 1))
const selectedCategoryId = computed(() => {
  const value = Number(route.query.categoryId)
  return Number.isInteger(value) && value > 0 ? value : null
})
const selectedRoot = computed(() => tree.value.find((root) =>
  root.id === selectedCategoryId.value
  || root.children?.some((child) => child.id === selectedCategoryId.value)) || null)
const selectedRootCategoryId = computed(() => selectedRoot.value?.id || null)
const selectedChildCategoryId = computed(() => selectedRoot.value?.children?.some(
  (child) => child.id === selectedCategoryId.value) ? selectedCategoryId.value : null)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size)))

const loadProducts = async () => {
  try {
    await products.fetchPage({
      page: page.value,
      size,
      keyword: keyword.value.trim() || undefined,
      categoryId: selectedCategoryId.value || undefined,
    })
  } catch (error) {
    ElMessage.error(errorMessage(error, t('message.productsFailed')))
  }
}

const search = () => {
  updateQuery({ page: 1, keyword: keyword.value.trim() || undefined })
}

const changePage = (nextPage) => {
  if (nextPage < 1 || nextPage > totalPages.value || nextPage === page.value) return
  updateQuery({ page: nextPage })
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const updateQuery = (changes) => router.push({
  name: 'products',
  query: { ...route.query, ...changes },
})

const selectRoot = (categoryId) => updateQuery({
  categoryId: categoryId || undefined,
  page: 1,
})

const selectChild = (categoryId) => updateQuery({
  categoryId: categoryId || selectedRootCategoryId.value || undefined,
  page: 1,
})

onMounted(async () => {
  keyword.value = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  try { await categories.fetchTree() }
  catch (error) { ElMessage.error(errorMessage(error, '分类加载失败')) }
})

watch(
  () => [route.query.categoryId, route.query.page, route.query.keyword],
  () => {
    keyword.value = typeof route.query.keyword === 'string' ? route.query.keyword : ''
    loadProducts()
  },
  { immediate: true },
)
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

    <nav class="category-nav" aria-label="商品分类">
      <div class="category-nav__roots">
        <button :class="{ active: !selectedCategoryId }" type="button" @click="selectRoot(null)">全部</button>
        <button
          v-for="root in tree"
          :key="root.id"
          :class="{ active: selectedRootCategoryId === root.id }"
          type="button"
          @click="selectRoot(root.id)"
        >{{ root.name }}</button>
      </div>
      <div v-if="selectedRoot" class="category-nav__children">
        <span>{{ selectedRoot.name }}</span>
        <button :class="{ active: !selectedChildCategoryId }" type="button" @click="selectChild(null)">全部</button>
        <button
          v-for="child in selectedRoot.children || []"
          :key="child.id"
          :class="{ active: selectedChildCategoryId === child.id }"
          type="button"
          @click="selectChild(child.id)"
        >{{ child.name }}</button>
      </div>
      <p v-else-if="categoryLoading" class="category-nav__loading">正在加载分类…</p>
    </nav>

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
.category-nav { margin: -12px 0 34px; border-top: 1px solid var(--ink); border-bottom: 1px solid var(--line); }
.category-nav__roots, .category-nav__children { display: flex; align-items: center; flex-wrap: wrap; gap: 3px; padding: 12px 0; }
.category-nav__children { border-top: 1px solid var(--line); }
.category-nav__children > span { margin-right: 16px; font-size: 10px; font-weight: 750; letter-spacing: .08em; }
.category-nav button { padding: 8px 13px; color: var(--muted); background: transparent; border: 1px solid transparent; cursor: pointer; }
.category-nav button:hover { color: var(--ink); border-color: var(--line); }
.category-nav button.active { color: var(--white); background: var(--ink); border-color: var(--ink); }
.category-nav__loading { margin: 0; padding: 14px 0; color: var(--muted); font-size: 11px; }
.catalog__grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 20px; }
.state { padding: 64px 0; color: var(--muted); text-align: center; }
.pagination { display: flex; align-items: center; justify-content: center; gap: 18px; margin-top: 40px; font-size: 12px; }
.pagination button { padding: 9px 15px; background: var(--white); border: 1px solid var(--line); cursor: pointer; }
.pagination button:disabled { cursor: not-allowed; opacity: .45; }
@media (max-width: 900px) { .catalog__grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 620px) { .catalog__heading { align-items: stretch; flex-direction: column; } .search { width: 100%; } }
</style>
