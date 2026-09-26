<template>
  <div class="login-container">
    <div class="left-section">
      <h1 class="title">桑都安 - SOUTOYASU -</h1>
      <p class="description">Spring Boot + Vue + MyBatis Plusで構築されたC2Cリユースプラットフォーム。</p>
      <p class="descr">学生同士で安心して商品を売買できるサービスです。</p>
      <img src="../assets/login.svg" alt="ログインイラスト" class="illustration" />
    </div>
    <div class="right-section">
      <h1 class="welcome-title">おかえりなさい</h1>
      <div class="login-type-wrapper">
        <p class="login-type">アカウントでログイン</p>
      </div>
      <el-form :model="user" :rules="rules" ref="loginRef" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="user.username" size="medium" placeholder="ユーザー名を入力" prefix-icon="el-icon-user"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="user.password" size="medium" type="password" placeholder="パスワードを入力" prefix-icon="el-icon-lock" show-password></el-input>
        </el-form-item>
        <el-form-item prop="role">
          <el-select v-model="user.role" placeholder="アカウント種別を選択" style="width: 100%">
            <el-option label="管理者" value="ADMIN"></el-option>
            <el-option label="一般ユーザー" value="USER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" @click="login">ログイン</el-button>
        </el-form-item>

        <div class="links">
          <div><a href="/register">新規アカウント登録</a></div>
          <div><a href="/login" @click.prevent="handleForgetPass">パスワードをお忘れですか？</a></div>
        </div>
      </el-form>
    </div>

    <el-dialog title="パスワードの再設定" :visible.sync="forgetPassDialogVis" width="30%">
      <el-form :model="forgetUserForm" label-width="80px">
        <el-form-item label="ユーザー名">
          <el-input v-model="forgetUserForm.username" autocomplete="off" placeholder="ユーザー名を入力"></el-input>
        </el-form-item>
        <el-form-item label="電話番号">
          <el-input v-model="forgetUserForm.phone" autocomplete="off" placeholder="電話番号を入力"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="forgetPassDialogVis = false">キャンセル</el-button>
        <el-button type="primary" @click="resetPassword">確定</el-button>
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
      user: {
        username: '',
        password: '',
        role: ''
      },
      rules: {
        username: [
          { required: true, message: 'ユーザー名を入力', trigger: 'blur' },
        ],
        password: [
          { required: true, message: 'パスワードを入力', trigger: 'blur' },
        ],
        role: [
          { required: true, message: 'アカウント種別を選択', trigger: 'change' },
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
      this.$request.put('/password', this.forgetUserForm).then(res => {
        if (res.code === '200') {
          this.$message.success('パスワードをリセットしました')
          this.forgetPassDialogVis = false
        } else {
          this.$notify.error({title: 'エラー', message: res.msg, showClose: false, duration: 2000});
        }
      })
    },
    login() {
      this.$refs['loginRef'].validate((valid) => {
        if (valid) {
          this.$request.post('/login', this.user).then(res => {
            if (res.code === '200') {
              const generatedAvatar = !res.data.avatar
              if (!res.data.avatar) {
                res.data.avatar = createPixelAvatarId(res.data.id)
              }
              localStorage.setItem("user", JSON.stringify(res.data))
              if (generatedAvatar) {
                this.$request.put('/user/avatar', { avatar: res.data.avatar }).catch(() => {})
              }
              if (this.user.role == 'ADMIN'){
                this.$router.push('/')
              } else {
                this.$router.push('/front/home')
              }
              this.$notify.success({title: '完了', message: 'ログインしました', showClose: false, duration: 2000});
            } else {
              this.$notify.error({title: 'エラー', message: res.msg, showClose: false, duration: 2000});
            }
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  height: 100vh;
}

.left-section {
  flex: 6;
  background-color: #1f2937;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.title {
  font-size:40px;
  font-weight: bold;
  margin-bottom: 20px;
}

.description {
  font-size: 20px;
  margin-bottom: 20px;
  text-align: center;
}

.descr {
  font-size: 24px;
  margin-bottom: 20px;
  text-align: center;
}

.illustration {
  width: 400px;
  height: auto;
  border-radius: 0;
}

.right-section {
  flex: 4;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background-color: #fff;
}

.welcome-title {
  font-size: 26px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #1e293b;
}

.login-type-wrapper {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  width: 200px;
}

.login-type-wrapper::before {
  content: '';
  flex: 1;
  height: 1px;
  background-color: #e2e8f0;
  margin-right: 10px;
}

.login-type-wrapper::after {
  content: '';
  flex: 1;
  height: 1px;
  background-color: #e2e8f0;
  margin-left: 10px;
}

.login-type {
  font-size: 13px;
  color: #64748b;
  white-space: nowrap;
}

.login-form {
  width: 350px;
}

.login-btn {
  width: 100%;
  height: 40px;
  font-size: 14px;
}

.links {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  margin: 20px 0;
  font-size: 14px;
  color: #409eff;
}

.links a {
  text-decoration: none;
  color: #409eff;
}

.links a:hover {
  text-decoration: underline;
}
</style>
