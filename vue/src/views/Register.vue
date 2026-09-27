<template>
  <div class="login-container">
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
          <p class="brand-subtext">不要になった教科書や家電を次の学友へ。かんたん1分で登録して、今日から売買を始めましょう！</p>

          <div class="brand-features">
            <div class="feature-tag"><i class="el-icon-circle-check"></i> 学生限定で安心</div>
            <div class="feature-tag"><i class="el-icon-refresh"></i> エコなリユース</div>
          </div>
        </div>
        <div class="illustration-wrapper">
          <img src="../assets/login.svg" alt="登録イラスト" class="illustration" />
        </div>
      </div>
    </div>

    <div class="right-section">
      <div class="login-card">
        <div class="login-header">
          <h2 class="welcome-title">新規登録</h2>
          <p class="welcome-subtitle">アカウントを作成して桑都安をはじめよう</p>
        </div>

        <el-form :model="user" :rules="rules" ref="registerRef" class="login-form" @keyup.enter.native="register">
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

          <el-form-item prop="confirmPass">
            <el-input
              v-model="user.confirmPass"
              class="custom-input"
              size="medium"
              type="password"
              placeholder="パスワードを再入力"
              prefix-icon="el-icon-lock"
              show-password
            ></el-input>
          </el-form-item>

          <el-form-item prop="role">
            <el-select v-model="user.role" class="custom-select" size="medium" placeholder="アカウント種別を選択" style="width: 100%">
              <el-option label="一般ユーザー" value="USER"></el-option>
            </el-select>
          </el-form-item>

          <div class="form-actions-row">
            <el-button type="primary" class="login-submit-btn" :loading="registering" @click="register">
              アカウント登録
            </el-button>
          </div>

          <div class="login-footer-links">
            <span class="has-account-text">既にアカウントをお持ちですか？</span>
            <router-link to="/login" class="register-link">
              ログインに戻る <i class="el-icon-arrow-right"></i>
            </router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { createPixelAvatarId } from '@/utils/pixelAvatar'

export default {
  name: 'Register',
  data() {
    const validatePassword = (rule, confirmPass, callback) => {
      if (confirmPass === '') {
        callback(new Error('パスワードを再入力してください'))
      } else if (confirmPass !== this.user.password) {
        callback(new Error('パスワードが一致しません'))
      } else {
        callback()
      }
    }
    return {
      registering: false,
      user: {
        username: '',
        password: '',
        confirmPass: '',
        role: 'USER'
      },
      rules: {
        username: [
          { required: true, message: 'ユーザー名を入力してください', trigger: 'blur' },
        ],
        password: [
          { required: true, message: 'パスワードを入力してください', trigger: 'blur' },
        ],
        confirmPass: [
          { validator: validatePassword, trigger: 'blur' }
        ],
        role: [
          { required: true, message: 'アカウント種別を選択してください', trigger: 'change' },
        ],
      }
    }
  },
  methods: {
    register() {
      this.$refs['registerRef'].validate((valid) => {
        if (!valid) return
        this.registering = true
        const user = Object.assign({}, this.user, {
          avatar: createPixelAvatarId()
        })
        this.$request.post('/register', user).then(res => {
          this.registering = false
          if (res.code === '200') {
            this.$router.push('/login')
            this.$notify.success({ title: '登録完了', message: 'アカウントを登録しました。ログインしてください', showClose: false, duration: 2500 })
          } else {
            this.$notify.error({ title: '登録エラー', message: res.msg || '登録に失敗しました', showClose: false, duration: 2500 })
          }
        }).catch(() => {
          this.registering = false
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

/* ================= 右侧卡片区 ================= */
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

.custom-input >>> .el-input__inner,
.custom-select >>> .el-input__inner {
  height: 44px;
  line-height: 44px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  background: #f8fafc;
  padding-left: 38px;
  font-size: 14px;
  transition: all .2s ease;
}

.custom-select >>> .el-input__inner {
  padding-left: 14px;
}

.custom-input >>> .el-input__inner:focus,
.custom-select >>> .el-input__inner:focus {
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

.login-footer-links {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  gap: 8px;
  flex-wrap: wrap;
}

.has-account-text {
  color: #64748b;
}

.register-link {
  color: #0d9488;
  font-weight: 600;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.register-link:hover {
  text-decoration: underline;
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

  .login-footer-links {
    flex-direction: column;
    align-items: center;
    gap: 12px;
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
