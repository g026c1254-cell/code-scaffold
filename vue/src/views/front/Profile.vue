<template>
  <div class="profile-page">
    <el-card class="profile-card">
      <div class="card-header">{{ $t('common.personalInfo') }}</div>
      <el-form :model="user" label-width="100px" class="profile-form">
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            action=""
            :http-request="uploadAvatar"
            :show-file-list="false"
            accept="image/*"
            :before-upload="beforeAvatarUpload">
            <img v-if="user.avatar" :src="getImageUrl(user.avatar)" class="avatar" @error="handleImageError">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </div>

        <el-form-item :label="$t('common.balance')">
          <div class="balance-display-wrap">
            <span class="balance-currency-symbol">¥</span>
            <span class="balance-amount-text">{{ formatBalance(user.account) }}</span>
            <span class="balance-currency-unit">{{ $t('common.yen') }}</span>
          </div>
        </el-form-item>

        <el-form-item :label="$t('common.username')">
          <el-input v-model="user.username" disabled></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.name')">
          <el-input v-model="user.name"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.phone')">
          <el-input v-model="user.phone"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.email')">
          <el-input v-model="user.email"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.address')">
          <el-input type="textarea" v-model="user.address"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.gender')">
          <el-radio v-model="user.sex" label="男">{{ $t('common.male') }}</el-radio>
          <el-radio v-model="user.sex" label="女">{{ $t('common.female') }}</el-radio>
        </el-form-item>
        <el-form-item :label="$t('common.age')">
          <el-input v-model="user.age"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.introduction')">
          <el-input type="textarea" v-model="user.infos"></el-input>
        </el-form-item>

        <div class="form-actions">
          <el-button type="primary" @click="update">{{ $t('common.save') }}</el-button>
          <el-button type="warning" @click="$router.push('/front/password')">{{ $t('common.changePassword') }}</el-button>
          <el-button type="success" icon="el-icon-wallet" @click="openRechargeDialog">{{ $t('common.recharge') }}</el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 残高チャージダイアログ (Recharge Dialog) -->
    <el-dialog
      :title="$t('common.rechargeTitle')"
      :visible.sync="rechargeDialogVisible"
      width="420px"
      :close-on-click-modal="false"
      custom-class="recharge-dialog">
      <div class="recharge-dialog-body">
        <div class="recharge-balance-info">
          <span class="recharge-balance-label">{{ $t('common.balance') }}:</span>
          <span class="recharge-balance-value">¥{{ formatBalance(user.account) }} {{ $t('common.yen') }}</span>
        </div>
        <el-form label-position="top">
          <el-form-item :label="$t('common.rechargeAmount')">
            <el-input-number
              v-model="rechargeAmount"
              :min="1"
              :max="10000000"
              :step="1000"
              :precision="0"
              controls-position="right"
              style="width: 100%;"
              :placeholder="$t('common.rechargePrompt')">
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
        <el-button @click="rechargeDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="success" :loading="rechargeLoading" @click="submitRecharge">
          {{ $t('common.confirm') }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { createPixelAvatar, createPixelAvatarId } from '@/utils/pixelAvatar'

export default {
  name: 'Profile',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      rechargeDialogVisible: false,
      rechargeAmount: 1000,
      rechargeLoading: false
    }
  },
  created() {
    this.loadUser()
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
        this.$message.warning(this.$t('common.inputValidAmount'))
        return
      }
      this.rechargeLoading = true
      this.$request.post('/user/recharge', {
        id: this.user.id,
        account: amount
      }).then(res => {
        if (res.code === '200') {
          this.$message.success(this.$t('common.rechargeSuccess'))
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
          this.$message.success(this.$t('common.rechargeSuccess'))
          this.rechargeDialogVisible = false
          this.user.account = updatedAccount
          localStorage.setItem('user', JSON.stringify(this.user))
          this.$emit('update:user', this.user)
          this.loadUser()
        } else {
          this.$message.error(res.msg || this.$t('common.rechargeFailed'))
        }
      }).catch(() => {
        this.$message.error(this.$t('common.rechargeFailed'))
      })
    },
    loadUser() {
      if (!this.user.id || !this.user.token) {
        this.$router.push('/login')
        return
      }
      const token = this.user.token
      this.$request.get('/user/selectById/' + this.user.id).then(res => {
        if (res.code === '200') {
          this.user = Object.assign({}, this.user, res.data, { token })
          if (!this.user.avatar) {
            this.user.avatar = createPixelAvatarId(this.user.id)
            this.saveAvatar(this.user.avatar)
          }
          localStorage.setItem('user', JSON.stringify(this.user))
          this.$emit('update:user', this.user)
        } else {
          this.$message.error(res.msg || this.$t('common.loadFailed'))
        }
      })
    },
    update() {
      this.$request.put('/user/update', this.user).then(res => {
        if (res.code === '200') {
          localStorage.setItem('user', JSON.stringify(this.user))
          this.$emit('update:user', this.user)
          this.$message.success(this.$t('common.saveSuccess'))
        } else {
          this.$message.error(res.msg || this.$t('common.saveFailed'))
        }
      })
    },
    beforeAvatarUpload(file) {
      const isImage = /^image\//.test(file.type)
      if (!isImage) {
        this.$message.error(this.$t('common.imageOnly'))
      }
      return isImage
    },
    uploadAvatar(options) {
      const formData = new FormData()
      formData.append('file', options.file)
      const token = JSON.parse(localStorage.getItem('user') || '{}').token || this.user.token
      this.$request.post('/file/upload', formData, {
        headers: { token }
      }).then(res => {
        if (res.code !== '200' || !res.data) {
          throw new Error(res.msg || this.$t('common.imageUploadFailed'))
        }
        return this.$request.put('/user/avatar', { avatar: res.data })
      }).then(res => {
        if (res.code !== '200') {
          throw new Error(res.msg || this.$t('common.saveFailed'))
        }
        this.user.avatar = res.data || this.user.avatar
        localStorage.setItem('user', JSON.stringify(this.user))
        this.$emit('update:user', this.user)
        this.$message.success(this.$t('common.saveSuccess'))
        options.onSuccess(res)
      }).catch(error => {
        options.onError(error)
        this.$message.error(error.message || this.$t('common.imageUploadFailed'))
      })
    },
    saveAvatar(avatar) {
      this.$request.put('/user/avatar', { avatar }).catch(() => {})
    },
    getImageUrl(url) {
      if (!url) return require('@/assets/empty.svg')
      if (String(url).indexOf('pixel:') === 0) {
        return createPixelAvatar(String(url).slice(6))
      }
      if (/^data:/i.test(url)) return url
      if (/^https?:\/\//i.test(url)) {
        try {
          const imageUrl = new URL(url)
          if (imageUrl.pathname.indexOf('/file/download/') === 0) {
            return this.$baseUrl + imageUrl.pathname + imageUrl.search
          }
        } catch (e) {
          return require('@/assets/empty.svg')
        }
        return url
      }
      return this.$baseUrl + (url.startsWith('/') ? '' : '/') + url
    },
    handleImageError(event) {
      event.target.src = require('@/assets/empty.svg')
    }
  }
}
</script>

