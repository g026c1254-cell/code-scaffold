<template>
  <div class="manager-person-wrapper">
    <el-card class="person-card" :body-style="{ padding: isMobile ? '16px 14px' : '24px 32px' }">
      <!-- 个人资料头部概览区 -->
      <div class="person-profile-header">
        <div class="avatar-container">
          <el-upload
            class="avatar-uploader"
            action=""
            :http-request="uploadAvatar"
            :show-file-list="false"
            accept="image/*"
            :before-upload="beforeAvatarUpload">
            <div class="avatar-wrapper">
              <img v-if="user.avatar" :src="getImageUrl(user.avatar)" class="avatar-img" @error="handleImageError" />
              <div v-else class="avatar-placeholder">
                <i class="el-icon-user-solid"></i>
              </div>
              <div class="avatar-edit-badge" title="写真を変更">
                <i class="el-icon-camera"></i>
              </div>
            </div>
          </el-upload>
        </div>
        <div class="profile-info-summary">
          <div class="profile-name">{{ user.name || user.username || '管理者' }}</div>
          <div class="profile-role-tag">
            <i class="el-icon-s-custom"></i>
            <span>{{ user.role === 'ADMIN' ? 'システム管理者' : '一般ユーザー' }}</span>
          </div>
        </div>
      </div>

      <!-- 表单编辑区 -->
      <el-form
        :model="user"
        :label-position="isMobile ? 'top' : 'right'"
        :label-width="isMobile ? 'auto' : '110px'"
        class="person-form">
        <el-form-item label="残高" prop="account">
          <div class="manager-balance-box">
            <span class="manager-balance-yen">¥</span>
            <span class="manager-balance-amount">{{ formatBalance(user.account) }}</span>
            <span class="manager-balance-unit">円</span>
          </div>
        </el-form-item>
        <el-form-item label="ユーザー名" prop="username">
          <el-input v-model="user.username" placeholder="ユーザー名" disabled prefix-icon="el-icon-user"></el-input>
        </el-form-item>
        <el-form-item label="氏名" prop="name">
          <el-input v-model="user.name" placeholder="氏名を入力してください" prefix-icon="el-icon-edit"></el-input>
        </el-form-item>
        <el-form-item label="電話番号" prop="phone">
          <el-input v-model="user.phone" placeholder="電話番号を入力してください" prefix-icon="el-icon-phone"></el-input>
        </el-form-item>
        <el-form-item label="メールアドレス" prop="email">
          <el-input v-model="user.email" placeholder="メールアドレスを入力してください" prefix-icon="el-icon-message"></el-input>
        </el-form-item>
        <el-form-item label="住所" prop="address">
          <el-input type="textarea" :rows="2" v-model="user.address" placeholder="住所を入力してください"></el-input>
        </el-form-item>
        <el-form-item label="性別" prop="sex">
          <el-radio-group v-model="user.sex">
            <el-radio label="男">男性</el-radio>
            <el-radio label="女">女性</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年齢" prop="age">
          <el-input v-model="user.age" placeholder="年齢を入力してください"></el-input>
        </el-form-item>
        <el-form-item label="自己紹介" prop="infos">
          <el-input type="textarea" :rows="3" v-model="user.infos" placeholder="自己紹介を入力してください"></el-input>
        </el-form-item>

        <div class="form-action-group">
          <el-button type="primary" class="save-btn" @click="update">保存する</el-button>
          <el-button class="password-btn" @click="formDetailVisible = true">パスワード変更</el-button>
          <el-button type="success" class="recharge-btn" icon="el-icon-wallet" @click="openRechargeDialog">チャージ</el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 密码修改抽屉 -->
    <el-drawer
      :visible.sync="formDetailVisible"
      :size="isMobile ? '100%' : '440px'"
      direction="rtl"
      :with-header="false"
      custom-class="password-change-drawer">
      <div class="drawer-header-bar">
        <div class="drawer-title-wrap">
          <i class="el-icon-lock"></i>
          <span>パスワード変更</span>
        </div>
        <el-button icon="el-icon-close" size="mini" circle @click="formDetailVisible = false" />
      </div>

      <div class="drawer-inner-content">
        <el-form ref="formRef" :model="user" :rules="rules" :label-position="isMobile ? 'top' : 'right'" :label-width="isMobile ? 'auto' : '120px'">
          <el-form-item label="ユーザー名" prop="username">
            <el-input v-model="user.username" placeholder="ユーザー名" disabled prefix-icon="el-icon-user"></el-input>
          </el-form-item>
          <el-form-item label="現在のパスワード" prop="password">
            <el-input show-password v-model="user.password" placeholder="現在のパスワード" prefix-icon="el-icon-key"></el-input>
          </el-form-item>
          <el-form-item label="新しいパスワード" prop="newPassword">
            <el-input show-password v-model="user.newPassword" placeholder="8文字以上（英数字を含む）" prefix-icon="el-icon-unlock"></el-input>
          </el-form-item>
          <el-form-item label="パスワード確認" prop="confirmPassword">
            <el-input show-password v-model="user.confirmPassword" placeholder="もう一度入力してください" prefix-icon="el-icon-check"></el-input>
          </el-form-item>
        </el-form>
      </div>

      <div class="drawer-footer-bar">
        <el-button type="primary" class="confirm-password-btn" @click="updatePassword">変更を確定</el-button>
        <el-button @click="formDetailVisible = false">キャンセル</el-button>
      </div>
    </el-drawer>

    <!-- 残高チャージダイアログ (Recharge Dialog) -->
    <el-dialog
      title="残高チャージ"
      :visible.sync="rechargeDialogVisible"
      width="420px"
      :close-on-click-modal="false"
      custom-class="recharge-dialog">
      <div class="recharge-dialog-body">
        <div class="recharge-balance-info">
          <span class="recharge-balance-label">現在の残高:</span>
          <span class="recharge-balance-value">¥{{ formatBalance(user.account) }} 円</span>
        </div>
        <el-form label-position="top">
          <el-form-item label="チャージ金額">
            <el-input-number
              v-model="rechargeAmount"
              :min="1"
              :max="10000000"
              :step="1000"
              :precision="0"
              controls-position="right"
              style="width: 100%;"
              placeholder="チャージする金額を入力してください">
            </el-input-number>
          </el-form-item>
          <div class="quick-recharge-row">
            <el-button size="mini" round plain @click="rechargeAmount = 1000">+1,000円</el-button>
            <el-button size="mini" round plain @click="rechargeAmount = 3000">+3,000円</el-button>
            <el-button size="mini" round plain @click="rechargeAmount = 5000">+5,000円</el-button>
            <el-button size="mini" round plain @click="rechargeAmount = 10000">+10,000円</el-button>
          </div>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rechargeDialogVisible = false">キャンセル</el-button>
        <el-button type="success" :loading="rechargeLoading" @click="submitRecharge">
          確定
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { createPixelAvatar, createPixelAvatarId } from '@/utils/pixelAvatar'

