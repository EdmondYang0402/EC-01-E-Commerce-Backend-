<script setup>
import UiIcon from '../common/UiIcon.vue'
import { useLocaleStore } from '../../stores/locale'

defineProps({
  banner: { type: Object, required: true },
})
const locale = useLocaleStore()
</script>

<template>
  <section class="hero" aria-labelledby="hero-title">
    <div class="hero__copy">
      <p class="hero__eyebrow">{{ banner.eyebrow }}</p>
      <h1 id="hero-title">
        <template v-for="(line, index) in banner.title.split('\n')" :key="line">
          {{ line }}<br v-if="index < banner.title.split('\n').length - 1" />
        </template>
        <span class="hero__dot">.</span>
      </h1>
      <p class="hero__description">{{ banner.description }}</p>
      <el-button class="hero__cta" tag="a" :href="banner.ctaTarget">
        {{ banner.ctaLabel }}
        <UiIcon name="arrow" :size="17" />
      </el-button>
      <div class="hero__pagination" :aria-label="locale.t('home.hero.slide')">
        <span class="is-active"></span><span></span><span></span>
      </div>
    </div>

    <div class="hero__art" aria-hidden="true">
      <div class="art-grid"></div>
      <div class="art-blue-circle"></div>
      <div class="art-yellow-square"></div>
      <div class="art-black-block"></div>
      <div class="art-red-semicircle"></div>
      <div class="art-arc"></div>
      <div class="art-line-vertical"></div>
      <div class="art-line-horizontal"></div>
      <div class="art-small-dot"></div>
    </div>
  </section>
</template>

<style scoped>
.hero {
  display: grid;
  grid-template-columns: 48% 52%;
  min-height: clamp(350px, 25.5vw, 430px);
  overflow: hidden;
  background: var(--paper);
}

.hero__copy {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 46px clamp(38px, 5.2vw, 82px);
}

.hero__eyebrow {
  margin: 0 0 18px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: .085em;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  font-size: clamp(48px, 4vw, 70px);
  font-weight: 620;
  line-height: .98;
  letter-spacing: -.055em;
}

.hero__dot { color: var(--red); }
.hero__description { max-width: 410px; margin: 21px 0 22px; color: var(--muted); font-size: 13px; line-height: 1.55; }
.hero__cta.el-button { width: max-content; height: 42px; padding: 0 17px 0 19px; color: white; font-size: 12px; font-weight: 600; text-decoration: none; background: var(--ink); border: 1px solid var(--ink); border-radius: 2px; }
.hero__cta.el-button :deep(span) { display: flex; align-items: center; gap: 25px; }
.hero__cta.el-button:hover { color: white; background: #2d2d2d; border-color: #2d2d2d; }
.hero__pagination { position: absolute; bottom: 26px; left: clamp(38px, 5.2vw, 82px); display: flex; gap: 13px; }
.hero__pagination span { width: 6px; height: 6px; background: #c8c5bf; border-radius: 50%; }
.hero__pagination .is-active { background: var(--red); }

.hero__art { position: relative; overflow: hidden; border-left: 1px solid rgba(22,22,22,.04); }
.art-blue-circle { position: absolute; z-index: 4; top: 4%; left: 29%; width: 31%; aspect-ratio: 1; background: var(--blue); border-radius: 50%; }
.art-yellow-square { position: absolute; z-index: 2; top: 27%; right: 23%; width: 31%; aspect-ratio: 1.02; background: var(--yellow); }
.art-black-block { position: absolute; z-index: 3; right: 0; bottom: 0; width: 28%; height: 34%; background: var(--ink); }
.art-red-semicircle { position: absolute; z-index: 4; right: 27%; bottom: -42%; width: 57%; aspect-ratio: 1; background: var(--red); border-radius: 50% 50% 0 0; }
.art-arc { position: absolute; bottom: -21%; left: -5%; width: 57%; aspect-ratio: 1; border: 1.5px solid var(--ink); border-radius: 50%; }
.art-line-vertical { position: absolute; z-index: 5; top: 0; right: 28%; width: 1px; height: 100%; background: var(--ink); }
.art-line-horizontal { position: absolute; z-index: 5; top: 54%; right: 0; width: 23%; height: 1px; background: var(--ink); }
.art-small-dot { position: absolute; z-index: 6; top: 57%; right: 11%; width: 7px; height: 7px; background: var(--red); border-radius: 50%; }
.art-grid { position: absolute; z-index: 5; top: 28%; right: 4.5%; width: 18%; height: 28%; background-image: radial-gradient(circle, var(--ink) 1.6px, transparent 1.8px); background-size: 18px 18px; }

@media (max-width: 900px) {
  .hero { grid-template-columns: 55% 45%; }
  .art-blue-circle { left: 10%; width: 46%; }
  .art-yellow-square { right: 17%; width: 40%; }
  .art-red-semicircle { right: 17%; width: 76%; }
}

@media (max-width: 680px) {
  .hero { grid-template-columns: 1fr; min-height: auto; }
  .hero__copy { min-height: 410px; padding: 44px 26px 60px; }
  .hero__art { height: 300px; border-top: 1px solid var(--line); border-left: 0; }
  .hero__pagination { bottom: 28px; left: 26px; }
}
</style>