<style scoped>
.profile-page {
  display: flex;
  justify-content: center;
  min-height: 90vh;
  padding: 22px 12px 40px;
  box-sizing: border-box;
}

.profile-card {
  width: min(680px, 96%);
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, .8);
  box-shadow: 0 10px 25px -5px rgba(15, 23, 42, .08);
}

.card-header {
  padding: 4px 0 18px;
  border-bottom: 1px solid #f0f0f0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.profile-form {
  padding: 20px 10px 0;
}

.avatar-section {
  margin: 4px 0 24px;
  text-align: center;
}

.avatar-uploader >>> .el-upload {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 96px;
  height: 96px;
  overflow: hidden;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  transition: border-color .2s ease, box-shadow .2s ease;
}

.avatar-uploader >>> .el-upload:hover {
  border-color: #ff8a3d;
  box-shadow: 0 0 0 4px rgba(255, 138, 61, .15);
}

.avatar {
  display: block;
  width: 96px;
  height: 96px;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-uploader-icon {
  color: #909399;
  font-size: 28px;
}

.balance-display-wrap {
  display: inline-flex;
  align-items: baseline;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
  padding: 6px 14px;
}

.balance-currency-symbol {
  font-size: 16px;
  font-weight: 700;
  color: #16a34a;
  margin-right: 2px;
}

.balance-amount-text {
  font-size: 20px;
  font-weight: 700;
  color: #15803d;
}

.balance-currency-unit {
  font-size: 13px;
  color: #16a34a;
  margin-left: 4px;
  font-weight: 600;
}

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

.form-actions {
  margin-top: 24px;
  text-align: center;
}

.form-actions .el-button {
  min-width: 120px;
  margin: 0 6px 8px;
}

@media (max-width: 640px) {
  .profile-page {
    padding: 10px 8px 30px;
    min-height: 0;
  }

  .profile-form {
    padding: 12px 0 0;
  }

  .profile-form >>> .el-form-item__label {
    float: none;
    display: block;
    width: 100% !important;
    text-align: left;
    padding: 0 0 6px;
    line-height: 1.3;
    font-size: 13px;
  }

  .profile-form >>> .el-form-item__content {
    margin-left: 0 !important;
  }

  .form-actions {
    display: flex;
    flex-direction: column-reverse;
    gap: 8px;
  }

  .form-actions .el-button {
    width: 100%;
    margin: 0 !important;
  }

  .recharge-dialog >>> .el-dialog {
    width: 92% !important;
  }
}
</style>
