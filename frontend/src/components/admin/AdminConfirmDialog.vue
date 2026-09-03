<script setup>
import { computed } from 'vue'
import { useLocaleStore } from '../../stores/locale'

const props = defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '' },
  message: { type: String, required: true },
  confirmText: { type: String, default: '' },
  busy: { type: Boolean, default: false },
  danger: { type: Boolean, default: false },
})

const emit = defineEmits(['confirm', 'cancel'])
const locale = useLocaleStore()
const dialogTitle = computed(() => props.title || locale.t('admin.dialog.defaultTitle'))
const dialogConfirmText = computed(() => props.confirmText || locale.t('admin.dialog.confirm'))
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="confirm-backdrop" role="presentation" @click.self="emit('cancel')">
      <section class="confirm-dialog" role="dialog" aria-modal="true" :aria-label="dialogTitle">
        <p>{{ locale.t('admin.layout.adminLabel') }}</p>
        <h2>{{ dialogTitle }}</h2>
        <div class="confirm-dialog__rule" />
        <span>{{ message }}</span>
        <footer>
          <button type="button" :disabled="busy" @click="emit('cancel')">{{ locale.t('admin.common.cancel') }}</button>
          <button type="button" class="primary" :class="{ danger }" :disabled="busy" @click="emit('confirm')">
            {{ busy ? locale.t('admin.common.processing') : dialogConfirmText }}
          </button>
        </footer>
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.confirm-backdrop { position: fixed; z-index: 100; display: grid; inset: 0; padding: 20px; place-items: center; background: rgb(21 21 21 / 38%); }
.confirm-dialog { width: min(430px, 100%); padding: 30px; background: var(--white); border: 1px solid var(--ink); box-shadow: 0 24px 70px rgb(21 21 21 / 16%); }
.confirm-dialog > p { margin: 0 0 8px; color: var(--red); font-size: 9px; font-weight: 750; letter-spacing: .15em; }
.confirm-dialog h2 { margin: 0; font-size: 28px; letter-spacing: -.04em; }
.confirm-dialog__rule { width: 48px; height: 2px; margin: 20px 0; background: var(--ink); }
.confirm-dialog > span { display: block; color: var(--muted); font-size: 13px; line-height: 1.7; }
.confirm-dialog footer { display: flex; justify-content: flex-end; gap: 9px; margin-top: 28px; }
.confirm-dialog button { min-width: 88px; padding: 10px 15px; color: var(--ink); background: var(--white); border: 1px solid var(--line); cursor: pointer; }
.confirm-dialog button.primary { color: var(--white); background: var(--ink); border-color: var(--ink); }
.confirm-dialog button.danger { background: var(--red); border-color: var(--red); }
.confirm-dialog button:disabled { cursor: wait; opacity: .55; }
</style>
