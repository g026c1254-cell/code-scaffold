<template>
  <div>
    <el-card style="width: 50%">
      <el-form :model="user" label-width="80px" style="padding-right: 20px">
        <div style="margin: 15px; text-align: center">
          <el-upload
              class="avatar-uploader"
              action="http://localhost:9999/file/upload"
              :headers="{ token: user.token }"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
          >
            <img v-if="user.avatar" :src="user.avatar" class="avatar" />
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </div>
        <el-form-item label="ユーザー名" prop="username">
          <el-input v-model="user.username" placeholder="ユーザー名" disabled></el-input>
        </el-form-item>
        <el-form-item label="氏名" prop="name">
          <el-input v-model="user.name" placeholder="氏名"></el-input>
        </el-form-item>
        <el-form-item label="電話番号" prop="phone">
          <el-input v-model="user.phone" placeholder="電話番号"></el-input>
        </el-form-item>
        <el-form-item label="メールアドレス" prop="email">
          <el-input v-model="user.email" placeholder="メールアドレス"></el-input>
        </el-form-item>
        <el-form-item label="住所" prop="address">
          <el-input type="textarea" v-model="user.address" placeholder="住所"></el-input>
        </el-form-item>
        <el-form-item label="性別" prop="sex">
          <el-radio v-model="user.sex" label="男">男性</el-radio>
          <el-radio v-model="user.sex" label="女">女性</el-radio>
        </el-form-item>
        <el-form-item label="年齢" prop="age">
          <el-input v-model="user.age" placeholder="年齢"></el-input>
        </el-form-item>
        <el-form-item label="自己紹介" prop="infos">
          <el-input type="textarea" v-model="user.infos" placeholder="自己紹介"></el-input>
        </el-form-item>
        <div style="text-align: center; margin-bottom: 20px">
          <el-button type="primary" @click="update">保存</el-button>
          <el-button type="success" @click="formDetailVisible = true">パスワード変更</el-button>
        </div>
      </el-form>
    </el-card>

    <el-drawer :visible.sync="formDetailVisible" title="パスワード変更" :with-header="false">
      <div class="drawer-header">
        <span class="drawer-title">パスワード変更</span>
        <div class="drawer-actions">
          <el-tooltip placement="top" :content="isFullscreen ? '全画面を終了' : '全画面'">
            <el-button icon="el-icon-full-screen" size="mini" circle @click="toggleFullscreen"/>
          </el-tooltip>
          <el-button icon="el-icon-close" size="mini" circle @click="formDetailVisible = false"/>
        </div>
      </div>

      <div class="drawer-content" ref="drawerContent">
        <el-form ref="formRef" :model="user" :rules="rules" label-width="80px" style="padding-right: 20px">
          <el-form-item label="ユーザー名" prop="username">
            <el-input  v-model="user.username" placeholder="ユーザー名" disabled></el-input>
          </el-form-item>
          <el-form-item label="現在のパスワード" prop="password">
            <el-input show-password v-model="user.password" placeholder="現在のパスワード"></el-input>
          </el-form-item>
          <el-form-item label="新しいパスワード" prop="newPassword">
            <el-input show-password v-model="user.newPassword" placeholder="新しいパスワード"></el-input>
          </el-form-item>
          <el-form-item label="パスワード確認" prop="confirmPassword">
            <el-input show-password v-model="user.confirmPassword" placeholder="パスワード確認"></el-input>
          </el-form-item>
        </el-form>
      </div>

      <div class="drawer-footer">
        <el-button type="primary" @click="updatePassword">変更を確定</el-button>
        <el-button @click="formDetailVisible = false">閉じる</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script>
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
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
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
      formDetailVisible: false,
      isFullscreen: false,
      drawerWidth: '50%',
      drawerPosition: 'right'
    }
  },
  created() {},
  methods: {
    updatePassword() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request.post('/user/password', this.user).then(res => {
            if (res.code === '200') {
              this.$notify.success({title: '完了', message: '保存しました', showClose: false, duration: 2000});
              this.$router.push('/login')
            } else {
              this.$notify.error({title: 'エラー', message: res.msg, showClose: false, duration: 2000});
            }
          })
        }
      })
    },
    update() {
      this.$request.put('/user/update', this.user).then(res => {
        if (res.code === '200') {
          this.$notify.success({title: '完了', message: '保存しました', showClose: false, duration: 2000});
          localStorage.setItem('user', JSON.stringify(this.user))
          // 触发父级的数据更新
          this.$emit('update:user', this.user)
        } else {
          this.$notify.error({title: 'エラー', message: res.msg, showClose: false, duration: 2000});
        }
      })
    },
    handleAvatarSuccess(response, file, fileList) {
      this.user.avatar = response.data
    },
    handleFullscreenChange() {
      if (!document.fullscreenElement && this.isFullscreen) {
        this.isFullscreen = false;
        this.drawerWidth = this.originalWidth;
        this.drawerPosition = this.originalPosition;
      }
    },
    toggleFullscreen() {
      if (!this.isFullscreen) {
        this.originalWidth = this.drawerWidth;
        this.originalPosition = this.drawerPosition;
        this.isFullscreen = true;
        this.drawerWidth = '100%';
        this.drawerPosition = 'bottom';
        this.$nextTick(() => {
          this.requestFullscreen(this.$refs.drawerContent);
        });
      } else {
        this.isFullscreen = false;
        this.drawerWidth = this.originalWidth;
        this.drawerPosition = this.originalPosition;
        this.exitFullscreen();
      }
    },
    requestFullscreen(element) {
      if (element.requestFullscreen) {
        element.requestFullscreen();
      } else if (element.webkitRequestFullscreen) {
        element.webkitRequestFullscreen();
      } else if (element.mozRequestFullScreen) {
        element.mozRequestFullScreen();
      } else if (element.msRequestFullscreen) {
        element.msRequestFullscreen();
      }
    },
    exitFullscreen() {
      if (document.exitFullscreen) {
        document.exitFullscreen();
      } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
      } else if (document.mozCancelFullScreen) {
        document.mozCancelFullScreen();
      } else if (document.msExitFullscreen) {
        document.msExitFullscreen();
      }
    }
  },
  mounted() {
    document.addEventListener('fullscreenchange', this.handleFullscreenChange);
    document.addEventListener('webkitfullscreenchange', this.handleFullscreenChange);
    document.addEventListener('mozfullscreenchange', this.handleFullscreenChange);
    document.addEventListener('MSFullscreenChange', this.handleFullscreenChange);
  },
  beforeDestroy() {
    document.removeEventListener('fullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('webkitfullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('mozfullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('MSFullscreenChange', this.handleFullscreenChange);
  },
}
</script>

<style scoped>
/deep/.el-form-item__label {
  font-weight: bold;
}
/deep/.el-upload {
  border-radius: 50%;
}
/deep/.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  border-radius: 50%;
}
/deep/.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
  border-radius: 50%;
}
.avatar {
  width: 78px;
  height: 78px;
  display: block;
  border-radius: 50%;
}
</style>
