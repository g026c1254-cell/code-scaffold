<template>
  <div class="container">
    <div class="content">
      <el-card class="password-card">
        <div class="card-header">パスワード変更</div>
        <el-form ref="formRef" :model="user" :rules="rules" label-width="80px" class="password-form">
          <el-form-item label="現在のパスワード" prop="password">
            <el-input show-password v-model="user.password" placeholder="現在のパスワードを入力" clearable></el-input>
          </el-form-item>
          <el-form-item label="新しいパスワード" prop="newPassword">
            <el-input show-password v-model="user.newPassword" placeholder="8文字以上、英字と数字を含むパスワード" clearable></el-input>
          </el-form-item>
          <el-form-item label="パスワード確認" prop="confirmPassword">
            <el-input show-password v-model="user.confirmPassword" placeholder="新しいパスワードを再入力" clearable></el-input>
          </el-form-item>
          <div class="form-actions">
            <el-button type="warning" @click="resetForm">リセット</el-button>
            <el-button type="primary" @click="update">変更を確定</el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  name: "Password",
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
      user: {
        password: '',
        newPassword: '',
        confirmPassword: '',
        id: JSON.parse(localStorage.getItem("user")).id
      },
      rules: {
        password: [{ required: true, message: '現在のパスワードを入力してください', trigger: 'blur' }],
        newPassword: [{ validator: validateNewPassword, required: true, trigger: 'blur' }],
        confirmPassword: [{ validator: validateConfirmPassword, required: true, trigger: 'blur' }]
      }
    };
  },
  methods: {
    resetForm() {
      this.$refs.formRef.resetFields();
    },
    update() {
      console.log(this.user)
      this.$refs.formRef.validate(async (valid) => {
        if (valid) {
          try {
            const submitData = {
              id: this.user.id,
              newPassword: this.user.newPassword,
              password: this.user.password
            };

            const res = await this.$request.post('/user/password', submitData);
            if (res.code === '200') {
              this.$notify.warning({title: '完了', message: 'パスワードを変更しました。再度ログインしてください', showClose: false, duration: 2000});

              // 清除本地存储的用户信息，确保安全退出
              localStorage.removeItem('user');
              localStorage.removeItem('userId');
              // 延迟跳转，让用户看到成功提示
              setTimeout(() => {
                this.$router.push('/login');
              }, 1500);
            } else {
              this.$notify.error({message: res.msg || '変更に失敗しました。しばらくしてから再試行してください', showClose: false, duration: 2000});
            }
          } catch (error) {
            this.$notify.error({message:'ネットワークエラーが発生しました。しばらくしてから再試行してください', showClose: false, duration: 2000});
          }
        }
      });
    }
  }
};
</script>

<style scoped>
.container {
  min-height: 90vh;
  padding: 20px 20px;
  box-sizing: border-box;
}

.content {
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.password-card {
  width: 30%;
  max-width: 1000px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  overflow: hidden;
}

.card-header {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  padding: 10px 20px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fff;
}

.password-form {
  padding: 25px 20px;
  background-color: #fff;
}

/deep/.el-form-item__label {
  font-weight: 500;
  color: #606266;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding-right: 10px;
}
</style>
