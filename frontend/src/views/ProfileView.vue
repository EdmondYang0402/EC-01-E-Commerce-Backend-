<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { errorMessage } from '../services/http'
import { useAuthStore } from '../stores/auth'
import { useLocaleStore } from '../stores/locale'

const auth = useAuthStore()
const locale = useLocaleStore()
const t = (key) => locale.t(key)
const router = useRouter()
const loading = ref(true)
const saving = ref(false)
const form = reactive({ username: '', nickname: '', email: '', phone: '', avatarUrl: '' })

const fillForm = (profile) => {
  form.username = profile?.username || ''
  form.nickname = profile?.nickname || ''
  form.email = profile?.email || ''
  form.phone = profile?.phone || ''
  form.avatarUrl = profile?.avatarUrl || ''
}

onMounted(async () => {
  try {
    fillForm(await auth.fetchProfile())
  } catch (error) {
    ElMessage.error(errorMessage(error, t('profile.loadFailed')))
  } finally {
    loading.value = false
  }
})

const save = async () => {
  saving.value = true
  try {
    const profile = await auth.updateProfile({
      nickname: form.nickname || null,
      email: form.email || null,
      phone: form.phone || null,
      avatarUrl: form.avatarUrl || null,
    })
    fillForm(profile)
    ElMessage.success(t('profile.updated'))
  } catch (error) {
    ElMessage.error(errorMessage(error, t('profile.updateFailed')))
  } finally {
    saving.value = false
  }
}

const logout = async () => {
  try {
    await auth.logout()
  } catch (error) {
    ElMessage.warning(errorMessage(error, t('profile.sessionCleared')))
  }
  await router.replace('/')
}
</script>

<template>
  <section class="profile page-shell">
    <div class="profile__heading">
      <div>
        <p class="eyebrow">{{ t('profile.eyebrow') }}</p>
        <h1>{{ t('profile.title') }}</h1>
      </div>
      <el-button plain @click="logout">{{ t('profile.signOut') }}</el-button>
    </div>

    <p v-if="loading" class="state">{{ t('profile.loading') }}</p>
    <form v-else class="profile-form" @submit.prevent="save">
      <label>{{ t('profile.username') }}<el-input v-model="form.username" disabled /></label>
      <label>{{ t('profile.nickname') }}<el-input v-model="form.nickname" maxlength="50" /></label>
      <label>{{ t('profile.email') }}<el-input v-model="form.email" type="email" maxlength="100" /></label>
      <label>{{ t('profile.phone') }}<el-input v-model="form.phone" maxlength="30" /></label>
      <label class="full">{{ t('profile.avatar') }}<el-input v-model="form.avatarUrl" maxlength="500" /></label>
      <div class="full actions"><el-button native-type="submit" :loading="saving">{{ t('profile.save') }}</el-button></div>
    </form>
  </section>
</template>

<style scoped>
.profile { min-height: 68vh; padding-top: 54px; padding-bottom: 82px; }
.profile__heading { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 32px; padding-bottom: 24px; border-bottom: 1px solid var(--line); }
.eyebrow { margin: 0 0 8px; color: var(--red); font-size: 10px; font-weight: 700; letter-spacing: .14em; text-transform: uppercase; }
h1 { margin: 0; font-size: 52px; letter-spacing: -.05em; }
.profile-form { display: grid; max-width: 760px; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 22px; }
label { display: grid; gap: 8px; font-size: 11px; font-weight: 650; }
.full { grid-column: 1 / -1; }
.actions { padding-top: 8px; }
.actions .el-button { width: 180px; height: 42px; color: white; background: var(--ink); border-color: var(--ink); }
.state { color: var(--muted); }
@media (max-width: 620px) { .profile-form { grid-template-columns: 1fr; } .full { grid-column: auto; } }
</style>
