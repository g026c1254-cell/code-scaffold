<template>
  <div>
    <!-- 表格内容 -->
    <el-card>
      <div style="margin-bottom: 10px">
        <el-input style="width: 200px" placeholder="ユーザー名を検索" v-model="username"></el-input>
        <el-input style="width: 200px; margin: 0 5px" placeholder="氏名を検索" v-model="name"></el-input>
        <el-button type="success" plain @click="load(1)">検索</el-button>
        <el-button type="info" plain @click="reset">リセット</el-button>
        <el-button type="primary" plain @click="handleAdd">追加</el-button>
      </div>
      <el-table :data="tableData" stripe>
        <el-table-column prop="id" label="番号" width="70" align="center">
          <template slot-scope='scope'>
            <span>{{ (pageNum - 1) * pageSize + (scope.$index + 1) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="ユーザー名"></el-table-column>
        <el-table-column prop="name" label="氏名"></el-table-column>
        <el-table-column prop="phone" label="電話番号"></el-table-column>
        <el-table-column prop="email" label="メールアドレス"></el-table-column>
        <el-table-column prop="address" label="住所"></el-table-column>
        <el-table-column prop="sex" label="性別"></el-table-column>
        <el-table-column prop="age" label="年齢"></el-table-column>
        <el-table-column prop="infos" label="自己紹介"></el-table-column>
        <el-table-column label="アイコン">
          <template v-slot="scope">
            <div style="display: flex; align-items: center">
              <el-image style="width: 50px; height: 50px; border-radius: 50%" v-if="scope.row.avatar"
                        :src="scope.row.avatar" :preview-src-list="[scope.row.avatar]"></el-image>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="240">
          <template v-slot="scope">
            <el-button size="mini" type="success" plain @click="detail(scope.row)">詳細</el-button>
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">編集</el-button>
            <el-button size="mini" type="danger" plain @click="del(scope.row.id)">削除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin: 10px 0">
        <el-pagination
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[2, 5, 10, 20]"
            :page-size="pageSize"
            layout="total, prev, pager, next"
            :total="total">
        </el-pagination>
      </div>
    </el-card>

    <!-- 新增 | 编辑弹框 -->
    <el-dialog title="情報" :visible.sync="fromVisible" width="30%">
      <el-form :model="form" label-width="80px" style="padding-right: 20px" :rules="rules" ref="formRef">
        <el-form-item label="ユーザー名" prop="username">
          <el-input v-model="form.username" placeholder="ユーザー名"></el-input>
        </el-form-item>
        <el-form-item label="氏名" prop="name">
          <el-input v-model="form.name" placeholder="氏名"></el-input>
        </el-form-item>
        <el-form-item label="電話番号" prop="phone">
          <el-input v-model="form.phone" placeholder="電話番号"></el-input>
        </el-form-item>
        <el-form-item label="メールアドレス" prop="email">
          <el-input v-model="form.email" placeholder="メールアドレス"></el-input>
        </el-form-item>
        <el-form-item label="住所" prop="address">
          <el-input type="textarea" v-model="form.address" placeholder="住所"></el-input>
        </el-form-item>
        <el-form-item label="アイコン">
          <el-upload
              class="avatar-uploader"
              :action="$baseUrl + '/file/upload'"
              :headers="{ token: user.token }"
              :file-list="form.avatar? [form.avatar] : []"
              list-type="picture"
              :on-success="handleAvatarSuccess">
            <el-button type="primary">アイコンをアップロード</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="性別" prop="sex">
          <el-radio v-model="form.sex" label="男">男性</el-radio>
          <el-radio v-model="form.sex" label="女">女性</el-radio>
        </el-form-item>
        <el-form-item label="年齢" prop="age">
          <el-input v-model="form.age" placeholder="年齢"></el-input>
        </el-form-item>
        <el-form-item label="自己紹介" prop="infos">
          <el-input type="textarea" v-model="form.infos" placeholder="自己紹介"></el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="fromVisible = false">キャンセル</el-button>
        <el-button type="primary" @click="save">確定</el-button>
      </div>
    </el-dialog>

    <!-- 详情内容 -->
    <el-drawer :visible.sync="formDetailVisible" title="詳細" :with-header="false">
      <div class="drawer-header">
        <span class="drawer-title">詳細</span>
        <div class="drawer-actions">
          <el-tooltip placement="top" :content="isFullscreen ? '全画面を終了' : '全画面'">
            <el-button icon="el-icon-full-screen" size="mini" circle @click="toggleFullscreen"/>
          </el-tooltip>
          <el-button icon="el-icon-close" size="mini" circle @click="formDetailVisible = false"/>
        </div>
      </div>

      <!-- 抽屉内容 -->
      <div class="drawer-content" ref="drawerContent">
        <el-form label-width="100px" style="padding-right: 40px" :model="form">
          <el-form-item label="ユーザー名" prop="username">
            <div>{{form.username}}</div>
          </el-form-item>
          <el-form-item label="氏名" prop="name">
            <div>{{form.name}}</div>
          </el-form-item>
          <el-form-item label="電話番号" prop="phone">
            <div>{{form.phone}}</div>
          </el-form-item>
          <el-form-item label="メールアドレス" prop="email">
            <div>{{form.email}}</div>
          </el-form-item>
          <el-form-item label="住所" prop="address">
            <div>{{form.address}}</div>
          </el-form-item>
          <el-form-item label="アイコン" prop="avatar">
            <div>
              <img v-if="user.avatar" :src="user.avatar" class="avatar" />
            </div>
          </el-form-item>
          <el-form-item label="性別" prop="sex">
            <div>{{form.sex}}</div>
          </el-form-item>
          <el-form-item label="年齢" prop="age">
            <div>{{form.age}}</div>
          </el-form-item>
          <el-form-item label="自己紹介" prop="infos">
            <div>{{form.infos}}</div>
          </el-form-item>
        </el-form>
      </div>

      <!-- 抽屉底部 -->
      <div class="drawer-footer">
        <el-button @click="formDetailVisible = false">閉じる</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script>
export default {
  name: "User",
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      username: '',
      name: '',
      total: 0,
      fromVisible: false,
      formDetailVisible: false,
      form: {},
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      rules: {
        username: [
          {required: true, message: 'ユーザー名を入力してください', trigger: 'blur'},
        ]
      },
      isFullscreen: false,
      drawerWidth: '50%',
      drawerPosition: 'right'
    }
  },
  created() {
    this.load()
  },
  methods: {
    del(id) {
      this.$confirm('削除してもよろしいですか？', '削除確認', {type: "warning"}).then(response => {
        this.$request.delete('/user/delete/' + id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$notify.success({title: '完了', message: '操作が完了しました', showClose: false, duration: 2000});
            this.load(1)
          } else {
            this.$notify.error({title: 'エラー', message: res.msg, showClose: false, duration: 2000});
          }
        })
      }).catch(() => {
      })
    },
    detail(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.formDetailVisible = true
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.fromVisible = true
    },
    handleAdd() {
      this.form = {role: 'USER'}
      this.fromVisible = true
    },
    save() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request({
            url: this.form.id ? '/user/update' : '/user/add',
            method: this.form.id ? 'PUT' : 'POST',
            data: this.form
          }).then(res => {
            if (res.code === '200') {
              this.$notify.success({title: '完了', message: '保存しました', showClose: false, duration: 2000});
              this.load(1)
              this.fromVisible = false
            } else {
              this.$notify.error({title: 'エラー', message: res.msg, showClose: false, duration: 2000});
            }
          })
        }
      })
    },
    reset() {
      this.name = ''
      this.username = ''
      this.load()
    },
    load(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/user/selectByPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          username: this.username,
          name: this.name,
          role: 'USER'
        }
      }).then(res => {
        this.tableData = res.data?.records
        this.total = res.data?.total
      })
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load()
    },
    handleAvatarSuccess(response, file, fileList) {
      this.form.avatar = response.data
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
.avatar {
  width: 78px;
  height: 78px;
  display: block;
  border-radius: 50%;
  object-fit: cover;
  transition: transform 0.3s ease;
}
</style>
