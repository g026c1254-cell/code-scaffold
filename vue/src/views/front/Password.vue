<template>
  <div class="container">
    <div class="content">
      <el-card class="password-card">
        <div class="card-header">パスワード変更</div>
        <el-form ref="formRef" :model="user" :rules="rules" label-width="110px" class="password-form">
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
            <el-button type="warning" plain @click="resetForm">リセット</el-button>
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
        if (this.user.confirmPassword) {
          this.$refs.formRef.validateField('confirmPassword');
        }
        callback();
      }
    };
    return {
      user: {
        password: '',
        newPassword: '',
        confirmPassword: '',
        id: JSON.parse(localStorage.getItem("user") || '{}').id
      },
      rules: {
        password: [{ required: true, message: '現在のパスワードを入力してください', trigger: 'blur' }],
        newPassword: [{ validator: validateNewPassword, required: true, trigger: 'blur' }],
        confirmPassword: [{ validator: validateConfirmPassword, required: true, trigger: 'blur' }]
      }
    };
  },
  created() {
    const user = JSON.parse(localStorage.getItem("user") || '{}')
    if (!user.id) {
      this.$router.push('/login')
      return
    }
    this.user.id = user.id
  },
  methods: {
    resetForm() {
      if (this.$refs.formRef) {
        this.$refs.formRef.resetFields();
      }
    },
    update() {
      this.$refs.formRef.validate(async (valid) => {
        if (valid) {
          try {
            const submitData = {
              id: this.user.id || JSON.parse(localStorage.getItem("user") || '{}').id,
              newPassword: this.user.newPassword,
              password: this.user.password
            };

            const res = await this.$request.post('/user/password', submitData);
            if (res.code === '200') {
              this.$notify.success({title: '完了', message: 'パスワードを変更しました。再度ログインしてください', showClose: false, duration: 2000});

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
  padding: 24px 16px 40px;
  box-sizing: border-box;
}

.content {
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.password-card {
  width: min(520px, 100%);
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, .8);
  box-shadow: 0 10px 25px -5px rgba(15, 23, 42, .08);
  overflow: hidden;
}

.card-header {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fff;
}

.password-form {
  padding: 24px 20px 20px;
  background-color: #fff;
}

.password-form >>> .el-form-item__label {
  font-weight: 500;
  color: #475569;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 24px;
}

@media (max-width: 600px) {
  .container {
    padding: 14px 10px 28px;
    min-height: 0;
  }

  .password-form {
    padding: 16px 12px;
  }

  .password-form >>> .el-form-item {
    margin-bottom: 18px;
  }

  .password-form >>> .el-form-item__label {
    float: none;
    display: block;
    width: 100% !important;
    text-align: left;
    padding: 0 0 6px;
    line-height: 1.3;
    font-size: 13px;
  }

  .password-form >>> .el-form-item__content {
    margin-left: 0 !important;
  }

  .form-actions {
    flex-direction: column-reverse;
    gap: 8px;
  }

  .form-actions .el-button {
    width: 100%;
    margin-left: 0 !important;
  }
}
</style>
