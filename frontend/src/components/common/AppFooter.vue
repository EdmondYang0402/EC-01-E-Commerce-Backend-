<script setup>
import UiIcon from './UiIcon.vue'
import { useLocaleStore } from '../../stores/locale'

const columns = [
  { title: 'footer.shop', links: ['footer.allProducts', 'footer.giftCards', 'footer.sale'] },
  { title: 'footer.care', links: ['footer.shipping', 'footer.returns', 'footer.faq'] },
  { title: 'footer.company', links: ['footer.about', 'footer.journal', 'footer.contact'] },
]
const locale = useLocaleStore()
const t = (key) => locale.t(key)
</script>

<template>
  <footer id="footer" class="footer">
    <div class="footer__inner page-shell">
      <section class="footer__brand">
        <RouterLink to="/">EC-01</RouterLink>
        <p>{{ t('footer.tagline') }}</p>
        <small>{{ t('footer.rights') }}</small>
      </section>

      <nav v-for="column in columns" :key="column.title" class="footer__column" :aria-label="t(column.title)">
        <h2>{{ t(column.title) }}</h2>
        <a v-for="link in column.links" :key="link" href="#">{{ t(link) }}</a>
      </nav>

      <section class="newsletter">
        <h2>{{ t('footer.newsletter') }}</h2>
        <p>{{ t('footer.newsletterText') }}</p>
        <div class="newsletter__form">
          <el-input :aria-label="t('footer.email')" :placeholder="t('footer.emailPlaceholder')" />
          <el-button>{{ t('footer.subscribe') }}</el-button>
        </div>
        <div class="socials" :aria-label="t('footer.social')">
          <a href="#" aria-label="Instagram">ig</a>
          <a href="#" aria-label="Pinterest">p</a>
          <a href="#" aria-label="Facebook">f</a>
          <a href="#" :aria-label="t('footer.more')"><UiIcon name="plus" :size="12" /></a>
        </div>
      </section>
    </div>
  </footer>
</template>

<style scoped>
.footer { padding: 30px 0 38px; background: var(--white); border-top: 1px solid var(--line); }
.footer__inner { display: grid; grid-template-columns: 1.4fr repeat(3, .75fr) 1.8fr; gap: clamp(26px, 4vw, 70px); }
.footer__brand > a { color: var(--ink); font-size: 21px; font-weight: 750; letter-spacing: -.055em; text-decoration: none; }
.footer__brand p, .newsletter p { margin: 11px 0 22px; color: var(--muted); font-size: 10px; line-height: 1.5; }
.footer__brand small { color: #7e7b76; font-size: 9px; }
.footer h2 { margin: 0 0 13px; font-size: 10px; font-weight: 700; }
.footer__column { display: flex; flex-direction: column; align-items: flex-start; gap: 9px; }
.footer__column a { color: var(--muted); font-size: 10px; text-decoration: none; }
.footer__column a:hover { color: var(--ink); }
.newsletter p { max-width: 250px; margin: -4px 0 10px; }
.newsletter__form { display: grid; grid-template-columns: 1fr auto; }
.newsletter__form :deep(.el-input__wrapper) { min-height: 32px; padding: 0 11px; box-shadow: 0 0 0 1px var(--line) inset; border-radius: 2px 0 0 2px; }
.newsletter__form :deep(.el-input__inner) { font-size: 10px; }
.newsletter__form .el-button { height: 32px; padding: 0 14px; color: white; font-size: 10px; background: var(--ink); border-color: var(--ink); border-radius: 0 2px 2px 0; }
.socials { display: flex; gap: 18px; margin-top: 20px; }
.socials a { display: grid; width: 17px; height: 17px; place-items: center; color: var(--ink); font-size: 9px; font-weight: 700; border: 1px solid var(--ink); border-radius: 50%; text-decoration: none; }

@media (max-width: 900px) {
  .footer__inner { grid-template-columns: repeat(3, 1fr); }
  .footer__brand, .newsletter { grid-column: span 3; }
  .newsletter { max-width: 440px; }
}

@media (max-width: 560px) {
  .footer__inner { grid-template-columns: repeat(2, 1fr); gap: 28px; }
  .footer__brand, .newsletter { grid-column: span 2; }
}
</style>
