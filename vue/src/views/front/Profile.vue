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
            :before-upload="beforeAvatarUpload">
            <img v-if="user.avatar" :src="getImageUrl(user.avatar)" class="avatar" @error="handleImageError">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </div>

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
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { createPixelAvatar, createPixelAvatarId } from '@/utils/pixelAvatar'

export default {
  name: 'Profile',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}')
    }
  },
  created() {
    this.loadUser()
  },
  methods: {
    loadUser() {
      if (!this.user.id || !this.user.token) {
        this.$router.push('/login')
        return
      }
      const token = this.user.token
      this.$request.get('/user/selectById/' + this.user.id).then(res => {
        if (res.code === '200') {
          // 查询用户资料不会返回登录 Token，不能让接口响应覆盖本地认证信息。
          this.user = Object.assign({}, this.user, res.data, { token })
          if (!this.user.avatar) {
            this.user.avatar = createPixelAvatarId(this.user.id)
            this.saveAvatar(this.user.avatar)
          }
          localStorage.setItem('user', JSON.stringify(this.user))
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
  border-color: #ff6700;
  box-shadow: 0 0 0 4px rgba(255, 103, 0, .1);
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

.form-actions {
  margin-top: 24px;
  text-align: center;
}

.form-actions .el-button {
  min-width: 120px;
  margin: 0 6px 8px;
}
</style>
