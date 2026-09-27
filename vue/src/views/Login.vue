<template>
  <div class="login-container">
    <!-- 左侧品牌宣传区 (PC 端淡青质感画卷，移动端自适应紧凑 Banner) -->
    <div class="left-section">
      <div class="brand-hero-wrapper">
        <div class="brand-hero-content">
          <div class="brand-badge">
            <span class="badge-dot"></span>
            <span>八王子学生専用 C2C リユース</span>
          </div>
          <h1 class="brand-title">
            <span class="brand-kanji">桑都安</span>
            <span class="brand-english">- SOUTOYASU -</span>
          </h1>
          <p class="brand-tagline">キャンパスをもっと身近に、もっとお得に。</p>
          <p class="brand-subtext">八王子エリアの大学生・専門学生同士で安心して教科書や家具・日用品を売り買いできるマーケットプレイス。</p>

          <div class="brand-features">
            <div class="feature-tag"><i class="el-icon-circle-check"></i> 学生限定で安心</div>
            <div class="feature-tag"><i class="el-icon-refresh"></i> エコなリユース</div>
          </div>
        </div>
        <div class="illustration-wrapper">
          <img src="../assets/login.svg" alt="ログインイラスト" class="illustration" />
        </div>
      </div>
    </div>

    <!-- 右侧登录表单区 -->
    <div class="right-section">
      <div class="login-card">
        <div class="login-header">
          <h2 class="welcome-title">おかえりなさい</h2>
          <p class="welcome-subtitle">アカウント情報を入力してログインしてください</p>
        </div>

        <el-form :model="user" :rules="rules" ref="loginRef" class="login-form" @keyup.enter.native="login">
          <!-- 身份选择：高质感淡青胶囊分段控制器 (Segmented Control) 代替生硬的下拉框 -->
          <div class="role-selector-wrapper">
            <div class="role-selector-label">ログイン種別</div>
            <div class="role-segmented">
              <div
                class="role-option"
                :class="{ 'is-active': user.role === 'USER' }"
                @click="user.role = 'USER'"
              >
                <i class="el-icon-user"></i>
                <span>一般ユーザー</span>
              </div>
              <div
                class="role-option"
                :class="{ 'is-active': user.role === 'ADMIN' }"
                @click="user.role = 'ADMIN'"
              >
                <i class="el-icon-s-custom"></i>
                <span>管理者</span>
              </div>
            </div>
            <!-- 隐藏的表单字段保证 rules 校验 -->
            <el-input v-model="user.role" style="display: none;"></el-input>
          </div>

          <el-form-item prop="username">
            <el-input
              v-model="user.username"
              class="custom-input"
              size="medium"
              placeholder="ユーザー名を入力"
              prefix-icon="el-icon-user"
              clearable
            ></el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="user.password"
              class="custom-input"
              size="medium"
              type="password"
              placeholder="パスワードを入力"
              prefix-icon="el-icon-lock"
              show-password
            ></el-input>
          </el-form-item>

          <div class="form-actions-row">
            <el-button type="primary" class="login-submit-btn" :loading="loggingIn" @click="login">
              ログイン
            </el-button>
          </div>

          <div class="login-footer-links">
            <router-link to="/register" class="register-link">
              新規アカウント登録 <i class="el-icon-arrow-right"></i>
            </router-link>
            <a href="javascript:;" class="forget-link" @click.prevent="handleForgetPass">
              パスワードをお忘れですか？
            </a>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 找回密码弹窗 -->
    <el-dialog
      title="パスワードの再設定"
      :visible.sync="forgetPassDialogVis"
      custom-class="forget-pass-dialog"
      :close-on-click-modal="false"
      width="420px"
    >
      <p class="dialog-tips">登録済みのユーザー名と電話番号を入力してください。</p>
      <el-form :model="forgetUserForm" label-width="84px" class="forget-form">
        <el-form-item label="ユーザー名">
          <el-input v-model="forgetUserForm.username" autocomplete="off" placeholder="ユーザー名を入力"></el-input>
        </el-form-item>
        <el-form-item label="電話番号">
          <el-input v-model="forgetUserForm.phone" autocomplete="off" placeholder="電話番号を入力"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="forgetPassDialogVis = false">キャンセル</el-button>
        <el-button type="primary" class="dialog-confirm-btn" @click="resetPassword">再設定を確定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { createPixelAvatarId } from '@/utils/pixelAvatar'