export default {
  name: "Person",
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('確認用パスワードを入力してください'));
      } else if (value !== this.user.newPassword) {
        callback(new Error('パスワードが一致しません'));
      } else {
        callback();
      }
    };
    const validateNewPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('新しいパスワードを入力してください'));
      } else if (value.length < 8) {
        callback(new Error('パスワードは8文字以上で入力してください'));
      } else if (!/[A-Za-z]/.test(value) || !/\d/.test(value)) {
        callback(new Error('パスワードには英字と数字を含めてください'));
      } else if (value === this.user.password) {
        callback(new Error('新しいパスワードは現在のパスワードと異なるものにしてください'));
      } else {
        callback();
      }
    };
    return {
      user: JSON.parse(localStorage.getItem("user") || '{}'),
      isMobile: typeof window !== 'undefined' ? window.innerWidth <= 768 : false,
      rechargeDialogVisible: false,
      rechargeAmount: 1000,
      rechargeLoading: false,
      rules: {
        password: [
          { required: true, message: '現在のパスワードを入力してください', trigger: 'blur' }
        ],
        newPassword: [
          { validator: validateNewPassword, required: true, trigger: 'blur' }
        ],
        confirmPassword: [
          { validator: validateConfirmPassword, required: true, trigger: 'blur' }
        ]
      },
      formDetailVisible: false
    }
  },
  created() {
    this.loadUser()
  },
  mounted() {
    this.handleResize = () => {
      this.isMobile = window.innerWidth <= 768
    }
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    if (this.handleResize) {
      window.removeEventListener('resize', this.handleResize)
    }
  },
  methods: {
    formatBalance(val) {
      if (val === null || val === undefined || isNaN(val)) return '0'
      return Number(val).toLocaleString()
    },
    openRechargeDialog() {
      this.rechargeAmount = 1000
      this.rechargeDialogVisible = true
    },
    submitRecharge() {
      const amount = Number(this.rechargeAmount)
      if (!amount || amount <= 0) {
        this.$notify.warning({ title: '注意', message: '有効なチャージ金額を入力してください', duration: 2000 })
        return
      }
      this.rechargeLoading = true
      this.$request.post('/user/recharge', {
        id: this.user.id,
        account: amount
      }).then(res => {
        if (res.code === '200') {
          this.$notify.success({ title: '完了', message: 'チャージが完了しました', duration: 2000 })
          this.rechargeDialogVisible = false
          this.loadUser()
        } else {
          this.fallbackRecharge(amount)
        }
      }).catch(() => {
        this.fallbackRecharge(amount)
      }).finally(() => {
        this.rechargeLoading = false
      })
    },
    fallbackRecharge(amount) {
      const current = Number(this.user.account || 0)
      const updatedAccount = Number((current + amount).toFixed(2))
      const updateData = Object.assign({}, this.user, { account: updatedAccount })
      this.$request.put('/user/update', updateData).then(res => {
        if (res.code === '200') {
          this.$notify.success({ title: '完了', message: 'チャージが完了しました', duration: 2000 })
          this.rechargeDialogVisible = false
          this.user.account = updatedAccount
          localStorage.setItem('user', JSON.stringify(this.user))
          this.$emit('update:user', this.user)
          this.loadUser()
        } else {
          this.$notify.error({ title: 'エラー', message: res.msg || 'チャージに失敗しました', duration: 2000 })
        }
      }).catch(() => {
        this.$notify.error({ title: 'エラー', message: 'チャージに失敗しました', duration: 2000 })
      })
    },
    loadUser() {
      if (!this.user.id) return
      this.$request.get('/user/selectById/' + this.user.id).then(res => {
        if (res.code === '200' && res.data) {
          const currentToken = this.user.token
          this.user = Object.assign({}, this.user, res.data, { token: currentToken })
          if (!this.user.avatar) {
            this.user.avatar = createPixelAvatarId(this.user.id)
            this.$request.put('/user/avatar', { avatar: this.user.avatar }).catch(() => {})
          }
          localStorage.setItem('user', JSON.stringify(this.user))
          this.$emit('update:user', this.user)
        }
      }).catch(() => {})
    },
    getImageUrl(url) {
      if (!url) return require('@/assets/empty.svg')
      if (String(url).indexOf('pixel:') === 0) {
        return createPixelAvatar(String(url).slice(6))
      }
      return url
    },
    handleImageError(event) {
      event.target.src = require('@/assets/empty.svg')
    },
    beforeAvatarUpload(file) {
      const isImage = /^image\//.test(file.type)
      if (!isImage) {
        this.$notify.error({ title: 'エラー', message: '画像ファイルを選択してください', duration: 2000 })
      }
      return isImage
    },
    uploadAvatar(options) {
      const formData = new FormData()
      formData.append('file', options.file)
      const token = this.user.token
      this.$request.post('/file/upload', formData, {
        headers: { token }
      }).then(res => {
        if (res.code !== '200' || !res.data) {
          throw new Error(res.msg || 'アップロードに失敗しました')
        }
        return this.$request.put('/user/avatar', { avatar: res.data })
      }).then(res => {
        if (res.code !== '200') {
          throw new Error(res.msg || '保存に失敗しました')
        }
        this.user.avatar = res.data || this.user.avatar
        localStorage.setItem('user', JSON.stringify(this.user))
        this.$emit('update:user', this.user)
        this.$notify.success({ title: '完了', message: 'アバターを更新しました', duration: 2000 })
        options.onSuccess(res)
      }).catch(error => {
        options.onError(error)
        this.$notify.error({ title: 'エラー', message: error.message || 'アップロードに失敗しました', duration: 2000 })
      })
    },
    update() {
      this.$request.put('/user/update', this.user).then(res => {
        if (res.code === '200') {
          this.$notify.success({ title: '完了', message: '保存しました', duration: 2000 })
          localStorage.setItem('user', JSON.stringify(this.user))
          this.$emit('update:user', this.user)
        } else {
          this.$notify.error({ title: 'エラー', message: res.msg, duration: 2000 })
        }
      }).catch(() => {
        this.$notify.error({ title: 'エラー', message: '更新に失敗しました', duration: 2000 })
      })
    },
    updatePassword() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request.post('/user/password', this.user).then(res => {
            if (res.code === '200') {
              this.$notify.success({ title: '完了', message: 'パスワードを変更しました。再度ログインしてください', duration: 2500 })
              this.formDetailVisible = false
              setTimeout(() => {
                localStorage.removeItem('user')
                this.$router.push('/login')
              }, 1200)
            } else {
              this.$notify.error({ title: 'エラー', message: res.msg, duration: 2000 })
            }
          }).catch(() => {
            this.$notify.error({ title: 'エラー', message: 'パスワード変更に失敗しました', duration: 2000 })
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.manager-person-wrapper {
  padding: 8px 4px 24px;
}

/* 主卡片：PC端最大宽度640px居中，移动端100% */
.person-card {
  max-width: 640px;
  margin: 0 auto;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid rgba(226, 232, 240, .8);
  box-shadow: 0 4px 16px rgba(15, 23, 42, .03);
}

/* 顶部管理员信息展示条 */
.person-profile-header {
  display: flex;
  align-items: center;
  gap: 18px;
  padding-bottom: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid #f1f5f9;
}

.avatar-container {
  flex-shrink: 0;
}

.avatar-wrapper {
  position: relative;
  width: 76px;
  height: 76px;
  border-radius: 50%;
  border: 2px solid #fed7aa;
  padding: 2px;
  background: #fff7ed;
  cursor: pointer;
  transition: all .2s ease;
}

.avatar-wrapper:hover {
  border-color: #ff8a3d;
  box-shadow: 0 0 0 3px rgba(255, 138, 61, .2);
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffedd5;
  color: #ea6b1f;
  font-size: 32px;
}

.avatar-edit-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 22px;
  height: 22px;
  background: #ff8a3d;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  border: 2px solid #ffffff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, .15);
}

.profile-info-summary {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.profile-name {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.profile-role-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 12px;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  font-size: 12px;
  color: #ea6b1f;
  font-weight: 600;
  width: fit-content;
}

/* 残高表示 */
.manager-balance-box {
  display: inline-flex;
  align-items: baseline;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
  padding: 6px 14px;
}

.manager-balance-yen {
  font-size: 16px;
  font-weight: 700;
  color: #16a34a;
  margin-right: 2px;
}

.manager-balance-amount {
  font-size: 20px;
  font-weight: 700;
  color: #15803d;
}

.manager-balance-unit {
  font-size: 13px;
  color: #16a34a;
  margin-left: 4px;
  font-weight: 600;
}

/* 表单与操作按钮 */
.person-form {
  margin-top: 10px;
}

.form-action-group {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 24px;
  flex-wrap: wrap;
}

.save-btn {
  flex: 1;
  min-width: 120px;
  max-width: 160px;
  background: linear-gradient(135deg, #ffa86b 0%, #ff7e29 100%) !important;
  border-color: transparent !important;
  box-shadow: 0 2px 8px rgba(255, 126, 41, .25);
  font-weight: 600;
  border-radius: 8px;
}

.password-btn {
  flex: 1;
  min-width: 120px;
  max-width: 160px;
  background: #fff7ed !important;
  border-color: #fed7aa !important;
  color: #ea6b1f !important;
  font-weight: 600;
  border-radius: 8px;
}

.password-btn:hover {
  background: #ffedd5 !important;
  border-color: #ff8a3d !important;
}

.recharge-btn {
  flex: 1;
  min-width: 120px;
  max-width: 160px;
  font-weight: 600;
  border-radius: 8px;
}

/* 抽屉样式 */
.drawer-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(226, 232, 240, .8);
  background: #faf8f5;
}

.drawer-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}

.drawer-title-wrap i {
  color: #ff8a3d;
  font-size: 17px;
}

.drawer-inner-content {
  padding: 20px;
  overflow-y: auto;
}

.drawer-footer-bar {
  display: flex;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid rgba(226, 232, 240, .8);
  background: #faf8f5;
  justify-content: flex-end;
}

.confirm-password-btn {
  background: linear-gradient(135deg, #ffa86b 0%, #ff7e29 100%) !important;
  border-color: transparent !important;
}

/* 充值弹窗样式 */
.recharge-dialog-body {
  padding: 8px 4px;
}

.recharge-balance-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: #f8fafc;
  border-radius: 8px;
  margin-bottom: 18px;
  border: 1px solid #e2e8f0;
}

.recharge-balance-label {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.recharge-balance-value {
  font-size: 16px;
  font-weight: 700;
  color: #15803d;
}

.quick-recharge-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

/* 移动端专属适配 */
@media (max-width: 768px) {
  .manager-person-wrapper {
    padding: 0;
  }

  .person-card {
    border-radius: 8px;
    border: none;
    box-shadow: none;
  }

  .person-profile-header {
    gap: 12px;
    padding-bottom: 14px;
    margin-bottom: 14px;
  }

  .avatar-wrapper {
    width: 68px;
    height: 68px;
  }

  .profile-name {
    font-size: 16px;
  }

  .form-action-group {
    flex-direction: column;
    gap: 8px;
  }

  .save-btn,
  .password-btn,
  .recharge-btn {
    width: 100%;
    max-width: none;
    margin: 0 !important;
  }

  .drawer-inner-content {
    padding: 14px;
  }

  .drawer-footer-bar {
    padding: 12px 14px;
  }

  .recharge-dialog >>> .el-dialog {
    width: 92% !important;
  }
}
</style>
