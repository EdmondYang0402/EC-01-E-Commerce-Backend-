<script setup>
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import HomeHero from '../components/home/HomeHero.vue'
import CategoryCard from '../components/home/CategoryCard.vue'
import ProductCard from '../components/home/ProductCard.vue'
import NewArrivalItem from '../components/home/NewArrivalItem.vue'
import UiIcon from '../components/common/UiIcon.vue'
import { errorMessage } from '../services/http'
import { useShopStore } from '../stores/shop'
import { useProductStore } from '../stores/products'
import { useCategoryStore } from '../stores/categories'
import { useLocaleStore } from '../stores/locale'
import { heroBanner } from '../mock/home'

const shop = useShopStore()
const products = useProductStore()
const categoryStore = useCategoryStore()
const locale = useLocaleStore()
const { records, loading } = storeToRefs(products)
const { tree: categoryTree, loading: categoryLoading } = storeToRefs(categoryStore)
const t = (key, params) => locale.t(key, params)
const categoryStyles = [
  { accent: 'red', shape: 'device' },
  { accent: 'blue', shape: 'keyboard' },
  { accent: 'yellow', shape: 'shirt' },
  { accent: 'black', shape: 'office' },
  { accent: 'cream', shape: 'home' },
]

const featuredProducts = computed(() => records.value.slice(0, 6))
const newArrivals = computed(() => records.value.slice(6, 12))
const banner = computed(() => ({
  ...heroBanner,
  eyebrow: t('home.hero.eyebrow'),
  title: t('home.hero.title'),
  description: t('home.hero.description'),
  ctaLabel: t('home.hero.cta'),
}))
const homeCategories = computed(() => categoryTree.value.slice(0, 5).map((category, index) => {
  const translationKey = `category.root.${category.id}`
  const translatedName = t(translationKey)
  return {
    ...category,
    ...categoryStyles[index],
    name: translatedName === translationKey ? category.name : translatedName,
    count: Number(category.productCount) || 0,
    to: { name: 'products', query: { categoryId: category.id } },
  }
}))

onMounted(async () => {
  await Promise.allSettled([
    products.fetchPage({ page: 1, size: 12 }).catch((error) => {
      ElMessage.error(errorMessage(error, t('message.productsFailed')))
    }),
    categoryStore.fetchTree().catch((error) => {
      ElMessage.error(errorMessage(error, t('message.categoriesFailed')))
    }),
  ])
})

const handleFavorite = (productId) => {
  shop.toggleFavorite(productId)
}
</script>

<template>
  <div class="home page-shell">
    <HomeHero :banner="banner" />

    <section id="categories" class="category-section" :aria-label="t('home.categories')">
      <p v-if="categoryLoading && !homeCategories.length" class="category-state">{{ t('home.categoriesLoading') }}</p>
      <p v-else-if="!homeCategories.length" class="category-state">{{ t('home.categoriesEmpty') }}</p>
      <CategoryCard v-for="category in homeCategories" :key="category.id" :category="category" />
    </section>

    <section id="featured" class="featured-section" aria-labelledby="featured-title">
      <div class="section-heading">
        <h2 id="featured-title">{{ t('home.featured') }}</h2>
        <RouterLink to="/products">{{ t('home.viewAll') }} <UiIcon name="arrow" :size="15" /></RouterLink>
      </div>
      <p v-if="loading" class="product-state">{{ t('home.loading') }}</p>
      <p v-else-if="!featuredProducts.length" class="product-state">{{ t('home.empty') }}</p>
      <div class="product-grid">
        <ProductCard
          v-for="product in featuredProducts"
          :key="product.id"
          :product="product"
          :favorite="shop.isFavorite(product.id)"
          @favorite="handleFavorite"
        />
      </div>
    </section>

    <section id="new-arrivals" class="arrivals-section" aria-labelledby="arrivals-title">
      <div class="arrivals-section__heading">
        <h2 id="arrivals-title">{{ t('home.arrivals') }}</h2>
        <RouterLink to="/products">{{ t('home.explore') }} <UiIcon name="arrow" :size="15" /></RouterLink>
      </div>
      <div class="arrivals-grid">
        <NewArrivalItem v-for="product in newArrivals" :key="product.id" :product="product" />
      </div>
    </section>
  </div>
</template>

<style scoped>
.home { padding-top: 0; }
.category-section { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 18px; padding: 16px 36px 17px; background: var(--white); }
.category-state { display: grid; grid-column: 1 / -1; min-height: 72px; margin: 0; color: var(--muted); font-size: 11px; text-align: center; place-content: center; }
.featured-section { padding: 0 36px 17px; }
.section-heading, .arrivals-section__heading { display: flex; align-items: center; justify-content: space-between; height: 40px; }
.section-heading h2, .arrivals-section h2 { margin: 0; font-size: 15px; font-weight: 680; letter-spacing: -.02em; }
.section-heading a, .arrivals-section__heading a { display: flex; align-items: center; gap: 8px; color: var(--muted); font-size: 10px; text-decoration: none; }
.section-heading a:hover, .arrivals-section__heading a:hover { color: var(--ink); }
.product-grid { display: grid; grid-template-columns: repeat(6, minmax(0, 1fr)); gap: 18px; }
.product-state { margin: 20px 0; color: var(--muted); font-size: 12px; }
.arrivals-section { padding: 0 36px 17px; background: var(--paper); }
.arrivals-section__heading { height: 48px; }
.arrivals-grid { display: grid; grid-template-columns: repeat(6, minmax(0, 1fr)); padding-bottom: 4px; }

@media (max-width: 1180px) {
  .category-section, .product-grid { gap: 12px; }
  .category-section, .featured-section, .arrivals-section { padding-right: 20px; padding-left: 20px; }
  .product-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .arrivals-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); row-gap: 16px; }
}

@media (max-width: 780px) {
  .category-section { grid-template-columns: repeat(5, 220px); overflow-x: auto; padding-top: 14px; scrollbar-width: none; }
  .category-section::-webkit-scrollbar { display: none; }
  .product-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .arrivals-grid { display: flex; overflow-x: auto; scrollbar-width: none; }
  .arrivals-grid::-webkit-scrollbar { display: none; }
  .arrivals-grid > * { min-width: 220px; }
}

@media (max-width: 480px) {
  .category-section, .featured-section, .arrivals-section { padding-right: 12px; padding-left: 12px; }
  .product-grid { gap: 10px; }
  .section-heading a { display: none; }
  .arrivals-section__heading a { font-size: 0; }
}
</style>