export default {
  name: 'Login',
  data() {
    return {
      forgetUserForm: {},
      forgetPassDialogVis: false,
      loggingIn: false,
      user: {
        username: '',
        password: '',
        role: 'USER'
      },
      rules: {
        username: [
          { required: true, message: 'ユーザー名を入力してください', trigger: 'blur' },
        ],
        password: [
          { required: true, message: 'パスワードを入力してください', trigger: 'blur' },
        ],
        role: [
          { required: true, message: 'アカウント種別を選択してください', trigger: 'change' },
        ],
      }
    }
  },
  methods: {
    handleForgetPass() {
      this.forgetUserForm = {}
      this.forgetPassDialogVis = true
    },
    resetPassword() {
      if (!this.forgetUserForm.username || !this.forgetUserForm.phone) {
        this.$message.warning('ユーザー名と電話番号を入力してください')
        return
      }
      this.$request.put('/password', this.forgetUserForm).then(res => {
        if (res.code === '200') {
          this.$message.success('パスワードをリセットしました（初期値: 123456）')
          this.forgetPassDialogVis = false
        } else {
          this.$notify.error({ title: 'エラー', message: res.msg || 'リセットに失敗しました', showClose: false, duration: 2500 })
        }
      })
    },
    login() {
      this.$refs['loginRef'].validate((valid) => {
        if (!valid) return
        this.loggingIn = true
        this.$request.post('/login', this.user).then(res => {
          this.loggingIn = false
          if (res.code === '200') {
            const generatedAvatar = !res.data.avatar
            if (!res.data.avatar) {
              res.data.avatar = createPixelAvatarId(res.data.id)
            }
            localStorage.setItem("user", JSON.stringify(res.data))
            if (generatedAvatar) {
              this.$request.put('/user/avatar', { avatar: res.data.avatar }).catch(() => {})
            }
            if (this.user.role === 'ADMIN') {
              this.$router.push('/')
            } else {
              this.$router.push('/front/home')
            }
            this.$notify.success({ title: 'ようこそ', message: 'ログインしました', showClose: false, duration: 2000 })
          } else {
            this.$notify.error({ title: 'ログイン失敗', message: res.msg || 'ユーザー名またはパスワードが正しくありません', showClose: false, duration: 2500 })
          }
        }).catch(() => {
          this.loggingIn = false
        })
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: #f0fdfa;
  color: #334155;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* ================= 左侧品牌画卷区 (淡青深墨质感) ================= */
.left-section {
  flex: 6;
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(circle at 18% 22%, rgba(20, 184, 166, .26), transparent 45%),
    radial-gradient(circle at 85% 78%, rgba(45, 212, 191, .18), transparent 40%),
    linear-gradient(145deg, #091a1d 0%, #0d282c 50%, #11383e 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 48px;
  box-sizing: border-box;
}

/* 居中大容器，确保上部文字与下部图案左侧垂直完美对齐 */
.brand-hero-wrapper {
  width: 100%;
  max-width: 480px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
  z-index: 2;
}

.brand-hero-content {
  width: 100%;
  margin-bottom: 24px;
}

.brand-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(20, 184, 166, .18);
  border: 1px solid rgba(45, 212, 191, .45);
  color: #5eead4;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 20px;
  margin-bottom: 18px;
}

.badge-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #2dd4bf;
  box-shadow: 0 0 8px #2dd4bf;
}

.brand-title {
  margin: 0 0 14px;
  line-height: 1.15;
  display: flex;
  align-items: baseline;
  gap: 12px;
  flex-wrap: wrap;
}

.brand-kanji {
  font-size: 42px;
  font-weight: 800;
  letter-spacing: .04em;
  background: linear-gradient(180deg, #ffffff 30%, #ccfbf1 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.brand-english {
  font-size: 20px;
  font-weight: 600;
  color: #5eead4;
  letter-spacing: .12em;
}

.brand-tagline {
  font-size: 20px;
  font-weight: 600;
  color: #f0fdfa;
  margin: 0 0 10px;
}

.brand-subtext {
  font-size: 14px;
  line-height: 1.6;
  color: #cbd5e1;
  margin: 0 0 22px;
}

.brand-features {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.feature-tag {
  background: rgba(255, 255, 255, .08);
  border: 1px solid rgba(255, 255, 255, .15);
  backdrop-filter: blur(8px);
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 12px;
  color: #f0fdfa;
}

.feature-tag i {
  color: #2dd4bf;
  margin-right: 4px;
}

/* 下方插画容器：与上方文字内容左侧严格垂直对齐 */
.illustration-wrapper {
  width: 100%;
  display: flex;
  justify-content: flex-start;
  margin-top: 8px;
}

.illustration {
  width: min(380px, 85%);
  height: auto;
  filter: drop-shadow(0 20px 30px rgba(0, 0, 0, .35));
  transition: transform .4s ease;
}

.illustration:hover {
  transform: translateY(-4px);
}

/* ================= 右侧登录卡片区 ================= */
.right-section {
  flex: 4;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 32px;
  background-color: #fff;
  box-sizing: border-box;
}

.login-card {
  width: 100%;
  max-width: 380px;
}

.login-header {
  margin-bottom: 24px;
}

.welcome-title {
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 8px;
}

.welcome-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

/* 角色胶囊分段控制器 (淡青主题) */
.role-selector-wrapper {
  margin-bottom: 20px;
}

.role-selector-label {
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 6px;
}

.role-segmented {
  display: flex;
  background: #f0fdfa;
  padding: 4px;
  border-radius: 12px;
  border: 1px solid #ccfbf1;
}

.role-option {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  border-radius: 9px;
  cursor: pointer;
  transition: all .2s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
  -webkit-tap-highlight-color: transparent;
}

.role-option i {
  font-size: 15px;
}

.role-option.is-active {
  background: #fff;
  color: #0d9488;
  box-shadow: 0 2px 8px rgba(13, 148, 136, .18);
  border: 1px solid rgba(20, 184, 166, .3);
}

/* 表单输入框微动效 (淡青高亮) */
.custom-input >>> .el-input__inner {
  height: 44px;
  line-height: 44px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  background: #f8fafc;
  padding-left: 38px;
  font-size: 14px;
  transition: all .2s ease;
}

.custom-input >>> .el-input__inner:focus {
  border-color: #0d9488;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(20, 184, 166, .16);
}

.custom-input >>> .el-input__prefix {
  left: 10px;
  display: flex;
  align-items: center;
  color: #94a3b8;
}

.custom-input >>> .el-input__prefix i {
  font-size: 16px;
}

/* 登录按钮：淡青渐变 */
.form-actions-row {
  margin-top: 10px;
  margin-bottom: 18px;
}

.login-submit-btn {
  width: 100%;
  height: 46px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: .04em;
  background: linear-gradient(135deg, #14b8a6 0%, #0d9488 100%) !important;
  border: none !important;
  box-shadow: 0 4px 14px rgba(13, 148, 136, .35);
  transition: transform .2s ease, box-shadow .2s ease;
}

.login-submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(13, 148, 136, .45);
}

.login-submit-btn:active {
  transform: translateY(0);
}

/* 底部链接 */
.login-footer-links {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  gap: 12px;
  flex-wrap: wrap;
}

.register-link {
  color: #0d9488;
  font-weight: 600;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  transition: opacity .2s ease;
}

.register-link:hover {
  opacity: .8;
  text-decoration: underline;
}

.forget-link {
  color: #64748b;
  text-decoration: none;
  transition: color .2s ease;
}

.forget-link:hover {
  color: #0d9488;
  text-decoration: underline;
}

/* 弹窗美化 */
.dialog-tips {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 16px;
}

.forget-form >>> .el-input__inner {
  border-radius: 8px;
}

.dialog-confirm-btn {
  background: linear-gradient(135deg, #14b8a6 0%, #0d9488 100%) !important;
  border: none !important;
  box-shadow: 0 3px 10px rgba(13, 148, 136, .3);
}

/* ================= 移动端适配 (<= 768px) ================= */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    min-height: 100vh;
  }

  .left-section {
    flex: 0 0 auto;
    width: 100%;
    min-height: auto;
    padding: 32px 20px 36px;
    align-items: flex-start;
  }

  .brand-hero-wrapper {
    align-items: flex-start;
  }

  .brand-hero-content {
    margin-bottom: 0;
  }

  .brand-badge {
    margin-bottom: 12px;
    font-size: 11px;
  }

  .brand-title {
    margin-bottom: 8px;
    gap: 8px;
  }

  .brand-kanji {
    font-size: 32px;
  }

  .brand-english {
    font-size: 16px;
  }

  .brand-tagline {
    font-size: 16px;
    margin-bottom: 6px;
  }

  .brand-subtext,
  .brand-features,
  .illustration-wrapper {
    display: none;
  }

  .right-section {
    flex: 1 0 auto;
    width: 100%;
    padding: 32px 20px 40px;
    border-radius: 24px 24px 0 0;
    margin-top: -18px;
    z-index: 5;
    box-shadow: 0 -8px 24px rgba(15, 23, 42, .08);
  }

  .login-card {
    max-width: 100%;
  }

  .welcome-title {
    font-size: 24px;
  }

  .login-container >>> .el-dialog {
    width: 92% !important;
    max-width: 420px;
    margin: 20vh auto 0 !important;
    border-radius: 16px;
  }

  .login-container >>> .el-dialog__body {
    padding: 16px 18px;
  }

  .login-footer-links {
    flex-direction: column;
    align-items: center;
    gap: 14px;
    margin-top: 10px;
  }
}

@media (max-width: 380px) {
  .left-section {
    padding: 24px 16px 28px;
  }

  .brand-kanji {
    font-size: 28px;
  }

  .right-section {
    padding: 24px 16px 32px;
  }
}
</style>
