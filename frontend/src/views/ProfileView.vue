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
const passwordSaving = ref(false)
const form = reactive({ username: '', nickname: '', email: '', phone: '', avatarUrl: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmNewPassword: '' })

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

const changePassword = async () => {
  const oldPassword = passwordForm.oldPassword
  const newPassword = passwordForm.newPassword
  const confirmNewPassword = passwordForm.confirmNewPassword
  if (!oldPassword || !newPassword || !confirmNewPassword) {
    ElMessage.warning(t('security.required'))
    return
  }
  if (newPassword !== confirmNewPassword) {
    ElMessage.warning(t('security.mismatch'))
    return
  }
  if (newPassword === oldPassword) {
    ElMessage.warning(t('security.samePassword'))
    return
  }
  if (newPassword.length < 8 || newPassword.length > 32) {
    ElMessage.warning(t('security.length'))
    return
  }
  if (!/^(?=.*[A-Za-z])(?=.*\d).+$/.test(newPassword)) {
    ElMessage.warning(t('security.format'))
    return
  }

  passwordSaving.value = true
  try {
    await auth.changePassword({ oldPassword, newPassword, confirmNewPassword })
    Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmNewPassword: '' })
    ElMessage.success(t('security.success'))
    await router.replace('/login')
  } catch (error) {
    ElMessage.error(errorMessage(error, t('security.failed')))
  } finally {
    passwordSaving.value = false
  }
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

    <section id="security" class="security-section">
      <header>
        <div>
          <p class="eyebrow">{{ t('security.eyebrow') }}</p>
          <h2>{{ t('security.title') }}</h2>
          <p>{{ t('security.intro') }}</p>
        </div>
        <span>{{ t('security.sessionNote') }}</span>
      </header>
      <form class="password-form" @submit.prevent="changePassword">
        <label>{{ t('security.currentPassword') }}<el-input v-model="passwordForm.oldPassword" type="password" show-password autocomplete="current-password" /></label>
        <label>{{ t('security.newPassword') }}<el-input v-model="passwordForm.newPassword" type="password" show-password autocomplete="new-password" /></label>
        <label>{{ t('security.confirmPassword') }}<el-input v-model="passwordForm.confirmNewPassword" type="password" show-password autocomplete="new-password" /></label>
        <p class="password-hint">{{ t('security.passwordHint') }}</p>
        <div class="password-actions"><el-button native-type="submit" :loading="passwordSaving">{{ t('security.submit') }}</el-button></div>
      </form>
    </section>
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
.security-section { margin-top: 58px; padding-top: 34px; border-top: 1px solid var(--ink); scroll-margin-top: 88px; }
.security-section > header { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; margin-bottom: 26px; }
.security-section h2 { margin: 0; font-size: 30px; letter-spacing: -.04em; }
.security-section header p:last-child { max-width: 540px; margin: 9px 0 0; color: var(--muted); font-size: 11px; line-height: 1.65; }
.security-section header > span { max-width: 240px; padding: 9px 12px; color: #8b6413; font-size: 9px; line-height: 1.5; background: #fbf4df; border: 1px solid #ead9a7; }
.password-form { display: grid; max-width: 760px; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 22px; padding: 25px; background: var(--paper); border: 1px solid var(--line); }
.password-form label:first-child { grid-column: 1 / -1; }
.password-hint { grid-column: 1 / -1; margin: -4px 0 0; color: var(--muted); font-size: 10px; line-height: 1.6; }
.password-actions { grid-column: 1 / -1; }
.password-actions .el-button { width: 180px; height: 42px; color: white; background: var(--ink); border-color: var(--ink); }
@media (max-width: 620px) { .profile-form { grid-template-columns: 1fr; } .full { grid-column: auto; } }
@media (max-width: 620px) { .security-section > header { align-items: flex-start; flex-direction: column; } .password-form { grid-template-columns: 1fr; padding: 18px; } .password-form label:first-child, .password-hint, .password-actions { grid-column: auto; } }
</style>
